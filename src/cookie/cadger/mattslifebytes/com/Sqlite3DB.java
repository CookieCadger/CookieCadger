package cookie.cadger.mattslifebytes.com;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Sqlite3DB
{
	private Connection dbInstance;
	private String executionPath;
	
	public Sqlite3DB() throws Exception
	{
		Class.forName("org.sqlite.JDBC");

		executionPath = System.getProperty("user.dir").replace("\\", "/");
		dbInstance = DriverManager.getConnection("jdbc:sqlite:" + executionPath + "/session.sqlite");
		
		// Cleans up the temporary DB on exit
		File sessionDB = new File(executionPath + "/session.sqlite");
		sessionDB.deleteOnExit();
		
		initTables();
	}
	
	public void initTables() throws SQLException
	{
		// Clear all tables
	    Statement stat = dbInstance.createStatement();
	    stat.executeUpdate("drop table if exists requests;");
	    stat.executeUpdate("drop table if exists clients;");
	    stat.executeUpdate("drop table if exists domains;");
		
	    // Now create them
	    stat.executeUpdate("create table requests (id INTEGER PRIMARY KEY, timerecorded INTEGER, uri VARCHAR, useragent VARCHAR, referer VARCHAR, cookies VARCHAR, domain_id INTEGER, client_id INTEGER);");
	    stat.executeUpdate("create table clients (id INTEGER PRIMARY KEY, mac VARCHAR, ip VARCHAR, netbios_hostname VARCHAR, mdns_hostname VARCHAR);");
	    stat.executeUpdate("create table domains (id INTEGER PRIMARY KEY, name VARCHAR);");
	    
	    stat.close();
	}
	
	public boolean containsValue(String table, String field, String value) throws SQLException
	{
	    Statement stat = dbInstance.createStatement();
	    ResultSet rs = stat.executeQuery("select count(id) from " + table + " where " + field + " = '" + value + "'");
	    
	    if(rs.getInt("count(id)") > 0)
	    {
	    	rs.close();
	    	stat.close();
	    	return true;
	    }
	    
	    rs.close();
	    stat.close();
	    return false;
	}
	
	public int getNewestRequestID(int domain_id) throws SQLException
	{
	    Statement stat = dbInstance.createStatement();
	    ResultSet rs = stat.executeQuery("select id from requests where domain_id = '" + Integer.toString(domain_id) + "' order by id desc limit 1");
	    
	    int value = rs.getInt("id");
	    rs.close();
	    stat.close();
	    
	    return value;
	}
	
	public int getIntegerValue(String table, String fieldToGet, String fieldToMatchAgainst, String valueToMatchAgainst) throws SQLException
	{
	    Statement stat = dbInstance.createStatement();
	    ResultSet rs = stat.executeQuery("select " + fieldToGet + " from " + table + " where " + fieldToMatchAgainst + " = '" + valueToMatchAgainst + "'");
	    
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
	    
	    String value = rs.getString(fieldToGet);
	    rs.close();
	    stat.close();
	    
	    return value;
	}
	
	// return the ID of the newly created client
	public int createClient(String mac, String ip) throws SQLException
	{
	    PreparedStatement prep = dbInstance.prepareStatement("insert into clients values(NULL,?,?,?,?);");
	    prep.setString(1, mac);
	    prep.setString(2, ip);
	    prep.setString(3, "");
	    prep.setString(4, "");
	    prep.addBatch();
	    prep.executeBatch();
		
	    Statement stat = dbInstance.createStatement();
	    ResultSet rs = stat.executeQuery("select last_insert_rowid();");
	    
	    int value = rs.getInt("last_insert_rowid()");
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
	    ResultSet rs = stat.executeQuery("select last_insert_rowid();");
	    
	    int value = rs.getInt("last_insert_rowid()");
	    rs.close();
	    stat.close();
	    prep.close();
	    
	    return value;
	}
	
	// return the ID of the newly created request
	public int createRequest(String uri, String useragent, String referer, String cookies, int domain_id, int client_id) throws SQLException
	{
		long unixTime = System.currentTimeMillis() / 1000L;
		try {			
			uri = URLDecoder.decode(uri, "UTF-8");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {			
			referer = URLDecoder.decode(referer, "UTF-8");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    PreparedStatement prep = dbInstance.prepareStatement("insert into requests values(NULL,?,?,?,?,?,?,?);");
	    prep.setLong(1, unixTime);
	    prep.setString(2, uri);
	    prep.setString(3, useragent);
	    prep.setString(4, referer);
	    prep.setString(5, cookies);
	    prep.setInt(6, domain_id);
	    prep.setInt(7, client_id);
	    prep.addBatch();
	    prep.executeBatch();
	 
	    Statement stat = dbInstance.createStatement();
	    ResultSet rs = stat.executeQuery("select last_insert_rowid();");
	    
	    int value = rs.getInt("last_insert_rowid()");
	    rs.close();
	    stat.close();
	    prep.close();
	    
	    return value;
	}
	
	public String[] getMacs() throws SQLException
	{
		Statement stat = dbInstance.createStatement();
	    ResultSet rs = stat.executeQuery("select mac from clients where 1;");

	    String[] value = toStringArray(rs, "mac");
	    
	    rs.close();
	    stat.close();
	    
	    return value;
	}
	
	public String[] getDomains(String mac) throws SQLException
	{
		Statement stat = dbInstance.createStatement();
	    ResultSet rs = stat.executeQuery("select distinct d.name from domains d inner join requests r on d.id = r.domain_id inner join clients c on c.id = r.client_id where c.mac = '" + mac + "';");

	    String[] value = toStringArray(rs, "name");
	    rs.close();
	    stat.close();
	    
	    return value;
	}
	
	public int getDomainCount(String mac) throws SQLException
	{
		Statement stat = dbInstance.createStatement();
	    ResultSet rs = stat.executeQuery("select count(distinct d.name) as num_domains from domains d inner join requests r on d.id = r.domain_id inner join clients c on c.id = r.client_id where c.mac = '" + mac + "';");
	    int numDomains = rs.getInt("num_domains");
	    
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
	
	public ArrayList<ArrayList> getRequests(String mac, String domain) throws SQLException
	{
		ArrayList<ArrayList> request_list = new ArrayList<ArrayList>();
		
		Statement stat = dbInstance.createStatement();
	    ResultSet rs = stat.executeQuery("select r.id, r.timerecorded, r.uri from requests r inner join domains d on r.domain_id = d.id inner join clients c on c.id = r.client_id where c.mac = '" + mac + "' AND d.name LIKE '" + domain + "';");
	    
	    ArrayList<String> ids = new ArrayList<String>();
	    ArrayList<String> timerecordeds = new ArrayList<String>();
	    ArrayList<String> uris = new ArrayList<String>();
	    
	    while (rs.next())
	    {
	      ids.add(rs.getString("id"));
	      timerecordeds.add(rs.getString("timerecorded"));
	      uris.add(rs.getString("uri"));
	    }
	    rs.close();
	    
	    request_list.add(ids);
	    request_list.add(timerecordeds);
	    request_list.add(uris);
	    
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
	private String[] toStringArray(ResultSet resultSet, String columnLabel) {
	    LinkedList<String> resultList = new LinkedList<String>();
	 
	    try {
	        while (resultSet.next()) {
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
