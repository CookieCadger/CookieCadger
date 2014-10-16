package com.cookiecadger;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import java.sql.SQLException;
import java.text.DateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;

public class Utils
{
	public static final String version = "1.08";
	public static String executionPath = System.getProperty("user.dir").replace("\\", "/");
	public static HashMap<String, Object> programSettings;
	private static Preferences prefs = null;
	
	private static Random rand = new Random();
	private static final int localRandomization = 1000 + rand.nextInt(99999); // We add 1000 to ensure always >= 4 digits
	
	public static CookieCadgerFrame cookieCadgerFrame = null;
	public static String dbEngine = null;
	public static boolean bUsingExternalDatabase = false;
	public static DatabaseHandler dbInstance = null;
	
	//														// Ubuntu/Debian	// Fedora/RedHat			// BackTrack 5 R3	// Windows 32-bit							//Windows 64-bit									//Mac OS X
	public final static String[] knownTsharkLocations = {	"/usr/bin/tshark",	"/usr/local/bin/tshark",	"/usr/sbin/tshark",	"C:\\Program Files\\Wireshark\\tshark.exe",	"C:\\Program Files (x86)\\Wireshark\\tshark.exe",	"/Applications/Wireshark.app/Contents/Resources/bin/tshark" };
	
	public static enum sessionDetectionChoices {
	    PROMPT("Always prompt"), YES("Yes"), NO("No");
	    private final String display;
	    private sessionDetectionChoices (String s) {
	        display = s;
	    }
	    @Override
	    public String toString() {
	        return display;
	    }
	}
	
	public static enum databaseEngineChoices {
	    SQLITE("SQLite (file-based, high performance)"), MYSQL("MySQL (works with multiple instances)");
	    private final String display;
	    private databaseEngineChoices (String s) {
	        display = s;
	    }
	    @Override
	    public String toString() {
	        return display;
	    }
	}
	
	public static void loadApplicationPreferences()
	{
		// Retrieve the user preference node
		if(prefs == null)
		{
			prefs = Preferences.userRoot().node(com.cookiecadger.Utils.class.getName());
		}
		
		programSettings = new HashMap<String,Object>();
		
		// Load all configuration file preferences in
		// Note that these might later be overridden by command-line options
		
		// Database
		programSettings.put("dbEngine", prefs.get("dbEngine", "sqlite"));
		programSettings.put("databaseHost", prefs.get("databaseHost", ""));
		programSettings.put("databaseUser", prefs.get("databaseUser", ""));
		programSettings.put("databasePass", prefs.get("databasePass", ""));
		programSettings.put("databaseName", prefs.get("databaseName", ""));
		programSettings.put("databaseRefreshRate", prefs.getInt("databaseRefreshRate", 15));
		
		// Program preferences
		programSettings.put("tsharkPath", prefs.get("tsharkPath", ""));
		programSettings.put("interfaceNum", prefs.getInt("interfaceNum", -1));
		
		// Session detection? -1 = undefined, 0 = no, 1 = yes
		programSettings.put("bSessionDetection", prefs.getInt("bSessionDetection", -1));
		
		// Everything else
		programSettings.put("bUseDemoMode", prefs.getBoolean("bUseDemoMode", false));
		programSettings.put("bCheckForUpdates", prefs.getBoolean("bCheckForUpdates", true));
		programSettings.put("bHeadless", prefs.getBoolean("bHeadless", false));
	}
	
	public static void savePreference(String key, Object value)
	{
	    if(value instanceof String)
	    {
	    	prefs.put(key, (String)value);
	    }
	    else if(value instanceof Boolean)
	    {
	    	prefs.putBoolean(key, (Boolean)value);
	    }
	    else if(value instanceof Integer)
	    {
	    	prefs.putInt(key, (Integer)value);
	    }
	}
	
	public static String getPreference(String key, String defaultValue)
	{
		return prefs.get(key, defaultValue);
	}
	
	public static Integer getPreference(String key, Integer defaultValue)
	{
		return prefs.getInt(key, defaultValue);
	}
	
