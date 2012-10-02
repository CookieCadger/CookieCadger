package com.mattslifebytes.cookiecadger.capture;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import org.apache.commons.exec.*;

import com.mattslifebytes.cookiecadger.CookieCadgerException;

public class CaptureEngine {

	private String tsharkPath;
	private String interfaceName;
	private String pcapFile;
	private BufferedReader reader;
	private ByteArrayOutputStream outputStream, errorStream;
	private CommandLine cmdLine;
	private DefaultExecutor exec;
	private PumpStreamHandler streamHandler;
	private DefaultExecuteResultHandler resultHandler;
	private ExecuteWatchdog watchdog; 
    private LinkedList<CookieCadgerException> debugList;
	
	public CaptureEngine(String tsharkPath, String interfaceName, String pcapFile)
	{
		this.tsharkPath = tsharkPath;
		this.debugList = new LinkedList<CookieCadgerException>();
		this.interfaceName = interfaceName;
		this.pcapFile = pcapFile;
	}
	
	public void startCapture() {
		outputStream = new ByteArrayOutputStream();
		errorStream = new ByteArrayOutputStream();
		cmdLine = new CommandLine(tsharkPath + getTsharkArgs(interfaceName, pcapFile));
		resultHandler = new DefaultExecuteResultHandler();
		streamHandler = new PumpStreamHandler(outputStream, errorStream);
		watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
		exec = new DefaultExecutor();
		exec.setStreamHandler(streamHandler);
		exec.setWatchdog(watchdog);
		try {
			exec.execute(cmdLine, resultHandler);
		} catch (ExecuteException e) {
			debugList.add(new CookieCadgerException("CaptureEngine: startCapture(): Exception encountered during tshark execution: " + e.getLocalizedMessage()));
		} catch (IOException e) {
			debugList.add(new CookieCadgerException("CaptureEngine: startCapture(): Exception encountered during tshark execution: " + e.getLocalizedMessage()));
		}
		try {
			reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(outputStream.toByteArray())));
			resultHandler.waitFor();
		} catch (InterruptedException e) {
			debugList.add(new CookieCadgerException("CaptureEngine: startCapture(): tshark thread was interrupted by another thread while it was waiting."));
		} finally {
			this.stopCapture();
		}
	}
	
	public void stopCapture() {
		streamHandler.stop();
		watchdog.destroyProcess();
		try {
			reader.close();
		} catch (IOException e) {
			debugList.add(new CookieCadgerException("CaptureEngine: stopCapture(): IOException when closing the BufferedReader: " + e.getLocalizedMessage()));
		}
		reader = null;
	}
	
	public BufferedReader getStream() {
		return reader;
	}
	
	public String getPcapFile() {
		return pcapFile;
	}
	
	private String getTsharkArgs(String interfaceName, String pcapFile) {
		if (pcapFile == null || pcapFile.isEmpty()) {
			return "-i " + interfaceName + " -f tcp dst port 80 or udp src port 5353 or udp src port 138 -T fields -e eth.src -e wlan.sa -e ip.src -e ipv6.src -e tcp.srcport -e tcp.dstport -e udp.srcport -e udp.dstport -e browser.command -e browser.server -e dns.resp.name -e http.host -e http.request.uri -e http.accept -e http.accept_encoding -e http.user_agent -e http.referer -e http.cookie -e http.authorization -e http.authbasic";
		} else {
			return "-r " + pcapFile + " -T fields -e eth.src -e wlan.sa -e ip.src -e ipv6.src -e tcp.srcport -e tcp.dstport -e udp.srcport -e udp.dstport -e browser.command -e browser.server -e dns.resp.name -e http.host -e http.request.uri -e http.accept -e http.accept_encoding -e http.user_agent -e http.referer -e http.cookie -e http.authorization -e http.authbasic";
		}
	}
	
}
