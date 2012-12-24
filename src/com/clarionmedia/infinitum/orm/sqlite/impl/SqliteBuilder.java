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

package com.clarionmedia.infinitum.orm.sqlite.impl;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;

import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;
import com.clarionmedia.infinitum.di.annotation.Autowired;
import com.clarionmedia.infinitum.di.annotation.PostConstruct;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.internal.PropertyLoader;
import com.clarionmedia.infinitum.orm.context.OrmContext;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.criteria.criterion.Criterion;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.TypeResolutionPolicy.SqliteDataType;
import com.clarionmedia.infinitum.orm.relationship.ManyToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToOneRelationship;
import com.clarionmedia.infinitum.orm.sql.SqlBuilder;
import com.clarionmedia.infinitum.orm.sql.SqlConstants;
import com.clarionmedia.infinitum.reflection.PackageReflector;

/**
 * <p>
 * Implementation of {@link SqlBuilder} for interacting with a SQLite database.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/03/12
 * @since 1.0
 */
public class SqliteBuilder implements SqlBuilder {

	// TODO: this class currently doesn't handle reserved keywords.
	// See: http://www.sqlite.org/lang_keywords.html

	@Autowired
	private SqliteMapper mMapper;

	@Autowired
	private PersistencePolicy mPersistencePolicy;

	@Autowired
	private PackageReflector mPackageReflector;

	@Autowired
	private OrmContext mContext;

	private PropertyLoader mPropLoader;

	@PostConstruct
	private void init() {
		mPropLoader = new PropertyLoader(mContext.getAndroidContext());
	}

	@Override
	public int createTables(SqliteDbHelper dbHelper)
			throws ModelConfigurationException, InfinitumConfigurationException {
		int count = 0;
		SQLiteDatabase db = dbHelper.getDatabase();
		for (String m : mContext.getDomainTypes()) {
			Class<?> c = mPackageReflector.getClass(m);
			if (c == null)
				throw new InfinitumConfigurationException("No such class '" + m
						+ "'.");
			String sql = createModelTableString(c);
			if (sql != null) {
				db.execSQL(sql);
				count++;
			}
			mPersistencePolicy.getManyToManyRelationships(c);
		}
		for (ManyToManyRelationship r : mPersistencePolicy.getManyToManyCache()
				.values()) {
			String sql = createManyToManyTableString(r);
			if (sql != null) {
				db.execSQL(sql);
				count++;
			}
		}
		return count;
	}

	@Override
	public int dropTables(SqliteDbHelper dbHelper) {
		int count = 0;
		SQLiteDatabase db = dbHelper.getDatabase();
		for (String m : mContext.getDomainTypes()) {
			Class<?> c = mPackageReflector.getClass(m);
			if (c == null)
				throw new InfinitumConfigurationException("No such class '" + m
						+ "'.");
			String sql = dropModelTableString(c);
			if (sql != null) {
				db.execSQL(sql);
				count++;
			}
		}
		return count;
	}

	@Override
	public String createQuery(Criteria<?> criteria) {
		Class<?> c = criteria.getEntityClass();
		StringBuilder query = new StringBuilder(SqlConstants.SELECT_ALL_FROM)
				.append(mPersistencePolicy.getModelTableName(c));
		String prefix = " WHERE ";
		for (Criterion criterion : criteria.getCriterion()) {
			query.append(prefix);
			prefix = ' ' + SqlConstants.AND + ' ';
			query.append(criterion.toSql(criteria));
		}
		if (criteria.getLimit() > 0)
			query.append(' ').append(SqlConstants.LIMIT).append(' ')
					.append(criteria.getLimit());
		if (criteria.getOffset() > 0)
			query.append(' ').append(SqlConstants.OFFSET).append(' ')
					.append(criteria.getOffset());
		return query.toString();
	}

