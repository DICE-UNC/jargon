package org.irods.jargon.pool.unittest;

import org.irods.jargon.pool.conncache.CachedIrodsProtocolManagerTest;
import org.irods.jargon.pool.conncache.JargonConnectionCacheTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ JargonConnectionCacheTest.class, CachedIrodsProtocolManagerTest.class })
public class AllTests {

}
