package org.irods.jargon.core.query;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

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
		List<GenQuerySelectField> selectFields = new ArrayList<GenQuerySelectField>();
		GenQuerySelectField field = GenQuerySelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_ACTION_ID.name(), String
						.valueOf(RodsGenQueryEnum.COL_AUDIT_ACTION_ID
								.getNumericValue()),
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD);
		selectFields.add(field);
		List<TranslatedGenQueryCondition> queryConditions = new ArrayList<TranslatedGenQueryCondition>();
		IRODSGenQuery query = IRODSGenQuery.instance("hello", 100);
		TranslatedIRODSGenQuery translatedQuery = TranslatedIRODSGenQuery
				.instance(selectFields, queryConditions, query);
		Assert.assertNotNull("translated query not created", translatedQuery);
	}

	@Test(expected = JargonException.class)
	public final void testInstanceNullSelects() throws Exception {
		List<TranslatedGenQueryCondition> queryConditions = new ArrayList<TranslatedGenQueryCondition>();
		IRODSGenQuery query = IRODSGenQuery.instance("hello", 100);
		TranslatedIRODSGenQuery.instance(null, queryConditions, query);
	}

	@Test(expected = JargonException.class)
	public final void testInstanceNullConditions() throws Exception {
		List<GenQuerySelectField> selectFields = new ArrayList<GenQuerySelectField>();
		GenQuerySelectField field = GenQuerySelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_ACTION_ID.name(), String
						.valueOf(RodsGenQueryEnum.COL_AUDIT_ACTION_ID
								.getNumericValue()),
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD);
		selectFields.add(field);
		IRODSGenQuery query = IRODSGenQuery.instance("hello", 100);
		TranslatedIRODSGenQuery.instance(selectFields, null, query);

	}
}
