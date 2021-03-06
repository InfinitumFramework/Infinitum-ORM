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

package com.clarionmedia.infinitum.orm.context;

import java.util.List;

import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;
import com.clarionmedia.infinitum.di.BeanProvider;
import com.clarionmedia.infinitum.orm.Session;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;

/**
 * <p>
 * {@code InfinitumOrmContext} is an extension of {@link InfinitumContext} that
 * contains configuration information for the framework ORM. It describes an
 * application's domain model and how it should be persisted. Entity persistence
 * can be configured using one of two policies: XML map files or annotations.
 * </p>
 * <p>
 * {@code InfinitumOrmContext} is used to retrieve {@link Session} instances for
 * configured data sources. For example, a SQLite {@code Session} would be
 * retrieved by doing the following:
 * </p>
 * 
 * <pre>
 * Session session = context.getSession(SessionType.SQLITE);
 * </pre>
 * 
 * @author Tyler Treat
 * @version 1.0 12/23/12
 * @since 1.0
 */
public interface InfinitumOrmContext extends InfinitumContext, BeanProvider {

	/**
	 * Represents the entity persistence configuration mode.
	 */
	public static enum ConfigurationMode {
		XML {
			public String toString() {
				return "xml";
			}
		},
		ANNOTATION {
			public String toString() {
				return "annotations";
			}
		}
	}

	/**
	 * Represents the configured data source for a {@link Session}.
	 */
	public static enum SessionType {
		SQLITE, REST
	}

	/**
	 * Retrieves a new {@link Session} instance for the configured data source.
	 * 
	 * @param source
	 *            the {@link SessionType} to target
	 * @return new {@code Session} instance
	 * @throws InfinitumConfigurationException
	 *             if the specified {@code SessionType} was not configured
	 */
	Session getSession(SessionType source) throws InfinitumConfigurationException;

	/**
	 * Returns the {@link ConfigurationMode} value of this
	 * {@code InfinitumContext}, indicating which style of configuration this
	 * application is using, XML- or annotation-based. An XML configuration
	 * means that domain model mappings are provided through XML mapping files,
	 * while an annotation configuration means that mappings and other
	 * properties are provided in source code using Java annotations.
	 * 
	 * @return {@code ConfigurationMode} for this application
	 */
	ConfigurationMode getConfigurationMode();

	/**
	 * Returns true if there is a SQLite database configured or false if not. If
	 * {@code infinitum.cfg.xml} is missing the {@code sqlite} element, this
	 * will be false.
	 * 
	 * @return {@code true} if a SQLite database is configured or {@code false}
	 *         if not
	 */
	boolean hasSqliteDb();

	/**
	 * Returns the name of the SQLite database for this {@code InfinitumContext}
	 * . This is the name used to construct the database and subsequently open
	 * it.
	 * 
	 * @return the name of the SQLite database for this {@code InfinitumContext}
	 */
	String getSqliteDbName();

	/**
	 * Returns the version number of the SQLite database for this
	 * {@code InfinitumContext}.
	 * 
	 * @return the SQLite database version number
	 */
	int getSqliteDbVersion();

	/**
	 * Returns a {@link List} of all fully-qualified domain model classes
	 * registered with this {@code InfinitumContext}. Domain types are defined
	 * as being persistent entities.
	 * 
	 * @return a {@code List} of all registered domain model classes
	 */
	List<String> getDomainTypes();

	/**
	 * Indicates if the database schema is configured to be automatically
	 * generated by the framework.
	 * 
	 * @return {@code true} if the schema is set to automatically generate,
	 *         {@code false} if not
	 */
	boolean isSchemaGenerated();

	/**
	 * Indicates if autocommit is enabled or disabled.
	 * 
	 * @return {@code true} if autocommit is enabled, {@code false} if not
	 */
	boolean isAutocommit();

	/**
	 * Retrieves the application {@link PersistencePolicy}, which is configured
	 * in {@code infinitum.cfg.xml}.
	 * 
	 * @return {@code PersistencePolicy} for this application
	 */
	PersistencePolicy getPersistencePolicy();

}
