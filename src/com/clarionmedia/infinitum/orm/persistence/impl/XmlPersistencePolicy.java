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

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import android.content.Context;
import android.content.res.Resources;
import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.di.annotation.Autowired;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.internal.StringUtil;
import com.clarionmedia.infinitum.orm.exception.InvalidMapFileException;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.relationship.ManyToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.ManyToOneRelationship;
import com.clarionmedia.infinitum.orm.relationship.ModelRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToOneRelationship;
import com.clarionmedia.infinitum.reflection.ClassReflector;

/**
 * <p>
 * This class provides runtime resolution for model persistence through XML map
 * files ({@code imf.xml}). Each persistent entity should have an
 * {@code imf.xml} file associated with it and placed in res/raw. If an entity
 * has no such file, it is marked as transient.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 05/09/12
 * @since 1.0
 * @see AnnotationsPersistencePolicy
 */
public class XmlPersistencePolicy extends PersistencePolicy {

	@Autowired
	private InfinitumContext mContext;

	@Autowired
	private ClassReflector mClassReflector;

	private Map<Class<?>, EntityMapping> mMappingCache;

	public XmlPersistencePolicy() {
		mMappingCache = new HashMap<Class<?>, EntityMapping>();
	}

	@Override
	public boolean isPersistent(Class<?> c) {
		return loadEntityMapping(c) != null;
	}

	@Override
	public String getModelTableName(Class<?> c) throws IllegalArgumentException, InvalidMapFileException {
		if (!isPersistent(c) || !mTypePolicy.isDomainModel(c))
			throw new IllegalArgumentException("Class '" + c.getName() + "' is transient.");
		EntityMapping mapping = loadEntityMapping(c);
		String table = mapping.getTable();
		return table == null ? c.getSimpleName().toLowerCase(Locale.getDefault()) : table;
	}

	@Override
	public List<Field> getPersistentFields(Class<?> c) {
		if (!isPersistent(c) || !mTypePolicy.isDomainModel(c))
			throw new IllegalArgumentException("Class '" + c.getName() + "' is transient.");
		EntityMapping mapping = loadEntityMapping(c);
		List<Property> properties = mapping.getProperties();
		List<Field> fields = new ArrayList<Field>();
		fields.add(getPrimaryKeyField(c));
		for (Property property : properties) {
			fields.add(mClassReflector.getField(c, property.mName));
		}
		return fields;
	}

	@Override
	public Field getPrimaryKeyField(Class<?> c) throws ModelConfigurationException {
		if (!isPersistent(c) || !mTypePolicy.isDomainModel(c))
			throw new IllegalArgumentException("Class '" + c.getName() + "' is transient.");
		EntityMapping mapping = loadEntityMapping(c);
		PrimaryKey pk = mapping.getPrimaryKey();
		return mClassReflector.getField(c, pk.mName);
	}

	@Override
	public String getFieldColumnName(Field f) {
		EntityMapping mapping = loadEntityMapping(f.getDeclaringClass());
		Property property = getProperty(mapping, f);
		return property.mColumn == null ? StringUtil.formatFieldName(f.getName()) : property.mColumn;
	}

	@Override
	public boolean isFieldPrimaryKey(Field f) {
		return f.equals(getPrimaryKeyField(f.getDeclaringClass()));
	}

	@Override
	public boolean isPrimaryKeyAutoIncrement(Field f) throws InfinitumRuntimeException {
		EntityMapping mapping = loadEntityMapping(f.getDeclaringClass());
		PrimaryKey pk = mapping.getPrimaryKey();
		if (pk == null)
			throw new ModelConfigurationException("Primary key missing in map file for class '" + f.getDeclaringClass().getName() + "'.");
		boolean autoincrement = pk.mAutoincrement;
		boolean isInteger = pk.mType.equalsIgnoreCase("int") || pk.mType.equalsIgnoreCase("integer") || pk.mType.equalsIgnoreCase("long");
		if (autoincrement && !isInteger)
			throw new InfinitumRuntimeException(String.format("Explicit primary key '%s' is not of type int or long in '%s'.", f.getName(),
					f.getDeclaringClass().getName()));
		return autoincrement;
	}

	@Override
	public boolean isFieldNullable(Field f) {
		EntityMapping mapping = loadEntityMapping(f.getDeclaringClass());
		Property property = getProperty(mapping, f);
		return !property.mNotNull;
	}

