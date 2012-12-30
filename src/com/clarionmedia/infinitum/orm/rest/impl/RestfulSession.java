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

package com.clarionmedia.infinitum.orm.rest.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.database.SQLException;

import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.context.RestfulContext;
import com.clarionmedia.infinitum.context.RestfulContext.MessageType;
import com.clarionmedia.infinitum.di.annotation.Autowired;
import com.clarionmedia.infinitum.di.annotation.PostConstruct;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.internal.caching.LruCache;
import com.clarionmedia.infinitum.logging.Logger;
import com.clarionmedia.infinitum.orm.Session;
import com.clarionmedia.infinitum.orm.context.InfinitumOrmContext;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.exception.SQLGrammarException;
import com.clarionmedia.infinitum.orm.internal.OrmPreconditions;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.TypeAdapter;
import com.clarionmedia.infinitum.orm.rest.RestfulMapper;
import com.clarionmedia.infinitum.orm.rest.RestfulModelMap;
import com.clarionmedia.infinitum.web.context.InfinitumWebContext;
import com.clarionmedia.infinitum.web.rest.RestfulClient;
import com.clarionmedia.infinitum.web.rest.impl.CachingEnabledRestfulClient;
import com.clarionmedia.infinitum.web.rest.impl.RestResponse;

/**
 * <p>
 * {@link Session} implementation for communicating with a RESTful web service
 * using domain objects. Infinitum provides two concrete implementations called
 * {@link RestfulJsonClient}, which is used for web services that respond with
 * JSON, and {@link RestfulXmlClient}, which is used for web services that
 * respond with XML. These can be extended or re-implemented for specific
 * business needs.
 * </p>
 *
 * @author Tyler Treat
 * @version 1.0 02/27/12
 * @since 1.0
 */
public abstract class RestfulSession implements Session {

    protected static final String ENCODING = "UTF-8";

    @Autowired
    protected PersistencePolicy mPersistencePolicy;

    @Autowired
    protected InfinitumOrmContext mInfinitumContext;

    @Autowired
    protected InfinitumWebContext mWebContext;

    protected RestfulContext mRestContext;
    protected boolean mIsOpen;
    protected String mHost;
    protected Logger mLogger;
    protected RestfulMapper mMapper;
    protected RestfulClient mRestClient;
    protected LruCache<Integer, Object> mSessionCache;
    protected int mCacheSize;

    /**
     * Creates a new {@code RestfulSession} with the given
     * {@link InfinitumContext} and cache size.
     *
     * @param context   the {@code InfinitumContext} of the {@code Session}
     * @param cacheSize the maximum number of {@code Objects} the {@code Session}
     *                  cache can store
     */
    public RestfulSession() {
        mCacheSize = DEFAULT_CACHE_SIZE;
        mSessionCache = new LruCache<Integer, Object>(mCacheSize);
    }

    @PostConstruct
    private void init() {
        mLogger = Logger.getInstance(mInfinitumContext, getClass().getSimpleName());
        mRestClient = new CachingEnabledRestfulClient(mWebContext);
    }

    /**
     * Returns an instance of the given persistent model {@link Class} as
     * identified by the specified primary key or {@code null} if no such entity
     * exists.
     *
     * @param c  the {@code Class} of the persistent instance to load
     * @param id the primary key value of the persistent instance to load
     * @return the persistent instance
     */
    protected abstract <T> T loadEntity(Class<T> type, Serializable id);

    @Override
    public Session open() throws SQLException {
        mRestContext = mInfinitumContext.getRestContext();
        mRestClient.setAuthStrategy(mWebContext.getAuthStrategy());
        mRestClient.setHttpParams(getHttpParams());
        switch (mRestContext.getMessageType()) {
            case XML:
                mMapper = mInfinitumContext.getBean("$RestfulXmlMapper", RestfulXmlMapper.class);
                break;
            case JSON:
                mMapper = mInfinitumContext.getBean("$RestfulJsonMapper", RestfulJsonMapper.class);
                break;
            default:
                mMapper = mInfinitumContext.getBean("$RestfulNameValueMapper", RestfulNameValueMapper.class);
        }
        mHost = mRestContext.getRestHost();
        if (!mHost.endsWith("/"))
            mHost += '/';
        mIsOpen = true;
        mLogger.debug("Session opened");
        return this;
    }

