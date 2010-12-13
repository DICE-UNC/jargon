package org.irods.jargon.core.query;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.irods.jargon.core.exception.JargonException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TranslatedIRODSQueryTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testInstance() throws Exception {
		List<SelectField> selectFields = new ArrayList<SelectField>();
		SelectField field = SelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_ACTION_ID.name(), String
						.valueOf(RodsGenQueryEnum.COL_AUDIT_ACTION_ID
								.getNumericValue()),
				SelectField.SelectFieldTypes.FIELD,
				SelectField.SelectFieldSource.DEFINED_QUERY_FIELD);
		selectFields.add(field);
		List<TranslatedQueryCondition> queryConditions = new ArrayList<TranslatedQueryCondition>();
		IRODSQuery query = IRODSQuery.instance("hello", 100);
		TranslatedIRODSQuery translatedQuery = TranslatedIRODSQuery.instance(
				selectFields, queryConditions, query);
		TestCase.assertNotNull("translated query not created", translatedQuery);
	}

	@Test(expected = JargonException.class)
	public final void testInstanceNullSelects() throws Exception {
		List<TranslatedQueryCondition> queryConditions = new ArrayList<TranslatedQueryCondition>();
		IRODSQuery query = IRODSQuery.instance("hello", 100);
		TranslatedIRODSQuery.instance(null, queryConditions, query);
	}

	@Test(expected = JargonException.class)
	public final void testInstanceNullConditions() throws Exception {
		List<SelectField> selectFields = new ArrayList<SelectField>();
		SelectField field = SelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_ACTION_ID.name(), String
						.valueOf(RodsGenQueryEnum.COL_AUDIT_ACTION_ID
								.getNumericValue()),
				SelectField.SelectFieldTypes.FIELD,
				SelectField.SelectFieldSource.DEFINED_QUERY_FIELD);
		selectFields.add(field);
		IRODSQuery query = IRODSQuery.instance("hello", 100);
		TranslatedIRODSQuery.instance(selectFields, null, query);

	}
}
