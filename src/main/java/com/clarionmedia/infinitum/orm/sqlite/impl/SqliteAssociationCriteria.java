package com.clarionmedia.infinitum.orm.sqlite.impl;

import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.context.InfinitumOrmContext;
import com.clarionmedia.infinitum.orm.criteria.AssociationCriteria;
import com.clarionmedia.infinitum.orm.relationship.ModelRelationship;
import com.clarionmedia.infinitum.orm.sql.SqlBuilder;

import java.lang.reflect.Field;

public class SqliteAssociationCriteria<T> extends SqliteCriteria<T> implements AssociationCriteria<T> {

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
    public SqliteAssociationCriteria(InfinitumOrmContext context, Class<T> entityClass,
                                     SqliteModelFactory modelFactory, SqlBuilder sqlBuilder,
                                     ModelRelationship relationship, Field relationshipField) throws
            InfinitumRuntimeException {
        super(context, entityClass, modelFactory, sqlBuilder);
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

}
