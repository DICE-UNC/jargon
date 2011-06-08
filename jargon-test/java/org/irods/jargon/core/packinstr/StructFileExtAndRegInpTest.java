package org.irods.jargon.core.packinstr;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class StructFileExtAndRegInpTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testInstanceForExtractBundleNoForce() throws Exception {
		String tarFileName = "/test/tarFileName.tar";
		String tarFileCollection = "/test/tarFileCollection";
		StructFileExtAndRegInp structFileExtAndRegInp = StructFileExtAndRegInp
				.instanceForExtractBundleNoForce(tarFileName,
						tarFileCollection, "");
		Assert.assertNotNull("null packing instruction", structFileExtAndRegInp);
		Assert.assertEquals("did not set correct API num",
				StructFileExtAndRegInp.STRUCT_FILE_EXTRACT_AND_REG_API_NBR,
				structFileExtAndRegInp.getApiNumber());
		Assert.assertFalse("should not have been flagged as a bulk operation",
				structFileExtAndRegInp.isExtractAsBulkOperation());
	}

	@Test
	public final void testInstanceForExtractBundleNoForceWithBulkOperationAndResource()
			throws Exception {
		String tarFileName = "/test/tarFileName.tar";
		String tarFileCollection = "/test/tarFileCollection";
		String destResource = "resource";
		StructFileExtAndRegInp structFileExtAndRegInp = StructFileExtAndRegInp
				.instanceForExtractBundleNoForceWithBulkOperation(tarFileName,
						tarFileCollection, destResource);
		Assert.assertNotNull("null packing instruction", structFileExtAndRegInp);
		Assert.assertEquals("did not set correct API num",
				StructFileExtAndRegInp.STRUCT_FILE_EXTRACT_AND_REG_API_NBR,
				structFileExtAndRegInp.getApiNumber());
		Assert.assertEquals("did not set resource properly", destResource,
				structFileExtAndRegInp.getResourceName());
		Assert.assertTrue("should have been flagged as a bulk operation",
				structFileExtAndRegInp.isExtractAsBulkOperation());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForExtractBundleNoForceWithBulkOperationAndNullResource()
			throws Exception {
		String tarFileName = "/test/tarFileName.tar";
		String tarFileCollection = "/test/tarFileCollection";
		String destResource = null;
		StructFileExtAndRegInp
				.instanceForExtractBundleNoForceWithBulkOperation(tarFileName,
						tarFileCollection, destResource);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForExtractBundleNoForceWithBulkOperationAndNullTar()
			throws Exception {
		String tarFileName = null;
		String tarFileCollection = "/test/tarFileCollection";
		String destResource = "foo";
		StructFileExtAndRegInp
				.instanceForExtractBundleNoForceWithBulkOperation(tarFileName,
						tarFileCollection, destResource);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForExtractBundleNoForceWithBulkOperationAndNullCollection()
			throws Exception {
		String tarFileName = "hello";
		String tarFileCollection = null;
		String destResource = "foo";
		StructFileExtAndRegInp
				.instanceForExtractBundleNoForceWithBulkOperation(tarFileName,
						tarFileCollection, destResource);
	}

	@Test
	public final void testInstanceForExtractBundleWithForceOption()
			throws Exception {
		String tarFileName = "/test/tarFileName.tar";
		String tarFileCollection = "/test/tarFileCollection";
		StructFileExtAndRegInp structFileExtAndRegInp = StructFileExtAndRegInp
				.instanceForExtractBundleWithForceOption(tarFileName,
						tarFileCollection, "");
		Assert.assertNotNull("null packing instruction", structFileExtAndRegInp);
		Assert.assertEquals("did not set correct API num",
				StructFileExtAndRegInp.STRUCT_FILE_EXTRACT_AND_REG_API_NBR,
				structFileExtAndRegInp.getApiNumber());
	}

	@Test
	public final void testInstanceForExtractBundleWithForceOptionAndBulkOperation()
			throws Exception {
		String tarFileName = "/test/tarFileName.tar";
		String tarFileCollection = "/test/tarFileCollection";
		StructFileExtAndRegInp structFileExtAndRegInp = StructFileExtAndRegInp
				.instanceForExtractBundleWithForceOptionAndBulkOperation(
						tarFileName, tarFileCollection, "");
		Assert.assertNotNull("null packing instruction", structFileExtAndRegInp);
		Assert.assertEquals("did not set correct API num",
				StructFileExtAndRegInp.STRUCT_FILE_EXTRACT_AND_REG_API_NBR,
				structFileExtAndRegInp.getApiNumber());
		Assert.assertTrue("should have been flagged as a bulk operation",
				structFileExtAndRegInp.isExtractAsBulkOperation());
	}

	@Test
	public final void testInstanceForCreateBundleOperation() throws Exception {
		String tarFileName = "/test/tarFileName.tar";
		String tarFileCollection = "/test/tarFileCollection";
		StructFileExtAndRegInp structFileExtAndRegInp = StructFileExtAndRegInp
				.instanceForCreateBundle(tarFileName, tarFileCollection, "");
		Assert.assertNotNull("null packing instruction", structFileExtAndRegInp);
		Assert.assertEquals("did not set correct API num",
				StructFileExtAndRegInp.STRUCT_FILE_BUNDLE_API_NBR,
				structFileExtAndRegInp.getApiNumber());
	}

	@Test
	public final void testMessageForCreateNoResource() throws Exception {
		String tarFileName = "/test/tarFileName.tar";
		String tarFileCollection = "/test/tarFileCollection";
		StructFileExtAndRegInp structFileExtAndRegInp = StructFileExtAndRegInp
				.instanceForCreateBundle(tarFileName, tarFileCollection, "");
		StringBuilder sb = new StringBuilder();

		sb.append("<StructFileExtAndRegInp_PI><objPath>/test/tarFileName.tar</objPath>\n");
		sb.append("<collection>/test/tarFileCollection</collection>\n");
		sb.append("<oprType>0</oprType>\n");
		sb.append("<flags>0</flags>\n");
		sb.append("<KeyValPair_PI><ssLen>1</ssLen>\n");
		sb.append("<keyWord>dataType</keyWord>\n");
		sb.append("<svalue>tar file</svalue>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("</StructFileExtAndRegInp_PI>\n");

		Assert.assertEquals("did not get expected packing instruction",
				sb.toString(), structFileExtAndRegInp.getParsedTags());
	}

	@Test
	public final void testMessageForCreateWithResource() throws Exception {
		String tarFileName = "/test/tarFileName.tar";
		String tarFileCollection = "/test/tarFileCollection";
		String resource = "resource";
		StructFileExtAndRegInp structFileExtAndRegInp = StructFileExtAndRegInp
				.instanceForCreateBundle(tarFileName, tarFileCollection,
						resource);
		StringBuilder sb = new StringBuilder();

		sb.append("<StructFileExtAndRegInp_PI><objPath>/test/tarFileName.tar</objPath>\n");
		sb.append("<collection>/test/tarFileCollection</collection>\n");
		sb.append("<oprType>0</oprType>\n");
		sb.append("<flags>0</flags>\n");
		sb.append("<KeyValPair_PI><ssLen>3</ssLen>\n");
		sb.append("<keyWord>dataType</keyWord>\n");
		sb.append("<keyWord>destRescName</keyWord>\n");
		sb.append("<keyWord>rescName</keyWord>\n");
		sb.append("<svalue>tar file</svalue>\n");
		sb.append("<svalue>resource</svalue>\n");
		sb.append("<svalue>resource</svalue>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("</StructFileExtAndRegInp_PI>\n");

		Assert.assertEquals("did not get expected packing instruction",
				sb.toString(), structFileExtAndRegInp.getParsedTags());
	}

}
