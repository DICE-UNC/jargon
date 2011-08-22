package org.irods.jargon.transfer.engine;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.transfer.TransferServiceFactoryImpl;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransferItem;
import org.irods.jargon.transfer.dao.domain.TransferState;
import org.irods.jargon.transfer.dao.domain.TransferStatus;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.irods.jargon.transfer.exception.CannotUpdateTransferInProgressException;
import org.irods.jargon.transfer.util.HibernateUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class TransferQueueServiceTest {

	private static Properties testingProperties = new Properties();

	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;

	public static final String IRODS_TEST_SUBDIR_PATH = "TransferQueueServiceTest";

	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;

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
		String databaseUrl = "jdbc:derby:" + System.getProperty("user.home")
				+ "/.idrop/target/database/transfer";
		DatabasePreparationUtils.clearAllDatabaseForTesting(databaseUrl,
				"transfer", "transfer"); // TODO: make a prop

	}

	@Before
	public void setUpEach() throws Exception {

	}

	@Ignore
	public void testGetCurrentQueue() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String rootCollection = "enqueueAPutWhenPaused";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();

		transferQueueService.purgeEntireQueue();

		transferQueueService.enqueuePutTransfer(localCollectionAbsolutePath,
				irodsCollectionRootAbsolutePath, "", irodsAccount);

		transferQueueService.enqueuePutTransfer(localCollectionAbsolutePath,
				irodsCollectionRootAbsolutePath, "", irodsAccount);

		transferQueueService.enqueuePutTransfer(localCollectionAbsolutePath,
				irodsCollectionRootAbsolutePath, "", irodsAccount);

		List<LocalIRODSTransfer> transferQueue = transferQueueService
				.getCurrentQueue();
		Assert.assertEquals(3, transferQueue.size());
		LocalIRODSTransfer enqueuedTransfer = transferQueue.get(0);
		Assert.assertEquals("this should still be enqueued",
				enqueuedTransfer.getTransferState(), TransferState.ENQUEUED);
	}

	@Test
	public void testGetErrorQueue() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String rootCollection = "testGetErrorQueue";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath(irodsCollectionRootAbsolutePath);
		enqueuedTransfer.setLocalAbsolutePath(localCollectionAbsolutePath);
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(irodsAccount
				.getDefaultStorageResource());
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
		enqueuedTransfer.setTransferState(TransferState.PROCESSING);
		enqueuedTransfer.setTransferStatus(TransferStatus.ERROR);
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();
		transferQueueService.updateLocalIRODSTransfer(enqueuedTransfer);

		enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath(irodsCollectionRootAbsolutePath);
		enqueuedTransfer.setLocalAbsolutePath(localCollectionAbsolutePath);
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(irodsAccount
				.getDefaultStorageResource());
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
		enqueuedTransfer.setTransferState(TransferState.PROCESSING);
		enqueuedTransfer.setTransferStatus(TransferStatus.WARNING);

		transferQueueService.updateLocalIRODSTransfer(enqueuedTransfer);

		// now get the queue
		List<LocalIRODSTransfer> errorQueue = transferQueueService
				.getErrorQueue();
		Assert.assertEquals("did not find the error transaction", 1,
				errorQueue.size());
		LocalIRODSTransfer errorTransfer = errorQueue.get(0);
		Assert.assertEquals("this does not have error status",
				TransferStatus.ERROR, errorTransfer.getTransferStatus());

	}

	@Test
	public void testGetWarningQueue() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String rootCollection = "testGetWarningQueue";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();

		LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath(irodsCollectionRootAbsolutePath);
		enqueuedTransfer.setLocalAbsolutePath(localCollectionAbsolutePath);
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(irodsAccount
				.getDefaultStorageResource());
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
		enqueuedTransfer.setTransferState(TransferState.PROCESSING);
		enqueuedTransfer.setTransferStatus(TransferStatus.WARNING);

		transferQueueService.updateLocalIRODSTransfer(enqueuedTransfer);
		enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath(irodsCollectionRootAbsolutePath);
		enqueuedTransfer.setLocalAbsolutePath(localCollectionAbsolutePath);
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(irodsAccount
				.getDefaultStorageResource());
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
		enqueuedTransfer.setTransferState(TransferState.PROCESSING);
		enqueuedTransfer.setTransferStatus(TransferStatus.OK);

		transferQueueService.updateLocalIRODSTransfer(enqueuedTransfer);
		// now get the queue
		List<LocalIRODSTransfer> transfers = transferQueueService
				.getWarningQueue();

		boolean txfrFound = false;
		for (LocalIRODSTransfer transfer : transfers) {
			if (transfer.getIrodsAbsolutePath().equals(
					irodsCollectionRootAbsolutePath)) {
				txfrFound = true;
				Assert.assertEquals("this does not have error status",
						TransferStatus.WARNING, transfer.getTransferStatus());

			}
		}

		TestCase.assertTrue("did not find transfer", txfrFound);

	}

	@Test
	public void testLastNInQueue() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String rootCollection = "enqueueAPutWhenPaused";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();

		transferQueueService.enqueuePutTransfer(localCollectionAbsolutePath,
				irodsCollectionRootAbsolutePath, "", irodsAccount);

		transferQueueService.enqueuePutTransfer(localCollectionAbsolutePath,
				irodsCollectionRootAbsolutePath, "", irodsAccount);

		transferQueueService.enqueuePutTransfer(localCollectionAbsolutePath,
				irodsCollectionRootAbsolutePath, "", irodsAccount);

		transferQueueService.enqueuePutTransfer(localCollectionAbsolutePath,
				irodsCollectionRootAbsolutePath, "", irodsAccount);

		List<LocalIRODSTransfer> transferQueue = transferQueueService
				.getLastNInQueue(3);
		Assert.assertEquals(3, transferQueue.size());

	}

	@Test
	public void testMarkTransferAsErrorAndTerminate() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String rootCollection = "enqueueAPutWhenPaused";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		TransferManager transferManager = new TransferManagerImpl(
				IRODSFileSystem.instance());

		transferManager.getTransferQueueService().enqueuePutTransfer(
				localCollectionAbsolutePath, irodsCollectionRootAbsolutePath,
				"", irodsAccount);
		List<LocalIRODSTransfer> transferQueue = transferManager
				.getTransferQueueService().getLastNInQueue(1);

		LocalIRODSTransfer transferToMark = transferQueue.get(0);
		transferManager.getTransferQueueService()
				.markTransferAsErrorAndTerminate(transferToMark,
						transferManager);

		// now get the error transfers, there should be one and it should be the
		// one I marked

		List<LocalIRODSTransfer> errorTransfers = transferManager
				.getTransferQueueService().showErrorTransfers();

		Assert.assertEquals("should have 1 error transfer", 1,
				errorTransfers.size());
	}

	@Test
	public void testRestartATransfer() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();

		final LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath("path");
		enqueuedTransfer.setLocalAbsolutePath("localPath");
		enqueuedTransfer.setLastSuccessfulPath("lastSuccessfulPath");
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(irodsAccount
				.getDefaultStorageResource());
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
		enqueuedTransfer.setTransferState(TransferState.COMPLETE);
		enqueuedTransfer.setTransferStatus(TransferStatus.ERROR);

		transferQueueService.updateLocalIRODSTransfer(enqueuedTransfer);

		// add two test items
		LocalIRODSTransferItem localIRODSTransferItem = new LocalIRODSTransferItem();
		localIRODSTransferItem.setError(false);
		localIRODSTransferItem.setFile(true);
		localIRODSTransferItem.setSourceFileAbsolutePath("/sourcepath1");
		localIRODSTransferItem.setTargetFileAbsolutePath("/targetpath1");
		localIRODSTransferItem.setTransferredAt(new Date());
		localIRODSTransferItem.setLocalIRODSTransfer(enqueuedTransfer);
		enqueuedTransfer.getLocalIRODSTransferItems().add(
				localIRODSTransferItem);

		localIRODSTransferItem = new LocalIRODSTransferItem();
		localIRODSTransferItem.setError(false);
		localIRODSTransferItem.setFile(true);
		localIRODSTransferItem.setSourceFileAbsolutePath("/sourcepath2");
		localIRODSTransferItem.setTargetFileAbsolutePath("/targetpath2");
		localIRODSTransferItem.setTransferredAt(new Date());
		localIRODSTransferItem.setLocalIRODSTransfer(enqueuedTransfer);
		enqueuedTransfer.getLocalIRODSTransferItems().add(
				localIRODSTransferItem);
		transferQueueService.updateLocalIRODSTransfer(enqueuedTransfer);

		transferQueueService.restartTransfer(enqueuedTransfer);

		List<LocalIRODSTransfer> transfers = transferQueueService
				.getCurrentQueue();

		boolean txfrFound = false;
		for (LocalIRODSTransfer transfer : transfers) {
			if (transfer.getIrodsAbsolutePath().equals(
					enqueuedTransfer.getIrodsAbsolutePath())) {
				txfrFound = true;
				Assert.assertEquals("this should still be enqueued",
						transfer.getTransferState(), TransferState.ENQUEUED);

				Assert.assertEquals("this should still be ok status now",
						transfer.getTransferStatus(), TransferStatus.OK);
			}
		}

		TestCase.assertTrue("did not find transfer", txfrFound);

	}

	@Test
	public void testMarkTransferAsErrorAndTerminatePassingException()
			throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String rootCollection = "testMarkTransferAsErrorAndTerminatePassingException";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = rootCollection;
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();

		transferQueueService.enqueuePutTransfer(localCollectionAbsolutePath,
				irodsCollectionRootAbsolutePath, "", irodsAccount);
		List<LocalIRODSTransfer> transferQueue = transferQueueService
				.getLastNInQueue(1);

		LocalIRODSTransfer transferToMark = transferQueue.get(0);
		TransferManager transferManager = new TransferManagerImpl(
				IRODSFileSystem.instance());
		transferQueueService.markTransferAsErrorAndTerminate(transferToMark,
				new JargonException("hello a jargon exception"),
				transferManager);

		// now get the error transfers, there should be one and it should be the
		// one I marked

		List<LocalIRODSTransfer> transfers = transferQueueService
				.showErrorTransfers();
		boolean txfrFound = false;
		for (LocalIRODSTransfer transfer : transfers) {
			if (transfer.getIrodsAbsolutePath().equals(
					irodsCollectionRootAbsolutePath)) {
				txfrFound = true;
				Assert.assertEquals("did not retain the exception",
						"hello a jargon exception",
						transfer.getGlobalException());

			}
		}

		TestCase.assertTrue("did not find transfer", txfrFound);

	}

	@Test
	public void testGetAllTransferItemsForTransfer() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		String rootCollection = "getAllTransferItemsForTransfer";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();

		LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath(irodsCollectionRootAbsolutePath);
		enqueuedTransfer.setLocalAbsolutePath(localCollectionAbsolutePath);
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(irodsAccount
				.getDefaultStorageResource());
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
		enqueuedTransfer.setTransferState(TransferState.ENQUEUED);
		enqueuedTransfer.setTransferStatus(TransferStatus.OK);

		transferQueueService.updateLocalIRODSTransfer(enqueuedTransfer);

		// add two test items LocalIRODSTransferItem localIRODSTransferItem =
		// new
		LocalIRODSTransferItem localIRODSTransferItem = new LocalIRODSTransferItem();
		localIRODSTransferItem.setError(false);
		localIRODSTransferItem.setFile(true);
		localIRODSTransferItem.setSourceFileAbsolutePath("/sourcepath1");
		localIRODSTransferItem.setTargetFileAbsolutePath("/targetpath1");
		localIRODSTransferItem.setTransferredAt(new Date());
		localIRODSTransferItem.setLocalIRODSTransfer(enqueuedTransfer);

		transferQueueService.addItemToTransfer(enqueuedTransfer,
				localIRODSTransferItem);

		localIRODSTransferItem = new LocalIRODSTransferItem();
		localIRODSTransferItem.setError(false);
		localIRODSTransferItem.setFile(true);
		localIRODSTransferItem.setSourceFileAbsolutePath("/sourcepath2");
		localIRODSTransferItem.setTargetFileAbsolutePath("/targetpath2");
		localIRODSTransferItem.setTransferredAt(new Date());
		localIRODSTransferItem.setLocalIRODSTransfer(enqueuedTransfer);
		transferQueueService.addItemToTransfer(enqueuedTransfer,
				localIRODSTransferItem);

		// now get all items for the transfer
		List<LocalIRODSTransferItem> transferItems = transferQueueService
				.getAllTransferItemsForTransfer(enqueuedTransfer.getId());
		irodsFileSystem.close();
		Assert.assertEquals("did not get the two transfer items", 2,
				transferItems.size());

	}

	@Test
	public void testGetAllTransferItemsForTransferWrongId() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		String rootCollection = "getAllTransferItemsForTransfer";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();

		LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath(irodsCollectionRootAbsolutePath);
		enqueuedTransfer.setLocalAbsolutePath(localCollectionAbsolutePath);
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(irodsAccount
				.getDefaultStorageResource());
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
		enqueuedTransfer.setTransferState(TransferState.ENQUEUED);
		enqueuedTransfer.setTransferStatus(TransferStatus.OK);

		transferQueueService.updateLocalIRODSTransfer(enqueuedTransfer);

		// add two test items
		LocalIRODSTransferItem localIRODSTransferItem = new LocalIRODSTransferItem();
		localIRODSTransferItem.setError(false);
		localIRODSTransferItem.setFile(true);
		localIRODSTransferItem.setSourceFileAbsolutePath("/sourcepath1");
		localIRODSTransferItem.setTargetFileAbsolutePath("/targetpath1");
		localIRODSTransferItem.setTransferredAt(new Date());
		localIRODSTransferItem.setLocalIRODSTransfer(enqueuedTransfer);
		enqueuedTransfer.getLocalIRODSTransferItems().add(
				localIRODSTransferItem);

		localIRODSTransferItem = new LocalIRODSTransferItem();
		localIRODSTransferItem.setError(false);
		localIRODSTransferItem.setFile(true);
		localIRODSTransferItem.setSourceFileAbsolutePath("/sourcepath2");
		localIRODSTransferItem.setTargetFileAbsolutePath("/targetpath2");
		localIRODSTransferItem.setTransferredAt(new Date());
		localIRODSTransferItem.setLocalIRODSTransfer(enqueuedTransfer);
		enqueuedTransfer.getLocalIRODSTransferItems().add(
				localIRODSTransferItem);

		// now get all items for the transfer
		List<LocalIRODSTransferItem> transferItems = transferQueueService
				.getAllTransferItemsForTransfer(new Long(9999999));
		irodsFileSystem.close();
		Assert.assertEquals("wrong id, should have just returned empty", 0,
				transferItems.size());

	}

	@Test
	public void testGetErrorTransferItemsForTransfer() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		String rootCollection = "getAllTransferItemsForTransfer";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();

		LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath(irodsCollectionRootAbsolutePath);
		enqueuedTransfer.setLocalAbsolutePath(localCollectionAbsolutePath);
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(irodsAccount
				.getDefaultStorageResource());
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
		enqueuedTransfer.setTransferState(TransferState.ENQUEUED);
		enqueuedTransfer.setTransferStatus(TransferStatus.OK);

		transferQueueService.updateLocalIRODSTransfer(enqueuedTransfer);

		// add two test items

		LocalIRODSTransferItem localIRODSTransferItem = new LocalIRODSTransferItem();
		localIRODSTransferItem.setError(false);
		localIRODSTransferItem.setFile(true);
		localIRODSTransferItem.setSourceFileAbsolutePath("/sourcepath1");
		localIRODSTransferItem.setTargetFileAbsolutePath("/targetpath1");
		localIRODSTransferItem.setTransferredAt(new Date());
		localIRODSTransferItem.setLocalIRODSTransfer(enqueuedTransfer);
		transferQueueService.addItemToTransfer(enqueuedTransfer,
				localIRODSTransferItem);

		localIRODSTransferItem = new LocalIRODSTransferItem();
		localIRODSTransferItem.setError(true);
		localIRODSTransferItem.setFile(true);
		localIRODSTransferItem.setSourceFileAbsolutePath("/sourcepath2");
		localIRODSTransferItem.setTargetFileAbsolutePath("/targetpath2");
		localIRODSTransferItem.setTransferredAt(new Date());
		localIRODSTransferItem.setLocalIRODSTransfer(enqueuedTransfer);
		transferQueueService.addItemToTransfer(enqueuedTransfer,
				localIRODSTransferItem);

		// now get all items for the transfer
		List<LocalIRODSTransferItem> transferItems = transferQueueService
				.getErrorTransferItemsForTransfer(enqueuedTransfer.getId());
		irodsFileSystem.close();
		Assert.assertEquals("did not get the error transfer item", 1,
				transferItems.size());

	}

	@Test
	public void testEnqueueReplicate() throws Exception {

		String testName = "testEnqueueReplicate";
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		transferQueueService.enqueueReplicateTransfer(testName,
				"targetResource", irodsAccount);

		// now get the data from the database

		List<LocalIRODSTransfer> transfers = transferQueueService
				.getCurrentQueue();

		boolean txfrFound = false;
		for (LocalIRODSTransfer transfer : transfers) {
			if (transfer.getIrodsAbsolutePath().equals(testName)) {
				txfrFound = true;

				TestCase.assertEquals("targetResource",
						transfer.getTransferResource());
				TestCase.assertEquals(TransferType.REPLICATE,
						transfer.getTransferType());

			}
		}

		TestCase.assertTrue("did not find transfer", txfrFound);

	}

	@Test
	public void testEnqueueCopy() throws Exception {
		String testName = "testEnqueueCopy";
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		transferQueueService.enqueueCopyTransfer(testName, "targetResource",
				testName, irodsAccount);

		// now get the data from the database

		List<LocalIRODSTransfer> transfers = transferQueueService
				.getCurrentQueue();

		boolean txfrFound = false;
		for (LocalIRODSTransfer transfer : transfers) {
			if (transfer.getIrodsAbsolutePath().equals(testName)) {
				txfrFound = true;

				TestCase.assertEquals("targetResource",
						transfer.getTransferResource());
				TestCase.assertEquals(TransferType.COPY,
						transfer.getTransferType());

			}
		}

		TestCase.assertTrue("did not find transfer", txfrFound);

	}

	@Test
	public void testEnqueueGet() throws Exception {
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		String testName = "testEnqueueGet";

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		transferQueueService.enqueueGetTransfer(testName,
				"targetLocalAbsolutePath", "sourceResource", irodsAccount);

		// now get the data from the database

		List<LocalIRODSTransfer> transfers = transferQueueService
				.getCurrentQueue();
		boolean txfrFound = false;
		for (LocalIRODSTransfer transfer : transfers) {
			if (transfer.getIrodsAbsolutePath().equals(testName)) {
				txfrFound = true;
				TestCase.assertEquals("sourceResource",
						transfer.getTransferResource());
				TestCase.assertEquals(TransferType.GET,
						transfer.getTransferType());

			}
		}

		TestCase.assertTrue("did not find transfer", txfrFound);

	}

	@Test
	public void testEnqueueGetBigFileName() throws Exception {

		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String fileNamePart = "filenamefilenamefilenameabcdkjfkdjfiaeojkjkldjflasfdjfasdfjasdf";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 50; i++) {
			sb.append(fileNamePart);
		}

		transferQueueService.enqueueGetTransfer(sb.toString(), sb.toString(),
				"sourceResource", irodsAccount);

		// now get the data from the database

		List<LocalIRODSTransfer> transfers = transferQueueService
				.getCurrentQueue();
		boolean txfrFound = false;
		for (LocalIRODSTransfer transfer : transfers) {
			if (transfer.getIrodsAbsolutePath().equals(sb.toString())) {
				txfrFound = true;
				TestCase.assertEquals("sourceResource",
						transfer.getTransferResource());
				TestCase.assertEquals(TransferType.GET,
						transfer.getTransferType());

			}
		}

		TestCase.assertTrue("did not find transfer", txfrFound);

	}

	@Test(expected = JargonException.class)
	public void testEnqueueGetNoSource() throws Exception {
		TransferQueueService transferQueueService = new TransferQueueServiceImpl();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		transferQueueService.enqueueGetTransfer("", "targetLocalAbsolutePath",
				"sourceResource", irodsAccount);
	}

	@Test(expected = JargonException.class)
	public void testEnqueueGetNoTarget() throws Exception {
		TransferQueueService transferQueueService = new TransferQueueServiceImpl();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		transferQueueService.enqueueGetTransfer("source", "", "sourceResource",
				irodsAccount);
	}

	@Test(expected = JargonException.class)
	public void testEnqueueGetNullTarget() throws Exception {
		TransferQueueService transferQueueService = new TransferQueueServiceImpl();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		transferQueueService.enqueueGetTransfer("source", null,
				"sourceResource", irodsAccount);
	}

	@Test(expected = JargonException.class)
	public void testEnqueueGetNullResource() throws Exception {
		TransferQueueService transferQueueService = new TransferQueueServiceImpl();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		transferQueueService.enqueueGetTransfer("source", "target", null,
				irodsAccount);
	}

	@Test(expected = JargonException.class)
	public void testEnqueueGetNullSource() throws Exception {
		TransferQueueService transferQueueService = new TransferQueueServiceImpl();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		transferQueueService.enqueueGetTransfer(null,
				"targetLocalAbsolutePath", "sourceResource", irodsAccount);
	}

	@Test(expected = JargonException.class)
	public void testEnqueueReplicateNoPath() throws Exception {
		TransferQueueService transferQueueService = new TransferQueueServiceImpl();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		transferQueueService.enqueueReplicateTransfer("", "targetResource",
				irodsAccount);
	}

	@Test(expected = JargonException.class)
	public void testEnqueueReplicateNullResource() throws Exception {
		TransferQueueService transferQueueService = new TransferQueueServiceImpl();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		transferQueueService.enqueueReplicateTransfer("hello", null,
				irodsAccount);
	}

	@Test(expected = JargonException.class)
	public void testEnqueueReplicateNullPath() throws Exception {
		TransferQueueService transferQueueService = new TransferQueueServiceImpl();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		transferQueueService.enqueueReplicateTransfer(null, "targetResource",
				irodsAccount);
	}

	@Test
	public void testPurgeAll() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();

		LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath("path1");
		enqueuedTransfer.setLocalAbsolutePath("path1");
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(irodsAccount
				.getDefaultStorageResource());
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
		enqueuedTransfer.setTransferState(TransferState.PROCESSING);
		enqueuedTransfer.setTransferStatus(TransferStatus.OK);

		transferQueueService.updateLocalIRODSTransfer(enqueuedTransfer);

		enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath("path2");
		enqueuedTransfer.setLocalAbsolutePath("path2");
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(irodsAccount
				.getDefaultStorageResource());
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
		enqueuedTransfer.setTransferState(TransferState.COMPLETE);
		enqueuedTransfer.setTransferStatus(TransferStatus.OK);

		transferQueueService.updateLocalIRODSTransfer(enqueuedTransfer);

		enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath("path3");
		enqueuedTransfer.setLocalAbsolutePath("path3");
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(irodsAccount
				.getDefaultStorageResource());
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
		enqueuedTransfer.setTransferState(TransferState.COMPLETE);
		enqueuedTransfer.setTransferStatus(TransferStatus.ERROR);

		transferQueueService.updateLocalIRODSTransfer(enqueuedTransfer);

		LocalIRODSTransferItem item = new LocalIRODSTransferItem();
		item.setLocalIRODSTransfer(enqueuedTransfer);
		item.setSourceFileAbsolutePath("path");
		item.setTargetFileAbsolutePath("path");
		transferQueueService.addItemToTransfer(enqueuedTransfer, item);

		// now purge
		transferQueueService.purgeQueue();

		List<LocalIRODSTransfer> actualTransfers = transferQueueService
				.getRecentQueue();

		TestCase.assertEquals("did not get the 1 processing transfer", 1,
				actualTransfers.size());
		LocalIRODSTransfer actualTransfer = actualTransfers.get(0);
		TestCase.assertEquals("the one transfer should be the processing item",
				TransferState.PROCESSING, actualTransfer.getTransferState());

	}

	@Test
	public void testPurgeComplete() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();

		transferQueueService.purgeQueue();

		LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath("path1");
		enqueuedTransfer.setLocalAbsolutePath("path1");
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(irodsAccount
				.getDefaultStorageResource());
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
		enqueuedTransfer.setTransferState(TransferState.PROCESSING);
		enqueuedTransfer.setTransferStatus(TransferStatus.OK);

		transferQueueService.updateLocalIRODSTransfer(enqueuedTransfer);

		enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath("path2");
		enqueuedTransfer.setLocalAbsolutePath("path2");
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(irodsAccount
				.getDefaultStorageResource());
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
		enqueuedTransfer.setTransferState(TransferState.COMPLETE);
		enqueuedTransfer.setTransferStatus(TransferStatus.OK);

		transferQueueService.updateLocalIRODSTransfer(enqueuedTransfer);

		enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath("path3");
		enqueuedTransfer.setLocalAbsolutePath("path3");
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(irodsAccount
				.getDefaultStorageResource());
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
		enqueuedTransfer.setTransferState(TransferState.COMPLETE);
		enqueuedTransfer.setTransferStatus(TransferStatus.ERROR);

		transferQueueService.updateLocalIRODSTransfer(enqueuedTransfer);

		LocalIRODSTransferItem item = new LocalIRODSTransferItem();
		item.setLocalIRODSTransfer(enqueuedTransfer);
		item.setSourceFileAbsolutePath("path");
		item.setTargetFileAbsolutePath("path");
		transferQueueService.addItemToTransfer(enqueuedTransfer, item);

		// now purge
		transferQueueService.purgeSuccessful();

		List<LocalIRODSTransfer> actualTransfers = transferQueueService
				.getRecentQueue();

		for (LocalIRODSTransfer transfer : actualTransfers) {
			TestCase.assertEquals("transfer should be the processing item",
					TransferState.PROCESSING, transfer.getTransferState());
		}

	}

	@Test
	public void testResubmitATransfer() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();

		transferQueueService.purgeQueue();

		final LocalIRODSTransfer enqueuedTransfer;
		LocalIRODSTransferItem localIRODSTransferItem;
		enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath("path");
		enqueuedTransfer.setLocalAbsolutePath("localPath");
		enqueuedTransfer.setLastSuccessfulPath("lastSuccessfulPath");
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(irodsAccount
				.getDefaultStorageResource());
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
		enqueuedTransfer.setTransferState(TransferState.COMPLETE);
		enqueuedTransfer.setTransferStatus(TransferStatus.ERROR);

		transferQueueService.updateLocalIRODSTransfer(enqueuedTransfer);

		// add two test items
		localIRODSTransferItem = new LocalIRODSTransferItem();
		localIRODSTransferItem.setError(false);
		localIRODSTransferItem.setFile(true);
		localIRODSTransferItem.setSourceFileAbsolutePath("/sourcepath1");
		localIRODSTransferItem.setTargetFileAbsolutePath("/targetpath1");
		localIRODSTransferItem.setTransferredAt(new Date());
		localIRODSTransferItem.setLocalIRODSTransfer(enqueuedTransfer);
		enqueuedTransfer.getLocalIRODSTransferItems().add(
				localIRODSTransferItem);

		localIRODSTransferItem = new LocalIRODSTransferItem();
		localIRODSTransferItem.setError(false);
		localIRODSTransferItem.setFile(true);
		localIRODSTransferItem.setSourceFileAbsolutePath("/sourcepath2");
		localIRODSTransferItem.setTargetFileAbsolutePath("/targetpath2");
		localIRODSTransferItem.setTransferredAt(new Date());
		localIRODSTransferItem.setLocalIRODSTransfer(enqueuedTransfer);
		enqueuedTransfer.getLocalIRODSTransferItems().add(
				localIRODSTransferItem);
		transferQueueService.updateLocalIRODSTransfer(enqueuedTransfer);

		transferQueueService.resubmitTransfer(enqueuedTransfer);

		LocalIRODSTransfer actual = transferQueueService
				.findLocalIRODSTransferById(enqueuedTransfer.getId());

		Assert.assertEquals("this should still be enqueued",
				actual.getTransferState(), TransferState.ENQUEUED);

		Assert.assertEquals("this should still be ok status now",
				actual.getTransferStatus(), TransferStatus.OK);
	}

	@Test
	public void testCancelATransfer() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();

		LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath("path");
		enqueuedTransfer.setLocalAbsolutePath("localPath");
		enqueuedTransfer.setLastSuccessfulPath("lastSuccessfulPath");
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(irodsAccount
				.getDefaultStorageResource());
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
		enqueuedTransfer.setTransferState(TransferState.ENQUEUED);
		enqueuedTransfer.setTransferStatus(TransferStatus.OK);

		transferQueueService.updateLocalIRODSTransfer(enqueuedTransfer);

		transferQueueService.setTransferAsCancelled(enqueuedTransfer);

		LocalIRODSTransfer actual = transferQueueService
				.findLocalIRODSTransferById(enqueuedTransfer.getId());
		Assert.assertEquals("this should still be enqueued",
				actual.getTransferState(), TransferState.CANCELLED);

		Assert.assertEquals("this should still be ok status now",
				actual.getTransferStatus(), TransferStatus.OK);
	}

	@Test
	public void testCancelACompletedTransfer() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();

		LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath("path");
		enqueuedTransfer.setLocalAbsolutePath("localPath");
		enqueuedTransfer.setLastSuccessfulPath("lastSuccessfulPath");
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(irodsAccount
				.getDefaultStorageResource());
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
		enqueuedTransfer.setTransferState(TransferState.COMPLETE);
		enqueuedTransfer.setTransferStatus(TransferStatus.OK);

		transferQueueService.updateLocalIRODSTransfer(enqueuedTransfer);
		transferQueueService.setTransferAsCancelled(enqueuedTransfer);

		LocalIRODSTransfer actual = transferQueueService
				.findLocalIRODSTransferById(enqueuedTransfer.getId());

		Assert.assertEquals("this should still be enqueued",
				actual.getTransferState(), TransferState.COMPLETE);

		Assert.assertEquals("this should still be ok status now",
				actual.getTransferStatus(), TransferStatus.OK);
	}

	@Test
	public void testProcessQueueAtStartupWithAProcessingTransferHanging()
			throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String rootCollection = "testGetErrorQueue";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();

		LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath(irodsCollectionRootAbsolutePath);
		enqueuedTransfer.setLocalAbsolutePath(localCollectionAbsolutePath);
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(irodsAccount
				.getDefaultStorageResource());
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
		enqueuedTransfer.setTransferState(TransferState.PROCESSING);
		enqueuedTransfer.setTransferStatus(TransferStatus.ERROR);

		transferQueueService.updateLocalIRODSTransfer(enqueuedTransfer);

		transferQueueService.processQueueAtStartup();

		// now get the queue
		LocalIRODSTransfer actual = transferQueueService
				.findLocalIRODSTransferById(enqueuedTransfer.getId());

		Assert.assertEquals("this does not have enqueued status",
				TransferState.ENQUEUED, actual.getTransferState());

	}

	@Test
	public void testCreateQueueServiceInUserHomeDirectory() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();

		LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath("path");
		enqueuedTransfer.setLocalAbsolutePath("localPath");
		enqueuedTransfer.setLastSuccessfulPath("lastSuccessfulPath");
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(irodsAccount
				.getDefaultStorageResource());
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
		enqueuedTransfer.setTransferState(TransferState.COMPLETE);
		enqueuedTransfer.setTransferStatus(TransferStatus.OK);

		transferQueueService.updateLocalIRODSTransfer(enqueuedTransfer);
		transferQueueService.setTransferAsCancelled(enqueuedTransfer);

		List<LocalIRODSTransfer> transferQueue = transferQueueService
				.getRecentQueue();
		Assert.assertTrue(transferQueue.size() > 0);

	}

	@Test
	public void testRestartClearsErrorAndStackTrace() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();

		LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath("path");
		enqueuedTransfer.setLocalAbsolutePath("localPath");
		enqueuedTransfer.setLastSuccessfulPath("lastSuccessfulPath");
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(irodsAccount
				.getDefaultStorageResource());
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
		enqueuedTransfer.setTransferState(TransferState.COMPLETE);
		enqueuedTransfer.setTransferStatus(TransferStatus.ERROR);
		enqueuedTransfer.setGlobalException("exception");
		enqueuedTransfer.setGlobalExceptionStackTrace("stack trace");

		transferQueueService.updateLocalIRODSTransfer(enqueuedTransfer);
		transferQueueService.restartTransfer(enqueuedTransfer);

		LocalIRODSTransfer dequeuedTransfer = transferQueueService
				.dequeueTransfer();

		TestCase.assertEquals("should be processing", TransferState.PROCESSING,
				dequeuedTransfer.getTransferState());
		TestCase.assertTrue("should not have an error", dequeuedTransfer
				.getGlobalException().isEmpty());
		TestCase.assertTrue("should have no stack trace", dequeuedTransfer
				.getGlobalExceptionStackTrace().isEmpty());
	}

	@Test
	public void testRestartPreservesLastGoodPath() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();

		final LocalIRODSTransfer enqueuedTransfer;
		enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath("path");
		enqueuedTransfer.setLocalAbsolutePath("localPath");
		enqueuedTransfer.setLastSuccessfulPath("lastSuccessfulPath");
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(irodsAccount
				.getDefaultStorageResource());
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
		enqueuedTransfer.setTransferState(TransferState.COMPLETE);
		enqueuedTransfer.setTransferStatus(TransferStatus.ERROR);
		enqueuedTransfer.setGlobalException("exception");
		enqueuedTransfer.setGlobalExceptionStackTrace("stack trace");

		transferQueueService.updateLocalIRODSTransfer(enqueuedTransfer);

		transferQueueService.restartTransfer(enqueuedTransfer);

		LocalIRODSTransfer dequeuedTransfer = transferQueueService
				.dequeueTransfer();

		TestCase.assertEquals("should have retained last good path",
				"lastSuccessfulPath", dequeuedTransfer.getLastSuccessfulPath());
	}

	@Test
	public void testResubmitClearsLastGoodPath() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();

		LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath("path");
		enqueuedTransfer.setLocalAbsolutePath("localPath");
		enqueuedTransfer.setLastSuccessfulPath("lastSuccessfulPath");
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(irodsAccount
				.getDefaultStorageResource());
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
		enqueuedTransfer.setTransferState(TransferState.COMPLETE);
		enqueuedTransfer.setTransferStatus(TransferStatus.ERROR);
		enqueuedTransfer.setGlobalException("exception");
		enqueuedTransfer.setGlobalExceptionStackTrace("stack trace");

		transferQueueService.updateLocalIRODSTransfer(enqueuedTransfer);

		transferQueueService.resubmitTransfer(enqueuedTransfer);

		LocalIRODSTransfer dequeuedTransfer = transferQueueService
				.dequeueTransfer();

		TestCase.assertTrue("should not have retained last good path",
				dequeuedTransfer.getLastSuccessfulPath().isEmpty());
	}

	@Test
	public void testUpdateUserPasswordInTransferManagerDataNothingInQueue() throws Exception {
		
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		
		String newPassword = "password";
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();
		transferQueueService.purgeEntireQueue();
		
		transferQueueService.updateUserPasswordInTransferManagerData(irodsAccount, newPassword);
		
		TestCase.assertTrue(true);
		
	}
	
	@Test
	public void testUpdateUserPasswordInTransferManagerDataOneCompletedInQueue() throws Exception {
		
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		
		String newPassword = "password";
		String encryptedNewPassword = HibernateUtil.obfuscate(newPassword);
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();
		transferQueueService.purgeEntireQueue();
		
		LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath("path");
		enqueuedTransfer.setLocalAbsolutePath("localPath");
		enqueuedTransfer.setLastSuccessfulPath("lastSuccessfulPath");
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(irodsAccount
				.getDefaultStorageResource());
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
		enqueuedTransfer.setTransferState(TransferState.COMPLETE);
		enqueuedTransfer.setTransferStatus(TransferStatus.OK);
		
		transferQueueService.updateLocalIRODSTransfer(enqueuedTransfer);
		transferQueueService.updateUserPasswordInTransferManagerData(irodsAccount, newPassword);
		
		List<LocalIRODSTransfer> actualTransfers = transferQueueService.getRecentQueue();
		TestCase.assertEquals("should be one in queue", 1, actualTransfers.size());
		LocalIRODSTransfer actualTransfer = actualTransfers.get(0);
		TestCase.assertEquals("should have new password", encryptedNewPassword, actualTransfer.getTransferPassword());
		
	}
	
	@Test(expected=CannotUpdateTransferInProgressException.class)
	public void testUpdateUserPasswordInTransferManagerDataOneEnqueuedInQueue() throws Exception {
		
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTertiaryTestProperties(testingProperties);
		
		String newPassword = "password";
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();
		transferQueueService.purgeEntireQueue();
		
		LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath("path");
		enqueuedTransfer.setLocalAbsolutePath("localPath");
		enqueuedTransfer.setLastSuccessfulPath("lastSuccessfulPath");
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(irodsAccount
				.getDefaultStorageResource());
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
		enqueuedTransfer.setTransferState(TransferState.ENQUEUED);
		enqueuedTransfer.setTransferStatus(TransferStatus.OK);
		
		transferQueueService.updateLocalIRODSTransfer(enqueuedTransfer);
		transferQueueService.updateUserPasswordInTransferManagerData(irodsAccount, newPassword);
		
		
		
		
	}


}
