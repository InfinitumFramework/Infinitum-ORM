/*
 * Copyright (C) 2012 Clarion Media, LLC
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

package com.clarionmedia.infinitum.orm.sqlite.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.clarionmedia.infinitum.logging.Logger;
import com.clarionmedia.infinitum.orm.ResultSet;
import com.clarionmedia.infinitum.orm.Session;
import com.clarionmedia.infinitum.orm.context.InfinitumOrmContext;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.TypeResolutionPolicy.SqliteDataType;
import com.clarionmedia.infinitum.orm.rest.RestfulPairsTypeAdapter;
import com.clarionmedia.infinitum.orm.sqlite.SqliteTypeAdapter;
import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class SqliteSessionTest {
	
	private static final int FOO_MODEL_HASH = 42;
	private static final int BAR_MODEL_HASH = 38;
	private static final int BAZ_MODEL_HASH = 176;
	private static final long FOO_MODEL_ID = 120;
	private static final long BAZ_MODEL_ID = 211;
	
	@Mock
	private InfinitumOrmContext mockInfinitumContext = mock(InfinitumOrmContext.class);
	
	@Mock
	private SqliteTemplate mockSqliteTemplate;
	
	@Mock
	private PersistencePolicy mockPersistencePolicy;
	
	@Mock
	private Logger mockLogger;
	
	@Mock
	private Context mockContext;
	
	@Mock
	private Map<Integer, Object> mockSessionCache;
	
	@Mock
	private Criteria<FooModel> mockFooCriteria;
	
	@Mock
	private Cursor mockCursor;
	
	@Mock
	private SqliteMapper mockSqliteMapper;
	
	@Mock
	private SQLiteDatabase mockSqliteDatabase;
	
	@InjectMocks
	private SqliteSession sqliteSession = new SqliteSession();
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(mockInfinitumContext.getPersistencePolicy()).thenReturn(mockPersistencePolicy);
		sqliteSession.open();
	}
	
	@After
	public void tearDown() {
		sqliteSession.close();
	}
	
	@Test
	public void testOpen() {
		// Run
		Session session = sqliteSession.open();
		
		// Verify
		verify(mockSqliteTemplate, times(2)).open();
		assertEquals("Session returned from open should be the same Session instance", sqliteSession, session);
	}
	
	@Test
	public void testClose() {
		/// Run
		Session session = sqliteSession.close();
		
		// Verify
		verify(mockSqliteTemplate).close();
		assertEquals("Session returned from close should be the same Session instance", sqliteSession, session);
	}
	
	@Test
	public void testIsOpen() {
		// Setup
		when(mockSqliteTemplate.isOpen()).thenReturn(true);
		
		// Run
		boolean isOpen = sqliteSession.isOpen();
		
		// Verify
		verify(mockSqliteTemplate).isOpen();
		assertTrue("Session should be open", isOpen);
	}
	
	@Test
	public void testRecycleCache() {
		// Run
		Session session = sqliteSession.recycleCache();
		
		// Verify
		verify(mockSessionCache).clear();
		assertEquals("Session returned from recycleCache should be the same Session instance", sqliteSession, session);
	}
	
	@Test
	public void testCreateCriteria() {
		// Setup
		when(mockSqliteTemplate.createCriteria(FooModel.class)).thenReturn(mockFooCriteria);
		
		// Run
		Criteria<FooModel> actualCriteria = sqliteSession.createCriteria(FooModel.class);
		
		// Verify
		verify(mockSqliteTemplate).createCriteria(FooModel.class);
		assertEquals("Criteria returned from createCriteria should be the same Criteria instance returned from mockSqliteTemplate", mockFooCriteria, actualCriteria);
	}
	
	@Test
	public void testSave_success() {
		// Setup
		FooModel foo = new FooModel();
		when(mockSqliteTemplate.save(foo)).thenReturn(FOO_MODEL_ID);
		when(mockPersistencePolicy.computeModelHash(foo)).thenReturn(FOO_MODEL_HASH);
		
		// Run
		long actualId = sqliteSession.save(foo);
		
		// Verify
		verify(mockSqliteTemplate).save(foo);
		verify(mockPersistencePolicy).computeModelHash(foo);
		verify(mockSessionCache).put(FOO_MODEL_HASH, foo);
		assertEquals("Returned ID should be equal to the model ID", FOO_MODEL_ID, actualId);
	}
	
	@Test
	public void testSave_fail() {
		// Setup
		FooModel foo = new FooModel();
		when(mockSqliteTemplate.save(foo)).thenReturn((long) -1);
		
		// Run
		long actualId = sqliteSession.save(foo);
		
		// Verify
		verify(mockSqliteTemplate).save(foo);
		verify(mockPersistencePolicy, times(0)).computeModelHash(foo);
		verify(mockSessionCache, times(0)).put(FOO_MODEL_HASH, foo);
		assertEquals("Returned ID should be -1", -1, actualId);
	}
	
	@Test
	public void testUpdate_success() {
		// Setup
		FooModel foo = new FooModel();
		when(mockSqliteTemplate.update(foo)).thenReturn(true);
		when(mockPersistencePolicy.computeModelHash(foo)).thenReturn(FOO_MODEL_HASH);
		
		// Run
		boolean success = sqliteSession.update(foo);
		
		// Verify
		verify(mockSqliteTemplate).update(foo);
		verify(mockPersistencePolicy).computeModelHash(foo);
		verify(mockSessionCache).put(FOO_MODEL_HASH, foo);
		assertEquals("Save should have returned successfully", true, success);
	}
	
	@Test
	public void testUpdate_fail() {
		// Setup
		FooModel foo = new FooModel();
		when(mockSqliteTemplate.update(foo)).thenReturn(false);
		
		// Run
		boolean success = sqliteSession.update(foo);
		
		// Verify
		verify(mockSqliteTemplate).update(foo);
		verify(mockPersistencePolicy, times(0)).computeModelHash(foo);
		verify(mockSessionCache, times(0)).put(FOO_MODEL_HASH, foo);
		assertEquals("Save should have returned unsuccessfully", false, success);
	}
	
	@Test
	public void testDelete_success() {
		// Setup
		FooModel foo = new FooModel();
		when(mockSqliteTemplate.delete(foo)).thenReturn(true);
		when(mockPersistencePolicy.computeModelHash(foo)).thenReturn(FOO_MODEL_HASH);
		
		// Run
		boolean success = sqliteSession.delete(foo);
		
		// Verify
		verify(mockSqliteTemplate).delete(foo);
		verify(mockPersistencePolicy).computeModelHash(foo);
		verify(mockSessionCache).remove(FOO_MODEL_HASH);
		assertEquals("Delete should have returned successfully", true, success);
	}
	
	@Test
	public void testDelete_fail() {
		// Setup
		FooModel foo = new FooModel();
		when(mockSqliteTemplate.delete(foo)).thenReturn(false);
		
		// Run
		boolean success = sqliteSession.delete(foo);
		
		// Verify
		verify(mockSqliteTemplate).delete(foo);
		verify(mockPersistencePolicy, times(0)).computeModelHash(foo);
		verify(mockSessionCache, times(0)).remove(FOO_MODEL_HASH);
		assertEquals("Delete should have returned unsuccessfully", false, success);
	}
	
	@Test
	public void testSaveOrUpdate_success() {
		// Setup
		FooModel foo = new FooModel();
		when(mockSqliteTemplate.saveOrUpdate(foo)).thenReturn(FOO_MODEL_ID);
		when(mockPersistencePolicy.computeModelHash(foo)).thenReturn(FOO_MODEL_HASH);
		
		// Run
		long actualId = sqliteSession.saveOrUpdate(foo);
		
		// Verify
		verify(mockSqliteTemplate).saveOrUpdate(foo);
		verify(mockPersistencePolicy).computeModelHash(foo);
		verify(mockSessionCache).put(FOO_MODEL_HASH, foo);
		assertEquals("Returned ID should be equal to the model ID", FOO_MODEL_ID, actualId);
	}
	
	@Test
	public void testSaveOrUpdate_fail() {
		// Setup
		FooModel foo = new FooModel();
		when(mockSqliteTemplate.saveOrUpdate(foo)).thenReturn((long) -1);
		
		// Run
		long actualId = sqliteSession.saveOrUpdate(foo);
		
		// Verify
		verify(mockSqliteTemplate).saveOrUpdate(foo);
		verify(mockPersistencePolicy, times(0)).computeModelHash(foo);
		verify(mockSessionCache, times(0)).put(FOO_MODEL_HASH, foo);
		assertEquals("Returned ID should be -1", -1, actualId);
	}
	
	@Test
	public void testSaveOrUpdateAll() {
		// Setup
		FooModel foo = new FooModel();
		BarModel bar = new BarModel();
		BazModel baz = new BazModel();
		List<Object> models = new ArrayList<Object>();
		models.add(foo);
		models.add(bar);
		models.add(baz);
		when(mockSqliteTemplate.saveOrUpdate(foo)).thenReturn(FOO_MODEL_ID);
		when(mockSqliteTemplate.saveOrUpdate(bar)).thenReturn((long) -1);
		when(mockSqliteTemplate.saveOrUpdate(baz)).thenReturn((long) 0);
		when(mockPersistencePolicy.computeModelHash(foo)).thenReturn(FOO_MODEL_HASH);
		when(mockPersistencePolicy.computeModelHash(baz)).thenReturn(BAZ_MODEL_HASH);
		
		// Run
		int actualResults = sqliteSession.saveOrUpdateAll(models);
		
		// Verify
		verify(mockSqliteTemplate).saveOrUpdate(foo);
		verify(mockSqliteTemplate).saveOrUpdate(bar);
		verify(mockSqliteTemplate).saveOrUpdate(baz);
		verify(mockPersistencePolicy).computeModelHash(foo);
		verify(mockPersistencePolicy).computeModelHash(baz);
		verify(mockPersistencePolicy, times(0)).computeModelHash(bar);
		verify(mockSessionCache).put(FOO_MODEL_HASH, foo);
		verify(mockSessionCache).put(BAZ_MODEL_HASH, baz);
		verify(mockSessionCache, times(0)).put(BAR_MODEL_HASH, bar);
		assertEquals("Number of items saved or updated should be 2", 2, actualResults);
	}
	
	@Test
	public void testSaveAll() {
		// Setup
		FooModel foo = new FooModel();
		BarModel bar = new BarModel();
		BazModel baz = new BazModel();
		List<Object> models = new ArrayList<Object>();
		models.add(foo);
		models.add(bar);
		models.add(baz);
		when(mockSqliteTemplate.save(foo)).thenReturn(FOO_MODEL_ID);
		when(mockSqliteTemplate.save(bar)).thenReturn((long) -1);
		when(mockSqliteTemplate.save(baz)).thenReturn(BAZ_MODEL_ID);
		when(mockPersistencePolicy.computeModelHash(foo)).thenReturn(FOO_MODEL_HASH);
		when(mockPersistencePolicy.computeModelHash(baz)).thenReturn(BAZ_MODEL_HASH);
		
		// Run
		int actualResults = sqliteSession.saveAll(models);
		
		// Verify
		verify(mockSqliteTemplate).save(foo);
		verify(mockSqliteTemplate).save(bar);
		verify(mockSqliteTemplate).save(baz);
		verify(mockPersistencePolicy).computeModelHash(foo);
		verify(mockPersistencePolicy).computeModelHash(baz);
		verify(mockPersistencePolicy, times(0)).computeModelHash(bar);
		verify(mockSessionCache).put(FOO_MODEL_HASH, foo);
		verify(mockSessionCache).put(BAZ_MODEL_HASH, baz);
		verify(mockSessionCache, times(0)).put(BAR_MODEL_HASH, bar);
		assertEquals("Number of items saved should be 2", 2, actualResults);
	}
	
	@Test
	public void testDeleteAll() {
		// Setup
		FooModel foo = new FooModel();
		BarModel bar = new BarModel();
		BazModel baz = new BazModel();
		List<Object> models = new ArrayList<Object>();
		models.add(foo);
		models.add(bar);
		models.add(baz);
		when(mockSqliteTemplate.delete(foo)).thenReturn(true);
		when(mockSqliteTemplate.delete(bar)).thenReturn(false);
		when(mockSqliteTemplate.delete(baz)).thenReturn(true);
		when(mockPersistencePolicy.computeModelHash(foo)).thenReturn(FOO_MODEL_HASH);
		when(mockPersistencePolicy.computeModelHash(baz)).thenReturn(BAZ_MODEL_HASH);
		
		// Run
		int actualResults = sqliteSession.deleteAll(models);
		
		// Verify
		verify(mockSqliteTemplate).delete(foo);
		verify(mockSqliteTemplate).delete(bar);
		verify(mockSqliteTemplate).delete(baz);
		verify(mockPersistencePolicy).computeModelHash(foo);
		verify(mockPersistencePolicy).computeModelHash(baz);
		verify(mockPersistencePolicy, times(0)).computeModelHash(bar);
		verify(mockSessionCache).remove(FOO_MODEL_HASH);
		verify(mockSessionCache).remove(BAZ_MODEL_HASH);
		verify(mockSessionCache, times(0)).remove(BAR_MODEL_HASH);
		assertEquals("Number of items deleted should be 2", 2, actualResults);
	}
	
	@Test
	public void testLoad_cacheHit() {
		// Setup
		FooModel expected = new FooModel();
		expected.id = FOO_MODEL_ID;
		when(mockPersistencePolicy.computeModelHash(FooModel.class, FOO_MODEL_ID)).thenReturn(FOO_MODEL_HASH);
		when(mockSessionCache.containsKey(FOO_MODEL_HASH)).thenReturn(true);
		when(mockSessionCache.get(FOO_MODEL_HASH)).thenReturn(expected);
		
		// Run
		FooModel actual = sqliteSession.load(FooModel.class, FOO_MODEL_ID);
		
		// Verify
		verify(mockPersistencePolicy).computeModelHash(FooModel.class, FOO_MODEL_ID);
		verify(mockSessionCache).containsKey(FOO_MODEL_HASH);
		verify(mockSessionCache).get(FOO_MODEL_HASH);
		verify(mockSqliteTemplate, times(0)).load(FooModel.class, FOO_MODEL_ID);
		assertEquals("Loaded object ID should be equal to expected object ID", expected.id, actual.id);
	}
	
	@Test
	public void testLoad_cacheMiss() {
		// Setup
		FooModel expected = new FooModel();
		expected.id = FOO_MODEL_ID;
		when(mockPersistencePolicy.computeModelHash(FooModel.class, FOO_MODEL_ID)).thenReturn(FOO_MODEL_HASH);
		when(mockSessionCache.containsKey(FOO_MODEL_HASH)).thenReturn(false);
		when(mockSqliteTemplate.load(FooModel.class, FOO_MODEL_ID)).thenReturn(expected);
		
		// Run
		FooModel actual = sqliteSession.load(FooModel.class, FOO_MODEL_ID);
		
		// Verify
		verify(mockPersistencePolicy).computeModelHash(FooModel.class, FOO_MODEL_ID);
		verify(mockSessionCache).containsKey(FOO_MODEL_HASH);
		verify(mockSessionCache, times(0)).get(FOO_MODEL_HASH);
		verify(mockSqliteTemplate).load(FooModel.class, FOO_MODEL_ID);
		assertEquals("Loaded object ID should be equal to expected object ID", expected.id, actual.id);
	}
	
	@Test
	public void testExecute() {
		// Setup
		String sql = "delete from foo";
		
		// Run
		Session session = sqliteSession.execute(sql);
		
		// Verify
		verify(mockSqliteTemplate).execute(sql);
		assertEquals("Session returned from execute should be the same Session instance", sqliteSession, session);
	}
	
	@Test
	public void testRegisterTypeAdapter_sqliteTypeAdapter() {
		// Setup
		SqliteTypeAdapter<Date> adapter = new SqliteTypeAdapter<Date>(SqliteDataType.TEXT) {
			@Override
			public void mapToObject(ResultSet result, int index, Field field,
					Object model) throws IllegalArgumentException,
					IllegalAccessException {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void mapToColumn(Date value, String column,
					ContentValues values) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void mapObjectToColumn(Object value, String column,
					ContentValues values) {
				// TODO Auto-generated method stub
				
			}
		};
		
		// Run
		Session session = sqliteSession.registerTypeAdapter(Date.class, adapter);
		
		// Verify
		verify(mockSqliteTemplate).registerTypeAdapter(Date.class, adapter);
		assertEquals("Session returned from registerTypeAdapter should be the same Session instance", sqliteSession, session);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testRegisterTypeAdapter_nonSqliteTypeAdapter() {
		// Setup
		RestfulPairsTypeAdapter<Date> adapter = new RestfulPairsTypeAdapter<Date>() {
			@Override
			public void mapToObject(ResultSet result, int index, Field field,
					Object model) throws IllegalArgumentException,
					IllegalAccessException {
			}
			@Override
			public void mapToPair(Date value, String field,
					List<NameValuePair> pairs) {
			}
			@Override
			public void mapObjectToPair(Object value, String field,
					List<NameValuePair> pairs) {
			}
		};
		
		// Run
		Session session = sqliteSession.registerTypeAdapter(Date.class, adapter);
		
		// Verify
		verify(mockSqliteTemplate, times(0)).registerTypeAdapter(any(Class.class), any(SqliteTypeAdapter.class));
		assertEquals("Session returned from registerTypeAdapter should be the same Session instance", sqliteSession, session);
	}
	
	@Test
	public void testGetRegisteredTypeAdapters() {
		// Setup
		SqliteTypeAdapter<Date> adapter = new SqliteTypeAdapter<Date>(SqliteDataType.TEXT) {
			@Override
			public void mapToObject(ResultSet result, int index, Field field,
					Object model) throws IllegalArgumentException,
					IllegalAccessException {
			}
			@Override
			public void mapToColumn(Date value, String column,
					ContentValues values) {
			}
			@Override
			public void mapObjectToColumn(Object value, String column,
					ContentValues values) {
			}
		};
		Map<Class<?>, SqliteTypeAdapter<?>> typeAdapters = new HashMap<Class<?>, SqliteTypeAdapter<?>>();
		typeAdapters.put(Date.class, adapter);
		when(mockSqliteTemplate.getRegisteredTypeAdapters()).thenReturn(typeAdapters);
		
		// Run
		@SuppressWarnings("rawtypes")
		Map actual = sqliteSession.getRegisteredTypeAdapters();
		
		// Verify
		verify(mockSqliteTemplate).getRegisteredTypeAdapters();
		assertEquals("Map returned from getRegisteredTypeAdapters should be equal to the expected Map", typeAdapters, actual);
	}
	
	@Test
	public void testBeginTransaction() {
		// Run
		Session session = sqliteSession.beginTransaction();
		
		// Verify
		verify(mockSqliteTemplate).beginTransaction();
		assertEquals("Session returned from beginTransaction should be the same Session instance", sqliteSession, session);
	}
	
	@Test
	public void testCommit() {
		// Run
		Session session = sqliteSession.commit();
		
		// Verify
		verify(mockSqliteTemplate).commit();
		assertEquals("Session returned from commit should be the same Session instance", sqliteSession, session);
	}
	
	@Test
	public void testRollback() {
		// Run
		Session session = sqliteSession.rollback();
		
		// Verify
		verify(mockSqliteTemplate).rollback();
		assertEquals("Session returned from rollback should be the same Session instance", sqliteSession, session);
	}
	
	@Test
	public void testIsTransactionOpen() {
		// Setup
		when(mockSqliteTemplate.isTransactionOpen()).thenReturn(true);
		
		// Run
		boolean actual = sqliteSession.isTransactionOpen();
		
		// Verify
		verify(mockSqliteTemplate).isTransactionOpen();
		assertEquals("Session should be open", true, actual);
	}
	
	@Test
	public void testSetAutocommit() {
		// Run
		Session session = sqliteSession.setAutocommit(true);
		
		// Verify
		verify(mockSqliteTemplate).setAutocommit(true);
		assertEquals("Session returned from setAutocommit should be the same Session instance", sqliteSession, session);
	}
	
	@Test
	public void testIsAutocommit() {
		// Setup
		when(mockSqliteTemplate.isAutocommit()).thenReturn(true);
		
		// Run
		boolean actual = sqliteSession.isAutocommit();
		
		// Verify
		verify(mockSqliteTemplate).isAutocommit();
		assertEquals("Session should be set to autocommit", true, actual);
	}
	
	@Test
	public void testExecuteForResult() {
		// Setup
		String sql = "select * from foo";
		when(mockSqliteTemplate.executeForResult(sql)).thenReturn(mockCursor);
		
		// Run
		Cursor actual = sqliteSession.executeForResult(sql);
		
		// Verify
		verify(mockSqliteTemplate).executeForResult(sql);
		assertEquals("Cursor returned from executeForResult should be equal to the expected Cursor", mockCursor, actual);
	}
	
	@Test
	public void testCache_success() {
		// Setup
		FooModel foo = new FooModel();
		when(mockSessionCache.size()).thenReturn(0);
		
		// Run
		boolean success = sqliteSession.cache(FOO_MODEL_HASH, foo);
		
		// Verify
		verify(mockSessionCache).size();
		verify(mockSessionCache).put(FOO_MODEL_HASH, foo);
		assertTrue("Cache should have been successful", success);
	}
	
	@Test
	public void testCache_fail() {
		// Setup
		FooModel foo = new FooModel();
		when(mockSessionCache.size()).thenReturn(Session.DEFAULT_CACHE_SIZE);
		
		// Run
		boolean success = sqliteSession.cache(FOO_MODEL_HASH, foo);
		
		// Verify
		verify(mockSessionCache).size();
		verify(mockSessionCache, times(0)).put(FOO_MODEL_HASH, foo);
		assertFalse("Cache should have been unsuccessful", success);
	}
	
	@Test
	public void testCheckCache() {
		// Setup
		when(mockSessionCache.containsKey(FOO_MODEL_HASH)).thenReturn(true);
		
		// Run
		boolean cached = sqliteSession.checkCache(FOO_MODEL_HASH);
		
		// Verify
		verify(mockSessionCache).containsKey(FOO_MODEL_HASH);
		assertTrue("Cache should contain key", cached);
	}
	
	@Test
	public void testSearchCache() {
		// Setup
		FooModel foo = new FooModel();
		foo.id = FOO_MODEL_ID;
		when(mockSessionCache.get(FOO_MODEL_HASH)).thenReturn(foo);
		
		// Run
		FooModel actual = (FooModel) sqliteSession.searchCache(FOO_MODEL_HASH);
		
		// Verify
		verify(mockSessionCache).get(FOO_MODEL_HASH);
		assertEquals("Cached object ID should be the same as the expected object ID", foo.id, actual.id);
	}
	
	@Test
	public void testGetSqliteMapper() {
		// Setup
		when(mockSqliteTemplate.getSqliteMapper()).thenReturn(mockSqliteMapper);
		
		// Run
		SqliteMapper actual = sqliteSession.getSqliteMapper();
		
		// Verify
		verify(mockSqliteTemplate).getSqliteMapper();
		assertEquals("SqliteMapper returned from getSqliteMapper should be equal to the expected SqliteMapper", mockSqliteMapper, actual);
	}
	
	private static class FooModel {
		private long id;
	}
	
	private static class BarModel {
	}
	
    private static class BazModel {
	}

}
