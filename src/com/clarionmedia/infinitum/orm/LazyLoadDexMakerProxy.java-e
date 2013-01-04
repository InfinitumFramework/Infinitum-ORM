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

package com.clarionmedia.infinitum.orm;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import android.content.Context;

import com.clarionmedia.infinitum.di.DexMakerProxy;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.internal.Preconditions;
import com.clarionmedia.infinitum.internal.caching.DexCaching;
import com.google.dexmaker.stock.ProxyBuilder;

/**
 * <p>
 * Used to proxy an {@link Object} which has been configured to lazily load.
 * Every method called on the {@code Object} will pass through this
 * {@link InvocationHandler}. This handler is responsible for lazily loading the
 * {@code Object}, which occurs in {@link LazyLoadDexMakerProxy#loadObject()}.
 * </p>
 * <p>
 * The first method invocation made on a proxy will result in the proxied
 * {@code Object} being loaded and then the method will be passed to the loaded
 * {@code Object} for invocation. Any subsequent method invocation made on the
 * proxy will simply be intercepted and propagated to the {@code Object}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/12/12
 * @since 1.0
 */
public abstract class LazyLoadDexMakerProxy extends DexMakerProxy {

	protected Class<?> mType;

	/**
	 * Creates a new {@code LazyLoadDexMakerProxy}.
	 * 
	 * @param context
	 *            the {@link Context} used to retrieve the DEX bytecode cache
	 * @param type
	 *            the {@link Class} to proxy
	 */
	public LazyLoadDexMakerProxy(Context context, Class<?> type) {
		super(context, null);
		Preconditions.checkNotNull(type);
		mType = type;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		if (mTarget == null) {
			mTarget = loadObject();
		}
		return method.invoke(mTarget, args);
	}

	@Override
	public Object getProxy() {
		try {
			return ProxyBuilder.forClass(mType).handler(this)
					.dexCache(DexCaching.getDexCache(mContext)).build();
		} catch (IOException e) {
			throw new InfinitumRuntimeException("DEX cache was not writeable.");
		}
	}
	
	@Override
	public Object getTarget() {
		if (mTarget == null) {
		    mTarget = loadObject();
		}
		return mTarget;
	}
	
	@Override
	public LazyLoadDexMakerProxy clone() {
		throw new UnsupportedOperationException("Clone is not supported for LazyLoadDexMakerProxy!");
	}

	/**
	 * Loads the proxied {@link Object}.
	 * 
	 * @return {@code Object}
	 */
	protected abstract Object loadObject();

}
