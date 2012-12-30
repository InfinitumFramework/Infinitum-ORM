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

package com.clarionmedia.infinitum.orm.persistence.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.internal.StringUtil;
import com.clarionmedia.infinitum.orm.OrmConstants.PersistenceMode;
import com.clarionmedia.infinitum.orm.annotation.Column;
import com.clarionmedia.infinitum.orm.annotation.Entity;
import com.clarionmedia.infinitum.orm.annotation.ManyToMany;
import com.clarionmedia.infinitum.orm.annotation.ManyToOne;
import com.clarionmedia.infinitum.orm.annotation.NotNull;
import com.clarionmedia.infinitum.orm.annotation.OneToMany;
import com.clarionmedia.infinitum.orm.annotation.OneToOne;
import com.clarionmedia.infinitum.orm.annotation.Persistence;
import com.clarionmedia.infinitum.orm.annotation.PrimaryKey;
import com.clarionmedia.infinitum.orm.annotation.Rest;
import com.clarionmedia.infinitum.orm.annotation.Table;
import com.clarionmedia.infinitum.orm.annotation.Unique;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.relationship.ManyToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.ManyToOneRelationship;
import com.clarionmedia.infinitum.orm.relationship.ModelRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToOneRelationship;

/**
 * <p>
 * This class provides runtime resolution for model persistence through Java
 * annotations. Model fields can be marked as transient or persistent using the
 * {@link Persistence} annotation. If no annotation is provided, the field will
 * be marked as persistent by default.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/12/12
 * @since 1.0
 * @see XmlPersistencePolicy
 */
public class AnnotationsPersistencePolicy extends PersistencePolicy {

	@Override
	public boolean isPersistent(Class<?> clazz) {
		Entity entity = clazz.getAnnotation(Entity.class);
		if (entity == null || entity.mode() == PersistenceMode.Persistent)
			return true;
		else
			return false;
	}

	@Override
	public String getModelTableName(Class<?> c) {
		if (!isPersistent(c) || !mTypePolicy.isDomainModel(c))
			throw new IllegalArgumentException("Class '" + c.getName()
					+ "' is transient.");
		String ret;
		Table table = c.getAnnotation(Table.class);
		if (table == null) {
			if (mTypePolicy.isDomainProxy(c)) {
				ret = c.getName();
				ret = ret.substring(0, ret.lastIndexOf("_Proxy")).toLowerCase();
			} else {
				ret = c.getSimpleName().toLowerCase();
			}
		} else {
			ret = table.value();
		}
		return ret;
	}

	@Override
	public List<Field> getPersistentFields(Class<?> c) {
		if (mPersistenceCache.containsKey(c))
			return mPersistenceCache.get(c);
		List<Field> ret = new ArrayList<Field>();
		List<Field> fields = mClassReflector.getAllFields(c);
		for (Field f : fields) {
			if (Modifier.isStatic(f.getModifiers())
					|| mTypePolicy.isDomainProxy(f.getDeclaringClass()))
				continue;
			Persistence persistence = f.getAnnotation(Persistence.class);
			PrimaryKey pk = f.getAnnotation(PrimaryKey.class);
			if ((persistence == null || persistence.value() == PersistenceMode.Persistent)
					|| pk != null)
				ret.add(f);
		}
		mPersistenceCache.put(c, ret);
		return ret;
	}

	@Override
	public Field getPrimaryKeyField(Class<?> c)
			throws ModelConfigurationException {
		if (mPrimaryKeyCache.containsKey(c))
			return mPrimaryKeyCache.get(c);
		Field ret = null;
		boolean found = false;
		List<Field> fields = getPersistentFields(c);
		for (Field f : fields) {
			PrimaryKey pk = f.getAnnotation(PrimaryKey.class);
			if (pk != null && !found) {
				ret = f;
				found = true;
			} else if (pk != null && found) {
				throw new ModelConfigurationException(String.format(
						mPropLoader.getErrorMessage("MULTIPLE_PK_ERROR"),
						c.getName()));
			}
		}
		// Look for id fields if the annotation is missing
		if (ret == null) {
			Field f = findPrimaryKeyField(c);
			if (f != null)
				ret = f;
		}

		if (ret == null)
			return null;
		mPrimaryKeyCache.put(c, ret);
		return ret;
	}

