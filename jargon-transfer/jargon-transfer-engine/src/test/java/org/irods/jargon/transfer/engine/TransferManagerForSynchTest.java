package org.irods.jargon.transfer.engine;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.datautils.tree.FileTreeDiffUtility;
import org.irods.jargon.datautils.tree.FileTreeDiffUtilityImpl;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.transfer.dao.domain.FrequencyType;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.irods.jargon.transfer.dao.domain.SynchronizationType;
import org.irods.jargon.transfer.dao.domain.TransferState;
import org.irods.jargon.transfer.engine.synch.SynchManagerService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TransferManagerForSynchTest {

	private static Properties testingProperties = new Properties();

	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;

	public static final String IRODS_TEST_SUBDIR_PATH = "TransferManagerForSynchTest";

	private static IRODSFileSystem irodsFileSystem;

	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;

	private static IRODSAccount testingIRODSAccount = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(
				testingProperties);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
		testingIRODSAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		/**
		 * String testDatabase = scratchFileUtils
		 * .createAndReturnAbsoluteScratchPath
		 * (".idrop/derby/target/database/transfer"); String databaseUrl =
		 * "jdbc:derby:" + testDatabase;
		 * DatabasePreparationUtils.clearAllDatabaseForTesting(databaseUrl,
		 * "transfer", "transfer"); irodsFileSystem =
		 * IRODSFileSystem.instance(); testingIRODSAccount =
		 * testingPropertiesHelper
		 * .buildIRODSAccountFromTestProperties(testingProperties);
		 */

	}

	/**
	 * @throws Exception
	 */
	@Before
	public void setUpEach() throws Exception {

	}

	@Test
	public void testEnqueueSynch() throws Exception {
		TransferManager transferManager = new TransferManagerImpl(
				IRODSFileSystem.instance(), IRODS_TEST_SUBDIR_PATH);

		String rootCollection = "testEnqueueSynch";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ rootCollection);
		IRODSFile irodsSynchFile = irodsFileSystem.getIRODSFileFactory(
				testingIRODSAccount)
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		irodsSynchFile.mkdirs();

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath, "testSubdir", 1, 2, 1,
						"testFile", ".txt", 3, 2, 2, 5);

		GridAccount gridAccount = transferManager.getGridAccountService()
				.addOrUpdateGridAccountBasedOnIRODSAccount(testingIRODSAccount);

		SynchManagerService synchManagerService = transferManager
				.getTransferServiceFactory().instanceSynchManagerService();

		synchManagerService.purgeAllSynchronizations();

		Synchronization synchronization = new Synchronization();
		synchronization
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchronization.setName(rootCollection);
		synchronization.setCreatedAt(new Date());
		synchronization.setFrequencyType(FrequencyType.EVERY_HOUR);
		synchronization.setIrodsSynchDirectory(irodsCollectionRootAbsolutePath);
		synchronization.setLocalSynchDirectory(localCollectionAbsolutePath);
		synchronization.setGridAccount(gridAccount);

		synchManagerService.createNewSynchConfiguration(synchronization);

		synchronization = synchManagerService.findByName(rootCollection);
		transferManager.purgeAllTransfers();
		transferManager.enqueueASynch(synchronization, gridAccount);

		// let synch run

		int waitCtr = 0;

		while (true) {
			if (waitCtr++ > 30) {
				Assert.fail("synch timed out");
			}
			Thread.sleep(1000);
			if (transferManager.getRunningStatus() == TransferManager.RunningStatus.IDLE) {
				break;
			}

		}

		Assert.assertEquals("should have been no errors",
				TransferManager.ErrorStatus.OK,
				transferManager.getErrorStatus());
		FileTreeDiffUtility fileTreeDiffUtility = new FileTreeDiffUtilityImpl(
				testingIRODSAccount,
				irodsFileSystem.getIRODSAccessObjectFactory());
		boolean noDiffs = fileTreeDiffUtility.verifyLocalAndIRODSTreesMatch(
				new File(localCollectionAbsolutePath),
				irodsCollectionRootAbsolutePath, 0L, 0L);

		Assert.assertTrue("diffs found after synch", noDiffs);

		List<LocalIRODSTransfer> transfers = transferManager.getRecentQueue();
		Assert.assertNotNull("no transfers in queue", transfers);
		Assert.assertEquals("expected one transfer", 1, transfers.size());
		LocalIRODSTransfer actualTransfer = transfers.get(0);
		Assert.assertEquals("did not set to complete", TransferState.COMPLETE,
				actualTransfer.getTransferState());

	}

	@Test
	public void testEnqueueSynchTwice() throws Exception {
		TransferManager transferManager = new TransferManagerImpl(
				IRODSFileSystem.instance(), IRODS_TEST_SUBDIR_PATH);

		String rootCollection = "testEnqueueSynchTwice";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ rootCollection);
		IRODSFile irodsSynchFile = irodsFileSystem.getIRODSFileFactory(
				testingIRODSAccount)
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		irodsSynchFile.mkdirs();

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath, "testSubdir", 1, 2, 1,
						"testFile", ".txt", 3, 2, 2, 5);

		GridAccount gridAccount = transferManager.getGridAccountService()
				.addOrUpdateGridAccountBasedOnIRODSAccount(testingIRODSAccount);

		Synchronization synchronization = new Synchronization();
		synchronization
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchronization.setName(rootCollection);
		synchronization.setCreatedAt(new Date());
		synchronization.setFrequencyType(FrequencyType.EVERY_HOUR);
		synchronization.setIrodsSynchDirectory(irodsCollectionRootAbsolutePath);
		synchronization.setGridAccount(gridAccount);
		synchronization.setLocalSynchDirectory(localCollectionAbsolutePath);
		SynchManagerService synchManagerService = transferManager
				.getTransferServiceFactory().instanceSynchManagerService();
		synchManagerService.createNewSynchConfiguration(synchronization);
		synchronization = synchManagerService.findByName(rootCollection);

		transferManager.purgeAllTransfers();
		transferManager.enqueueASynch(synchronization, gridAccount);
		transferManager.enqueueASynch(synchronization, gridAccount);

		// let synch run

		int waitCtr = 0;

		while (true) {
			if (waitCtr++ > 30) {
				Assert.fail("synch timed out");
			}
			Thread.sleep(1000);
			if (transferManager.getRunningStatus() == TransferManager.RunningStatus.IDLE) {
				break;
			}

		}

		/*
		 * Assert.assertEquals("should have been no errors",
		 * TransferManager.ErrorStatus.OK, transferManager.getErrorStatus());
		 */
		FileTreeDiffUtility fileTreeDiffUtility = new FileTreeDiffUtilityImpl(
				testingIRODSAccount,
				irodsFileSystem.getIRODSAccessObjectFactory());
		boolean noDiffs = fileTreeDiffUtility.verifyLocalAndIRODSTreesMatch(
				new File(localCollectionAbsolutePath),
				irodsCollectionRootAbsolutePath, 0L, 0L);

		Assert.assertTrue("diffs found after synch", noDiffs);

		// make sure only one synch with this name

		List<Synchronization> synchronizations = synchManagerService
				.listAllSynchronizations();
		int synchCount = 0;

		for (Synchronization actualSynchronization : synchronizations) {
			if (actualSynchronization.getName().equals(rootCollection)) {
				synchCount++;
			}
		}

		Assert.assertEquals("found more than one synch with a given name", 1,
				synchCount);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testEnqueueSynchNullSynch() throws Exception {
		TransferManager transferManager = new TransferManagerImpl(
				IRODSFileSystem.instance(), IRODS_TEST_SUBDIR_PATH);

		GridAccount gridAccount = transferManager.getGridAccountService()
				.addOrUpdateGridAccountBasedOnIRODSAccount(testingIRODSAccount);

		transferManager.enqueueASynch(null, gridAccount);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testEnqueueSynchNullAccount() throws Exception {
		TransferManager transferManager = new TransferManagerImpl(
				IRODSFileSystem.instance(), IRODS_TEST_SUBDIR_PATH);

		transferManager.enqueueASynch(new Synchronization(), null);

	}

}
