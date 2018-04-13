package org.irods.jargon.core.unittest;

import org.irods.jargon.core.nio.provider.IrodsFileSystemProviderTest;
import org.irods.jargon.core.nio.provider.IrodsNioFileSystemTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ IrodsNioFileSystemTest.class, IrodsFileSystemProviderTest.class })
public class NioTests {

}
