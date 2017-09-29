package org.irods.jargon.testutils;

import java.io.File;
import java.util.Properties;

import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class CyberduckProfileBuilderTest {

	private static Properties testingProperties = new Properties();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "CyberduckProfileBuilderTest";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
	}

	@Test
	public void testBuildProfile() throws Exception {

		String testFileName = "testprofile";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		File testProfile = new File(absPath, testFileName);
		CyberduckProfileBuilder.writeCyberduckProfile(testProfile.getAbsolutePath(), testingProperties);

	}

}
