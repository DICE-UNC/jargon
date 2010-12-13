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
public class IreplCommandTest {
	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	public static final String IRODS_TEST_SUBDIR_PATH = "IreplCommandTest";
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

	@Test
	public void testIreplFile() throws Exception {

		String testFileName = "testIreplExecution.txt";
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

		// now replicate the file to the secondary resource

		IreplCommand ireplCommand = new IreplCommand();
		ireplCommand.setObjectToReplicate(iputCommand.getIrodsFileName() + '/'
				+ testFileName);
		ireplCommand.setDestResource(testingProperties
				.getProperty(IRODS_SECONDARY_RESOURCE_KEY));
		invoker.invokeCommandAndGetResultAsString(ireplCommand);

		// check that file is in both resources

		IlsCommand ilsCommand = new IlsCommand();
		ilsCommand.setIlsBasePath(ireplCommand.getObjectToReplicate());
		ilsCommand.setLongFormat(true);
		String ilsResult = invoker
				.invokeCommandAndGetResultAsString(ilsCommand);
		TestCase.assertTrue("did not find first resource",
				ilsResult.indexOf(testingProperties
						.getProperty(IRODS_RESOURCE_KEY)) > -1);
		TestCase.assertTrue("did not find replicated resource", ilsResult
				.indexOf(testingProperties
						.getProperty(IRODS_SECONDARY_RESOURCE_KEY)) > -1);

	}

}
