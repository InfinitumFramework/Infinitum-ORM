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
			throw new InfinitumRuntimeException(
					"Autocommit is disabled, but there is no open transaction.");
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
	public static void checkPersistenceForModify(Object model,
			PersistencePolicy policy) {
		if (!policy.isPersistent(model.getClass()))
			throw new InfinitumRuntimeException(String.format(
					"Cannot modify transient class '%s'.", model.getClass()
							.getName()));
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
