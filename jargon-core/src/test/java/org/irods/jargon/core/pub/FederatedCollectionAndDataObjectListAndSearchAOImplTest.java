package org.irods.jargon.core.pub;

import java.io.File;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
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

	/**
	 * put a collection on the federated zone, this will be accessed from a user
	 * on the primary zone this will use the fedread directory as specified in
	 * the6 test-scripts/fedtestsetup_fedzonex.sh scripts.
	 */
	@Test
	public void testListCollectionWithOneDataObject() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

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

	/**
	 * For a collection, add a federated user, and then query the collections
	 * with permissions to make sure the cross zone user is found, with the
	 * appropriate zone.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testListCollectionsUnderPathWithPermissionsIncludingACrossZoneUser()
			throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String testCollectionName = "testListCollectionsUnderPathWithPermissionsIncludingACrossZoneUser";
		String subCollName = "testListCollectionsUnderPathWithPermissionsIncludingACrossZoneUserSubColl";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollectionName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection, subCollName);
		irodsFile.deleteWithForceOption();
		irodsFile.mkdirs();

		collectionAO
				.setAccessPermissionRead(
						"",
						irodsFile.getAbsolutePath(),
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
						true);

		collectionAO
				.setAccessPermissionRead(
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_ZONE_KEY),
						irodsFile.getAbsolutePath(),
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_USER_KEY),
						true);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		List<CollectionAndDataObjectListingEntry> collections = collectionAndDataObjectListAndSearchAO
				.listCollectionsUnderPathWithPermissions(targetIrodsCollection,
						0);

		Assert.assertEquals("should have 1 collection", 1, collections.size());

		boolean foundCrossZone = false;

		List<UserFilePermission> userFilePermissions = collections.get(0)
				.getUserFilePermission();
		Assert.assertNotNull("got a null userFilePermissions",
				userFilePermissions);
		Assert.assertFalse("did not find the three permissions",
				userFilePermissions.isEmpty());
		Assert.assertTrue("did not find the three permissions",
				userFilePermissions.size() >= 3);

		for (UserFilePermission userFilePermission : userFilePermissions) {
			if (userFilePermission
					.getUserZone()
					.equals(testingProperties
							.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_ZONE_KEY))
					&& userFilePermission
							.getNameWithZone()
							.equals(testingProperties
									.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_USER_KEY)
									+ '#'
									+ testingProperties
											.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_ZONE_KEY))) {
				foundCrossZone = true;
			}

		}

		Assert.assertTrue("did not find cross zone user with zone info",
				foundCrossZone);
	}

	/**
	 * For a collection, add a federated user, and then query the collections
	 * with permissions to make sure the cross zone user is found, with the
	 * appropriate zone. In this case the query is made from a user logged in to
	 * zone 2. The query should look at the data on zone1
	 * 
	 * @throws Exception
	 */
	@Ignore
	// TODO: check https://github.com/DICE-UNC/jargon/issues/127
	public void testListCollectionsUnderPathWithPermissionsIncludingACrossZoneUserQueryFromZone2()
			throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String testCollectionName = "testListCollectionsUnderPathWithPermissionsIncludingACrossZoneUserQueryFromZone2";
		String subCollName = "testListCollectionsUnderPathWithPermissionsIncludingACrossZoneUserQueryFromZone2SubColl";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollectionName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection, subCollName);
		irodsFile.deleteWithForceOption();
		irodsFile.mkdirs();

		collectionAO
				.setAccessPermissionRead(
						"",
						irodsFile.getAbsolutePath(),
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
						true);

		collectionAO
				.setAccessPermissionRead(
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_ZONE_KEY),
						irodsFile.getAbsolutePath(),
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_USER_KEY),
						true);

		IRODSAccount zone2Account = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);
		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(zone2Account);

		List<CollectionAndDataObjectListingEntry> collections = collectionAndDataObjectListAndSearchAO
				.listCollectionsUnderPathWithPermissions(targetIrodsCollection,
						0);

		Assert.assertEquals("should have 1 collection", 1, collections.size());

		boolean foundCrossZone = false;

		List<UserFilePermission> userFilePermissions = collections.get(0)
				.getUserFilePermission();
		Assert.assertNotNull("got a null userFilePermissions",
				userFilePermissions);
		Assert.assertFalse("did not find the permissions",
				userFilePermissions.isEmpty());
		Assert.assertTrue("did not find the three permissions",
				userFilePermissions.size() >= 3);

		for (UserFilePermission userFilePermission : userFilePermissions) {
			if (userFilePermission
					.getUserZone()
					.equals(testingProperties
							.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_ZONE_KEY))
					&& userFilePermission
							.getUserName()
							.equals(testingProperties
									.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_USER_KEY))) {
				foundCrossZone = true;
			}

		}

		Assert.assertTrue("did not find cross zone user with zone info",
				foundCrossZone);
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
		IRODSAccount zone1Account = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile irodsFile = null;

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testSubdir);
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.deleteWithForceOption();
		irodsFile.mkdir();
		irodsFile.close();

		String myTarget = "";

		for (int i = 0; i < count; i++) {
			myTarget = targetIrodsCollection + "/c" + (10000 + i) + fileName;
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(myTarget);
			irodsFile.createNewFile();
			irodsFile.close();
			dataObjectAO.setAccessPermissionWrite(zone1Account.getZone(),
					irodsFile.getAbsolutePath(), zone1Account.getUserName());
		}

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

		UserFilePermission ownerEntry = null;
		UserFilePermission crossZoneEntry = null;

		for (CollectionAndDataObjectListingEntry resultEntry : entries) {
			Assert.assertTrue(
					"this is not a data object",
					resultEntry.getObjectType() == CollectionAndDataObjectListingEntry.ObjectType.DATA_OBJECT);
			Assert.assertTrue("file name not correctly returned", resultEntry
					.getPathOrName().indexOf(fileName) > -1);
			Assert.assertTrue("did not get both expected permissions",
					resultEntry.getUserFilePermission().size() == 2);

			for (UserFilePermission permission : resultEntry
					.getUserFilePermission()) {
				if (permission.getFilePermissionEnum() == FilePermissionEnum.OWN) {
					ownerEntry = permission;
				} else if (permission.getFilePermissionEnum() == FilePermissionEnum.WRITE) {
					crossZoneEntry = permission;
				}
			}

			Assert.assertNotNull("did not get owner entry", ownerEntry);
			Assert.assertNotNull("did not get crossZone entry", crossZoneEntry);

			Assert.assertEquals("did not set crossZone zone correctly",
					zone1Account.getZone(), crossZoneEntry.getUserZone());
			Assert.assertEquals("did not set owner zone correctly",
					irodsAccount.getZone(), ownerEntry.getUserZone());

		}

		// each entry has two permissions, will have extra for fed so tolerate
		// that
		for (CollectionAndDataObjectListingEntry actualEntry : entries) {
			Assert.assertTrue("did not get both expected permissions",
					actualEntry.getUserFilePermission().size() == 2);
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

	@Test
	public void testCountFilesAndCollectionsUnderPathInAnotherZone()
			throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String subdirPrefix = "testCountFilesAndCollectionsUnderPathInAnotherZone";
		String fileName = "testCountFilesAndCollectionsUnderPathInAnotherZone.txt";

		int count = 10;

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

		String myTarget = "";

		for (int i = 0; i < count; i++) {
			myTarget = targetIrodsCollection + "/c" + (10000 + i)
					+ subdirPrefix;
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(myTarget);
			irodsFile.mkdir();
			irodsFile.close();
		}

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
		int ctr = actual
				.countDataObjectsAndCollectionsUnderPath(targetIrodsCollection);
		Assert.assertEquals(count * 2, ctr);

	}

	/**
	 * put up a collection with some data in zone1, then list the collections
	 * and data objects from the perspective of a user on zone2. The results
	 * should be the data from zone1.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testListFilesAndCollectionsUnderPathWithAccessInfoInAnotherZone()
			throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String subdirPrefix = "testListFilesAndCollectionsUnderPathWithAccessInfoInAnotherZone";
		String fileName = "testListFilesAndCollectionsUnderPathWithAccessInfoInAnotherZone.csv";

		int count = 30;

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

		String myTarget = "";

		// add another acl for another user to this file
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

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
				.listDataObjectsAndCollectionsUnderPathWithPermissions(targetIrodsCollection);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());

		for (CollectionAndDataObjectListingEntry entry : entries) {
			System.out.println("bad entry?:" + entry);
			if (entry.isCollection()) {
				Assert.assertEquals(
						"did not have the three permissions for collection", 3,
						entry.getUserFilePermission().size());
			} else {
				Assert.assertEquals(

				"did not have the  permissions for data objects", 3, entry
						.getUserFilePermission().size());
			}
		}

	}

	/**
	 * Bug [#1842] [iROD-Chat:11109] imcoll symlinks across zones
	 * 
	 * @throws Exception
	 */
	@Test
	public void testListDataObjectsUnderPathWhenSoftLinkInAnotherZoneBug1842()
			throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String fileName = "testListDataObjectsUnderPathWhenSoftLinkInAnotherZoneBug1842.txt";
		String testSubdir = "testListDataObjectsUnderPathWhenSoftLinkInAnotherZoneBug1842";
		String mountSubdir = "testListDataObjectsUnderPathWhenSoftLinkInAnotherZoneBug1842SoftLink";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testSubdir);
		int length = 300;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, fileName, length);
		File localFile = new File(localFilePath);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);

		// delete to clean up
		destFile.deleteWithForceOption();
		destFile.mkdirs();

		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		IRODSAccount zone1Account = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		// make a symlink in zone1 to the coll in zone2

		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						zone1Account);

		String softLinkCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ mountSubdir);

		mountedCollectionAO.unmountACollection(softLinkCollection, "");

		mountedCollectionAO.createASoftLink(targetIrodsCollection,
				softLinkCollection);

		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(zone1Account);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.listDataObjectsUnderPath(softLinkCollection, 0);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		CollectionAndDataObjectListingEntry entry = entries
				.get(entries.size() - 1);
		Assert.assertTrue(entry.isLastResult());
		Assert.assertEquals(entry.getCount(), entries.size());
		Assert.assertEquals("did not find all of the entries", 1,
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
}
