package org.irods.jargon.core.pub;

import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.pub.domain.UserGroup;
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

	@Test
	public void testListingUnderHomeWithGroupPermittedFolderBug313() throws Exception {

		if (!testingPropertiesHelper.isTestStrictACL(testingProperties)) {
			return;
		}

		SettableJargonProperties newProps = new SettableJargonProperties();
		newProps.setDefaultToPublicIfNothingUnderRootWhenListing(true);
		irodsFileSystem.getIrodsSession().setJargonProperties(newProps);

		String workingDir = "/" + irodsAccount.getZone() + "/home";
		String homeDir = "grpBug313";
		String groupName = "test-grpBug313";
		String subFolderName = "testListingUnderHomeWithGroupPermittedFolderBug313";

		// create a folder under home for grpBug313

		IRODSAccount adminAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSFile groupTopLevel = irodsFileSystem.getIRODSAccessObjectFactory().getIRODSFileFactory(adminAccount)
				.instanceIRODSFile(workingDir, homeDir);
		groupTopLevel.delete();
		groupTopLevel.mkdirs();

		// add a group to go with this folder

		UserGroupAO userGroupAO = irodsFileSystem.getIRODSAccessObjectFactory().getUserGroupAO(adminAccount);

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(groupName);
		userGroup.setZone(irodsAccount.getZone());

		userGroupAO.removeUserGroup(userGroup);
		try {
			userGroupAO.addUserGroup(userGroup);
		} catch (DuplicateDataException dde) {
			System.out.println("TODO why does it do this?");
			/*
			 * TODO: irods treats the group as existing b/c the directory is there under
			 * home..why?
			 */
		}

		// make test2 a member of grpBug313

		IRODSAccount test2Account = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		userGroupAO.addUserToGroup(groupName, test2Account.getUserName(), "");

		// set permissions, inheritance make a dir under /zone/home/grpBug313

		CollectionAO collectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(adminAccount);
		collectionAO.setAccessPermissionInheritAsAdmin("", groupTopLevel.getAbsolutePath(), true);
		collectionAO.setAccessPermissionOwnAsAdmin("", groupTopLevel.getAbsolutePath(), groupName, true);

		// add a subfolder to find
		IRODSFile subfolderFile = irodsFileSystem.getIRODSAccessObjectFactory().getIRODSFileFactory(adminAccount)
				.instanceIRODSFile(groupTopLevel.getAbsolutePath(), subFolderName);
		subfolderFile.mkdirs();

		// do a listing under /zone/home as secondary user and expect to see the
		// grpBug313 folder under home with the user and public
		String queryPath = "/" + irodsAccount.getZone() + "/home";
		CollectionAndDataObjectListAndSearchAO collList = irodsFileSystem.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(test2Account);
		List<CollectionAndDataObjectListingEntry> listing = collList.listDataObjectsAndCollectionsUnderPath(queryPath);
		Assert.assertFalse("no entries", listing.isEmpty());

		boolean foundExpectedEntryViaGroup = false;

		for (CollectionAndDataObjectListingEntry entry : listing) {
			if (entry.getNodeLabelDisplayValue().equals(homeDir)) {
				foundExpectedEntryViaGroup = true;
			}
		}

		Assert.assertTrue("Didnt find expected entry via group permissions", foundExpectedEntryViaGroup);

	}

}