	@Override
	public boolean isFieldUnique(Field f) {
		EntityMapping mapping = loadEntityMapping(f.getDeclaringClass());
		Property property = getProperty(mapping, f);
		return property.mUnique;
	}

	@Override
	public Set<ManyToManyRelationship> getManyToManyRelationships(Class<?> c) {
		if (!isPersistent(c) || !mTypePolicy.isDomainModel(c))
			throw new IllegalArgumentException("Class '" + c.getName() + "' is transient.");
		EntityMapping mapping = loadEntityMapping(c);
		Set<ManyToManyRelationship> ret = new HashSet<ManyToManyRelationship>();
		for (ManyToManyRelationship r : mManyToManyCache.values()) {
			if (r.contains(c))
				ret.add(r);
		}
		if (ret.size() > 0)
			return ret;
		List<Field> fields = getManyToManyFields(c, mapping);
		for (Field f : fields) {
			ManyToManyRelationship rel = new ManyToManyRelationship(f);
			mManyToManyCache.put(f, rel);
			ret.add(rel);
		}
		return ret;
	}

	@Override
	public Cascade getCascadeMode(Class<?> c) {
		if (!isPersistent(c) || !mTypePolicy.isDomainModel(c))
			throw new IllegalArgumentException("Class '" + c.getName() + "' is transient.");
		EntityMapping mapping = loadEntityMapping(c);
		return mapping.getCascade();
	}

	@Override
	public boolean isRelationship(Field f) {
		EntityMapping mapping = loadEntityMapping(f.getDeclaringClass());
		Set<Field> fields = new HashSet<Field>();
		fields.addAll(getManyToManyFields(f.getDeclaringClass(), mapping));
		fields.addAll(getManyToOneFields(f.getDeclaringClass(), mapping));
		fields.addAll(getOneToManyFields(f.getDeclaringClass(), mapping));
		fields.addAll(getOneToOneFields(f.getDeclaringClass(), mapping));
		return fields.contains(f);
	}

	@Override
	public boolean isManyToManyRelationship(Field f) {
		EntityMapping mapping = loadEntityMapping(f.getDeclaringClass());
		return getManyToManyFields(f.getDeclaringClass(), mapping).contains(f);
	}

	@Override
	public boolean isOneToOneRelationship(Field f) {
		EntityMapping mapping = loadEntityMapping(f.getDeclaringClass());
		return getOneToOneFields(f.getDeclaringClass(), mapping).contains(f);
	}

	public boolean isManyToOneRelationship(Field f) {
		EntityMapping mapping = loadEntityMapping(f.getDeclaringClass());
		return getManyToOneFields(f.getDeclaringClass(), mapping).contains(f);
	}

	public boolean isOneToManyRelationship(Field f) {
		EntityMapping mapping = loadEntityMapping(f.getDeclaringClass());
		return getOneToManyFields(f.getDeclaringClass(), mapping).contains(f);
	}

	@Override
	public boolean isToOneRelationship(Field f) {
		EntityMapping mapping = loadEntityMapping(f.getDeclaringClass());
		Set<Field> fields = new HashSet<Field>();
		fields.addAll(getManyToOneFields(f.getDeclaringClass(), mapping));
		fields.addAll(getOneToOneFields(f.getDeclaringClass(), mapping));
		return fields.contains(f);
	}

	@Override
	public ModelRelationship getRelationship(Field f) {
		if (isManyToManyRelationship(f))
			return new ManyToManyRelationship(f);
		if (isOneToOneRelationship(f))
			return new OneToOneRelationship(f);
		if (isManyToOneRelationship(f))
			return new ManyToOneRelationship(f);
		if (isOneToManyRelationship(f))
			return new OneToManyRelationship(f);
		return null;

	}

	@Override
	public Field findRelationshipField(Class<?> c, ModelRelationship rel) {
		if (!isPersistent(c) || !mTypePolicy.isDomainModel(c))
			throw new IllegalArgumentException("Class '" + c.getName() + "' is transient.");
		EntityMapping mapping = loadEntityMapping(c);
		switch (rel.getRelationType()) {
		case ManyToMany:
			for (ManyToMany mtm : mapping.getManyToMany()) {
				if (rel.getName().equalsIgnoreCase(mtm.mName))
					return mClassReflector.getField(c, mtm.mKeyField);
			}
			break;
		case ManyToOne:
			for (ManyToOne mto : mapping.getManyToOne()) {
				if (rel.getName().equalsIgnoreCase(mto.mName))
					return mClassReflector.getField(c, mto.mField);
			}
			break;
		case OneToMany:
			for (OneToMany otm : mapping.getOneToMany()) {
				if (rel.getName().equalsIgnoreCase(otm.mName))
					return mClassReflector.getField(c, otm.mField);
			}
			break;
		case OneToOne:
			for (OneToOne oto : mapping.getOneToOne()) {
				if (rel.getName().equalsIgnoreCase(oto.mName))
					return mClassReflector.getField(c, oto.mField);
			}
		}
		return null;
	}

