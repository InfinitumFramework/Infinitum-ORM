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

import com.clarionmedia.infinitum.di.annotation.Component;
import com.clarionmedia.infinitum.orm.OrmConstants.PersistenceMode;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy.Cascade;

import java.lang.annotation.*;

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
 * @version 1.1.0 06/13/13
 * @since 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
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
