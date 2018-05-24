package org.irods.jargon.core.packinstr;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

public class GetTempPasswordInTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testInstance() throws Exception {
		GetTempPasswordIn instance = GetTempPasswordIn.instance();
		Assert.assertNotNull("null instance returned", instance);
		Assert.assertEquals("wrong API number", GetTempPasswordIn.GET_TEMP_PASSWORD_API_NBR, instance.getApiNumber());
	}

}
