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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.simpleframework.xml.core.Persister;

import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.Session;
import com.clarionmedia.infinitum.orm.internal.OrmPreconditions;
import com.clarionmedia.infinitum.orm.rest.Deserializer;
import com.clarionmedia.infinitum.orm.rest.XmlDeserializer;
import com.clarionmedia.infinitum.web.rest.impl.RestResponse;

/**
 * <p>
 * Concrete implementation of {@link RestfulSession} for web services which send
 * responses back as XML.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 05/21/12
 * @since 1.0
 */
public class RestfulXmlSession extends RestfulSession {

	protected Map<Class<?>, XmlDeserializer<?>> mXmlDeserializers;

	/**
	 * Creates a new {@code RestfulXmlSession}.
	 */
	public RestfulXmlSession() {
		mXmlDeserializers = new HashMap<Class<?>, XmlDeserializer<?>>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T loadEntity(Class<T> type, Serializable id) throws InfinitumRuntimeException, IllegalArgumentException {
		OrmPreconditions.checkForOpenSession(mIsOpen);
		OrmPreconditions.checkPersistenceForLoading(type, mPersistencePolicy);
		mLogger.debug("Sending GET request to retrieve entity");
		String uri = mHost + mPersistencePolicy.getRestEndpoint(type) + "/" + id;
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "application/xml");
		try {
			RestResponse response = mRestClient.executeGet(uri, headers);
			if (response.getStatusCode() == HttpStatus.SC_OK) {
				String xmlResponse = response.getResponseDataAsString();
				T ret = null;
				// Attempt to use a registered deserializer
				if (mXmlDeserializers.containsKey(type))
					ret = (T) mXmlDeserializers.get(type).deserializeObject(xmlResponse);
				// Otherwise fallback to Simple
				else
					ret = new Persister().read(type, xmlResponse);
				if (ret != null) {
				    int objHash = mPersistencePolicy.computeModelHash(ret);
				    cache(objHash, ret);
				}
				return ret;
			}
		} catch (Exception e) {
			mLogger.error("Unable to read web service response", e);
			return null;
		}
		return null;
	}

	@Override
	public <T> Session registerDeserializer(Class<T> type, Deserializer<T> deserializer) {
		if (XmlDeserializer.class.isAssignableFrom(deserializer.getClass()))
			mXmlDeserializers.put(type, (XmlDeserializer<T>) deserializer);
		return this;
	}

}
