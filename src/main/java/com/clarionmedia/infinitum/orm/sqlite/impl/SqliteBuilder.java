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

import android.database.sqlite.SQLiteDatabase;
import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;
import com.clarionmedia.infinitum.di.annotation.Autowired;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.context.InfinitumOrmContext;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.criteria.Order;
import com.clarionmedia.infinitum.orm.criteria.criterion.Criterion;
import com.clarionmedia.infinitum.orm.exception.InvalidCriteriaException;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.TypeResolutionPolicy.SqliteDataType;
import com.clarionmedia.infinitum.orm.relationship.ManyToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToOneRelationship;
import com.clarionmedia.infinitum.orm.sql.SqlBuilder;
import com.clarionmedia.infinitum.orm.sql.SqlConstants;
import com.clarionmedia.infinitum.reflection.ClassReflector;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

/**
 * <p> Implementation of {@link SqlBuilder} for interacting with a SQLite database. </p>
 *
 * @author Tyler Treat
 * @version 1.1.0 06/15/13
 * @since 1.0
 */
public class SqliteBuilder implements SqlBuilder {

    // TODO: this class currently doesn't handle reserved keywords.
    // See: http://www.sqlite.org/lang_keywords.html

    @Autowired
    private SqliteMapper mMapper;

    @Autowired
    private PersistencePolicy mPersistencePolicy;

    @Autowired
    private ClassReflector mClassReflector;

    @Autowired
    private InfinitumOrmContext mContext;

    @Override
    public int createTables(SqliteDbHelper dbHelper) throws ModelConfigurationException,
            InfinitumConfigurationException {
        int count = 0;
        SQLiteDatabase db = dbHelper.getDatabase();
        for (String m : mContext.getDomainTypes()) {
            Class<?> c = mClassReflector.getClass(m);
            if (c == null)
                throw new InfinitumConfigurationException("No such class '" + m + "'.");
            String sql = createModelTableString(c);
            if (sql != null) {
                db.execSQL(sql);
                count++;
            }
            // Sort of hackish...this loads the M:M rels into cache so we can
            // access them below
            // TODO Consider revisiting this
            mPersistencePolicy.getManyToManyRelationships(c);
        }
        for (ManyToManyRelationship r : mPersistencePolicy.getManyToManyCache().values()) {
            String sql = createManyToManyTableString(r);
            if (sql != null) {
                db.execSQL(sql);
                count++;
            }
        }
        return count;
    }

    @Override
    public int dropTables(SqliteDbHelper dbHelper) {
        int count = 0;
        SQLiteDatabase db = dbHelper.getDatabase();
        for (String m : mContext.getDomainTypes()) {
            Class<?> c = mClassReflector.getClass(m);
            if (c == null)
                throw new InfinitumConfigurationException("No such class '" + m + "'.");
            String sql = dropModelTableString(c);
            if (sql != null) {
                db.execSQL(sql);
                count++;
            }
        }
        return count;
    }

    @Override
    public String createQuery(Criteria<?> criteria) {
        Class<?> c = criteria.getEntityClass();
        StringBuilder query = new StringBuilder(SqlConstants.SELECT_ALL_FROM).append(mPersistencePolicy
                .getModelTableName(c));
        String prefix = " WHERE ";

        // Append Criterion expressions
        for (Criterion criterion : criteria.getCriterion()) {
            query.append(prefix);
            prefix = ' ' + SqlConstants.AND + ' ';
            query.append(criterion.toSql(criteria));
        }

        // Append order by expressions
        if (criteria.getOrderings().size() > 0) {
            query.append(' ').append(SqlConstants.ORDER_BY).append(' ');
            String separator = "";
            for (Order ordering : criteria.getOrderings()) {
                query.append(separator);
                separator = ", ";
                Field field = mPersistencePolicy.findPersistentField(c, ordering.getProperty());
                if (field == null)
                    throw new InvalidCriteriaException(String.format("Invalid Criteria for type '%s'.", c.getName()));
                String column = mPersistencePolicy.getFieldColumnName(field);
                query.append(column).append(' ');
                if (ordering.isIgnoreCase()) {
                    query.append(SqlConstants.COLLATE_NOCASE).append(' ');
                }
                query.append(ordering.getOrdering().name());
            }
        }

        // Append limit and offset expressions
        int limit = criteria.getLimit();
        if (limit > 0)
            query.append(' ').append(SqlConstants.LIMIT).append(' ').append(limit);
        if (criteria.getOffset() > 0) {
            if (limit == 0)
                query.append(' ').append(SqlConstants.LIMIT).append(' ').append(Integer.MAX_VALUE);
            query.append(' ').append(SqlConstants.OFFSET).append(' ').append(criteria.getOffset());
        }
        return query.toString();
    }

