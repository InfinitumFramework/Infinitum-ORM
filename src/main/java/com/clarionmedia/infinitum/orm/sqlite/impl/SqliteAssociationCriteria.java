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
import com.clarionmedia.infinitum.orm.context.InfinitumOrmContext;
import com.clarionmedia.infinitum.orm.criteria.AssociationCriteria;
import com.clarionmedia.infinitum.orm.criteria.criterion.Criterion;
import com.clarionmedia.infinitum.orm.relationship.ModelRelationship;
import com.clarionmedia.infinitum.orm.sql.SqlBuilder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Implementation of {@link AssociationCriteria} for SQLite queries.</p>
 *
 * @author Tyler Treat
 * @version 1.1.0 06/17/13
 * @since 1.0
 */
public class SqliteAssociationCriteria extends SqliteCriteria<Object> implements AssociationCriteria<Object> {

    private ModelRelationship mRelationship;
    private Field mRelationshipField;

    /**
     * Constructs a new {@code SqliteAssociationCriteria}.
     *
     * @param context           the {@link InfinitumOrmContext} this {@code SqliteCriteria} is scoped to
     * @param entityClass       the {@code Class} to create {@code SqliteCriteria} for
     * @param modelFactory      {@link SqliteModelFactory} for generating models
     * @param sqlBuilder        {@link SqlBuilder} for generating SQL statements
     * @param relationship      the {@link ModelRelationship} being queried on
     * @param relationshipField the {@link Field} representing the association
     * @param parent            the parent of this {@code Criteria}
     * @throws com.clarionmedia.infinitum.exception.InfinitumRuntimeException
     *          if {@code entityClass} is transient
     */
    public SqliteAssociationCriteria(InfinitumOrmContext context, Class<Object> entityClass,
                                     SqliteModelFactory modelFactory, SqlBuilder sqlBuilder,
                                     ModelRelationship relationship, Field relationshipField,
                                     SqliteCriteria<?> parent) throws
            InfinitumRuntimeException {
        super(context, entityClass, modelFactory, sqlBuilder, parent);
        mRelationship = relationship;
        mRelationshipField = relationshipField;
    }

    @Override
    public ModelRelationship getRelationship() {
        return mRelationship;
    }

    @Override
    public Field getRelationshipField() {
        return mRelationshipField;
    }

    @Override
    public <E> List<E> list(Class<E> type) {
        return (List<E>) list();
    }

    @Override
    public List<Object> list() {
        SqliteCriteria<?> criteria = getRootCriteria();

        Cursor result = criteria.mSession.executeForResult(criteria.getRepresentation());
        List<Object> ret = new ArrayList<Object>(result.getCount());
        if (result.getCount() == 0) {
            result.close();
            return ret;
        }
        try {
            while (result.moveToNext()) {
                Object entity = criteria.mModelFactory.createFromCursor(result, criteria.mEntityClass);
                ret.add(entity);
                // Cache results
                criteria.mSession.cache(criteria.mPersistencePolicy.computeModelHash(entity), entity);
            }
            return ret;
        } finally {
            result.close();
        }
    }

    @Override
    public <E> E unique(Class<E> type) {
        return (E) unique();
    }

    @Override
    public Object unique() throws InfinitumRuntimeException {
        SqliteCriteria<?> criteria = getRootCriteria();

        Cursor result = criteria.mSession.executeForResult(criteria.getRepresentation());
        if (result.getCount() > 1) {
            throw new InfinitumRuntimeException(String.format("Criteria query for '%s' specified unique result but " +
                    "there were %d results.",
                    criteria.mEntityClass.getName(), result.getCount()));
        } else if (result.getCount() == 0) {
            result.close();
            return null;
        }
        result.moveToFirst();
        try {
            Object ret = criteria.mModelFactory.createFromCursor(result, criteria.mEntityClass);
            // Cache result
            criteria.mSession.cache(criteria.mPersistencePolicy.computeModelHash(ret), ret);
            return ret;
        } finally {
            result.close();
        }
    }

    @Override
    public AssociationCriteria<Object> add(Criterion criterion) {
        mCriterion.add(criterion);
        return this;
    }

    @Override
    public AssociationCriteria<Object> limit(int limit) {
        mLimit = limit;
        return this;
    }

    @Override
    public AssociationCriteria<Object> offset(int offset) {
        mOffset = offset;
        return this;
    }

    @Override
    public long count() {
        SqliteCriteria<?> criteria = getRootCriteria();

        Cursor result = criteria.mSession.executeForResult(criteria.mSqlBuilder.createCountQuery(criteria));
        result.moveToFirst();
        long ret = result.getLong(0);
        result.close();
        return ret;
    }

    @Override
    public Cursor cursor() {
        SqliteCriteria<?> criteria = getRootCriteria();
        return criteria.mSession.executeForResult(criteria.getRepresentation());
    }

    private SqliteCriteria<?> getRootCriteria() {
        SqliteCriteria criteria = this;
        while (criteria.mParent != null) {
            criteria = criteria.mParent;
        }
        return criteria;
    }

}
