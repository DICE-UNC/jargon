package org.irods.jargon.testutils.filemanip;

import java.util.Properties;

import org.irods.jargon.testutils.AssertionHelper;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileGeneratorTest {

	private static Properties testingProperties = new Properties();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "FileGeneratorTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		new AssertionHelper();
	}

	@Test
	public void testGenerateManyFilesAndCollectionsInParentCollectionByAbsolutePath()
			throws Exception {

		String collName = "testGenerateManyFilesAndCollectionsInParentCollectionByAbsolutePath";
		String targetLocalFile = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ "/" + collName);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						targetLocalFile, "coll", 2, 4, 2, "testFile", ".txt",
						20000, 9000, 1, 2);

	}

}
