package org.irods.jargon.core.security;

import junit.framework.Assert;
import junit.framework.TestCase;

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

	@Test
	public void testDeriveHexSubsetOfChallenge() throws Exception {
		String challenge = "uejIyZS2C5h33yMdZsN8bptECJnymAhUmdqsA40/LkMfztiLjrILv+c3xJK9SVJXX7KKAX84Y9c8FtSwuMbR2A==";
		String expected = "b9e8c8c994b60b9877df231d66c37c6e";

		String actual = IRODSPasswordUtilities
				.deriveHexSubsetOfChallenge(challenge);
		TestCase.assertEquals("did not correctly derive challenge value",
				expected, actual);

	}

}
