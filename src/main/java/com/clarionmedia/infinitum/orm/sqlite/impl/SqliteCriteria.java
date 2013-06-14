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

import android.database.Cursor;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.Session;
import com.clarionmedia.infinitum.orm.context.InfinitumOrmContext;
import com.clarionmedia.infinitum.orm.context.InfinitumOrmContext.SessionType;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.criteria.Order;
import com.clarionmedia.infinitum.orm.criteria.criterion.Criterion;
import com.clarionmedia.infinitum.orm.internal.OrmPreconditions;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.sql.SqlBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * <p> Implementation of {@link Criteria} for SQLite queries. </p>
 *
 * @author Tyler Treat
 * @version 1.1.0 06/13/13
 * @since 1.0
 */
public class SqliteCriteria<T> implements Criteria<T> {

    private Class<T> mEntityClass;
    private SqliteSession mSession;
    private SqliteModelFactory mModelFactory;
    private List<Criterion> mCriterion;
    private int mLimit;
    private int mOffset;
    private SqlBuilder mSqlBuilder;
    private PersistencePolicy mPersistencePolicy;
    private List<Order> mOrderings;

    /**
     * Constructs a new {@code SqliteCriteria}.
     *
     * @param context     the {@link InfinitumOrmContext} this {@code SqliteCriteria} is scoped to
     * @param entityClass the {@code Class} to create {@code SqliteCriteria} for
     * @param sqlBuilder  {@link SqlBuilder} for generating SQL statements
     * @throws InfinitumRuntimeException if {@code entityClass} is transient
     */
    public SqliteCriteria(InfinitumOrmContext context, Class<T> entityClass, SqliteModelFactory modelFactory,
                          SqlBuilder sqlBuilder) throws InfinitumRuntimeException {
        OrmPreconditions.checkPersistenceForLoading(entityClass, context.getPersistencePolicy());
        mSession = (SqliteSession) context.getSession(SessionType.SQLITE);
        mEntityClass = entityClass;
        mModelFactory = modelFactory;
        mCriterion = new ArrayList<Criterion>();
        mSqlBuilder = sqlBuilder;
        mPersistencePolicy = context.getPersistencePolicy();
        mOrderings = new ArrayList<Order>(5);
    }

    @Override
    public String getRepresentation() {
        return mSqlBuilder.createQuery(this);
    }

    @Override
    public Class<T> getEntityClass() {
        return mEntityClass;
    }

    @Override
    public List<Criterion> getCriterion() {
        return mCriterion;
    }

    @Override
    public int getLimit() {
        return mLimit;
    }

    @Override
    public int getOffset() {
        return mOffset;
    }

    @Override
    public Criteria<T> add(Criterion criterion) {
        mCriterion.add(criterion);
        return this;
    }

    @Override
    public Criteria<T> limit(int limit) {
        mLimit = limit;
        return this;
    }

    @Override
    public Criteria<T> offset(int offset) {
        mOffset = offset;
        return this;
    }

    @Override
    public List<T> list() {
        Cursor result = mSession.executeForResult(getRepresentation());
        List<T> ret = new ArrayList<T>(result.getCount());
        if (result.getCount() == 0) {
            result.close();
            return ret;
        }
        try {
            while (result.moveToNext()) {
                T entity = mModelFactory.createFromCursor(result, mEntityClass);
                ret.add(entity);
                // Cache results
                mSession.cache(mPersistencePolicy.computeModelHash(entity), entity);
            }
            return ret;
        } finally {
            result.close();
        }
    }

    @Override
    public T unique() throws InfinitumRuntimeException {
        Cursor result = mSession.executeForResult(getRepresentation());
        if (result.getCount() > 1) {
            throw new InfinitumRuntimeException(String.format("Criteria query for '%s' specified unique result but " +
                    "there were %d results.",
                    mEntityClass.getName(), result.getCount()));
        } else if (result.getCount() == 0) {
            result.close();
            return null;
        }
        result.moveToFirst();
        try {
            T ret = mModelFactory.createFromCursor(result, mEntityClass);
            // Cache result
            mSession.cache(mPersistencePolicy.computeModelHash(ret), ret);
            return ret;
        } finally {
            result.close();
        }
    }

    @Override
    public SqliteMapper getObjectMapper() {
        return mSession.getSqliteMapper();
    }

    @Override
    public long count() {
        Cursor result = mSession.executeForResult(mSqlBuilder.createCountQuery(this));
        result.moveToFirst();
        long ret = result.getLong(0);
        result.close();
        return ret;
    }

    @Override
    public Cursor cursor() {
        return mSession.executeForResult(getRepresentation());
    }

    @Override
    public Session getSession() {
        return mSession;
    }

    @Override
    public Criteria<T> orderBy(Order order) {
        mOrderings.add(order);
        return this;
    }

    @Override
    public List<Order> getOrderings() {
        return mOrderings;
    }

}
