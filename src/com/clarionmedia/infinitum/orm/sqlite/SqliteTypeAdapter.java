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
