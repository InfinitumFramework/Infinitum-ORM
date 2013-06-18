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

import android.database.Cursor;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.context.InfinitumOrmContext;
import com.clarionmedia.infinitum.orm.context.InfinitumOrmContext.SessionType;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.relationship.ModelRelationship;
import com.clarionmedia.infinitum.orm.sql.SqlBuilder;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class SqliteAssociationCriteriaTest {

    private SqlBuilder mockSqlBuilder;
    private SqliteSession mockSqliteSession;
    private InfinitumOrmContext mockInfinitumContext;
    private SqliteModelFactory mockSqliteModelFactory;
    private SqliteMapper mockSqliteMapper;
    private Class<Object> entityClass;
    private SqliteAssociationCriteria sqliteAssociationCriteria;
    private SqliteCriteria parentCriteria;
    private PersistencePolicy mockPersistencePolicy;
    private Cursor mockCursor;
    private ModelRelationship mockRelationship;

    @Before
    public void setup() {
        entityClass = Object.class;
        mockSqliteSession = mock(SqliteSession.class);
        mockInfinitumContext = mock(InfinitumOrmContext.class);
        mockSqlBuilder = mock(SqlBuilder.class);
        mockSqliteModelFactory = mock(SqliteModelFactory.class);
        mockSqliteMapper = mock(SqliteMapper.class);
        mockPersistencePolicy = mock(PersistencePolicy.class);
        mockRelationship = mock(ModelRelationship.class);
        when(mockInfinitumContext.getPersistencePolicy()).thenReturn(mockPersistencePolicy);
        when(mockInfinitumContext.getSession(SessionType.SQLITE)).thenReturn(mockSqliteSession);
        when(mockPersistencePolicy.isPersistent(entityClass)).thenReturn(true);
        parentCriteria = new SqliteCriteria<Object>(mockInfinitumContext, entityClass, mockSqliteModelFactory,
                mockSqlBuilder, null);
        sqliteAssociationCriteria = new SqliteAssociationCriteria(mockInfinitumContext, entityClass,
                mockSqliteModelFactory, mockSqlBuilder, mockRelationship, null, parentCriteria);
        mockCursor = mock(Cursor.class);
    }

    @Test
    public void testGetRepresentation() {
        // Setup
        String expected = "SQL criteria query";
        when(mockSqlBuilder.createQuery(sqliteAssociationCriteria)).thenReturn(expected);

        // Run
        String actual = sqliteAssociationCriteria.getRepresentation();

        // Verify
        verify(mockSqlBuilder).createQuery(sqliteAssociationCriteria);
        assertEquals("Returned query should match expected value", expected, actual);
    }

    @Test
    public void testList_noResults() {
        // Setup
        String query = "SQL criteria query";
        when(mockSqlBuilder.createQuery(parentCriteria)).thenReturn(query);
        when(mockSqliteSession.executeForResult(query)).thenReturn(mockCursor);
        when(mockCursor.getCount()).thenReturn(0);

        // Run
        List<Object> actual = sqliteAssociationCriteria.list();

        // Verify
        verify(mockSqliteSession).executeForResult(query);
        verify(mockCursor, times(2)).getCount();
        verify(mockCursor).close();
        assertEquals("Returned list should be empty", 0, actual.size());
    }

    @Test
    public void testList_results() {
        // Setup
        String query = "SQL criteria query";
        when(mockSqlBuilder.createQuery(parentCriteria)).thenReturn(query);
        when(mockSqliteSession.executeForResult(query)).thenReturn(mockCursor);
        final int RESULT_COUNT = 3;
        when(mockCursor.getCount()).thenReturn(RESULT_COUNT);
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
        when(mockSqliteModelFactory.createFromCursor(mockCursor, entityClass)).thenReturn(new Object());

        // Run
        List<Object> actual = sqliteAssociationCriteria.list();

        // Verify
        verify(mockSqliteSession).executeForResult(query);
        verify(mockCursor, times(2)).getCount();
        verify(mockCursor).close();
        verify(mockCursor, times(4)).moveToNext();
        verify(mockSqliteModelFactory, times(3)).createFromCursor(mockCursor, entityClass);
        verify(mockSqliteSession, times(3)).cache(any(Integer.class), any(Object.class));
        assertEquals("Returned list should be empty", RESULT_COUNT, actual.size());
    }

    @Test
    public void testUnique_noResult() {
        // Setup
        String query = "SQL criteria query";
        when(mockSqlBuilder.createQuery(parentCriteria)).thenReturn(query);
        when(mockSqliteSession.executeForResult(query)).thenReturn(mockCursor);
        when(mockCursor.getCount()).thenReturn(0);

        // Run
        Object actual = sqliteAssociationCriteria.unique();

        // Verify
        verify(mockSqliteSession).executeForResult(query);
        verify(mockCursor, times(2)).getCount();
        verify(mockCursor).close();
        assertNull("Returned result should be null", actual);
    }

    @Test
    public void testUnique_result() {
        // Setup
        String query = "SQL criteria query";
        when(mockSqlBuilder.createQuery(parentCriteria)).thenReturn(query);
        when(mockSqliteSession.executeForResult(query)).thenReturn(mockCursor);
        when(mockCursor.getCount()).thenReturn(1);
        when(mockSqliteModelFactory.createFromCursor(mockCursor, entityClass)).thenReturn(new Object());

        // Run
        Object actual = sqliteAssociationCriteria.unique();

        // Verify
        verify(mockSqliteSession).executeForResult(query);
        verify(mockCursor, times(2)).getCount();
        verify(mockCursor).close();
        assertNotNull("Returned result should not be null", actual);
    }

    @Test(expected = InfinitumRuntimeException.class)
    public void testUnique_noUnique() {
        // Setup
        String query = "SQL criteria query";
        when(mockSqlBuilder.createQuery(parentCriteria)).thenReturn(query);
        when(mockSqliteSession.executeForResult(query)).thenReturn(mockCursor);
        when(mockCursor.getCount()).thenReturn(3);
        when(mockSqliteModelFactory.createFromCursor(mockCursor, entityClass)).thenReturn(new Object());

        // Run
        sqliteAssociationCriteria.unique();

        // Verify
        assertTrue("Exception should have been thrown", false);
    }

    @Test
    public void testGetObjectMapper() {
        // Setup
        when(mockSqliteSession.getSqliteMapper()).thenReturn(mockSqliteMapper);

        // Run
        SqliteMapper actual = sqliteAssociationCriteria.getObjectMapper();

        // Verify
        verify(mockSqliteSession).getSqliteMapper();
        assertEquals("Returned result should match expected value", mockSqliteMapper, actual);
    }

    @Test
    public void testCount() {
        // Setup
        String query = "SQL criteria query";
        when(mockSqlBuilder.createCountQuery(parentCriteria)).thenReturn(query);
        when(mockSqliteSession.executeForResult(query)).thenReturn(mockCursor);
        when(mockCursor.moveToFirst()).thenReturn(true);
        final long EXPECTED = 5;
        when(mockCursor.getLong(0)).thenReturn(EXPECTED);

        // Run
        long actual = sqliteAssociationCriteria.count();

        // Verify
        verify(mockSqliteSession).executeForResult(query);
        verify(mockSqlBuilder).createCountQuery(parentCriteria);
        verify(mockCursor).moveToFirst();
        verify(mockCursor).getLong(0);
        verify(mockCursor).close();
        assertEquals("Returned result should match expected value", EXPECTED, actual);
    }

}
