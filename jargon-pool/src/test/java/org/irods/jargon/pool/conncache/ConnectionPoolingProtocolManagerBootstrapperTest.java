package org.irods.jargon.pool.conncache;

import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryImpl;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConnectionPoolingProtocolManagerBootstrapperTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	public static final String IRODS_TEST_SUBDIR_PATH = "ConnectionPoolingProtocolManagerBootstrapperTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
	}

	@Test
	public void testInit() throws Exception {
		JargonKeyedPoolConfig config = new JargonKeyedPoolConfig();
		IRODSSession irodsSession = new IRODSSession();
		IRODSAccessObjectFactory irodsAccessObjectFactory = new IRODSAccessObjectFactoryImpl(irodsSession);
		ConnectionPoolingProtocolManagerBootstrapper bootstrapper = new ConnectionPoolingProtocolManagerBootstrapper();
		bootstrapper.setIrodsSession(irodsSession);
		bootstrapper.setJargonKeyedPoolConfig(config);
		bootstrapper.init();

		IRODSAccount test1 = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		EnvironmentalInfoAO environmentalInfoAO = irodsAccessObjectFactory.getEnvironmentalInfoAO(test1);
		environmentalInfoAO.getIRODSServerCurrentTime();
		Assert.assertTrue(true);
		// no error means success
		irodsSession.closeSession();

	}

}
