package org.irods.jargon.transferengine;

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
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.transfer.TransferStatus.TransferType;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.transferengine.domain.LocalIRODSTransfer;
import org.irods.jargon.transferengine.domain.LocalIRODSTransferItem;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TransferQueueServiceTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "TransferQueueServiceTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IDatabaseTester databaseTester;

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
		DatabasePreparationUtils.makeSureDatabaseIsInitialized();
		databaseTester = new JdbcDatabaseTester(
				"org.apache.derby.jdbc.EmbeddedDriver",
				"jdbc:derby:target/transferDatabase", "transfer", "transfer");
	}

	@Before
	public void setUpEach() throws Exception {

		IDataSet ds = new XmlDataSet(new FileReader("data/export-empty.xml"));
		DatabaseOperation.CLEAN_INSERT.execute(databaseTester.getConnection(),
				ds);
		databaseTester.closeConnection(databaseTester.getConnection());
	}

	@Test
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

		TransferQueueService transferQueueService = TransferQueueService
				.instance();

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
				enqueuedTransfer.getTransferState(),
				LocalIRODSTransfer.TRANSFER_STATE_ENQUEUED);
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

		TransferQueueService transferQueueService = TransferQueueService
				.instance();

		final Session session = transferQueueService.getHibernateUtil()
				.getSession();

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
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_WARNING);

			session.save(enqueuedTransfer);
			session.flush();
			tx.commit();
		} catch (RuntimeException e) {

			if (tx != null) {
				tx.rollback();
			}

			throw new JargonException(e);
		} finally {
			// session.close();
		}

		// now get the queue
		List<LocalIRODSTransfer> errorQueue = transferQueueService
				.getErrorQueue();
		Assert.assertEquals("did not find the error transaction", 1,
				errorQueue.size());
		LocalIRODSTransfer errorTransfer = errorQueue.get(0);
		Assert.assertEquals("this does not have error status",
				LocalIRODSTransfer.TRANSFER_STATUS_ERROR,
				errorTransfer.getTransferErrorStatus());

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

		TransferQueueService transferQueueService = TransferQueueService
				.instance();

		final Session session = transferQueueService.getHibernateUtil()
				.getSession();

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
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_WARNING);

			session.save(enqueuedTransfer);
			session.flush();

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
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_OK);

			session.save(enqueuedTransfer);
			session.flush();
			tx.commit();
		} catch (RuntimeException e) {

			if (tx != null) {
				tx.rollback();
			}

			throw new JargonException(e);
		} finally {
			// session.close();
		}

		// now get the queue
		List<LocalIRODSTransfer> errorQueue = transferQueueService
				.getWarningQueue();
		Assert.assertEquals("did not find the error transaction", 1,
				errorQueue.size());
		LocalIRODSTransfer errorTransfer = errorQueue.get(0);
		Assert.assertEquals("this does not have error status",
				LocalIRODSTransfer.TRANSFER_STATUS_WARNING,
				errorTransfer.getTransferErrorStatus());

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

		TransferQueueService transferQueueService = TransferQueueService
				.instance();

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

		TransferQueueService transferQueueService = TransferQueueService
				.instance();

		TransferManager transferManager = TransferManagerImpl.instance();

		transferQueueService.enqueuePutTransfer(localCollectionAbsolutePath,
				irodsCollectionRootAbsolutePath, "", irodsAccount);
		List<LocalIRODSTransfer> transferQueue = transferQueueService
				.getLastNInQueue(1);

		LocalIRODSTransfer transferToMark = transferQueue.get(0);
		transferQueueService.markTransferAsErrorAndTerminate(transferToMark,
				transferManager);

		// now get the error transfers, there should be one and it should be the
		// one I marked

		List<LocalIRODSTransfer> errorTransfers = transferQueueService
				.showErrorTransfers();

		Assert.assertEquals("should have 1 error transfer", 1,
				errorTransfers.size());
	}

	@Test
	public void testRestartATransfer() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		TransferQueueService transferQueueService = TransferQueueService
				.instance();
		final Session session = transferQueueService.getHibernateUtil()
				.getSession();

		Transaction tx = null;
		final LocalIRODSTransfer enqueuedTransfer;
		LocalIRODSTransferItem localIRODSTransferItem;
		try {
			tx = session.beginTransaction();
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
			enqueuedTransfer.setTransferType("PUT");
			enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
			enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
			enqueuedTransfer
					.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_COMPLETE);
			enqueuedTransfer
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_ERROR);

			session.save(enqueuedTransfer);

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
			session.save(enqueuedTransfer);

			tx.commit();
		} catch (RuntimeException e) {

			if (tx != null) {
				tx.rollback();
			}

			throw new JargonException(e);
		} finally {
			// session.close();
		}

		transferQueueService.restartTransfer(enqueuedTransfer);

		List<LocalIRODSTransfer> transferQueue = transferQueueService
				.getCurrentQueue();
		Assert.assertEquals(1, transferQueue.size());
		LocalIRODSTransfer actualEnqueuedTransfer = transferQueue.get(0);
		Assert.assertEquals("this should still be enqueued",
				actualEnqueuedTransfer.getTransferState(),
				LocalIRODSTransfer.TRANSFER_STATE_ENQUEUED);

		Assert.assertEquals("this should still be ok status now",
				actualEnqueuedTransfer.getTransferErrorStatus(),
				LocalIRODSTransfer.TRANSFER_STATUS_OK);
	}

	@Test
	public void testMarkTransferAsErrorAndTerminatePassingException()
			throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String rootCollection = "enqueueAPutWhenPaused";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		TransferQueueService transferQueueService = TransferQueueService
				.instance();

		transferQueueService.enqueuePutTransfer(localCollectionAbsolutePath,
				irodsCollectionRootAbsolutePath, "", irodsAccount);
		List<LocalIRODSTransfer> transferQueue = transferQueueService
				.getLastNInQueue(1);

		LocalIRODSTransfer transferToMark = transferQueue.get(0);
		TransferManager transferManager = TransferManagerImpl.instance();
		transferQueueService.markTransferAsErrorAndTerminate(transferToMark,
				new JargonException("hello a jargon exception"),
				transferManager);

		// now get the error transfers, there should be one and it should be the
		// one I marked

		List<LocalIRODSTransfer> errorTransfers = transferQueueService
				.showErrorTransfers();

		Assert.assertEquals("should have 1 error transfer", 1,
				errorTransfers.size());

		LocalIRODSTransfer actualTransfer = errorTransfers.get(0);
		Assert.assertEquals("did not retain the exception",
				"hello a jargon exception", actualTransfer.getGlobalException());

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

		TransferQueueService transferQueueService = TransferQueueService
				.instance();
		final Session session = transferQueueService.getHibernateUtil()
				.getSession();

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
			enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
			enqueuedTransfer
					.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_ENQUEUED);
			enqueuedTransfer
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_OK);

			session.save(enqueuedTransfer);

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

			session.flush();
			tx.commit();
		} catch (RuntimeException e) {

			if (tx != null) {
				tx.rollback();
			}

			throw new JargonException(e);
		} finally {
			// session.close();
		}

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

		TransferQueueService transferQueueService = TransferQueueService
				.instance();
		final Session session = transferQueueService.getHibernateUtil()
				.getSession();

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
			enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
			enqueuedTransfer
					.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_ENQUEUED);
			enqueuedTransfer
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_OK);

			session.save(enqueuedTransfer);

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

			session.flush();
			tx.commit();
		} catch (RuntimeException e) {

			if (tx != null) {
				tx.rollback();
			}

			throw new JargonException(e);
		} finally {
			// session.close();
		}

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

		TransferQueueService transferQueueService = TransferQueueService
				.instance();
		final Session session = transferQueueService.getHibernateUtil()
				.getSession();

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
			enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
			enqueuedTransfer
					.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_ENQUEUED);
			enqueuedTransfer
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_OK);

			session.save(enqueuedTransfer);

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
			localIRODSTransferItem.setError(true);
			localIRODSTransferItem.setFile(true);
			localIRODSTransferItem.setSourceFileAbsolutePath("/sourcepath2");
			localIRODSTransferItem.setTargetFileAbsolutePath("/targetpath2");
			localIRODSTransferItem.setTransferredAt(new Date());
			localIRODSTransferItem.setLocalIRODSTransfer(enqueuedTransfer);
			enqueuedTransfer.getLocalIRODSTransferItems().add(
					localIRODSTransferItem);

			session.flush();
			tx.commit();
		} catch (RuntimeException e) {

			if (tx != null) {
				tx.rollback();
			}

			throw new JargonException(e);
		} finally {
			// session.close();
		}

		// now get all items for the transfer
		List<LocalIRODSTransferItem> transferItems = transferQueueService
				.getErrorTransferItemsForTransfer(enqueuedTransfer.getId());
		irodsFileSystem.close();
		Assert.assertEquals("did not get the error transfer item", 1,
				transferItems.size());

	}

	@Test
	public void testEnqueueReplicate() throws Exception {
		TransferQueueService transferQueueService = TransferQueueService
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		transferQueueService.enqueueReplicateTransfer("irodsAbsolutePath",
				"targetResource", irodsAccount);

		// now get the data from the database

		List<LocalIRODSTransfer> queue = transferQueueService.getCurrentQueue();
		TestCase.assertEquals(
				"should just be 1 replicate transfer in the queue", 1,
				queue.size());
		LocalIRODSTransfer actualTransfer = queue.get(0);

		TestCase.assertEquals("irodsAbsolutePath",
				actualTransfer.getIrodsAbsolutePath());
		TestCase.assertEquals("targetResource",
				actualTransfer.getTransferResource());
		TestCase.assertEquals(TransferType.REPLICATE.name(),
				actualTransfer.getTransferType());
	}

	@Test
	public void testEnqueueGet() throws Exception {
		TransferQueueService transferQueueService = TransferQueueService
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		transferQueueService.enqueueGetTransfer("irodsSourceAbsolutePath",
				"targetLocalAbsolutePath", "sourceResource", irodsAccount);

		// now get the data from the database

		List<LocalIRODSTransfer> queue = transferQueueService.getCurrentQueue();
		TestCase.assertEquals(
				"should just be 1 replicate transfer in the queue", 1,
				queue.size());
		LocalIRODSTransfer actualTransfer = queue.get(0);

		TestCase.assertEquals("irodsSourceAbsolutePath",
				actualTransfer.getIrodsAbsolutePath());
		TestCase.assertEquals("sourceResource",
				actualTransfer.getTransferResource());
		TestCase.assertEquals(TransferType.GET.name(),
				actualTransfer.getTransferType());
	}

	@Test
	public void testEnqueueGetBigFileName() throws Exception {
		TransferQueueService transferQueueService = TransferQueueService
				.instance();
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

		List<LocalIRODSTransfer> queue = transferQueueService.getCurrentQueue();
		TestCase.assertEquals(
				"should just be 1 replicate transfer in the queue", 1,
				queue.size());
		LocalIRODSTransfer actualTransfer = queue.get(0);

		TestCase.assertEquals(sb.toString(),
				actualTransfer.getIrodsAbsolutePath());
		TestCase.assertEquals("sourceResource",
				actualTransfer.getTransferResource());
		TestCase.assertEquals(TransferType.GET.name(),
				actualTransfer.getTransferType());
	}

	@Test(expected = JargonException.class)
	public void testEnqueueGetNoSource() throws Exception {
		TransferQueueService transferQueueService = TransferQueueService
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		transferQueueService.enqueueGetTransfer("", "targetLocalAbsolutePath",
				"sourceResource", irodsAccount);
	}

	@Test(expected = JargonException.class)
	public void testEnqueueGetNoTarget() throws Exception {
		TransferQueueService transferQueueService = TransferQueueService
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		transferQueueService.enqueueGetTransfer("source", "", "sourceResource",
				irodsAccount);
	}

	@Test(expected = JargonException.class)
	public void testEnqueueGetNullTarget() throws Exception {
		TransferQueueService transferQueueService = TransferQueueService
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		transferQueueService.enqueueGetTransfer("source", null,
				"sourceResource", irodsAccount);
	}

	@Test(expected = JargonException.class)
	public void testEnqueueGetNullResource() throws Exception {
		TransferQueueService transferQueueService = TransferQueueService
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		transferQueueService.enqueueGetTransfer("source", "target", null,
				irodsAccount);
	}

	@Test(expected = JargonException.class)
	public void testEnqueueGetNullSource() throws Exception {
		TransferQueueService transferQueueService = TransferQueueService
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		transferQueueService.enqueueGetTransfer(null,
				"targetLocalAbsolutePath", "sourceResource", irodsAccount);
	}

	@Test(expected = JargonException.class)
	public void testEnqueueReplicateNoPath() throws Exception {
		TransferQueueService transferQueueService = TransferQueueService
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		transferQueueService.enqueueReplicateTransfer("", "targetResource",
				irodsAccount);
	}

	@Test(expected = JargonException.class)
	public void testEnqueueReplicateNullResource() throws Exception {
		TransferQueueService transferQueueService = TransferQueueService
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		transferQueueService.enqueueReplicateTransfer("hello", null,
				irodsAccount);
	}

	@Test(expected = JargonException.class)
	public void testEnqueueReplicateNullPath() throws Exception {
		TransferQueueService transferQueueService = TransferQueueService
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		transferQueueService.enqueueReplicateTransfer(null, "targetResource",
				irodsAccount);
	}

	@Test
	public void testPurgeAll() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		TransferQueueService transferQueueService = TransferQueueService
				.instance();
		final Session session = transferQueueService.getHibernateUtil()
				.getSession();

		Transaction tx = null;
		LocalIRODSTransfer enqueuedTransfer;
		try {
			tx = session.beginTransaction();

			enqueuedTransfer = new LocalIRODSTransfer();
			enqueuedTransfer.setCreatedAt(new Date());
			enqueuedTransfer.setIrodsAbsolutePath("path1");
			enqueuedTransfer.setLocalAbsolutePath("path1");
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
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_OK);

			session.save(enqueuedTransfer);

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
			enqueuedTransfer.setTransferType("PUT");
			enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
			enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
			enqueuedTransfer
					.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_COMPLETE);
			enqueuedTransfer
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_OK);

			session.save(enqueuedTransfer);

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
			enqueuedTransfer.setTransferType("PUT");
			enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
			enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
			enqueuedTransfer
					.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_COMPLETE);
			enqueuedTransfer
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_ERROR);

			session.save(enqueuedTransfer);
			session.flush();
			tx.commit();

			// now purge
			transferQueueService.purgeQueue();

			List<LocalIRODSTransfer> actualTransfers = transferQueueService
					.getRecentQueue();

			TestCase.assertEquals("did not get the 1 processing transfer", 1,
					actualTransfers.size());
			LocalIRODSTransfer actualTransfer = actualTransfers.get(0);
			TestCase.assertEquals(
					"the one transfer should be the processing item",
					LocalIRODSTransfer.TRANSFER_STATE_PROCESSING,
					actualTransfer.getTransferState());

		} catch (RuntimeException e) {

			if (tx != null) {
				tx.rollback();
			}

			throw new JargonException(e);
		} finally {
			// session.close();
		}
	}

	@Test
	public void testPurgeComplete() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		TransferQueueService transferQueueService = TransferQueueService
				.instance();
		final Session session = transferQueueService.getHibernateUtil()
				.getSession();

		Transaction tx = null;
		LocalIRODSTransfer enqueuedTransfer;
		try {
			tx = session.beginTransaction();

			enqueuedTransfer = new LocalIRODSTransfer();
			enqueuedTransfer.setCreatedAt(new Date());
			enqueuedTransfer.setIrodsAbsolutePath("path1");
			enqueuedTransfer.setLocalAbsolutePath("path1");
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
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_OK);

			session.save(enqueuedTransfer);

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
			enqueuedTransfer.setTransferType("PUT");
			enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
			enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
			enqueuedTransfer
					.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_COMPLETE);
			enqueuedTransfer
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_OK);

			session.save(enqueuedTransfer);

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
			enqueuedTransfer.setTransferType("PUT");
			enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
			enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
			enqueuedTransfer
					.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_COMPLETE);
			enqueuedTransfer
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_ERROR);

			session.save(enqueuedTransfer);
			session.flush();
			tx.commit();

			// now purge
			transferQueueService.purgeQueue();

			List<LocalIRODSTransfer> actualTransfers = transferQueueService
					.getRecentQueue();

			TestCase.assertEquals("did not get the 1 processing transfer", 1,
					actualTransfers.size());
			LocalIRODSTransfer actualTransfer = actualTransfers.get(0);
			TestCase.assertEquals(
					"the one transfer should be the processing item",
					LocalIRODSTransfer.TRANSFER_STATE_PROCESSING,
					actualTransfer.getTransferState());

		} catch (RuntimeException e) {

			if (tx != null) {
				tx.rollback();
			}

			throw new JargonException(e);
		} finally {
			// session.close();
		}

	}

	@Test
	public void testResubmitATransfer() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		TransferQueueService transferQueueService = TransferQueueService
				.instance();
		Session session = transferQueueService.getHibernateUtil().getSession();

		Transaction tx = null;
		final LocalIRODSTransfer enqueuedTransfer;
		LocalIRODSTransferItem localIRODSTransferItem;
		try {
			tx = session.beginTransaction();
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
			enqueuedTransfer.setTransferType("PUT");
			enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
			enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
			enqueuedTransfer
					.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_COMPLETE);
			enqueuedTransfer
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_ERROR);

			session.save(enqueuedTransfer);

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
			session.save(enqueuedTransfer);

			tx.commit();
		} catch (RuntimeException e) {

			if (tx != null) {
				tx.rollback();
			}

			throw new JargonException(e);
		} finally {
			// session.close();
		}

		transferQueueService.resubmitTransfer(enqueuedTransfer);

		List<LocalIRODSTransfer> transferQueue = transferQueueService
				.getCurrentQueue();
		Assert.assertEquals(1, transferQueue.size());
		LocalIRODSTransfer actualEnqueuedTransfer = transferQueue.get(0);
		Assert.assertEquals("this should still be enqueued",
				actualEnqueuedTransfer.getTransferState(),
				LocalIRODSTransfer.TRANSFER_STATE_ENQUEUED);

		session = transferQueueService.getHibernateUtil().getSession();
		// get the items for the transfer, they should be now empty
		try {
			tx = session.beginTransaction();
			session.lock(actualEnqueuedTransfer, LockMode.NONE);
			Assert.assertEquals(
					"the items should have been removed on a resubmit", 0,
					actualEnqueuedTransfer.getLocalIRODSTransferItems().size());
			tx.commit();
		} catch (RuntimeException e) {

			if (tx != null) {
				tx.rollback();
			}

			throw new JargonException(e);
		} finally {
			// session.close();
		}

		Assert.assertEquals("this should still be ok status now",
				actualEnqueuedTransfer.getTransferErrorStatus(),
				LocalIRODSTransfer.TRANSFER_STATUS_OK);
	}

	@Test
	public void testCancelATransfer() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		TransferQueueService transferQueueService = TransferQueueService
				.instance();
		final Session session = transferQueueService.getHibernateUtil()
				.getSession();

		Transaction tx = null;
		final LocalIRODSTransfer enqueuedTransfer;
		try {
			tx = session.beginTransaction();
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
			enqueuedTransfer.setTransferType("PUT");
			enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
			enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
			enqueuedTransfer
					.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_ENQUEUED);
			enqueuedTransfer
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_OK);

			session.save(enqueuedTransfer);

			tx.commit();
		} catch (RuntimeException e) {

			if (tx != null) {
				tx.rollback();
			}

			throw new JargonException(e);
		} finally {
			// session.close();
		}

		transferQueueService.setTransferAsCancelled(enqueuedTransfer);

		List<LocalIRODSTransfer> transferQueue = transferQueueService
				.getRecentQueue();
		Assert.assertEquals(1, transferQueue.size());
		LocalIRODSTransfer actualEnqueuedTransfer = transferQueue.get(0);
		Assert.assertEquals("this should still be enqueued",
				actualEnqueuedTransfer.getTransferState(),
				LocalIRODSTransfer.TRANSFER_STATE_CANCELLED);

		Assert.assertEquals("this should still be ok status now",
				actualEnqueuedTransfer.getTransferErrorStatus(),
				LocalIRODSTransfer.TRANSFER_STATUS_OK);
	}

	@Test
	public void testCancelACompletedTransfer() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		TransferQueueService transferQueueService = TransferQueueService
				.instance();
		final Session session = transferQueueService.getHibernateUtil()
				.getSession();

		Transaction tx = null;
		final LocalIRODSTransfer enqueuedTransfer;
		try {
			tx = session.beginTransaction();
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
			enqueuedTransfer.setTransferType("PUT");
			enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
			enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
			enqueuedTransfer
					.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_COMPLETE);
			enqueuedTransfer
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_OK);

			session.save(enqueuedTransfer);

			tx.commit();
		} catch (RuntimeException e) {

			if (tx != null) {
				tx.rollback();
			}

			throw new JargonException(e);
		} finally {
			// session.close();
		}

		transferQueueService.setTransferAsCancelled(enqueuedTransfer);

		List<LocalIRODSTransfer> transferQueue = transferQueueService
				.getRecentQueue();
		Assert.assertEquals(1, transferQueue.size());
		LocalIRODSTransfer actualEnqueuedTransfer = transferQueue.get(0);
		Assert.assertEquals("this should still be enqueued",
				actualEnqueuedTransfer.getTransferState(),
				LocalIRODSTransfer.TRANSFER_STATE_COMPLETE);

		Assert.assertEquals("this should still be ok status now",
				actualEnqueuedTransfer.getTransferErrorStatus(),
				LocalIRODSTransfer.TRANSFER_STATUS_OK);
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

		TransferQueueService transferQueueService = TransferQueueService
				.instance();
		final Session session = transferQueueService.getHibernateUtil()
				.getSession();

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

			if (tx != null) {
				tx.rollback();
			}

			throw new JargonException(e);
		} finally {
			// session.close();
		}

		transferQueueService.processQueueAtStartup();

		// now get the queue
		List<LocalIRODSTransfer> transferQueue = transferQueueService
				.getCurrentQueue();
		Assert.assertEquals("did not find the error transaction", 1,
				transferQueue.size());
		LocalIRODSTransfer transfer = transferQueue.get(0);
		Assert.assertEquals("this does not have enqueued status",
				LocalIRODSTransfer.TRANSFER_STATE_ENQUEUED,
				transfer.getTransferState());

	}

	@Test
	public void testCreateQueueServiceInUserHomeDirectory() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUserDbName = testingProperties
				.getProperty("test.userdir.dbname");
		TransferQueueService transferQueueService = TransferQueueService
				.instanceGivingPathToTransferDatabase(testUserDbName);
		final Session session = transferQueueService.getHibernateUtil()
				.getSession();

		Transaction tx = null;
		final LocalIRODSTransfer enqueuedTransfer;
		try {
			tx = session.beginTransaction();
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
			enqueuedTransfer.setTransferType("PUT");
			enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
			enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
			enqueuedTransfer
					.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_COMPLETE);
			enqueuedTransfer
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_OK);

			session.save(enqueuedTransfer);

			tx.commit();
		} catch (RuntimeException e) {

			if (tx != null) {
				tx.rollback();
			}

			throw new JargonException(e);
		} finally {
			// session.close();
		}

		transferQueueService.setTransferAsCancelled(enqueuedTransfer);

		List<LocalIRODSTransfer> transferQueue = transferQueueService
				.getRecentQueue();
		Assert.assertTrue(transferQueue.size() > 0);

	}

	@Test
	public void testRestartClearsErrorAndStackTrace() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		TransferQueueService transferQueueService = TransferQueueService
				.instance();

		final Session session = transferQueueService.getHibernateUtil()
				.getSession();

		Transaction tx = null;
		final LocalIRODSTransfer enqueuedTransfer;
		try {
			tx = session.beginTransaction();
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
			enqueuedTransfer.setTransferType("PUT");
			enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
			enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
			enqueuedTransfer
					.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_COMPLETE);
			enqueuedTransfer
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_ERROR);
			enqueuedTransfer.setGlobalException("exception");
			enqueuedTransfer.setGlobalExceptionStackTrace("stack trace");

			session.save(enqueuedTransfer);

			tx.commit();
		} catch (RuntimeException e) {

			if (tx != null) {
				tx.rollback();
			}

			throw new JargonException(e);
		}

		transferQueueService.restartTransfer(enqueuedTransfer);

		LocalIRODSTransfer dequeuedTransfer = transferQueueService
				.dequeueTransfer();

		TestCase.assertEquals("should be processing",
				LocalIRODSTransfer.TRANSFER_STATE_PROCESSING,
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

		TransferQueueService transferQueueService = TransferQueueService
				.instance();

		final Session session = transferQueueService.getHibernateUtil()
				.getSession();

		Transaction tx = null;
		final LocalIRODSTransfer enqueuedTransfer;
		try {
			tx = session.beginTransaction();
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
			enqueuedTransfer.setTransferType("PUT");
			enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
			enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
			enqueuedTransfer
					.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_COMPLETE);
			enqueuedTransfer
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_ERROR);
			enqueuedTransfer.setGlobalException("exception");
			enqueuedTransfer.setGlobalExceptionStackTrace("stack trace");

			session.save(enqueuedTransfer);

			tx.commit();
		} catch (RuntimeException e) {

			if (tx != null) {
				tx.rollback();
			}

			throw new JargonException(e);
		}

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

		TransferQueueService transferQueueService = TransferQueueService
				.instance();

		final Session session = transferQueueService.getHibernateUtil()
				.getSession();

		Transaction tx = null;
		final LocalIRODSTransfer enqueuedTransfer;
		try {
			tx = session.beginTransaction();
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
			enqueuedTransfer.setTransferType("PUT");
			enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
			enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
			enqueuedTransfer
					.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_COMPLETE);
			enqueuedTransfer
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_ERROR);
			enqueuedTransfer.setGlobalException("exception");
			enqueuedTransfer.setGlobalExceptionStackTrace("stack trace");

			session.save(enqueuedTransfer);

			tx.commit();
		} catch (RuntimeException e) {

			if (tx != null) {
				tx.rollback();
			}

			throw new JargonException(e);
		}

		transferQueueService.resubmitTransfer(enqueuedTransfer);

		LocalIRODSTransfer dequeuedTransfer = transferQueueService
				.dequeueTransfer();

		TestCase.assertTrue("should not have retained last good path",
				dequeuedTransfer.getLastSuccessfulPath().isEmpty());
	}

}
