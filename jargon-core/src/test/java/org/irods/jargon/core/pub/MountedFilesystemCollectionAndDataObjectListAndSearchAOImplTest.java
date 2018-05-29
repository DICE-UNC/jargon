package org.irods.jargon.core.pub;

import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class MountedFilesystemCollectionAndDataObjectListAndSearchAOImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "MountedFilesystemCollectionAndDataObjectListAndSearchAOImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(testingProperties);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		irodsFileSystem.closeAndEatExceptions();

	}

	@Test
	public void testListInMountedDirDataObjs() throws Exception {

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		// this test requires the prop test.option.reg.basedir to be set, and to
		// contain the contents of test-data/reg. This is a manual setup step

		String scratchDir = "testListInMountedDirDataObjs";
		String testFilePrefix = "testFile";
		int count = 10;

		String localCollectionAbsolutePath = testingProperties.getProperty(TestingPropertiesHelper.IRODS_REG_BASEDIR);

		String localScratchAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/' + scratchDir);

		FileGenerator.generateManyFilesInParentCollectionByAbsolutePath(localScratchAbsolutePath, testFilePrefix,
				".txt", count, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getMountedCollectionAO(irodsAccount);

		IRODSFile unmountFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		unmountFile.delete();

		mountedCollectionAO.unmountACollection(targetIrodsCollection, irodsAccount.getDefaultStorageResource());

		mountedCollectionAO.createMountedFileSystemCollection(localCollectionAbsolutePath, targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		// put the scratch files to the mount

		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(localScratchAbsolutePath, targetIrodsCollection, irodsAccount.getDefaultStorageResource(),
				null, null);

		CollectionAndDataObjectListAndSearchAO ao = irodsFileSystem.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		List<CollectionAndDataObjectListingEntry> actual = ao
				.listDataObjectsAndCollectionsUnderPath(targetIrodsCollection + "/" + scratchDir);
		Assert.assertFalse("no results", actual.isEmpty());

		Assert.assertEquals("did not find all of the files", count, actual.size());

		// inspect first data object

		CollectionAndDataObjectListingEntry entry = actual.get(0);

		Assert.assertEquals("wrong parent name", targetIrodsCollection + "/" + scratchDir, entry.getParentPath());
		Assert.assertEquals("should be data object", ObjectType.DATA_OBJECT, entry.getObjectType());
		Assert.assertEquals("should be mounted coll", ObjStat.SpecColType.MOUNTED_COLL, entry.getSpecColType());
		Assert.assertEquals("should have a count of 1", 1, entry.getCount());

		// inspect last data object

		entry = actual.get(actual.size() - 1);
		Assert.assertEquals("should have a count equal to size", actual.size(), entry.getCount());
		Assert.assertTrue("should be last record", entry.isLastResult());

	}

	/**
	 * Looking for any leaks in file handles
	 *
	 * @throws Exception
	 */
	@Test
	public void testListCollectionsInMountedDirWithMultiplePagesMultipleTimes() throws Exception {

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		// this test requires the prop test.option.reg.basedir to be set, and to
		// contain the contents of test-data/reg. This is a manual setup step

		String targetCollectionName = "testListCollectionsInMountedDirWithMultiplePagesMultipleTimes";
		String scratchDir = "testListCollectionsInMountedDirWithMultiplePagesMultipleTimes";
		String testFilePrefix = "subdir";
		int count = 300;
		int times = 100;

		String localCollectionAbsolutePath = testingProperties.getProperty(TestingPropertiesHelper.IRODS_REG_BASEDIR);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getMountedCollectionAO(irodsAccount);

		IRODSFile unmountFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		unmountFile.delete();

		mountedCollectionAO.unmountACollection(targetIrodsCollection, irodsAccount.getDefaultStorageResource());

		mountedCollectionAO.createMountedFileSystemCollection(localCollectionAbsolutePath, targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		// mkdirs under the mount

		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		IRODSFile dirFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection, targetCollectionName);
		dirFile.mkdirs();
		String testDirPath = dirFile.getAbsolutePath();

		for (int i = 0; i < count; i++) {
			dirFile = irodsFileFactory.instanceIRODSFile(testDirPath, testFilePrefix + i);
			dirFile.mkdir();
		}

		CollectionAndDataObjectListAndSearchAO ao = irodsFileSystem.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		for (int i = 0; i < times; i++) {
			ao.listCollectionsUnderPath(targetIrodsCollection + "/" + scratchDir, 0);
		}

		// looking for run w/o exception
		Assert.assertTrue(true);

	}

	@Test
	public void testListCollectionsInMountedDirWithMultiplePages() throws Exception {

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		// this test requires the prop test.option.reg.basedir to be set, and to
		// contain the contents of test-data/reg. This is a manual setup step

		String targetCollectionName = "testListCollectionsInMountedDirWithMultiplePages";
		String scratchDir = "testListCollectionsInMountedDirWithMultiplePages";
		String testFilePrefix = "subdir";
		int count = 200;

		String localCollectionAbsolutePath = testingProperties.getProperty(TestingPropertiesHelper.IRODS_REG_BASEDIR);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getMountedCollectionAO(irodsAccount);

		IRODSFile unmountFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		unmountFile.delete();

		mountedCollectionAO.unmountACollection(targetIrodsCollection, irodsAccount.getDefaultStorageResource());

		mountedCollectionAO.createMountedFileSystemCollection(localCollectionAbsolutePath, targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		// mkdirs under the mount

		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		IRODSFile dirFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection, targetCollectionName);
		dirFile.mkdirs();
		String testDirPath = dirFile.getAbsolutePath();

		for (int i = 0; i < count; i++) {
			dirFile = irodsFileFactory.instanceIRODSFile(testDirPath, testFilePrefix + i);
			dirFile.mkdir();
		}

		CollectionAndDataObjectListAndSearchAO ao = irodsFileSystem.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		List<CollectionAndDataObjectListingEntry> actual = ao
				.listCollectionsUnderPath(targetIrodsCollection + "/" + scratchDir, 0);
		Assert.assertFalse("no results", actual.isEmpty());

		int countFoundMine = 0;

		for (CollectionAndDataObjectListingEntry entry : actual) {
			if (entry.getPathOrName().indexOf(testFilePrefix) > -1) {
				countFoundMine++;
			}
		}

		Assert.assertEquals("did not find all of the files", count, countFoundMine);

	}

	@Test
	public void testListMissingCollectionInMountedDir() throws Exception {

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		// this test requires the prop test.option.reg.basedir to be set, and to
		// contain the contents of test-data/reg. This is a manual setup step

		String targetCollectionName = "testListMissingCollectionInMountedDir";
		String testSubdir = "subdir";

		String localCollectionAbsolutePath = testingProperties.getProperty(TestingPropertiesHelper.IRODS_REG_BASEDIR);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getMountedCollectionAO(irodsAccount);

		IRODSFile unmountFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		unmountFile.delete();

		mountedCollectionAO.unmountACollection(targetIrodsCollection, irodsAccount.getDefaultStorageResource());

		mountedCollectionAO.createMountedFileSystemCollection(localCollectionAbsolutePath, targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		IRODSFile dirFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection, targetCollectionName);
		dirFile.mkdirs();
		String testDirPath = dirFile.getAbsolutePath();

		CollectionAndDataObjectListAndSearchAO ao = irodsFileSystem.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		List<CollectionAndDataObjectListingEntry> actual = ao.listCollectionsUnderPath(testDirPath + "/" + testSubdir,
				0);
		Assert.assertTrue("should be empty collection", actual.isEmpty());
	}

}
