package org.irods.jargon.pool.conncache;

import java.util.Properties;

import org.irods.jargon.core.connection.AbstractIRODSMidLevelProtocol;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.junit.Assert;

public class JargonConnectionCacheTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	public static final String IRODS_TEST_SUBDIR_PATH = "JargonConnectionCacheTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testBorrowObjectK() throws Exception {
		JargonKeyedPoolConfig config = new JargonKeyedPoolConfig();
		JargonPooledObjectFactory jargonPooledObjectFactory = new JargonPooledObjectFactory();
		jargonPooledObjectFactory.setIrodsSession(irodsFileSystem.getIrodsSession());
		jargonPooledObjectFactory.setIrodsSimpleProtocolManager(irodsFileSystem.getIrodsProtocolManager());

		JargonConnectionCache jargonConnectionCache = new JargonConnectionCache(jargonPooledObjectFactory, config);

		CachedIrodsProtocolManager cachedIrodsProtocolManager = new CachedIrodsProtocolManager();
		cachedIrodsProtocolManager.setJargonConnectionCache(jargonConnectionCache);

		IRODSAccount test1 = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		AbstractIRODSMidLevelProtocol test1Protocol1 = jargonConnectionCache.borrowObject(test1);
		jargonConnectionCache.returnObject(test1, test1Protocol1);

		test1Protocol1 = jargonConnectionCache.borrowObject(test1);
		jargonConnectionCache.returnObject(test1, test1Protocol1);

		test1Protocol1 = jargonConnectionCache.borrowObject(test1);
		jargonConnectionCache.returnObject(test1, test1Protocol1);
		long borrowed = jargonConnectionCache.getBorrowedCount();
		long returned = jargonConnectionCache.getReturnedCount();
		long active = jargonConnectionCache.getNumActive();

		Assert.assertEquals(3, borrowed);
		Assert.assertEquals(3, returned);
		Assert.assertEquals(0, active);

		jargonConnectionCache.close();
		long idle = jargonConnectionCache.getNumIdle();

		Assert.assertEquals(0, idle);

	}

	@Test
	public void testBorrowAndReturnLoop() throws Exception {
		JargonKeyedPoolConfig config = new JargonKeyedPoolConfig();
		JargonPooledObjectFactory jargonPooledObjectFactory = new JargonPooledObjectFactory();
		jargonPooledObjectFactory.setIrodsSession(irodsFileSystem.getIrodsSession());
		jargonPooledObjectFactory.setIrodsSimpleProtocolManager(irodsFileSystem.getIrodsProtocolManager());

		JargonConnectionCache jargonConnectionCache = new JargonConnectionCache(jargonPooledObjectFactory, config);

		CachedIrodsProtocolManager cachedIrodsProtocolManager = new CachedIrodsProtocolManager();
		cachedIrodsProtocolManager.setJargonConnectionCache(jargonConnectionCache);

		IRODSAccount test1 = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccount test2 = testingPropertiesHelper.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSAccount test3 = testingPropertiesHelper.buildIRODSAccountFromTertiaryTestProperties(testingProperties);

		AbstractIRODSMidLevelProtocol test1Protocol1;
		AbstractIRODSMidLevelProtocol test1Protocol2;
		AbstractIRODSMidLevelProtocol test1Protocol3;

		int iters = 100;

		for (int i = 0; i < iters; i++) {
			test1Protocol1 = jargonConnectionCache.borrowObject(test1);
			jargonConnectionCache.returnObject(test1, test1Protocol1);
			test1Protocol2 = jargonConnectionCache.borrowObject(test2);
			jargonConnectionCache.returnObject(test2, test1Protocol2);
			test1Protocol3 = jargonConnectionCache.borrowObject(test3);
			jargonConnectionCache.returnObject(test3, test1Protocol3);
		}

		long borrowed = jargonConnectionCache.getBorrowedCount();
		long returned = jargonConnectionCache.getReturnedCount();

		jargonConnectionCache.close();

		Assert.assertEquals(iters * 3, borrowed);
		Assert.assertEquals(iters * 3, returned);

	}

	@Test
	public void testBorrowAndReturnLoopMultiplePerKey() throws Exception {
		JargonKeyedPoolConfig config = new JargonKeyedPoolConfig();
		JargonPooledObjectFactory jargonPooledObjectFactory = new JargonPooledObjectFactory();
		jargonPooledObjectFactory.setIrodsSession(irodsFileSystem.getIrodsSession());
		jargonPooledObjectFactory.setIrodsSimpleProtocolManager(irodsFileSystem.getIrodsProtocolManager());

		JargonConnectionCache jargonConnectionCache = new JargonConnectionCache(jargonPooledObjectFactory, config);

		CachedIrodsProtocolManager cachedIrodsProtocolManager = new CachedIrodsProtocolManager();
		cachedIrodsProtocolManager.setJargonConnectionCache(jargonConnectionCache);

		IRODSAccount test1 = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccount test2 = testingPropertiesHelper.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSAccount test3 = testingPropertiesHelper.buildIRODSAccountFromTertiaryTestProperties(testingProperties);

		AbstractIRODSMidLevelProtocol test1Protocol1a;
		AbstractIRODSMidLevelProtocol test1Protocol1b;
		AbstractIRODSMidLevelProtocol test1Protocol1c;

		AbstractIRODSMidLevelProtocol test1Protocol2a;
		AbstractIRODSMidLevelProtocol test1Protocol2b;
		AbstractIRODSMidLevelProtocol test1Protocol2c;
		AbstractIRODSMidLevelProtocol test1Protocol2d;

		AbstractIRODSMidLevelProtocol test1Protocol3;

		int iters = 300;

		for (int i = 0; i < iters; i++) {
			test1Protocol1a = jargonConnectionCache.borrowObject(test1);
			test1Protocol1b = jargonConnectionCache.borrowObject(test1);
			test1Protocol1c = jargonConnectionCache.borrowObject(test1);

			jargonConnectionCache.returnObject(test1, test1Protocol1a);
			jargonConnectionCache.returnObject(test1, test1Protocol1c);
			jargonConnectionCache.returnObject(test1, test1Protocol1b);

			test1Protocol2a = jargonConnectionCache.borrowObject(test2);
			test1Protocol2b = jargonConnectionCache.borrowObject(test2);
			jargonConnectionCache.returnObject(test2, test1Protocol2a);

			test1Protocol2c = jargonConnectionCache.borrowObject(test2);
			test1Protocol2d = jargonConnectionCache.borrowObject(test2);
			jargonConnectionCache.returnObject(test2, test1Protocol2b);
			jargonConnectionCache.returnObject(test2, test1Protocol2d);
			jargonConnectionCache.returnObject(test2, test1Protocol2c);

			test1Protocol3 = jargonConnectionCache.borrowObject(test3);
			jargonConnectionCache.returnObject(test3, test1Protocol3);
		}

		long borrowed = jargonConnectionCache.getBorrowedCount();
		long returned = jargonConnectionCache.getReturnedCount();

		jargonConnectionCache.close();

		Assert.assertEquals(borrowed, returned);

	}

}
