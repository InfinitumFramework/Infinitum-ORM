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
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import com.clarionmedia.infinitum.http.rest.RestfulModelMap;

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
