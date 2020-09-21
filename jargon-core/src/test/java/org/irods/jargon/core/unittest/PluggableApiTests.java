package org.irods.jargon.core.unittest;

import org.irods.jargon.core.pub.apiplugin.domain.AtomicMetadataInputTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ AtomicMetadataInputTest.class })

/**
 * Suite to run tests on parts of the pluggable api support. These are only
 * executed if the target iRODS is > 4.2.8
 */
public class PluggableApiTests {

}
