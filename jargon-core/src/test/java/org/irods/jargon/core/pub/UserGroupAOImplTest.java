package org.irods.jargon.core.pub;

import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.pub.domain.UserGroup;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
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
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		UserGroupAO userGroupAO = accessObjectFactory
				.getUserGroupAO(irodsAccount);
		Assert.assertNotNull("userGroupAO is null", userGroupAO);
		irodsSession.closeSession();
	}

	@Test
	public final void testFind() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		UserGroupAO userGroupAO = accessObjectFactory
				.getUserGroupAO(irodsAccount);
		UserGroup expectedUserGroup = userGroupAO.findByName("rodsadmin");
		UserGroup actualUserGroup = userGroupAO.find(expectedUserGroup
				.getUserGroupId());
		irodsSession.closeSession();
		Assert.assertNotNull("no user group returned", actualUserGroup);
		Assert.assertEquals("unexpected user group",
				expectedUserGroup.getUserGroupName(),
				actualUserGroup.getUserGroupName());
	}

	@Test
	public final void testFindByName() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		UserGroupAO userGroupAO = accessObjectFactory
				.getUserGroupAO(irodsAccount);
		UserGroup userGroup = userGroupAO.findByName("rodsadmin");
		irodsSession.closeSession();
		Assert.assertNotNull("no user group returned", userGroup);
		Assert.assertEquals("unexpected user group", "rodsadmin",
				userGroup.getUserGroupName());

	}
	
	@Test
	public final void testFindByNameNotExists() throws Exception {
		
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory
				.getUserGroupAO(irodsAccount);
		UserGroup userGroup = userGroupAO.findByName("i dont exist here");
		Assert.assertNull("user group returned, there should not be one", userGroup);
		
	}

	@Test
	public final void testFindWhere() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		UserGroupAO userGroupAO = accessObjectFactory
				.getUserGroupAO(irodsAccount);

		StringBuilder query = new StringBuilder();
		query.append(RodsGenQueryEnum.COL_USER_GROUP_NAME.getName());
		query.append(" = '");
		query.append("rodsadmin");
		query.append("'");

		List<UserGroup> userGroup = userGroupAO.findWhere(query.toString());
		irodsSession.closeSession();
		Assert.assertNotNull("no user group returned", userGroup);
		Assert.assertTrue("no user group returned for query",
				userGroup.size() == 1);
	}

	@Test
	public final void testFindUserGroupsForUser() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		UserGroupAO userGroupAO = accessObjectFactory
				.getUserGroupAO(irodsAccount);

		List<UserGroup> userGroup = userGroupAO
				.findUserGroupsForUser(testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_USER_KEY));
		irodsSession.closeSession();
		Assert.assertNotNull("no user group returned", userGroup);
		Assert.assertTrue("no user group returned for query",
				userGroup.size() > 0);

	}
	
	@Test
	public final void testAddUserGroup() throws Exception {
		
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);		
		String testUserGroup = "testAddUserGroup";
		
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory
				.getUserGroupAO(irodsAccount);
		
		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroup);
		userGroup.setZone(irodsAccount.getZone());
		
		userGroupAO.removeUserGroup(userGroup);
		userGroupAO.addUserGroup(userGroup);
		
		UserGroup actual = userGroupAO.findByName(testUserGroup);
		Assert.assertNotNull("no user group returned", actual);
		Assert.assertEquals("user group has wrong name", testUserGroup,
				userGroup.getUserGroupName());
		
		userGroupAO.removeUserGroup(userGroup);
	}
	
	@Test
	public final void testRemoveUserGroup() throws Exception {
		
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);		
		String testUserGroup = "testRemoveUserGroup";
		
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory
				.getUserGroupAO(irodsAccount);
		
		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroup);
		userGroup.setZone(irodsAccount.getZone());
		
		userGroupAO.removeUserGroup(userGroup);
		userGroupAO.addUserGroup(userGroup);
		userGroupAO.removeUserGroup(userGroup);

		UserGroup actual = userGroupAO.findByName(testUserGroup);
		Assert.assertNull("user group returned", actual);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public final void testAddUserGroupNullGroupName() throws Exception {
		
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);		
		String testUserGroup = null;
		
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory
				.getUserGroupAO(irodsAccount);
		
		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroup);
		userGroup.setZone(irodsAccount.getZone());
		
		userGroupAO.addUserGroup(userGroup);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public final void testAddUserGroupBlankGroupName() throws Exception {
		
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);		
		String testUserGroup = "";
		
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory
				.getUserGroupAO(irodsAccount);
		
		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroup);
		userGroup.setZone(irodsAccount.getZone());
		
		userGroupAO.addUserGroup(userGroup);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public final void testAddUserGroupNoZone() throws Exception {
		
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);		
		String testUserGroup = "testAddUserGroupNoZone";
		
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory
				.getUserGroupAO(irodsAccount);
		
		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroup);
		
		userGroupAO.addUserGroup(userGroup);
		
	}
	
	@Test(expected=DuplicateDataException.class)
	public final void testAddDuplicateUserGroup() throws Exception {
		
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);		
		String testUserGroup = "testAddDuplicateUserGroup";
		
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		UserGroupAO userGroupAO = accessObjectFactory
				.getUserGroupAO(irodsAccount);
		
		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroup);
		userGroup.setZone(irodsAccount.getZone());
		
		userGroupAO.removeUserGroup(userGroup);
		userGroupAO.addUserGroup(userGroup);
		userGroupAO.addUserGroup(userGroup);
	
	}

}
