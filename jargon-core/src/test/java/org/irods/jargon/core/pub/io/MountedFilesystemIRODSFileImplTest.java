/**
 * 
 */
package org.irods.jargon.core.pub.io;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.MountedCollectionAO;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class MountedFilesystemIRODSFileImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "MountedFilesystemIRODSFileTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(
				testingProperties);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public final void testListFiles() throws Exception {
		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		// this test requires the prop test.option.reg.basedir to be set, and to
		// contain the contents of test-data/reg. This is a manual setup step

		String scratchDir = "testListFiles";
		String testFilePrefix = "testFile";
		int count = 200;

		String localCollectionAbsolutePath = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_REG_BASEDIR);

		String localScratchAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + scratchDir);

		FileGenerator.generateManyFilesInParentCollectionByAbsolutePath(
				localScratchAbsolutePath, testFilePrefix, ".txt", count, 1, 2);

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

		// put the scratch files to the mount

		DataTransferOperations dto = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dto.putOperation(localScratchAbsolutePath, targetIrodsCollection,
				irodsAccount.getDefaultStorageResource(), null, null);

		IRODSFile parentFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection, scratchDir);

		File[] irodsFiles = parentFile.listFiles();
		Assert.assertNotNull(irodsFiles);
		Assert.assertTrue("no results", irodsFiles.length > 0);
		Assert.assertEquals("did not get all expected files", count,
				irodsFiles.length);

	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#isFile()}.
	 */
	@Test
	public final void testIsFileWhenFile() throws Exception {
		// this test requires the prop test.option.reg.basedir to be set, and to
		// contain the contents of test-data/reg. This is a manual setup step

		String scratchDir = "testIsFileWhenFile";
		String testFileName = "testIsFileWhenFile.txt";

		String localCollectionAbsolutePath = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_REG_BASEDIR);

		String localScratchAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + scratchDir);

		FileGenerator.generateFileOfFixedLengthGivenName(
				localScratchAbsolutePath, testFileName, 1);

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

		// put the scratch files to the mount

		DataTransferOperations dto = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dto.putOperation(localScratchAbsolutePath, targetIrodsCollection,
				irodsAccount.getDefaultStorageResource(), null, null);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + scratchDir,
						testFileName);

		Assert.assertTrue("this should be a file", irodsFile.isFile());
	}

	@Test
	public final void testIsDirectory() throws Exception {

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		String subdirName = "testIsDirectory";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String localCollectionAbsolutePath = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_REG_BASEDIR);

		// now get an irods file and see if it is readable, it should be

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

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

		IRODSFile dirFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection, subdirName);
		dirFile.mkdirs();
		dirFile.reset();

		boolean isDir = dirFile.isDirectory();
		Assert.assertTrue("this should be a collection", isDir);
	}

	@Test
	public final void testIsDirectoryNotExists() throws Exception {

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		String subdirName = "testIsDirectoryNotExists";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String localCollectionAbsolutePath = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_REG_BASEDIR);

		// now get an irods file and see if it is readable, it should be

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

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

		IRODSFile dirFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection, subdirName);

		boolean isDir = dirFile.isDirectory();
		Assert.assertFalse("this should not exist", isDir);
	}

	@Test
	public final void testExistsDir() throws Exception {
		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		String subdirName = "testExistsDir";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String localCollectionAbsolutePath = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_REG_BASEDIR);

		// now get an irods file and see if it is readable, it should be

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

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

		IRODSFile dirFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection, subdirName);
		dirFile.mkdirs();
		dirFile.reset();

		boolean exists = dirFile.exists();
		Assert.assertTrue("this should exists", exists);
	}

	@Test
	public final void testExistsDirNotExists() throws Exception {
		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		String subdirName = "testExistsDirNotExists";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String localCollectionAbsolutePath = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_REG_BASEDIR);

		// now get an irods file and see if it is readable, it should be

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

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

		IRODSFile dirFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection, subdirName);

		boolean exists = dirFile.exists();
		Assert.assertFalse("this should not exist", exists);
	}
}
