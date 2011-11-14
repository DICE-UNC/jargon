package org.irods.jargon.core.pub;

import java.util.List;

import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.domain.AuditedAction;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DataObjectAuditAOImplTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFindAllAuditRecordsForDataObject() throws Exception {

		/*  FIXME: implement in test rig

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		DataObjectAuditAO dataObjectAuditAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAuditAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile("/compZone/home/baretto/Alpha/test.r");
		List<AuditedAction> actions = dataObjectAuditAO.findAllAuditRecordsForDataObject(irodsFile, 0);
		TestCase.assertNotNull("no audit info returned", actions);
*/
	}

}
