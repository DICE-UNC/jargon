package org.irods.jargon.core.protovalues;

import org.junit.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AuditActionEnumTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testValueOf() {
		AuditActionEnum actual = AuditActionEnum
				.valueOf(AuditActionEnum.ACCESS_GRANTED.getAuditCode());
		Assert.assertTrue(actual.getAuditCode() == AuditActionEnum.ACCESS_GRANTED
				.getAuditCode());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValueOfInvalid() {
		AuditActionEnum.valueOf(1);

	}

}
