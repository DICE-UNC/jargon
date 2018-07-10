package org.irods.jargon.core.unittest;

import org.irods.jargon.core.pub.ParallelTransferOperationsTest;
import org.irods.jargon.core.pub.io.FileListingAndRecursiveGetReplicateTestingWithBigCollectionTest;
import org.irods.jargon.core.unittest.functionaltest.EncryptedTransferTests;
import org.irods.jargon.core.unittest.functionaltest.IRODSCommandsFunctionalTest;
import org.irods.jargon.core.unittest.functionaltest.IRODSTenThousandCollectionsTest;
import org.irods.jargon.core.unittest.functionaltest.IRODSThousandCollectionsTest;
import org.irods.jargon.core.unittest.functionaltest.IRODSThousandFilesTest;
import org.irods.jargon.core.unittest.functionaltest.SslNegotiationFunctionalTests;
import org.irods.jargon.core.unittest.functionaltest.StreamOpsLazyWalkTest;
import org.irods.jargon.core.unittest.functionaltest.TestBug38GetFileFromSoftLinkedPublicCollection;
import org.irods.jargon.core.unittest.functionaltest.TestParallelTransferToIcatRerouteRemoteBug132;
import org.irods.jargon.core.unittest.functionaltest.pep.IRODSFileOutputStreamPEPFunctionalTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ AllTests.class, IRODSThousandFilesTest.class, IRODSThousandCollectionsTest.class,
		FileListingAndRecursiveGetReplicateTestingWithBigCollectionTest.class, ParallelTransferOperationsTest.class,
		IRODSCommandsFunctionalTest.class, IRODSTenThousandCollectionsTest.class,
		TestBug38GetFileFromSoftLinkedPublicCollection.class, TestParallelTransferToIcatRerouteRemoteBug132.class,
		SslNegotiationFunctionalTests.class, IRODSFileOutputStreamPEPFunctionalTest.class, EncryptedTransferTests.class,
		StreamOpsLazyWalkTest.class })
public class AllTestIncludingLongRunningAndFunctionalTests {

}
