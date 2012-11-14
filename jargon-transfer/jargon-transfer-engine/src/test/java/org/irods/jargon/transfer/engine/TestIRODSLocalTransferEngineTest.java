package org.irods.jargon.transfer.engine;

import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.transfer.DefaultTransferControlBlock;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.transfer.TransferServiceFactory;
import org.irods.jargon.transfer.TransferServiceFactoryImpl;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.irods.jargon.transfer.dao.domain.TransferState;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.irods.jargon.transfer.util.DomainUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */

public class TestIRODSLocalTransferEngineTest {

	private static Properties testingProperties = new Properties();

	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;

	public static final String IRODS_TEST_SUBDIR_PATH = "TestIRODSLocalTransferEngineTest";

	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;

	private static org.irods.jargon.testutils.AssertionHelper assertionHelper = null;

	private static IRODSFileSystem irodsFileSystem;

	@Autowired
	private TransferManager transferManager;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		irodsFileSystem = IRODSFileSystem.instance();
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(
				testingProperties);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		assertionHelper = new org.irods.jargon.testutils.AssertionHelper();
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		String testDatabase = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(".idrop/derby/target/database/transfer");
		String databaseUrl = "jdbc:derby:" + testDatabase;
		DatabasePreparationUtils.clearAllDatabaseForTesting(databaseUrl,
				"transfer", "transfer");

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Before
	public void setUpEach() throws Exception {

	}

	@Test
	public void testInstance() throws Exception {

		TransferManagerImpl transferManager = new TransferManagerImpl(
				irodsFileSystem, IRODS_TEST_SUBDIR_PATH);
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();
		IRODSLocalTransferEngine irodsLocalTransferEngine = IRODSLocalTransferEngine
				.instance(transferManager, transferControlBlock, null);
		Assert.assertNotNull(
				"no irodsLocalTransferEngine returned by initializer",
				irodsLocalTransferEngine);
	}

	@Test(expected = JargonException.class)
	public void testInstanceNullTransferManager() throws Exception {
		IRODSLocalTransferEngine.instance(null, null, null);
	}

	@Test
	public void testProcessPutOperationOneFile() throws Exception {

		String testFileName = "testProcessPutOperation.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 30);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		GridAccount gridAccount = DomainUtils
				.gridAccountFromIRODSAccount(irodsAccount);

		TransferManagerImpl transferManager = new TransferManagerImpl(
				irodsFileSystem, IRODS_TEST_SUBDIR_PATH);

		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();
		IRODSLocalTransferEngine irodsLocalTransferEngine = IRODSLocalTransferEngine
				.instance(transferManager, transferControlBlock, null);

		TransferServiceFactory transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();

		transferQueueService
				.enqueuePutTransfer(
						localFileName,
						targetIrodsFile,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						gridAccount);

		// get the queued transfer to give to the transfer engine, it needs to
		// be in the database
		LocalIRODSTransfer localIRODSTransfer = transferQueueService
				.dequeueTransfer();

		irodsLocalTransferEngine.processOperation(localIRODSTransfer);


		// note that other tests cover the actual put transfer, this test is
		// looking for an error-free invocation, maybe more sensitive assertions
		// are needed
		Assert.assertTrue(true);

	}