	@Override
	public boolean isLazy(Class<?> c) {
		if (!isPersistent(c) || !mTypePolicy.isDomainModel(c))
			throw new IllegalArgumentException("Class '" + c.getName() + "' is transient.");
		EntityMapping mapping = loadEntityMapping(c);
		return mapping.isLazy();
	}

	@Override
	public String getRestEndpoint(Class<?> c) throws IllegalArgumentException {
		if (!isPersistent(c) || !mTypePolicy.isDomainModel(c))
			throw new IllegalArgumentException("Class '" + c.getName() + "' is transient.");
		EntityMapping mapping = loadEntityMapping(c);
		return mapping.getRest();
	}

	@Override
	public String getEndpointFieldName(Field f) throws IllegalArgumentException {
		EntityMapping mapping = loadEntityMapping(f.getDeclaringClass());
		Property property = getProperty(mapping, f);
		return property.mRest;
	}

	private EntityMapping loadEntityMapping(Class<?> clazz) {
		if (mMappingCache.containsKey(clazz))
			return mMappingCache.get(clazz);
		Context context = mContext.getAndroidContext();
		Resources res = context.getResources();
		int id = res.getIdentifier(clazz.getSimpleName().toLowerCase(Locale.getDefault()), "raw", context.getPackageName());
		if (id == 0)
			return null;
		Resources resources = context.getResources();
		Serializer serializer = new Persister();
		try {
			InputStream stream = resources.openRawResource(id);
			String xml = new Scanner(stream).useDelimiter("\\A").next();
			EntityMapping ret = serializer.read(EntityMapping.class, xml);
			if (ret == null)
				throw new InfinitumRuntimeException("Unable to read map file for class '" + clazz.getName() + "'.");
			mMappingCache.put(clazz, ret);
			return ret;
		} catch (Exception e) {
			throw new InfinitumRuntimeException("Unable to read map file for class '" + clazz.getName() + "'.", e);
		}
	}

	private Property getProperty(EntityMapping mapping, Field field) {
		if (isFieldPrimaryKey(field))
			return mapping.getPrimaryKey();
		List<Property> properties = mapping.getProperties();
		for (Property property : properties) {
			if (property.mName.equals(field.getName())) {
				return property;
			}
		}
		throw new ModelConfigurationException("Field '" + field.getName() + "' missing in map file for class '"
				+ field.getDeclaringClass().getName() + "'.");
	}

	private List<Field> getManyToManyFields(Class<?> clazz, EntityMapping mapping) {
		List<Field> fields = new ArrayList<Field>();
		for (ManyToMany mtm : mapping.getManyToMany()) {
			fields.add(mClassReflector.getField(clazz, mtm.mKeyField));
		}
		return fields;
	}

	private List<Field> getManyToOneFields(Class<?> clazz, EntityMapping mapping) {
		List<Field> fields = new ArrayList<Field>();
		for (ManyToOne mto : mapping.getManyToOne()) {
			fields.add(mClassReflector.getField(clazz, mto.mField));
		}
		return fields;
	}

	private List<Field> getOneToManyFields(Class<?> clazz, EntityMapping mapping) {
		List<Field> fields = new ArrayList<Field>();
		for (OneToMany otm : mapping.getOneToMany()) {
			fields.add(mClassReflector.getField(clazz, otm.mField));
		}
		return fields;
	}

	private List<Field> getOneToOneFields(Class<?> clazz, EntityMapping mapping) {
		List<Field> fields = new ArrayList<Field>();
		for (OneToOne oto : mapping.getOneToOne()) {
			fields.add(mClassReflector.getField(clazz, oto.mField));
		}
		return fields;
	}

	@Root(name = "infinitum-mapping")
	private static class EntityMapping {

		@Element(name = "class")
		private ClassMapping mClassMapping;

		public String getTable() {
			return mClassMapping.mTable;
		}

		public boolean isLazy() {
			return mClassMapping.mLazy;
		}

