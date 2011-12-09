package org.irods.jargon.core.unittest;

import org.irods.jargon.core.pub.BulkFileOperationsAOImplTest;
import org.irods.jargon.core.pub.CollectionAOImplTest;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAOImplTest;
import org.irods.jargon.core.pub.DataObjectAOImplTest;
import org.irods.jargon.core.pub.DataObjectAuditAOImplTest;
import org.irods.jargon.core.pub.DataTransferOperationsImplTest;
import org.irods.jargon.core.pub.EnvironmentalInfoAOTest;
import org.irods.jargon.core.pub.FederatedCollectionAndDataObjectListAndSearchAOImplTest;
import org.irods.jargon.core.pub.FederatedDataTransferOperationsImplTest;
import org.irods.jargon.core.pub.FederatedIRODSGenQueryExecutorImplTest;
import org.irods.jargon.core.pub.FederatedUserAOTest;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryImplTest;
import org.irods.jargon.core.pub.IRODSFileSystemTest;
import org.irods.jargon.core.pub.IRODSGenQueryExecutorImplTest;
import org.irods.jargon.core.pub.QuotaAOImplTest;
import org.irods.jargon.core.pub.RemoteExecutionOfCommandsAOImplTest;
import org.irods.jargon.core.pub.ResourceAOTest;
import org.irods.jargon.core.pub.RuleProcessingAOImplTest;
import org.irods.jargon.core.pub.SimpleQueryExecutorAOImplTest;
import org.irods.jargon.core.pub.Stream2StreamAOImplTest;
import org.irods.jargon.core.pub.UserAOTest;
import org.irods.jargon.core.pub.UserGroupAOImplTest;
import org.irods.jargon.core.pub.ZoneAOTest;
import org.irods.jargon.core.pub.aohelper.UserAOHelperTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ IRODSGenQueryExecutorImplTest.class,
		EnvironmentalInfoAOTest.class, ResourceAOTest.class, UserAOTest.class,
		ZoneAOTest.class, UserGroupAOImplTest.class,
		CollectionAOImplTest.class, DataObjectAOImplTest.class,
		RuleProcessingAOImplTest.class, IRODSFileSystemTest.class,
		DataTransferOperationsImplTest.class,
		RemoteExecutionOfCommandsAOImplTest.class,
		CollectionAndDataObjectListAndSearchAOImplTest.class,
		IRODSAccessObjectFactoryImplTest.class,
		BulkFileOperationsAOImplTest.class,
		SimpleQueryExecutorAOImplTest.class, Stream2StreamAOImplTest.class,
		QuotaAOImplTest.class, DataObjectAuditAOImplTest.class,
		FederatedCollectionAndDataObjectListAndSearchAOImplTest.class,
		FederatedIRODSGenQueryExecutorImplTest.class,
		FederatedDataTransferOperationsImplTest.class, UserAOHelperTest.class,
		FederatedUserAOTest.class })
/**
 * Suite to run Access Object tests in org.irods.jargon.pub.*
 * <p/>
 * Note that 'Federated*' tests will run, but a check prevents the test from actually doing anything unless the federated zone
 * has been set up per the test-scripts/fedTestSetup.txt file
 */
public class AOTests {

}
