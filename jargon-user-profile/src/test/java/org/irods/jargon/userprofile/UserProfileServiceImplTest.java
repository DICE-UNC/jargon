package org.irods.jargon.userprofile;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class UserProfileServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;
	private static final String IRODS_TEST_SUBDIR_PATH = "UserProfileServiceTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {

		irodsFileSystem.closeAndEatExceptions();

	}

	@Test
	public final void testUserProfileServiceImpl() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		new UserProfileServiceImpl(accessObjectFactory, irodsAccount);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testUserProfileServiceImplNullAccount() throws Exception {
		IRODSAccount irodsAccount = null;
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		new UserProfileServiceImpl(accessObjectFactory, irodsAccount);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testUserProfileServiceImplNullAccessObjectFactory() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = null;
		new UserProfileServiceImpl(accessObjectFactory, irodsAccount);
	}

	/**
	 * Test ability to add and remove profile repeatedly
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testRepeatedlyAddAndRemoveProfileForUser() throws Exception {

		int count = 5;
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		String testUser = "testRepeatedlyAddAndRemoveProfileForUser";

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		try {
			userAO.findByName(testUser);
		} catch (DataNotFoundException dnf) {
			User newUser = new User();
			newUser.setName(testUser);
			newUser.setUserType(UserTypeEnum.RODS_USER);

			userAO.addUser(newUser);
			userAO.changeAUserPasswordByAnAdmin(testUser, testUser);
		}

		IRODSAccount testUserAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(testingProperties, testUser, testUser);

		UserProfileService userProfileService = new UserProfileServiceImpl(accessObjectFactory, testUserAccount);

		for (int i = 0; i < count; i++) {

			userProfileService.removeProfileInformation(testUser);

			UserProfile userProfile = new UserProfile();
			userProfile.setUserName(testUser);
			userProfile.setZone(irodsAccount.getZone());

			// adjust the config to use the admin uid as the protected profile
			// access person
			userProfileService.getUserProfileServiceConfiguration()
					.setProtectedProfileReadWriteGroup(irodsAccount.getUserName());

			userProfileService.addProfileForUser(testUser, userProfile);

			// make the files are there with the necessary permissions

			String userHomeDir = userProfileService.getUserProfileDir(testUser);

			IRODSFile userProfileFile = accessObjectFactory.getIRODSFileFactory(testUserAccount).instanceIRODSFile(
					userHomeDir, userProfileService.getUserProfileServiceConfiguration().getPublicProfileFileName());

			Assert.assertTrue("public user profile not created", userProfileFile.exists());

			IRODSFile protectedProfileFile = accessObjectFactory.getIRODSFileFactory(testUserAccount).instanceIRODSFile(
					userHomeDir, userProfileService.getUserProfileServiceConfiguration().getProtectedProfileFileName());

			Assert.assertTrue("protected user profile not created", protectedProfileFile.exists());
		}

	}

	/**
	 * Add a profile twice
	 * 
	 * @throws Exception
	 */
	@Test(expected = DuplicateDataException.class)
	public final void testAddProfileWhenOneExists() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		String testUser = "testAddProfileWhenOneExists";
		String password = "password";

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		try {
			userAO.findByName(testUser);
		} catch (DataNotFoundException dnf) {
			User newUser = new User();
			newUser.setName(testUser);
			newUser.setUserType(UserTypeEnum.RODS_USER);

			userAO.addUser(newUser);
			userAO.changeAUserPasswordByAnAdmin(testUser, password);
		}

		IRODSAccount testUserAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(testingProperties, testUser, password);

		UserProfileService userProfileService = new UserProfileServiceImpl(accessObjectFactory, testUserAccount);

		userProfileService.removeProfileInformation(testUser);

		UserProfile userProfile = new UserProfile();
		userProfile.setUserName(testUser);
		userProfile.setZone(irodsAccount.getZone());

		// adjust the config to use the admin uid as the protected profile
		// access person
		userProfileService.getUserProfileServiceConfiguration()
				.setProtectedProfileReadWriteGroup(irodsAccount.getUserName());

		userProfileService.addProfileForUser(testUser, userProfile);
		userProfileService.addProfileForUser(testUser, userProfile);
	}

	@Test
	public final void testAddProfileForUser() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		String testUser = "testAddProfileForUser2";

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		try {
			userAO.findByName(testUser);
		} catch (DataNotFoundException dnf) {
			User newUser = new User();
			newUser.setName(testUser);
			newUser.setUserType(UserTypeEnum.RODS_USER);

			userAO.addUser(newUser);
			userAO.changeAUserPasswordByAnAdmin(testUser, testUser);
		}

		IRODSAccount testUserAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(testingProperties, testUser, testUser);

		UserProfileService userProfileService = new UserProfileServiceImpl(accessObjectFactory, testUserAccount);

		userProfileService.removeProfileInformation(testUser);

		UserProfile userProfile = new UserProfile();
		userProfile.setUserName(testUser);
		userProfile.setZone(irodsAccount.getZone());

		String actualNickName = "nickName";
		String actualDescription = "description";

		userProfile.getUserProfilePublicFields().setDescription(actualDescription);
		userProfile.getUserProfilePublicFields().setNickName(actualNickName);
		// access person
		userProfileService.getUserProfileServiceConfiguration()
				.setProtectedProfileReadWriteGroup(irodsAccount.getUserName());

		userProfileService.addProfileForUser(testUser, userProfile);

		// make the files are there with the necessary permissions

		String userHomeDir = userProfileService.getUserProfileDir(testUser);

		IRODSFile userProfileFile = accessObjectFactory.getIRODSFileFactory(testUserAccount).instanceIRODSFile(
				userHomeDir, userProfileService.getUserProfileServiceConfiguration().getPublicProfileFileName());

		Assert.assertTrue("public user profile not created", userProfileFile.exists());

		// make sure public can read profile

		DataObjectAO dataObjectAO = accessObjectFactory.getDataObjectAO(irodsAccount);
		FilePermissionEnum permissionData = dataObjectAO.getPermissionForDataObject(userProfileFile.getAbsolutePath(),
				"public", irodsAccount.getZone());

		Assert.assertEquals("public should have read permission", FilePermissionEnum.READ, permissionData);

		// admin should have write

		permissionData = dataObjectAO.getPermissionForDataObject(userProfileFile.getAbsolutePath(),
				irodsAccount.getUserName(), irodsAccount.getZone());

		Assert.assertEquals("rods should have write permission", FilePermissionEnum.WRITE, permissionData);

		// inspect data in AVU

		List<AVUQueryElement> query = new ArrayList<AVUQueryElement>();

		query.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.UNITS, QueryConditionOperators.EQUAL,
				UserProfileService.AVU_UNIT_NAMESPACE));

		List<MetaDataAndDomainData> metadata = dataObjectAO
				.findMetadataValuesForDataObject(userProfileFile.getAbsolutePath());

		// check for metadata, other unit tests will look at the details field
		// by field
		Assert.assertFalse("no metadata found", metadata.isEmpty());

		IRODSFile protectedProfileFile = accessObjectFactory.getIRODSFileFactory(testUserAccount).instanceIRODSFile(
				userHomeDir, userProfileService.getUserProfileServiceConfiguration().getProtectedProfileFileName());

		Assert.assertTrue("protected user profile not created", protectedProfileFile.exists());

	}

	@Test
	public final void testRetrieveProfileForUser() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		String testUser = "testRetrieveProfileForUser";

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		try {
			userAO.findByName(testUser);
		} catch (DataNotFoundException dnf) {
			User newUser = new User();
			newUser.setName(testUser);
			newUser.setUserType(UserTypeEnum.RODS_USER);

			userAO.addUser(newUser);
			userAO.changeAUserPasswordByAnAdmin(testUser, testUser);
		}

		IRODSAccount testUserAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(testingProperties, testUser, testUser);

		UserProfileService userProfileService = new UserProfileServiceImpl(accessObjectFactory, testUserAccount);

		userProfileService.removeProfileInformation(testUser);

		UserProfile userProfile = new UserProfile();
		userProfile.setUserName(testUser);
		userProfile.setZone(irodsAccount.getZone());

		String actualNickName = "nickName";
		String actualDescription = "description";
		String actualEmail = "emal@something.com";

		userProfile.getUserProfilePublicFields().setDescription(actualDescription);
		userProfile.getUserProfilePublicFields().setNickName(actualNickName);
		userProfile.getUserProfileProtectedFields().setMail(actualEmail);

		// access person
		userProfileService.getUserProfileServiceConfiguration()
				.setProtectedProfileReadWriteGroup(irodsAccount.getUserName());

		userProfileService.addProfileForUser(testUser, userProfile);

		// now retrive
		UserProfile actual = userProfileService.retrieveUserProfile(testUser);
		Assert.assertEquals("user name not set", userProfile.getUserName(), actual.getUserName());
		Assert.assertEquals("zone not set", userProfile.getZone(), actual.getZone());
		Assert.assertEquals("nick name not set", userProfile.getUserProfilePublicFields().getNickName(),
				actual.getUserProfilePublicFields().getNickName());
		Assert.assertEquals("description not set", userProfile.getUserProfilePublicFields().getDescription(),
				actual.getUserProfilePublicFields().getDescription());
		Assert.assertEquals("mail not set", userProfile.getUserProfileProtectedFields().getMail(),
				actual.getUserProfileProtectedFields().getMail());
	}

	/**
	 * Test general operation when a subdir is not defined for the profile info
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testRetrieveProfileForUserNoSubdir() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		String testUser = "testRetrieveProfileForUserNoSubdir";

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		try {
			userAO.findByName(testUser);
		} catch (DataNotFoundException dnf) {
			User newUser = new User();
			newUser.setName(testUser);
			newUser.setUserType(UserTypeEnum.RODS_USER);

			userAO.addUser(newUser);
			userAO.changeAUserPasswordByAnAdmin(testUser, testUser);
		}

		IRODSAccount testUserAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(testingProperties, testUser, testUser);

		UserProfileService userProfileService = new UserProfileServiceImpl(accessObjectFactory, testUserAccount);
		userProfileService.getUserProfileServiceConfiguration().setProfileSubdirName("");

		userProfileService.removeProfileInformation(testUser);

		UserProfile userProfile = new UserProfile();
		userProfile.setUserName(testUser);
		userProfile.setZone(irodsAccount.getZone());

		String actualNickName = "nickName";
		String actualDescription = "description";
		String actualEmail = "emal@something.com";

		userProfile.getUserProfilePublicFields().setDescription(actualDescription);
		userProfile.getUserProfilePublicFields().setNickName(actualNickName);
		userProfile.getUserProfileProtectedFields().setMail(actualEmail);

		// access person
		userProfileService.getUserProfileServiceConfiguration()
				.setProtectedProfileReadWriteGroup(irodsAccount.getUserName());

		userProfileService.addProfileForUser(testUser, userProfile);

		// now retrive
		UserProfile actual = userProfileService.retrieveUserProfile(testUser);
		Assert.assertEquals("user name not set", userProfile.getUserName(), actual.getUserName());
		Assert.assertEquals("zone not set", userProfile.getZone(), actual.getZone());
		Assert.assertEquals("nick name not set", userProfile.getUserProfilePublicFields().getNickName(),
				actual.getUserProfilePublicFields().getNickName());
		Assert.assertEquals("description not set", userProfile.getUserProfilePublicFields().getDescription(),
				actual.getUserProfilePublicFields().getDescription());
		Assert.assertEquals("mail not set", userProfile.getUserProfileProtectedFields().getMail(),
				actual.getUserProfileProtectedFields().getMail());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testRetrieveProfileForUserNullUser() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		String testUser = "testRetrieveProfileForUserNullUser";
		String password = "password";

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		try {
			userAO.findByName(testUser);
		} catch (DataNotFoundException dnf) {
			User newUser = new User();
			newUser.setName(testUser);
			newUser.setUserType(UserTypeEnum.RODS_USER);

			userAO.addUser(newUser);
			userAO.changeAUserPasswordByAnAdmin(testUser, password);
		}

		IRODSAccount testUserAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(testingProperties, testUser, password);

		UserProfileService userProfileService = new UserProfileServiceImpl(accessObjectFactory, testUserAccount);

		// now retrive
		userProfileService.retrieveUserProfile(null);

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testRetrieveProfileForUserBlankUser() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		String testUser = "testRetrieveProfileForUserBlankUser";
		String password = "password";

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		try {
			userAO.findByName(testUser);
		} catch (DataNotFoundException dnf) {
			User newUser = new User();
			newUser.setName(testUser);
			newUser.setUserType(UserTypeEnum.RODS_USER);

			userAO.addUser(newUser);
			userAO.changeAUserPasswordByAnAdmin(testUser, password);
		}

		IRODSAccount testUserAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(testingProperties, testUser, password);

		UserProfileService userProfileService = new UserProfileServiceImpl(accessObjectFactory, testUserAccount);

		// now retrive
		userProfileService.retrieveUserProfile("");

	}

	@Test
	public final void testUpdateProfileForUser() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		String testUser = "testUpdateProfileForUser";
		String updatedPostFix = "updated";

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		try {
			userAO.findByName(testUser);
		} catch (DataNotFoundException dnf) {
			User newUser = new User();
			newUser.setName(testUser);
			newUser.setUserType(UserTypeEnum.RODS_USER);

			userAO.addUser(newUser);
			userAO.changeAUserPasswordByAnAdmin(testUser, testUser);
		}

		IRODSAccount testUserAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(testingProperties, testUser, testUser);

		UserProfileService userProfileService = new UserProfileServiceImpl(accessObjectFactory, testUserAccount);

		userProfileService.removeProfileInformation(testUser);

		UserProfile userProfile = new UserProfile();
		userProfile.setUserName(testUser);
		userProfile.setZone(irodsAccount.getZone());

		String actualNickName = "nickName";
		String actualDescription = "description";
		String actualEmail = "emal@something.com";

		userProfile.getUserProfilePublicFields().setDescription(actualDescription);
		userProfile.getUserProfilePublicFields().setNickName(actualNickName);
		userProfile.getUserProfileProtectedFields().setMail(actualEmail);

		// access person
		userProfileService.getUserProfileServiceConfiguration()
				.setProtectedProfileReadWriteGroup(irodsAccount.getUserName());

		userProfileService.addProfileForUser(testUser, userProfile);

		// now retrive and update the email and descr
		UserProfile beforeUpdate = userProfileService.retrieveUserProfile(testUser);
		beforeUpdate.getUserProfileProtectedFields().setMail(actualEmail + updatedPostFix);
		beforeUpdate.getUserProfilePublicFields().setDescription(actualDescription + updatedPostFix);

		userProfileService.updateUserProfile(beforeUpdate);

		// retrieve again and check email and description

		UserProfile actual = userProfileService.retrieveUserProfile(testUser);

		Assert.assertEquals("did not update description", actualDescription + updatedPostFix,
				actual.getUserProfilePublicFields().getDescription());
		Assert.assertEquals("did not update email", actualEmail + updatedPostFix,
				actual.getUserProfileProtectedFields().getMail());

	}

	/**
	 * Update a profile on a non-existent user
	 * 
	 * @throws Exception
	 */
	@Test(expected = JargonRuntimeException.class)
	public final void testUpdateProfileForUserUserNotExists() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		String testUser = "testUpdateProfileForUserUserNotExists";

		IRODSAccount testUserAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(testingProperties, testUser, testUser);

		UserProfileService userProfileService = new UserProfileServiceImpl(accessObjectFactory, testUserAccount);

		UserProfile userProfile = new UserProfile();
		userProfile.setUserName(testUser);
		userProfile.setZone(irodsAccount.getZone());
		userProfileService.updateUserProfile(userProfile);

	}

	/**
	 * Update a profile on an existing user who has no profile
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testUpdateProfileForUserThatExistsButDoesNotHaveAProfile() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		String testUser = "testUpdateProfileForUserThatExistsButDoesNotHaveAProfile";
		String password = "password";

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		try {
			userAO.findByName(testUser);
		} catch (DataNotFoundException dnf) {
			User newUser = new User();
			newUser.setName(testUser);
			newUser.setUserType(UserTypeEnum.RODS_USER);

			userAO.addUser(newUser);
			userAO.changeAUserPasswordByAnAdmin(testUser, password);
		}

		IRODSAccount testUserAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(testingProperties, testUser, password);

		UserProfileService userProfileService = new UserProfileServiceImpl(accessObjectFactory, testUserAccount);

		userProfileService.removeProfileInformation(testUser);

		UserProfile userProfile = new UserProfile();
		userProfile.setUserName(testUser);
		userProfile.setZone(irodsAccount.getZone());

		String actualNickName = "nickName";
		String actualDescription = "description";
		String actualEmail = "emal@something.com";

		userProfile.getUserProfilePublicFields().setDescription(actualDescription);
		userProfile.getUserProfilePublicFields().setNickName(actualNickName);
		userProfile.getUserProfileProtectedFields().setMail(actualEmail);

		// access person
		userProfileService.getUserProfileServiceConfiguration()
				.setProtectedProfileReadWriteGroup(irodsAccount.getUserName());

		userProfileService.updateUserProfile(userProfile);
		UserProfile actual = userProfileService.retrieveUserProfile(testUser);
		// really should just not get a data not found error, should really add,
		// the test below is a formality
		Assert.assertNotNull("profile was null", actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testUpdateProfileForNullUserProfile() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		String testUser = "testUpdateProfileForNullUserProfile";
		String password = "password";
		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		try {
			userAO.findByName(testUser);
		} catch (DataNotFoundException dnf) {
			User newUser = new User();
			newUser.setName(testUser);
			newUser.setUserType(UserTypeEnum.RODS_USER);

			userAO.addUser(newUser);
			userAO.changeAUserPasswordByAnAdmin(testUser, password);
		}

		IRODSAccount testUserAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(testingProperties, testUser, password);

		UserProfileService userProfileService = new UserProfileServiceImpl(accessObjectFactory, testUserAccount);

		UserProfile userProfile = null;
		userProfileService.updateUserProfile(userProfile);

	}

	@Test
	public final void testRetrieveProfileForUserAllPublicFieldsTested() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		String testUser = "AllPublicFieldsTested";

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);

		try {
			userAO.findByName(testUser);
		} catch (DataNotFoundException dnf) {
			User newUser = new User();
			newUser.setName(testUser);
			newUser.setUserType(UserTypeEnum.RODS_USER);

			userAO.addUser(newUser);
			userAO.changeAUserPasswordByAnAdmin(testUser, testUser);
		}

		IRODSAccount testUserAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(testingProperties, testUser, testUser);

		UserProfileService userProfileService = new UserProfileServiceImpl(accessObjectFactory, testUserAccount);

		userProfileService.removeProfileInformation(testUser);

		UserProfile userProfile = new UserProfile();
		userProfile.setUserName(testUser);
		userProfile.setZone(irodsAccount.getZone());

		String actualNickName = "nickName";
		String actualDescription = "description";
		String actualEmail = "emal@something.com";

		userProfile.getUserProfilePublicFields().setDescription(actualDescription);
		userProfile.getUserProfilePublicFields().setNickName(actualNickName);
		userProfile.getUserProfileProtectedFields().setMail(actualEmail);
		userProfile.getUserProfilePublicFields().setCn(UserProfileConstants.CN);
		userProfile.getUserProfilePublicFields().setGivenName(UserProfileConstants.GIVEN_NAME);
		userProfile.getUserProfilePublicFields().setJpegPhoto(UserProfileConstants.JPEG_PHOTO);
		userProfile.getUserProfilePublicFields().setLabeledURL(UserProfileConstants.LABELED_URL);
		userProfile.getUserProfilePublicFields().setLocalityName(UserProfileConstants.LOCALITY_NAME);
		userProfile.getUserProfilePublicFields().setPostalAddress(UserProfileConstants.POSTAL_ADDRESS);
		userProfile.getUserProfilePublicFields().setPostalCode(UserProfileConstants.POSTAL_CODE);
		userProfile.getUserProfilePublicFields().setPostOfficeBox(UserProfileConstants.POST_OFFICE_BOX);
		userProfile.getUserProfilePublicFields().setSn(UserProfileConstants.SN);
		userProfile.getUserProfilePublicFields().setSt(UserProfileConstants.STATE);
		userProfile.getUserProfilePublicFields().setStreet(UserProfileConstants.STREET);
		userProfile.getUserProfilePublicFields().setTelephoneNumber(UserProfileConstants.TELEPHONE_NUMBER);
		userProfile.getUserProfilePublicFields().setTitle(UserProfileConstants.TITLE);

		// access person
		userProfileService.getUserProfileServiceConfiguration()
				.setProtectedProfileReadWriteGroup(irodsAccount.getUserName());

		userProfileService.addProfileForUser(testUser, userProfile);

		// now retrive
		UserProfile actual = userProfileService.retrieveUserProfile(testUser);
		Assert.assertEquals("user name not set", userProfile.getUserName(), actual.getUserName());
		Assert.assertEquals("zone not set", userProfile.getZone(), actual.getZone());
		Assert.assertEquals("nick name not set", userProfile.getUserProfilePublicFields().getNickName(),
				actual.getUserProfilePublicFields().getNickName());
		Assert.assertEquals("description not set", userProfile.getUserProfilePublicFields().getDescription(),
				actual.getUserProfilePublicFields().getDescription());
		Assert.assertEquals("mail not set", userProfile.getUserProfileProtectedFields().getMail(),
				actual.getUserProfileProtectedFields().getMail());

		Assert.assertEquals("cn not set", UserProfileConstants.CN, actual.getUserProfilePublicFields().getCn());
		Assert.assertEquals("GIVEN_NAME not set", UserProfileConstants.GIVEN_NAME,
				actual.getUserProfilePublicFields().getGivenName());
		Assert.assertEquals("JPEG_PHOTO not set", UserProfileConstants.JPEG_PHOTO,
				actual.getUserProfilePublicFields().getJpegPhoto());
		Assert.assertEquals("LABELED_URL not set", UserProfileConstants.LABELED_URL,
				actual.getUserProfilePublicFields().getLabeledURL());
		Assert.assertEquals("LOCALITY_NAME not set", UserProfileConstants.LOCALITY_NAME,
				actual.getUserProfilePublicFields().getLocalityName());
		Assert.assertEquals("POSTAL_ADDRESS not set", UserProfileConstants.POSTAL_ADDRESS,
				actual.getUserProfilePublicFields().getPostalAddress());
		Assert.assertEquals("POSTAL_CODE not set", UserProfileConstants.POSTAL_CODE,
				actual.getUserProfilePublicFields().getPostalCode());
		Assert.assertEquals("POST_OFFICE_BOX not set", UserProfileConstants.POST_OFFICE_BOX,
				actual.getUserProfilePublicFields().getPostOfficeBox());
		Assert.assertEquals("SN not set", UserProfileConstants.SN, actual.getUserProfilePublicFields().getSn());
		Assert.assertEquals("STREET not set", UserProfileConstants.STREET,
				actual.getUserProfilePublicFields().getStreet());
		Assert.assertEquals("STATE not set", UserProfileConstants.STATE, actual.getUserProfilePublicFields().getSt());
		Assert.assertEquals("TELEPHONE_NUMBER not set", UserProfileConstants.TELEPHONE_NUMBER,
				actual.getUserProfilePublicFields().getTelephoneNumber());
		Assert.assertEquals("TITLE not set", UserProfileConstants.TITLE,
				actual.getUserProfilePublicFields().getTitle());
	}

}
