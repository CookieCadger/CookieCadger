package com.cookiecadger;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

public class RequestInterceptor implements HttpRequestInterceptor
{
	private String cookies = null;
	private String referer = null;
	private String useragent = null;
	private String randomization = null;
	private String authorization = null;
	
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException
    {        
        if(cookies != null && cookies.length() > 0)
        {
        	request.removeHeaders("Cookie");
        	request.addHeader("Cookie", cookies);
        }
        
        if(referer != null && referer.length() > 0)
        {
        	request.removeHeaders("Referer");
        	request.removeHeaders("Referrer");
        	request.addHeader("Referer", referer);
        	
        	// After first load, let the browser take over
        	setReferer(null);
        }
        
        if(useragent != null && useragent.length() > 0)
        {
        	request.removeHeaders("User-Agent");
        	request.addHeader("User-Agent", useragent);
        }
        
        if(authorization != null && authorization.length() > 0)
        {
        	request.removeHeaders("Authorization");
        	request.addHeader("Authorization", authorization);
        }
        
        Header[] acceptHeaders = request.getHeaders("Accept");
        if(randomization != null && acceptHeaders.length == 1) // It always should...
        {
        	request.removeHeaders("Accept");
        	request.addHeader("Accept", acceptHeaders[0].getValue() + ";" + randomization);
        }
    }
    
    public void setCookies(String cookies)
    {
    	this.cookies = cookies;
    }
    
    public void setAuthorization(String authorization)
    {
    	this.authorization = authorization;
    }
    
    public void setRandomization(String randomization)
    {
    	this.randomization = randomization;
    }
    
    public void setReferer(String referer)
    {
    	this.referer = referer;
    }
    
    public void setUserAgent(String useragent)
    {
    	this.useragent = useragent;
    }
    
    public void clear()
    {
    	this.cookies = null;
    	this.referer = null;
    	this.useragent = null;
    	this.authorization = null;
    }
}