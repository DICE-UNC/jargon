package org.irods.jargon.conveyor.functionaltest;

import java.io.File;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.conveyor.core.ConfigurationPropertyConstants;
import org.irods.jargon.conveyor.core.ConveyorExecutorService.RunningStatus;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.conveyor.unittest.utils.TransferTestRunningUtilities;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.packinstr.TransferOptions.ForceOption;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.transfer.dao.domain.ConfigurationProperty;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferStateEnum;
import org.irods.jargon.transfer.dao.domain.TransferStatusEnum;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:transfer-dao-beans.xml",
		"classpath:transfer-dao-hibernate-spring.cfg.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)
public class ConveyorServiceFunctionalTests {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "ConveyorServiceFunctionalTests";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;
	private static int TRANSFER_TIMEOUT = -1;

	@Autowired
	private ConveyorService conveyorService;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		irodsFileSystem = IRODSFileSystem.instance();
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(
				testingProperties);
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.clearIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
	}

	@Before
	public void setUp() throws Exception {
		conveyorService.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());
		conveyorService.getQueueManagerService().purgeAllFromQueue();
		conveyorService.getGridAccountService().resetPassPhraseAndAccounts();
		conveyorService.getConveyorExecutorService().requestResumeFromPause();

	}

	@After
	public void tearDown() throws Exception {
		conveyorService.getQueueManagerService().purgeAllFromQueue();
	}

	@Test
	public void testPutToDirectoryWhereNoPermissionsCauseFailure()
			throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		conveyorService.validatePassPhrase(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PASSWORD_KEY));
		conveyorService.getGridAccountService()
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);
		ConfigurationProperty logSuccessful = new ConfigurationProperty();
		logSuccessful
				.setPropertyKey(ConfigurationPropertyConstants.LOG_SUCCESSFUL_FILES_KEY);
		logSuccessful.setPropertyValue("true");

		conveyorService.getConfigurationService().addConfigurationProperty(
				logSuccessful);

		ConfigurationProperty maxErrors = new ConfigurationProperty();
		maxErrors
				.setPropertyKey(ConfigurationPropertyConstants.MAX_ERRORS_BEFORE_CANCEL_KEY);
		maxErrors.setPropertyValue(5);
		conveyorService.getConfigurationService().addConfigurationProperty(
				maxErrors);

		String rootCollection = "testEnqueuePutTransferOperationAndWaitUntilDone";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromSecondaryTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testEnqueuePutTransferOperationAndWaitUntilDone", 2,
						5, 2, "testFile", ".txt", 10, 5, 100, 2000);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		File localFile = new File(localCollectionAbsolutePath);

		Transfer transfer = new Transfer();
		transfer.setIrodsAbsolutePath(destFile.getAbsolutePath());
		transfer.setLocalAbsolutePath(localFile.getAbsolutePath());
		transfer.setTransferType(TransferType.PUT);

		conveyorService.getQueueManagerService().enqueueTransferOperation(
				transfer, irodsAccount);

		TransferTestRunningUtilities.waitForTransferToRunOrTimeout(
				conveyorService, TRANSFER_TIMEOUT);

		List<Transfer> transfers = conveyorService.getQueueManagerService()
				.listAllTransfersInQueue();
		Assert.assertFalse("no transfers in queue", transfers.isEmpty());
		Assert.assertEquals("should be 1 transfer..maybe test cleanup is bad",
				1, transfers.size());
		transfer = transfers.get(0);

		transfer = conveyorService.getQueueManagerService()
				.initializeGivenTransferByLoadingChildren(transfer);

		Assert.assertFalse("did not create a transfer attempt", transfer
				.getTransferAttempts().isEmpty());

		Assert.assertEquals("did not get complete status",
				TransferStateEnum.COMPLETE, transfer.getTransferState());

		TransferAttempt attempts[] = new TransferAttempt[transfer
				.getTransferAttempts().size()];
		attempts = transfer.getTransferAttempts().toArray(attempts);
		Assert.assertEquals("should be 1 attempt", 1, attempts.length);

		TransferAttempt attempt = attempts[0];
		Assert.assertNotNull("transfer attempt not persisted", attempt.getId());
		Assert.assertEquals(TransferStatusEnum.ERROR,
				attempt.getAttemptStatus());
		Assert.assertFalse("should have an error message", attempt
				.getErrorMessage().isEmpty());
	}

	@Test
	public void testPutThenPause() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		conveyorService.validatePassPhrase(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PASSWORD_KEY));
		conveyorService.getGridAccountService()
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);
		ConfigurationProperty logSuccessful = new ConfigurationProperty();
		logSuccessful
				.setPropertyKey(ConfigurationPropertyConstants.LOG_SUCCESSFUL_FILES_KEY);
		logSuccessful.setPropertyValue("true");

		conveyorService.getConfigurationService().addConfigurationProperty(
				logSuccessful);

		ConfigurationProperty maxErrors = new ConfigurationProperty();
		maxErrors
				.setPropertyKey(ConfigurationPropertyConstants.MAX_ERRORS_BEFORE_CANCEL_KEY);
		maxErrors.setPropertyValue(5);
		conveyorService.getConfigurationService().addConfigurationProperty(
				maxErrors);

		String rootCollection = "testPutThenPause";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath, "testPutThenPause", 4, 6,
						4, "testFile", ".txt", 10, 5, 100, 2000);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		File localFile = new File(localCollectionAbsolutePath);

		Transfer transfer = new Transfer();
		transfer.setIrodsAbsolutePath(destFile.getAbsolutePath());
		transfer.setLocalAbsolutePath(localFile.getAbsolutePath());
		transfer.setTransferType(TransferType.PUT);

		conveyorService.getQueueManagerService().enqueueTransferOperation(
				transfer, irodsAccount);

		while (conveyorService.getConveyorExecutorService().getRunningStatus() == RunningStatus.IDLE) {
			Thread.sleep(1000);
		}

		conveyorService.getConveyorExecutorService().requestPause();

		TransferTestRunningUtilities.waitForTransferToRunOrTimeout(
				conveyorService, TRANSFER_TIMEOUT);

		List<Transfer> transfers = conveyorService.getQueueManagerService()
				.listAllTransfersInQueue();
		Assert.assertFalse("no transfers in queue", transfers.isEmpty());
		Transfer lastTransfer = transfers.get(0);
		Assert.assertEquals("should be a cancelled transfer",
				TransferStateEnum.CANCELLED, lastTransfer.getTransferState());
		Assert.assertEquals("should be paused", RunningStatus.PAUSED,
				conveyorService.getConveyorExecutorService().getRunningStatus());
	}

	@Test
	// BUG [#1672] double free or corruption caused by replication of collection
	// (it has long paths, btw) on iRODS 3.3
	public void testReplicateDataObjectsInCollection() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		conveyorService.validatePassPhrase(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PASSWORD_KEY));
		conveyorService.getGridAccountService()
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);
		conveyorService.cancelQueueTimerTask();
		conveyorService.getConveyorExecutorService().requestResumeFromPause();

		ConfigurationProperty logSuccessful = new ConfigurationProperty();
		logSuccessful
				.setPropertyKey(ConfigurationPropertyConstants.LOG_SUCCESSFUL_FILES_KEY);
		logSuccessful.setPropertyValue("true");

		conveyorService.getConfigurationService().addConfigurationProperty(
				logSuccessful);

		ConfigurationProperty maxErrors = new ConfigurationProperty();
		maxErrors
				.setPropertyKey(ConfigurationPropertyConstants.MAX_ERRORS_BEFORE_CANCEL_KEY);
		maxErrors.setPropertyValue(5);
		conveyorService.getConfigurationService().addConfigurationProperty(
				maxErrors);

		String rootCollection = "testReplicateDataObjectsInCollection";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testReplicateDataObjectsInCollection", 2, 5, 2,
						"testFile", ".txt", 10, 5, 100, 2000);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		File localFile = new File(localCollectionAbsolutePath);

		DataTransferOperations dto = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		TransferControlBlock tcb = irodsFileSystem.getIrodsSession()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.USE_FORCE);

		dto.putOperation(localFile, destFile, null, tcb);

		Transfer transfer = new Transfer();
		transfer.setIrodsAbsolutePath(destFile.getAbsolutePath());
		transfer.setResourceName(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY));
		transfer.setTransferType(TransferType.REPLICATE);

		conveyorService.getQueueManagerService().enqueueTransferOperation(
				transfer, irodsAccount);

		TransferTestRunningUtilities.waitForTransferToRunOrTimeout(
				conveyorService, TRANSFER_TIMEOUT);

		List<Transfer> transfers = conveyorService.getQueueManagerService()
				.listAllTransfersInQueue();
		Assert.assertFalse("no transfers in queue", transfers.isEmpty());
		Assert.assertEquals("should be 1 transfer..maybe test cleanup is bad",
				1, transfers.size());
		transfer = transfers.get(0);

		transfer = conveyorService.getQueueManagerService()
				.initializeGivenTransferByLoadingChildren(transfer);

		Assert.assertFalse("did not create a transfer attempt", transfer
				.getTransferAttempts().isEmpty());

		Assert.assertEquals("did not get complete status",
				TransferStateEnum.COMPLETE, transfer.getTransferState());

		TransferAttempt attempts[] = new TransferAttempt[transfer
				.getTransferAttempts().size()];
		attempts = transfer.getTransferAttempts().toArray(attempts);
		Assert.assertEquals("should be 1 attempt", 1, attempts.length);

		TransferAttempt attempt = attempts[0];
		Assert.assertNotNull("transfer attempt not persisted", attempt.getId());
		Assert.assertEquals(TransferStatusEnum.OK, attempt.getAttemptStatus());

	}
}
