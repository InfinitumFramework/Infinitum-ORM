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

public class SqliteAssociationCriteria extends SqliteCriteria<Object> implements AssociationCriteria<Object> {

    private ModelRelationship mRelationship;
    private Field mRelationshipField;

    /**
     * Constructs a new {@code SqliteAssociationCriteria}.
     *
     * @param context      the {@link com.clarionmedia.infinitum.orm.context.InfinitumOrmContext} this {@code
     *                     SqliteCriteria} is scoped to
     * @param entityClass  the {@code Class} to create {@code SqliteCriteria} for
     * @param sqlBuilder   {@link com.clarionmedia.infinitum.orm.sql.SqlBuilder} for generating SQL statements
     * @param relationship the {@link ModelRelationship} being queried on
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
        SqliteCriteria criteria = this;
        while (criteria.mParent != null) {
            criteria = criteria.mParent;
        }

        Cursor result = criteria.mSession.executeForResult(criteria.getRepresentation());
        List ret = new ArrayList(result.getCount());
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

}
