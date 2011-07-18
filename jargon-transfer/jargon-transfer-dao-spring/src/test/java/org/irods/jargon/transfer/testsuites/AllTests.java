package org.irods.jargon.transfer.testsuites;

import org.irods.jargon.transfer.TransferServiceFactoryImplTest;
import org.irods.jargon.transfer.dao.ConfigurationPropertyDAOTest;
import org.irods.jargon.transfer.dao.LocalIRODSTransferDAOTest;
import org.irods.jargon.transfer.dao.SynchronizationDAOTest;
import org.irods.jargon.transfer.engine.ConfigurationServiceImplTest;
import org.irods.jargon.transfer.engine.synch.SynchManagerServiceImplTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ ConfigurationPropertyDAOTest.class, LocalIRODSTransferDAOTest.class, SynchronizationDAOTest.class,
        ConfigurationServiceImplTest.class, SynchManagerServiceImplTest.class, TransferServiceFactoryImplTest.class })
public class AllTests {

}
