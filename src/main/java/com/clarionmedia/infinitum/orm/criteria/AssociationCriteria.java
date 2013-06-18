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

package com.clarionmedia.infinitum.orm.criteria;

import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.criteria.criterion.Criterion;
import com.clarionmedia.infinitum.orm.relationship.ModelRelationship;

import java.lang.reflect.Field;
import java.util.List;

/**
 * <p> Specialization of {@link Criteria} for querying on entity associations. </p>
 *
 * @author Tyler Treat
 * @version 1.1.0 06/17/13
 * @since 1.0
 */
public interface AssociationCriteria<T> extends Criteria<T> {

    /**
     * Returns the {@link ModelRelationship} being queried on.
     *
     * @return {@code ModelRelationship}
     */
    ModelRelationship getRelationship();

    /**
     * Returns the {@link Field} representing the relationship being queried on
     *
     * @return association {@code Field}
     */
    Field getRelationshipField();

    /**
     * Retrieves the root query results as a {@link List}.
     *
     * @param type the type of the results to return
     * @return query results in {@code List} form
     */
    <E> List<E> list(Class<E> type);

    /**
     * Adds a {@link Criterion} to filter retrieved query results.
     *
     * @param criterion the {@code Criterion} to apply to the {@link Criteria} query
     * @return this {@code AssociationCriteria} to allow for method chaining
     */
    AssociationCriteria<T> add(Criterion criterion);

    /**
     * Limits the number of query results.
     *
     * @param limit max number of entities to retrieve
     * @return this {@code AssociationCriteria} to allow for method chaining
     */
    AssociationCriteria<T> limit(int limit);

    /**
     * Offsets the result set by the given amount.
     *
     * @param offset amount to offset results
     * @return this {@code AssociationCriteria} to allow for method chaining
     */
    AssociationCriteria<T> offset(int offset);

    /**
     * Retrieves a unique query result for the root {@code Criteria} query.
     *
     * @return unique query result or {@code null} if no such result exists
     * @throws InfinitumRuntimeException if there was not a unique result for the query
     */
    <E> E unique(Class<E> type) throws InfinitumRuntimeException;

}
