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

import java.io.Serializable;

import com.clarionmedia.infinitum.context.ContextFactory;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.exception.InvalidCriteriaException;

/**
 * <p>
 * This class represents a query criterion to refine the results of a
 * {@link Criteria} query.
 * </p>
 *
 * @author Tyler Treat
 * @version 1.0 02/17/12
 */
public abstract class Criterion implements Serializable {

    private static final long serialVersionUID = -7011049036451270500L;

    protected String mFieldName;
    protected ContextFactory mContextFactory;

    /**
     * Constructs a new {@code Criterion} with the given {@link Field} name.
     *
     * @param fieldName the name of the {@code Field} to apply the condition to
     */
    public Criterion(String fieldName) {
        mContextFactory = ContextFactory.newInstance();
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
     * Returns the name of the {@link Field} this {@code Criterion} is being
     * applied to.
     *
     * @return name of {@code Field}
     */
    public String getFieldName() {
        return mFieldName;
    }

}
