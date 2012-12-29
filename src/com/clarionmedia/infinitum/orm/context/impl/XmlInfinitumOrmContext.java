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

package com.clarionmedia.infinitum.orm.context.impl;

import static java.lang.Boolean.parseBoolean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.context.RestfulContext;
import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;
import com.clarionmedia.infinitum.context.impl.XmlApplicationContext;
import com.clarionmedia.infinitum.di.AbstractBeanDefinition;
import com.clarionmedia.infinitum.di.BeanDefinitionBuilder;
import com.clarionmedia.infinitum.di.BeanFactory;
import com.clarionmedia.infinitum.orm.Session;
import com.clarionmedia.infinitum.orm.context.InfinitumOrmContext;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.impl.AnnotationsPersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.impl.DefaultTypeResolutionPolicy;
import com.clarionmedia.infinitum.orm.persistence.impl.XmlPersistencePolicy;
import com.clarionmedia.infinitum.orm.rest.impl.RestfulJsonMapper;
import com.clarionmedia.infinitum.orm.rest.impl.RestfulJsonSession;
import com.clarionmedia.infinitum.orm.rest.impl.RestfulNameValueMapper;
import com.clarionmedia.infinitum.orm.rest.impl.RestfulSession;
import com.clarionmedia.infinitum.orm.rest.impl.RestfulXmlMapper;
import com.clarionmedia.infinitum.orm.sqlite.SqliteUtil;
import com.clarionmedia.infinitum.orm.sqlite.impl.SqliteBuilder;
import com.clarionmedia.infinitum.orm.sqlite.impl.SqliteMapper;
import com.clarionmedia.infinitum.orm.sqlite.impl.SqliteModelFactory;
import com.clarionmedia.infinitum.orm.sqlite.impl.SqliteSession;
import com.clarionmedia.infinitum.orm.sqlite.impl.SqliteTemplate;

/**
 * <p>
 * Implementation of {@link InfinitumOrmContext} which is initialized through
 * XML as a child of an {@link XmlApplicationContext} instance.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 12/23/12
 * @since 1.0
 */
public class XmlInfinitumOrmContext implements InfinitumOrmContext {

	private XmlApplicationContext mParentContext;
	private List<InfinitumContext> mChildContexts;

	/**
	 * Creates a new {@code XmlInfinitumOrmContext} instance as a child of the
	 * given {@link XmlApplicationContext}.
	 * 
	 * @param parentContext
	 *            the parent of this context
	 */
	public XmlInfinitumOrmContext(XmlApplicationContext parentContext) {
		mParentContext = parentContext;
		mChildContexts = new ArrayList<InfinitumContext>();
	}

	@Override
	public void postProcess(Context context) {
	}

	@Override
	public List<AbstractBeanDefinition> getBeans(BeanDefinitionBuilder beanDefinitionBuilder) {
		List<AbstractBeanDefinition> beans = new ArrayList<AbstractBeanDefinition>();
		beans.add(beanDefinitionBuilder.setName("$OrmContext").setType(XmlInfinitumOrmContext.class).build());
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("mIsAutocommit", isAutocommit());
		beans.add(beanDefinitionBuilder.setName("$SqliteTemplate").setType(SqliteTemplate.class).setProperties(properties).build());
		beans.add(beanDefinitionBuilder.setName("$SqliteSession").setType(SqliteSession.class).build());
		beans.add(beanDefinitionBuilder.setName("$SqliteMapper").setType(SqliteMapper.class).build());
		beans.add(beanDefinitionBuilder.setName("$TypeResolutionPolicy").setType(DefaultTypeResolutionPolicy.class).build());
		beans.add(beanDefinitionBuilder.setName("$SqliteModelFactory").setType(SqliteModelFactory.class).build());
		beans.add(beanDefinitionBuilder.setName("$SqliteBuilder").setType(SqliteBuilder.class).build());
		beans.add(beanDefinitionBuilder.setName("$SqliteUtil").setType(SqliteUtil.class).build());
		beans.add(beanDefinitionBuilder.setName("$RestfulXmlMapper").setType(RestfulXmlMapper.class).build());
		beans.add(beanDefinitionBuilder.setName("$RestfulJsonMapper").setType(RestfulJsonMapper.class).build());
		beans.add(beanDefinitionBuilder.setName("$RestfulNameValueMapper").setType(RestfulNameValueMapper.class).build());
		beans.add(beanDefinitionBuilder.setName("$RestfulJsonSession").setType(RestfulJsonSession.class).build());
		Class<?> type = getConfigurationMode() == ConfigurationMode.ANNOTATION ? AnnotationsPersistencePolicy.class
				: XmlPersistencePolicy.class;
		beans.add(beanDefinitionBuilder.setName("$PersistencePolicy").setType(type).build());
		return beans;
	}

