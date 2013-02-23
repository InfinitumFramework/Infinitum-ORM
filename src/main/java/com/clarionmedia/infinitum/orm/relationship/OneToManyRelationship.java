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

import java.lang.reflect.Field;

import com.clarionmedia.infinitum.orm.annotation.OneToMany;

/**
 * <p>
 * This class encapsulates a one-to-many relationship between two models.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/03/12
 */
public class OneToManyRelationship extends ForeignKeyRelationship {
	
	private String mColumn;
	private Class<?> mOneType;
	private Class<?> mManyType;
	
	public OneToManyRelationship() {
		mRelationType = RelationType.OneToMany;
	}

	public OneToManyRelationship(Field f) {
		this();
		OneToMany otm = f.getAnnotation(OneToMany.class);
		mFirst = f.getDeclaringClass();
		setOneType(mFirst);
		mSecond = mClassReflector.getClass(otm.className());
		setManyType(mSecond);
		setOwner(mSecond);
		mName = otm.name();
		mColumn = otm.column();
	}

	public void setColumn(String mColumn) {
		this.mColumn = mColumn;
	}

	public String getColumn() {
		return mColumn;
	}

	public void setOneType(Class<?> mOneType) {
		this.mOneType = mOneType;
	}

	public Class<?> getOneType() {
		return mOneType;
	}

	public void setManyType(Class<?> mManyType) {
		this.mManyType = mManyType;
	}

	public Class<?> getManyType() {
		return mManyType;
	}

}
