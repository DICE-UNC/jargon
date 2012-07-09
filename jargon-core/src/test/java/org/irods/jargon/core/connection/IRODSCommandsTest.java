package org.irods.jargon.core.connection;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSCommandsTest {

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
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSCommands irodsProtocol = IRODSCommands.instance(irodsAccount,
				irodsConnectionManager, irodsFileSystem.getIrodsSession()
						.buildPipelineConfigurationBasedOnJargonProperties());
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
				irodsConnectionManager, irodsFileSystem.getIrodsSession()
						.buildPipelineConfigurationBasedOnJargonProperties());
		Assert.assertTrue("i should have been connected", irodsProtocol
				.getCachedChallengeValue().length() > 0);
		irodsProtocol.disconnect();
	}

	/**
	 * test starting a connection and sending a startup pack when connection
	 * restarting is off
	 * 
	 * @throws Exception
	 */
	@Test
	public void testStartupPackNoReconn() throws Exception {

		SettableJargonProperties jargonProperties = new SettableJargonProperties();
		jargonProperties.setReconnect(false);
		IRODSFileSystem testFS = IRODSFileSystem.instance();
		testFS.getIrodsSession().setJargonProperties(jargonProperties);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		/*
		 * EnvironmentalInfoAO environmentalInfoAO = testFS
		 * .getIRODSAccessObjectFactory().getEnvironmentalInfoAO( irodsAccount);
		 * IRODSServerProperties irodsServerProperties = environmentalInfoAO
		 * .getIRODSServerProperties();
		 */

		IRODSCommands irodsCommands = testFS.getIRODSAccessObjectFactory()
				.getIrodsSession().currentConnection(irodsAccount);
		StartupResponseData startupResponseData = irodsCommands
				.getStartupResponseData();

		testFS.closeAndEatExceptions();
		Assert.assertNotNull("null startup response data", startupResponseData);
		Assert.assertFalse("no api version", startupResponseData
				.getApiVersion().isEmpty());
		Assert.assertFalse("no rel version", startupResponseData
				.getRelVersion().isEmpty());

	}

	/**
	 * test starting a connection and sending a startup pack when connection
	 * restarting is o
	 * 
	 * @throws Exception
	 */
	@Test
	public void testStartupPackWithReconn() throws Exception {

		SettableJargonProperties jargonProperties = new SettableJargonProperties();
		jargonProperties.setReconnect(true);
		IRODSFileSystem testFS = IRODSFileSystem.instance();
		testFS.getIrodsSession().setJargonProperties(jargonProperties);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		/*
		 * EnvironmentalInfoAO environmentalInfoAO = testFS
		 * .getIRODSAccessObjectFactory().getEnvironmentalInfoAO( irodsAccount);
		 * IRODSServerProperties irodsServerProperties = environmentalInfoAO
		 * .getIRODSServerProperties();
		 */

		IRODSCommands irodsCommands = testFS.getIRODSAccessObjectFactory()
				.getIrodsSession().currentConnection(irodsAccount);
		StartupResponseData startupResponseData = irodsCommands
				.getStartupResponseData();

		testFS.closeAndEatExceptions();
		Assert.assertNotNull("null startup response data", startupResponseData);
		Assert.assertFalse("no api version", startupResponseData
				.getApiVersion().isEmpty());
		Assert.assertFalse("no rel version", startupResponseData
				.getRelVersion().isEmpty());
		Assert.assertTrue("no port", startupResponseData.getReconnPort() > 0);
		Assert.assertFalse("no restart host", startupResponseData
				.getReconnAddr().isEmpty());
		Assert.assertFalse("no restart cookie", startupResponseData.getCookie()
				.isEmpty());

	}

	@Test
	public void testDisconnect() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSCommands irodsProtocol = IRODSCommands.instance(irodsAccount,
				irodsConnectionManager, irodsFileSystem.getIrodsSession()
						.buildPipelineConfigurationBasedOnJargonProperties());
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
		IRODSCommands irodsProtocol = IRODSCommands.instance(irodsAccount,
				irodsConnectionManager, irodsFileSystem.getIrodsSession()
						.buildPipelineConfigurationBasedOnJargonProperties());
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
		IRODSCommands irodsProtocolEngine = IRODSCommands.instance(
				irodsAccount, irodsConnectionManager, irodsFileSystem
						.getIrodsSession()
						.buildPipelineConfigurationBasedOnJargonProperties());
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
				irodsAccount, irodsConnectionManager, irodsFileSystem
						.getIrodsSession()
						.buildPipelineConfigurationBasedOnJargonProperties());
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
				irodsAccount, irodsConnectionManager, irodsFileSystem
						.getIrodsSession()
						.buildPipelineConfigurationBasedOnJargonProperties());
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
				irodsConnectionManager, irodsFileSystem.getIrodsSession()
						.buildPipelineConfigurationBasedOnJargonProperties());
		irodsProtocol.disconnect();
		Assert.assertNotNull(irodsProtocol.getConnectionUri());
	}

}
