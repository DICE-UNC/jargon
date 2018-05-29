package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.ProtocolFormException;
import org.irods.jargon.core.packinstr.DataObjInpForReg.ChecksumHandling;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataObjInpForRegTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * just test no errors in instance
	 */
	@Test
	public final void testInstance() throws Exception {
		DataObjInpForReg.instance("phys", "irods", "", "", false, false, ChecksumHandling.NONE, false, "");
	}

	/**
	 * null phys path
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceNullPhysPath() throws Exception {
		DataObjInpForReg.instance(null, "irods", "", "", false, false, ChecksumHandling.NONE, false, "");
	}

	/**
	 * spaces phys path
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceSpacePhysPath() throws Exception {
		DataObjInpForReg.instance("", "irods", "", "", false, false, ChecksumHandling.NONE, false, "");
	}

	/**
	 * null irods path
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceNullIrodsPath() throws Exception {
		DataObjInpForReg.instance("pys", null, "", "", false, false, ChecksumHandling.NONE, false, "");
	}

	/**
	 * spaces irods path
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceSpaceIrodsPath() throws Exception {
		DataObjInpForReg.instance("phys", "", "", "", false, false, ChecksumHandling.NONE, false, "");
	}

	/**
	 * null checksum handling
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceNullChecksumHandling() throws Exception {
		DataObjInpForReg.instance("pys", "irods", "", "", false, false, null, false, "");
	}

	/**
	 * null checksum
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceNullChecksum() throws Exception {
		DataObjInpForReg.instance("pys", "irods", "", "", false, false, ChecksumHandling.NONE, false, null);
	}

	/**
	 * verify checksum but checksum is blank
	 */
	@Test(expected = ProtocolFormException.class)
	public final void testInstanceChecksumMissingWhenVerify() throws Exception {
		DataObjInpForReg.instance("pys", "irods", "", "", false, false, ChecksumHandling.VERFIY_CHECKSUM, false, "");
	}

	/**
	 * verify checksum but checksum is blank
	 */
	@Test(expected = ProtocolFormException.class)
	public final void testInstanceChecksumWhenRecursive() throws Exception {
		DataObjInpForReg.instance("pys", "irods", "", "", false, true, ChecksumHandling.VERFIY_CHECKSUM, false, "xxx");
	}

	@Test
	public final void testGetTagValueWhenNoForce() throws Exception {
		DataObjInpForReg dataObjInput = DataObjInpForReg.instance("phys", "irods", "", "", false, false,
				ChecksumHandling.NONE, false, "");

		String tagValue = dataObjInput.getParsedTags();
		Assert.assertNotNull("null tags returned", tagValue);

		StringBuilder sb = new StringBuilder();
		sb.append("<DataObjInp_PI><objPath>irods</objPath>\n");
		sb.append("<createMode>0</createMode>\n");
		sb.append("<openFlags>0</openFlags>\n");
		sb.append("<offset>0</offset>\n");
		sb.append("<dataSize>0</dataSize>\n");
		sb.append("<numThreads>0</numThreads>\n");
		sb.append("<oprType>0</oprType>\n");
		sb.append("<KeyValPair_PI><ssLen>3</ssLen>\n");
		sb.append("<keyWord>dataType</keyWord>\n");
		sb.append("<keyWord>filePath</keyWord>\n");
		sb.append("<keyWord>destRescName</keyWord>\n");
		sb.append("<svalue>generic</svalue>\n");
		sb.append("<svalue>phys</svalue>\n");
		sb.append("<svalue></svalue>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("</DataObjInp_PI>");

		String expected = sb.toString().trim();
		Assert.assertEquals("invalid tag generated", expected, tagValue.trim());

	}

}
