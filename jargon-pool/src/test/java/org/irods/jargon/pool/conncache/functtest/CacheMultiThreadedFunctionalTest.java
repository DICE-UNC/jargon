/**
 * 
 */
package org.irods.jargon.pool.conncache.functtest;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryImpl;
import org.irods.jargon.pool.conncache.CachedIrodsProtocolManager;
import org.irods.jargon.pool.conncache.JargonConnectionCache;
import org.irods.jargon.pool.conncache.JargonKeyedPoolConfig;
import org.irods.jargon.pool.conncache.JargonPooledObjectFactory;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Functional test will run a multi-threaded client exercising pooled
 * connections
 * 
 * @author conwaymc
 *
 */
public class CacheMultiThreadedFunctionalTest {

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
	public void testMultiThreadedA() throws Exception {
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
		IRODSAccount test2 = testingPropertiesHelper.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSAccount test3 = testingPropertiesHelper.buildIRODSAccountFromTertiaryTestProperties(testingProperties);

		List<IRODSAccount> accounts = new ArrayList<IRODSAccount>();
		accounts.add(test1);
		accounts.add(test2);
		accounts.add(test3);
		accounts.add(test1);
		accounts.add(test2);
		accounts.add(test3);

		IRODSAccessObjectFactory iaf = new IRODSAccessObjectFactoryImpl(irodsSession);
		ExecutorService executor = Executors.newFixedThreadPool(10);
		// List<Future<PoolCallResult>> list = new ArrayList<Future<PoolCallResult>>();
		List<PoolValidatorClient> clients = new ArrayList<PoolValidatorClient>();
		Random rand = new Random();

		int count = 500;
		int actIdx;
		for (int i = 0; i < count; i++) {
			actIdx = rand.nextInt(accounts.size());
			clients.add(new PoolValidatorClient(accounts.get(actIdx), iaf));
		}

		executor.invokeAll(clients).stream().map(future -> {
			try {
				return future.get();
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}).forEach(System.out::println);

	}

}
