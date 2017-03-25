package org.irods.jargon.pool.conncache;

import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryImpl;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.BeforeClass;
import org.junit.Test;

public class CachedIrodsProtocolManagerTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	public static final String IRODS_TEST_SUBDIR_PATH = "JargonConnectionCacheTest";
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
	public void simpleTestBorrowAndReturn() throws Exception {

		JargonKeyedPoolConfig config = new JargonKeyedPoolConfig();
		JargonPooledObjectFactory jargonPooledObjectFactory = new JargonPooledObjectFactory();
		IRODSSimpleProtocolManager irodsSimpleProtocolManager = new IRODSSimpleProtocolManager();
		jargonPooledObjectFactory.setIrodsSimpleProtocolManager(irodsSimpleProtocolManager);
		JargonConnectionCache jargonConnectionCache = new JargonConnectionCache(jargonPooledObjectFactory, config);

		CachedIrodsProtocolManager cachedIrodsProtocolManager = new CachedIrodsProtocolManager();
		cachedIrodsProtocolManager.setJargonConnectionCache(jargonConnectionCache);

		IRODSSession irodsSession = IRODSSession.instance(cachedIrodsProtocolManager);
		jargonPooledObjectFactory.setIrodsSession(irodsSession);
		IRODSAccount test1 = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory iaf = new IRODSAccessObjectFactoryImpl(irodsSession);

		EnvironmentalInfoAO environmentalInfoAO = iaf.getEnvironmentalInfoAO(test1);
		environmentalInfoAO.getIRODSServerPropertiesFromIRODSServer();
		iaf.closeSession();
		jargonConnectionCache.close();

	}

}
