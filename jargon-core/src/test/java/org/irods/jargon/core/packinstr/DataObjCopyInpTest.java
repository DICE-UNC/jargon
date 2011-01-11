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
public class DataObjCopyInpTest {

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
	 * {@link org.irods.jargon.core.packinstr.DataObjCopyInp#instance(java.lang.String, java.lang.String)}
	 * .
	 */
	@Test
	public final void testInstanceFromFileToFile() throws Exception {
		DataObjCopyInp copy = DataObjCopyInp.instance("fromFile", "toFile",
				DataObjInp.RENAME_DIRECTORY_OPERATION_TYPE);
		Assert.assertNotNull(copy);
	}

	@Test(expected = JargonException.class)
	public final void testInstanceNullFrom() throws Exception {
		DataObjCopyInp.instance(null, "to", 11);
	}

	@Test(expected = JargonException.class)
	public final void testInstanceNullTo() throws Exception {
		DataObjCopyInp.instance("from", null, 12);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.packinstr.DataObjCopyInp#getParsedTags()}.
	 */
	@Test
	public final void testGetParsedTags() throws Exception {
		DataObjCopyInp copy = DataObjCopyInp.instance("fromFile", "toFile", 11);
		Assert.assertTrue("got a blank xml protocol", copy.getParsedTags()
				.length() > 0);
	}

	@Test
	public final void testGetParsedTagsCheckXML() throws Exception {
		DataObjCopyInp copy = DataObjCopyInp.instance("fromFile", "toFile",
				DataObjInp.RENAME_DIRECTORY_OPERATION_TYPE);

		StringBuilder b = new StringBuilder();
		b.append("<DataObjCopyInp_PI><DataObjInp_PI><objPath>fromFile</objPath>\n");
		b.append("<createMode>0</createMode>\n");
		b.append("<openFlags>0</openFlags>\n");
		b.append("<offset>0</offset>\n");
		b.append("<dataSize>0</dataSize>\n");
		b.append("<numThreads>0</numThreads>\n");
		b.append("<oprType>12</oprType>\n");
		b.append("<KeyValPair_PI><ssLen>0</ssLen>\n");
		b.append("</KeyValPair_PI>\n");
		b.append("</DataObjInp_PI>\n");
		b.append("<DataObjInp_PI><objPath>toFile</objPath>\n");
		b.append("<createMode>0</createMode>\n");
		b.append("<openFlags>0</openFlags>\n");
		b.append("<offset>0</offset>\n");
		b.append("<dataSize>0</dataSize>\n");
		b.append("<numThreads>0</numThreads>\n");
		b.append("<oprType>12</oprType>\n");
		b.append("<KeyValPair_PI><ssLen>0</ssLen>\n");
		b.append("</KeyValPair_PI>\n");
		b.append("</DataObjInp_PI>\n");
		b.append("</DataObjCopyInp_PI>\n");
		String expected = b.toString();
		Assert.assertEquals("unexpected xml protocol values", expected,
				copy.getParsedTags());

	}

	@Test
	public final void testGetParsedTagsForRenameFile() throws Exception {
		String testFileName = "/sourceFile/testRenameOriginal.txt";
		String testNewFileName = "/destFile/testRenameNew.txt";

		DataObjCopyInp dataObjCopyInp = DataObjCopyInp.instanceForRenameFile(
				testFileName, testNewFileName);

		StringBuilder sb = new StringBuilder();
		sb.append("<DataObjCopyInp_PI><DataObjInp_PI><objPath>/sourceFile/testRenameOriginal.txt</objPath>\n");
		sb.append("<createMode>0</createMode>\n");
		sb.append("<openFlags>0</openFlags>\n");
		sb.append("<offset>0</offset>\n");
		sb.append("<dataSize>0</dataSize>\n");
		sb.append("<numThreads>0</numThreads>\n");
		sb.append("<oprType>11</oprType>\n");
		sb.append("<KeyValPair_PI><ssLen>0</ssLen>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("</DataObjInp_PI>\n");
		sb.append("<DataObjInp_PI><objPath>/destFile/testRenameNew.txt</objPath>\n");
		sb.append("<createMode>0</createMode>\n");
		sb.append("<openFlags>0</openFlags>\n");
		sb.append("<offset>0</offset>\n");
		sb.append("<dataSize>0</dataSize>\n");
		sb.append("<numThreads>0</numThreads>\n");
		sb.append("<oprType>11</oprType>\n");
		sb.append("<KeyValPair_PI><ssLen>0</ssLen>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("</DataObjInp_PI>\n");
		sb.append("</DataObjCopyInp_PI>\n");

		Assert.assertEquals("did not get expected packing instruction",
				sb.toString(), dataObjCopyInp.getParsedTags());
		Assert.assertEquals("did not get expected API number",
				DataObjCopyInp.RENAME_FILE_API_NBR,
				dataObjCopyInp.getApiNumber());

	}

	@Test
	public final void testGetParsedTagsForRenameCollection() throws Exception {
		String testFileName = "/source/sourceColl";
		String testNewFileName = "/dest/destColl";

		DataObjCopyInp dataObjCopyInp = DataObjCopyInp
				.instanceForRenameCollection(testFileName, testNewFileName);

		StringBuilder sb = new StringBuilder();
		sb.append("<DataObjCopyInp_PI><DataObjInp_PI><objPath>/source/sourceColl</objPath>\n");
		sb.append("<createMode>0</createMode>\n");
		sb.append("<openFlags>0</openFlags>\n");
		sb.append("<offset>0</offset>\n");
		sb.append("<dataSize>0</dataSize>\n");
		sb.append("<numThreads>0</numThreads>\n");
		sb.append("<oprType>12</oprType>\n");
		sb.append("<KeyValPair_PI><ssLen>0</ssLen>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("</DataObjInp_PI>\n");
		sb.append("<DataObjInp_PI><objPath>/dest/destColl</objPath>\n");
		sb.append("<createMode>0</createMode>\n");
		sb.append("<openFlags>0</openFlags>\n");
		sb.append("<offset>0</offset>\n");
		sb.append("<dataSize>0</dataSize>\n");
		sb.append("<numThreads>0</numThreads>\n");
		sb.append("<oprType>12</oprType>\n");
		sb.append("<KeyValPair_PI><ssLen>0</ssLen>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("</DataObjInp_PI>\n");
		sb.append("</DataObjCopyInp_PI>\n");

		Assert.assertEquals("did not get expected packing instruction",
				sb.toString(), dataObjCopyInp.getParsedTags());
		Assert.assertEquals("did not get expected API number",
				DataObjCopyInp.RENAME_FILE_API_NBR,
				dataObjCopyInp.getApiNumber());

	}

}
