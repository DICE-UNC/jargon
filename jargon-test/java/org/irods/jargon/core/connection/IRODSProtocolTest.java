package org.irods.jargon.core.connection;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSProtocolTest {

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
	public void testIsConnected() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSCommands irodsProtocol = IRODSCommands.instance(irodsAccount,
				irodsConnectionManager);
		Assert.assertTrue("i should have been connected",
				irodsProtocol.isConnected());
		irodsProtocol.disconnect();
	}

	@Test
	public void testChallengeIsCachedForStandardPassword() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSCommands irodsProtocol = IRODSCommands.instance(irodsAccount,
				irodsConnectionManager);
		Assert.assertTrue("i should have been connected", irodsProtocol
				.getCachedChallengeValue().length() > 0);
		irodsProtocol.disconnect();
	}

	@Test
	public void testDisconnect() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSCommands irodsProtocol = IRODSCommands.instance(irodsAccount,
				irodsConnectionManager);
		irodsProtocol.disconnect();
		Assert.assertFalse("i should have disconnected",
				irodsProtocol.isConnected());
	}

	@Test
	public void testDisconnectWithIOException() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSCommands irodsProtocolEngine = IRODSCommands.instance(
				irodsAccount, irodsConnectionManager);
		irodsProtocolEngine.disconnectWithIOException();
		Assert.assertFalse("i should have disconnected",
				irodsProtocolEngine.isConnected());
	}

	@Test
	public void testGetIRODSAccount() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSCommands irodsProtocolEngine = IRODSCommands.instance(
				irodsAccount, irodsConnectionManager);
		IRODSAccount actualIRODSAccount = irodsProtocolEngine.getIRODSAccount();
		irodsProtocolEngine.disconnect();
		Assert.assertEquals(
				"i should have gotten back the correct IRODSAccount",
				irodsAccount.getUserName(), actualIRODSAccount.getUserName());
	}

	@Test
	public void testGetIRODSServerProperties() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSCommands irodsProtocolEngine = IRODSCommands.instance(
				irodsAccount, irodsConnectionManager);
		IRODSServerProperties irodsServerProperties = irodsProtocolEngine
				.getIRODSServerProperties();
		irodsProtocolEngine.disconnect();
		Assert.assertNotNull(irodsServerProperties);
	}

	@Test
	public void testGetConnectionUri() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSCommands irodsProtocol = IRODSCommands.instance(irodsAccount,
				irodsConnectionManager);
		irodsProtocol.disconnect();
		Assert.assertNotNull(irodsProtocol.getConnectionUri());
	}

}
