package org.irods.jargon.core.query;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;

public class IRODSSimpleQueryResultSetTest {

	@Test
	public void testGetNumberOfResultColumns() {
		int colCount = 10;
		AbstractAliasedQuery simpleQuery = Mockito.mock(SimpleQuery.class);
		@SuppressWarnings("unchecked")
		List<IRODSQueryResultRow> results = Mockito.mock(List.class);
		@SuppressWarnings("unchecked")
		List<String> columnNames = Mockito.mock(List.class);
		Mockito.when(columnNames.size()).thenReturn(colCount);
		IRODSSimpleQueryResultSet resultSet = IRODSSimpleQueryResultSet
				.instance(simpleQuery, results, columnNames, false);
		Assert.assertEquals("did not get col count", colCount,
				resultSet.getNumberOfResultColumns());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceNullSimpleQuery() {
		AbstractAliasedQuery simpleQuery = null;
		@SuppressWarnings("unchecked")
		List<IRODSQueryResultRow> results = Mockito.mock(List.class);
		@SuppressWarnings("unchecked")
		List<String> columnNames = Mockito.mock(List.class);
		IRODSSimpleQueryResultSet.instance(simpleQuery, results, columnNames,
				false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceNullResults() {
		AbstractAliasedQuery simpleQuery = Mockito.mock(SimpleQuery.class);
		List<IRODSQueryResultRow> results = null;
		@SuppressWarnings("unchecked")
		List<String> columnNames = Mockito.mock(List.class);
		IRODSSimpleQueryResultSet.instance(simpleQuery, results, columnNames,
				false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceNullColumnNames() {
		AbstractAliasedQuery simpleQuery = Mockito.mock(SimpleQuery.class);
		@SuppressWarnings("unchecked")
		List<IRODSQueryResultRow> results = Mockito.mock(List.class);
		List<String> columnNames = null;
		IRODSSimpleQueryResultSet.instance(simpleQuery, results, columnNames,
				false);
	}
}
