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

package com.clarionmedia.infinitum.orm.relationship;

import com.clarionmedia.infinitum.context.ContextFactory;
import com.clarionmedia.infinitum.orm.annotation.ManyToMany;
import com.clarionmedia.infinitum.orm.context.InfinitumOrmContext;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;

import java.lang.reflect.Field;

/**
 * <p>
 * This class encapsulates a many-to-many relationship between two models.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/19/12
 */
public class ManyToManyRelationship extends ModelRelationship {

	private String mTableName;
	private String mFirstFieldName;
	private String mSecondFieldName;
	private PersistencePolicy mPolicy;
	
	public ManyToManyRelationship() {
		mRelationType = RelationType.ManyToMany;
		mPolicy = ContextFactory.getInstance().getContext(InfinitumOrmContext.class).getPersistencePolicy();
	}

	public ManyToManyRelationship(Field f) {
		this();
		ManyToMany mtm = f.getAnnotation(ManyToMany.class);
		mTableName = mtm.table();
		mFirst = f.getDeclaringClass();
		mSecond = mClassReflector.getClass(mtm.className());
		mFirstFieldName = mtm.keyField();
		mSecondFieldName = mtm.foreignField();
		mName = mtm.name();
	}

	public String getTableName() {
		return mTableName;
	}

	public void setTableName(String tableName) {
		mTableName = tableName;
	}

	public String getFirstFieldName() {
		return mFirstFieldName;
	}

	public Field getFirstField() {
		return mPolicy.findPersistentField(mFirst, mFirstFieldName);
	}

	public void setFirstFieldName(String firstField) {
		mFirstFieldName = firstField;
	}

	public String getSecondFieldName() {
		return mSecondFieldName;
	}

	public Field getSecondField() {
		return mPolicy.findPersistentField(mSecond, mSecondFieldName);
	}

	public void setSecondFieldName(String secondField) {
		mSecondFieldName = secondField;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ManyToManyRelationship))
			return false;
		ManyToManyRelationship o = (ManyToManyRelationship) other;
		return mTableName.equalsIgnoreCase(o.mTableName)
				&& ((mFirst == o.mFirst && mSecond == o.mSecond && mFirstFieldName.equalsIgnoreCase(o.mFirstFieldName) && mSecondFieldName
						.equalsIgnoreCase(o.mSecondFieldName)) || mFirst == o.mSecond && mSecond == o.mFirst
						&& mFirstFieldName.equalsIgnoreCase(o.mSecondFieldName)
						&& mSecondFieldName.equalsIgnoreCase(o.mFirstFieldName));
	}

	@Override
	public int hashCode() {
		int hash = 1;
		final int PRIME = 31;
		hash *= PRIME;
		hash *= PRIME + mTableName.hashCode();
		hash *= PRIME + mFirst.hashCode();
		hash *= PRIME + mSecond.hashCode();
		hash *= PRIME + mFirstFieldName.hashCode();
		hash *= PRIME + mSecondFieldName.hashCode();
		return hash;
	}

}
