package org.irods.jargon.dataprofile.unittest;

import org.irods.jargon.dataprofile.DataProfileServiceImplTest;
import org.irods.jargon.dataprofile.DataTypeResolutionServiceImplTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ DataProfileServiceImplTest.class, DataTypeResolutionServiceImplTest.class })
public class AllTests {

}
