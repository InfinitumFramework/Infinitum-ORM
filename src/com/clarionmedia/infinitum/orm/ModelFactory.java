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
