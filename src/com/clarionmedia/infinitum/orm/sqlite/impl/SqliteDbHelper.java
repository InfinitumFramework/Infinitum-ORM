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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.clarionmedia.infinitum.di.annotation.Autowired;
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
 * 
 */
public class SqliteDbHelper extends SQLiteOpenHelper {

	@Autowired
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
	@Autowired
	public SqliteDbHelper(InfinitumOrmContext context, SqliteMapper mapper) {
		super(context.getAndroidContext(), context.getSqliteDbName(), null, context.getSqliteDbVersion());
		mLogger = Logger.getInstance(context, getClass().getSimpleName());
		mInfinitumContext = context;
		mPropLoader = new PropertyLoader(mInfinitumContext.getAndroidContext());
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
