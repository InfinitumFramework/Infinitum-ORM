/*
 * Copyright (c) 2012 Tyler Treat
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clarionmedia.infinitum.orm.rest.impl;

import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;

import com.clarionmedia.infinitum.orm.rest.RestfulModelMap;

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
