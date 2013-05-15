package org.irods.jargon.conveyor.unittest;

import org.irods.jargon.conveyor.basic.BasicConveyorBootstrapperImplTest;
import org.irods.jargon.conveyor.basic.BasicConveyorServiceTest;
import org.irods.jargon.conveyor.basic.BasicQueueManagerServiceImplTest;
import org.irods.jargon.conveyor.basic.ConfigurationServiceImplTest;
import org.irods.jargon.conveyor.basic.GridAccountServiceImplTest;
import org.irods.jargon.conveyor.basic.TransferAccountingManagementServiceImplTest;
import org.irods.jargon.conveyor.core.ConveyorExecutorServiceImplTest;
import org.irods.jargon.conveyor.gridaccount.GridAccountConfigurationProcessorTest;
import org.irods.jargon.transfer.dao.ConfigurationPropertyDAOTest;
import org.irods.jargon.transfer.dao.GridAccountDAOImplTest;
import org.irods.jargon.transfer.dao.KeyStoreDAOImplTest;
import org.irods.jargon.transfer.dao.TransferDAOTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({

ConfigurationPropertyDAOTest.class, GridAccountDAOImplTest.class,
		KeyStoreDAOImplTest.class, TransferDAOTest.class,
		ConveyorExecutorServiceImplTest.class,
		BasicConveyorBootstrapperImplTest.class,
		GridAccountServiceImplTest.class, BasicConveyorServiceTest.class,
		GridAccountConfigurationProcessorTest.class,
		ConfigurationServiceImplTest.class,
		TransferAccountingManagementServiceImplTest.class,
		BasicQueueManagerServiceImplTest.class, })
/**
 * Suite to run all tests (except long running and functional), further refined by settings in testing.properites.  Some subtests may be shut
 * off by these properties.
 */
public class AllTests {

}
