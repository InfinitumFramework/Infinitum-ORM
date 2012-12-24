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
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import android.database.Cursor;

import com.clarionmedia.infinitum.di.annotation.Autowired;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.LazyLoadDexMakerProxy;
import com.clarionmedia.infinitum.orm.ModelFactory;
import com.clarionmedia.infinitum.orm.ResultSet;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.relationship.ForeignKeyRelationship;
import com.clarionmedia.infinitum.orm.relationship.ManyToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.ManyToOneRelationship;
import com.clarionmedia.infinitum.orm.relationship.ModelRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToOneRelationship;
import com.clarionmedia.infinitum.orm.sqlite.SqliteTypeAdapter;
import com.clarionmedia.infinitum.reflection.ClassReflector;

/**
 * <p>
 * This is an implementation of {@link ModelFactory} for processing {@link SqliteResult}
 * queries.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/20/12
 * @since 1.0
 */
public class SqliteModelFactory implements ModelFactory {

	@Autowired
	private SqliteBuilder mSqlBuilder;
	
	@Autowired
	private SqliteSession mSession;
	
	@Autowired
	private SqliteMapper mMapper;
	
	@Autowired
	private PersistencePolicy mPersistencePolicy;
	
	@Autowired
	private ClassReflector mClassReflector;

	@Override
	public <T> T createFromResult(ResultSet result, Class<T> modelClass) {
		if (!(result instanceof SqliteResult))
			throw new IllegalArgumentException("SqliteModelFactory can only process SqliteResults.");
		return createFromCursorRec(((SqliteResult) result).getCursor(), modelClass);
	}
	
	/**
	 * Constructs a domain model instance and populates its {@link Field}'s from
	 * the given {@link Cursor}. The precondition for this method is that the
	 * {@code Cursor} is currently at the row to convert to an {@link Object}
	 * from the correct table.
	 * 
	 * @param cursor
	 *            the {@code Cursor} containing the row to convert to an
	 *            {@code Object}
	 * @param modelClass
	 *            the {@code Class} of the {@code Object} being instantiated
	 * @return a populated instance of the specified {@code Class}
	 * @throws ModelConfigurationException
	 *             if the specified model {@code Class} does not contain an
	 *             empty constructor
	 * @throws InfinitumRuntimeException
	 *             if the model could not be instantiated
	 */
	public <T> T createFromCursor(Cursor cursor, Class<T> modelClass) throws ModelConfigurationException, InfinitumRuntimeException {
		return createFromCursorRec(cursor, modelClass);
	}

	@SuppressWarnings("unchecked")
	private <T> T createFromCursorRec(Cursor cursor, Class<T> modelClass) throws ModelConfigurationException, InfinitumRuntimeException {
		T ret = null;
		SqliteResult result = new SqliteResult(cursor);
		ret = (T) mClassReflector.getClassInstance(modelClass);
		List<Field> fields = mPersistencePolicy.getPersistentFields(modelClass);
		for (Field field : fields) {
			field.setAccessible(true);
			if (!mPersistencePolicy.isRelationship(field)) {
				SqliteTypeAdapter<?> resolver = mMapper.resolveType(field.getType());
				int index = result.getColumnIndex(mPersistencePolicy.getFieldColumnName(field));
				try {
					resolver.mapToObject(result, index, field, ret);
				} catch (IllegalArgumentException e) {
					throw new InfinitumRuntimeException("Could not map '" + field.getType().getName() + "'");
				} catch (IllegalAccessException e) {
					throw new InfinitumRuntimeException("Could not map '" + field.getType().getName() + "'");
				}
			}
		}
		int objHash = mPersistencePolicy.computeModelHash(ret);
		if (mSession.checkCache(objHash))
			return (T) mSession.searchCache(objHash);
		mSession.cache(objHash, ret);
		loadRelationships(ret);
		return ret;
	}

	private <T> void loadRelationships(T model)
			throws ModelConfigurationException, InfinitumRuntimeException {
		for (Field f : mPersistencePolicy.getPersistentFields(model.getClass())) {
			f.setAccessible(true);
			if (!mPersistencePolicy.isRelationship(f))
				continue;
			ModelRelationship rel = mPersistencePolicy.getRelationship(f);
			switch (rel.getRelationType()) {
			case ManyToMany:
				if (mPersistencePolicy.isLazy(model.getClass()))
					lazilyLoadManyToMany((ManyToManyRelationship) rel, f, model);
				else
					loadManyToMany((ManyToManyRelationship) rel, f, model);
				break;
			case ManyToOne:
				if (mPersistencePolicy.isLazy(model.getClass()))
					lazilyLoadManyToOne((ManyToOneRelationship) rel, f, model);
				else
					loadManyToOne((ManyToOneRelationship) rel, f, model);
				break;
			case OneToMany:
				if (mPersistencePolicy.isLazy(model.getClass()))
					lazilyLoadOneToMany((OneToManyRelationship) rel, f, model);
				else
					loadOneToMany((OneToManyRelationship) rel, f, model);
				break;
			case OneToOne:
				if (mPersistencePolicy.isLazy(model.getClass()))
					lazilyLoadOneToOne((OneToOneRelationship) rel, f, model);
				else
					loadOneToOne((OneToOneRelationship) rel, f, model);
				break;
			}
		}
	}

