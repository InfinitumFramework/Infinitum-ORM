/*
 * Copyright (C) 2012 Clarion Media, LLC
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

package com.clarionmedia.infinitum.orm.exception;

import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;

/**
 * <p>
 * Indicates there is an issue with the mapping of a {@code Field} in a
 * persistent domain model. Specifically, this may mean that Infinitum was
 * unable to map a {@code Field} to a column in the database. This can typically
 * be avoided by registering a {@link TypeAdapter} with the {@link SqliteMapper}
 * being used to map a model of the {@code Type} in question.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/14/12
 */
public class InvalidMappingException extends InfinitumRuntimeException {

	private static final long serialVersionUID = 470937993563172405L;

	/**
	 * Constructs a new {@code InvalidMappingException} with the given error
	 * message.
	 * 
	 * @param error
	 *            the error message for the {@link InfinitumRuntimeException}
	 */
	public InvalidMappingException(String error) {
		super(error);
	}

}