		public Cascade getCascade() {
			String cascade = mClassMapping.mCascade;
			if (cascade == null)
				return Cascade.ALL;
			if (cascade.equalsIgnoreCase("all"))
				return Cascade.ALL;
			if (cascade.equalsIgnoreCase("none"))
				return Cascade.NONE;
			if (cascade.equalsIgnoreCase("keys"))
				return Cascade.KEYS;
			return Cascade.ALL;
		}

		public String getRest() {
			return mClassMapping.mRest;
		}

		public PrimaryKey getPrimaryKey() {
			return mClassMapping.mPrimaryKey;
		}

		public List<Property> getProperties() {
			if (mClassMapping.mProperties == null)
				return new ArrayList<Property>();
			return mClassMapping.mProperties;
		}

		public List<ManyToMany> getManyToMany() {
			if (mClassMapping.mManyToMany == null)
				return new ArrayList<ManyToMany>();
			return mClassMapping.mManyToMany;
		}

		public List<ManyToOne> getManyToOne() {
			if (mClassMapping.mManyToOne == null)
				return new ArrayList<ManyToOne>();
			return mClassMapping.mManyToOne;
		}

		public List<OneToMany> getOneToMany() {
			if (mClassMapping.mOneToMany == null)
				return new ArrayList<OneToMany>();
			return mClassMapping.mOneToMany;
		}

		public List<OneToOne> getOneToOne() {
			if (mClassMapping.mOneToOne == null)
				return new ArrayList<OneToOne>();
			return mClassMapping.mOneToOne;
		}

		@Root(name = "class")
		private static class ClassMapping {

			@Attribute(name = "name")
			private String mName;

			@Attribute(name = "table", required = false)
			private String mTable;

			@Attribute(name = "lazy", required = false)
			private boolean mLazy;

			@Attribute(name = "cascade", required = false)
			private String mCascade;

			@Attribute(name = "rest", required = false)
			private String mRest;

			@Element(name = "primary-key")
			private PrimaryKey mPrimaryKey;

			@ElementList(entry = "property", inline = true, required = false)
			private List<Property> mProperties;

			@ElementList(entry = "many-to-many", inline = true, required = false)
			private List<ManyToMany> mManyToMany;

			@ElementList(entry = "one-to-many", inline = true, required = false)
			private List<OneToMany> mOneToMany;

			@ElementList(entry = "many-to-one", inline = true, required = false)
			private List<ManyToOne> mManyToOne;

			@ElementList(entry = "one-to-one", inline = true, required = false)
			private List<OneToOne> mOneToOne;

		}

	}

	@Root(name = "property")
	private static class Property {

		@Attribute(name = "name")
		protected String mName;

		@Attribute(name = "column", required = false)
		protected String mColumn;

		@Attribute(name = "type")
		protected String mType;

		@Attribute(name = "not-null", required = false)
		private boolean mNotNull;

		@Attribute(name = "unique", required = false)
		private boolean mUnique;

		@Attribute(name = "rest", required = false)
		private String mRest;

	}

	@Root(name = "primary-key")
	private static class PrimaryKey extends Property {

		@Attribute(name = "autoincrement", required = false)
		private boolean mAutoincrement;

	}

	@Root(name = "many-to-many")
	private static class ManyToMany {

		@Attribute(name = "name")
		private String mName;

		@Attribute(name = "class")
		private String mClass;

		@Attribute(name = "foreign-field")
		private String mForeignField;

		@Attribute(name = "key-field")
		private String mKeyField;

		@Attribute(name = "table")
		private String mTable;

	}

	@Root(name = "many-to-one")
	private static class ManyToOne {

		@Attribute(name = "name")
		private String mName;

		@Attribute(name = "field")
		private String mField;

		@Attribute(name = "class")
		private String mClass;

		@Attribute(name = "column")
		private String mColumn;

	}

	@Root(name = "one-to-many")
	private static class OneToMany {

		@Attribute(name = "name")
		private String mName;

		@Attribute(name = "field")
		private String mField;

		@Attribute(name = "class")
		private String mClass;

		@Attribute(name = "column")
		private String mColumn;

	}

	@Root(name = "one-to-one")
	private static class OneToOne {

		@Attribute(name = "name")
		private String mName;

		@Attribute(name = "field")
		private String mField;

		@Attribute(name = "class")
		private String mClass;

		@Attribute(name = "column")
		private String mColumn;

	}

}