package org.irods.jargon.testutils.filemanip;

import static org.irods.jargon.testutils.TestingPropertiesHelper.GENERATED_FILE_DIRECTORY_KEY;

import java.io.File;
import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.TestingUtilsException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author Mike Conway, DICE (www.irods.org)
 * @since 11/03/2009
 * testing of {@link org.irods.jargon.testutils.filemanip.ScratchFileUtils}
 */

public class ScratchFileUtilsTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	public static final String IRODS_TEST_SUBDIR_PATH = "ScratchFileUtilsTest";
	private static ScratchFileUtils scratchFileUtils = null;
	private static String scratchFileSubdir = "";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileSubdir = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH); 
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testCreateScratchDirIfNotExists() throws Exception {
		Properties props = new Properties();
		props.setProperty(GENERATED_FILE_DIRECTORY_KEY, 
				testingProperties.getProperty(GENERATED_FILE_DIRECTORY_KEY));
		ScratchFileUtils scratchFileUtils = new ScratchFileUtils(props);
		scratchFileUtils.createScratchDirIfNotExists("testutils");
	}
	
	@Test
	public final void testCreateScratchDirWhenNotExists() throws Exception {
		Properties props = new Properties();
		props.setProperty(GENERATED_FILE_DIRECTORY_KEY, 
				testingProperties.getProperty(GENERATED_FILE_DIRECTORY_KEY));
		ScratchFileUtils scratchFileUtils = new ScratchFileUtils(props);
		String scratchFileName = String.valueOf(System.currentTimeMillis());
		scratchFileUtils.createScratchDirIfNotExists(scratchFileName);
		TestCase.assertTrue("did not create scratch dir", scratchFileUtils.checkIfFileExistsInScratch(scratchFileName));
	}
	
	@Test
	public final void testCheckFileExistsUnderScratch() throws Exception {
		Properties props = new Properties();
		String testingDir = "testutils";
		
		// setup a dir
		
		props.setProperty(GENERATED_FILE_DIRECTORY_KEY, 
				testingProperties.getProperty(GENERATED_FILE_DIRECTORY_KEY));
		ScratchFileUtils scratchFileUtils = new ScratchFileUtils(props);
		scratchFileUtils.createScratchDirIfNotExists(testingDir);
		
		// now I should find that dir
		boolean foundDir = scratchFileUtils.checkIfFileExistsInScratch(testingDir);
		TestCase.assertTrue("did not find scratch file just added", foundDir);
	}
	
	@Test(expected=TestingUtilsException.class)
	public final void testCatchMalformedGeneratedFilePath() throws Exception {
		Properties props = new Properties();
		props.setProperty(GENERATED_FILE_DIRECTORY_KEY, "C:/temp/bogus");
		ScratchFileUtils utils = new ScratchFileUtils(props);
	}
	
	@Test
	public final void testCreateAndReturnScratchPathToFileName() throws Exception {
		Properties props = new Properties();
		String testingDir = "createandreturnscratchpath";
		String testingFileName = "hello1.txt";
		// setup a dir
		
		props.setProperty(GENERATED_FILE_DIRECTORY_KEY, 
				testingProperties.getProperty(GENERATED_FILE_DIRECTORY_KEY));
		
		StringBuilder pathBuilder = new StringBuilder();
		pathBuilder.append(testingDir);
		pathBuilder.append('/');
		pathBuilder.append(testingFileName);
		String newPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(testingDir);
		
		// now I should find that dir
		boolean foundDir = scratchFileUtils.checkIfFileExistsInScratch(testingDir);
		TestCase.assertTrue("did not find scratch file just added", foundDir);
	}
	
	@Test
	public final void testCreateAndReturnAbsoluteScratchPathToFileName() throws Exception {
		Properties props = new Properties();
		String testingDir = "testutils/createandreturnscratchpath/";
		String testingFileName = "hello1.txt";
		// setup a dir
		
		props.setProperty(GENERATED_FILE_DIRECTORY_KEY, 
				testingProperties.getProperty(GENERATED_FILE_DIRECTORY_KEY));
		
		ScratchFileUtils scratchFileUtils = new ScratchFileUtils(props);
		StringBuilder pathBuilder = new StringBuilder();
		pathBuilder.append(testingDir);
		pathBuilder.append(testingFileName);
		String newPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(testingDir);
		TestCase.assertTrue("no path returned", newPath.length() > 0);
		
		// now create a file using the path
		FileGenerator.generateFileOfFixedLengthGivenName(newPath, testingFileName, 1);
		
		File existsFile = new File(newPath);
		TestCase.assertTrue("did not find scratch file just added", existsFile.exists());
	}
	
	@Test
	public final void testComputeValidFileChecksum() throws Exception {
		
		String testingFileName = "checksum.dat"; 
		ScratchFileUtils scratchFileUtils = new ScratchFileUtils(testingProperties);
		
		// now create a file using the path
		String fullPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(fullPath, testingFileName, 11);
		byte[] actualChecksum = scratchFileUtils.computeFileCheckSum(IRODS_TEST_SUBDIR_PATH + '/' + testingFileName);
		
		TestCase.assertTrue("no checksum generated", actualChecksum.length > 0);
	}

}
