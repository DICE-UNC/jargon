package org.irods.jargon.core.packinstr;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataObjInpForUnmountTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testInstanceForUnmount() {
		DataObjInpForUnmount actual = DataObjInpForUnmount.instanceForUnmount(
				"source", "");
		Assert.assertEquals("wrong api number", DataObjInpForMcoll.MCOLL_AN,
				actual.getApiNumber());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForUnmountNullColl() {
		DataObjInpForUnmount.instanceForUnmount(null, "resc");
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForUnmountBlankColl() {
		DataObjInpForUnmount.instanceForUnmount("", "resc");
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForUnmountNullResc() {
		DataObjInpForUnmount.instanceForUnmount("source", null);
	}

}
