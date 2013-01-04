/*
 * Copyright (C) 2012 Clarion Media, LLC
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
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import com.clarionmedia.infinitum.orm.rest.RestfulModelMap;

/**
 * <p>
 * Concrete implementation of {@link RestfulModelMap} representing a domain model
 * instance mapped to a RESTful web service resource for name-value pair message
 * types.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/21/12
 * @since 1.0
 */
public class RestfulNameValueModelMap extends RestfulModelMap {

	private List<NameValuePair> mNameValuePairs;

	/**
	 * Creates a new {@code RestfulNameValueModelMap} for the given
	 * {@link Object}.
	 * 
	 * @param model
	 *            the {@code Object} to map
	 */
	public RestfulNameValueModelMap(Object model) {
		super(model);
		setNameValuePairs(new ArrayList<NameValuePair>());
	}
	
	@Override
	public HttpEntity toHttpEntity() {
		try {
			return new UrlEncodedFormEntity(mNameValuePairs);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	/**
	 * Retrieves the name-value pairs for the model.
	 * 
	 * @return {@link List} of {@link NameValuePair} instances
	 */
	public List<NameValuePair> getNameValuePairs() {
		return mNameValuePairs;
	}

	/**
	 * Sets the name-value pairs for the model.
	 * 
	 * @param nameValuePairs
	 *            the name-value pairs to set
	 */
	public void setNameValuePairs(List<NameValuePair> nameValuePairs) {
		mNameValuePairs = nameValuePairs;
	}

	/**
	 * Adds the given {@link NameValuePair} to the model map.
	 * 
	 * @param pair
	 *            the {@code NameValuePair} to add
	 */
	public void addNameValuePair(NameValuePair pair) {
		mNameValuePairs.add(pair);
	}

}
