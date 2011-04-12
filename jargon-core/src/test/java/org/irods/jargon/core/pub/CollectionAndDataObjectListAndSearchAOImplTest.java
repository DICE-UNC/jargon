package org.irods.jargon.core.pub;

import java.io.File;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class CollectionAndDataObjectListAndSearchAOImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "CollectionAndDataObjectListAndSearchAOImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	@SuppressWarnings("unused")
	private static org.irods.jargon.testutils.AssertionHelper assertionHelper = null;

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
		assertionHelper = new org.irods.jargon.testutils.AssertionHelper();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testGetInstanceFromFactory() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		Assert.assertNotNull(actual);
		irodsFileSystem.close();

	}

	@Test
	public void testGetInstanceFromFactoryCloseGivingAccount() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		irodsFileSystem.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		irodsFileSystem.close(irodsAccount);
		// no errors means test passes
		Assert.assertTrue(true);

	}

	@Test
	public void testListCollectionsUnderPath() throws Exception {

		String subdirPrefix = "testListCollectionsUnderPath";
		int count = 1500;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
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

		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.listCollectionsUnderPath(targetIrodsCollection, 0);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		CollectionAndDataObjectListingEntry entry = entries
				.get(entries.size() - 1);
		Assert.assertEquals(entry.getCount(), entries.size());

		// now get the next page
		entries = actual.listCollectionsUnderPath(targetIrodsCollection, 1000);
		entry = entries.get(entries.size() - 1);
		Assert.assertEquals(
				CollectionAndDataObjectListingEntry.ObjectType.COLLECTION,
				entry.getObjectType());
		Assert.assertEquals("i am not the owner", irodsAccount.getUserName(),
				entry.getOwnerName());

		irodsFileSystem.close();

		Assert.assertTrue(entry.isLastResult());
		// TestCase.assertTrue(entry.isLastResult());
		Assert.assertEquals(entry.getCount(), entries.size());
		Assert.assertEquals(500, entries.size());

	}
	
	@Test
	public void testListCollectionsUnderPathDiffUser() throws Exception {

		String subdirPrefix = "testListCollectionsUnderPathDiffUser";
		int count = 1500;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
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

		IRODSAccount secondaryAccount = testingPropertiesHelper.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(secondaryAccount);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.listCollectionsUnderPath(targetIrodsCollection, 0);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		CollectionAndDataObjectListingEntry entry = entries
				.get(entries.size() - 1);
		Assert.assertEquals(entry.getCount(), entries.size());

		// now get the next page
		entries = actual.listCollectionsUnderPath(targetIrodsCollection, 1000);
		entry = entries.get(entries.size() - 1);
		Assert.assertEquals(
				CollectionAndDataObjectListingEntry.ObjectType.COLLECTION,
				entry.getObjectType());
		
		irodsFileSystem.close();

		Assert.assertTrue(entry.isLastResult());
		// TestCase.assertTrue(entry.isLastResult());
		Assert.assertEquals(entry.getCount(), entries.size());
		Assert.assertEquals(500, entries.size());

	}

	@Test
	public void testListCollectionsUnderPathWithPermissions() throws Exception {

		String subdirPrefix = "testListCollectionsUnderPathWithPermissions";
		int count = 20;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
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
			collectionAO.setAccessPermissionWrite(irodsAccount.getZone(), irodsFile
					.getAbsolutePath(), testingProperties
					.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
					false);
		}

		CollectionAndDataObjectListAndSearchAOImpl actual = (CollectionAndDataObjectListAndSearchAOImpl) irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.listCollectionsUnderPathWithPermissions(targetIrodsCollection,
						0);
		irodsFileSystem.close();

		Assert.assertNotNull(entries);
		Assert.assertFalse("entries was empty", entries.isEmpty());
		CollectionAndDataObjectListingEntry entry = entries
				.get(entries.size() - 1);
		Assert.assertEquals("i am not the owner", irodsAccount.getUserName(),
				entry.getOwnerName());
		
		// each entry has two permissions
		for (CollectionAndDataObjectListingEntry actualEntry : entries) {
			TestCase.assertEquals("did not get both expected permissions", 2, actualEntry.getUserFilePermission().size());
		}
		
	}

	@Test
	public void testListCollectionsUnderPathWhenPathIsRootDir()
			throws Exception {

		String subdirPrefix = "/";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		String targetIrodsCollection = subdirPrefix;
		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.listCollectionsUnderPath(targetIrodsCollection, 0);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());

		// bounce thru the results and make sure a root entry is not returned as
		// a child of the root entry

		for (CollectionAndDataObjectListingEntry entry : entries) {
			Assert.assertFalse("found root path as child of root path", entry
					.getPathOrName().equals("/"));
		}

	}

	@Test
	public void testListDataObjectsUnderPath() throws Exception {

		String fileName = "testListDataObjectsUnderPath.txt";
		String testSubdir = "testListDataObjectsUnderPath";
		int count = 200;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFile irodsFile = null;

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
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

		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		irodsFileSystem.close();
		List<CollectionAndDataObjectListingEntry> entries = actual
				.listDataObjectsUnderPath(targetIrodsCollection, 0);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		CollectionAndDataObjectListingEntry entry = entries
				.get(entries.size() - 1);
		Assert.assertTrue(entry.isLastResult());
		Assert.assertEquals(entry.getCount(), entries.size());
		Assert.assertEquals(200, entries.size());

		// bounce thru and make sure each is a data object with the correct name

		for (CollectionAndDataObjectListingEntry resultEntry : entries) {
			Assert.assertTrue(
					"this is not a data object",
					resultEntry.getObjectType() == CollectionAndDataObjectListingEntry.ObjectType.DATA_OBJECT);
			Assert.assertTrue("file name not correctly returned", resultEntry
					.getPathOrName().indexOf(fileName) > -1);
		}

	}
	
	@Test
	public void testListDataObjectsUnderPathWithAccessInfo() throws Exception {

		String fileName = "testListDataObjectsUnderPathWithAccessInfo.txt";
		String testSubdir = "testListDataObjectsUnderPathWithAccessInfo";
		int count = 200;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFile irodsFile = null;
		
		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
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
			dataObjectAO.setAccessPermissionWrite(irodsAccount.getZone(), irodsFile.getAbsolutePath(), testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));	
		}

		CollectionAndDataObjectListAndSearchAO actual =  irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		irodsFileSystem.close();
		List<CollectionAndDataObjectListingEntry> entries = actual.listDataObjectsUnderPathWithPermissions(targetIrodsCollection, 0);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		CollectionAndDataObjectListingEntry entry = entries
				.get(entries.size() - 1);
		Assert.assertTrue(entry.isLastResult());
		Assert.assertEquals(200, entries.size());

		// bounce thru and make sure each is a data object with the correct name

		for (CollectionAndDataObjectListingEntry resultEntry : entries) {
			Assert.assertTrue(
					"this is not a data object",
					resultEntry.getObjectType() == CollectionAndDataObjectListingEntry.ObjectType.DATA_OBJECT);
			Assert.assertTrue("file name not correctly returned", resultEntry
					.getPathOrName().indexOf(fileName) > -1);
		}

	}
	
	@Test
	public void testListDataObjectsUnderPathWithAccessInfoAsDiffUser() throws Exception {

		String fileName = "testListDataObjectsUnderPathWithAccessInfoAsDiffUser.xls";
		String testSubdir = "testListDataObjectsUnderPathWithAccessInfoAsDiffUser";
		int count = 20;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFile irodsFile = null;
		
		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
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
			dataObjectAO.setAccessPermissionWrite(irodsAccount.getZone(), irodsFile.getAbsolutePath(), testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));	
		}

		IRODSAccount secondaryAccount = testingPropertiesHelper.buildIRODSAccountFromTertiaryTestProperties(testingProperties);
		CollectionAndDataObjectListAndSearchAO actual =  irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(secondaryAccount);
		List<CollectionAndDataObjectListingEntry> entries = actual.listDataObjectsUnderPathWithPermissions(targetIrodsCollection, 0);
		irodsFileSystem.close();
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

	}
	
	@Test
	public void testListDataObjectsUnderPathWithAccessInfoWithAReplica() throws Exception {

		String fileName = "testListDataObjectsUnderPathWithAccessInfoWithAReplica.txt";
		String testSubdir = "testListDataObjectsUnderPathWithAccessInfoWithAReplica";
		int count = 200;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFile irodsFile = null;
		
		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
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
			dataObjectAO.setAccessPermissionWrite(irodsAccount.getZone(), irodsFile.getAbsolutePath(), testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));	
		}
		
		// replicate that collection to make sure that we don't get 'double data' in the results.
		
		DataTransferOperations transferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataTransferOperations(irodsAccount);
		transferOperationsAO.replicate(targetIrodsCollection, testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY), null, null);

		CollectionAndDataObjectListAndSearchAO actual =  irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		irodsFileSystem.close();
		List<CollectionAndDataObjectListingEntry> entries = actual.listDataObjectsUnderPathWithPermissions(targetIrodsCollection, 0);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		CollectionAndDataObjectListingEntry entry = entries
				.get(entries.size() - 1);
		Assert.assertTrue(entry.isLastResult());
		Assert.assertEquals(200, entries.size());
		Assert.assertEquals("record should be the 800th", 800, entry.getCount());

		// bounce thru and make sure each is a data object with the correct name

		for (CollectionAndDataObjectListingEntry resultEntry : entries) {
			Assert.assertTrue(
					"this is not a data object",
					resultEntry.getObjectType() == CollectionAndDataObjectListingEntry.ObjectType.DATA_OBJECT);
			Assert.assertEquals("owner name not set", testingProperties.getProperty(TestingPropertiesHelper.IRODS_USER_KEY), resultEntry.getOwnerName());
			Assert.assertEquals("length should be zero", 0, resultEntry.getDataSize());
			Assert.assertEquals("should be two permissions for file", 2, resultEntry.getUserFilePermission().size());
		}

	}

	@Test
	public void testListFilesAndCollectionsUnderPath() throws Exception {

		String subdirPrefix = "testListFilesAndCollectionsUnderPath";
		String fileName = "testListCollectionsUnderPath.txt";

		int count = 30;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
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

		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.listDataObjectsAndCollectionsUnderPath(targetIrodsCollection);
		irodsFileSystem.close();
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		Assert.assertEquals(count * 2, entries.size());
	}
	
	@Test
	public void testListFilesAndCollectionsUnderPathWithAccessInfo() throws Exception {

		String subdirPrefix = "testListFilesAndCollectionsUnderPathWithAccessInfo";
		String fileName = "testListFilesAndCollectionsUnderPathWithAccessInfo.csv";

		int count = 30;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
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
		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		for (int i = 0; i < count; i++) {
			myTarget = targetIrodsCollection + "/c" + (10000 + i)
					+ subdirPrefix;
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(myTarget);
			irodsFile.mkdir();
			irodsFile.close();
			collectionAO.setAccessPermissionWrite(irodsAccount.getZone(), irodsFile
					.getAbsolutePath(), testingProperties
					.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
					false);
		}

		for (int i = 0; i < count; i++) {
			myTarget = targetIrodsCollection + "/c" + (10000 + i) + fileName;
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(myTarget);
			irodsFile.createNewFile();
			irodsFile.close();
			dataObjectAO.setAccessPermissionWrite(irodsAccount.getZone(), irodsFile
					.getAbsolutePath(), testingProperties
					.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));
		}

		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.listDataObjectsAndCollectionsUnderPathWithPermissions(targetIrodsCollection);
		irodsFileSystem.close();
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		Assert.assertEquals(count * 2, entries.size());
		
		// bounce thru entries, each has two permissions
		
		for (CollectionAndDataObjectListingEntry entry : entries) {
			TestCase.assertEquals("did not have the two permissions", 2, entry.getUserFilePermission().size());
		}
		
	}
	
	@Test
	public void testListFilesAndCollectionsUnderPathWithAccessInfoForTertiaryUser() throws Exception {

		String subdirPrefix = "testListFilesAndCollectionsUnderPathWithAccessInfoForTertiaryUser";
		String fileName = "testListFilesAndCollectionsUnderPathWithAccessInfoForTertiaryUser.doc";

		int count = 30;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
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
		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		for (int i = 0; i < count; i++) {
			myTarget = targetIrodsCollection + "/c" + (10000 + i)
					+ subdirPrefix;
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(myTarget);
			irodsFile.mkdir();
			irodsFile.close();
			collectionAO.setAccessPermissionWrite(irodsAccount.getZone(), irodsFile
					.getAbsolutePath(), testingProperties
					.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
					false);
		}

		for (int i = 0; i < count; i++) {
			myTarget = targetIrodsCollection + "/c" + (10000 + i) + fileName;
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(myTarget);
			irodsFile.createNewFile();
			irodsFile.close();
			dataObjectAO.setAccessPermissionWrite(irodsAccount.getZone(), irodsFile
					.getAbsolutePath(), testingProperties
					.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));
		}

		IRODSAccount tertiaryAccount = testingPropertiesHelper.buildIRODSAccountFromTertiaryTestProperties(testingProperties);
		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(tertiaryAccount);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.listDataObjectsAndCollectionsUnderPathWithPermissions(targetIrodsCollection);
		irodsFileSystem.close();
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		Assert.assertEquals(count * 2, entries.size());
		
		// bounce thru entries, each has two permissions
		
		for (CollectionAndDataObjectListingEntry entry : entries) {
			TestCase.assertEquals("did not have the two permissions", 2, entry.getUserFilePermission().size());
		}
		
	}

	@Test
	public void testCountFilesAndCollectionsUnderPath() throws Exception {

		String subdirPrefix = "testCountFilesAndCollectionsUnderPath";
		String fileName = "testListCollectionsUnderPath.txt";

		int count = 30;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
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

		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		int ctr = actual
				.countDataObjectsAndCollectionsUnderPath(targetIrodsCollection);
		irodsFileSystem.close();
		Assert.assertEquals(count * 2, ctr);

	}

	@Test
	public void testSearchCollections() throws Exception {

		String subdirPrefix = "testSearchCollections";
		String commonTerm = "commonTermForSearch";
		String secondSubdir = "secondSubdir";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ subdirPrefix);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdir();

		// make a subdir with the search term

		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + commonTerm);
		irodsFile.mkdir();

		// make a second subdir, put the same search term under the subdir
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + secondSubdir);
		irodsFile.mkdir();

		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(
						irodsFile.getAbsolutePath() + "/" + commonTerm);
		irodsFile.mkdir();

		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(
						irodsFile.getAbsolutePath() + "/" + "joeBOB"
								+ commonTerm);
		irodsFile.mkdir();

		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.searchCollectionsBasedOnName(commonTerm);

		Assert.assertNotNull(entries);
		Assert.assertEquals("did not find the two subdirs I added", 3,
				entries.size());
		irodsFileSystem.close();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSearchCollectionsNullTerm() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		actual.searchCollectionsBasedOnName(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSearchCollectionsBlankTerm() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		actual.searchCollectionsBasedOnName("");
	}

	@Test
	public void testSearchDataObjects() throws Exception {

		String subdirPrefix = "testSearchDataObjects";
		String searchTerm = "testSearchDataObjectsIBetYouWontReplicateThisNameAnywhere";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ subdirPrefix);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdir();

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, searchTerm
						+ "testv1.txt", 1);

		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdir();

		File localFile = new File(localFileName);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		dataObjectAO.putLocalDataObjectToIRODS(localFile, irodsFile, true);

		// second file, slightly different prefix on name
		localFileName = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, "someSortOfPrefix" + searchTerm + "testv1.txt", 1);
		localFile = new File(localFileName);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, irodsFile, true);

		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		irodsFileSystem.close();
		List<CollectionAndDataObjectListingEntry> entries = actual
				.searchDataObjectsBasedOnName(searchTerm, 0);
		Assert.assertNotNull(entries);
		Assert.assertEquals(2, entries.size());

		irodsFileSystem.close();
	}

	@Test
	public void testSearchCollectionsAndDataObjects() throws Exception {

		String subdirPrefix = "testSearchCollectionsAndDataObjects";
		String searchTerm = "testSearchCollectionsAndDataObjectsAndThisIsAPrettyUniqueSearchNameToo";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ subdirPrefix);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdir();

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, searchTerm
						+ "testv1.txt", 1);

		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdir();

		File localFile = new File(localFileName);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		dataObjectAO.putLocalDataObjectToIRODS(localFile, irodsFile, true);

		// second file, slightly different prefix on name
		localFileName = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, "someSortOfPrefix" + searchTerm + "testv1.txt", 1);
		localFile = new File(localFileName);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, irodsFile, true);

		// make a subdir with the search term
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + searchTerm);
		irodsFile.mkdir();

		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(
						irodsFile.getAbsolutePath() + "/" + searchTerm
								+ "somethingElseToo");
		irodsFile.mkdir();

		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.searchCollectionsAndDataObjectsBasedOnName(searchTerm);
		Assert.assertNotNull(entries);
		Assert.assertEquals(4, entries.size());

		irodsFileSystem.close();
	}

	@Test
	public void testGetFullObjectForTypeDataObject() throws Exception {

		String testFileName = "testGetFullObjectForTypeDataObject.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, true);
		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		Object actual = listAndSearchAO
				.getFullObjectForType(targetIrodsCollection + "/"
						+ testFileName);
		irodsFileSystem.close();
		Assert.assertNotNull("object was null", actual);
		boolean isDataObject = actual instanceof DataObject;
		Assert.assertTrue("was not a data object", isDataObject);

	}

	@Test
	public void testGetFullObjectForCollection() throws Exception {

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		Object actual = listAndSearchAO
				.getFullObjectForType(targetIrodsCollection);
		irodsFileSystem.close();
		Assert.assertNotNull("object was null", actual);
		boolean isCollection = actual instanceof Collection;
		Assert.assertTrue("was not a collection", isCollection);

	}

	@Test(expected = DataNotFoundException.class)
	public void testGetFullObjectForNonExistant() throws Exception {

		String testFileName = "testGetFullObjectForNonExistant.txt";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		listAndSearchAO.getFullObjectForType(targetIrodsCollection + "/"
				+ testFileName);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetFullObjectNullPath() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		listAndSearchAO.getFullObjectForType(null);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetFullObjectBlankPath() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		listAndSearchAO.getFullObjectForType("");

	}

}
