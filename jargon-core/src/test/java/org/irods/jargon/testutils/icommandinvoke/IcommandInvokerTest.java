package org.irods.jargon.testutils.icommandinvoke;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.icommandinvoke.icommands.IlsCommand;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class IcommandInvokerTest {
	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
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
	public void testInvokeViaExecutor() throws Exception {
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IlsCommand ilsCommand = new IlsCommand();
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		String result = invoker.invokeViaExecutor(ilsCommand);
		Assert.assertTrue("did not get a response from the ils command", result
				.length() > 0);
	}

}
