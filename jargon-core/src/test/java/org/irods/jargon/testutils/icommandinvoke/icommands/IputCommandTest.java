/**
 *
 */
package org.irods.jargon.testutils.icommandinvoke.icommands;

import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.irods.jargon.testutils.icommandinvoke.IcommandException;
import org.irods.jargon.testutils.icommandinvoke.IcommandInvoker;
import org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.irods.jargon.testutils.TestingPropertiesHelper.*;

/**
 * @author Mike Conway, DICE (www.irods.org)
 * 
 */
public class IputCommandTest {
	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	public static final String IRODS_TEST_SUBDIR_PATH = "IputCommandTest";
	private static ScratchFileUtils scratchFileUtils = null;
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testingProperties = testingPropertiesHelper.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.icommandinvoke.icommands.IputCommand#buildCommand()}
	 * .
	 */
	@Test(expected = IcommandException.class)
	public void testNoLocalFile() throws Exception {
		IputCommand iputCommand = new IputCommand();
		iputCommand.buildCommand();
	}

	@Test
	public void testExecuteCommand() throws Exception {
		// no exception = passed
		String testFileName = "testIputExecution.txt";
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);

		// generate testing file
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String absPathToFile = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 20);

		IputCommand iputCommand = new IputCommand();

		iputCommand.setLocalFileName(absPathToFile);
		iputCommand.setIrodsFileName(testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH));

		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);
	}

	@Test
	public void testPutSecondaryResource() throws Exception {
		// no exception = passed
		String testFileName = "testPutSecondaryResource.txt";
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);

		// generate testing file
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String absPathToFile = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 20);

		IputCommand iputCommand = new IputCommand();

		iputCommand.setLocalFileName(absPathToFile);
		iputCommand.setIrodsFileName(testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH));
		iputCommand.setIrodsResource(testingProperties
				.getProperty(IRODS_SECONDARY_RESOURCE_KEY));

		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);
	}

	@Test
	public void testIputWithCollection() throws Exception {
		String testFileName = "testIputWithCollection.txt";
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);

		// generate testing file and get absolute path
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				20);

		StringBuilder localFileName = new StringBuilder();
		localFileName.append(absPath);
		localFileName.append(testFileName);

		String actualCollectionPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		// now put the file
		IputCommand iputCommand = new IputCommand();
		iputCommand.setIrodsFileName(actualCollectionPath);
		iputCommand.setForceOverride(true);
		iputCommand.setLocalFileName(localFileName.toString());

		invoker.invokeCommandAndGetResultAsString(iputCommand);

		// now check if file exists in irods
		IlsCommand ilsCommand = new IlsCommand();
		ilsCommand.setIlsBasePath(actualCollectionPath);

		String res = invoker.invokeCommandAndGetResultAsString(ilsCommand);
		TestCase.assertTrue("did not find file I just put", res
				.indexOf(testFileName) > -1);
	}

	/**
	 * Expect a -317000 USER_INPUT_PATH_ERR because the local file is not found
	 * 
	 * @throws Exception
	 */
	@Test(expected = IcommandException.class)
	public void testExecuteNonExistantLocalFile() throws Exception {
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);

		IputCommand iputCommand = new IputCommand();
		iputCommand.setLocalFileName("c:/temp/bogusbogus.txt");

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

	}

}
