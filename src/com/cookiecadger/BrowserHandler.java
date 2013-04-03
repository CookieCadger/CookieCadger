package com.cookiecadger;

import org.browsermob.proxy.ProxyServer;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

public class BrowserHandler
{
	private static RequestInterceptor requestIntercept;
	private static WebDriver driver = null;
	private static ProxyServer server = null;
	private static Proxy proxy = null;
	
	public static void loadRequestIntoBrowser(String domain, String uri, String userAgent, String referer, String cookies, String authorization)
	{
		Utils.cookieCadgerFrame.consoleScrollPane.setVisible(false);
		
		Utils.cookieCadgerFrame.loadingRequestProgressBar.setString("Loading request into browser, please wait...");
		Utils.cookieCadgerFrame.loadingRequestProgressBar.setVisible(true);
	    
		if(server == null)
		{
			server = new ProxyServer(7878);

	        requestIntercept = new RequestInterceptor();
	        requestIntercept.setRandomization(Integer.toString(Utils.getLocalRandomization()));
	        
	        try {
				server.start();
				server.addRequestInterceptor(requestIntercept);

				proxy = new Proxy();
				proxy.setHttpProxy("127.0.0.1:7878"); // Set browser to localhost proxy

			} catch (Exception e) {
				e.printStackTrace();
			}	
		}

		// Ask to get page title.
		// If none, this will Exception and set driver to null as necessary.
		// We do this as a (hackish) check to see if the browser is still open
		// from a previous replay, or if the user already closed it and needs a new one. 
		try
		{
			driver.getTitle();
		}
		catch (Exception e)
		{
			driver = null;
		}
		
		if(driver == null)
		{
			// configure it as a desired capability
			DesiredCapabilities capabilities = new DesiredCapabilities();
			capabilities.setCapability(CapabilityType.PROXY, proxy);
			
			driver = new FirefoxDriver(capabilities);
		}
		else
		{
			// Everything is already set to go, just clear the proxy settings
			requestIntercept.clear();
		}
		
        if(!cookies.isEmpty())
        {
        	requestIntercept.setCookies(cookies);
        }
        
        if(!authorization.isEmpty())
        {
        	requestIntercept.setAuthorization(authorization);
        }
        
        if(userAgent.isEmpty())
        {
        	// None specifically specified, so load from browser via the WebDriver
        	// (Without this BrowserMob modifies it and adds a unique tag)
        	userAgent = (String) ((JavascriptExecutor) driver).executeScript("return navigator.userAgent;");
        }
        requestIntercept.setUserAgent(userAgent);
        
        if(!referer.isEmpty())
        {
        	requestIntercept.setReferer(referer);
        }

        try
        {
        	driver.get("http://" + domain + uri);
        }
        catch (Exception e)
        {
        	// Nothing
        }
        
        Utils.cookieCadgerFrame.loadingRequestProgressBar.setVisible(false);
        Utils.cookieCadgerFrame.consoleScrollPane.setVisible(true);
	}
	
	public static void closeConnections()
	{
		// Get the WebDriver fully unloaded
		try
		{
			driver.close();
		}
		catch (Exception ex)
		{}
		finally
		{
			driver = null;
		}
	}
}
