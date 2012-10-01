package com.mattslifebytes.cookiecadger;

public class CookieCadgerInterface {
	private CookieCadgerFrame frame;
	
	/**
	 * Interface to gracefully close CookieCadger handles when improper arguments are passed on program start.
	 */
	public void onBadArgsClose() {
		frame.dispose();
	}
}
