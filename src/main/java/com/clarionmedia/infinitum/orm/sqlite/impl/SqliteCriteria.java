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
import com.clarionmedia.infinitum.orm.criteria.AssociationCriteria;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.criteria.Order;
import com.clarionmedia.infinitum.orm.criteria.criterion.Criterion;
import com.clarionmedia.infinitum.orm.internal.OrmPreconditions;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.relationship.ModelRelationship;
import com.clarionmedia.infinitum.orm.sql.SqlBuilder;
import com.clarionmedia.infinitum.reflection.ClassReflector;
import com.clarionmedia.infinitum.reflection.impl.JavaClassReflector;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * <p> Implementation of {@link Criteria} for SQLite queries. </p>
 *
 * @author Tyler Treat
 * @version 1.1.0 06/17/13
 * @since 1.0
 */
public class SqliteCriteria<T> implements Criteria<T> {

    private InfinitumOrmContext mOrmContext;
    protected Class<T> mEntityClass;
    protected SqliteSession mSession;
    protected SqliteModelFactory mModelFactory;
    protected List<Criterion> mCriterion;
    protected int mLimit;
    protected int mOffset;
    protected SqlBuilder mSqlBuilder;
    protected PersistencePolicy mPersistencePolicy;
    private List<Order> mOrderings;
    private List<AssociationCriteria<?>> mAssociationCriteria;
    protected SqliteCriteria<?> mParent;

    /**
     * Constructs a new {@code SqliteCriteria}.
     *
     * @param context      the {@link InfinitumOrmContext} this {@code SqliteCriteria} is scoped to
     * @param entityClass  the {@code Class} to create {@code SqliteCriteria} for
     * @param modelFactory {@link SqliteModelFactory} for generating models
     * @param sqlBuilder   {@link SqlBuilder} for generating SQL statements
     * @param parent       the parent of this {@code Criteria}
     * @throws InfinitumRuntimeException if {@code entityClass} is transient
     */
    public SqliteCriteria(InfinitumOrmContext context, Class<T> entityClass, SqliteModelFactory modelFactory,
                          SqlBuilder sqlBuilder, SqliteCriteria<?> parent) throws InfinitumRuntimeException {
        OrmPreconditions.checkPersistenceForLoading(entityClass, context.getPersistencePolicy());
        mOrmContext = context;
        mSession = (SqliteSession) context.getSession(SessionType.SQLITE);
        mEntityClass = entityClass;
        mModelFactory = modelFactory;
        mCriterion = new ArrayList<Criterion>();
        mSqlBuilder = sqlBuilder;
        mPersistencePolicy = context.getPersistencePolicy();
        mOrderings = new ArrayList<Order>(5);
        mAssociationCriteria = new ArrayList<AssociationCriteria<?>>(3);
        mParent = parent;
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

    @Override
    public AssociationCriteria<?> createCriteria(String association) {
        AssociationCriteria<?> associationCriteria = getAssociationCriteria(association);
        mAssociationCriteria.add(associationCriteria);
        return associationCriteria;
    }

    @Override
    public List<AssociationCriteria<?>> getAssociationCriteria() {
        return mAssociationCriteria;
    }

    private AssociationCriteria<?> getAssociationCriteria(String association) {
        ClassReflector classReflector = new JavaClassReflector();
        Field associationField = classReflector.getField(mEntityClass, association);
        if (associationField == null) {
            throw new InfinitumRuntimeException("No relationship field '" + association + "' in type " + mEntityClass
                    .getName());
        }

        ModelRelationship relationship = mPersistencePolicy.getRelationship(associationField);
        Class associationType;
        if (relationship.getFirstType() == mEntityClass) {
            associationType = relationship.getSecondType();
        } else {
            associationType = relationship.getFirstType();
        }

        return new SqliteAssociationCriteria(mOrmContext,
                associationType, mModelFactory, mSqlBuilder, relationship, associationField, this);
    }

}
