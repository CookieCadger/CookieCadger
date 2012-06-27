package cookie.cadger.mattslifebytes.com;

import java.awt.EventQueue;

public class Program
{	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CookieCadgerInterface cookieCadgerInterface = new CookieCadgerInterface();
					cookieCadgerInterface.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}