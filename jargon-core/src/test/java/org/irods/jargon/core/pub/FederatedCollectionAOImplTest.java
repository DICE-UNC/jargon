package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.MetaDataAndDomainData;
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
	 * Add a user from zone2 as a permission, then ask, via zone1 for that
	 * user's info, giving the user name in user#zone format
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testGetPermissionsForCollectionForUserWhoIsCrossZone()
			throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String testCollectionName = "testGetPermissionsForCollectionForUserWhoIsCrossZone";

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

		String concatenatedUserName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_USER_KEY)
				+ "#"
				+ testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_ZONE_KEY);

		UserFilePermission userFilePermission = collectionAO
				.getPermissionForUserName(targetIrodsCollection,
						concatenatedUserName);
		Assert.assertNotNull("got a null userFilePermission",
				userFilePermission);

		Assert.assertEquals("did not get user name concatenated",
				concatenatedUserName, userFilePermission.getUserName());
		Assert.assertEquals(
				"did not get user zone",
				String.valueOf(testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_ZONE_KEY)),
				userFilePermission.getUserZone());
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
							.getNameWithZone()
							.equals(testingProperties
									.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_USER_KEY)
									+ '#'
									+ testingProperties
											.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_ZONE_KEY))) {
				foundCrossZone = true;
			}

		}

		Assert.assertTrue("did not find cross zone user with zone info",
				foundCrossZone);

	}

	/**
	 * Bug [#1440] federation testing error showing sharing tab, -10033 user not
	 * found error
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testGetPermissionsForCollectionInOtherZone()
			throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String testCollectionName = "testGetPermissionsForCollectionInOtherZone";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollectionName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);
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
							.getNameWithZone()
							.equals(testingProperties
									.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_USER_KEY)
									+ '#'
									+ testingProperties
											.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_ZONE_KEY))) {
				foundCrossZone = true;
			}

		}

		Assert.assertTrue("did not find cross zone user with zone info",
				foundCrossZone);

	}

	/**
	 * Find the metadata values associated with a given collection where the
	 * collection is in zone2, and I access it from federated zone 1
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testFindMetadataValuesByMetadataQueryForCollectionInAnotherZone()
			throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String testSubdir = "testFindMetadataValuesByMetadataQueryForCollectionInAnotherZone";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testSubdir);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdirs();

		// initialize the AVU data
		String expectedAttribName = "testFindMetadataValuesByMetadataQueryForCollectionInAnotherZoneattrib1";
		String expectedAttribValue = "testFindMetadataValuesByMetadataQueryForCollectionInAnotherZonevalue1";

		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);

		AvuData dataToAdd = AvuData.instance(expectedAttribName,
				expectedAttribValue, "");
		collectionAO.deleteAVUMetadata(targetIrodsCollection, dataToAdd);
		collectionAO.addAVUMetadata(targetIrodsCollection, dataToAdd);

		IRODSAccount zone1Account = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		collectionAO = accessObjectFactory.getCollectionAO(zone1Account);

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				AVUQueryOperatorEnum.EQUAL, expectedAttribName));

		List<MetaDataAndDomainData> result = collectionAO
				.findMetadataValuesByMetadataQueryForCollection(queryElements,
						targetIrodsCollection);
		Assert.assertFalse("no query result returned", result.isEmpty());
	}

	/**
	 * Set up a collection in zone2, and then log in to zone1 and add metadata
	 * to that collection
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAddAvuMetadataToAFederatedCollection() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}
		String testDirName = "testAddAvuMetadataToAFederatedCollection";
		String expectedAttribName = "testAddAvuMetadataToAFederatedCollection-testattrib1";
		String expectedAttribValue = "testAddAvuMetadataToAFederatedCollection-testvalue1";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneWriteTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testDirName);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdirs();

		// collection in place, now log in to zone1 and add the metadata

		IRODSAccount zone1Account = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(zone1Account);

		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedAttribValue, "");

		collectionAO.deleteAVUMetadata(targetIrodsCollection, avuData);
		collectionAO.addAVUMetadata(targetIrodsCollection, avuData);

		// now list the metadata
		List<MetaDataAndDomainData> metadata = collectionAO
				.findMetadataValuesForCollection(targetIrodsCollection);

		Assert.assertEquals("did not find one metadata value I just added", 1,
				metadata.size());

	}

}
