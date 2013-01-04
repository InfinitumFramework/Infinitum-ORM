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
