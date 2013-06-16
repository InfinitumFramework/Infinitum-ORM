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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import com.clarionmedia.infinitum.di.AbstractProxy;
import com.clarionmedia.infinitum.di.annotation.Autowired;
import com.clarionmedia.infinitum.di.annotation.PostConstruct;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.internal.Pair;
import com.clarionmedia.infinitum.internal.Primitives;
import com.clarionmedia.infinitum.logging.Logger;
import com.clarionmedia.infinitum.logging.impl.SmartLogger;
import com.clarionmedia.infinitum.orm.context.InfinitumOrmContext;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.exception.SQLGrammarException;
import com.clarionmedia.infinitum.orm.internal.OrmPreconditions;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy.Cascade;
import com.clarionmedia.infinitum.orm.persistence.TypeResolutionPolicy;
import com.clarionmedia.infinitum.orm.relationship.ManyToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.ManyToOneRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToOneRelationship;
import com.clarionmedia.infinitum.orm.sql.SqlBuilder;
import com.clarionmedia.infinitum.orm.sqlite.SqliteOperations;
import com.clarionmedia.infinitum.orm.sqlite.SqliteTypeAdapter;
import com.clarionmedia.infinitum.orm.sqlite.SqliteUtils;
import com.clarionmedia.infinitum.reflection.ClassReflector;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

/**
 * <p> An implementation of {@link SqliteOperations}. This class is designed to provide implementations of core CRUD
 * operations for interacting with a SQLite database and act as a factory for constructing {@link Criteria} and {@link
 * Criteria} queries. </p>
 *
 * @author Tyler Treat
 * @version 1.1.0 06/15/13
 * @since 1.0
 */
public class SqliteTemplate implements SqliteOperations {

    @Autowired
    protected InfinitumOrmContext mInfinitumContext;

    @Autowired
    protected SqliteMapper mMapper;

    @Autowired
    protected SqliteModelFactory mModelFactory;

    @Autowired
    protected SqlBuilder mSqlBuilder;

    @Autowired
    protected PersistencePolicy mPersistencePolicy;

    @Autowired
    protected TypeResolutionPolicy mTypePolicy;

    @Autowired
    protected SqliteUtils mSqliteUtil;

    @Autowired
    protected ClassReflector mClassReflector;

    protected SqliteDbHelper mDbHelper;
    protected boolean mIsAutocommit;
    protected boolean mIsOpen;
    protected Stack<Boolean> mTransactionStack;
    protected SQLiteDatabase mSqliteDb;
    protected Logger mLogger;

    @PostConstruct
    private void init() {
        mLogger = new SmartLogger(getClass().getSimpleName());
        mTransactionStack = new Stack<Boolean>();
        mDbHelper = SqliteDbHelper.getInstance(mInfinitumContext, mSqlBuilder);
    }

    @Override
    public <T> Criteria<T> createCriteria(Class<T> entityClass) {
        return new SqliteCriteria<T>(mInfinitumContext, entityClass, mModelFactory, mSqlBuilder);
    }

    @Override
    public synchronized void open() throws SQLException {
        if (mIsOpen)
            return;
        mSqliteDb = mDbHelper.getWritableDatabase();
        mIsOpen = true;
    }

    @Override
    public synchronized void close() {
        if (!mIsOpen)
            return;
        mDbHelper.close();
        mIsOpen = false;
    }

    @Override
    public synchronized boolean isOpen() {
        return mIsOpen;
    }

    @Override
    public void beginTransaction() {
        if (mIsAutocommit)
            return;
        mSqliteDb.beginTransaction();
        mTransactionStack.push(true);
        mLogger.debug("Transaction started");
    }

    @Override
    public void commit() {
        if (!isTransactionOpen())
            return;
        mSqliteDb.setTransactionSuccessful();
        mSqliteDb.endTransaction();
        mTransactionStack.pop();
        mLogger.debug("Transaction committed");
    }

    @Override
    public void rollback() {
        if (!isTransactionOpen())
            return;
        mSqliteDb.endTransaction();
        mTransactionStack.pop();
        mLogger.debug("Transaction rolled back");
    }

    @Override
    public boolean isTransactionOpen() {
        return mTransactionStack.size() > 0;
    }

    @Override
    public void setAutocommit(boolean autocommit) {
        mIsAutocommit = autocommit;
    }

    @Override
    public boolean isAutocommit() {
        return mIsAutocommit;
    }

