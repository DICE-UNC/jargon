/**
 * 
 */
package org.irods.jargon.core.pub;

import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.NoAPIPrivException;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.icommandinvoke.IcommandException;
import org.irods.jargon.testutils.icommandinvoke.IcommandInvoker;
import org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext;
import org.irods.jargon.testutils.icommandinvoke.icommands.ImetaAddCommand;
import org.irods.jargon.testutils.icommandinvoke.icommands.ImetaCommand.MetaObjectType;
import org.irods.jargon.testutils.icommandinvoke.icommands.ImetaRemoveCommand;
import org.irods.jargon.testutils.icommandinvoke.icommands.RemoveUserCommand;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class UserAOTest {

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
	public void testGetUserAO() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
		Assert.assertNotNull("userAO is null", userAO);
		irodsSession.closeSession();
	}

	@Test
	public void testListUsers() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
		List<User> users = userAO.findAll();
		irodsSession.closeSession();
		Assert.assertTrue("no users returned", users.size() > 0);
	}

	@Test
	public void testFindWhere() throws Exception {

		String testUserName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_USER_KEY);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		StringBuilder sb = new StringBuilder();
		sb.append(RodsGenQueryEnum.COL_USER_NAME.getName());
		sb.append(" LIKE '");
		sb.append(testUserName.charAt(0));
		sb.append("%'");
		List<User> users = userAO.findWhere(sb.toString());
		Assert.assertTrue("no users returned", users.size() > 0);

	}

	@Test
	public void testFindWhereNoWhere() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		List<User> users = userAO.findWhere("");
		Assert.assertTrue("no users returned", users.size() > 0);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testFindWhereNullWhere() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
		userAO.findWhere(null);

	}

	@Test
	public void testGetUserByIdFound() throws Exception {
		String testUserName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_USER_KEY);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
		User user = userAO.findByName(testUserName);
		Assert.assertEquals(testUserName, user.getName());
		User idUser = userAO.findById(user.getId());
		Assert.assertEquals(user.getName(), idUser.getName());
	}

	@Test
	public void testGetUserByNameFound() throws Exception {
		String testUserName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_USER_KEY);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
		User user = userAO.findByName(testUserName);
		Assert.assertEquals(testUserName, user.getName());

	}

	@Test
	public void testAddUser() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUser = "addUserTestUser";
		try {
			RemoveUserCommand command = new RemoveUserCommand();
			command.setUserName(testUser);
			IrodsInvocationContext invocationContext = testingPropertiesHelper
					.buildIRODSInvocationContextFromTestProperties(testingProperties);
			IcommandInvoker invoker = new IcommandInvoker(invocationContext);
			invoker.invokeCommandAndGetResultAsString(command);
		} catch (IcommandException ice) {
			// ignore exception, user may not exist
		}

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
		User addedUser = new User();
		addedUser.setName(testUser);
		addedUser.setUserType(UserTypeEnum.RODS_USER);
		userAO.addUser(addedUser);

		User actualUser = userAO.findByName(addedUser.getName());

		Assert.assertNotNull("no user returned", actualUser);
	}

	@Test
	public void testUpdateUserUpdateComment() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUser = "addUserUpdateComment";

		// setup, delete user if it exists

		try {
			RemoveUserCommand command = new RemoveUserCommand();
			command.setUserName(testUser);
			IrodsInvocationContext invocationContext = testingPropertiesHelper
					.buildIRODSInvocationContextFromTestProperties(testingProperties);
			IcommandInvoker invoker = new IcommandInvoker(invocationContext);
			invoker.invokeCommandAndGetResultAsString(command);
		} catch (IcommandException ice) {
			// ignore exception, user may not exist
		}

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
		User addedUser = new User();
		addedUser.setName(testUser);
		addedUser.setUserType(UserTypeEnum.RODS_USER);
		userAO.addUser(addedUser);

		User updateUser = userAO.findByName(addedUser.getName());
		updateUser.setComment(testUser);

		userAO.updateUser(updateUser);
		User updatedUser = userAO.findByName(addedUser.getName());

		Assert.assertEquals("should have updated comment",
				updateUser.getComment(), updatedUser.getComment());

	}

	@Test
	public void testUpdateUserUpdateInfo() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUser = "addUserUpdateInfoTest";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		// setup, delete user if it exists

		try {
			userAO.deleteUser(testUser);
		} catch (Exception e) {
			// ignore exception, user may not exist
		}

		User addedUser = new User();
		addedUser.setName(testUser);
		addedUser.setUserType(UserTypeEnum.RODS_USER);
		userAO.addUser(addedUser);

		User updateUser = userAO.findByName(addedUser.getName());
		updateUser.setInfo("info for update user rrr111$%##$#%R");

		userAO.updateUser(updateUser);
		User updatedUser = userAO.findByName(addedUser.getName());

		Assert.assertEquals("should have updated info", updateUser.getInfo(),
				updatedUser.getInfo());

	}

	@Test
	public void testUpdateUserInfo() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUser = "testUpdateUserInfo";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		// setup, delete user if it exists

		try {
			userAO.deleteUser(testUser);
		} catch (Exception e) {
			// ignore exception, user may not exist
		}

		User addedUser = new User();
		addedUser.setName(testUser);
		addedUser.setUserType(UserTypeEnum.RODS_USER);
		userAO.addUser(addedUser);

		userAO.updateUserInfo(testUser, testUser);

		User updatedUser = userAO.findByName(addedUser.getName());

		Assert.assertEquals("should have updated info", testUser,
				updatedUser.getInfo());
	}

	@Test
	public void testUpdateUserInfoBlank() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUser = "testUpdateUserInfoBlank";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		// setup, delete user if it exists

		try {
			userAO.deleteUser(testUser);
		} catch (Exception e) {
			// ignore exception, user may not exist
		}

		User addedUser = new User();
		addedUser.setName(testUser);
		addedUser.setUserType(UserTypeEnum.RODS_USER);
		userAO.addUser(addedUser);

		userAO.updateUserInfo(testUser, "");

		User updatedUser = userAO.findByName(addedUser.getName());

		Assert.assertEquals("should have updated info", "",
				updatedUser.getInfo());
	}

	@Test(expected = DataNotFoundException.class)
	public void testUpdateUserInfoNotFound() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUser = "testUpdateUserInfoNotFound";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		userAO.updateUserInfo(testUser, testUser);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUpdateUserInfoNullUser() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUser = null;

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		userAO.updateUserInfo(testUser, testUser);
	}

	@Test
	public void testUpdateUserUpdateZone() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUser = "addUserUpdatedZone";
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		try {
			userAO.deleteUser(testUser);
		} catch (Exception e) {
			// ignore exception, user may not exist
		}
		User addedUser = new User();
		addedUser.setName(testUser);
		addedUser.setUserType(UserTypeEnum.RODS_USER);
		userAO.addUser(addedUser);

		User updateUser = userAO.findByName(addedUser.getName());
		updateUser.setZone(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_ZONE_KEY));

		userAO.updateUser(updateUser);
		User updatedUser = userAO.findByName(addedUser.getName());

		Assert.assertEquals("should have updated zone", updateUser.getZone(),
				updatedUser.getZone());

	}

	@Test
	public void testUpdateUserUpdateInfoCommentAndZone() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUser = "addUserUpdatedInfoCommentZone";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		try {
			userAO.deleteUser(testUser);
		} catch (Exception e) {
			// ignore exception, user may not exist
		}
		User addedUser = new User();
		addedUser.setName(testUser);
		addedUser.setUserType(UserTypeEnum.RODS_USER);
		userAO.addUser(addedUser);

		User updateUser = userAO.findByName(addedUser.getName());
		updateUser.setZone(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_ZONE_KEY));
		updateUser.setComment("updatedComment");
		updateUser.setInfo("updatedInfo");

		userAO.updateUser(updateUser);
		User updatedUser = userAO.findByName(addedUser.getName());

		Assert.assertEquals("should have updated zone", updateUser.getZone(),
				updatedUser.getZone());
		Assert.assertEquals("should have updated info", updateUser.getInfo(),
				updatedUser.getInfo());
		Assert.assertEquals("should have updated comment",
				updateUser.getComment(), updatedUser.getComment());

	}

	@Test
	public void testUpdateUserUpdateInfoCommentAndZoneWhenNullZone()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUser = "addUserUpdatedInfoCommentZoneNullZone";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		try {
			userAO.deleteUser(testUser);
		} catch (Exception e) {
			// ignore exception, user may not exist
		}
		User addedUser = new User();

		addedUser.setName(testUser);
		addedUser.setUserType(UserTypeEnum.RODS_USER);
		addedUser.setZone(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_ZONE_KEY));
		addedUser.setComment("orig");
		addedUser.setInfo("orig");

		userAO.addUser(addedUser);

		User updateUser = userAO.findByName(addedUser.getName());
		updateUser.setZone(null);
		updateUser.setComment("updatedComment");
		updateUser.setInfo("updatedInfo");

		boolean threwException = false;

		try {
			userAO.updateUser(updateUser);
		} catch (JargonException je) {
			threwException = true;
		}

		User updatedUser = userAO.findByName(addedUser.getName());

		Assert.assertTrue("should have caught exception before updates",
				threwException);
		Assert.assertEquals("should not have updated zone",
				addedUser.getZone(), updatedUser.getZone());
		Assert.assertEquals("should not have updated info",
				addedUser.getInfo(), updatedUser.getInfo());
		Assert.assertEquals("should not have updated comment",
				addedUser.getComment(), updatedUser.getComment());

	}

	@Test
	public void testDeleteUser() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUser = "deleteUserTestUser";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		try {
			userAO.deleteUser(testUser);
		} catch (Exception e) {
			// ignore exception, user may not exist
		}

		User addedUser = new User();
		addedUser.setName(testUser);
		addedUser.setUserType(UserTypeEnum.RODS_USER);
		userAO.addUser(addedUser);

		userAO.deleteUser(testUser);

		boolean found = true;
		try {
			userAO.findByName(testUser);
		} catch (Exception e) {
			// ok, shoud not find
			found = false;
		}

		Assert.assertFalse(
				"i should not have found the user, it was supposed to be deleted",
				found);
	}

	@Test(expected = DataNotFoundException.class)
	public void testDeleteNonExistentUser() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAdminAccountFromTestProperties(testingProperties);

		String testUser = "deleteUserThatDoesNotExist";
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
		userAO.deleteUser(testUser);

		// look up should not be there and will return anticipated exception

		userAO.findByName(testUser);
	}

	@Test(expected = DuplicateDataException.class)
	public void testAddDuplicateUser() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUser = "addUserDuplicateUser";

		// setup, delete user if it exists

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		try {
			userAO.deleteUser(testUser);
		} catch (Exception e) {
			// ignore exception, user may not exist
		}
		User addedUser = new User();
		addedUser.setName(testUser);
		addedUser.setUserType(UserTypeEnum.RODS_USER);
		userAO.addUser(addedUser);
		userAO.addUser(addedUser);
	}

	@Test
	public void testListUserMetadataForId() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);

		String meta1Attrib = "attr1";
		String meta1Value = "c";

		ImetaRemoveCommand imr = new ImetaRemoveCommand();
		imr.setAttribName(meta1Attrib);
		imr.setMetaObjectType(MetaObjectType.USER_META);
		imr.setAttribValue(meta1Value);
		imr.setObjectPath(irodsAccount.getUserName());
		invoker.invokeCommandAndGetResultAsString(imr);

		ImetaAddCommand metaAddCommand = new ImetaAddCommand();
		metaAddCommand.setAttribName(meta1Attrib);
		metaAddCommand.setAttribValue(meta1Value);
		metaAddCommand.setMetaObjectType(MetaObjectType.USER_META);
		metaAddCommand.setObjectPath(irodsAccount.getUserName());
		invoker.invokeCommandAndGetResultAsString(metaAddCommand);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		User user = userAO.findByName(irodsAccount.getUserName());

		List<AvuData> avuList = userAO.listUserMetadataForUserId(user.getId());

		invoker.invokeCommandAndGetResultAsString(imr);

		Assert.assertNotNull("null avu data returned", avuList);
		Assert.assertFalse("no avus returned", avuList.isEmpty());
	}

	@Test
	public void testListUserMetadataForUserName() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);

		String meta1Attrib = "unattr1";
		String meta1Value = "unc";

		ImetaRemoveCommand imr = new ImetaRemoveCommand();
		imr.setAttribName(meta1Attrib);
		imr.setMetaObjectType(MetaObjectType.USER_META);
		imr.setAttribValue(meta1Value);
		imr.setObjectPath(irodsAccount.getUserName());
		invoker.invokeCommandAndGetResultAsString(imr);

		ImetaAddCommand metaAddCommand = new ImetaAddCommand();
		metaAddCommand.setAttribName(meta1Attrib);
		metaAddCommand.setAttribValue(meta1Value);
		metaAddCommand.setMetaObjectType(MetaObjectType.USER_META);
		metaAddCommand.setObjectPath(irodsAccount.getUserName());
		invoker.invokeCommandAndGetResultAsString(metaAddCommand);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		List<AvuData> avuList = userAO.listUserMetadataForUserName(irodsAccount
				.getUserName());

		invoker.invokeCommandAndGetResultAsString(imr);

		Assert.assertNotNull("null avu data returned", avuList);
		Assert.assertFalse("no avus returned", avuList.isEmpty());
	}

	// FIXME: see clientLogin.c and iadmin.c(line 807) for details yet to be
	// implemented

	@Test
	public void testChangeUserPassword() throws Exception {
		String testUser = "testx";
		String password1 = "test";
		String password2 = "PaZz!word123";
		String password3 = "p@ssw000000000000000000000000000000000rd3";
		String password4 = "$paZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZw**&rd";

		// add the user as a rodsadmin

		User user = new User();
		user.setComment("comment");
		user.setInfo("info");
		user.setName(testUser);
		user.setUserType(UserTypeEnum.RODS_USER);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		UserAO adminUserAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getUserAO(irodsAccount);

		// pre-clean, remove testing user if there
		adminUserAO.deleteUser(testUser);

		// now add user
		adminUserAO.addUser(user);

		// set the first password
		adminUserAO.changeAUserPasswordByAnAdmin(testUser, password1);

		// get an account as the given user, change password, re-log in several
		// iterations

		IRODSAccount userAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUser, password1);
		UserAO myUserAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getUserAO(userAccount);
		myUserAO.changeAUserPasswordByThatUser(testUser, password1, password2);

		userAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUser, password2);
		myUserAO = irodsFileSystem.getIRODSAccessObjectFactory().getUserAO(
				userAccount);
		myUserAO.changeAUserPasswordByThatUser(testUser, password2, password3);

		userAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUser, password3);
		myUserAO = irodsFileSystem.getIRODSAccessObjectFactory().getUserAO(
				userAccount);
		myUserAO.changeAUserPasswordByThatUser(testUser, password3, password4);

		userAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUser, password4);
		myUserAO = irodsFileSystem.getIRODSAccessObjectFactory().getUserAO(
				userAccount);

		// now clean up as an admin

		adminUserAO.deleteUser(testUser);
	}

	@Test
	public void testAddUserMetadata() throws Exception {

		String testAttrib = "testAddUserMetadataAttrib";
		String testValue = "testAddUserMetadataValue";
		String testUnit = "";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		UserAO userAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getUserAO(irodsAccount);

		AvuData avuData = AvuData.instance(testAttrib, testValue, testUnit);
		userAO.deleteAVUMetadata(irodsAccount.getUserName(), avuData);

		userAO.addAVUMetadata(irodsAccount.getUserName(), avuData);
		List<AvuData> avuList = userAO.listUserMetadataForUserName(irodsAccount
				.getUserName());

		boolean avuFound = false;

		for (AvuData actualAvuData : avuList) {
			if (actualAvuData.getAttribute().equals(testAttrib)
					&& actualAvuData.getValue().equals(testValue)) {
				avuFound = true;
				break;
			}
		}

		Assert.assertTrue("did not find the expected AVU", avuFound);

	}

	@Test(expected = JargonException.class)
	public void testAddUserMetadataByNonAdminUser() throws Exception {

		String testAttrib = "testAddUserMetadataByNonAdminUserAttrib";
		String testValue = "testAddUserMetadataByNonAdminUserValue";
		String testUnit = "";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		UserAO userAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getUserAO(irodsAccount);

		AvuData avuData = AvuData.instance(testAttrib, testValue, testUnit);

		userAO.addAVUMetadata(irodsAccount.getUserName(), avuData);

	}

	@Test
	public void testDeleteUserMetadata() throws Exception {

		String testAttrib = "testDeleteUserMetadataAttrib";
		String testValue = "testDeleteUserMetadataValue";
		String testUnit = "";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		UserAO userAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getUserAO(irodsAccount);

		AvuData avuData = AvuData.instance(testAttrib, testValue, testUnit);

		userAO.addAVUMetadata(irodsAccount.getUserName(), avuData);

		userAO.deleteAVUMetadata(irodsAccount.getUserName(), avuData);

		List<AvuData> avuList = userAO.listUserMetadataForUserName(irodsAccount
				.getUserName());

		boolean avuFound = false;

		for (AvuData actualAvuData : avuList) {
			if (actualAvuData.getAttribute().equals(testAttrib)
					&& actualAvuData.getValue().equals(testValue)) {
				avuFound = true;
				break;
			}
		}

		Assert.assertFalse("found the expected AVU, should have been deleted",
				avuFound);

	}

	/*
	 * currently ignored for [#151] mod avu 816000 protocol errors
	 */
	@Ignore
	public void testModifyUserMetadata() throws Exception {

		String testAttrib = "testModifyUserMetadataAttrib";
		String testValue = "testModifyUserMetadataValue";
		String testUnit = "";

		String testValueUpdate = "testModifyUserMetadataValueUpdate";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		UserAO userAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getUserAO(irodsAccount);

		AvuData avuData = AvuData.instance(testAttrib, testValue, testUnit);

		userAO.addAVUMetadata(irodsAccount.getUserName(), avuData);

		avuData = AvuData.instance(testAttrib, testValueUpdate, testUnit);

		userAO.modifyAVUMetadata(irodsAccount.getUserName(), avuData);

		List<AvuData> avuList = userAO.listUserMetadataForUserName(irodsAccount
				.getUserName());

		boolean avuFound = false;

		for (AvuData actualAvuData : avuList) {
			if (actualAvuData.getAttribute().equals(testAttrib)
					&& actualAvuData.getValue().equals(testValueUpdate)) {
				avuFound = true;
				break;
			}
		}

		Assert.assertTrue("did not find the expected modified AVU", avuFound);

	}

	@Test(expected = JargonException.class)
	public void testAddUserMetadataInvalidUser() throws Exception {

		String testAttrib = "testDeleteUserMetadataAttrib";
		String testValue = "testDeleteUserMetadataValue";
		String testUnit = "";
		String testUser = "xxxxxxx";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		UserAO userAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getUserAO(irodsAccount);

		AvuData avuData = AvuData.instance(testAttrib, testValue, testUnit);
		userAO.addAVUMetadata(testUser, avuData);

	}

	@Test(expected = JargonException.class)
	public void testDeleteUserMetadataInvalidUser() throws Exception {

		String testAttrib = "testDeleteUserMetadataAttrib";
		String testValue = "testDeleteUserMetadataValue";
		String testUnit = "";
		String testUser = "xxxxxxx";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		UserAO userAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getUserAO(irodsAccount);

		AvuData avuData = AvuData.instance(testAttrib, testValue, testUnit);
		userAO.deleteAVUMetadata(testUser, avuData);

	}

	@Test
	public void testAddUserMetadataTwice() throws Exception {

		String testAttrib = "testAddUserMetadataTwiceAttrib";
		String testValue = "testAddUserMetadataTwiceValue";
		String testUnit = "";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		UserAO userAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getUserAO(irodsAccount);

		AvuData avuData = AvuData.instance(testAttrib, testValue, testUnit);

		try {
			userAO.addAVUMetadata(irodsAccount.getUserName(), avuData);
			userAO.addAVUMetadata(irodsAccount.getUserName(), avuData);

			List<AvuData> avuList = userAO
					.listUserMetadataForUserName(irodsAccount.getUserName());

			boolean avuFound = false;

			for (AvuData actualAvuData : avuList) {
				if (actualAvuData.getAttribute().equals(testAttrib)
						&& actualAvuData.getValue().equals(testValue)) {
					avuFound = true;
					break;
				}
			}

			Assert.assertTrue("did not find the expected AVU", avuFound);
		} catch (DuplicateDataException dde) {
			// this is post 3.1, will get the dde and that's expected
			return;
		}

	}

	@Test
	public void testFindUserNameWhereUserNameLike() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		List<String> users = userAO.findUserNameLike("t");
		irodsSession.closeSession();
		Assert.assertTrue("no users returned", users.size() > 0);

	}

	@Test
	public void testFindUserNameWhereUserNameLikeNoResultExpected()
			throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		List<String> users = userAO.findUserNameLike("zzzzzazzzz848848djdfajf");
		irodsSession.closeSession();
		Assert.assertFalse("should be no users returned", users.size() > 0);

	}

	@Test
	public void testFindUserNameWhereUserNameLikeUserNameSpaces()
			throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		List<String> users = userAO.findUserNameLike("");
		irodsSession.closeSession();
		Assert.assertTrue("no users returned", users.size() > 0);

	}

	@Test
	public void testGenerateTempPassword() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		String tempPassword = userAO.getTemporaryPasswordForConnectedUser();
		Assert.assertNotNull("null temp password", tempPassword);

		IRODSAccount account = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, irodsAccount.getUserName(),
						tempPassword);
		irodsFileSystem.closeAndEatExceptions();
		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(account);
		Assert.assertNotNull("did not connect and get environmental info",
				environmentalInfoAO.getIRODSServerProperties());

	}

	/**
	 * As a rods admin, generate a password on behalf of a normal test user
	 * 
	 * @throws Exception
	 */
	@Ignore
	// wait for next iRODS release
	public void testGenerateTempPasswordForAnotherUserAsRodsAdmin()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAdminAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
		String tempUserName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_USER_KEY);

		String tempPassword = userAO
				.getTemporaryPasswordForASpecifiedUser(tempUserName);
		Assert.assertNotNull("null temp password", tempPassword);

		IRODSAccount account = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, tempUserName, tempPassword);
		irodsFileSystem.closeAndEatExceptions();
		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(account);
		Assert.assertNotNull("did not connect and get environmental info",
				environmentalInfoAO.getIRODSServerProperties());

	}

	/**
	 * Generate a password on behalf of a normal test user. In this case, I am
	 * not rodsadmin and not privileged. This should cause an error
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGenerateTempPasswordForAnotherUserWhenNotRodsAdmin()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTertiaryTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
		String tempUserName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY);

		boolean gotException = false;

		try {
			userAO.getTemporaryPasswordForASpecifiedUser(tempUserName);
		} catch (UnsupportedOperationException uoe) {
			// being called on a version prior to 3.1
			return;
		} catch (NoAPIPrivException ne) {
			gotException = true;
		}

		Assert.assertTrue("did not get expected API priv exception",
				gotException);

	}
}
