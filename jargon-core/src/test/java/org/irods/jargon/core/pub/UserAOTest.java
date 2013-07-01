/**
 * 
 */
package org.irods.jargon.core.pub;

import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSAccount.AuthScheme;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.NoAPIPrivException;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.testutils.TestingPropertiesHelper;
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
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
		Assert.assertNotNull("userAO is null", userAO);
	}

	@Test
	public void testListUsers() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
		List<User> users = userAO.findAll();
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

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
		String testUser = "addUserTestUser";
		userAO.deleteUser(testUser);

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

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String testUser = "addUserUpdateComment";

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
		userAO.deleteUser(testUser);

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
	public void testRemoveUserDNBadUser() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUser = "testRemoveUserDNBadUser";
		String testDN = "testRemoveUserDNBadUser";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		// setup, delete user if it exists

		try {
			userAO.deleteUser(testUser);
		} catch (Exception e) {
			// ignore exception, user may not exist
		}

		userAO.removeUserDN(testUser, testDN);

	}

	@Test(expected = InvalidUserException.class)
	public void testAddUserDNBadUser() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUser = "testAddUserDNBadUser";
		String testDN = "testAddUserDNBadUserValue";

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		// setup, delete user if it exists

		try {
			userAO.deleteUser(testUser);
		} catch (Exception e) {
			// ignore exception, user may not exist
		}

		userAO.updateUserDN(testUser, testDN);

	}

	@Test
	public void testAddUserDN() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUser = "testAddUserDN";
		String testDN = "testAddUserDNValue";

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

		userAO.updateUserDN(testUser, testDN);

		User actual = userAO.findByName(testUser);

		Assert.assertEquals(testDN, actual.getUserDN());

		userAO.removeUserDN(testUser, testDN);

		actual = userAO.findByName(testUser);

		Assert.assertEquals("", actual.getUserDN());

	}

	@Test
	public void testAddUserDNByAddUser() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUser = "testAddUserDNByAddUser";
		String testDN = "testAddUserDNByAddUser";

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
		addedUser.setUserDN(testDN);
		userAO.addUser(addedUser);

		User actual = userAO.findByName(testUser);

		Assert.assertEquals(testDN, actual.getUserDN());

	}

	@Test
	public void testRemoveUserDNByUpdateUser() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUser = "testRemoveUserDNByUpdateUser";
		String testDN = "testRemoveUserDNByUpdateUser";

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
		addedUser.setUserDN(testDN);
		userAO.addUser(addedUser);

		addedUser = userAO.findByName(testUser);
		addedUser.setUserDN("");
		userAO.updateUser(addedUser);

		User actual = userAO.findByName(testUser);

		Assert.assertEquals("", actual.getUserDN());

	}

	@Test
	public void testUpdateUserDNByUpdateUser() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUser = "testUpdateUserDNByUpdateUser";
		String testDN = "testUpdateUserDNByUpdateUser";
		String testDN2 = "testUpdateUserDNByUpdateUser2";

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
		addedUser.setUserDN(testDN);
		userAO.addUser(addedUser);

		addedUser = userAO.findByName(testUser);
		addedUser.setUserDN(testDN2);
		userAO.updateUser(addedUser);

		User actual = userAO.findByName(testUser);

		Assert.assertEquals(testDN2, actual.getUserDN());

	}

	@Test
	public void testAddUserDNByUpdateUser() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testUser = "testAddUserDNByAddUser";
		String testDN = "testAddUserDNByAddUser";

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

		addedUser = userAO.findByName(testUser);
		addedUser.setUserDN(testDN);
		userAO.updateUser(addedUser);

		User actual = userAO.findByName(testUser);

		Assert.assertEquals(testDN, actual.getUserDN());

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

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		String meta1Attrib = "attr1";
		String meta1Value = "c";

		AvuData avuData = AvuData.instance(meta1Attrib, meta1Value, "");
		userAO.deleteAVUMetadata(irodsAccount.getUserName(), avuData);
		userAO.addAVUMetadata(irodsAccount.getUserName(), avuData);

		User user = userAO.findByName(irodsAccount.getUserName());

		List<AvuData> avuList = userAO.listUserMetadataForUserId(user.getId());

		Assert.assertNotNull("null avu data returned", avuList);
		Assert.assertFalse("no avus returned", avuList.isEmpty());
	}

	@Test
	public void testListUserMetadataForUserName() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String meta1Attrib = "unattr1";
		String meta1Value = "unc";

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		AvuData avuData = AvuData.instance(meta1Attrib, meta1Value, "");
		userAO.deleteAVUMetadata(irodsAccount.getUserName(), avuData);
		userAO.addAVUMetadata(irodsAccount.getUserName(), avuData);

		List<AvuData> avuList = userAO.listUserMetadataForUserName(irodsAccount
				.getUserName());

		userAO.deleteAVUMetadata(irodsAccount.getUserName(), avuData);

		Assert.assertNotNull("null avu data returned", avuList);
		Assert.assertFalse("no avus returned", avuList.isEmpty());
	}

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

		irodsFileSystem.getIRODSAccessObjectFactory();

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
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		List<String> users = userAO.findUserNameLike("t");
		Assert.assertTrue("no users returned", users.size() > 0);

	}

	@Test
	public void testFindUserNameWhereUserNameLikeNoResultExpected()
			throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		List<String> users = userAO.findUserNameLike("zzzzzazzzz848848djdfajf");
		Assert.assertFalse("should be no users returned", users.size() > 0);

	}

	@Test
	public void testFindUserNameWhereUserNameLikeUserNameSpaces()
			throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		List<String> users = userAO.findUserNameLike("");
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
		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(account);
		Assert.assertNotNull("did not connect and get environmental info",
				environmentalInfoAO.getIRODSServerProperties());

	}

	/**
	 * Bug [#1070] [iROD-Chat:9099] iDropLiteApplet AuthenticationException
	 * 
	 * @throws Exception
	 *             This looks like an iRODS bug, reported, ignored for now....
	 */
	@Ignore
	public void testGenerateTempPasswordWhenPAMAuthenticatedBug1070()
			throws Exception {

		if (!testingPropertiesHelper.isTestPAM(testingProperties)) {
			return;
		}

		String pamUser = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PAM_USER_KEY);
		String pamPassword = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PAM_PASSWORD_KEY);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, pamUser, pamPassword);

		irodsAccount.setAuthenticationScheme(AuthScheme.PAM);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		String tempPassword = userAO.getTemporaryPasswordForConnectedUser();
		Assert.assertNotNull("null temp password", tempPassword);

		IRODSAccount account = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, irodsAccount.getUserName(),
						tempPassword);
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
