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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import android.database.SQLException;

import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.http.rest.Deserializer;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.exception.SQLGrammarException;
import com.clarionmedia.infinitum.orm.persistence.TypeAdapter;
import com.clarionmedia.infinitum.orm.sqlite.impl.SqliteSession;

/**
 * <p>
 * Represents the lifecycle of an Infinitum persistence service and acts as an
 * interface to a configured application datastore. All database interaction
 * goes through the {@code Session}, which also provides an API for creating
 * {@link Criteria} and {@link Criteria} instances. {@code Session} instances
 * should be acquired from an {@link InfinitumContext} by calling
 * {@link InfinitumContext#getSession(com.clarionmedia.infinitum.context.InfinitumContext.DataSource)}
 * .
 * </p>
 * <p>
 * When a {@code Session} is acquired, it must be opened by calling
 * {@link Session#open()} before any {@code Session} operations can be
 * performed. Subsequently, {@link Session#close()} should be called to close
 * the persistence service and clean up any resources.
 * </p>
 * <p>
 * In order to keep track of transient and persistent entities, {@code Session}
 * implements a {@code Session} cache. This cache can be configured to recycle
 * automatically in order to reclaim memory in {@code infinitum.cfg.xml}. The
 * cache can also be explicitly recycled by invoking
 * {@link Session#recycleCache()}. Additionally, the cache size can be modified
 * by calling {@link Session#setCacheSize(int)}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/15/12
 * @since 1.0
 */
public interface Session {
	
	/**
	 * The default number of cache entries before eviction occurs.
	 */
	public static final int DEFAULT_CACHE_SIZE = 100;

	/**
	 * Opens the {@code Session} for transactions.
	 * 
	 * @return {@code Session} to allow chaining
	 * 
	 * @throws SQLException
	 *             if the {@code Session} cannot be opened
	 */
	Session open() throws SQLException;

	/**
	 * Closes the {@code Session} and cleans up any resources.
	 * 
	 * @return {@code Session} to allow chaining
	 */
	Session close();

	/**
	 * Indicates if the {@code Session} is open.
	 * 
	 * @return {@code true} if the {@code Session} is open, {@code false} if not
	 */
	boolean isOpen();

	/**
	 * Starts a new database transaction. If autocommit is enabled, invoking
	 * this method will have no effect. A transaction must be committed or
	 * rolled back by calling {@link Session#commit()} or
	 * {@link Session#rollback()}, respectively.
	 * 
	 * @return {@code Session} to allow chaining
	 */
	Session beginTransaction();

	/**
	 * Commits the current transaction. This method is idempotent. If no
	 * transaction is open or autocommit is enabled, this will have no effect.
	 * 
	 * @return {@code Session} to allow chaining
	 */
	Session commit();

	/**
	 * Rolls back the current transaction. This method is idempotent. If no
	 * transaction is open or autocommit is enabled, this will have no effect.
	 * 
	 * @return {@code Session} to allow chaining
	 */
	Session rollback();

	/**
	 * Indicates if a transaction is currently open.
	 * 
	 * @return {@code true} if a transaction is open, {@code false} if not
	 */
	boolean isTransactionOpen();

	/**
	 * Sets the autocommit value for this {@code Session}. If autocommit is
	 * enabled, a {@code Session} transaction must be initiated by invoking
	 * {@link Session#beginTransaction()}. A transaction must then be committed
	 * or rolled back by calling {@link Session#commit()} or
	 * {@link Session#rollback()}, respectively. If autocommit is disabled,
	 * database operations will be committed when they are executed.
	 * 
	 * @param autocommit
	 *            {@code true} to enable autocommit, {@code false} to disable it
	 * @return {@code Session} to allow chaining
	 */
	Session setAutocommit(boolean autocommit);

	/**
	 * Indicates if autocommit is enabled or disabled.
	 * 
	 * @return {@code true} if autocommit is enabled, {@code false} if not
	 */
	boolean isAutocommit();

