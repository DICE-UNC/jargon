package org.irods.jargon.core.query;

import static org.junit.Assert.*;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;
import org.mockito.Mockito;

public class IRODSSimpleQueryResultSetTest {

	@Test
	public void testGetNumberOfResultColumns() {
		int colCount = 10;
		SimpleQuery simpleQuery = Mockito.mock(SimpleQuery.class);
		List<IRODSQueryResultRow> results = Mockito.mock(List.class);
		List<String> columnNames = Mockito.mock(List.class);
		Mockito.when(columnNames.size()).thenReturn(colCount);
		IRODSSimpleQueryResultSet resultSet = IRODSSimpleQueryResultSet.instance(simpleQuery, results, columnNames, false);
		TestCase.assertEquals("did not get col count", colCount, resultSet.getNumberOfResultColumns());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInstanceNullSimpleQuery() {
		int colCount = 10;
		SimpleQuery simpleQuery = null;
		List<IRODSQueryResultRow> results = Mockito.mock(List.class);
		List<String> columnNames = Mockito.mock(List.class);
		IRODSSimpleQueryResultSet.instance(simpleQuery, results, columnNames, false);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInstanceNullResults() {
		int colCount = 10;
		SimpleQuery simpleQuery = Mockito.mock(SimpleQuery.class);
		List<IRODSQueryResultRow> results = null;
		List<String> columnNames = Mockito.mock(List.class);
		IRODSSimpleQueryResultSet.instance(simpleQuery, results, columnNames, false);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testInstanceNullColumnNames() {
		int colCount = 10;
		SimpleQuery simpleQuery = Mockito.mock(SimpleQuery.class);
		List<IRODSQueryResultRow> results = Mockito.mock(List.class);
		List<String> columnNames = null;
		IRODSSimpleQueryResultSet.instance(simpleQuery, results, columnNames, false);
	}
}
