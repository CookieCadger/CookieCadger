package com.cookiecadger;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;


public class CaptureHandler
{
	public ArrayList<Boolean> bCapturing = new ArrayList<Boolean>();
	public ArrayList<String> deviceName = new ArrayList<String>();
	public ArrayList<String> deviceDescription = new ArrayList<String>();
	public ArrayList<Process> deviceCaptureProcess = new ArrayList<Process>();
	public ArrayList<String> sessionDetectors = new ArrayList<String>();
	
	public CaptureHandler()
	{
		loadPlugins();
	}
	
	public void startCapture(int ethDevNumber, String pcapFile) throws IOException
	{
		ProcessBuilder pb = null;
		Process proc = null;
		ProcessWatcher pw = null;
		InputStream is = null;
		
		Date capStart = new Date();
		ArrayList<String> nonTabbedOutput = new ArrayList<String>();
		
		if(pcapFile == null || pcapFile.isEmpty())
		{			
			Utils.consoleMessage("Opening '" + deviceName.get(ethDevNumber) + "' for traffic capture.");
			pb = new ProcessBuilder(new String[] { (String) Utils.programSettings.get("tsharkPath"), "-i", deviceName.get(ethDevNumber), "-f", "tcp dst port 80 or udp src port 5353 or udp src port 138", "-T", "fields", "-e", "eth.src", "-e", "wlan.sa", "-e", "ip.src", "-e", "ipv6.src", "-e", "tcp.srcport", "-e", "tcp.dstport", "-e", "udp.srcport", "-e", "udp.dstport", "-e", "browser.command", "-e", "browser.server", "-e", "dns.resp.name", "-e", "http.host", "-e", "http.request.uri", "-e", "http.accept", "-e", "http.accept_encoding", "-e", "http.user_agent", "-e", "http.referer", "-e", "http.cookie", "-e", "http.authorization", "-e", "http.authbasic" } );
			//pb.redirectErrorStream(true); // Wireshark 1.10.0 adds garbage to error stream, not usable anymore
			deviceCaptureProcess.set(ethDevNumber, pb.start());
			pw = new ProcessWatcher(deviceCaptureProcess.get(ethDevNumber));
			is = deviceCaptureProcess.get(ethDevNumber).getInputStream();
		}
		else
		{
			Utils.consoleMessage("Opening '" + pcapFile + "' for traffic capture.");
			pb = new ProcessBuilder(new String[] { (String) Utils.programSettings.get("tsharkPath"), "-r", pcapFile, "-T", "fields", "-e", "eth.src", "-e", "wlan.sa", "-e", "ip.src", "-e", "ipv6.src", "-e", "tcp.srcport", "-e", "tcp.dstport", "-e", "udp.srcport", "-e", "udp.dstport", "-e", "browser.command", "-e", "browser.server", "-e", "dns.resp.name", "-e", "http.host", "-e", "http.request.uri", "-e", "http.accept", "-e", "http.accept_encoding", "-e", "http.user_agent", "-e", "http.referer", "-e", "http.cookie", "-e", "http.authorization", "-e", "http.authbasic" } );
			//pb.redirectErrorStream(true); // Wireshark 1.10.0 adds garbage to error stream, not usable anymore
			proc = pb.start();
			pw = new ProcessWatcher(proc);
			is = proc.getInputStream();
		}
		
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);

