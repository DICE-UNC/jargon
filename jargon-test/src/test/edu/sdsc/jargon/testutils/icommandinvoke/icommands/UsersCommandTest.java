/**
 *
 */
package edu.sdsc.jargon.testutils.icommandinvoke.icommands;

import java.util.Properties;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import edu.sdsc.jargon.testutils.TestingPropertiesHelper;
import edu.sdsc.jargon.testutils.icommandinvoke.IcommandException;
import edu.sdsc.jargon.testutils.icommandinvoke.IcommandInvoker;
import edu.sdsc.jargon.testutils.icommandinvoke.IrodsInvocationContext;

/**
 * Testing functionality of iadmin lu command
 * @author Mike Conway, DICE (www.irods.org)
 * @since 10/20/2009
 */
public class UsersCommandTest {

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
	 * Test method for {@link org.irods.jargon.icommandinvoke.icommands.ImkdirCommand#buildCommand()}.
	 */
	@Test
	public void testExecLuCommand() throws Exception {
		
		// set up a test dir to remove via command
		ListUsersCommand command = new ListUsersCommand();
		IrodsInvocationContext invocationContext = testingPropertiesHelper.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		String result =invoker.invokeCommandAndGetResultAsString(command);
		TestCase.assertTrue("did not find user name in users", result.indexOf(testingProperties.getProperty(TestingPropertiesHelper.IRODS_USER_KEY)) > -1);
	}
	
	@Test(expected=IcommandException.class)
	public void testRemoveUserNotExistsCommand() throws Exception {
		
		RemoveUserCommand command = new RemoveUserCommand();
		command.setUserName("i dont exist");
		IrodsInvocationContext invocationContext = testingPropertiesHelper.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		String result =invoker.invokeCommandAndGetResultAsString(command);
		
	}
	
	@Ignore // need to implement
	public void testListUserDn() throws Exception {
		
	}
	
}
