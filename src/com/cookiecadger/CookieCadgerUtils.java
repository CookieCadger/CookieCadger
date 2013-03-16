package com.cookiecadger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

public class CookieCadgerUtils
{
	public static final String version = "1.00";
	public static HashMap<String, Object> programSettings;
	private static Preferences prefs = null;
	private static final int localRandomization = 1000 + (int)(Math.random() * ((20110 - 1000) + 1));;
	
	//														// Ubuntu/Debian	// Fedora/RedHat			// BackTrack 5 R3	// Windows 32-bit							//Windows 64-bit									//Mac OS X
	public final static String[] knownTsharkLocations = {	"/usr/bin/tshark",	"/usr/local/bin/tshark",	"/usr/sbin/tshark",	"C:\\Program Files\\Wireshark\\tshark.exe",	"C:\\Program Files (x86)\\Wireshark\\tshark.exe",	"/Applications/Wireshark.app/Contents/Resources/bin/tshark" };
	
	public static enum browserChoices {
	    FIREFOX("Mozilla Firefox"), CHROME("Google Chrome");
	    private final String display;
	    private browserChoices (String s) {
	        display = s;
	    }
	    @Override
	    public String toString() {
	        return display;
	    }
	}
	
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
	    SQLITE("SQLite"), MYSQL("MySQL (works with multiple instances)");
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
			prefs = Preferences.userRoot().node(com.cookiecadger.CookieCadgerUtils.class.getName());
		}
		
		programSettings = new HashMap<String,Object>();
		
		programSettings.put("dbEngine", prefs.get("dbEngine", "sqlite"));
		programSettings.put("databaseHost", prefs.get("databaseHost", ""));
		programSettings.put("databaseUser", prefs.get("databaseUser", ""));
		programSettings.put("databasePass", prefs.get("databasePass", ""));
		programSettings.put("databaseName", prefs.get("databaseName", ""));
		programSettings.put("databaseRefreshRate", prefs.getInt("databaseRefreshRate", 15));
	}
	
	public static void saveApplicationPreferences()
	{
		for (Iterator<Map.Entry<String, Object>> it = programSettings.entrySet().iterator(); it.hasNext();)
		{
		    Map.Entry<String, Object> entry = it.next();
		    String key = entry.getKey();
		    Object value = entry.getValue();
		    
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
	}
	
	public static int getLocalRandomization()
	{
		return localRandomization;
	}
	
	public static void displayAboutWindow()
	{
		JOptionPane.showMessageDialog(null, "Cookie Cadger (v"+ version +")\n\n" +
				"Copyright (c) 2012, Matthew Sullivan <MattsLifeBytes.com / @MattsLifeBytes>\n" +
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
				"3. By using this software, you agree to provide the Software Creator (Matthew\n" +
				"   Sullivan) exactly one drink of his choice under $10 USD in value if he\n" +
				"   requests it of you.\n" +
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