	@Override
	public String createCountQuery(Criteria<?> criteria) {
		Class<?> c = criteria.getEntityClass();
		StringBuilder query = new StringBuilder(SqlConstants.SELECT_COUNT_FROM)
				.append(mPersistencePolicy.getModelTableName(c));
		String prefix = " WHERE ";
		for (Criterion criterion : criteria.getCriterion()) {
			query.append(prefix);
			prefix = ' ' + SqlConstants.AND + ' ';
			query.append(criterion.toSql(criteria));
		}
		if (criteria.getLimit() > 0)
			query.append(' ').append(SqlConstants.LIMIT).append(' ')
					.append(criteria.getLimit());
		if (criteria.getOffset() > 0)
			query.append(' ').append(SqlConstants.OFFSET).append(' ')
					.append(criteria.getOffset());
		return query.toString();
	}

	@Override
	public String createManyToManyJoinQuery(ManyToManyRelationship rel,
			Serializable id, Class<?> direction)
			throws InfinitumRuntimeException {
		if (!rel.contains(direction))
			throw new InfinitumRuntimeException(
					String.format(
							"'%s' is not a valid direction for relationship '%s'<=>'%s'.",
							direction.getName(), rel.getFirstType().getName(),
							rel.getSecondType().getName()));
		StringBuilder query = new StringBuilder(String.format(
				SqlConstants.ALIASED_SELECT_ALL_FROM, 'x')).append(
				mPersistencePolicy.getModelTableName(rel.getFirstType()))
				.append(' ');
		if (direction == rel.getFirstType())
			query.append("x, ");
		else
			query.append("y, ");
		query.append(mPersistencePolicy.getModelTableName(rel.getSecondType()))
				.append(' ');
		if (direction == rel.getSecondType())
			query.append("x, ");
		else
			query.append("y, ");
		query.append(rel.getTableName()).append(" z ")
				.append(SqlConstants.WHERE).append(' ').append("z.");
		if (direction == rel.getFirstType()) {
			query.append(
					mPersistencePolicy.getModelTableName(rel.getFirstType()))
					.append('_')
					.append(mPersistencePolicy.getFieldColumnName(rel
							.getFirstField()))
					.append(" = ")
					.append("x.")
					.append(mPersistencePolicy.getFieldColumnName(rel
							.getFirstField()))
					.append(' ')
					.append(SqlConstants.AND)
					.append(" z.")
					.append(mPersistencePolicy.getModelTableName(rel
							.getSecondType()))
					.append('_')
					.append(mPersistencePolicy.getFieldColumnName(rel
							.getSecondField()))
					.append(" = ")
					.append("y.")
					.append(mPersistencePolicy.getFieldColumnName(rel
							.getSecondField()))
					.append(' ')
					.append(SqlConstants.AND)
					.append(" y.")
					.append(mPersistencePolicy.getFieldColumnName(rel
							.getSecondField())).append(" = ");
		} else {
			query.append(
					mPersistencePolicy.getModelTableName(rel.getSecondType()))
					.append('_')
					.append(mPersistencePolicy.getFieldColumnName(rel
							.getSecondField()))
					.append(" = ")
					.append("x.")
					.append(mPersistencePolicy.getFieldColumnName(rel
							.getSecondField()))
					.append(' ')
					.append(SqlConstants.AND)
					.append(" z.")
					.append(mPersistencePolicy.getModelTableName(rel
							.getFirstType()))
					.append('_')
					.append(mPersistencePolicy.getFieldColumnName(rel
							.getFirstField()))
					.append(" = ")
					.append("y.")
					.append(mPersistencePolicy.getFieldColumnName(rel
							.getFirstField()))
					.append(' ')
					.append(SqlConstants.AND)
					.append(" y.")
					.append(mPersistencePolicy.getFieldColumnName(rel
							.getFirstField())).append(" = ");
		}
		switch (mMapper.getSqliteDataType(id)) {
		case TEXT:
			query.append("'").append(id).append("'");
			break;
		default:
			query.append(id);
		}
		return query.toString();
	}

