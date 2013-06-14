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

package com.clarionmedia.infinitum.orm.criteria.criterion;

import java.io.Serializable;

import com.clarionmedia.infinitum.context.ContextFactory;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.exception.InvalidCriteriaException;

/**
 * <p> This class represents a query criterion to refine the results of a {@link Criteria} query. </p>
 *
 * @author Tyler Treat
 * @version 1.1.0 06/12/13
 * @since 1.0
 */
public abstract class Criterion implements Serializable {

    private static final long serialVersionUID = -7011049036451270500L;

    protected String mFieldName;
    protected ContextFactory mContextFactory;
    protected boolean mIgnoreCase;

    /**
     * Constructs a new {@code Criterion} with the given {@link Field} name.
     *
     * @param fieldName the name of the {@code Field} to apply the condition to
     */
    public Criterion(String fieldName) {
        mContextFactory = ContextFactory.getInstance();
        mFieldName = fieldName;
    }

    /**
     * Retrieves the SQL fragment for the {@code Criterion} as a {@link String}.
     *
     * @param criteria the {@link Criteria} this {@code Criterion} belongs to
     * @return SQL {@code String}
     * @throws InvalidCriteriaException if there was a problem creating the {@code Criteria} instance
     */
    public abstract String toSql(Criteria<?> criteria)
            throws InvalidCriteriaException;

    /**
     * Returns the name of the {@link Field} this {@code Criterion} is being applied to.
     *
     * @return name of {@code Field}
     */
    public String getFieldName() {
        return mFieldName;
    }

    /**
     * Sets the {@code Criterion} to ignore case.
     *
     * @return the {@code Criterion} with case ignored
     */
    public Criterion ignoreCase() {
        mIgnoreCase = true;
        return this;
    }

}
