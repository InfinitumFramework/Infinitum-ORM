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

import com.clarionmedia.infinitum.orm.OrmConstants.PersistenceMode;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy.Cascade;

/**
 * <p>
 * This annotation is used to indicate the persistence state of a model. A model
 * can be marked as either persistent or transient using the
 * {@code PersistenceMode} enumeration. Persistent models must include an empty
 * constructor in order for the Infinitum ORM to work. For example:
 * </p>
 * 
 * <pre>
 * public class Foobar {
 * 	// ...
 * 	public Foobar() {
 * 	}
 * 	// ...
 * }
 * </pre>
 * 
 * @author Tyler Treat
 * @version 1.0
 * @since 02/12/12
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {

	/**
	 * Returns the {@link PersistenceMode} for this entity. The default mode is
	 * persistent.
	 * 
	 * @return {@code PersistenceMode}
	 */
	PersistenceMode mode() default PersistenceMode.Persistent;

	/**
	 * Indicates the entity's cascade mode. {@link Cascade#ALL} means that when
	 * the entity's state is changed in the database, all entities related to it
	 * will also be updated. {@link Cascade#NONE} means that no related entities
	 * will be cascaded. {@link Cascade#KEYS} means that only foreign keys will
	 * be cascaded.
	 * 
	 * @return {@code true} if cascading is enabled, {@code false} if not
	 */
	Cascade cascade() default Cascade.ALL;

	/**
	 * Indicates if the entity has lazy loading enabled. If it is, related
	 * entities will be dynamically loaded when accessed.
	 * 
	 * @return {@code true} if lazy loading is enabled, {@code false} if not
	 */
	boolean lazy() default true;

	/**
	 * Returns the REST endpoint name for this entity.
	 * 
	 * @return REST endpoint name
	 */
	String endpoint() default "";
}
