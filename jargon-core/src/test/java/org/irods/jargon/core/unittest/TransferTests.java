package org.irods.jargon.core.unittest;

import org.irods.jargon.core.transfer.AesCipherWrapperTest;
import org.irods.jargon.core.transfer.DefaultTransferControlBlockTest;
import org.irods.jargon.core.transfer.EncryptionWrapperFactoryTest;
import org.irods.jargon.core.transfer.TransferStatusTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ TransferStatusTest.class,
		DefaultTransferControlBlockTest.class,
		EncryptionWrapperFactoryTest.class, AesCipherWrapperTest.class })
public class TransferTests {

}
