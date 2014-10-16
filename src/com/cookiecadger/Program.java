/*
 * Copyright (c) 2014, Matthew Sullivan <MattsLifeBytes.com / @MattsLifeBytes>
 * 
 * Additional portions generously contributed by:
 * - Ben Holland <https://github.com/benjholla>
 * - Justin Kaufman <akaritakai@gmail.com>
 * 
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

package com.cookiecadger;

import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;

public class Program
{	
	/**
	 * Launch the application.
	 */
	public static void main(final String[] args)
	{
		// Mac menu bar support. Huge thanks to Mike Angstadt for putting this
		// code out there for use.
		// http://code.google.com/p/evolutionchamber/issues/detail?id=102
		MacSupport.initIfMac("Cookie Cadger", false, "/resource/cookiecadger.png", new MacHandler()
		{
			@Override
			public void handleQuit(Object applicationEvent)
			{
				System.exit(0);
			}

			@Override
			public void handleAbout(Object applicationEvent)
			{
				Utils.displayAboutWindow();
			}
		});
		
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				if(args.length > 0)
				{
					for (String arg : args)
					{
						if(arg.contains("-help") || arg.contains("/help") || arg.contains("/?") || arg.contains("-?"))
						{
							System.err.println("Cookie Cadger, version " + Utils.version);
							System.err.println(	"Example usage:\njava -jar CookieCadger.jar \n" +
									"\t--tshark=/usr/sbin/tshark\n" +
									"\t--headless=on\n" +
									"\t--interfacenum=2\t(requires --headless=on)\n" +
									"\t--detection=on\n" +
									"\t--demo=on\n" +
									"\t--update=on\n" +
									"\t--dbengine=mysql\t(default is 'sqlite' for local, file-based storage)\n" +
									"\t--dbhost=localhost\t(requires --dbengine=mysql)\n" +
									"\t--dbuser=user\t\t(requires --dbengine=mysql)\n" +
									"\t--dbpass=pass\t\t(requires --dbengine=mysql)\n" +
									"\t--dbname=cadgerdata\t(requires --dbengine=mysql)\n" +
									"\t--dbrefreshrate=15\t(in seconds, requires --dbengine=mysql, requires --headless=off)"
									);
							return;
						}
					}
				}
				
				// Load settings from local config
				Utils.loadApplicationPreferences();
				Utils.handleProgramArguments(args);
				
				// Check if ability to even run a GUI exists
				if (GraphicsEnvironment.isHeadless())
				{
					Utils.consoleMessage("No graphical environment found. Dropping to headless mode.");
					Utils.programSettings.put("bHeadless", true);
				}
				
				// Shall we create a graphical or non-graphical session?
				if((Boolean) Utils.programSettings.get("bHeadless"))
				{
					new CookieCadgerHeadless();
				}
				else
				{
					try
					{
						CookieCadgerFrame cookieCadgerFrame = new CookieCadgerFrame();
						cookieCadgerFrame.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
}