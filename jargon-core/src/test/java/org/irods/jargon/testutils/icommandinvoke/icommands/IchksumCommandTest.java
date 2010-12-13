package org.irods.jargon.testutils.icommandinvoke.icommands;

import java.util.Arrays;
import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.irods.jargon.testutils.icommandinvoke.IcommandInvoker;
import org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import static org.irods.jargon.testutils.TestingPropertiesHelper.*;

/**
 * Test chksum via icommand interface
 * 
 * @author Mike Conway, DICE (www.renci.org)
 * @since
 */
public class IchksumCommandTest {
	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IchksumCommandTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testingProperties = testingPropertiesHelper.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils.createDirectoryUnderScratch(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
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

	@Ignore
	// work in progress
	public void testPutThenChecksumAFile() throws Exception {
		String testFileName = "testQueryForResource.txt";

		// generate a file and put into irods
		String fullPathToTestFile = FileGenerator
				.generateFileOfFixedLengthGivenName(testingProperties
						.getProperty(GENERATED_FILE_DIRECTORY_KEY)
						+ IRODS_TEST_SUBDIR_PATH + "/", testFileName, 7);

		// get the actual checksum
		byte[] expectedChecksum = scratchFileUtils
				.computeFileCheckSum(IRODS_TEST_SUBDIR_PATH + "/"
						+ testFileName);

		IputCommand iputCommand = new IputCommand();
		iputCommand.setLocalFileName(fullPathToTestFile);
		iputCommand.setIrodsFileName(testingPropertiesHelper
				.buildIRODSCollectionRelativePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH));
		iputCommand.setForceOverride(true);

		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		// FIXME: fix code for comparing a generated MD5 with a checksum
		// returned from an ichksum command...
		// I have put the file, now assert that the file I put has the same
		// checksum as the file I generated
		IchksumCommand chksumCommand = new IchksumCommand();
		chksumCommand.setIrodsFileName(testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName));
		String chksumResult = invoker
				.invokeCommandAndGetResultAsString(chksumCommand);
		String expectedChecksumString = Arrays.toString(expectedChecksum);
		TestCase.assertTrue("did not compute checksum, expected:"
				+ expectedChecksumString + " actual:" + chksumResult,
				chksumResult.indexOf(expectedChecksumString) > -1);
	}
}
