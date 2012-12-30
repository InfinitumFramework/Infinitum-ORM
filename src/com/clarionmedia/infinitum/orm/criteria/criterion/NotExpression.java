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

import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.exception.InvalidCriteriaException;
import com.clarionmedia.infinitum.orm.sql.SqlConstants;

/**
 * <p>
 * Represents a negation of a {@link Criterion} expression.
 * </p>
 *
 * @author Tyler Treat
 * @version 1.0 02/18/12
 */
public class NotExpression extends Criterion {

    private static final long serialVersionUID = 2819651961490738355L;

    private Criterion mExpression;

    public NotExpression(Criterion expression) {
        super(null);
        mExpression = expression;
    }

    @Override
    public String toSql(Criteria<?> criteria) throws InvalidCriteriaException {
        return new StringBuilder(SqlConstants.NEGATION).append(" (").append(mExpression.toSql(criteria)).append(')')
                .toString();
    }

}
