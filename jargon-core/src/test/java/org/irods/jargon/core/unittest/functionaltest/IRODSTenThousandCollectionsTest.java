package org.irods.jargon.core.unittest.functionaltest;

import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.connection.SettableJargonPropertiesMBean;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSTenThousandCollectionsTest {
	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IRODSTenThousandCollectionsTestParent";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	public static final String collDir = "coll";
	public static final String funcTestUserPrefix = "IRODSTenThoCollTest";
	public static final int usersCount = 2;
	private static IRODSFileSystem irodsFileSystem;
	private static final String testFilePrefix = "IRODSTenThousandCollectionsTest";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);

		irodsFileSystem = IRODSFileSystem.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		// make the parent subdir
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSFile parentDir = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		parentDir.mkdirs();

		CollectionAO collectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		collectionAO.setAccessPermissionInherit(irodsAccount.getZone(), targetIrodsCollection, true);

		/**
		 * UserAO userAO = irodsFileSystem.getIRODSAccessObjectFactory()
		 * .getUserAO(irodsAccount);
		 *
		 * String userName; User user = new User(); for (int i = 0; i < usersCount; i++)
		 * { userName = funcTestUserPrefix + i; user.setName(userName);
		 * user.setUserType(UserTypeEnum.RODS_USER); try { userAO.addUser(user); } catch
		 * (Exception e) {
		 *
		 * } collectionAO.setAccessPermissionRead(irodsAccount.getZone(),
		 * targetIrodsCollection, userName, true); }
		 */

		// now make 10000 subcolls

		IRODSFile subColl;
		for (int i = 0; i < 10000; i++) {
			subColl = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection,
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
	public void testListFilesAndCollectionsUnderPathWithPaging() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.listDataObjectsAndCollectionsUnderPath(targetIrodsCollection);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());

	}

	@Test
	public void testListAllCollectionsUnderPath() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		SettableJargonPropertiesMBean props = new SettableJargonProperties(irodsFileSystem.getJargonProperties());
		props.setUsingSpecificQueryForCollectionListingWithPermissions(false);
		props.setMaxFilesAndDirsQueryMax(200);
		irodsFileSystem.getIrodsSession().setJargonProperties(props);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		List<CollectionAndDataObjectListingEntry> entries = actual.listAllCollectionsUnderPath(targetIrodsCollection,
				0);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());

		/*
		 * get the collection name of the last entry provided
		 */

		Assert.assertEquals("did not get all 10000 entries", entries.size());
		CollectionAndDataObjectListingEntry lastEntry = entries.get(entries.size() - 1);
		Assert.assertFalse("should  be last record", lastEntry.isLastResult());

	}

	@Test
	public void testListAllFilesAndCollectionsUnderPath() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		SettableJargonPropertiesMBean props = new SettableJargonProperties(irodsFileSystem.getJargonProperties());
		props.setUsingSpecificQueryForCollectionListingWithPermissions(false);
		props.setMaxFilesAndDirsQueryMax(1000);
		irodsFileSystem.getIrodsSession().setJargonProperties(props);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		CollectionAndDataObjectListAndSearchAO actual = irodsFileSystem.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		List<CollectionAndDataObjectListingEntry> entries = actual
				.listAllDataObjectsAndCollectionsUnderPath(targetIrodsCollection);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());

		/*
		 * get the collection name of the last entry provided
		 */

		Assert.assertEquals("did not get all 10000 entries", entries.size());
		CollectionAndDataObjectListingEntry lastEntry = entries.get(entries.size() - 1);
		Assert.assertFalse("should  be last record", lastEntry.isLastResult());

	}

}
