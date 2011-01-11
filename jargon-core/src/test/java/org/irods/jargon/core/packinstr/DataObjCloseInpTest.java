package org.irods.jargon.core.packinstr;

import junit.framework.Assert;

import org.irods.jargon.core.exception.JargonException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataObjCloseInpTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testInstance() throws Exception {
		DataObjCloseInp dataObjCloseInp = DataObjCloseInp.instance(2, 0L);
		Assert.assertNotNull("null PI returned", dataObjCloseInp);
	}

	@Test(expected = JargonException.class)
	public final void testInstanceNoFD() throws Exception {
		DataObjCloseInp.instance(0, 0L);
	}

	@Test(expected = JargonException.class)
	public final void testInstanceNegBytesWritten() throws Exception {
		DataObjCloseInp.instance(2, -1L);
	}

	@Test
	public final void testGetParsedTags() throws Exception {
		DataObjCloseInp dataObjCloseInp = DataObjCloseInp.instance(2, 0L);
		String tags = dataObjCloseInp.getParsedTags();
		StringBuilder sb = new StringBuilder();
		sb.append("<dataObjCloseInp_PI><l1descInx>2</l1descInx>\n");
		sb.append("<bytesWritten>0</bytesWritten>\n");
		sb.append("</dataObjCloseInp_PI>\n");
		String expected = sb.toString();
		Assert.assertEquals("did not get expected tags", expected, tags);
	}

	@Test
	public final void testGetFileDescriptor() throws Exception {
		DataObjCloseInp dataObjCloseInp = DataObjCloseInp.instance(2, 0L);
		Assert.assertEquals("should have gotten fd", 2,
				dataObjCloseInp.getFileDescriptor());
	}

	@Test
	public final void testGetBytesWritten() throws Exception {
		DataObjCloseInp dataObjCloseInp = DataObjCloseInp.instance(2, 0L);
		Assert.assertEquals("should have gotten bytes written", 0L,
				dataObjCloseInp.getBytesWritten());
	}

}
