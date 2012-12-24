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

package com.clarionmedia.infinitum.http.rest.impl;

import java.io.InputStream;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.params.HttpParams;

import com.clarionmedia.infinitum.http.rest.AuthenticationStrategy;
import com.clarionmedia.infinitum.http.rest.MessageConverter;
import com.clarionmedia.infinitum.http.rest.RestfulMappingClient;
import com.clarionmedia.infinitum.orm.context.InfinitumOrmContext;

/**
 * <p>
 * Implementation of {@link RestfulMappingClient} with caching support. This is
 * a thin layer which wraps {@link CachingEnabledRestfulClient} and converts its
 * responses into objects.
 * </p>
 * <p>
 * It uses a {@link MessageConverter} to perform conversions. If one is not
 * specified, either through the constructor or the setter, it will use a
 * {@link GsonMessageConverter} by default, meaning Gson must be on the class
 * path in this case.
 * </p>
 * 
 * @author Tyler
 * @version 1.0 12/23/12
 * @since 1.0
 */
public class CachingEnabledRestfulMappingClient implements RestfulMappingClient {

	private CachingEnabledRestfulClient mRestClient;
	private MessageConverter mMessageConverter;

	public CachingEnabledRestfulMappingClient(InfinitumOrmContext context) {
		mRestClient = new CachingEnabledRestfulClient(context);
		mMessageConverter = new GsonMessageConverter();
	}

	public CachingEnabledRestfulMappingClient(InfinitumOrmContext context, MessageConverter messageConverter) {
		mRestClient = new CachingEnabledRestfulClient(context);
		mMessageConverter = messageConverter;
	}

	@Override
	public <T> T executeGet(String uri, Class<T> responseType) {
		RestResponse response = mRestClient.executeGet(uri);
		return mMessageConverter.convert(responseType, response);
	}

	@Override
	public <T> T executeGet(String uri, Map<String, String> headers, Class<T> responseType) {
		RestResponse response = mRestClient.executeGet(uri, headers);
		return mMessageConverter.convert(responseType, response);
	}

	@Override
	public <T> T executePost(String uri, String messageBody, String contentType, Class<T> responseType) {
		RestResponse response = mRestClient.executePost(uri, messageBody, contentType);
		return mMessageConverter.convert(responseType, response);
	}

	@Override
	public <T> T executePost(String uri, String messageBody, String contentType, Map<String, String> headers, Class<T> responseType) {
		RestResponse response = mRestClient.executePost(uri, messageBody, contentType, headers);
		return mMessageConverter.convert(responseType, response);
	}

	@Override
	public <T> T executePost(String uri, HttpEntity httpEntity, Class<T> responseType) {
		RestResponse response = mRestClient.executePost(uri, httpEntity);
		return mMessageConverter.convert(responseType, response);
	}

	@Override
	public <T> T executePost(String uri, HttpEntity httpEntity, Map<String, String> headers, Class<T> responseType) {
		RestResponse response = mRestClient.executePost(uri, httpEntity, headers);
		return mMessageConverter.convert(responseType, response);
	}

	@Override
	public <T> T executePost(String uri, InputStream messageBody, int messageBodyLength, String contentType, Class<T> responseType) {
		RestResponse response = mRestClient.executePost(uri, messageBody, messageBodyLength, contentType);
		return mMessageConverter.convert(responseType, response);
	}

	@Override
	public <T> T executePost(String uri, InputStream messageBody, int messageBodyLength, String contentType, Map<String, String> headers,
			Class<T> responseType) {
		RestResponse response = mRestClient.executePost(uri, messageBody, messageBodyLength, contentType, headers);
		return mMessageConverter.convert(responseType, response);
	}

	@Override
	public RestResponse executeDelete(String uri) {
		return mRestClient.executeDelete(uri);
	}

	@Override
	public RestResponse executeDelete(String uri, Map<String, String> headers) {
		return mRestClient.executeDelete(uri, headers);
	}

	@Override
	public RestResponse executePut(String uri, String messageBody, String contentType) {
		return mRestClient.executePut(uri, messageBody, contentType);
	}

	@Override
	public RestResponse executePut(String uri, String messageBody, String contentType, Map<String, String> headers) {
		return mRestClient.executePut(uri, messageBody, contentType, headers);
	}

	@Override
	public RestResponse executePut(String uri, HttpEntity httpEntity) {
		return mRestClient.executePut(uri, httpEntity);
	}

	@Override
	public RestResponse executePut(String uri, HttpEntity httpEntity, Map<String, String> headers) {
		return mRestClient.executePut(uri, httpEntity, headers);
	}

	@Override
	public RestResponse executePut(String uri, InputStream messageBody, int messageBodyLength, String contentType) {
		return mRestClient.executePut(uri, messageBody, messageBodyLength, contentType);
	}

	@Override
	public RestResponse executePut(String uri, InputStream messageBody, int messageBodyLength, String contentType,
			Map<String, String> headers) {
		return mRestClient.executePut(uri, messageBody, messageBodyLength, contentType, headers);
	}

	@Override
	public RestResponse executeRequest(HttpUriRequest request) {
		return mRestClient.executeRequest(request);
	}

	@Override
	public <T> T executeRequest(HttpUriRequest request, Class<T> responseType) {
		RestResponse response = mRestClient.executeRequest(request);
		return mMessageConverter.convert(responseType, response);
	}

	@Override
	public void setConnectionTimeout(int timeout) {
		mRestClient.setConnectionTimeout(timeout);
	}

	@Override
	public void setResponseTimeout(int timeout) {
		mRestClient.setResponseTimeout(timeout);
	}

	@Override
	public void setHttpParams(HttpParams httpParams) {
		mRestClient.setHttpParams(httpParams);
	}

	@Override
	public void setAuthStrategy(AuthenticationStrategy authStrategy) {
		mRestClient.setAuthStrategy(authStrategy);
	}

	@Override
	public void setMessageConverter(MessageConverter messageConverter) {
		mMessageConverter = messageConverter;
	}

}
