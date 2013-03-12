package com.cookiecadger;

/**
 * Handles various events specific to the Mac OSX environment.
 * 
 * @author Mike Angstadt
 * http://code.google.com/p/evolutionchamber/issues/detail?id=102
 * 
 */
public abstract class MacHandler {
	private boolean aboutOverriden = true;

	/**
	 * Handles the event of the user selecting the "Quit" option from the Mac
	 * menu bar.
	 * 
	 * @param applicationEvent the com.apple.eawt.ApplicationEvent object that
	 * accompanies the event
	 */
	public void handleQuit(Object applicationEvent) {
	}

	/**
	 * Used for internal purposes. Override {@link handleAbout} instead.
	 * 
	 * @param applicationEvent the com.apple.eawt.ApplicationEvent object that
	 * accompanies the event
	 */
	public final void internalHandleAbout(Object applicationEvent) {
		handleAbout(applicationEvent);
		if (aboutOverriden) {
			//if the handleAbout() method was overridden, then call the ApplicationEvent.setHandled() method in order to prevent the generic About window from appearing
			try {
				//equivalent to: applicationEventInstance.setHandled(true)
				applicationEvent.getClass().getMethod("setHandled", boolean.class).invoke(applicationEvent, true);
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Handles the event of the user selecting the "About" option from the Mac
	 * menu bar.
	 * 
	 * @param applicationEvent the com.apple.eawt.ApplicationEvent object that
	 * accompanies the event
	 */
	public void handleAbout(Object applicationEvent) {
		aboutOverriden = false;
	}

	/**
	 * Handles the event of the user selecting the "Preferences" option from the
	 * Mac menu bar.
	 * 
	 * @param applicationEvent the com.apple.eawt.ApplicationEvent object that
	 * accompanies the event
	 */
	public void handlePreferences(Object applicationEvent) {
	}
}
