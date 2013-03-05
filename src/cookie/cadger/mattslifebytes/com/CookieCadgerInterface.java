/*
 * Copyright (c) 2012, Matthew Sullivan <MattsLifeBytes.com / @MattsLifeBytes>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution. 
 * 3. By using this software, you agree to provide the Software Creator (Matthew
 *    Sullivan) exactly one drink of his choice under $10 USD in value if he
 *    requests it of you.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * First time in Java (+ Eclipse)... sorry for the mess!  M.S. 
 */

package cookie.cadger.mattslifebytes.com;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JOptionPane;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.browsermob.proxy.ProxyServer;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import cookie.cadger.mattslifebytes.com.SortedListModel.SortOrder;

public class CookieCadgerInterface extends JFrame
{
	private static int localRandomization;
	private static String executionPath = System.getProperty("user.dir").replace("\\", "/");
	
	private JPanel contentPane, requestsPanel, sessionsPanel;
	private EnhancedListModel clientsListModel, domainsListModel, requestsListModel, sessionsListModel;
	private DefaultComboBoxModel<String> interfacesListModel;
	private EnhancedJList clientsList, domainsList, requestsList, sessionsList;
	private JTextArea txtConsole;
	private JScrollPane consoleScrollPane;
	private JTabbedPane tabbedPane;
	private JProgressBar loadingRequestProgressBar;
	private String pathToTshark;
	private ArrayList<Boolean> bCapturing = new ArrayList<Boolean>();
	private ArrayList<String> deviceName = new ArrayList<String>();
	private ArrayList<String> deviceDescription = new ArrayList<String>();
	private ArrayList<Process> deviceCaptureProcess = new ArrayList<Process>();
	private ArrayList<String> sessionDetectors = new ArrayList<String>();
	private HashMap<String, Component> componentMap; // Cheers to Jesse Strickland (stackoverflow.com/questions/4958600/get-a-swing-component-by-name)
	private JPopupMenu clientsPopup, requestsPopup, sessionsPopup;
	
	private WebDriver driver = null;
	private ProxyServer server = null;
	private Proxy proxy = null;
	private Sqlite3DB dbInstance = null;
	private boolean bUseSessionDetection = false;
	private boolean bUseSessionDetectionSpecified = false;
	private boolean bUpdateChecking = true;
	private boolean bUseDemoMode = false;
	private boolean bUseDemoModeSpecified = false;
	private RequestInterceptor requestIntercept;

	private void StartCapture(int ethDevNumber, String pcapFile) throws IOException
	{
		ProcessBuilder pb = null;
		Process proc = null;
		ProcessWatcher pw = null;
		InputStream is = null;
		
		Date capStart = new Date();
		ArrayList<String> nonTabbedOutput = new ArrayList<String>();
		
		if(pcapFile.isEmpty())
		{			
			Console("Opening '" + deviceName.get(ethDevNumber) + "' for traffic capture.", true);
			pb = new ProcessBuilder(new String[] { pathToTshark, "-i", deviceName.get(ethDevNumber), "-f", "tcp dst port 80 or udp src port 5353 or udp src port 138", "-T", "fields", "-e", "eth.src", "-e", "wlan.sa", "-e", "ip.src", "-e", "ipv6.src", "-e", "tcp.srcport", "-e", "tcp.dstport", "-e", "udp.srcport", "-e", "udp.dstport", "-e", "browser.command", "-e", "browser.server", "-e", "dns.resp.name", "-e", "http.host", "-e", "http.request.uri", "-e", "http.accept", "-e", "http.accept_encoding", "-e", "http.user_agent", "-e", "http.referer", "-e", "http.cookie", "-e", "http.authorization", "-e", "http.authbasic" } );
			pb.redirectErrorStream(true);
			deviceCaptureProcess.set(ethDevNumber, pb.start());
			pw = new ProcessWatcher(deviceCaptureProcess.get(ethDevNumber));
			is = deviceCaptureProcess.get(ethDevNumber).getInputStream();
		}
		else
		{
			Console("Opening '" + pcapFile + "' for traffic capture.", true);
			pb = new ProcessBuilder(new String[] { pathToTshark, "-r", pcapFile, "-T", "fields", "-e", "eth.src", "-e", "wlan.sa", "-e", "ip.src", "-e", "ipv6.src", "-e", "tcp.srcport", "-e", "tcp.dstport", "-e", "udp.srcport", "-e", "udp.dstport", "-e", "browser.command", "-e", "browser.server", "-e", "dns.resp.name", "-e", "http.host", "-e", "http.request.uri", "-e", "http.accept", "-e", "http.accept_encoding", "-e", "http.user_agent", "-e", "http.referer", "-e", "http.cookie", "-e", "http.authorization", "-e", "http.authbasic" } );
			pb.redirectErrorStream(true);
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
						if(!pcapFile.isEmpty() && (!tcpDestination.equals("80") && !udpSource.equals("5353") && !udpSource.equals("138")))
						{
							continue;
						}
						
						// When Cookie Cadger creates requests it appends a randomization
						// to the Accept header. Check for it and ignore if matched.
						if(accept.contains(", " + Integer.toString(localRandomization)))
						{
							continue;
						}

						if(!requestUri.isEmpty())
						{
							bUsefulData = true;
							clientID = HandleClient(macAddress);
							
							ProcessRequest(macAddress, ipv4Address, ipv6Address, requestHost, accept, acceptEncoding, requestUri, userAgent, refererUri, cookieData, authorization, authBasic);
						}
						else if(!netbiosCommand.isEmpty() && netbiosCommand.equals("0x0f") && !netbiosName.isEmpty()) // 0x0f = host announcement broadcast
						{
							bUsefulData = true;
							clientID = HandleClient(macAddress);
							
							dbInstance.setStringValue("clients", "netbios_hostname", netbiosName, "mac_address", macAddress);
						
							// If only show hosts with GET requests is unchecked, always show. If checked and total for this MAC > 0, show as well
							if(!clientsListModel.contains(macAddress) && !((JCheckBox)GetComponentByName("chckbxOnlyShowHosts")).isSelected() || ( ((JCheckBox)GetComponentByName("chckbxOnlyShowHosts")).isSelected() && !clientsListModel.contains(macAddress) && dbInstance.getIntegerValue("clients", "has_http_requests", "id", Integer.toString(clientID)) > 0 ) )
							{
								clientsListModel.addElement(new EnhancedJListItem(clientID, macAddress, null));
								clientsList.performHighlight(macAddress, Color.BLUE);
							}
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
									clientID = HandleClient(macAddress);
									
									mdnsNameStr = mdnsNameStr.replace(".local", "");

									dbInstance.setStringValue("clients", "mdns_hostname", mdnsNameStr, "mac_address", macAddress);
									
									// If only show hosts with GET requests is unchecked, always show. If checked and total for this MAC > 0, show as well
									if(!clientsListModel.contains(macAddress) && !((JCheckBox)GetComponentByName("chckbxOnlyShowHosts")).isSelected() || ( ((JCheckBox)GetComponentByName("chckbxOnlyShowHosts")).isSelected() && !clientsListModel.contains(macAddress) && dbInstance.getIntegerValue("clients", "has_http_requests", "id", Integer.toString(clientID)) == 1 ) )
									{
										clientsListModel.addElement(new EnhancedJListItem(clientID, macAddress, null));
										clientsList.performHighlight(macAddress, Color.BLUE);
									}
								}
							}
						}
						
						if(bUsefulData)
						{
							if(!ipv4Address.isEmpty())
								dbInstance.setStringValue("clients", "ipv4_address", ipv4Address, "mac_address", macAddress);
							
							if(!ipv6Address.isEmpty())
								dbInstance.setStringValue("clients", "ipv6_address", ipv6Address, "mac_address", macAddress);
						}
						
						// And update the informational display
						UpdateDescriptionForMac(macAddress);
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
			Console("EXCEPTION", true);

			if(!pcapFile.isEmpty() && proc != null)
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
		
		if(pcapFile.isEmpty())
			StopCapture(ethDevNumber); // Update UI and clean up
		
		Date capEnd = new Date();
		int runTimeInSeconds = (int) ((capEnd.getTime() - capStart.getTime()) / 1000);
		
