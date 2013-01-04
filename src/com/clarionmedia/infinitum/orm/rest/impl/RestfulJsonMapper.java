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

import java.util.HashMap;
import java.util.Map;

import com.clarionmedia.infinitum.orm.exception.InvalidMappingException;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.TypeAdapter;
import com.clarionmedia.infinitum.orm.rest.RestfulJsonTypeAdapter;
import com.clarionmedia.infinitum.orm.rest.RestfulMapper;
import com.clarionmedia.infinitum.orm.rest.RestfulPairsTypeAdapter;
import com.google.gson.Gson;

/**
 * <p>
 * This implementation of {@link RestfulMapper} provides methods to map domain
 * models to RESTful web service resources for JSON message types.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 08/05/12
 * @since 1.0
 */
public class RestfulJsonMapper extends RestfulMapper {

	private Map<Class<?>, RestfulJsonTypeAdapter<?>> mTypeAdapters;
	private Gson mGson;

	/**
	 * Constructs a new {@code RestfulJsonMapper}.
	 */
	public RestfulJsonMapper() {
		mTypeAdapters = new HashMap<Class<?>, RestfulJsonTypeAdapter<?>>();
		mGson = new Gson();
	}

	@Override
	public RestfulStringModelMap mapModel(Object model) throws InvalidMappingException, ModelConfigurationException {
		// We do not map transient classes!
		if (!mPersistencePolicy.isPersistent(model.getClass()))
			return null;
		RestfulStringModelMap modelMap = new RestfulStringModelMap(model);
		String json;
		if (mTypeAdapters.containsKey(model.getClass()))
			json = mTypeAdapters.get(model.getClass()).serializeObjectToJson(model);
		else
			json = mGson.toJson(model);
		modelMap.setMessage(json);
		return modelMap;
	}

	@Override
	public <T> void registerTypeAdapter(Class<T> type, TypeAdapter<T> adapter) {
		if (RestfulJsonTypeAdapter.class.isAssignableFrom(adapter.getClass()))
		    mTypeAdapters.put(type, (RestfulJsonTypeAdapter<?>) adapter);
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
