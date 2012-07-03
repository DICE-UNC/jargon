package org.irods.jargon.core.pub;

import java.io.File;
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
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataObjectAuditAOImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "DataObjectAuditAOImplTest";
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
	public void testFindAllAuditRecordsForDataObject() throws Exception {

		if (!testingPropertiesHelper.isTestAudit(testingProperties)) {
			return;
		}

		String testFileName = System.currentTimeMillis()
				+ "testFindAllAuditRecordsForDataObject.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, true);

		// get the audit data for this file

		DataObjectAuditAO dataObjectAuditAO = accessObjectFactory
				.getDataObjectAuditAO(irodsAccount);
		List<AuditedAction> auditData = dataObjectAuditAO
				.findAllAuditRecordsForDataObject(destFile, 0, 1000);
		Assert.assertFalse("empty audit data", auditData.isEmpty());

		AuditedAction action = auditData.get(0);

		Assert.assertEquals("did not set data name",
				destFile.getAbsolutePath(), action.getDomainObjectUniqueName());
		Assert.assertNotNull("did not set audit enum",
				action.getAuditActionEnum());

	}

	@Test(expected = FileNotFoundException.class)
	public void testFindAllAuditRecordsForDataObjectNotExists()
			throws Exception {

		if (!testingPropertiesHelper.isTestAudit(testingProperties)) {
			return;
		}

		String testFileName = System.currentTimeMillis()
				+ "testFindAllAuditRecordsForDataObject.txt";

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

		// get the audit data for this file

		DataObjectAuditAO dataObjectAuditAO = accessObjectFactory
				.getDataObjectAuditAO(irodsAccount);
		dataObjectAuditAO.findAllAuditRecordsForDataObject(destFile, 0, 1000);

	}

	@Test
	public void testFindAuditRecordForDataObject() throws Exception {

		if (!testingPropertiesHelper.isTestAudit(testingProperties)) {
			return;
		}

		String testFileName = System.currentTimeMillis()
				+ "testFindAuditRecordForDataObject.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, true);

		// get the audit data for this file

		DataObjectAuditAO dataObjectAuditAO = accessObjectFactory
				.getDataObjectAuditAO(irodsAccount);
		List<AuditedAction> auditData = dataObjectAuditAO
				.findAllAuditRecordsForDataObject(destFile, 0, 1000);
		TestCase.assertFalse("empty audit data", auditData.isEmpty());

		AuditedAction expected = auditData.get(0);

		// now find that audit record directly and match

		AuditedAction actual = dataObjectAuditAO.getAuditedActionForDataObject(
				destFile,
				String.valueOf(expected.getAuditActionEnum().getAuditCode()),
				expected.getTimeStampInIRODSFormat());
		// really if no data not found exception we're good
		TestCase.assertNotNull("did not get audit object", actual);

	}

	@Test(expected = FileNotFoundException.class)
	public void testFindAuditRecordForDataObjectNotExists() throws Exception {

		if (!testingPropertiesHelper.isTestAudit(testingProperties)) {
			return;
		}

		String testFileName = System.currentTimeMillis()
				+ "testFindAuditRecordForDataObject.txt";

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

		// get the audit data for this file

		DataObjectAuditAO dataObjectAuditAO = accessObjectFactory
				.getDataObjectAuditAO(irodsAccount);

		dataObjectAuditAO.getAuditedActionForDataObject(
				destFile, "99999", "9999");

	}

	@Test(expected = IllegalArgumentException.class)
	public void testFindAuditRecordForDataObjectNullIrodsFile()
			throws Exception {

		if (!testingPropertiesHelper.isTestAudit(testingProperties)) {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		// get the audit data for this file

		DataObjectAuditAO dataObjectAuditAO = accessObjectFactory
				.getDataObjectAuditAO(irodsAccount);

		// I don't know if I like this, picking max val to make sure there is no
		// record..
		dataObjectAuditAO.getAuditedActionForDataObject(null,
 "9", "9");

	}

}
