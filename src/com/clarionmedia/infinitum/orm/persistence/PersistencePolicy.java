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

package com.clarionmedia.infinitum.orm.persistence;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.clarionmedia.infinitum.aop.AopProxy;
import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.di.annotation.Autowired;
import com.clarionmedia.infinitum.di.annotation.PostConstruct;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.internal.PropertyLoader;
import com.clarionmedia.infinitum.logging.Logger;
import com.clarionmedia.infinitum.orm.exception.InvalidMapFileException;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.impl.AnnotationsPersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.impl.XmlPersistencePolicy;
import com.clarionmedia.infinitum.orm.relationship.ManyToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.ManyToOneRelationship;
import com.clarionmedia.infinitum.orm.relationship.ModelRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToOneRelationship;
import com.clarionmedia.infinitum.reflection.ClassReflector;

/**
 * <p>
 * Provides a runtime resolution policy for model persistence based on the
 * Infinitum configuration. There are two types of persistence policies:
 * annotation and XML.
 * </p>
 * <p>
 * Domain classes should be individually registered in {@code infinitum.cfg.xml}
 * using {@code <model resource="com.foo.domain.MyModel" />} in the
 * {@code domain} element.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 05/09/12
 * @since 1.0
 * @see AnnotationsPersistencePolicy
 * @see XmlPersistencePolicy
 */
public abstract class PersistencePolicy {

	/**
	 * Used to indicate an entity's cascade mode. {@code All} means every
	 * related entity is cascaded, {@code None} means no related entities are
	 * cascaded, and {@code Keys} means only foreign keys will be cascaded.
	 */
	public static enum Cascade {
		ALL, NONE, KEYS
	};

	// This Map caches which fields are persistent
	protected Map<Class<?>, List<Field>> mPersistenceCache;

	// This Map caches the field-column map
	protected Map<Field, String> mColumnCache;

	// This Map caches the primary key Field for each persistent class
	protected Map<Class<?>, Field> mPrimaryKeyCache;

	// This Map caches the "nullability" of Fields
	protected Map<Field, Boolean> mFieldNullableCache;

	// This Map caches the uniqueness of Fields
	protected Map<Field, Boolean> mFieldUniqueCache;

	// This Map caches the many-to-many relationships
	protected Map<Field, ManyToManyRelationship> mManyToManyCache;

	// This Map caches the many-to-one relationships
	protected Map<Field, ManyToOneRelationship> mManyToOneCache;

	// This Map caches the one-to-many relationships
	protected Map<Field, OneToManyRelationship> mOneToManyCache;

	// This Map caches the one-to-one relationships
	protected Map<Field, OneToOneRelationship> mOneToOneCache;

	// This Map caches the lazy-loading status for each persistent class
	protected Map<Class<?>, Boolean> mLazyLoadingCache;

	// This Map caches the endpoint names for models
	protected Map<Class<?>, String> mRestEndpointCache;

	// This Map caches the endpoint field names for model Fields
	protected Map<Field, String> mRestFieldCache;

	@Autowired
	protected TypeResolutionPolicy mTypePolicy;
	
	@Autowired
	protected ClassReflector mClassReflector;
	
	@Autowired
	protected InfinitumContext mContext;
	
	protected Logger mLogger;
	protected PropertyLoader mPropLoader;

	/**
	 * Constructs a new {@code PersistencePolicy}.
	 */
	public PersistencePolicy() {
		mPersistenceCache = new HashMap<Class<?>, List<Field>>();
		mColumnCache = new HashMap<Field, String>();
		mPrimaryKeyCache = new HashMap<Class<?>, Field>();
		mFieldNullableCache = new HashMap<Field, Boolean>();
		mFieldUniqueCache = new HashMap<Field, Boolean>();
		mManyToManyCache = new HashMap<Field, ManyToManyRelationship>();
		mManyToOneCache = new HashMap<Field, ManyToOneRelationship>();
		mOneToManyCache = new HashMap<Field, OneToManyRelationship>();
		mOneToOneCache = new HashMap<Field, OneToOneRelationship>();
		mLazyLoadingCache = new HashMap<Class<?>, Boolean>();
		mRestEndpointCache = new HashMap<Class<?>, String>();
		mRestFieldCache = new HashMap<Field, String>();
	}
	
