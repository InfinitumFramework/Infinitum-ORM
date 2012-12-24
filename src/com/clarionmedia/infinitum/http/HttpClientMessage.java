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

package com.clarionmedia.infinitum.http;

import java.util.Map;

import org.apache.http.HttpMessage;

/**
 * <p>
 * Encapsulates an HTTP message, either a request or response.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 08/15/12
 * @since 1.0
 */
public interface HttpClientMessage {
	
	/**
	 * Returns the wrapped {@link HttpMessage}.
	 * 
	 * @return {@code HttpMessage}
	 */
	HttpMessage unwrap();

	/**
	 * Returns the headers that were included with the response.
	 * 
	 * @return {@link Map} containing headers
	 */
	Map<String, String> getHeaders();

	/**
	 * Returns the value for the given header.
	 * 
	 * @param header
	 *            the header to retrieve
	 * @return header value
	 */
	String getHeader(String header);

}
