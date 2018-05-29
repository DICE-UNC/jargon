package org.irods.jargon.core.checksum;

import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SHA256LocalChecksumComputerStrategyTest {

	private static Properties testingProperties = new Properties();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "SHA256LocalChecksumComputerStrategyTest";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testInstanceChecksumForPackingInstruction() throws Exception {
		String testFileName = "testInstanceChecksumForPackingInstruction.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		byte[] expectedDigest = LocalFileUtils.computeSHA256FileCheckSumViaAbsolutePath(localFileName);
		String expectedAsString = Base64.encodeBase64String(expectedDigest).trim();
		AbstractChecksumComputeStrategy checksumStrategy = new SHA256LocalChecksumComputerStrategy();
		ChecksumValue actual = checksumStrategy.computeChecksumValueForLocalFile(localFileName);

		Assert.assertEquals("did not compute sha256 checksum and string encode it", expectedAsString,
				actual.getChecksumStringValue());
		Assert.assertEquals("transmission value improper", "sha2:" + expectedAsString,
				actual.getChecksumTransmissionFormat());
	}

}
