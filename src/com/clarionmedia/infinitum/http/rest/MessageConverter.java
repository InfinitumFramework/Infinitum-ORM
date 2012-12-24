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

package com.clarionmedia.infinitum.http.rest;

import com.clarionmedia.infinitum.http.rest.impl.RestResponse;

/**
 * <p>
 * Converts {@link RestResponse} messages to objects.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 12/23/12
 * @since 1.0
 */
public interface MessageConverter {

	/**
	 * Reads an object of the given type form the given input message and
	 * returns it.
	 * 
	 * @param clazz
	 *            the type of the object to return
	 * @param response
	 *            the response to convert
	 * @return converted object
	 */
	<T> T convert(Class<T> clazz, RestResponse response);

}
