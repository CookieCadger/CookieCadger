package com.mattslifebytes.cookiecadger;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

/**
 * RequestInterceptor is a routine that implements a specific aspect of the HTTP
 * protocol. Usually protocol interceptors are expected to act upon one specific
 * header or a group of related headers of the incoming message or populate the
 * outgoing message with one specific header or a group of related headers.
 * Protocol Interceptors can also manipulate content entities enclosed with
 * messages. Usually this is accomplished by using the 'Decorator' pattern where
 * a wrapper entity class is used to decorate the original entity. Protocol
 * interceptors must be implemented as thread-safe. Similarly to servlets,
 * protocol interceptors should not use instance variables unless access to
 * those variables is synchronized.
 */
public class RequestInterceptor implements HttpRequestInterceptor {

	private String cookies = null;
	private String referrer = null;
	private String useragent = null;
	private String randomization = null;
	private String authorization = null;

	/**
	 * Processes a request. On the client side, this step is performed before
	 * the request is sent to the server. On the server side, this step is
	 * performed on incoming messages before the message body is evaluated.
	 * 
	 * @param request
	 *            the request to preprocess
	 * @param context
	 *            the context for the request
	 */
	@Override
	public void process(HttpRequest request, HttpContext context)
			throws HttpException, IOException {
		synchronized (this) {
			if (cookies != null) {
				request.removeHeaders("Cookie");
				request.addHeader("Cookie", cookies);
			}
			if (referrer != null) {
				request.removeHeaders("Referer");
				request.removeHeaders("Referrer");
				request.addHeader("Referer", referrer);
				// After first load, let the browser take over
				setReferrer(null);
			}
			if (useragent != null) {
				request.removeHeaders("User-Agent");
				request.addHeader("User-Agent", useragent);
			}
			if (authorization != null) {
				request.removeHeaders("Authorization");
				request.addHeader("Authorization", authorization);
			}
			Header[] acceptHeaders = request.getHeaders("Accept");
			if (randomization != null && acceptHeaders.length == 1) {
				// This is expected to always be true
				request.removeHeaders("Accept");
				request.addHeader("Accept", acceptHeaders[0].getValue() + ", "
						+ randomization);
			}
		}
	}

	/**
	 * Sets the cookie for the RequestInterceptor
	 * @param cookies the cookie to set
	 */
	public void setCookies(String cookies) {
		this.cookies = cookies;
	}

	/**
	 * Sets the authorization for the RequestInterceptor
	 * @param authorization the authorization to set
	 */
	public void setAuthorization(String authorization) {
		this.authorization = authorization;
	}

	/**
	 * Sets the randomization for the RequestInterceptor
	 * @param randomization the randomization to set
	 */
	public void setRandomization(String randomization) {
		this.randomization = randomization;
	}

	/**
	 * Sets the referrer for the RequestInterceptor
	 * @param referer the referrer to set
	 */
	public void setReferrer(String referrer) {
		this.referrer = referrer;
	}

	/**
	 * Sets the user agent for the RequestInterceptor
	 * @param useragent the user agent to set
	 */
	public void setUserAgent(String useragent) {
		this.useragent = useragent;
	}

	/**
	 * Clears cookie, referrer, user agent, and authorization data from RequestInterceptor.
	 */
	public void clear() {
		this.cookies = null;
		this.referrer = null;
		this.useragent = null;
		this.authorization = null;
	}

}