    @Override
    public long save(Object model) throws InfinitumRuntimeException {
        OrmPreconditions.checkForTransaction(mIsAutocommit, isTransactionOpen());
        OrmPreconditions.checkPersistenceForModify(model, mPersistencePolicy);
        Map<Integer, Object> objectMap = new HashMap<Integer, Object>();
        long result = saveRec(model, objectMap);
        if (result > 0)
            mLogger.debug(model.getClass().getSimpleName() + " model saved");
        else
            mLogger.debug(model.getClass().getSimpleName() + " model was not saved");
        return result;
    }

    @Override
    public boolean update(Object model) throws InfinitumRuntimeException {
        OrmPreconditions.checkForTransaction(mIsAutocommit, isTransactionOpen());
        OrmPreconditions.checkPersistenceForModify(model, mPersistencePolicy);
        Map<Integer, Object> objectMap = new HashMap<Integer, Object>();
        boolean result = updateRec(model, objectMap);
        if (result)
            mLogger.debug(model.getClass().getSimpleName() + " model updated");
        else
            mLogger.debug(model.getClass().getSimpleName() + " model was not updated");
        return result;
    }

    @Override
    public boolean delete(Object model) throws InfinitumRuntimeException {
        model = AbstractProxy.getTarget(model);
        OrmPreconditions.checkForTransaction(mIsAutocommit, isTransactionOpen());
        OrmPreconditions.checkPersistenceForModify(model, mPersistencePolicy);
        String tableName = mPersistencePolicy.getModelTableName(model.getClass());
        String whereClause = mSqliteUtil.getWhereClause(model, mMapper);
        int result = mSqliteDb.delete(tableName, whereClause, null);
        if (result == 1) {
            deleteRelationships(model);
            mLogger.debug(model.getClass().getSimpleName() + " model deleted");
        } else {
            mLogger.debug(model.getClass().getSimpleName() + " model was not deleted");
        }
        return result == 1;
    }

    @Override
    public long saveOrUpdate(Object model) throws InfinitumRuntimeException {
        OrmPreconditions.checkForTransaction(mIsAutocommit, isTransactionOpen());
        OrmPreconditions.checkPersistenceForModify(model, mPersistencePolicy);
        Map<Integer, Object> objectMap = new HashMap<Integer, Object>();
        long result = saveOrUpdateRec(model, objectMap);
        if (result == 0)
            mLogger.debug(model.getClass().getSimpleName() + " model updated");
        else if (result > 0)
            mLogger.debug(model.getClass().getSimpleName() + " model saved");
        else
            mLogger.debug(model.getClass().getSimpleName() + " model was not saved or updated");
        return result;
    }

