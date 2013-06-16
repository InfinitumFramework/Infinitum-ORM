/*
 * Copyright (C) 2013 Clarion Media, LLC
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

package com.clarionmedia.infinitum.orm.sqlite.impl;

import android.database.sqlite.SQLiteDatabase;
import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.context.InfinitumOrmContext;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.criteria.Order;
import com.clarionmedia.infinitum.orm.criteria.criterion.Criterion;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.TypeResolutionPolicy.SqliteDataType;
import com.clarionmedia.infinitum.orm.relationship.ManyToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.ModelRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToOneRelationship;
import com.clarionmedia.infinitum.reflection.ClassReflector;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class SqliteBuilderTest {

	private static final String MODEL_TABLE_1 = "table1";
	private static final String MODEL_TABLE_2 = "table2";
	private static final String MTM_TABLE = "join_table";
	private static final String CRITERION_A_SQL = "foo = bar";
	private static final String CRITERION_B_SQL = "baz = 42";
	private static final String MOCK_TYPE_A = "A";
	private static final String MOCK_TYPE_B = "B";
	private static final String MOCK_TYPE_C = "C";

	@Mock
	private SqliteMapper mockSqliteMapper;

	@Mock
	private PersistencePolicy mockPersistencePolicy;

	@Mock
	private ClassReflector mockClassReflector;

	@Mock
	private InfinitumOrmContext mockInfinitumContext;

	@Mock
	private SqliteDbHelper mockDbHelper;

	@Mock
	private SQLiteDatabase mockSqliteDb;

	@Mock
	private ManyToManyRelationship mockManyToManyRelationship;

	@Mock
	private Criteria<?> mockCriteria;

	@Mock
	private Criterion mockCriterionA;

	@Mock
	private Criterion mockCriterionB;

    @Mock
    private SqliteAssociationCriteria<?> mockAssociationCriteria;

    @Mock
    private OneToOneRelationship mockOtoRelationship;

	@InjectMocks
	private SqliteBuilder sqliteBuilder = new SqliteBuilder();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		List<String> mockDomainTypes = new ArrayList<String>();
		mockDomainTypes.add(MOCK_TYPE_A);
		mockDomainTypes.add(MOCK_TYPE_B);
		mockDomainTypes.add(MOCK_TYPE_C);
		when(mockInfinitumContext.getDomainTypes()).thenReturn(mockDomainTypes);
		when(mockDbHelper.getDatabase()).thenReturn(mockSqliteDb);
		when(mockPersistencePolicy.isPersistent(Long.class)).thenReturn(true);
		when(mockPersistencePolicy.isPersistent(Integer.class)).thenReturn(true);
		when(mockPersistencePolicy.getPersistentFields(Long.class)).thenReturn(Arrays.asList(ArrayList.class.getDeclaredFields()));
		when(mockPersistencePolicy.getPersistentFields(Integer.class)).thenReturn(Arrays.asList(ArrayList.class.getDeclaredFields()));
		Field field = ArrayList.class.getDeclaredFields()[0];
		when(mockManyToManyRelationship.getTableName()).thenReturn(MTM_TABLE);
		when(mockManyToManyRelationship.getFirstField()).thenReturn(field);
		when(mockManyToManyRelationship.getSecondField()).thenReturn(field);
		when(mockSqliteMapper.getSqliteDataType(field)).thenReturn(SqliteDataType.TEXT);
		when(mockCriterionA.toSql(mockCriteria)).thenReturn(CRITERION_A_SQL);
		when(mockCriterionB.toSql(mockCriteria)).thenReturn(CRITERION_B_SQL);
        when(mockCriterionA.toSql(mockAssociationCriteria)).thenReturn(CRITERION_A_SQL);
        when(mockCriterionB.toSql(mockAssociationCriteria)).thenReturn(CRITERION_B_SQL);
		doReturn(Integer.class).when(mockManyToManyRelationship).getFirstType();
		doReturn(Long.class).when(mockManyToManyRelationship).getSecondType();
		doReturn(Integer.class).when(mockClassReflector).getClass(any(String.class));
		doReturn(Long.class).when(mockClassReflector).getClass(any(String.class));
	}

	@Test
	public void testCreateTables_success() {
		// Run
		int result = sqliteBuilder.createTables(mockDbHelper);

		// Verify
		verify(mockSqliteDb, times(3)).execSQL(any(String.class));
		assertEquals("Returned value should be 3", 3, result);
	}

	@Test(expected = InfinitumConfigurationException.class)
	public void testCreateTables_fail() {
		// Setup
		doReturn(null).when(mockClassReflector).getClass(any(String.class));

		// Run
		sqliteBuilder.createTables(mockDbHelper);

		// Verify
		assertTrue("Exception should have been thrown", false);
	}

	@Test
	public void testCreateTables_withManyToMany_success() {
		// Setup
		Map<Field, ManyToManyRelationship> mockCache = new HashMap<Field, ManyToManyRelationship>();
		Field field = ArrayList.class.getDeclaredFields()[0];
		mockCache.put(field, mockManyToManyRelationship);
		when(mockPersistencePolicy.getManyToManyCache()).thenReturn(mockCache);

		// Run
		int result = sqliteBuilder.createTables(mockDbHelper);

		// Verify
		verify(mockSqliteDb, times(4)).execSQL(any(String.class));
		assertEquals("Returned value should be 4", 4, result);
	}

	@Test
	public void testDropTables_success() {
		// Run
		int result = sqliteBuilder.dropTables(mockDbHelper);

		// Verify
		verify(mockSqliteDb, times(3)).execSQL(any(String.class));
		assertEquals("Returned value should be 3", 3, result);
	}

	@Test(expected = InfinitumConfigurationException.class)
	public void testDropTables_fail() {
		// Setup
		doReturn(null).when(mockClassReflector).getClass(any(String.class));

		// Run
		sqliteBuilder.dropTables(mockDbHelper);

		// Verify
		assertTrue("Exception should have been thrown", false);
	}

	@Test
	public void testCreateQuery_singleCriterion_noLimitOrOffset_noOrderBy() {
		// Setup
		doReturn(Object.class).when(mockCriteria).getEntityClass();
		List<Criterion> mockCriterionList = new ArrayList<Criterion>();
		mockCriterionList.add(mockCriterionA);
		when(mockCriteria.getCriterion()).thenReturn(mockCriterionList);
		when(mockCriteria.getLimit()).thenReturn(0);
		when(mockCriteria.getOffset()).thenReturn(0);
		when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);

		// Run
		String expected = "SELECT * FROM " + MODEL_TABLE_1 + " WHERE " + CRITERION_A_SQL;
		String actual = sqliteBuilder.createQuery(mockCriteria);

		// Verify
		verify(mockCriteria).getEntityClass();
		verify(mockCriteria).getCriterion();
		verify(mockCriteria).getLimit();
		verify(mockCriteria).getOffset();
        verify(mockCriteria).getOrderings();
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}

    @Test
    public void testCreateQuery_singleCriterion_noLimitOrOffset_orderBy() {
        // Setup
        doReturn(Object.class).when(mockCriteria).getEntityClass();
        List<Criterion> mockCriterionList = new ArrayList<Criterion>();
        mockCriterionList.add(mockCriterionA);
        when(mockCriteria.getCriterion()).thenReturn(mockCriterionList);
        when(mockCriteria.getLimit()).thenReturn(0);
        when(mockCriteria.getOffset()).thenReturn(0);
        when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);
        List<Order> orderings = new ArrayList<Order>();
        String property = "prop";
        orderings.add(Order.desc(property));
        when(mockCriteria.getOrderings()).thenReturn(orderings);
        Field field = ArrayList.class.getDeclaredFields()[0];
        doReturn(field).when(mockPersistencePolicy).findPersistentField(Object.class, property);
        String orderField = "foo";
        when(mockPersistencePolicy.getFieldColumnName(field)).thenReturn(orderField);

        // Run
        String expected = "SELECT * FROM " + MODEL_TABLE_1 + " WHERE " + CRITERION_A_SQL + " ORDER BY " + orderField
                + " DESC";
        String actual = sqliteBuilder.createQuery(mockCriteria);

        // Verify
        verify(mockCriteria).getEntityClass();
        verify(mockCriteria).getCriterion();
        verify(mockCriteria).getLimit();
        verify(mockCriteria).getOffset();
        verify(mockCriteria, times(2)).getOrderings();
        assertEquals("Returned SQL query should match expected value", expected, actual);
    }

    @Test
    public void testCreateQuery_singleCriterion_noLimitOrOffset_orderBy_ignoreCase() {
        // Setup
        doReturn(Object.class).when(mockCriteria).getEntityClass();
        List<Criterion> mockCriterionList = new ArrayList<Criterion>();
        mockCriterionList.add(mockCriterionA);
        when(mockCriteria.getCriterion()).thenReturn(mockCriterionList);
        when(mockCriteria.getLimit()).thenReturn(0);
        when(mockCriteria.getOffset()).thenReturn(0);
        when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);
        List<Order> orderings = new ArrayList<Order>();
        String property = "prop";
        orderings.add(Order.desc(property).ignoreCase());
        when(mockCriteria.getOrderings()).thenReturn(orderings);
        Field field = ArrayList.class.getDeclaredFields()[0];
        doReturn(field).when(mockPersistencePolicy).findPersistentField(Object.class, property);
        String orderField = "foo";
        when(mockPersistencePolicy.getFieldColumnName(field)).thenReturn(orderField);

        // Run
        String expected = "SELECT * FROM " + MODEL_TABLE_1 + " WHERE " + CRITERION_A_SQL + " ORDER BY " + orderField
                + " COLLATE NOCASE DESC";
        String actual = sqliteBuilder.createQuery(mockCriteria);

        // Verify
        verify(mockCriteria).getEntityClass();
        verify(mockCriteria).getCriterion();
        verify(mockCriteria).getLimit();
        verify(mockCriteria).getOffset();
        verify(mockCriteria, times(2)).getOrderings();
        assertEquals("Returned SQL query should match expected value", expected, actual);
    }

    @Test
    public void testCreateQuery_singleCriterion_noLimitOrOffset_multipleOrderBy() {
        // Setup
        doReturn(Object.class).when(mockCriteria).getEntityClass();
        List<Criterion> mockCriterionList = new ArrayList<Criterion>();
        mockCriterionList.add(mockCriterionA);
        when(mockCriteria.getCriterion()).thenReturn(mockCriterionList);
        when(mockCriteria.getLimit()).thenReturn(0);
        when(mockCriteria.getOffset()).thenReturn(0);
        when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);
        List<Order> orderings = new ArrayList<Order>();
        String property1 = "prop1";
        String property2 = "prop2";
        orderings.add(Order.desc(property1));
        orderings.add(Order.asc(property2));
        when(mockCriteria.getOrderings()).thenReturn(orderings);
        Field field1 = ArrayList.class.getDeclaredFields()[0];
        Field field2 = ArrayList.class.getDeclaredFields()[1];
        doReturn(field1).when(mockPersistencePolicy).findPersistentField(Object.class, property1);
        doReturn(field2).when(mockPersistencePolicy).findPersistentField(Object.class, property2);
        String orderField1 = "foo";
        String orderField2 = "bar";
        when(mockPersistencePolicy.getFieldColumnName(field1)).thenReturn(orderField1);
        when(mockPersistencePolicy.getFieldColumnName(field2)).thenReturn(orderField2);

        // Run
        String expected = "SELECT * FROM " + MODEL_TABLE_1 + " WHERE " + CRITERION_A_SQL + " ORDER BY " + orderField1
                + " DESC, " + orderField2 + " ASC";
        String actual = sqliteBuilder.createQuery(mockCriteria);

        // Verify
        verify(mockCriteria).getEntityClass();
        verify(mockCriteria).getCriterion();
        verify(mockCriteria).getLimit();
        verify(mockCriteria).getOffset();
        verify(mockCriteria, times(2)).getOrderings();
        assertEquals("Returned SQL query should match expected value", expected, actual);
    }

	@Test
	public void testCreateQuery_singleCriterion_limitAndOffset_noOrderBy() {
		// Setup
		doReturn(Object.class).when(mockCriteria).getEntityClass();
		List<Criterion> mockCriterionList = new ArrayList<Criterion>();
		mockCriterionList.add(mockCriterionA);
		when(mockCriteria.getCriterion()).thenReturn(mockCriterionList);
		when(mockCriteria.getLimit()).thenReturn(10);
		when(mockCriteria.getOffset()).thenReturn(5);
		when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);

		// Run
		String expected = "SELECT * FROM " + MODEL_TABLE_1 + " WHERE " + CRITERION_A_SQL + " LIMIT 10 OFFSET 5";
		String actual = sqliteBuilder.createQuery(mockCriteria);

		// Verify
		verify(mockCriteria).getEntityClass();
		verify(mockCriteria).getCriterion();
		verify(mockCriteria).getLimit();
		verify(mockCriteria, times(2)).getOffset();
		verify(mockCriterionA).toSql(mockCriteria);
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}

    @Test
    public void testCreateQuery_singleCriterion_limitAndOffset_orderBy() {
        // Setup
        doReturn(Object.class).when(mockCriteria).getEntityClass();
        List<Criterion> mockCriterionList = new ArrayList<Criterion>();
        mockCriterionList.add(mockCriterionA);
        when(mockCriteria.getCriterion()).thenReturn(mockCriterionList);
        when(mockCriteria.getLimit()).thenReturn(10);
        when(mockCriteria.getOffset()).thenReturn(5);
        when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);
        List<Order> orderings = new ArrayList<Order>();
        String property = "prop";
        orderings.add(Order.asc(property));
        when(mockCriteria.getOrderings()).thenReturn(orderings);
        Field field = ArrayList.class.getDeclaredFields()[0];
        doReturn(field).when(mockPersistencePolicy).findPersistentField(Object.class, property);
        String orderField = "foo";
        when(mockPersistencePolicy.getFieldColumnName(field)).thenReturn(orderField);

        // Run
        String expected = "SELECT * FROM " + MODEL_TABLE_1 + " WHERE " + CRITERION_A_SQL + " ORDER BY " + orderField
                + " ASC LIMIT 10 OFFSET 5";
        String actual = sqliteBuilder.createQuery(mockCriteria);

        // Verify
        verify(mockCriteria).getEntityClass();
        verify(mockCriteria).getCriterion();
        verify(mockCriteria).getLimit();
        verify(mockCriteria, times(2)).getOffset();
        verify(mockCriterionA).toSql(mockCriteria);
        assertEquals("Returned SQL query should match expected value", expected, actual);
    }

    @Test
    public void testCreateQuery_singleCriterion_limitAndOffset_multipleOrderBy() {
        // Setup
        doReturn(Object.class).when(mockCriteria).getEntityClass();
        List<Criterion> mockCriterionList = new ArrayList<Criterion>();
        mockCriterionList.add(mockCriterionA);
        when(mockCriteria.getCriterion()).thenReturn(mockCriterionList);
        when(mockCriteria.getLimit()).thenReturn(10);
        when(mockCriteria.getOffset()).thenReturn(5);
        when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);
        List<Order> orderings = new ArrayList<Order>();
        String property1 = "prop1";
        String property2 = "prop2";
        orderings.add(Order.desc(property1));
        orderings.add(Order.asc(property2));
        when(mockCriteria.getOrderings()).thenReturn(orderings);
        Field field1 = ArrayList.class.getDeclaredFields()[0];
        Field field2 = ArrayList.class.getDeclaredFields()[1];
        doReturn(field1).when(mockPersistencePolicy).findPersistentField(Object.class, property1);
        doReturn(field2).when(mockPersistencePolicy).findPersistentField(Object.class, property2);
        String orderField1 = "foo";
        String orderField2 = "bar";
        when(mockPersistencePolicy.getFieldColumnName(field1)).thenReturn(orderField1);
        when(mockPersistencePolicy.getFieldColumnName(field2)).thenReturn(orderField2);

        // Run
        String expected = "SELECT * FROM " + MODEL_TABLE_1 + " WHERE " + CRITERION_A_SQL + " ORDER BY " + orderField1
                + " DESC, " + orderField2 + " ASC LIMIT 10 OFFSET 5";
        String actual = sqliteBuilder.createQuery(mockCriteria);

        // Verify
        verify(mockCriteria).getEntityClass();
        verify(mockCriteria).getCriterion();
        verify(mockCriteria).getLimit();
        verify(mockCriteria, times(2)).getOffset();
        verify(mockCriterionA).toSql(mockCriteria);
        assertEquals("Returned SQL query should match expected value", expected, actual);
    }

	@Test
	public void testCreateQuery_multipleCriterion_noLimitOrOffset_noOrderBy() {
		// Setup
		doReturn(Object.class).when(mockCriteria).getEntityClass();
		List<Criterion> mockCriterionList = new ArrayList<Criterion>();
		mockCriterionList.add(mockCriterionA);
		mockCriterionList.add(mockCriterionB);
		when(mockCriteria.getCriterion()).thenReturn(mockCriterionList);
		when(mockCriteria.getLimit()).thenReturn(0);
		when(mockCriteria.getOffset()).thenReturn(0);
		when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);

		// Run
		String expected = "SELECT * FROM " + MODEL_TABLE_1 + " WHERE " + CRITERION_A_SQL + " AND " + CRITERION_B_SQL;
		String actual = sqliteBuilder.createQuery(mockCriteria);

		// Verify
		verify(mockCriteria).getEntityClass();
		verify(mockCriteria).getCriterion();
		verify(mockCriteria).getLimit();
		verify(mockCriteria).getOffset();
		verify(mockCriterionA).toSql(mockCriteria);
		verify(mockCriterionB).toSql(mockCriteria);
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}

    @Test
    public void testCreateQuery_multipleCriterion_noLimitOrOffset_orderBy() {
        // Setup
        doReturn(Object.class).when(mockCriteria).getEntityClass();
        List<Criterion> mockCriterionList = new ArrayList<Criterion>();
        mockCriterionList.add(mockCriterionA);
        mockCriterionList.add(mockCriterionB);
        when(mockCriteria.getCriterion()).thenReturn(mockCriterionList);
        when(mockCriteria.getLimit()).thenReturn(0);
        when(mockCriteria.getOffset()).thenReturn(0);
        when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);
        List<Order> orderings = new ArrayList<Order>();
        String property = "prop";
        orderings.add(Order.asc(property));
        when(mockCriteria.getOrderings()).thenReturn(orderings);
        Field field = ArrayList.class.getDeclaredFields()[0];
        doReturn(field).when(mockPersistencePolicy).findPersistentField(Object.class, property);
        String orderField = "foo";
        when(mockPersistencePolicy.getFieldColumnName(field)).thenReturn(orderField);

        // Run
        String expected = "SELECT * FROM " + MODEL_TABLE_1 + " WHERE " + CRITERION_A_SQL + " AND " + CRITERION_B_SQL
                + " ORDER BY " + orderField + " ASC";
        String actual = sqliteBuilder.createQuery(mockCriteria);

        // Verify
        verify(mockCriteria).getEntityClass();
        verify(mockCriteria).getCriterion();
        verify(mockCriteria).getLimit();
        verify(mockCriteria).getOffset();
        verify(mockCriterionA).toSql(mockCriteria);
        verify(mockCriterionB).toSql(mockCriteria);
        assertEquals("Returned SQL query should match expected value", expected, actual);
    }

    @Test
    public void testCreateQuery_multipleCriterion_noLimitOrOffset_multipleOrderBy() {
        // Setup
        doReturn(Object.class).when(mockCriteria).getEntityClass();
        List<Criterion> mockCriterionList = new ArrayList<Criterion>();
        mockCriterionList.add(mockCriterionA);
        mockCriterionList.add(mockCriterionB);
        when(mockCriteria.getCriterion()).thenReturn(mockCriterionList);
        when(mockCriteria.getLimit()).thenReturn(0);
        when(mockCriteria.getOffset()).thenReturn(0);
        when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);
        List<Order> orderings = new ArrayList<Order>();
        String property1 = "prop1";
        String property2 = "prop2";
        orderings.add(Order.desc(property1));
        orderings.add(Order.asc(property2));
        when(mockCriteria.getOrderings()).thenReturn(orderings);
        Field field1 = ArrayList.class.getDeclaredFields()[0];
        Field field2 = ArrayList.class.getDeclaredFields()[1];
        doReturn(field1).when(mockPersistencePolicy).findPersistentField(Object.class, property1);
        doReturn(field2).when(mockPersistencePolicy).findPersistentField(Object.class, property2);
        String orderField1 = "foo";
        String orderField2 = "bar";
        when(mockPersistencePolicy.getFieldColumnName(field1)).thenReturn(orderField1);
        when(mockPersistencePolicy.getFieldColumnName(field2)).thenReturn(orderField2);

        // Run
        String expected = "SELECT * FROM " + MODEL_TABLE_1 + " WHERE " + CRITERION_A_SQL + " AND " + CRITERION_B_SQL
                + " ORDER BY " + orderField1 + " DESC, " + orderField2 + " ASC";
        String actual = sqliteBuilder.createQuery(mockCriteria);

        // Verify
        verify(mockCriteria).getEntityClass();
        verify(mockCriteria).getCriterion();
        verify(mockCriteria).getLimit();
        verify(mockCriteria).getOffset();
        verify(mockCriterionA).toSql(mockCriteria);
        verify(mockCriterionB).toSql(mockCriteria);
        assertEquals("Returned SQL query should match expected value", expected, actual);
    }

	@Test
	public void testCreateQuery_multipleCriterion_limitAndOffset_noOrderBy() {
		// Setup
		doReturn(Object.class).when(mockCriteria).getEntityClass();
		List<Criterion> mockCriterionList = new ArrayList<Criterion>();
		mockCriterionList.add(mockCriterionA);
		mockCriterionList.add(mockCriterionB);
		when(mockCriteria.getCriterion()).thenReturn(mockCriterionList);
		when(mockCriteria.getLimit()).thenReturn(10);
		when(mockCriteria.getOffset()).thenReturn(5);
		when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);

		// Run
		String expected = "SELECT * FROM " + MODEL_TABLE_1 + " WHERE " + CRITERION_A_SQL + " AND " + CRITERION_B_SQL + " LIMIT 10 OFFSET 5";
		String actual = sqliteBuilder.createQuery(mockCriteria);

		// Verify
		verify(mockCriteria).getEntityClass();
		verify(mockCriteria).getCriterion();
		verify(mockCriteria).getLimit();
		verify(mockCriteria, times(2)).getOffset();
		verify(mockCriterionA).toSql(mockCriteria);
		verify(mockCriterionB).toSql(mockCriteria);
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}

    @Test
    public void testCreateQuery_multipleCriterion_limitAndOffset_orderBy() {
        // Setup
        doReturn(Object.class).when(mockCriteria).getEntityClass();
        List<Criterion> mockCriterionList = new ArrayList<Criterion>();
        mockCriterionList.add(mockCriterionA);
        mockCriterionList.add(mockCriterionB);
        when(mockCriteria.getCriterion()).thenReturn(mockCriterionList);
        when(mockCriteria.getLimit()).thenReturn(10);
        when(mockCriteria.getOffset()).thenReturn(5);
        when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);
        List<Order> orderings = new ArrayList<Order>();
        String property = "prop";
        orderings.add(Order.desc(property));
        when(mockCriteria.getOrderings()).thenReturn(orderings);
        Field field = ArrayList.class.getDeclaredFields()[0];
        doReturn(field).when(mockPersistencePolicy).findPersistentField(Object.class, property);
        String orderField = "foo";
        when(mockPersistencePolicy.getFieldColumnName(field)).thenReturn(orderField);

        // Run
        String expected = "SELECT * FROM " + MODEL_TABLE_1 + " WHERE " + CRITERION_A_SQL + " AND " + CRITERION_B_SQL
                + " ORDER BY " + orderField + " DESC LIMIT 10 OFFSET 5";
        String actual = sqliteBuilder.createQuery(mockCriteria);

        // Verify
        verify(mockCriteria).getEntityClass();
        verify(mockCriteria).getCriterion();
        verify(mockCriteria).getLimit();
        verify(mockCriteria, times(2)).getOffset();
        verify(mockCriterionA).toSql(mockCriteria);
        verify(mockCriterionB).toSql(mockCriteria);
        assertEquals("Returned SQL query should match expected value", expected, actual);
    }

    @Test
    public void testCreateQuery_multipleCriterion_limitAndOffset_multipleOrderBy() {
        // Setup
        doReturn(Object.class).when(mockCriteria).getEntityClass();
        List<Criterion> mockCriterionList = new ArrayList<Criterion>();
        mockCriterionList.add(mockCriterionA);
        mockCriterionList.add(mockCriterionB);
        when(mockCriteria.getCriterion()).thenReturn(mockCriterionList);
        when(mockCriteria.getLimit()).thenReturn(10);
        when(mockCriteria.getOffset()).thenReturn(5);
        when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);
        List<Order> orderings = new ArrayList<Order>();
        String property1 = "prop1";
        String property2 = "prop2";
        orderings.add(Order.desc(property1));
        orderings.add(Order.asc(property2));
        when(mockCriteria.getOrderings()).thenReturn(orderings);
        Field field1 = ArrayList.class.getDeclaredFields()[0];
        Field field2 = ArrayList.class.getDeclaredFields()[1];
        doReturn(field1).when(mockPersistencePolicy).findPersistentField(Object.class, property1);
        doReturn(field2).when(mockPersistencePolicy).findPersistentField(Object.class, property2);
        String orderField1 = "foo";
        String orderField2 = "bar";
        when(mockPersistencePolicy.getFieldColumnName(field1)).thenReturn(orderField1);
        when(mockPersistencePolicy.getFieldColumnName(field2)).thenReturn(orderField2);

        // Run
        String expected = "SELECT * FROM " + MODEL_TABLE_1 + " WHERE " + CRITERION_A_SQL + " AND " + CRITERION_B_SQL
                + " ORDER BY " + orderField1 + " DESC, " + orderField2 + " ASC LIMIT 10 OFFSET 5";
        String actual = sqliteBuilder.createQuery(mockCriteria);

        // Verify
        verify(mockCriteria).getEntityClass();
        verify(mockCriteria).getCriterion();
        verify(mockCriteria).getLimit();
        verify(mockCriteria, times(2)).getOffset();
        verify(mockCriterionA).toSql(mockCriteria);
        verify(mockCriterionB).toSql(mockCriteria);
        assertEquals("Returned SQL query should match expected value", expected, actual);
    }

	@Test
	public void testCreateCountQuery_singleCriterion_noLimitOrOffset() {
		// Setup
		doReturn(Object.class).when(mockCriteria).getEntityClass();
		List<Criterion> mockCriterionList = new ArrayList<Criterion>();
		mockCriterionList.add(mockCriterionA);
		when(mockCriteria.getCriterion()).thenReturn(mockCriterionList);
		when(mockCriteria.getLimit()).thenReturn(0);
		when(mockCriteria.getOffset()).thenReturn(0);
		when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);

		// Run
		String expected = "SELECT count(*) FROM " + MODEL_TABLE_1 + " WHERE " + CRITERION_A_SQL;
		String actual = sqliteBuilder.createCountQuery(mockCriteria);

		// Verify
		verify(mockCriteria).getEntityClass();
		verify(mockCriteria).getCriterion();
		verify(mockCriteria).getLimit();
		verify(mockCriteria).getOffset();
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}

	@Test
	public void testCreateCountQuery_singleCriterion_limitAndOffset() {
		// Setup
		doReturn(Object.class).when(mockCriteria).getEntityClass();
		List<Criterion> mockCriterionList = new ArrayList<Criterion>();
		mockCriterionList.add(mockCriterionA);
		when(mockCriteria.getCriterion()).thenReturn(mockCriterionList);
		when(mockCriteria.getLimit()).thenReturn(10);
		when(mockCriteria.getOffset()).thenReturn(5);
		when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);

		// Run
		String expected = "SELECT count(*) FROM " + MODEL_TABLE_1 + " WHERE " + CRITERION_A_SQL + " LIMIT 10 OFFSET 5";
		String actual = sqliteBuilder.createCountQuery(mockCriteria);

		// Verify
		verify(mockCriteria).getEntityClass();
		verify(mockCriteria).getCriterion();
		verify(mockCriteria).getLimit();
		verify(mockCriteria, times(2)).getOffset();
		verify(mockCriterionA).toSql(mockCriteria);
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}

	@Test
	public void testCreateCountQuery_multipleCriterion_noLimitOrOffset() {
		// Setup
		doReturn(Object.class).when(mockCriteria).getEntityClass();
		List<Criterion> mockCriterionList = new ArrayList<Criterion>();
		mockCriterionList.add(mockCriterionA);
		mockCriterionList.add(mockCriterionB);
		when(mockCriteria.getCriterion()).thenReturn(mockCriterionList);
		when(mockCriteria.getLimit()).thenReturn(0);
		when(mockCriteria.getOffset()).thenReturn(0);
		when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);

		// Run
		String expected = "SELECT count(*) FROM " + MODEL_TABLE_1 + " WHERE " + CRITERION_A_SQL + " AND " + CRITERION_B_SQL;
		String actual = sqliteBuilder.createCountQuery(mockCriteria);

		// Verify
		verify(mockCriteria).getEntityClass();
		verify(mockCriteria).getCriterion();
		verify(mockCriteria).getLimit();
		verify(mockCriteria).getOffset();
		verify(mockCriterionA).toSql(mockCriteria);
		verify(mockCriterionB).toSql(mockCriteria);
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}

	@Test
	public void testCreateCountQuery_multipleCriterion_limitAndOffset() {
		// Setup
		doReturn(Object.class).when(mockCriteria).getEntityClass();
		List<Criterion> mockCriterionList = new ArrayList<Criterion>();
		mockCriterionList.add(mockCriterionA);
		mockCriterionList.add(mockCriterionB);
		when(mockCriteria.getCriterion()).thenReturn(mockCriterionList);
		when(mockCriteria.getLimit()).thenReturn(10);
		when(mockCriteria.getOffset()).thenReturn(5);
		when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);

		// Run
		String expected = "SELECT count(*) FROM " + MODEL_TABLE_1 + " WHERE " + CRITERION_A_SQL + " AND " + CRITERION_B_SQL + " LIMIT 10 OFFSET 5";
		String actual = sqliteBuilder.createCountQuery(mockCriteria);

		// Verify
		verify(mockCriteria).getEntityClass();
		verify(mockCriteria).getCriterion();
		verify(mockCriteria).getLimit();
		verify(mockCriteria, times(2)).getOffset();
		verify(mockCriterionA).toSql(mockCriteria);
		verify(mockCriterionB).toSql(mockCriteria);
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}

	@Test
	public void testCreateManyToManyJoinQuery_firstType_success() {
		// Setup
		ManyToManyRelationship mockRelationship = mock(ManyToManyRelationship.class);
		doReturn(Integer.class).when(mockRelationship).getFirstType();
		doReturn(Long.class).when(mockRelationship).getSecondType();
		Field field = ArrayList.class.getDeclaredFields()[0];
		when(mockRelationship.getFirstField()).thenReturn(field);
		when(mockRelationship.getSecondField()).thenReturn(field);
		when(mockRelationship.contains(Integer.class)).thenReturn(true);
		when(mockRelationship.getTableName()).thenReturn(MTM_TABLE);
		final String COL_NAME = "col";
		when(mockPersistencePolicy.getFieldColumnName(field)).thenReturn(COL_NAME);
		Serializable id = 42;
		when(mockSqliteMapper.getSqliteDataType(id)).thenReturn(SqliteDataType.INTEGER);
		when(mockPersistencePolicy.getModelTableName(Integer.class)).thenReturn(MODEL_TABLE_1);
		when(mockPersistencePolicy.getModelTableName(Long.class)).thenReturn(MODEL_TABLE_2);

		// Run
		String expected = "SELECT x.* FROM table1 x, table2 y, join_table z WHERE z.table1_col_1 = x.col AND z" +
                ".table2_col_2 = y.col AND y.col = 42";
		String actual = sqliteBuilder.createManyToManyJoinQuery(mockRelationship, id, Integer.class);

		// Verify
		verify(mockRelationship).contains(Integer.class);
		verify(mockRelationship, times(4)).getFirstType();
		verify(mockRelationship, times(3)).getSecondType();
		verify(mockRelationship, times(2)).getFirstField();
		verify(mockRelationship, times(3)).getSecondField();
		verify(mockRelationship).getTableName();
		verify(mockPersistencePolicy, times(2)).getModelTableName(mockRelationship.getFirstType());
		verify(mockPersistencePolicy, times(2)).getModelTableName(mockRelationship.getSecondType());
		verify(mockPersistencePolicy, times(5)).getFieldColumnName(field);
		verify(mockSqliteMapper).getSqliteDataType(id);
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}

	@Test
	public void testCreateManyToManyJoinQuery_secondType_success() {
		// Setup
		ManyToManyRelationship mockRelationship = mock(ManyToManyRelationship.class);
		doReturn(Integer.class).when(mockRelationship).getFirstType();
		doReturn(Long.class).when(mockRelationship).getSecondType();
		Field field = ArrayList.class.getDeclaredFields()[0];
		when(mockRelationship.getFirstField()).thenReturn(field);
		when(mockRelationship.getSecondField()).thenReturn(field);
		when(mockRelationship.contains(Long.class)).thenReturn(true);
		when(mockRelationship.getTableName()).thenReturn(MTM_TABLE);
		final String COL_NAME = "col";
		when(mockPersistencePolicy.getFieldColumnName(field)).thenReturn(COL_NAME);
		Serializable id = 42;
		when(mockSqliteMapper.getSqliteDataType(id)).thenReturn(SqliteDataType.INTEGER);
		when(mockPersistencePolicy.getModelTableName(Integer.class)).thenReturn(MODEL_TABLE_1);
		when(mockPersistencePolicy.getModelTableName(Long.class)).thenReturn(MODEL_TABLE_2);

		// Run
		String expected = "SELECT x.* FROM table1 y, table2 x, join_table z WHERE z.table2_col_2 = x.col AND z" +
                ".table1_col_1 = y.col AND y.col = 42";
		String actual = sqliteBuilder.createManyToManyJoinQuery(mockRelationship, id, Long.class);

		// Verify
		verify(mockRelationship).contains(Long.class);
		verify(mockRelationship, times(4)).getFirstType();
		verify(mockRelationship, times(3)).getSecondType();
		verify(mockRelationship, times(3)).getFirstField();
		verify(mockRelationship, times(2)).getSecondField();
		verify(mockRelationship).getTableName();
		verify(mockPersistencePolicy, times(2)).getModelTableName(mockRelationship.getFirstType());
		verify(mockPersistencePolicy, times(2)).getModelTableName(mockRelationship.getSecondType());
		verify(mockPersistencePolicy, times(5)).getFieldColumnName(field);
		verify(mockSqliteMapper).getSqliteDataType(id);
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}

	@Test(expected = InfinitumRuntimeException.class)
	public void testCreateManyToManyJoinQuery_fail() {
		// Setup
		ManyToManyRelationship mockRelationship = mock(ManyToManyRelationship.class);
		doReturn(Integer.class).when(mockRelationship).getFirstType();
		doReturn(Long.class).when(mockRelationship).getSecondType();
		Field field = ArrayList.class.getDeclaredFields()[0];
		when(mockRelationship.getFirstField()).thenReturn(field);
		when(mockRelationship.getSecondField()).thenReturn(field);
		when(mockRelationship.contains(Integer.class)).thenReturn(false);
		when(mockRelationship.getTableName()).thenReturn(MTM_TABLE);
		final String COL_NAME = "col";
		when(mockPersistencePolicy.getFieldColumnName(field)).thenReturn(COL_NAME);
		Serializable id = 42;
		when(mockSqliteMapper.getSqliteDataType(id)).thenReturn(SqliteDataType.INTEGER);
		when(mockPersistencePolicy.getModelTableName(Integer.class)).thenReturn(MODEL_TABLE_1);
		when(mockPersistencePolicy.getModelTableName(Long.class)).thenReturn(MODEL_TABLE_2);

		// Run
		sqliteBuilder.createManyToManyJoinQuery(mockRelationship, id, Integer.class);

		// Verify
		assertTrue("Exception should have been thrown", false);
	}

	@Test
	public void testCreateDeleteStaleRelationshipQuery_firstType_singleRelated() {
		// Setup
		List<Serializable> relatedKeys = new ArrayList<Serializable>();
		Serializable id = 42;
		Serializable entityPk = 100;
		relatedKeys.add(id);
		final String COL_NAME = "id";
		Object entity = new Object();
		doReturn(Object.class).when(mockManyToManyRelationship).getFirstType();
		when(mockPersistencePolicy.getModelTableName(mockManyToManyRelationship.getFirstType())).thenReturn(MODEL_TABLE_1);
		when(mockPersistencePolicy.getModelTableName(mockManyToManyRelationship.getSecondType())).thenReturn(MODEL_TABLE_2);
		when(mockPersistencePolicy.getFieldColumnName(mockManyToManyRelationship.getFirstField())).thenReturn(COL_NAME);
		when(mockPersistencePolicy.getFieldColumnName(mockManyToManyRelationship.getSecondField())).thenReturn(COL_NAME);
		when(mockPersistencePolicy.getPrimaryKey(entity)).thenReturn(entityPk);
		when(mockSqliteMapper.getSqliteDataType(id)).thenReturn(SqliteDataType.INTEGER);

		// Run
		String expected = "DELETE FROM " + MTM_TABLE + " WHERE " + MODEL_TABLE_1 + "_" + COL_NAME + "_1" + " = '100' " +
                "AND "
                + MODEL_TABLE_2 + "_" + COL_NAME + "_2 NOT IN (" + id + ")";
		String actual = sqliteBuilder.createDeleteStaleRelationshipQuery(mockManyToManyRelationship, entity, relatedKeys);

		// Verify
		verify(mockManyToManyRelationship).getTableName();
		verify(mockManyToManyRelationship, times(4)).getFirstType();
		verify(mockManyToManyRelationship, times(2)).getSecondType();
		verify(mockManyToManyRelationship, times(3)).getFirstField();
		verify(mockManyToManyRelationship, times(2)).getSecondField();
		verify(mockPersistencePolicy).getPrimaryKey(entity);
		verify(mockPersistencePolicy).getModelTableName(mockManyToManyRelationship.getFirstType());
		verify(mockPersistencePolicy).getModelTableName(mockManyToManyRelationship.getSecondType());
		verify(mockPersistencePolicy, times(2)).getFieldColumnName(mockManyToManyRelationship.getFirstField());
		verify(mockSqliteMapper).getSqliteDataType(any(Field.class));
		verify(mockSqliteMapper).getSqliteDataType(any(Serializable.class));
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}

	@Test
	public void testCreateDeleteStaleRelationshipQuery_secondType_singleRelated() {
		// Setup
		List<Serializable> relatedKeys = new ArrayList<Serializable>();
		Serializable id = 42;
		Serializable entityPk = 100;
		relatedKeys.add(id);
		final String COL_NAME = "id";
		Object entity = new Object();
		when(mockPersistencePolicy.getModelTableName(mockManyToManyRelationship.getFirstType())).thenReturn(MODEL_TABLE_1);
		when(mockPersistencePolicy.getModelTableName(mockManyToManyRelationship.getSecondType())).thenReturn(MODEL_TABLE_2);
		when(mockPersistencePolicy.getFieldColumnName(mockManyToManyRelationship.getFirstField())).thenReturn(COL_NAME);
		when(mockPersistencePolicy.getFieldColumnName(mockManyToManyRelationship.getSecondField())).thenReturn(COL_NAME);
		when(mockPersistencePolicy.getPrimaryKey(entity)).thenReturn(entityPk);
		when(mockSqliteMapper.getSqliteDataType(id)).thenReturn(SqliteDataType.INTEGER);

		// Run
		String expected = "DELETE FROM " + MTM_TABLE + " WHERE " + MODEL_TABLE_2 + "_" + COL_NAME + "_2 = '100' AND "
                + MODEL_TABLE_1 + "_" + COL_NAME + "_1 NOT IN (" + id + ")";
		String actual = sqliteBuilder.createDeleteStaleRelationshipQuery(mockManyToManyRelationship, entity, relatedKeys);

		// Verify
		verify(mockManyToManyRelationship).getTableName();
		verify(mockManyToManyRelationship, times(4)).getFirstType();
		verify(mockManyToManyRelationship, times(2)).getSecondType();
		verify(mockManyToManyRelationship, times(2)).getFirstField();
		verify(mockManyToManyRelationship, times(3)).getSecondField();
		verify(mockPersistencePolicy).getPrimaryKey(entity);
		verify(mockPersistencePolicy).getModelTableName(mockManyToManyRelationship.getFirstType());
		verify(mockPersistencePolicy).getModelTableName(mockManyToManyRelationship.getSecondType());
		verify(mockPersistencePolicy, times(2)).getFieldColumnName(mockManyToManyRelationship.getFirstField());
		verify(mockSqliteMapper).getSqliteDataType(any(Field.class));
		verify(mockSqliteMapper).getSqliteDataType(any(Serializable.class));
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}

	@Test
	public void testCreateDeleteStaleRelationshipQuery_multipleRelated() {
		// Setup
		List<Serializable> relatedKeys = new ArrayList<Serializable>();
		Serializable id1 = 42;
		Serializable id2 = 11;
		Serializable id3 = 38;
		Serializable entityPk = 100;
		relatedKeys.add(id1);
		relatedKeys.add(id2);
		relatedKeys.add(id3);
		final String COL_NAME = "id";
		Object entity = new Object();
		doReturn(Object.class).when(mockManyToManyRelationship).getFirstType();
		when(mockPersistencePolicy.getModelTableName(mockManyToManyRelationship.getFirstType())).thenReturn(MODEL_TABLE_1);
		when(mockPersistencePolicy.getModelTableName(mockManyToManyRelationship.getSecondType())).thenReturn(MODEL_TABLE_2);
		when(mockPersistencePolicy.getFieldColumnName(mockManyToManyRelationship.getFirstField())).thenReturn(COL_NAME);
		when(mockPersistencePolicy.getFieldColumnName(mockManyToManyRelationship.getSecondField())).thenReturn(COL_NAME);
		when(mockPersistencePolicy.getPrimaryKey(entity)).thenReturn(entityPk);
		when(mockSqliteMapper.getSqliteDataType(id1)).thenReturn(SqliteDataType.INTEGER);
		when(mockSqliteMapper.getSqliteDataType(id2)).thenReturn(SqliteDataType.INTEGER);
		when(mockSqliteMapper.getSqliteDataType(id3)).thenReturn(SqliteDataType.INTEGER);

		// Run
		String expected = "DELETE FROM " + MTM_TABLE + " WHERE " + MODEL_TABLE_1 + "_" + COL_NAME + "_1 = '100' AND "
                + MODEL_TABLE_2 + "_" + COL_NAME + "_2 NOT IN (" + id1 + ", " + id2 + ", " + id3 + ")";
		String actual = sqliteBuilder.createDeleteStaleRelationshipQuery(mockManyToManyRelationship, entity, relatedKeys);

		// Verify
		verify(mockManyToManyRelationship).getTableName();
		verify(mockManyToManyRelationship, times(4)).getFirstType();
		verify(mockManyToManyRelationship, times(2)).getSecondType();
		verify(mockManyToManyRelationship, times(3)).getFirstField();
		verify(mockManyToManyRelationship, times(2)).getSecondField();
		verify(mockPersistencePolicy).getPrimaryKey(entity);
		verify(mockPersistencePolicy).getModelTableName(mockManyToManyRelationship.getFirstType());
		verify(mockPersistencePolicy).getModelTableName(mockManyToManyRelationship.getSecondType());
		verify(mockPersistencePolicy, times(2)).getFieldColumnName(mockManyToManyRelationship.getFirstField());
		verify(mockSqliteMapper).getSqliteDataType(any(Field.class));
		verify(mockSqliteMapper, times(3)).getSqliteDataType(any(Serializable.class));
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}

	@Test
	public void testCreateUpdateForeignKeyQuery_singleRelated() {
		// Setup
		List<Serializable> relatedKeys = new ArrayList<Serializable>();
		Serializable id = 42;
		Serializable entityPk = 100;
		relatedKeys.add(id);
		final String COL_NAME = "id";
		final String PK_NAME = "pk";
		Object entity = new Object();
		OneToManyRelationship mockOneToManyRelationship = mock(OneToManyRelationship.class);
		doReturn(Integer.class).when(mockOneToManyRelationship).getManyType();
		when(mockOneToManyRelationship.getColumn()).thenReturn(COL_NAME);
		when(mockPersistencePolicy.getModelTableName(Integer.class)).thenReturn(MODEL_TABLE_1);
		Field field = ArrayList.class.getDeclaredFields()[0];
		when(mockPersistencePolicy.getPrimaryKeyField(Object.class)).thenReturn(field);
		when(mockPersistencePolicy.getPrimaryKeyField(Integer.class)).thenReturn(field);
		when(mockPersistencePolicy.getFieldColumnName(field)).thenReturn(PK_NAME);
		when(mockPersistencePolicy.getPrimaryKey(entity)).thenReturn(entityPk);
		when(mockSqliteMapper.getSqliteDataType(field)).thenReturn(SqliteDataType.INTEGER);
		when(mockSqliteMapper.getSqliteDataType(id)).thenReturn(SqliteDataType.INTEGER);

		// Run
		String expected = "UPDATE " + MODEL_TABLE_1 + " SET " + COL_NAME + " = " + entityPk + " WHERE " + PK_NAME + " IN (" + id + ")";
		String actual = sqliteBuilder.createUpdateForeignKeyQuery(mockOneToManyRelationship, entity, relatedKeys);

		// Verify
		verify(mockOneToManyRelationship, times(2)).getManyType();
		verify(mockOneToManyRelationship).getColumn();
		verify(mockPersistencePolicy).getModelTableName(mockOneToManyRelationship.getManyType());
		verify(mockPersistencePolicy).getPrimaryKeyField(Object.class);
		verify(mockPersistencePolicy).getPrimaryKeyField(Integer.class);
		verify(mockPersistencePolicy).getPrimaryKey(entity);
		verify(mockPersistencePolicy).getFieldColumnName(field);
		verify(mockSqliteMapper).getSqliteDataType(field);
		verify(mockSqliteMapper).getSqliteDataType(id);
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}

	@Test
	public void testCreateUpdateForeignKeyQuery_multipleRelated() {
		// Setup
		List<Serializable> relatedKeys = new ArrayList<Serializable>();
		Serializable id1 = 42;
		Serializable id2 = 38;
		Serializable id3 = 71;
		Serializable entityPk = 100;
		relatedKeys.add(id1);
		relatedKeys.add(id2);
		relatedKeys.add(id3);
		final String COL_NAME = "id";
		final String PK_NAME = "pk";
		Object entity = new Object();
		OneToManyRelationship mockOneToManyRelationship = mock(OneToManyRelationship.class);
		doReturn(Integer.class).when(mockOneToManyRelationship).getManyType();
		when(mockOneToManyRelationship.getColumn()).thenReturn(COL_NAME);
		when(mockPersistencePolicy.getModelTableName(Integer.class)).thenReturn(MODEL_TABLE_1);
		Field field = ArrayList.class.getDeclaredFields()[0];
		when(mockPersistencePolicy.getPrimaryKeyField(Object.class)).thenReturn(field);
		when(mockPersistencePolicy.getPrimaryKeyField(Integer.class)).thenReturn(field);
		when(mockPersistencePolicy.getFieldColumnName(field)).thenReturn(PK_NAME);
		when(mockPersistencePolicy.getPrimaryKey(entity)).thenReturn(entityPk);
		when(mockSqliteMapper.getSqliteDataType(field)).thenReturn(SqliteDataType.INTEGER);
		when(mockSqliteMapper.getSqliteDataType(id1)).thenReturn(SqliteDataType.INTEGER);
		when(mockSqliteMapper.getSqliteDataType(id2)).thenReturn(SqliteDataType.INTEGER);
		when(mockSqliteMapper.getSqliteDataType(id3)).thenReturn(SqliteDataType.INTEGER);

		// Run
		String expected = "UPDATE " + MODEL_TABLE_1 + " SET " + COL_NAME + " = " + entityPk + " WHERE " + PK_NAME + " IN (" + id1 + ", " + id2 + ", " + id3 + ")";
		String actual = sqliteBuilder.createUpdateForeignKeyQuery(mockOneToManyRelationship, entity, relatedKeys);

		// Verify
		verify(mockOneToManyRelationship, times(2)).getManyType();
		verify(mockOneToManyRelationship).getColumn();
		verify(mockPersistencePolicy).getModelTableName(mockOneToManyRelationship.getManyType());
		verify(mockPersistencePolicy).getPrimaryKeyField(Object.class);
		verify(mockPersistencePolicy).getPrimaryKeyField(Integer.class);
		verify(mockPersistencePolicy).getPrimaryKey(entity);
		verify(mockPersistencePolicy).getFieldColumnName(field);
		verify(mockSqliteMapper).getSqliteDataType(field);
		verify(mockSqliteMapper).getSqliteDataType(id1);
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}

	@Test
	public void testCreateUpdateOneToOneForeignKeyQuery() {
		// Setup
		Integer related = 42;
		Serializable entityPk = 30;
		Serializable relatedPk = 100;
		final String COL_NAME = "id";
		final String FK_COL = "fk";
		Object entity = new Object();
		OneToOneRelationship mockOneToOneRelationship = mock(OneToOneRelationship.class);
		when(mockOneToOneRelationship.getColumn()).thenReturn(FK_COL);
		when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);
		Field field1 = ArrayList.class.getDeclaredFields()[0];
		Field field2 = ArrayList.class.getDeclaredFields()[1];
		when(mockPersistencePolicy.getPrimaryKeyField(Integer.class)).thenReturn(field1);
		when(mockPersistencePolicy.getPrimaryKey(related)).thenReturn(relatedPk);
		when(mockPersistencePolicy.getPrimaryKeyField(Object.class)).thenReturn(field2);
		when(mockPersistencePolicy.getFieldColumnName(field2)).thenReturn(COL_NAME);
		when(mockPersistencePolicy.getPrimaryKey(entity)).thenReturn(entityPk);
		when(mockSqliteMapper.getSqliteDataType(any(Field.class))).thenReturn(SqliteDataType.INTEGER);

		// Run
		String expected = "UPDATE " + MODEL_TABLE_1 + " SET " + FK_COL + " = " + relatedPk + " WHERE " + COL_NAME + " = " + entityPk;
		String actual = sqliteBuilder.createUpdateOneToOneForeignKeyQuery(mockOneToOneRelationship, entity, related);

		// Verify
		verify(mockOneToOneRelationship).getColumn();
		verify(mockPersistencePolicy).getModelTableName(Object.class);
		verify(mockPersistencePolicy).getPrimaryKeyField(related.getClass());
		verify(mockPersistencePolicy).getPrimaryKeyField(entity.getClass());
		verify(mockPersistencePolicy).getPrimaryKey(related);
		verify(mockPersistencePolicy).getPrimaryKey(entity);
		verify(mockPersistencePolicy).getFieldColumnName(field2);
		verify(mockSqliteMapper).getSqliteDataType(field1);
		verify(mockSqliteMapper).getSqliteDataType(field2);
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}

	@Test
	public void testCreateManyToManyDeleteQuery_firstType() {
		// Setup
		Object entity = new Object();
		doReturn(Object.class).when(mockManyToManyRelationship).getFirstType();
		when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);
		Field field = ArrayList.class.getDeclaredFields()[0];
		final String COL_NAME = "col";
		Serializable pk = 42;
		when(mockManyToManyRelationship.getFirstField()).thenReturn(field);
		when(mockPersistencePolicy.getFieldColumnName(field)).thenReturn(COL_NAME);
		when(mockPersistencePolicy.getPrimaryKeyField(entity.getClass())).thenReturn(field);
		when(mockPersistencePolicy.getPrimaryKey(entity)).thenReturn(pk);
		when(mockSqliteMapper.getSqliteDataType(field)).thenReturn(SqliteDataType.INTEGER);

		// Run
		String expected = "DELETE FROM " + MTM_TABLE + " WHERE " + MODEL_TABLE_1 + "_" + COL_NAME + "_1 = " + pk;
		String actual = sqliteBuilder.createManyToManyDeleteQuery(entity, mockManyToManyRelationship);

		// Verify
		verify(mockManyToManyRelationship).getTableName();
		verify(mockManyToManyRelationship, times(2)).getFirstType();
		verify(mockManyToManyRelationship).getFirstField();
		verify(mockPersistencePolicy).getModelTableName(Object.class);
		verify(mockPersistencePolicy).getFieldColumnName(field);
		verify(mockPersistencePolicy).getPrimaryKeyField(entity.getClass());
		verify(mockPersistencePolicy).getPrimaryKey(entity);
		verify(mockSqliteMapper).getSqliteDataType(field);
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}

	@Test
	public void testCreateManyToManyDeleteQuery_secondType() {
		// Setup
		Object entity = new Object();
		doReturn(Integer.class).when(mockManyToManyRelationship).getFirstType();
		doReturn(Object.class).when(mockManyToManyRelationship).getSecondType();
		when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);
		Field field = ArrayList.class.getDeclaredFields()[0];
		final String COL_NAME = "col";
		Serializable pk = 42;
		when(mockManyToManyRelationship.getFirstField()).thenReturn(field);
		when(mockPersistencePolicy.getFieldColumnName(field)).thenReturn(COL_NAME);
		when(mockPersistencePolicy.getPrimaryKeyField(entity.getClass())).thenReturn(field);
		when(mockPersistencePolicy.getPrimaryKey(entity)).thenReturn(pk);
		when(mockSqliteMapper.getSqliteDataType(field)).thenReturn(SqliteDataType.INTEGER);

		// Run
		String expected = "DELETE FROM " + MTM_TABLE + " WHERE " + MODEL_TABLE_1 + "_" + COL_NAME + "_2 = " + pk;
		String actual = sqliteBuilder.createManyToManyDeleteQuery(entity, mockManyToManyRelationship);

		// Verify
		verify(mockManyToManyRelationship).getTableName();
		verify(mockManyToManyRelationship).getFirstType();
		verify(mockManyToManyRelationship).getSecondType();
		verify(mockManyToManyRelationship).getSecondField();
		verify(mockPersistencePolicy).getModelTableName(Object.class);
		verify(mockPersistencePolicy).getFieldColumnName(field);
		verify(mockPersistencePolicy).getPrimaryKeyField(entity.getClass());
		verify(mockPersistencePolicy).getPrimaryKey(entity);
		verify(mockSqliteMapper).getSqliteDataType(field);
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}

	@Test
	public void testCreateUpdateQuery() {
		// Setup
		Object entity = new Object();
		Object related = new Object();
		Serializable pk = 42;
		Serializable fk = 103;
		final String PK_NAME = "pk";
		final String COL_NAME = "col";
		when(mockPersistencePolicy.getPrimaryKey(related)).thenReturn(fk);
		when(mockPersistencePolicy.getPrimaryKey(entity)).thenReturn(pk);
		when(mockPersistencePolicy.getModelTableName(entity.getClass())).thenReturn(MODEL_TABLE_1);
		Field field = ArrayList.class.getDeclaredFields()[0];
		when(mockPersistencePolicy.getPrimaryKeyField(related.getClass())).thenReturn(field);
		when(mockPersistencePolicy.getPrimaryKeyField(entity.getClass())).thenReturn(field);
		when(mockPersistencePolicy.getFieldColumnName(field)).thenReturn(PK_NAME);
		when(mockSqliteMapper.getSqliteDataType(field)).thenReturn(SqliteDataType.INTEGER);

		// Run
		String expected = "UPDATE " + MODEL_TABLE_1 + " SET " + COL_NAME + " = " + fk + " WHERE " + PK_NAME + " = " + pk;
		String actual = sqliteBuilder.createUpdateQuery(entity, related, COL_NAME);

		// Verify
		verify(mockPersistencePolicy).getPrimaryKey(related);
		verify(mockPersistencePolicy).getPrimaryKey(entity);
		verify(mockPersistencePolicy).getModelTableName(entity.getClass());
		verify(mockPersistencePolicy, times(3)).getPrimaryKeyField(related.getClass());
		verify(mockPersistencePolicy, times(3)).getPrimaryKeyField(entity.getClass());
		verify(mockPersistencePolicy).getFieldColumnName(field);
		verify(mockSqliteMapper, times(2)).getSqliteDataType(field);
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}

    @Test
    public void testGetAssociationCriteriaDiscriminator_oneToOne_associatedOwner() throws NoSuchFieldException {
        // Setup
        Field pkField = Foo.class.getDeclaredField("id");
        when(mockPersistencePolicy.getPrimaryKeyField(Foo.class)).thenReturn(pkField);
        String pkCol = "id";
        when(mockPersistencePolicy.getFieldColumnName(pkField)).thenReturn(pkCol);
        when(mockPersistencePolicy.getPrimaryKeyField(Object.class)).thenReturn(pkField);
        when(mockAssociationCriteria.getRelationship()).thenReturn(mockOtoRelationship);
        when(mockOtoRelationship.getRelationType()).thenReturn(ModelRelationship.RelationType.OneToOne);
        doReturn(Foo.class).when(mockOtoRelationship).getOwner();
        String fkCol = "fk";
        when(mockOtoRelationship.getColumn()).thenReturn(fkCol);
        Field field = Foo.class.getDeclaredField("bar");
        when(mockAssociationCriteria.getRelationshipField()).thenReturn(field);
        doReturn(Foo.class).when(mockAssociationCriteria).getEntityClass();

        String fooTable = "foo";
        when(mockPersistencePolicy.getModelTableName(Foo.class)).thenReturn(fooTable);
        List<Criterion> mockCriterionList = new ArrayList<Criterion>();
        mockCriterionList.add(mockCriterionA);
        when(mockAssociationCriteria.getCriterion()).thenReturn(mockCriterionList);

        // Run
        String expected = pkCol + " IN (SELECT " + fkCol + " FROM " + fooTable + " WHERE " + CRITERION_A_SQL + ")";
        String actual = sqliteBuilder.getAssociationCriteriaDiscriminator(Object.class, mockAssociationCriteria);

        // Verify
        assertEquals("Returned SQL fragment should match expected value", expected, actual);
    }

    @Test
    public void testGetAssociationCriteriaDiscriminator_oneToOne_associatedNotOwner() throws NoSuchFieldException {
        // Setup
        Field pkField = Foo.class.getDeclaredField("id");
        when(mockPersistencePolicy.getPrimaryKeyField(Foo.class)).thenReturn(pkField);
        String pkCol = "id";
        when(mockPersistencePolicy.getFieldColumnName(pkField)).thenReturn(pkCol);
        when(mockPersistencePolicy.getPrimaryKeyField(Object.class)).thenReturn(pkField);
        when(mockAssociationCriteria.getRelationship()).thenReturn(mockOtoRelationship);
        when(mockOtoRelationship.getRelationType()).thenReturn(ModelRelationship.RelationType.OneToOne);
        doReturn(Object.class).when(mockOtoRelationship).getOwner();
        String fkCol = "fk";
        when(mockOtoRelationship.getColumn()).thenReturn(fkCol);
        Field relationshipField = Foo.class.getDeclaredField("bar");
        when(mockAssociationCriteria.getRelationshipField()).thenReturn(relationshipField);
        String relCol = relationshipField.getName();
        when(mockPersistencePolicy.getFieldColumnName(relationshipField)).thenReturn(relCol);
        doReturn(Foo.class).when(mockAssociationCriteria).getEntityClass();

        String fooTable = "foo";
        when(mockPersistencePolicy.getModelTableName(Foo.class)).thenReturn(fooTable);
        List<Criterion> mockCriterionList = new ArrayList<Criterion>();
        mockCriterionList.add(mockCriterionA);
        when(mockAssociationCriteria.getCriterion()).thenReturn(mockCriterionList);

        // Run
        String expected = relCol + " IN (SELECT " + pkCol + " FROM " + fooTable + " WHERE " + CRITERION_A_SQL + ")";
        String actual = sqliteBuilder.getAssociationCriteriaDiscriminator(Object.class, mockAssociationCriteria);

        // Verify
        assertEquals("Returned SQL fragment should match expected value", expected, actual);
    }

    private class Foo {

        private long id;
        private int bar;

    }

}
