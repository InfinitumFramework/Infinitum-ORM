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
 * Indicates there is an issue with the way a persistent domain model is
 * configured, either through annotations or XML mappings.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/13/12
 * @since 1.0
 */
public class ModelConfigurationException extends InfinitumRuntimeException {

	private static final long serialVersionUID = 470937993563172405L;

	/**
	 * Constructs a new {@code ModelConfigurationException} with the given error
	 * message.
	 * 
	 * @param error
	 *            the error message for the {@link ModelConfigurationException}
	 */
	public ModelConfigurationException(String error) {
		super(error);
	}
	
	/**
	 * Constructs a new {@code ModelConfigurationException} with the given error
	 * message.
	 * 
	 * @param error
	 *            the error message for the {@link ModelConfigurationException}
	 * @param throwable
	 *            the cause of this exception
	 */
	public ModelConfigurationException(String error, Throwable throwable) {
		super(error, throwable);
	}

}
