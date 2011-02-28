package org.irods.jargon.transfer.engine.unittest;

import org.irods.jargon.transfer.engine.TestIRODSLocalTransferEngineTest;
import org.irods.jargon.transfer.engine.TransferManagerTest;
import org.irods.jargon.transfer.engine.TransferQueueServiceTest;
import org.irods.jargon.transfer.engine.synch.SynchronizeProcessorImplTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ TestIRODSLocalTransferEngineTest.class, TransferManagerTest.class,
        TransferQueueServiceTest.class, SynchronizeProcessorImplTest.class })
public class AllTests {

}
