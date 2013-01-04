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
import com.clarionmedia.infinitum.orm.annotation.ManyToOne;

/**
 * <p>
 * This class encapsulates a many-to-one relationship between two models.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/03/12
 */
public class ManyToOneRelationship extends ForeignKeyRelationship {
	
	public ManyToOneRelationship() {
		mRelationType = RelationType.ManyToOne;
	}

	public ManyToOneRelationship(Field f) {
		this();
		ManyToOne mto = f.getAnnotation(ManyToOne.class);
		mFirst = f.getDeclaringClass();
		mSecond = mPackageReflector.getClass(mto.className());
		mName = mto.name();
		setOwner(f.getDeclaringClass());
		setColumn(mto.column());
	}

}
