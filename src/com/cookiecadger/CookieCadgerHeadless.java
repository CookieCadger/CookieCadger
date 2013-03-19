package com.cookiecadger;

import java.io.IOException;
import java.util.Scanner;

public class CookieCadgerHeadless
{
	public CookieCadgerHeadless()
	{
		Scanner systemInput = new Scanner( System.in );
		
		// Name and license
		Utils.consoleMessage("\n\nCookie Cadger (v"+ Utils.version +", https://cookiecadger.com)\nCreated by Matthew Sullivan - mattslifebytes.com\nThis software is freely distributed under the terms of the FreeBSD license.\n");
		
		if(Utils.programSettings.get("dbEngine").equals("sqlite"))
		{
			Utils.consoleMessage("Fatal error: headless mode requires the use of an external database. Invoke with '--help' for database options.");
			System.exit(1);
		}
		
		// Prepare database
		Utils.initializeDatabase();
		
		// Prepare capture handler
		CaptureHandler captureHandler = new CaptureHandler();
		
		// Get capture devices
		captureHandler.initializeDeviceList();
		
		int captureDevice = -1;
		// Check if interface was specified
		if((Integer) Utils.programSettings.get("interfaceNum") != -1)
		{
			captureDevice = (Integer) Utils.programSettings.get("interfaceNum") - 1;  // Adjusting for ComSci counting
		}
		else
		{
			Utils.consoleMessage("Please enter the ID of the device you wish to capture from:");
			captureDevice = (systemInput.nextInt() - 1); // Adjusting for ComSci counting
		}
		
		// Check if session detection was declared
		if((Integer) Utils.programSettings.get("bSessionDetection") == -1)
		{
			Utils.consoleMessage("\nSession detection replays web requests in the background and analyzes " +
            		"them for evidence that a user is logged in. Enabling session detection " +
            		"will cause Cookie Cadger to utilize a larger amount of available " +
            		"system resources.\n\n" +
            		"By enabling this feature you also understand that:\n" +
            		"1) Cookie Cadger will (potentially) automatically impersonate any " +
            		"network user without their explicit permission or interaction on your part.\n\n" +
            		"2) The legality of doing so varies between jurisdictions. It is your " +
            		"responsibility to understand and comply with any applicable laws.\n\n");
			
			while(true)
			{
				Utils.consoleMessage("Would you like to enable session detection? [Y/n]");
				
				String enableSessionDetection = systemInput.next();
				
				if(enableSessionDetection.toLowerCase().equals("y"))
				{
					Utils.programSettings.put("bSessionDetection", 1);
					break;
				}
				else if(enableSessionDetection.toLowerCase().equals("n"))
				{
					Utils.programSettings.put("bSessionDetection", 0);
					break;
				}
			}
		}
		
		try {
			captureHandler.startCapture(captureDevice, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
