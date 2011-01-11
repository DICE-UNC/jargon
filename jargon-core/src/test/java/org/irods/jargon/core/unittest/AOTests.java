package org.irods.jargon.core.unittest;

import org.irods.jargon.core.pub.BulkFileOperationsAOImplTest;
import org.irods.jargon.core.pub.CollectionAOImplTest;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAOImplTest;
import org.irods.jargon.core.pub.DataObjectAOImplTest;
import org.irods.jargon.core.pub.DataTransferOperationsImplTest;
import org.irods.jargon.core.pub.EnvironmentalInfoAOTest;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryImplTest;
import org.irods.jargon.core.pub.IRODSGenQueryExecutorImplTest;
import org.irods.jargon.core.pub.RODSFileSystemTest;
import org.irods.jargon.core.pub.RemoteExecutionOfCommandsAOImplTest;
import org.irods.jargon.core.pub.ResourceAOTest;
import org.irods.jargon.core.pub.RuleProcessingAOImplTest;
import org.irods.jargon.core.pub.UserAOTest;
import org.irods.jargon.core.pub.UserGroupAOImplTest;
import org.irods.jargon.core.pub.ZoneAOTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ IRODSGenQueryExecutorImplTest.class,
		EnvironmentalInfoAOTest.class, ResourceAOTest.class, UserAOTest.class,
		ZoneAOTest.class, UserGroupAOImplTest.class,
		CollectionAOImplTest.class, DataObjectAOImplTest.class,
		RuleProcessingAOImplTest.class, RODSFileSystemTest.class,
		DataTransferOperationsImplTest.class,
		RemoteExecutionOfCommandsAOImplTest.class,
		CollectionAndDataObjectListAndSearchAOImplTest.class,
		IRODSAccessObjectFactoryImplTest.class,
		BulkFileOperationsAOImplTest.class })
public class AOTests {

}
