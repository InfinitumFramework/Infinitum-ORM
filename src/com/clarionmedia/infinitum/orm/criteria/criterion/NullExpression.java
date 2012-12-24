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

package com.clarionmedia.infinitum.orm.criteria.criterion;

import java.lang.reflect.Field;

import com.clarionmedia.infinitum.orm.context.OrmContext;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.exception.InvalidCriteriaException;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.sql.SqlConstants;

/**
 * <p>
 * Represents a condition restraining a {@link Field} value to {@code null}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/18/12
 */
public class NullExpression extends Criterion {

	private static final long serialVersionUID = 1282172886230328002L;

	/**
	 * Constructs a new {@code NullExpression} with the given {@link Field}.
	 * 
	 * @param fieldName
	 *            the name of the field to check {@code null} for
	 */
	public NullExpression(String fieldName) {
		super(fieldName);
	}

	@Override
	public String toSql(Criteria<?> criteria) throws InvalidCriteriaException {
		StringBuilder query = new StringBuilder();
		Class<?> c = criteria.getEntityClass();
		Field f = null;
		PersistencePolicy policy = ((OrmContext) mContextFactory.getContext()).getPersistencePolicy();
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
		query.append(colName).append(' ').append(SqlConstants.IS_NULL);
		return query.toString();
	}

}
