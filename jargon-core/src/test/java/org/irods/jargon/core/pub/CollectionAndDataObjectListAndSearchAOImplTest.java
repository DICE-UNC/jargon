package org.irods.jargon.core.pub;

import java.io.File;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.domain.ObjStat.SpecColType;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.query.PagingAwareCollectionListing;
import org.irods.jargon.core.utils.LocalFileUtils;
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
	private static IRODSFileSystem irodsFileSystem = null;

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
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();

	}

	@Test
	public void testGetInstanceFromFactory() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		Assert.assertNotNull(actual);

	}

	@Test
	public void testGetInstanceFromFactoryCloseGivingAccount() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		irodsFileSystem.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		// no errors means test passes
		Assert.assertTrue(true);

	}

	@Test
	public void testListCollectionsUnderPathWithPermissionsSmallWithSpecificQuery()
			throws Exception {

		String subdirPrefix = "testListCollectionsUnderPathWithPermissionsSmallWithSpecificQuery";
		int count = 100;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		SettableJargonProperties props = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		props.setUsingSpecificQueryForCollectionListingWithPermissions(true);
		irodsFileSystem.getIrodsSession().setJargonProperties(props);

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
				.listCollectionsUnderPathWithPermissions(targetIrodsCollection,
						0);
		Assert.assertNotNull("null result from query", entries);
		Assert.assertFalse("should not have been and empty result list",
				entries.isEmpty());
		CollectionAndDataObjectListingEntry entry = entries
				.get(entries.size() - 1);
		Assert.assertEquals(entry.getCount(), entries.size());

		Assert.assertTrue("should be last result", entry.isLastResult());
		Assert.assertEquals(
				"last record count should equal number of expected total records",
				count, entry.getCount());
	}

	@Test
	public void testListCollectionsUnderPathWithPermissionsSmallWithoutSpecificQuery()
			throws Exception {

		String subdirPrefix = "testListCollectionsUnderPathWithPermissionsSmallWithoutSpecificQuery";
		int count = 100;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		SettableJargonProperties props = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		props.setUsingSpecificQueryForCollectionListingWithPermissions(false);
		irodsFileSystem.getIrodsSession().setJargonProperties(props);

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
				.listCollectionsUnderPathWithPermissions(targetIrodsCollection,
						0);
		Assert.assertNotNull("null result from query", entries);
		Assert.assertFalse("should not have been and empty result list",
				entries.isEmpty());
		CollectionAndDataObjectListingEntry entry = entries
				.get(entries.size() - 1);
		Assert.assertEquals(entry.getCount(), entries.size());
		Assert.assertTrue("should be last result", entry.isLastResult());
		Assert.assertEquals(
				"last record count should equal number of expected total records",
				count, entry.getCount());

	}

	@Test
	public void testListCollectionsUnderPath() throws Exception {

		String subdirPrefix = "testListCollectionsUnderPath";
		int count = 1500;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

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
		Assert.assertNotNull("null result from query", entries);
		Assert.assertFalse("should not have been and empty result list",
				entries.isEmpty());
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

		Assert.assertTrue("should be last result", entry.isLastResult());
		Assert.assertEquals(
				"last record count should equal number of expected total records",
				count, entry.getCount());
		Assert.assertEquals(500, entries.size());

	}

	@Test
	public void testListCollectionsUnderPathGivingPagingAwareCollectionListing()
			throws Exception {

		String subdirPrefix = "testListCollectionsUnderPathGivingPagingAwareCollectionListing";
		int count = 50;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

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
		PagingAwareCollectionListing pagingAwareCollectionListing = actual
				.listDataObjectsAndCollectionsUnderPathProducingPagingAwareCollectionListing(targetIrodsCollection);
		Assert.assertEquals(count,
				pagingAwareCollectionListing.getCollectionsCount());
		Assert.assertEquals(count,
				pagingAwareCollectionListing.getCollectionsTotalRecords());
		Assert.assertEquals(0,
				pagingAwareCollectionListing.getCollectionsOffset());
		Assert.assertTrue(pagingAwareCollectionListing.isCollectionsComplete());
		Assert.assertEquals(irodsFileSystem.getJargonProperties()
				.getMaxFilesAndDirsQueryMax(), pagingAwareCollectionListing
				.getPageSizeUtilized());
		Assert.assertEquals(0,
				pagingAwareCollectionListing.getDataObjectsCount());
		Assert.assertEquals(0,
				pagingAwareCollectionListing.getDataObjectsTotalRecords());
		Assert.assertEquals(0,
				pagingAwareCollectionListing.getDataObjectsOffset());
		Assert.assertTrue(pagingAwareCollectionListing.isDataObjectsComplete());
		Assert.assertEquals(count, pagingAwareCollectionListing
				.getCollectionAndDataObjectListingEntries().size());
	}

	@Test
	public void testListCollectionsUnderPathDiffUser() throws Exception {

		String subdirPrefix = "testListCollectionsUnderPathDiffUser";
		int count = 1500;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

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

		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(secondaryAccount);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.listCollectionsUnderPath(targetIrodsCollection, 0);
		Assert.assertNotNull("null result returned", entries);
		Assert.assertFalse("entries should not be empty", entries.isEmpty());
		CollectionAndDataObjectListingEntry entry = entries
				.get(entries.size() - 1);
		Assert.assertEquals(entry.getCount(), entries.size());

		// now get the next page
		entries = actual.listCollectionsUnderPath(targetIrodsCollection, 1000);
		entry = entries.get(entries.size() - 1);
		Assert.assertEquals(
				CollectionAndDataObjectListingEntry.ObjectType.COLLECTION,
				entry.getObjectType());

	}

	@Test
	public void testListCollectionsUnderPathWithPermissions() throws Exception {

		String subdirPrefix = "testListCollectionsUnderPathWithPermissions";
		int count = 20;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

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
			collectionAO
					.setAccessPermissionWrite(
							irodsAccount.getZone(),
							irodsFile.getAbsolutePath(),
							testingProperties
									.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
							false);
		}

		CollectionAndDataObjectListAndSearchAOImpl actual = (CollectionAndDataObjectListAndSearchAOImpl) irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.listCollectionsUnderPathWithPermissions(targetIrodsCollection,
						0);

		Assert.assertNotNull(entries);
		Assert.assertFalse("entries was empty", entries.isEmpty());
		CollectionAndDataObjectListingEntry entry = entries
				.get(entries.size() - 1);
		Assert.assertEquals("i am not the owner", irodsAccount.getUserName(),
				entry.getOwnerName());

		// each entry has two permissions
		for (CollectionAndDataObjectListingEntry actualEntry : entries) {
			Assert.assertEquals("did not get both expected permissions", 2,
					actualEntry.getUserFilePermission().size());
		}

	}

	@Test
	public void testListCollectionsUnderPathWhenPathIsRootDir()
			throws Exception {

		String subdirPrefix = "/";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = subdirPrefix;
		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.listCollectionsUnderPath(targetIrodsCollection, 0);
		Assert.assertNotNull("null entries list returned", entries);
		Assert.assertFalse("result entries should not be empty",
				entries.isEmpty());

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
	public void testListDataObjectsUnderPathGivingPagingAwareCollectionListing()
			throws Exception {

		String fileName = "testListDataObjectsUnderPathGivingPagingAwareCollectionListing.txt";
		String testSubdir = "testListDataObjectsUnderPathGivingPagingAwareCollectionListing";
		int count = 200;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
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

		PagingAwareCollectionListing pagingAwareCollectionListing = actual
				.listDataObjectsAndCollectionsUnderPathProducingPagingAwareCollectionListing(targetIrodsCollection);
		Assert.assertEquals(0,
				pagingAwareCollectionListing.getCollectionsCount());
		Assert.assertEquals(0,
				pagingAwareCollectionListing.getCollectionsTotalRecords());
		Assert.assertEquals(0,
				pagingAwareCollectionListing.getCollectionsOffset());
		Assert.assertTrue(pagingAwareCollectionListing.isCollectionsComplete());
		Assert.assertEquals(irodsFileSystem.getJargonProperties()
				.getMaxFilesAndDirsQueryMax(), pagingAwareCollectionListing
				.getPageSizeUtilized());
		Assert.assertEquals(count,
				pagingAwareCollectionListing.getDataObjectsCount());
		Assert.assertEquals(count,
				pagingAwareCollectionListing.getDataObjectsTotalRecords());
		Assert.assertEquals(0,
				pagingAwareCollectionListing.getDataObjectsOffset());
		Assert.assertTrue(pagingAwareCollectionListing.isDataObjectsComplete());
		Assert.assertEquals(count, pagingAwareCollectionListing
				.getCollectionAndDataObjectListingEntries().size());

	}

	/**
	 * Bug [#1211] Re: [iROD-Chat:9536] jargon mangling UTF-8 characters
	 * 
	 * @throws Exception
	 */
	@Test
	public void testListDataObjectsUnderPathBug1211() throws Exception {

		String testSubdir = "testListDataObjectsUnderPathBug1211";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile irodsFile = null;

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testSubdir);
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdir();

		// make funny file name by hex string
		byte[] funnyFileNameBytes = LocalFileUtils
				.hexStringToByteArray("c39937c38f39415156c2b2c39612c397c2847cc3915e33c39e");
		String utf8DecodedFunnyFileName = new String(funnyFileNameBytes,
				"UTF-8");
		Assert.assertNotNull(utf8DecodedFunnyFileName);

		// [-61, -103, 55, -61, -113, 57, 65, 81, 86, -62, -78, -61, -106, 92,
		// 120, 49, 50, -61, -105, 92, 117, 48, 48, 56, 52, 124, -61, -111, 94,
		// 51, -61, -98]

		IRODSFile funnyFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection,
						utf8DecodedFunnyFileName);
		funnyFile.createNewFile();

		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.listDataObjectsUnderPath(targetIrodsCollection, 0);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		CollectionAndDataObjectListingEntry entry = entries
				.get(entries.size() - 1);
		Assert.assertEquals("did not properly decode file name",
				utf8DecodedFunnyFileName, entry.getPathOrName());

	}

	/**
	 * Bug [#1211] Re: [iROD-Chat:9536] jargon mangling UTF-8 characters
	 * 
	 * @throws Exception
	 */
	@Test
	public void testListDataObjectsUnderPathBug1211UseEncodedUTF8()
			throws Exception {

		String fileName = "\u00d9\u0037\u00cf\u0039\u0041\u0051\u0056\u00b2\u00d6\u0012\u00d7\u0084\u007c\u00d1\u005e\u0033\u00de";
		String testSubdir = "testListDataObjectsUnderPathBug1211UseEncodedUTF8";

		byte[] bytes = fileName.getBytes();
		Assert.assertNotNull(bytes);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile irodsFile = null;

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testSubdir);
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdir();

		// make funny file name by hex string

		IRODSFile funnyFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection, fileName);
		funnyFile.createNewFile();

		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.listDataObjectsUnderPath(targetIrodsCollection, 0);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		CollectionAndDataObjectListingEntry entry = entries
				.get(entries.size() - 1);
		Assert.assertEquals("did not properly decode file name", fileName,
				entry.getPathOrName());

	}

	@Test
	public void testListDataObjectsUnderPathWithAccessInfo() throws Exception {

		String fileName = "testListDataObjectsUnderPathWithAccessInfo.txt";
		String testSubdir = "testListDataObjectsUnderPathWithAccessInfo";
		int count = 200;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile irodsFile = null;

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

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
			dataObjectAO
					.setAccessPermissionWrite(
							irodsAccount.getZone(),
							irodsFile.getAbsolutePath(),
							testingProperties
									.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));
		}

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
	public void testListDataObjectsUnderPathWithAccessInfoAsDiffUser()
			throws Exception {

		String fileName = "testListDataObjectsUnderPathWithAccessInfoAsDiffUser.xls";
		String testSubdir = "testListDataObjectsUnderPathWithAccessInfoAsDiffUser";
		int count = 20;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile irodsFile = null;

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

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
			dataObjectAO
					.setAccessPermissionWrite(
							irodsAccount.getZone(),
							irodsFile.getAbsolutePath(),
							testingProperties
									.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));
		}

		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromTertiaryTestProperties(testingProperties);
		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(secondaryAccount);
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

	}

	@Test
	public void testListDataObjectsUnderPathWithAccessInfoWithAReplica()
			throws Exception {

		String fileName = "testListDataObjectsUnderPathWithAccessInfoWithAReplica.txt";
		String testSubdir = "testListDataObjectsUnderPathWithAccessInfoWithAReplica";
		int count = 200;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile irodsFile = null;

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

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
			dataObjectAO
					.setAccessPermissionWrite(
							irodsAccount.getZone(),
							irodsFile.getAbsolutePath(),
							testingProperties
									.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));
		}

		// replicate that collection to make sure that we don't get 'double
		// data' in the results.

		DataTransferOperations transferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		transferOperationsAO
				.replicate(
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY),
						null, null);

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
		Assert.assertEquals(200, entries.size());
		Assert.assertEquals("record should be the 800th", 800, entry.getCount());

		// bounce thru and make sure each is a data object with the correct name

		for (CollectionAndDataObjectListingEntry resultEntry : entries) {
			Assert.assertTrue(
					"this is not a data object",
					resultEntry.getObjectType() == CollectionAndDataObjectListingEntry.ObjectType.DATA_OBJECT);
			Assert.assertEquals("owner name not set", testingProperties
					.getProperty(TestingPropertiesHelper.IRODS_USER_KEY),
					resultEntry.getOwnerName());
			Assert.assertEquals("length should be zero", 0,
					resultEntry.getDataSize());
			Assert.assertEquals("should be two permissions for file", 2,
					resultEntry.getUserFilePermission().size());
		}

	}

	@Test
	public void testListFilesAndCollectionsUnderPath() throws Exception {

		String subdirPrefix = "testListFilesAndCollectionsUnderPath";
		String fileName = "testListCollectionsUnderPath.txt";

		int count = 30;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

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
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		Assert.assertEquals(count * 2, entries.size());
	}

	@Test
	public void testListFilesAndCollectionsUnderPathWithAccessInfo()
			throws Exception {

		String subdirPrefix = "testListFilesAndCollectionsUnderPathWithAccessInfo";
		String fileName = "testListFilesAndCollectionsUnderPathWithAccessInfo.csv";

		int count = 30;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

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

		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.listDataObjectsAndCollectionsUnderPathWithPermissions(targetIrodsCollection);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		Assert.assertEquals(count * 2, entries.size());

		// bounce thru entries, each has two permissions

		for (CollectionAndDataObjectListingEntry entry : entries) {
			Assert.assertEquals("did not have the two permissions", 2, entry
					.getUserFilePermission().size());
		}

	}

	@Test
	public void testListFilesAndCollectionsUnderPathWithAccessInfoForTertiaryUser()
			throws Exception {

		String subdirPrefix = "testListFilesAndCollectionsUnderPathWithAccessInfoForTertiaryUser";
		String fileName = "testListFilesAndCollectionsUnderPathWithAccessInfoForTertiaryUser.doc";

		int count = 30;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

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

		IRODSAccount tertiaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromTertiaryTestProperties(testingProperties);
		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(tertiaryAccount);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.listDataObjectsAndCollectionsUnderPathWithPermissions(targetIrodsCollection);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		Assert.assertEquals(count * 2, entries.size());

		// bounce thru entries, each has two permissions

		for (CollectionAndDataObjectListingEntry entry : entries) {
			Assert.assertEquals("did not have the two permissions", 2, entry
					.getUserFilePermission().size());
		}

	}

	@Test
	public void testCountFilesAndCollectionsUnderPath() throws Exception {

		String subdirPrefix = "testCountFilesAndCollectionsUnderPath";
		String fileName = "testListCollectionsUnderPath.txt";

		int count = 30;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

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
		Assert.assertEquals(count * 2, ctr);

	}

	@Test
	public void testSearchCollections() throws Exception {

		String subdirPrefix = "testSearchCollections";
		String commonTerm = "commonTermForSearch";
		String secondSubdir = "secondSubdir";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ subdirPrefix);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.deleteWithForceOption();
		irodsFile.reset();
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
		Assert.assertTrue("did not find the two subdirs I added",
				entries.size() >= 3);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSearchCollectionsNullTerm() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

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

		String subdirPrefix = "testSearchDataObjectsSubdir";
		String searchTerm = "testSearchDataObjectsIBetYouWontReplicateThisNameAnywhereTerm";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

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
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		dataObjectAO.putLocalDataObjectToIRODS(localFile, irodsFile, true);

		// second file, slightly different prefix on name
		localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath,
						"testSearchDataObjects" + searchTerm + "testv1.txt", 1);
		localFile = new File(localFileName);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, irodsFile, true);

		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.searchDataObjectsBasedOnName(searchTerm, 0);
		Assert.assertNotNull(entries);
		// Assert.assertTrue(entries.size() > 2);

	}

	@Test
	public void testSearchDataObjectsReplicated() throws Exception {

		String subdirPrefix = "testSearchDataObjectsOneReplicated";
		String searchTerm = "testSearchDataObjectsOneReplicated";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

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
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		dataObjectAO.putLocalDataObjectToIRODS(localFile, irodsFile, true);
		dataObjectAO
				.replicateIrodsDataObject(
						irodsFile.getAbsolutePath() + "/" + localFile.getName(),
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY));

		// second file, slightly different prefix on name
		localFileName = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, "xxx" + searchTerm + "testv1.txt", 1);
		localFile = new File(localFileName);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, irodsFile, true);

		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.searchDataObjectsBasedOnName(searchTerm, 0);
		Assert.assertNotNull(entries);
		// seems to occasionally fail need to look at this, probably just a
		// side-effect so accept >2 for now - mcc
		Assert.assertTrue(entries.size() >= 2);

	}

	@Test
	public void testSearchCollectionsAndDataObjects() throws Exception {

		String subdirPrefix = "testSearchCollectionsAndDataObjectsSubdir";
		String searchTerm = "testSearchCollectionsAndDataObjectsAndThisIsAPrettyUniqueSearchNameTooTerm";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

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
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem
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
		// Assert.assertTrue(entries.size() > 4);

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
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem
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
		Assert.assertNotNull("object was null", actual);
		boolean isDataObject = actual instanceof DataObject;
		Assert.assertTrue("was not a data object", isDataObject);

	}

	@Test
	public void testGetFullObjectForTypeDataObjectEmbeddedPlusAndSpacesInDataName()
			throws Exception {

		String testCollName = "2003_01_26_02 + band";
		String testFileName = "106-0653_IMG.JPG";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdirs();
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, true);
		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		Object actual = listAndSearchAO
				.getFullObjectForType(targetIrodsCollection + "/"
						+ testFileName);
		Assert.assertNotNull("object was null", actual);
		boolean isDataObject = actual instanceof DataObject;
		Assert.assertTrue("was not a data object", isDataObject);

	}

	@Test
	public void testGetFullObjectForRoot() throws Exception {

		String targetIrodsCollection = "/";
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
	public void testGetFullObjectForCollection() throws Exception {

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
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

	@Test(expected = FileNotFoundException.class)
	public void testGetFullObjectForNonExistant() throws Exception {

		String testFileName = "testGetFullObjectForNonExistant.txt";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

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

		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		listAndSearchAO.getFullObjectForType(null);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetFullObjectBlankPath() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		listAndSearchAO.getFullObjectForType("");

	}

	@Test
	public void testListDataObjectsSharedWithAUser() throws Exception {

		String subdirPrefix = "testListFilesAndCollectionsUnderPathWithAccessInfoForTertiaryUser";
		String fileName = "testListFilesAndCollectionsUnderPathWithAccessInfoForTertiaryUser.doc";

		int count = 30;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

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

		irodsFileSystem.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		/*
		 * List<CollectionAndDataObjectListingEntry> entries = actual
		 * .listDataObjectsSharedWithAGivenUser("/", "test2", 0);
		 */

	}

	@Test(expected = IllegalArgumentException.class)
	/**
	 * collectionAndDataObjectListingEntry for a null path should give exception
	 */
	public void testCollectionAndDataObjectListingEntryForNullPath()
			throws Exception {

		String targetIrodsCollection = null;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		listAndSearchAO
				.getCollectionAndDataObjectListingEntryAtGivenAbsolutePath(targetIrodsCollection);

	}

	@Test(expected = FileNotFoundException.class)
	/**
	 * collectionAndDataObjectListingEntry for a non existent path should give FileNotFoundException
	 */
	public void testCollectionAndDataObjectListingEntryForNonExistentCollection()
			throws Exception {

		String targetIrodsCollection = "/this/is/not/a/path";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		listAndSearchAO
				.getCollectionAndDataObjectListingEntryAtGivenAbsolutePath(targetIrodsCollection);

	}

	@Test
	/**
	 * collectionAndDataObjectListingEntry as a collection at the given irods absolute path
	 */
	public void testCollectionAndDataObjectListingEntryForCollection()
			throws Exception {

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		CollectionAndDataObjectListingEntry entry = listAndSearchAO
				.getCollectionAndDataObjectListingEntryAtGivenAbsolutePath(targetIrodsCollection);
		Assert.assertNotNull("did not find collection", entry);
	}

	@Test
	/**
	 * collectionAndDataObjectListingEntry as a collection at the given irods absolute path
	 */
	public void testCollectionAndDataObjectListingEntryForCollectionWhenRoot()
			throws Exception {

		String targetIrodsCollection = "/";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		CollectionAndDataObjectListingEntry entry = listAndSearchAO
				.getCollectionAndDataObjectListingEntryAtGivenAbsolutePath(targetIrodsCollection);
		Assert.assertNotNull("did not find collection", entry);
	}

	@Test
	/**
	 * Obtain an entry at the given valid abs path that is a data object
	 * @throws Exception
	 */
	public void testCollectionAndDataObjectListingEntryForDataObject()
			throws Exception {

		String testFileName = "testCollectionAndDataObjectListingEntryForDataObject.txt";
		long fileSize = 2;
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, fileSize);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, true);
		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		ObjStat objStat = listAndSearchAO
				.retrieveObjectStatForPath(targetIrodsCollection + "/"
						+ testFileName);
		Assert.assertNotNull("null objStat returned", objStat);
		CollectionAndDataObjectListingEntry entry = listAndSearchAO
				.getCollectionAndDataObjectListingEntryAtGivenAbsolutePath(targetIrodsCollection
						+ "/" + testFileName);
		Assert.assertEquals("wrong path", objStat.getAbsolutePath(),
				entry.getFormattedAbsolutePath());
		Assert.assertEquals("wrong length", objStat.getObjSize(),
				entry.getDataSize());
		Assert.assertEquals("wrong created", objStat.getCreatedAt(),
				entry.getCreatedAt());
		Assert.assertEquals("wrong modified", objStat.getModifiedAt(),
				entry.getModifiedAt());
		Assert.assertEquals("wrong objType",
				CollectionAndDataObjectListingEntry.ObjectType.DATA_OBJECT,
				entry.getObjectType());

	}

	@Test
	/**
	 * objStat for normal iRODS collection
	 */
	public void testObjectStatForCollection() throws Exception {

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		ObjStat objStat = listAndSearchAO
				.retrieveObjectStatForPath(targetIrodsCollection);
		Assert.assertNotNull("null objStat returned", objStat);
		Assert.assertEquals("did not get correct path", targetIrodsCollection,
				objStat.getAbsolutePath());
		Assert.assertEquals("not a collection", ObjectType.COLLECTION,
				objStat.getObjectType());
		Assert.assertEquals("not a normal spec col type", SpecColType.NORMAL,
				objStat.getSpecColType());
		Assert.assertTrue("did not set object id", objStat.getDataId() > 0);
		Assert.assertFalse("no owner name", objStat.getOwnerName().isEmpty());
		Assert.assertFalse("no owner zone", objStat.getOwnerZone().isEmpty());
		Assert.assertTrue("should have 0 len for collection",
				objStat.getObjSize() == 0);
	}

	@Test(expected = FileNotFoundException.class)
	public void testObjectStatForPathNotExists() throws Exception {

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH
								+ "/idontexistreallyidont");

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		listAndSearchAO.retrieveObjectStatForPath(targetIrodsCollection);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testObjectStatForNullPath() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		listAndSearchAO.retrieveObjectStatForPath(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testObjectStatForEmptyPath() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		listAndSearchAO.retrieveObjectStatForPath("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testObjectStatNullPath() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		listAndSearchAO.retrieveObjectStatForPath(null);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testObjectStatEmptyPath() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		listAndSearchAO.retrieveObjectStatForPath("");

	}

	@Test
	public void testObjStatForDataObject() throws Exception {

		String testFileName = "testObjStatForDataObject.txt";
		long fileSize = 2;
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, fileSize);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, true);
		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		ObjStat objStat = listAndSearchAO
				.retrieveObjectStatForPath(targetIrodsCollection + "/"
						+ testFileName);
		Assert.assertNotNull("null objStat returned", objStat);
		Assert.assertEquals("did not get correct path", targetIrodsCollection
				+ "/" + testFileName, objStat.getAbsolutePath());
		Assert.assertEquals("not a collection", ObjectType.DATA_OBJECT,
				objStat.getObjectType());
		Assert.assertEquals("not a normal spec col type", SpecColType.NORMAL,
				objStat.getSpecColType());
		Assert.assertTrue("did not set object id", objStat.getDataId() > 0);
		Assert.assertFalse("no owner name", objStat.getOwnerName().isEmpty());
		Assert.assertFalse("no owner zone", objStat.getOwnerZone().isEmpty());
		Assert.assertEquals("wrong file size", fileSize, objStat.getObjSize());
	}

}
