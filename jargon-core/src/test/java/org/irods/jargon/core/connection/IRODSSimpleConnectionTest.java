package org.irods.jargon.core.connection;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSSimpleConnectionTest {
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
	public void testOpenAndCloseSimpleConnection() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		AbstractIRODSMidLevelProtocol connection = irodsFileSystem
				.getIrodsSession().currentConnection(irodsAccount);
		connection.disconnect();

		Assert.assertFalse("connection should not be connected",
				connection.isConnected());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOpenAndCloseSimpleConnectionNullSession() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		irodsFileSystem.getIrodsProtocolManager().getIRODSProtocol(
				irodsAccount,
				irodsFileSystem.getIrodsSession()
				.buildPipelineConfigurationBasedOnJargonProperties(),
				null);

	}

}
