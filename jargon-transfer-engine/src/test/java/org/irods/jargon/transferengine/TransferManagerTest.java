package org.irods.jargon.transferengine;

import java.io.File;
import java.io.FileReader;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.transferengine.domain.LocalIRODSTransfer;
import org.irods.jargon.transferengine.domain.LocalIRODSTransferItem;
import org.irods.jargon.transferengine.util.HibernateUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
  
public class TransferManagerTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "TransferManagerTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static org.irods.jargon.testutils.AssertionHelper assertionHelper = null;
	private static IDatabaseTester databaseTester;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(
				testingProperties);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		try {
			irodsTestSetupUtilities.initializeIrodsScratchDirectory();
			irodsTestSetupUtilities
					.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
			scratchFileUtils.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
			DatabasePreparationUtils.makeSureDatabaseIsInitialized();

		} catch (Exception e) {
			// ignore for now, need to rework test cases to allow for threads
			// that need to run
		}
		assertionHelper = new org.irods.jargon.testutils.AssertionHelper();
	}

	@Test
	public void testInstance() throws Exception {
		TransferManager transferManager = TransferManager.instance();
		TestCase.assertNotNull("null transferManager from instance()",
				transferManager);
	}

	/**
	 * @throws Exception
	 */
	@Before
	public void setUpEach() throws Exception {
		databaseTester = new JdbcDatabaseTester(
				"org.apache.derby.jdbc.EmbeddedDriver",
				"jdbc:derby:target/transferDatabase", "transfer", "transfer");

		IDataSet ds = new XmlDataSet(new FileReader("data/export-empty.xml"));
		DatabaseOperation.CLEAN_INSERT.execute(databaseTester.getConnection(),
				ds);
		databaseTester.closeConnection(databaseTester.getConnection());
	}

	@Test
	public void testPause() throws Exception {
		TransferManager transferManager = TransferManager.instance();
		transferManager.pause();
		TestCase.assertTrue(transferManager.isPaused());
	}

	@Test
	public void testResumeNotPaused() throws Exception {
		TransferManager transferManager = TransferManager.instance();
		transferManager.resume();
		TestCase
				.assertFalse(transferManager.getRunningStatus() == TransferManager.RunningStatus.PAUSED);
	}

	@Test
	public void testResumeWhenPaused() throws Exception {
		TransferManager transferManager = TransferManager.instance();
		transferManager.pause();
		transferManager.resume();
		TestCase
				.assertFalse(
						"transferManager should not be paused",
						transferManager.getRunningStatus() == TransferManager.RunningStatus.PAUSED);
	}

	@Test
	public void testNotifyWarningCondition() throws Exception {
		TransferManager transferManager = TransferManager.instance();
		transferManager.notifyWarningCondition();
		TestCase.assertEquals("transferManager should be in a warning state",
				TransferManager.ErrorStatus.WARNING, transferManager
						.getErrorStatus());
	}

	@Test
	public void testNotifyProcessingCondition() throws Exception {
		TransferManager transferManager = TransferManager.instance();
		transferManager.notifyProcessing();
		TestCase.assertEquals(
				"transferManager should be in a processing state",
				TransferManager.RunningStatus.PROCESSING, transferManager
						.getRunningStatus());
	}

	@Test
	public void testNotifyWarningConditionWhenAlreadyError() throws Exception {
		TransferManager transferManager = TransferManager.instance();
		transferManager.notifyErrorCondition();
		transferManager.notifyWarningCondition();
		TestCase.assertEquals(
				"transferManager should still be in an error state",
				TransferManager.ErrorStatus.ERROR, transferManager
						.getErrorStatus());
	}

	@Test
	public void testNotifyErrorCondition() throws Exception {
		TransferManager transferManager = TransferManager.instance();
		transferManager.notifyErrorCondition();
		TestCase.assertEquals("transferManager should be in an error state",
				TransferManager.ErrorStatus.ERROR, transferManager
						.getErrorStatus());
	}

	@Test
	public void enqueueAPutWhenIdleBlankResource() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);

		String rootCollection = "testGetTransferOneInQueue";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath, "testSubdir", 1, 2, 1,
						"testFile", ".txt", 9, 8, 2, 21);

		TransferManager transferManager = TransferManager.instance();
		transferManager.pause();

		transferManager.enqueueAPut(localCollectionAbsolutePath,
				irodsCollectionRootAbsolutePath, "", irodsAccount);

		TestCase.assertEquals("should have been no errors",
				TransferManager.ErrorStatus.OK, transferManager
						.getErrorStatus());
		// FIXME: not a good test, need to get some functional tests going...
	}

	@Test
	public void testEnqueueAPutWhenPaused() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);

		String rootCollection = "enqueueAPutWhenPaused";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath, "testSubdir", 1, 2, 1,
						"testFile", ".txt", 9, 8, 2, 21);

		TransferManager transferManager = TransferManager.instance();
		transferManager.pause();

		transferManager.enqueueAPut(localCollectionAbsolutePath,
				irodsCollectionRootAbsolutePath, "", irodsAccount);

		TestCase.assertEquals("should be finished and idle",
				TransferManager.RunningStatus.PAUSED, transferManager
						.getRunningStatus());
		TestCase.assertEquals("should have been no errors",
				TransferManager.ErrorStatus.OK, transferManager
						.getErrorStatus());
		TransferQueueService transferQueueService = TransferQueueService
				.instance();

		List<LocalIRODSTransfer> transferQueue = transferQueueService
				.getCurrentQueue();
		TestCase.assertEquals(1, transferQueue.size());
		LocalIRODSTransfer enqueuedTransfer = transferQueue.get(0);
		TestCase
				.assertEquals("this should still be enqueued", enqueuedTransfer
						.getTransferState(),
						LocalIRODSTransfer.TRANSFER_STATE_ENQUEUED);

	}

	@Test
	public void testEnqueueAPutWithRestart() throws Exception {
		
		System.out.println(">>>>>>>> testEnqueueAPutWithRestart");
		
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);

		String rootCollection = "testEnqueueAPutWithRestart";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		

		FileGenerator.generateManyFilesInGivenDirectory(
				IRODS_TEST_SUBDIR_PATH
				+ '/' + rootCollection, "test", ".doc", 20, 1, 2);
		
		// find the 3rd file and make it the last good file
		
		File localDirectory = new File(localCollectionAbsolutePath);
		File[] localFiles = localDirectory.listFiles();
		
		TestCase.assertTrue("not enough files generated for the test", localFiles.length > 3);
		
		String lastFileName = localFiles[2].getAbsolutePath();

		TransferQueueService transferQueueService = TransferQueueService
				.instance();

		TransferManager transferManager = TransferManager.instance();

		final Session session =transferManager.getTransferQueueService().getHibernateUtil().getSession();


		Transaction tx = null;
		final LocalIRODSTransfer enqueuedTransfer;
		LocalIRODSTransferItem localIRODSTransferItem;
		try {
			tx = session.beginTransaction();
			enqueuedTransfer = new LocalIRODSTransfer();
			enqueuedTransfer.setCreatedAt(new Date());
			enqueuedTransfer
					.setIrodsAbsolutePath(irodsCollectionRootAbsolutePath);
			enqueuedTransfer.setLocalAbsolutePath(localCollectionAbsolutePath);
			enqueuedTransfer.setTransferHost(irodsAccount.getHost());
			enqueuedTransfer.setTransferPort(irodsAccount.getPort());
			enqueuedTransfer.setTransferResource(irodsAccount
					.getDefaultStorageResource());
			enqueuedTransfer.setTransferZone(irodsAccount.getZone());
			enqueuedTransfer.setTransferStart(new Date());
			enqueuedTransfer.setTransferType("PUT");
			enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
			enqueuedTransfer.setTransferPassword(HibernateUtil.obfuscate(irodsAccount.getPassword()));
			enqueuedTransfer
					.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_ENQUEUED);
			enqueuedTransfer
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_OK);
			enqueuedTransfer.setLastSuccessfulPath(lastFileName);

			session.save(enqueuedTransfer);

			session.flush();
			tx.commit();
		} catch (RuntimeException e) {

			if (tx != null)
				tx.rollback();

			throw new JargonException(e);
		} finally {
			//session.close();
		}
		
		// let put run
		
		transferManager.processNextInQueueIfIdle();

		int waitCtr = 0;

		while (true) {
			if (waitCtr++ > 20) {
				TestCase.fail("put test timed out");
			}
			Thread.sleep(1000);
			if (transferManager.getRunningStatus() == TransferManager.RunningStatus.IDLE) {
				break;
			}

		}

		TestCase.assertEquals("should have been no errors",
				TransferManager.ErrorStatus.OK, transferManager
						.getErrorStatus());
		
		// now make sure that the first 3 files were not put
		IRODSFile testDir = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(irodsCollectionRootAbsolutePath + '/' + rootCollection);
		File[] irodsFilesThatWerePut = testDir.listFiles();
		System.out.println(">>>>>> irodsFilesThatWerePut:" + irodsFilesThatWerePut);
		IRODSFile actualIrodsFile = null;
		
		for (File irodsFile : irodsFilesThatWerePut) {
			actualIrodsFile = (IRODSFile) irodsFile;
			
			System.out.println("IRODS file:" + actualIrodsFile.getAbsolutePath());
			
			for (int i = 0; i < 2; i++) {
				if (irodsFile.getName().equals(localFiles[i].getName())) {
					TestCase.fail("file should not have been transferred:" + irodsFile.getAbsolutePath());
				}
			}
		}
		
		TestCase.assertEquals("correct number of files were not transferred", localFiles.length - 3, irodsFilesThatWerePut.length);

	}

	@Test
	public void testEnqueueAPutTwice() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);

		String rootCollection = "testEnqueueAPutTwice";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath, "testSubdir", 1, 2, 1,
						"testFile", ".txt", 9, 8, 2, 21);

		TransferManager transferManager = TransferManager.instance();
		transferManager.pause();

		transferManager.enqueueAPut(localCollectionAbsolutePath,
				irodsCollectionRootAbsolutePath, "", irodsAccount);
		transferManager.enqueueAPut(localCollectionAbsolutePath,
				irodsCollectionRootAbsolutePath, "", irodsAccount);

		List<LocalIRODSTransfer> transferQueue = transferManager
				.getCurrentQueue();
		TestCase.assertEquals(2, transferQueue.size());

	}

	@Test
	public void enqueueAReplicate() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);

		String rootCollection = "enqueueAReplicate";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath, "testSubdir", 1, 2, 1,
						"testFile", ".txt", 9, 8, 2, 21);

		TransferManager transferManager = TransferManager.instance();
		// transferManager.pause();

		transferManager.enqueueAPut(localCollectionAbsolutePath,
				irodsCollectionRootAbsolutePath, "", irodsAccount);

		// let put run

		int waitCtr = 0;

		while (true) {
			if (waitCtr++ > 20) {
				TestCase.fail("put test timed out");
			}
			Thread.sleep(1000);
			if (transferManager.getRunningStatus() == TransferManager.RunningStatus.IDLE) {
				break;
			}

		}

		TestCase.assertEquals("should have been no errors",
				TransferManager.ErrorStatus.OK, transferManager
						.getErrorStatus());

		// put is done, now replicate

		transferManager
				.enqueueAReplicate(
						irodsCollectionRootAbsolutePath + "/" + rootCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY),
						irodsAccount);

		waitCtr = 0;

		while (true) {
			if (waitCtr++ > 20) {
				TestCase.fail("replicate test timed out");
			}
			Thread.sleep(1000);
			if (transferManager.getRunningStatus().equals(
					TransferManager.RunningStatus.IDLE)) {
				break;
			}
		}

		TestCase.assertEquals("should have been no errors",
				TransferManager.ErrorStatus.OK, transferManager
						.getErrorStatus());
		
	}
	
	@Test
	public void enqueueAGet() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);

		String rootCollection = "enqueueAGet";
		String returnedCollection = "enqueueAGeReturned";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath, "testSubdir", 1, 2, 1,
						"testFile", ".txt", 9, 8, 2, 21);

		TransferManager transferManager = TransferManager.instance();
		// transferManager.pause();

		transferManager.enqueueAPut(localCollectionAbsolutePath,
				irodsCollectionRootAbsolutePath, "", irodsAccount);

		// let put run

		int waitCtr = 0;

		while (true) {
			if (waitCtr++ > 20) {
				TestCase.fail("put test timed out");
			}
			Thread.sleep(1000);
			if (transferManager.getRunningStatus() == TransferManager.RunningStatus.IDLE) {
				break;
			}

		}

		TestCase.assertEquals("should have been no errors",
				TransferManager.ErrorStatus.OK, transferManager
						.getErrorStatus());

		// put is done, now get
		
		String localReturnedAbsolutePath = scratchFileUtils
		.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		localReturnedAbsolutePath = localReturnedAbsolutePath + returnedCollection;
			
		transferManager.enqueueAGet(irodsCollectionRootAbsolutePath + "/" + rootCollection, localReturnedAbsolutePath, testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), irodsAccount);
		
		waitCtr = 0;

		while (true) {
			if (waitCtr++ > 20) {
				TestCase.fail("get test timed out");
			}
			Thread.sleep(1000);
			if (transferManager.getRunningStatus().equals(
					TransferManager.RunningStatus.IDLE)) {
				break;
			}
		}

		TestCase.assertEquals("should have been no errors",
				TransferManager.ErrorStatus.OK, transferManager
						.getErrorStatus());
		
	}
	
	
	@Test
	public void testProcessQueueAtStartupWithAProcessingTransferHanging() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String rootCollection = "testGetErrorQueue";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		TransferQueueService transferQueueService = TransferQueueService
				.instance();

		TransferManager transferManager = TransferManager.instance();

		final Session session =transferManager.getTransferQueueService().getHibernateUtil().getSession();

		Transaction tx = null;
		LocalIRODSTransfer enqueuedTransfer;
		try {
			tx = session.beginTransaction();
			enqueuedTransfer = new LocalIRODSTransfer();
			enqueuedTransfer.setCreatedAt(new Date());
			enqueuedTransfer
					.setIrodsAbsolutePath(irodsCollectionRootAbsolutePath);
			enqueuedTransfer.setLocalAbsolutePath(localCollectionAbsolutePath);
			enqueuedTransfer.setTransferHost(irodsAccount.getHost());
			enqueuedTransfer.setTransferPort(irodsAccount.getPort());
			enqueuedTransfer.setTransferResource(irodsAccount
					.getDefaultStorageResource());
			enqueuedTransfer.setTransferZone(irodsAccount.getZone());
			enqueuedTransfer.setTransferStart(new Date());
			enqueuedTransfer.setTransferType("PUT");
			enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
			enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
			enqueuedTransfer
					.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_PROCESSING);
			enqueuedTransfer
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_ERROR);

			session.save(enqueuedTransfer);
			session.flush();
			tx.commit();
		} catch (RuntimeException e) {

			if (tx != null)
				tx.rollback();

			throw new JargonException(e);
		} finally {
			//session.close();
		}
		
		transferManager = TransferManager.instance();
		
		// now get the queue
		List<LocalIRODSTransfer> transferQueue = transferQueueService
				.getCurrentQueue();
		Assert.assertEquals("did not find the error transaction", 1, transferQueue
				.size());
		LocalIRODSTransfer transfer = transferQueue.get(0);
		Assert.assertEquals("this does not have enqueued status",
				LocalIRODSTransfer.TRANSFER_STATE_ENQUEUED, transfer.getTransferState());

	}
	
}