	/**
	 * Recycles the {@code Session} cache, effectively reclaiming its memory.
	 * 
	 * @return {@code Session} to allow chaining
	 */
	Session recycleCache();

	/**
	 * Sets the {@code Session} cache capacity in terms of how many
	 * {@code Objects} it can store. The cache size dictates when the cache is
	 * recycled if it is configured to do so automatically. Once the cache
	 * reaches this capacity, it will be recycled. The cache can also be
	 * recycled manually by calling {@link SqliteSession#recycleCache()}, which
	 * will reclaim it regardless of how Infinitum is configured.
	 * 
	 * @param mCacheSize
	 *            the maximum number of {@code Objects} the cache can store
	 * @return {@code Session} to allow chaining
	 */
	Session setCacheSize(int mCacheSize);

	/**
	 * Returns the maximum capacity of the {@code Session} cache in terms of how
	 * many {@code Objects} it can store.
	 * 
	 * @return the maximum number of {@code Objects} the cache can store
	 */
	int getCacheSize();
	
	/**
	 * Caches the given model identified by the given hash code.
	 * 
	 * @param hash
	 *            the hash code which maps to the model
	 * @param model
	 *            the {@link Object} to cache
	 * @return {@code true} if the model was cached, {@code false} if not
	 */
	boolean cache(int hash, Object model);
	
	/**
	 * Indicates if the session cache contains the given hash code.
	 * 
	 * @param hash
	 *            the hash code to check for
	 * @return {@code true} if the cache contains the hash code, {@code false}
	 *         if not
	 */
	boolean checkCache(int hash);
	
	/**
	 * Returns the model with the given hash code from the session cache.
	 * 
	 * @param hash
	 *            the hash code of the model to retrieve
	 * @return the model {@link Object} identified by the given hash code or
	 *         {@code null} if no such entity exists in the cache
	 */
	Object searchCache(int hash);

	/**
	 * Persists the given {@link Object} to the database. This method is
	 * not idempotent, meaning if the record already exists, a new one will
	 * attempt to persist.
	 * 
	 * @param model
	 *            {@code Object} to persist to the database
	 * @return the row ID of the newly inserted record, or -1 if the insert
	 *         failed
	 * @throws InfinitumRuntimeException
	 *             if the model is marked transient
	 */
	long save(Object model) throws InfinitumRuntimeException;

	/**
	 * Updates the given {@link Object} in the database.
	 * 
	 * @param model
	 *            {@code Object} to update in the database
	 * @return {@code true} if the updated succeeded, {@code false} if it failed
	 * @throws InfinitumRuntimeException
	 *             if the model is marked transient
	 */
	boolean update(Object model) throws InfinitumRuntimeException;

	/**
	 * Deletes the given {@link Object} from the database if it exists.
	 * 
	 * @param model
	 *            {@code Object} to delete from the database
	 * @return true if the record was deleted, false otherwise
	 * @throws InfinitumRuntimeException
	 *             if the model is marked transient
	 */
	boolean delete(Object model) throws InfinitumRuntimeException;

	/**
	 * Persists the given {@link Object} to the database, or, if it already
	 * exists, updates the record.
	 * 
	 * @param model
	 *            {@code Object} to save or update in the database
	 * @return the row ID of the newly inserted row, 0 if the row was updated, or -1 if the operation failed.
	 * @throws InfinitumRuntimeException
	 *             if the model is marked transient
	 */
	long saveOrUpdate(Object model) throws InfinitumRuntimeException;

	/**
	 * Persists or updates the entire collection of {@code Objects} in the
	 * database.
	 * 
	 * @param models
	 *            {@code Objects} to save or update in the database
	 * @return the number of records saved or updated
	 * @throws InfinitumRuntimeException
	 *             if one or more of the models is marked transient
	 */
	int saveOrUpdateAll(Collection<? extends Object> models)
			throws InfinitumRuntimeException;

