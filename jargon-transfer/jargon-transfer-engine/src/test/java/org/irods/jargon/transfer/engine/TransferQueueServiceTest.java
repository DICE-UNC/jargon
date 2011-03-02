package org.irods.jargon.transfer.engine;

import java.io.FileReader;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.transfer.dao.LocalIRODSTransferDAO;
import org.irods.jargon.transfer.dao.LocalIRODSTransferItemDAO;
import org.irods.jargon.transfer.dao.TransferDAOManager;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransferItem;
import org.irods.jargon.transfer.dao.domain.TransferState;
import org.irods.jargon.transfer.dao.domain.TransferStatus;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TransferQueueServiceTest {

    private static Properties testingProperties = new Properties();

    private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

    private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;

    public static final String IRODS_TEST_SUBDIR_PATH = "TransferQueueServiceTest";

    private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;

    private final TransferDAOManager transferDAOMgr = TransferDAOManager.getInstance();

    private static IDatabaseTester databaseTester;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
        testingProperties = testingPropertiesLoader.getTestProperties();
        scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(testingProperties);
        irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
        irodsTestSetupUtilities.initializeIrodsScratchDirectory();
        irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
        DatabasePreparationUtils.makeSureDatabaseIsInitialized();
        databaseTester = new JdbcDatabaseTester("org.apache.derby.jdbc.EmbeddedDriver",
                "jdbc:derby:target/database/transfer", "transfer", "transfer");
    }

    @Before
    public void setUpEach() throws Exception {
        IDataSet ds = new XmlDataSet(new FileReader("data/export-empty.xml"));
        DatabaseOperation.CLEAN_INSERT.execute(databaseTester.getConnection(), ds);
        databaseTester.closeConnection(databaseTester.getConnection());
    }

    @Test
    public void testGetCurrentQueue() throws Exception {
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        String rootCollection = "enqueueAPutWhenPaused";
        String localCollectionAbsolutePath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
                + '/' + rootCollection);

        String irodsCollectionRootAbsolutePath = testingPropertiesHelper
                .buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

        TransferQueueService transferQueueService = new TransferQueueService();

        transferQueueService.enqueuePutTransfer(localCollectionAbsolutePath, irodsCollectionRootAbsolutePath, "",
                irodsAccount);

        transferQueueService.enqueuePutTransfer(localCollectionAbsolutePath, irodsCollectionRootAbsolutePath, "",
                irodsAccount);

        transferQueueService.enqueuePutTransfer(localCollectionAbsolutePath, irodsCollectionRootAbsolutePath, "",
                irodsAccount);

        List<LocalIRODSTransfer> transferQueue = transferQueueService.getCurrentQueue();
        Assert.assertEquals(3, transferQueue.size());
        LocalIRODSTransfer enqueuedTransfer = transferQueue.get(0);
        Assert.assertEquals("this should still be enqueued", enqueuedTransfer.getTransferState(),
                TransferState.ENQUEUED);
    }

    @Test
    public void testGetErrorQueue() throws Exception {
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        String rootCollection = "testGetErrorQueue";
        String localCollectionAbsolutePath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
                + '/' + rootCollection);

        String irodsCollectionRootAbsolutePath = testingPropertiesHelper
                .buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

        TransferQueueService transferQueueService = new TransferQueueService();

        LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr.getTransferDAOBean().getLocalIRODSTransferDAO();

        LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
        enqueuedTransfer.setCreatedAt(new Date());
        enqueuedTransfer.setIrodsAbsolutePath(irodsCollectionRootAbsolutePath);
        enqueuedTransfer.setLocalAbsolutePath(localCollectionAbsolutePath);
        enqueuedTransfer.setTransferHost(irodsAccount.getHost());
        enqueuedTransfer.setTransferPort(irodsAccount.getPort());
        enqueuedTransfer.setTransferResource(irodsAccount.getDefaultStorageResource());
        enqueuedTransfer.setTransferZone(irodsAccount.getZone());
        enqueuedTransfer.setTransferStart(new Date());
        enqueuedTransfer.setTransferType(TransferType.PUT);
        enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
        enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
        enqueuedTransfer.setTransferState(TransferState.PROCESSING);
        enqueuedTransfer.setTransferStatus(TransferStatus.ERROR);

        localIRODSTransferDAO.save(enqueuedTransfer);

        enqueuedTransfer = new LocalIRODSTransfer();
        enqueuedTransfer.setCreatedAt(new Date());
        enqueuedTransfer.setIrodsAbsolutePath(irodsCollectionRootAbsolutePath);
        enqueuedTransfer.setLocalAbsolutePath(localCollectionAbsolutePath);
        enqueuedTransfer.setTransferHost(irodsAccount.getHost());
        enqueuedTransfer.setTransferPort(irodsAccount.getPort());
        enqueuedTransfer.setTransferResource(irodsAccount.getDefaultStorageResource());
        enqueuedTransfer.setTransferZone(irodsAccount.getZone());
        enqueuedTransfer.setTransferStart(new Date());
        enqueuedTransfer.setTransferType(TransferType.PUT);
        enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
        enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
        enqueuedTransfer.setTransferState(TransferState.PROCESSING);
        enqueuedTransfer.setTransferStatus(TransferStatus.WARNING);

        localIRODSTransferDAO.save(enqueuedTransfer);

        // now get the queue
        List<LocalIRODSTransfer> errorQueue = transferQueueService.getErrorQueue();
        Assert.assertEquals("did not find the error transaction", 1, errorQueue.size());
        LocalIRODSTransfer errorTransfer = errorQueue.get(0);
        Assert.assertEquals("this does not have error status", TransferStatus.ERROR, errorTransfer.getTransferStatus());

    }

    @Test
    public void testGetWarningQueue() throws Exception {
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        String rootCollection = "testGetWarningQueue";
        String localCollectionAbsolutePath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
                + '/' + rootCollection);

        String irodsCollectionRootAbsolutePath = testingPropertiesHelper
                .buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

        TransferQueueService transferQueueService = new TransferQueueService();
        LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr.getTransferDAOBean().getLocalIRODSTransferDAO();

        LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
        enqueuedTransfer.setCreatedAt(new Date());
        enqueuedTransfer.setIrodsAbsolutePath(irodsCollectionRootAbsolutePath);
        enqueuedTransfer.setLocalAbsolutePath(localCollectionAbsolutePath);
        enqueuedTransfer.setTransferHost(irodsAccount.getHost());
        enqueuedTransfer.setTransferPort(irodsAccount.getPort());
        enqueuedTransfer.setTransferResource(irodsAccount.getDefaultStorageResource());
        enqueuedTransfer.setTransferZone(irodsAccount.getZone());
        enqueuedTransfer.setTransferStart(new Date());
        enqueuedTransfer.setTransferType(TransferType.PUT);
        enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
        enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
        enqueuedTransfer.setTransferState(TransferState.PROCESSING);
        enqueuedTransfer.setTransferStatus(TransferStatus.WARNING);

        localIRODSTransferDAO.save(enqueuedTransfer);

        enqueuedTransfer = new LocalIRODSTransfer();
        enqueuedTransfer.setCreatedAt(new Date());
        enqueuedTransfer.setIrodsAbsolutePath(irodsCollectionRootAbsolutePath);
        enqueuedTransfer.setLocalAbsolutePath(localCollectionAbsolutePath);
        enqueuedTransfer.setTransferHost(irodsAccount.getHost());
        enqueuedTransfer.setTransferPort(irodsAccount.getPort());
        enqueuedTransfer.setTransferResource(irodsAccount.getDefaultStorageResource());
        enqueuedTransfer.setTransferZone(irodsAccount.getZone());
        enqueuedTransfer.setTransferStart(new Date());
        enqueuedTransfer.setTransferType(TransferType.PUT);
        enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
        enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
        enqueuedTransfer.setTransferState(TransferState.PROCESSING);
        enqueuedTransfer.setTransferStatus(TransferStatus.OK);

        localIRODSTransferDAO.save(enqueuedTransfer);

        // now get the queue
        List<LocalIRODSTransfer> errorQueue = transferQueueService.getWarningQueue();
        Assert.assertEquals("did not find the error transaction", 1, errorQueue.size());
        LocalIRODSTransfer errorTransfer = errorQueue.get(0);
        Assert.assertEquals("this does not have error status", TransferStatus.WARNING,
                errorTransfer.getTransferStatus());

    }

    @Test
    public void testLastNInQueue() throws Exception {
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        String rootCollection = "enqueueAPutWhenPaused";
        String localCollectionAbsolutePath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
                + '/' + rootCollection);

        String irodsCollectionRootAbsolutePath = testingPropertiesHelper
                .buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

        TransferQueueService transferQueueService = new TransferQueueService();

        transferQueueService.enqueuePutTransfer(localCollectionAbsolutePath, irodsCollectionRootAbsolutePath, "",
                irodsAccount);

        transferQueueService.enqueuePutTransfer(localCollectionAbsolutePath, irodsCollectionRootAbsolutePath, "",
                irodsAccount);

        transferQueueService.enqueuePutTransfer(localCollectionAbsolutePath, irodsCollectionRootAbsolutePath, "",
                irodsAccount);

        transferQueueService.enqueuePutTransfer(localCollectionAbsolutePath, irodsCollectionRootAbsolutePath, "",
                irodsAccount);

        List<LocalIRODSTransfer> transferQueue = transferQueueService.getLastNInQueue(3);
        Assert.assertEquals(3, transferQueue.size());

    }

    @Test
    public void testMarkTransferAsErrorAndTerminate() throws Exception {
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        String rootCollection = "enqueueAPutWhenPaused";
        String localCollectionAbsolutePath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
                + '/' + rootCollection);

        String irodsCollectionRootAbsolutePath = testingPropertiesHelper
                .buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

        TransferQueueService transferQueueService = new TransferQueueService();

        TransferManager transferManager = new TransferManagerImpl();

        transferQueueService.enqueuePutTransfer(localCollectionAbsolutePath, irodsCollectionRootAbsolutePath, "",
                irodsAccount);
        List<LocalIRODSTransfer> transferQueue = transferQueueService.getLastNInQueue(1);

        LocalIRODSTransfer transferToMark = transferQueue.get(0);
        transferQueueService.markTransferAsErrorAndTerminate(transferToMark, transferManager);

        // now get the error transfers, there should be one and it should be the
        // one I marked

        List<LocalIRODSTransfer> errorTransfers = transferQueueService.showErrorTransfers();

        Assert.assertEquals("should have 1 error transfer", 1, errorTransfers.size());
    }

    @Test
    public void testRestartATransfer() throws Exception {
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        TransferQueueService transferQueueService = new TransferQueueService();
        LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr.getTransferDAOBean().getLocalIRODSTransferDAO();

        final LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
        enqueuedTransfer.setCreatedAt(new Date());
        enqueuedTransfer.setIrodsAbsolutePath("path");
        enqueuedTransfer.setLocalAbsolutePath("localPath");
        enqueuedTransfer.setLastSuccessfulPath("lastSuccessfulPath");
        enqueuedTransfer.setTransferHost(irodsAccount.getHost());
        enqueuedTransfer.setTransferPort(irodsAccount.getPort());
        enqueuedTransfer.setTransferResource(irodsAccount.getDefaultStorageResource());
        enqueuedTransfer.setTransferZone(irodsAccount.getZone());
        enqueuedTransfer.setTransferStart(new Date());
        enqueuedTransfer.setTransferType(TransferType.PUT);
        enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
        enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
        enqueuedTransfer.setTransferState(TransferState.COMPLETE);
        enqueuedTransfer.setTransferStatus(TransferStatus.ERROR);

        localIRODSTransferDAO.save(enqueuedTransfer);

        // add two test items
        LocalIRODSTransferItem localIRODSTransferItem = new LocalIRODSTransferItem();
        localIRODSTransferItem.setError(false);
        localIRODSTransferItem.setFile(true);
        localIRODSTransferItem.setSourceFileAbsolutePath("/sourcepath1");
        localIRODSTransferItem.setTargetFileAbsolutePath("/targetpath1");
        localIRODSTransferItem.setTransferredAt(new Date());
        localIRODSTransferItem.setLocalIRODSTransfer(enqueuedTransfer);
        enqueuedTransfer.getLocalIRODSTransferItems().add(localIRODSTransferItem);

        localIRODSTransferItem = new LocalIRODSTransferItem();
        localIRODSTransferItem.setError(false);
        localIRODSTransferItem.setFile(true);
        localIRODSTransferItem.setSourceFileAbsolutePath("/sourcepath2");
        localIRODSTransferItem.setTargetFileAbsolutePath("/targetpath2");
        localIRODSTransferItem.setTransferredAt(new Date());
        localIRODSTransferItem.setLocalIRODSTransfer(enqueuedTransfer);
        enqueuedTransfer.getLocalIRODSTransferItems().add(localIRODSTransferItem);
        localIRODSTransferDAO.save(enqueuedTransfer);

        transferQueueService.restartTransfer(enqueuedTransfer);

        List<LocalIRODSTransfer> transferQueue = transferQueueService.getCurrentQueue();
        Assert.assertEquals(1, transferQueue.size());
        LocalIRODSTransfer actualEnqueuedTransfer = transferQueue.get(0);
        Assert.assertEquals("this should still be enqueued", actualEnqueuedTransfer.getTransferState(),
                TransferState.ENQUEUED);

        Assert.assertEquals("this should still be ok status now", actualEnqueuedTransfer.getTransferStatus(),
                TransferStatus.OK);
    }

    @Test
    public void testMarkTransferAsErrorAndTerminatePassingException() throws Exception {
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        String rootCollection = "enqueueAPutWhenPaused";
        String localCollectionAbsolutePath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
                + '/' + rootCollection);

        String irodsCollectionRootAbsolutePath = testingPropertiesHelper
                .buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

        TransferQueueService transferQueueService = new TransferQueueService();

        transferQueueService.enqueuePutTransfer(localCollectionAbsolutePath, irodsCollectionRootAbsolutePath, "",
                irodsAccount);
        List<LocalIRODSTransfer> transferQueue = transferQueueService.getLastNInQueue(1);

        LocalIRODSTransfer transferToMark = transferQueue.get(0);
        TransferManager transferManager = new TransferManagerImpl();
        transferQueueService.markTransferAsErrorAndTerminate(transferToMark, new JargonException(
                "hello a jargon exception"), transferManager);

        // now get the error transfers, there should be one and it should be the
        // one I marked

        List<LocalIRODSTransfer> errorTransfers = transferQueueService.showErrorTransfers();

        Assert.assertEquals("should have 1 error transfer", 1, errorTransfers.size());

        LocalIRODSTransfer actualTransfer = errorTransfers.get(0);
        Assert.assertEquals("did not retain the exception", "hello a jargon exception",
                actualTransfer.getGlobalException());

    }

    @Test
    public void testGetAllTransferItemsForTransfer() throws Exception {
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
        String rootCollection = "getAllTransferItemsForTransfer";
        String localCollectionAbsolutePath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
                + '/' + rootCollection);

        String irodsCollectionRootAbsolutePath = testingPropertiesHelper
                .buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

        TransferQueueService transferQueueService = new TransferQueueService();
        LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr.getTransferDAOBean().getLocalIRODSTransferDAO();
        LocalIRODSTransferItemDAO localIRODSTransferItemDAO = transferDAOMgr.getTransferDAOBean().getLocalIRODSTransferItemDAO();

        LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
        enqueuedTransfer.setCreatedAt(new Date());
        enqueuedTransfer.setIrodsAbsolutePath(irodsCollectionRootAbsolutePath);
        enqueuedTransfer.setLocalAbsolutePath(localCollectionAbsolutePath);
        enqueuedTransfer.setTransferHost(irodsAccount.getHost());
        enqueuedTransfer.setTransferPort(irodsAccount.getPort());
        enqueuedTransfer.setTransferResource(irodsAccount.getDefaultStorageResource());
        enqueuedTransfer.setTransferZone(irodsAccount.getZone());
        enqueuedTransfer.setTransferStart(new Date());
        enqueuedTransfer.setTransferType(TransferType.PUT);
        enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
        enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
        enqueuedTransfer.setTransferState(TransferState.ENQUEUED);
        enqueuedTransfer.setTransferStatus(TransferStatus.OK);

        localIRODSTransferDAO.save(enqueuedTransfer);

        // add two test items
        LocalIRODSTransferItem localIRODSTransferItem = new LocalIRODSTransferItem();
        localIRODSTransferItem.setError(false);
        localIRODSTransferItem.setFile(true);
        localIRODSTransferItem.setSourceFileAbsolutePath("/sourcepath1");
        localIRODSTransferItem.setTargetFileAbsolutePath("/targetpath1");
        localIRODSTransferItem.setTransferredAt(new Date());
        localIRODSTransferItem.setLocalIRODSTransfer(enqueuedTransfer);
        localIRODSTransferItemDAO.save(localIRODSTransferItem);

        localIRODSTransferItem = new LocalIRODSTransferItem();
        localIRODSTransferItem.setError(false);
        localIRODSTransferItem.setFile(true);
        localIRODSTransferItem.setSourceFileAbsolutePath("/sourcepath2");
        localIRODSTransferItem.setTargetFileAbsolutePath("/targetpath2");
        localIRODSTransferItem.setTransferredAt(new Date());
        localIRODSTransferItem.setLocalIRODSTransfer(enqueuedTransfer);
        localIRODSTransferItemDAO.save(localIRODSTransferItem);

        // now get all items for the transfer
        List<LocalIRODSTransferItem> transferItems = transferQueueService
                .getAllTransferItemsForTransfer(enqueuedTransfer.getId());
        irodsFileSystem.close();
        Assert.assertEquals("did not get the two transfer items", 2, transferItems.size());

    }

    @Test
    public void testGetAllTransferItemsForTransferWrongId() throws Exception {
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
        String rootCollection = "getAllTransferItemsForTransfer";
        String localCollectionAbsolutePath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
                + '/' + rootCollection);

        String irodsCollectionRootAbsolutePath = testingPropertiesHelper
                .buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

        TransferQueueService transferQueueService = new TransferQueueService();
        LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr.getTransferDAOBean().getLocalIRODSTransferDAO();

        LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
        enqueuedTransfer.setCreatedAt(new Date());
        enqueuedTransfer.setIrodsAbsolutePath(irodsCollectionRootAbsolutePath);
        enqueuedTransfer.setLocalAbsolutePath(localCollectionAbsolutePath);
        enqueuedTransfer.setTransferHost(irodsAccount.getHost());
        enqueuedTransfer.setTransferPort(irodsAccount.getPort());
        enqueuedTransfer.setTransferResource(irodsAccount.getDefaultStorageResource());
        enqueuedTransfer.setTransferZone(irodsAccount.getZone());
        enqueuedTransfer.setTransferStart(new Date());
        enqueuedTransfer.setTransferType(TransferType.PUT);
        enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
        enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
        enqueuedTransfer.setTransferState(TransferState.ENQUEUED);
        enqueuedTransfer.setTransferStatus(TransferStatus.OK);

        localIRODSTransferDAO.save(enqueuedTransfer);

        // add two test items
        LocalIRODSTransferItem localIRODSTransferItem = new LocalIRODSTransferItem();
        localIRODSTransferItem.setError(false);
        localIRODSTransferItem.setFile(true);
        localIRODSTransferItem.setSourceFileAbsolutePath("/sourcepath1");
        localIRODSTransferItem.setTargetFileAbsolutePath("/targetpath1");
        localIRODSTransferItem.setTransferredAt(new Date());
        localIRODSTransferItem.setLocalIRODSTransfer(enqueuedTransfer);
        enqueuedTransfer.getLocalIRODSTransferItems().add(localIRODSTransferItem);

        localIRODSTransferItem = new LocalIRODSTransferItem();
        localIRODSTransferItem.setError(false);
        localIRODSTransferItem.setFile(true);
        localIRODSTransferItem.setSourceFileAbsolutePath("/sourcepath2");
        localIRODSTransferItem.setTargetFileAbsolutePath("/targetpath2");
        localIRODSTransferItem.setTransferredAt(new Date());
        localIRODSTransferItem.setLocalIRODSTransfer(enqueuedTransfer);
        enqueuedTransfer.getLocalIRODSTransferItems().add(localIRODSTransferItem);

        // now get all items for the transfer
        List<LocalIRODSTransferItem> transferItems = transferQueueService.getAllTransferItemsForTransfer(new Long(
                9999999));
        irodsFileSystem.close();
        Assert.assertEquals("wrong id, should have just returned empty", 0, transferItems.size());

    }

    @Test
    public void testGetErrorTransferItemsForTransfer() throws Exception {
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
        String rootCollection = "getAllTransferItemsForTransfer";
        String localCollectionAbsolutePath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
                + '/' + rootCollection);

        String irodsCollectionRootAbsolutePath = testingPropertiesHelper
                .buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

        TransferQueueService transferQueueService = new TransferQueueService();
        LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr.getTransferDAOBean().getLocalIRODSTransferDAO();
        LocalIRODSTransferItemDAO localIRODSTransferItemDAO = transferDAOMgr.getTransferDAOBean().getLocalIRODSTransferItemDAO();

        LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
        enqueuedTransfer.setCreatedAt(new Date());
        enqueuedTransfer.setIrodsAbsolutePath(irodsCollectionRootAbsolutePath);
        enqueuedTransfer.setLocalAbsolutePath(localCollectionAbsolutePath);
        enqueuedTransfer.setTransferHost(irodsAccount.getHost());
        enqueuedTransfer.setTransferPort(irodsAccount.getPort());
        enqueuedTransfer.setTransferResource(irodsAccount.getDefaultStorageResource());
        enqueuedTransfer.setTransferZone(irodsAccount.getZone());
        enqueuedTransfer.setTransferStart(new Date());
        enqueuedTransfer.setTransferType(TransferType.PUT);
        enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
        enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
        enqueuedTransfer.setTransferState(TransferState.ENQUEUED);
        enqueuedTransfer.setTransferStatus(TransferStatus.OK);

        localIRODSTransferDAO.save(enqueuedTransfer);

        // add two test items
        LocalIRODSTransferItem localIRODSTransferItem = new LocalIRODSTransferItem();
        localIRODSTransferItem.setError(false);
        localIRODSTransferItem.setFile(true);
        localIRODSTransferItem.setSourceFileAbsolutePath("/sourcepath1");
        localIRODSTransferItem.setTargetFileAbsolutePath("/targetpath1");
        localIRODSTransferItem.setTransferredAt(new Date());
        localIRODSTransferItem.setLocalIRODSTransfer(enqueuedTransfer);
        localIRODSTransferItemDAO.save(localIRODSTransferItem);

        localIRODSTransferItem = new LocalIRODSTransferItem();
        localIRODSTransferItem.setError(true);
        localIRODSTransferItem.setFile(true);
        localIRODSTransferItem.setSourceFileAbsolutePath("/sourcepath2");
        localIRODSTransferItem.setTargetFileAbsolutePath("/targetpath2");
        localIRODSTransferItem.setTransferredAt(new Date());
        localIRODSTransferItem.setLocalIRODSTransfer(enqueuedTransfer);
        localIRODSTransferItemDAO.save(localIRODSTransferItem);

        // now get all items for the transfer
        List<LocalIRODSTransferItem> transferItems = transferQueueService
                .getErrorTransferItemsForTransfer(enqueuedTransfer.getId());
        irodsFileSystem.close();
        Assert.assertEquals("did not get the error transfer item", 1, transferItems.size());

    }

    @Test
    public void testEnqueueReplicate() throws Exception {
        TransferQueueService transferQueueService = new TransferQueueService();
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        transferQueueService.enqueueReplicateTransfer("irodsAbsolutePath", "targetResource", irodsAccount);

        // now get the data from the database

        List<LocalIRODSTransfer> queue = transferQueueService.getCurrentQueue();
        TestCase.assertEquals("should just be 1 replicate transfer in the queue", 1, queue.size());
        LocalIRODSTransfer actualTransfer = queue.get(0);

        TestCase.assertEquals("irodsAbsolutePath", actualTransfer.getIrodsAbsolutePath());
        TestCase.assertEquals("targetResource", actualTransfer.getTransferResource());
        TestCase.assertEquals(TransferType.REPLICATE, actualTransfer.getTransferType());
    }

    @Test
    public void testEnqueueGet() throws Exception {
        TransferQueueService transferQueueService = new TransferQueueService();
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        transferQueueService.enqueueGetTransfer("irodsSourceAbsolutePath", "targetLocalAbsolutePath", "sourceResource",
                irodsAccount);

        // now get the data from the database

        List<LocalIRODSTransfer> queue = transferQueueService.getCurrentQueue();
        TestCase.assertEquals("should just be 1 replicate transfer in the queue", 1, queue.size());
        LocalIRODSTransfer actualTransfer = queue.get(0);

        TestCase.assertEquals("irodsSourceAbsolutePath", actualTransfer.getIrodsAbsolutePath());
        TestCase.assertEquals("sourceResource", actualTransfer.getTransferResource());
        TestCase.assertEquals(TransferType.GET, actualTransfer.getTransferType());
    }

    @Test
    public void testEnqueueGetBigFileName() throws Exception {
        TransferQueueService transferQueueService = new TransferQueueService();
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        String fileNamePart = "filenamefilenamefilenameabcdkjfkdjfiaeojkjkldjflasfdjfasdfjasdf";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            sb.append(fileNamePart);
        }

        transferQueueService.enqueueGetTransfer(sb.toString(), sb.toString(), "sourceResource", irodsAccount);

        // now get the data from the database

        List<LocalIRODSTransfer> queue = transferQueueService.getCurrentQueue();
        TestCase.assertEquals("should just be 1 replicate transfer in the queue", 1, queue.size());
        LocalIRODSTransfer actualTransfer = queue.get(0);

        TestCase.assertEquals(sb.toString(), actualTransfer.getIrodsAbsolutePath());
        TestCase.assertEquals("sourceResource", actualTransfer.getTransferResource());
        TestCase.assertEquals(TransferType.GET, actualTransfer.getTransferType());
    }

    @Test(expected = JargonException.class)
    public void testEnqueueGetNoSource() throws Exception {
        TransferQueueService transferQueueService = new TransferQueueService();
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        transferQueueService.enqueueGetTransfer("", "targetLocalAbsolutePath", "sourceResource", irodsAccount);
    }

    @Test(expected = JargonException.class)
    public void testEnqueueGetNoTarget() throws Exception {
        TransferQueueService transferQueueService = new TransferQueueService();
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        transferQueueService.enqueueGetTransfer("source", "", "sourceResource", irodsAccount);
    }

    @Test(expected = JargonException.class)
    public void testEnqueueGetNullTarget() throws Exception {
        TransferQueueService transferQueueService = new TransferQueueService();
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        transferQueueService.enqueueGetTransfer("source", null, "sourceResource", irodsAccount);
    }

    @Test(expected = JargonException.class)
    public void testEnqueueGetNullResource() throws Exception {
        TransferQueueService transferQueueService = new TransferQueueService();
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        transferQueueService.enqueueGetTransfer("source", "target", null, irodsAccount);
    }

    @Test(expected = JargonException.class)
    public void testEnqueueGetNullSource() throws Exception {
        TransferQueueService transferQueueService = new TransferQueueService();
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        transferQueueService.enqueueGetTransfer(null, "targetLocalAbsolutePath", "sourceResource", irodsAccount);
    }

    @Test(expected = JargonException.class)
    public void testEnqueueReplicateNoPath() throws Exception {
        TransferQueueService transferQueueService = new TransferQueueService();
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        transferQueueService.enqueueReplicateTransfer("", "targetResource", irodsAccount);
    }

    @Test(expected = JargonException.class)
    public void testEnqueueReplicateNullResource() throws Exception {
        TransferQueueService transferQueueService = new TransferQueueService();
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        transferQueueService.enqueueReplicateTransfer("hello", null, irodsAccount);
    }

    @Test(expected = JargonException.class)
    public void testEnqueueReplicateNullPath() throws Exception {
        TransferQueueService transferQueueService = new TransferQueueService();
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        transferQueueService.enqueueReplicateTransfer(null, "targetResource", irodsAccount);
    }

    @Test
    public void testPurgeAll() throws Exception {
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        TransferQueueService transferQueueService = new TransferQueueService();
        LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr.getTransferDAOBean().getLocalIRODSTransferDAO();

        LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
        enqueuedTransfer.setCreatedAt(new Date());
        enqueuedTransfer.setIrodsAbsolutePath("path1");
        enqueuedTransfer.setLocalAbsolutePath("path1");
        enqueuedTransfer.setTransferHost(irodsAccount.getHost());
        enqueuedTransfer.setTransferPort(irodsAccount.getPort());
        enqueuedTransfer.setTransferResource(irodsAccount.getDefaultStorageResource());
        enqueuedTransfer.setTransferZone(irodsAccount.getZone());
        enqueuedTransfer.setTransferStart(new Date());
        enqueuedTransfer.setTransferType(TransferType.PUT);
        enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
        enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
        enqueuedTransfer.setTransferState(TransferState.PROCESSING);
        enqueuedTransfer.setTransferStatus(TransferStatus.OK);

        localIRODSTransferDAO.save(enqueuedTransfer);

        enqueuedTransfer = new LocalIRODSTransfer();
        enqueuedTransfer.setCreatedAt(new Date());
        enqueuedTransfer.setIrodsAbsolutePath("path2");
        enqueuedTransfer.setLocalAbsolutePath("path2");
        enqueuedTransfer.setTransferHost(irodsAccount.getHost());
        enqueuedTransfer.setTransferPort(irodsAccount.getPort());
        enqueuedTransfer.setTransferResource(irodsAccount.getDefaultStorageResource());
        enqueuedTransfer.setTransferZone(irodsAccount.getZone());
        enqueuedTransfer.setTransferStart(new Date());
        enqueuedTransfer.setTransferType(TransferType.PUT);
        enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
        enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
        enqueuedTransfer.setTransferState(TransferState.COMPLETE);
        enqueuedTransfer.setTransferStatus(TransferStatus.OK);

        localIRODSTransferDAO.save(enqueuedTransfer);

        enqueuedTransfer = new LocalIRODSTransfer();
        enqueuedTransfer.setCreatedAt(new Date());
        enqueuedTransfer.setIrodsAbsolutePath("path3");
        enqueuedTransfer.setLocalAbsolutePath("path3");
        enqueuedTransfer.setTransferHost(irodsAccount.getHost());
        enqueuedTransfer.setTransferPort(irodsAccount.getPort());
        enqueuedTransfer.setTransferResource(irodsAccount.getDefaultStorageResource());
        enqueuedTransfer.setTransferZone(irodsAccount.getZone());
        enqueuedTransfer.setTransferStart(new Date());
        enqueuedTransfer.setTransferType(TransferType.PUT);
        enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
        enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
        enqueuedTransfer.setTransferState(TransferState.COMPLETE);
        enqueuedTransfer.setTransferStatus(TransferStatus.ERROR);

        // now purge
        transferQueueService.purgeQueue();

        List<LocalIRODSTransfer> actualTransfers = transferQueueService.getRecentQueue();

        TestCase.assertEquals("did not get the 1 processing transfer", 1, actualTransfers.size());
        LocalIRODSTransfer actualTransfer = actualTransfers.get(0);
        TestCase.assertEquals("the one transfer should be the processing item", TransferState.PROCESSING,
                actualTransfer.getTransferState());

    }

    @Test
    public void testPurgeComplete() throws Exception {
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        TransferQueueService transferQueueService = new TransferQueueService();
        LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr.getTransferDAOBean().getLocalIRODSTransferDAO();

        LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
        enqueuedTransfer.setCreatedAt(new Date());
        enqueuedTransfer.setIrodsAbsolutePath("path1");
        enqueuedTransfer.setLocalAbsolutePath("path1");
        enqueuedTransfer.setTransferHost(irodsAccount.getHost());
        enqueuedTransfer.setTransferPort(irodsAccount.getPort());
        enqueuedTransfer.setTransferResource(irodsAccount.getDefaultStorageResource());
        enqueuedTransfer.setTransferZone(irodsAccount.getZone());
        enqueuedTransfer.setTransferStart(new Date());
        enqueuedTransfer.setTransferType(TransferType.PUT);
        enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
        enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
        enqueuedTransfer.setTransferState(TransferState.PROCESSING);
        enqueuedTransfer.setTransferStatus(TransferStatus.OK);

        localIRODSTransferDAO.save(enqueuedTransfer);

        enqueuedTransfer = new LocalIRODSTransfer();
        enqueuedTransfer.setCreatedAt(new Date());
        enqueuedTransfer.setIrodsAbsolutePath("path2");
        enqueuedTransfer.setLocalAbsolutePath("path2");
        enqueuedTransfer.setTransferHost(irodsAccount.getHost());
        enqueuedTransfer.setTransferPort(irodsAccount.getPort());
        enqueuedTransfer.setTransferResource(irodsAccount.getDefaultStorageResource());
        enqueuedTransfer.setTransferZone(irodsAccount.getZone());
        enqueuedTransfer.setTransferStart(new Date());
        enqueuedTransfer.setTransferType(TransferType.PUT);
        enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
        enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
        enqueuedTransfer.setTransferState(TransferState.COMPLETE);
        enqueuedTransfer.setTransferStatus(TransferStatus.OK);

        localIRODSTransferDAO.save(enqueuedTransfer);

        enqueuedTransfer = new LocalIRODSTransfer();
        enqueuedTransfer.setCreatedAt(new Date());
        enqueuedTransfer.setIrodsAbsolutePath("path3");
        enqueuedTransfer.setLocalAbsolutePath("path3");
        enqueuedTransfer.setTransferHost(irodsAccount.getHost());
        enqueuedTransfer.setTransferPort(irodsAccount.getPort());
        enqueuedTransfer.setTransferResource(irodsAccount.getDefaultStorageResource());
        enqueuedTransfer.setTransferZone(irodsAccount.getZone());
        enqueuedTransfer.setTransferStart(new Date());
        enqueuedTransfer.setTransferType(TransferType.PUT);
        enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
        enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
        enqueuedTransfer.setTransferState(TransferState.COMPLETE);
        enqueuedTransfer.setTransferStatus(TransferStatus.ERROR);

        localIRODSTransferDAO.save(enqueuedTransfer);

        // now purge
        transferQueueService.purgeQueue();

        List<LocalIRODSTransfer> actualTransfers = transferQueueService.getRecentQueue();

        TestCase.assertEquals("did not get the 1 processing transfer", 1, actualTransfers.size());
        LocalIRODSTransfer actualTransfer = actualTransfers.get(0);
        TestCase.assertEquals("the one transfer should be the processing item", TransferState.PROCESSING,
                actualTransfer.getTransferState());

    }

    @Test
    public void testResubmitATransfer() throws Exception {
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        TransferQueueService transferQueueService = new TransferQueueService();
        LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr.getTransferDAOBean().getLocalIRODSTransferDAO();

        final LocalIRODSTransfer enqueuedTransfer;
        LocalIRODSTransferItem localIRODSTransferItem;
        enqueuedTransfer = new LocalIRODSTransfer();
        enqueuedTransfer.setCreatedAt(new Date());
        enqueuedTransfer.setIrodsAbsolutePath("path");
        enqueuedTransfer.setLocalAbsolutePath("localPath");
        enqueuedTransfer.setLastSuccessfulPath("lastSuccessfulPath");
        enqueuedTransfer.setTransferHost(irodsAccount.getHost());
        enqueuedTransfer.setTransferPort(irodsAccount.getPort());
        enqueuedTransfer.setTransferResource(irodsAccount.getDefaultStorageResource());
        enqueuedTransfer.setTransferZone(irodsAccount.getZone());
        enqueuedTransfer.setTransferStart(new Date());
        enqueuedTransfer.setTransferType(TransferType.PUT);
        enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
        enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
        enqueuedTransfer.setTransferState(TransferState.COMPLETE);
        enqueuedTransfer.setTransferStatus(TransferStatus.ERROR);

        localIRODSTransferDAO.save(enqueuedTransfer);

        // add two test items
        localIRODSTransferItem = new LocalIRODSTransferItem();
        localIRODSTransferItem.setError(false);
        localIRODSTransferItem.setFile(true);
        localIRODSTransferItem.setSourceFileAbsolutePath("/sourcepath1");
        localIRODSTransferItem.setTargetFileAbsolutePath("/targetpath1");
        localIRODSTransferItem.setTransferredAt(new Date());
        localIRODSTransferItem.setLocalIRODSTransfer(enqueuedTransfer);
        enqueuedTransfer.getLocalIRODSTransferItems().add(localIRODSTransferItem);

        localIRODSTransferItem = new LocalIRODSTransferItem();
        localIRODSTransferItem.setError(false);
        localIRODSTransferItem.setFile(true);
        localIRODSTransferItem.setSourceFileAbsolutePath("/sourcepath2");
        localIRODSTransferItem.setTargetFileAbsolutePath("/targetpath2");
        localIRODSTransferItem.setTransferredAt(new Date());
        localIRODSTransferItem.setLocalIRODSTransfer(enqueuedTransfer);
        enqueuedTransfer.getLocalIRODSTransferItems().add(localIRODSTransferItem);
        localIRODSTransferDAO.save(enqueuedTransfer);

        transferQueueService.resubmitTransfer(enqueuedTransfer);

        List<LocalIRODSTransfer> transferQueue = transferQueueService.getCurrentQueue();
        Assert.assertEquals(1, transferQueue.size());
        LocalIRODSTransfer actualEnqueuedTransfer = transferQueue.get(0);
        Assert.assertEquals("this should still be enqueued", actualEnqueuedTransfer.getTransferState(),
                TransferState.ENQUEUED);

        Assert.assertEquals("the items should have been removed on a resubmit", 0, actualEnqueuedTransfer
                .getLocalIRODSTransferItems().size());

        Assert.assertEquals("this should still be ok status now", actualEnqueuedTransfer.getTransferStatus(),
                TransferStatus.OK);
    }

    @Test
    public void testCancelATransfer() throws Exception {
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        TransferQueueService transferQueueService = new TransferQueueService();
        LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr.getTransferDAOBean().getLocalIRODSTransferDAO();

        LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
        enqueuedTransfer.setCreatedAt(new Date());
        enqueuedTransfer.setIrodsAbsolutePath("path");
        enqueuedTransfer.setLocalAbsolutePath("localPath");
        enqueuedTransfer.setLastSuccessfulPath("lastSuccessfulPath");
        enqueuedTransfer.setTransferHost(irodsAccount.getHost());
        enqueuedTransfer.setTransferPort(irodsAccount.getPort());
        enqueuedTransfer.setTransferResource(irodsAccount.getDefaultStorageResource());
        enqueuedTransfer.setTransferZone(irodsAccount.getZone());
        enqueuedTransfer.setTransferStart(new Date());
        enqueuedTransfer.setTransferType(TransferType.PUT);
        enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
        enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
        enqueuedTransfer.setTransferState(TransferState.ENQUEUED);
        enqueuedTransfer.setTransferStatus(TransferStatus.OK);

        localIRODSTransferDAO.save(enqueuedTransfer);

        transferQueueService.setTransferAsCancelled(enqueuedTransfer);

        List<LocalIRODSTransfer> transferQueue = transferQueueService.getRecentQueue();
        Assert.assertEquals(1, transferQueue.size());
        LocalIRODSTransfer actualEnqueuedTransfer = transferQueue.get(0);
        Assert.assertEquals("this should still be enqueued", actualEnqueuedTransfer.getTransferState(),
                TransferState.CANCELLED);

        Assert.assertEquals("this should still be ok status now", actualEnqueuedTransfer.getTransferStatus(),
                TransferStatus.OK);
    }

    @Test
    public void testCancelACompletedTransfer() throws Exception {
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        TransferQueueService transferQueueService = new TransferQueueService();
        LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr.getTransferDAOBean().getLocalIRODSTransferDAO();

        LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
        enqueuedTransfer.setCreatedAt(new Date());
        enqueuedTransfer.setIrodsAbsolutePath("path");
        enqueuedTransfer.setLocalAbsolutePath("localPath");
        enqueuedTransfer.setLastSuccessfulPath("lastSuccessfulPath");
        enqueuedTransfer.setTransferHost(irodsAccount.getHost());
        enqueuedTransfer.setTransferPort(irodsAccount.getPort());
        enqueuedTransfer.setTransferResource(irodsAccount.getDefaultStorageResource());
        enqueuedTransfer.setTransferZone(irodsAccount.getZone());
        enqueuedTransfer.setTransferStart(new Date());
        enqueuedTransfer.setTransferType(TransferType.PUT);
        enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
        enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
        enqueuedTransfer.setTransferState(TransferState.COMPLETE);
        enqueuedTransfer.setTransferStatus(TransferStatus.OK);

        localIRODSTransferDAO.save(enqueuedTransfer);

        transferQueueService.setTransferAsCancelled(enqueuedTransfer);

        List<LocalIRODSTransfer> transferQueue = transferQueueService.getRecentQueue();
        Assert.assertEquals(1, transferQueue.size());
        LocalIRODSTransfer actualEnqueuedTransfer = transferQueue.get(0);
        Assert.assertEquals("this should still be enqueued", actualEnqueuedTransfer.getTransferState(),
                TransferState.COMPLETE);

        Assert.assertEquals("this should still be ok status now", actualEnqueuedTransfer.getTransferStatus(),
                TransferStatus.OK);
    }

    @Test
    public void testProcessQueueAtStartupWithAProcessingTransferHanging() throws Exception {
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        String rootCollection = "testGetErrorQueue";
        String localCollectionAbsolutePath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
                + '/' + rootCollection);

        String irodsCollectionRootAbsolutePath = testingPropertiesHelper
                .buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

        TransferQueueService transferQueueService = new TransferQueueService();
        LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr.getTransferDAOBean().getLocalIRODSTransferDAO();

        LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
        enqueuedTransfer.setCreatedAt(new Date());
        enqueuedTransfer.setIrodsAbsolutePath(irodsCollectionRootAbsolutePath);
        enqueuedTransfer.setLocalAbsolutePath(localCollectionAbsolutePath);
        enqueuedTransfer.setTransferHost(irodsAccount.getHost());
        enqueuedTransfer.setTransferPort(irodsAccount.getPort());
        enqueuedTransfer.setTransferResource(irodsAccount.getDefaultStorageResource());
        enqueuedTransfer.setTransferZone(irodsAccount.getZone());
        enqueuedTransfer.setTransferStart(new Date());
        enqueuedTransfer.setTransferType(TransferType.PUT);
        enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
        enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
        enqueuedTransfer.setTransferState(TransferState.PROCESSING);
        enqueuedTransfer.setTransferStatus(TransferStatus.ERROR);

        localIRODSTransferDAO.save(enqueuedTransfer);

        transferQueueService.processQueueAtStartup();

        // now get the queue
        List<LocalIRODSTransfer> transferQueue = transferQueueService.getCurrentQueue();
        Assert.assertEquals("did not find the error transaction", 1, transferQueue.size());
        LocalIRODSTransfer transfer = transferQueue.get(0);
        Assert.assertEquals("this does not have enqueued status", TransferState.ENQUEUED, transfer.getTransferState());

    }

    @Test
    public void testCreateQueueServiceInUserHomeDirectory() throws Exception {
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        String testUserDbName = testingProperties.getProperty("test.userdir.dbname");
        TransferQueueService transferQueueService = new TransferQueueService();
        LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr.getTransferDAOBean().getLocalIRODSTransferDAO();

        LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
        enqueuedTransfer.setCreatedAt(new Date());
        enqueuedTransfer.setIrodsAbsolutePath("path");
        enqueuedTransfer.setLocalAbsolutePath("localPath");
        enqueuedTransfer.setLastSuccessfulPath("lastSuccessfulPath");
        enqueuedTransfer.setTransferHost(irodsAccount.getHost());
        enqueuedTransfer.setTransferPort(irodsAccount.getPort());
        enqueuedTransfer.setTransferResource(irodsAccount.getDefaultStorageResource());
        enqueuedTransfer.setTransferZone(irodsAccount.getZone());
        enqueuedTransfer.setTransferStart(new Date());
        enqueuedTransfer.setTransferType(TransferType.PUT);
        enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
        enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
        enqueuedTransfer.setTransferState(TransferState.COMPLETE);
        enqueuedTransfer.setTransferStatus(TransferStatus.OK);

        localIRODSTransferDAO.save(enqueuedTransfer);

        transferQueueService.setTransferAsCancelled(enqueuedTransfer);

        List<LocalIRODSTransfer> transferQueue = transferQueueService.getRecentQueue();
        Assert.assertTrue(transferQueue.size() > 0);

    }

    @Test
    public void testRestartClearsErrorAndStackTrace() throws Exception {
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        TransferQueueService transferQueueService = new TransferQueueService();

        LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr.getTransferDAOBean().getLocalIRODSTransferDAO();

        LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
        enqueuedTransfer.setCreatedAt(new Date());
        enqueuedTransfer.setIrodsAbsolutePath("path");
        enqueuedTransfer.setLocalAbsolutePath("localPath");
        enqueuedTransfer.setLastSuccessfulPath("lastSuccessfulPath");
        enqueuedTransfer.setTransferHost(irodsAccount.getHost());
        enqueuedTransfer.setTransferPort(irodsAccount.getPort());
        enqueuedTransfer.setTransferResource(irodsAccount.getDefaultStorageResource());
        enqueuedTransfer.setTransferZone(irodsAccount.getZone());
        enqueuedTransfer.setTransferStart(new Date());
        enqueuedTransfer.setTransferType(TransferType.PUT);
        enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
        enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
        enqueuedTransfer.setTransferState(TransferState.COMPLETE);
        enqueuedTransfer.setTransferStatus(TransferStatus.ERROR);
        enqueuedTransfer.setGlobalException("exception");
        enqueuedTransfer.setGlobalExceptionStackTrace("stack trace");

        localIRODSTransferDAO.save(enqueuedTransfer);

        transferQueueService.restartTransfer(enqueuedTransfer);

        LocalIRODSTransfer dequeuedTransfer = transferQueueService.dequeueTransfer();

        TestCase.assertEquals("should be processing", TransferState.PROCESSING, dequeuedTransfer.getTransferState());
        TestCase.assertTrue("should not have an error", dequeuedTransfer.getGlobalException().isEmpty());
        TestCase.assertTrue("should have no stack trace", dequeuedTransfer.getGlobalExceptionStackTrace().isEmpty());
    }

    @Test
    public void testRestartPreservesLastGoodPath() throws Exception {
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        TransferQueueService transferQueueService = new TransferQueueService();

        LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr.getTransferDAOBean().getLocalIRODSTransferDAO();

        final LocalIRODSTransfer enqueuedTransfer;
        enqueuedTransfer = new LocalIRODSTransfer();
        enqueuedTransfer.setCreatedAt(new Date());
        enqueuedTransfer.setIrodsAbsolutePath("path");
        enqueuedTransfer.setLocalAbsolutePath("localPath");
        enqueuedTransfer.setLastSuccessfulPath("lastSuccessfulPath");
        enqueuedTransfer.setTransferHost(irodsAccount.getHost());
        enqueuedTransfer.setTransferPort(irodsAccount.getPort());
        enqueuedTransfer.setTransferResource(irodsAccount.getDefaultStorageResource());
        enqueuedTransfer.setTransferZone(irodsAccount.getZone());
        enqueuedTransfer.setTransferStart(new Date());
        enqueuedTransfer.setTransferType(TransferType.PUT);
        enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
        enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
        enqueuedTransfer.setTransferState(TransferState.COMPLETE);
        enqueuedTransfer.setTransferStatus(TransferStatus.ERROR);
        enqueuedTransfer.setGlobalException("exception");
        enqueuedTransfer.setGlobalExceptionStackTrace("stack trace");

        localIRODSTransferDAO.save(enqueuedTransfer);

        transferQueueService.restartTransfer(enqueuedTransfer);

        LocalIRODSTransfer dequeuedTransfer = transferQueueService.dequeueTransfer();

        TestCase.assertEquals("should have retained last good path", "lastSuccessfulPath",
                dequeuedTransfer.getLastSuccessfulPath());
    }

    @Test
    public void testResubmitClearsLastGoodPath() throws Exception {
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        TransferQueueService transferQueueService = new TransferQueueService();

        LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr.getTransferDAOBean().getLocalIRODSTransferDAO();

        LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
        enqueuedTransfer.setCreatedAt(new Date());
        enqueuedTransfer.setIrodsAbsolutePath("path");
        enqueuedTransfer.setLocalAbsolutePath("localPath");
        enqueuedTransfer.setLastSuccessfulPath("lastSuccessfulPath");
        enqueuedTransfer.setTransferHost(irodsAccount.getHost());
        enqueuedTransfer.setTransferPort(irodsAccount.getPort());
        enqueuedTransfer.setTransferResource(irodsAccount.getDefaultStorageResource());
        enqueuedTransfer.setTransferZone(irodsAccount.getZone());
        enqueuedTransfer.setTransferStart(new Date());
        enqueuedTransfer.setTransferType(TransferType.PUT);
        enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
        enqueuedTransfer.setTransferPassword(irodsAccount.getPassword());
        enqueuedTransfer.setTransferState(TransferState.COMPLETE);
        enqueuedTransfer.setTransferStatus(TransferStatus.ERROR);
        enqueuedTransfer.setGlobalException("exception");
        enqueuedTransfer.setGlobalExceptionStackTrace("stack trace");

        localIRODSTransferDAO.save(enqueuedTransfer);

        transferQueueService.resubmitTransfer(enqueuedTransfer);

        LocalIRODSTransfer dequeuedTransfer = transferQueueService.dequeueTransfer();

        TestCase.assertTrue("should not have retained last good path", dequeuedTransfer.getLastSuccessfulPath()
                .isEmpty());
    }

}