	@PostConstruct
	private void init() {
		mLogger = Logger.getInstance(mContext, getClass().getSimpleName());
		mPropLoader = new PropertyLoader(mContext.getAndroidContext());
	}

	/**
	 * Indicates if the given {@code Class} is persistent or transient.
	 * 
	 * @param c
	 *            the {@code Class} to check persistence for
	 * @return {@code true} if persistent, {@code false} if transient
	 */
	public abstract boolean isPersistent(Class<?> c);

	/**
	 * Retrieves the name of the database table for the specified {@code Class}.
	 * If the {@code Class} is transient, this method will return {@code null}.
	 * 
	 * @param c
	 *            the {@code Class} to retrieve the table name for
	 * @return the name of the database table for the specified domain model
	 *         {@code Class}
	 * @throws IllegalArgumentException
	 *             if the given {@code Class} is transient
	 * @throws InvalidMapFileException
	 *             if the map file for the given {@code Class} is invalid
	 */
	public abstract String getModelTableName(Class<?> c)
			throws IllegalArgumentException, InvalidMapFileException;

	/**
	 * Retrieves a {@code List} of all persistent {@code Fields} for the given
	 * {@code Class}.
	 * 
	 * @param c
	 *            the {@code Class} to retrieve persistent {@code Fields} for
	 * @return {@code List} of all persistent {@code Fields} for the specified
	 *         {@code Class}
	 */
	public abstract List<Field> getPersistentFields(Class<?> c);

	/**
	 * Retrieves the primary key {@code Field} for the given {@code Class}.
	 * 
	 * @param c
	 *            the {@code Class} to retrieve the primary key {@code Field}
	 *            for
	 * @return the primary key {@code Field} for the specified {@code Class}
	 * @throws ModelConfigurationException
	 *             if multiple primary keys are declared in {@code c}
	 */
	public abstract Field getPrimaryKeyField(Class<?> c)
			throws ModelConfigurationException;

	/**
	 * Retrieves the name of the database column the specified {@code Field}
	 * maps to.
	 * 
	 * @param f
	 *            the {@code Field} to retrieve the column for
	 * @return the name of the column
	 */
	public abstract String getFieldColumnName(Field f);

	/**
	 * Determines if the given {@link Field} is a primary key.
	 * 
	 * @param f
	 *            the {@code Field} to check
	 * @return {@code true} if it is a primary key, {@code false} if it's not
	 */
	public abstract boolean isFieldPrimaryKey(Field f);

	/**
	 * Determines if the given primary key {@link Field} is set to
	 * autoincrement. This method assumes, as a precondition, that the
	 * {@code Field} being passed is guaranteed to be a primary key, whether
	 * implicitly or explicitly.
	 * 
	 * @param f
	 *            the primary key {@code Field} to check if it's set to
	 *            autoincrement
	 * @return {@code true} if it is set to autoincrement, {@code false} if it's
	 *         not
	 * @throws InfinitumRuntimeException
	 *             if an explicit primary key that is set to autoincrement is
	 *             not of type int or long
	 */
	public abstract boolean isPrimaryKeyAutoIncrement(Field f)
			throws InfinitumRuntimeException;

	/**
	 * Checks if the specified {@code Field's} associated column is nullable.
	 * 
	 * @param f
	 *            the {@code Field} to check if nullable
	 * @return {@code true} if the field is nullable, {@code false} if it is not
	 *         nullable
	 */
	public abstract boolean isFieldNullable(Field f);

	/**
	 * Checks if the specified {@code Field} is unique, meaning each record must
	 * have a different value in the table. This is a way of implementing a
	 * unique constraint on a column.
	 * 
	 * @param f
	 *            the {@code Field} to check for uniqueness
	 * @return {@code true} if it is unique, {@code false} if not
	 */
	public abstract boolean isFieldUnique(Field f);

	/**
	 * Retrieves a {@link Set} of all {@link ManyToManyRelationship} instances
	 * for the given {@link Class}.
	 * 
	 * @param c
	 *            the {@code Class} to get relationships for
	 * @return {@code Set} of all many-to-many relationships
	 */
	public abstract Set<ManyToManyRelationship> getManyToManyRelationships(
			Class<?> c);

	/**
	 * Retrieves the {@link Cascade} mode for the given persistent {@link Class}
	 * .
	 * 
	 * @param c
	 *            the {@code Class} to retrieve {@code Cascade} mode for
	 * @return {@code Cascade} mode
	 */
	public abstract Cascade getCascadeMode(Class<?> c);

