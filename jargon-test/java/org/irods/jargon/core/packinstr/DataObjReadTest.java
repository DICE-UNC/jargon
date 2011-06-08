package org.irods.jargon.core.packinstr;

import junit.framework.Assert;

import org.irods.jargon.core.exception.JargonException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataObjReadTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testGetParsedTags() throws Exception {
		DataObjRead dataObjRead = DataObjRead.instance(3, 1);
		String actualTags = dataObjRead.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<dataObjReadInp_PI><l1descInx>3</l1descInx>\n");
		sb.append("<len>1</len>\n");
		sb.append("</dataObjReadInp_PI>\n");

		Assert.assertEquals("invalid packing instruction", sb.toString(),
				actualTags);
	}

	@Test
	public final void testInstance() throws Exception {
		DataObjRead dataObjRead = DataObjRead.instance(3, 100);
		Assert.assertNotNull("null object returned from initializer",
				dataObjRead);
	}

	@Test(expected = JargonException.class)
	public final void testInstanceZeroFD() throws Exception {
		DataObjRead.instance(0, 100);
	}

	@Test(expected = JargonException.class)
	public final void testInstanceNegLength() throws Exception {
		DataObjRead.instance(1, -100);
	}

}
