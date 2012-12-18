package org.irods.jargon.conveyor.unittest;

import org.irods.jargon.conveyor.basic.BasicConveyorBootstrapperImplTest;
import org.irods.jargon.conveyor.basic.GridAccountServiceImplTest;
import org.irods.jargon.conveyor.core.ConveyorExecutorServiceImplTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ ConveyorExecutorServiceImplTest.class,
		BasicConveyorBootstrapperImplTest.class, GridAccountServiceImplTest.class })
/**
 * Suite to run all tests (except long running and functional), further refined by settings in testing.properites.  Some subtests may be shut
 * off by these properties.
 */
public class AllTests {

}
