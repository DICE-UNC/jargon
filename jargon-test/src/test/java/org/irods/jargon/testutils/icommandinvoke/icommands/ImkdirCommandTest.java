/**
 *
 */
package org.irods.jargon.testutils.icommandinvoke.icommands;

import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.irods.jargon.testutils.icommandinvoke.IcommandInvoker;
import org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;



/**
 * Testing functionality of iput command via icommand wrapper
 * 
 * @author Mike Conway, DICE (www.irods.org)
 * @since 10/20/2009
 */
public class ImkdirCommandTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	public static final String IRODS_COMMAND_TEST_PATH = "ImkdirCommandTest";
	private static ScratchFileUtils scratchFileUtils = null;
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_COMMAND_TEST_PATH);
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
	 * {@link org.irods.jargon.icommandinvoke.icommands.ImkdirCommand#buildCommand()}
	 * .
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testNullCollection() throws Exception {
		ImkdirCommand command = new ImkdirCommand();
		command.setCollectionName(null);
		command.buildCommand();
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.icommandinvoke.icommands.ImkdirCommand#buildCommand()}
	 * .
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testZeroLengthCollection() throws Exception {
		ImkdirCommand command = new ImkdirCommand();
		command.setCollectionName("");
		command.buildCommand();
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.icommandinvoke.icommands.ImkdirCommand#buildCommand()}
	 * .
	 */
	@Test
	public void testCanBuildValidMkdirCommand() throws Exception {
		ImkdirCommand command = new ImkdirCommand();
		command.setCollectionName("scratch");
		command.buildCommand();
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.icommandinvoke.icommands.ImkdirCommand#buildCommand()}
	 * .
	 */
	@Test
	public void testExecMkdirCommand() throws Exception {

		
		ImkdirCommand command = new ImkdirCommand();
		String testingDirName = "mkdir-test";
		command.setCollectionName(testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, testingDirName));
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(command);

		// use an ils command to see if the dir now exists
		IlsCommand ilsCommand = new IlsCommand();
		ilsCommand.setIlsBasePath(testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, testingDirName));
		String ilsResult = invoker
				.invokeCommandAndGetResultAsString(ilsCommand);
		TestCase.assertTrue("test directory is not there", ilsResult
				.indexOf(testingDirName) > -1);
	}

}
