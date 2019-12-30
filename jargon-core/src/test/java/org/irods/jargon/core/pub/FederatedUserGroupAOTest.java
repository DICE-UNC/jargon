package org.irods.jargon.core.pub;

import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.pub.domain.UserGroup;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class FederatedUserGroupAOTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testFindUserGroupsForUserInZone() throws Exception {
		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		// zone2 account
		IRODSAccount federatedAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(federatedAccount);
		String testUserGroup = "testFindUserGroupsForUserInZone";
		String testUser = testingPropertiesHelper.buildUserNameForNonAdminUserInFederatedZone(testingProperties);
		// test1#zone1 added to zone1 in group testFindUserGroupsForUserInZone
		userGroupAO.removeUserGroup(testUserGroup);

		UserGroup addedGroup = new UserGroup();
		addedGroup.setUserGroupName(testUserGroup);
		addedGroup.setZone(federatedAccount.getZone());
		userGroupAO.addUserGroup(addedGroup);
		userGroupAO.addUserToGroup(testUserGroup, testUser, "");

		// now go to other zone and list users in the given group
		// zone1
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		UserGroupAO federatedUserGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);
		// find test1#zone1 in zone2, it's there
		List<UserGroup> userGroupList = federatedUserGroupAO.findUserGroupsForUserInZone(testUser,
				federatedAccount.getZone());
		Assert.assertFalse("no user groups found", userGroupList.isEmpty());

	}

	@Test
	public void testListUsersInFederatedZone() throws Exception {
		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		IRODSAccount federatedAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory.getUserGroupAO(federatedAccount);
		String testUserGroup = "testListUsersInFederatedZone";
		String testUser = "testListUsersInFederatedZoneuser" + "#" + irodsAccount.getZone();

		UserAO userAO = accessObjectFactory.getUserAO(federatedAccount);
		userAO.deleteUser(testUser);
		userGroupAO.removeUserGroup(testUserGroup);

		User user = new User();
		user.setName(testUser);
		user.setUserType(UserTypeEnum.RODS_USER);
		userAO.addUser(user);

		UserGroup addedGroup = new UserGroup();
		addedGroup.setUserGroupName(testUserGroup);
		addedGroup.setZone(federatedAccount.getZone());
		userGroupAO.addUserGroup(addedGroup);
		userGroupAO.addUserToGroup(testUserGroup, testUser, "");

		// now go to other zone and list users in the given group
		UserGroupAO federatedUserGroupAO = accessObjectFactory.getUserGroupAO(irodsAccount);
		List<User> userGroupMembers = federatedUserGroupAO.listUserGroupMembers(testUserGroup,
				federatedAccount.getZone());
		Assert.assertFalse(userGroupMembers.isEmpty());

	}

}
