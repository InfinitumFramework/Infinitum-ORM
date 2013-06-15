/*
 * Copyright (C) 2013 Clarion Media, LLC
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

import com.clarionmedia.infinitum.orm.annotation.ManyToOne;

import java.lang.reflect.Field;

/**
 * <p> This class encapsulates a many-to-one relationship between two models. </p>
 *
 * @author Tyler Treat
 * @version 1.1.0 06/15/13
 * @since 1.0
 */
public class ManyToOneRelationship extends ForeignKeyRelationship {

    public ManyToOneRelationship() {
        mRelationType = RelationType.ManyToOne;
    }

    public ManyToOneRelationship(Field f) {
        this();
        ManyToOne mto = f.getAnnotation(ManyToOne.class);
        mFirst = f.getDeclaringClass();
        mSecond = f.getType();
        mName = mto.name();
        setOwner(f.getDeclaringClass());
        setColumn(mto.column());
    }

}
