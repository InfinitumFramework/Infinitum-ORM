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

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.clarionmedia.infinitum.orm.ObjectMapper;
import com.clarionmedia.infinitum.orm.exception.InvalidMappingException;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.TypeAdapter;
import com.clarionmedia.infinitum.orm.rest.RestfulMapper;
import com.clarionmedia.infinitum.orm.rest.RestfulPairsTypeAdapter;
import com.clarionmedia.infinitum.orm.rest.RestfulXmlTypeAdapter;

/**
 * <p>
 * This implementation of {@link ObjectMapper} provides methods to map domain
 * models to RESTful web service resources for XML message types.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 08/05/12
 * @since 1.0
 */
public class RestfulXmlMapper extends RestfulMapper {

	private Map<Class<?>, RestfulXmlTypeAdapter<?>> mTypeAdapters;
	private Serializer mSerializer;

	/**
	 * Constructs a new {@code RestfulJsonMapper}.
	 */
	public RestfulXmlMapper() {
		mTypeAdapters = new HashMap<Class<?>, RestfulXmlTypeAdapter<?>>();
		mSerializer = new Persister();
	}

	@Override
	public RestfulStringModelMap mapModel(Object model) throws InvalidMappingException, ModelConfigurationException {
		// We do not map transient classes!
		if (!mPersistencePolicy.isPersistent(model.getClass()))
			return null;
		RestfulStringModelMap modelMap = new RestfulStringModelMap(model);
		String xml;
		if (mTypeAdapters.containsKey(model.getClass()))
			xml = mTypeAdapters.get(model.getClass()).serializeObjectToXml(model);
		else {
			StringWriter writer = new StringWriter();
			try {
				mSerializer.write(model, writer);
				xml = writer.toString();
			} catch (Exception e) {
				return null;
			}
		}
		modelMap.setMessage(xml);
		return modelMap;
	}

	@Override
	public <T> void registerTypeAdapter(Class<T> type, TypeAdapter<T> adapter) {
		if (RestfulXmlTypeAdapter.class.isAssignableFrom(adapter.getClass()))
		    mTypeAdapters.put(type, (RestfulXmlTypeAdapter<?>) adapter);
	}

	@Override
	public Map<Class<?>, ? extends TypeAdapter<?>> getRegisteredTypeAdapters() {
		return mTypeAdapters;
	}

	@Override
	public <T> RestfulPairsTypeAdapter<T> resolveType(Class<T> type) {
		throw new UnsupportedOperationException();
	}

}
