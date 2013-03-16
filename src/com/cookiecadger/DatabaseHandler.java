package com.cookiecadger;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class DatabaseHandler
{
	private Connection dbInstance;
	private String executionPath;
	private String dbEngine = null;
	private String lastInsertIdFunction = null;
	
	public DatabaseHandler() throws Exception
	{
		executionPath = System.getProperty("user.dir").replace("\\", "/");
		dbEngine = (String)CookieCadgerUtils.programSettings.get("dbEngine");
		
		if(dbEngine.equals("mysql"))
		{
			lastInsertIdFunction = "last_insert_id()";
			
			Class.forName("com.mysql.jdbc.Driver");
			
			String databaseHost = (String)CookieCadgerUtils.programSettings.get("databaseHost");
			String databaseUser = (String)CookieCadgerUtils.programSettings.get("databaseUser");
			String databasePass = (String)CookieCadgerUtils.programSettings.get("databasePass");
			String databaseName = (String)CookieCadgerUtils.programSettings.get("databaseName");
			dbInstance = DriverManager.getConnection("jdbc:mysql://" + databaseHost + "/" + databaseName + "?user="+ databaseUser +"&password=" + databasePass);
		}
		else
		{
			// Default back to SQLite
			
			lastInsertIdFunction = "last_insert_rowid()";			
			Class.forName("org.sqlite.JDBC");
			dbInstance = DriverManager.getConnection("jdbc:sqlite:" + executionPath + "/session.sqlite");
			clearTables();
		}
		
		// Cleans up the temporary DB on exit
		File sessionDB = new File(executionPath + "/session.sqlite");
		sessionDB.deleteOnExit();
		
		initTables();
	}
	
	public void clearTables() throws SQLException
	{
		Statement stat = dbInstance.createStatement();
		
		// Clear all tables
	    stat.executeUpdate("drop table if exists requests;");
	    stat.executeUpdate("drop table if exists clients;");
	    stat.executeUpdate("drop table if exists domains;");
	    stat.executeUpdate("drop table if exists sessions;");
	    
	    stat.close();
	}
	
	public void initTables() throws SQLException
	{
		Statement stat = dbInstance.createStatement();
		
		if(dbEngine.equals("sqlite"))
		{
		    stat.executeUpdate("create table requests (id INTEGER PRIMARY KEY, timerecorded INTEGER, uri VARCHAR, useragent VARCHAR, referer VARCHAR, cookies VARCHAR, authorization VARCHAR, auth_basic VARCHAR, description VARCHAR, domain_id INTEGER, client_id INTEGER);");
		    stat.executeUpdate("create table clients (id INTEGER PRIMARY KEY, mac_address VARCHAR, ipv4_address VARCHAR, ipv6_address VARCHAR, netbios_hostname VARCHAR, mdns_hostname VARCHAR, has_http_requests INTEGER);");
		    stat.executeUpdate("create table domains (id INTEGER PRIMARY KEY, name VARCHAR);");
		    stat.executeUpdate("create table sessions (id INTEGER PRIMARY KEY, user_token VARCHAR, description VARCHAR, profile_photo_url VARCHAR, session_uri VARCHAR, request_id INTEGER);");	    	
	    }
	    else
	    {
		    stat.executeUpdate("create table if not exists requests (id INTEGER AUTO_INCREMENT PRIMARY KEY, timerecorded INTEGER, uri VARCHAR(4096), useragent VARCHAR(256), referer VARCHAR(4096), cookies VARCHAR(4096), authorization VARCHAR(256), auth_basic VARCHAR(256), description VARCHAR(4096), domain_id INTEGER, client_id INTEGER);");
		    stat.executeUpdate("create table if not exists clients (id INTEGER AUTO_INCREMENT PRIMARY KEY, mac_address VARCHAR(24), ipv4_address VARCHAR(16), ipv6_address VARCHAR(64), netbios_hostname VARCHAR(128), mdns_hostname VARCHAR(128), has_http_requests INTEGER);");
		    stat.executeUpdate("create table if not exists domains (id INTEGER AUTO_INCREMENT PRIMARY KEY, name VARCHAR(128));");
		    stat.executeUpdate("create table if not exists sessions (id INTEGER AUTO_INCREMENT PRIMARY KEY, user_token VARCHAR(128), description VARCHAR(512), profile_photo_url VARCHAR(4096), session_uri VARCHAR(4096), request_id INTEGER);");
	    }
		
	    stat.close();
	}
	
	public boolean containsValue(String table, String field, String value) throws SQLException
	{
	    Statement stat = dbInstance.createStatement();
	    ResultSet rs = stat.executeQuery("select count(id) as r_count from " + table + " where " + field + " = '" + value + "'");
	    rs.next();
	    
	    if(rs.getInt("r_count") > 0)
	    {
	    	rs.close();
	    	stat.close();
	    	return true;
	    }
	    
	    rs.close();
	    stat.close();
	    return false;
	}
	
	public int getNewestRequestID(int client_id, int domain_id) throws SQLException
	{
	    Statement stat = dbInstance.createStatement();
	    ResultSet rs = stat.executeQuery("select id from requests where client_id = '" + Integer.toString(client_id) + "' and domain_id = '" + Integer.toString(domain_id) + "' order by id desc limit 1");

	    rs.next();
	    int value = rs.getInt("id");
	    rs.close();
	    stat.close();
	    
	    return value;
	}
	
	public int getIntegerValue(String table, String fieldToGet, String fieldToMatchAgainst, String valueToMatchAgainst) throws SQLException
	{
	    Statement stat = dbInstance.createStatement();
	    ResultSet rs = stat.executeQuery("select " + fieldToGet + " from " + table + " where " + fieldToMatchAgainst + " = '" + valueToMatchAgainst + "'");

	    rs.next();
	    int value = rs.getInt(fieldToGet);
	    rs.close();
	    stat.close();
	    
	    return value;
	}
	
	public void setStringValue(String table, String fieldToSet, String valueToSet, String fieldToMatchAgainst, String valueToMatchAgainst) throws SQLException
	{
	    Statement stat = dbInstance.createStatement();
	    
	    stat.executeUpdate("update " + table + " set " + fieldToSet + " = '" + valueToSet + "' where " + fieldToMatchAgainst + " = '" + valueToMatchAgainst + "'");
	    stat.close();
	}
	
	public String getStringValue(String table, String fieldToGet, String fieldToMatchAgainst, String valueToMatchAgainst) throws SQLException
	{
	    Statement stat = dbInstance.createStatement();
	    ResultSet rs = stat.executeQuery("select " + fieldToGet + " from " + table + " where " + fieldToMatchAgainst + " = '" + valueToMatchAgainst + "'");

	    rs.next();
	    String value = rs.getString(fieldToGet);
	    rs.close();
	    stat.close();
	    
	    return value;
	}
	
	public HashMap<String, String> getStringValue(String table, String[] fieldToGet, String fieldToMatchAgainst, String valueToMatchAgainst) throws SQLException
	{
		HashMap<String, String> resultMap = new HashMap<String,String>();
		
		String fieldNames = "";
		for (int i = 0; i < fieldToGet.length; i++)
		{
			fieldNames = fieldNames + fieldToGet[i];
			
			if( i + 1 < fieldToGet.length)
			{
				fieldNames = fieldNames + ",";
			}
		}

	    Statement stat = dbInstance.createStatement();
	    ResultSet rs = stat.executeQuery("select " + fieldNames + " from " + table + " where " + fieldToMatchAgainst + " = '" + valueToMatchAgainst + "'");

	    rs.next();
	    
	    for(String field : fieldToGet)
	    {
	    	resultMap.put(field, rs.getString(field));
	    }
	    
	    rs.close();
	    stat.close();
	    
	    return resultMap;
	}
	
	// return the ID of the newly created client
	public int createClient(String macAddress) throws SQLException
	{
	    PreparedStatement prep = dbInstance.prepareStatement("insert into clients values(NULL,?,?,?,?,?,?);");
	    prep.setString(1, macAddress);
	    prep.setString(2, "");
	    prep.setString(3, "");
	    prep.setString(4, "");
	    prep.setString(5, "");
	    prep.setBoolean(6, false);
	    prep.addBatch();
	    prep.executeBatch();
		
	    Statement stat = dbInstance.createStatement();
	    ResultSet rs = stat.executeQuery("select " + lastInsertIdFunction + ";");

	    rs.next();
	    int value = rs.getInt(lastInsertIdFunction);
	    rs.close();
	    stat.close();
	    prep.close();
	    
	    return value;
	}

	public int createSession(int requestID, String userToken, String description, String profilePhotoURL, String sessionURI) throws SQLException
	{
		PreparedStatement prep = dbInstance.prepareStatement("insert into sessions values(NULL,?,?,?,?,?);");
	    prep.setString(1, userToken);
	    prep.setString(2, description);
	    prep.setString(3, profilePhotoURL);
	    prep.setString(4, sessionURI);
	    prep.setInt(5, requestID);
	    prep.addBatch();
	    prep.executeBatch();
	    
	    Statement stat = dbInstance.createStatement();	    
	    ResultSet rs = stat.executeQuery("select " + lastInsertIdFunction + ";");

	    rs.next();
	    int value = rs.getInt(lastInsertIdFunction);
	    rs.close();
	    stat.close();
	    prep.close();
	    
	    return value;
	}
	
	// return the ID of the newly created client
	public int createDomain(String name) throws SQLException
	{
	    PreparedStatement prep = dbInstance.prepareStatement("insert into domains values(NULL,?);");
	    prep.setString(1, name);
	    prep.addBatch();
	    prep.executeBatch();
	    
	    Statement stat = dbInstance.createStatement();	    
	    ResultSet rs = stat.executeQuery("select " + lastInsertIdFunction + ";");

	    rs.next();
	    int value = rs.getInt(lastInsertIdFunction);
	    rs.close();
	    stat.close();
	    prep.close();
	    
	    return value;
	}
	
	// return the ID of the newly created request
	public int createRequest(String uri, String useragent, String referer, String cookies, String authorization, String authBasic, int domain_id, int client_id) throws SQLException
	{
		long unixTime = System.currentTimeMillis() / 1000L;
		try {			
			uri = URLDecoder.decode(uri, "UTF-8");
		} catch (Exception e) {
			// Invalid, but nothing we can do about it
		}
		
		try {			
			referer = URLDecoder.decode(referer, "UTF-8");
		} catch (Exception e) {
			// Invalid, but nothing we can do about it
		}
		
	    PreparedStatement prep = dbInstance.prepareStatement("insert into requests values(NULL,?,?,?,?,?,?,?,NULL,?,?);");
	    prep.setLong(1, unixTime);
	    prep.setString(2, uri);
	    prep.setString(3, useragent);
	    prep.setString(4, referer);
	    prep.setString(5, cookies);
	    prep.setString(6, authorization);
	    prep.setString(7, authBasic);
	    prep.setInt(8, domain_id);
	    prep.setInt(9, client_id);
	    prep.addBatch();
	    prep.executeBatch();
	 
	    Statement stat = dbInstance.createStatement();
	    ResultSet rs = stat.executeQuery("select " + lastInsertIdFunction + ";");

	    rs.next();
	    int value = rs.getInt(lastInsertIdFunction);
	    rs.close();
	    stat.close();
	    prep.close();
	    
	    return value;
	}
	
	public String[] getMacs(boolean bOnlyHostsWithData, String searchString) throws SQLException
	{
		String criteria = "1";
		
		if(bOnlyHostsWithData)
		{
			criteria = "has_http_requests = 1";
		}
		
		if(searchString != null && searchString.length() > 0)
		{
			criteria = criteria + " AND mac_address LIKE '%" + searchString + "%'";
		}

		Statement stat = dbInstance.createStatement();
	    ResultSet rs = stat.executeQuery("select mac_address from clients where " + criteria + ";");

	    String[] value = toStringArray(rs, "mac_address");
	    
	    rs.close();
	    stat.close();
	    
	    return value;
	}
	
	public String[] getUserAgents(String macAddress) throws SQLException
	{
		Statement stat = dbInstance.createStatement();
	    ResultSet rs = stat.executeQuery("select distinct r.useragent from requests r inner join clients c on c.id = r.client_id where c.mac_address = '" + macAddress + "';");

	    String[] value = toStringArray(rs, "useragent");
	    rs.close();
	    stat.close();
	    
	    return value;
	}
	
	public EnhancedJListItem[] getSessions() throws SQLException
	{
		Statement stat = dbInstance.createStatement();
	    ResultSet rs = stat.executeQuery("select count(id) as count from sessions where 1;");
	    
	    rs.next();
	    int numSessions = rs.getInt("count");
	    
	    EnhancedJListItem[] items = new EnhancedJListItem [numSessions];
	    int i = 0;
	    
		stat = dbInstance.createStatement();
	    rs = stat.executeQuery("select id, description, profile_photo_url from sessions where 1;");

	    while (rs.next())
	    {
	        // Get the data from the row using the column index
	    	int id = rs.getInt("id");
	        String description = rs.getString("description");
	        String profile_photo_url = rs.getString("profile_photo_url");
	        
	        items[i] = new EnhancedJListItem(id, description, null);
	        items[i].setProfileImageURL(profile_photo_url);
	        i++;
	    }
	    
	    rs.close();
	    stat.close();
	    
	    return items;
	}
	
	public String[] getDomains(String macAddress, String searchString) throws SQLException
	{
		String criteria = "";
		
		if(searchString != null && searchString.length() > 0)
		{
			criteria = "AND d.name LIKE '%" + searchString + "%'";
		}
		
		Statement stat = dbInstance.createStatement();
	    ResultSet rs = stat.executeQuery("select distinct d.name from domains d inner join requests r on d.id = r.domain_id inner join clients c on c.id = r.client_id where c.mac_address = '" + macAddress + "' " + criteria + ";");

	    String[] value = toStringArray(rs, "name");
	    rs.close();
	    stat.close();
	    
	    return value;
	}
	
	public int getDomainCount(String macAddress) throws SQLException
	{		
		Statement stat = dbInstance.createStatement();
	    ResultSet rs = stat.executeQuery("select count(distinct d.name) as num_domains from domains d inner join requests r on d.id = r.domain_id inner join clients c on c.id = r.client_id where c.mac_address = '" + macAddress + "';");

	    rs.next();
	    int numDomains = rs.getInt("num_domains");
	    rs.close();
	    stat.close();
	    
	    return numDomains;
	}
	
	public int getClientCount() throws SQLException
	{
		Statement stat = dbInstance.createStatement();
	    ResultSet rs = stat.executeQuery("select count(id) as num_clients from clients where 1;");

	    rs.next();
	    int numDomains = rs.getInt("num_clients");
	    rs.close();
	    stat.close();
	    
	    return numDomains;
	}
	
	public String[] getCookies(String id) throws SQLException
	{
		Statement stat = dbInstance.createStatement();
	    ResultSet rs = stat.executeQuery("select cookies from requests where id = '" + id + "';");

	    String[] value = toStringArray(rs, "cookies");
	    rs.close();
	    stat.close();
	    
	    return value;
	}
	
	public ArrayList<ArrayList> getRequests(String macAddress, String domain, String searchString) throws SQLException
	{
		String criteria = "";
		
		if(searchString != null && searchString.length() > 0)
		{
			criteria = "AND r.uri LIKE '%" + searchString + "%'";
		}
		
		ArrayList<ArrayList> request_list = new ArrayList<ArrayList>();
		
		Statement stat = dbInstance.createStatement();
	    ResultSet rs = stat.executeQuery("select r.id, r.timerecorded, r.uri, r.description from requests r inner join domains d on r.domain_id = d.id inner join clients c on c.id = r.client_id where c.mac_address = '" + macAddress + "' AND d.name LIKE '" + domain + "' " + criteria + ";");
	    
	    ArrayList<String> ids = new ArrayList<String>();
	    ArrayList<String> timerecordeds = new ArrayList<String>();
	    ArrayList<String> uris = new ArrayList<String>();
	    ArrayList<String> descriptions = new ArrayList<String>();
	    
	    while (rs.next())
	    {
	      ids.add(rs.getString("id"));
	      timerecordeds.add(rs.getString("timerecorded"));
	      uris.add(rs.getString("uri"));
	      descriptions.add(rs.getString("description"));
	    }
	    rs.close();
	    
	    request_list.add(ids);
	    request_list.add(timerecordeds);
	    request_list.add(uris);
	    request_list.add(descriptions);
	    
	    return request_list;
	}
	
	public void closeDatabase() throws SQLException
	{
		dbInstance.close();
	}
	
	public void saveDatabase()
	{	
		JFileChooser fc = new JFileChooser();
		FileFilter pcapFilter = new FileNameExtensionFilter("*.sqlite", "sqlite");
		fc.addChoosableFileFilter(pcapFilter);
		fc.setFileFilter(pcapFilter);

		int returnVal = fc.showSaveDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            File fileToSaveTo = fc.getSelectedFile();
            if(!fileToSaveTo.getPath().toLowerCase().endsWith(".sqlite") && fc.getFileFilter().equals(pcapFilter))
            {
            	fileToSaveTo = new File(fileToSaveTo.getPath() + ".sqlite");
            }
            File fileToSaveFrom = new File(executionPath + "/session.sqlite");
            
            try {
				copy(fileToSaveFrom, fileToSaveTo);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}
	
	public void openDatabase()
	{	
		JFileChooser fc = new JFileChooser();
		FileFilter pcapFilter = new FileNameExtensionFilter("*.sqlite", "sqlite");
		fc.addChoosableFileFilter(pcapFilter);
		fc.setFileFilter(pcapFilter);

		int returnVal = fc.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
        	File fileToSaveFrom = fc.getSelectedFile();
            File fileToSaveTo = new File(executionPath + "/session.sqlite");
            
            try {
				copy(fileToSaveFrom, fileToSaveTo);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}
	
	/**
	 * Returns a String array containing the data from the column specified 
	 * by index name (i.e. "quantity", "price", etc.) of the ResultSet instance.
	 * http://www.brilliantsheep.com/converting-database-resultset-to-string-array-in-java/ 
	 */
	private String[] toStringArray(ResultSet resultSet, String columnLabel)
	{
	    LinkedList<String> resultList = new LinkedList<String>();
	 
	    try {
	        while (resultSet.next())
	        {
	            resultList.add(resultSet.getString(columnLabel));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	 
	    return resultList.toArray(new String[0]);
	}
	
	void copy(File src, File dst) throws IOException {
	    InputStream in = new FileInputStream(src);
	    OutputStream out = new FileOutputStream(dst);

	    // Transfer bytes from in to out
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
	        out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
	}

}
