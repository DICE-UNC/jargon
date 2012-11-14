package org.irods.jargon.core.unittest;

import org.irods.jargon.core.connection.IRODSCommandsReconnectionTest;
import org.irods.jargon.core.pub.ParallelTransferOperationsTest;
import org.irods.jargon.core.pub.io.FileListingAndRecursiveGetReplicateTestingWithBigCollectionTest;
import org.irods.jargon.core.unittest.functionaltest.IRODSCommandsFunctionalTest;
import org.irods.jargon.core.unittest.functionaltest.IRODSThousandCollectionsTest;
import org.irods.jargon.core.unittest.functionaltest.IRODSThousandFilesTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ AllTests.class, IRODSThousandFilesTest.class,
		IRODSThousandCollectionsTest.class,
		FileListingAndRecursiveGetReplicateTestingWithBigCollectionTest.class,
		ParallelTransferOperationsTest.class,
		IRODSCommandsReconnectionTest.class, IRODSCommandsFunctionalTest.class})
public class AllTestIncludingLongRunningAndFunctionalTests {

}