	@Override
	public String getFieldColumnName(Field f) {
		if (mColumnCache.containsKey(f))
			return mColumnCache.get(f);
		String ret;
		Column c = f.getAnnotation(Column.class);
		if (c == null) {
			ret = StringUtil.formatFieldName(f.getName());
			if (f.isAnnotationPresent(ManyToOne.class))
				ret = new ManyToOneRelationship(f).getColumn();
			else if (f.isAnnotationPresent(OneToOne.class))
				ret = new OneToOneRelationship(f).getColumn();
		} else {
			ret = c.value();
		}
		mColumnCache.put(f, ret);
		return ret;
	}

	@Override
	public boolean isFieldPrimaryKey(Field f) {
		return f.equals(getPrimaryKeyField(f.getDeclaringClass()));
	}

	@Override
	public boolean isPrimaryKeyAutoIncrement(Field f)
			throws InfinitumRuntimeException {
		PrimaryKey pk = f.getAnnotation(PrimaryKey.class);
		if (pk == null) {
			if (f.getType() == int.class || f.getType() == Integer.class
					|| f.getType() == long.class || f.getType() == Long.class)
				return true;
			else
				return false;
		}
		boolean ret = pk.autoincrement();
		if (!ret)
			return false;
		// throw runtime exception if explicit PK is not an int or long
		if (f.getType() == int.class || f.getType() == Integer.class
				|| f.getType() == long.class || f.getType() == Long.class)
			return true;
		else
			throw new InfinitumRuntimeException(String.format(
					mPropLoader.getErrorMessage("EXPLICIT_PK_TYPE_ERROR"),
					f.getName(), f.getDeclaringClass().getName()));
	}

	@Override
	public boolean isFieldNullable(Field f) {
		if (mFieldNullableCache.containsKey(f))
			return mFieldNullableCache.get(f);
		boolean ret;
		ret = !f.isAnnotationPresent(NotNull.class);
		mFieldNullableCache.put(f, ret);
		return ret;
	}

	@Override
	public boolean isFieldUnique(Field f) {
		if (mFieldUniqueCache.containsKey(f))
			return mFieldUniqueCache.get(f);
		if (f.isAnnotationPresent(OneToOne.class)) {
			OneToOne oto = f.getAnnotation(OneToOne.class);
			if (oto.owner() == f.getDeclaringClass()) {
				mFieldUniqueCache.put(f, true);
				return true;
			}
		}
		boolean unique = f.isAnnotationPresent(Unique.class);
		mFieldUniqueCache.put(f, unique);
		return unique;
	}

	@Override
	public Set<ManyToManyRelationship> getManyToManyRelationships(
			Class<?> c) {
		if (!isPersistent(c))
			throw new IllegalArgumentException("Class '" + c.getName()
					+ "' is transient.");
		Set<ManyToManyRelationship> ret = new HashSet<ManyToManyRelationship>();
		for (ManyToManyRelationship r : mManyToManyCache.values()) {
			if (r.contains(c))
				ret.add(r);
		}
		if (ret.size() > 0)
			return ret;
		List<Field> fields = getPersistentFields(c);
		for (Field f : fields) {
			if (!f.isAnnotationPresent(ManyToMany.class))
				continue;
			ManyToManyRelationship rel = new ManyToManyRelationship(f);
			mManyToManyCache.put(f, rel);
			ret.add(rel);
		}
		return ret;
	}

	@Override
	public Cascade getCascadeMode(Class<?> c) {
		if (!c.isAnnotationPresent(Entity.class))
			return Cascade.ALL;
		Entity entity = c.getAnnotation(Entity.class);
		return entity.cascade();
	}

	@Override
	public boolean isRelationship(Field f) {
		return f.isAnnotationPresent(ManyToMany.class)
				|| f.isAnnotationPresent(ManyToOne.class)
				|| f.isAnnotationPresent(OneToMany.class)
				|| f.isAnnotationPresent(OneToOne.class);
	}

