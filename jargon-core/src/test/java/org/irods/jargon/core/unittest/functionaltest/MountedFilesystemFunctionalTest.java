package org.irods.jargon.core.unittest.functionaltest;

import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.MountedCollectionAO;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.Assert;

public class MountedFilesystemFunctionalTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "MountedFilesystemFunctionalTest";
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
	public void testListTwoPagesOfFilesInMountedDir() throws Exception {

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		// this test requires the prop test.option.reg.basedir to be set, and to
		// contain the contents of test-data/reg. This is a manual setup step

		String targetCollectionName = "testListTwoPagesOfFilesInMountedDir";
		String scratchDir = "testListTwoPagesOfFilesInMountedDir";
		String testFilePrefix = "testFile";
		int count = 250;

		String localCollectionAbsolutePath = testingProperties.getProperty(TestingPropertiesHelper.IRODS_REG_BASEDIR);

		// String localCollectionAbsolutePath = "/home/test1/reg";
		String localScratchAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/' + scratchDir);

		FileGenerator.generateManyFilesInParentCollectionByAbsolutePath(localScratchAbsolutePath, testFilePrefix,
				".txt", count, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + targetCollectionName);

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
				.listDataObjectsUnderPath(targetIrodsCollection + "/" + scratchDir, 0);
		Assert.assertFalse("no results", actual.isEmpty());

		int countFoundMine = 0;

		for (CollectionAndDataObjectListingEntry entry : actual) {
			if (entry.getPathOrName().indexOf(testFilePrefix) > -1) {
				countFoundMine++;
			}
		}

		Assert.assertEquals("did not find all of the files for first page", 100, countFoundMine);
		CollectionAndDataObjectListingEntry lastEntry = actual.get(actual.size() - 1);

		Assert.assertFalse("this is last entry", lastEntry.isLastResult());

		// get next page

		actual = ao.listDataObjectsUnderPath(targetIrodsCollection, lastEntry.getCount());

	}

	/**
	 * Check for errors not closing results by asking for a count and getting a
	 * partial page n number of times
	 *
	 * @throws Exception
	 */
	@Test
	public void testListTwoPagesOfFilesInMountedDirNTimes() throws Exception {

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		// this test requires the prop test.option.reg.basedir to be set, and to
		// contain the contents of test-data/reg. This is a manual setup step

		String targetCollectionName = "testListTwoPagesOfFilesInMountedDirNTimes";
		String scratchDir = "testListTwoPagesOfFilesInMountedDirNTimes";
		String testFilePrefix = "testFile";
		int count = 250;
		int iterations = 50;

		String localCollectionAbsolutePath = testingProperties.getProperty(TestingPropertiesHelper.IRODS_REG_BASEDIR);

		// String localCollectionAbsolutePath = "/home/test1/reg";
		String localScratchAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/' + scratchDir);

		FileGenerator.generateManyFilesInParentCollectionByAbsolutePath(localScratchAbsolutePath, testFilePrefix,
				".txt", count, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + targetCollectionName);

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

		for (int i = 0; i < iterations; i++) {
			ao.listDataObjectsUnderPath(targetIrodsCollection + "/" + scratchDir, 0);
		}

	}

}
