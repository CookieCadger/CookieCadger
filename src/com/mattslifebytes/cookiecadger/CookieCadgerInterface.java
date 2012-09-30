package com.mattslifebytes.cookiecadger;

public class CookieCadgerInterface {
	private CookieCadgerFrame frame;

	/**
	 * Interface to gracefully close CookieCadger in case of an exception.
	 */
	public void exceptionClose() {
		frame.dispose();
	}
}
