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

/**
 * <p>
 * Encapsulates an HTTP response message sent back from a server.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 08/15/12
 * @since 1.0
 */
public interface HttpClientResponse extends HttpClientMessage {

	/**
	 * Returns the HTTP status code that was included with the response.
	 * 
	 * @return status code
	 */
	int getStatusCode();

	/**
	 * Returns the response message data as it was received from the server.
	 * 
	 * @return message data as a byte array
	 */
	byte[] getResponseData();

	/**
	 * Returns the response message data as a {@link String}.
	 * 
	 * @return message data {@code String}
	 */
	String getResponseDataAsString();

	/**
	 * Returns the cookies that were included with the response.
	 * 
	 * @return {@link Map} containing cookies
	 */
	Map<String, String> getCookies();

}
