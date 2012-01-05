package org.irods.jargon.core.packinstr;

import junit.framework.Assert;

import org.irods.jargon.core.exception.JargonException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataObjInpTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testInstanceForReplicateToResourceGroup()
			throws Exception {
		DataObjInp dataObjInp = DataObjInp.instanceForReplicateToResourceGroup(
				"file", "rg");
		Assert.assertNotNull("got a null dataObjInp", dataObjInp);

	}

	@Test(expected = JargonException.class)
	public final void testInstanceForReplicateToResourceGroupNullFile()
			throws Exception {
		DataObjInp.instanceForReplicateToResourceGroup(null, "rg");
	}

	@Test(expected = JargonException.class)
	public final void testInstanceForReplicateToResourceGroupBlankResourceGroup()
			throws Exception {
		DataObjInp.instanceForReplicateToResourceGroup("file", "");
	}

	@Test(expected = JargonException.class)
	public final void testInstanceForReplicateToResourceGroupBlankFile()
			throws Exception {
		DataObjInp.instanceForReplicateToResourceGroup("", "rg");
	}

	@Test(expected = JargonException.class)
	public final void testInstanceForReplicateToResourceGroupNullResourceGroup()
			throws Exception {
		DataObjInp.instanceForReplicateToResourceGroup("file", null);
	}

	@Test
	public final void testInstanceForOpen() throws Exception {
		DataObjInp dataObjInp = DataObjInp.instanceForOpen("/abspath",
				DataObjInp.OpenFlags.READ_WRITE);
		Assert.assertNotNull("data obj inp returned was null", dataObjInp);
	}

	@Test
	public final void testInstanceForPut() throws Exception {
		DataObjInp dataObjInp = DataObjInp.instanceForNormalPutStrategy(
				"/abspath", 100, "aresource", true, null, false);
		Assert.assertNotNull("data obj inp returned was null", dataObjInp);
		Assert.assertEquals("wrong API number assigned",
				DataObjInp.PUT_FILE_API_NBR, dataObjInp.getApiNumber());
	}

	@Test
	public final void testInstanceForGet() throws Exception {
		DataObjInp dataObjInp = DataObjInp.instanceForGet("/abspath", 0, null);
		Assert.assertNotNull("data obj inp returned was null", dataObjInp);
		Assert.assertEquals("wrong API number assigned",
				DataObjInp.GET_FILE_API_NBR, dataObjInp.getApiNumber());
	}

	@Test
	public final void testInstanceForChecksum() throws Exception {
		DataObjInp dataObjInp = DataObjInp
				.instanceForDataObjectChecksum("/abspath");
		Assert.assertNotNull("data obj inp returned was null", dataObjInp);
		Assert.assertEquals("wrong API number assigned",
				DataObjInp.CHECKSUM_API_NBR, dataObjInp.getApiNumber());
	}

	@Test(expected = JargonException.class)
	public final void testInstanceForChecksumBlank() throws Exception {
		DataObjInp.instanceForDataObjectChecksum("");
	}

	@Test(expected = JargonException.class)
	public final void testInstanceForDeleteNoForceBlank() throws Exception {
		DataObjInp.instanceForDeleteWithNoForce("");
	}

	@Test(expected = JargonException.class)
	public final void testInstanceForDeleteNoForceNull() throws Exception {
		DataObjInp.instanceForDeleteWithNoForce(null);
	}

	@Test(expected = JargonException.class)
	public final void testInstanceForChecksumNull() throws Exception {
		DataObjInp.instanceForDataObjectChecksum(null);
	}

	@Test
	public final void testGetParsedTagsForInitialCallToPut() throws Exception {
		DataObjInp dataObjInp = DataObjInp
				.instanceForInitialCallToPut(
						"/test1/home/test1/test-scratch/IrodsCommandsPutTest/testPutOverwriteFileNotInIRODS.txt",
						716800000, "test1-resc", false, null, false);

		StringBuilder sb = new StringBuilder();
		sb.append("<DataObjInp_PI><objPath>/test1/home/test1/test-scratch/IrodsCommandsPutTest/testPutOverwriteFileNotInIRODS.txt</objPath>\n");
		sb.append("<createMode>33188</createMode>\n");
		sb.append("<openFlags>2</openFlags>\n");
		sb.append("<offset>0</offset>\n");
		sb.append("<dataSize>716800000</dataSize>\n");
		sb.append("<numThreads>0</numThreads>\n");
		sb.append("<oprType>1</oprType>\n");
		sb.append("<KeyValPair_PI><ssLen>1</ssLen>\n");
		sb.append("<keyWord>destRescName</keyWord>\n");
		sb.append("<svalue>test1-resc</svalue>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("</DataObjInp_PI>\n");
		Assert.assertEquals("did not get expected packing instruction",
				sb.toString(), dataObjInp.getParsedTags());

	}

	@Test
	public final void testGetParsedTagsForCallToPutNormalMode()
			throws Exception {
		DataObjInp dataObjInp = DataObjInp
				.instanceForNormalPutStrategy(
						"/test1/home/test1/test-scratch/IrodsCommandsPutTest/testPutOverwriteFileNotInIRODS.txt",
						1, "test1-resc", true, null, false);

		StringBuilder sb = new StringBuilder();
		sb.append("<DataObjInp_PI><objPath>/test1/home/test1/test-scratch/IrodsCommandsPutTest/testPutOverwriteFileNotInIRODS.txt</objPath>\n");
		sb.append("<createMode>33188</createMode>\n");
		sb.append("<openFlags>2</openFlags>\n");
		sb.append("<offset>0</offset>\n");
		sb.append("<dataSize>1</dataSize>\n");
		sb.append("<numThreads>0</numThreads>\n");
		sb.append("<oprType>1</oprType>\n");
		sb.append("<KeyValPair_PI><ssLen>4</ssLen>\n");
		sb.append("<keyWord>dataType</keyWord>\n");
		sb.append("<keyWord>dataIncluded</keyWord>\n");
		sb.append("<keyWord>forceFlag</keyWord>\n");
		sb.append("<keyWord>destRescName</keyWord>\n");
		sb.append("<svalue>generic</svalue>\n");
		sb.append("<svalue></svalue>\n");
		sb.append("<svalue></svalue>\n");
		sb.append("<svalue>test1-resc</svalue>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("</DataObjInp_PI>\n");
		Assert.assertEquals("did not get expected packing instruction",
				sb.toString(), dataObjInp.getParsedTags());
	}

	@Test
	public final void testGetParsedTagsForCallToPutNormalModeWithExec()
			throws Exception {
		DataObjInp dataObjInp = DataObjInp
				.instanceForNormalPutStrategy(
						"/test1/home/test1/test-scratch/IrodsCommandsPutTest/testPutOverwriteFileNotInIRODS.txt",
						1, "test1-resc", true, null, true);

		StringBuilder sb = new StringBuilder();
		sb.append("<DataObjInp_PI><objPath>/test1/home/test1/test-scratch/IrodsCommandsPutTest/testPutOverwriteFileNotInIRODS.txt</objPath>\n");
		sb.append("<createMode>33261</createMode>\n");
		sb.append("<openFlags>2</openFlags>\n");
		sb.append("<offset>0</offset>\n");
		sb.append("<dataSize>1</dataSize>\n");
		sb.append("<numThreads>0</numThreads>\n");
		sb.append("<oprType>1</oprType>\n");
		sb.append("<KeyValPair_PI><ssLen>4</ssLen>\n");
		sb.append("<keyWord>dataType</keyWord>\n");
		sb.append("<keyWord>dataIncluded</keyWord>\n");
		sb.append("<keyWord>forceFlag</keyWord>\n");
		sb.append("<keyWord>destRescName</keyWord>\n");
		sb.append("<svalue>generic</svalue>\n");
		sb.append("<svalue></svalue>\n");
		sb.append("<svalue></svalue>\n");
		sb.append("<svalue>test1-resc</svalue>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("</DataObjInp_PI>\n");
		Assert.assertEquals("did not get expected packing instruction",
				sb.toString(), dataObjInp.getParsedTags());
	}

	@Test
	public final void testGetParsedTagsForGet() throws Exception {
		DataObjInp dataObjInp = DataObjInp
				.instanceForGet(
						"/test1/home/test1/test-scratch/IrodsCommandsGetTest/testGet.txt",
						0, null);

		StringBuilder sb = new StringBuilder();
		sb.append("<DataObjInp_PI><objPath>/test1/home/test1/test-scratch/IrodsCommandsGetTest/testGet.txt</objPath>\n");
		sb.append("<createMode>0</createMode>\n");
		sb.append("<openFlags>0</openFlags>\n");
		sb.append("<offset>0</offset>\n");
		sb.append("<dataSize>0</dataSize>\n");
		sb.append("<numThreads>0</numThreads>\n");
		sb.append("<oprType>2</oprType>\n");
		sb.append("<KeyValPair_PI><ssLen>0</ssLen>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("</DataObjInp_PI>\n");

		Assert.assertEquals("did not get expected packing instruction",
				sb.toString(), dataObjInp.getParsedTags());
		Assert.assertEquals("did not get expected API number",
				DataObjInp.GET_FILE_API_NBR, dataObjInp.getApiNumber());

	}

	@Test
	public final void testGetParsedTagsForDeleteNoForce() throws Exception {
		DataObjInp dataObjInp = DataObjInp
				.instanceForDeleteWithNoForce("/test1/home/test1/test-scratch/IrodsCommandsDeleteTest/testDeleteOneFileNoForce.txt");

		StringBuilder sb = new StringBuilder();
		sb.append("<DataObjInp_PI><objPath>/test1/home/test1/test-scratch/IrodsCommandsDeleteTest/testDeleteOneFileNoForce.txt</objPath>\n");
		sb.append("<createMode>0</createMode>\n");
		sb.append("<openFlags>0</openFlags>\n");
		sb.append("<offset>0</offset>\n");
		sb.append("<dataSize>0</dataSize>\n");
		sb.append("<numThreads>0</numThreads>\n");
		sb.append("<oprType>0</oprType>\n");
		sb.append("<KeyValPair_PI><ssLen>0</ssLen>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("</DataObjInp_PI>\n");

		Assert.assertEquals("did not get expected packing instruction",
				sb.toString(), dataObjInp.getParsedTags());
		Assert.assertEquals("did not get expected API number",
				DataObjInp.DELETE_FILE_API_NBR, dataObjInp.getApiNumber());

	}

	@Test
	public final void testGetParsedTagsForChecksum() throws Exception {
		DataObjInp dataObjInp = DataObjInp
				.instanceForDataObjectChecksum("/test1/home/test1/test-scratch/IRODSCommandsMiscTest/testChecksum.txt");

		StringBuilder sb = new StringBuilder();
		sb.append("<DataObjInp_PI><objPath>/test1/home/test1/test-scratch/IRODSCommandsMiscTest/testChecksum.txt</objPath>\n");
		sb.append("<createMode>0</createMode>\n");
		sb.append("<openFlags>0</openFlags>\n");
		sb.append("<offset>0</offset>\n");
		sb.append("<dataSize>0</dataSize>\n");
		sb.append("<numThreads>0</numThreads>\n");
		sb.append("<oprType>0</oprType>\n");
		sb.append("<KeyValPair_PI><ssLen>0</ssLen>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("</DataObjInp_PI>\n");

		Assert.assertEquals("did not get expected packing instruction",
				sb.toString(), dataObjInp.getParsedTags());
		Assert.assertEquals("did not get expected API number",
				DataObjInp.CHECKSUM_API_NBR, dataObjInp.getApiNumber());
	}

	@Test
	public final void testGetParsedTagsForGetWithResource() throws Exception {
		DataObjInp dataObjInp = DataObjInp
				.instanceForGetSpecifyingResource(
						"/test1/home/test1/test-scratch/IrodsCommandsGetTest/testGetSpecifyingResource.txt",
						"test1-resc", null);

		StringBuilder sb = new StringBuilder();
		sb.append("<DataObjInp_PI><objPath>/test1/home/test1/test-scratch/IrodsCommandsGetTest/testGetSpecifyingResource.txt</objPath>\n");
		sb.append("<createMode>0</createMode>\n");
		sb.append("<openFlags>0</openFlags>\n");
		sb.append("<offset>0</offset>\n");
		sb.append("<dataSize>0</dataSize>\n");
		sb.append("<numThreads>0</numThreads>\n");
		sb.append("<oprType>2</oprType>\n");
		sb.append("<KeyValPair_PI><ssLen>1</ssLen>\n");
		sb.append("<keyWord>rescName</keyWord>\n");
		sb.append("<svalue>test1-resc</svalue>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("</DataObjInp_PI>\n");

		Assert.assertEquals("did not get expected packing instruction",
				sb.toString(), dataObjInp.getParsedTags());
		Assert.assertEquals("did not get expected API number",
				DataObjInp.GET_FILE_API_NBR, dataObjInp.getApiNumber());

	}

	@Test
	public final void testGetParsedTagsForReplicate() throws Exception {
		DataObjInp dataObjInp = DataObjInp
				.instanceForReplicate(
						"/test1/home/test1/test-scratch/IrodsFileCommandsTest/testReplicate1.txt",
						"test1-resc2");

		StringBuilder sb = new StringBuilder();
		sb.append("<DataObjInp_PI><objPath>/test1/home/test1/test-scratch/IrodsFileCommandsTest/testReplicate1.txt</objPath>\n");
		sb.append("<createMode>0</createMode>\n");
		sb.append("<openFlags>0</openFlags>\n");
		sb.append("<offset>0</offset>\n");
		sb.append("<dataSize>0</dataSize>\n");
		sb.append("<numThreads>0</numThreads>\n");
		sb.append("<oprType>6</oprType>\n");
		sb.append("<KeyValPair_PI><ssLen>1</ssLen>\n");
		sb.append("<keyWord>destRescName</keyWord>\n");
		sb.append("<svalue>test1-resc2</svalue>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("</DataObjInp_PI>\n");

		Assert.assertEquals("did not get expected packing instruction",
				sb.toString(), dataObjInp.getParsedTags());
		Assert.assertEquals("did not get expected API number",
				DataObjInp.REPLICATE_API_NBR, dataObjInp.getApiNumber());

	}

	@Test
	public final void testGetParsedTagsForReplicateToResourceGroup()
			throws Exception {
		DataObjInp dataObjInp = DataObjInp
				.instanceForReplicateToResourceGroup(
						"/test1/home/test1/test-scratch/IrodsFileCommandsTest/testReplicate1.txt",
						"test1-resc2");

		StringBuilder sb = new StringBuilder();
		sb.append("<DataObjInp_PI><objPath>/test1/home/test1/test-scratch/IrodsFileCommandsTest/testReplicate1.txt</objPath>\n");
		sb.append("<createMode>0</createMode>\n");
		sb.append("<openFlags>0</openFlags>\n");
		sb.append("<offset>0</offset>\n");
		sb.append("<dataSize>0</dataSize>\n");
		sb.append("<numThreads>0</numThreads>\n");
		sb.append("<oprType>6</oprType>\n");
		sb.append("<KeyValPair_PI><ssLen>2</ssLen>\n");
		sb.append("<keyWord>all</keyWord>\n");
		sb.append("<keyWord>destRescName</keyWord>\n");
		sb.append("<svalue></svalue>\n");
		sb.append("<svalue>test1-resc2</svalue>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("</DataObjInp_PI>\n");

		Assert.assertEquals("did not get expected packing instruction",
				sb.toString(), dataObjInp.getParsedTags());
		Assert.assertEquals("did not get expected API number",
				DataObjInp.REPLICATE_API_NBR, dataObjInp.getApiNumber());

	}

	@Test(expected = JargonException.class)
	public final void instanceForReplicateBlankFile() throws Exception {
		DataObjInp.instanceForReplicate("", "test1-resc2");
	}

	@Test(expected = JargonException.class)
	public final void instanceForReplicateNullFile() throws Exception {
		DataObjInp.instanceForReplicate(null, "test1-resc2");
	}

	@Test(expected = JargonException.class)
	public final void instanceForReplicateBlankResc() throws Exception {
		DataObjInp.instanceForReplicate("/a/file/path", "");
	}

	@Test(expected = JargonException.class)
	public final void instanceForReplicateNullResc() throws Exception {
		DataObjInp.instanceForReplicate("/a/file/path", null);
	}

	@Test
	public final void testGetParsedTagsForGetHostForPut() throws Exception {
		DataObjInp dataObjInp = DataObjInp
				.instanceForGetHostForPut(
						"/test1/home/test1/test-scratch/IrodsCommandsPutTest/testPutOverwriteFileNotInIRODS.txt",
						"test1-resc");

		StringBuilder sb = new StringBuilder();
		sb.append("<DataObjInp_PI><objPath>/test1/home/test1/test-scratch/IrodsCommandsPutTest/testPutOverwriteFileNotInIRODS.txt</objPath>\n");
		sb.append("<createMode>0</createMode>\n");
		sb.append("<openFlags>0</openFlags>\n");
		sb.append("<offset>0</offset>\n");
		sb.append("<dataSize>0</dataSize>\n");
		sb.append("<numThreads>0</numThreads>\n");
		sb.append("<oprType>1</oprType>\n");
		sb.append("<KeyValPair_PI><ssLen>1</ssLen>\n");
		sb.append("<keyWord>rescName</keyWord>\n");
		sb.append("<svalue>test1-resc</svalue>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("</DataObjInp_PI>\n");

		Assert.assertEquals("did not get expected packing instruction",
				sb.toString(), dataObjInp.getParsedTags());
		Assert.assertEquals("did not get expected API number",
				DataObjInp.GET_HOST_FOR_PUT_API_NBR, dataObjInp.getApiNumber());
	}

	@Test(expected = JargonException.class)
	public final void testGetHostForPutBlankSource() throws Exception {
		DataObjInp.instanceForGetHostForPut("", "test1-resc");
	}

	@Test(expected = JargonException.class)
	public final void testGetHostForPutNullSource() throws Exception {
		DataObjInp.instanceForGetHostForPut(null, "test1-resc");
	}

	@Test(expected = JargonException.class)
	public final void testGetHostForPutNullResource() throws Exception {
		DataObjInp.instanceForGetHostForPut("hello", null);
	}

	@Test
	public final void testGetHostForPutBlankResource() throws Exception {
		DataObjInp.instanceForGetHostForPut("hello", "");
	}

	// implement this get test, then test via data object, include resource,
	// then parallell get/put

	@Test
	public final void testGetParsedTagsForGetHostForGet() throws Exception {
		DataObjInp dataObjInp = DataObjInp
				.instanceForGetHostForGet(
						"/test1/home/test1/test-scratch/IrodsCommandsGetTest/testGet.txt",
						"");

		StringBuilder sb = new StringBuilder();
		sb.append("<DataObjInp_PI><objPath>/test1/home/test1/test-scratch/IrodsCommandsGetTest/testGet.txt</objPath>\n");
		sb.append("<createMode>0</createMode>\n");
		sb.append("<openFlags>0</openFlags>\n");
		sb.append("<offset>0</offset>\n");
		sb.append("<dataSize>0</dataSize>\n");
		sb.append("<numThreads>0</numThreads>\n");
		sb.append("<oprType>2</oprType>\n");
		sb.append("<KeyValPair_PI><ssLen>0</ssLen>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("</DataObjInp_PI>\n");

		Assert.assertEquals("did not get expected packing instruction",
				sb.toString(), dataObjInp.getParsedTags());
		Assert.assertEquals("did not get expected API number",
				DataObjInp.GET_HOST_FOR_GET_API_NBR, dataObjInp.getApiNumber());

	}

	@Test(expected = JargonException.class)
	public final void testGetParsedTagsForGetHostForGetBlankSource()
			throws Exception {
		DataObjInp.instanceForGetHostForGet("", "");
	}

	@Test(expected = JargonException.class)
	public final void testGetParsedTagsForGetHostForGetNullSource()
			throws Exception {
		DataObjInp.instanceForGetHostForGet(null, "");
	}

	@Test(expected = JargonException.class)
	public final void testGetParsedTagsForGetHostForGetNullResource()
			throws Exception {
		DataObjInp.instanceForGetHostForGet("hello", null);
	}

}
