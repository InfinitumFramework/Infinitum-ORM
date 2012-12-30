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

/**
 * <p>
 * This annotation indicates that the annotated {@link Field} represents a
 * many-to-one relationship with another persistent class.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/19/12
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ManyToOne {

	/**
	 * Returns the name of the persistent {@link Class} this relationship links
	 * to.
	 * 
	 * @return name of persistent {@code Class}
	 */
	String className();

	/**
	 * Returns the name of the column representing the foreign key in this
	 * relationship.
	 * 
	 * @return name of foreign key column
	 */
	String column();
	
	/**
	 * Returns the name of this relationship.
	 * 
	 * @return name of many-to-one relationship.
	 */
	String name();

}
