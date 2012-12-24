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

package com.clarionmedia.infinitum.http.rest.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.http.client.methods.HttpUriRequest;

import com.clarionmedia.infinitum.http.impl.HashableHttpRequest;
import com.clarionmedia.infinitum.internal.caching.AbstractCache;

/**
 * <p>
 * Concrete implementation of {@link AbstractCache} for caching REST responses.
 * The cache is keyed off of a {@link HashableHttpRequest}, which is a wrapper
 * for {@link HttpUriRequest} that implements {@code hashCode} and
 * {@code equals} methods.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 08/15/12
 * @since 1.0
 */
public class RestResponseCache extends AbstractCache<HashableHttpRequest, RestResponse> {

	private static final String CACHE_NAME = "httpcache";
	
	/**
	 * Creates a new {@code HttpResponseCache} with the given initial capacity
	 * and default expiration timeout.
	 * 
	 * @param initialCapacity
	 *            the initial cache capacity
	 * @param defaultExpiration
	 *            the default expiration timeout in seconds
	 */
	public RestResponseCache() {
		super(CACHE_NAME);
	}
	
	/**
	 * Creates a new {@code HttpResponseCache} with the given default expiration timeout.
	 * 
	 * @param defaultExpiration
	 *            the default expiration timeout in seconds
	 */
	public RestResponseCache(long defaultExpiration) {
		super(CACHE_NAME, defaultExpiration);
	}

	/**
	 * Creates a new {@code HttpResponseCache} with the given initial capacity
	 * and default expiration timeout.
	 * 
	 * @param initialCapacity
	 *            the initial cache capacity
	 * @param defaultExpiration
	 *            the default expiration timeout in seconds
	 */
	public RestResponseCache(int initialCapacity, long defaultExpiration) {
		super(CACHE_NAME, initialCapacity, defaultExpiration);
	}

	@Override
	public String getFileNameForKey(HashableHttpRequest request) {
		return getFileNameFromUri(request);
	}

	@Override
	protected RestResponse readValueFromDisk(File file) throws IOException {
		BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
		long fileSize = file.length();
		if (fileSize > Integer.MAX_VALUE) {
			inputStream.close();
			throw new IOException("Cannot read files larger than " + Integer.MAX_VALUE + " bytes.");
		}

		// The first byte is the status code
		int statusCode = inputStream.read();
		
		// The second byte is the size of the header data
		int headerSize = inputStream.read();
		
		// Next is the header data itself
		byte[] headerData = new byte[headerSize];
		inputStream.read(headerData, 0, headerSize);

		// The remainder is the message data
		int messageDataSize = (int) fileSize - headerSize - 2;
		byte[] messageData = new byte[messageDataSize];
		inputStream.read(messageData, 0, messageDataSize);
		inputStream.close();

		RestResponse response = new RestResponse();
		response.setStatusCode(statusCode);
		response.setResponseData(messageData);
		String headers = new String(headerData);
		Map<String, String> headerMap = new HashMap<String, String>();
		String[] values = headers.split("\n");
		for (int i = 0; i < values.length;)
			headerMap.put(values[i++], values[i++]);
		response.setHeaders(headerMap);
		return response;
	}

	@Override
	protected void writeValueToDisk(File file, RestResponse data) throws IOException {
		BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
		outputStream.write(data.getStatusCode());
		byte[] headerData = getHeaderData(data.getHeaders());
		outputStream.write(headerData.length);
		outputStream.write(headerData);
		outputStream.write(data.getResponseData());
		outputStream.close();
	}

	private String getFileNameFromUri(HashableHttpRequest request) {
		// replace all special URI characters with a single y symbol
		String uri =  request.getRequestUri().toLowerCase(Locale.getDefault());
		if (uri.startsWith("http://"))
			uri = uri.substring(7);
		else if (uri.startsWith("https://"))
			uri = uri.substring(8);
		String formatted = request.getHttpMethod() + '_' + uri;
		return formatted.replaceAll("[.:/,%?&=]", "_").replaceAll("[+]+", "_");
	}

	private byte[] getHeaderData(Map<String, String> headers) {
		StringBuilder sb = new StringBuilder();
		String prefix = "";
		for (Entry<String, String> header : headers.entrySet()) {
			sb.append(prefix).append(header.getKey()).append('\n').append(header.getValue());
			prefix = "\n";
		}
		return sb.toString().getBytes();
	}
	
}
