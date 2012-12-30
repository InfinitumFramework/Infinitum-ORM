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

package com.clarionmedia.infinitum.orm.sql;

/**
 * <p>
 * Contains SQL language constants.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/18/12
 */
public class SqlConstants {
	
	// SQL keywords
	public static final String WHERE = "WHERE";
	public static final String LOWER = "LOWER";
	public static final String AND = "AND";
	public static final String OR = "OR";
	public static final String IS_NULL = "IS NULL";
	public static final String IS_NOT_NULL = "IS NOT NULL";
	public static final String NOT_NULL = "NOT NULL";
	public static final String NEGATION = "NOT";
	public static final String LIMIT = "LIMIT";
	public static final String OFFSET = "OFFSET";
	public static final String PRIMARY_KEY = "PRIMARY KEY";
	public static final String AUTO_INCREMENT = "AUTOINCREMENT";
	public static final String UNIQUE = "UNIQUE";
	public static final String NOT_IN = "NOT IN";
	public static final String IN = "IN";
	public static final String UPDATE = "UPDATE";
	public static final String SET = "SET";
	
	// SQL Operators
	public static final String OP_EQUALS = "=";
	public static final String OP_NOT_EQUALS = "<>";
	public static final String OP_GREATER_THAN = ">";
	public static final String OP_LESS_THAN = "<";
	public static final String OP_GREATER_THAN_EQUAL_TO = ">=";
	public static final String OP_LESS_THAN_EQUAL_TO = "<=";
	public static final String OP_LIKE = "LIKE";
	public static final String OP_BETWEEN = "BETWEEN";
	public static final String OP_IN = "IN";
	
	// SQL fragments
	public static final String CREATE_TABLE = "CREATE TABLE";
	public static final String DROP_TABLE = "DROP TABLE IF EXISTS";
	public static final String SELECT_ALL_FROM = "SELECT * FROM ";
	public static final String SELECT_COUNT_FROM = "SELECT count(*) FROM ";
	public static final String ALIASED_SELECT_ALL_FROM = "SELECT %s.* FROM ";
	public static final String DELETE_FROM = "DELETE FROM ";
	public static final String DELETE_FROM_WHERE = "DELETE FROM %s WHERE ";

}