    @Override
    public String createCountQuery(Criteria<?> criteria) {
        Class<?> c = criteria.getEntityClass();
        StringBuilder query = new StringBuilder(SqlConstants.SELECT_COUNT_FROM).append(mPersistencePolicy
                .getModelTableName(c));
        String prefix = " WHERE ";
        for (Criterion criterion : criteria.getCriterion()) {
            query.append(prefix);
            prefix = ' ' + SqlConstants.AND + ' ';
            query.append(criterion.toSql(criteria));
        }
        int limit = criteria.getLimit();
        if (limit > 0)
            query.append(' ').append(SqlConstants.LIMIT).append(' ').append(limit);
        if (criteria.getOffset() > 0) {
            if (limit == 0)
                query.append(' ').append(SqlConstants.LIMIT).append(' ').append(Integer.MAX_VALUE);
            query.append(' ').append(SqlConstants.OFFSET).append(' ').append(criteria.getOffset());
        }
        return query.toString();
    }

    @Override
    public String createManyToManyJoinQuery(ManyToManyRelationship rel, Serializable id, Class<?> direction)
            throws InfinitumRuntimeException {
        if (!rel.contains(direction))
            throw new InfinitumRuntimeException(String.format("'%s' is not a valid direction for relationship " +
                    "'%s'<=>'%s'.",
                    direction.getName(), rel.getFirstType().getName(), rel.getSecondType().getName()));
        StringBuilder query = new StringBuilder(String.format(SqlConstants.ALIASED_SELECT_ALL_FROM, 'x')).append(
                mPersistencePolicy.getModelTableName(rel.getFirstType())).append(' ');
        if (direction == rel.getFirstType())
            query.append("x, ");
        else
            query.append("y, ");
        query.append(mPersistencePolicy.getModelTableName(rel.getSecondType())).append(' ');
        if (direction == rel.getSecondType() && rel.getFirstType() != rel.getSecondType())
            query.append("x, ");
        else
            query.append("y, ");
        query.append(rel.getTableName()).append(" z ").append(SqlConstants.WHERE).append(' ').append("z.");
        if (direction == rel.getFirstType()) {
            query.append(mPersistencePolicy.getModelTableName(rel.getFirstType())).append('_')
                    .append(mPersistencePolicy.getFieldColumnName(rel.getFirstField())).append("_1").append(" = ")
                    .append("x.")
                    .append(mPersistencePolicy.getFieldColumnName(rel.getFirstField())).append(' ').append
                    (SqlConstants.AND).append(" z.")
                    .append(mPersistencePolicy.getModelTableName(rel.getSecondType())).append('_')
                    .append(mPersistencePolicy.getFieldColumnName(rel.getSecondField())).append("_2").append(" = ")
                    .append("y.")
                    .append(mPersistencePolicy.getFieldColumnName(rel.getSecondField())).append(' ').append
                    (SqlConstants.AND).append(" y.")
                    .append(mPersistencePolicy.getFieldColumnName(rel.getSecondField())).append(" = ");
        } else {
            query.append(mPersistencePolicy.getModelTableName(rel.getSecondType())).append('_')
                    .append(mPersistencePolicy.getFieldColumnName(rel.getSecondField())).append("_2").append(" = ")
                    .append("x.")
                    .append(mPersistencePolicy.getFieldColumnName(rel.getSecondField())).append(' ').append
                    (SqlConstants.AND).append(" z.")
                    .append(mPersistencePolicy.getModelTableName(rel.getFirstType())).append('_')
                    .append(mPersistencePolicy.getFieldColumnName(rel.getFirstField())).append("_1").append(" = ")
                    .append("y.")
                    .append(mPersistencePolicy.getFieldColumnName(rel.getFirstField())).append(' ').append
                    (SqlConstants.AND).append(" y.")
                    .append(mPersistencePolicy.getFieldColumnName(rel.getFirstField())).append(" = ");
        }
        switch (mMapper.getSqliteDataType(id)) {
            case TEXT:
                query.append("'").append(id).append("'");
                break;
            default:
                query.append(id);
        }
        return query.toString();
    }

