package org.irods.jargon.transfer.engine.synch;

import java.io.File;
import java.util.Date;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.transfer.DefaultTransferControlBlock;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.datautils.synchproperties.SynchPropertiesServiceImpl;
import org.irods.jargon.datautils.tree.FileTreeDiffUtility;
import org.irods.jargon.datautils.tree.FileTreeDiffUtilityImpl;
import org.irods.jargon.datautils.tree.FileTreeModel;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.transfer.TransferEngineException;
import org.irods.jargon.transfer.dao.domain.FrequencyType;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.irods.jargon.transfer.dao.domain.SynchronizationType;
import org.irods.jargon.transfer.dao.domain.TransferState;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.irods.jargon.transfer.engine.TransferManager;
import org.irods.jargon.transfer.synch.InPlaceSynchronizingDiffProcessorImpl;
import org.irods.jargon.transfer.synch.SynchronizeProcessorImpl;
import org.irods.jargon.transfer.synch.SynchronizingDiffProcessor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class InPlaceSynchronizingDiffProcessorImplTest {
	private static Properties testingProperties = new Properties();

	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;

	public static final String IRODS_TEST_SUBDIR_PATH = "InPlaceSynchronizingDiffProcessorImplTest";

	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;

	private static IRODSFileSystem irodsFileSystem = null;

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

	}

	@AfterClass
	public static void afterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test(expected = TransferEngineException.class)
	public void testProcessDiffMissingContracts() throws Exception {
		LocalIRODSTransfer testTransfer = new LocalIRODSTransfer();
		testTransfer.setTransferType(TransferType.SYNCH);
		Synchronization synchronization = new Synchronization();
		testTransfer.setSynchronization(synchronization);
		FileTreeModel fileTreeModel = new FileTreeModel(null);

		SynchronizingDiffProcessor processor = new InPlaceSynchronizingDiffProcessorImpl();
		processor.processDiff(testTransfer, fileTreeModel);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testProcessDiffMissingTransfer() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		FileTreeModel fileTreeModel = new FileTreeModel(null);

		InPlaceSynchronizingDiffProcessorImpl processor = new InPlaceSynchronizingDiffProcessorImpl();
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		processor.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		processor.setTransferManager(transferManager);
		processor.setIrodsAccount(irodsAccount);
		processor.processDiff(null, fileTreeModel);

	}

	@Test(expected = TransferEngineException.class)
	public void testProcessDiffMissingAccountInContract() throws Exception {

		FileTreeModel fileTreeModel = new FileTreeModel(null);

		LocalIRODSTransfer testTransfer = new LocalIRODSTransfer();
		testTransfer.setTransferType(TransferType.SYNCH);
		Synchronization synchronization = new Synchronization();
		testTransfer.setSynchronization(synchronization);

		InPlaceSynchronizingDiffProcessorImpl processor = new InPlaceSynchronizingDiffProcessorImpl();
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		processor.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		processor.setTransferManager(transferManager);
		processor.processDiff(testTransfer, fileTreeModel);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testProcessDiffMissingDiff() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		LocalIRODSTransfer testTransfer = new LocalIRODSTransfer();
		testTransfer.setTransferType(TransferType.SYNCH);
		Synchronization synchronization = new Synchronization();
		testTransfer.setSynchronization(synchronization);

		InPlaceSynchronizingDiffProcessorImpl processor = new InPlaceSynchronizingDiffProcessorImpl();
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		processor.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		processor.setTransferManager(transferManager);
		processor.setIrodsAccount(irodsAccount);

		processor.processDiff(testTransfer, null);

	}

	@Test(expected = TransferEngineException.class)
	public void testProcessDiffTransferNotSynch() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		FileTreeModel fileTreeModel = new FileTreeModel(null);

		LocalIRODSTransfer testTransfer = new LocalIRODSTransfer();
		testTransfer.setTransferType(TransferType.PUT);
		Synchronization synchronization = new Synchronization();
		testTransfer.setSynchronization(synchronization);

		InPlaceSynchronizingDiffProcessorImpl processor = new InPlaceSynchronizingDiffProcessorImpl();
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		processor.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		processor.setTransferManager(transferManager);
		processor.setIrodsAccount(irodsAccount);

		processor.processDiff(testTransfer, fileTreeModel);

	}

	@Test
	public void testSynchLocalDirWithSeveralFilesPlus() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String rootCollection = "testSynchLocalPlus";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ rootCollection);

		File irodsRoot = (File) irodsFileSystem.getIRODSFileFactory(
				irodsAccount)
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		irodsRoot.mkdirs();

		FileGenerator.generateManyFilesInGivenDirectory(IRODS_TEST_SUBDIR_PATH
				+ '/' + rootCollection, "test", ".doc", 20, 1, 2);

		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		SynchronizeProcessorImpl synchronizeProcessor = new SynchronizeProcessorImpl();
		FileTreeDiffUtility fileTreeDiffUtility = new FileTreeDiffUtilityImpl(
				irodsAccount, irodsFileSystem.getIRODSAccessObjectFactory());
		synchronizeProcessor.setFileTreeDiffUtility(fileTreeDiffUtility);
		synchronizeProcessor.setIrodsAccount(irodsAccount);
		synchronizeProcessor
				.setSynchPropertiesService(new SynchPropertiesServiceImpl(
						irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount));
		synchronizeProcessor.setTransferManager(transferManager);
		synchronizeProcessor.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());
		synchronizeProcessor.setIrodsAccount(irodsAccount);
		synchronizeProcessor.setTransferControlBlock(transferControlBlock);

		InPlaceSynchronizingDiffProcessorImpl processor = new InPlaceSynchronizingDiffProcessorImpl();
		processor.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());
		processor.setTransferManager(transferManager);
		processor.setIrodsAccount(irodsAccount);
		processor.setTransferControlBlock(transferControlBlock);
		synchronizeProcessor.setSynchronizingDiffProcessor(processor);
		processor.setTransferManager(transferManager);

		Synchronization synchronization = new Synchronization();
		synchronization.setCreatedAt(new Date());
		synchronization.setDefaultResourceName(irodsAccount
				.getDefaultStorageResource());
		synchronization.setFrequencyType(FrequencyType.EVERY_HOUR);
		synchronization.setId(new Long(1));
		synchronization.setIrodsHostName(irodsAccount.getHost());
		synchronization.setIrodsPassword(irodsAccount.getPassword());
		synchronization.setIrodsPort(irodsAccount.getPort());
		synchronization.setIrodsSynchDirectory(irodsCollectionRootAbsolutePath);
		synchronization.setLocalSynchDirectory(localCollectionAbsolutePath);
		synchronization.setIrodsUserName(irodsAccount.getUserName());
		synchronization.setIrodsZone(irodsAccount.getZone());
		synchronization.setName("testname");
		synchronization
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);

		LocalIRODSTransfer localIRODSTransfer = new LocalIRODSTransfer();
		localIRODSTransfer.setCreatedAt(new Date());
		localIRODSTransfer.setId(new Long(1));
		localIRODSTransfer
				.setIrodsAbsolutePath(irodsCollectionRootAbsolutePath);
		localIRODSTransfer.setLocalAbsolutePath(localCollectionAbsolutePath);
		localIRODSTransfer.setSynchronization(synchronization);
		localIRODSTransfer.setTransferHost(irodsAccount.getHost());
		localIRODSTransfer.setTransferPassword(irodsAccount.getPassword());
		localIRODSTransfer.setTransferPort(irodsAccount.getPort());
		localIRODSTransfer.setTransferResource(irodsAccount
				.getDefaultStorageResource());
		localIRODSTransfer.setTransferState(TransferState.ENQUEUED);
		localIRODSTransfer.setTransferType(TransferType.SYNCH);

		synchronizeProcessor.synchronizeLocalToIRODS(localIRODSTransfer);
		boolean noDiffs = fileTreeDiffUtility.verifyLocalAndIRODSTreesMatch(
				new File(localCollectionAbsolutePath),
				irodsCollectionRootAbsolutePath, 0L, 0L);

		Assert.assertTrue("diffs found after synch", noDiffs);

	}

	@Test
	public void testFileTreeDiffLocalLocalFileLengthSameLocalChecksumUpdated()
			throws Exception {

		String rootCollection = "testFileTreeDiffLocalLocalFileLengthSameLocalChecksumUpdated";
		String newChildFileName = "testFileTreeDiffLocalLocalFileLengthSameLocalChecksumUpdated.txt";
		String subChildFileName = "subchild.txt";
		String subChildFolderName = "subchild";

		int origLength = 1000;
		int newLength = 1000;

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		
		testingPropertiesHelper.buildIRODSCollectionRelativePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath, rootCollection, 2, 3, 2,
						"testFile", ".txt", 3, 2, 1, 2);

		FileGenerator.generateFileOfFixedLengthGivenName(
				localCollectionAbsolutePath, newChildFileName, origLength);
		
		
		FileGenerator.generateFileOfFixedLengthGivenName(localCollectionAbsolutePath + "/" + subChildFolderName, subChildFileName, origLength);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		
		
		String irodsBackupDirRoot = "/" + irodsAccount.getZone() + "/home/" + irodsAccount.getUserName() + "/" + InPlaceSynchronizingDiffProcessorImpl.BACKUP_PREFIX + "/"
		+ testingPropertiesHelper.buildIRODSCollectionRelativePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);
		
		IRODSFile backupRoot = irodsFileFactory.instanceIRODSFile(irodsBackupDirRoot);
		backupRoot.delete();
		
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		FileGenerator.generateFileOfFixedLengthGivenName(
				localCollectionAbsolutePath, newChildFileName, newLength);
		FileGenerator.generateFileOfFixedLengthGivenName(localCollectionAbsolutePath + "/" + subChildFolderName, subChildFileName, origLength);

		
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		SynchronizeProcessorImpl synchronizeProcessor = new SynchronizeProcessorImpl();
		FileTreeDiffUtility fileTreeDiffUtility = new FileTreeDiffUtilityImpl(
				irodsAccount, irodsFileSystem.getIRODSAccessObjectFactory());
		synchronizeProcessor.setFileTreeDiffUtility(fileTreeDiffUtility);
		synchronizeProcessor.setIrodsAccount(irodsAccount);
		synchronizeProcessor
				.setSynchPropertiesService(new SynchPropertiesServiceImpl(
						irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount));
		synchronizeProcessor.setTransferManager(transferManager);
		synchronizeProcessor.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());
		synchronizeProcessor.setIrodsAccount(irodsAccount);
		synchronizeProcessor.setTransferControlBlock(transferControlBlock);

		InPlaceSynchronizingDiffProcessorImpl processor = new InPlaceSynchronizingDiffProcessorImpl();
		processor.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());
		processor.setTransferManager(transferManager);
		processor.setIrodsAccount(irodsAccount);
		processor.setTransferControlBlock(transferControlBlock);
		synchronizeProcessor.setSynchronizingDiffProcessor(processor);
		processor.setTransferManager(transferManager);

		Synchronization synchronization = new Synchronization();
		synchronization.setCreatedAt(new Date());
		synchronization.setDefaultResourceName(irodsAccount
				.getDefaultStorageResource());
		synchronization.setFrequencyType(FrequencyType.EVERY_HOUR);
		synchronization.setId(new Long(1));
		synchronization.setIrodsHostName(irodsAccount.getHost());
		synchronization.setIrodsPassword(irodsAccount.getPassword());
		synchronization.setIrodsPort(irodsAccount.getPort());
		synchronization.setIrodsSynchDirectory(irodsCollectionRootAbsolutePath + '/' + rootCollection);
		synchronization.setLocalSynchDirectory(localCollectionAbsolutePath);
		synchronization.setIrodsUserName(irodsAccount.getUserName());
		synchronization.setIrodsZone(irodsAccount.getZone());
		synchronization.setName("testname");
		synchronization
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);

		LocalIRODSTransfer localIRODSTransfer = new LocalIRODSTransfer();
		localIRODSTransfer.setCreatedAt(new Date());
		localIRODSTransfer.setId(new Long(1));
		localIRODSTransfer
				.setIrodsAbsolutePath(irodsCollectionRootAbsolutePath + '/' + rootCollection);
		localIRODSTransfer.setLocalAbsolutePath(localCollectionAbsolutePath);
		localIRODSTransfer.setSynchronization(synchronization);
		localIRODSTransfer.setTransferHost(irodsAccount.getHost());
		localIRODSTransfer.setTransferPassword(irodsAccount.getPassword());
		localIRODSTransfer.setTransferPort(irodsAccount.getPort());
		localIRODSTransfer.setTransferResource(irodsAccount
				.getDefaultStorageResource());
		localIRODSTransfer.setTransferState(TransferState.ENQUEUED);
		localIRODSTransfer.setTransferType(TransferType.SYNCH);

		synchronizeProcessor.synchronizeLocalToIRODS(localIRODSTransfer);
		
		// add checks to make sure back files are there
		
		boolean noDiffs = fileTreeDiffUtility.verifyLocalAndIRODSTreesMatch(
				new File(localCollectionAbsolutePath),
				irodsCollectionRootAbsolutePath + '/' + rootCollection, 0L, 0L);
		
		// TODO: look for the files in the backup area
		

		Assert.assertTrue("diffs found after synch", noDiffs);

	}
}
