/**
 * 
 */
package org.irods.jargon.core.packinstr;

import junit.framework.Assert;

import org.irods.jargon.core.exception.JargonException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DataObjWriteInpTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.packinstr.DataObjWriteInp#instance(int, long)}
	 * .
	 */
	@Test
	public final void testInstance() throws Exception {
		DataObjWriteInp dataObjWriteInp = DataObjWriteInp.instance(1, 100L);
		Assert.assertNotNull(dataObjWriteInp);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.packinstr.DataObjWriteInp#instance(int, long)}
	 * .
	 */
	@Test(expected = JargonException.class)
	public final void testInstanceBadFileDescriptor() throws Exception {
		DataObjWriteInp.instance(-1, 100L);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.packinstr.DataObjWriteInp#instance(int, long)}
	 * .
	 */
	@Test(expected = JargonException.class)
	public final void testInstanceBadLength() throws Exception {
		DataObjWriteInp.instance(1, -100L);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.packinstr.DataObjWriteInp#getParsedTags()}.
	 */
	@Test
	public final void testGetParsedTags() throws Exception {
		DataObjWriteInp dataObjWriteInp = DataObjWriteInp.instance(1, 100L);
		String tagVal = dataObjWriteInp.getParsedTags();
		StringBuilder sb = new StringBuilder();
		sb.append("<dataObjWriteInp_PI><dataObjInx>1</dataObjInx>\n");
		sb.append("<len>100</len>\n");
		sb.append("</dataObjWriteInp_PI>\n");
		Assert.assertEquals("invalid xml generated", sb.toString(), tagVal);
	}

}
