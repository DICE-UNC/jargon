package org.irods.jargon.core.query;

import junit.framework.Assert;

import org.irods.jargon.core.exception.JargonException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class GenQuerySelectFieldTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testInstanceRodsGenQueryEnumSelectFieldTypesSelectFieldSource()
			throws Exception {
		GenQuerySelectField selectField = GenQuerySelectField.instance(
				RodsGenQueryEnum.COL_AUDIT_ACTION_ID,
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD);
		Assert.assertNotNull("null instance", selectField);
	}

	@Test(expected = JargonException.class)
	public final void testInstanceNullRodsGenQueryEnum() throws Exception {
		GenQuerySelectField.instance(null,
				GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD);
	}

	@Test(expected = JargonException.class)
	public final void testInstanceNullSelectFieldType() throws Exception {
		GenQuerySelectField
				.instance(
						RodsGenQueryEnum.COL_AUDIT_ACTION_ID,
						null,
						GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD);
	}

	@Test(expected = JargonException.class)
	public final void testInstanceNullSelectFieldSource() throws Exception {
		GenQuerySelectField.instance(RodsGenQueryEnum.COL_AUDIT_ACTION_ID,
				GenQuerySelectField.SelectFieldTypes.FIELD, null);
	}

	@Test
	public final void testInstanceWithNameAndType() throws Exception {
		GenQuerySelectField selectField = GenQuerySelectField.instance("blah",
				"1234", GenQuerySelectField.SelectFieldTypes.FIELD,
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD);
		Assert.assertNotNull("null instance", selectField);
	}

}
