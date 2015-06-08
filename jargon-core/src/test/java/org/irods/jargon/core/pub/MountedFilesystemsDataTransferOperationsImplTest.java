package org.irods.jargon.core.pub;

import java.io.File;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class MountedFilesystemsDataTransferOperationsImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "MountedFilesystemsDataTransferOperationsImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static org.irods.jargon.testutils.AssertionHelper assertionHelper = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		irodsFileSystem = IRODSFileSystem.instance();
		SettableJargonProperties settableJargonProperties = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		settableJargonProperties.setInternalCacheBufferSize(-1);
		settableJargonProperties.setInternalOutputStreamBufferSize(65535);
		irodsFileSystem.getIrodsSession().setJargonProperties(
				settableJargonProperties);

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

	@AfterClass
	public static void afterClass() throws Exception {

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testPutOneFile() throws Exception {

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		// generate a local scratch file
		String testFileName = "testPutOneFile.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						32 * 1024);
		String localCollectionAbsolutePath = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_REG_BASEDIR);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		IRODSFile unmountFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		unmountFile.delete();

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		mountedCollectionAO.createMountedFileSystemCollection(
				localCollectionAbsolutePath, targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
		assertionHelper.assertIrodsFileMatchesLocalFileChecksum(
				destFile.getAbsolutePath(), localFile.getAbsolutePath());
	}

	@Test
	public void testGetCollectionWithTwoFilesNoCallbacks() throws Exception {

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		String rootCollection = "testGetCollectionWithTwoFilesNoCallbacks";
		String returnedLocalCollection = "testGetCollectionWithTwoFilesNoCallbacksReturnedLocalFiles";

		String sourceDirAbsPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		File sourceDirFile = new File(sourceDirAbsPath + "/" + rootCollection);
		sourceDirFile.mkdirs();

		String localCollectionAbsolutePath = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_REG_BASEDIR);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		IRODSFile unmountFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		unmountFile.delete();

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		mountedCollectionAO.createMountedFileSystemCollection(
				localCollectionAbsolutePath, targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						sourceDirAbsPath + "/" + rootCollection, "subdir", 1,
						1, 1, "testFile", ".txt", 2, 2, 1, 2);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(sourceDirFile, destFile, null,
				null);
		destFile.close();

		destFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection
				+ "/" + rootCollection);

		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(
				sourceDirFile, (File) destFile);

		// now get the files into a local return collection and verify

		String returnLocalCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + returnedLocalCollection);
		File returnLocalFile = new File(returnLocalCollectionAbsolutePath);

		dataTransferOperationsAO.getOperation(destFile, returnLocalFile, null,
				null);

		String returnLocalCollectionCompareAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + returnedLocalCollection + '/' + rootCollection);
		File returnCompareLocalFile = new File(
				returnLocalCollectionCompareAbsolutePath);

		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(
				sourceDirFile, returnCompareLocalFile);
	}

	@Test(expected = DataNotFoundException.class)
	public void testGetFromMissingSubdirInMountedCollection() throws Exception {

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			throw new DataNotFoundException("throw to match expected");
		}

		String rootCollection = "testGetFromMissingSubdirInMountedCollection";
		String returnedLocalCollection = "testGetCollectionWithTwoFilesNoCallbacksReturnedLocalFiles";

		String sourceDirAbsPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		File sourceDirFile = new File(sourceDirAbsPath + "/" + rootCollection);
		sourceDirFile.mkdirs();

		String localCollectionAbsolutePath = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_REG_BASEDIR);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		IRODSFile unmountFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		unmountFile.delete();

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		mountedCollectionAO.createMountedFileSystemCollection(
				localCollectionAbsolutePath, targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + "/" + rootCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		String returnLocalCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + returnedLocalCollection);
		File returnLocalFile = new File(returnLocalCollectionAbsolutePath);

		dataTransferOperationsAO.getOperation(destFile, returnLocalFile, null,
				null);

	}

	@Test
	public void testCopyFromMountedToIrods() throws Exception {

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		String rootCollection = "testCopyFromMountedToIrods";
		String copyCollectionSubdir = "testCopyFromMountedToIrodsCopyTarget";

		String copyTargetCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, copyCollectionSubdir);

		String sourceDirAbsPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		File sourceDirFile = new File(sourceDirAbsPath + "/" + rootCollection);
		sourceDirFile.mkdirs();

		String localCollectionAbsolutePath = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_REG_BASEDIR);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		mountedCollectionAO.createMountedFileSystemCollection(
				localCollectionAbsolutePath, targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		FileGenerator.generateManyFilesInGivenDirectory(IRODS_TEST_SUBDIR_PATH
				+ "/" + rootCollection, "xxx", ".txt", 10, 10, 100);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(sourceDirFile, destFile, null,
				null);
		destFile.close();

		destFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection
				+ "/" + rootCollection);

		IRODSFile copyTargetIrodsFile = irodsFileFactory
				.instanceIRODSFile(copyTargetCollection);

		dataTransferOperationsAO
				.copy(destFile, copyTargetIrodsFile, null, null);

		copyTargetIrodsFile = irodsFileFactory.instanceIRODSFile(
				copyTargetIrodsFile.getAbsolutePath(), rootCollection);

		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(
				sourceDirFile, (File) copyTargetIrodsFile);
	}

	@Ignore
	// FIXME: overheaded for https://github.com/DICE-UNC/jargon/issues/117
	public void testCopyFromIrodsToMounted() throws Exception {

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		String irodsSourceDirName = "testCopyFromIrodsToMounted/sourceDir";
		String irodsTargetDirName = "testCopyFromIrodsToMounted/targetDir";
		String rootCollection = "root";

		String copyTargetCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, irodsTargetDirName);

		String localSourceDirPath = (IRODS_TEST_SUBDIR_PATH
				+ "/testCopyFromIrodsToMounted/" + rootCollection);

		String localCollectionAbsolutePath = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_REG_BASEDIR);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String irodsCollectionForTarget = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ irodsTargetDirName);

		String irodsCollectionForSource = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ irodsSourceDirName);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		IRODSFile mountedCollectionTargetFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						irodsCollectionForTarget);

		IRODSFile irodsSourceFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(irodsCollectionForSource);

		mountedCollectionTargetFile.delete();

		mountedCollectionAO.unmountACollection(irodsCollectionForTarget,
				irodsAccount.getDefaultStorageResource());

		mountedCollectionTargetFile.mkdirs();

		mountedCollectionAO.createMountedFileSystemCollection(
				localCollectionAbsolutePath, irodsCollectionForTarget,
				irodsAccount.getDefaultStorageResource());

		FileGenerator.generateManyFilesInGivenDirectory(localSourceDirPath,
				"xxx", ".txt", 10, 10, 100);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);

		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		String localSourceDirAbsPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(localSourceDirPath);
		File sourceDirFile = new File(localSourceDirAbsPath);

		dataTransferOperationsAO.putOperation(sourceDirFile, irodsSourceFile,
				null, null);

		IRODSFile copyTargetIrodsFile = irodsFileFactory
				.instanceIRODSFile(copyTargetCollection);

		irodsSourceFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(irodsCollectionForSource, rootCollection);

		dataTransferOperationsAO.copy(irodsSourceFile, copyTargetIrodsFile,
				null, null);

		IRODSFile actual = irodsFileFactory.instanceIRODSFile(
				copyTargetIrodsFile.getAbsolutePath(), rootCollection);

		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(
				sourceDirFile, (File) actual);
	}

	@Ignore
	// FIXME: overheaded for https://github.com/DICE-UNC/jargon/issues/117
	public void testMoveFromMountedToIrods() throws Exception {

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		String rootCollection = "testMoveFromMountedToIrods";
		String copyCollectionSubdir = "MountedFileSystemsDataTansferOperationsImplTestMoveTarget";

		String copyTargetCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, copyCollectionSubdir);

		String sourceDirAbsPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		File sourceDirFile = new File(sourceDirAbsPath + "/" + rootCollection);
		sourceDirFile.mkdirs();

		String localCollectionAbsolutePath = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_REG_BASEDIR);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		IRODSFile unmountFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		unmountFile.delete();

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		mountedCollectionAO.createMountedFileSystemCollection(
				localCollectionAbsolutePath, targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		FileGenerator.generateManyFilesInGivenDirectory(IRODS_TEST_SUBDIR_PATH
				+ "/" + rootCollection, "xxx", ".txt", 10, 10, 100);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(sourceDirFile, destFile, null,
				null);
		destFile.close();

		destFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection
				+ "/" + rootCollection);

		IRODSFile copyTargetIrodsFile = irodsFileFactory
				.instanceIRODSFile(copyTargetCollection);

		dataTransferOperationsAO.move(destFile, copyTargetIrodsFile);

		copyTargetIrodsFile = irodsFileFactory.instanceIRODSFile(
				copyTargetIrodsFile.getAbsolutePath(), rootCollection);

		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(
				sourceDirFile, (File) copyTargetIrodsFile);
	}

}
