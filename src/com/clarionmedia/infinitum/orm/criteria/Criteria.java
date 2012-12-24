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

package com.clarionmedia.infinitum.orm.criteria;

import java.util.List;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.ObjectMapper;
import com.clarionmedia.infinitum.orm.criteria.criterion.Criterion;

/**
 * <p>
 * This interface represents a query for a particular persistent class.
 * {@code Criteria} queries consist of {@link Criterion}, which act as
 * restrictions on a query.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/17/12
 */
public interface Criteria<T> {

	/**
	 * Returns the {@code Criteria} query in SQL form.
	 * 
	 * @return SQL {@link String} for this {@code Criteria}
	 */
	String toSql();

	/**
	 * Returns the {@link Class} associated with this {@code Criteria}.
	 * 
	 * @return {@code Criteria } entity {@code Class}
	 */
	Class<?> getEntityClass();

	/**
	 * Returns the {@link List} of {@link Criterion} for this {@code Criteria}.
	 * 
	 * @return {@code List} of {@code Criterion}
	 */
	List<Criterion> getCriterion();

	/**
	 * Returns the result set limit for this {@code Criteria}.
	 * 
	 * @return result set limit
	 */
	int getLimit();

	/**
	 * Returns the offset value for this {@code Criteria}.
	 * 
	 * @return offset value
	 */
	int getOffset();

	/**
	 * Returns the {@link ObjectMapper} associated with this {@code Criteria}.
	 * 
	 * @return {@code ObjectMapper}
	 */
	ObjectMapper getObjectMapper();

	/**
	 * Adds a {@link Criterion} to filter retrieved query results.
	 * 
	 * @param criterion
	 *            the {@code Criterion} to apply to the {@link Criteria} query
	 * @return this {@code Criteria} to allow for method chaining
	 */
	Criteria<T> add(Criterion criterion);

	/**
	 * Limits the number of query results.
	 * 
	 * @param limit
	 *            max number of entities to retrieve
	 * @return this {@code Criteria} to allow for method chaining
	 */
	Criteria<T> limit(int limit);

	/**
	 * Offsets the result set by the given amount.
	 * 
	 * @param offset
	 *            amount to offset results
	 * @return this {@code Criteria} to allow for method chaining
	 */
	Criteria<T> offset(int offset);

	/**
	 * Retrieves the query results as a {@link List}.
	 * 
	 * @return query results in {@code List} form
	 */
	List<T> list();

	/**
	 * Retrieves a unique query result for the {@code Criteria} query.
	 * 
	 * @return unique query result or {@code null} if no such result exists
	 * @throws InfinitumRuntimeException
	 *             if there was not a unique result for the query
	 */
	T unique() throws InfinitumRuntimeException;

	/**
	 * Retrieves the number of results for the {@code Criteria} query.
	 * 
	 * @return number of results for the query
	 */
	long count();

}
