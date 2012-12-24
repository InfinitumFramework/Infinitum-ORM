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
