package org.irods.jargon.core.pub;

import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.packinstr.DataObjInpForFileLock.LockType;
import org.irods.jargon.core.pub.domain.FileLock;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileLockManagerAOImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "FileLockManagerAOImplTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
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
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testObtainFileWriteLockWithoutWaitThenAttemptAsDifferentUser()
			throws Exception {
		String testFileName = "testObtainFileWriteLockWithoutWaitThenAttemptAsDifferentUser.dat";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccount secondaryIrodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsFile, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null,
				null);

		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		dataObjectAO.setAccessPermissionWrite(irodsAccount.getZone(),
				targetIrodsFile, secondaryIrodsAccount.getUserName());

		FileLockManagerAO fileLockAO = accessObjectFactory
				.getFileLockManagerAO(irodsAccount);

		FileLock fileLock = fileLockAO.obtainFileLockWithoutWait(
				targetIrodsFile, LockType.WRITE_LOCK);
		Assert.assertNotNull("null fileLock", fileLock);
		Assert.assertEquals(targetIrodsFile, fileLock.getIrodsAbsolutePath());
		Assert.assertFalse("did not set fd", fileLock.getFd() == 0);
		Assert.assertEquals("did not set lock type", LockType.WRITE_LOCK,
				fileLock.getLockType());

		FileLockManagerAO secondaryFileLockAO = accessObjectFactory
				.getFileLockManagerAO(secondaryIrodsAccount);
		fileLock = secondaryFileLockAO.obtainFileLockWithoutWait(
				targetIrodsFile, LockType.WRITE_LOCK);

	}

	@Test
	public void testObtainFileWriteLockWithoutWaitNonRodsAdmin()
			throws Exception {
		String testFileName = "testObtainFileWriteLockWithoutWaitNonRodsAdmin.dat";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromSecondaryTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsFile, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null,
				null);

		FileLockManagerAO fileLockAO = accessObjectFactory
				.getFileLockManagerAO(irodsAccount);

		FileLock fileLock = fileLockAO.obtainFileLockWithoutWait(
				targetIrodsFile, LockType.WRITE_LOCK);
		Assert.assertNotNull("null fileLock", fileLock);
		Assert.assertEquals(targetIrodsFile, fileLock.getIrodsAbsolutePath());
		Assert.assertFalse("did not set fd", fileLock.getFd() == 0);
		Assert.assertEquals("did not set lock type", LockType.WRITE_LOCK,
				fileLock.getLockType());

	}

	@Test
	public void testObtainFileWriteLockWithoutWait() throws Exception {
		String testFileName = "testObtainFileLockWithoutWait.dat";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsFile, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null,
				null);

		FileLockManagerAO fileLockAO = accessObjectFactory
				.getFileLockManagerAO(irodsAccount);

		FileLock fileLock = fileLockAO.obtainFileLockWithoutWait(
				targetIrodsFile, LockType.WRITE_LOCK);
		Assert.assertNotNull("null fileLock", fileLock);
		Assert.assertEquals(targetIrodsFile, fileLock.getIrodsAbsolutePath());
		Assert.assertFalse("did not set fd", fileLock.getFd() == 0);
		Assert.assertEquals("did not set lock type", LockType.WRITE_LOCK,
				fileLock.getLockType());

	}

	@Test
	public void testObtainFileWriteLockNotExists() throws Exception {
		String testFileName = "testObtainFileLockNotExists.dat";

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		FileLockManagerAO fileLockAO = accessObjectFactory
				.getFileLockManagerAO(irodsAccount);

		FileLock fileLock = fileLockAO.obtainFileLockWithoutWait(
				targetIrodsFile, LockType.WRITE_LOCK);
		Assert.assertNotNull("null fileLock", fileLock);

	}

}
