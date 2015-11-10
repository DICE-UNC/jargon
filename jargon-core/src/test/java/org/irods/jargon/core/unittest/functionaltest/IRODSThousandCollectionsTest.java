package org.irods.jargon.core.unittest.functionaltest;

import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSThousandCollectionsTest {
	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IRODSThousandCollectionsTestParent";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	public static final String collDir = "coll";
	public static final String funcTestUserPrefix = "IRODSThousandCollectionsTest";
	public static final int usersCount = 100;
	private static IRODSFileSystem irodsFileSystem;
	private static final String testFilePrefix = "IRODSThousandCollectionsTest";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		irodsFileSystem = IRODSFileSystem.instance();

		scratchFileUtils
		.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
		.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

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

		// now make 1000 subcolls

		IRODSFile subColl;
		for (int i = 0; i < 1000; i++) {
			subColl = irodsFileSystem.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(targetIrodsCollection,
							testFilePrefix + i);
			subColl.mkdirs();
		}

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

	@Test
	public void testListFilesAndCollectionsUnderPathWithAccessInfoViaSpecificQuery()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		SettableJargonProperties props = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		props.setUsingSpecificQueryForCollectionListingWithPermissions(true);
		// props.setMaxFilesAndDirsQueryMax(200);
		irodsFileSystem.getIrodsSession().setJargonProperties(props);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

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

		String fileName = entries.get(entries.size() - 1)
				.getNodeLabelDisplayValue();

		String nbrPart = fileName.substring(testFilePrefix.length());
		System.out.println("nbrPart");
		int nextNbr = Integer.parseInt(nbrPart) + 1;

		String expectedNextPath = testFilePrefix + nextNbr;

		// now query again

		entries = actual.listCollectionsUnderPathWithPermissions(
				targetIrodsCollection, entries.get(entries.size() - 1)
				.getCount());

		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());

		// first entry should be for the expected next file
		CollectionAndDataObjectListingEntry firstEntryOfSecondQuery = entries
				.get(0);
		Assert.assertEquals(
				"did not get the expected first record form the second page of entries",
				expectedNextPath,
				firstEntryOfSecondQuery.getNodeLabelDisplayValue());

		CollectionAndDataObjectListingEntry lastEntryOfSecondQuery = entries
				.get(entries.size() - 1);

		Assert.assertFalse("should not be last record",
				lastEntryOfSecondQuery.isLastResult());

		fileName = entries.get(entries.size() - 1).getNodeLabelDisplayValue();

		nbrPart = fileName.substring(testFilePrefix.length());
		nextNbr = Integer.parseInt(nbrPart) + 1;

		expectedNextPath = testFilePrefix + nextNbr;

		// now query again

		entries = actual.listCollectionsUnderPathWithPermissions(
				targetIrodsCollection, entries.get(entries.size() - 1)
				.getCount());

		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());

		CollectionAndDataObjectListingEntry firstEntryOfThirdQuery = entries
				.get(0);

		Assert.assertEquals(
				"did not get the expected first record form the third page of entries",
				expectedNextPath,
				firstEntryOfThirdQuery.getNodeLabelDisplayValue());

		// should be one more page of results

		CollectionAndDataObjectListingEntry lastEntryOfThirdQuery = entries
				.get(entries.size() - 1);

		Assert.assertFalse("should be a fouth page of results",
				lastEntryOfThirdQuery.isLastResult());

		// now query again

		entries = actual.listCollectionsUnderPathWithPermissions(
				targetIrodsCollection, entries.get(entries.size() - 1)
				.getCount());

		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());

		// last should be 274, query again
		CollectionAndDataObjectListingEntry lastEntryOfFourthQuery = entries
				.get(entries.size() - 1);

		Assert.assertFalse("should be a fifth page of results",
				lastEntryOfFourthQuery.isLastResult());

		fileName = lastEntryOfFourthQuery.getNodeLabelDisplayValue();

		nbrPart = fileName.substring(testFilePrefix.length());
		nextNbr = Integer.parseInt(nbrPart) + 1;

		expectedNextPath = testFilePrefix + nextNbr;

		entries = actual.listCollectionsUnderPathWithPermissions(
				targetIrodsCollection, entries.get(entries.size() - 1)
				.getCount());

		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());

		CollectionAndDataObjectListingEntry firstEntryOfFifthQuery = entries
				.get(0);

		Assert.assertEquals(
				"did not get the expected first record form the third page of entries",
				expectedNextPath,
				firstEntryOfFifthQuery.getNodeLabelDisplayValue());

		// now just page through till last page

		while (true) {
			entries = actual.listCollectionsUnderPathWithPermissions(
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
						testingProperties, IRODS_TEST_SUBDIR_PATH);

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

		String fileName = entries.get(entries.size() - 1)
				.getNodeLabelDisplayValue();

		String nbrPart = fileName.substring(testFilePrefix.length());
		System.out.println("nbrPart");
		int nextNbr = Integer.parseInt(nbrPart) + 1;

		String expectedNextPath = testFilePrefix + nextNbr;

		// now query again

		entries = actual.listCollectionsUnderPathWithPermissions(
				targetIrodsCollection, entries.get(entries.size() - 1)
				.getCount());

		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());

		// first entry should be for the expected next file
		CollectionAndDataObjectListingEntry firstEntryOfSecondQuery = entries
				.get(0);
		Assert.assertEquals(
				"did not get the expected first record form the second page of entries",
				expectedNextPath,
				firstEntryOfSecondQuery.getNodeLabelDisplayValue());

		CollectionAndDataObjectListingEntry lastEntryOfSecondQuery = entries
				.get(entries.size() - 1);

		Assert.assertFalse("should not be last record",
				lastEntryOfSecondQuery.isLastResult());

		fileName = entries.get(entries.size() - 1).getNodeLabelDisplayValue();

		nbrPart = fileName.substring(testFilePrefix.length());
		nextNbr = Integer.parseInt(nbrPart) + 1;

		expectedNextPath = testFilePrefix + nextNbr;

		// now query again

		entries = actual.listCollectionsUnderPathWithPermissions(
				targetIrodsCollection, entries.get(entries.size() - 1)
				.getCount());

		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());

		CollectionAndDataObjectListingEntry firstEntryOfThirdQuery = entries
				.get(0);

		Assert.assertEquals(
				"did not get the expected first record form the third page of entries",
				expectedNextPath,
				firstEntryOfThirdQuery.getNodeLabelDisplayValue());

		// should be one more page of results

		CollectionAndDataObjectListingEntry lastEntryOfThirdQuery = entries
				.get(entries.size() - 1);

		Assert.assertFalse("should be a fouth page of results",
				lastEntryOfThirdQuery.isLastResult());

		// now query again

		entries = actual.listCollectionsUnderPathWithPermissions(
				targetIrodsCollection, entries.get(entries.size() - 1)
				.getCount());

		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());

		// last should be 274, query again
		CollectionAndDataObjectListingEntry lastEntryOfFourthQuery = entries
				.get(entries.size() - 1);

		Assert.assertFalse("should be a fifth page of results",
				lastEntryOfFourthQuery.isLastResult());

		fileName = lastEntryOfFourthQuery.getNodeLabelDisplayValue();

		nbrPart = fileName.substring(testFilePrefix.length());
		nextNbr = Integer.parseInt(nbrPart) + 1;

		expectedNextPath = testFilePrefix + nextNbr;

		entries = actual.listCollectionsUnderPathWithPermissions(
				targetIrodsCollection, entries.get(entries.size() - 1)
				.getCount());

		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());

		CollectionAndDataObjectListingEntry firstEntryOfFifthQuery = entries
				.get(0);

		Assert.assertEquals(
				"did not get the expected first record form the third page of entries",
				expectedNextPath,
				firstEntryOfFifthQuery.getNodeLabelDisplayValue());

		// now just page through till last page

		while (true) {
			entries = actual.listCollectionsUnderPathWithPermissions(
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

}
