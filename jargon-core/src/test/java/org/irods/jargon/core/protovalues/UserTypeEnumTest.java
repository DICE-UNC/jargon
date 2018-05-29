package org.irods.jargon.core.protovalues;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class UserTypeEnumTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testGetUserTypeList() throws Exception {
		List<String> userTypes = UserTypeEnum.getUserTypeList();
		Assert.assertTrue("no user types returned", userTypes.size() > 0);
	}

}
