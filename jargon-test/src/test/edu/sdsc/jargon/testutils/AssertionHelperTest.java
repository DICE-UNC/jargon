package edu.sdsc.jargon.testutils;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.sdsc.jargon.testutils.filemanip.FileGenerator;
import edu.sdsc.jargon.testutils.filemanip.ScratchFileUtils;
import edu.sdsc.jargon.testutils.icommandinvoke.IcommandInvoker;
import edu.sdsc.jargon.testutils.icommandinvoke.IrodsInvocationContext;
import edu.sdsc.jargon.testutils.icommandinvoke.icommands.IputCommand;

public class AssertionHelperTest {

	private static ScratchFileUtils scratchFileUtils = null;
	private static String scratchFileSubdir = "";
	public static final String IRODS_TEST_SUBDIR_PATH = "AssertionHelperTest";
	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testingProperties = testingPropertiesHelper.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileSubdir = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
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

	@Test(expected = IRODSTestAssertionException.class)
	public final void testAssertLocalFileExistsInScratchExpectFailure()
			throws Exception {
		String bogusFileName = "thisisbogus.bog";
		AssertionHelper assertionHelper = new AssertionHelper();
		assertionHelper.assertLocalFileExistsInScratch(bogusFileName);
	}

	@Test
	public final void testAssertLocalFileExistsInScratchWhenValid()
			throws Exception {
		String testFileName = "testassertexists.txt";
		String absolutePathToTestFile = FileGenerator
				.generateFileOfFixedLengthGivenName(scratchFileSubdir,
						testFileName, 10);
		AssertionHelper assertionHelper = new AssertionHelper();
		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH
				+ "/" + testFileName);
	}

	@Test
	public final void testAssertLocalScratchFileLengthEquals() throws Exception {
		String testFileName = "testassertequals.txt";
		long expectedLength = 15;
		String absolutePathToTestFile = FileGenerator
				.generateFileOfFixedLengthGivenName(scratchFileSubdir,
						testFileName, expectedLength);
		AssertionHelper assertionHelper = new AssertionHelper();
		assertionHelper.assertLocalScratchFileLengthEquals(
				IRODS_TEST_SUBDIR_PATH + "/" + testFileName, expectedLength);
	}

	@Test(expected = IRODSTestAssertionException.class)
	public final void testAssertLocalScratchFileLengthEqualsWhenNotEquals()
			throws Exception {
		String testFileName = "testassertequals.txt";
		long expectedLength = 15;
		String absolutePathToTestFile = FileGenerator
				.generateFileOfFixedLengthGivenName(scratchFileSubdir,
						testFileName, expectedLength + 1);
		AssertionHelper assertionHelper = new AssertionHelper();
		assertionHelper.assertLocalScratchFileLengthEquals(
				IRODS_TEST_SUBDIR_PATH + "/" + testFileName, expectedLength);
	}

	@Test
	public final void testAssertLocalScratchFileHasChecksum() throws Exception {
		String testFileName = "testchecksum.txt";
		long fileLength = 15;
		String absolutePathToTestFile = FileGenerator
				.generateFileOfFixedLengthGivenName(scratchFileSubdir,
						testFileName, fileLength);

		// get the checksum of this file
		byte[] actualChecksum = scratchFileUtils
				.computeFileCheckSum(IRODS_TEST_SUBDIR_PATH + '/'
						+ testFileName);

		// now assert that the file has this checksum
		AssertionHelper assertionHelper = new AssertionHelper();
		assertionHelper.assertLocalFileHasChecksum(IRODS_TEST_SUBDIR_PATH + "/"
				+ testFileName, actualChecksum);
	}

	@Test
	public final void testAssertIrodsFileOrCollectionDoesNotExist()
			throws Exception {
		String testFileName = IRODS_TEST_SUBDIR_PATH + "/idontexistinirods.doc";
		String irodsAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, testFileName);
		AssertionHelper assertionHelper = new AssertionHelper();
		assertionHelper.assertIrodsFileOrCollectionDoesNotExist(testFileName);

	}

	@Test(expected = IRODSTestAssertionException.class)
	public final void testAssertNonExistentIRODSFile() throws Exception {
		String testFileName = "not a chance I exists!!!!!";
		AssertionHelper assertionHelper = new AssertionHelper();
		assertionHelper.assertIrodsFileOrCollectionExists(testFileName);
	}

	@Test
	public final void testAssertExistentIRODSFile() throws Exception {
		AssertionHelper assertionHelper = new AssertionHelper();
		assertionHelper
				.assertIrodsFileOrCollectionExists(testingPropertiesHelper
						.buildIRODSCollectionAbsolutePathFromTestProperties(
								testingProperties, IRODS_TEST_SUBDIR_PATH));
	}

	@Test
	public final void testMatchIRODSToLocalChecksumShouldMatch()
			throws Exception {
		String testFileName = "testMatchIRODSToLocalChecksumShouldMatch.txt";
		String absolutePathToTestFile = FileGenerator
				.generateFileOfFixedLengthGivenName(scratchFileSubdir,
						testFileName, 20);

		String targetIrodsCollection = IRODS_TEST_SUBDIR_PATH;
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();
		iputCommand.setLocalFileName(absolutePathToTestFile);
		iputCommand.setIrodsFileName(testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH));
		iputCommand.setIrodsResource(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY));
		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		AssertionHelper assertionHelper = new AssertionHelper();
		String absoluteIrodsPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		assertionHelper.assertIrodsFileMatchesLocalFileChecksum(
				absoluteIrodsPath, absolutePathToTestFile);
	}

}
