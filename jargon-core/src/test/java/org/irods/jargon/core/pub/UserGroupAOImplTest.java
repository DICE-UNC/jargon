package org.irods.jargon.core.pub;

import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.exception.CatalogSQLException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.InvalidGroupException;
import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.pub.domain.UserGroup;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class UserGroupAOImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	@SuppressWarnings("unused")
	private static org.irods.jargon.testutils.AssertionHelper assertionHelper = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		assertionHelper = new org.irods.jargon.testutils.AssertionHelper();
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public final void testUserGroupAOImpl() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl.instance(irodsSession);
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);
		Assert.assertNotNull("userGroupAO is null", userGroupAO);
		irodsSession.closeSession();
	}

	@Test
	public final void testFind() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl.instance(irodsSession);
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);
		UserGroup expectedUserGroup = userGroupAO.findByName(
				testingPropertiesHelper.getTestProperties().getProperty(TestingPropertiesHelper.IRODS_USER_GROUP_KEY));
		Assert.assertNotNull("no user group set up for this test", expectedUserGroup);
		UserGroup actualUserGroup = userGroupAO.find(expectedUserGroup.getUserGroupId());
		irodsSession.closeSession();
		Assert.assertNotNull("no user group returned", actualUserGroup);
		Assert.assertEquals("unexpected user group", expectedUserGroup.getUserGroupName(),
				actualUserGroup.getUserGroupName());
	}

	/**
	 * [#890] 806000 error UserGroupAO.find() when id is given as a string
	 *
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testFindGivenString() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl.instance(irodsSession);
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);

		userGroupAO.find("xxx");

	}

	@Test
	public final void testFindByName() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl.instance(irodsSession);
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);
		UserGroup userGroup = userGroupAO
				.findByName((String) testingProperties.get(TestingPropertiesHelper.IRODS_USER_GROUP_KEY));
		irodsSession.closeSession();
		Assert.assertNotNull("no user group returned", userGroup);
		Assert.assertEquals("unexpected user group",
				testingProperties.get(TestingPropertiesHelper.IRODS_USER_GROUP_KEY), userGroup.getUserGroupName());

	}

	@Test
	public final void findUserGroups() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl.instance(irodsSession);
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);
		UserGroup userGroup = userGroupAO
				.findByName((String) testingProperties.get(TestingPropertiesHelper.IRODS_USER_GROUP_KEY));
		Assert.assertNotNull("no user group returned", userGroup);

		List<UserGroup> userGroups = userGroupAO
				.findUserGroups(userGroup.getUserGroupName().substring(0, userGroup.getUserGroupName().length() - 2));
		Assert.assertFalse("no user groups found", userGroups.isEmpty());
	}

	@Test
	public final void findUserGroupsCaseInsensitive() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl.instance(irodsSession);
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);
		List<UserGroup> userGroups = userGroupAO.findUserGroups(
				((String) testingProperties.get(TestingPropertiesHelper.IRODS_USER_GROUP_KEY)).toUpperCase(), true);
		Assert.assertFalse("no user groups found", userGroups.isEmpty());
	}

	@Test
	public final void findUserGroupNamesCaseInsensitive() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl.instance(irodsSession);
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);
		List<String> userGroups = userGroupAO.findUserGroupNames(
				((String) testingProperties.get(TestingPropertiesHelper.IRODS_USER_GROUP_KEY)).toUpperCase(), true);
		Assert.assertFalse("no user groups found", userGroups.isEmpty());
	}

	@Test
	public final void findUserGroupsEmptyTerm() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl.instance(irodsSession);
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);

		List<UserGroup> userGroups = userGroupAO.findUserGroups("");
		Assert.assertFalse("no user groups found", userGroups.isEmpty());
	}

	@Test
	public final void testFindByNameNotExists() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);
		UserGroup userGroup = userGroupAO.findByName("i dont exist here");
		Assert.assertNull("user group returned, there should not be one", userGroup);

	}

	@Test
	public final void testFindAll() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl.instance(irodsSession);
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);

		List<UserGroup> userGroup = userGroupAO.findAll();
		irodsSession.closeSession();
		Assert.assertNotNull("no user group returned", userGroup);
		Assert.assertFalse("no user groups returned for query", userGroup.isEmpty());
	}

	@Test
	public final void testFindUserGroupsForUser() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl.instance(irodsSession);
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);

		List<UserGroup> userGroup = userGroupAO
				.findUserGroupsForUser(testingProperties.getProperty(TestingPropertiesHelper.IRODS_USER_KEY));
		irodsSession.closeSession();
		Assert.assertNotNull("no user group returned", userGroup);
		Assert.assertTrue("no user group returned for query", userGroup.size() > 0);

		for (UserGroup actual : userGroup) {
			Assert.assertFalse("should not have user name in results",
					irodsAccount.getUserName().equals(actual.getUserGroupName()));
		}

	}

	@Test
	public final void testAddUserGroup() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		String testUserGroup = "testAddUserGroup";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroup);
		userGroup.setZone(irodsAccount.getZone());

		userGroupAO.removeUserGroup(userGroup);
		userGroupAO.addUserGroup(userGroup);

		UserGroup actual = userGroupAO.findByName(testUserGroup);
		Assert.assertNotNull("no user group returned", actual);
		Assert.assertEquals("user group has wrong name", testUserGroup, userGroup.getUserGroupName());

		userGroupAO.removeUserGroup(userGroup);
	}

	/**
	 * "User lacks privileges to invoke the given API" when adding groups / users to
	 * groups #255
	 *
	 * @throws Exception
	 */
	@Test
	public final void testCreateGroupAndAddUserAsGroupAdminBug255() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		String testUserGroup = "bug255group";
		String testUser = "bug255user";
		String testGroupAdminUser = "bug255GroupAdmin";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
		userAO.deleteUser(testGroupAdminUser);
		userAO.deleteUser(testUser);
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);
		userGroupAO.removeUserGroup(testUserGroup);

		User groupAdminUser = new User();
		groupAdminUser.setName(testGroupAdminUser);
		groupAdminUser.setUserType(UserTypeEnum.GROUP_ADMIN);
		userAO.addUser(groupAdminUser);
		userAO.changeAUserPasswordByAnAdmin(testGroupAdminUser, testGroupAdminUser);

		IRODSAccount groupAdminAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(testingProperties, testGroupAdminUser,
						testGroupAdminUser);

		UserGroup addedGroup = new UserGroup();
		addedGroup.setUserGroupName(testUserGroup);
		addedGroup.setZone(irodsAccount.getZone());

		UserGroupAO groupAdminGroupAO = accessObjectFactory.getUserGroupAO(groupAdminAccount);
		groupAdminGroupAO.addUserGroupAsGroupAdmin(addedGroup);

	}

	@Test
	public final void testAddUserToGroupAsGroupAdminBug255() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		String testUserGroup = "testAddUserToGroupAsGroupAdminBug255";
		String testUser = "bug255user2";
		String testGroupAdminUser = "bug255GroupAdmin2";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroup);
		userGroup.setZone(irodsAccount.getZone());

		userGroupAO.removeUserGroup(userGroup);
		userGroupAO.addUserGroup(userGroup);

		User groupAdminUser = new User();
		groupAdminUser.setName(testGroupAdminUser);
		groupAdminUser.setUserType(UserTypeEnum.GROUP_ADMIN);
		userAO.deleteUser(testGroupAdminUser);
		userAO.addUser(groupAdminUser);
		userAO.changeAUserPasswordByAnAdmin(testGroupAdminUser, testGroupAdminUser);

		User userToAdd = new User();
		userToAdd.setName(testUser);
		userToAdd.setUserType(UserTypeEnum.RODS_USER);
		userAO.deleteUser(testUser);
		userAO.addUser(userToAdd);
		userAO.changeAUserPasswordByAnAdmin(testUser, testUser);

		IRODSAccount groupAdminAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(testingProperties, testGroupAdminUser,
						testGroupAdminUser);

		UserGroupAO groupAdminUserGroupAO = accessObjectFactory.getUserGroupAO(groupAdminAccount);
		groupAdminUserGroupAO.addUserToGroupAsGroupAdmin(testUserGroup, testUser, irodsAccount.getZone());

		// list group members and check

		List<User> users = userGroupAO.listUserGroupMembers(testUserGroup);
		boolean found = false;
		for (User foundUser : users) {
			if (foundUser.getName().equals(testUser)) {
				found = true;
				break;
			}
		}
		Assert.assertTrue("did not add user to group", found);
	}

	/**
	 * Add the current iRODS user to a new group and see if it lists
	 *
	 * @throws Exception
	 */
	@Test
	public final void testAddUserToGroup() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		String testUserGroup = "testAddUserToGroup";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroup);
		userGroup.setZone(irodsAccount.getZone());

		userGroupAO.removeUserGroup(userGroup);
		userGroupAO.addUserGroup(userGroup);

		userGroupAO.addUserToGroup(testUserGroup, irodsAccount.getUserName(), irodsAccount.getZone());

		List<User> users = userGroupAO.listUserGroupMembers(testUserGroup);

		boolean foundMine = false;
		for (User user : users) {
			if (user.getName().equals(irodsAccount.getUserName())) {
				foundMine = true;
			}
		}

		// userGroupAO.removeUserGroup(userGroup);
		// Assert.assertTrue("did not find user I just added", foundMine);
	}

	/**
	 * Add the current iRODS user to a group that does not exist
	 *
	 * @throws Exception
	 */
	@Test(expected = InvalidGroupException.class)
	public final void testAddUserToGroupGroupDoesNotExist() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		String testUserGroup = "testAddUserToGroupGroupDoesNotExist";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);

		userGroupAO.addUserToGroup(testUserGroup, irodsAccount.getUserName(), null);

	}

	/**
	 * Add a non-existent user to an existing user group
	 *
	 * @throws Exception
	 */
	@Test(expected = InvalidUserException.class)
	public final void testAddUserToGroupUserDoesNotExist() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		String testUserGroup = "testAddUserToGroupUserDoesNotExist";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroup);
		userGroup.setZone(irodsAccount.getZone());

		userGroupAO.removeUserGroup(userGroup);
		userGroupAO.addUserGroup(userGroup);

		userGroupAO.addUserToGroup(testUserGroup, "testAddUserToGroupUserDoesNotExist", null);

	}

	/**
	 * Add the current iRODS user to a new group twice
	 *
	 * @throws Exception
	 */
	public final void testAddUserToGroupTwice() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		String testUserGroup = "testAddUserToGroupTwice";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroup);
		userGroup.setZone(irodsAccount.getZone());

		userGroupAO.removeUserGroup(userGroup);
		userGroupAO.addUserGroup(userGroup);

		userGroupAO.addUserToGroup(testUserGroup, irodsAccount.getUserName(), null);

		if (accessObjectFactory.getIRODSServerProperties(irodsAccount).isAtLeastIrods430()) {
			Assert.assertThrows(CatalogSQLException.class,
					() -> userGroupAO.addUserToGroup(testUserGroup, irodsAccount.getUserName(), null));
		} else {
			Assert.assertThrows(DuplicateDataException.class,
					() -> userGroupAO.addUserToGroup(testUserGroup, irodsAccount.getUserName(), null));
		}

	}

	@Test
	public final void testListUserGroupMembersGroupExists() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		String testUserGroup = "testListUserGroupMembersGroupExists";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroup);
		userGroup.setZone(irodsAccount.getZone());

		userGroupAO.removeUserGroup(userGroup);
		userGroupAO.addUserGroup(userGroup);

		userGroupAO.addUserToGroup(testUserGroup, irodsAccount.getUserName(), null);

		List<User> users = userGroupAO.listUserGroupMembers(testUserGroup);
		Assert.assertTrue("no users found", users.size() == 1);
		// should be the added user
		User user = users.get(0);
		Assert.assertEquals("did not find normal user", irodsAccount.getUserName(), user.getName());

		userGroupAO.removeUserGroup(userGroup);
	}

	/**
	 * List members of a non-existent group
	 *
	 * @throws Exception
	 */
	@Test
	public final void testListUserGroupMembersGroupNotExists() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		String testUserGroup = "testListUserGroupMembersGroupNotExists";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);

		List<User> users = userGroupAO.listUserGroupMembers(testUserGroup);
		Assert.assertTrue("no users should have been found", users.isEmpty());

	}

	@Test
	public final void testRemoveUserGroup() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		String testUserGroup = "testRemoveUserGroup";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroup);
		userGroup.setZone(irodsAccount.getZone());

		userGroupAO.removeUserGroup(userGroup);
		userGroupAO.addUserGroup(userGroup);
		userGroupAO.removeUserGroup(userGroup);

		UserGroup actual = userGroupAO.findByName(testUserGroup);
		Assert.assertNull("user group returned", actual);
	}

	/**
	 * Remove a user group by name that exists
	 *
	 * @throws Exception
	 */
	@Test
	public final void testRemoveUserGroupByName() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		String testUserGroup = "testRemoveUserGroupByName";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroup);
		userGroup.setZone(irodsAccount.getZone());

		userGroupAO.removeUserGroup(userGroup);
		userGroupAO.addUserGroup(userGroup);
		userGroupAO.removeUserGroup(userGroup.getUserGroupName());

		UserGroup actual = userGroupAO.findByName(testUserGroup);
		Assert.assertNull("user group returned", actual);
	}

	/**
	 * Remove a user group by name that does not exist, should just log and continue
	 * as normal
	 *
	 * @throws Exception
	 */
	@Test
	public final void testRemoveUserGroupByNameThatDoesNotExist() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		String testUserGroup = "testRemoveUserGroupByNameThatDoesNotExist";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);

		userGroupAO.removeUserGroup(testUserGroup);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testAddUserGroupNullGroupName() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		String testUserGroup = null;

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroup);
		userGroup.setZone(irodsAccount.getZone());

		userGroupAO.addUserGroup(userGroup);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testAddUserGroupBlankGroupName() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		String testUserGroup = "";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroup);
		userGroup.setZone(irodsAccount.getZone());

		userGroupAO.addUserGroup(userGroup);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testAddUserGroupNoZone() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		String testUserGroup = "testAddUserGroupNoZone";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroup);

		userGroupAO.addUserGroup(userGroup);

	}

	public final void testAddDuplicateUserGroup() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		String testUserGroup = "testAddDuplicateUserGroup";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroup);
		userGroup.setZone(irodsAccount.getZone());

		userGroupAO.removeUserGroup(userGroup);
		userGroupAO.addUserGroup(userGroup);

		if (accessObjectFactory.getIRODSServerProperties(irodsAccount).isAtLeastIrods430()) {
			Assert.assertThrows(CatalogSQLException.class, () -> userGroupAO.addUserGroup(userGroup));
		} else {
			Assert.assertThrows(DuplicateDataException.class, () -> userGroupAO.addUserGroup(userGroup));
		}

	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddUserToGroupNullUserGroup() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);
		userGroupAO.addUserToGroup(null, "test", null);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testRemoveUserFromGroupNullUserGroup() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);
		userGroupAO.removeUserFromGroup(null, "test", null);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testRemoveUserFromGroupBlankUserGroup() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);
		userGroupAO.removeUserFromGroup("", "test", null);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testRemoveUserFromGroupNullUser() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);
		userGroupAO.removeUserFromGroup("test", null, null);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testRemoveUserFromGroupBlankUser() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);
		userGroupAO.removeUserFromGroup("test", "", null);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddUserToGroupNullUserName() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);
		userGroupAO.addUserToGroup("test", null, "");

	}

	/**
	 * Test a normal remove of an existing user from an existing group
	 *
	 * @throws Exception
	 */
	@Test
	public final void testRemoveUserFromGroup() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		String testUserGroup = "testRemoveUserFromGroup";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroup);
		userGroup.setZone(irodsAccount.getZone());

		userGroupAO.removeUserGroup(userGroup);
		userGroupAO.addUserGroup(userGroup);

		userGroupAO.addUserToGroup(testUserGroup, irodsAccount.getUserName(), null);

		userGroupAO.removeUserFromGroup(testUserGroup, irodsAccount.getUserName(), null);

		List<User> users = userGroupAO.listUserGroupMembers(testUserGroup);

		boolean foundMine = false;
		for (User user : users) {
			if (user.getName().equals(irodsAccount.getUserName())) {
				foundMine = true;
			}
		}

		userGroupAO.removeUserGroup(userGroup);
		Assert.assertFalse("found removed user", foundMine);
	}

	/**
	 * Remove a user that does not exist from the group
	 *
	 * @throws Exception
	 */
	@Test(expected = InvalidUserException.class)
	public final void testRemoveUserFromGroupUserNotExists() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		String testUserGroup = "testRemoveUserFromGroupUserNotExists";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroup);
		userGroup.setZone(irodsAccount.getZone());

		userGroupAO.removeUserGroup(userGroup);
		userGroupAO.addUserGroup(userGroup);

		userGroupAO.removeUserFromGroup(testUserGroup, "testRemoveUserFromGroupUserNotExists-bogus", null);
	}

	@Test(expected = InvalidGroupException.class)
	public final void testRemoveUserFromGroupGroupNotExists() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		String testUserGroup = "testRemoveUserFromGroupGroupNotExists";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);

		userGroupAO.removeUserFromGroup(testUserGroup, irodsAccount.getUserName(), null);
	}

	/**
	 * Test removal of valid user not in group.
	 *
	 * @throws Exception
	 */
	@Test
	public final void testRemoveUserFromGroupUserNotInGroup() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		String testUserGroup = "testRemoveUserFromGroupUserNotInGroup";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroup);
		userGroup.setZone(irodsAccount.getZone());

		userGroupAO.removeUserGroup(userGroup);
		userGroupAO.addUserGroup(userGroup);

		if (accessObjectFactory.getIRODSServerProperties(irodsAccount).isAtLeastIrods432()) {
			JargonException thrown = Assert.assertThrows(JargonException.class,
					() -> userGroupAO.removeUserFromGroup(testUserGroup,
							testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY), null));
			Assert.assertEquals(thrown.getUnderlyingIRODSExceptionCode(), -1830000); // USER_NOT_IN_GROUP
		}
		else {
			userGroupAO.removeUserFromGroup(testUserGroup,
					testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY), null);
		}
	}

	/**
	 * Check if an existing user is in an existing group
	 *
	 * @throws Exception
	 */
	@Test
	public final void testIsUserInGroup() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		String testUserGroup = "testIsUserInGroup";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroup);
		userGroup.setZone(irodsAccount.getZone());

		userGroupAO.removeUserGroup(userGroup);
		userGroupAO.addUserGroup(userGroup);

		userGroupAO.addUserToGroup(testUserGroup, irodsAccount.getUserName(), null);

		boolean inGroup = userGroupAO.isUserInGroup(irodsAccount.getUserName(), testUserGroup);
		Assert.assertTrue("user should be in group", inGroup);
	}

	/**
	 * Check if an existing user is in an non-existent group
	 *
	 * @throws Exception
	 */
	@Test
	public final void testIsUserInGroupNoGroup() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		String testUserGroup = "testIsUserInGroupNoGroup";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);

		boolean inGroup = userGroupAO.isUserInGroup(irodsAccount.getUserName(), testUserGroup);
		Assert.assertFalse("user should not be in group", inGroup);
	}

	/**
	 * check null handling user group
	 *
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testIsUserInGroupNullGroup() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);

		userGroupAO.isUserInGroup(irodsAccount.getUserName(), null);
	}

	/**
	 * check null handling user
	 *
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testIsUserInGroupNullUser() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);

		userGroupAO.isUserInGroup(null, "test");
	}

}
