package org.irods.jargon.transferengine.unittest;

import org.irods.jargon.transferengine.TestIRODSLocalTransferEngineTest;
import org.irods.jargon.transferengine.TransferManagerTest;
import org.irods.jargon.transferengine.TransferQueueServiceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ TestIRODSLocalTransferEngineTest.class,
		TransferManagerTest.class, TransferQueueServiceTest.class })
public class AllTests {

}
