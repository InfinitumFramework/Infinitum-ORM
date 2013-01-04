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

package com.clarionmedia.infinitum.orm.criteria;

import java.util.List;

import android.database.Cursor;

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
 * @since 1.0
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

	/**
	 * Retrieves the database {@link Cursor} for the {@code Criteria} query.
	 * 
	 * @return query {@code Cursor}
	 */
	Cursor cursor();

}
