package org.irods.jargon.core.security;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSPasswordUtilitiesTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testObfuscate() throws Exception {
		String password = "hello";
		String newPassword = "argybargy";
		String obfuscated = IRODSPasswordUtilities.obfuscateIRODSPassword(
				newPassword, password);
		Assert.assertEquals("o(yrwoSMzIqkTFzPV\"\"3V12(a00U*f+YRQ*N#MQJ",
				obfuscated);

	}

}
