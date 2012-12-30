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
import com.clarionmedia.infinitum.orm.sql.SqlConstants;
import java.lang.reflect.Field;

/**
 * <p>
 * Provides static factory methods for creating new {@link Criterion} to add to
 * {@link Criteria} queries. {@code Criterion} act as conditions to restrict or
 * refine the result sets of queries. They generally are not constructed
 * directly but rather obtained through this class. However, it is possible to
 * create new types of {@code Criterion} in which case they would need to be
 * instantiated directly.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/18/12
 */
public class Conditions {

	/**
	 * Returns a new {@link Criterion} placing an "equals" condition on the
	 * {@link Field} with the given name.
	 * 
	 * <p>
	 * For example, to query for all models whose {@code Field} "foo" has a
	 * value "bar", calling {@code Conditions.eq("foo", "bar")} would
	 * essentially be the equivalent of doing {@code WHERE foo = 'bar'} in a SQL
	 * query.
	 * </p>
	 * 
	 * @param fieldName
	 *            the name of the {@code Field} to place the restriction on
	 * @param value
	 *            the value of the {@code Field}
	 * @return a {@code Criterion} matching this condition
	 */
	public static BinaryExpression eq(String fieldName, Object value) {
		return new BinaryExpression(fieldName, value, SqlConstants.OP_EQUALS);
	}

	/**
	 * Returns a new {@link Criterion} placing a "not equal" condition on the
	 * {@link Field} with the given name.
	 * 
	 * <p>
	 * For example, to query for all models whose {@code Field} "foo" has a
	 * value other than "bar", calling {@code Conditions.ne("foo", "bar")} would
	 * essentially be the equivalent of doing {@code WHERE foo <> 'bar'} in a
	 * SQL query.
	 * </p>
	 * 
	 * @param fieldName
	 *            the name of the {@code Field} to place the restriction on
	 * @param value
	 *            the value of the {@code Field}
	 * @return a {@code Criterion} matching this condition
	 */
	public static BinaryExpression ne(String fieldName, Object value) {
		return new BinaryExpression(fieldName, value, SqlConstants.OP_NOT_EQUALS);
	}

	/**
	 * Returns a new {@link Criterion} placing a "greater than" condition on the
	 * {@link Field} with the given name.
	 * 
	 * <p>
	 * For example, to query for all models whose {@code Field} "foo" has a
	 * value greater than 42, calling {@code Conditions.gt("foo", 42)} would
	 * essentially be the equivalent of doing {@code WHERE foo > 42} in a SQL
	 * query.
	 * </p>
	 * 
	 * @param fieldName
	 *            the name of the {@code Field} to place the restriction on
	 * @param value
	 *            the value of the {@code Field}
	 * @return a {@code Criterion} matching this condition
	 */
	public static BinaryExpression gt(String fieldName, Object value) {
		return new BinaryExpression(fieldName, value, SqlConstants.OP_GREATER_THAN);
	}

	/**
	 * Returns a new {@link Criterion} placing a "less than" condition on the
	 * {@link Field} with the given name.
	 * 
	 * <p>
	 * For example, to query for all models whose {@code Field} "foo" has a
	 * value less than 42, calling {@code Conditions.lt("foo", 42)} would
	 * essentially be the equivalent of doing {@code WHERE foo < 42} in a SQL
	 * query.
	 * </p>
	 * 
	 * @param fieldName
	 *            the name of the {@code Field} to place the restriction on
	 * @param value
	 *            the value of the {@code Field}
	 * @return a {@code Criterion} matching this condition
	 */
	public static BinaryExpression lt(String fieldName, Object value) {
		return new BinaryExpression(fieldName, value, SqlConstants.OP_LESS_THAN);
	}

	/**
	 * Returns a new {@link Criterion} placing a "greater than or equal to"
	 * condition on the {@link Field} with the given name.
	 * 
	 * <p>
	 * For example, to query for all models whose {@code Field} "foo" has a
	 * value greater than or equal to 42, calling
	 * {@code Conditions.gte("foo", 42)} would essentially be the equivalent of
	 * doing {@code WHERE foo >= 42} in a SQL query.
	 * </p>
	 * 
	 * @param fieldName
	 *            the name of the {@code Field} to place the restriction on
	 * @param value
	 *            the value of the {@code Field}
	 * @return a {@code Criterion} matching this condition
	 */
	public static BinaryExpression gte(String fieldName, Object value) {
		return new BinaryExpression(fieldName, value, SqlConstants.OP_GREATER_THAN_EQUAL_TO);
	}

	/**
	 * Returns a new {@link Criterion} placing a "less than or equal to"
	 * condition on the {@link Field} with the given name.
	 * 
	 * <p>
	 * For example, to query for all models whose {@code Field} "foo" has a
	 * value less than or equal to 42, calling {@code Conditions.lte("foo", 42)}
	 * would essentially be the equivalent of doing {@code WHERE foo <= 42} in a
	 * SQL query.
	 * </p>
	 * 
	 * @param fieldName
	 *            the name of the {@code Field} to place the restriction on
	 * @param value
	 *            the value of the {@code Field}
	 * @return a {@code Criterion} matching this condition
	 */
	public static BinaryExpression lte(String fieldName, Object value) {
		return new BinaryExpression(fieldName, value, SqlConstants.OP_LESS_THAN_EQUAL_TO);
	}

