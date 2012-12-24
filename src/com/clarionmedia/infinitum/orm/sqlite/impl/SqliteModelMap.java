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

import android.content.ContentValues;
import com.clarionmedia.infinitum.orm.ModelMap;

/**
 * <p>
 * Concrete implementation of {@link ModelMap} representing a domain model
 * instance mapped to a SQLite table.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/23/12
 */
public class SqliteModelMap extends ModelMap {

	private ContentValues mContentValues;

	/**
	 * Constructs a new {@code SqliteModelMap} for the given model.
	 * 
	 * @param model
	 *            the model to map
	 */
	public SqliteModelMap(Object model) {
		super(model);
	}

	/**
	 * Retrieves the {@link ContentValues} for this {@code SqliteModelMap}.
	 * 
	 * @return {@code ContentValues} containing mapped values
	 */
	public ContentValues getContentValues() {
		return mContentValues;
	}

	/**
	 * Sets the {@link ContentValues} for this {@code SqliteModelMap}.
	 * 
	 * @param contentValues
	 *            the {@code ContentValues} to set
	 */
	public void setContentValues(ContentValues contentValues) {
		mContentValues = contentValues;
	}

}
