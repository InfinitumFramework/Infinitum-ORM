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

package com.clarionmedia.infinitum.orm.sqlite.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.di.annotation.Autowired;
import com.clarionmedia.infinitum.di.annotation.PostConstruct;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.http.rest.Deserializer;
import com.clarionmedia.infinitum.internal.caching.LruCache;
import com.clarionmedia.infinitum.logging.Logger;
import com.clarionmedia.infinitum.orm.Session;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.exception.SQLGrammarException;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.TypeAdapter;
import com.clarionmedia.infinitum.orm.sqlite.SqliteTypeAdapter;

/**
 * <p>
 * Implementation of {@link Session} for interacting with SQLite.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/15/12
 * @since 1.0
 */
public class SqliteSession implements Session {

	@Autowired
	private SqliteTemplate mSqlite;
	
	@Autowired
	private InfinitumContext mInfinitumContext;
	
	@Autowired
	private PersistencePolicy mPolicy;
	
	private Map<Integer, Object> mSessionCache;
	private Logger mLogger;
	private int mCacheSize;

	/**
	 * Constructs a new {@code SqliteSession}.
	 */
	public SqliteSession() {
		this(DEFAULT_CACHE_SIZE);
	}

	/**
	 * Constructs a new {@code SqliteSession} with the given cache size.
	 * 
	 * @param cacheSize
	 *            the maximum number of {@code Objects} the {@code Session}
	 *            cache can store
	 */
	public SqliteSession(int cacheSize) {
		mCacheSize = cacheSize;
	}
	
	@PostConstruct
	private void init() {
		mLogger = Logger.getInstance(mInfinitumContext, getClass().getSimpleName());
		mSessionCache = new LruCache<Integer, Object>(mCacheSize);
	}

	@Override
	public Session open() throws SQLException {
		try {
			mSqlite.open();
			mLogger.debug("Session opened");
			return this;
		} catch (SQLException e) {
			mLogger.error("Session not opened", e);
			throw e;
		}
	}

	@Override
	public Session close() {
		mSqlite.close();
		recycleCache();
		mLogger.debug("Session closed");
		return this;
	}

	@Override
	public boolean isOpen() {
		return mSqlite.isOpen();
	}

	@Override
	public Session recycleCache() {
		mSessionCache.clear();
		return this;
	}

	@Override
	public Session setCacheSize(int cacheSize) {
		mCacheSize = cacheSize;
		return this;
	}

	@Override
	public int getCacheSize() {
		return mCacheSize;
	}

	@Override
	public <T> Criteria<T> createCriteria(Class<T> entityClass) {
		return mSqlite.createCriteria(entityClass);
	}

	@Override
	public long save(Object model) throws InfinitumRuntimeException {
		long id = mSqlite.save(model);
		if (id != -1) {
			// Add to session cache
			int hash = mPolicy.computeModelHash(model);
			mSessionCache.put(hash, model);
		}
		return id;
	}

	@Override
	public boolean update(Object model) throws InfinitumRuntimeException {
		boolean success = mSqlite.update(model);
		if (success) {
			// Update session cache
			int hash = mPolicy.computeModelHash(model);
			mSessionCache.put(hash, model);
		}
		return success;
	}

	@Override
	public boolean delete(Object model) throws InfinitumRuntimeException {
		boolean success = mSqlite.delete(model);
		if (success) {
			// Remove from session cache
			int hash = mPolicy.computeModelHash(model);
			mSessionCache.remove(hash);
		}
		return success;
	}

	@Override
	public long saveOrUpdate(Object model) throws InfinitumRuntimeException {
		long id = mSqlite.saveOrUpdate(model);
		if (id >= 0) {
			// Update session cache
			int hash = mPolicy.computeModelHash(model);
			mSessionCache.put(hash, model);
		}
		return id;
	}

	@Override
	public int saveOrUpdateAll(Collection<? extends Object> models)
			throws InfinitumRuntimeException {
		int count = 0;
		for (Object model : models) {
			if (mSqlite.saveOrUpdate(model) >= 0) {
				count++;
				// Update session cache
				int hash = mPolicy.computeModelHash(model);
				mSessionCache.put(hash, model);
			}
		}
		return count;
	}

