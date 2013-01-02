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

package com.clarionmedia.infinitum.orm.sqlite.impl;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.clarionmedia.infinitum.internal.PropertyLoader;
import com.clarionmedia.infinitum.logging.Logger;
import com.clarionmedia.infinitum.orm.context.InfinitumOrmContext;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.sql.SqlBuilder;

/**
 * <p>
 * A helper class to manage database creation and version management. This is an
 * extension of {@link SQLiteOpenHelper} that will take care of opening a
 * database, creating it if it does not exist, and upgrading it if necessary.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/12/12
 * @since 1.0
 */
public class SqliteDbHelper extends SQLiteOpenHelper {

	private SqlBuilder mSqlBuilder;
	private SQLiteDatabase mSqliteDb;
	private InfinitumOrmContext mInfinitumContext;
	private Logger mLogger;
	private PropertyLoader mPropLoader;

	/**
	 * Constructs a new {@code SqliteDbHelper} with the given {@link Context}
	 * and {@link SqliteMapper}.
	 * 
	 * @param context
	 *            the {link InfinitumContext} of the {@code SqliteDbHelper}
	 * @param mapper
	 *            the {@code SqliteMapper} to use for {@link Object} mapping
	 */
	public SqliteDbHelper(InfinitumOrmContext context, SqliteMapper mapper, SqlBuilder sqlBuilder) {
		super(context.getAndroidContext(), context.getSqliteDbName(), null, context.getSqliteDbVersion());
		mLogger = Logger.getInstance(getClass().getSimpleName());
		mInfinitumContext = context;
		mPropLoader = new PropertyLoader(mInfinitumContext.getAndroidContext());
		mSqlBuilder = sqlBuilder;
	}

	/**
	 * Returns an instance of the {@link SQLiteDatabase}.
	 * 
	 * @return the {@code SQLiteDatabase} for this application
	 */
	public SQLiteDatabase getDatabase() {
		return mSqliteDb;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		mSqliteDb = db;
		if (!mInfinitumContext.isSchemaGenerated())
			return;
		mLogger.debug("Creating database tables");
		try {
			mSqlBuilder.createTables(this);
		} catch (ModelConfigurationException e) {
			mLogger.error(mPropLoader.getErrorMessage("CREATE_TABLES_ERROR"), e);
		}
		mLogger.debug("Database tables created successfully");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		mLogger.debug("Upgrading database from version " + oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		mSqliteDb = db;
		mSqlBuilder.dropTables(this);
		mLogger.debug("Database tables dropped successfully");
		onCreate(db);
	}

}
