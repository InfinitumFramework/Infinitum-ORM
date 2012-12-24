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

package com.clarionmedia.infinitum.orm.sqlite;

import java.lang.reflect.Field;
import java.util.Map;

import android.database.Cursor;
import android.database.SQLException;

import com.clarionmedia.infinitum.orm.DatastoreOperations;
import com.clarionmedia.infinitum.orm.exception.SQLGrammarException;
import com.clarionmedia.infinitum.orm.persistence.TypeAdapter;
import com.clarionmedia.infinitum.orm.sqlite.impl.SqliteTemplate;

/**
 * <p>
 * This interface specifies methods for basic SQLite operations. This is
 * typically not used directly, rather the implementation {@link SqliteTemplate}
 * is used.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/11/12
 */
public interface SqliteOperations extends DatastoreOperations {

	/**
	 * Opens the database for transactions.
	 * 
	 * @throws SQLException
	 *             if the database cannot be opened
	 */
	void open() throws SQLException;

	/**
	 * Closes the database.
	 */
	void close();

	/**
	 * Indicates if the database is open.
	 * 
	 * @return {@code true} if the database is open, {@code false} if not
	 */
	boolean isOpen();

	/**
	 * Starts a new database transaction. If autocommit is enabled, invoking
	 * this method will have no effect. A transaction must be committed or
	 * rolled back by calling {@link SqliteOperations#commit()} or
	 * {@link SqliteOperations#rollback()}, respectively.
	 */
	void beginTransaction();

	/**
	 * Commits the current transaction. This method is idempotent. If no
	 * transaction is open or autocommit is enabled, this will have no effect.
	 */
	void commit();

	/**
	 * Rolls back the current transaction. This method is idempotent. If no
	 * transaction is open or autocommit is enabled, this will have no effect.
	 */
	void rollback();

	/**
	 * Indicates if a transaction is currently open.
	 * 
	 * @return {@code true} if a transaction is open, {@code false} if not
	 */
	boolean isTransactionOpen();

	/**
	 * Sets the autocommit value for this {@code SqliteOperations}. If
	 * autocommit is enabled, a transaction must be initiated by invoking
	 * {@link SqliteOperations#beginTransaction()}. A transaction must then be
	 * committed or rolled back by calling {@link SqliteOperations#commit()} or
	 * {@link SqliteOperations#rollback()}, respectively. If autocommit is
	 * disabled, database operations will be committed when they are executed.
	 * 
	 * @param autocommit
	 *            {@code true} to enable autocommit, {@code false} to disable it
	 */
	void setAutocommit(boolean autocommit);

	/**
	 * Indicates if autocommit is enabled or disabled.
	 * 
	 * @return {@code true} if autocommit is enabled, {@code false} if not
	 */
	boolean isAutocommit();

	/**
	 * Executes the given SQL query on the database for a result.
	 * 
	 * @param sql
	 *            the SQL query to execute
	 * @param force
	 *            indicates if the query should be executed regardless of
	 *            transaction state
	 * @return {@link Cursor} containing the results of the query
	 * @throws SQLGrammarException
	 *             if the SQL was formatted incorrectly
	 */
	Cursor executeForResult(String sql, boolean force)
			throws SQLGrammarException;

	/**
	 * Registers the given {@link TypeAdapter} for the specified {@link Class}
	 * with this {@code SqliteMapper} instance. The {@code TypeAdapter} allows a
	 * {@link Field} of this type to be mapped to a database column. Registering
	 * a {@code TypeAdapter} for a {@code Class} which already has a
	 * {@code TypeAdapter} registered for it will result in the previous
	 * {@code TypeAdapter} being overridden.
	 * 
	 * @param type
	 *            the {@code Class} this {@code TypeAdapter} is for
	 * @param adapter
	 *            the {@code TypeAdapter} to register
	 */
	<T> void registerTypeAdapter(Class<T> type, SqliteTypeAdapter<T> adapter);

	/**
	 * Returns a {@link Map} containing all {@link SqliteTypeAdapter} instances
	 * registered with this {@code Session} and the {@link Class} instance in
	 * which they are registered for.
	 * 
	 * @return {@code Map<Class<?>, SqliteTypeAdapter<?>>
	 */
	Map<Class<?>, SqliteTypeAdapter<?>> getRegisteredTypeAdapters();

}
