package cookie.cadger.mattslifebytes.com;

import java.awt.EventQueue;

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
		MacSupport.initIfMac("Cookie Cadger", false, null, new MacHandler()
		{
			@Override
			public void handleQuit(Object applicationEvent)
			{
				System.exit(0);
			}

			@Override
			public void handleAbout(Object applicationEvent)
			{
				CookieCadgerUtils.DisplayAboutWindow();
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
							System.err.println("Example usage:\njava -jar CookieCadger.jar [--tshark=/usr/sbin/tshark] [--detection=on] [--demo=on]");
							return;
						}
					}
				}
				
				try
				{					
					CookieCadgerInterface cookieCadgerInterface = new CookieCadgerInterface(args);
					cookieCadgerInterface.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}