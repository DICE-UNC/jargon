package org.irods.jargon.core.pub;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class FederatedUserAOTest {

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

	/**
	 * Get a user that is set up as a user on this zone from a federated zone
	 * this checks a lookup in user#zone format
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetUserByNameThatIsAFederatedUser() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String testUserName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_USER_KEY)
				+ "#"
				+ testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_ZONE_KEY);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
		User user = userAO.findByName(testUserName);
		Assert.assertEquals(
				testingProperties
				.getProperty(
						"did not get federated user",
						testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_USER_KEY)),
						user.getName());
		Assert.assertEquals(
				testingProperties
				.getProperty(
						"did not get federated zone",
						testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_ZONE_KEY)),
						user.getZone());

	}

	/**
	 * Look for a user on zone2 by a certain id by asking zone1, giving the fact
	 * that they are on zone2
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetUserByIdAndZoneThatIsAFederatedUser() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		IRODSAccount federatedAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);
		UserAO fedUserAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getUserAO(federatedAccount);
		User fedUserOnOtherZone = fedUserAO.findByName(federatedAccount
				.getUserName());

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
		User user = userAO.findByIdInZone(fedUserOnOtherZone.getId(),
				federatedAccount.getZone());
		Assert.assertEquals("names do not match", fedUserOnOtherZone.getName(),
				user.getName());
		Assert.assertEquals("zones do not match", fedUserOnOtherZone.getZone(),
				user.getZone());

	}
}