	/**
	 * Indicates if the given persistent {@link Field} is part of an entity
	 * relationship, either many-to-many, many-to-one, one-to-many, or
	 * one-to-one.
	 * 
	 * @param f
	 *            the {@code Field} to check
	 * @return {@code true} if it is part of a relationship, {@code false} if
	 *         not
	 */
	public abstract boolean isRelationship(Field f);

	/**
	 * Indicates if the given persistent {@link Field} is part of a many-to-many
	 * entity relationship.
	 * 
	 * @param f
	 *            the {@code Field} to check
	 * @return {@code true} if it is part of a many-to-many relationship,
	 *         {@code false} if not
	 */
	public abstract boolean isManyToManyRelationship(Field f);

	/**
	 * Indicates if the given persistent {@link Field} is part of a one-to-one
	 * entity relationship.
	 * 
	 * @param f
	 *            the {@code Field} to check
	 * @return {@code true} if it is part of a one-to-one relationship,
	 *         {@code false} if not
	 */
	public abstract boolean isOneToOneRelationship(Field f);

	/**
	 * Indicates if the given persistent {@link Field} is part of a many-to-one
	 * or one-to-one entity relationship.
	 * 
	 * @param f
	 *            the {@code Field} to check
	 * @return {@code true} if it is part of a many-to-one or one-to-one
	 *         relationship, {@code false} if not
	 */
	public abstract boolean isToOneRelationship(Field f);

	/**
	 * Retrieves the {@link ModelRelationship} the given {@link Field} is a part
	 * of.
	 * 
	 * @param f
	 *            the {@code Field} to retrieve the relationship for
	 * @return the {@code ModelRelationship} for {@code f} or {@code null} if
	 *         there is none
	 */
	public abstract ModelRelationship getRelationship(Field f);

	/**
	 * Retrieves the {@link Field} pertaining to the given
	 * {@link ModelRelationship} for the specified {@link Class}. If no such
	 * {@code Field} exists, {@code null} is returned.
	 * 
	 * @param c
	 *            the {@code Class} to retrieve the {@code Field} from
	 * @param rel
	 *            the {@code ModelRelationship} to retrieve the {@code Field}
	 *            for
	 * @return {@code Field} pertaining to the relationship or {@code null}
	 */
	public abstract Field findRelationshipField(Class<?> c,
			ModelRelationship rel);

	/**
	 * Indicates if the given persistent {@link Class} has lazy loading enabled
	 * or not.
	 * 
	 * @param c
	 *            the {@code Class} to check lazy-loading status
	 * @return {@code true} if lazy loading is enabled, {@code false} if not
	 */
	public abstract boolean isLazy(Class<?> c);

	/**
	 * Retrieves the REST endpoint name for the given persistent {@link Class}.
	 * 
	 * @param c
	 *            the {@code Class} to retrieve the REST endpoint name for
	 * @return endpoint name
	 * @throws IllegalArgumentException
	 *             if the given {@code Class} is not a domain model or
	 *             persistent
	 */
	public abstract String getRestEndpoint(Class<?> c)
			throws IllegalArgumentException;

	/**
	 * Retrieves the REST endpoint field name for the given persistent
	 * {@link Field}.
	 * 
	 * @param f
	 *            the {@code Field} to retrieve the endpoint field name for
	 * @return endpoint field name
	 * @throws IllegalArgumentException
	 *             if the containing {@link Class} of the given {@code Field} is
	 *             transient or if the {@code Field} itself is marked transient
	 */
	public abstract String getEndpointFieldName(Field f)
			throws IllegalArgumentException;

	/**
	 * Finds the persistent {@link Field} for the given {@link Class} which has
	 * the specified name. Returns {@code null} if no such {@code Field} exists.
	 * 
	 * @param c
	 *            the {@code Class} containing the {@code Field}
	 * @param name
	 *            the name of the {@code Field} to retrieve
	 * @return {@code Field} with specified name
	 */
	public Field findPersistentField(Class<?> c, String name) {
		List<Field> fields = getPersistentFields(c);
		for (Field f : fields) {
			if (f.getName().equalsIgnoreCase(name)) {
				f.setAccessible(true);
				return f;
			}
		}
		return null;
	}

