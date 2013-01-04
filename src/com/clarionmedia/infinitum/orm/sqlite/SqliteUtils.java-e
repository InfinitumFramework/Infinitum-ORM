/*
 * Copyright (c) 2012 Tyler Treat
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

package com.clarionmedia.infinitum.orm.sqlite;

import java.io.Serializable;
import java.lang.reflect.Field;

import com.clarionmedia.infinitum.di.AbstractProxy;
import com.clarionmedia.infinitum.di.annotation.Autowired;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.TypeResolutionPolicy;
import com.clarionmedia.infinitum.orm.persistence.TypeResolutionPolicy.SqliteDataType;
import com.clarionmedia.infinitum.orm.sqlite.impl.SqliteMapper;
import com.clarionmedia.infinitum.reflection.ClassReflector;

/**
 * <p>
 * This class contains utility methods for generating SQL strings for a SQLite
 * database.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/15/12
 * @since 1.0
 */
public class SqliteUtils {

	@Autowired
	private TypeResolutionPolicy mTypePolicy;
	
	@Autowired
	private PersistencePolicy mPersistencePolicy;
	
	@Autowired
	private ClassReflector mClassReflector;

	/**
	 * Generates a "where clause" {@link String} used for updating or deleting
	 * the given persistent {@link Object} in the database. Where conditions are
	 * indicated by primary key {@link Field}'s. Note that the actual
	 * {@code String} "where" is not included with the resulting output.
	 * 
	 * <p>
	 * For example, passing an object of class {@code Foobar} which has a
	 * primary key {@code foo} with a value of {@code 42} will result in the
	 * where clause {@code foo = 42}.
	 * </p>
	 * 
	 * @param model
	 *            the model to generate the where clause for
	 * @return where clause {@code String} for specified {@code Object}
	 * @throws InfinitumRuntimeException
	 *             if there is an error generating the SQL
	 */
	public String getWhereClause(Object model, SqliteMapper mapper)
			throws InfinitumRuntimeException {
		if (AbstractProxy.isAopProxy(model)) {
			model = AbstractProxy.getProxy(model).getTarget();
		}
		Field pk = mPersistencePolicy.getPrimaryKeyField(model.getClass());
		StringBuilder whereClause = new StringBuilder();
		pk.setAccessible(true);
		whereClause.append(mPersistencePolicy.getFieldColumnName(pk)).append(" = ");
		SqliteDataType t = mapper.getSqliteDataType(pk);
		Serializable pkVal = null;
		try {
			pkVal = (Serializable) mClassReflector.getFieldValue(model, pk);
		} catch (ClassCastException e) {
			throw new ModelConfigurationException("Invalid primary key specified for type '" + model.getClass().getName() + "'.");
		}
		if (t == SqliteDataType.TEXT)
			whereClause.append("'").append(pkVal).append("'");
		else
			whereClause.append(pkVal);
		return whereClause.toString();
	}

	/**
	 * Generates a "where clause" {@link String} used for the given persistent
	 * {@link Class} in the database with the given primary key. Where
	 * conditions are indicated by primary key {@link Field}'s. Note that the
	 * actual {@code String} "where" is not included with the resulting output.
	 * 
	 * <p>
	 * For example, passing a {@code Class} {@code Foobar} which has a primary
	 * key {@code foo} with a value of {@code 42} will result in the where
	 * clause {@code foo = 42}.
	 * </p>
	 * 
	 * @param c
	 *            the {@code Class} of the model
	 * @param id
	 *            the primary key for the model
	 * @return where clause {@code String} for specified {@code Class} and
	 *         primary key
	 * @throws IllegalArgumentException
	 *             if there is a mismatch between the {@code Class}'s primary
	 *             key type and the type of the given primary key
	 */
	public String getWhereClause(Class<?> c, Serializable id, SqliteMapper mapper) throws IllegalArgumentException {
		Field pk = mPersistencePolicy.getPrimaryKeyField(c);
		if (!mTypePolicy.isValidPrimaryKey(pk, id))
			throw new IllegalArgumentException(String.format("Invalid primary key value of type '%s' for '%s'.", id.getClass().getSimpleName(), c.getName()));
		StringBuilder sb = new StringBuilder();
		sb.append(mPersistencePolicy.getFieldColumnName(pk)).append(" = ");
		SqliteDataType t = mapper.getSqliteDataType(pk);
		if (t == SqliteDataType.TEXT)
			sb.append("'").append(id).append("'");
		else
			sb.append(id);
		return sb.toString();
	}

}
