/**
 *
 */
package edu.sdsc.jargon.testutils.icommandinvoke.icommands;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.sdsc.jargon.testutils.TestingPropertiesHelper;
import edu.sdsc.jargon.testutils.icommandinvoke.IcommandException;
import edu.sdsc.jargon.testutils.icommandinvoke.IcommandInvoker;
import edu.sdsc.jargon.testutils.icommandinvoke.IrodsInvocationContext;

/**
 * Testing functionality of irm command via icommand wrapper
 * @author Mike Conway, DICE (www.irods.org)
 * @since 10/20/2009
 */
public class IrmCommandTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
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

	@Test(expected=IllegalArgumentException.class)
	public void testNullObjectName() throws Exception {
		IrmCommand command = new IrmCommand();
		command.setObjectName(null);
		command.buildCommand();
	}

		@Test(expected=IllegalArgumentException.class)
	public void testZeroLengthObjectName() throws Exception {
		IrmCommand command = new IrmCommand();
		command.setObjectName("");
		command.buildCommand();
	}
	
	/**
	 * Test method for {@link org.irods.jargon.icommandinvoke.icommands.ImkdirCommand#buildCommand()}.
	 */
	@Test
	public void testCanBuildValidIrmCommand() throws Exception {
		IrmCommand command = new IrmCommand();
		command.setObjectName("scratch");
		command.buildCommand();
	}
	
	/**
	 * Test method for {@link org.irods.jargon.icommandinvoke.icommands.ImkdirCommand#buildCommand()}.
	 */
	@Test
	public void testExecIrmCommand() throws Exception {
		
		// set up a test dir to remove via command
		
		String testingDirName = "scratch/testdir-rm";
		
		ImkdirCommand command = new ImkdirCommand();
		command.setCollectionName(testingDirName);
		IrodsInvocationContext invocationContext = testingPropertiesHelper.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(command);
		
		// now make sure the dir is there
		IlsCommand ilsCommand = new IlsCommand();
		ilsCommand.setIlsBasePath("scratch");
		String ilsResult = invoker.invokeCommandAndGetResultAsString(ilsCommand);
		TestCase.assertTrue("test directory is not there", ilsResult.indexOf(testingDirName) > -1);
		

		// dir is out there in the proper place, now zap, this is the exercise of the real test
		
		IrmCommand rmCommand = new IrmCommand();
		rmCommand.setForce(true);
		rmCommand.setObjectName(testingDirName);
		invoker.invokeCommandAndGetResultAsString(rmCommand);
		
		// now make sure the dir is gone
		ilsCommand = new IlsCommand();
		ilsCommand.setIlsBasePath("scratch");
		ilsResult = invoker.invokeCommandAndGetResultAsString(ilsCommand);
		TestCase.assertTrue("test directory is still there", ilsResult.indexOf(testingDirName) == -1);
		
	}
	
}
