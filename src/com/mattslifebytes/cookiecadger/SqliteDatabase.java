package com.mattslifebytes.cookiecadger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.IOUtils;

/**
 * A SqliteDatabase object to store intercepted HTTP requests.
 */
public class SqliteDatabase {
	
	private Connection dbInstance;
	private String executionPath;
    private LinkedList<CookieCadgerException> debugList;
	
    /**
     * Default constructor for the database object. Initializes the database connection and sets database settings.
     */
    public SqliteDatabase() {
    	/* connect to the database */
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			debugList.add(new CookieCadgerException("Sqlite3DB: Sqlite3DB(): we weren't able to find the sqlite database driver."));
		}
		executionPath = System.getProperty("user.dir").replace("\\", "/");
		try {
			dbInstance = DriverManager.getConnection("jdbc:sqlite:" + executionPath + "/session.sqlite");
		} catch (SQLException e) {
			// SQLException has the special property of chained exceptions.
			while(e != null) {
				debugList.add(new CookieCadgerException("Sqlite3DB: Sqlite3DB(): we encountered an issue accessing the database: " + e.getLocalizedMessage()));
				e = e.getNextException();
			}
		}
		/* initialize the debug list */
    	debugList = new LinkedList<CookieCadgerException>();
    	/* ensure that the temporary database will be cleaned up on exit */
		File sessionDB = new File(executionPath + "/session.sqlite");
		sessionDB.deleteOnExit();
		/* initialize the tables */
		initTables();
    }
    
    /**
     * Clears and then initializes the database tables.
     */
    private void initTables() {    	
		try {			
			Statement stat = dbInstance.createStatement();
			try {
				/* Clear all the tables */
				stat.executeUpdate("drop table if exists requests;");
				stat.executeUpdate("drop table if exists clients;");
				stat.executeUpdate("drop table if exists domains;");
				stat.executeUpdate("drop table if exists sessions;");
				/* Now create all the tables */
				stat.executeUpdate("create table requests (id INTEGER PRIMARY KEY, timerecorded INTEGER, uri VARCHAR, useragent VARCHAR, referer VARCHAR, cookies VARCHAR, authorization VARCHAR, auth_basic VARCHAR, domain_id INTEGER, client_id INTEGER);");
				stat.executeUpdate("create table clients (id INTEGER PRIMARY KEY, mac_address VARCHAR, ipv4_address VARCHAR, ipv6_address VARCHAR, netbios_hostname VARCHAR, mdns_hostname VARCHAR, has_http_requests INTEGER);");
				stat.executeUpdate("create table domains (id INTEGER PRIMARY KEY, name VARCHAR);");
				stat.executeUpdate("create table sessions (id INTEGER PRIMARY KEY, user_token, description VARCHAR, profile_photo_url VARCHAR, session_uri VARCHAR, request_id INTEGER);");
			} finally {
				stat.close();
			}
		}  catch (SQLException e) {
			// SQLException has the special property of chained exceptions.
			while(e != null) {
				debugList.add(new CookieCadgerException("Sqlite3DB: initTables(): we encountered an issue querying the database: " + e.getLocalizedMessage()));
				e = e.getNextException();
			}
		}
    }
    
    /**
     * Closes the database instance.
     */
    public void closeDatabase() {
    	try {
    		if(dbInstance == null) {
				debugList.add(new CookieCadgerException("Sqlite3DB: closeDatabase(): the database connection is null."));
    		} else {
    			dbInstance.close();
    		}
		} catch (SQLException e) {
			while(e != null) {
				debugList.add(new CookieCadgerException("Sqlite3DB: closeDatabase(): we encountered an issue closing the database: " + e.getLocalizedMessage()));
				e = e.getNextException();
			}
		}
    }
    
    /**
     * Opens a file open dialogue to let a user select a SQLite file to use in CookieCadger. The specified file is copied to a known path relative to the program, and is therefore not modified.
     */
    public void openDatabase() {
    	JFileChooser chooser = new JFileChooser();
		FileFilter pcapFilter = new FileNameExtensionFilter("*.sqlite", "sqlite");
		chooser.addChoosableFileFilter(pcapFilter);
		chooser.setFileFilter(pcapFilter);
		int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
        	File source = chooser.getSelectedFile();
            File destination = new File(executionPath + "/session.sqlite");
			createFileCopy(source, destination);
        }
    }
    
    /**
     * Opens a file save dialogue to let a user save the current database to a SQLite file.
     */
    public void saveDatabase()
	{	
		JFileChooser chooser = new JFileChooser();
		FileFilter pcapFilter = new FileNameExtensionFilter("*.sqlite", "sqlite");
		chooser.addChoosableFileFilter(pcapFilter);
		chooser.setFileFilter(pcapFilter);
		int returnVal = chooser.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            File destination = chooser.getSelectedFile();
            if(!destination.getPath().toLowerCase().endsWith(".sqlite") && chooser.getFileFilter().equals(pcapFilter))
            {
            	destination = new File(destination.getPath() + ".sqlite");
            }
            File source = new File(executionPath + "/session.sqlite");
			createFileCopy(source, destination);
        }
	}
    
    /**
     * Makes a copy of the database file.
     * @param source the database file to copy.
     * @param destination the file to copy to.
     * @return true if the copy operation was successful, false otherwise.
     */
	private void createFileCopy(File source, File destination) {
		/* Check for issues before we try copying */
		if (source == null) {
			debugList.add(new CookieCadgerException("Sqlite3DB: createFileCopy(File, File): source file can't be null."));
			return;
		}
		if (destination == null) {
			debugList.add(new CookieCadgerException("Sqlite3DB: createFileCopy(File, File): destination file can't be null."));
			return;
		}
		if (source.exists() == false) {
			debugList.add(new CookieCadgerException("Sqlite3DB: createFileCopy(File, File): source file does not exist (not found)."));
			return;
		}
		if (source.isDirectory()) {
			debugList.add(new CookieCadgerException("Sqlite3DB: createFileCopy(File, File): source file exists, but is a directory."));
			return;
		}
		try {
			if (source.getCanonicalPath().equals(destination.getCanonicalPath())) {
				debugList.add(new CookieCadgerException("Sqlite3DB: createFileCopy(File, File): source file and destination file are the same."));
				return;
			}
		} catch (IOException e) {
			debugList.add(new CookieCadgerException("Sqlite3DB: createFileCopy(File, File): IOException encountered when testing if source and destination files were the same: " + e.getLocalizedMessage()));
			return;
		}
		File parentFile = destination.getParentFile();
		if (parentFile != null) {
			if(parentFile.mkdir() == false && parentFile.isDirectory() == false) {
				debugList.add(new CookieCadgerException("Sqlite3DB: createFileCopy(File, File): destination directory cannot be created."));
				return;
			}
		}
		if (destination.exists() && destination.canWrite() == false) {
			debugList.add(new CookieCadgerException("Sqlite3DB: createFileCopy(File, File): destination file exists but is read-only."));
			return;
		}
		/* Try copying */
		FileInputStream fis = null;
		FileOutputStream fos = null;
		FileChannel input = null;
		FileChannel output = null;
		try {
			fis = new FileInputStream(source);
			fos = new FileOutputStream(destination);
			input = fis.getChannel();
			output = fos.getChannel();
			long size = input.size();
			long pos = 0;
			long count = 0;
			while (pos < size) {
				long buffer = 31457280; // 30 MB
				count = size - pos > buffer ? buffer : size - pos;
				pos += output.transferFrom(input, pos, count);
			}
		} catch (Exception e) {
			debugList.add(new CookieCadgerException("Sqlite3DB: createFileCopy(File, File): encountered an error during copying: " + e.getClass() + ": " + e.getLocalizedMessage()));
			return;
		/* Clean up the handles */
		} finally {
			IOUtils.closeQuietly(output);
			IOUtils.closeQuietly(fos);
			IOUtils.closeQuietly(input);
			IOUtils.closeQuietly(fis);
		}
		/* Check if the copy was a success */
		if (source.length() != destination.length()) {
			debugList.add(new CookieCadgerException("Sqlite3DB: createFileCopy(File, File): failed to copy full contents: source file and destination file are not the same size as expected."));
			return;
		}
	}
	
}