	@Override
	public String createDeleteStaleRelationshipQuery(
			ManyToManyRelationship rel, Object model,
			List<Serializable> relatedKeys) {
		Serializable pk = mPersistencePolicy.getPrimaryKey(model);
		StringBuilder ret = new StringBuilder(SqlConstants.DELETE_FROM)
				.append(rel.getTableName()).append(' ')
				.append(SqlConstants.WHERE).append(' ');
		Field col;
		if (model.getClass() == rel.getFirstType()) {
			ret.append(
					mPersistencePolicy.getModelTableName(rel.getFirstType())
							+ '_'
							+ mPersistencePolicy.getFieldColumnName(rel
									.getFirstField())).append(" = ");
			col = rel.getFirstField();
		} else {
			ret.append(
					mPersistencePolicy.getModelTableName(rel.getSecondType())
							+ '_'
							+ mPersistencePolicy.getFieldColumnName(rel
									.getSecondField())).append(" = ");
			col = rel.getSecondField();
		}
		switch (mMapper.getSqliteDataType(col)) {
		case TEXT:
			ret.append("'").append(pk).append("'");
			break;
		default:
			ret.append(pk);
		}
		ret.append(' ').append(SqlConstants.AND).append(' ');
		if (model.getClass() == rel.getFirstType())
			ret.append(mPersistencePolicy.getModelTableName(rel.getSecondType())
					+ '_'
					+ mPersistencePolicy.getFieldColumnName(rel
							.getSecondField()));
		else
			ret.append(mPersistencePolicy.getModelTableName(rel.getFirstType())
					+ '_'
					+ mPersistencePolicy.getFieldColumnName(rel.getFirstField()));
		ret.append(' ').append(SqlConstants.NOT_IN).append(" (");
		String prefix = "";
		for (Serializable key : relatedKeys) {
			ret.append(prefix);
			switch (mMapper.getSqliteDataType(key)) {
			case TEXT:
				ret.append("'").append(key).append("'");
				break;
			default:
				ret.append(key);
			}
			prefix = ", ";
		}
		return ret.append(")").toString();
	}

	@Override
	public String createUpdateForeignKeyQuery(OneToManyRelationship rel,
			Object model, List<Serializable> relatedKeys) {
		StringBuilder ret = new StringBuilder(SqlConstants.UPDATE)
				.append(' ')
				.append(mPersistencePolicy.getModelTableName(rel.getManyType()))
				.append(' ').append(SqlConstants.SET).append(' ')
				.append(rel.getColumn()).append(" = ");
		Field pkField = mPersistencePolicy.getPrimaryKeyField(model.getClass());
		pkField.setAccessible(true);
		Serializable pk = mPersistencePolicy.getPrimaryKey(model);
		switch (mMapper.getSqliteDataType(pkField)) {
		case TEXT:
			ret.append("'").append(pk).append("'");
			break;
		default:
			ret.append(pk);
		}
		ret.append(' ').append(SqlConstants.WHERE).append(' ');
		pkField = mPersistencePolicy.getPrimaryKeyField(rel.getManyType());
		ret.append(mPersistencePolicy.getFieldColumnName(pkField)).append(' ')
				.append(SqlConstants.IN).append(" (");
		String prefix = "";
		for (Serializable key : relatedKeys) {
			ret.append(prefix);
			switch (mMapper.getSqliteDataType(key)) {
			case TEXT:
				ret.append("'").append(key).append("'");
				break;
			default:
				ret.append(key);
			}
			prefix = ", ";
		}
		return ret.append(")").toString();
	}

