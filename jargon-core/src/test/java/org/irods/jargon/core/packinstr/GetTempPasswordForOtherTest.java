package org.irods.jargon.core.packinstr;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class GetTempPasswordForOtherTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testGetTagValue() throws Exception {
		String testUser = "testUser";
		GetTempPasswordForOther instance = GetTempPasswordForOther
				.instance(testUser);

		StringBuilder sb = new StringBuilder();
		sb.append("<getTempPasswordForOtherInp_PI><targetUser>testUser</targetUser>\n");
		sb.append("<unused>null</unused>\n");
		sb.append("</getTempPasswordForOtherInp_PI>\n");

		String tagValue = instance.getParsedTags();
		TestCase.assertNotNull("no tag value returned", tagValue);
		TestCase.assertEquals("did not get expected tags", sb.toString(),
				tagValue);

	}

	@Test
	public final void testInstance() {
		String testUser = "testUser";
		GetTempPasswordForOther instance = GetTempPasswordForOther
				.instance(testUser);
		Assert.assertNotNull("null instance returned", instance);
		Assert.assertEquals("wrong API number",
				GetTempPasswordForOther.GET_TEMP_PASSWORD_FOR_OTHER_API_NBR,
				instance.getApiNumber());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceNullUser() {
		GetTempPasswordForOther.instance(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceBlankUser() {
		GetTempPasswordForOther.instance("");
	}

}