    @Override
    public Session close() {
        recycleCache();
        mIsOpen = false;
        mLogger.debug("Session closed");
        return this;
    }

    @Override
    public boolean isOpen() {
        return mIsOpen;
    }

    @Override
    public Session beginTransaction() {
        throw new UnsupportedOperationException("RestfulSession does not support transactions!");
    }

    @Override
    public Session commit() {
        throw new UnsupportedOperationException("RestfulSession does not support transactions!");
    }

    @Override
    public Session rollback() {
        throw new UnsupportedOperationException("RestfulSession does not support transactions!");
    }

    @Override
    public boolean isTransactionOpen() {
        throw new UnsupportedOperationException("RestfulSession does not support transactions!");
    }

    @Override
    public Session setAutocommit(boolean autocommit) {
        throw new UnsupportedOperationException("RestfulSession does not support transactions!");
    }

    @Override
    public boolean isAutocommit() {
        throw new UnsupportedOperationException("RestfulSession does not support transactions!");
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

    @SuppressWarnings("unchecked")
    @Override
    public <T> T load(Class<T> type, Serializable id) throws InfinitumRuntimeException, IllegalArgumentException {
        OrmPreconditions.checkForOpenSession(mIsOpen);
        OrmPreconditions.checkPersistenceForLoading(type, mPersistencePolicy);
        // TODO Validate primary key
        int objHash = mPersistencePolicy.computeModelHash(type, id);
        if (checkCache(objHash))
            return (T) searchCache(objHash);
        return loadEntity(type, id);
    }

    @Override
    public long save(Object model) {
        OrmPreconditions.checkForOpenSession(mIsOpen);
        OrmPreconditions.checkPersistenceForModify(model, mPersistencePolicy);
        mLogger.debug("Sending POST request to save entity");
        String uri = mHost + mPersistencePolicy.getRestEndpoint(model.getClass());
        Map<String, String> headers = new HashMap<String, String>();
        RestfulModelMap modelMap = mMapper.mapModel(model);
        if (mRestContext.getMessageType() == MessageType.JSON)
            headers.put("Content-Type", "application/json");
        else if (mRestContext.getMessageType() == MessageType.XML)
            headers.put("Content-Type", "application/xml");
        RestResponse response = mRestClient.executePost(uri, modelMap.toHttpEntity(), headers);
        if (response == null)
            return -1;
        return response.getStatusCode() < 400 ? 0 : -1;
    }

    @Override
    public boolean delete(Object model) {
        OrmPreconditions.checkForOpenSession(mIsOpen);
        OrmPreconditions.checkPersistenceForModify(model, mPersistencePolicy);
        mLogger.debug("Sending DELETE request to delete entity");
        Serializable pk = mPersistencePolicy.getPrimaryKey(model);
        String uri = mHost + mPersistencePolicy.getRestEndpoint(model.getClass()) + "/" + pk.toString();
        Map<String, String> headers = new HashMap<String, String>();
        RestResponse response = mRestClient.executeDelete(uri, headers);
        if (response == null)
            return false;
        switch (response.getStatusCode()) {
            case HttpStatus.SC_OK:
            case HttpStatus.SC_ACCEPTED:
            case HttpStatus.SC_NO_CONTENT:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean update(Object model) throws InfinitumRuntimeException {
        OrmPreconditions.checkForOpenSession(mIsOpen);
        OrmPreconditions.checkPersistenceForModify(model, mPersistencePolicy);
        mLogger.debug("Sending PUT request to update entity");
        String uri = mHost + mPersistencePolicy.getRestEndpoint(model.getClass());
        Map<String, String> headers = new HashMap<String, String>();
        RestfulModelMap modelMap = mMapper.mapModel(model);
        if (mRestContext.getMessageType() == MessageType.JSON)
            headers.put("Content-Type", "application/json");
        else if (mRestContext.getMessageType() == MessageType.XML)
            headers.put("Content-Type", "application/xml");
        RestResponse response = mRestClient.executePut(uri, modelMap.toHttpEntity(), headers);
        switch (response.getStatusCode()) {
            case HttpStatus.SC_OK:
            case HttpStatus.SC_NO_CONTENT:
                return true;
            default:
                return false;
        }
    }

    /**
     * Makes an HTTP request to the web service to update the given model or
     * save it if it does not exist in the database.
     *
     * @param model the model to save or update
     * @return 0 if the model was updated, 1 if the model was saved, or -1 if
     *         the operation failed
     */
    @Override
    public long saveOrUpdate(Object model) {
        OrmPreconditions.checkForOpenSession(mIsOpen);
        OrmPreconditions.checkPersistenceForModify(model, mPersistencePolicy);
        mLogger.debug("Sending PUT request to save or update entity");
        String uri = mHost + mPersistencePolicy.getRestEndpoint(model.getClass());
        Map<String, String> headers = new HashMap<String, String>();
        RestfulModelMap modelMap = mMapper.mapModel(model);
        if (mRestContext.getMessageType() == MessageType.JSON)
            headers.put("Content-Type", "application/json");
        else if (mRestContext.getMessageType() == MessageType.XML)
            headers.put("Content-Type", "application/xml");
        RestResponse response = mRestClient.executePut(uri, modelMap.toHttpEntity(), headers);
        switch (response.getStatusCode()) {
            case HttpStatus.SC_CREATED:
                return 1;
            case HttpStatus.SC_OK:
            case HttpStatus.SC_NO_CONTENT:
                return 0;
            default:
                return -1;
        }
    }

    @Override
    public int saveOrUpdateAll(Collection<? extends Object> models) throws InfinitumRuntimeException {
        OrmPreconditions.checkForOpenSession(mIsOpen);
        int count = 0;
        for (Object model : models) {
            if (saveOrUpdate(model) >= 0)
                count++;
        }
        return count;
    }

    @Override
    public int saveAll(Collection<? extends Object> models) throws InfinitumRuntimeException {
        OrmPreconditions.checkForOpenSession(mIsOpen);
        int count = 0;
        for (Object model : models) {
            if (save(model) == 0)
                count++;
        }
        return count;
    }

    @Override
    public int deleteAll(Collection<? extends Object> models) throws InfinitumRuntimeException {
        OrmPreconditions.checkForOpenSession(mIsOpen);
        int count = 0;
        for (Object model : models) {
            if (delete(model))
                count++;
        }
        return count;
    }

    @Override
    public Session execute(String sql) throws SQLGrammarException {
        throw new UnsupportedOperationException("RestfulSession does not support SQL operations!");
    }

    @Override
    public <T> Criteria<T> createCriteria(Class<T> entityClass) {
        throw new UnsupportedOperationException("RestfulSession does not support criteria operations!");
    }

    @Override
    public <T> Session registerTypeAdapter(Class<T> type, TypeAdapter<T> adapter) {
        mMapper.registerTypeAdapter(type, adapter);
        return this;
    }

    @Override
    public Map<Class<?>, ? extends TypeAdapter<?>> getRegisteredTypeAdapters() {
        return mMapper.getRegisteredTypeAdapters();
    }

    /**
     * Returns a {@link HttpParams} configured using the
     * {@link InfinitumContext}.
     *
     * @return {@code HttpParams}
     */
    protected HttpParams getHttpParams() {
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, mRestContext.getConnectionTimeout());
        HttpConnectionParams.setSoTimeout(httpParams, mRestContext.getResponseTimeout());
        HttpConnectionParams.setTcpNoDelay(httpParams, true);
        return httpParams;
    }

}
