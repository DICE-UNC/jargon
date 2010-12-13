package org.irods.jargon.core.query;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.irods.jargon.core.exception.JargonException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class IRODSQueryResultRowTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	// FIXME: fix mockito errors

	@Test
	public void testInstance() throws Exception {

		List<SelectField> selectFields = new ArrayList<SelectField>();
		selectFields.add(SelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_ACTION_ID,
				SelectField.SelectFieldTypes.FIELD,
				SelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(SelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_COMMENT,
				SelectField.SelectFieldTypes.FIELD,
				SelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(SelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_CREATE_TIME,
				SelectField.SelectFieldTypes.FIELD,
				SelectField.SelectFieldSource.DEFINED_QUERY_FIELD));

		TranslatedIRODSQuery query = mock(TranslatedIRODSQuery.class);
		when(query.getSelectFields()).thenReturn(selectFields);

		List<String> resultColumns = new ArrayList<String>();
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_ACTION_ID.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_COMMENT.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_CREATE_TIME.getName());

		IRODSQueryResultRow resultRow = IRODSQueryResultRow.instance(
				resultColumns, query);
		TestCase.assertNotNull("no result row created", resultRow);

	}

	@Test(expected = JargonException.class)
	public void testInstanceNullResultSet() throws Exception {

		List<SelectField> selectFields = new ArrayList<SelectField>();
		selectFields.add(SelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_ACTION_ID,
				SelectField.SelectFieldTypes.FIELD,
				SelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(SelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_COMMENT,
				SelectField.SelectFieldTypes.FIELD,
				SelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(SelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_CREATE_TIME,
				SelectField.SelectFieldTypes.FIELD,
				SelectField.SelectFieldSource.DEFINED_QUERY_FIELD));

		TranslatedIRODSQuery query = mock(TranslatedIRODSQuery.class);
		when(query.getSelectFields()).thenReturn(selectFields);
		@SuppressWarnings("unused")
		IRODSQueryResultRow resultRow = IRODSQueryResultRow.instance(null,
				query);

	}

	@Test(expected = JargonException.class)
	public void testInstanceNullTranslatedQuery() throws Exception {

		List<String> resultColumns = new ArrayList<String>();
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_ACTION_ID.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_COMMENT.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_CREATE_TIME.getName());
		@SuppressWarnings("unused")
		IRODSQueryResultRow resultRow = IRODSQueryResultRow.instance(
				resultColumns, null);

	}

	@Test
	public void testGetByColumnId() throws Exception {

		List<SelectField> selectFields = new ArrayList<SelectField>();
		selectFields.add(SelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_ACTION_ID,
				SelectField.SelectFieldTypes.FIELD,
				SelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(SelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_COMMENT,
				SelectField.SelectFieldTypes.FIELD,
				SelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(SelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_CREATE_TIME,
				SelectField.SelectFieldTypes.FIELD,
				SelectField.SelectFieldSource.DEFINED_QUERY_FIELD));

		TranslatedIRODSQuery query = mock(TranslatedIRODSQuery.class);
		when(query.getSelectFields()).thenReturn(selectFields);

		List<String> resultColumns = new ArrayList<String>();
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_ACTION_ID.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_COMMENT.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_CREATE_TIME.getName());

		IRODSQueryResultRow resultRow = IRODSQueryResultRow.instance(
				resultColumns, query);
		String actualColumn = resultRow.getColumn(0);
		TestCase.assertEquals("did not get expected column",
				RodsGenQueryEnum.COL_AUDIT_ACTION_ID.getName(), actualColumn);

	}

	@Test(expected = JargonException.class)
	public void testGetByColumnIdOutOfRange() throws Exception {

		List<SelectField> selectFields = new ArrayList<SelectField>();
		selectFields.add(SelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_ACTION_ID,
				SelectField.SelectFieldTypes.FIELD,
				SelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(SelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_COMMENT,
				SelectField.SelectFieldTypes.FIELD,
				SelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(SelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_CREATE_TIME,
				SelectField.SelectFieldTypes.FIELD,
				SelectField.SelectFieldSource.DEFINED_QUERY_FIELD));

		TranslatedIRODSQuery query = mock(TranslatedIRODSQuery.class);
		when(query.getSelectFields()).thenReturn(selectFields);

		List<String> resultColumns = new ArrayList<String>();
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_ACTION_ID.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_COMMENT.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_CREATE_TIME.getName());

		IRODSQueryResultRow resultRow = IRODSQueryResultRow.instance(
				resultColumns, query);
		resultRow.getColumn(99);

	}

	@Test
	public void testGetByColumnName() throws Exception {

		List<SelectField> selectFields = new ArrayList<SelectField>();
		selectFields.add(SelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_ACTION_ID,
				SelectField.SelectFieldTypes.FIELD,
				SelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(SelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_COMMENT,
				SelectField.SelectFieldTypes.FIELD,
				SelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(SelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_CREATE_TIME,
				SelectField.SelectFieldTypes.FIELD,
				SelectField.SelectFieldSource.DEFINED_QUERY_FIELD));

		TranslatedIRODSQuery query = mock(TranslatedIRODSQuery.class);
		when(query.getSelectFields()).thenReturn(selectFields);

		List<String> resultColumns = new ArrayList<String>();
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_ACTION_ID.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_COMMENT.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_CREATE_TIME.getName());

		IRODSQueryResultRow resultRow = IRODSQueryResultRow.instance(
				resultColumns, query);
		String actualColumn = resultRow
				.getColumn(RodsGenQueryEnum.COL_AUDIT_ACTION_ID.getName());
		TestCase.assertEquals("did not get expected column",
				RodsGenQueryEnum.COL_AUDIT_ACTION_ID.getName(), actualColumn);

	}

	@Test(expected = JargonException.class)
	public void testGetByColumnNullName() throws Exception {

		List<SelectField> selectFields = new ArrayList<SelectField>();
		selectFields.add(SelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_ACTION_ID,
				SelectField.SelectFieldTypes.FIELD,
				SelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(SelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_COMMENT,
				SelectField.SelectFieldTypes.FIELD,
				SelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(SelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_CREATE_TIME,
				SelectField.SelectFieldTypes.FIELD,
				SelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		TranslatedIRODSQuery query = mock(TranslatedIRODSQuery.class);
		when(query.getSelectFields()).thenReturn(selectFields);

		List<String> resultColumns = new ArrayList<String>();
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_ACTION_ID.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_COMMENT.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_CREATE_TIME.getName());

		IRODSQueryResultRow resultRow = IRODSQueryResultRow.instance(
				resultColumns, query);
		resultRow.getColumn(null);

	}

	@Test(expected = JargonException.class)
	public void testGetByColumnBlankName() throws Exception {

		List<SelectField> selectFields = new ArrayList<SelectField>();
		selectFields.add(SelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_ACTION_ID,
				SelectField.SelectFieldTypes.FIELD,
				SelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(SelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_COMMENT,
				SelectField.SelectFieldTypes.FIELD,
				SelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(SelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_CREATE_TIME,
				SelectField.SelectFieldTypes.FIELD,
				SelectField.SelectFieldSource.DEFINED_QUERY_FIELD));

		TranslatedIRODSQuery query = mock(TranslatedIRODSQuery.class);
		when(query.getSelectFields()).thenReturn(selectFields);

		List<String> resultColumns = new ArrayList<String>();
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_ACTION_ID.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_COMMENT.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_CREATE_TIME.getName());

		IRODSQueryResultRow resultRow = IRODSQueryResultRow.instance(
				resultColumns, query);
		resultRow.getColumn("");

	}

	@Test(expected = JargonException.class)
	public void testGetByColumnBogusName() throws Exception {

		List<SelectField> selectFields = new ArrayList<SelectField>();
		selectFields.add(SelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_ACTION_ID,
				SelectField.SelectFieldTypes.FIELD,
				SelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(SelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_COMMENT,
				SelectField.SelectFieldTypes.FIELD,
				SelectField.SelectFieldSource.DEFINED_QUERY_FIELD));
		selectFields.add(SelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_CREATE_TIME,
				SelectField.SelectFieldTypes.FIELD,
				SelectField.SelectFieldSource.DEFINED_QUERY_FIELD));

		TranslatedIRODSQuery query = mock(TranslatedIRODSQuery.class);
		when(query.getSelectFields()).thenReturn(selectFields);

		List<String> resultColumns = new ArrayList<String>();
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_ACTION_ID.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_COMMENT.getName());
		resultColumns.add(RodsGenQueryEnum.COL_AUDIT_CREATE_TIME.getName());

		IRODSQueryResultRow resultRow = IRODSQueryResultRow.instance(
				resultColumns, query);
		resultRow.getColumn("bogus");

	}

}
