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

import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;

import com.clarionmedia.infinitum.http.rest.RestfulModelMap;

/**
 * <p>
 * Concrete implementation of {@link RestfulModelMap} representing a domain
 * model instance mapped to a RESTful web service resource for JSON and XML
 * message types.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/21/12
 * @since 1.0
 */
public class RestfulStringModelMap extends RestfulModelMap {

	private String mMessage;

	/**
	 * Creates a new {@code RestfulTextModelMap} for the given {@link Object}.
	 * 
	 * @param model
	 *            the {@code Object} to map
	 */
	public RestfulStringModelMap(Object model) {
		super(model);
	}

	@Override
	public HttpEntity toHttpEntity() {
		try {
			return new StringEntity(mMessage);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	/**
	 * Returns the model map message {@link String}.
	 * 
	 * @return model map message
	 */
	public String getMessage() {
		return mMessage;
	}

	/**
	 * Sets the model map message {@link String}.
	 * 
	 * @param message
	 *            the model map message to set
	 */
	public void setMessage(String message) {
		mMessage = message;
	}

}
