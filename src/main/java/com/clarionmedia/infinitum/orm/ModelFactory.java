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

package com.clarionmedia.infinitum.orm;

import java.lang.Object;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;

/**
 * <p>
 * This interface provides an API for creating new instances of model classes
 * from ORM query results. It's important to note that model classes must
 * contain an empty, parameterless constructor in order for these methods to
 * work since {@link Object} construction is done using reflection. If no such
 * constructor is present, a {@link ModelConfigurationException} will be thrown
 * at runtime.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/20/12
 * @since 1.0
 */
public interface ModelFactory {

	/**
	 * Constructs a domain model instance and populates its fields from the
	 * given {@link ResultSet}. The precondition for this method is that the
	 * {@code ResultSet} is currently at the row to convert to an {@link Object}
	 * from the correct table.
	 * 
	 * @param result
	 *            the {@code ResultSet} containing the row to convert to an
	 *            {@code Object}
	 * @param modelClass
	 *            the {@code Class} of the {@code Object} being instantiated
	 * @return a populated instance of the specified {@code Class}
	 * @throws ModelConfigurationException
	 *             if the specified model {@code Class} does not contain an
	 *             empty constructor
	 * @throws InfinitumRuntimeException
	 *             if the model could not be instantiated
	 * @throws IllegalArgumentException
	 *             if the {@code ModelFactory} implementation cannot process the
	 *             {@code ResultSet} type
	 */
	<T> T createFromResult(ResultSet result, Class<T> modelClass)
			throws ModelConfigurationException, InfinitumRuntimeException,
			IllegalArgumentException;

}
