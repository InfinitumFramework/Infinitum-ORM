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

package com.clarionmedia.infinitum.orm.persistence.impl;

import java.io.Serializable;
import java.lang.reflect.Field;

import com.clarionmedia.infinitum.di.annotation.Autowired;
import com.clarionmedia.infinitum.internal.Primitives;
import com.clarionmedia.infinitum.orm.context.InfinitumOrmContext;
import com.clarionmedia.infinitum.orm.persistence.TypeResolutionPolicy;

/**
 * <p>
 * This class provides runtime resolution of data types for the purpose of
 * persistence in the ORM.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/14/12
 */
public class DefaultTypeResolutionPolicy implements TypeResolutionPolicy {

	@Autowired
	private InfinitumOrmContext mContext;

	@Override
	public boolean isValidPrimaryKey(Field pkField, Serializable pk) {
		if (pk == null)
			return false;
		Class<?> pkUnwrapped = Primitives.unwrap(pkField.getType());
		Class<?> idUnwrapped = Primitives.unwrap(pk.getClass());
		// Handle ambiguous PK values (Java resolves 42 as an int, but it's also
		// valid for PKs of type long, double, or float)
		if ((pkUnwrapped == long.class || pkUnwrapped == double.class || pkUnwrapped == float.class)
				&& idUnwrapped == int.class)
			return true;
		return pkUnwrapped == idUnwrapped;
	}

	@Override
	public boolean isDomainModel(Class<?> c) {
		for (String s : mContext.getDomainTypes()) {
			if (c.getName().equalsIgnoreCase(s))
				return true;
		}
		return isDomainProxy(c);
	}

	@Override
	public boolean isDomainProxy(Class<?> c) {
		for (String s : mContext.getDomainTypes()) {
			String name = s;
			if (name.contains("."))
				name = name.substring(name.lastIndexOf('.') + 1);
			if (c.getName().equalsIgnoreCase(name + "_Proxy"))
				return true;
		}
		return false;
	}

}
