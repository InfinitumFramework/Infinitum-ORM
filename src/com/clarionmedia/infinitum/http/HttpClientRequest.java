package com.clarionmedia.infinitum.http;

import org.apache.http.client.methods.HttpUriRequest;

/**
 * <p>
 * Encapsulates an HTTP request message sent to a server.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 08/15/12
 * @since 1.0
 */
public interface HttpClientRequest extends HttpClientMessage {

	/**
	 * Returns the request URI.
	 * 
	 * @return uri the request URI
	 */
	String getRequestUri();

	/**
	 * Sets the request URI.
	 * 
	 * @param uri
	 *            the request URI to set
	 */
	void setRequestUri(String uri);

	/**
	 * Returns the HTTP method name, such as GET, POST, PUT, etc.
	 * 
	 * @return HTTP method name
	 */
	String getHttpMethod();

	/**
	 * Adds the given header and value to the request.
	 * 
	 * @param header
	 *            the header name
	 * @param value
	 *            the header value
	 */
	void addHeader(String header, String value);

	/**
	 * Returns the wrapped {@link HttpUriRequest}.
	 * 
	 * @return {@code HttpUriRequest}
	 */
	HttpUriRequest unwrap();

}