	private <T> void lazilyLoadOneToOne(final OneToOneRelationship rel, Field field, T model) {
		final String sql = getOneToOneEntityQuery(model, rel.getSecondType(), field, rel);
		Object related = null;
		if (mSession.count(sql.replace("*", "count(*)")) > 0) {
			related = new LazyLoadDexMakerProxy(mSession.getContext(), rel.getSecondType()) {
				@Override
				protected Object loadObject() {
					Object ret = null;
					Cursor result = mSession.executeForResult(sql, true);
					try {
					    while (result.moveToNext())
						    ret = createFromCursor(result, rel.getSecondType());
					} finally {
					    result.close();
					}
					return ret;
				}
			}.getProxy();
		}
		mClassReflector.setFieldValue(model, field, related);
	}

	private <T> void loadOneToOne(OneToOneRelationship rel, Field field, T model) {
		String sql = getOneToOneEntityQuery(model, rel.getSecondType(), field, rel);
		Cursor result = mSession.executeForResult(sql, true);
		try {
		    while (result.moveToNext())
		        mClassReflector.setFieldValue(model, field, createFromCursor(result, rel.getSecondType()));
		} finally {
		    result.close();
		}
	}

	private <T> void lazilyLoadOneToMany(final OneToManyRelationship rel, Field field, T model) {
		final StringBuilder sql = new StringBuilder("SELECT * FROM ")
				.append(mPersistencePolicy.getModelTableName(rel.getManyType()))
				.append(" WHERE ").append(rel.getColumn()).append(" = ");
		Serializable pk = mPersistencePolicy.getPrimaryKey(model);
		switch (mMapper.getSqliteDataType(mPersistencePolicy.getPrimaryKeyField(model.getClass()))) {
		case TEXT:
			sql.append("'").append(pk).append("'");
			break;
		default:
			sql.append(pk);
		}
		@SuppressWarnings("unchecked")
		final Collection<Object> collection = (Collection<Object>) mClassReflector.getFieldValue(model, field);
		@SuppressWarnings("unchecked")
		Collection<Object> related = (Collection<Object>) new LazyLoadDexMakerProxy(mSession.getContext(), collection.getClass()) {
			@Override
			protected Object loadObject() {
				Cursor result = mSession.executeForResult(sql.toString(), true);
				while (result.moveToNext())
					collection.add(createFromCursor(result, rel.getManyType()));
				result.close();
				return collection;
			}
		}.getProxy();
		mClassReflector.setFieldValue(model, field, related);
	}

	private <T> void loadOneToMany(OneToManyRelationship rel, Field field, T model) {
		StringBuilder sql = new StringBuilder("SELECT * FROM ")
				.append(mPersistencePolicy.getModelTableName(rel.getManyType()))
				.append(" WHERE ").append(rel.getColumn()).append(" = ");
		Serializable pk = mPersistencePolicy.getPrimaryKey(model);
		switch (mMapper.getSqliteDataType(mPersistencePolicy.getPrimaryKeyField(model.getClass()))) {
		case TEXT:
			sql.append("'").append(pk).append("'");
			break;
		default:
			sql.append(pk);
		}
		@SuppressWarnings("unchecked")
		Collection<Object> related = (Collection<Object>) mClassReflector.getFieldValue(model, field);
		Cursor result = mSession.executeForResult(sql.toString(), true);
		try {
		    while (result.moveToNext())
			    related.add(createFromCursor(result, rel.getManyType()));
		} finally {
			result.close();
		}
		mClassReflector.setFieldValue(model, field, related);
	}

	private <T> void lazilyLoadManyToOne(ManyToOneRelationship rel, Field field, T model) {
		final Class<?> direction = model.getClass() == rel.getFirstType() ? rel.getSecondType() : rel.getFirstType();
		final String sql = getEntityQuery(model, direction, field, rel);
		Object related = null;
		if (mSession.count(sql.replace("*", "count(*)")) > 0) {
			related = new LazyLoadDexMakerProxy(mSession.getContext(), rel.getSecondType()) {
				@Override
				protected Object loadObject() {
					Object ret = null;
					Cursor result = mSession.executeForResult(sql, true);
					try {
					    while (result.moveToNext())
						    ret = createFromCursor(result, direction);
					} finally {
					    result.close();
					}
					return ret;
				}
			}.getProxy();
		}
		mClassReflector.setFieldValue(model, field, related);
	}

	private <T> void loadManyToOne(ManyToOneRelationship rel, Field field, T model) {
		Class<?> direction = model.getClass() == rel.getFirstType() ? rel.getSecondType() : rel.getFirstType();
		String sql = getEntityQuery(model, direction, field, rel);
		Cursor result = mSession.executeForResult(sql, true);
		try {
		    while (result.moveToNext())
		        mClassReflector.setFieldValue(model, field, createFromCursor(result, direction));
		} finally {
		    result.close();
		}
	}

