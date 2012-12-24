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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.RequestWrapper;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.clarionmedia.infinitum.context.RestfulContext;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.http.impl.HashableHttpRequest;
import com.clarionmedia.infinitum.http.rest.AuthenticationStrategy;
import com.clarionmedia.infinitum.http.rest.RestfulClient;
import com.clarionmedia.infinitum.internal.DateFormatter;
import com.clarionmedia.infinitum.internal.caching.AbstractCache;
import com.clarionmedia.infinitum.logging.Logger;
import com.clarionmedia.infinitum.orm.context.OrmContext;

/**
 * <p>
 * Implementation of {@link RestfulClient} with caching support.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 07/04/12
 * @since 1.0
 */
public class CachingEnabledRestfulClient implements RestfulClient {

	protected Logger mLogger;
	protected HttpParams mHttpParams;
	protected RestResponseCache mResponseCache;
	protected boolean mIsAuthenticated;
	protected AuthenticationStrategy mAuthStrategy;

	/**
	 * Creates a new {@code CachingEnabledRestfulClient}.
	 */
	public CachingEnabledRestfulClient(OrmContext context) {
		mLogger = Logger.getInstance(context, getClass().getSimpleName());
		mHttpParams = new BasicHttpParams();
		mResponseCache = new RestResponseCache();
		mResponseCache.enableDiskCache(context.getAndroidContext(), AbstractCache.DISK_CACHE_INTERNAL);
		RestfulContext restContext = context.getRestfulConfiguration();
		if (restContext != null) {
			mIsAuthenticated = restContext.isRestAuthenticated();
			mAuthStrategy = context.getAuthStrategy();
		}
	}

	/**
	 * Clears the response cache.
	 */
	public void clearCache() {
		mResponseCache.clear();
	}

	@Override
	public RestResponse executeGet(String uri) {
		try {
			RequestWrapper request = new RequestWrapper(new HttpGet(uri));
			return executeRequest(new HashableHttpRequest(request));
		} catch (ProtocolException e) {
			throw new InfinitumRuntimeException("Unable to execute request", e);
		}
	}

	@Override
	public RestResponse executeGet(String uri, Map<String, String> headers) {
		HttpGet httpGet = new HttpGet(uri);
		for (Entry<String, String> header : headers.entrySet()) {
			httpGet.addHeader(header.getKey(), header.getValue());
		}
		try {
			RequestWrapper request = new RequestWrapper(httpGet);
			return executeRequest(new HashableHttpRequest(request));
		} catch (ProtocolException e) {
			throw new InfinitumRuntimeException("Unable to execute request", e);
		}
	}