		if(pcapFile.isEmpty() && runTimeInSeconds <= 20)
		{
			Console(deviceName.get(ethDevNumber) + ": " + "============================================================================", true);
			Console(deviceName.get(ethDevNumber) + ": " + "Start of diagnostic information for interface '" + deviceName.get(ethDevNumber) + "'", true);
			Console(deviceName.get(ethDevNumber) + ": " + "============================================================================", true);
			
			for (String output : nonTabbedOutput)
			{
				Console(deviceName.get(ethDevNumber) + ": " + output, true);				
			}
			
			Console(deviceName.get(ethDevNumber) + ": " + "============================================================================", true);
			Console(deviceName.get(ethDevNumber) + ": " + "Potential error detected! Capture stopped / died in " + Integer.toString(runTimeInSeconds) + " seconds.", true);
			Console(deviceName.get(ethDevNumber) + ": " + "All messages from the 'tshark' program have been printed above to assist you in solving any errors.", true);
			Console(deviceName.get(ethDevNumber) + ": " + "============================================================================", true);
		}
		else
		{
			if(pcapFile.isEmpty())
				Console("'" + deviceName.get(ethDevNumber) + "' has been closed and is finished with traffic capture. Capture duration: " + Integer.toString(runTimeInSeconds) + " seconds.", true);
			else
				Console("'" + pcapFile + "' has finished processing. Processing duration: " + Integer.toString(runTimeInSeconds) + " seconds.", true);
		}
	}
	
	private void StopCapture(int ethDevNumber)
	{
		interfacesListModel.removeElementAt(ethDevNumber);
		interfacesListModel.insertElementAt(deviceName.get(ethDevNumber) + " [" + deviceDescription.get(ethDevNumber) + "]", ethDevNumber);
		((JComboBox<?>)GetComponentByName("interfaceListComboBox")).setSelectedIndex(ethDevNumber);

		if (deviceCaptureProcess.get(ethDevNumber) != null)
		{
			deviceCaptureProcess.get(ethDevNumber).destroy();
		}
		
		bCapturing.set(ethDevNumber, false);
		
		SetCaptureButtonText();
	}
	
	private void PrepCapture(int ethDevNumber)
	{
		interfacesListModel.removeElementAt(ethDevNumber);
		interfacesListModel.insertElementAt(deviceName.get(ethDevNumber) + " [" + deviceDescription.get(ethDevNumber) + "] (CURRENTLY CAPTURING)", ethDevNumber);
		((JComboBox<?>)GetComponentByName("interfaceListComboBox")).setSelectedIndex(ethDevNumber);
		
		bCapturing.set(ethDevNumber, true);
		
		SetCaptureButtonText();
	}
	
	private void ResetDataset(boolean bInitTables)
	{
		try
		{
			// If this user session is not empty set
			if(dbInstance.getMacs(false).length > 0)
			{
			    // Ask the user to save
			    int saveDatasetResult = JOptionPane.showConfirmDialog(
			            contentPane,
			            		"Would you like to save the current dataset?",
			            "Save?",
			            JOptionPane.YES_NO_OPTION);
			    
			    if (saveDatasetResult == JOptionPane.YES_OPTION)
			    {
			    	dbInstance.saveDatabase();
			    }
			}
		} catch (HeadlessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if(bInitTables)
		{
			try {
				dbInstance.initTables();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		requestsListModel.clear();
		domainsListModel.clear();
		clientsListModel.clear();
		sessionsListModel.clear();
	}
	
	private void SetCaptureButtonText()
	{
		int selection = ((JComboBox<?>)GetComponentByName("interfaceListComboBox")).getSelectedIndex();
		if(selection == -1)
		{
			((JButton)GetComponentByName("btnMonitorOnSelected")).setEnabled(false);
			((JButton)GetComponentByName("btnMonitorOnSelected")).setText("Select An Interface");
		}
		else
		{
			((JButton)GetComponentByName("btnMonitorOnSelected")).setEnabled(true);
			
			if(bCapturing.get(selection))
			{
				((JButton)GetComponentByName("btnMonitorOnSelected")).setText("Stop Capture on " + deviceName.get(selection));
			}
			else
			{
				((JButton)GetComponentByName("btnMonitorOnSelected")).setText("Start Capture on " + deviceName.get(selection));
			}
		}
	}
	
	private void ProcessRequest(final String macAddress, final String ipv4Address, final String ipv6Address, final String requestHost, final String accept, final String acceptEncoding, final String requestUri, final String userAgent, final String refererUri, final String cookieData, final String authorization, final String authBasic)
	{
		int clientID = -1;
		int domainID = -1;
		int reqID = -1;
		final int requestID;
	
		try
		{
			if(dbInstance.containsValue("clients", "mac_address", macAddress))
			{
				// We've seen this mac already, just get the ClientID
				clientID = dbInstance.getIntegerValue("clients", "id", "mac_address", macAddress); // In 'clients' get id where mac == macAddrSource
				
				// We're going to have activity in a previously identified host, highlight
				clientsList.performHighlight(macAddress, Color.BLUE);
			}
			else // Client object doesn't exist for MAC? Create one.
			{
				clientID = dbInstance.createClient(macAddress);
			}
			
			// Tell the client that a request was seen
			dbInstance.setStringValue("clients", "has_http_requests", Integer.toString(1), "id", Integer.toString(clientID));
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try
		{
			if(dbInstance.containsValue("domains", "name", requestHost))
			{
				// We've seen this domain already, just get the DomainID
				domainID = dbInstance.getIntegerValue("domains", "id", "name", requestHost);
			}
			else // Domain object doesn't exist for this name? Create one.
			{
				// Create new domain
				domainID = dbInstance.createDomain(requestHost);
				
				// And display it, if appropriate to do so
				if(!clientsList.isSelectionEmpty() && ((EnhancedJListItem)clientsList.getSelectedValue()).toString().equals(macAddress))
				{
					domainsListModel.addElement(new EnhancedJListItem(domainID, requestHost, null));
				}
			}
			
			// And highlight it for activity
			domainsList.performHighlight(requestHost, Color.BLUE);
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		try
		{
			reqID = dbInstance.createRequest(requestUri, userAgent, refererUri, cookieData, authorization, authBasic, domainID, clientID);
			
			// Update the requests list if necessary
			if(!clientsList.isSelectionEmpty() && ((EnhancedJListItem)clientsList.getSelectedValue()).toString().equals(macAddress) && !domainsList.isSelectionEmpty() && (((EnhancedJListItem)domainsList.getSelectedValue()).toString().equals(requestHost) || (((EnhancedJListItem)domainsList.getSelectedValue()).toString().equals("[ All Domains ]"))))
			{
				Date now = new Date();	
				String dateString = DateFormat.getTimeInstance(DateFormat.MEDIUM).format(now);

				requestsListModel.addElement(new EnhancedJListItem(reqID, dateString + ": " + requestUri, null));
			}
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			if(!clientsListModel.contains(macAddress) && !((JCheckBox)GetComponentByName("chckbxOnlyShowHosts")).isSelected() || ( ((JCheckBox)GetComponentByName("chckbxOnlyShowHosts")).isSelected() && !clientsListModel.contains(macAddress) && dbInstance.getIntegerValue("clients", "has_http_requests", "id", Integer.toString(clientID)) == 1 ) )
			{
				clientsListModel.addElement(new EnhancedJListItem(clientID, macAddress, null));
				clientsList.performHighlight(macAddress, Color.BLUE);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		UpdateDescriptionForMac(macAddress);
		requestID = reqID; // Set the 'final' request ID to pass into the new handler Thread
		
		if(!bUseSessionDetection) // And that's as far as we'll go
			return;

		if(cookieData.isEmpty() ||
				requestHost.isEmpty() ||
				requestUri.isEmpty() ||
				accept.isEmpty() ||
				cookieData.isEmpty() ||
				requestUri.contains("favicon.ico"))
			return;

    	SwingWorker<?, ?> analyzeRequestWorker = new SwingWorker<Object, Object>() {            
        	@Override            
            public Object doInBackground()
        	{
				// For a unique token, we'll use a quick concat of the user MAC and the request host
        		String uniqueID = macAddress + "," + requestHost;
				
				// Now check for the token. If new, pass on to handler classes
				try
				{	
					if(!dbInstance.containsValue("sessions", "user_token", uniqueID))
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
					    			profileImageUrl = null;

				    			CreateSession(requestID, uniqueID, description, profileImageUrl, sessionUri);

				    			// Session created, check to see if we should auto-load it as well.
				    			if(((JCheckBox)GetComponentByName("chckbxAutomaticallyLoadSessions")).isSelected())
				    			{						    							
				    				LoadRequestIntoBrowser(requestHost, requestUri, userAgent, refererUri, cookieData, authorization);
				    			}
					    		
					    		// A match has been made against this request. Exit to prevent other
					    		// detectors from needlessly using system resources.
					    		break;
					    	}
						}
					}
				} catch (Exception e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        		
                return null;
            }
        };
        analyzeRequestWorker.execute();
	}
	
	private int HandleClient(String macAddress)
	{
		try
		{
			if(dbInstance.containsValue("clients", "mac_address", macAddress))
			{
				return dbInstance.getIntegerValue("clients", "id", "mac_address", macAddress);
			}
			else
			{
				return dbInstance.createClient(macAddress);
			}
		}
		catch (Exception e)
		{
			// Do nothing
		}
		
		return -1;
	}
	
	private void ChangeClientsList()
	{
		int clients = 0;
		final int clientCount;
		
		try {
			clients = dbInstance.getClientCount();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		clientCount = clients;
		
		consoleScrollPane.setVisible(false);
		loadingRequestProgressBar.setString("Loading client list, please wait... [0 / " + clientCount + "]");
		loadingRequestProgressBar.setVisible(true);
        
		Thread changeClientsListThread = new Thread(new Runnable()
		{
		     public void run()
		     {
        		boolean bPreviousSelection = false;
        		String previousSelection = null;
        		int recordCount = 0;
        		
        		if(!clientsList.isSelectionEmpty())
        		{
        			bPreviousSelection = true;
        			previousSelection = ((EnhancedJListItem)clientsList.getSelectedValue()).toString();
        		}
        		
        		clientsListModel.clear();
        		clientsList.setModel(new EnhancedListModel());
        		
        		try
        		{
        			boolean bOnlyHostsWithData = ((JCheckBox)GetComponentByName("chckbxOnlyShowHosts")).isSelected();

        			String[] macAddresses = dbInstance.getMacs(bOnlyHostsWithData);
        			for (String s : macAddresses)
        			{
        				// Update the GUI with progress
        			    if (recordCount % 10 == 0)
        			    {
        			    	final int currentRecord = recordCount;

        			    	SwingUtilities.invokeLater(new Runnable()
							{
							    public void run()
							    {        			    	
							    	loadingRequestProgressBar.setString("Loading client list, please wait... (" + currentRecord + " / " + clientCount + ")");
							    }
							});
        			    }
        			    recordCount++;

        			    if(!clientsListModel.contains(s))
        			    {
        			    	int clientID = dbInstance.getIntegerValue("clients", "id", "mac_address", s);
        			    	clientsListModel.addElement(new EnhancedJListItem(clientID, s, null));
        			    }

        			    UpdateDescriptionForMac(s);
        			}
        		}
        		catch (SQLException e)
        		{
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
        		
        		clientsList.setModel(clientsListModel);
        		
        		if(bPreviousSelection)
        		{
        			// If the newly generated list still contains the previously selected value, show it
        			if (clientsListModel.contains(previousSelection))
        			{
        				int index = clientsListModel.indexOf(previousSelection);
        				clientsList.setSelectedValue(clientsListModel.getElementAt(index), true);
        			}
        		}
		    	
                loadingRequestProgressBar.setVisible(false);
                consoleScrollPane.setVisible(true);
            }
    	});
		
		changeClientsListThread.start();
	}
	
	private void ChangeDomainsList(String macAddress)
	{
		boolean bPreviousSelection = false;
		String previousSelection = null;
		
		if(!domainsList.isSelectionEmpty())
		{
			bPreviousSelection = true;
			previousSelection = ((EnhancedJListItem)domainsList.getSelectedValue()).toString();
		}
		
		domainsListModel.clear();
		requestsListModel.clear();
		
		domainsListModel.addElement(new EnhancedJListItem(-1, "[ All Domains ]", null));
		try
		{
			for (String s : dbInstance.getDomains(macAddress))
			{
				int domainID = dbInstance.getIntegerValue("domains", "id", "name", s);
				domainsListModel.addElement(new EnhancedJListItem(domainID, s, null));
			}
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(bPreviousSelection)
		{
			// If the newly generated list still contains the previously selected value, show it
			if (domainsListModel.contains(previousSelection))
			{
				int index = domainsListModel.indexOf(previousSelection);
				domainsList.setSelectedValue(domainsListModel.getElementAt(index), true);
			}
		}
	}
	
	private void ChangeRequestsList(String macAddress, String uriHost)
	{
		boolean bPreviousSelection = false;
		String previousSelection = null;
		
		if(!requestsList.isSelectionEmpty())
		{
			bPreviousSelection = true;
			previousSelection = ((EnhancedJListItem)requestsList.getSelectedValue()).toString();
		}
		
		boolean bShowAllDomains = false;
		requestsListModel.clear();
		
		if(uriHost.equals("[ All Domains ]"))
		{
			bShowAllDomains = true;
			uriHost = "%";
		}
		
		try {
			ArrayList<ArrayList> requests = dbInstance.getRequests(macAddress, uriHost);
			
			ArrayList<String> ids = requests.get(0);
			ArrayList<String> timerecordeds = requests.get(1);
			ArrayList<String> uris = requests.get(2);
			
			for (int i = 0; i < ids.size(); i++)
			{
				long timeStamp = new Long(timerecordeds.get(i));
				Date then = new Date((long)timeStamp * 1000);
				Date now = new Date();
				
				SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
				boolean sameDay = fmt.format(then).equals(fmt.format(now));
				
				String dateString;
				if(sameDay)
				{
					dateString = DateFormat.getTimeInstance(DateFormat.MEDIUM).format(then);
				}
				else
				{
					dateString = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(then);
				}

				int requestID = new Integer(ids.get(i));
				String uri = uris.get(i);
				
				// If showing all domains in the request list, make sure the full request Url gets displayed
				if(bShowAllDomains)
				{
					int domainID = dbInstance.getIntegerValue("requests", "domain_id", "id", Integer.toString(requestID));
					String domain = dbInstance.getStringValue("domains", "name", "id", Integer.toString(domainID));
					uri = domain + uri;
				}

				requestsListModel.addElement(new EnhancedJListItem(requestID, dateString + ": " + uri, null));
				
				UpdateDescriptionForRequest(requestID);
			}
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(bPreviousSelection)
		{
			// If the newly generated list still contains the previously selected value, show it
			if (requestsListModel.contains(previousSelection))
			{
				int index = requestsListModel.indexOf(previousSelection);
				requestsList.setSelectedValue(requestsListModel.getElementAt(index), true);
			}
		}
	}

	private void ChangeSessionsList()
	{
		Thread changeSessionsListThread = new Thread(new Runnable()
		{
		     public void run()
		     {
				Image photo = null;
				
				sessionsListModel.clear();
				
				EnhancedJListItem[] sessions;
				try
				{
					sessions = dbInstance.getSessions();
					
					for (EnhancedJListItem item : sessions)
					{
						String profilePhotoUrl = item.getProfileImageUrl();
						boolean bHaveImage = false;
						
						// Make image
						if(profilePhotoUrl != null && !profilePhotoUrl.isEmpty())
						{
							try
							{
								URL url = new URL(profilePhotoUrl);
								photo = ImageIO.read(url);
								
								bHaveImage = true;
							}
							catch (Exception e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
								
								bHaveImage = false;
							}
						}
						
						if(bHaveImage)
							item.setThumbnail(photo);
						
						sessionsListModel.addElement(item);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		     }
		});
		
		changeSessionsListThread.start();
	}
	
	private void UpdateDescriptionForMac(String macAddress)
	{
		if(clientsListModel.contains(macAddress))
		{
			int location = clientsListModel.indexOf(macAddress);
			((EnhancedJListItem)clientsListModel.getElementAt(location)).setDescription(GenerateDescriptionForMac(macAddress, true));
		}
	}
	
	private String GenerateDescriptionForMac(String macAddress, boolean bUseHTML)
	{
		String htmlOpen = "";
		String boldOpen = "";
		String fontOpen = "";
		String fontClose = "";
		String boldClose = "";
		String htmlClose = "";
		String newLine = "\r\n";
		
		if(bUseHTML)
		{
			htmlOpen = "<html>";
			boldOpen = "<b>";
			fontOpen = "<font size=4>";
			fontClose = "</font>";
			boldClose = "</b>";
			htmlClose = "</html>";
			newLine = "<br>";
		}
		
		try
		{
			String netbiosHost = dbInstance.getStringValue("clients", "netbios_hostname", "mac_address", macAddress);
			String mdnsHost = dbInstance.getStringValue("clients", "mdns_hostname", "mac_address", macAddress);
			String ipv4Address = dbInstance.getStringValue("clients", "ipv4_address", "mac_address", macAddress);
			String ipv6Address = dbInstance.getStringValue("clients", "ipv6_address", "mac_address", macAddress);
			
			String notesTxt = htmlOpen + fontOpen + boldOpen + "MAC Address: " + boldClose + macAddress;
			
			if(!ipv4Address.isEmpty())
			{
				notesTxt = notesTxt + newLine + boldOpen + "IPv4 Address: " + boldClose + ipv4Address;
			}
			
			if(!ipv6Address.isEmpty())
			{				
				notesTxt = notesTxt + newLine + boldOpen + "IPv6 Address: " + boldClose + ipv6Address;
			}
			
			if(!netbiosHost.isEmpty())
			{
				notesTxt = notesTxt + newLine + boldOpen + "Host Name (NetBIOS): " + boldClose + netbiosHost;
			}
			
			if(!mdnsHost.isEmpty())
			{
				notesTxt = notesTxt + newLine + boldOpen + "Host Name (mDNS/Bonjour): " + boldClose + mdnsHost;
			}

			String[] userAgents = dbInstance.getUserAgents(macAddress);
			ArrayList<String> displayUserAgents = new ArrayList<String>();
			
			for (String userAgent : userAgents)
			{
				if(userAgent.trim().length() > 0)
					displayUserAgents.add(userAgent.trim());
			}
			
			if(displayUserAgents.size() > 0)
			{
				notesTxt = notesTxt + newLine + boldOpen + "Detected User-Agents: " + boldClose;
				
				for (String userAgent : displayUserAgents)
				{
					notesTxt = notesTxt + newLine + "  - " + userAgent;
				}
			}
			
			notesTxt = notesTxt + fontClose + htmlClose;
			
			return notesTxt;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	private void UpdateDescriptionForRequest(int request)
	{
		if(requestsListModel.contains(request))
		{
			int location = requestsListModel.indexOf(request);
			((EnhancedJListItem)requestsListModel.getElementAt(location)).setDescription(GenerateDescriptionForRequest(request, true, true));
		}
	}
	
	private String GenerateDescriptionForRequest(int requestID, boolean bUseHTML, boolean bTruncate)
	{
		String htmlOpen = "";
		String boldOpen = "";
		String fontOpen = "";
		String fontClose = "";
		String boldClose = "";
		String htmlClose = "";
		String newLine = "\r\n";
		
		if(bUseHTML)
		{
			htmlOpen = "<html>";
			boldOpen = "<b>";
			fontOpen = "<font size=4>";
			fontClose = "</font>";
			boldClose = "</b>";
			htmlClose = "</html>";
			newLine = "<br>";
		}
		
		try {
			String timeRecorded = dbInstance.getStringValue("requests", "timerecorded", "id", Integer.toString(requestID));
			
			long timeStamp = new Long(timeRecorded);
			Date then = new Date((long)timeStamp * 1000);		
			String dateString = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(then);
			
			String uri = dbInstance.getStringValue("requests", "uri", "id", Integer.toString(requestID));
			if(bTruncate && uri.length() > 90)
				uri = uri.substring(0, 86) + boldOpen + " ..." + boldClose;
			 
			String notesTxt = htmlOpen + fontOpen + boldOpen + "Date: " + boldClose + dateString;
			notesTxt = notesTxt + newLine + boldOpen + "Uri: " + boldClose + uri;
			String userAgent = dbInstance.getStringValue("requests", "useragent", "id", Integer.toString(requestID));
			String referer = dbInstance.getStringValue("requests", "referer", "id", Integer.toString(requestID));
			String authBasic = dbInstance.getStringValue("requests", "auth_basic", "id", Integer.toString(requestID));
			String cookies = dbInstance.getStringValue("requests", "cookies", "id", Integer.toString(requestID));
			
			if(!userAgent.isEmpty())
			{
				if(bTruncate && userAgent.length() > 90)
					userAgent = userAgent.substring(0, 86) + boldOpen + " ..." + boldClose;
				
				notesTxt = notesTxt + newLine + boldOpen + "User Agent: " + boldClose + userAgent;
			}
			
			if(!referer.isEmpty())
			{
				if(bTruncate && referer.length() > 90)
					referer = referer.substring(0, 86) + boldOpen + " ..." + boldClose;
				
				notesTxt = notesTxt + newLine + boldOpen + "Referer: " + boldClose + userAgent;
			}
			
			if(!authBasic.isEmpty())
			{
				if(bTruncate && authBasic.length() > 90)
					authBasic = authBasic.substring(0, 86) + boldOpen + " ..." + boldClose;
				
				notesTxt = notesTxt + newLine + boldOpen + "Basic Authorization Credentials: " + boldClose + authBasic;
			}
			
			if(!cookies.isEmpty())
			{
				if(bTruncate && cookies.length() > 90)
					cookies = cookies.substring(0, 86) + boldOpen + " ..." + boldClose;
				
				notesTxt = notesTxt + newLine + boldOpen + "Cookies: " + boldClose + cookies;
			}
			
			return notesTxt;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	// Now we're at the good part.
	private void LoadRequestIntoBrowser(String domain, String uri, String useragent, String referer, String cookies, String authorization)
	{
		consoleScrollPane.setVisible(false);
		
		loadingRequestProgressBar.setString("Loading request into browser, please wait...");
		loadingRequestProgressBar.setVisible(true);
	    
		if(server == null)
		{
			server = new ProxyServer(7878);
	        
	        requestIntercept = new RequestInterceptor();
	        requestIntercept.setRandomization(Integer.toString(localRandomization));
	        
	        try {
				server.start();
				server.addRequestInterceptor(requestIntercept);
				proxy = server.seleniumProxy(); // Set HTTP proxy
				proxy.setSslProxy(null); // Unset SSL entirely
;				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}

		// Ask to get page title
		// If none, this will Exception and set driver to null as necessary
		try
		{
			driver.getTitle();
		}
		catch (Exception e)
		{
			driver = null;
		}
		
		if(driver == null)
		{	        
			// configure it as a desired capability
			DesiredCapabilities capabilities = new DesiredCapabilities();
			capabilities.setCapability(CapabilityType.PROXY, proxy);
			
			driver = new FirefoxDriver(capabilities);
		}
		else
		{
			// Everything is already set to go, just clear the proxy settings
			requestIntercept.clear();
		}
		
        if(!cookies.isEmpty())
        {
        	requestIntercept.setCookies(cookies);
        }
        
        if(!authorization.isEmpty())
        {
        	requestIntercept.setAuthorization(authorization);
        }
        
        if(useragent.isEmpty())
        {
        	// None specifically specified, so load from Firefox via the WebDriver
        	// (Without this BrowserMob modifies it and adds a unique tag)
        	useragent = (String) ((JavascriptExecutor) driver).executeScript("return navigator.userAgent;");
        }
        requestIntercept.setUserAgent(useragent);
        
        if(!referer.isEmpty())
        {
        	requestIntercept.setReferer(referer);
        }

        try
        {
        	driver.get("http://" + domain + uri);        	
        }
        catch (Exception e)
        {
        	// Nothing
        }
        
        loadingRequestProgressBar.setVisible(false);
        consoleScrollPane.setVisible(true);
	}
	
	private void PrepareToCloseApplication()
	{
		// Reset DB, which will ask user to save
		ResetDataset(false);
		
		// Get the WebDriver fully unloaded
		try
		{
			driver.close();
		}
		catch (Exception ex)
		{}
		finally
		{
			driver = null;
		}
		
		try
		{
			for (Process p : deviceCaptureProcess)
			{
				if(p != null)
					p.destroy();
			}
			
			if(dbInstance != null)
				dbInstance.closeDatabase();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private void HandleProgramArguments(String[] args)
	{
		int i = 0;
		String arg;
		boolean bTerminate = false;

		while (i < args.length && (args[i].startsWith("-") || args[i].startsWith("/")))
		{
			arg = args[i++];

		    // use this type of check for arguments that require arguments
			if (arg.contains("tshark"))
			{
				boolean filledRequirements = false;
				if(arg.contains("tshark="))
				{
					String value = arg.split("=")[1];
					if(value.length() > 0)
					{
						filledRequirements = true;
						this.pathToTshark = value;
					}
				}

				if(!filledRequirements)
				{
					System.err.println("--tshark requires a path to the tshark binary");
					bTerminate = true;
				}
			}
			else if (arg.contains("detection"))
			{
				boolean filledRequirements = false;
				if(arg.contains("detection="))
				{
					String value = arg.split("=")[1];
					
					if(value.equals("on"))
					{
						bUseSessionDetection = true;
						bUseSessionDetectionSpecified = true;
						filledRequirements = true;
					}
					else if (value.equals("off"))
					{
						bUseSessionDetection = false;
						bUseSessionDetectionSpecified = true;
						filledRequirements = true;
					}
				}

				if(!filledRequirements)
				{
					bTerminate = true;
					System.err.println("--detection (Session Detection) requires an 'on' or 'off' value");
				}
			}
			else if (arg.contains("update"))
			{
				boolean filledRequirements = false;
				if(arg.contains("update="))
				{
					String value = arg.split("=")[1];
					
					if(value.equals("on"))
					{
						bUpdateChecking = true;
						filledRequirements = true;
					}
					else if (value.equals("off"))
					{
						bUpdateChecking = false;
						filledRequirements = true;
					}
				}

				if(!filledRequirements)
				{
					bTerminate = true;
					System.err.println("--detection (Session Detection) requires an 'on' or 'off' value");
				}
			}
			else if (arg.contains("demo"))
			{
				boolean filledRequirements = false;
				if(arg.contains("demo="))
				{
					String value = arg.split("=")[1];
					
					if(value.equals("on"))
					{
						this.bUseDemoMode = true;
						bUseDemoModeSpecified = true;
						filledRequirements = true;
					}
					else if (value.equals("off"))
					{
						this.bUseDemoMode = false;
						bUseDemoModeSpecified = false;
						filledRequirements = true;
					}
				}

				if(!filledRequirements)
				{
					bTerminate = true;
					System.err.println("--demo (automatic loading of session into the browser) requires an 'on' or 'off' value. Session Detection must also be enabled.");
				}
			}
			
			if(bTerminate)
			{
				dispose();
				System.exit(0);
			}
		}
	}
	
	public CookieCadgerInterface(String args[]) throws Exception
	{
		HandleProgramArguments(args);
		
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e)
			{
				super.windowClosing(e);
				PrepareToCloseApplication();
			}
		});	    
		dbInstance = new Sqlite3DB();
		localRandomization = 1000 + (int)(Math.random() * ((20110 - 1000) + 1));
		
		setTitle("Cookie Cadger");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 950, 680);
		
		URL url = this.getClass().getResource("/resource/cookiecadger.png");
		BufferedImage img = ImageIO.read(url);
		this.setIconImage(img);
		
		if(!bUseSessionDetectionSpecified)
		{
	        // Ask the user about session detection
	        int sessionDetection = JOptionPane.showConfirmDialog(
	                contentPane,
	                		"Session detection replays web requests in the background and analyzes\n" +
	                		"them for evidence that a user is logged in. Enabling session detection\n" +
	                		"will cause Cookie Cadger to utilize a larger amount of available\n" +
	                		"system resources.\n\n" +
	                		"By enabling this feature you also understand that:\n\n" +
	                		"1) Cookie Cadger will (potentially) automatically impersonate any\n" +
	                		"network user without their explicit permission or interaction on your part.\n\n" +
	                		"2) The legality of doing so varies between jurisdictions. It is your\n" +
	                		"responsibility to understand and comply with any applicable laws.\n\n" +
	                		"Would you like to enable session detection?\n ",
	                "Enable Session Detection?",
	                JOptionPane.YES_NO_OPTION);
	        
	        if (sessionDetection == JOptionPane.YES_OPTION)
	        {
	        	bUseSessionDetection = true;
	        }
		}
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmStartNewSession = new JMenuItem("Start New Dataset");
		mnFile.add(mntmStartNewSession);
		
		JSeparator separator = new JSeparator();
		mnFile.add(separator);
		
		JMenuItem mntmSaveSession = new JMenuItem("Save Dataset");
		mnFile.add(mntmSaveSession);
		
		JMenuItem mntmLoadSession = new JMenuItem("Load Dataset");
		mnFile.add(mntmLoadSession);
		
		mnFile.add(new JSeparator());
		
		JMenuItem mntmOpenACapture = new JMenuItem("Open a Capture File (*.pcap)");
		mnFile.add(mntmOpenACapture);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mnFile.add(mntmExit);
		
		JMenu mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);
		
		JMenuItem mntmCopySelectedRequest = new JMenuItem("Copy Selected Request to Clipboard");
		mnEdit.add(mntmCopySelectedRequest);
		
		JMenuItem mntmCopyAllRequests = new JMenuItem("Copy All Requests to Clipboard");
		mnEdit.add(mntmCopyAllRequests);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		JMenuItem mntmAbout = new JMenuItem("About Cookie Cadger");
		mnHelp.add(mntmAbout);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		clientsListModel = new EnhancedListModel();
		domainsListModel = new EnhancedListModel();
		requestsListModel = new EnhancedListModel();
		sessionsListModel = new EnhancedListModel();
		interfacesListModel = new DefaultComboBoxModel<String>();
		
		tabbedPane = new JTabbedPane();
		tabbedPane.setBounds(28, 48, 894, 416);

		requestsPanel = new JPanel();
		requestsPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		requestsPanel.setLayout(null);
		
		tabbedPane.addTab("Requests", null, requestsPanel, null);
		
		if(bUseSessionDetection)
		{
			sessionsPanel = new JPanel();
			sessionsPanel.setLayout(null);

			tabbedPane.addTab("Recognized Sessions", null, sessionsPanel, null);
		
			JScrollPane sessionsScrollPane = new JScrollPane();
			sessionsScrollPane.setBounds(2, 2, 886, 320);
			sessionsPanel.add(sessionsScrollPane);
			
			sessionsList = new EnhancedJList();
			sessionsList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
			sessionsList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			sessionsList.setVisibleRowCount(-1);
			sessionsList.setModel(sessionsListModel);
			
			sessionsScrollPane.setViewportView(sessionsList);
			
			JCheckBox chckbxAutomaticallyLoadSessions = new JCheckBox("Automatically Load Sessions Into Browser (Demo Mode)");
			chckbxAutomaticallyLoadSessions.setBounds(12, 332, 420, 25);
			chckbxAutomaticallyLoadSessions.setName("chckbxAutomaticallyLoadSessions");
			
			if(bUseDemoModeSpecified)
			{
				System.out.println("DEMO SPECIFIED: " + bUseDemoMode);
				chckbxAutomaticallyLoadSessions.setSelected(bUseDemoMode);
			}
			sessionsPanel.add(chckbxAutomaticallyLoadSessions);
			
			JButton btnLoadSelectedSession = new JButton("Load Selected Session");
			btnLoadSelectedSession.setBounds(657, 332, 220, 25);
			sessionsPanel.add(btnLoadSelectedSession);
			
			btnLoadSelectedSession.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
			    	SwingWorker<?, ?> loadSessionWorker = new SwingWorker<Object, Object>() {            
			        	@Override
			            public Object doInBackground()
			        	{
							EnhancedJListItem listItem = (EnhancedJListItem)sessionsList.getSelectedValue();
							int sessionID = listItem.getID();
							
							try
							{
								int requestID = dbInstance.getIntegerValue("sessions", "request_id", "id", Integer.toString(sessionID));
								int domainID = dbInstance.getIntegerValue("requests", "domain_id", "id", Integer.toString(requestID));
								String domain = dbInstance.getStringValue("domains", "name", "id", Integer.toString(domainID));
								String useragent = dbInstance.getStringValue("requests", "useragent", "id", Integer.toString(requestID));
								String referer = dbInstance.getStringValue("requests", "referer", "id", Integer.toString(requestID));
								String authorization = dbInstance.getStringValue("requests", "authorization", "id", Integer.toString(requestID));
								String cookies = dbInstance.getStringValue("requests", "cookies", "id", Integer.toString(requestID));
								String sessionUri = dbInstance.getStringValue("sessions", "session_uri", "id", Integer.toString(sessionID));
								String uri;
								
								if(sessionUri != null && !sessionUri.isEmpty())
								{
									uri = sessionUri;
								}
								else
								{
									uri = dbInstance.getStringValue("requests", "uri", "id", Integer.toString(requestID));
								}

								LoadRequestIntoBrowser(domain, uri, useragent, referer, cookies, authorization);
							} catch (Exception e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			        		
			                return null;
			            }
			        };
			        loadSessionWorker.execute();
				}
			});
			
			sessionsPopup = new JPopupMenu();
			JMenuItem sessionsMenuItem = new JMenuItem("View Associated Request");
			sessionsMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(!sessionsList.isSelectionEmpty())
					{
						int sessionID = ((EnhancedJListItem)sessionsList.getSelectedValue()).getID();

						try
						{
							final int requestID = dbInstance.getIntegerValue("sessions", "request_id", "id", Integer.toString(sessionID));
							final int domainID = dbInstance.getIntegerValue("requests", "domain_id", "id", Integer.toString(requestID));
							final int clientID = dbInstance.getIntegerValue("requests", "client_id", "id", Integer.toString(requestID));
							
							SwingUtilities.invokeLater(new Runnable()
							{
							    public void run()
							    {
							    	// Select the tab
							        tabbedPane.setSelectedIndex(0);

									SwingUtilities.invokeLater(new Runnable()
									{
									    public void run()
									    {
									    	// Select the client
									    	if(clientsListModel.contains(clientID))
									    	{
									    		clientsList.setSelectedValue(clientsListModel.getElementById(clientID), true);
									    		
												SwingUtilities.invokeLater(new Runnable()
												{
												    public void run()
												    {												    	
												    	// Select the domain												    	
												    	if(domainsListModel.contains(domainID))
												    	{
												    		domainsList.setSelectedValue(domainsListModel.getElementById(domainID), true);
												    		
															SwingUtilities.invokeLater(new Runnable()
															{
															    public void run()
															    {
															    	// Select the request
															    	if(requestsListModel.contains(requestID))
															    	{
															    		requestsList.setSelectedValue(requestsListModel.getElementById(requestID), true);
															    	}
															    }
															});
												    	}
												    }
												});
									    	}
									    }
									});
							    }
							});
						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
					}
				}
			});
		    sessionsPopup.add(sessionsMenuItem);
		    
		    sessionsList.addMouseListener(new MouseAdapter()
		    {
		    	public void mouseClicked(MouseEvent me)
		    	{
		            if (SwingUtilities.isRightMouseButton(me))
		            {	                
		                Point mousePosition = me.getPoint();
		                int index = sessionsList.locationToIndex(me.getPoint());
	        			Rectangle cellRect = sessionsList.getCellBounds(index, index);
	        			
	        			// If point inside rectangle
	        			if(mousePosition.x >= cellRect.getMinX() && mousePosition.x < cellRect.getMaxX() && mousePosition.y >= cellRect.getMinY() && mousePosition.y < cellRect.getMaxY())
	        			{
	    	            	// If right-clicked item is not currently selected, do that
	    	            	if( index != sessionsList.getSelectedIndex())
	    	            		sessionsList.setSelectedIndex(index);
	    	            	
	    	            	sessionsPopup.show(sessionsList, me.getX(), me.getY());
	        			}
		            }
		    	}
		    });
		}
		contentPane.add(tabbedPane);
		
		txtConsole = new JTextArea();
		txtConsole.setBackground(UIManager.getColor("Panel.background"));
		txtConsole.setFont(new Font("Verdana", Font.BOLD, 12));
		txtConsole.setEditable(false);
		
		consoleScrollPane = new JScrollPane();
		consoleScrollPane.setBounds(28, 475, 895, 130);
		contentPane.add(consoleScrollPane);
		consoleScrollPane.setViewportView(txtConsole);
		
		loadingRequestProgressBar = new JProgressBar(JProgressBar.HORIZONTAL);
	    loadingRequestProgressBar.setStringPainted(true);
	    loadingRequestProgressBar.setFont(new Font("Verdana", Font.BOLD, 22));
	    loadingRequestProgressBar.setIndeterminate(true);
	    loadingRequestProgressBar.setVisible(false);
	    loadingRequestProgressBar.setBounds(consoleScrollPane.getBounds());
	    contentPane.add(loadingRequestProgressBar);
		
		JButton btnMonitorOnSelected = new JButton("Select An Interface");
		btnMonitorOnSelected.setEnabled(false);
		btnMonitorOnSelected.setName("btnMonitorOnSelected");
		btnMonitorOnSelected.setBounds(681, 14, 240, 25);
		contentPane.add(btnMonitorOnSelected);
		
		JComboBox<String> interfaceListComboBox = new JComboBox<String>();
		interfaceListComboBox.setName("interfaceListComboBox");
		interfaceListComboBox.setBounds(28, 14, 626, 24);
		contentPane.add(interfaceListComboBox);
		
		interfaceListComboBox.setModel(interfacesListModel);
		
		JScrollPane macListScrollPanel = new JScrollPane();
		macListScrollPanel.setBounds(22, 2, 152, 328);
		requestsPanel.add(macListScrollPanel);
		
		clientsList = new EnhancedJList();
		
		clientsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		clientsList.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				if(!e.getValueIsAdjusting() && !clientsList.isSelectionEmpty())
				{
					JList<?> list = (JList<?>)e.getSource();
					String item = ((EnhancedJListItem)list.getSelectedValue()).toString();
					ChangeDomainsList(item);
				}
			}
		});

		macListScrollPanel.setViewportView(clientsList);
		SortedListModel macListModelSorted = new SortedListModel(clientsListModel);
		macListModelSorted.setSortOrder(SortOrder.ASCENDING);
		clientsList.setModel(macListModelSorted);
		
		JCheckBox chckbxOnlyShowHosts = new JCheckBox("<html>Only show hosts<br>with HTTP traffic");
		chckbxOnlyShowHosts.setName("chckbxOnlyShowHosts");
		chckbxOnlyShowHosts.setSelected(true);
		chckbxOnlyShowHosts.setBounds(2, 331, 162, 38);
		requestsPanel.add(chckbxOnlyShowHosts);
		
		JScrollPane domainListScrollPane = new JScrollPane();
		domainListScrollPane.setBounds(180, 2, 200, 366);
		requestsPanel.add(domainListScrollPane);
		
		domainsList = new EnhancedJList();
		
		domainsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		domainsList.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				if(!e.getValueIsAdjusting() && !clientsList.isSelectionEmpty() && !domainsList.isSelectionEmpty())
				{
					JList<?> list = (JList<?>)e.getSource();
					String item = ((EnhancedJListItem)list.getSelectedValue()).toString();
					String macAddress = ((EnhancedJListItem)clientsList.getSelectedValue()).toString();
					ChangeRequestsList(macAddress, item);
				}
			}
		});
		
		domainListScrollPane.setViewportView(domainsList);
		SortedListModel domainListModelSorted = new SortedListModel(domainsListModel);
		domainListModelSorted.setSortOrder(SortOrder.ASCENDING);
		domainsList.setModel(domainListModelSorted);
		
		JScrollPane requestListScrollPanel = new JScrollPane();
		requestListScrollPanel.setBounds(386, 2, 482, 336);
		requestsPanel.add(requestListScrollPanel);

		requestsList = new EnhancedJList();
		
		Font fontMonospace = new Font( "Monospaced", Font.BOLD, 12 ); 
		requestsList.setFont(fontMonospace);
		requestsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		requestsList.setModel(requestsListModel);
		requestListScrollPanel.setViewportView(requestsList);
		
		clientsPopup = new JPopupMenu();
		
		JMenuItem copyHostInfoMenuItem = new JMenuItem("Copy Host Information");
		copyHostInfoMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!clientsList.isSelectionEmpty())
				{
					Toolkit toolkit = Toolkit.getDefaultToolkit();
					Clipboard clipboard = toolkit.getSystemClipboard();
					String macAddress = ((EnhancedJListItem)clientsList.getSelectedValue()).toString();
					StringSelection strSel = new StringSelection(GenerateDescriptionForMac(macAddress, false));
					clipboard.setContents(strSel, null);
				}
			}
		});
	    clientsPopup.add(copyHostInfoMenuItem);
	    
		JMenuItem copyMacAddressMenuItem = new JMenuItem("Copy MAC Address");
		copyMacAddressMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!clientsList.isSelectionEmpty())
				{
					Toolkit toolkit = Toolkit.getDefaultToolkit();
					Clipboard clipboard = toolkit.getSystemClipboard();
					String macAddress = ((EnhancedJListItem)clientsList.getSelectedValue()).toString();
					StringSelection strSel = new StringSelection(macAddress);
					clipboard.setContents(strSel, null);
				}
			}
		});
	    clientsPopup.add(copyMacAddressMenuItem);
	    
	    clientsList.addMouseListener(new MouseAdapter()
	    {
	    	public void mouseClicked(MouseEvent me)
	    	{
	            if (SwingUtilities.isRightMouseButton(me))
	            {
	            	// Disable ToolTip for 3 seconds
	            	SwingWorker<?, ?> worker = new SwingWorker<Object, Object>() {            
	                	@Override            
	                    public Object doInBackground() {
	                        try {
	                        	ToolTipManager.sharedInstance().setEnabled(false);
	                            Thread.sleep(3000);
	                        } catch (InterruptedException e) { /*Who cares*/ }
	                        return null;
	                    }
	                    @Override
	                    public void done()
	                    {
	                    	ToolTipManager.sharedInstance().setEnabled(true);
	                    }
	                };
	                worker.execute();
	            	
	                Point mousePosition = me.getPoint();
	                int index = clientsList.locationToIndex(me.getPoint());
        			Rectangle cellRect = clientsList.getCellBounds(index, index);
        			
        			// If point inside rectangle
        			if(mousePosition.x >= cellRect.getMinX() && mousePosition.x < cellRect.getMaxX() && mousePosition.y >= cellRect.getMinY() && mousePosition.y < cellRect.getMaxY())
        			{
    	            	// If right-clicked item is not currently selected, do that
    	            	if( index != clientsList.getSelectedIndex())
    	            		clientsList.setSelectedIndex(index);
    	            	
    	            	clientsPopup.show(clientsList, me.getX(), me.getY());
        			}
	            }
	    	}
	    });
	    
		requestsPopup = new JPopupMenu();
		JMenuItem requestMenuItem = new JMenuItem("Copy Request Information");
		requestMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!requestsList.isSelectionEmpty())
				{
					Toolkit toolkit = Toolkit.getDefaultToolkit();
					Clipboard clipboard = toolkit.getSystemClipboard();
					int request = ((EnhancedJListItem)requestsList.getSelectedValue()).getID();
					StringSelection strSel = new StringSelection(GenerateDescriptionForRequest(request, false, false));				
					clipboard.setContents(strSel, null);
				}
			}
		});
	    requestsPopup.add(requestMenuItem);
	    
	    requestsList.addMouseListener(new MouseAdapter()
	    {
	    	public void mouseClicked(MouseEvent me)
	    	{
	            if (SwingUtilities.isRightMouseButton(me))
	            {
	            	// Disable ToolTip for 3 seconds
	            	SwingWorker<?, ?> worker = new SwingWorker<Object, Object>() {            
	                	@Override            
	                    public Object doInBackground() {
	                        try {
	                        	ToolTipManager.sharedInstance().setEnabled(false);
	                            Thread.sleep(3000);
	                        } catch (InterruptedException e) { /*Who cares*/ }
	                        return null;
	                    }
	                    @Override
	                    public void done()
	                    {
	                    	ToolTipManager.sharedInstance().setEnabled(true);
	                    }
	                };
	                worker.execute();
	                
	                Point mousePosition = me.getPoint();
	                int index = requestsList.locationToIndex(me.getPoint());
        			Rectangle cellRect = requestsList.getCellBounds(index, index);
        			
        			// If point inside rectangle
        			if(mousePosition.x >= cellRect.getMinX() && mousePosition.x < cellRect.getMaxX() && mousePosition.y >= cellRect.getMinY() && mousePosition.y < cellRect.getMaxY())
        			{
    	            	// If right-clicked item is not currently selected, do that
    	            	if( index != requestsList.getSelectedIndex())
    	            		requestsList.setSelectedIndex(index);
    	            	
    	            	requestsPopup.show(requestsList, me.getX(), me.getY());
        			}
	            }
	    	}
	    });
	    
		JButton btnLoadDomainCookies = new JButton("Load Domain Cookies");
		btnLoadDomainCookies.setBounds(386, 342, 234, 25);
		requestsPanel.add(btnLoadDomainCookies);
		
		JButton btnReplayRequest = new JButton("Replay This Request");
		btnReplayRequest.setBounds(633, 342, 234, 25);
		requestsPanel.add(btnReplayRequest);
		
		JLabel lblSoftwareUpdateAvailable = new JLabel("");
		lblSoftwareUpdateAvailable.setName("lblSoftwareUpdateAvailable");
		lblSoftwareUpdateAvailable.setBounds(28, 606, 895, 15);
		contentPane.add(lblSoftwareUpdateAvailable);
		
		/*
		 * Create and associate the ActionListeners for all objects
		 */
		
		mntmStartNewSession.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				ResetDataset(true);
			}
		});

		mntmOpenACapture.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				JFileChooser fc = new JFileChooser();
				FileFilter pcapFilter = new FileNameExtensionFilter("*.pcap | *.cap | *.pcapdump", "pcap", "cap", "pcapdump");
				fc.addChoosableFileFilter(pcapFilter);
				fc.setFileFilter(pcapFilter);
				int returnVal = fc.showOpenDialog(null);

		        if (returnVal == JFileChooser.APPROVE_OPTION)
		        {
		        	File file = fc.getSelectedFile();
		        	final String pcapFile = file.getAbsolutePath();
		        	JOptionPane.showMessageDialog(null, "This process could take some time, please be patient.");
		        	
			    	SwingWorker<?, ?> openCaptureWorker = new SwingWorker<Object, Object>() {            
			        	@Override            
			            public Object doInBackground()
			        	{
			        		try
							{
								consoleScrollPane.setVisible(false);
								loadingRequestProgressBar.setString("Processing capture file, please wait...");
								loadingRequestProgressBar.setVisible(true);

								StartCapture(-1, pcapFile);
								
				                loadingRequestProgressBar.setVisible(false);
				                consoleScrollPane.setVisible(true);
					                
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			        		
			                return null;
			            }
			        };
			        
			        openCaptureWorker.execute();
				}
			}
		});


		mntmSaveSession.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dbInstance.saveDatabase();
			}
		});


		mntmLoadSession.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				ResetDataset(false); // Do not re-init tables
				dbInstance.openDatabase();
		        
		    	ChangeClientsList();
		    	
		    	if(bUseSessionDetection)
		    		ChangeSessionsList();
			}
		});


		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				PrepareToCloseApplication();
				dispose();
				System.exit(0);
			}
		});


		mntmCopySelectedRequest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(requestsList.isSelectionEmpty())
				{
					JOptionPane.showMessageDialog(null, "You must first select a request.");
				}
				else
				{
					Toolkit toolkit = Toolkit.getDefaultToolkit();
					Clipboard clipboard = toolkit.getSystemClipboard();				
					int request = ((EnhancedJListItem)requestsList.getSelectedValue()).getID();
					StringSelection strSel = new StringSelection(GenerateDescriptionForRequest(request, false, false));
					clipboard.setContents(strSel, null);
				}
			}
		});


		mntmCopyAllRequests.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String allRequests = "";
				
				for (int i = 0; i < requestsListModel.getSize(); i++)
				{
					int request = ((EnhancedJListItem)requestsListModel.getElementAt(i)).getID();
					allRequests = allRequests + GenerateDescriptionForRequest(request, false, false);
					
					if( i < requestsListModel.getSize() - 1)
						allRequests = allRequests + "\r\n\r\n---\r\n\r\n";
				}
				
				Toolkit toolkit = Toolkit.getDefaultToolkit();
				Clipboard clipboard = toolkit.getSystemClipboard();
				StringSelection strSel = new StringSelection(allRequests);
				clipboard.setContents(strSel, null);	
			}
		});


		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				CookieCadgerUtils.DisplayAboutWindow();
			}
		});


		btnLoadDomainCookies.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0)
			{
				if(((EnhancedJListItem)domainsList.getSelectedValue()).getID() == -1) // "All Domains"
				{
					JOptionPane.showMessageDialog(null, "This option is not available when \"[ All Domains ]\" is selected.");
				}
				else if(!domainsList.isSelectionEmpty())
				{
			    	SwingWorker<?, ?> loadRequestWorker = new SwingWorker<Object, Object>() {            
			        	@Override            
			            public Object doInBackground()
			        	{
							try {
								int clientID = dbInstance.getIntegerValue("clients", "id", "mac_address", ((EnhancedJListItem)clientsList.getSelectedValue()).toString());
								int domainID = dbInstance.getIntegerValue("domains", "id", "name", ((EnhancedJListItem)domainsList.getSelectedValue()).toString());
								int requestID = dbInstance.getNewestRequestID(clientID, domainID);
								
								String domain = ((EnhancedJListItem)domainsList.getSelectedValue()).toString();
								String uri = "/";
								String useragent = dbInstance.getStringValue("requests", "useragent", "id", Integer.toString(requestID));
								String referer = dbInstance.getStringValue("requests", "referer", "id", Integer.toString(requestID));
								String authorization = dbInstance.getStringValue("requests", "authorization", "id", Integer.toString(requestID));
								String cookies = dbInstance.getStringValue("requests", "cookies", "id", Integer.toString(requestID));
								
								LoadRequestIntoBrowser(domain, uri, useragent, referer, cookies, authorization);
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							return null;
			            }
			        };
			        
			        loadRequestWorker.execute();
				}
			}
		});


		btnReplayRequest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0)
			{
				if(!requestsList.isSelectionEmpty())
				{
			    	SwingWorker<?, ?> loadRequestWorker = new SwingWorker<Object, Object>() {            
			        	@Override            
			            public Object doInBackground()
			        	{
							EnhancedJListItem listItem = (EnhancedJListItem)requestsList.getSelectedValue();
							int requestID = listItem.getID();
							
							try {
								int domainID = dbInstance.getIntegerValue("requests", "domain_id", "id", Integer.toString(requestID));
								String domain = dbInstance.getStringValue("domains", "name", "id", Integer.toString(domainID));
								String uri = dbInstance.getStringValue("requests", "uri", "id", Integer.toString(requestID));
								String useragent = dbInstance.getStringValue("requests", "useragent", "id", Integer.toString(requestID));
								String referer = dbInstance.getStringValue("requests", "referer", "id", Integer.toString(requestID));
								String authorization = dbInstance.getStringValue("requests", "authorization", "id", Integer.toString(requestID));
								String cookies = dbInstance.getStringValue("requests", "cookies", "id", Integer.toString(requestID));
								
								LoadRequestIntoBrowser(domain, uri, useragent, referer, cookies, authorization);
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			        		
			                return null;
			            }
			        };
			        
			        loadRequestWorker.execute();
				}
			}
		});
		
		interfaceListComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				SetCaptureButtonText();
			}
		});


		btnMonitorOnSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				int selectedInterface = ((JComboBox<?>)GetComponentByName("interfaceListComboBox")).getSelectedIndex();
				if(selectedInterface == -1)
					return;
					
				boolean bInterfaceIsCapturing = bCapturing.get(selectedInterface); // Make a copy because this value will be changing a lot...
							
				if(bInterfaceIsCapturing)
				{
					StopCapture(selectedInterface);
				}
				else
				{
					PrepCapture(selectedInterface);
					
			    	SwingWorker<?, ?> captureWorker = new SwingWorker<Object, Object>() {            
			        	@Override
			            public Object doInBackground()
			        	{
							try {
								int selectedInterface = ((JComboBox<?>)GetComponentByName("interfaceListComboBox")).getSelectedIndex();
								StartCapture(selectedInterface, "");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							return null;
			            }
			        };
			        
			        captureWorker.execute();
				}
			}
		});
		
		chckbxOnlyShowHosts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				domainsListModel.clear();
				requestsListModel.clear();
				
				// Status toggled, re-create the mac list
				ChangeClientsList();
			}
		});
		
		// Associate all components with the HashMap
		componentMap = CreateComponentMap(contentPane);
				
		// Get capture devices
		InitializeDevices();
		
		// Name and license
		Console("\n\nCookie Cadger (v"+ CookieCadgerUtils.version +")\nCreated by Matthew Sullivan - mattslifebytes.com\nThis software is freely distributed under the terms of the FreeBSD license.\n", true);
		
		// Populate the ComboBox	
		for (int i = 0; i < deviceName.size(); i++)
		{
			String interfaceText;
			
			interfaceText = deviceName.get(i) + " [" + deviceDescription.get(i) + "]";
			interfacesListModel.addElement(interfaceText);
		}
		
		// Select the best available interface (for Mac/Linux systems)
		int itemToSelect = -1;
		boolean bFinished = false;
		
		String[] interfaceNames = { "mon", "wlan", "en", "eth" };
		for (String interfaceName : interfaceNames)
		{
			for (int i = 0; i < deviceName.size(); i++)
			{
				if(deviceName.get(i).contains(interfaceName))
				{
					itemToSelect = i;
					bFinished = true;
					break;
				}
			}
			
			if(bFinished)
				break;
		}
		
		interfaceListComboBox.setSelectedIndex(itemToSelect);
		
		if(bUpdateChecking)
		{
			// Check for software update
	    	SwingWorker<?, ?> updateWorker = new SwingWorker<Object, Object>() {            
	        	@Override            
	            public Object doInBackground()
	        	{
	        		try {
	        			JLabel lblSoftwareUpdateAvailable = ((JLabel)GetComponentByName("lblSoftwareUpdateAvailable"));

	        			InetAddress ip = InetAddress.getLocalHost(); // Get an active IP
	        	        NetworkInterface network = NetworkInterface.getByInetAddress(ip); // and match to Mac Address
	        	        byte[] mac = network.getHardwareAddress();
	        	        MessageDigest shaOfMacAddress = MessageDigest.getInstance("SHA-512");
	        	        byte[] macAddressHash = shaOfMacAddress.digest(mac);

	        	        StringBuffer macAddressHashStringBuilder = new StringBuffer();
	        	        for (int i = 0; i < macAddressHash.length; i++)
	        	        {
	        	        	macAddressHashStringBuilder.append(Integer.toString((macAddressHash[i] & 0xff) + 0x100, 16).substring(1));
	        	        }
	        	        String macAddressHashString = macAddressHashStringBuilder.toString();
						String releasedVersion = readUrl("https://www.cookiecadger.com/update/?update=" + CookieCadgerUtils.version + "; " + System.getProperty("os.name") + "; " + System.getProperty("os.version") + "; " + System.getProperty("os.arch") + "; " + macAddressHashString, "Cookie Cadger, " + CookieCadgerUtils.version, "text/html", null);
						
						if(releasedVersion.length() > 0 && releasedVersion.contains("Cookie Cadger")) // String has stuff in it? Display.
						{
							lblSoftwareUpdateAvailable.setText(releasedVersion);
							lblSoftwareUpdateAvailable.setForeground(Color.BLUE);
							lblSoftwareUpdateAvailable.addMouseListener(new MouseAdapter()
							{
								@Override
								public void mouseClicked(MouseEvent e) {
									try
									{
										Desktop.getDesktop().browse(new URI("http://www.cookiecadger.com/update/"));
									}
									catch (Exception ex) {
										// Do nothing
									}
								}
							});
						}
					}
	        		catch (Exception e)
	        		{
	        			e.printStackTrace();
						// Do nothing
					}
	        		
	                return null;
	            }
	        };
	        updateWorker.execute();
		}
        // Load all plugin classes
        try
        {
			File folder = new File(executionPath + "/plugins/");
			File[] listOfFiles = folder.listFiles();
			 
			for (int i = 0; i < listOfFiles.length && listOfFiles[i].isFile(); i++)
			{ 
				String pluginClassFilename = listOfFiles[i].getName();
				if (pluginClassFilename.toLowerCase().endsWith(".js"))
				{					
					sessionDetectors.add(executionPath + "/plugins/" + pluginClassFilename);
				}
			}
        }
        catch(NullPointerException npe)
        {
        	// Do nothing
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
	}
	
	private void InitializeDevices()
	{
		File tshark;
		
		if(pathToTshark == null || pathToTshark.isEmpty()) // no program arg specified
		{
			// Get tshark location by checking likely Linux, Windows, and Mac paths
			//								// Ubuntu/Debian	// Fedora/RedHat			// BackTrack 5 R3	// Windows 32-bit							//Windows 64-bit									//Mac OS X
			String[] pathCheckStrings = {	"/usr/bin/tshark",	"/usr/local/bin/tshark",	"/usr/sbin/tshark",	"C:\\Program Files\\Wireshark\\tshark.exe",	"C:\\Program Files (x86)\\Wireshark\\tshark.exe",	"/Applications/Wireshark.app/Contents/Resources/bin/tshark" };
			
			for(String path : pathCheckStrings)
			{
				if(new File(path).exists())
				{
					Console("tshark located at " + path, false);
					pathToTshark = path;
					break;
				}
			}
		}
		else // program arg specified, check that tshark exists there
		{
			if(new File(pathToTshark).exists())
			{
				Console("tshark specified at " + pathToTshark, false);
			}
			else
			{
				JOptionPane.showMessageDialog(null, "You specified a path to 'tshark' as an argument when starting this program, but the given path is invalid.");
				pathToTshark = null; // Empty the user-specified value
			}
		}
		
		if(pathToTshark == null || pathToTshark.isEmpty())
		{
			JOptionPane.showMessageDialog(null, "Error: couldn't find 'tshark' (part of the 'Wireshark' suite). This software cannot capture or analyze packets without it.\nYou can still load previously saved sessions for replaying in the browser, but be aware you might encounter errors.\n\nYou can manually specify the location to 'tshark' as a program argument.\n\nUsage:\njava -jar CookieCadger.jar --tshark=<full path to tshark>");
		}
		else
		{
			Console("Querying tshark for capture devices; tshark output follows:", false);

			String line = "";
			try {
				ProcessBuilder pb = new ProcessBuilder(new String[] { pathToTshark, "-D" } );
				pb.redirectErrorStream(true);
				Process proc = pb.start();
				InputStream is = proc.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				
				while ((line = br.readLine()) != null)
				{
					// Print every piece of output to the console
					Console(line, false);

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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Console("Capture device search completed with " + deviceName.size() + " devices found.", false);
		}
	}

	private void CreateSession(int requestID, String userToken, String description, String profilePhotoUrl, String sessionUri)
	{	
		boolean bHaveImage = false;
		Image photo = null;
		
		// Make image
		if(profilePhotoUrl != null && !profilePhotoUrl.isEmpty())
		{
			try
			{
				URL url = new URL(profilePhotoUrl);
				photo = ImageIO.read(url);
				
				bHaveImage = true;
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				bHaveImage = false;
			}
		}
		
		try
		{
			// Why check again? Well, the querying of all this can be time consuming,
			// meaning that other requests could have already taken care of it.
			if(!dbInstance.containsValue("sessions", "user_token", userToken))
			{
				// Update DB
				int sessionID = dbInstance.createSession(requestID, userToken, description, profilePhotoUrl, sessionUri);
				
				// Add listing to UI
				EnhancedJListItem item = new EnhancedJListItem(sessionID, description, null);
				
				if(bHaveImage)
					item.setThumbnail(photo);
				
				sessionsListModel.addElement(item);
				sessionsList.performHighlight(description, Color.BLUE);
			}
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void Console(String text, boolean bAutoScroll)
	{
		if(txtConsole.getText().isEmpty())
			txtConsole.setText(text);
		else
			txtConsole.append("\n" + text);
		
		if(bAutoScroll)
			txtConsole.setCaretPosition(txtConsole.getDocument().getLength());
		
		System.out.println(text);
	}
	
	private HashMap<String, Component> CreateComponentMap(JPanel panel)
	{
		HashMap<String, Component> componentMap = new HashMap<String,Component>();
        Component[] components = panel.getComponents();
        for (int i=0; i < components.length; i++)
        {
        	if(components[i] instanceof JTabbedPane)
        	{
        		// Find the tabbed panel and iterate its components as well
        		for (Component cmp : ((JTabbedPane) components[i]).getComponents())
        		{
        			if(cmp instanceof JPanel)
        			{
        				for (Component x : ((JPanel) cmp).getComponents())
        				{
        		        	componentMap.put(x.getName(), x);
        				}
        			}
        		}
        	}
        	
       		componentMap.put(components[i].getName(), components[i]);
        }
        
        return componentMap;
	}

	private Component GetComponentByName(String name) {
        if (componentMap.containsKey(name)) {
                return (Component) componentMap.get(name);
        }
        else return null;
	}
	
	public static String readUrl(String urlString, String userAgent, String accept, String cookies) throws Exception
	{
	    BufferedReader reader = null;
	    try {
	        URL url = new URL(urlString);
	        URLConnection urlConn = url.openConnection();
	        
	        if(cookies != null && !cookies.isEmpty())
	        	urlConn.setRequestProperty("Cookie", cookies);
	        
	        urlConn.setRequestProperty("User-Agent", userAgent);
	        urlConn.setRequestProperty("Accept", accept + ", " + Integer.toString(localRandomization));
	        
	        reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
	        StringBuffer buffer = new StringBuffer();
	        int read;
	        char[] chars = new char[1024];
	        while ((read = reader.read(chars)) != -1)
	            buffer.append(chars, 0, read); 

	        return buffer.toString();
	    } catch (FileNotFoundException ex) {
	    	// Nothing
	    } finally {
	        if (reader != null)
	            reader.close();
	    }
	    
	    return "";
	}
}