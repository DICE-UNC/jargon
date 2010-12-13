package org.irods.jargon.core.connection;

import static org.junit.Assert.*;

import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConnectionProxyDefinitionTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
	}

	@Test
	public final void testConnectionProxyDefinition() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		ConnectionProxyDefinition def = ConnectionProxyDefinition.instance(
				"role", irodsAccount);
		TestCase.assertNotNull("null connection proxy from instance method",
				def);
	}

	@Test(expected = JargonException.class)
	public final void testConnectionProxyDefinitionNullAccount()
			throws Exception {
		ConnectionProxyDefinition.instance("role", null);
	}

	@Test(expected = JargonException.class)
	public final void testConnectionProxyDefinitionNullRole() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		ConnectionProxyDefinition def = ConnectionProxyDefinition.instance(
				null, irodsAccount);
	}

	@Test(expected = JargonException.class)
	public final void testConnectionProxyDefinitionBlankRole() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		ConnectionProxyDefinition def = ConnectionProxyDefinition.instance("",
				irodsAccount);
	}
}
