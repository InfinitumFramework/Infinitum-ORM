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

package com.clarionmedia.infinitum.orm.sql;

/**
 * <p>
 * Contains SQL language constants.
 * </p>
 *
 * @author Tyler Treat
 * @version 1.0.6 04/02/13
 * @since 1.0
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
    public static final String ORDER_BY = "ORDER BY";

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