	@Override
	public RestResponse executePost(String uri, String messageBody, String contentType) {
		HttpPost httpPost = new HttpPost(uri);
		httpPost.addHeader("content-type", contentType);
		try {
			httpPost.setEntity(new StringEntity(messageBody, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			mLogger.error("Unable to send POST request (could not encode message body)", e);
			return null;
		}
		try {
			RequestWrapper request = new RequestWrapper(httpPost);
			return executeRequest(new HashableHttpRequest(request));
		} catch (ProtocolException e) {
			throw new InfinitumRuntimeException("Unable to execute request", e);
		}
	}

	@Override
	public RestResponse executePost(String uri, String messageBody, String contentType, Map<String, String> headers) {
		HttpPost httpPost = new HttpPost(uri);
		for (Entry<String, String> header : headers.entrySet()) {
			httpPost.addHeader(header.getKey(), header.getValue());
		}
		httpPost.addHeader("content-type", contentType);
		try {
			httpPost.setEntity(new StringEntity(messageBody, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			mLogger.error("Unable to send POST request (could not encode message body)", e);
			return null;
		}
		try {
			RequestWrapper request = new RequestWrapper(httpPost);
			return executeRequest(new HashableHttpRequest(request));
		} catch (ProtocolException e) {
			throw new InfinitumRuntimeException("Unable to execute request", e);
		}
	}

	@Override
	public RestResponse executePost(String uri, HttpEntity httpEntity) {
		HttpPost httpPost = new HttpPost(uri);
		httpPost.addHeader("content-type", httpEntity.getContentType().getValue());
		httpPost.setEntity(httpEntity);
		try {
			RequestWrapper request = new RequestWrapper(httpPost);
			return executeRequest(new HashableHttpRequest(request));
		} catch (ProtocolException e) {
			throw new InfinitumRuntimeException("Unable to execute request", e);
		}
	}

	@Override
	public RestResponse executePost(String uri, HttpEntity httpEntity, Map<String, String> headers) {
		HttpPost httpPost = new HttpPost(uri);
		for (Entry<String, String> header : headers.entrySet()) {
			httpPost.addHeader(header.getKey(), header.getValue());
		}
		httpPost.setEntity(httpEntity);
		try {
			RequestWrapper request = new RequestWrapper(httpPost);
			return executeRequest(new HashableHttpRequest(request));
		} catch (ProtocolException e) {
			throw new InfinitumRuntimeException("Unable to execute request", e);
		}
	}

	@Override
	public RestResponse executePost(String uri, InputStream messageBody, int messageBodyLength, String contentType) {
		HttpPost httpPost = new HttpPost(uri);
		httpPost.addHeader("content-type", contentType);
		httpPost.setEntity(new InputStreamEntity(messageBody, messageBodyLength));
		try {
			RequestWrapper request = new RequestWrapper(httpPost);
			return executeRequest(new HashableHttpRequest(request));
		} catch (ProtocolException e) {
			throw new InfinitumRuntimeException("Unable to execute request", e);
		}
	}

	@Override
	public RestResponse executePost(String uri, InputStream messageBody, int messageBodyLength, String contentType,
			Map<String, String> headers) {
		HttpPost httpPost = new HttpPost(uri);
		for (Entry<String, String> header : headers.entrySet()) {
			httpPost.addHeader(header.getKey(), header.getValue());
		}
		httpPost.addHeader("content-type", contentType);
		httpPost.setEntity(new InputStreamEntity(messageBody, messageBodyLength));
		try {
			RequestWrapper request = new RequestWrapper(httpPost);
			return executeRequest(new HashableHttpRequest(request));
		} catch (ProtocolException e) {
			throw new InfinitumRuntimeException("Unable to execute request", e);
		}
	}

	@Override
	public RestResponse executeDelete(String uri) {
		try {
			RequestWrapper request = new RequestWrapper(new HttpDelete(uri));
			return executeRequest(new HashableHttpRequest(request));
		} catch (ProtocolException e) {
			throw new InfinitumRuntimeException("Unable to execute request", e);
		}
	}

	@Override
	public RestResponse executeDelete(String uri, Map<String, String> headers) {
		HttpDelete httpDelete = new HttpDelete(uri);
		for (Entry<String, String> header : headers.entrySet()) {
			httpDelete.addHeader(header.getKey(), header.getValue());
		}
		try {
			RequestWrapper request = new RequestWrapper(httpDelete);
			return executeRequest(new HashableHttpRequest(request));
		} catch (ProtocolException e) {
			throw new InfinitumRuntimeException("Unable to execute request", e);
		}
	}

	@Override
	public RestResponse executePut(String uri, String messageBody, String contentType) {
		HttpPut httpPut = new HttpPut(uri);
		httpPut.addHeader("content-type", contentType);
		try {
			httpPut.setEntity(new StringEntity(messageBody, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			mLogger.error("Unable to send PUT request (could not encode message body)", e);
			return null;
		}
		try {
			RequestWrapper request = new RequestWrapper(httpPut);
			return executeRequest(new HashableHttpRequest(request));
		} catch (ProtocolException e) {
			throw new InfinitumRuntimeException("Unable to execute request", e);
		}
	}

	@Override
	public RestResponse executePut(String uri, String messageBody, String contentType, Map<String, String> headers) {
		HttpPut httpPut = new HttpPut(uri);
		for (Entry<String, String> header : headers.entrySet()) {
			httpPut.addHeader(header.getKey(), header.getValue());
		}
		httpPut.addHeader("content-type", contentType);
		try {
			httpPut.setEntity(new StringEntity(messageBody, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			mLogger.error("Unable to send PUT request (could not encode message body)", e);
			return null;
		}
		try {
			RequestWrapper request = new RequestWrapper(httpPut);
			return executeRequest(new HashableHttpRequest(request));
		} catch (ProtocolException e) {
			throw new InfinitumRuntimeException("Unable to execute request", e);
		}
	}

	@Override
	public RestResponse executePut(String uri, HttpEntity httpEntity) {
		HttpPut httpPut = new HttpPut(uri);
		httpPut.addHeader("content-type", httpEntity.getContentType().getValue());
		httpPut.setEntity(httpEntity);
		try {
			RequestWrapper request = new RequestWrapper(httpPut);
			return executeRequest(new HashableHttpRequest(request));
		} catch (ProtocolException e) {
			throw new InfinitumRuntimeException("Unable to execute request", e);
		}
	}

	@Override
	public RestResponse executePut(String uri, HttpEntity httpEntity, Map<String, String> headers) {
		HttpPut httpPut = new HttpPut(uri);
		for (Entry<String, String> header : headers.entrySet()) {
			httpPut.addHeader(header.getKey(), header.getValue());
		}
		httpPut.setEntity(httpEntity);
		try {
			RequestWrapper request = new RequestWrapper(httpPut);
			return executeRequest(new HashableHttpRequest(request));
		} catch (ProtocolException e) {
			throw new InfinitumRuntimeException("Unable to execute request", e);
		}
	}

	@Override
	public RestResponse executePut(String uri, InputStream messageBody, int messageBodyLength, String contentType) {
		HttpPut httpPut = new HttpPut(uri);
		httpPut.addHeader("content-type", contentType);
		httpPut.setEntity(new InputStreamEntity(messageBody, messageBodyLength));
		try {
			RequestWrapper request = new RequestWrapper(httpPut);
			return executeRequest(new HashableHttpRequest(request));
		} catch (ProtocolException e) {
			throw new InfinitumRuntimeException("Unable to execute request", e);
		}
	}

	@Override
	public RestResponse executePut(String uri, InputStream messageBody, int messageBodyLength, String contentType,
			Map<String, String> headers) {
		HttpPut httpPut = new HttpPut(uri);
		for (Entry<String, String> header : headers.entrySet()) {
			httpPut.addHeader(header.getKey(), header.getValue());
		}
		httpPut.addHeader("content-type", contentType);
		httpPut.setEntity(new InputStreamEntity(messageBody, messageBodyLength));
		try {
			RequestWrapper request = new RequestWrapper(httpPut);
			return executeRequest(new HashableHttpRequest(request));
		} catch (ProtocolException e) {
			throw new InfinitumRuntimeException("Unable to execute request", e);
		}
	}

	@Override
	public RestResponse executeRequest(HttpUriRequest request) {
		try {
			RequestWrapper wrapped = new RequestWrapper(request);
			return executeRequest(new HashableHttpRequest(wrapped));
		} catch (ProtocolException e) {
			throw new InfinitumRuntimeException("Unable to execute request", e);
		}
	}

	@Override
	public void setConnectionTimeout(int timeout) {
		HttpConnectionParams.setConnectionTimeout(mHttpParams, timeout);
	}

	@Override
	public void setResponseTimeout(int timeout) {
		HttpConnectionParams.setSoTimeout(mHttpParams, timeout);
	}

	@Override
	public void setHttpParams(HttpParams httpParams) {
		mHttpParams = httpParams;
	}
	
	@Override
	public void setAuthStrategy(AuthenticationStrategy authStrategy) {
		mAuthStrategy = authStrategy;
		mIsAuthenticated = authStrategy != null;
	}

	private RestResponse executeRequest(HashableHttpRequest hashableHttpRequest) {
		if (mIsAuthenticated)
			mAuthStrategy.authenticate(hashableHttpRequest);
		if (mResponseCache.containsKey(hashableHttpRequest)) {
			RestResponse cachedResponse = mResponseCache.get(hashableHttpRequest);
			if (cachedResponse != null)
				return cachedResponse;
		}
		HttpUriRequest httpRequest = hashableHttpRequest.unwrap();
		mLogger.debug("Sending " + httpRequest.getMethod() + " request to " + httpRequest.getURI() + " with "
				+ httpRequest.getAllHeaders().length + " headers");
		HttpClient httpClient = new DefaultHttpClient(mHttpParams);
		try {
			HttpResponse response = httpClient.execute(httpRequest);
			RestResponse restResponse = new RestResponse(response);
			StatusLine statusLine = response.getStatusLine();
			restResponse.setStatusCode(statusLine.getStatusCode());
			HttpEntity entity = response.getEntity();
			if (entity == null) {
				restResponse.setResponseData(new byte[] {});
			} else {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				entity.writeTo(out);
				out.close();
				restResponse.setResponseData(out.toByteArray());
			}
			long expiration = getResponseExpiration(restResponse);
			if (expiration > 0)
				mResponseCache.put(hashableHttpRequest, restResponse, expiration);
			return restResponse;
		} catch (ClientProtocolException e) {
			mLogger.error("Unable to send " + httpRequest.getMethod() + " request", e);
			return null;
		} catch (IOException e) {
			mLogger.error("Unable to read web service response", e);
			return null;
		}
	}

	private long getResponseExpiration(RestResponse response) {
		long seconds = 0;
		try {
			for (Entry<String, String> header : response.getHeaders().entrySet()) {
				String name = header.getKey().trim();
				if (name.equalsIgnoreCase("cache-control")) {
					String[] values = header.getValue().split(",");
					for (String cacheControl : values) {
						cacheControl = cacheControl.trim();
						if (cacheControl.equalsIgnoreCase("no-cache") || cacheControl.equalsIgnoreCase("no-store")
								|| cacheControl.equalsIgnoreCase("must-revalidate"))
							return 0;
						if (cacheControl.toLowerCase(Locale.getDefault()).startsWith("max-age")) {
							seconds = Long.parseLong(cacheControl.split("=")[1].trim());
						}
					}
				} else if (name.equalsIgnoreCase("expires")) {
					Date expirationDate = DateFormatter.parseHttpExpiresStringAsDate(header.getValue().trim());
					long difference = expirationDate.getTime() - System.currentTimeMillis();
					if (difference > 0)
						seconds = difference;
				} else if (name.equalsIgnoreCase("pragma")) {
					if (header.getValue().trim().equalsIgnoreCase("no-cache"))
						return 0;
				}
			}
		} catch (Exception e) {
			mLogger.error("Unable to retrieve HTTP response expiration.");
		}
		return seconds;
	}

}
