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

import org.apache.http.HttpEntity;
import com.clarionmedia.infinitum.orm.ModelMap;

/**
 * <p>
 * Abstract implementation of {@link ModelMap} representing a domain model
 * instance mapped to a RESTful web service resource.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 08/05/12
 * @since 1.0
 */
public abstract class RestfulModelMap extends ModelMap {

	/**
	 * Creates a new {@code RestfulModelMap} for the given {@link Object}.
	 * 
	 * @param model
	 *            the {@code Object} to map
	 */
	public RestfulModelMap(Object model) {
		super(model);
	}

	/**
	 * Retrieves an {@link HttpEntity} representation of the model.
	 * 
	 * @return {@code HttpEntity}
	 */
	public abstract HttpEntity toHttpEntity();

}
