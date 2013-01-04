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

package com.clarionmedia.infinitum.orm.persistence;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * <p>
 * This interface provides runtime resolution of data types for the purpose of
 * persistence in the ORM.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 05/17/12
 */
public interface TypeResolutionPolicy {

	// Represent the data types used in SQLite
	public static enum SqliteDataType {
		NULL, INTEGER, REAL, TEXT, BLOB
	};

	/**
	 * Indicates if the given ID is a valid value for the given primary key
	 * {@link Field}. Precondition assumes pkField is in fact a primary key.
	 * 
	 * @param pkField
	 *            the primary key {@code Field}
	 * @param id
	 *            the primary key value to check
	 * @return {@code true} if it is a valid primary key value, {@code false} if
	 *         not
	 */
	boolean isValidPrimaryKey(Field pkField, Serializable id);

	/**
	 * Indicates if the given {@link Class} is a registered domain model for
	 * this application.
	 * 
	 * @param c
	 *            the {@code Class} to check
	 * @return {@code true} if it is a domain model, {@code false} if not
	 */
	boolean isDomainModel(Class<?> c);

	/**
	 * Indicates if the given {@link Class} is a proxy for a domain model.
	 * 
	 * @param c
	 *            the {@code Class} to check
	 * @return {@code true} if it is a domain proxy, {@code false} if not
	 */
	boolean isDomainProxy(Class<?> c);

}
