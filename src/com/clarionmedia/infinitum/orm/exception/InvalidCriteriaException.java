/*
 * Copyright (c) 2012 Tyler Treat
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
 * Indicates there is an issue with a defined {@link Criteria} query instance.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/17/12
 */
public class InvalidCriteriaException extends InfinitumRuntimeException {

	private static final long serialVersionUID = -9089986309430815844L;

	/**
	 * Constructs a new {@code InvalidCriteriaException} with the given error
	 * message.
	 * 
	 * @param error
	 *            the error message for the {@link InfinitumRuntimeException}
	 */
	public InvalidCriteriaException(String error) {
		super(error);
	}

}
