package cookie.cadger.mattslifebytes.com;

import java.awt.EventQueue;

import javax.swing.JOptionPane;

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
			public void run() {
				try
				{
					String pathToTshark = "";
					if(args.length > 1)
					{
						JOptionPane.showMessageDialog(null, "You've entered too many program arguments.\n\nUsage:\njava -jar CookieCadger.jar <optional: full path to tshark>");
						return;
					}
					else if(args.length == 1)
					{
						pathToTshark = args[0];
					}
					
					CookieCadgerInterface cookieCadgerInterface = new CookieCadgerInterface(pathToTshark);
					cookieCadgerInterface.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}