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

package com.clarionmedia.infinitum.orm;

import java.io.Serializable;

import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.exception.SQLGrammarException;

/**
 * <p>
 * This interface specifies methods for interacting with a datastore. This is
 * not typically used directly but allows for greater testability.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/15/12
 */
public interface DatastoreOperations {

	/**
	 * Constructs a new {@link Criteria} for the given entity {@link Class}.
	 * 
	 * @param entityClass
	 *            the {@code Class} to create the {@code Criteria} for
	 * @return new {@code Criteria}
	 */
	<T> Criteria<T> createCriteria(Class<T> entityClass);

	/**
	 * Persists the given {@link Object} to the database. This method is
	 * not idempotent, meaning if the record already exists, a new one will
	 * attempt to persist.
	 * 
	 * @param model
	 *            {@code Object} to persist to the database
	 * @return the row ID of the newly inserted record, or -1 if the insert
	 *         failed
	 * @throws InfinitumRuntimeException
	 *             if the model is marked transient
	 */
	long save(Object model) throws InfinitumRuntimeException;

	/**
	 * Updates the given {@link Object} in the database.
	 * 
	 * @param model
	 *            {@code Object} to update in the database
	 * @return {@code true} if the updated succeeded, {@code false} if it failed
	 * @throws InfinitumRuntimeException
	 *             if the model is marked transient
	 */
	boolean update(Object model) throws InfinitumRuntimeException;

	/**
	 * Deletes the given {@link Object} from the database if it exists.
	 * 
	 * @param model
	 *            {@code Object} to delete from the database
	 * @return true if the record was deleted, false otherwise
	 * @throws InfinitumRuntimeException
	 *             if the model is marked transient
	 */
	boolean delete(Object model) throws InfinitumRuntimeException;

	/**
	 * Persists the given {@link Object} to the database, or, if it already
	 * exists, updates the record.
	 * 
	 * @param model
	 *            {@code Object} to save or update in the database
	 * @return the row ID of the newly inserted row, 0 if the row was updated, or -1 if the operation failed
	 * @throws InfinitumRuntimeException
	 *             if the model is marked transient
	 */
	long saveOrUpdate(Object model) throws InfinitumRuntimeException;

	/**
	 * Returns an instance of the given persistent model {@link Class} as
	 * identified by the specified primary key(s) or {@code null} if no such
	 * entity exists.
	 * 
	 * @param c
	 *            the {@code Class} of the persistent instance to load
	 * @param id
	 *            the primary key value of the persistent instance to load
	 * @return the persistent instance
	 * @throws InfinitumRuntimeException
	 *             if the specified {@code Class} is marked transient
	 * @throws IllegalArgumentException
	 *             if an incorrect number of primary keys is provided
	 */
	<T extends Object> T load(Class<T> c, Serializable id) throws InfinitumRuntimeException, IllegalArgumentException;

	/**
	 * Executes the given SQL non-query on the database, meaning no result is
	 * expected.
	 * 
	 * @param sql
	 *            the SQL query to execute
	 * @throws SQLGrammarException
	 *             if the SQL was formatted incorrectly
	 */
	void execute(String sql) throws SQLGrammarException;

}
