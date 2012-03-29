package org.irods.jargon.core.connection;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.auth.AuthMechanism;
import org.irods.jargon.core.connection.auth.StandardIRODSAuth;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSProtocolTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testIsConnected() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		AuthMechanism authMechanism = new StandardIRODSAuth();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSCommands irodsProtocol = IRODSCommands.instance(irodsAccount,
				irodsConnectionManager, irodsFileSystem.getIrodsSession()
						.buildPipelineConfigurationBasedOnJargonProperties(),
				authMechanism);
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
		AuthMechanism authMechanism = new StandardIRODSAuth();
		IRODSCommands irodsProtocol = IRODSCommands.instance(irodsAccount,
				irodsConnectionManager, irodsFileSystem.getIrodsSession()
						.buildPipelineConfigurationBasedOnJargonProperties(),
				authMechanism);
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
		AuthMechanism authMechanism = new StandardIRODSAuth();
		IRODSCommands irodsProtocol = IRODSCommands.instance(irodsAccount,
				irodsConnectionManager, irodsFileSystem.getIrodsSession()
						.buildPipelineConfigurationBasedOnJargonProperties(),
				authMechanism);
		irodsProtocol.disconnect();
		Assert.assertFalse("i should have disconnected",
				irodsProtocol.isConnected());
	}

	@Test
	public void testConnectAnonymous() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildAnonymousIRODSAccountFromTestProperties(testingProperties);
		AuthMechanism authMechanism = new StandardIRODSAuth();
		IRODSCommands irodsProtocol = IRODSCommands.instance(irodsAccount,
				irodsConnectionManager, irodsFileSystem.getIrodsSession()
						.buildPipelineConfigurationBasedOnJargonProperties(),
				authMechanism);
		Assert.assertTrue("i should have connected",
				irodsProtocol.isConnected());
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
		AuthMechanism authMechanism = new StandardIRODSAuth();
		IRODSCommands irodsProtocol = IRODSCommands.instance(irodsAccount,
				irodsConnectionManager, irodsFileSystem.getIrodsSession()
						.buildPipelineConfigurationBasedOnJargonProperties(),
				authMechanism);
		irodsProtocol.setIrodsSession(irodsFileSystem.getIrodsSession());
		irodsProtocol.disconnectWithIOException();
		Assert.assertFalse("i should have disconnected",
				irodsProtocol.isConnected());
	}

	@Test
	public void testGetIRODSAccount() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		AuthMechanism authMechanism = new StandardIRODSAuth();
		IRODSCommands irodsProtocol = IRODSCommands.instance(irodsAccount,
				irodsConnectionManager, irodsFileSystem.getIrodsSession()
						.buildPipelineConfigurationBasedOnJargonProperties(),
				authMechanism);
		IRODSAccount actualIRODSAccount = irodsProtocol.getIrodsAccount();
		irodsProtocol.disconnect();
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
		AuthMechanism authMechanism = new StandardIRODSAuth();
		IRODSCommands irodsProtocol = IRODSCommands.instance(irodsAccount,
				irodsConnectionManager, irodsFileSystem.getIrodsSession()
						.buildPipelineConfigurationBasedOnJargonProperties(),
				authMechanism);
		IRODSServerProperties irodsServerProperties = irodsProtocol
				.getIRODSServerProperties();
		irodsProtocol.disconnect();
		Assert.assertNotNull(irodsServerProperties);
	}

	@Test
	public void testGetConnectionUri() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		AuthMechanism authMechanism = new StandardIRODSAuth();
		IRODSCommands irodsProtocol = IRODSCommands.instance(irodsAccount,
				irodsConnectionManager, irodsFileSystem.getIrodsSession()
						.buildPipelineConfigurationBasedOnJargonProperties(),
				authMechanism);
		irodsProtocol.disconnect();
		Assert.assertNotNull(irodsProtocol.getConnectionUri());
	}

}
