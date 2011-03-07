package org.irods.jargon.core.query;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.irods.jargon.core.exception.JargonException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSQueryResultRowTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testInstance() throws Exception {

		List<GenQuerySelectField> selectFields = new ArrayList<GenQuerySelectField>();
		selectFields.add(GenQuerySelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_ACTION_ID,
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(GenQuerySelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_COMMENT,
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(GenQuerySelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_CREATE_TIME,
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD));

		TranslatedIRODSGenQuery query = mock(TranslatedIRODSGenQuery.class);
		when(query.getSelectFields()).thenReturn(selectFields);

		List<String> resultColumns = new ArrayList<String>();
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_ACTION_ID.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_COMMENT.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_CREATE_TIME.getName());

		IRODSQueryResultRow resultRow = IRODSQueryResultRow.instance(
				resultColumns, resultColumns);
		Assert.assertNotNull("no result row created", resultRow);

	}

	@Test(expected = JargonException.class)
	public void testInstanceNullResultSet() throws Exception {

		List<GenQuerySelectField> selectFields = new ArrayList<GenQuerySelectField>();
		selectFields.add(GenQuerySelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_ACTION_ID,
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(GenQuerySelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_COMMENT,
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(GenQuerySelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_CREATE_TIME,
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD));

		TranslatedIRODSGenQuery query = mock(TranslatedIRODSGenQuery.class);
		when(query.getSelectFields()).thenReturn(selectFields);
		IRODSQueryResultRow.instance(null, new ArrayList<String>());

	}

	@Test(expected = JargonException.class)
	public void testInstanceNullTranslatedQuery() throws Exception {

		List<String> resultColumns = new ArrayList<String>();
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_ACTION_ID.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_COMMENT.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_CREATE_TIME.getName());
		IRODSQueryResultRow.instance(resultColumns, null);

	}

	@Test
	public void testGetByColumnId() throws Exception {

		List<GenQuerySelectField> selectFields = new ArrayList<GenQuerySelectField>();
		selectFields.add(GenQuerySelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_ACTION_ID,
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(GenQuerySelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_COMMENT,
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(GenQuerySelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_CREATE_TIME,
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD));

		TranslatedIRODSGenQuery query = mock(TranslatedIRODSGenQuery.class);
		when(query.getSelectFields()).thenReturn(selectFields);

		List<String> resultColumns = new ArrayList<String>();
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_ACTION_ID.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_COMMENT.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_CREATE_TIME.getName());

		IRODSQueryResultRow resultRow = IRODSQueryResultRow.instance(
				resultColumns, new ArrayList<String>());
		String actualColumn = resultRow.getColumn(0);
		Assert.assertEquals("did not get expected column",
				RodsGenQueryEnum.COL_AUDIT_ACTION_ID.getName(), actualColumn);

	}

	@Test(expected = JargonException.class)
	public void testGetByColumnIdOutOfRange() throws Exception {

		List<GenQuerySelectField> selectFields = new ArrayList<GenQuerySelectField>();
		selectFields.add(GenQuerySelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_ACTION_ID,
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(GenQuerySelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_COMMENT,
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(GenQuerySelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_CREATE_TIME,
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD));

		TranslatedIRODSGenQuery query = mock(TranslatedIRODSGenQuery.class);
		when(query.getSelectFields()).thenReturn(selectFields);

		List<String> resultColumns = new ArrayList<String>();
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_ACTION_ID.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_COMMENT.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_CREATE_TIME.getName());

		IRODSQueryResultRow resultRow = IRODSQueryResultRow.instance(
				resultColumns, new ArrayList<String>());
		resultRow.getColumn(99);

	}

	@Test
	public void testGetByColumnName() throws Exception {

		List<GenQuerySelectField> selectFields = new ArrayList<GenQuerySelectField>();
		selectFields.add(GenQuerySelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_ACTION_ID,
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(GenQuerySelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_COMMENT,
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(GenQuerySelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_CREATE_TIME,
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD));

		TranslatedIRODSGenQuery query = mock(TranslatedIRODSGenQuery.class);
		when(query.getSelectFields()).thenReturn(selectFields);

		List<String> resultColumns = new ArrayList<String>();
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_ACTION_ID.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_COMMENT.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_CREATE_TIME.getName());

		IRODSQueryResultRow resultRow = IRODSQueryResultRow.instance(
				resultColumns, resultColumns);
		String actualColumn = resultRow
				.getColumn(RodsGenQueryEnum.COL_AUDIT_ACTION_ID.getName());
		Assert.assertEquals("did not get expected column",
				RodsGenQueryEnum.COL_AUDIT_ACTION_ID.getName(), actualColumn);

	}

	@Test(expected = JargonException.class)
	public void testGetByColumnNullName() throws Exception {

		List<GenQuerySelectField> selectFields = new ArrayList<GenQuerySelectField>();
		selectFields.add(GenQuerySelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_ACTION_ID,
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(GenQuerySelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_COMMENT,
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(GenQuerySelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_CREATE_TIME,
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		TranslatedIRODSGenQuery query = mock(TranslatedIRODSGenQuery.class);
		when(query.getSelectFields()).thenReturn(selectFields);

		List<String> resultColumns = new ArrayList<String>();
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_ACTION_ID.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_COMMENT.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_CREATE_TIME.getName());

		IRODSQueryResultRow resultRow = IRODSQueryResultRow.instance(
				resultColumns, new ArrayList<String>());
		resultRow.getColumn(null);

	}

	@Test(expected = JargonException.class)
	public void testGetByColumnBlankName() throws Exception {

		List<GenQuerySelectField> selectFields = new ArrayList<GenQuerySelectField>();
		selectFields.add(GenQuerySelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_ACTION_ID,
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(GenQuerySelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_COMMENT,
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(GenQuerySelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_CREATE_TIME,
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD));

		TranslatedIRODSGenQuery query = mock(TranslatedIRODSGenQuery.class);
		when(query.getSelectFields()).thenReturn(selectFields);

		List<String> resultColumns = new ArrayList<String>();
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_ACTION_ID.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_COMMENT.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_CREATE_TIME.getName());

		IRODSQueryResultRow resultRow = IRODSQueryResultRow.instance(
				resultColumns, new ArrayList<String>());
		resultRow.getColumn("");

	}

	@Test(expected = JargonException.class)
	public void testGetByColumnBogusName() throws Exception {

		List<GenQuerySelectField> selectFields = new ArrayList<GenQuerySelectField>();
		selectFields.add(GenQuerySelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_ACTION_ID,
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(GenQuerySelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_COMMENT,
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(GenQuerySelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_CREATE_TIME,
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD));

		TranslatedIRODSGenQuery query = mock(TranslatedIRODSGenQuery.class);
		when(query.getSelectFields()).thenReturn(selectFields);

		List<String> resultColumns = new ArrayList<String>();
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_ACTION_ID.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_COMMENT.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_CREATE_TIME.getName());

		IRODSQueryResultRow resultRow = IRODSQueryResultRow.instance(
				resultColumns, new ArrayList<String>());
		resultRow.getColumn("bogus");

	}
	
	

}