	public static boolean getPreference(String key, Boolean defaultValue)
	{
		return prefs.getBoolean(key, defaultValue);
	}
	
	public static void initializeDatabase()
	{
		dbEngine = (String)Utils.programSettings.get("dbEngine");
		bUsingExternalDatabase = !dbEngine.equals("sqlite");
		
		try
		{
			dbInstance = new DatabaseHandler();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();

			if(cookieCadgerFrame != null)
			{
				JOptionPane.showMessageDialog(cookieCadgerFrame,
						"Cookie Cadger will not operate correctly with the current database settings. Expect errors.\nPlease check your settings and ensure the database server is working.\nThe error was:\n\n" + ex.getMessage(),
						"Database Failed to Initalize!",
						JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				Utils.consoleMessage("\n\nDatabase failed to initalize, Cookie Cadger is unable to load.\nException information is shown above.");
				System.exit(1);
			}
		}
	}
	
	public static int getLocalRandomization()
	{
		return localRandomization;
	}
	
	public static void displayAboutWindow()
	{
		JOptionPane.showMessageDialog(null, "Cookie Cadger (v"+ version +", https://cookiecadger.com)\n\n" +
				"Copyright (c) 2013, Matthew Sullivan <MattsLifeBytes.com / @MattsLifeBytes>\n" +
				"\n" +
				"Additional portions generously contributed by:\n" +
				" - Ben Holland <https://github.com/benjholla>\n" +
				" - Justin Kaufman <akaritakai@gmail.com>\n" +
				"\n" +
				"All rights reserved.\n" +
				"\n" +
				"Redistribution and use in source and binary forms, with or without\n" +
				"modification, are permitted provided that the following conditions are met: \n" +
				"\n" +
				"1. Redistributions of source code must retain the above copyright notice, this\n" +
				"   list of conditions and the following disclaimer. \n" +
				"2. Redistributions in binary form must reproduce the above copyright notice,\n" +
				"   this list of conditions and the following disclaimer in the documentation\n" +
				"   and/or other materials provided with the distribution. \n" +
				"\n" +
				"THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND\n" +
				"ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED\n" +
				"WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE\n" +
				"DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR\n" +
				"ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES\n" +
				"(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;\n" +
				"LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND\n" +
				"ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT\n" +
				"(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS\n" +
				"SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE."
				);
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
	        urlConn.setRequestProperty("Accept", accept + ";" + Integer.toString(localRandomization));
	        
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
	
	public static void consoleMessage(String text)
	{
		if(cookieCadgerFrame != null)
		{
			cookieCadgerFrame.addConsoleText(text);
		}
		
		System.out.println(text);
	}
	
	public static String generateDescriptionForRequest(int requestID, boolean bUseHTML, boolean bTruncate)
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
			String[] fields = new String[] { "timerecorded", "useragent", "referer", "auth_basic", "cookies", "uri", "domain_id", "client_id" };
			HashMap<String,String> resultMap = Utils.dbInstance.getStringValue("requests", fields, "id", Integer.toString(requestID));
			
			String timeRecorded = resultMap.get("timerecorded");
			String userAgent = resultMap.get("useragent");
			String referer = resultMap.get("referer");
			String authBasic = resultMap.get("auth_basic");
			String cookies = resultMap.get("cookies");
			String uri = resultMap.get("uri");
			int domainID = new Integer(resultMap.get("domain_id"));
			String domain = Utils.dbInstance.getStringValue("domains", "name", "id", Integer.toString(domainID));
			int clientID = new Integer(resultMap.get("client_id"));
			String macAddress = Utils.dbInstance.getStringValue("clients", "mac_address", "id", Integer.toString(clientID));
			
			long timeStamp = new Long(timeRecorded);
			Date then = new Date((long)timeStamp * 1000);
			String dateString = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(then);
			
			if(bTruncate && uri.length() > 90)
				uri = uri.substring(0, 86) + boldOpen + " ..." + boldClose;
			 
			String notesTxt = htmlOpen + fontOpen + boldOpen + "Date: " + boldClose + dateString;
			notesTxt = notesTxt + newLine + boldOpen + "Client MAC: " + boldClose + macAddress;
			notesTxt = notesTxt + newLine + boldOpen + "Domain: " + boldClose + domain;
			notesTxt = notesTxt + newLine + boldOpen + "Uri: " + boldClose + uri;

			
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
				
				notesTxt = notesTxt + newLine + boldOpen + "Referer: " + boldClose + referer;
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
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void handleProgramArguments(String[] args)
	{
		int i = 0;
		String arg;
		boolean bTerminate = false;

		while (i < args.length && (args[i].startsWith("-") || args[i].startsWith("/")))
		{
			arg = args[i++];

			if (arg.contains("headless"))
			{
				boolean filledRequirements = false;
				if(arg.contains("headless="))
				{
					String value = arg.split("=")[1];
					
					if(value.equals("on"))
					{
						Utils.programSettings.put("bHeadless", true);
						filledRequirements = true;
					}
					else if (value.equals("off"))
					{
						Utils.programSettings.put("bHeadless", false);
						filledRequirements = true;
					}
				}

				if(!filledRequirements)
				{
					bTerminate = true;
					System.err.println("--headless (command-line operation without a GUI) requires an 'on' or 'off' value.");
				}
			}
			else if (arg.contains("interfacenum"))
			{
				boolean filledRequirements = false;
				if(arg.contains("interfacenum="))
				{
					String value = arg.split("=")[1];
					if(value.length() > 0)
					{
						Utils.programSettings.put("interfaceNum", new Integer(value));
						filledRequirements = true;
					}
				}

				if(!filledRequirements)
				{
					System.err.println("--interfacenum (specification of capture interface) requires an interface number.");
					bTerminate = true;
				}
			}
			else if (arg.contains("tshark"))
			{
				boolean filledRequirements = false;
				if(arg.contains("tshark="))
				{
					String value = arg.split("=")[1];
					if(value.length() > 0)
					{
						filledRequirements = true;
						programSettings.put("tsharkPath", prefs.get("tsharkPath", value));
						
					}
				}

				if(!filledRequirements)
				{
					System.err.println("--tshark requires a path to the tshark binary.");
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
						Utils.programSettings.put("bSessionDetection", 1);
						filledRequirements = true;
					}
					else if (value.equals("off"))
					{
						Utils.programSettings.put("bSessionDetection", 0);
						filledRequirements = true;
					}
				}

				if(!filledRequirements)
				{
					bTerminate = true;
					System.err.println("--detection (session detection) requires an 'on' or 'off' value.");
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
						Utils.programSettings.put("bCheckForUpdates", 1);
						filledRequirements = true;
					}
					else if (value.equals("off"))
					{
						Utils.programSettings.put("bCheckForUpdates", 0);
						filledRequirements = true;
					}
				}

				if(!filledRequirements)
				{
					bTerminate = true;
					System.err.println("--update (automatic software update checks) requires an 'on' or 'off' value.");
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
						Utils.programSettings.put("bUseDemoMode", 1);
						filledRequirements = true;
					}
					else if (value.equals("off"))
					{
						Utils.programSettings.put("bUseDemoMode", 0);
						filledRequirements = true;
					}
				}

				if(!filledRequirements)
				{
					bTerminate = true;
					System.err.println("--demo (automatic loading of session into the browser) requires an 'on' or 'off' value. Session Detection must also be enabled.");
				}
			}
			else if (arg.contains("dbengine"))
			{
				boolean filledRequirements = false;
				if(arg.contains("dbengine="))
				{
					String value = arg.split("=")[1];
					
					if(value.equals("sqlite"))
					{
						Utils.programSettings.put("dbEngine", "sqlite");
						filledRequirements = true;
					}
					else if (value.equals("mysql"))
					{
						Utils.programSettings.put("dbEngine", "mysql");
						filledRequirements = true;
					}
				}

				if(!filledRequirements)
				{
					bTerminate = true;
					System.err.println("--dbengine (database engine) requires either 'sqlite' or 'mysql' as its value.");
				}
			}
			else if (arg.contains("dbhost"))
			{
				boolean filledRequirements = false;
				if(arg.contains("dbhost="))
				{
					String value = arg.split("=")[1];
					if(value.length() > 0)
					{
						Utils.programSettings.put("databaseHost", value);
						filledRequirements = true;
					}
				}

				if(!filledRequirements)
				{
					System.err.println("--dbhost (database hostname) requires a hostname for the database server.");
					bTerminate = true;
				}
			}
			else if (arg.contains("dbuser"))
			{
				boolean filledRequirements = false;
				if(arg.contains("dbuser="))
				{
					String value = arg.split("=")[1];
					if(value.length() > 0)
					{
						Utils.programSettings.put("databaseUser", value);
						filledRequirements = true;
					}
				}

				if(!filledRequirements)
				{
					System.err.println("--dbuser (database user) requires a user name for the database server.");
					bTerminate = true;
				}
			}
			else if (arg.contains("dbpass"))
			{
				boolean filledRequirements = false;
				if(arg.contains("dbpass="))
				{
					String value = arg.split("=")[1];
					if(value.length() > 0)
					{
						Utils.programSettings.put("databasePass", value);
						filledRequirements = true;
					}
				}

				if(!filledRequirements)
				{
					System.err.println("--dbpass (database password) requires a password for the database server.");
					bTerminate = true;
				}
			}
			else if (arg.contains("dbname"))
			{
				boolean filledRequirements = false;
				if(arg.contains("dbname="))
				{
					String value = arg.split("=")[1];
					if(value.length() > 0)
					{
						Utils.programSettings.put("databaseName", value);
						filledRequirements = true;
					}
				}

				if(!filledRequirements)
				{
					System.err.println("--dbname (database name) requires a database name for the database server.");
					bTerminate = true;
				}
			}
			else if (arg.contains("dbrefreshrate"))
			{
				boolean filledRequirements = false;
				if(arg.contains("dbrefreshrate="))
				{
					String value = arg.split("=")[1];
					if(value.length() > 0)
					{
						Utils.programSettings.put("databaseRefreshRate", new Integer(value));
						filledRequirements = true;
					}
				}

				if(!filledRequirements)
				{
					System.err.println("--dbrefreshrate (database automatic refresh interval) requires a refresh rate (in seconds) for updating the GUI from the database server.");
					bTerminate = true;
				}
			}
			
			if(bTerminate)
			{
				System.exit(0);
			}
		}
	}
	
	public static BufferedImage createResizedCopy(Image originalImage, int scaledWidth, int scaledHeight, boolean preserveAlpha)
    {
    	int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
    	BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
    	Graphics2D g = scaledBI.createGraphics();
    	if (preserveAlpha) {
    		g.setComposite(AlphaComposite.Src);
    	}
    	g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null); 
    	g.dispose();
    	return scaledBI;
    }
	
	// Thanks, http://www.mkyong.com/java/how-to-decompress-files-from-a-zip-file/
	public static void unZipFile(String zipFile, String outputFolder)
	{    
		byte[] buffer = new byte[1024];

	    try
	    {
	        //create output directory is not exists
	        File folder = new File(outputFolder);
	        if(!folder.exists())
	        {
	            folder.mkdir();
	        }
	        
	        //get the zip file content
	        ZipInputStream zis =
	        new ZipInputStream(new FileInputStream(zipFile));
	        //get the zipped file list entry
	        ZipEntry ze = zis.getNextEntry();
	        
	        while(ze!=null)
	        {
	            String fileName = ze.getName();
	            File newFile = new File(outputFolder + File.separator + fileName);
	            
	            //create all non exists folders
	            //else you will hit FileNotFoundException for compressed folder
	            new File(newFile.getParent()).mkdirs();
	            
	            FileOutputStream fos = new FileOutputStream(newFile);
	            
	            int len;
	            while ((len = zis.read(buffer)) > 0)
	            {
	                fos.write(buffer, 0, len);
	            }
	            
	            fos.close();
	            ze = zis.getNextEntry();
	        }
	        
	        zis.closeEntry();
	        zis.close();
	        
        }
	    catch(IOException ex)
        {
	    	ex.printStackTrace();
	    }
	}
}