	@Override
	public boolean isManyToManyRelationship(Field f) {
		return f.isAnnotationPresent(ManyToMany.class);
	}
	
	@Override
	public boolean isOneToOneRelationship(Field f) {
		return f.isAnnotationPresent(OneToOne.class);
	}

	@Override
	public boolean isToOneRelationship(Field f) {
		return f.isAnnotationPresent(ManyToOne.class)
				|| f.isAnnotationPresent(OneToOne.class);
	}

	@Override
	public ModelRelationship getRelationship(Field f) {
		if (f.isAnnotationPresent(ManyToMany.class))
			return new ManyToManyRelationship(f);
		if (f.isAnnotationPresent(ManyToOne.class))
			return new ManyToOneRelationship(f);
		if (f.isAnnotationPresent(OneToMany.class))
			return new OneToManyRelationship(f);
		if (f.isAnnotationPresent(OneToOne.class))
			return new OneToOneRelationship(f);
		return null;
	}

	@Override
	public Field findRelationshipField(Class<?> c, ModelRelationship rel) {
		for (Field f : getPersistentFields(c)) {
			f.setAccessible(true);
			if (!isRelationship(f))
				continue;
			switch (rel.getRelationType()) {
				case ManyToMany :
					ManyToMany mtm = f.getAnnotation(ManyToMany.class);
					if (rel.getName().equalsIgnoreCase(mtm.name()))
						return f;
					break;
				case ManyToOne :
					ManyToOne mto = f.getAnnotation(ManyToOne.class);
					if (rel.getName().equalsIgnoreCase(mto.name()))
						return f;
					break;
				case OneToMany :
					OneToMany otm = f.getAnnotation(OneToMany.class);
					if (rel.getName().equalsIgnoreCase(otm.name()))
						return f;
					break;
				case OneToOne :
					OneToOne oto = f.getAnnotation(OneToOne.class);
					if (rel.getName().equalsIgnoreCase(oto.name()))
						return f;
			}
		}
		return null;
	}

	@Override
	public boolean isLazy(Class<?> c) {
		if (mLazyLoadingCache.containsKey(c))
			return mLazyLoadingCache.get(c);
		boolean ret;
		if (!c.isAnnotationPresent(Entity.class)) {
			ret = true;
		} else {
			Entity entity = c.getAnnotation(Entity.class);
			ret = entity.lazy();
		}
		mLazyLoadingCache.put(c, ret);
		return ret;
	}

	@Override
	public String getRestEndpoint(Class<?> c)
			throws IllegalArgumentException {
		if (!isPersistent(c) || !mTypePolicy.isDomainModel(c))
			throw new IllegalArgumentException();
		if (mRestEndpointCache.containsKey(c))
			return mRestEndpointCache.get(c);
		String ret;
		if (!c.isAnnotationPresent(Entity.class)) {
			ret = c.getSimpleName().toLowerCase();
		} else {
			Entity entity = c.getAnnotation(Entity.class);
			ret = entity.endpoint();
			if (ret.equals(""))
				ret = c.getSimpleName().toLowerCase();
		}
		mRestEndpointCache.put(c, ret);
		return ret;
	}

	@Override
	public String getEndpointFieldName(Field f)
			throws IllegalArgumentException {
		if (!isPersistent(f.getDeclaringClass())
				|| !mTypePolicy.isDomainModel(f.getDeclaringClass()))
			throw new IllegalArgumentException();
		if (f.isAnnotationPresent(Persistence.class)) {
			Persistence p = f.getAnnotation(Persistence.class);
			if (p.value() == PersistenceMode.Transient)
				throw new IllegalArgumentException();
		}
		if (mRestFieldCache.containsKey(f))
			return mRestFieldCache.get(f);
		String ret;
		if (!f.isAnnotationPresent(Rest.class)) {
			ret = StringUtil.formatFieldName(f.getName());
		} else {
			Rest rest = f.getAnnotation(Rest.class);
			ret = rest.value();
		}
		mRestFieldCache.put(f, ret);
		return ret;
	}

}
