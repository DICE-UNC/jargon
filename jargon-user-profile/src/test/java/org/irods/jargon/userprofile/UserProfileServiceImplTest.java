package org.irods.jargon.userprofile;

import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.domain.User;
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
	private static final String IRODS_TEST_SUBDIR_PATH = "UserProfileServiceImplTest";
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

	@Test
	public final void testAddProfileForUser() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		String testUser = "testAddProfileForUser";

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

		// adjust the config to use the admin uid as the protected profile
		// access person
		userProfileService.getUserProfileServiceConfiguration()
				.setProtectedProfileReadWriteGroup(irodsAccount.getUserName());

		userProfileService.addProfileForUser(testUser, userProfile);

	}

}