		String line = null;
		try
		{
			while ((line = br.readLine()) != null || pw.isFinished()) // Do while you have data to read, or if the process finishes start the loop to ensure all buffer has read, then exit
			{
				if(line == null) // Can happen while loading in and the proc finishes faster than readin
				{
					break;
				}

				if(line.contains("\t"))
				{
					String[] values = line.split("\t", -1); // -1 limit means don't ignore whitespace

					if(values.length > 4) // If only contains four or less values, likely continuation data, ignore.
					{		
						String macAddressWired = values[0];
						String macAddressWireless = values[1];
						
						String macAddress;
						if(!macAddressWired.isEmpty())
							macAddress = macAddressWired;
						else if(!macAddressWireless.isEmpty())
							macAddress = macAddressWireless;
						else // No MAC would hopefully never actually happen, but you never know
							macAddress = "Unknown";
						
						String ipv4Address = values[2];
						String ipv6Address = values[3];
						//String tcpSource = values[4]; //Unused
						String tcpDestination = values[5];
						String udpSource = values[6];
						//String udpDestination = values[7]; //Unused
						String netbiosCommand = values[8];
						String netbiosName = values[9];
						String mdnsName = values[10];
						String requestHost = values[11];
						String requestUri = values[12];
						String accept = values[13];
						String acceptEncoding = values[14];
						String userAgent = values[15];
						String refererUri = values[16];
						String cookieData = values[17];
						String authorization = values[18];
						String authBasic = values[19];
			
						boolean bUsefulData = false;
						int clientID = -1;
						
						// Poor man's implementation of a packet filter for when pcaps are loaded
						if(pcapFile != null && !pcapFile.isEmpty() && (!tcpDestination.equals("80") && !udpSource.equals("5353") && !udpSource.equals("138")))
						{
							continue;
						}
						
						// When Cookie Cadger creates requests it appends a randomization
						// to the Accept header. Check for it and ignore if matched.
						if(accept.contains(";" + Integer.toString(Utils.getLocalRandomization())))
						{
							continue;
						}

						if(!requestUri.isEmpty())
						{
							bUsefulData = true;
							clientID = handleClient(macAddress);

							processRequest(macAddress, ipv4Address, ipv6Address, requestHost, accept, acceptEncoding, requestUri, userAgent, refererUri, cookieData, authorization, authBasic);
						}
						else if(!netbiosCommand.isEmpty() && netbiosCommand.equals("0x0f") && !netbiosName.isEmpty()) // 0x0f = host announcement broadcast
						{
							bUsefulData = true;
							clientID = handleClient(macAddress);
							
							Utils.dbInstance.setStringValue("clients", "netbios_hostname", netbiosName, "mac_address", macAddress);
						}
						else if(!mdnsName.isEmpty())
						{
							if(mdnsName.contains(","))
							{
								String[] mdnsResponses = mdnsName.split(",");
								String mdnsNameStr = mdnsResponses[mdnsResponses.length - 1];
								
								if(!mdnsNameStr.contains(".arpa") && !mdnsNameStr.contains("_tcp") && !mdnsNameStr.contains("_udp") && !mdnsNameStr.contains("<Root>"))
								{
									bUsefulData = true;
									clientID = handleClient(macAddress);
									
									mdnsNameStr = mdnsNameStr.replace(".local", "");

									Utils.dbInstance.setStringValue("clients", "mdns_hostname", mdnsNameStr, "mac_address", macAddress);
								}
							}
						}
						
						if(bUsefulData)
						{
							if(!ipv4Address.isEmpty())
								Utils.dbInstance.setStringValue("clients", "ipv4_address", ipv4Address, "mac_address", macAddress);
							
							if(!ipv6Address.isEmpty())
								Utils.dbInstance.setStringValue("clients", "ipv6_address", ipv6Address, "mac_address", macAddress);
						}
						
						if(Utils.cookieCadgerFrame != null)
						{
							// And update the informational display
							Utils.cookieCadgerFrame.updateDescriptionForMac(macAddress);
						}
					}
				}
				else
				{
					nonTabbedOutput.add(line);
				}
			}
		}
		catch (IOException ioe)
		{
			// Do nothing
		}
		catch (Exception e)
		{
			e.printStackTrace();

			if(pcapFile != null && !pcapFile.isEmpty() && proc != null)
			{
				proc.destroy();
			}
			else
			{
				if(deviceCaptureProcess.get(ethDevNumber) != null)
					deviceCaptureProcess.get(ethDevNumber).destroy();
			}
		}
		finally
		{
			if(br != null)
				br.close();
			if(isr != null)
				isr.close();
			if(is != null)
				is.close();
		}
		
		if(pcapFile != null && pcapFile.isEmpty() && Utils.cookieCadgerFrame != null)
			Utils.cookieCadgerFrame.stopCapture(ethDevNumber); // Update UI and clean up
		
		Date capEnd = new Date();
		int runTimeInSeconds = (int) ((capEnd.getTime() - capStart.getTime()) / 1000);
		
