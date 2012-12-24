/*
 * Copyright (c) 2012 Tyler Treat
 * 
 * This file is part of Infinitum Framework.
 *
 * Infinitum Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Infinitum Framework is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Infinitum Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.clarionmedia.infinitum.http.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.RequestWrapper;

import com.clarionmedia.infinitum.http.HttpClientRequest;

/**
 * <p>
 * Wrapper for {@link HttpUriRequest} to support hashing and equality for
 * the purpose of HTTP caching.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 08/14/12
 * @since 1.0
 */
public class HashableHttpRequest extends RequestWrapper implements HttpClientRequest {

	private RequestWrapper mHttpRequest;

	/**
	 * Creates a new {@code HashableHttpRequest} for the given
	 * {@link RequestWrapper}.
	 * 
	 * @param request
	 *            the {@code RequestWrapper} to wrap
	 * @throws ProtocolException 
	 */
	public HashableHttpRequest(RequestWrapper request) throws ProtocolException {
		super(request);
		mHttpRequest = request;
	}
	
	@Override
	public HttpUriRequest unwrap() {
		return mHttpRequest;
	}

	@Override
	public Map<String, String> getHeaders() {
		Map<String, String> headers = new HashMap<String, String>();
		for (Header header : mHttpRequest.getAllHeaders())
			headers.put(header.getName(), header.getValue());
		return headers;
	}

	@Override
	public String getHeader(String header) {
		Header[] headers = mHttpRequest.getHeaders(header);
		StringBuilder sb = new StringBuilder();
		String prefix = "";
		for (Header h : headers) {
			sb.append(prefix);
			sb.append(h.getValue());
			prefix = ";";
		}
		return sb.toString();
	}
	
	@Override
	public void addHeader(String header, String value) {
		mHttpRequest.addHeader(header, value);
	}

	@Override
	public String getRequestUri() {
		return mHttpRequest.getURI().toString();
	}
	
	@Override
	public void setRequestUri(String uri) {
		mHttpRequest.setURI(URI.create(uri));
	}
	
	@Override
	public String getHttpMethod() {
		return mHttpRequest.getMethod();
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int hash = 7;
		hash *= PRIME + mHttpRequest.getMethod().hashCode();
		for (Header header : mHttpRequest.getAllHeaders()) {
			hash *= PRIME + header.getName().hashCode();
			hash *= PRIME + header.getValue().hashCode();
		}
		hash *= PRIME
				+ mHttpRequest.getProtocolVersion().getProtocol()
						.hashCode();
		hash *= PRIME + mHttpRequest.getProtocolVersion().getMajor();
		hash *= PRIME + mHttpRequest.getProtocolVersion().getMinor();
		hash *= PRIME + mHttpRequest.getURI().toString().hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (getClass() != other.getClass())
			return false;
		HashableHttpRequest otherRequest = (HashableHttpRequest) other;
		Header[] headers = mHttpRequest.getAllHeaders();
		Header[] otherHeaders = otherRequest.mHttpRequest.getAllHeaders();
		if (headers.length != otherHeaders.length)
			return false;
		boolean match = false;
		for (Header header : headers) {
			for (Header otherHeader : otherHeaders) {
				if (header.getName().equals(otherHeader.getName())
						&& header.getValue().equals(otherHeader.getValue())) {
					match = true;
					break;
				}
			}
			if (!match)
				return false;
		}
		return mHttpRequest.getMethod().equals(
				otherRequest.mHttpRequest.getMethod())
				&& mHttpRequest
						.getProtocolVersion()
						.getProtocol()
						.equals(otherRequest.mHttpRequest
								.getProtocolVersion().getProtocol())
				&& mHttpRequest.getProtocolVersion().getMajor() == otherRequest.mHttpRequest
						.getProtocolVersion().getMajor()
				&& mHttpRequest.getProtocolVersion().getMinor() == otherRequest.mHttpRequest
						.getProtocolVersion().getMinor()
				&& mHttpRequest.getURI().equals(
						otherRequest.mHttpRequest.getURI());
	}
	
	@Override
	public String toString() {
		return "[" + mHttpRequest.getMethod() + " " + mHttpRequest.getURI().toString() + "]";
	}
	
}