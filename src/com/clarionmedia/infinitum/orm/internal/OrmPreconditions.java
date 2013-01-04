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

package com.clarionmedia.infinitum.orm.internal;

import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;

/**
 * <p>
 * Utility class used to check method preconditions.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/18/12
 */
public class OrmPreconditions {

	/**
	 * Verifies that if autocommit is disabled, a transaction is open.
	 * 
	 * @param autocommit
	 *            indicates if autocommit is enabled or disabled
	 * @param txOpen
	 *            indicates if there is a transaction open or not
	 */
	public static void checkForTransaction(boolean autocommit, boolean txOpen) {
		if (!autocommit && !txOpen)
			throw new InfinitumRuntimeException("Autocommit is disabled, but there is no open transaction.");
	}

	/**
	 * Verifies that a session is open.
	 * 
	 * @param isOpen
	 *            indicates if session is open
	 */
	public static void checkForOpenSession(boolean isOpen) {
		if (!isOpen)
			throw new InfinitumRuntimeException("Session is not open.");
	}

	/**
	 * Verifies that the given model is persistent and can be saved, updated, or
	 * deleted.
	 * 
	 * @param model
	 *            model to check persistence for
	 * @param policy
	 *            {@link PersistencePolicy} to use
	 */
	public static void checkPersistenceForModify(Object model, PersistencePolicy policy) {
		if (!policy.isPersistent(model.getClass()))
			throw new InfinitumRuntimeException(String.format("Cannot modify transient class '%s'.", model.getClass().getName()));
	}

	/**
	 * Verifies that the given model {@link Class} is persistent and can be
	 * loaded.
	 * 
	 * @param c
	 *            {@code Class} to check persistence for
	 * @param policy
	 *            {@link PersistencePolicy} to use
	 */
	public static void checkPersistenceForLoading(Class<?> c, PersistencePolicy policy) {
		if (!policy.isPersistent(c))
			throw new InfinitumRuntimeException(String.format("Cannot load transient class '%s'.", c.getName()));
	}

}
