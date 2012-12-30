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

package com.clarionmedia.infinitum.orm.internal.bind;

import java.lang.reflect.Field;
import java.util.Date;

import android.content.ContentValues;
import com.clarionmedia.infinitum.internal.DateFormatter;
import com.clarionmedia.infinitum.orm.ResultSet;
import com.clarionmedia.infinitum.orm.persistence.TypeResolutionPolicy.SqliteDataType;
import com.clarionmedia.infinitum.orm.sqlite.SqliteTypeAdapter;

/**
 * <p>
 * {@code SqliteTypeAdapters} for basic types.
 * </p>
 *
 * @author Tyler Treat
 * @version 1.0 03/17/12
 */
public final class SqliteTypeAdapters {

    public static final SqliteTypeAdapter<String> STRING = new SqliteTypeAdapter<String>(SqliteDataType.TEXT) {
        @Override
        public void mapToObject(ResultSet result, int index, Field field, Object model) throws IllegalArgumentException, IllegalAccessException {
            field.setAccessible(true);
            field.set(model, result.getString(index));
        }

        @Override
        public void mapToColumn(String value, String column, ContentValues values) {
            values.put(column, value);
        }

        @Override
        public void mapObjectToColumn(Object value, String column, ContentValues values) {
            values.put(column, (String) value);
        }
    };

    public static final SqliteTypeAdapter<Integer> INTEGER = new SqliteTypeAdapter<Integer>(SqliteDataType.INTEGER) {
        @Override
        public void mapToObject(ResultSet result, int index, Field field, Object model) throws IllegalArgumentException, IllegalAccessException {
            field.setAccessible(true);
            field.set(model, result.getInt(index));
        }

        @Override
        public void mapToColumn(Integer value, String column, ContentValues values) {
            values.put(column, value);
        }

        @Override
        public void mapObjectToColumn(Object value, String column, ContentValues values) {
            values.put(column, (Integer) value);
        }
    };

    public static final SqliteTypeAdapter<Long> LONG = new SqliteTypeAdapter<Long>(SqliteDataType.INTEGER) {
        @Override
        public void mapToObject(ResultSet result, int index, Field field, Object model) throws IllegalArgumentException, IllegalAccessException {
            field.setAccessible(true);
            field.set(model, result.getLong(index));
        }

        @Override
        public void mapToColumn(Long value, String column, ContentValues values) {
            values.put(column, value);
        }

        @Override
        public void mapObjectToColumn(Object value, String column, ContentValues values) {
            values.put(column, (Long) value);
        }
    };

    public static final SqliteTypeAdapter<Float> FLOAT = new SqliteTypeAdapter<Float>(SqliteDataType.REAL) {
        @Override
        public void mapToObject(ResultSet result, int index, Field field, Object model) throws IllegalArgumentException, IllegalAccessException {
            field.setAccessible(true);
            field.set(model, result.getFloat(index));
        }

        @Override
        public void mapToColumn(Float value, String column, ContentValues values) {
            values.put(column, value);
        }

        @Override
        public void mapObjectToColumn(Object value, String column, ContentValues values) {
            values.put(column, (Float) value);
        }
    };

    public static final SqliteTypeAdapter<Double> DOUBLE = new SqliteTypeAdapter<Double>(SqliteDataType.REAL) {
        @Override
        public void mapToObject(ResultSet result, int index, Field field, Object model) throws IllegalArgumentException, IllegalAccessException {
            field.setAccessible(true);
            field.set(model, result.getDouble(index));
        }

        @Override
        public void mapToColumn(Double value, String column, ContentValues values) {
            values.put(column, value);
        }

        @Override
        public void mapObjectToColumn(Object value, String column, ContentValues values) {
            values.put(column, (Double) value);
        }
    };

    public static final SqliteTypeAdapter<Short> SHORT = new SqliteTypeAdapter<Short>(SqliteDataType.INTEGER) {
        @Override
        public void mapToObject(ResultSet result, int index, Field field, Object model) throws IllegalArgumentException, IllegalAccessException {
            field.setAccessible(true);
            field.set(model, result.getShort(index));
        }

        @Override
        public void mapToColumn(Short value, String column, ContentValues values) {
            values.put(column, value);
        }

        @Override
        public void mapObjectToColumn(Object value, String column, ContentValues values) {
            values.put(column, (Short) value);
        }
    };

    public static final SqliteTypeAdapter<Boolean> BOOLEAN = new SqliteTypeAdapter<Boolean>(SqliteDataType.INTEGER) {
        @Override
        public void mapToObject(ResultSet result, int index, Field field, Object model) throws IllegalArgumentException, IllegalAccessException {
            field.setAccessible(true);
            int b = result.getInt(index);
            field.set(model, b == 1);
        }

        @Override
        public void mapToColumn(Boolean value, String column, ContentValues values) {
            values.put(column, value);
        }

        @Override
        public void mapObjectToColumn(Object value, String column, ContentValues values) {
            values.put(column, (Boolean) value);
        }
    };

    public static final SqliteTypeAdapter<Byte> BYTE = new SqliteTypeAdapter<Byte>(SqliteDataType.BLOB) {
        @Override
        public void mapToObject(ResultSet result, int index, Field field, Object model) throws IllegalArgumentException, IllegalAccessException {
            field.setAccessible(true);
            field.set(model, result.getBlob(index)[0]);
        }

        @Override
        public void mapToColumn(Byte value, String column, ContentValues values) {
            values.put(column, value);
        }

        @Override
        public void mapObjectToColumn(Object value, String column, ContentValues values) {
            values.put(column, (Byte) value);
        }
    };

    public static final SqliteTypeAdapter<byte[]> BYTE_ARRAY = new SqliteTypeAdapter<byte[]>(SqliteDataType.BLOB) {
        @Override
        public void mapToObject(ResultSet result, int index, Field field, Object model) throws IllegalArgumentException, IllegalAccessException {
            field.setAccessible(true);
            field.set(model, result.getBlob(index));
        }

        @Override
        public void mapToColumn(byte[] value, String column, ContentValues values) {
            values.put(column, value);
        }

        @Override
        public void mapObjectToColumn(Object value, String column, ContentValues values) {
            values.put(column, (byte[]) value);
        }
    };

    public static final SqliteTypeAdapter<Character> CHARACTER = new SqliteTypeAdapter<Character>(SqliteDataType.TEXT) {
        @Override
        public void mapToObject(ResultSet result, int index, Field field, Object model) throws IllegalArgumentException, IllegalAccessException {
            field.setAccessible(true);
            field.set(model, result.getString(index).charAt(0));
        }

        @Override
        public void mapToColumn(Character value, String column, ContentValues values) {
            values.put(column, value.toString());
        }

        @Override
        public void mapObjectToColumn(Object value, String column, ContentValues values) {
            values.put(column, value.toString());
        }
    };

    public static final SqliteTypeAdapter<Date> DATE = new SqliteTypeAdapter<Date>(SqliteDataType.INTEGER) {
        @Override
        public void mapToObject(ResultSet result, int index, Field field, Object model) throws IllegalArgumentException, IllegalAccessException {
            field.setAccessible(true);
            String dateStr = result.getString(index);
            field.set(model, DateFormatter.parseStringAsDate(dateStr));
        }

        @Override
        public void mapToColumn(Date value, String column, ContentValues values) {
            values.put(column, DateFormatter.getDateAsISO8601String(value));
        }

        @Override
        public void mapObjectToColumn(Object value, String column, ContentValues values) {
            values.put(column, DateFormatter.getDateAsISO8601String((Date) value));
        }
    };
}
