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
