package org.irods.jargon.core.pub;

import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CollectionAndDataObjectListAndSearchAOImpForStrictACLTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	public static final String IRODS_TEST_SUBDIR_PATH = "CollectionAndDataObjectListAndSearchAOImpForStrictACLTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;
	private static IRODSAccount anonymousAccount = null;
	private static IRODSAccount irodsAccount = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();

		if (!testingPropertiesHelper.isTestStrictACL(testingProperties)) {
			return;
		}

		new org.irods.jargon.testutils.filemanip.ScratchFileUtils(testingProperties);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
		anonymousAccount = testingPropertiesHelper.buildAnonymousIRODSAccountFromTestProperties(testingProperties);
		irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = "/" + anonymousAccount.getZone() + "/home/public";
		IRODSFile publicDir = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		publicDir.mkdirs();
		CollectionAO collectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		collectionAO.setAccessPermissionReadAsAdmin(irodsAccount.getZone(), targetIrodsCollection,
				anonymousAccount.getUserName(), true);

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if (!testingPropertiesHelper.isTestStrictACL(testingProperties)) {
			return;
		}
		irodsFileSystem.closeAndEatExceptions();

	}

	@Test
	public void testListUnderRootWhenStrictACL() throws Exception {

		if (!testingPropertiesHelper.isTestStrictACL(testingProperties)) {
			return;
		}

		SettableJargonProperties newProps = new SettableJargonProperties();
		newProps.setDefaultToPublicIfNothingUnderRootWhenListing(true);
		irodsFileSystem.getIrodsSession().setJargonProperties(newProps);
		CollectionAndDataObjectListAndSearchAO listAndSearch = irodsFileSystem.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(anonymousAccount);
		List<CollectionAndDataObjectListingEntry> entries = listAndSearch.listDataObjectsAndCollectionsUnderPath("/");
		Assert.assertFalse("no entries available under root", entries.isEmpty());
		CollectionAndDataObjectListingEntry entry = entries.get(0);
		Assert.assertEquals("did not return the /zone", "/" + irodsAccount.getZone(), entry.getFormattedAbsolutePath());

	}

	@Test
	public void testListUnderZoneWhenStrictACL() throws Exception {

		if (!testingPropertiesHelper.isTestStrictACL(testingProperties)) {
			return;
		}

		String rootAndZone = "/" + irodsAccount.getZone();

		SettableJargonProperties newProps = new SettableJargonProperties();
		newProps.setDefaultToPublicIfNothingUnderRootWhenListing(true);
		irodsFileSystem.getIrodsSession().setJargonProperties(newProps);
		CollectionAndDataObjectListAndSearchAO listAndSearch = irodsFileSystem.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(anonymousAccount);
		List<CollectionAndDataObjectListingEntry> entries = listAndSearch
				.listDataObjectsAndCollectionsUnderPath(rootAndZone);
		Assert.assertFalse("no entries available under /zone", entries.isEmpty());
		CollectionAndDataObjectListingEntry entry = entries.get(0);
		Assert.assertEquals("did not return the /zone/home", "/" + irodsAccount.getZone() + "/home",
				entry.getFormattedAbsolutePath());

	}

	@Test
	public void testListUnderHomeAndLookForPublicWhenStrictACL() throws Exception {

		if (!testingPropertiesHelper.isTestStrictACL(testingProperties)) {
			return;
		}

		String workingDir = "/" + irodsAccount.getZone() + "/home";

		SettableJargonProperties newProps = new SettableJargonProperties();
		newProps.setDefaultToPublicIfNothingUnderRootWhenListing(true);
		irodsFileSystem.getIrodsSession().setJargonProperties(newProps);
		CollectionAndDataObjectListAndSearchAO listAndSearch = irodsFileSystem.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(anonymousAccount);
		List<CollectionAndDataObjectListingEntry> entries = listAndSearch
				.listDataObjectsAndCollectionsUnderPath(workingDir);
		Assert.assertFalse("no entries available under /zone/home", entries.isEmpty());
		CollectionAndDataObjectListingEntry entry = entries.get(0);
		Assert.assertEquals("did not return the /zone/home/public", "/" + irodsAccount.getZone() + "/home/public",
				entry.getFormattedAbsolutePath());

	}

	@Test(expected = FileNotFoundException.class)
	public void testListUnderHomeAndLookForPublicWhenStrictACLAndNotTurnedOn() throws Exception {

		if (!testingPropertiesHelper.isTestStrictACL(testingProperties)) {
			return;
		}

		String workingDir = "/" + irodsAccount.getZone() + "/home";

		SettableJargonProperties newProps = new SettableJargonProperties();
		newProps.setDefaultToPublicIfNothingUnderRootWhenListing(false);
		irodsFileSystem.getIrodsSession().setJargonProperties(newProps);
		CollectionAndDataObjectListAndSearchAO listAndSearch = irodsFileSystem.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(anonymousAccount);
		listAndSearch.listDataObjectsAndCollectionsUnderPath(workingDir);

	}
}
