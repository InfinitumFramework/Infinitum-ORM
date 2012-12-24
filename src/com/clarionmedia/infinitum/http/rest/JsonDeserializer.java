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

package com.clarionmedia.infinitum.http.rest;

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