		if((pcapFile == null || pcapFile.isEmpty()) && runTimeInSeconds <= 20)
		{
			Utils.consoleMessage(deviceName.get(ethDevNumber) + ": " + "============================================================================");
			Utils.consoleMessage(deviceName.get(ethDevNumber) + ": " + "Start of diagnostic information for interface '" + deviceName.get(ethDevNumber) + "'");
			Utils.consoleMessage(deviceName.get(ethDevNumber) + ": " + "============================================================================");
			
			for (String output : nonTabbedOutput)
			{
				Utils.consoleMessage(deviceName.get(ethDevNumber) + ": " + output);				
			}
			
			Utils.consoleMessage(deviceName.get(ethDevNumber) + ": " + "============================================================================");
			Utils.consoleMessage(deviceName.get(ethDevNumber) + ": " + "Potential error detected! Capture stopped / died in " + Integer.toString(runTimeInSeconds) + " seconds.");
			Utils.consoleMessage(deviceName.get(ethDevNumber) + ": " + "All messages from the 'tshark' program have been printed above to assist you in solving any errors.");
			Utils.consoleMessage(deviceName.get(ethDevNumber) + ": " + "============================================================================");
		}
		else
		{
			if(pcapFile == null || pcapFile.isEmpty())
				Utils.consoleMessage("'" + deviceName.get(ethDevNumber) + "' has been closed and is finished with traffic capture. Capture duration: " + Integer.toString(runTimeInSeconds) + " seconds.");
			else
				Utils.consoleMessage("'" + pcapFile + "' has finished processing. Processing duration: " + Integer.toString(runTimeInSeconds) + " seconds.");
		}
	}
	
	private void processRequest(final String macAddress, final String ipv4Address, final String ipv6Address, final String requestHost, final String accept, final String acceptEncoding, final String requestUri, final String userAgent, final String refererUri, final String cookieData, final String authorization, final String authBasic)
	{
		int clientID = -1;
		int domainID = -1;
		int reqID = -1;
		final int requestID;
	
		try
		{
			if(Utils.dbInstance.containsValue("clients", "mac_address", macAddress))
			{
				// We've seen this mac already, just get the ClientID
				clientID = Utils.dbInstance.getIntegerValue("clients", "id", "mac_address", macAddress); // In 'clients' get id where mac == macAddrSource
				
				if(Utils.cookieCadgerFrame != null)
				{
					// We're going to have activity in a previously identified host, highlight
					Utils.cookieCadgerFrame.clientsList.performHighlight(macAddress, Color.BLUE);
				}
			}
			else // Client object doesn't exist for MAC? Create one.
			{
				clientID = Utils.dbInstance.createClient(macAddress);
			}
			
			// Tell the client that a request was seen
			Utils.dbInstance.setStringValue("clients", "has_http_requests", Integer.toString(1), "id", Integer.toString(clientID));
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		try
		{
			if(Utils.dbInstance.containsValue("domains", "name", requestHost))
			{
				// We've seen this domain already, just get the DomainID
				domainID = Utils.dbInstance.getIntegerValue("domains", "id", "name", requestHost);
			}
			else // Domain object doesn't exist for this name? Create one.
			{
				// Create new domain
				domainID = Utils.dbInstance.createDomain(requestHost);
				
				if(Utils.cookieCadgerFrame != null)
				{
					// And display it, if appropriate to do so
					if(!Utils.cookieCadgerFrame.clientsList.isSelectionEmpty() && ((EnhancedJListItem)Utils.cookieCadgerFrame.clientsList.getSelectedValue()).toString().equals(macAddress) &&
							(Utils.cookieCadgerFrame.txtDomainSearch.getText().isEmpty() || (Utils.cookieCadgerFrame.txtDomainSearch.getText().length() > 0 && requestHost.toLowerCase().contains(Utils.cookieCadgerFrame.txtDomainSearch.getText().toLowerCase())))
							)
					{
						Utils.cookieCadgerFrame.domainsListModel.addElement(new EnhancedJListItem(domainID, requestHost, null));
					}
				}
			}
			
			if(Utils.cookieCadgerFrame != null)
			{
				// And highlight it for activity
				Utils.cookieCadgerFrame.domainsList.performHighlight(requestHost, Color.BLUE);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
				
		try
		{
			reqID = Utils.dbInstance.createRequest(requestUri, userAgent, refererUri, cookieData, authorization, authBasic, domainID, clientID);
			
			// Generate hover tooltip text and save it back to the database.
			// This allows us to only have to do this heavy processing once.
			String description = Utils.generateDescriptionForRequest(reqID, true, true);
			Utils.dbInstance.setStringValue("requests", "description", description, "id", Integer.toString(reqID));
			
			if(Utils.cookieCadgerFrame != null)
			{				
				// Update the requests list if necessary
				if(!Utils.cookieCadgerFrame.clientsList.isSelectionEmpty() && ((EnhancedJListItem)Utils.cookieCadgerFrame.clientsList.getSelectedValue()).toString().equals(macAddress) && !Utils.cookieCadgerFrame.domainsList.isSelectionEmpty() && (((EnhancedJListItem)Utils.cookieCadgerFrame.domainsList.getSelectedValue()).toString().equals(requestHost) || (((EnhancedJListItem)Utils.cookieCadgerFrame.domainsList.getSelectedValue()).toString().equals("[ All Domains ]"))) &&
						(Utils.cookieCadgerFrame.txtRequestSearch.getText().isEmpty() || (Utils.cookieCadgerFrame.txtRequestSearch.getText().length() > 0 && requestUri.toLowerCase().contains(Utils.cookieCadgerFrame.txtRequestSearch.getText().toLowerCase()))))
				{
					Date now = new Date();	
					String dateString = DateFormat.getTimeInstance(DateFormat.MEDIUM).format(now);
					
					// If showing all domains in the request list, make sure the full request Url gets displayed
					boolean bShowAllDomains = false;
					if(!Utils.cookieCadgerFrame.domainsList.isSelectionEmpty() && ((EnhancedJListItem)Utils.cookieCadgerFrame.domainsList.getSelectedValue()).toString().equals("[ All Domains ]"))
					{
						bShowAllDomains = true;
					}
									
					String requestDisplay = null;
					if(bShowAllDomains)
					{
						requestDisplay = requestHost + requestUri;
					}
					else
					{
						requestDisplay = requestUri;
					}
					
					EnhancedJListItem requestItem = new EnhancedJListItem(reqID, dateString + ": " + requestDisplay, description);
					Utils.cookieCadgerFrame.requestsListModel.addElement(requestItem);
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		if(Utils.cookieCadgerFrame != null)
		{
			if(!Utils.cookieCadgerFrame.clientsListModel.contains(macAddress) &&
					(Utils.cookieCadgerFrame.txtClientSearch.getText().isEmpty() || (Utils.cookieCadgerFrame.txtClientSearch.getText().length() > 0 && macAddress.contains(Utils.cookieCadgerFrame.txtClientSearch.getText()))))
			{
				Utils.cookieCadgerFrame.clientsListModel.addElement(new EnhancedJListItem(clientID, macAddress, null));
				Utils.cookieCadgerFrame.clientsList.performHighlight(macAddress, Color.BLUE);
			}
	
			Utils.cookieCadgerFrame.updateDescriptionForMac(macAddress);
		}
		
		requestID = reqID; // Set the 'final' request ID to pass into the new handler Thread

		if((Integer) Utils.programSettings.get("bSessionDetection") != 1) // And that's as far as we'll go if detection is off
			return;

		if(cookieData.isEmpty() ||
				requestHost.isEmpty() ||
				requestUri.isEmpty() ||
				accept.isEmpty() ||
				cookieData.isEmpty() ||
				requestUri.contains("favicon."))
			return;

    	SwingWorker<?, ?> analyzeRequestWorker = new SwingWorker<Object, Object>() {            
        	@Override            
            public Object doInBackground()
        	{
				// For a unique token, we'll use a quick concat of the user MAC and the request host
        		// And yes, this means that we can't pick up multiple logins to the same site from the
        		// same client. And while this is sad, there's not really a better way...
        		String uniqueID = macAddress + "," + requestHost;
				
				// Now check for the token. If new, pass on to handler classes
				try
				{	
					if(!Utils.dbInstance.containsValue("sessions", "user_token", uniqueID))
					{
						// Don't make these class variables, ever.
						// Tried, that, ended up blowing up the Threads as
						// both would access the same object during transactions
						ScriptEngineManager manager = new ScriptEngineManager();
					    ScriptEngine engine = manager.getEngineByName("js");
						
						for(String sd : sessionDetectors)
						{
					    	FileReader reader = new FileReader(sd);
					    	
					    	try
						    {	
					    		engine.eval(reader);
						    }
					    	catch (ScriptException se)
					    	{
					    		System.err.println("Exception in plugin '" + sd + "', stack trace follows:");
					    		se.printStackTrace();
					    	}
					    	reader.close();
	
					    	try
					    	{
					    		if(engine instanceof Invocable)
					    		{
					    			((Invocable)engine).invokeFunction("processRequest", requestHost, requestUri, userAgent, accept, cookieData);
					    		}
					    	}
					    	catch (ScriptException se)
					    	{
					    		System.err.println("Exception in plugin '" + sd + "', function 'processRequest'. Stack trace follows:");
					    		se.printStackTrace();
					    	}

					    	String description = "";
					    	String profileImageUrl = "";
					    	String sessionUri = "";
					    	
					    	try
					    	{
					    		description = (String)engine.get("description");
						    	profileImageUrl = (String)engine.get("profileImageUrl");
						    	sessionUri = (String)engine.get("sessionUri");
					    	}
					    	catch (Exception e)
					    	{
					    		// Do nothing
					    	}

					    	if(description != null && description.length() > 0 && !description.equals("null"))
					    	{
					    		if(profileImageUrl != null && profileImageUrl.equals("null"))
					    			profileImageUrl = null;
					    		
					    		if(sessionUri != null && sessionUri.equals("null"))
					    			sessionUri = null;

				    			handleSession(requestID, uniqueID, description, profileImageUrl, sessionUri);

				    			if(Utils.cookieCadgerFrame != null)
				    			{
					    			// Session created, check to see if we should auto-load it as well.
					    			if(((JCheckBox)Utils.cookieCadgerFrame.getComponentByName("chckbxAutomaticallyLoadSessions")).isSelected())
					    			{						    							
					    				BrowserHandler.loadRequestIntoBrowser(requestHost, requestUri, userAgent, refererUri, cookieData, authorization);
					    			}
				    			}
					    		
					    		// A match has been made against this request. Exit to prevent other
					    		// detectors from needlessly using system resources.
					    		break;
					    	}
						}
					}
				} catch (Exception e1)
				{
					e1.printStackTrace();
				}
        		
                return null;
            }
        };
        analyzeRequestWorker.execute();
	}
	
	private int handleClient(String macAddress)
	{		
		try
		{
			if(Utils.dbInstance.containsValue("clients", "mac_address", macAddress))
			{
				return Utils.dbInstance.getIntegerValue("clients", "id", "mac_address", macAddress);
			}
			else
			{
				if(Utils.cookieCadgerFrame == null)
				{
					Utils.consoleMessage("New client: " + macAddress);
				}
				
				return Utils.dbInstance.createClient(macAddress);
			}
		}
		catch (Exception e)
		{
			// Do nothing
		}
		
		return -1;
	}
	
	private void handleSession(int requestID, String userToken, String description, String profilePhotoUrl, String sessionUri)
	{
		try
		{
			// Why check again? Well, the querying of all this can be time consuming,
			// meaning that other requests could have already taken care of it.
			if(!Utils.dbInstance.containsValue("sessions", "user_token", userToken))
			{				
				// Update DB
				int sessionID = Utils.dbInstance.createSession(requestID, userToken, description, profilePhotoUrl, sessionUri);
				
				if(Utils.cookieCadgerFrame != null)
				{
					Utils.cookieCadgerFrame.changeSessionsList(false);
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public void initializeDeviceList()
	{
		File tshark;
		
		String tsharkPath = (String) Utils.programSettings.get("tsharkPath");
		
		if(tsharkPath.isEmpty()) // no program argument or preference for tshark, try to find it
		{
			// Get tshark location by checking likely Linux, Windows, and Mac paths
			for(String path : Utils.knownTsharkLocations)
			{
				if(new File(path).exists())
				{
					Utils.consoleMessage("tshark located at " + path);
					tsharkPath = path;
					
					// Found it
					Utils.programSettings.put("tsharkPath", path);
					break;
				}
			}
		}
		else // tshark path specified, check that tshark actually exists there
		{
			if(new File(tsharkPath).exists())
			{
				Utils.consoleMessage("tshark specified at " + tsharkPath);
			}
			else
			{
				tsharkNotice("You specified a path to 'tshark', but the given path is invalid.");
				tsharkPath = ""; // Empty the user-specified value
				
				// Clear out the saved value, which doesn't exist
				Utils.savePreference("tsharkPath", "");
				Utils.programSettings.put("tsharkPath", "");
			}
		}
		
		if(tsharkPath.isEmpty())
		{
			tsharkNotice("Error: couldn't find 'tshark' (part of the 'Wireshark' suite). This software cannot capture or analyze packets without it.\nYou can still load previously saved sessions for replaying in the browser, but be aware you might encounter errors.\n\nYou can manually specify the location to 'tshark' in the 'Program Settings' area, or as a program argument.\n\nUsage:\njava -jar CookieCadger.jar --tshark=<full path to tshark>");
		}
		else
		{
			Utils.consoleMessage("Querying tshark for capture devices; tshark output follows:");

			String line = "";
			try {
				ProcessBuilder pb = new ProcessBuilder(new String[] { tsharkPath, "-D" } );
				pb.redirectErrorStream(true);
				Process proc = pb.start();
				InputStream is = proc.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				
				while ((line = br.readLine()) != null)
				{
					// Print every piece of output to the console
					Utils.consoleMessage(line);

					boolean isNumericStart = true;
					try
					{
						// If line starts with an Integer, its valid
						Integer.parseInt(line.substring(0, 1));
					}
					catch(Exception e)
					{
						isNumericStart = false;
					}

					if(isNumericStart)
					{
						if(line.contains("(") && line.contains(")"))
						{
							// This line has a description
							deviceDescription.add(line.substring(line.indexOf(" (") + 2, line.indexOf(")")).trim());
							deviceName.add(line.substring(line.indexOf(". ") + 2, line.indexOf(" (")));
						}
						else
						{
							// This line has no description
							deviceDescription.add("no description");
							deviceName.add(line.substring(line.indexOf(" ") + 1));
						}
						bCapturing.add(false);
						deviceCaptureProcess.add(null);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			Utils.consoleMessage("Capture device search completed with " + deviceName.size() + " devices found.");
		}
	}
	
	private void loadPlugins()
	{
		// Always check for plugins updates, if allowed
		if((Boolean) Utils.programSettings.get("bCheckForUpdates"))
		{
    		try
    		{
    			// Get the plugins ZIP file
    			String userTempDirectory = System.getProperty("java.io.tmpdir").replace("\\", "/");
    			File pluginsFile = new File(userTempDirectory + "/plugins.zip");

    			String UrlString = "https://www.cookiecadger.com/files/plugins.zip";
    			HttpURLConnection httpConnection = (HttpURLConnection) new URL(UrlString).openConnection();
    			httpConnection.setRequestMethod("GET");
    				
    			httpConnection.setRequestProperty ("Content-Type", "application/octet-stream");
    			httpConnection.setRequestProperty ("User-Agent", "Cookie Cadger, " + Utils.version);
    			FileUtils.copyInputStreamToFile(httpConnection.getInputStream(), pluginsFile);
    			
    			try
    			{
    				// Clean out existing plugins
    				FileUtils.cleanDirectory(new File(userTempDirectory + "/plugins"));
    			}
    			catch (Exception e)
    			{
    				// Doesn't exist yet, don't worry about it
    			}
    			
    			// Unzip
    			Utils.unZipFile(pluginsFile.getAbsolutePath(), userTempDirectory + "/plugins");
    			
    			// Delete ZIP
    			pluginsFile.delete();
			}
    		catch (Exception e)
    		{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
        // Load all plugin classes
        try
        {
        	String userTempDirectory = System.getProperty("java.io.tmpdir").replace("\\", "/");
        	File folder;
        	File[] listOfFiles;
        	
        	// Look for plugins recently extracted in the temporary location
			folder = new File(userTempDirectory + "/plugins/");
			listOfFiles = folder.listFiles();
			 
			for (int i = 0; i < listOfFiles.length && listOfFiles[i].isFile(); i++)
			{ 
				String pluginClassFilename = listOfFiles[i].getName();
				if (pluginClassFilename.toLowerCase().endsWith(".js"))
				{					
					sessionDetectors.add(userTempDirectory + "/plugins/" + pluginClassFilename);
				}
			}
			
			// Look for plugins in the directory on the same level as Cookie Cadger
			folder = new File(Utils.executionPath + "/plugins/");
			listOfFiles = folder.listFiles();
			 
			for (int i = 0; i < listOfFiles.length && listOfFiles[i].isFile(); i++)
			{ 
				String pluginClassFilename = listOfFiles[i].getName();
				if (pluginClassFilename.toLowerCase().endsWith(".js"))
				{					
					sessionDetectors.add(Utils.executionPath + "/plugins/" + pluginClassFilename);
				}
			}
        }
        catch(NullPointerException npe)
        {
        	// No plugins directory; do nothing
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
	}
	
	private void tsharkNotice(String notice)
	{
		if(Utils.cookieCadgerFrame != null)
			JOptionPane.showMessageDialog(null, notice);
		else
			Utils.consoleMessage(notice);
	}
}
