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
 * A {@code Deserializer} is used to convert web service responses into domain
 * objects. Web service responses are typically sent back as JSON or XML. For
 * the former, see the implementation {@link JsonDeserializer}, and the latter,
 * {@link XmlDeserializer}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 05/21/12
 */
public interface Deserializer<T> {

	/**
	 * Deserializes the given response {@link String} into an Object of the
	 * generic type.
	 * 
	 * @param response
	 *            the response {@code String} to deserialize
	 * @return {@code Object}
	 */
	T deserializeObject(String response);

	/**
	 * Deserializes the given response {@link String} consisting of an object
	 * collection into a {@link List} of objects of the generic type.
	 * 
	 * @param response
	 *            the response {@code String} to deserialize
	 * @return {@code List} of {@code Objects}
	 */
	List<T> deserializeObjects(String response);

}
