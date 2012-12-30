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

package com.clarionmedia.infinitum.orm.relationship;

import java.lang.reflect.Field;

import com.clarionmedia.infinitum.context.ContextFactory;
import com.clarionmedia.infinitum.orm.annotation.ManyToMany;
import com.clarionmedia.infinitum.orm.context.InfinitumOrmContext;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;

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
        mPolicy = ContextFactory.newInstance().getContext(InfinitumOrmContext.class).getPersistencePolicy();
    }

    public ManyToManyRelationship(Field f) {
        this();
        ManyToMany mtm = f.getAnnotation(ManyToMany.class);
        mTableName = mtm.table();
        mFirst = f.getDeclaringClass();
        mSecond = mPackageReflector.getClass(mtm.className());
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
