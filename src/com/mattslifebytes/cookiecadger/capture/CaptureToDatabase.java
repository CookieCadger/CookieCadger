package com.mattslifebytes.cookiecadger.capture;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;

import com.mattslifebytes.cookiecadger.SqliteDatabase;
import com.mattslifebytes.cookiecadger.CookieCadgerException;

public class CaptureToDatabase extends Thread {
	
	private CaptureEngine engine;
	private SqliteDatabase database;
	private String localRandomization;
	private LinkedList<CookieCadgerException> debugList;

	public CaptureToDatabase(SqliteDatabase database, CaptureEngine engine, String localRandomization) {
		this.database = database;
		this.engine = engine;
		this.localRandomization = localRandomization;
		this.debugList = new LinkedList<CookieCadgerException>();
	}
	
	public void run() {
		String line = null;
		BufferedReader reader = engine.getStream();
		while(reader != null) {
			try {
				line = reader.readLine();
			} catch (IOException e) {
				debugList.add(new CookieCadgerException("CaptureToDatabase: run(): Couldn't read from BufferedReader (probably means we stopped capture / process ended before we could finish reading the line)"));
				break;
			}
			if (line == null)
				break; 
			if(line.contains("\t")) {
				processLine(line);
			}
		}
	}
	
	private void processLine(String line) {
		int clientID = -1;
		boolean usefulData = false;
		String[] values = line.split("\t", -1);
		// if we observed 4 or less values, we likely saw continuation data, ignore.
		// if we saw more than 20 values, something has gone horrifically wrong.
		if (values.length > 4 || values.length < 20) {
			TsharkData data = new TsharkData(values);
			// Basic packet filter for when a pcap is loaded 
			if(engine.getPcapFile() != null && engine.getPcapFile().isEmpty() == false)
				if((data.getTcpDestination().equals("80") == false && data.getUdpSource().equals("5353") == false && data.getUdpSource().equals("138") == false))
					return;
			// When CookieCadger creates requests it appends a randomization to the Accept header. Check for it and ignore if matched.
			if(data.getAccept().contains(", " + localRandomization))
				return;
			if(data.getRefererUri().isEmpty() == false) {
				usefulData = true;
				clientID = handleClient(data.getMacAddress());
				// process request
			}
		}
	}
	
}