    @Override
    public <T> T load(Class<T> clazz, Serializable id) throws InfinitumRuntimeException, IllegalArgumentException {
        OrmPreconditions.checkPersistenceForLoading(clazz, mPersistencePolicy);
        if (!mTypePolicy.isValidPrimaryKey(mPersistencePolicy.getPrimaryKeyField(clazz), id))
            throw new IllegalArgumentException(String.format("Invalid primary key value of type '%s' for '%s'.",
                    id.getClass()
                            .getSimpleName(), clazz.getName()));
        Cursor cursor = mSqliteDb.query(mPersistencePolicy.getModelTableName(clazz), null,
                mSqliteUtil.getWhereClause(clazz, id, mMapper),
                null, null, null, null, "1");
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }
        cursor.moveToFirst();
        SqliteResult result = new SqliteResult(cursor);
        T ret = null;
        try {
            ret = mModelFactory.createFromResult(result, clazz);
        } catch (InfinitumRuntimeException e) {
            throw e;
        } finally {
            result.close();
        }
        mLogger.debug(clazz.getSimpleName() + " model loaded");
        return ret;
    }

    @Override
    public void execute(String sql) throws SQLGrammarException {
        OrmPreconditions.checkForTransaction(mIsAutocommit, isTransactionOpen());
        mLogger.debug("Executing SQL: " + sql);
        try {
            mSqliteDb.execSQL(sql);
        } catch (SQLiteException e) {
            throw new SQLGrammarException(String.format("There was a problem with the SQL formatting. Could not " +
                    "execute query: %s", sql));
        }
    }

    @Override
    public Cursor executeForResult(String sql) throws SQLGrammarException {
        mLogger.debug("Executing SQL: " + sql);
        try {
            return mSqliteDb.rawQuery(sql, null);
        } catch (SQLiteException e) {
            throw new SQLGrammarException(String.format("There was a problem with the SQL formatting. Could not " +
                    "execute query: %s", sql));
        }
    }

    @Override
    public <T> void registerTypeAdapter(Class<T> type, SqliteTypeAdapter<T> adapter) {
        mMapper.registerTypeAdapter(type, adapter);
    }

    @Override
    public Map<Class<?>, SqliteTypeAdapter<?>> getRegisteredTypeAdapters() {
        return mMapper.getRegisteredTypeAdapters();
    }

    /**
     * Returns the {@link SqliteMapper} associated with this {@code SqliteTemplate}.
     *
     * @return {@code SqliteMapper}
     */
    public SqliteMapper getSqliteMapper() {
        return mMapper;
    }

    private long saveOrUpdateRec(Object model, Map<Integer, Object> objectMap) {
        // First try to update the entity, then try to save it if needed
        return updateRec(model, objectMap) ? 0 : saveRec(model, objectMap);
    }

    private long saveRec(Object model, Map<Integer, Object> objectMap) {
        model = AbstractProxy.getTarget(model);
        // Check if the entity has already been persisted
        int objHash = mPersistencePolicy.computeModelHash(model);
        if (objectMap.containsKey(objHash) && !mPersistencePolicy.isPKNullOrZero(model))
            return 0;
        // Persist it
        SqliteModelMap map = mMapper.mapModel(model);
        ContentValues values = map.getContentValues();
        String tableName = mPersistencePolicy.getModelTableName(model.getClass());
        long rowId = mSqliteDb.insert(tableName, null, values);
        if (rowId <= 0) {
            // Persist failed
            return rowId;
        }
        // Persist succeeded
        setPrimaryKey(model, rowId);
        objHash = mPersistencePolicy.computeModelHash(model);
        objectMap.put(objHash, model);
        processRelationships(map, objectMap, model, mPersistencePolicy.getCascadeMode(model.getClass()));
        return rowId;
    }

    private boolean updateRec(Object model, Map<Integer, Object> objectMap) {
        model = AbstractProxy.getTarget(model);
        int objHash = mPersistencePolicy.computeModelHash(model);
        if (objectMap.containsKey(objHash) && !mPersistencePolicy.isPKNullOrZero(model))
            return true;
        SqliteModelMap map = mMapper.mapModel(model);
        ContentValues values = map.getContentValues();
        String tableName = mPersistencePolicy.getModelTableName(model.getClass());
        String whereClause = mSqliteUtil.getWhereClause(model, mMapper);
        if (values.size() == 0)
            return false;
        long ret = mSqliteDb.update(tableName, values, whereClause, null);
        if (ret <= 0) {
            return false;
        }
        objectMap.put(objHash, model);
        processRelationships(map, objectMap, model, mPersistencePolicy.getCascadeMode(model.getClass()));
        return true;
    }

    private void processRelationships(SqliteModelMap map, Map<Integer, Object> objectMap, Object model,
                                      Cascade cascade) {
        if (cascade == Cascade.NONE)
            return;
        processManyToManyRelationships(model, map, objectMap, cascade);
        processManyToOneRelationships(model, map, objectMap, cascade);
        processOneToManyRelationships(model, map, objectMap, cascade);
        processOneToOneRelationships(model, map, objectMap, cascade);
    }

    private void processManyToManyRelationships(Object model, SqliteModelMap map, Map<Integer, Object> objectMap,
                                                Cascade cascade) {
        for (Pair<ManyToManyRelationship, Iterable<Object>> relationshipPair : map.getManyToManyRelationships()) {
            ManyToManyRelationship relationship = relationshipPair.getFirst();
            List<Serializable> nonStaleKeys = new ArrayList<Serializable>();
            for (Object relatedEntity : relationshipPair.getSecond()) {
                if (relatedEntity == null) {
                    // Related entity is null, nothing to do here...
                    continue;
                }
                int relatedHash = mPersistencePolicy.computeModelHash(relatedEntity);
                if (objectMap.containsKey(relatedHash) && !mPersistencePolicy.isPKNullOrZero(relatedEntity)) {
                    nonStaleKeys.add(mPersistencePolicy.getPrimaryKey(relatedEntity));
                    continue;
                }
                // Cascade.All means we persist/update related entities
                if (cascade == Cascade.ALL) {
                    // Save or update the related entity
                    if (saveOrUpdateRec(relatedEntity, objectMap) >= 0) {
                        // Persist relationship to many-to-many table
                        insertManyToManyRelationship(model, relatedEntity, relationship);
                        nonStaleKeys.add(mPersistencePolicy.getPrimaryKey(relatedEntity));
                    }
                    // Cascade.Keys means we persist/update foreign keys
                } else if (cascade == Cascade.KEYS && !mPersistencePolicy.isPKNullOrZero(relatedEntity)) {
                    // Persist relationship to many-to-many table
                    insertManyToManyRelationship(model, relatedEntity, relationship);
                    nonStaleKeys.add(mPersistencePolicy.getPrimaryKey(relatedEntity));
                }
            }
            // Delete stale relationships
            String staleRelQuery = mSqlBuilder.createDeleteStaleRelationshipQuery(relationship, model, nonStaleKeys);
            // Execute only if there are stale relationships
            if (!staleRelQuery.contains("NOT IN ()"))
                mSqliteDb.execSQL(staleRelQuery);
        }
    }

    private void processOneToOneRelationships(Object model, SqliteModelMap map, Map<Integer, Object> objectMap,
                                              Cascade cascade) {
        for (Pair<OneToOneRelationship, Object> relationshipPair : map.getOneToOneRelationships()) {
            OneToOneRelationship relationship = relationshipPair.getFirst();
            Object relatedEntity = relationshipPair.getSecond();
            if (mClassReflector.isNull(relatedEntity)) {
                // Related entity is null, nothing to do here...
                continue;
            }
            // Cascade.All means we persist/update related entities
            if (cascade == Cascade.ALL) {
                // Save or update the related entity
                if (saveOrUpdateRec(relatedEntity, objectMap) >= 0 && relationship.getOwner() == model.getClass()) {
                    // Update the relationship owner's foreign key
                    String sql = mSqlBuilder.createUpdateOneToOneForeignKeyQuery(relationship, model, relatedEntity);
                    mSqliteDb.execSQL(sql);
                }
                // Cascade.Keys means we persist/update foreign keys
            } else if (cascade == Cascade.KEYS && !mPersistencePolicy.isPKNullOrZero(relatedEntity)) {
                // Update the relationship owner's foreign key
                String sql = mSqlBuilder.createUpdateOneToOneForeignKeyQuery(relationship, model, relatedEntity);
                mSqliteDb.execSQL(sql);
            }
        }
    }

    private void processOneToManyRelationships(Object model, SqliteModelMap map, Map<Integer, Object> objectMap,
                                               Cascade cascade) {
        for (Pair<OneToManyRelationship, Iterable<Object>> relationshipPair : map.getOneToManyRelationships()) {
            List<Serializable> relatedKeys = new ArrayList<Serializable>();
            for (Object relatedEntity : relationshipPair.getSecond()) {
                if (relatedEntity == null) {
                    // Related entity is null, nothing to do here...
                    continue;
                }
                int relatedHash = mPersistencePolicy.computeModelHash(relatedEntity);
                if (objectMap.containsKey(relatedHash) && !mPersistencePolicy.isPKNullOrZero(relatedEntity))
                    continue;
                // Cascade.All means we persist/update related entities
                if (cascade == Cascade.ALL) {
                    // Save or update the related entity
                    if (saveOrUpdateRec(relatedEntity, objectMap) >= 0) {
                        // Include its foreign key to be updated
                        relatedKeys.add(mPersistencePolicy.getPrimaryKey(relatedEntity));
                    }
                    // Cascade.Keys means we persist/update foreign keys
                } else if (cascade == Cascade.KEYS && !mPersistencePolicy.isPKNullOrZero(relatedEntity)) {
                    // Include its foreign key to be updated
                    relatedKeys.add(mPersistencePolicy.getPrimaryKey(relatedEntity));
                }
            }
            // Update the foreign keys
            String updateQuery = mSqlBuilder.createUpdateForeignKeyQuery(relationshipPair.getFirst(), model,
                    relatedKeys);
            mSqliteDb.execSQL(updateQuery);
        }
    }

    private void processManyToOneRelationships(Object model, SqliteModelMap map, Map<Integer, Object> objectMap,
                                               Cascade cascade) {
        for (Pair<ManyToOneRelationship, Object> relationshipPair : map.getManyToOneRelationships()) {
            Object relatedEntity = relationshipPair.getSecond();
            if (mClassReflector.isNull(relatedEntity)) {
                // Related entity is null, nothing to do here...
                continue;
            }
            // Cascade.All means we persist/update related entities
            if (cascade == Cascade.ALL) {
                // Save or update the related entity
                if (saveOrUpdateRec(relatedEntity, objectMap) >= 0) {
                    // Update the foreign key
                    String update = mSqlBuilder.createUpdateQuery(model, relatedEntity,
                            relationshipPair.getFirst().getColumn());
                    mSqliteDb.execSQL(update);
                }
                // Cascade.Keys means we persist/update foreign keys
            } else if (cascade == Cascade.KEYS && !mPersistencePolicy.isPKNullOrZero(relatedEntity)) {
                // Update the foreign key
                String update = mSqlBuilder.createUpdateQuery(model, relatedEntity,
                        relationshipPair.getFirst().getColumn());
                mSqliteDb.execSQL(update);
            }
        }
    }

    private void insertManyToManyRelationship(Object model, Object related, ManyToManyRelationship mtm) {
        ContentValues relationshipData = new ContentValues();
        Class<?> firstType = mtm.getFirstType();
        Class<?> secondType = mtm.getSecondType();
        // TODO Doesn't support reflexive relationships
        try {
            Field firstField;
            Field secondField;
            Serializable firstPk;
            Serializable secondPk;
            if (model.getClass() == firstType) {
                firstField = mPersistencePolicy.findPersistentField(firstType, mtm.getFirstFieldName());
                secondField = mPersistencePolicy.findPersistentField(secondType, mtm.getSecondFieldName());
                firstPk = (Serializable) mClassReflector.getFieldValue(model, firstField);
                secondPk = (Serializable) mClassReflector.getFieldValue(related, secondField);
            } else if (model.getClass() == secondType) {
                secondField = mPersistencePolicy.findPersistentField(firstType, mtm.getFirstFieldName());
                firstField = mPersistencePolicy.findPersistentField(secondType, mtm.getSecondFieldName());
                firstPk = (Serializable) mClassReflector.getFieldValue(related, firstField);
                secondPk = (Serializable) mClassReflector.getFieldValue(model, secondField);
            } else {
                throw new InfinitumRuntimeException("Invalid many-to-many relationship");
            }
            String firstCol = mPersistencePolicy.getModelTableName(firstType) + '_' + mPersistencePolicy
                    .getFieldColumnName(firstField) + "_1";
            String secondCol = mPersistencePolicy.getModelTableName(secondType) + '_' + mPersistencePolicy
                    .getFieldColumnName(secondField) + "_2";
            putRelationalKey(relationshipData, firstCol, firstField, firstPk);
            putRelationalKey(relationshipData, secondCol, secondField, secondPk);
            boolean result;
            try {
                result = mSqliteDb.insertOrThrow(mtm.getTableName(), null, relationshipData) > 0;
            } catch (SQLException e) {
                return;
            }
            if (result)
                mLogger.debug(firstType.getSimpleName() + "-" + secondType.getSimpleName() + " relationship saved");
            else
                mLogger.error(firstType.getSimpleName() + "-" + secondType.getSimpleName() + " relationship was not " +
                        "saved");
        } catch (ClassCastException e) {
            throw new ModelConfigurationException("Invalid primary key.", e);
        }
    }

    private void deleteRelationships(Object model) {
        SqliteModelMap map = mMapper.mapModel(model);
        for (Pair<ManyToManyRelationship, Iterable<Object>> relationshipPair : map.getManyToManyRelationships()) {
            ManyToManyRelationship relationship = relationshipPair.getFirst();
            mSqliteDb.execSQL(mSqlBuilder.createManyToManyDeleteQuery(model, relationship), null);
        }
        // TODO Update non M:M relationships?
    }

    private void setPrimaryKey(Object model, long rowId) {
        Field pkField = mPersistencePolicy.getPrimaryKeyField(model.getClass());
        Class<?> pkType = Primitives.unwrap(pkField.getType());
        // The row ID is not a PK if the PK type is not int or long
        if (pkType != int.class && pkType != long.class)
            return;
        mClassReflector.setFieldValue(model, pkField, rowId);
    }

    private void putRelationalKey(ContentValues relationshipData, String column, Field field, Serializable value) {
        switch (mMapper.getSqliteDataType(field)) {
            case INTEGER:
                if (Primitives.unwrap(field.getType()) == int.class)
                    relationshipData.put(column, (Integer) value);
                else if (Primitives.unwrap(field.getType()) == long.class)
                    relationshipData.put(column, (Long) value);
                else
                    relationshipData.put(column, (Short) value);
                break;
            case TEXT:
                if (Primitives.unwrap(field.getType()) == char.class)
                    relationshipData.put(column, Character.toString((Character) value));
                else
                    relationshipData.put(column, (String) value);
                break;
            case REAL:
                if (Primitives.unwrap(field.getType()) == float.class)
                    relationshipData.put(column, (Float) value);
                else
                    relationshipData.put(column, (Double) value);
                break;
            case BLOB:
                if (Primitives.unwrap(field.getType()) == byte.class)
                    relationshipData.put(column, (Byte) value);
                else
                    relationshipData.put(column, (byte[]) value);
                break;
            default:
                throw new InfinitumRuntimeException("Invalid relational key type");
        }
    }

}
