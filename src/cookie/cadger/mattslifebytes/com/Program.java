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