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

package com.clarionmedia.infinitum.orm.sqlite;

import android.content.ContentValues;
import com.clarionmedia.infinitum.orm.persistence.TypeAdapter;
import com.clarionmedia.infinitum.orm.persistence.TypeResolutionPolicy.SqliteDataType;

/**
 * <p>
 * Facilitates the mapping of Java data types to columns in a SQLite database
 * and vice versa.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/17/12
 */
public abstract class SqliteTypeAdapter<T> implements TypeAdapter<T> {

	private SqliteDataType mSqliteType;

	/**
	 * Creates a new {@code SqliteTypeAdapter} pertaining to the given
	 * {@link SqliteDataType}.
	 * 
	 * @param dataType
	 *            the {@code SqliteDataType} of the column this type maps to
	 */
	public SqliteTypeAdapter(SqliteDataType dataType) {
		mSqliteType = dataType;
	}

	/**
	 * Maps the given value to the given column.
	 * 
	 * @param value
	 *            the value being mapped
	 * @param column
	 *            the column being mapped to
	 * @param values
	 *            the {@link ContentValues} containing the data mappings for the
	 *            entire row
	 */
	public abstract void mapToColumn(T value, String column, ContentValues values);

	/**
	 * Maps the given {@link Object} value to the given column.
	 * 
	 * @param value
	 *            the value being mapped
	 * @param column
	 *            the column being mapped to
	 * @param values
	 *            the {@link ContentValues} containing the data mappings for the
	 *            entire row
	 */
	public abstract void mapObjectToColumn(Object value, String column, ContentValues values);

	/**
	 * Sets the {@link SqliteDataType} for this {@code SqliteTypeAdapter}. This
	 * value indicates the data type of the column being mapped to.
	 * 
	 * @param sqliteType
	 *            the {@code SqliteDataType} of column
	 */
	public void setSqliteType(SqliteDataType sqliteType) {
		mSqliteType = sqliteType;
	}

	/**
	 * Returns the {@link SqliteDataType} for this {@code SqliteTypeAdapter}.
	 * This value indicates the data type of the column being mapped to.
	 * 
	 * @return {@code SqliteDataType} of column
	 */
	public SqliteDataType getSqliteType() {
		return mSqliteType;
	}

}
