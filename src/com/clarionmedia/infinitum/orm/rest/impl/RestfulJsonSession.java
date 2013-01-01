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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;

import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.Session;
import com.clarionmedia.infinitum.orm.internal.OrmPreconditions;
import com.clarionmedia.infinitum.orm.rest.Deserializer;
import com.clarionmedia.infinitum.orm.rest.JsonDeserializer;
import com.clarionmedia.infinitum.web.rest.impl.RestResponse;
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
		OrmPreconditions.checkForOpenSession(mIsOpen);
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
