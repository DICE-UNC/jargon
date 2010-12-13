/**
 * 
 */
package org.irods.jargon.core.pub;

import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.icommandinvoke.IcommandException;
import org.irods.jargon.testutils.icommandinvoke.IcommandInvoker;
import org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext;
import org.irods.jargon.testutils.icommandinvoke.icommands.ImetaAddCommand;
import org.irods.jargon.testutils.icommandinvoke.icommands.ImetaRemoveCommand;
import org.irods.jargon.testutils.icommandinvoke.icommands.RemoveUserCommand;
import org.irods.jargon.testutils.icommandinvoke.icommands.ImetaCommand.MetaObjectType;
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

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
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
		TestCase.assertNotNull("userAO is null", userAO);
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
		TestCase.assertTrue("no users returned", users.size() > 0);

	}

	@Test
	public void testFindWhere() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		StringBuilder sb = new StringBuilder();
		sb.append(RodsGenQueryEnum.COL_USER_NAME.getName());
		sb.append(" LIKE 't%'");
		List<User> users = userAO.findWhere(sb.toString());
		irodsSession.closeSession();
		TestCase.assertTrue("no users returned", users.size() > 0);

	}

	@Test
	public void testFindWhereNoWhere() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		List<User> users = userAO.findWhere("");
		irodsSession.closeSession();
		TestCase.assertTrue("no users returned", users.size() > 0);

	}

	@Test(expected = JargonException.class)
	public void testFindWhereNullWhere() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		userAO.findWhere(null);

	}

	@Test
	public void testGetUserByIdFound() throws Exception {
		String testUserName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_USER_KEY);
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
		User user = userAO.findByName(testUserName);
		TestCase.assertEquals(testUserName, user.getName());
		User idUser = userAO.findById(user.getId());
		TestCase.assertEquals(user.getName(), idUser.getName());
		irodsSession.closeSession();
	}

	@Test
	public void testGetUserByNameFound() throws Exception {
		String testUserName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_USER_KEY);
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
		User user = userAO.findByName(testUserName);
		TestCase.assertEquals(testUserName, user.getName());
		irodsSession.closeSession();

	}

	@Test
	public void testAddUser() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUser = "addUserTestUser";
		String testComment = "testComment";
		String testInfo = "testInfo";

		// setup, delete user if it exists

		try {
			RemoveUserCommand command = new RemoveUserCommand();
			command.setUserName(testUser);
			IrodsInvocationContext invocationContext = testingPropertiesHelper
					.buildIRODSInvocationContextFromTestProperties(testingProperties);
			IcommandInvoker invoker = new IcommandInvoker(invocationContext);
			String result = invoker.invokeCommandAndGetResultAsString(command);
		} catch (IcommandException ice) {
			// ignore exception, user may not exist
		}

		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
		User addedUser = new User();
		addedUser.setName(testUser);
		addedUser.setUserType(UserTypeEnum.RODS_USER);
		userAO.addUser(addedUser);

		User actualUser = userAO.findByName(addedUser.getName());
		irodsSession.closeSession();

		TestCase.assertNotNull("no user returned", actualUser);
	}

	@Test
	public void testUpdateUserUpdateComment() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
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
			String result = invoker.invokeCommandAndGetResultAsString(command);
		} catch (IcommandException ice) {
			// ignore exception, user may not exist
		}

		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
		User addedUser = new User();
		addedUser.setName(testUser);
		addedUser.setUserType(UserTypeEnum.RODS_USER);
		userAO.addUser(addedUser);

		User updateUser = userAO.findByName(addedUser.getName());
		updateUser.setComment(testUser);

		userAO.updateUser(updateUser);
		User updatedUser = userAO.findByName(addedUser.getName());
		irodsSession.closeSession();

		TestCase.assertEquals("should have updated comment", updateUser
				.getComment(), updatedUser.getComment());

	}

	@Test
	public void testUpdateUserUpdateInfo() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUser = "addUserUpdateInfo";

		// setup, delete user if it exists

		try {
			RemoveUserCommand command = new RemoveUserCommand();
			command.setUserName(testUser);
			IrodsInvocationContext invocationContext = testingPropertiesHelper
					.buildIRODSInvocationContextFromTestProperties(testingProperties);
			IcommandInvoker invoker = new IcommandInvoker(invocationContext);
			String result = invoker.invokeCommandAndGetResultAsString(command);
		} catch (IcommandException ice) {
			// ignore exception, user may not exist
		}

		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
		User addedUser = new User();
		addedUser.setName(testUser);
		addedUser.setUserType(UserTypeEnum.RODS_USER);
		userAO.addUser(addedUser);

		User updateUser = userAO.findByName(addedUser.getName());
		updateUser.setInfo("info for update user rrr111$%##$#%R");

		userAO.updateUser(updateUser);
		User updatedUser = userAO.findByName(addedUser.getName());
		irodsSession.closeSession();

		TestCase.assertEquals("should have updated info", updateUser.getInfo(),
				updatedUser.getInfo());

	}

	@Test
	public void testUpdateUserUpdateZone() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUser = "addUserUpdatedZone";

		// setup, delete user if it exists

		try {
			RemoveUserCommand command = new RemoveUserCommand();
			command.setUserName(testUser);
			IrodsInvocationContext invocationContext = testingPropertiesHelper
					.buildIRODSInvocationContextFromTestProperties(testingProperties);
			IcommandInvoker invoker = new IcommandInvoker(invocationContext);
			String result = invoker.invokeCommandAndGetResultAsString(command);
		} catch (IcommandException ice) {
			// ignore exception, user may not exist
		}

		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
		User addedUser = new User();
		addedUser.setName(testUser);
		addedUser.setUserType(UserTypeEnum.RODS_USER);
		userAO.addUser(addedUser);

		User updateUser = userAO.findByName(addedUser.getName());
		updateUser.setZone(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_ZONE_KEY));

		userAO.updateUser(updateUser);
		User updatedUser = userAO.findByName(addedUser.getName());
		irodsSession.closeSession();

		TestCase.assertEquals("should have updated zone", updateUser.getZone(),
				updatedUser.getZone());

	}

	@Test
	public void testUpdateUserUpdateInfoCommentAndZone() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUser = "addUserUpdatedInfoCommentZone";

		// setup, delete user if it exists

		try {
			RemoveUserCommand command = new RemoveUserCommand();
			command.setUserName(testUser);
			IrodsInvocationContext invocationContext = testingPropertiesHelper
					.buildIRODSInvocationContextFromTestProperties(testingProperties);
			IcommandInvoker invoker = new IcommandInvoker(invocationContext);
			String result = invoker.invokeCommandAndGetResultAsString(command);
		} catch (IcommandException ice) {
			// ignore exception, user may not exist
		}

		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
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
		irodsSession.closeSession();

		TestCase.assertEquals("should have updated zone", updateUser.getZone(),
				updatedUser.getZone());
		TestCase.assertEquals("should have updated info", updateUser.getInfo(),
				updatedUser.getInfo());
		TestCase.assertEquals("should have updated comment", updateUser
				.getComment(), updatedUser.getComment());

	}

	@Test
	public void testUpdateUserUpdateInfoCommentAndZoneWhenNullZone()
			throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUser = "addUserUpdatedInfoCommentZoneNullZone";

		// setup, delete user if it exists

		try {
			RemoveUserCommand command = new RemoveUserCommand();
			command.setUserName(testUser);
			IrodsInvocationContext invocationContext = testingPropertiesHelper
					.buildIRODSInvocationContextFromTestProperties(testingProperties);
			IcommandInvoker invoker = new IcommandInvoker(invocationContext);
			String result = invoker.invokeCommandAndGetResultAsString(command);
		} catch (IcommandException ice) {
			// ignore exception, user may not exist
		}

		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
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
		irodsSession.closeSession();

		TestCase.assertTrue("should have caught exception before updates",
				threwException);
		TestCase.assertEquals("should not have updated zone", addedUser
				.getZone(), updatedUser.getZone());
		TestCase.assertEquals("should not have updated info", addedUser
				.getInfo(), updatedUser.getInfo());
		TestCase.assertEquals("should not have updated comment", addedUser
				.getComment(), updatedUser.getComment());

	}

	@Test
	public void testDeleteUser() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUser = "deleteUserTestUser";

		// setup, delete user if it exists

		try {
			RemoveUserCommand command = new RemoveUserCommand();
			command.setUserName(testUser);
			IrodsInvocationContext invocationContext = testingPropertiesHelper
					.buildIRODSInvocationContextFromTestProperties(testingProperties);
			IcommandInvoker invoker = new IcommandInvoker(invocationContext);
			String result = invoker.invokeCommandAndGetResultAsString(command);
		} catch (IcommandException ice) {
			// ignore exception, user may not exist
		}

		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
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

		irodsSession.closeSession();
		TestCase
				.assertFalse(
						"i should not have found the user, it was supposed to be deleted",
						found);

	}

	@Test
	public void testDeleteNonExistentUser() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUser = "deleteUserThatDoesNotExist";
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
		userAO.deleteUser(testUser);
		irodsSession.closeSession();
	}

	@Test(expected = DuplicateDataException.class)
	public void testAddDuplicateUser() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUser = "addUserDuplicateUser";

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

		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
		User addedUser = new User();
		addedUser.setName(testUser);
		addedUser.setUserType(UserTypeEnum.RODS_USER);
		userAO.addUser(addedUser);
		userAO.addUser(addedUser);
	}

	@Test
	public void testListUserMetadata() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
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

		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		User user = userAO.findByName(irodsAccount.getUserName());

		List<AvuData> avuList = userAO.listUserMetadata(user.getId());

		irodsSession.closeSession();

		invoker.invokeCommandAndGetResultAsString(imr);

		TestCase.assertNotNull("null avu data returned", avuList);
		TestCase.assertFalse("no avus returned", avuList.isEmpty());
	}

	// FIXME: see clientLogin.c and iadmin.c(line 807) for details yet to be implemented

	@Ignore
	public void testChangeUserPassword() throws Exception {
		String testUser = "testChangeUserPassword";
		String password1 ="password1";
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
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		UserAO adminUserAO = irodsFileSystem.getIRODSAccessObjectFactory().getUserAO(irodsAccount);
		
		// pre-clean, remove testing user if there
		//adminUserAO.deleteUser(testUser);
		
		// now add user
		//adminUserAO.addUser(user);
		
		// set the first password
		//adminUserAO.changeAUserPasswordByAnAdmin(testUser, password1);
		
		// get an account as the given user, change password, re-LOG in several iterations
		
		IRODSAccount userAccount = testingPropertiesHelper.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(testingProperties, testUser, password1);
		UserAO myUserAO = irodsFileSystem.getIRODSAccessObjectFactory().getUserAO(userAccount);
		myUserAO.changeAUserPasswordByThatUser(testUser, password1, password2);
		
		userAccount = testingPropertiesHelper.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(testingProperties, testUser, password2);
		myUserAO = irodsFileSystem.getIRODSAccessObjectFactory().getUserAO(userAccount);
		myUserAO.changeAUserPasswordByThatUser(testUser, password2, password3);
			
		userAccount = testingPropertiesHelper.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(testingProperties, testUser, password3);
		myUserAO = irodsFileSystem.getIRODSAccessObjectFactory().getUserAO(userAccount);
		myUserAO.changeAUserPasswordByThatUser(testUser, password3, password4);

		userAccount = testingPropertiesHelper.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(testingProperties, testUser, password4);
		myUserAO = irodsFileSystem.getIRODSAccessObjectFactory().getUserAO(userAccount);
		
		// now clean up as an admin
		
		adminUserAO.deleteUser(testUser);
		irodsFileSystem.closeAndEatExceptions();
	}
	
	
}