	@Override
	public String createUpdateOneToOneForeignKeyQuery(
			OneToOneRelationship relationship, Object model, Object related) {
		StringBuilder sb = new StringBuilder(SqlConstants.UPDATE).append(' ')
				.append(mPersistencePolicy.getModelTableName(model.getClass()))
				.append(' ').append(SqlConstants.SET).append(' ')
				.append(relationship.getColumn()).append(" = ");
		Field pkField = mPersistencePolicy.getPrimaryKeyField(related
				.getClass());
		pkField.setAccessible(true);
		Serializable pk = mPersistencePolicy.getPrimaryKey(related);
		switch (mMapper.getSqliteDataType(pkField)) {
		case TEXT:
			sb.append("'").append(pk).append("'");
			break;
		default:
			sb.append(pk);
		}
		sb.append(' ').append(SqlConstants.WHERE).append(' ');
		pkField = mPersistencePolicy.getPrimaryKeyField(model.getClass());
		sb.append(mPersistencePolicy.getFieldColumnName(pkField)).append(" = ");
		pk = mPersistencePolicy.getPrimaryKey(model);
		switch (mMapper.getSqliteDataType(pkField)) {
		case TEXT:
			sb.append("'").append(pk).append("'");
			break;
		default:
			sb.append(pk);
		}
		return sb.toString();
	}

	@Override
	public String createManyToManyDeleteQuery(Object obj,
			ManyToManyRelationship rel) {
		StringBuilder query = new StringBuilder(String.format(
				SqlConstants.DELETE_FROM_WHERE, rel.getTableName()));
		if (obj.getClass() == rel.getFirstType())
			query.append(mPersistencePolicy.getModelTableName(rel
					.getFirstType())
					+ '_'
					+ mPersistencePolicy.getFieldColumnName(rel.getFirstField()));
		else
			query.append(mPersistencePolicy.getModelTableName(rel
					.getSecondType())
					+ '_'
					+ mPersistencePolicy.getFieldColumnName(rel
							.getSecondField()));
		query.append(" = ");
		Field pkField = mPersistencePolicy.getPrimaryKeyField(obj.getClass());
		pkField.setAccessible(true);
		Serializable pk = mPersistencePolicy.getPrimaryKey(obj);
		switch (mMapper.getSqliteDataType(pkField)) {
		case TEXT:
			query.append("'").append(pk).append("'");
			break;
		default:
			query.append(pk);
		}
		return query.toString();
	}

	@Override
	public String createUpdateQuery(Object model, Object related, String column) {
		Serializable pk = mPersistencePolicy.getPrimaryKey(related);
		StringBuilder update = new StringBuilder(SqlConstants.UPDATE).append(
				" ").append(
				mPersistencePolicy.getModelTableName(model.getClass()));
		update.append(" ").append(SqlConstants.SET).append(" ").append(column)
				.append(" = ");
		switch (mMapper.getSqliteDataType(mPersistencePolicy
				.getPrimaryKeyField(related.getClass()))) {
		case TEXT:
			update.append("'").append(pk).append("'");
			break;
		default:
			update.append(pk);
		}
		update.append(" ")
				.append(SqlConstants.WHERE)
				.append(" ")
				.append(mPersistencePolicy
						.getFieldColumnName(mPersistencePolicy
								.getPrimaryKeyField(model.getClass())))
				.append(" = ");
		pk = mPersistencePolicy.getPrimaryKey(model);
		switch (mMapper.getSqliteDataType(mPersistencePolicy
				.getPrimaryKeyField(model.getClass()))) {
		case TEXT:
			update.append("'").append(pk).append("'");
			break;
		default:
			update.append(pk);
		}
		return update.toString();
	}

