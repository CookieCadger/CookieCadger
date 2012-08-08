package cookie.cadger.mattslifebytes.com;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

public class RequestInterceptor implements HttpRequestInterceptor
{
	private String cookies = null;
	private String referer = null;
	private String useragent = null;
	
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException
    {        
        if(cookies != null)
        {
        	request.removeHeaders("Cookie");
        	request.addHeader("Cookie", cookies);
        }
        
        if(referer != null)
        {
        	request.removeHeaders("Referer");
        	request.removeHeaders("Referrer");
        	request.addHeader("Referer", referer);
        }
        
        if(useragent != null)
        {
        	request.removeHeaders("User-Agent");
        	request.addHeader("User-Agent", useragent);
        }
    }
    
    public void setCookies(String cookies)
    {
    	this.cookies = cookies;
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
    }
}