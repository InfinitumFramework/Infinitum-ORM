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

package com.clarionmedia.infinitum.orm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;

/**
 * <p>
 * This annotation indicates if a {@link Field} is a primary key. If the
 * annotation is missing from the class hierarchy, Infinitum will look for a
 * {@code Field} called {@code mId} or {@code id} to use as the primary key. If
 * such a {@code Field} is found, autoincrement will be enabled for it by
 * default if it is of type {@code int} or {@code long}. If the primary key is
 * assigned to a {@code Field} which is not an {@code int} or {@code long}
 * and {@code autoincrement} is enabled, a
 * {@link ModelConfigurationException} will be thrown at runtime. Any
 * {@code Field} marked as a primary key will inherently be marked as
 * persistent, regardless of any {@link Persistence} annotation that might be
 * associated with it.
 * </p>
 * 
 * <p>
 * The Infinitum ORM currently only supports a single primary key per model,
 * meaning composite keys will throw a {@code ModelConfigurationException}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/13/12
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PrimaryKey {

	/**
	 * Indicates if the primary key is set to autoincrement. This is only valid
	 * for primary keys which are of type {@code int} or {@code long}.
	 * 
	 * @return {@code true} if autoincrement is enabled, {@code false} if not
	 */
	boolean autoincrement() default true;

}
