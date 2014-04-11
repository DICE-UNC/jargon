package org.irods.jargon.conveyor.flowmanager.flow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.BeforeClass;
import org.junit.Test;

public class FlowSpecCacheServiceTest {
	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "FlowSpecCacheServiceTest";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(
				testingProperties);
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
	}

	@Test
	public void testInitBaseRule() throws Exception {

		String groovyFile = "/testFlowDsl/testInitBaseRule.groovy";

		String scratchPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ "/testInitBaseRule");

		File scratchPathDestFile = new File(scratchPath);
		scratchPathDestFile.mkdirs();
		scratchPathDestFile = new File(scratchPath, "testInitBaseRule.groovy");
		File groovySourceFile = LocalFileUtils
				.getClasspathResourceAsFile(groovyFile);
		FileUtils.copyFile(groovySourceFile, scratchPathDestFile);
		FlowSpecCacheService flowSpecCacheService = new FlowSpecCacheService();
		List<String> paths = new ArrayList<String>();
		paths.add(scratchPath);
		flowSpecCacheService.setFlowSourceLocalAbsolutePaths(paths);
		flowSpecCacheService.init();

	}

}
