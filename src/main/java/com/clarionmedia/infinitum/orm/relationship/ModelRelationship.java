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

import com.clarionmedia.infinitum.reflection.ClassReflector;
import com.clarionmedia.infinitum.reflection.impl.JavaClassReflector;


/**
 * <p>
 * This abstract class is used to define a relationship between two domain
 * entities.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/25/12
 */
public abstract class ModelRelationship {

	public static enum RelationType {
		ManyToMany, ManyToOne, OneToMany, OneToOne
	};

	protected Class<?> mFirst;
	protected Class<?> mSecond;
	protected RelationType mRelationType;
	protected String mName;
	protected ClassReflector mClassReflector;
	
	public ModelRelationship() {
		mClassReflector = new JavaClassReflector();
	}

	public Class<?> getFirstType() {
		return mFirst;
	}

	public void setFirstType(Class<?> first) {
		mFirst = first;
	}

	public Class<?> getSecondType() {
		return mSecond;
	}

	public void setSecondType(Class<?> second) {
		mSecond = second;
	}

	public boolean contains(Class<?> c) {
		return mFirst == c || mSecond == c;
	}

	public RelationType getRelationType() {
		return mRelationType;
	}

	public void setRelationType(RelationType type) {
		mRelationType = type;
	}
	
	public String getName() {
		return mName;
	}
	
	public void setName(String name) {
		mName = name;
	}

}
