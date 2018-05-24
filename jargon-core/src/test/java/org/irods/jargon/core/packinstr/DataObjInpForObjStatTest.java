package org.irods.jargon.core.packinstr;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

public class DataObjInpForObjStatTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testInstanceForObjStat() throws Exception {
		DataObjInpForObjStat dataObjInp = DataObjInpForObjStat.instance("/a/file/path");
		Assert.assertNotNull("got a null dataObjInp", dataObjInp);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForObjStatNullPath() throws Exception {
		DataObjInpForObjStat.instance(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForObjStatBlankPath() throws Exception {
		DataObjInpForObjStat.instance("");
	}

	@Test
	public final void testGetParsedTags() throws Exception {

		DataObjInpForObjStat dataObjInp = DataObjInpForObjStat.instance("/a/file/path");

		StringBuilder sb = new StringBuilder();
		sb.append("<DataObjInp_PI><objPath>/a/file/path</objPath>\n");
		sb.append("<createMode>0</createMode>\n");
		sb.append("<openFlags>0</openFlags>\n");
		sb.append("<offset>0</offset>\n");
		sb.append("<dataSize>0</dataSize>\n");
		sb.append("<numThreads>0</numThreads>\n");
		sb.append("<oprType>0</oprType>\n");
		sb.append("<KeyValPair_PI><ssLen>0</ssLen>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("</DataObjInp_PI>\n");
		Assert.assertEquals("did not get expected packing instruction", sb.toString(), dataObjInp.getParsedTags());
	}
}
