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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;

import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.http.rest.Deserializer;
import com.clarionmedia.infinitum.http.rest.JsonDeserializer;
import com.clarionmedia.infinitum.orm.Session;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * <p>
 * Concrete implementation of {@link RestfulSession} for web services which send
 * responses back as JSON.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/21/12
 * @since 1.0
 */
public class RestfulJsonSession extends RestfulSession {

	protected Map<Class<?>, JsonDeserializer<?>> mJsonDeserializers;

	/**
	 * Creates a new {@code RestfulJsonSession}.
	 */
	public RestfulJsonSession() {
		mJsonDeserializers = new HashMap<Class<?>, JsonDeserializer<?>>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T loadEntity(Class<T> type, Serializable id) throws InfinitumRuntimeException, IllegalArgumentException {
		mLogger.debug("Sending GET request to retrieve entity");
		String uri = mHost + mPersistencePolicy.getRestEndpoint(type) + "/" + id;
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "application/json");
		try {
			RestResponse response = mRestClient.executeGet(uri, headers);
			if (response.getStatusCode() == HttpStatus.SC_OK) {
				String jsonResponse = response.getResponseDataAsString();
				T ret;
				// Attempt to use a registered deserializer
				if (mJsonDeserializers.containsKey(type))
					ret = (T) mJsonDeserializers.get(type).deserializeObject(jsonResponse);
				// Otherwise fallback to Gson
				else
					ret = new Gson().fromJson(jsonResponse, type);
				int objHash = mPersistencePolicy.computeModelHash(ret);
				cache(objHash, ret);
				return ret;
			}
		} catch (JsonSyntaxException e) {
			mLogger.error("Unable to deserialize web service response", e);
			return null;
		}
		return null;
	}

	@Override
	public <T> Session registerDeserializer(Class<T> type, Deserializer<T> deserializer) {
		if (JsonDeserializer.class.isAssignableFrom(deserializer.getClass()))
			mJsonDeserializers.put(type, (JsonDeserializer<T>) deserializer);
		return this;
	}

}
