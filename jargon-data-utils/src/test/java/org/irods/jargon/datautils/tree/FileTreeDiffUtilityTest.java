package org.irods.jargon.datautils.tree;

import java.io.File;
import java.util.Enumeration;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.datautils.tree.FileTreeDiffEntry.DiffType;
import org.irods.jargon.testutils.AssertionHelper;
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
	private static AssertionHelper assertionHelper = null;

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
		assertionHelper = new AssertionHelper();
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
		FileTreeModel diffModel = FileTreeDiffUtility.generateDiffLocalToIRODS(
				localFileRoot, irodsAccount,
				irodsFileSystem.getIRODSAccessObjectFactory(),
				targetIrodsAbsolutePath);
		irodsFileSystem.close();
		Assert.assertNotNull("null diffModel", diffModel);
		FileTreeNode fileTreeNode = (FileTreeNode) diffModel.getRoot();
		FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) fileTreeNode
				.getUserObject();
		Assert.assertEquals("did not get the root no-diff entry",
				FileTreeDiffEntry.DiffType.DIRECTORY_NO_DIFF,
				fileTreeDiffEntry.getDiffType());

		File[] childrenOfLocal = localFile.listFiles();
		Enumeration nodes = fileTreeNode.children();
		compareFileTreeToNodesForDirMatchesAndNoDiffs(childrenOfLocal, nodes);

	}

	@Test
	public void testFileTreeDiffIrodsPlusOneDir() throws Exception {

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
		FileTreeModel diffModel = FileTreeDiffUtility.generateDiffLocalToIRODS(
				localFileRoot, irodsAccount,
				irodsFileSystem.getIRODSAccessObjectFactory(),
				targetIrodsAbsolutePath);
		irodsFileSystem.close();
		Assert.assertNotNull("null diffModel", diffModel);
		FileTreeNode fileTreeNode = (FileTreeNode) diffModel.getRoot();
		FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) fileTreeNode
				.getUserObject();
		Assert.assertEquals("did not get the root no-diff entry",
				FileTreeDiffEntry.DiffType.DIRECTORY_NO_DIFF,
				fileTreeDiffEntry.getDiffType());

		File[] childrenOfLocal = localFile.listFiles();
		Enumeration nodes = fileTreeNode.children();
		compareFileTreeToNodesForDirMatchesAndExpectADiff(childrenOfLocal,
				nodes, DiffType.RIGHT_HAND_PLUS, "newChild");
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
		FileTreeModel diffModel = FileTreeDiffUtility.generateDiffLocalToIRODS(
				localFileRoot, irodsAccount,
				irodsFileSystem.getIRODSAccessObjectFactory(),
				targetIrodsAbsolutePath);
		irodsFileSystem.close();
		Assert.assertNotNull("null diffModel", diffModel);
		FileTreeNode fileTreeNode = (FileTreeNode) diffModel.getRoot();
		FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) fileTreeNode
				.getUserObject();
		Assert.assertEquals("did not get the root no-diff entry",
				FileTreeDiffEntry.DiffType.DIRECTORY_NO_DIFF,
				fileTreeDiffEntry.getDiffType());

		File[] childrenOfLocal = localFile.listFiles();
		Enumeration nodes = fileTreeNode.children();
		compareFileTreeToNodesForDirMatchesAndExpectADiff(childrenOfLocal,
				nodes, DiffType.LEFT_HAND_PLUS, "newChild");
	}

	private void compareFileTreeToNodesForDirMatchesAndNoDiffs(
			final File[] files, final Enumeration nodes) {
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
					Enumeration childNodes = fileTreeNode.children();
					compareFileTreeToNodesForDirMatchesAndNoDiffs(
							childrenOfLocal, childNodes);
				} else {
					Assert.fail("node is out of synch (missing) for file:"
							+ child.getAbsolutePath());
				}
			}
		}
	}

	private void compareFileTreeToNodesForDirMatchesAndExpectADiff(
			final File[] files, final Enumeration nodes,
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
					Enumeration childNodes = fileTreeNode.children();
					compareFileTreeToNodesForDirMatchesAndExpectADiff(
							childrenOfLocal, childNodes, diffType, fileName);
				} else {
					Assert.fail("node is out of synch (missing) for file:"
							+ child.getAbsolutePath());
				}
			}
		}
	}

}
