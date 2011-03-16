package org.irods.jargon.datautils.tree;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.datautils.tree.FileTreeDiffEntry.DiffType;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileTreeDiffUtilityTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "FileTreeDiffUtilityTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
	}

	@Test
	public void testFileTreeDiffNoDiff() throws Exception {

		String rootCollection = "testFileTreeDiffNoDiff";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testPutThenGetMultipleCollectionsMultipleFiles", 2, 3,
						2, "testFile", ".txt", 3, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		File localFileRoot = new File(localCollectionAbsolutePath);
		String targetIrodsAbsolutePath = irodsCollectionRootAbsolutePath + "/"
				+ rootCollection;

		// files now put, set up and call for the diff
		FileTreeDiffUtility fileTreeDiffUtility = new FileTreeDiffUtilityImpl(
				irodsAccount, irodsFileSystem.getIRODSAccessObjectFactory());
		FileTreeModel diffModel = fileTreeDiffUtility.generateDiffLocalToIRODS(
				localFileRoot, targetIrodsAbsolutePath, 0);
		irodsFileSystem.close();
		Assert.assertNotNull("null diffModel", diffModel);
		FileTreeNode fileTreeNode = (FileTreeNode) diffModel.getRoot();
		FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) fileTreeNode
				.getUserObject();
		Assert.assertEquals("did not get the root no-diff entry",
				FileTreeDiffEntry.DiffType.DIRECTORY_NO_DIFF,
				fileTreeDiffEntry.getDiffType());

		File[] childrenOfLocal = localFile.listFiles();
		Enumeration<?> nodes = fileTreeNode.children();
		compareFileTreeToNodesForDirMatchesAndNoDiffs(childrenOfLocal, nodes);

	}

	@Test
	public void testFileTreeDiffIrodsPlusOneDirNoPriorSynch() throws Exception {

		String rootCollection = "testFileTreeDiffIrodsPlusOneDir";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testPutThenGetMultipleCollectionsMultipleFiles", 2, 3,
						2, "testFile", ".txt", 3, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		// navigate down a couple of levels and put a file somewhere
		destFile = irodsFileFactory.instanceIRODSFile(destFile
				.getAbsolutePath() + "/" + rootCollection);
		File[] children = destFile.listFiles();
		if (children.length > 1) {
			File childFile = children[0];
			IRODSFile newChildOfChild = irodsFileFactory.instanceIRODSFile(
					childFile.getAbsolutePath(), "newChild");
			newChildOfChild.mkdirs();
		} else {
			Assert.fail("test setup failed, no children");
		}

		File localFileRoot = new File(localCollectionAbsolutePath);
		String targetIrodsAbsolutePath = irodsCollectionRootAbsolutePath + "/"
				+ rootCollection;

		// files now put, set up and call for the diff
		FileTreeDiffUtility fileTreeDiffUtility = new FileTreeDiffUtilityImpl(
				irodsAccount, irodsFileSystem.getIRODSAccessObjectFactory());
		FileTreeModel diffModel = fileTreeDiffUtility.generateDiffLocalToIRODS(
				localFileRoot, targetIrodsAbsolutePath, 0);
		irodsFileSystem.close();
		Assert.assertNotNull("null diffModel", diffModel);
		FileTreeNode fileTreeNode = (FileTreeNode) diffModel.getRoot();
		FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) fileTreeNode
				.getUserObject();
		Assert.assertEquals("did not get the root no-diff entry",
				FileTreeDiffEntry.DiffType.DIRECTORY_NO_DIFF,
				fileTreeDiffEntry.getDiffType());

		File[] childrenOfLocal = localFile.listFiles();
		Enumeration<?> nodes = fileTreeNode.children();
		compareFileTreeToNodesForDirMatchesAndExpectADiff(childrenOfLocal,
				nodes, DiffType.RIGHT_HAND_PLUS, "newChild");
	}
	
	@Test
	public void testFileTreeDiffIrodsPlusOneFileNoPriorSynch() throws Exception {

		String rootCollection = "testFileTreeDiffIrodsPlusOneFileNoPriorSynch";
		String newChildFileName = "newChild.txt";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testPutThenGetMultipleCollectionsMultipleFiles", 2, 3,
						2, "testFile", ".txt", 3, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		// navigate down a couple of levels and put a file somewhere
		destFile = irodsFileFactory.instanceIRODSFile(destFile
				.getAbsolutePath() + "/" + rootCollection);
		File[] children = destFile.listFiles();
		if (children.length > 1) {
			File childFile = children[0];
			IRODSFile newChildOfChild = irodsFileFactory.instanceIRODSFile(
					childFile.getAbsolutePath(),newChildFileName);
			newChildOfChild.createNewFile();
		} else {
			Assert.fail("test setup failed, no children");
		}

		File localFileRoot = new File(localCollectionAbsolutePath);
		String targetIrodsAbsolutePath = irodsCollectionRootAbsolutePath + "/"
				+ rootCollection;

		// files now put, set up and call for the diff
		FileTreeDiffUtility fileTreeDiffUtility = new FileTreeDiffUtilityImpl(
				irodsAccount, irodsFileSystem.getIRODSAccessObjectFactory());
		FileTreeModel diffModel = fileTreeDiffUtility.generateDiffLocalToIRODS(
				localFileRoot, targetIrodsAbsolutePath, 0);
		Assert.assertNotNull("null diffModel", diffModel);
		FileTreeNode fileTreeNode = (FileTreeNode) diffModel.getRoot();
		FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) fileTreeNode
				.getUserObject();
		Assert.assertEquals("did not get the root no-diff entry",
				FileTreeDiffEntry.DiffType.DIRECTORY_NO_DIFF,
				fileTreeDiffEntry.getDiffType());

		List<FileTreeDiffEntry> diffEntriesFound = new ArrayList<FileTreeDiffEntry>();
		int ctr = descendModelAndFindTheDiff( DiffType.RIGHT_HAND_PLUS, fileTreeNode, diffEntriesFound);
		TestCase.assertEquals("wrong number of cached diffs", 1, diffEntriesFound.size());
		TestCase.assertEquals("should have just 1 diff", 1, ctr);
		TestCase.assertEquals("wrong diff type", DiffType.RIGHT_HAND_PLUS, diffEntriesFound.get(0).getDiffType());
		TestCase.assertEquals("unexpectedFileName",newChildFileName, diffEntriesFound.get(0).getCollectionAndDataObjectListingEntry().getPathOrName());
	}


	@Test
	public void testFileTreeDiffIrodsPlusOneFileModifiedAfterLastSynch()
			throws Exception {

		String rootCollection = "testFileTreeDiffIrodsPlusOneFileModifiedAfterLastSynch";
		String newChildFileName = "newChild.txt";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testFileTreeDiffIrodsPlusOneDirModifiedAfterLastSynch",
						2, 3, 2, "testFile", ".txt", 3, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
		
		Thread.sleep(1000);

		// navigate down a couple of levels and put a file somewhere
		destFile = irodsFileFactory.instanceIRODSFile(destFile
				.getAbsolutePath() + "/" + rootCollection);
		File[] children = destFile.listFiles();
		IRODSFile newChildOfChild = null;
		if (children.length > 1) {
			File childFile = children[0];
			newChildOfChild = irodsFileFactory.instanceIRODSFile(
					childFile.getAbsolutePath(), newChildFileName);
			newChildOfChild.createNewFile();
		} else {
			Assert.fail("test setup failed, no children");
		}

		File localFileRoot = new File(localCollectionAbsolutePath);
		String targetIrodsAbsolutePath = irodsCollectionRootAbsolutePath + "/"
				+ rootCollection;

		// files now put, set up and call for the diff
		FileTreeDiffUtility fileTreeDiffUtility = new FileTreeDiffUtilityImpl(
				irodsAccount, irodsFileSystem.getIRODSAccessObjectFactory());
		long timestampToUse = newChildOfChild.lastModified() - 500;

		FileTreeModel diffModel = fileTreeDiffUtility.generateDiffLocalToIRODS(
				localFileRoot, targetIrodsAbsolutePath,
				timestampToUse);
		irodsFileSystem.close();
		Assert.assertNotNull("null diffModel", diffModel);
		FileTreeNode fileTreeNode = (FileTreeNode) diffModel.getRoot();
		FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) fileTreeNode
				.getUserObject();
		Assert.assertEquals("did not get the root no-diff entry",
				FileTreeDiffEntry.DiffType.DIRECTORY_NO_DIFF,
				fileTreeDiffEntry.getDiffType());
		
		List<FileTreeDiffEntry> diffEntriesFound = new ArrayList<FileTreeDiffEntry>();
		
		int ctr = descendModelAndFindTheDiff( DiffType.RIGHT_HAND_PLUS, fileTreeNode, diffEntriesFound);
		TestCase.assertEquals("wrong number of cached diffs", 1, diffEntriesFound.size());
		TestCase.assertEquals("should have just 1 diff", 1, ctr);
		TestCase.assertEquals("wrong diff type", DiffType.RIGHT_HAND_PLUS, diffEntriesFound.get(0).getDiffType());
		TestCase.assertEquals("unexpectedFileName",newChildFileName, diffEntriesFound.get(0).getCollectionAndDataObjectListingEntry().getPathOrName());
	}
	
	@Test
	public void testFileTreeDiffIrodsPlusOneDirModifiedAfterLastSynch()
			throws Exception {

		String rootCollection = "testFileTreeDiffIrodsPlusOneDirModifiedAfterLastSynch";
		String newChildDirName = "newChildDir";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testFileTreeDiffIrodsPlusOneDirModifiedAfterLastSynch",
						2, 3, 2, "testFile", ".txt", 3, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
		
		Thread.sleep(1000);

		// navigate down a couple of levels and put a dir somewhere
		destFile = irodsFileFactory.instanceIRODSFile(destFile
				.getAbsolutePath() + "/" + rootCollection);
		File[] children = destFile.listFiles();
		IRODSFile newChildOfChild = null;
		if (children.length > 1) {
			File childFile = children[0];
			newChildOfChild = irodsFileFactory.instanceIRODSFile(
					childFile.getAbsolutePath(), newChildDirName);
			newChildOfChild.mkdirs();
		} else {
			Assert.fail("test setup failed, no children");
		}

		File localFileRoot = new File(localCollectionAbsolutePath);
		String targetIrodsAbsolutePath = irodsCollectionRootAbsolutePath + "/"
				+ rootCollection;

		// files now put, set up and call for the diff
		FileTreeDiffUtility fileTreeDiffUtility = new FileTreeDiffUtilityImpl(
				irodsAccount, irodsFileSystem.getIRODSAccessObjectFactory());
		long timestampToUse = newChildOfChild.lastModified() - 500;

		FileTreeModel diffModel = fileTreeDiffUtility.generateDiffLocalToIRODS(
				localFileRoot, targetIrodsAbsolutePath,
				timestampToUse);
		irodsFileSystem.close();
		Assert.assertNotNull("null diffModel", diffModel);
		FileTreeNode fileTreeNode = (FileTreeNode) diffModel.getRoot();
		FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) fileTreeNode
				.getUserObject();
		Assert.assertEquals("did not get the root no-diff entry",
				FileTreeDiffEntry.DiffType.DIRECTORY_NO_DIFF,
				fileTreeDiffEntry.getDiffType());
		
		List<FileTreeDiffEntry> diffEntriesFound = new ArrayList<FileTreeDiffEntry>();
		
		int ctr = descendModelAndFindTheDiff(DiffType.RIGHT_HAND_PLUS, fileTreeNode, diffEntriesFound);
		TestCase.assertEquals("wrong number of cached diffs", 1, diffEntriesFound.size());
		TestCase.assertEquals("should have just 1 diff", 1, ctr);
		TestCase.assertEquals("wrong diff type", DiffType.RIGHT_HAND_PLUS, diffEntriesFound.get(0).getDiffType());
		TestCase.assertEquals("diff should reflect a directory", CollectionAndDataObjectListingEntry.ObjectType.COLLECTION, diffEntriesFound.get(0).getCollectionAndDataObjectListingEntry().getObjectType());
		TestCase.assertEquals("unexpectedFileName",newChildDirName, diffEntriesFound.get(0).getCollectionAndDataObjectListingEntry().getLastPathComponentForCollectionName());
	}

	@Test
	public void testFileTreeDiffIrodsPlusOneDirModifiedBeforeLastSynch()
			throws Exception {

		String rootCollection = "testFileTreeDiffIrodsPlusOneDirModifiedBeforeLastSynch";
		String newChildDirName = "newChildDir";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testFileTreeDiffIrodsPlusOneDirModifiedAfterLastSynch",
						2, 3, 2, "testFile", ".txt", 3, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
	
		// navigate down a couple of levels and put a dir somewhere
		destFile = irodsFileFactory.instanceIRODSFile(destFile
				.getAbsolutePath() + "/" + rootCollection);
		File[] children = destFile.listFiles();
		IRODSFile newChildOfChild = null;
		if (children.length > 1) {
			File childFile = children[0];
			newChildOfChild = irodsFileFactory.instanceIRODSFile(
					childFile.getAbsolutePath(), newChildDirName);
			newChildOfChild.mkdirs();
		} else {
			Assert.fail("test setup failed, no children");
		}

		File localFileRoot = new File(localCollectionAbsolutePath);
		String targetIrodsAbsolutePath = irodsCollectionRootAbsolutePath + "/"
				+ rootCollection;

		// files now put, set up and call for the diff
		FileTreeDiffUtility fileTreeDiffUtility = new FileTreeDiffUtilityImpl(
				irodsAccount, irodsFileSystem.getIRODSAccessObjectFactory());
		long timestampToUse = newChildOfChild.lastModified() + 5000;

		FileTreeModel diffModel = fileTreeDiffUtility.generateDiffLocalToIRODS(
				localFileRoot, targetIrodsAbsolutePath,
				timestampToUse);
		irodsFileSystem.close();
		Assert.assertNotNull("null diffModel", diffModel);
		FileTreeNode fileTreeNode = (FileTreeNode) diffModel.getRoot();
		FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) fileTreeNode
				.getUserObject();
		Assert.assertEquals("did not get the root no-diff entry",
				FileTreeDiffEntry.DiffType.DIRECTORY_NO_DIFF,
				fileTreeDiffEntry.getDiffType());
		
		List<FileTreeDiffEntry> diffEntriesFound = new ArrayList<FileTreeDiffEntry>();
		
		int ctr = descendModelAndFindTheDiff(DiffType.RIGHT_HAND_PLUS, fileTreeNode, diffEntriesFound);
		TestCase.assertEquals("wrong number of cached diffs", 0, diffEntriesFound.size());
		TestCase.assertEquals("should have no diff, as irods dir added before last synch", 0, ctr);
		
	}

	@Test
	public void testFileTreeDiffIrodsPlusOneFileModifiedBeforeLastSynch()
			throws Exception {

		String rootCollection = "testFileTreeDiffIrodsPlusOneFileModifiedBeforeLastSynch";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testFileTreeDiffIrodsPlusOneDirModifiedBeforeLastSynch",
						2, 3, 2, "testFile", ".txt", 3, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		// navigate down a couple of levels and put a file somewhere
		destFile = irodsFileFactory.instanceIRODSFile(destFile
				.getAbsolutePath() + "/" + rootCollection);
		File[] children = destFile.listFiles();
		IRODSFile newChildOfChild = null;
		if (children.length > 1) {
			File childFile = children[0];
			newChildOfChild = irodsFileFactory.instanceIRODSFile(
					childFile.getAbsolutePath(), "newChild");
			newChildOfChild.createNewFile();
		} else {
			Assert.fail("test setup failed, no children");
		}

		File localFileRoot = new File(localCollectionAbsolutePath);
		String targetIrodsAbsolutePath = irodsCollectionRootAbsolutePath + "/"
				+ rootCollection;
		
		// files now put, set up and call for the diff
		FileTreeDiffUtility fileTreeDiffUtility = new FileTreeDiffUtilityImpl(
				irodsAccount, irodsFileSystem.getIRODSAccessObjectFactory());
	
		long timestampToUse = newChildOfChild.lastModified() + 500;
		FileTreeModel diffModel = fileTreeDiffUtility.generateDiffLocalToIRODS(
				localFileRoot, targetIrodsAbsolutePath,
				timestampToUse);
		irodsFileSystem.close();
		Assert.assertNotNull("null diffModel", diffModel);
		FileTreeNode fileTreeNode = (FileTreeNode) diffModel.getRoot();
		FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) fileTreeNode
				.getUserObject();
		Assert.assertEquals("did not get the root no-diff entry",
				FileTreeDiffEntry.DiffType.DIRECTORY_NO_DIFF,
				fileTreeDiffEntry.getDiffType());

	List<FileTreeDiffEntry> diffEntriesFound = new ArrayList<FileTreeDiffEntry>();
		
		int ctr = descendModelAndFindTheDiff(DiffType.RIGHT_HAND_PLUS, fileTreeNode, diffEntriesFound);
		TestCase.assertEquals("wrong number of cached diffs", 0, diffEntriesFound.size());
		TestCase.assertEquals("should have no diff, since the mod was before the last synch", 0, ctr);
	}

	@Test
	public void testFileTreeDiffLocalPlusOneDir() throws Exception {

		String rootCollection = "testFileTreeDiffLocalPlusOneDir";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testPutThenGetMultipleCollectionsMultipleFiles", 2, 3,
						2, "testFile", ".txt", 3, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		// navigate down a couple of levels and put a file somewhere
		destFile = irodsFileFactory.instanceIRODSFile(destFile
				.getAbsolutePath() + "/" + rootCollection);
		File[] children = localFile.listFiles();
		if (children.length > 1) {
			File childFile = children[0];
			File newChildOfChild = new File(childFile.getAbsolutePath(),
					"newChild");
			newChildOfChild.mkdirs();
		} else {
			Assert.fail("test setup failed, no children");
		}

		File localFileRoot = new File(localCollectionAbsolutePath);
		String targetIrodsAbsolutePath = irodsCollectionRootAbsolutePath + "/"
				+ rootCollection;

		// files now put, set up and call for the diff
		FileTreeDiffUtility fileTreeDiffUtility = new FileTreeDiffUtilityImpl(
				irodsAccount, irodsFileSystem.getIRODSAccessObjectFactory());
		FileTreeModel diffModel = fileTreeDiffUtility.generateDiffLocalToIRODS(
				localFileRoot, targetIrodsAbsolutePath, 0);
		irodsFileSystem.close();
		Assert.assertNotNull("null diffModel", diffModel);
		FileTreeNode fileTreeNode = (FileTreeNode) diffModel.getRoot();
		FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) fileTreeNode
				.getUserObject();
		Assert.assertEquals("did not get the root no-diff entry",
				FileTreeDiffEntry.DiffType.DIRECTORY_NO_DIFF,
				fileTreeDiffEntry.getDiffType());

		File[] childrenOfLocal = localFile.listFiles();
		Enumeration<?> nodes = fileTreeNode.children();
		compareFileTreeToNodesForDirMatchesAndExpectADiff(childrenOfLocal,
				nodes, DiffType.LEFT_HAND_PLUS, "newChild");
	}
	
	@Test
	public void testFileTreeDiffLocalMinusOneFileAtEndAfterTransfer() throws Exception {

		String rootCollection = "testFileTreeDiffLocalMinusOneFileAtEndAfterTransfer";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		
		FileGenerator.generateManyFilesInGivenDirectory(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection, "test", ".jsp", 20, 1, 2);
					
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		// now zap the last local file
		File[] localFiles = localFile.listFiles();
		
		File lastFile = localFiles[localFiles.length -1];
		String lastFileName = lastFile.getName();
		lastFile.delete();

		File localFileRoot = new File(localCollectionAbsolutePath);
		String targetIrodsAbsolutePath = irodsCollectionRootAbsolutePath + "/"
				+ rootCollection;

		// files now put, set up and call for the diff
		FileTreeDiffUtility fileTreeDiffUtility = new FileTreeDiffUtilityImpl(
				irodsAccount, irodsFileSystem.getIRODSAccessObjectFactory());
		FileTreeModel diffModel = fileTreeDiffUtility.generateDiffLocalToIRODS(
				localFileRoot, targetIrodsAbsolutePath, 0);
		irodsFileSystem.close();
		Assert.assertNotNull("null diffModel", diffModel);
		
		List<FileTreeDiffEntry> diffEntriesFound = new ArrayList<FileTreeDiffEntry>();

		descendModelAndFindTheDiff(DiffType.RIGHT_HAND_PLUS, (FileTreeNode) diffModel.getRoot(), diffEntriesFound);
		TestCase.assertEquals("wrong number of cached diffs", 1, diffEntriesFound.size());
		FileTreeDiffEntry diffEntry = diffEntriesFound.get(0);
		TestCase.assertEquals("did not find the right hand plus", lastFileName, diffEntry.getCollectionAndDataObjectListingEntry().getPathOrName());
		
	}
	
	@Test
	public void testFileTreeDiffLocalMinusOneDirAtEndAfterTransfer() throws Exception {

		String rootCollection = "testFileTreeDiffLocalMinusOneDirAtEndAfterTransfer";
		String subdirName = "subdir";
		int nbrLocalDirs = 12;

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		
		File localFile = new File(localCollectionAbsolutePath);
		File subdirFile;
		
		for (int i = 0; i < nbrLocalDirs; i++) {
			subdirFile = new File(localFile, subdirName + i);
			subdirFile.mkdirs();
		}
		
		
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		// now zap the last local dir
		File[] localFiles = localFile.listFiles();
		
		File lastFile = localFiles[localFiles.length -1];
		lastFile.delete();

		String targetIrodsAbsolutePath = irodsCollectionRootAbsolutePath + "/"
				+ rootCollection;

		// files now put, set up and call for the diff
		FileTreeDiffUtility fileTreeDiffUtility = new FileTreeDiffUtilityImpl(
				irodsAccount, irodsFileSystem.getIRODSAccessObjectFactory());
		FileTreeModel diffModel = fileTreeDiffUtility.generateDiffLocalToIRODS(
				localFile, targetIrodsAbsolutePath, 0);
		irodsFileSystem.close();
		Assert.assertNotNull("null diffModel", diffModel);
		
		List<FileTreeDiffEntry> diffEntriesFound = new ArrayList<FileTreeDiffEntry>();

		descendModelAndFindTheDiff(DiffType.RIGHT_HAND_PLUS, (FileTreeNode) diffModel.getRoot(), diffEntriesFound);
		TestCase.assertEquals("wrong number of cached diffs", 1, diffEntriesFound.size());
		
	}

	@Test
	public void testFileTreeDiffTimestampAfterCutoff() throws Exception {

		String rootCollection = "testFileTreeDiffTimestampAfterCutoff";
		String testFileName1 = "testFileName1";
		String testFileName2 = "testFileName2";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsRootFile = irodsFileFactory.instanceIRODSFile(
				irodsCollectionRootAbsolutePath, rootCollection);
		irodsRootFile.mkdirs();

		File localRootFile = new File(localCollectionAbsolutePath);
		localRootFile.mkdirs();

		String absFileName1 = FileGenerator.generateFileOfFixedLengthGivenName(
				localCollectionAbsolutePath, testFileName1, 2);
		String absFileName2 = FileGenerator.generateFileOfFixedLengthGivenName(
				localCollectionAbsolutePath, testFileName2, 2);

		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(new File(absFileName1),
				irodsRootFile, null, null);
		dataTransferOperationsAO.putOperation(new File(absFileName2),
				irodsRootFile, null, null);

		// grab the time, then put the file again after a pause so that the
		// timestamp is updated
		long cutoffTimestamp = System.currentTimeMillis();
		Thread.sleep(5000);
		dataTransferOperationsAO.putOperation(new File(absFileName1),
				irodsRootFile, null, null);

		FileTreeDiffUtility fileTreeDiffUtility = new FileTreeDiffUtilityImpl(
				irodsAccount, irodsFileSystem.getIRODSAccessObjectFactory());
		FileTreeModel diffModel = fileTreeDiffUtility
				.generateDiffLocalToIRODS(localRootFile,
						irodsRootFile.getAbsolutePath(), cutoffTimestamp);
		irodsFileSystem.close();

		File[] childrenOfLocal = localRootFile.listFiles();
		FileTreeNode fileTreeNode = (FileTreeNode) diffModel.getRoot();
		Enumeration<?> nodes = fileTreeNode.children();
		compareFileTreeToNodesForDirMatchesAndExpectADiff(childrenOfLocal,
				nodes, DiffType.FILE_OUT_OF_SYNCH, testFileName1);
	}

	@Test
	public void testFileTreeDiffNoCutoffSet() throws Exception {

		String rootCollection = "testFileTreeDiffNoCutoffSet";
		String testFileName1 = "testFileName1";
		String testFileName2 = "testFileName2";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsRootFile = irodsFileFactory.instanceIRODSFile(
				irodsCollectionRootAbsolutePath, rootCollection);
		irodsRootFile.mkdirs();

		File localRootFile = new File(localCollectionAbsolutePath);
		localRootFile.mkdirs();

		String absFileName1 = FileGenerator.generateFileOfFixedLengthGivenName(
				localCollectionAbsolutePath, testFileName1, 2);
		String absFileName2 = FileGenerator.generateFileOfFixedLengthGivenName(
				localCollectionAbsolutePath, testFileName2, 2);

		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(new File(absFileName1),
				irodsRootFile, null, null);
		dataTransferOperationsAO.putOperation(new File(absFileName2),
				irodsRootFile, null, null);

		// grab the time, then put the file again after a pause so that the
		// timestamp is updated
		long cutoffTimestamp = 0;
		Thread.sleep(2000);
		dataTransferOperationsAO.putOperation(new File(absFileName1),
				irodsRootFile, null, null);

		FileTreeDiffUtility fileTreeDiffUtility = new FileTreeDiffUtilityImpl(
				irodsAccount, irodsFileSystem.getIRODSAccessObjectFactory());
		FileTreeModel diffModel = fileTreeDiffUtility
				.generateDiffLocalToIRODS(localRootFile,
						irodsRootFile.getAbsolutePath(), cutoffTimestamp);
		irodsFileSystem.close();

		File[] childrenOfLocal = localRootFile.listFiles();
		FileTreeNode fileTreeNode = (FileTreeNode) diffModel.getRoot();
		Enumeration<?> nodes = fileTreeNode.children();
		compareFileTreeToNodesForDirMatchesAndNoDiffs(childrenOfLocal, nodes);
	}

	private void compareFileTreeToNodesForDirMatchesAndNoDiffs(
			final File[] files, final Enumeration<?> nodes) {
		for (File child : files) {
			if (child.isDirectory()) {
				if (nodes.hasMoreElements()) {
					FileTreeNode fileTreeNode = (FileTreeNode) nodes
							.nextElement();
					FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) fileTreeNode
							.getUserObject();
					Assert.assertEquals("nodes out of synch", child
							.getAbsolutePath(), fileTreeDiffEntry
							.getCollectionAndDataObjectListingEntry()
							.getFormattedAbsolutePath());
					Assert.assertEquals(
							"node is not a no-diff directory entry",
							FileTreeDiffEntry.DiffType.DIRECTORY_NO_DIFF,
							fileTreeDiffEntry.getDiffType());
					File[] childrenOfLocal = child.listFiles();
					Enumeration<?> childNodes = fileTreeNode.children();
					compareFileTreeToNodesForDirMatchesAndNoDiffs(
							childrenOfLocal, childNodes);
				} else {
					Assert.fail("node is out of synch (missing) for file:"
							+ child.getAbsolutePath());
				}
			}
		}
	}
	
	private int descendModelAndFindTheDiff(final DiffType expectedDiffType, final FileTreeNode node, final List<FileTreeDiffEntry> diffEntries) {
		
		int diffsFound = 0;
		FileTreeDiffEntry diffEntry = (FileTreeDiffEntry) node.getUserObject();
		if (diffEntry.getDiffType() == expectedDiffType) {
			diffsFound++;
		}
		
		if (diffEntry.getDiffType() != DiffType.DIRECTORY_NO_DIFF) {
			diffEntries.add(diffEntry);

		}
		
		@SuppressWarnings("rawtypes")
		Enumeration children = node.children();
		
		while (children.hasMoreElements()) {
			diffsFound += descendModelAndFindTheDiff(expectedDiffType, (FileTreeNode) children.nextElement(), diffEntries);
		}
		
		return diffsFound;
		
	}

	private void compareFileTreeToNodesForDirMatchesAndExpectADiff(
			final File[] files, final Enumeration<?> nodes,
			final FileTreeDiffEntry.DiffType diffType, final String fileName) {
		for (File child : files) {
			if (child.isDirectory()) {
				if (nodes.hasMoreElements()) {
					FileTreeNode fileTreeNode = (FileTreeNode) nodes
							.nextElement();
					FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) fileTreeNode
							.getUserObject();

					if (fileTreeDiffEntry.getDiffType() != DiffType.DIRECTORY_NO_DIFF) {
						if (fileTreeDiffEntry.getDiffType() != diffType) {
							Assert.fail("unexpectedDiffType");
						}
						if (fileTreeDiffEntry
								.getCollectionAndDataObjectListingEntry()
								.getFormattedAbsolutePath().indexOf(fileName) == -1) {
							Assert.fail("a file name that I didn't anticipate was found to have a diff errror");
						}
					}

					File[] childrenOfLocal = child.listFiles();
					Enumeration<?> childNodes = fileTreeNode.children();
					compareFileTreeToNodesForDirMatchesAndExpectADiff(
							childrenOfLocal, childNodes, diffType, fileName);
				} else {
					Assert.fail("node is out of synch (missing) for file:"
							+ child.getAbsolutePath());
				}
			}
		}
	}
	
	@Test
	public void testFileTreeDiffLocalPlusOneFileModifiedAfterLastSynch()
			throws Exception {

		String rootCollection = "testFileTreeDiffLocalPlusOneFileModifiedAfterLastSynch";
		String newChildFileName = "newChild.txt";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testFileTreeDiffLocalPlusOneFileModifiedAfterLastSynchSd",
						2, 3, 2, "testFile", ".txt", 3, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
		long timestampToUse = new Date().getTime();
		Thread.sleep(3000);

		// navigate down a couple of levels and put a file somewhere
		localFile = new File(localFile
				.getAbsolutePath());
		File[] children = localFile.listFiles();
		File newChildOfChild = null;
		if (children.length > 1) {
			File childFile = children[0];
			newChildOfChild =new File(
					childFile.getAbsolutePath(), newChildFileName);
			newChildOfChild.createNewFile();
		} else {
			Assert.fail("test setup failed, no children");
		}

		File localFileRoot = new File(localCollectionAbsolutePath);
		String targetIrodsAbsolutePath = irodsCollectionRootAbsolutePath + "/"
				+ rootCollection;

		// files now put, set up and call for the diff
		FileTreeDiffUtility fileTreeDiffUtility = new FileTreeDiffUtilityImpl(
				irodsAccount, irodsFileSystem.getIRODSAccessObjectFactory());
		

		FileTreeModel diffModel = fileTreeDiffUtility.generateDiffLocalToIRODS(
				localFileRoot, targetIrodsAbsolutePath,
				timestampToUse);
		irodsFileSystem.close();
		Assert.assertNotNull("null diffModel", diffModel);
		FileTreeNode fileTreeNode = (FileTreeNode) diffModel.getRoot();
		List<FileTreeDiffEntry> diffEntriesFound = new ArrayList<FileTreeDiffEntry>();
		
		int ctr = descendModelAndFindTheDiff(DiffType.LEFT_HAND_PLUS, fileTreeNode, diffEntriesFound);
		TestCase.assertEquals("wrong number of cached diffs", 1, diffEntriesFound.size());
		TestCase.assertEquals("should have just 1 diff", 1, ctr);
		TestCase.assertEquals("wrong diff type", DiffType.LEFT_HAND_PLUS, diffEntriesFound.get(0).getDiffType());
		TestCase.assertEquals("unexpectedFileName",newChildFileName, diffEntriesFound.get(0).getCollectionAndDataObjectListingEntry().getPathOrName());
	}
	
	@Test
	public void testFileTreeDiffLocalPlusOneDirModifiedAfterLastSynch()
			throws Exception {

		String rootCollection = "testFileTreeDiffLocalPlusOneDirModifiedAfterLastSynch";
		String newChildFileName = "newChild";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testFileTreeDiffLocalPlusOneDirModifiedAfterLastSynch",
						2, 3, 2, "testFile", ".txt", 3, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
		long timestampToUse = new Date().getTime();

		Thread.sleep(3000);

		// navigate down a couple of levels and put a dir somewhere
		localFile = new File(localFile
				.getAbsolutePath());
		File[] children = localFile.listFiles();
		File newChildOfChild = null;
		if (children.length > 1) {
			File childFile = children[0];
			newChildOfChild =new File(
					childFile.getAbsolutePath(), newChildFileName);
			newChildOfChild.mkdirs();
		} else {
			Assert.fail("test setup failed, no children");
		}

		File localFileRoot = new File(localCollectionAbsolutePath);
		String targetIrodsAbsolutePath = irodsCollectionRootAbsolutePath + "/"
				+ rootCollection;

		// files now put, set up and call for the diff
		FileTreeDiffUtility fileTreeDiffUtility = new FileTreeDiffUtilityImpl(
				irodsAccount, irodsFileSystem.getIRODSAccessObjectFactory());
		
		FileTreeModel diffModel = fileTreeDiffUtility.generateDiffLocalToIRODS(
				localFileRoot, targetIrodsAbsolutePath,
				timestampToUse);
		irodsFileSystem.close();
		Assert.assertNotNull("null diffModel", diffModel);
		FileTreeNode fileTreeNode = (FileTreeNode) diffModel.getRoot();
		List<FileTreeDiffEntry> diffEntriesFound = new ArrayList<FileTreeDiffEntry>();
		
		int ctr = descendModelAndFindTheDiff(DiffType.LEFT_HAND_PLUS, fileTreeNode, diffEntriesFound);
		TestCase.assertEquals("wrong number of cached diffs", 1, diffEntriesFound.size());
		TestCase.assertEquals("should have just 1 diff", 1, ctr);
		TestCase.assertEquals("wrong diff type", DiffType.LEFT_HAND_PLUS, diffEntriesFound.get(0).getDiffType());
		TestCase.assertEquals("unexpectedFileName",newChildFileName, diffEntriesFound.get(0).getCollectionAndDataObjectListingEntry().getLastPathComponentForCollectionName());
	}
	
	@Test
	public void testFileTreeDiffLocalPlusOneFileModifiedBeforeLastSynch()
			throws Exception {

		String rootCollection = "testFileTreeDiffLocalPlusOneFileModifiedBeforeLastSynch";
		String newChildFileName = "newChild.txt";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testFileTreeDiffLocalPlusOneFileModifiedBeforeLastSynchSd",
						2, 3, 2, "testFile", ".txt", 3, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
	
		// navigate down a couple of levels and put a file somewhere
		localFile = new File(localFile
				.getAbsolutePath());
		File[] children = localFile.listFiles();
		File newChildOfChild = null;
		if (children.length > 1) {
			File childFile = children[0];
			newChildOfChild =new File(
					childFile.getAbsolutePath(), newChildFileName);
			newChildOfChild.createNewFile();
		} else {
			Assert.fail("test setup failed, no children");
		}
		
		long timestampToUse = new Date().getTime();

		File localFileRoot = new File(localCollectionAbsolutePath);
		String targetIrodsAbsolutePath = irodsCollectionRootAbsolutePath + "/"
				+ rootCollection;

		// files now put, set up and call for the diff
		FileTreeDiffUtility fileTreeDiffUtility = new FileTreeDiffUtilityImpl(
				irodsAccount, irodsFileSystem.getIRODSAccessObjectFactory());
		

		FileTreeModel diffModel = fileTreeDiffUtility.generateDiffLocalToIRODS(
				localFileRoot, targetIrodsAbsolutePath,
				timestampToUse);
		irodsFileSystem.close();
		Assert.assertNotNull("null diffModel", diffModel);
		FileTreeNode fileTreeNode = (FileTreeNode) diffModel.getRoot();
		List<FileTreeDiffEntry> diffEntriesFound = new ArrayList<FileTreeDiffEntry>();
		
		descendModelAndFindTheDiff(DiffType.LEFT_HAND_PLUS, fileTreeNode, diffEntriesFound);
		TestCase.assertEquals("should have no cached diffs", 0, diffEntriesFound.size());
		
	}
	
	@Test
	public void testFileTreeDiffLocalPlusOneDirModifiedBeforeLastSynch()
			throws Exception {

		String rootCollection = "testFileTreeDiffLocalPlusOneDirModifiedBeforeLastSynch";
		String newChildFileName = "newChild";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testFileTreeDiffLocalPlusOneDirModifiedBeforeLastSynch",
						2, 3, 2, "testFile", ".txt", 3, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
		
		// navigate down a couple of levels and put a dir somewhere
		localFile = new File(localFile
				.getAbsolutePath());
		File[] children = localFile.listFiles();
		File newChildOfChild = null;
		if (children.length > 1) {
			File childFile = children[0];
			newChildOfChild =new File(
					childFile.getAbsolutePath(), newChildFileName);
			newChildOfChild.mkdirs();
		} else {
			Assert.fail("test setup failed, no children");
		}

		long timestampToUse = new Date().getTime();

		File localFileRoot = new File(localCollectionAbsolutePath);
		String targetIrodsAbsolutePath = irodsCollectionRootAbsolutePath + "/"
				+ rootCollection;

		// files now put, set up and call for the diff
		FileTreeDiffUtility fileTreeDiffUtility = new FileTreeDiffUtilityImpl(
				irodsAccount, irodsFileSystem.getIRODSAccessObjectFactory());

		FileTreeModel diffModel = fileTreeDiffUtility.generateDiffLocalToIRODS(
				localFileRoot, targetIrodsAbsolutePath,
				timestampToUse);
		irodsFileSystem.close();
		Assert.assertNotNull("null diffModel", diffModel);
		FileTreeNode fileTreeNode = (FileTreeNode) diffModel.getRoot();
		List<FileTreeDiffEntry> diffEntriesFound = new ArrayList<FileTreeDiffEntry>();
		
		descendModelAndFindTheDiff(DiffType.LEFT_HAND_PLUS, fileTreeNode, diffEntriesFound);
		TestCase.assertEquals("should have no cached diffs", 0, diffEntriesFound.size());
		
	}
	

}