	private <T> void lazilyLoadManyToMany(final ManyToManyRelationship rel, Field field, T model) {
		// TODO Add reflexive M:M support
		final Class<?> direction = model.getClass() == rel.getFirstType() ? rel.getSecondType() : rel.getFirstType();
		Serializable pk = mPersistencePolicy.getPrimaryKey(model);
		final String sql = mSqlBuilder.createManyToManyJoinQuery(rel, pk, direction);
		@SuppressWarnings("unchecked")
		final Collection<Object> collection = (Collection<Object>) mClassReflector.getFieldValue(model, field);
		@SuppressWarnings("unchecked")
		Collection<Object> related = (Collection<Object>) new LazyLoadDexMakerProxy(mSession.getContext(), collection.getClass()) {
			@Override
			protected Object loadObject() {
				Cursor result = mSession.executeForResult(sql, true);
				try {
				    while (result.moveToNext())
					    collection.add(createFromCursor(result, direction));
				} finally {
				    result.close();
				}
				return collection;
			}
		}.getProxy();
		mClassReflector.setFieldValue(model, field, related);
	}

	private <T> void loadManyToMany(ManyToManyRelationship rel, Field f, T model) throws ModelConfigurationException, InfinitumRuntimeException {
		// TODO Add reflexive M:M support
		Class<?> direction = model.getClass() == rel.getFirstType() ? rel.getSecondType() : rel.getFirstType();
		Serializable pk = mPersistencePolicy.getPrimaryKey(model);
		String sql = mSqlBuilder.createManyToManyJoinQuery(rel, pk, direction);
		Cursor result = mSession.executeForResult(sql, true);
		@SuppressWarnings("unchecked")
		Collection<Object> related = (Collection<Object>) mClassReflector.getFieldValue(model, f);
		try {
		    while (result.moveToNext())
			    related.add(createFromCursor(result, direction));
		} finally {
		    result.close();
		}
		mClassReflector.setFieldValue(model, f, related);
	}

	private String getEntityQuery(Object model, Class<?> c, Field field, ForeignKeyRelationship rel) {
		StringBuilder sql = new StringBuilder("SELECT * FROM ")
				.append(mPersistencePolicy.getModelTableName(c))
				.append(" WHERE ")
				.append(mPersistencePolicy.getFieldColumnName(mPersistencePolicy.getPrimaryKeyField(c))).append(" = ");
		switch (mMapper.getSqliteDataType(field)) {
		case TEXT:
			sql.append("'").append(getForeignKey(model, rel)).append("'");
			break;
		default:
			sql.append(getForeignKey(model, rel));
		}
		return sql.append(" LIMIT 1").toString();
	}
	
	private String getOneToOneEntityQuery(Object model, Class<?> relatedClass, Field field, OneToOneRelationship rel) {
		boolean isOwner = rel.getOwner() == model.getClass();
		StringBuilder sql = new StringBuilder("SELECT * FROM ")
				.append(mPersistencePolicy.getModelTableName(relatedClass))
				.append(" WHERE ");
		if (isOwner) {
		    sql.append(mPersistencePolicy.getFieldColumnName(mPersistencePolicy.getPrimaryKeyField(relatedClass)));
		} else {
			sql.append(rel.getColumn());
		}
		sql.append(" = ");
		switch (mMapper.getSqliteDataType(field)) {
		case TEXT:
			sql.append("'").append(getOneToOneKey(model, isOwner, rel)).append("'");
			break;
		default:
			sql.append(getOneToOneKey(model, isOwner, rel));
		}
		return sql.append(" LIMIT 1").toString();
	}
	
	private Serializable getOneToOneKey(Object model, boolean isOwner, OneToOneRelationship rel) {
		if (isOwner) {
			return getForeignKey(model, rel);
		} else {
			return mPersistencePolicy.getPrimaryKey(model);
		}
	}

	private Serializable getForeignKey(Object model, ForeignKeyRelationship rel) {
		StringBuilder q = new StringBuilder("SELECT ")
				.append(rel.getColumn())
				.append(" FROM ")
				.append(mPersistencePolicy.getModelTableName(model.getClass()))
				.append(" WHERE ")
				.append(mPersistencePolicy.getFieldColumnName(mPersistencePolicy
						.getPrimaryKeyField(model.getClass()))).append(" = ");
		Serializable pk = mPersistencePolicy.getPrimaryKey(model);
		switch (mMapper.getSqliteDataType(pk)) {
		case TEXT:
			q.append("'").append(pk).append("'");
			break;
		default:
			q.append(pk);
		}
		Cursor result = mSession.executeForResult(q.toString(), true);
		result.moveToFirst();
		Serializable id;
		try {
			id = result.getString(0);
		} catch (ClassCastException e) {
			throw new ModelConfigurationException("Invalid primary key specified for '" + model.getClass().getName() + "'.");
		} finally {
			result.close();
		}
		return id;
	}

}
