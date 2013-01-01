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

import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.exception.InvalidCriteriaException;

/**
 * <p>
 * Represents a compound Boolean logical expression consisting of two
 * {@link Criterion} expressions and a logical operator.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/18/12
 */
public class LogicalExpression extends Criterion {

	private static final long serialVersionUID = 2819651961490738355L;

	private Criterion mLhs;
	private Criterion mRhs;
	private String mOperator;

	public LogicalExpression(Criterion lhs, Criterion rhs, String operator) {
		super(null);
		mLhs = lhs;
		mRhs = rhs;
		mOperator = operator;
	}

	@Override
	public String toSql(Criteria<?> criteria) throws InvalidCriteriaException {
		return new StringBuilder("(").append(mLhs.toSql(criteria)).append(' ').append(mOperator).append(' ')
				.append(mRhs.toSql(criteria)).append(')').toString();
	}

}
