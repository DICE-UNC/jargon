package org.irods.jargon.core.packinstr;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataObjInpForMcollTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testInstanceForSoftLinkMount() {
		DataObjInpForMcoll actual = DataObjInpForMcoll
				.instanceForSoftLinkMount("source", "target", "resc");
		Assert.assertEquals("wrong api number", DataObjInpForMcoll.MCOLL_AN,
				actual.getApiNumber());
	}
	
	@Test
	public final void testInstanceForMSSOMount() {
		DataObjInpForMcoll actual = DataObjInpForMcoll
				.instanceForMSSOMount("source", "target", "resc");
		Assert.assertEquals("wrong api number", DataObjInpForMcoll.MCOLL_AN,
				actual.getApiNumber());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForSoftLinkMountNullSource() {
		DataObjInpForMcoll.instanceForSoftLinkMount(null, "target", "resc");
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForSoftLinkMountBlankSource() {
		DataObjInpForMcoll.instanceForSoftLinkMount("", "target", "resc");
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForSoftLinkMountNullTarget() {
		DataObjInpForMcoll.instanceForSoftLinkMount("source", null, "resc");
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForSoftLinkMountBlankTarget() {
		DataObjInpForMcoll.instanceForSoftLinkMount("source", "", "resc");
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForSoftLinkMountNullResc() {
		DataObjInpForMcoll.instanceForSoftLinkMount("source", "target", null);
	}

	@Test
	public final void testInstanceForSoftLinkMountBlankResc() {
		DataObjInpForMcoll.instanceForSoftLinkMount("source", "target", "");
	}

}
