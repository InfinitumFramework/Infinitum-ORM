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

package com.clarionmedia.infinitum.orm.criteria.criterion;

import java.io.Serializable;
import java.lang.reflect.Field;

import com.clarionmedia.infinitum.orm.context.InfinitumOrmContext;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.exception.InvalidCriteriaException;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.sql.SqlConstants;

/**
 * <p>
 * Represents a binary logical expression {@link Criterion}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/17/12
 */
public class BinaryExpression extends Criterion {

	private static final long serialVersionUID = 1282172886230328002L;

	private Object mValue;
	private String mOperator;
	private boolean mIgnoreCase;

	/**
	 * Constructs a new {@code BinaryExpression} with the given field name,
	 * value, and binary operator.
	 * 
	 * @param fieldName
	 *            the name of the field to check value for
	 * @param value
	 *            the value to check for
	 * @param operator
	 *            the binary operator
	 */
	public BinaryExpression(String fieldName, Object value, String operator) {
		super(fieldName);
		mValue = value;
		mOperator = operator;
	}

	/**
	 * Constructs a new {@code BinaryExpression} with the given {@link Field}
	 * name, value, and binary operator, and ignore case {@code boolean}.
	 * 
	 * @param fieldName
	 *            the name of the field to check value for
	 * @param value
	 *            the value to check for
	 * @param operator
	 *            the binary operator
	 * @param ignoreCase
	 *            indicates if case should be ignored for {@link String} values
	 */
	public BinaryExpression(String fieldName, Object value, String operator, boolean ignoreCase) {
		super(fieldName);
		mFieldName = fieldName;
		mValue = value;
		mOperator = operator;
		mIgnoreCase = ignoreCase;
	}

	@Override
	public String toSql(Criteria<?> criteria) throws InvalidCriteriaException {
		StringBuilder query = new StringBuilder();
		Class<?> c = criteria.getEntityClass();
		Field f = null;
		PersistencePolicy policy = mContextFactory.getContext(InfinitumOrmContext.class).getPersistencePolicy();
		try {
			f = policy.findPersistentField(c, mFieldName);
			if (f == null)
				throw new InvalidCriteriaException(String.format("Invalid Criteria for type '%s'.", c.getName()));
			f.setAccessible(true);
		} catch (SecurityException e) {
			throw new InvalidCriteriaException(String.format("Invalid Criteria for type '%s'.", c.getName()));
		}
		String colName = policy.getFieldColumnName(f);
		boolean lowerCase = mIgnoreCase && criteria.getObjectMapper().isTextColumn(f);
		if (lowerCase)
			query.append(SqlConstants.LOWER).append('(');
		query.append(colName);
		if (lowerCase)
			query.append(')');
		query.append(' ').append(mOperator).append(' ');
		// If it's a related object, use its primary key
		if (policy.isToOneRelationship(f)) {
			Serializable pk = policy.getPrimaryKey(mValue);
			if (criteria.getObjectMapper().isTextColumn(f))
				query.append("'").append(pk).append("'");
			else
				query.append(pk);
		} else {
			if (criteria.getObjectMapper().isTextColumn(f))
				query.append("'").append(mValue).append("'");
			else
				query.append(mValue);
		}
		return query.toString();
	}

}
