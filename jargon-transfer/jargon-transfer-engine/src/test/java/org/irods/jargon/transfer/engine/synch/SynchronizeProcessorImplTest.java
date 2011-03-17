package org.irods.jargon.transfer.engine.synch;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.datautils.tree.FileTreeDiffEntry;
import org.irods.jargon.datautils.tree.FileTreeDiffEntry.DiffType;
import org.irods.jargon.datautils.tree.FileTreeDiffUtility;
import org.irods.jargon.datautils.tree.FileTreeModel;
import org.irods.jargon.datautils.tree.FileTreeNode;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.transfer.engine.TransferManager;
import org.irods.jargon.transfer.synch.SynchronizeProcessor;
import org.irods.jargon.transfer.synch.SynchronizeProcessorImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class SynchronizeProcessorImplTest {

	private static Properties testingProperties = new Properties();

	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;

	public static final String IRODS_TEST_SUBDIR_PATH = "SynchronizeProcessorImplTest";

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
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);

	}

	@Test(expected = IllegalStateException.class)
	public void testSynchronizeLocalToIRODSNoInitialized() throws Exception {
		SynchronizeProcessor synchProcessor = new SynchronizeProcessorImpl();
		synchProcessor.synchronizeLocalToIRODS("name", "path", "path", 0, 0);
	}

	@Test(expected = IllegalStateException.class)
	public void testCreateInstanceWithNullTransferManager() throws Exception {
		FileTreeDiffUtility fileTreeDiffUtility = Mockito
				.mock(FileTreeDiffUtility.class);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferManager transferManager = null;
		SynchronizeProcessor synchProcessor = new SynchronizeProcessorImpl();
		synchProcessor.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		synchProcessor.setIrodsAccount(irodsAccount);
		synchProcessor.setTransferManager(transferManager);
		synchProcessor.setFileTreeDiffUtility(fileTreeDiffUtility);
		synchProcessor.synchronizeLocalToIRODS("name", "path", "path", 0, 0);
		// just looking for no errors here
		Assert.assertTrue(true);
	}

	@Test(expected = IllegalStateException.class)
	public void testCreateInstanceWithNullIrodsAccessObjectFactory()
			throws Exception {
		FileTreeDiffUtility fileTreeDiffUtility = Mockito
				.mock(FileTreeDiffUtility.class);
		IRODSAccessObjectFactory irodsAccessObjectFactory = null;
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		SynchronizeProcessor synchProcessor = new SynchronizeProcessorImpl();
		synchProcessor.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		synchProcessor.setIrodsAccount(irodsAccount);
		synchProcessor.setTransferManager(transferManager);
		synchProcessor.setFileTreeDiffUtility(fileTreeDiffUtility);
		synchProcessor.synchronizeLocalToIRODS("name", "path", "path", 0, 0);

	}

	@Test(expected = IllegalStateException.class)
	public void testCreateInstanceWithNullFileDiffUtility() throws Exception {
		FileTreeDiffUtility fileTreeDiffUtility = null;
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		SynchronizeProcessor synchProcessor = new SynchronizeProcessorImpl();
		synchProcessor.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		synchProcessor.setIrodsAccount(irodsAccount);
		synchProcessor.setTransferManager(transferManager);
		synchProcessor.setFileTreeDiffUtility(fileTreeDiffUtility);
		synchProcessor.synchronizeLocalToIRODS("name", "path", "path", 0, 0);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testBlankLocalDevice() throws Exception {
		FileTreeDiffUtility fileTreeDiffUtility = Mockito
				.mock(FileTreeDiffUtility.class);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		SynchronizeProcessor synchProcessor = new SynchronizeProcessorImpl();
		synchProcessor.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		synchProcessor.setIrodsAccount(irodsAccount);
		synchProcessor.setTransferManager(transferManager);
		synchProcessor.setFileTreeDiffUtility(fileTreeDiffUtility);
		synchProcessor.synchronizeLocalToIRODS("", "path", "path", 0, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullLocalDevice() throws Exception {
		FileTreeDiffUtility fileTreeDiffUtility = Mockito
				.mock(FileTreeDiffUtility.class);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		SynchronizeProcessor synchProcessor = new SynchronizeProcessorImpl();
		synchProcessor.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		synchProcessor.setIrodsAccount(irodsAccount);
		synchProcessor.setTransferManager(transferManager);
		synchProcessor.setFileTreeDiffUtility(fileTreeDiffUtility);
		synchProcessor.synchronizeLocalToIRODS(null, "path", "path", 0, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBlankLocalPath() throws Exception {
		FileTreeDiffUtility fileTreeDiffUtility = Mockito
				.mock(FileTreeDiffUtility.class);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		SynchronizeProcessor synchProcessor = new SynchronizeProcessorImpl();
		synchProcessor.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		synchProcessor.setIrodsAccount(irodsAccount);
		synchProcessor.setTransferManager(transferManager);
		synchProcessor.setFileTreeDiffUtility(fileTreeDiffUtility);
		synchProcessor.synchronizeLocalToIRODS("device", "", "path", 0, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullLocalPath() throws Exception {
		FileTreeDiffUtility fileTreeDiffUtility = Mockito
				.mock(FileTreeDiffUtility.class);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		SynchronizeProcessor synchProcessor = new SynchronizeProcessorImpl();
		synchProcessor.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		synchProcessor.setIrodsAccount(irodsAccount);
		synchProcessor.setTransferManager(transferManager);
		synchProcessor.setFileTreeDiffUtility(fileTreeDiffUtility);
		synchProcessor.synchronizeLocalToIRODS("device", null, "path", 0, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBlankRemotePath() throws Exception {
		FileTreeDiffUtility fileTreeDiffUtility = Mockito
				.mock(FileTreeDiffUtility.class);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		SynchronizeProcessor synchProcessor = new SynchronizeProcessorImpl();
		synchProcessor.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		synchProcessor.setIrodsAccount(irodsAccount);
		synchProcessor.setTransferManager(transferManager);
		synchProcessor.setFileTreeDiffUtility(fileTreeDiffUtility);
		synchProcessor.synchronizeLocalToIRODS("device", "local", "", 0, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullRemotePath() throws Exception {
		FileTreeDiffUtility fileTreeDiffUtility = Mockito
				.mock(FileTreeDiffUtility.class);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		SynchronizeProcessor synchProcessor = new SynchronizeProcessorImpl();
		synchProcessor.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		synchProcessor.setIrodsAccount(irodsAccount);
		synchProcessor.setTransferManager(transferManager);
		synchProcessor.setFileTreeDiffUtility(fileTreeDiffUtility);
		synchProcessor.synchronizeLocalToIRODS("device", "local", null, 0, 0);
	}

	@Test(expected = JargonException.class)
	public void testScheduleAPutNullDiffModel() throws Exception {
		FileTreeDiffUtility fileTreeDiffUtility = Mockito
				.mock(FileTreeDiffUtility.class);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		SynchronizeProcessor synchProcessor = new SynchronizeProcessorImpl();
		synchProcessor.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		synchProcessor.setIrodsAccount(irodsAccount);
		synchProcessor.setTransferManager(transferManager);
		synchProcessor.setFileTreeDiffUtility(fileTreeDiffUtility);
		synchProcessor.synchronizeLocalToIRODS("device", "/c1/c2",
				"test1/home/theperson", 0, 0);
	}

	@Test
	public void testScheduleWithALocalPlus() throws Exception {

		String localRoot = "/local/root/";
		String irodsRoot = "/test1/home/test/";
		String c1Name = "c1";
		String c2Name = "c2";

		FileTreeDiffUtility fileTreeDiffUtility = Mockito
				.mock(FileTreeDiffUtility.class);

		CollectionAndDataObjectListingEntry collEntry = new CollectionAndDataObjectListingEntry();
		collEntry.setId(1);
		collEntry.setCount(100);
		collEntry.setObjectType(ObjectType.COLLECTION);
		collEntry.setParentPath("/local");
		collEntry.setPathOrName(localRoot);
		FileTreeDiffEntry fileTreeDiffEntry = FileTreeDiffEntry.instance(
				DiffType.DIRECTORY_NO_DIFF, collEntry);
		FileTreeNode fileTreeNode = new FileTreeNode(fileTreeDiffEntry);
		FileTreeModel fileTreeModel = new FileTreeModel(fileTreeNode);

		// add a subdir that is ok
		collEntry = new CollectionAndDataObjectListingEntry();
		collEntry.setId(1);
		collEntry.setCount(100);
		collEntry.setObjectType(ObjectType.COLLECTION);
		collEntry.setParentPath(localRoot);
		collEntry.setPathOrName(localRoot + c1Name);
		fileTreeDiffEntry = FileTreeDiffEntry.instance(
				DiffType.DIRECTORY_NO_DIFF, collEntry);
		fileTreeNode.add(new FileTreeNode(fileTreeDiffEntry));

		// add a subdir that is left plus (new local)
		collEntry = new CollectionAndDataObjectListingEntry();
		collEntry.setId(1);
		collEntry.setCount(100);
		collEntry.setObjectType(ObjectType.COLLECTION);
		collEntry.setParentPath(localRoot);
		collEntry.setPathOrName(localRoot + c2Name);
		fileTreeDiffEntry = FileTreeDiffEntry.instance(DiffType.LEFT_HAND_PLUS,
				collEntry);
		fileTreeNode.add(new FileTreeNode(fileTreeDiffEntry));

		Mockito.when(
				fileTreeDiffUtility.generateDiffLocalToIRODS(
						(File) Matchers.any(), Matchers.anyString(),
						Matchers.anyLong(), Matchers.anyLong())).thenReturn(
				fileTreeModel);

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		SynchronizeProcessor synchProcessor = new SynchronizeProcessorImpl();
		synchProcessor.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		synchProcessor.setIrodsAccount(irodsAccount);
		synchProcessor.setTransferManager(transferManager);
		synchProcessor.setFileTreeDiffUtility(fileTreeDiffUtility);
		synchProcessor.synchronizeLocalToIRODS("device", localRoot, irodsRoot,
				0, 0);

		Mockito.verify(transferManager).enqueueAPut(localRoot + c2Name,
				irodsRoot + c2Name, irodsAccount.getDefaultStorageResource(),
				irodsAccount);
	}
	
	@Test
	public void testScheduleWithALocalFileNewer() throws Exception {

		String localRoot = "/local/root/";
		String irodsRoot = "/test1/home/test/";
		String c1Name = "c1";
		String c2Name = "c2";

		FileTreeDiffUtility fileTreeDiffUtility = Mockito
				.mock(FileTreeDiffUtility.class);

		CollectionAndDataObjectListingEntry collEntry = new CollectionAndDataObjectListingEntry();
		collEntry.setId(1);
		collEntry.setCount(100);
		collEntry.setObjectType(ObjectType.COLLECTION);
		collEntry.setParentPath("/local");
		collEntry.setPathOrName(localRoot);
		FileTreeDiffEntry fileTreeDiffEntry = FileTreeDiffEntry.instance(
				DiffType.DIRECTORY_NO_DIFF, collEntry);
		FileTreeNode fileTreeNode = new FileTreeNode(fileTreeDiffEntry);
		FileTreeModel fileTreeModel = new FileTreeModel(fileTreeNode);

		// add a subdir that is ok
		collEntry = new CollectionAndDataObjectListingEntry();
		collEntry.setId(1);
		collEntry.setCount(100);
		collEntry.setObjectType(ObjectType.COLLECTION);
		collEntry.setParentPath(localRoot);
		collEntry.setPathOrName(localRoot + c1Name);
		fileTreeDiffEntry = FileTreeDiffEntry.instance(
				DiffType.DIRECTORY_NO_DIFF, collEntry);
		fileTreeNode.add(new FileTreeNode(fileTreeDiffEntry));

		// add a subdir that is left plus (new local)
		collEntry = new CollectionAndDataObjectListingEntry();
		collEntry.setId(1);
		collEntry.setCount(100);
		collEntry.setObjectType(ObjectType.COLLECTION);
		collEntry.setParentPath(localRoot);
		collEntry.setPathOrName(localRoot + c2Name);
		fileTreeDiffEntry = FileTreeDiffEntry.instance(DiffType.LEFT_HAND_NEWER,
				collEntry);
		fileTreeNode.add(new FileTreeNode(fileTreeDiffEntry));

		Mockito.when(
				fileTreeDiffUtility.generateDiffLocalToIRODS(
						(File) Matchers.any(), Matchers.anyString(),
						Matchers.anyLong(), Matchers.anyLong())).thenReturn(
				fileTreeModel);

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		SynchronizeProcessor synchProcessor = new SynchronizeProcessorImpl();
		synchProcessor.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		synchProcessor.setIrodsAccount(irodsAccount);
		synchProcessor.setTransferManager(transferManager);
		synchProcessor.setFileTreeDiffUtility(fileTreeDiffUtility);
		synchProcessor.synchronizeLocalToIRODS("device", localRoot, irodsRoot,
				0, 0);

		Mockito.verify(transferManager).enqueueAPut(localRoot + c2Name,
				irodsRoot + c2Name, irodsAccount.getDefaultStorageResource(),
				irodsAccount);
	}
	
	@Test
	public void testScheduleWithAnIrodsFileNewer() throws Exception {

		String localRoot = "/local/root/";
		String irodsRoot = "/test1/home/test/";
		String c1Name = "c1";
		String c2Name = "c2";

		FileTreeDiffUtility fileTreeDiffUtility = Mockito
				.mock(FileTreeDiffUtility.class);

		CollectionAndDataObjectListingEntry collEntry = new CollectionAndDataObjectListingEntry();
		collEntry.setId(1);
		collEntry.setCount(100);
		collEntry.setObjectType(ObjectType.COLLECTION);
		collEntry.setParentPath("/local");
		collEntry.setPathOrName(localRoot);
		FileTreeDiffEntry fileTreeDiffEntry = FileTreeDiffEntry.instance(
				DiffType.DIRECTORY_NO_DIFF, collEntry);
		FileTreeNode fileTreeNode = new FileTreeNode(fileTreeDiffEntry);
		FileTreeModel fileTreeModel = new FileTreeModel(fileTreeNode);

		// add a subdir that is ok
		collEntry = new CollectionAndDataObjectListingEntry();
		collEntry.setId(1);
		collEntry.setCount(100);
		collEntry.setObjectType(ObjectType.COLLECTION);
		collEntry.setParentPath(localRoot);
		collEntry.setPathOrName(localRoot + c1Name);
		fileTreeDiffEntry = FileTreeDiffEntry.instance(
				DiffType.DIRECTORY_NO_DIFF, collEntry);
		fileTreeNode.add(new FileTreeNode(fileTreeDiffEntry));

		// add a file that is right plus (new irods)
		collEntry = new CollectionAndDataObjectListingEntry();
		collEntry.setId(1);
		collEntry.setCount(100);
		collEntry.setObjectType(ObjectType.DATA_OBJECT);
		collEntry.setParentPath(irodsRoot.substring(0, irodsRoot.length() -1));
		collEntry.setPathOrName(c2Name);
		fileTreeDiffEntry = FileTreeDiffEntry.instance(DiffType.RIGHT_HAND_NEWER,
				collEntry);
		fileTreeNode.add(new FileTreeNode(fileTreeDiffEntry));

		Mockito.when(
				fileTreeDiffUtility.generateDiffLocalToIRODS(
						(File) Matchers.any(), Matchers.anyString(),
						Matchers.anyLong(), Matchers.anyLong())).thenReturn(
				fileTreeModel);

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		SynchronizeProcessor synchProcessor = new SynchronizeProcessorImpl();
		synchProcessor.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		synchProcessor.setIrodsAccount(irodsAccount);
		synchProcessor.setTransferManager(transferManager);
		synchProcessor.setFileTreeDiffUtility(fileTreeDiffUtility);
		synchProcessor.synchronizeLocalToIRODS("device", localRoot, irodsRoot,
				0, 0);

		Mockito.verify(transferManager).enqueueAGet(irodsRoot + c2Name,
				localRoot + c2Name, irodsAccount.getDefaultStorageResource(),
				irodsAccount);
	}
	
	@Test
	public void testScheduleWithAnIrodsPlusFile() throws Exception {

		String localRoot = "/local/root/";
		String irodsRoot = "/test1/home/test/";
		String c1Name = "c1";
		String c2Name = "c2";

		FileTreeDiffUtility fileTreeDiffUtility = Mockito
				.mock(FileTreeDiffUtility.class);

		CollectionAndDataObjectListingEntry collEntry = new CollectionAndDataObjectListingEntry();
		collEntry.setId(1);
		collEntry.setCount(100);
		collEntry.setObjectType(ObjectType.COLLECTION);
		collEntry.setParentPath("/local");
		collEntry.setPathOrName(localRoot);
		FileTreeDiffEntry fileTreeDiffEntry = FileTreeDiffEntry.instance(
				DiffType.DIRECTORY_NO_DIFF, collEntry);
		FileTreeNode fileTreeNode = new FileTreeNode(fileTreeDiffEntry);
		FileTreeModel fileTreeModel = new FileTreeModel(fileTreeNode);

		// add a subdir that is ok
		collEntry = new CollectionAndDataObjectListingEntry();
		collEntry.setId(1);
		collEntry.setCount(100);
		collEntry.setObjectType(ObjectType.COLLECTION);
		collEntry.setParentPath(localRoot);
		collEntry.setPathOrName(localRoot + c1Name);
		fileTreeDiffEntry = FileTreeDiffEntry.instance(
				DiffType.DIRECTORY_NO_DIFF, collEntry);
		fileTreeNode.add(new FileTreeNode(fileTreeDiffEntry));

		// add a file that is right plus (new irods)
		collEntry = new CollectionAndDataObjectListingEntry();
		collEntry.setId(1);
		collEntry.setCount(100);
		collEntry.setObjectType(ObjectType.DATA_OBJECT);
		collEntry.setParentPath(irodsRoot.substring(0, irodsRoot.length() -1));
		collEntry.setPathOrName(c2Name);
		fileTreeDiffEntry = FileTreeDiffEntry.instance(DiffType.RIGHT_HAND_PLUS,
				collEntry);
		fileTreeNode.add(new FileTreeNode(fileTreeDiffEntry));

		Mockito.when(
				fileTreeDiffUtility.generateDiffLocalToIRODS(
						(File) Matchers.any(), Matchers.anyString(),
						Matchers.anyLong(), Matchers.anyLong())).thenReturn(
				fileTreeModel);

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		SynchronizeProcessor synchProcessor = new SynchronizeProcessorImpl();
		synchProcessor.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		synchProcessor.setIrodsAccount(irodsAccount);
		synchProcessor.setTransferManager(transferManager);
		synchProcessor.setFileTreeDiffUtility(fileTreeDiffUtility);
		synchProcessor.synchronizeLocalToIRODS("device", localRoot, irodsRoot,
				0, 0);

		Mockito.verify(transferManager).enqueueAGet(irodsRoot + c2Name,
				localRoot + c2Name, irodsAccount.getDefaultStorageResource(),
				irodsAccount);
	}


	@Test
	public void testScheduleWithALocalPlusLocalRootGivenNoTrailingSlash()
			throws Exception {

		String localRoot = "/local/root";
		String irodsRoot = "/test1/home/test/";
		String c1Name = "c1";
		String c2Name = "c2";

		FileTreeDiffUtility fileTreeDiffUtility = Mockito
				.mock(FileTreeDiffUtility.class);

		CollectionAndDataObjectListingEntry collEntry = new CollectionAndDataObjectListingEntry();
		collEntry.setId(1);
		collEntry.setCount(100);
		collEntry.setObjectType(ObjectType.COLLECTION);
		collEntry.setParentPath("/local");
		collEntry.setPathOrName(localRoot);
		FileTreeDiffEntry fileTreeDiffEntry = FileTreeDiffEntry.instance(
				DiffType.DIRECTORY_NO_DIFF, collEntry);
		FileTreeNode fileTreeNode = new FileTreeNode(fileTreeDiffEntry);
		FileTreeModel fileTreeModel = new FileTreeModel(fileTreeNode);

		// add a subdir that is ok
		collEntry = new CollectionAndDataObjectListingEntry();
		collEntry.setId(1);
		collEntry.setCount(100);
		collEntry.setObjectType(ObjectType.COLLECTION);
		collEntry.setParentPath(localRoot);
		collEntry.setPathOrName(localRoot + "/" + c1Name);
		fileTreeDiffEntry = FileTreeDiffEntry.instance(
				DiffType.DIRECTORY_NO_DIFF, collEntry);
		fileTreeNode.add(new FileTreeNode(fileTreeDiffEntry));

		// add a subdir that is left plus (new local)
		collEntry = new CollectionAndDataObjectListingEntry();
		collEntry.setId(1);
		collEntry.setCount(100);
		collEntry.setObjectType(ObjectType.COLLECTION);
		collEntry.setParentPath(localRoot);
		collEntry.setPathOrName(localRoot + "/" + c2Name);
		fileTreeDiffEntry = FileTreeDiffEntry.instance(DiffType.LEFT_HAND_PLUS,
				collEntry);
		fileTreeNode.add(new FileTreeNode(fileTreeDiffEntry));

		Mockito.when(
				fileTreeDiffUtility.generateDiffLocalToIRODS(
						(File) Matchers.any(), Matchers.anyString(),
						Matchers.anyLong(), Matchers.anyLong())).thenReturn(
				fileTreeModel);

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		SynchronizeProcessor synchProcessor = new SynchronizeProcessorImpl();
		synchProcessor.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		synchProcessor.setIrodsAccount(irodsAccount);
		synchProcessor.setTransferManager(transferManager);
		synchProcessor.setFileTreeDiffUtility(fileTreeDiffUtility);
		synchProcessor.synchronizeLocalToIRODS("device", localRoot, irodsRoot,
				0, 0);

		Mockito.verify(transferManager).enqueueAPut(localRoot + "/" + c2Name,
				irodsRoot + c2Name, irodsAccount.getDefaultStorageResource(),
				irodsAccount);
	}

	@Test
	public void testScheduleWithALocalPlusIrodsRootGivenNoTrailingSlash()
			throws Exception {

		String localRoot = "/local/root/";
		String irodsRoot = "/test1/home/test";
		String c1Name = "c1";
		String c2Name = "c2";

		FileTreeDiffUtility fileTreeDiffUtility = Mockito
				.mock(FileTreeDiffUtility.class);

		CollectionAndDataObjectListingEntry collEntry = new CollectionAndDataObjectListingEntry();
		collEntry.setId(1);
		collEntry.setCount(100);
		collEntry.setObjectType(ObjectType.COLLECTION);
		collEntry.setParentPath("/local");
		collEntry.setPathOrName(localRoot);
		FileTreeDiffEntry fileTreeDiffEntry = FileTreeDiffEntry.instance(
				DiffType.DIRECTORY_NO_DIFF, collEntry);
		FileTreeNode fileTreeNode = new FileTreeNode(fileTreeDiffEntry);
		FileTreeModel fileTreeModel = new FileTreeModel(fileTreeNode);

		// add a subdir that is ok
		collEntry = new CollectionAndDataObjectListingEntry();
		collEntry.setId(1);
		collEntry.setCount(100);
		collEntry.setObjectType(ObjectType.COLLECTION);
		collEntry.setParentPath(localRoot);
		collEntry.setPathOrName(localRoot + c1Name);
		fileTreeDiffEntry = FileTreeDiffEntry.instance(
				DiffType.DIRECTORY_NO_DIFF, collEntry);
		fileTreeNode.add(new FileTreeNode(fileTreeDiffEntry));

		// add a subdir that is left plus (new local)
		collEntry = new CollectionAndDataObjectListingEntry();
		collEntry.setId(1);
		collEntry.setCount(100);
		collEntry.setObjectType(ObjectType.COLLECTION);
		collEntry.setParentPath(localRoot);
		collEntry.setPathOrName(localRoot + c2Name);
		fileTreeDiffEntry = FileTreeDiffEntry.instance(DiffType.LEFT_HAND_PLUS,
				collEntry);
		fileTreeNode.add(new FileTreeNode(fileTreeDiffEntry));

		Mockito.when(
				fileTreeDiffUtility.generateDiffLocalToIRODS(
						(File) Matchers.any(), Matchers.anyString(),
						Matchers.anyLong(), Matchers.anyLong())).thenReturn(
				fileTreeModel);

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		SynchronizeProcessor synchProcessor = new SynchronizeProcessorImpl();
		synchProcessor.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		synchProcessor.setIrodsAccount(irodsAccount);
		synchProcessor.setTransferManager(transferManager);
		synchProcessor.setFileTreeDiffUtility(fileTreeDiffUtility);
		synchProcessor.synchronizeLocalToIRODS("device", localRoot, irodsRoot,
				0, 0);

		Mockito.verify(transferManager).enqueueAPut(localRoot + c2Name,
				irodsRoot + "/" + c2Name,
				irodsAccount.getDefaultStorageResource(), irodsAccount);
	}

}