	@Override
	public int saveAll(Collection<? extends Object> models)
			throws InfinitumRuntimeException {
		int count = 0;
		for (Object model : models) {
			if (mSqlite.save(model) > 0) {
				count++;
				// Update session cache
				int hash = mPolicy.computeModelHash(model);
				mSessionCache.put(hash, model);
			}
		}
		return count;
	}

	@Override
	public int deleteAll(Collection<? extends Object> models)
			throws InfinitumRuntimeException {
		int count = 0;
		for (Object model : models) {
			if (mSqlite.delete(model)) {
				count++;
				// Remove from session cache
				int hash = mPolicy.computeModelHash(model);
				mSessionCache.remove(hash);
			}
		}
		return count;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T load(Class<T> c, Serializable id)
			throws InfinitumRuntimeException, IllegalArgumentException {
		int hash = mPolicy.computeModelHash(c, id);
		if (checkCache(hash))
			return (T) mSessionCache.get(hash);
		return mSqlite.load(c, id);
	}

	@Override
	public Session execute(String sql) throws SQLGrammarException {
		mSqlite.execute(sql);
		return this;
	}

	@Override
	public <T> Session registerTypeAdapter(Class<T> type, TypeAdapter<T> adapter) {
		if (adapter instanceof SqliteTypeAdapter)
			mSqlite.registerTypeAdapter(type, (SqliteTypeAdapter<T>) adapter);
		return this;
	}

	@Override
	public Map<Class<?>, ? extends TypeAdapter<?>> getRegisteredTypeAdapters() {
		return mSqlite.getRegisteredTypeAdapters();
	}

	@Override
	public Session beginTransaction() {
		mSqlite.beginTransaction();
		return this;
	}

	@Override
	public Session commit() {
		mSqlite.commit();
		return this;
	}

	@Override
	public Session rollback() {
		mSqlite.rollback();
		return this;
	}

	@Override
	public boolean isTransactionOpen() {
		return mSqlite.isTransactionOpen();
	}

	@Override
	public Session setAutocommit(boolean autocommit) {
		mSqlite.setAutocommit(autocommit);
		return this;
	}

	@Override
	public boolean isAutocommit() {
		return mSqlite.isAutocommit();
	}

	@Override
	public <T> Session registerDeserializer(Class<T> type,
			Deserializer<T> deserializer) {
		// TODO SqliteSession does not currently utilize Deserializers
		return this;
	}
	
	@Override
	public boolean cache(int hash, Object model) {
		if (mSessionCache.size() >= mCacheSize)
			return false;
		mSessionCache.put(hash, model);
		return true;
	}

	@Override
	public boolean checkCache(int hash) {
		return mSessionCache.containsKey(hash);
	}

	@Override
	public Object searchCache(int hash) {
		return mSessionCache.get(hash);
	}

	/**
	 * Executes the given SQL query on the database for a result.
	 * 
	 * @param sql
	 *            the SQL query to execute
	 * @param force
	 *            indicates if the query should be executed regardless of
	 *            transaction state, i.e. there is no open transaction
	 * @return {@link Cursor} containing the results of the query
	 * @throws SQLGrammarException
	 *             if the SQL was formatted incorrectly
	 */
	public Cursor executeForResult(String sql, boolean force)
			throws SQLGrammarException {
		return mSqlite.executeForResult(sql, force);
	}

	/**
	 * Executes the given count query and returns the number of rows resulting
	 * from it.
	 * 
	 * @param sql
	 *            the SQL count query to execute
	 * @return number of rows
	 * @throws SQLGrammarException
	 *             if the SQL was formatted incorrectly
	 */
	public int count(String sql) throws SQLGrammarException {
		Cursor result = null;
		try {
			result = mSqlite.executeForResult(sql, true);
			if (result.moveToFirst())
				return result.getInt(0);
			else
				return 0;
		} finally {
			if (result != null)
				result.close();
		}
	}

	/**
	 * Returns the {@link Context} for this {@code SqliteSession}.
	 * 
	 * @return {@code Context}
	 */
	public Context getContext() {
		return mInfinitumContext.getAndroidContext();
	}

	/**
	 * Returns the {@link SqliteMapper} associated with this
	 * {@code SqliteSession}.
	 * 
	 * @return {@code SqliteMapper}
	 */
	public SqliteMapper getSqliteMapper() {
		return mSqlite.getSqliteMapper();
	}

}