	@Override
	public Session getSession(DataSource source) throws InfinitumConfigurationException {
		switch (source) {
		case SQLITE:
			return getBean("$SqliteSession", SqliteSession.class);
		case REST:
			String client = mParentContext.getRestContext().getClientBean();
			RestfulSession session;
			if (client == null) {
				// Use RestfulJsonSession if no client is defined
				session = getBean("$RestfulJsonSession", RestfulJsonSession.class);
			} else {
				// Otherwise use the preferred client
				session = getBean(client, RestfulSession.class);
			}
			return session;
		default:
			throw new InfinitumConfigurationException("Data source not configured.");
		}
	}

	@Override
	public PersistencePolicy getPersistencePolicy() {
		return getBean("$PersistencePolicy", PersistencePolicy.class);
	}

	@Override
	public ConfigurationMode getConfigurationMode() {
		String mode = mParentContext.getAppConfig().get("mode");
		if (mode == null)
			return ConfigurationMode.ANNOTATION;
		if (mode.equalsIgnoreCase(ConfigurationMode.XML.toString()))
			return ConfigurationMode.XML;
		else if (mode.equalsIgnoreCase(ConfigurationMode.ANNOTATION.toString()))
			return ConfigurationMode.ANNOTATION;
		throw new InfinitumConfigurationException("Unknown configuration mode '" + mode + "'.");
	}

	@Override
	public boolean hasSqliteDb() {
		return mParentContext.getSqliteConfig() != null;
	}

	@Override
	public String getSqliteDbName() {
		String dbName = mParentContext.getSqliteConfig().get("dbName");
		if (dbName == null || dbName.length() == 0)
			throw new InfinitumConfigurationException("SQLite database name not specified.");
		return dbName;
	}

	@Override
	public int getSqliteDbVersion() {
		String dbVersion = mParentContext.getSqliteConfig().get("dbVersion");
		if (dbVersion == null)
			throw new InfinitumConfigurationException("SQLite database version not specified.");
		return Integer.parseInt(dbVersion);
	}

	@Override
	public List<String> getDomainTypes() {
		List<String> models = new ArrayList<String>();
		for (XmlApplicationContext.Model model : mParentContext.getModels()) {
			models.add(model.getResource());
		}
		return models;
	}

	@Override
	public boolean isSchemaGenerated() {
		String isGenerated = mParentContext.getSqliteConfig().get("generateSchema");
		if (isGenerated == null)
			return true;
		return parseBoolean(isGenerated);
	}

	@Override
	public boolean isAutocommit() {
		String autocommit = mParentContext.getSqliteConfig().get("autocommit");
		if (autocommit == null)
			return true;
		return parseBoolean(autocommit);
	}

	@Override
	public boolean isDebug() {
		return mParentContext.isDebug();
	}

	@Override
	public Context getAndroidContext() {
		return mParentContext.getAndroidContext();
	}

	@Override
	public BeanFactory getBeanFactory() {
		return mParentContext.getBeanFactory();
	}

	@Override
	public Object getBean(String name) {
		return mParentContext.getBean(name);
	}

	@Override
	public <T> T getBean(String name, Class<T> clazz) {
		return mParentContext.getBean(name, clazz);
	}

	@Override
	public boolean isComponentScanEnabled() {
		return mParentContext.isComponentScanEnabled();
	}

	@Override
	public List<InfinitumContext> getChildContexts() {
		return mChildContexts;
	}

	@Override
	public void addChildContext(InfinitumContext context) {
		mChildContexts.add(context);
	}

	@Override
	public InfinitumContext getParentContext() {
		return mParentContext;
	}

	@Override
	public RestfulContext getRestContext() {
		return mParentContext.getRestContext();
	}

}
