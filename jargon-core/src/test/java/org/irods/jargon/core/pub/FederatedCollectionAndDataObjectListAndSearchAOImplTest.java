package org.irods.jargon.core.pub;

import java.io.File;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test collection list and search operations between federated zones
 * <p/>
 * Note that the test properties and server config must be set up per the
 * test-scripts/fedTestSetup.txt file. By default, the tests will be skipped.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class FederatedCollectionAndDataObjectListAndSearchAOImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "FederatedCollectionAndDataObjectListAndSearchAOImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;
	public static final String FED_READ_DIR = "fedread";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
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

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if (irodsFileSystem != null) {
			irodsFileSystem.closeAndEatExceptions();
		}
	}

	@Test
	public void testListCollectionWithOneDataObject() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		/*
		 * put a collection on the federated zone, this will be accessed from a
		 * user on the primary zone this will use the fedread directory as
		 * specified in the6 test-scripts/fedtestsetup_fedzonex.sh scripts.
		 */

		String fileName = "testListCollectionWithOneDataObject.txt";
		String testSubdir = "testListCollectionWithOneDataObject";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);

		String targetIrodsPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testSubdir);

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, fileName, 3);
		File localFile = new File(localFilePath);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsPath);

		// delete to clean up
		destFile.deleteWithForceOption();
		destFile.mkdirs();

		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		/*
		 * setup done, now connect from the first zone and try to list the coll
		 * with the data object
		 */

		IRODSAccount fedAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		CollectionAndDataObjectListAndSearchAO collectionListAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(fedAccount);
		List<CollectionAndDataObjectListingEntry> entries = collectionListAndSearchAO
				.listDataObjectsUnderPath(targetIrodsPath, 0);
		Assert.assertNotNull("null entries returned", entries);

	}

	@Test
	public void testListDataObjectsUnderPathInAnotherZone() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String fileName = "testListDataObjectsUnderPathInAnotherZone.txt";
		String testSubdir = "testListDataObjectsUnderPathInAnotherZone";
		int count = 20;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);

		IRODSFile irodsFile = null;

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testSubdir);
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdir();
		irodsFile.close();

		String myTarget = "";

		for (int i = 0; i < count; i++) {
			myTarget = targetIrodsCollection + "/c" + (10000 + i) + fileName;
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(myTarget);
			irodsFile.createNewFile();
			irodsFile.close();
		}

		IRODSAccount zone1Account = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(zone1Account);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.listDataObjectsUnderPath(targetIrodsCollection, 0);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		CollectionAndDataObjectListingEntry entry = entries
				.get(entries.size() - 1);
		Assert.assertTrue(entry.isLastResult());
		Assert.assertEquals(entry.getCount(), entries.size());
		Assert.assertEquals("did not find all of the entries", count,
				entries.size());

		// bounce thru and make sure each is a data object with the correct name

		for (CollectionAndDataObjectListingEntry resultEntry : entries) {
			Assert.assertTrue(
					"this is not a data object",
					resultEntry.getObjectType() == CollectionAndDataObjectListingEntry.ObjectType.DATA_OBJECT);
			Assert.assertTrue("file name not correctly returned", resultEntry
					.getPathOrName().indexOf(fileName) > -1);
		}

	}

	/**
	 * Try and list stuff from the federated zone underneath the /zonename for
	 * the fed zone, should get at least the home dir
	 * 
	 * @throws Exception
	 */
	@Test
	public void testListCollectionsUnderPathInAnotherZoneFromRoot()
			throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);
		String targetIrodsCollection = "/" + irodsAccount.getZone();

		IRODSAccount zone1Account = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(zone1Account);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.listCollectionsUnderPath(targetIrodsCollection, 0);
		Assert.assertNotNull("null result from query", entries);
		Assert.assertFalse("should not have been an empty result list",
				entries.isEmpty());

	}

	@Test
	public void testListCollectionsUnderPathNotExists() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);
		String targetIrodsCollection = "/" + irodsAccount.getZone()
				+ "/idontexistatall";

		IRODSAccount zone1Account = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(zone1Account);

		boolean gotError = false;

		try {
			actual.listCollectionsUnderPath(targetIrodsCollection, 0);
		} catch (FileNotFoundException e) {
			gotError = true;
		}

		Assert.assertTrue("should have gotten fileNotFoundException", gotError);

	}

	@Test
	public void testListCollectionsUnderPathWithPermissionsInAnotherZone()
			throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String subdirPrefix = "testListCollectionsUnderPathWithPermissionsInAnotherZone";
		int count = 20;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ subdirPrefix);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdir();
		irodsFile.close();

		// add another acl for another user to this file

		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		collectionAO.setAccessPermissionWrite(irodsAccount.getZone(), irodsFile
				.getAbsolutePath(), testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
				false);

		String myTarget = "";

		for (int i = 0; i < count; i++) {
			myTarget = targetIrodsCollection + "/c" + (10000 + i)
					+ subdirPrefix;
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(myTarget);
			irodsFile.mkdir();
			irodsFile.close();
			collectionAO
					.setAccessPermissionWrite(
							irodsAccount.getZone(),
							irodsFile.getAbsolutePath(),
							testingProperties
									.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
							false);
		}

		IRODSAccount zone1Account = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		CollectionAndDataObjectListAndSearchAOImpl actual = (CollectionAndDataObjectListAndSearchAOImpl) irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(zone1Account);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.listCollectionsUnderPathWithPermissions(targetIrodsCollection,
						0);

		Assert.assertNotNull(entries);
		Assert.assertFalse("entries was empty", entries.isEmpty());
		CollectionAndDataObjectListingEntry entry = entries
				.get(entries.size() - 1);
		Assert.assertEquals("i am not the owner", irodsAccount.getUserName(),
				entry.getOwnerName());

		// each entry has two permissions, will have extra for fed so tolerate
		// that
		for (CollectionAndDataObjectListingEntry actualEntry : entries) {
			TestCase.assertTrue("did not get both expected permissions",
					actualEntry.getUserFilePermission().size() > 2);
		}

	}

	@Test
	public void testListDataObjectsUnderPathWithAccessInfoInAnotherZone()
			throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String fileName = "testListDataObjectsUnderPathWithAccessInfo.txt";
		String testSubdir = "testListDataObjectsUnderPathWithAccessInfo";
		int count = 10;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);
		IRODSFile irodsFile = null;

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testSubdir);
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdir();
		irodsFile.close();

		String myTarget = "";

		for (int i = 0; i < count; i++) {
			myTarget = targetIrodsCollection + "/c" + (10000 + i) + fileName;
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(myTarget);
			irodsFile.createNewFile();
			irodsFile.close();
			dataObjectAO
					.setAccessPermissionWrite(
							irodsAccount.getZone(),
							irodsFile.getAbsolutePath(),
							testingProperties
									.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));
		}

		IRODSAccount zone1Account = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(zone1Account);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.listDataObjectsUnderPathWithPermissions(targetIrodsCollection,
						0);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		CollectionAndDataObjectListingEntry entry = entries
				.get(entries.size() - 1);
		Assert.assertTrue(entry.isLastResult());
		Assert.assertEquals(count, entries.size());

		// bounce thru and make sure each is a data object with the correct name

		for (CollectionAndDataObjectListingEntry resultEntry : entries) {
			Assert.assertTrue(
					"this is not a data object",
					resultEntry.getObjectType() == CollectionAndDataObjectListingEntry.ObjectType.DATA_OBJECT);
			Assert.assertTrue("file name not correctly returned", resultEntry
					.getPathOrName().indexOf(fileName) > -1);
		}

		// each entry has two permissions, will have extra for fed so tolerate
		// that
		for (CollectionAndDataObjectListingEntry actualEntry : entries) {
			TestCase.assertTrue("did not get both expected permissions",
					actualEntry.getUserFilePermission().size() > 2);
		}

	}

	@Test
	public void testGetFullObjectForCollectionInAnotherZone() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		Object actual = listAndSearchAO
				.getFullObjectForType(targetIrodsCollection);
		Assert.assertNotNull("object was null", actual);
		boolean isCollection = actual instanceof Collection;
		Assert.assertTrue("was not a collection", isCollection);

	}

}
