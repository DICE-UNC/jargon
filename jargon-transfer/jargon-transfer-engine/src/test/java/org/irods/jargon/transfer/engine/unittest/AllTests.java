package org.irods.jargon.transfer.engine.unittest;

import org.irods.jargon.transfer.engine.TestIRODSLocalTransferEngineTest;
import org.irods.jargon.transfer.engine.TransferManagerForSynchTest;
import org.irods.jargon.transfer.engine.TransferManagerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ TestIRODSLocalTransferEngineTest.class,
		TransferManagerTest.class, TransferManagerForSynchTest.class,
		 })
public class AllTests {

}
