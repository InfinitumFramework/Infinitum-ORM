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

package com.clarionmedia.infinitum.orm.rest;

import java.util.List;

/**
 * <p>
 * Provides an API for deserializing JSON responses into domain model instances.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/25/12
 */
public abstract class JsonDeserializer<T> implements Deserializer<T> {

	/**
	 * Deserializes the given JSON {@link String} into an Object of the generic
	 * type.
	 * 
	 * @param json
	 *            the JSON {@code String} to deserialize
	 * @return {@code Object}
	 */
	public abstract T deserializeObject(String json);

	/**
	 * Deserializes the given JSON {@link String} consisting of an object array
	 * into a {@link List} of objects of the generic type.
	 * 
	 * @param json
	 *            the JSON {@code String} to deserialize
	 * @return {@code List} of {@code Objects}
	 */
	public abstract List<T> deserializeObjects(String json);

}
