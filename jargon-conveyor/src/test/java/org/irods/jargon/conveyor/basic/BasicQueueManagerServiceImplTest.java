package org.irods.jargon.conveyor.basic;

import java.io.File;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.conveyor.core.ConfigurationPropertyConstants;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.conveyor.core.TransferAccountingManagementService;
import org.irods.jargon.conveyor.unittest.utils.TransferTestRunningUtilities;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.transfer.dao.domain.ConfigurationProperty;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferItem;
import org.irods.jargon.transfer.dao.domain.TransferStateEnum;
import org.irods.jargon.transfer.dao.domain.TransferStatusEnum;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:transfer-dao-beans.xml",
		"classpath:transfer-dao-hibernate-spring.cfg.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)
// @Transactional
public class BasicQueueManagerServiceImplTest {
	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "BasicQueueManagerServiceImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	@SuppressWarnings("unused")
	private static org.irods.jargon.testutils.AssertionHelper assertionHelper = null;
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
		assertionHelper = new org.irods.jargon.testutils.AssertionHelper();
	}

	@Before
	public void setUp() throws Exception {
		conveyorService.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());
		conveyorService.getQueueManagerService().purgeAllFromQueue();
		conveyorService.getGridAccountService().resetPassPhraseAndAccounts();

	}

	@After
	public void tearDown() throws Exception {
		conveyorService.getQueueManagerService().purgeAllFromQueue();
	}

	@Test
	public void testEnqueuePutTransferOperationAndWaitUntilDone()
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

		String rootCollection = "testEnqueuePutTransferOperationAndWaitUntilDone";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testEnqueuePutTransferOperationAndWaitUntilDone", 1,
						1, 1, "testFile", ".txt", 2, 2, 1, 2);

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

		List<TransferItem> items = conveyorService.getQueueManagerService()
				.getNextTransferItems(attempt.getId(), 0, 1000);

		Assert.assertEquals("should be 2 items", 2, items.size());

	}

	@Test
	public void testRestartPutTransferOperationAndWaitUntilDone()
			throws Exception {
		int totFiles = 10;
		int restartAt = 4;
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

		ConfigurationProperty logRestart = new ConfigurationProperty();
		logRestart
				.setPropertyKey(ConfigurationPropertyConstants.LOG_RESTART_FILES);
		logRestart.setPropertyValue("true");

		conveyorService.getConfigurationService().addConfigurationProperty(
				logRestart);

		String rootCollection = "testRestartPutTransferOperationAndWaitUntilDone";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator.generateManyFilesInGivenDirectory(IRODS_TEST_SUBDIR_PATH
				+ '/' + rootCollection, "test", ".txt", totFiles, 1, 1);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		File localFile = new File(localCollectionAbsolutePath);
		File[] children = localFile.listFiles();

		GridAccount gridAccount = conveyorService.getGridAccountService()
				.findGridAccountByIRODSAccount(irodsAccount);

		Transfer transfer = new Transfer();
		transfer.setIrodsAbsolutePath(destFile.getAbsolutePath());
		transfer.setLocalAbsolutePath(localFile.getAbsolutePath());
		transfer.setTransferType(TransferType.PUT);
		transfer.setTransferState(TransferStateEnum.COMPLETE);
		transfer.setGridAccount(gridAccount);
		transfer.setLastTransferStatus(TransferStatusEnum.ERROR);

		conveyorService.getQueueManagerService().saveOrUpdateTransfer(transfer);

		TransferAttempt attemptThatFailed = new TransferAttempt();
		attemptThatFailed.setAttemptStatus(TransferStatusEnum.ERROR);
		attemptThatFailed.setLastSuccessfulPath(children[restartAt - 2]
				.getAbsolutePath());
		conveyorService.getQueueManagerService().addTransferAttemptToTransfer(
				transfer.getId(), attemptThatFailed);

		// now restart
		conveyorService.getQueueManagerService()
				.enqueueRestartOfTransferOperation(transfer.getId());

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
		Assert.assertEquals("should be 2 attempts", 2, attempts.length);

		TransferAttempt attempt = attempts[1];
		Assert.assertNotNull("transfer restart attempt not persisted",
				attempt.getId());
		List<TransferItem> items = conveyorService.getQueueManagerService()
				.getNextTransferItems(attempt.getId(), 0, 1000);

		Assert.assertEquals("should be n items", totFiles, items.size());

		int i = 1;
		for (TransferItem item : items) {
			Assert.assertNotNull("null source",
					item.getSourceFileAbsolutePath());
			Assert.assertNotNull("null target",
					item.getTargetFileAbsolutePath());
			Assert.assertNotNull("null transfer type", item.getTransferType());
			Assert.assertEquals(TransferType.PUT, item.getTransferType());
			Assert.assertTrue("should be file", item.isFile());
			if (i < restartAt) {
				Assert.assertTrue("file should be marked as skipped",
						item.isSkipped());
			} else {
				Assert.assertFalse("file should not be marked as skipped",
						item.isSkipped());
			}
			i++;

		}

	}

	@Test
	public void testEnqueuePutTransferOperationThrowExceptionOnDTO()
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

		// shim in mock ao factory, this is cleaned up in the @Before method

		String rootCollection = "testEnqueuePutTransferOperationAndWaitUntilDone";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		Transfer transfer = new Transfer();
		transfer.setIrodsAbsolutePath(rootCollection);
		transfer.setLocalAbsolutePath(localCollectionAbsolutePath);
		transfer.setTransferType(TransferType.PUT);

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		DataTransferOperations dto = Mockito.mock(DataTransferOperations.class);

		TransferControlBlock tcb = irodsFileSystem.getIrodsSession()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();

		Mockito.when(
				irodsAccessObjectFactory
						.getDataTransferOperations(irodsAccount)).thenReturn(
				dto);
		Mockito.doThrow(new JargonException("blah"))
				.when(dto)
				.putOperation(Mockito.anyString(), Mockito.anyString(),
						Mockito.anyString(),
						Mockito.any(TransferStatusCallbackListener.class),
						Mockito.any(TransferControlBlock.class));
		Mockito.when(
				irodsAccessObjectFactory
						.buildDefaultTransferControlBlockBasedOnJargonProperties())
				.thenReturn(tcb);
		conveyorService.setIrodsAccessObjectFactory(irodsAccessObjectFactory);

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
		Assert.assertEquals("should have a status of error",
				TransferStatusEnum.ERROR, attempt.getAttemptStatus());
		Assert.assertNotNull("null error message", attempt.getErrorMessage());
		Assert.assertEquals(
				"missing the attempt creating the transfer process message",
				TransferAccountingManagementService.ERROR_ATTEMPTING_TO_RUN,
				attempt.getErrorMessage());
		Assert.assertNotNull("missing global exception",
				attempt.getGlobalException());
		Assert.assertEquals("global exception message incorrect", "blah",
				attempt.getGlobalException());
		Assert.assertNotNull("should be a stack trace, was null",
				attempt.getGlobalExceptionStackTrace());
		Assert.assertFalse("empty stack trace", attempt
				.getGlobalExceptionStackTrace().isEmpty());

	}

	@Test
	public void testProcessQueueOnStartupWithProcessingTransaction()
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

		ConfigurationProperty logRestart = new ConfigurationProperty();
		logRestart
				.setPropertyKey(ConfigurationPropertyConstants.LOG_RESTART_FILES);
		logRestart.setPropertyValue("true");

		conveyorService.getConfigurationService().addConfigurationProperty(
				logRestart);

		String rootCollection = "testProcessQueueOnStartupWithProcessingTransaction";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		File localFile = new File(localCollectionAbsolutePath);

		GridAccount gridAccount = conveyorService.getGridAccountService()
				.findGridAccountByIRODSAccount(irodsAccount);

		Transfer transfer = new Transfer();
		transfer.setIrodsAbsolutePath(destFile.getAbsolutePath());
		transfer.setLocalAbsolutePath(localFile.getAbsolutePath());
		transfer.setTransferType(TransferType.PUT);
		transfer.setTransferState(TransferStateEnum.PROCESSING);
		transfer.setGridAccount(gridAccount);
		transfer.setLastTransferStatus(TransferStatusEnum.OK);

		conveyorService.getQueueManagerService().saveOrUpdateTransfer(transfer);

		TransferAttempt attemptThatWasProcessing = new TransferAttempt();
		attemptThatWasProcessing.setAttemptStatus(TransferStatusEnum.OK);
		conveyorService.getQueueManagerService().addTransferAttemptToTransfer(
				transfer.getId(), attemptThatWasProcessing);

		// now simulate startup
		conveyorService.getQueueManagerService().preprocessQueueAtStartup();

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

		// I'll get a warning because there are no files, but it shows it was
		// enqueued and processed
		Assert.assertEquals(
				"did not get warning status, it shold have been marked enqueued and subsequently dequeued and processed",
				TransferStateEnum.COMPLETE, transfer.getTransferState());

		TransferAttempt attempts[] = new TransferAttempt[transfer
				.getTransferAttempts().size()];
		attempts = transfer.getTransferAttempts().toArray(attempts);
		Assert.assertEquals("should be 2 attempts", 2, attempts.length);

		TransferAttempt attempt = attempts[0];
		Assert.assertEquals("did not set status to error",
				TransferStatusEnum.ERROR, attempt.getAttemptStatus());
		Assert.assertNotNull("did not set message", attempt.getErrorMessage());

		attempt = attempts[1];
		// this is warning because there were no files in the test
		Assert.assertEquals(
				"did not set status to warning (meaining it was enqueued and processed",
				TransferStatusEnum.WARNING, attempt.getAttemptStatus());
		Assert.assertNotNull("did not set message", attempt.getErrorMessage());

	}

	@Test
	public void testPurgeSuccessfulTransfers() throws Exception {
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

		GridAccount gridAccount = conveyorService.getGridAccountService()
				.findGridAccountByIRODSAccount(irodsAccount);

		Transfer transfer = new Transfer();
		transfer.setGridAccount(gridAccount);
		transfer.setIrodsAbsolutePath("irods");
		transfer.setLocalAbsolutePath("local");
		transfer.setTransferType(TransferType.PUT);
		transfer.setLastTransferStatus(TransferStatusEnum.OK);
		transfer.setTransferState(TransferStateEnum.COMPLETE);

		conveyorService.getQueueManagerService().saveOrUpdateTransfer(transfer);
		transfer = new Transfer();
		transfer.setGridAccount(gridAccount);

		transfer.setIrodsAbsolutePath("irods");
		transfer.setLocalAbsolutePath("local");
		transfer.setTransferType(TransferType.PUT);
		transfer.setLastTransferStatus(TransferStatusEnum.OK);
		transfer.setTransferState(TransferStateEnum.PROCESSING);
		conveyorService.getQueueManagerService().saveOrUpdateTransfer(transfer);

		conveyorService.getQueueManagerService().purgeSuccessfulFromQueue();

		List<Transfer> actual = conveyorService.getQueueManagerService()
				.listAllTransfersInQueue();
		Assert.assertFalse("no transfers left in queue", actual.isEmpty());

	}
}
