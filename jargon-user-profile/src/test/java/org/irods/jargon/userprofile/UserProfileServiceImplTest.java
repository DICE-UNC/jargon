package org.irods.jargon.userprofile;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
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
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class UserProfileServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;
	private static final String IRODS_TEST_SUBDIR_PATH = "UserProfileServiceTest";
	private static ScratchFileUtils scratchFileUtils = null;
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();

		scratchFileUtils = new ScratchFileUtils(testingProperties);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {

		irodsFileSystem.closeAndEatExceptions();

	}

	@Test
	public final void testUserProfileServiceImpl() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		new UserProfileServiceImpl(accessObjectFactory, irodsAccount);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testUserProfileServiceImplNullAccount() throws Exception {
		IRODSAccount irodsAccount = null;
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		new UserProfileServiceImpl(accessObjectFactory, irodsAccount);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testUserProfileServiceImplNullAccessObjectFactory()
			throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = null;
		new UserProfileServiceImpl(accessObjectFactory, irodsAccount);
	}

	/**
	 * Test ability to add and remove profile repeatedly
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testRepeatedlyAddAndRemoveProfileForUser()
			throws Exception {

		int count = 15;
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAdminAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
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
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUser, testUser);

		UserProfileService userProfileService = new UserProfileServiceImpl(
				accessObjectFactory, testUserAccount);

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

		IRODSFile userProfileFile = accessObjectFactory.getIRODSFileFactory(
				testUserAccount).instanceIRODSFile(
				userHomeDir,
				userProfileService.getUserProfileServiceConfiguration()
						.getPublicProfileFileName());

		TestCase.assertTrue("public user profile not created",
				userProfileFile.exists());

		IRODSFile protectedProfileFile = accessObjectFactory
				.getIRODSFileFactory(testUserAccount).instanceIRODSFile(
						userHomeDir,
						userProfileService.getUserProfileServiceConfiguration()
								.getProtectedProfileFileName());

		TestCase.assertTrue("protected user profile not created",
				protectedProfileFile.exists());
		}

	}

	@Test
	public final void testAddProfileForUser() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAdminAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
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
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUser, testUser);

		UserProfileService userProfileService = new UserProfileServiceImpl(
				accessObjectFactory, testUserAccount);

		userProfileService.removeProfileInformation(testUser);

		UserProfile userProfile = new UserProfile();
		userProfile.setUserName(testUser);
		userProfile.setZone(irodsAccount.getZone());

		String actualNickName = "nickName";
		String actualDescription = "description";
		String actualZone = "zone";

		userProfile.getUserProfilePublicFields().setDescription(
				actualDescription);
		userProfile.getUserProfilePublicFields().setNickName(actualNickName);
		// access person
		userProfileService.getUserProfileServiceConfiguration()
				.setProtectedProfileReadWriteGroup(irodsAccount.getUserName());

		userProfileService.addProfileForUser(testUser, userProfile);

		// make the files are there with the necessary permissions

		String userHomeDir = userProfileService.getUserProfileDir(testUser);

		IRODSFile userProfileFile = accessObjectFactory.getIRODSFileFactory(
				testUserAccount).instanceIRODSFile(
				userHomeDir,
				userProfileService.getUserProfileServiceConfiguration()
						.getPublicProfileFileName());

		TestCase.assertTrue("public user profile not created",
				userProfileFile.exists());

		// make sure public can read profile

		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		FilePermissionEnum permissionData = dataObjectAO
				.getPermissionForDataObject(
				userProfileFile.getAbsolutePath(),
						"public", irodsAccount.getZone());

		TestCase.assertEquals("public should have read permission",
				FilePermissionEnum.READ, permissionData);

		// admin should have write

		permissionData = dataObjectAO.getPermissionForDataObject(
				userProfileFile.getAbsolutePath(), irodsAccount.getUserName(),
				irodsAccount.getZone());

		TestCase.assertEquals("rods should have write permission",
				FilePermissionEnum.WRITE, permissionData);

		// inspect data in AVU

		List<AVUQueryElement> query = new ArrayList<AVUQueryElement>();

		query.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.UNITS,
				AVUQueryOperatorEnum.EQUAL,
				UserProfileService.AVU_UNIT_NAMESPACE));

		List<MetaDataAndDomainData> metadata = dataObjectAO
				.findMetadataValuesForDataObject(userProfileFile
						.getAbsolutePath());

		// check for metadata, other unit tests will look at the details field
		// by field
		TestCase.assertFalse("no metadata found", metadata.isEmpty());

		IRODSFile protectedProfileFile = accessObjectFactory
				.getIRODSFileFactory(testUserAccount).instanceIRODSFile(
						userHomeDir,
						userProfileService.getUserProfileServiceConfiguration()
								.getProtectedProfileFileName());

		TestCase.assertTrue("protected user profile not created",
				protectedProfileFile.exists());

	}

	@Test
	public final void testRetrieveProfileForUser() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAdminAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
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
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUser, testUser);

		UserProfileService userProfileService = new UserProfileServiceImpl(
				accessObjectFactory, testUserAccount);

		userProfileService.removeProfileInformation(testUser);

		UserProfile userProfile = new UserProfile();
		userProfile.setUserName(testUser);
		userProfile.setZone(irodsAccount.getZone());

		String actualNickName = "nickName";
		String actualDescription = "description";
		String actualZone = "zone";
		String actualEmail = "emal@something.com";

		userProfile.getUserProfilePublicFields().setDescription(
				actualDescription);
		userProfile.getUserProfilePublicFields().setNickName(actualNickName);
		userProfile.getUserProfileProtectedFields().setMail(actualEmail);

		// access person
		userProfileService.getUserProfileServiceConfiguration()
				.setProtectedProfileReadWriteGroup(irodsAccount.getUserName());

		userProfileService.addProfileForUser(testUser, userProfile);

		// now retrive
		UserProfile actual = userProfileService.retrieveUserProfile(testUser);
		TestCase.assertEquals("user name not set", userProfile.getUserName(),
				actual.getUserName());
		TestCase.assertEquals("zone not set", userProfile.getZone(),
				actual.getZone());
		TestCase.assertEquals("nick name not set", userProfile
				.getUserProfilePublicFields().getNickName(), actual
				.getUserProfilePublicFields().getNickName());
		TestCase.assertEquals("description not set", userProfile
				.getUserProfilePublicFields().getDescription(), actual
				.getUserProfilePublicFields().getDescription());


	}

}