	/**
	 * Returns a new {@link Criterion} placing a "between" condition on the
	 * {@link Field} with the given name.
	 * 
	 * <p>
	 * For example, to query for all models whose {@code Field} "foo" has a
	 * value between 0 and 42, calling {@code Conditions.between("foo", 0, 42)}
	 * would essentially be the equivalent of doing
	 * {@code WHERE foo BETWEEN 0 and 42} in a SQL query.
	 * </p>
	 * 
	 * @param fieldName
	 *            the name of the {@code Field} to place the restriction on
	 * @param value
	 *            the value of the {@code Field}
	 * @return a {@code Criterion} matching this condition
	 */
	public static BetweenExpression between(String fieldName, Object low, Object high) {
		return new BetweenExpression(fieldName, low, high);
	}

	/**
	 * Returns a new {@link Criterion} placing an "in" condition on the
	 * {@link Field} with the given name.
	 * 
	 * <p>
	 * For example, to query for all models whose {@code Field} "foo" has the
	 * value 2, 4, 6, or 8, calling {@code Conditions.in("foo", new Integer[]
	 * 2, 4, 6, 8})} would essentially be the equivalent of doing
	 * {@code WHERE foo IN (2, 4, 6, 8)} in a SQL query.
	 * </p>
	 * 
	 * @param fieldName
	 *            the name of the {@code Field} to place the restriction on
	 * @param value
	 *            the value of the {@code Field}
	 * @return a {@code Criterion} matching this condition
	 */
	public static InExpression in(String fieldName, Object[] values) {
		return new InExpression(fieldName, values);
	}

	/**
	 * Returns a new {@link Criterion} placing a "like" condition on the
	 * {@link Field} with the given name.
	 * 
	 * <p>
	 * For example, to query for all models whose {@code Field} "foo" has a
	 * value containing "bar", calling {@code Conditions.like("foo", "%bar%")}
	 * would essentially be the equivalent of doing
	 * {@code WHERE foo LIKE '%bar%'} in a SQL query.
	 * </p>
	 * 
	 * @param fieldName
	 *            the name of the {@code Field} to place the restriction on
	 * @param value
	 *            the value of the {@code Field}
	 * @return a {@code Criterion} matching this condition
	 */
	public static BinaryExpression like(String fieldName, Object value) {
		return new BinaryExpression(fieldName, value, SqlConstants.OP_LIKE);
	}

	/**
	 * Returns a new {@link Criterion} placing an "is null" condition on the
	 * {@link Field} with the given name.
	 * 
	 * <p>
	 * For example, to query for all models whose {@code Field} "foo" has the
	 * value {@code null}, calling {@code Conditions.isNull("foo")} would
	 * essentially be the equivalent of doing {@code WHERE foo IS NULL} in a SQL
	 * query.
	 * </p>
	 * 
	 * @param fieldName
	 *            the name of the {@code Field} to place the restriction on
	 * @return a {@code Criterion} matching this condition
	 */
	public static NullExpression isNull(String fieldName) {
		return new NullExpression(fieldName);
	}

	/**
	 * Returns a new {@link Criterion} placing an "is not null" condition on the
	 * {@link Field} with the given name.
	 * 
	 * <p>
	 * For example, to query for all models whose {@code Field} "foo" does not
	 * have the value {@code null}, calling {@code Conditions.isNotNull("foo")}
	 * would essentially be the equivalent of doing
	 * {@code WHERE foo IS NOT NULL} in a SQL query.
	 * </p>
	 * 
	 * @param fieldName
	 *            the name of the {@code Field} to place the restriction on
	 * @return a {@code Criterion} matching this condition
	 */
	public static NotNullExpression isNotNull(String fieldName) {
		return new NotNullExpression(fieldName);
	}

	/**
	 * Returns a new {@link Criterion} consisting of the conjunction of the two
	 * given {@code Criterion} expressions.
	 * 
	 * @param lhs
	 *            the left-hand side of the {@link LogicalExpression}
	 * @param rhs
	 *            the right-hand side of the {@code LogicalExpression}
	 * @return Criterion conjunctioned {@code LogicalExpression}
	 */
	public static LogicalExpression and(Criterion lhs, Criterion rhs) {
		return new LogicalExpression(lhs, rhs, SqlConstants.AND);
	}

	/**
	 * Returns a new {@link Criterion} consisting of the disjunction of the two
	 * given {@code Criterion} expressions.
	 * 
	 * @param lhs
	 *            the left-hand side of the {@link LogicalExpression}
	 * @param rhs
	 *            the right-hand side of the {@code LogicalExpression}
	 * @return Criterion disjunctioned {@code LogicalExpression}
	 */
	public static LogicalExpression or(Criterion lhs, Criterion rhs) {
		return new LogicalExpression(lhs, rhs, SqlConstants.OR);
	}

	/**
	 * Returns a new {@link Criterion} consisting of the negation of the given
	 * {@code Criterion} expression.
	 * 
	 * @param expression
	 *            the expression to negate
	 * @return Criterion negated {@code Criterion}
	 */
	public static Criterion not(Criterion expression) {
		return new NotExpression(expression);
	}

}