    @Override
    public String createDeleteStaleRelationshipQuery(ManyToManyRelationship rel, Object model,
                                                     List<Serializable> relatedKeys) {
        Serializable pk = mPersistencePolicy.getPrimaryKey(model);
        StringBuilder ret = new StringBuilder(SqlConstants.DELETE_FROM).append(rel.getTableName()).append(' ').append
                (SqlConstants.WHERE)
                .append(' ');
        Field col;
        if (model.getClass() == rel.getFirstType()) {
            ret.append(mPersistencePolicy.getModelTableName(rel.getFirstType())).append('_').append
                    (mPersistencePolicy.getFieldColumnName(rel.getFirstField())).append("_1").append(" = ");
            col = rel.getFirstField();
        } else {
            ret.append(mPersistencePolicy.getModelTableName(rel.getSecondType())).append('_').append
                    (mPersistencePolicy.getFieldColumnName(rel.getSecondField())).append("_2").append(" = ");
            col = rel.getSecondField();
        }
        switch (mMapper.getSqliteDataType(col)) {
            case TEXT:
                ret.append("'").append(pk).append("'");
                break;
            default:
                ret.append(pk);
        }
        ret.append(' ').append(SqlConstants.AND).append(' ');
        if (model.getClass() == rel.getFirstType())
            ret.append(mPersistencePolicy.getModelTableName(rel.getSecondType())).append('_').append
                    (mPersistencePolicy.getFieldColumnName(rel.getSecondField())).append("_2");
        else
            ret.append(mPersistencePolicy.getModelTableName(rel.getFirstType())).append('_').append
                    (mPersistencePolicy.getFieldColumnName(rel.getFirstField())).append("_1");
        ret.append(' ').append(SqlConstants.NOT_IN).append(" (");
        String prefix = "";
        for (Serializable key : relatedKeys) {
            ret.append(prefix);
            switch (mMapper.getSqliteDataType(key)) {
                case TEXT:
                    ret.append("'").append(key).append("'");
                    break;
                default:
                    ret.append(key);
            }
            prefix = ", ";
        }
        return ret.append(")").toString();
    }

    @Override
    public String createUpdateForeignKeyQuery(OneToManyRelationship rel, Object model, List<Serializable> relatedKeys) {
        StringBuilder ret = new StringBuilder(SqlConstants.UPDATE).append(' ')
                .append(mPersistencePolicy.getModelTableName(rel.getManyType())).append(' ').append(SqlConstants.SET)
                .append(' ')
                .append(rel.getColumn()).append(" = ");
        Field pkField = mPersistencePolicy.getPrimaryKeyField(model.getClass());
        pkField.setAccessible(true);
        Serializable pk = mPersistencePolicy.getPrimaryKey(model);
        switch (mMapper.getSqliteDataType(pkField)) {
            case TEXT:
                ret.append("'").append(pk).append("'");
                break;
            default:
                ret.append(pk);
        }
        ret.append(' ').append(SqlConstants.WHERE).append(' ');
        pkField = mPersistencePolicy.getPrimaryKeyField(rel.getManyType());
        ret.append(mPersistencePolicy.getFieldColumnName(pkField)).append(' ').append(SqlConstants.IN).append(" (");
        String prefix = "";
        for (Serializable key : relatedKeys) {
            ret.append(prefix);
            switch (mMapper.getSqliteDataType(key)) {
                case TEXT:
                    ret.append("'").append(key).append("'");
                    break;
                default:
                    ret.append(key);
            }
            prefix = ", ";
        }
        return ret.append(")").toString();
    }

