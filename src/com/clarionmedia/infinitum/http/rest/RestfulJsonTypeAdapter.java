package com.clarionmedia.infinitum.http.rest;

import com.clarionmedia.infinitum.orm.persistence.TypeAdapter;

/**
 * <p>
 * Facilitates the mapping of Java data types to JSON.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 08/05/12
 * @since 1.0
 */
public abstract class RestfulJsonTypeAdapter<T> implements TypeAdapter<T> {

	/**
	 * Serializes the given value to JSON.
	 * 
	 * @param value
	 *            the value being serialized
	 */
	public abstract String serializeToJson(T value);

	/**
	 * Serializes the given {@link Object} value to JSON.
	 * 
	 * @param value
	 *            the value being serialized
	 */
	public abstract String serializeObjectToJson(Object value);

}
