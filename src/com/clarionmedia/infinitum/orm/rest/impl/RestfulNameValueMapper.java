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

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.clarionmedia.infinitum.di.AbstractProxy;
import com.clarionmedia.infinitum.internal.Primitives;
import com.clarionmedia.infinitum.orm.ObjectMapper;
import com.clarionmedia.infinitum.orm.exception.InvalidMappingException;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.internal.bind.RestfulPairsTypeAdapters;
import com.clarionmedia.infinitum.orm.persistence.TypeAdapter;
import com.clarionmedia.infinitum.orm.rest.RestfulMapper;
import com.clarionmedia.infinitum.orm.rest.RestfulPairsTypeAdapter;

/**
 * <p>
 * This implementation of {@link ObjectMapper} provides methods to map domain
 * models to RESTful web service resources for name-value pair message types.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/21/12
 * @since 1.0
 */
public class RestfulNameValueMapper extends RestfulMapper {

	private Map<Class<?>, RestfulPairsTypeAdapter<?>> mTypeAdapters;

	/**
	 * Constructs a new {@code RestfulNameValueMapper}.
	 */
	public RestfulNameValueMapper() {
		mTypeAdapters = new HashMap<Class<?>, RestfulPairsTypeAdapter<?>>();
		mTypeAdapters.put(boolean.class, RestfulPairsTypeAdapters.BOOLEAN);
		mTypeAdapters.put(byte.class, RestfulPairsTypeAdapters.BYTE);
		mTypeAdapters.put(byte[].class, RestfulPairsTypeAdapters.BYTE_ARRAY);
		mTypeAdapters.put(char.class, RestfulPairsTypeAdapters.CHARACTER);
		mTypeAdapters.put(Date.class, RestfulPairsTypeAdapters.DATE);
		mTypeAdapters.put(double.class, RestfulPairsTypeAdapters.DOUBLE);
		mTypeAdapters.put(float.class, RestfulPairsTypeAdapters.FLOAT);
		mTypeAdapters.put(int.class, RestfulPairsTypeAdapters.INTEGER);
		mTypeAdapters.put(long.class, RestfulPairsTypeAdapters.LONG);
		mTypeAdapters.put(short.class, RestfulPairsTypeAdapters.SHORT);
		mTypeAdapters.put(String.class, RestfulPairsTypeAdapters.STRING);
	}

	@Override
	public RestfulNameValueModelMap mapModel(Object model)
			throws InvalidMappingException, ModelConfigurationException {
		// We do not map transient classes!
		if (!mPersistencePolicy.isPersistent(model.getClass()))
			return null;
		RestfulNameValueModelMap modelMap = new RestfulNameValueModelMap(model);
		for (Field field : mPersistencePolicy.getPersistentFields(model.getClass())) {
			// Don't map primary keys if they are autoincrementing
			if (mPersistencePolicy.isFieldPrimaryKey(field)
					&& mPersistencePolicy.isPrimaryKeyAutoIncrement(field))
				continue;
			// Map relationships
			if (mPersistencePolicy.isRelationship(field)) {
				mapRelationship(modelMap, model, field);
				continue;
			}
			// Map Field values
			mapField(modelMap, model, field);
		}
		return modelMap;
	}

	@Override
	public <T> void registerTypeAdapter(Class<T> type, TypeAdapter<T> adapter) {
		if (RestfulPairsTypeAdapter.class.isAssignableFrom(adapter.getClass()))
			mTypeAdapters.put(type, (RestfulPairsTypeAdapter<?>) adapter);
	}

	@Override
	public Map<Class<?>, ? extends TypeAdapter<?>> getRegisteredTypeAdapters() {
		return mTypeAdapters;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> RestfulPairsTypeAdapter<T> resolveType(Class<T> type)
			throws InvalidMappingException {
		type = Primitives.unwrap(type);
		if (mTypeAdapters.containsKey(type))
			return (RestfulPairsTypeAdapter<T>) mTypeAdapters.get(type);
		throw new InvalidMappingException(String.format(
				mPropLoader.getErrorMessage("CANNOT_MAP_TYPE"),
				type.getSimpleName()));
	}

	// Map Field value to NameValuePair
	private void mapField(RestfulNameValueModelMap modelMap, Object model,
			Field field) {
		if (AbstractProxy.isAopProxy(model))
			model = AbstractProxy.getTarget(model);
		Object val = mClassReflector.getFieldValue(model, field);
		String fieldName = mPersistencePolicy.getEndpointFieldName(field);
		resolveType(field.getType()).mapObjectToPair(val, fieldName,
				modelMap.getNameValuePairs());
	}

}
