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

import static edu.sdsc.jargon.testutils.TestingPropertiesHelper.*;

/**
 * @author Mike Conway, DICE (www.renci.org)
 * @since 2.2.1
 */
public class IlsCommandTest {

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

	/**
	 * Test method for
	 * {@link edu.sdsc.jargon.testutils.icommandinvoke.icommands.IlsCommand#executeCommand(edu.sdsc.jargon.testutils.icommandinvoke.IrodsInvocationContext)}
	 * .
	 */
	@Test
	public void testExecuteCommand() throws Exception {

		IrodsInvocationContext invocationContext = testingPropertiesHelper.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IlsCommand ilsCommand = new IlsCommand();
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		String result = invoker.invokeCommandAndGetResultAsString(ilsCommand);
		Assert.assertTrue("did not get a response from the ils command",
				result.length() > 0);

	}
	
	/**
	 * Test method for
	 * {@link edu.sdsc.jargon.testutils.icommandinvoke.icommands.IlsCommand#executeCommand(edu.sdsc.jargon.testutils.icommandinvoke.IrodsInvocationContext)}
	 * .
	 */
	@Test
	public void testExecuteCommandWithBasePath() throws Exception {

		IrodsInvocationContext invocationContext = testingPropertiesHelper.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		
		ImkdirCommand mkdirCommand = new ImkdirCommand();
		String testName = "scratch/bpilstest";
		mkdirCommand.setCollectionName(testName);
		invoker.invokeCommandAndGetResultAsString(mkdirCommand);
		
		// exercise test code
		IlsCommand ilsCommand = new IlsCommand();
		ilsCommand.setIlsBasePath("scratch");
		String ilsResult = invoker.invokeCommandAndGetResultAsString(ilsCommand);
		
		Assert.assertTrue("did not list just-added collection via ils",
				ilsResult.indexOf(testName) > -1);

	}
	
	@Test
	public void testExecuiteCommandReturningString() throws Exception {
		IlsCommand ilsCommand = new IlsCommand();
		IrodsInvocationContext invocationContext = testingPropertiesHelper.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		String ilsResult = invoker.invokeCommandAndGetResultAsString(ilsCommand);
		TestCase.assertTrue("did not find home dir in ils result", ilsResult.indexOf("/home") > -1);
	
	}

}
