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
import com.clarionmedia.infinitum.di.impl.GenericBeanDefinitionBuilder;
import com.clarionmedia.infinitum.http.rest.impl.RestfulJsonMapper;
import com.clarionmedia.infinitum.http.rest.impl.RestfulJsonSession;
import com.clarionmedia.infinitum.http.rest.impl.RestfulNameValueMapper;
import com.clarionmedia.infinitum.http.rest.impl.RestfulSession;
import com.clarionmedia.infinitum.http.rest.impl.RestfulXmlMapper;
import com.clarionmedia.infinitum.orm.Session;
import com.clarionmedia.infinitum.orm.context.OrmContext;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.impl.AnnotationsPersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.impl.DefaultTypeResolutionPolicy;
import com.clarionmedia.infinitum.orm.persistence.impl.XmlPersistencePolicy;
import com.clarionmedia.infinitum.orm.sqlite.SqliteUtil;
import com.clarionmedia.infinitum.orm.sqlite.impl.SqliteBuilder;
import com.clarionmedia.infinitum.orm.sqlite.impl.SqliteDbHelper;
import com.clarionmedia.infinitum.orm.sqlite.impl.SqliteMapper;
import com.clarionmedia.infinitum.orm.sqlite.impl.SqliteModelFactory;
import com.clarionmedia.infinitum.orm.sqlite.impl.SqliteSession;
import com.clarionmedia.infinitum.orm.sqlite.impl.SqliteTemplate;

public class OrmContextImpl implements OrmContext {

	private static PersistencePolicy sPersistencePolicy;

	private XmlApplicationContext mParentContext;
	private List<InfinitumContext> mChildContexts;

	public OrmContextImpl(XmlApplicationContext parentContext) {
		mParentContext = parentContext;
		mChildContexts = new ArrayList<InfinitumContext>();
	}

	@Override
	public Session getSession(DataSource source) throws InfinitumConfigurationException {
		switch (source) {
		case SQLITE:
			return getBean("$SqliteSession", SqliteSession.class);
		case REST:
			String client = getRestfulConfiguration().getClientBean();
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
		if (sPersistencePolicy == null) {
			switch (getConfigurationMode()) {
			case ANNOTATION:
				sPersistencePolicy = getBean("$AnnotationsPersistencePolicy", AnnotationsPersistencePolicy.class);
				break;
			case XML:
				sPersistencePolicy = getBean("$XmlPersistencePolicy", XmlPersistencePolicy.class);
				break;
			}
		}
		return sPersistencePolicy;
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
	public RestfulContext getRestfulConfiguration() {
		return mParentContext.getRestContext();
	}

	@Override
	public void postProcess(Context context) {
		registerOrmComponents();
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

	private void registerOrmComponents() {
		BeanFactory beanFactory = mParentContext.getBeanFactory();
		BeanDefinitionBuilder beanDefinitionBuilder = new GenericBeanDefinitionBuilder(beanFactory);
		AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.setName("$OrmContext").setType(OrmContextImpl.class).build();
		beanFactory.registerBean(beanDefinition);
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("mIsAutocommit", isAutocommit());
		beanDefinition = beanDefinitionBuilder.setName("$SqliteTemplate").setType(SqliteTemplate.class).setProperties(properties).build();
		beanFactory.registerBean(beanDefinition);
		beanDefinition = beanDefinitionBuilder.setName("$SqliteSession").setType(SqliteSession.class).build();
		beanFactory.registerBean(beanDefinition);
		beanDefinition = beanDefinitionBuilder.setName("$SqliteDbHelper").setType(SqliteDbHelper.class).build();
		beanFactory.registerBean(beanDefinition);
		beanDefinition = beanDefinitionBuilder.setName("$SqliteMapper").setType(SqliteMapper.class).build();
		beanFactory.registerBean(beanDefinition);
		beanDefinition = beanDefinitionBuilder.setName("$TypeResolutionPolicy").setType(DefaultTypeResolutionPolicy.class).build();
		beanFactory.registerBean(beanDefinition);
		beanDefinition = beanDefinitionBuilder.setName("$SqliteModelFactory").setType(SqliteModelFactory.class).build();
		beanFactory.registerBean(beanDefinition);
		beanDefinition = beanDefinitionBuilder.setName("$SqliteBuilder").setType(SqliteBuilder.class).build();
		beanFactory.registerBean(beanDefinition);
		beanDefinition = beanDefinitionBuilder.setName("$SqliteUtil").setType(SqliteUtil.class).build();
		beanFactory.registerBean(beanDefinition);
		beanDefinition = beanDefinitionBuilder.setName("$RestfulXmlMapper").setType(RestfulXmlMapper.class).build();
		beanFactory.registerBean(beanDefinition);
		beanDefinition = beanDefinitionBuilder.setName("$RestfulJsonMapper").setType(RestfulJsonMapper.class).build();
		beanFactory.registerBean(beanDefinition);
		beanDefinition = beanDefinitionBuilder.setName("$RestfulNameValueMapper").setType(RestfulNameValueMapper.class).build();
		beanFactory.registerBean(beanDefinition);
		beanDefinition = beanDefinitionBuilder.setName("$RestfulJsonSession").setType(RestfulJsonSession.class).build();
		beanFactory.registerBean(beanDefinition);
		Class<?> type = getConfigurationMode() == ConfigurationMode.ANNOTATION ? AnnotationsPersistencePolicy.class
				: XmlPersistencePolicy.class;
		beanDefinition = beanDefinitionBuilder.setName("$PersistencePolicy").setType(type).build();
		beanFactory.registerBean(beanDefinition);
	}

}
