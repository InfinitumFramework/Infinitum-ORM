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

package com.clarionmedia.infinitum.orm.sqlite.impl;

import com.clarionmedia.infinitum.orm.ResultSet;

import android.database.Cursor;

/**
 * <p>
 * This implementation represents a {@link ResultSet} from a SQLite database
 * query. It's essentially a wrapper for a {@link Cursor}, which is what is
 * typically used to represent SQLite relations.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/23/12
 */
public class SqliteResult implements ResultSet {

	private Cursor mCursor;

	/**
	 * Constructs a new {@code SqliteResult} with the given {@link Cursor}.
	 * 
	 * @param cursor
	 *            the SQLite {@code Cursor} to wrap
	 */
	public SqliteResult(Cursor cursor) {
		mCursor = cursor;
	}

	/**
	 * Retrieves the SQLite {@link Cursor} this {@code SqliteResult} is
	 * wrapping.
	 * 
	 * @return {@code Cursor}
	 */
	public Cursor getCursor() {
		return mCursor;
	}

	/**
	 * Sets the SQLite {@link Cursor} which this {@code SqliteResult} wraps.
	 * 
	 * @param cursor
	 *            the {@code Cursor} to wrap
	 */
	public void setCursor(Cursor cursor) {
		mCursor = cursor;
	}

	@Override
	public void close() {
		mCursor.close();
	}

	@Override
	public int getInt(int columnIndex) {
		return mCursor.getInt(columnIndex);
	}

	@Override
	public long getLong(int columnIndex) {
		return mCursor.getLong(columnIndex);
	}

	@Override
	public String getString(int columnIndex) {
		return mCursor.getString(columnIndex);
	}

	@Override
	public float getFloat(int columnIndex) {
		return mCursor.getFloat(columnIndex);
	}

	@Override
	public double getDouble(int columnIndex) {
		return mCursor.getDouble(columnIndex);
	}

	@Override
	public short getShort(int columnIndex) {
		return mCursor.getShort(columnIndex);
	}

	@Override
	public byte[] getBlob(int columnIndex) {
		return mCursor.getBlob(columnIndex);
	}

	@Override
	public int getColumnCount() {
		return mCursor.getColumnCount();
	}

	@Override
	public int getColumnIndex(String columnName) {
		return mCursor.getColumnIndex(columnName);
	}

}