	@Test
	public void testProcessPutOperationOneFileWithSpacesInTheName()
			throws Exception {

		String testFileName = "Addendum - Duke Digital Projects Developer.docx";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 30);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testFileName);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		TransferManagerImpl transferManager = new TransferManagerImpl(
				irodsFileSystem, IRODS_TEST_SUBDIR_PATH);

		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();
		IRODSLocalTransferEngine irodsLocalTransferEngine = IRODSLocalTransferEngine
				.instance(transferManager, transferControlBlock, null);

		TransferServiceFactory transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();

		GridAccount gridAccount = DomainUtils
				.gridAccountFromIRODSAccount(irodsAccount);

		transferQueueService
				.enqueuePutTransfer(
						localFileName,
						targetIrodsFile,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						gridAccount);

		// get the queued transfer to give to the transfer engine, it needs to
		// be in the database
		LocalIRODSTransfer localIRODSTransfer = transferQueueService
				.dequeueTransfer();

		irodsLocalTransferEngine.processOperation(localIRODSTransfer);

		IRODSFile actualFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsFile);
		Assert.assertTrue("file does not exist after put", actualFile.exists());

		// note that other tests cover the actual put transfer, this test is
		// looking for an error-free invocation, maybe more sensitive assertions
		// are needed
		Assert.assertTrue(true);

	}

	@Test
	public void testEnqueuePutOperation() throws Exception {

		String rootCollection = "testEnqueuePutOperation";
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

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		GridAccount gridAccount = DomainUtils
				.gridAccountFromIRODSAccount(irodsAccount);

		TransferManager transferManager = new TransferManagerImpl(
				irodsFileSystem, IRODS_TEST_SUBDIR_PATH);
		transferManager.pause();

		transferManager.enqueueAPut(localCollectionAbsolutePath,
				irodsCollectionRootAbsolutePath, "", gridAccount);


	}

	@Test
	public void testTransferMutlipleCollectionsNoExpectedErrors()
			throws Exception {
		String rootCollection = "testTransferMutlipleCollectionsNoExpectedErrors";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath, "testSubdir", 2, 4, 2,
						"testFile", ".txt", 10, 9, 20, 200);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		GridAccount gridAccount = DomainUtils
				.gridAccountFromIRODSAccount(irodsAccount);

		TransferManagerImpl transferManager = new TransferManagerImpl(
				irodsFileSystem, IRODS_TEST_SUBDIR_PATH);

		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();
		IRODSLocalTransferEngine irodsLocalTransferEngine = IRODSLocalTransferEngine
				.instance(transferManager, transferControlBlock, null);

		TransferServiceFactory transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();

		transferQueueService
				.enqueuePutTransfer(
						localCollectionAbsolutePath,
						irodsCollectionRootAbsolutePath,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						gridAccount);

		// get the queued transfer to give to the transfer engine, it needs to
		// be in the database
		LocalIRODSTransfer localIRODSTransfer = transferQueueService
				.dequeueTransfer();

		irodsLocalTransferEngine.processOperation(localIRODSTransfer);

		// I am actually only looking for no errors, transfers tested elsewhere
		Assert.assertTrue(true);

	}

	@Test
	public void testSuccessCallbackSetsLastGoodPath() throws Exception {

		String testName = "testSuccessCallbackSetsLastGoodPath";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		TransferManagerImpl transferManager = new TransferManagerImpl(
				irodsFileSystem, IRODS_TEST_SUBDIR_PATH);

		transferManager.pause();

		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();
		IRODSLocalTransferEngine irodsLocalTransferEngine = IRODSLocalTransferEngine
				.instance(transferManager, transferControlBlock, null);

		LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath(testName);
		enqueuedTransfer.setLocalAbsolutePath(testName);
		GridAccount gridAccount = DomainUtils
				.gridAccountFromIRODSAccount(irodsAccount);
		enqueuedTransfer.setGridAccount(gridAccount);
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferState(TransferState.ENQUEUED);
		enqueuedTransfer
				.setTransferStatus(org.irods.jargon.transfer.dao.domain.TransferStatus.OK);

		transferManager.getTransferQueueService().updateLocalIRODSTransfer(
				enqueuedTransfer);

		irodsLocalTransferEngine.setCurrentTransfer(enqueuedTransfer);
		TransferStatus transferStatus = TransferStatus.instance(
				org.irods.jargon.core.transfer.TransferStatus.TransferType.PUT,
				"sourceFromStatus", "targetFromStatus", "targetResource", 100L,
				100L, 1, 1, TransferStatus.TransferState.SUCCESS, "xx", "xx");

		irodsLocalTransferEngine.statusCallback(transferStatus);

		// get the transfer now

		TransferServiceFactory transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();

		List<LocalIRODSTransfer> transfers = transferQueueService
				.getLastNInQueue(10);

		boolean txfrFound = false;
		for (LocalIRODSTransfer transfer : transfers) {
			if (transfer.getIrodsAbsolutePath().equals(testName)) {
				txfrFound = true;
				Assert.assertEquals("last good path not set",
						"sourceFromStatus", transfer.getLastSuccessfulPath());

			}
		}

		Assert.assertTrue("did not find transfer", txfrFound);

	}

	@Test
	public void testSuccessCallbackLastSuccessSetThenErrorOccurs()
			throws Exception {

		String testName = "testSuccessCallbackLastSuccessSetThenErrorOccurs";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		TransferManagerImpl transferManager = new TransferManagerImpl(
				irodsFileSystem, IRODS_TEST_SUBDIR_PATH);

		transferManager.pause();

		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();
		IRODSLocalTransferEngine irodsLocalTransferEngine = IRODSLocalTransferEngine
				.instance(transferManager, transferControlBlock, null);

		LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath(testName);
		enqueuedTransfer.setLocalAbsolutePath(testName);
		GridAccount gridAccount = DomainUtils
				.gridAccountFromIRODSAccount(irodsAccount);
		enqueuedTransfer.setGridAccount(gridAccount);
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferState(TransferState.ENQUEUED);
		enqueuedTransfer
				.setTransferStatus(org.irods.jargon.transfer.dao.domain.TransferStatus.OK);

		transferManager.getTransferQueueService().updateLocalIRODSTransfer(
				enqueuedTransfer);

		irodsLocalTransferEngine.setCurrentTransfer(enqueuedTransfer);
		TransferStatus transferStatus = TransferStatus.instance(
				org.irods.jargon.core.transfer.TransferStatus.TransferType.PUT,
				"sourceFromStatus", "targetFromStatus", "targetResource", 100L,
				100L, 1, 2, TransferStatus.TransferState.SUCCESS, "xx", "xx");

		irodsLocalTransferEngine.statusCallback(transferStatus);

		TransferStatus badStatus = TransferStatus.instanceForException(
				org.irods.jargon.core.transfer.TransferStatus.TransferType.PUT,
				"sourceFromStatusError", "targetFromStatusError",
				"targetResource", 100L, 100L, 2, 2,
				new JargonException("blah"), "xx", "xx");

		irodsLocalTransferEngine.statusCallback(badStatus);

		// get the transfer now

		List<LocalIRODSTransfer> transfers = transferManager
				.getTransferQueueService().getLastNInQueue(10);

		boolean txfrFound = false;
		for (LocalIRODSTransfer transfer : transfers) {
			if (transfer.getIrodsAbsolutePath().equals(testName)) {
				txfrFound = true;
				Assert.assertEquals("last good path not set",
						"sourceFromStatus", transfer.getLastSuccessfulPath());

			}
		}

		Assert.assertTrue("did not find transfer", txfrFound);

	}

	@Test
	public void testTransferPutWithRestart() throws Exception {
		String rootCollection = "testTransferPutWithRestart";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator.generateManyFilesInGivenDirectory(IRODS_TEST_SUBDIR_PATH
				+ '/' + rootCollection, "test", ".csv", 10, 1, 2);

		// nab the third file and use as the last successful file
		File localSourceDir = new File(localCollectionAbsolutePath);
		String sourcePaths[] = localSourceDir.list();
		String lastPath = localCollectionAbsolutePath + sourcePaths[2];

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);

		TransferManagerImpl transferManager = new TransferManagerImpl(
				irodsFileSystem, IRODS_TEST_SUBDIR_PATH);

		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance(lastPath);
		IRODSLocalTransferEngine irodsLocalTransferEngine = IRODSLocalTransferEngine
				.instance(transferManager, transferControlBlock, null);

		// need to load this transfer to the database
		LocalIRODSTransfer localIRODSTransfer = new LocalIRODSTransfer();
		localIRODSTransfer.setLocalAbsolutePath(localCollectionAbsolutePath);
		localIRODSTransfer
				.setIrodsAbsolutePath(irodsCollectionRootAbsolutePath);
		localIRODSTransfer.setTransferType(TransferType.PUT);
		GridAccount gridAccount = DomainUtils
				.gridAccountFromIRODSAccount(irodsAccount);
		localIRODSTransfer.setGridAccount(gridAccount);
		localIRODSTransfer.setLastSuccessfulPath(lastPath);
		transferManager.getTransferQueueService().updateLocalIRODSTransfer(
				localIRODSTransfer);

		irodsLocalTransferEngine.processOperation(localIRODSTransfer);
		IRODSFile targetIrodsCollection = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath + "/"
						+ rootCollection);
		String transferredFiles[] = targetIrodsCollection.list();

		Set<String> transferredFileSet = new HashSet<String>(
				Arrays.asList(transferredFiles));

		assertTrue("transferred files should be after the last successful",
				!transferredFileSet.contains(sourcePaths[0]));
		assertTrue("transferred files should be after the last successful",
				!transferredFileSet.contains(sourcePaths[1]));
		assertTrue("transferred files should be after the last successful",
				!transferredFileSet.contains(sourcePaths[2]));
	}

	@Test
	public void testTransferPutFileThenCollection() throws Exception {
		String rootCollection = "testTransferPutFileThenCollection";
		String subDirCollection = "subdirCollection";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator.generateManyFilesInGivenDirectory(IRODS_TEST_SUBDIR_PATH
				+ '/' + rootCollection, "aaa", ".csv", 1, 1, 2);

		// now make a dir with some files and put in the top dir such that the
		// file comes before this directory

		File testCollectionLocal = new File(localCollectionAbsolutePath + "/"
				+ subDirCollection);
		testCollectionLocal.mkdirs();
		FileGenerator.generateManyFilesInGivenDirectory(
				testCollectionLocal.getAbsolutePath(), "bbb", ".csv", 10, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);

		TransferManagerImpl transferManager = new TransferManagerImpl(
				irodsFileSystem, IRODS_TEST_SUBDIR_PATH);

		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();
		IRODSLocalTransferEngine irodsLocalTransferEngine = IRODSLocalTransferEngine
				.instance(transferManager, transferControlBlock, null);

		// need to load this transfer to the database

		LocalIRODSTransfer localIRODSTransfer = new LocalIRODSTransfer();
		localIRODSTransfer.setLocalAbsolutePath(localCollectionAbsolutePath);
		localIRODSTransfer
				.setIrodsAbsolutePath(irodsCollectionRootAbsolutePath);
		localIRODSTransfer.setTransferType(TransferType.PUT);
		GridAccount gridAccount = DomainUtils
				.gridAccountFromIRODSAccount(irodsAccount);
		localIRODSTransfer.setGridAccount(gridAccount);

		transferManager.getTransferQueueService().updateLocalIRODSTransfer(
				localIRODSTransfer);

		irodsLocalTransferEngine.processOperation(localIRODSTransfer);
		IRODSFile targetIrodsCollection = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath + "/"
						+ rootCollection);
		String transferredFiles[] = targetIrodsCollection.list();
		Assert.assertTrue(transferredFiles.length > 0);

	}

	@Test
	public void testProcessGetOperationOneFile() throws Exception {

		String testFileName = "testProcessGetOperationOneFile.txt";
		String testReturnedFileName = "testProcessGetOperationOneFileReturned.txt";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 30);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		GridAccount gridAccount = DomainUtils
				.gridAccountFromIRODSAccount(irodsAccount);

		// put a file
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localFileName);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
		TransferManagerImpl transferManager = new TransferManagerImpl(
				irodsFileSystem, IRODS_TEST_SUBDIR_PATH);

		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();
		IRODSLocalTransferEngine irodsLocalTransferEngine = IRODSLocalTransferEngine
				.instance(transferManager, transferControlBlock, null);

		String localReturnedAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		localReturnedAbsolutePath = localReturnedAbsolutePath
				+ testReturnedFileName;

		transferManager
				.getTransferQueueService()
				.enqueueGetTransfer(
						targetIrodsFile,
						localReturnedAbsolutePath,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						gridAccount);

		// get the queued transfer to give to the transfer engine, it needs to
		// be in the database
		LocalIRODSTransfer localIRODSTransfer = transferManager
				.getTransferQueueService().dequeueTransfer();

		irodsLocalTransferEngine.processOperation(localIRODSTransfer);

		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH
				+ "/" + testReturnedFileName);

	}

	@Test
	public void testProcessGetOperationOneFileTestCallbacks() throws Exception {

		String testFileName = "testProcessGetOperationOneFileTestCallbacks.txt";
		String testReturnedFileName = "testProcessGetOperationOneFileTestCallbacksReturned.txt";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 30);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		GridAccount gridAccount = DomainUtils
				.gridAccountFromIRODSAccount(irodsAccount);

		// put a file
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localFileName);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
		DummyTransferManagerCallbackListener transferManagerCallbackListener = new DummyTransferManagerCallbackListener();
		TransferManagerImpl transferManager = new TransferManagerImpl(
				irodsFileSystem, transferManagerCallbackListener,
				IRODS_TEST_SUBDIR_PATH);

		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();
		IRODSLocalTransferEngine irodsLocalTransferEngine = IRODSLocalTransferEngine
				.instance(transferManager, transferControlBlock, null);

		String localReturnedAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		localReturnedAbsolutePath = localReturnedAbsolutePath
				+ testReturnedFileName;

		transferManager
				.getTransferQueueService()
				.enqueueGetTransfer(
						targetIrodsFile,
						localReturnedAbsolutePath,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						gridAccount);

		// get the queued transfer to give to the transfer engine, it needs to
		// be in the database
		LocalIRODSTransfer localIRODSTransfer = transferManager
				.getTransferQueueService().dequeueTransfer();

		irodsLocalTransferEngine.processOperation(localIRODSTransfer);

		Assert.assertTrue("should have gotten a processed callback",
				transferManagerCallbackListener.getTransferStatusHistory()
						.size() > 0);

	}

}
