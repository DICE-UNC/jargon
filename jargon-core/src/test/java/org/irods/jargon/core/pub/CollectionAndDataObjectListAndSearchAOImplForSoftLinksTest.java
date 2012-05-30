package org.irods.jargon.core.pub;

import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class CollectionAndDataObjectListAndSearchAOImplForSoftLinksTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	public static final String IRODS_TEST_SUBDIR_PATH = "CollectionAndDataObjectListAndSearchAOImplForSoftLinksTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();

	}

	/**
	 * Soft link a collection with one subdir and try to list it
	 * 
	 * @throws Exception
	 */
	@Test
	public void testListCollectionsUnderPathWhenIsALinkedColl()
			throws Exception {

		String sourceCollectionName = "testListCollectionsUnderPathWhenIsALinkedCollSource";
		String targetCollectionName = "testListCollectionsUnderPathWhenIsALinkedCollTarget";
		String subfileName = "testListCollectionsUnderPathWhenIsALinkedCollSubdir";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String sourceIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ sourceCollectionName);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		// set up source collection
		IRODSFile sourceFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						sourceIrodsCollection);
		sourceFile.mkdirs();
		IRODSFile subFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(sourceIrodsCollection, subfileName);
		subFile.mkdirs();

		// create the soft link
		mountedCollectionAO.createASoftLink(sourceIrodsCollection,
				targetIrodsCollection);

		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		List<CollectionAndDataObjectListingEntry> listed = listAndSearchAO
				.listCollectionsUnderPath(targetIrodsCollection, 0);

		Assert.assertFalse("list is empty", listed.isEmpty());

		CollectionAndDataObjectListingEntry entry = listed.get(0);
		Assert.assertEquals(targetIrodsCollection, entry.getParentPath());
		Assert.assertEquals(sourceIrodsCollection, entry.getSpecialObjectPath());

	}

	/**
	 * Soft link a collection with 10 data objects and list them
	 * 
	 * @throws Exception
	 */
	@Test
	public void testListDataObjectsUnderPathWhenIsALinkedColl()
			throws Exception {

		String sourceCollectionName = "testListDataObjectsUnderPathWhenIsALinkedCollSource";
		String targetCollectionName = "testListDataObjectsUnderPathWhenIsALinkedCollTarget";
		String subfileNameSuffix = ".txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String sourceIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ sourceCollectionName);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		// set up source collection
		IRODSFile sourceFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						sourceIrodsCollection);
		sourceFile.mkdirs();

		int count = 10;

		IRODSFile irodsFile = null;

		String myTarget = "";

		for (int i = 0; i < count; i++) {
			myTarget = sourceIrodsCollection + "/c" + (10000 + i)
					+ subfileNameSuffix;
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(myTarget);
			irodsFile.createNewFile();
			irodsFile.close();
		}

		// create the soft link
		mountedCollectionAO.createASoftLink(sourceIrodsCollection,
				targetIrodsCollection);

		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		List<CollectionAndDataObjectListingEntry> listed = listAndSearchAO
				.listDataObjectsUnderPath(targetIrodsCollection, 0);

		Assert.assertEquals("should list 10 data objects", count, listed.size());

		CollectionAndDataObjectListingEntry entry = listed.get(0);
		Assert.assertEquals(targetIrodsCollection, entry.getParentPath());
		Assert.assertEquals(sourceIrodsCollection, entry.getSpecialObjectPath());

	}

	/**
	 * Soft link a collection with 10 data objects and list them
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCountUnderPathWhenIsALinkedColl() throws Exception {

		String sourceCollectionName = "testCountUnderPathWhenIsALinkedCollSource";
		String targetCollectionName = "testCountUnderPathWhenIsALinkedCollTarget";
		String subfileNameSuffix = ".txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String sourceIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ sourceCollectionName);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		// set up source collection
		IRODSFile sourceFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						sourceIrodsCollection);
		sourceFile.mkdirs();

		// add a subdir
		sourceFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(sourceIrodsCollection + "/sub1");
		sourceFile.mkdirs();

		sourceFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(sourceIrodsCollection + "/sub2");
		sourceFile.mkdirs();

		int count = 10;

		IRODSFile irodsFile = null;

		String myTarget = "";

		for (int i = 0; i < count; i++) {
			myTarget = sourceIrodsCollection + "/c" + (10000 + i)
					+ subfileNameSuffix;
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(myTarget);
			irodsFile.createNewFile();
			irodsFile.close();
		}

		// create the soft link
		mountedCollectionAO.createASoftLink(sourceIrodsCollection,
				targetIrodsCollection);

		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		int countOfObjs = listAndSearchAO
				.countDataObjectsAndCollectionsUnderPath(targetIrodsCollection);
		Assert.assertEquals("wrong count of colls", 12, countOfObjs);

	}

	/**
	 * Soft link a collection with one subdir and try to list it
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetEntryForPathWhenIsALinkedColl() throws Exception {

		String sourceCollectionName = "testGetEntryForPathWhenIsALinkedCollSource";
		String targetCollectionName = "testGetEntryForPathWhenIsALinkedCollTarget";
		String subfileName = "testGetEntryForPathWhenIsALinkedCollSubdir";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String sourceIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ sourceCollectionName);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		// set up source collection
		IRODSFile sourceFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						sourceIrodsCollection);
		sourceFile.mkdirs();
		IRODSFile subFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(sourceIrodsCollection, subfileName);
		subFile.mkdirs();

		// create the soft link
		mountedCollectionAO.createASoftLink(sourceIrodsCollection,
				targetIrodsCollection);

		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		CollectionAndDataObjectListingEntry entry = listAndSearchAO
				.getCollectionAndDataObjectListingEntryAtGivenAbsolutePath(targetIrodsCollection
						+ "/" + subfileName);
		Assert.assertNotNull("null entry", entry);
		Assert.assertEquals(targetIrodsCollection, entry.getParentPath());
		Assert.assertEquals(sourceIrodsCollection, entry.getSpecialObjectPath());

	}

	/**
	 * Put some data objects under a collection, soft link it, and then get the
	 * listing from the perspective of the soft linked absolute path
	 * 
	 * @throws Exception
	 */
	@Test
	public void testListDataObjectsUnderPathWithAccessInfoWhenSoftLink()
			throws Exception {

		String sourceCollectionName = "testListDataObjectsUnderPathWithAccessInfoWhenSoftLinkSource";
		String targetCollectionName = "testListDataObjectsUnderPathWithAccessInfoWhenSoftLinkTarget";
		String subfileName = "testListDataObjectsUnderPathWithAccessInfoWhenSoftLink.txt";

		int count = 20;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile irodsFile = null;

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		String sourceIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ sourceCollectionName);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ targetCollectionName);

		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(sourceIrodsCollection);
		irodsFile.mkdir();
		irodsFile.close();

		String myTarget = "";

		for (int i = 0; i < count; i++) {
			myTarget = sourceIrodsCollection + "/c" + (10000 + i) + subfileName;
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

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		// create the soft link
		mountedCollectionAO.createASoftLink(sourceIrodsCollection,
				targetIrodsCollection);

		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
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
			Assert.assertEquals(targetIrodsCollection, entry.getParentPath());
			Assert.assertEquals(sourceIrodsCollection,
					entry.getSpecialObjectPath());

		}

	}

	/**
	 * Soft link a collection with one subdir and try to list it
	 * 
	 * @throws Exception
	 */
	@Test
	public void testListCollectionsUnderPathWithPermissionsWhenIsALinkedColl()
			throws Exception {

		String sourceCollectionName = "testListCollectionsUnderPathWhenIsALinkedCollSource";
		String targetCollectionName = "testListCollectionsUnderPathWhenIsALinkedCollTarget";
		String subfileName = "testListCollectionsUnderPathWhenIsALinkedCollSubdir";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String sourceIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ sourceCollectionName);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);

		// set up source collection
		IRODSFile sourceFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						sourceIrodsCollection);
		sourceFile.mkdirs();
		IRODSFile subFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(sourceIrodsCollection, subfileName);
		subFile.mkdirs();
		collectionAO.setAccessPermissionWrite(irodsAccount.getZone(), subFile
				.getAbsolutePath(), testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
				false);

		// create the soft link
		mountedCollectionAO.createASoftLink(sourceIrodsCollection,
				targetIrodsCollection);

		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		List<CollectionAndDataObjectListingEntry> listed = listAndSearchAO
				.listCollectionsUnderPathWithPermissions(targetIrodsCollection,
						0);

		Assert.assertFalse("list is empty", listed.isEmpty());

		CollectionAndDataObjectListingEntry entry = listed.get(0);
		Assert.assertEquals(targetIrodsCollection, entry.getParentPath());
		Assert.assertEquals(sourceIrodsCollection, entry.getSpecialObjectPath());
		Assert.assertEquals("did not get both expected permissions", 2, entry
				.getUserFilePermission().size());

	}

}
