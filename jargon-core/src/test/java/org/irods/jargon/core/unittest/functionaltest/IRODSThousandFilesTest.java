package org.irods.jargon.core.unittest.functionaltest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.MountedCollectionAO;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSThousandFilesTest {
	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IrodsThousandFilesTestParent";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	public static final String collDir = "coll";
	public static final String funcTestUserPrefix = "IRODSThousandFilesTest";
	public static final int usersCount = 100;
	private static IRODSFileSystem irodsFileSystem;
	private static final String testFilePrefix = "thousandFileTest";
	private static final String testFileSuffix = ".txt";
	private static String localAbsPath = "";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);

		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);

		irodsTestSetupUtilities = new IRODSTestSetupUtilities();

		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);

		irodsFileSystem = IRODSFileSystem.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		// put in the thousand files

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		localAbsPath = absPath + collDir;

		FileGenerator.generateManyFilesInGivenDirectory(IRODS_TEST_SUBDIR_PATH
				+ "/" + collDir, testFilePrefix, testFileSuffix, 1000, 20, 500);

		DataTransferOperations dto = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		// make the put subdir
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSFile parentDir = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		parentDir.mkdirs();

		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		collectionAO.setAccessPermissionInherit(irodsAccount.getZone(),
				targetIrodsCollection, true);
		UserAO userAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getUserAO(irodsAccount);

		String userName;
		User user = new User();
		for (int i = 0; i < usersCount; i++) {
			userName = funcTestUserPrefix + i;
			user.setName(userName);
			user.setUserType(UserTypeEnum.RODS_USER);
			try {
				userAO.addUser(user);
			} catch (Exception e) {

			}
			collectionAO.setAccessPermissionRead(irodsAccount.getZone(),
					targetIrodsCollection, userName, true);
		}

		File sourceFile = new File(absPath + collDir);

		dto.putOperation(sourceFile, parentDir, null, null);

		// now add avu's to each
		addAVUsToEachFile();

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	public static final void addAVUsToEachFile() throws Exception {

		String avu1Attrib = "avu1";
		String avu1Value = "avu1value";
		String avu2Attrib = "avu2";

		IRODSAccount account = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile irodsFile = irodsFileSystem
				.getIRODSFileFactory(account)
				.instanceIRODSFile(
						testingPropertiesHelper
								.buildIRODSCollectionAbsolutePathFromTestProperties(
										testingProperties,
										IRODS_TEST_SUBDIR_PATH + '/' + collDir));

		// get a list of files underneath the top-level directory, and add some
		// avu's to each one

		String[] fileList = irodsFile.list();
		IRODSFile subFile = null;
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(account);

		for (String element : fileList) {
			subFile = irodsFileSystem.getIRODSFileFactory(account)
					.instanceIRODSFile(
							irodsFile.getAbsolutePath() + '/' + element);

			dataObjectAO.addAVUMetadata(subFile.getAbsolutePath(),
					AvuData.instance(avu1Attrib, avu1Value, ""));
			dataObjectAO.addAVUMetadata(subFile.getAbsolutePath(),
					AvuData.instance(avu2Attrib, avu1Value, ""));

		}

		irodsFileSystem.close();
	}

	@Test
	public void testSearchForAvuFiles() throws Exception {
		String avu1Attrib = "avu1";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		List<AVUQueryElement> avuQueryElements = new ArrayList<AVUQueryElement>();
		avuQueryElements
				.add(AVUQueryElement.instanceForValueQuery(
						AVUQueryPart.ATTRIBUTE, AVUQueryOperatorEnum.EQUAL,
						avu1Attrib));

		List<MetaDataAndDomainData> metadataElements = dataObjectAO
				.findMetadataValuesByMetadataQuery(avuQueryElements);

		Assert.assertFalse("did not get results", metadataElements.isEmpty());
	}

	@Test
	public void testListFilesAndCollectionsUnderPathWithAccessInfoViaGenQuery()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		SettableJargonProperties props = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		props.setUsingSpecificQueryForCollectionListingWithPermissions(false);
		irodsFileSystem.getIrodsSession().setJargonProperties(props);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/coll");

		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.listDataObjectsAndCollectionsUnderPathWithPermissions(targetIrodsCollection);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());

		/*
		 * First batch is 1000 * count entries, since the last entry is not the
		 * 'last' record, it should be dropped for the next batch. Therefore,
		 * the result should be 1000/count - 1 entries
		 */

		int recordLimit = irodsFileSystem.getJargonProperties()
				.getMaxFilesAndDirsQueryMax();
		int expectedCount = recordLimit / usersCount - 1;

		// should have 1000 in this batch
		Assert.assertEquals("did not get back the  rows I requested",
				expectedCount, entries.size());

		// bounce thru entries, each has count permissions + 1 for the user that
		// created the collection

		for (CollectionAndDataObjectListingEntry entry : entries) {
			Assert.assertEquals(
					"did not have the expected number of permissions",
					usersCount + 1, entry.getUserFilePermission().size());
		}

		/*
		 * get the collection name of the last entry provided
		 */
		String lastPathFromInputData = entries.get(entries.size() - 1)
				.getFormattedAbsolutePath();

		System.out.println("last path:" + lastPathFromInputData);

		String fileName = entries.get(entries.size() - 1).getPathOrName();

		// file name like thousandFileTest141.txt

		String nbrPart = fileName.substring(testFilePrefix.length(),
				fileName.indexOf('.'));
		System.out.println("nbrPart");
		int nextNbr = Integer.parseInt(nbrPart) + 1;

		String expectedNextPath = testFilePrefix + nextNbr + testFileSuffix;

		// now query again

		entries = actual.listDataObjectsUnderPathWithPermissions(
				targetIrodsCollection, entries.get(entries.size() - 1)
						.getCount());

		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());

		// first entry should be for the expected next file
		CollectionAndDataObjectListingEntry firstEntryOfSecondQuery = entries
				.get(0);
		Assert.assertEquals(
				"did not get the expected first record form the second page of entries",
				expectedNextPath, firstEntryOfSecondQuery.getPathOrName());

		CollectionAndDataObjectListingEntry lastEntryOfSecondQuery = entries
				.get(entries.size() - 1);

		expectedNextPath = testFilePrefix + 186 + testFileSuffix;

		Assert.assertEquals(
				"did not get the expected last record form the second page of entries",
				expectedNextPath, lastEntryOfSecondQuery.getPathOrName());

		Assert.assertFalse("should not be last record",
				lastEntryOfSecondQuery.isLastResult());

		fileName = entries.get(entries.size() - 1).getPathOrName();

		// file name like thousandFileTest141.txt

		nbrPart = fileName.substring(testFilePrefix.length(),
				fileName.indexOf('.'));
		nextNbr = Integer.parseInt(nbrPart) + 1;

		expectedNextPath = testFilePrefix + nextNbr + testFileSuffix;

		// now query again

		entries = actual.listDataObjectsUnderPathWithPermissions(
				targetIrodsCollection, entries.get(entries.size() - 1)
						.getCount());

		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());

		CollectionAndDataObjectListingEntry firstEntryOfThirdQuery = entries
				.get(0);

		Assert.assertEquals(
				"did not get the expected first record form the third page of entries",
				expectedNextPath, firstEntryOfThirdQuery.getPathOrName());

		// should be one more page of results

		CollectionAndDataObjectListingEntry lastEntryOfThirdQuery = entries
				.get(entries.size() - 1);

		Assert.assertFalse("should be a fouth page of results",
				lastEntryOfThirdQuery.isLastResult());

		// now query again

		entries = actual.listDataObjectsUnderPathWithPermissions(
				targetIrodsCollection, entries.get(entries.size() - 1)
						.getCount());

		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());

		// last should be 274, query again
		CollectionAndDataObjectListingEntry lastEntryOfFourthQuery = entries
				.get(entries.size() - 1);

		Assert.assertFalse("should be a fifth page of results",
				lastEntryOfFourthQuery.isLastResult());

		fileName = lastEntryOfFourthQuery.getPathOrName();

		nbrPart = fileName.substring(testFilePrefix.length(),
				fileName.indexOf('.'));
		nextNbr = Integer.parseInt(nbrPart) + 1;

		expectedNextPath = testFilePrefix + nextNbr + testFileSuffix;

		entries = actual.listDataObjectsUnderPathWithPermissions(
				targetIrodsCollection, entries.get(entries.size() - 1)
						.getCount());

		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());

		CollectionAndDataObjectListingEntry firstEntryOfFifthQuery = entries
				.get(0);

		Assert.assertEquals(
				"did not get the expected first record form the third page of entries",
				expectedNextPath, firstEntryOfFifthQuery.getPathOrName());

		// now just page through till last page

		while (true) {
			entries = actual.listDataObjectsUnderPathWithPermissions(
					targetIrodsCollection, entries.get(entries.size() - 1)
							.getCount());
			CollectionAndDataObjectListingEntry lastEntryOfLoopedQuery = entries
					.get(entries.size() - 1);
			if (lastEntryOfLoopedQuery.isLastResult() == true) {
				break;
			}

		}

		// System.out.println("got to last entry");

		// here we just want to know that we made it out of the loop
		Assert.assertTrue(true);

	}

	@Test
	public void testListFilesAndCollectionsUnderPathWithAccessInfoViaSpecificQuery()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		SettableJargonProperties props = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		props.setUsingSpecificQueryForCollectionListingWithPermissions(true);
		irodsFileSystem.getIrodsSession().setJargonProperties(props);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/coll");

		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.listDataObjectsAndCollectionsUnderPathWithPermissions(targetIrodsCollection);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());

		/*
		 * First batch is 1000 * count entries, since the last entry is not the
		 * 'last' record, it should be dropped for the next batch. Therefore,
		 * the result should be 1000/count - 1 entries
		 */

		int recordLimit = irodsFileSystem.getJargonProperties()
				.getMaxFilesAndDirsQueryMax();
		int expectedCount = recordLimit / usersCount - 1;

		// should have 1000 in this batch
		Assert.assertEquals("did not get back the  rows I requested",
				expectedCount, entries.size());

		// bounce thru entries, each has count permissions + 1 for the user that
		// created the collection

		for (CollectionAndDataObjectListingEntry entry : entries) {
			Assert.assertEquals(
					"did not have the expected number of permissions",
					usersCount + 1, entry.getUserFilePermission().size());
		}

		/*
		 * get the collection name of the last entry provided
		 */
		String lastPathFromInputData = entries.get(entries.size() - 1)
				.getFormattedAbsolutePath();

		System.out.println("last path:" + lastPathFromInputData);

		String fileName = entries.get(entries.size() - 1).getPathOrName();

		// file name like thousandFileTest141.txt

		String nbrPart = fileName.substring(testFilePrefix.length(),
				fileName.indexOf('.'));
		System.out.println("nbrPart");
		int nextNbr = Integer.parseInt(nbrPart) + 1;

		String expectedNextPath = testFilePrefix + nextNbr + testFileSuffix;

		// now query again

		entries = actual.listDataObjectsUnderPathWithPermissions(
				targetIrodsCollection, entries.get(entries.size() - 1)
						.getCount());

		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());

		// first entry should be for the expected next file
		CollectionAndDataObjectListingEntry firstEntryOfSecondQuery = entries
				.get(0);
		Assert.assertEquals(
				"did not get the expected first record form the second page of entries",
				expectedNextPath, firstEntryOfSecondQuery.getPathOrName());

		CollectionAndDataObjectListingEntry lastEntryOfSecondQuery = entries
				.get(entries.size() - 1);

		expectedNextPath = testFilePrefix + 186 + testFileSuffix;

		Assert.assertEquals(
				"did not get the expected last record form the second page of entries",
				expectedNextPath, lastEntryOfSecondQuery.getPathOrName());

		Assert.assertFalse("should not be last record",
				lastEntryOfSecondQuery.isLastResult());

		fileName = entries.get(entries.size() - 1).getPathOrName();

		// file name like thousandFileTest141.txt

		nbrPart = fileName.substring(testFilePrefix.length(),
				fileName.indexOf('.'));
		nextNbr = Integer.parseInt(nbrPart) + 1;

		expectedNextPath = testFilePrefix + nextNbr + testFileSuffix;

		// now query again

		entries = actual.listDataObjectsUnderPathWithPermissions(
				targetIrodsCollection, entries.get(entries.size() - 1)
						.getCount());

		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());

		CollectionAndDataObjectListingEntry firstEntryOfThirdQuery = entries
				.get(0);

		Assert.assertEquals(
				"did not get the expected first record form the third page of entries",
				expectedNextPath, firstEntryOfThirdQuery.getPathOrName());

		// should be one more page of results

		CollectionAndDataObjectListingEntry lastEntryOfThirdQuery = entries
				.get(entries.size() - 1);

		Assert.assertFalse("should be a fouth page of results",
				lastEntryOfThirdQuery.isLastResult());

		// now query again

		entries = actual.listDataObjectsUnderPathWithPermissions(
				targetIrodsCollection, entries.get(entries.size() - 1)
						.getCount());

		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());

		// last should be 274, query again
		CollectionAndDataObjectListingEntry lastEntryOfFourthQuery = entries
				.get(entries.size() - 1);

		Assert.assertFalse("should be a fifth page of results",
				lastEntryOfFourthQuery.isLastResult());

		fileName = lastEntryOfFourthQuery.getPathOrName();

		nbrPart = fileName.substring(testFilePrefix.length(),
				fileName.indexOf('.'));
		nextNbr = Integer.parseInt(nbrPart) + 1;

		expectedNextPath = testFilePrefix + nextNbr + testFileSuffix;

		entries = actual.listDataObjectsUnderPathWithPermissions(
				targetIrodsCollection, entries.get(entries.size() - 1)
						.getCount());

		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());

		CollectionAndDataObjectListingEntry firstEntryOfFifthQuery = entries
				.get(0);

		Assert.assertEquals(
				"did not get the expected first record form the third page of entries",
				expectedNextPath, firstEntryOfFifthQuery.getPathOrName());

		// now just page through till last page

		while (true) {
			entries = actual.listDataObjectsUnderPathWithPermissions(
					targetIrodsCollection, entries.get(entries.size() - 1)
							.getCount());
			CollectionAndDataObjectListingEntry lastEntryOfLoopedQuery = entries
					.get(entries.size() - 1);
			if (lastEntryOfLoopedQuery.isLastResult() == true) {
				break;
			}

		}

		// System.out.println("got to last entry");

		// here we just want to know that we made it out of the loop
		Assert.assertTrue(true);

	}

	/**
	 * As a practical matter, this test is only run if the test runs on the same
	 * machine as the iRODS physical file system.
	 *
	 * The prop test.option.exercise.filesystem.mount.local must be true in
	 * testing properties, as configured in your settings.xml file
	 *
	 * @throws Exception
	 */
	@Test
	public void testListFilesAndCollectionsUnderMountedFilePath()
			throws Exception {

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		if (!testingPropertiesHelper
				.isTestFileSystemMountLocal(testingProperties)) {
			return;
		}

		String testIrodsMountPoint = "testListFilesAndCollectionsUnderMountedFilePath";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		SettableJargonProperties props = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		props.setUsingSpecificQueryForCollectionListingWithPermissions(false);
		irodsFileSystem.getIrodsSession().setJargonProperties(props);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testIrodsMountPoint);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		mountedCollectionAO
				.createMountedFileSystemCollection(localAbsPath,
						targetIrodsCollection,
						irodsAccount.getDefaultStorageResource());

		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.listDataObjectsAndCollectionsUnderPath(targetIrodsCollection);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());

		// System.out.println("got to last entry");

		// here we just want to know that we made it out of the loop
		Assert.assertTrue(true);

	}

}
