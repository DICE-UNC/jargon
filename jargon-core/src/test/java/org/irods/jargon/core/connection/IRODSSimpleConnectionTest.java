package org.irods.jargon.core.connection;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSSimpleConnectionTest {
	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem = null;

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
	public void testOpenAndCloseSimpleConnection() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		AbstractIRODSMidLevelProtocol connection = irodsFileSystem
				.getIrodsSession().currentConnection(irodsAccount);
		connection.disconnect();

		Assert.assertFalse("connection should not be connected",
				connection.isConnected());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOpenAndCloseSimpleConnectionNullSession() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		irodsFileSystem.getIrodsProtocolManager().getIRODSProtocol(
				irodsAccount,
				irodsFileSystem.getIrodsSession()
						.buildPipelineConfigurationBasedOnJargonProperties(),
				null);

	}

	@Test
	public void testLoginWithLinuxSpecialCharsInPasswordBug202()
			throws Exception {

		String testUser = "testLoginWithLinuxSpecialChars";
		String password1 = "te=st";
		String password2 = "PaZz!word123";
		String password3 = "p@ssw0000000000=0000000000000000000000rd3";
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

		IRODSAccount testAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUser, password1);
		irodsFileSystem.getIRODSAccessObjectFactory().authenticateIRODSAccount(
				testAccount);

		// get an account as the given user, change password, re-log in several
		// iterations

		IRODSAccount userAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUser, password1);
		UserAO myUserAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getUserAO(userAccount);
		myUserAO.changeAUserPasswordByThatUser(testUser, password1, password2);

		testAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUser, password2);
		irodsFileSystem.getIRODSAccessObjectFactory().authenticateIRODSAccount(
				testAccount);

		userAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUser, password2);
		myUserAO = irodsFileSystem.getIRODSAccessObjectFactory().getUserAO(
				userAccount);
		myUserAO.changeAUserPasswordByThatUser(testUser, password2, password3);
		testAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUser, password3);
		irodsFileSystem.getIRODSAccessObjectFactory().authenticateIRODSAccount(
				testAccount);

		userAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUser, password3);
		myUserAO = irodsFileSystem.getIRODSAccessObjectFactory().getUserAO(
				userAccount);
		myUserAO.changeAUserPasswordByThatUser(testUser, password3, password4);
		testAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUser, password4);

		userAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUser, password4);
		myUserAO = irodsFileSystem.getIRODSAccessObjectFactory().getUserAO(
				userAccount);

		// now clean up as an admin

		adminUserAO.deleteUser(testUser);

	}

}
