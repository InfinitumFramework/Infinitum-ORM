package com.clarionmedia.infinitum.http.rest;

import java.util.List;

import org.apache.http.NameValuePair;

import com.clarionmedia.infinitum.orm.persistence.TypeAdapter;

/**
 * <p>
 * Facilitates the mapping of Java data types to RESTful web service name-value
 * pair fields.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/21/12
 * @since 1.0
 */
public abstract class RestfulPairsTypeAdapter<T> implements TypeAdapter<T> {

	/**
	 * Maps the given value to the given web service name-value pair field.
	 * 
	 * @param value
	 *            the value being mapped
	 * @param field
	 *            the name-value pair field being mapped to
	 * @param pairs
	 *            {@code NameValuePairs} containing the data mappings for the
	 *            model
	 */
	public abstract void mapToPair(T value, String field,
			List<NameValuePair> pairs);

	/**
	 * Maps the given {@link Object} value to the given web service name-value
	 * pair field.
	 * 
	 * @param value
	 *            the value being mapped
	 * @param field
	 *            the name-value pair field being mapped to
	 * @param pairs
	 *            {@code NameValuePairs} containing the data mappings for the
	 *            model
	 */
	public abstract void mapObjectToPair(Object value, String field,
			List<NameValuePair> pairs);

}
