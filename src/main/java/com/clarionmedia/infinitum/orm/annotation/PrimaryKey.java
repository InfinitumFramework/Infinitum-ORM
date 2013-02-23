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
