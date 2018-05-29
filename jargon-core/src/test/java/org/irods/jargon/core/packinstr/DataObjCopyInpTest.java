/**
 *
 */
package org.irods.jargon.core.packinstr;

import org.junit.AfterClass;
import org.junit.Assert;
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

	@Test
	public final void testGetParsedTagsCheckXML() throws Exception {
		DataObjCopyInp copy = DataObjCopyInp.instanceForRenameCollection("fromFile", "toFile");

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
		Assert.assertEquals("unexpected xml protocol values", expected, copy.getParsedTags());

	}

	@Test
	public void testInstanceForCopy() throws Exception {
		String testFileName = "/sourceFile/testRenameOriginal.txt";
		String testNewFileName = "/destFile/testRenameNew.txt";
		String testResource = "resc";
		long testLength = 123;

		DataObjCopyInp dataObjCopyInp = DataObjCopyInp.instanceForCopy(testFileName, testNewFileName, testResource,
				testLength, false);

		Assert.assertEquals("invalid api number set", DataObjCopyInp.COPY_API_NBR, dataObjCopyInp.getApiNumber());
		Assert.assertEquals("invalid source path", testFileName, dataObjCopyInp.getFromFileAbsolutePath());
		Assert.assertEquals("invalid target path", testNewFileName, dataObjCopyInp.getToFileAbsolutePath());
		Assert.assertEquals("invalid resource", testResource, dataObjCopyInp.getResourceName());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceForCopyBlankSource() throws Exception {
		String testFileName = "";
		String testNewFileName = "/destFile/testRenameNew.txt";
		String testResource = "resc";
		long testLength = 123;

		DataObjCopyInp.instanceForCopy(testFileName, testNewFileName, testResource, testLength, false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceForCopyNullTarget() throws Exception {
		String testFileName = "testfile";
		String testNewFileName = null;
		String testResource = "resc";
		long testLength = 123;

		DataObjCopyInp.instanceForCopy(testFileName, testNewFileName, testResource, testLength, false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceForCopyBlankTarget() throws Exception {
		String testFileName = "target";
		String testNewFileName = "";
		String testResource = "resc";
		long testLength = 123;

		DataObjCopyInp.instanceForCopy(testFileName, testNewFileName, testResource, testLength, false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceForCopyNullSource() throws Exception {
		String testFileName = null;
		String testNewFileName = "/destFile/testRenameNew.txt";
		String testResource = "resc";
		long testLength = 123;

		DataObjCopyInp.instanceForCopy(testFileName, testNewFileName, testResource, testLength, false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceForCopyNullResource() throws Exception {
		String testFileName = "source";
		String testNewFileName = "/destFile/testRenameNew.txt";
		String testResource = null;
		long testLength = 123;

		DataObjCopyInp.instanceForCopy(testFileName, testNewFileName, testResource, testLength, false);
	}

	@Test
	public final void testGetParsedTagsForRenameFile() throws Exception {
		String testFileName = "/sourceFile/testRenameOriginal.txt";
		String testNewFileName = "/destFile/testRenameNew.txt";

		DataObjCopyInp dataObjCopyInp = DataObjCopyInp.instanceForRenameFile(testFileName, testNewFileName);

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

		Assert.assertEquals("did not get expected packing instruction", sb.toString(), dataObjCopyInp.getParsedTags());
		Assert.assertEquals("did not get expected API number", DataObjCopyInp.RENAME_FILE_API_NBR,
				dataObjCopyInp.getApiNumber());

	}

	@Test
	public final void testGetParsedTagsForRenameCollection() throws Exception {
		String testFileName = "/source/sourceColl";
		String testNewFileName = "/dest/destColl";

		DataObjCopyInp dataObjCopyInp = DataObjCopyInp.instanceForRenameCollection(testFileName, testNewFileName);

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

		Assert.assertEquals("did not get expected packing instruction", sb.toString(), dataObjCopyInp.getParsedTags());
		Assert.assertEquals("did not get expected API number", DataObjCopyInp.RENAME_FILE_API_NBR,
				dataObjCopyInp.getApiNumber());

	}

	@Test
	public final void testGetParsedTagsForCopyWithoutForce() throws Exception {

		String testFileName = "/sourceFile/testRenameOriginal.txt";
		String testNewFileName = "/destFile/testRenameNew.txt";
		String testResource = "resc";
		long testLength = 123;

		DataObjCopyInp dataObjCopyInp = DataObjCopyInp.instanceForCopy(testFileName, testNewFileName, testResource,
				testLength, false);

		StringBuilder b = new StringBuilder();
		b.append("<DataObjCopyInp_PI><DataObjInp_PI><objPath>/sourceFile/testRenameOriginal.txt</objPath>\n");
		b.append("<createMode>0</createMode>\n");
		b.append("<openFlags>0</openFlags>\n");
		b.append("<offset>0</offset>\n");
		b.append("<dataSize>123</dataSize>\n");
		b.append("<numThreads>0</numThreads>\n");
		b.append("<oprType>10</oprType>\n");
		b.append("<KeyValPair_PI><ssLen>0</ssLen>\n");
		b.append("</KeyValPair_PI>\n");
		b.append("</DataObjInp_PI>\n");
		b.append("<DataObjInp_PI><objPath>/destFile/testRenameNew.txt</objPath>\n");
		b.append("<createMode>0</createMode>\n");
		b.append("<openFlags>0</openFlags>\n");
		b.append("<offset>0</offset>\n");
		b.append("<dataSize>0</dataSize>\n");
		b.append("<numThreads>0</numThreads>\n");
		b.append("<oprType>9</oprType>\n");
		b.append("<KeyValPair_PI><ssLen>1</ssLen>\n");
		b.append("<keyWord>destRescName</keyWord>\n");
		b.append("<svalue>resc</svalue>\n");
		b.append("</KeyValPair_PI>\n");
		b.append("</DataObjInp_PI>\n");
		b.append("</DataObjCopyInp_PI>\n");

		String expected = b.toString();
		Assert.assertEquals("unexpected xml protocol values", expected, dataObjCopyInp.getParsedTags());

	}

}
