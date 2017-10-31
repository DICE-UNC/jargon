package org.irods.jargon.core.utils;

import java.io.File;
import java.util.Properties;

import org.junit.Assert;

import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class LocalFileUtilsTest {

	private static Properties testingProperties = new Properties();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "LocalFileUtilsTest";

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
	public void testCountFilesInDirectory() throws Exception {
		String rootCollection = "testCountFilesInDirectory";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testPutCollectionWithTwoFiles", 1, 1, 1, "testFile",
						".txt", 2, 2, 1, 2);

		File rootCollectionFile = new File(localCollectionAbsolutePath);
		int fileCtr = LocalFileUtils.countFilesInDirectory(rootCollectionFile);
		Assert.assertEquals("did not count the two files", 2, fileCtr);

	}

	@Test
	public void testGetFileExtension() throws Exception {
		String testName = "blah.hellothere file name";
		String testExtension = ".txt";

		StringBuilder sb = new StringBuilder();
		sb.append(testName);
		sb.append(testExtension);
		String actual = LocalFileUtils.getFileExtension(sb.toString());
		Assert.assertEquals(testExtension, actual);

	}

	@Test
	public void testGetFileNameUpToExtension() throws Exception {
		String testName = "blah.hellothere file name";
		String testExtension = ".txt";

		StringBuilder sb = new StringBuilder();
		sb.append(testName);
		sb.append(testExtension);
		String actual = LocalFileUtils.getFileNameUpToExtension(sb.toString());
		Assert.assertEquals(testName, actual);

	}

	@Test
	public void testGenerateSHA256Checksum() throws Exception {
		String testFileName = "testGenerateSHA256Checksum.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		byte[] actual = LocalFileUtils
				.computeSHA256FileCheckSumViaAbsolutePath(localFileName);
		Assert.assertNotNull("no checksum", actual);

	}

}
