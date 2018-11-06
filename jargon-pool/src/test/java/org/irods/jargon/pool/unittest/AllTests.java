package org.irods.jargon.pool.unittest;

import org.irods.jargon.pool.conncache.CachedIrodsProtocolManagerTest;
import org.irods.jargon.pool.conncache.ConnectionPoolingProtocolManagerBootstrapperTest;
import org.irods.jargon.pool.conncache.JargonConnectionCacheTest;
import org.irods.jargon.pool.conncache.functtest.CacheMultiThreadedFunctionalTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ JargonConnectionCacheTest.class, CachedIrodsProtocolManagerTest.class,
		CacheMultiThreadedFunctionalTest.class, ConnectionPoolingProtocolManagerBootstrapperTest.class })
public class AllTests {

}
