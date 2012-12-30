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

import java.lang.reflect.Field;
import com.clarionmedia.infinitum.orm.ResultSet;

/**
 * <p>
 * Facilitates the mapping of datastore values to Java data types.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/17/12
 * @since 1.0
 */
public interface TypeAdapter<T> {

	/**
	 * Maps a datastore value to a domain model's {@link Field}.
	 * 
	 * @param result
	 *            the {@link ResultSet} where the column is being mapped from
	 * @param index
	 *            the index of the column holding the data to be mapped
	 * @param field
	 *            the {@code Field} being mapped to
	 * @param model
	 *            the model which the {@code Field} is populating
	 * @throws IllegalArgumentException
	 *             if the mapped value is not compatible with the declaring
	 *             class
	 * @throws IllegalAccessException
	 *             if the {@code Field} is not accessible
	 */
	void mapToObject(ResultSet result, int index, Field field, Object model)
			throws IllegalArgumentException, IllegalAccessException;

}
