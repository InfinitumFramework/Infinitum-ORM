package com.clarionmedia.infinitum.http.rest;

import com.clarionmedia.infinitum.orm.persistence.TypeAdapter;

/**
 * <p>
 * Facilitates the mapping of Java data types to XML.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 08/05/12
 * @since 1.0
 */
public abstract class RestfulXmlTypeAdapter<T> implements TypeAdapter<T> {

	/**
	 * Serializes the given value to XML.
	 * 
	 * @param value
	 *            the value being serialized
	 */
	public abstract String serializeToXml(T value);

	/**
	 * Serializes the given {@link Object} value to XML.
	 * 
	 * @param value
	 *            the value being serialized
	 */
	public abstract String serializeObjectToXml(Object value);

}
