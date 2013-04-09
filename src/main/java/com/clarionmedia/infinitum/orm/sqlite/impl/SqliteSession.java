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

package com.clarionmedia.infinitum.orm.sqlite.impl;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.di.annotation.Autowired;
import com.clarionmedia.infinitum.event.annotation.Event;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.internal.caching.LruCache;
import com.clarionmedia.infinitum.logging.Logger;
import com.clarionmedia.infinitum.orm.Session;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.exception.SQLGrammarException;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.TypeAdapter;
import com.clarionmedia.infinitum.orm.rest.Deserializer;
import com.clarionmedia.infinitum.orm.sqlite.SqliteTypeAdapter;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * <p>
 * Implementation of {@link Session} for interacting with SQLite.
 * </p>
 * <p>
 * {@code SqliteSession} is threadsafe in the sense that if two threads call
 * {@link #open()}, then if one thread performs an operation and then closes
 * the {@code Session} while the other thread subsequently does the same, the
 * latter thread will not be affected by the prior closing of the {@code Session}.
 * This is because {@code SqliteSession} tracks how many times {@link #open()} has
 * been called and decrements this counter when {@link #close()} is called,
 * actually closing the {@code Session} only when the count reaches zero.
 * </p>
 *
 * @author Tyler Treat
 * @version 1.0.6 04/09/13
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
    private int mSessionCount;

    /**
     * Constructs a new {@code SqliteSession}.
     */
    public SqliteSession() {
        this(DEFAULT_CACHE_SIZE);
    }

    /**
     * Constructs a new {@code SqliteSession} with the given cache size.
     *
     * @param cacheSize the maximum number of {@code Objects} the {@code Session}
     *                  cache can store
     */
    public SqliteSession(int cacheSize) {
        mSessionCount = 0;
        mCacheSize = cacheSize;
        mLogger = Logger.getInstance(getClass().getSimpleName());
        mSessionCache = new LruCache<Integer, Object>(mCacheSize);
    }

    @Override
    public synchronized Session open() throws SQLException {
        try {
            mSqlite.open();
            mSessionCount++;
            mLogger.debug("Session opened");
            return this;
        } catch (SQLException e) {
            mLogger.error("Session not opened", e);
            throw e;
        }
    }

    @Override
    public synchronized Session close() {
        mSessionCount--;
        if (mSessionCount == 0) {
            // No more open references to this Session, so actually close it
            mSqlite.close();
            recycleCache();
        }
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
    @Event("entitySaved")
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
    @Event("entityUpdated")
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
    @Event("entityDeleted")
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
    @Event("entitySavedOrUpdated")
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
    public int saveOrUpdateAll(Collection<? extends Object> models) throws InfinitumRuntimeException {
        int count = 0;
        for (Object model : models) {
            if (saveOrUpdate(model) >= 0)
                count++;
        }
        return count;
    }

    @Override
    public int saveAll(Collection<? extends Object> models) throws InfinitumRuntimeException {
        int count = 0;
        for (Object model : models) {
            if (save(model) != -1)
                count++;
        }
        return count;
    }

    @Override
    public int deleteAll(Collection<? extends Object> models) throws InfinitumRuntimeException {
        int count = 0;
        for (Object model : models) {
            if (delete(model))
                count++;
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T load(Class<T> c, Serializable id) throws InfinitumRuntimeException, IllegalArgumentException {
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
    public <T> Session registerDeserializer(Class<T> type, Deserializer<T> deserializer) {
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
     * @param sql the SQL query to execute
     * @return {@link Cursor} containing the results of the query
     * @throws SQLGrammarException if the SQL was formatted incorrectly
     */
    public Cursor executeForResult(String sql) throws SQLGrammarException {
        return mSqlite.executeForResult(sql);
    }

    /**
     * Executes the given count query and returns the number of rows resulting
     * from it.
     *
     * @param sql the SQL count query to execute
     * @return number of rows
     * @throws SQLGrammarException if the SQL was formatted incorrectly
     */
    public int count(String sql) throws SQLGrammarException {
        Cursor result = null;
        try {
            result = mSqlite.executeForResult(sql);
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