    @Override
    public String createUpdateOneToOneForeignKeyQuery(OneToOneRelationship relationship, Object model, Object related) {
        StringBuilder sb = new StringBuilder(SqlConstants.UPDATE).append(' ')
                .append(mPersistencePolicy.getModelTableName(model.getClass())).append(' ').append(SqlConstants.SET)
                .append(' ')
                .append(relationship.getColumn()).append(" = ");
        Field pkField = mPersistencePolicy.getPrimaryKeyField(related.getClass());
        pkField.setAccessible(true);
        Serializable pk = mPersistencePolicy.getPrimaryKey(related);
        switch (mMapper.getSqliteDataType(pkField)) {
            case TEXT:
                sb.append("'").append(pk).append("'");
                break;
            default:
                sb.append(pk);
        }
        sb.append(' ').append(SqlConstants.WHERE).append(' ');
        pkField = mPersistencePolicy.getPrimaryKeyField(model.getClass());
        sb.append(mPersistencePolicy.getFieldColumnName(pkField)).append(" = ");
        pk = mPersistencePolicy.getPrimaryKey(model);
        switch (mMapper.getSqliteDataType(pkField)) {
            case TEXT:
                sb.append("'").append(pk).append("'");
                break;
            default:
                sb.append(pk);
        }
        return sb.toString();
    }

    @Override
    public String createManyToManyDeleteQuery(Object obj, ManyToManyRelationship rel) {
        StringBuilder query = new StringBuilder(String.format(SqlConstants.DELETE_FROM_WHERE, rel.getTableName()));
        if (obj.getClass() == rel.getFirstType())
            query.append(mPersistencePolicy.getModelTableName(rel.getFirstType())).append('_').append
                    (mPersistencePolicy.getFieldColumnName(rel.getFirstField())).append("_1");
        else
            query.append(mPersistencePolicy.getModelTableName(rel.getSecondType())).append('_').append
                    (mPersistencePolicy.getFieldColumnName(rel.getSecondField())).append("_2");
        query.append(" = ");
        Field pkField = mPersistencePolicy.getPrimaryKeyField(obj.getClass());
        pkField.setAccessible(true);
        Serializable pk = mPersistencePolicy.getPrimaryKey(obj);
        switch (mMapper.getSqliteDataType(pkField)) {
            case TEXT:
                query.append("'").append(pk).append("'");
                break;
            default:
                query.append(pk);
        }
        return query.toString();
    }

    @Override
    public String createUpdateQuery(Object model, Object related, String column) {
        Serializable pk = mPersistencePolicy.getPrimaryKey(related);
        StringBuilder update = new StringBuilder(SqlConstants.UPDATE).append(" ").append(
                mPersistencePolicy.getModelTableName(model.getClass()));
        update.append(" ").append(SqlConstants.SET).append(" ").append(column).append(" = ");
        switch (mMapper.getSqliteDataType(mPersistencePolicy.getPrimaryKeyField(related.getClass()))) {
            case TEXT:
                update.append("'").append(pk).append("'");
                break;
            default:
                update.append(pk);
        }
        update.append(" ").append(SqlConstants.WHERE).append(" ")
                .append(mPersistencePolicy.getFieldColumnName(mPersistencePolicy.getPrimaryKeyField(model.getClass())
                )).append(" = ");
        pk = mPersistencePolicy.getPrimaryKey(model);
        switch (mMapper.getSqliteDataType(mPersistencePolicy.getPrimaryKeyField(model.getClass()))) {
            case TEXT:
                update.append("'").append(pk).append("'");
                break;
            default:
                update.append(pk);
        }
        return update.toString();
    }