	private String createManyToManyTableString(ManyToManyRelationship rel)
			throws ModelConfigurationException {
		if (!mPersistencePolicy.isPersistent(rel.getFirstType())
				|| !mPersistencePolicy.isPersistent(rel.getSecondType()))
			return null;
		StringBuilder sb = new StringBuilder(SqlConstants.CREATE_TABLE)
				.append(' ').append(rel.getTableName()).append(" (");
		Field first = rel.getFirstField();
		if (first == null)
			throw new ModelConfigurationException(String.format(mPropLoader
					.getErrorMessage("MM_RELATIONSHIP_ERROR"), rel
					.getFirstType().getName(), rel.getSecondType().getName()));
		Field second = rel.getSecondField();
		if (second == null)
			throw new ModelConfigurationException(String.format(mPropLoader
					.getErrorMessage("MM_RELATIONSHIP_ERROR"), rel
					.getFirstType().getName(), rel.getSecondType().getName()));
		String firstCol = mPersistencePolicy.getModelTableName(rel
				.getFirstType())
				+ '_'
				+ mPersistencePolicy.getFieldColumnName(first);
		String secondCol = mPersistencePolicy.getModelTableName(rel
				.getSecondType())
				+ '_'
				+ mPersistencePolicy.getFieldColumnName(second);
		sb.append(firstCol).append(' ')
				.append(mMapper.getSqliteDataType(first).toString())
				.append(' ').append(", ").append(secondCol).append(' ')
				.append(mMapper.getSqliteDataType(second).toString())
				.append(", ").append(SqlConstants.PRIMARY_KEY).append('(')
				.append(firstCol).append(", ").append(secondCol).append("))");
		return sb.toString();
	}

	private String createModelTableString(Class<?> c)
			throws ModelConfigurationException {
		if (!mPersistencePolicy.isPersistent(c))
			return null;
		StringBuilder sb = new StringBuilder(SqlConstants.CREATE_TABLE)
				.append(' ').append(mPersistencePolicy.getModelTableName(c))
				.append(" (");
		appendColumns(c, sb);
		appendUniqueConstraints(c, sb);
		sb.append(')');
		return sb.toString();
	}

	private String dropModelTableString(Class<?> c)
			throws ModelConfigurationException {
		if (!mPersistencePolicy.isPersistent(c))
			return null;
		return SqlConstants.DROP_TABLE + ' '
				+ mPersistencePolicy.getModelTableName(c);
	}

	private void appendColumns(Class<?> c, StringBuilder sb)
			throws ModelConfigurationException {
		List<Field> fields = mPersistencePolicy.getPersistentFields(c);

		// Throw a runtime exception if there are no persistent fields
		if (fields.size() == 0)
			throw new ModelConfigurationException(String.format(
					mPropLoader.getErrorMessage("NO_PERSISTENT_FIELDS"),
					c.getName()));

		String prefix = "";
		for (Field f : fields) {
			// M:M relationships are stored in a join table
			if (mPersistencePolicy.isManyToManyRelationship(f))
				continue;
			if (mPersistencePolicy.isOneToOneRelationship(f)) {
				OneToOneRelationship oto = new OneToOneRelationship(f);
				// The owner contains the FK
				if (oto.getOwner() != c)
					continue;
			}
			SqliteDataType type = mMapper.getSqliteDataType(f);
			if (type == null)
				continue;
			sb.append(prefix);
			prefix = ", ";

			// Append column name and data type, e.g. "foo INTEGER"
			sb.append(mPersistencePolicy.getFieldColumnName(f)).append(' ')
					.append(type.toString());

			// Check if the column is a PRIMARY KEY
			if (mPersistencePolicy.isFieldPrimaryKey(f)) {
				sb.append(" ").append(SqlConstants.PRIMARY_KEY);
				if (mPersistencePolicy.isPrimaryKeyAutoIncrement(f))
					sb.append(" ").append(SqlConstants.AUTO_INCREMENT);
			}

			// Check if the column is NOT NULL
			if (!mPersistencePolicy.isFieldNullable(f))
				sb.append(" ").append(SqlConstants.NOT_NULL);
		}
	}

	private void appendUniqueConstraints(Class<?> c, StringBuilder sb) {
		List<Field> fields = mPersistencePolicy.getUniqueFields(c);

		// Append any unique constraints, e.g. UNIQUE(foo, bar)
		if (fields.size() > 0) {
			sb.append(", ").append(SqlConstants.UNIQUE).append('(');
			String prefix = "";
			for (Field f : fields) {
				sb.append(prefix);
				prefix = ", ";
				sb.append(mPersistencePolicy.getFieldColumnName(f));
			}
			sb.append(')');
		}
	}

}