	/**
	 * Persists the entire collection of {@code Objects} to the database.
	 * 
	 * @param models
	 *            {@code Objects} to persist to the database
	 * @return the number of records saved
	 * @throws InfinitumRuntimeException
	 *             if one or more of the models is marked transient
	 */
	int saveAll(Collection<? extends Object> models)
			throws InfinitumRuntimeException;

	/**
	 * Deletes the entire collection of {@code Objects} from the database if
	 * they exist.
	 * 
	 * @param models
	 *            {@code Objects} to delete from the database
	 * @return the number of records deleted
	 * @throws InfinitumRuntimeException
	 *             if one or more of the models is marked transient
	 */
	int deleteAll(Collection<? extends Object> models)
			throws InfinitumRuntimeException;

	/**
	 * Returns an instance of the given persistent model {@link Class} as
	 * identified by the specified primary key or {@code null} if no such entity
	 * exists.
	 * 
	 * @param c
	 *            the {@code Class} of the persistent instance to load
	 * @param id
	 *            the primary key value of the persistent instance to load
	 * @return the persistent instance
	 * @throws InfinitumRuntimeException
	 *             if the specified {@code Class} is marked transient
	 * @throws IllegalArgumentException
	 *             if an invalid primary key is provided
	 */
	<T extends Object> T load(Class<T> c, Serializable id)
			throws InfinitumRuntimeException, IllegalArgumentException;

	/**
	 * Executes the given SQL non-query on the database, meaning no result is
	 * expected.
	 * 
	 * @param sql
	 *            the SQL query to execute
	 * @return {@code Session} to allow chaining
	 * 
	 * @throws SQLGrammarException
	 *             if the SQL was formatted incorrectly
	 */
	Session execute(String sql) throws SQLGrammarException;

	/**
	 * Creates a new {@link Criteria} instance for the given persistent entity
	 * {@link Class}.
	 * 
	 * @param entityClass
	 *            the persistent {@code Class} being queried for
	 * @return {@code Criteria} for entityClass
	 */
	<T> Criteria<T> createCriteria(Class<T> entityClass);

	/**
	 * Registers the given {@link TypeAdapter} for the specified {@link Class}
	 * with this {@code Session} instance. The {@code TypeAdapter} allows a
	 * {@link Field} of this type to be mapped to a database column. Registering
	 * a {@code TypeAdapter} for a {@code Class} which already has a
	 * {@code TypeAdapter} registered for it will result in the previous
	 * {@code TypeAdapter} being overridden.
	 * 
	 * @param type
	 *            the {@code Class} this {@code TypeAdapter} is for
	 * @param adapter
	 *            the {@code TypeAdapter} to register
	 * @return {@code Session} to allow chaining
	 */
	<T> Session registerTypeAdapter(Class<T> type, TypeAdapter<T> adapter);

	/**
	 * Returns a {@link Map} containing all {@link TypeAdapter} instances
	 * registered with this {@code Session} and the {@link Class} instances in
	 * which they are registered for.
	 * 
	 * @return {@code Map<Class<?>, ? extends TypeAdapter<?>>
	 */
	Map<Class<?>, ? extends TypeAdapter<?>> getRegisteredTypeAdapters();
	
	/**
	 * Registers the given {@link Deserializer} for the given {@link Class}
	 * type. Registering a {@code Deserializer} for a {@code Class} which
	 * already has a {@code Deserializer} registered for it will result in the
	 * previous {@code Deserializer} being overridden.
	 * 
	 * @param type
	 *            the {@code Class} to associate this deserializer with
	 * @param deserializer
	 *            the {@code Deserializer} to use when deserializing
	 *            {@code Objects} of the given type
	 * @return {@code Session} to allow chaining
	 */
	<T> Session registerDeserializer(Class<T> type, Deserializer<T> deserializer);

}
