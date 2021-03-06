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
