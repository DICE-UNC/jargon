/**
 * 
 */
package org.irods.jargon.testutils;

import static org.irods.jargon.testutils.TestingPropertiesHelper.IRODS_SCRATCH_DIR_KEY;

import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.testutils.icommandinvoke.IcommandInvoker;
import org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext;
import org.irods.jargon.testutils.icommandinvoke.icommands.IlsCommand;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Test of utilities used by Junit to initialize IRODS scratch area to a clean state
 * @author Mike Conway, DICE (www.irods.org)
 * 
 */
public class IRODSTestSetupUtilitiesTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testingProperties = testingPropertiesHelper.getTestProperties();
		IRODSTestSetupUtilities utils = new IRODSTestSetupUtilities();

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
	 * {@link org.irods.jargon.testutils.IRODSTestSetupUtilities#IrodsTestSetupUtilities()}
	 * .
	 */
	@Test
	public final void testCanIConstructIrodsTestSetupUtilitiesAndLoadProps()
			throws Exception {
		// can I set up constructor (ok if no error)
		IRODSTestSetupUtilities utils = new IRODSTestSetupUtilities();
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.testutils.IRODSTestSetupUtilities#clearIrodsScratchDirectory()}
	 * .
	 */
	@Test
	public final void testCanIClearIrodsScratchDirectory() throws Exception {
		// ok if no error
		IRODSTestSetupUtilities utils = new IRODSTestSetupUtilities();
		utils.clearIrodsScratchDirectory();
	}
	
	/**
	 * 
	 * try repeatedly clearing to simulate clear operation when do dir present
	 * 
	 */
	@Test
	public final void testCanIClearNonExistentIrodsScratchDirectory() throws Exception {
		// ok if no error
		IRODSTestSetupUtilities utils = new IRODSTestSetupUtilities();
		utils.clearIrodsScratchDirectory();
		utils.clearIrodsScratchDirectory();
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.testutils.IRODSTestSetupUtilities#clearIrodsScratchDirectory()}
	 * .
	 */
	@Test
	public final void testInitializeIrodsScratchDirectory() throws Exception {

		IRODSTestSetupUtilities utils = new IRODSTestSetupUtilities();
		utils.initializeIrodsScratchDirectory();
		// do an ils and make sure scratch is there now

		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);

		IlsCommand ilsCommand = new IlsCommand();
		String ilsResult = invoker
				.invokeCommandAndGetResultAsString(ilsCommand);
		TestCase
				.assertTrue(
						"did not find scratch dir created in irods",
						ilsResult.indexOf("/"
								+ testingProperties
										.getProperty(IRODS_SCRATCH_DIR_KEY)) > -1);

	}

	@Test
	public final void testInitializeDirectoryForTest() throws Exception {
		String expectedDirectoryName = "testthisdir";
		IRODSTestSetupUtilities utils = new IRODSTestSetupUtilities();
		utils.initializeIrodsScratchDirectory();
		utils.initializeDirectoryForTest(expectedDirectoryName);
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		IlsCommand ilsCommand = new IlsCommand();
		ilsCommand.setIlsBasePath(testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, expectedDirectoryName));
		String ilsResult = invoker
				.invokeCommandAndGetResultAsString(ilsCommand);
		TestCase.assertTrue("did not find test dir created in irods", ilsResult
				.indexOf("/" + expectedDirectoryName) > -1);
	}

}
