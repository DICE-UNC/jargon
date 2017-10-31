package org.irods.jargon.core.pub.domain;

import org.junit.Assert;

import org.irods.jargon.core.exception.JargonException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class AvuDataTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testInstance() throws Exception {
		Assert.assertNotNull(AvuData.instance("x", "y", "z"));
	}

	@Test(expected = JargonException.class)
	public final void testInstanceNullAttrib() throws Exception {
		AvuData.instance(null, "y", "z");
	}

	@Test(expected = JargonException.class)
	public final void testInstanceBlankAttrib() throws Exception {
		AvuData.instance("", "y", "z");
	}

	@Test(expected = JargonException.class)
	public final void testInstanceNulValue() throws Exception {
		AvuData.instance("x", null, "z");
	}

	@Test(expected = JargonException.class)
	public final void testInstanceNullUnits() throws Exception {
		AvuData.instance("x", "y", null);
	}
}
