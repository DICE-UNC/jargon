package org.irods.jargon.core.pub;

import java.util.List;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test CollectionAOImpl operations in a federated zone environment
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class FederatedCollectionAOImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	public static final String IRODS_TEST_SUBDIR_PATH = "FederatedCollectionAOImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		irodsFileSystem.closeAndEatExceptions();
	}

	/**
	 * Set read on a collection for a user in a federated zone
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testSetReadForUserInFederatedZone() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String testFileName = "testSetReadForUserInFederatedZone";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdirs();

		collectionAO
				.setAccessPermissionRead(
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_ZONE_KEY),
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_USER_KEY),
						true);

		// log in as the federated user and test read access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);
		IRODSFile irodsFileForSecondaryUser = irodsFileSystem
				.getIRODSFileFactory(secondaryAccount).instanceIRODSFile(
						targetIrodsCollection);
		Assert.assertTrue(irodsFileForSecondaryUser.canRead());

	}

	/**
	 * Create a collection in the primary zone, add a permission to another user
	 * from that same zone, then the primary user in the federated zone, then
	 * check that list permissions shows all three
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testGetPermissionsForCollectionWithCrossZonePermissions()
			throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String testCollectionName = "testGetPermissionsForCollectionWithCrossZonePermissions";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollectionName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdirs();

		collectionAO
				.setAccessPermissionRead(
						"",
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
						true);

		collectionAO
				.setAccessPermissionRead(
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_ZONE_KEY),
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_USER_KEY),
						true);

		List<UserFilePermission> userFilePermissions = collectionAO
				.listPermissionsForCollection(targetIrodsCollection);
		Assert.assertNotNull("got a null userFilePermissions",
				userFilePermissions);
		Assert.assertEquals("did not find the three permissions", 3,
				userFilePermissions.size());

		boolean foundCrossZone = false;

		for (UserFilePermission userFilePermission : userFilePermissions) {
			if (userFilePermission
					.getUserZone()
					.equals(testingProperties
							.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_ZONE_KEY))
					&& userFilePermission
							.getUserName()
							.equals(testingProperties
									.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_USER_KEY))) {
				foundCrossZone = true;
			}

		}

		TestCase.assertTrue("did not find cross zone user with zone info",
				foundCrossZone);

	}

}
