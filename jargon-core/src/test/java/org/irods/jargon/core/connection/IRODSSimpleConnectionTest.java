package org.irods.jargon.core.connection;

import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSSimpleConnectionTest {
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

	@Test
	public void testOpenAndCloseSimpleConnection() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSProtocolManager connectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSManagedConnection connection = connectionManager
				.getIRODSProtocol(irodsAccount);
		connection.disconnect();
		TestCase.assertFalse(connection.isConnected());
	}

}