	/**
	 * Retrieves a {@code List} of all unique {@code Fields} for the given
	 * {@code Class}.
	 * 
	 * @param c
	 *            the {@code Class} to retrieve unique {@code Fields} for
	 * @return {@code List} of all unique {@code Fields} for the specified
	 *         {@code Class}
	 */
	public List<Field> getUniqueFields(Class<?> c) {
		if (!isPersistent(c))
			throw new IllegalArgumentException("Class '" + c.getName()
					+ "' is transient.");
		List<Field> ret = new ArrayList<Field>();
		List<Field> fields = getPersistentFields(c);
		for (Field f : fields) {
			if (mFieldUniqueCache.containsKey(f) && mFieldUniqueCache.get(f))
				ret.add(f);
			else {
				boolean unique = isFieldUnique(f);
				mFieldUniqueCache.put(f, unique);
				if (unique)
					ret.add(f);
			}
		}
		return ret;
	}

	/**
	 * Calculates a hash code for the specified persistent model based on its
	 * {@link Class} and primary key.
	 * 
	 * @param model
	 *            the model entity to compute the hash for
	 * @return hash code for the model
	 */
	public int computeModelHash(Object model) {
		Field f = getPrimaryKeyField(model.getClass());
		Serializable pk = null;
		try {
			pk = (Serializable) mClassReflector.getFieldValue(model, f);
		} catch (ClassCastException e) {
			throw new ModelConfigurationException("Invalid primary key specified for '" + model.getClass().getName() + "'.");
		}
		return computeModelHash(model.getClass(), pk);
	}

	/**
	 * Calculates a hash code based on the given {@link Class} and primary key.
	 * 
	 * @param c
	 *            the {@code Class} to compute the hash for
	 * @param pk
	 *            the primary key to compute hash for
	 * @return hash code
	 */
	public int computeModelHash(Class<?> c, Serializable pk) {
		final int PRIME = 31;
		int hash = 7;
		hash *= PRIME + c.hashCode();
		hash *= PRIME + pk.hashCode();
		return hash;
	}

	/**
	 * Retrieves the primary key value for the given persistent model.
	 * 
	 * @param model
	 *            the model to retrieve the primary key for
	 * @return primary key value
	 */
	public Serializable getPrimaryKey(Object model) {
		Serializable ret = null;
		if (AopProxy.isAopProxy(model)) {
			model = AopProxy.getProxy(model).getTarget();
		}
		Field pkField = getPrimaryKeyField(model.getClass());
		pkField.setAccessible(true);
		try {
			ret = (Serializable) pkField.get(model);
		} catch (IllegalArgumentException e) {
			mLogger.error("Unable to retrieve primary key for object of type '"
					+ model.getClass().getName() + "'", e);
		} catch (IllegalAccessException e) {
			mLogger.error("Unable to retrieve primary key for object of type '"
					+ model.getClass().getName() + "'", e);
		} catch (ClassCastException e) {
			throw new ModelConfigurationException(
					"Invalid primary key specified for '"
							+ model.getClass().getName() + "'.");
		}
		return ret;
	}

	/**
	 * Indicates if the primary key {@link Field} for the given model is 0 or
	 * {@code null}.
	 * 
	 * @param model
	 *            the model to check the primary key value for
	 * @return {@code true} if it is 0 or {@code null}, false if not
	 */
	public boolean isPKNullOrZero(Object model) {
		Serializable pk = getPrimaryKey(model);
		if (pk == null)
			return true;
		if (pk instanceof Integer)
			return (((Integer) pk) == 0);
		else if (pk instanceof Long)
			return (((Long) pk) == 0);
		else if (pk instanceof Float)
			return (((Float) pk) == 0);
		else if (pk instanceof Double)
			return (((Double) pk) == 0);
		return false;
	}

	/**
	 * Returns the many-to-many relationship cache.
	 * 
	 * @return many-to-many cache
	 */
	public Map<Field, ManyToManyRelationship> getManyToManyCache() {
		return mManyToManyCache;
	}

	protected Field findPrimaryKeyField(Class<?> c) {
		List<Field> fields = getPersistentFields(c);
		for (Field f : fields) {
			if (f.getName().equals("mId") || f.getName().equals("mID")
					|| f.getName().equalsIgnoreCase("id"))
				return f;
		}
		return null;
	}

}
