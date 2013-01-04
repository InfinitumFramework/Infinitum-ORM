/*
 * Copyright (C) 2012 Clarion Media, LLC
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

import java.lang.reflect.Field;

import com.clarionmedia.infinitum.orm.context.InfinitumOrmContext;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.exception.InvalidCriteriaException;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.sql.SqlConstants;

/**
 * <p>
 * Represents a condition restraining a {@link Field} value to a specified set
 * of values.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/18/12
 */
public class InExpression extends Criterion {

	private static final long serialVersionUID = 1282172886230328002L;

	private Object[] mValues;

	/**
	 * Constructs a new {@code InExpression} with the given {@link Field} name
	 * and array of values.
	 * 
	 * @param fieldName
	 *            the name of the field to check value for
	 * @param values
	 *            the set of values to constrain the {@code Field} to
	 */
	public InExpression(String fieldName, Object[] values) {
		super(fieldName);
		mValues = values;
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
				throw new InvalidCriteriaException(String.format("Invalid Criteria for type '%s'.",
						c.getName()));
			f.setAccessible(true);
		} catch (SecurityException e) {
			throw new InvalidCriteriaException(String.format("Invalid Criteria for type '%s'.",
					c.getName()));
		}
		String colName = policy.getFieldColumnName(f);
		query.append(colName).append(' ').append(SqlConstants.OP_IN).append(" (");
		String prefix = "";
		for (Object val : mValues) {
			query.append(prefix);
			prefix = ", ";
			if (criteria.getObjectMapper().isTextColumn(f))
				query.append("'").append(val.toString()).append("'");
			else
				query.append(val.toString());
		}
		query.append(')');
		return query.toString();
	}

}
