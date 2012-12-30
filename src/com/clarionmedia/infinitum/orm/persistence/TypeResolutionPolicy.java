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
