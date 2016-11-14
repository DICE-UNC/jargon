package org.irods.jargon.core.checksum;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MD5LocalChecksumComputerStrategyTest {

	private static Properties testingProperties = new Properties();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "MD5LocalChecksumComputerStrategyTest";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testInstanceChecksumForPackingInstruction() throws Exception {
		String testFileName = "testInstanceChecksumForPackingInstruction.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		byte[] expectedDigest = LocalFileUtils
				.computeMD5FileCheckSumViaAbsolutePath(localFileName);
		String expectedAsString = LocalFileUtils
				.digestByteArrayToString(expectedDigest);

		MD5LocalChecksumComputerStrategy checksumStrategy = new MD5LocalChecksumComputerStrategy();
		ChecksumValue actual = checksumStrategy
				.computeChecksumValueForLocalFile(localFileName);

		Assert.assertEquals(
				"did not compute md5 checksum and string encode it",
				expectedAsString, actual.getChecksumStringValue());
		Assert.assertEquals("transmission value improper", expectedAsString,
				actual.getChecksumTransmissionFormat());

	}

}