    private String createManyToManyTableString(ManyToManyRelationship rel) throws ModelConfigurationException {
        if (!mPersistencePolicy.isPersistent(rel.getFirstType()) || !mPersistencePolicy.isPersistent(rel
                .getSecondType()))
            return null;
        StringBuilder sb = new StringBuilder(SqlConstants.CREATE_TABLE).append(' ').append(rel.getTableName()).append
                (" (");
        Field first = rel.getFirstField();
        if (first == null)
            throw new ModelConfigurationException(String.format(
                    "Could not create many-to-many relationship between '%s' and '%s'. Are the specified columns " +
                            "correct?", rel
                    .getFirstType().getName(), rel.getSecondType().getName()));
        Field second = rel.getSecondField();
        if (second == null)
            throw new ModelConfigurationException(String.format(
                    "Could not create many-to-many relationship between '%s' and '%s'. Are the specified columns " +
                            "correct?", rel
                    .getFirstType().getName(), rel.getSecondType().getName()));
        String firstCol = mPersistencePolicy.getModelTableName(rel.getFirstType()) + '_' + mPersistencePolicy
                .getFieldColumnName(first) + "_1";
        String secondCol = mPersistencePolicy.getModelTableName(rel.getSecondType()) + '_' + mPersistencePolicy
                .getFieldColumnName(second) + "_2";
        sb.append(firstCol).append(' ').append(mMapper.getSqliteDataType(first).toString()).append(' ').append(", " +
                "").append(secondCol)
                .append(' ').append(mMapper.getSqliteDataType(second).toString()).append(", " +
                "").append(SqlConstants.PRIMARY_KEY).append('(')
                .append(firstCol).append(", ").append(secondCol).append("))");
        return sb.toString();
    }

    private String createModelTableString(Class<?> c) throws ModelConfigurationException {
        if (!mPersistencePolicy.isPersistent(c))
            return null;
        StringBuilder sb = new StringBuilder(SqlConstants.CREATE_TABLE).append(' ').append(mPersistencePolicy
                .getModelTableName(c))
                .append(" (");
        appendColumns(c, sb);
        appendUniqueConstraints(c, sb);
        sb.append(')');
        return sb.toString();
    }

    private String dropModelTableString(Class<?> c) throws ModelConfigurationException {
        if (!mPersistencePolicy.isPersistent(c))
            return null;
        return SqlConstants.DROP_TABLE + ' ' + mPersistencePolicy.getModelTableName(c);
    }

    private void appendColumns(Class<?> c, StringBuilder sb) throws ModelConfigurationException {
        List<Field> fields = mPersistencePolicy.getPersistentFields(c);

        // Throw a runtime exception if there are no persistent fields
        if (fields.size() == 0)
            throw new ModelConfigurationException(String.format("No persistent fields declared in '%s'.", c.getName()));

        String prefix = "";
        for (Field f : fields) {
            // M:M relationships are stored in a join table
            if (mPersistencePolicy.isManyToManyRelationship(f))
                continue;
            if (mPersistencePolicy.isOneToOneRelationship(f)) {
                OneToOneRelationship oto = new OneToOneRelationship(f);
                // The owner contains the FK
                if (oto.getOwner() != c)
                    continue;
            }
            SqliteDataType type = mMapper.getSqliteDataType(f);
            if (type == null)
                continue;
            sb.append(prefix);
            prefix = ", ";

            // Append column name and data type, e.g. "foo INTEGER"
            sb.append(mPersistencePolicy.getFieldColumnName(f)).append(' ').append(type.toString());

            // Check if the column is a PRIMARY KEY
            if (mPersistencePolicy.isFieldPrimaryKey(f)) {
                sb.append(" ").append(SqlConstants.PRIMARY_KEY);
                if (mPersistencePolicy.isPrimaryKeyAutoIncrement(f))
                    sb.append(" ").append(SqlConstants.AUTO_INCREMENT);
            }

            // Check if the column is NOT NULL
            if (!mPersistencePolicy.isFieldNullable(f))
                sb.append(" ").append(SqlConstants.NOT_NULL);
        }
    }

    private void appendUniqueConstraints(Class<?> c, StringBuilder sb) {
        List<Field> fields = mPersistencePolicy.getUniqueFields(c);

        // Append any unique constraints, e.g. UNIQUE(foo, bar)
        if (fields.size() > 0) {
            sb.append(", ").append(SqlConstants.UNIQUE).append('(');
            String prefix = "";
            for (Field f : fields) {
                sb.append(prefix);
                prefix = ", ";
                sb.append(mPersistencePolicy.getFieldColumnName(f));
            }
            sb.append(')');
        }
    }

}
