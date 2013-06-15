/*
 * Copyright (C) 2013 Clarion Media, LLC
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

/**
 * <p>
 * This annotation indicates that the annotated {@link java.lang.reflect.Field} represents a
 * many-to-many relationship with another persistent class.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.1.0 06/14/13
 * @since 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ManyToMany {

	/**
	 * Returns the name of the many-to-many table.
	 * 
	 * @return table name where relationships are stored
	 */
	String table();

	/**
	 * Returns the name of the {@link java.lang.reflect.Field} identifying this {@code Class's}
	 * side of the relationship, typically the primary key.
	 * 
	 * @return name of key {@code Field}
	 */
	String keyField();

	/**
	 * Returns the name of the {@link java.lang.reflect.Field} identifying the associated
	 * {@code Class's} side of the relationship, typically the primary key of
	 * the associated {@code Class}.
	 * 
	 * @return name of foreign key {@code Field}
	 */
	String foreignField();

	/**
	 * Returns the name of this relationship.
	 * 
	 * @return name of many-to-many relationship.
	 */
	String name();

}
