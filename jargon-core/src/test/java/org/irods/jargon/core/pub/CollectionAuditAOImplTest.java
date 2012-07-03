package org.irods.jargon.core.pub;

import java.util.List;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.pub.domain.AuditedAction;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class CollectionAuditAOImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "CollectionAuditAOImplTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();

		if (!testingPropertiesHelper.isTestAudit(testingProperties)) {
			return;
		}

		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {

		if (!testingPropertiesHelper.isTestAudit(testingProperties)) {
			return;
		}

		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public final void testFindAllAuditRecordsForCollection() throws Exception {

		if (!testingPropertiesHelper.isTestAudit(testingProperties)) {
			return;
		}

		String testFileName = System.currentTimeMillis()
				+ "testFindAllAuditRecordsForCollection";

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		destFile.mkdirs();

		// be another user and access this collection
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);

		// force an auditable action

		collectionAO.setAccessPermissionRead(irodsAccount.getZone(),
				destFile.getAbsolutePath(), secondaryAccount.getUserName(),
				true);

		// get the audit data for this file

		CollectionAuditAO collectionAuditAO = accessObjectFactory
				.getCollectionAuditAO(irodsAccount);
		List<AuditedAction> auditData = collectionAuditAO
				.findAllAuditRecordsForCollection(destFile, 0, 1000);
		Assert.assertFalse("empty audit data", auditData.isEmpty());

		AuditedAction action = auditData.get(0);

		Assert.assertEquals("did not set data name",
				destFile.getAbsolutePath(), action.getDomainObjectUniqueName());
		Assert.assertNotNull("did not set audit enum",
				action.getAuditActionEnum());

	}

	@Test(expected = FileNotFoundException.class)
	public final void testFindAllAuditRecordsForCollectionNotExists()
			throws Exception {

		if (!testingPropertiesHelper.isTestAudit(testingProperties)) {
			throw new FileNotFoundException("expected");
		}

		String testFileName = System.currentTimeMillis()
				+ "testFindAllAuditRecordsForCollectionNotExists";

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);

		CollectionAuditAO collectionAuditAO = accessObjectFactory
				.getCollectionAuditAO(irodsAccount);
		collectionAuditAO.findAllAuditRecordsForCollection(destFile, 0, 1000);

	}

	@Test
	public void testFindAuditRecordForCollection() throws Exception {

		if (!testingPropertiesHelper.isTestAudit(testingProperties)) {
			return;
		}

		String testFileName = System.currentTimeMillis()
				+ "testFindAuditRecordForCollection";

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		destFile.mkdirs();

		// get the audit data for this file

		CollectionAuditAO collectionAuditAO = accessObjectFactory
				.getCollectionAuditAO(irodsAccount);
		List<AuditedAction> auditData = collectionAuditAO
				.findAllAuditRecordsForCollection(destFile, 0, 1000);
		TestCase.assertFalse("empty audit data", auditData.isEmpty());

		AuditedAction expected = auditData.get(0);

		// now find that audit record directly and match

		AuditedAction actual = collectionAuditAO.getAuditedActionForCollection(
				destFile,
				String.valueOf(expected.getAuditActionEnum().getAuditCode()),
				expected.getTimeStampInIRODSFormat());
		// really if no data not found exception we're good
		TestCase.assertNotNull("did not get audit object", actual);

	}

	@Test(expected = FileNotFoundException.class)
	public void testFindAuditRecordForCollectionNotExists() throws Exception {

		if (!testingPropertiesHelper.isTestAudit(testingProperties)) {
			throw new FileNotFoundException("expected");
		}

		String testFileName = System.currentTimeMillis()
				+ "testFindAuditRecordForCollection";

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		CollectionAuditAO collectionAuditAO = accessObjectFactory
				.getCollectionAuditAO(irodsAccount);

		collectionAuditAO.getAuditedActionForCollection(
				destFile, "999999", "99999");

	}
}
