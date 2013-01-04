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

package com.clarionmedia.infinitum.orm.relationship;

/**
 * <p>
 * This abstract class represents a relationship which relies on a foreign key
 * to make the entity association, such as many-to-one, one-to-many, and
 * one-to-one relationships.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/11/12
 * @since 1.0
 */
public abstract class ForeignKeyRelationship extends ModelRelationship {

	protected String mColumn;
	protected Class<?> mOwner;

	public String getColumn() {
		return mColumn;
	}

	public void setColumn(String column) {
		mColumn = column;
	}
	
	public Class<?> getOwner() {
		return mOwner;
	}
	
	public void setOwner(Class<?> owner) {
		mOwner = owner;
	}

}
