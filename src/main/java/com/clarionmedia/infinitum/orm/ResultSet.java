/*
 * Copyright (C) 2012 Clarion Media, LLC
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

package com.clarionmedia.infinitum.orm;

/**
 * <p>
 * This interface represents a result set from an SQL database query.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/23/12
 */
public interface ResultSet {

	/**
	 * Closes the {@code ResultSet}.
	 */
	void close();

	/**
	 * Retrieves an {@code int} from the given column index.
	 * 
	 * @param columnIndex
	 *            index of column to retrieve value from
	 * @return {@code int} from {@code ResultSet} column
	 */
	int getInt(int columnIndex);

	/**
	 * Retrieves a {@code long} from the given column index.
	 * 
	 * @param columnIndex
	 *            index of column to retrieve value from
	 * @return {@code long} from {@code ResultSet} column
	 */
	long getLong(int columnIndex);

	/**
	 * Retrieves a {@code String} from the given column index.
	 * 
	 * @param columnIndex
	 *            index of column to retrieve value from
	 * @return {@code String} from {@code ResultSet} column
	 */
	String getString(int columnIndex);

	/**
	 * Retrieves a {@code float} from the given column index.
	 * 
	 * @param columnIndex
	 *            index of column to retrieve value from
	 * @return {@code float} from {@code ResultSet} column
	 */
	float getFloat(int columnIndex);

	/**
	 * Retrieves a {@code double} from the given column index.
	 * 
	 * @param columnIndex
	 *            index of column to retrieve value from
	 * @return {@code double} from {@code ResultSet} column
	 */
	double getDouble(int columnIndex);

	/**
	 * Retrieves a {@code short} from the given column index.
	 * 
	 * @param columnIndex
	 *            index of column to retrieve value from
	 * @return {@code short} from {@code ResultSet} column
	 */
	short getShort(int columnIndex);

	/**
	 * Retrieves a {@code byte[]} from the given column index.
	 * 
	 * @param columnIndex
	 *            index of column to retrieve value from
	 * @return {@code byte[]} from {@code ResultSet} column
	 */
	byte[] getBlob(int columnIndex);

	/**
	 * Retrieves the number of columns contained in the {@code ResultSet}.
	 * 
	 * @return number of columns
	 */
	int getColumnCount();

	/**
	 * Retrieves the column index of the column with the given name.
	 * 
	 * @param columnName
	 *            the name of the column to get the name for
	 * @return column name
	 */
	int getColumnIndex(String columnName);

}
