package org.irods.jargon.core.utils;

import junit.framework.Assert;

import org.junit.Test;

public class RandomUtilsTest {

	@Test
	public void testGenerateRandomBytesOfLength() {
		int len = 100;
		byte[] actual = RandomUtils.generateRandomBytesOfLength(len);
		Assert.assertEquals(len, actual.length);
	}

	@Test
	public void testGenerateRandomCharsOfLength() {
		int len = 256;
		char[] actual = RandomUtils.generateRandomChars(len);
		Assert.assertNotNull(actual);
		Assert.assertEquals(len, actual.length);

	}

}
