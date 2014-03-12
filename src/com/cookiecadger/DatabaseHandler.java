package com.cookiecadger;

import java.sql.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class DatabaseHandler
{
	private Connection dbInstance;
	private String userHomeDirectory;
	private String dbEngine = null;
	private String lastInsertIdFunction = null;
	
	public DatabaseHandler() throws Exception
	{
		userHomeDirectory = System.getProperty("user.home").replace("\\", "/");
		dbEngine = (String)Utils.programSettings.get("dbEngine");
		
		if(dbEngine.equals("mysql"))
		{
			lastInsertIdFunction = "last_insert_id()";
			
			Class.forName("com.mysql.jdbc.Driver");
			
			String databaseHost = (String)Utils.programSettings.get("databaseHost");
			String databaseUser = (String)Utils.programSettings.get("databaseUser");
			String databasePass = (String)Utils.programSettings.get("databasePass");
			String databaseName = (String)Utils.programSettings.get("databaseName");
			dbInstance = DriverManager.getConnection("jdbc:mysql://" + databaseHost + "/" + databaseName + "?user="+ databaseUser +"&password=" + databasePass);
		}
		else
		{
			// Default back to SQLite
			
			lastInsertIdFunction = "last_insert_rowid()";			
			Class.forName("org.sqlite.JDBC");
			dbInstance = DriverManager.getConnection("jdbc:sqlite:" + userHomeDirectory + "/session.sqlite");
			clearTables();
			
			// Cleans up the temporary DB on exit
			File sessionDB = new File(userHomeDirectory + "/session.sqlite");
			sessionDB.deleteOnExit();
		}
		
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
		    stat.executeUpdate("create table requests (id INTEGER PRIMARY KEY, timerecorded INTEGER, uri TEXT, useragent TEXT, referer TEXT, cookies TEXT, authorization VARCHAR, auth_basic VARCHAR, description VARCHAR, domain_id INTEGER, client_id INTEGER);");
		    stat.executeUpdate("create table clients (id INTEGER PRIMARY KEY, mac_address VARCHAR, ipv4_address VARCHAR, ipv6_address VARCHAR, netbios_hostname VARCHAR, mdns_hostname VARCHAR, has_http_requests INTEGER);");
		    stat.executeUpdate("create table domains (id INTEGER PRIMARY KEY, name VARCHAR);");
		    stat.executeUpdate("create table sessions (id INTEGER PRIMARY KEY, user_token VARCHAR, description VARCHAR, profile_photo_url TEXT, session_uri TEXT, request_id INTEGER);");	    	
	    }
	    else
	    {
	    	// MySQL
		    stat.executeUpdate("create table if not exists requests (id INTEGER AUTO_INCREMENT PRIMARY KEY, timerecorded INTEGER, uri TEXT, useragent TEXT, referer TEXT, cookies TEXT, authorization VARCHAR(1024), auth_basic VARCHAR(1024), description VARCHAR(4096), domain_id INTEGER, client_id INTEGER);");
		    stat.executeUpdate("create table if not exists clients (id INTEGER AUTO_INCREMENT PRIMARY KEY, mac_address VARCHAR(24), ipv4_address VARCHAR(16), ipv6_address VARCHAR(64), netbios_hostname VARCHAR(128), mdns_hostname VARCHAR(128), has_http_requests INTEGER);");
		    stat.executeUpdate("create table if not exists domains (id INTEGER AUTO_INCREMENT PRIMARY KEY, name VARCHAR(1024));");
		    stat.executeUpdate("create table if not exists sessions (id INTEGER AUTO_INCREMENT PRIMARY KEY, user_token VARCHAR(1024), description VARCHAR(512), profile_photo_url TEXT, session_uri TEXT, request_id INTEGER);");
	    }
		
	    stat.close();
	}
	
	public boolean containsValue(String table, String field, String value) throws SQLException
	{
		boolean bContainsValue = false;
		
	    PreparedStatement prep = dbInstance.prepareStatement("select count(id) as r_count from " + table + " where " + field + " = ?;");
	    prep.setString(1, value);
	    ResultSet rs = prep.executeQuery();
	    rs.next();
	    
	    if(rs.getInt("r_count") > 0)
	    {
	    	bContainsValue = true;
	    }
	    
	    rs.close();
	    prep.close();
	    
	    return bContainsValue;
	}
	
	public int getNewestRequestID(int client_id, int domain_id) throws SQLException
	{
	    PreparedStatement prep = dbInstance.prepareStatement("select id from requests where client_id = ? and domain_id = ? order by id desc limit 1;");
	    prep.setInt(1, client_id);
	    prep.setInt(2, domain_id);
	    ResultSet rs = prep.executeQuery();
	    rs.next();
	    
	    int value = rs.getInt("id");
	    rs.close();
	    prep.close();
	    
	    return value;
	}
	
	public int getIntegerValue(String table, String fieldToGet, String fieldToMatchAgainst, String valueToMatchAgainst) throws SQLException
	{
	    PreparedStatement prep = dbInstance.prepareStatement("select " + fieldToGet + " from " + table + " where " + fieldToMatchAgainst + " = ?;");
	    prep.setString(1, valueToMatchAgainst);
	    ResultSet rs = prep.executeQuery();
	    rs.next();

	    int value = rs.getInt(fieldToGet);
	    rs.close();
	    prep.close();
	    
	    return value;
	}
	
	public void setStringValue(String table, String fieldToSet, String valueToSet, String fieldToMatchAgainst, String valueToMatchAgainst) throws SQLException
	{
		PreparedStatement prep = dbInstance.prepareStatement("update " + table + " set " + fieldToSet + " = ? where " + fieldToMatchAgainst + " = ?;");
		prep.setString(1, valueToSet);
		prep.setString(2, valueToMatchAgainst);
		
		prep.executeUpdate();
		prep.close();
	}
	
	public String getStringValue(String table, String fieldToGet, String fieldToMatchAgainst, String valueToMatchAgainst) throws SQLException
	{
	    PreparedStatement prep = dbInstance.prepareStatement("select " + fieldToGet + " from " + table + " where " + fieldToMatchAgainst + " = ?;");
	    prep.setString(1, valueToMatchAgainst);
	    ResultSet rs = prep.executeQuery();
	    rs.next();

	    String value = rs.getString(fieldToGet);
	    rs.close();
	    prep.close();
	    
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

	    PreparedStatement prep = dbInstance.prepareStatement("select " + fieldNames + " from " + table + " where " + fieldToMatchAgainst + " = ?;");
	    prep.setString(1, valueToMatchAgainst);
	    ResultSet rs = prep.executeQuery();
	    rs.next();
	    
	    for(String field : fieldToGet)
	    {
	    	resultMap.put(field, rs.getString(field));
	    }
	    
	    rs.close();
	    prep.close();
	    
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
	    prep.close();
	    
	    Statement stat = dbInstance.createStatement();	    
	    ResultSet rs = stat.executeQuery("select " + lastInsertIdFunction + ";");

	    rs.next();
	    int value = rs.getInt(lastInsertIdFunction);
	    rs.close();
	    stat.close();
	    
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
	
	public String[] getMacs(String searchString) throws SQLException
	{
		String criteria = "has_http_requests = 1";
		boolean bHasMacSearch = false;
		
		if(searchString != null && searchString.length() > 0)
		{
			searchString = "%" + searchString + "%";
			criteria = criteria + " AND mac_address LIKE ?";
			bHasMacSearch = true;
		}
		
		PreparedStatement prep = dbInstance.prepareStatement("select mac_address from clients where " + criteria + ";");
		if(bHasMacSearch)
		{
			prep.setString(1, searchString);
		}

	    ResultSet rs = prep.executeQuery();
		
	    String[] value = toStringArray(rs, "mac_address");
	    rs.close();
	    prep.close();
	    
	    return value;
	}
	
	public String[] getUserAgents(String macAddress) throws SQLException
	{
		PreparedStatement prep = dbInstance.prepareStatement("select distinct r.useragent from requests r inner join clients c on c.id = r.client_id where c.mac_address = ?;");
		prep.setString(1, macAddress);
		ResultSet rs = prep.executeQuery();

	    String[] value = toStringArray(rs, "useragent");
	    rs.close();
	    prep.close();
	    
	    return value;
	}
	
	public EnhancedJListItem[] getSessions() throws SQLException
	{
		Statement stat = dbInstance.createStatement();
	    ResultSet rs = stat.executeQuery("select count(id) as r_count from sessions where 1;");
	    
	    rs.next();
	    int numSessions = rs.getInt("r_count");
	    rs.close();
	    stat.close();
	    
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
		boolean bHasDomainSearch = false;
		
		if(searchString != null && searchString.length() > 0)
		{
			searchString = "%" + searchString + "%";
			criteria = "AND d.name LIKE ?";
			bHasDomainSearch = true;
		}
		
		PreparedStatement prep = dbInstance.prepareStatement("select distinct d.name from domains d inner join requests r on d.id = r.domain_id inner join clients c on c.id = r.client_id where c.mac_address = ? " + criteria + ";");
		prep.setString(1, macAddress);
		if(bHasDomainSearch)
		{
			prep.setString(2, searchString);
		}

	    ResultSet rs = prep.executeQuery();
		
	    String[] value = toStringArray(rs, "name");
	    rs.close();
	    prep.close();
	    
	    return value;
	}
	
	public int getDomainCount(String macAddress) throws SQLException
	{	
		PreparedStatement prep = dbInstance.prepareStatement("select count(distinct d.name) as num_domains from domains d inner join requests r on d.id = r.domain_id inner join clients c on c.id = r.client_id where c.mac_address = ?;");
		prep.setString(1, macAddress);
		ResultSet rs = prep.executeQuery();

	    rs.next();
	    int numDomains = rs.getInt("num_domains");
	    rs.close();
	    prep.close();
	    
	    return numDomains;
	}
	
	public int getClientCount() throws SQLException
	{
		Statement stat = dbInstance.createStatement();
	    ResultSet rs = stat.executeQuery("select count(id) as num_clients from clients where has_http_requests = 1;");

	    rs.next();
	    int numClients = rs.getInt("num_clients");
	    rs.close();
	    stat.close();
	    
	    return numClients;
	}
		
	public ArrayList<ArrayList> getRequests(String macAddress, String domain, String searchString) throws SQLException
	{
		String criteria = "";
		boolean bHasUriSearch = false;
		
		if(searchString != null && searchString.length() > 0)
		{
			searchString = "%" + searchString + "%";
			criteria = "AND r.uri LIKE ?";
			bHasUriSearch = true;
		}
		
		ArrayList<ArrayList> request_list = new ArrayList<ArrayList>();

		PreparedStatement prep = dbInstance.prepareStatement("select r.id, r.timerecorded, r.uri, r.description from requests r inner join domains d on r.domain_id = d.id inner join clients c on c.id = r.client_id where c.mac_address = ? AND d.name LIKE ? " + criteria + ";");
		prep.setString(1, macAddress);
		prep.setString(2, domain);
		if(bHasUriSearch)
		{
			prep.setString(3, searchString);
		}
		
		ResultSet rs = prep.executeQuery();
		
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
	    prep.close();
	    
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
            File fileToSaveFrom = new File(userHomeDirectory + "/session.sqlite");
            
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
            File fileToSaveTo = new File(userHomeDirectory + "/session.sqlite");
            
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
