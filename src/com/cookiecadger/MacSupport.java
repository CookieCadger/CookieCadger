package com.cookiecadger;

import java.awt.Image;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessControlException;

import javax.swing.ImageIcon;

/**
 * This class contains code which helps to better integrate the application into
 * the Mac OSX environment.
 * 
 * @see http://developer.apple.com/mac/library/documentation/Java/Reference/1.5.0/appledoc/api/index.html
 * 
 * @author Mike Angstadt
 * http://code.google.com/p/evolutionchamber/issues/detail?id=102
 * 
 */
public class MacSupport {

	/**
	 * Runs initialization code specific to Mac OSX.
	 * 
	 * @param title the title of the application
	 * @param enablePreferences true to enable the "Preferences" menu option,
	 * false to disable it. If enabled, you must override
	 * MacHandler.handlePreferences() in the handler parameter
	 * @param dockImage the classpath to the image that will appear in the dock
	 * and when alt-tabbed. Null for no image.
	 * @param handler handles the various Mac events
	 */
	public static void init(String title, boolean enablePreferences, String dockImage, final MacHandler handler) {
		try {
			// enable Mac menu bar
			System.setProperty("apple.laf.useScreenMenuBar", "true");

			// set application name
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", title);

			// create an implementation of the ApplicationListener interface
			Class<?> applicationListenerInterface = Class.forName("com.apple.eawt.ApplicationListener");
			Object applicationListenerInstance = Proxy.newProxyInstance(MacSupport.class.getClassLoader(), new Class<?>[] { applicationListenerInterface }, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
					// the name of the invoked method
					String methodName = method.getName();

					// the com.apple.eawt.ApplicationEvent object
					Object applicationEvent = arguments[0];

					if (methodName.equals("handleQuit")) {
						handler.handleQuit(applicationEvent);
					} else if (methodName.equals("handleAbout")) {
						handler.internalHandleAbout(applicationEvent);
					} else if (methodName.equals("handlePreferences")) {
						handler.handlePreferences(applicationEvent);
					}
					return null;
				}
			});

			// equivalent to: Application applicationInstance = Application.getApplication();
			Class<?> applicationClass = Class.forName("com.apple.eawt.Application");
			Method getApplicationMethod = applicationClass.getMethod("getApplication");
			Object applicationInstance = getApplicationMethod.invoke(null);

			// equivalent to: applicationInstance.setEnabledPreferencesMenu(true);
			// the preferences menu option is disabled by default
			Method setEnabledPreferencesMenuMethod = applicationClass.getMethod("setEnabledPreferencesMenu", boolean.class);
			setEnabledPreferencesMenuMethod.invoke(applicationInstance, enablePreferences);

			// equivalent to: applicationInstance.addApplicationListener(applicationAdapterImplementation);
			Method addApplicationListenerMethod = applicationClass.getMethod("addApplicationListener", applicationListenerInterface);
			addApplicationListenerMethod.invoke(applicationInstance, applicationListenerInstance);

			// this image will appear when then user alt-tabs and in the dock
			// equivalent to: applicationInstance.setDockIconImage(...)
			if (dockImage != null) {
				Method setDockIconImageMethod = applicationClass.getMethod("setDockIconImage", Image.class);
				ImageIcon icon = new ImageIcon(MacSupport.class.getResource(dockImage));
				setDockIconImageMethod.invoke(applicationInstance, icon.getImage());
			}
		} catch (AccessControlException e) {
			//this exception will be thrown when run from WebStart in two places: (1) when setting the "com.apple.mrj.application.apple.menu.about.name" property and (2) when getting the "com.apple.eawt.Application" class, so unfortunately, none of this code works with WebStart
			//however, WebStart does do some of these things automatically: setting the title and setting the dock icon image
			//giving the application all-permissions in the JNLP would probably solve this problem, but then you have to show the user that scary security warning dialog, as well as sign the JAR
		} catch (Throwable e) {
		}
	}

	/**
	 * Determines whether the application is running on a Mac.
	 * 
	 * @return true if the application is running on a Mac, false if not
	 */
	public static boolean isMac() {
		String osName = System.getProperty("os.name").toLowerCase();
		return osName.startsWith("mac os x");
	}

	/**
	 * Runs initialization code specific to Mac OSX only if the application is
	 * running on a Mac.
	 * 
	 * @param title the title of the application
	 * @param enablePreferences true to enable the "Preferences" menu option,
	 * false to disable it. If enabled, you must override
	 * MacHandler.handlePreferences() in the handler parameter
	 * @param dockImage the classpath to the image that will appear in the dock
	 * and when alt-tabbed. Null for no image.
	 * @param handler handles the various Mac events
	 */
	public static void initIfMac(String title, boolean enablePreferences, String dockImage, MacHandler handler) {
		if (isMac()) {
			init(title, enablePreferences, dockImage, handler);
		}
	}
}
