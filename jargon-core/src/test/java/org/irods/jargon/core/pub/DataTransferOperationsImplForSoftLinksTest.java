package org.irods.jargon.core.pub;

import java.io.File;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.domain.ObjStat.SpecColType;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.Assert;

public class DataTransferOperationsImplForSoftLinksTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "DataTransferOperationsImplForSoftLinksTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		irodsFileSystem = IRODSFileSystem.instance();
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(testingProperties);
		scratchFileUtils.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.clearIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
	}

	@AfterClass
	public static void afterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	/**
	 * Put one file to a soft linked collection
	 *
	 * @throws Exception
	 */
	@Test
	public void testPutOneFileToSoftLinkDir() throws Exception {
		String sourceCollectionName = "testPutOneFileToSoftLinkDirSource";
		String targetCollectionName = "testPutOneFileToSoftLinkDirTarget";
		String testFileName = "testPutOneFileToSoftLinkDir.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		String sourceIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + sourceCollectionName);

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + targetCollectionName);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getMountedCollectionAO(irodsAccount);

		mountedCollectionAO.unmountACollection(targetIrodsCollection, irodsAccount.getDefaultStorageResource());

		// set up source collection
		IRODSFile sourceFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(sourceIrodsCollection);
		sourceFile.mkdirs();

		// create the soft link
		mountedCollectionAO.createASoftLink(sourceIrodsCollection, targetIrodsCollection);

		File localFile = new File(localFileName);

		// now put the file

		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		// now get the obj stat on that target file
		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		ObjStat actual = listAndSearchAO.retrieveObjectStatForPath(targetIrodsCollection + "/" + testFileName);

		Assert.assertEquals("did not get data object", CollectionAndDataObjectListingEntry.ObjectType.DATA_OBJECT,
				actual.getObjectType());
		Assert.assertEquals("should be marked as a soft coll", ObjStat.SpecColType.LINKED_COLL,
				actual.getSpecColType());

	}

	/**
	 * Get one file from a soft linked collection
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetOneFileFromSoftLinkDir() throws Exception {
		String sourceCollectionName = "testGetOneFileFromSoftLinkDirSource";
		String targetCollectionName = "testGetOneFileFromSoftLinkDirTarget";
		String testFileName = "testGetOneFileFromSoftLinkDir.txt";
		String retrievedFileName = "testGetOneFileFromSoftLinkDirRetrieved.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		String sourceIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + sourceCollectionName);

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + targetCollectionName);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getMountedCollectionAO(irodsAccount);

		mountedCollectionAO.unmountACollection(targetIrodsCollection, irodsAccount.getDefaultStorageResource());

		// set up source collection
		IRODSFile sourceFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(sourceIrodsCollection);
		sourceFile.mkdirs();

		// create the soft link
		mountedCollectionAO.createASoftLink(sourceIrodsCollection, targetIrodsCollection);

		File localFile = new File(localFileName);

		// now put the file

		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		// get the file
		destFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);
		File actual = new File(localFile.getParent(), retrievedFileName);
		dataTransferOperationsAO.getOperation(destFile, actual, null, null);
		Assert.assertTrue("local file does not exist", actual.exists());
	}

	@Test
	public void testCopySoftLinkedCollectionToTargetCollectionUsingSoftLinkCollectionName() throws Exception {

		String sourceCollectionName = "testCopySoftLinkedCollectionToTargetCollectionUsingSoftLinkCollectionNameSource";
		String targetCollectionName = "testCopySoftLinkedCollectionToTargetCollectionUsingSoftLinkCollectionNameTarget";
		String copyToCollectionName = "testCopySoftLinkedCollectionToTargetCollectionUsingSoftLinkCollectionNameCopyToDir";
		String testFileName = "testCopySoftLinkedCollectionToTargetCollectionUsingSoftLinkCollectionNameSource.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		String sourceIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + sourceCollectionName);

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + targetCollectionName);

		String copyToIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + copyToCollectionName);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getMountedCollectionAO(irodsAccount);

		mountedCollectionAO.unmountACollection(targetIrodsCollection, irodsAccount.getDefaultStorageResource());

		// set up source collection
		IRODSFile sourceFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(sourceIrodsCollection);
		sourceFile.mkdirs();

		// create the soft link
		mountedCollectionAO.createASoftLink(sourceIrodsCollection, targetIrodsCollection);

		File localFile = new File(localFileName);

		// now put the file

		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		dataTransferOperationsAO.copy(destFile.getAbsolutePath(), "", copyToIrodsCollection, null, null);

		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		ObjStat actual = listAndSearchAO.retrieveObjectStatForPath(copyToIrodsCollection + "/" + targetCollectionName);
		Assert.assertEquals("should now be a normal coll", SpecColType.NORMAL, actual.getSpecColType());

	}

}
