package org.irods.jargon.core.pub;

import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.NoAPIPrivException;
import org.irods.jargon.core.pub.domain.Quota;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class QuotaAOImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "QuotaAOImplTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.QuotaAOImpl#QuotaAOImpl(org.irods.jargon.core.connection.IRODSSession, org.irods.jargon.core.connection.IRODSAccount)}
	 * .
	 */
	@Test
	public void testQuotaAOImpl() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		irodsFileSystem.getIRODSAccessObjectFactory().getQuotaAO(irodsAccount);
		Assert.assertTrue(true);
	}

	@Test
	public void testSetUserResourceQuotaThenListAll() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTertiaryTestProperties(testingProperties);

		// Skip if pre iRODS 4.3.0.
		Assume.assumeFalse("iRODS 4.3.0 does not support user quotas", irodsFileSystem.getIRODSAccessObjectFactory()
				.getIRODSServerProperties(irodsAccount).isAtLeastIrods430());

		IRODSAccount adminAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		QuotaAO quotaAO = irodsFileSystem.getIRODSAccessObjectFactory().getQuotaAO(adminAccount);

		long quotaVal = 653000L;
		quotaAO.setUserQuotaForResource(irodsAccount.getUserName(),
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY), quotaVal);

		List<Quota> actual = quotaAO.listAllQuota();
		Assert.assertTrue("empty quota list result returned", actual.size() > 0);

		boolean foundQuota = false;

		for (Quota quota : actual) {
			if (quota.getUserName().equals(irodsAccount.getUserName()) && quota.getResourceName()
					.equals(testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY))) {
				foundQuota = true;
				Assert.assertEquals("did not properly set resource quota value", quotaVal, quota.getQuotaLimit());
			}
		}

		Assert.assertTrue("did not find expected user/resource quota setting", foundQuota);

	}

	@Test(expected = NoAPIPrivException.class)
	public void testListAllNotRodsadmin() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTertiaryTestProperties(testingProperties);
		QuotaAO quotaAO = irodsFileSystem.getIRODSAccessObjectFactory().getQuotaAO(irodsAccount);

		quotaAO.listAllQuota();
	}

	@Test
	public void testSetUserResourceQuotaToValueOtherThanZeroIsNotAllowed() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTertiaryTestProperties(testingProperties);

		Assume.assumeTrue("iRODS 4.3.0 does not support user quotas", irodsFileSystem.getIRODSAccessObjectFactory()
				.getIRODSServerProperties(irodsAccount).isAtLeastIrods430());

		IRODSAccount adminAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		QuotaAO quotaAO = irodsFileSystem.getIRODSAccessObjectFactory().getQuotaAO(adminAccount);

		long quotaVal = 6893400L;
		JargonException thrown = Assert.assertThrows(JargonException.class,
				() -> quotaAO.setUserQuotaForResource(irodsAccount.getUserName(),
						testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY), quotaVal));
		Assert.assertEquals(thrown.getUnderlyingIRODSExceptionCode(), -169000); // SYS_NOT_ALLOWED
	}

	@Test
	public void testSetUserResourceQuotaThenListUser() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTertiaryTestProperties(testingProperties);

		// Skip if pre iRODS 4.3.0.
		Assume.assumeFalse("iRODS 4.3.0 does not support user quotas", irodsFileSystem.getIRODSAccessObjectFactory()
				.getIRODSServerProperties(irodsAccount).isAtLeastIrods430());

		IRODSAccount adminAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		QuotaAO quotaAO = irodsFileSystem.getIRODSAccessObjectFactory().getQuotaAO(adminAccount);

		long quotaVal = 6893400L;
		quotaAO.setUserQuotaForResource(irodsAccount.getUserName(),
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY), quotaVal);

		List<Quota> actual = quotaAO.listQuotaForAUser(irodsAccount.getUserName());
		Assert.assertTrue("empty quota list result returned", actual.size() > 0);

		boolean foundQuota = false;

		for (Quota quota : actual) {
			if (quota.getUserName().equals(irodsAccount.getUserName()) && quota.getResourceName()
					.equals(testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY))) {
				foundQuota = true;
				Assert.assertEquals("did not properly set resource quota value", quotaVal, quota.getQuotaLimit());
			}
		}

		Assert.assertTrue("did not find expected user/resource quota setting", foundQuota);

	}

	@Test
	public void testSetThenListGlobalQuotaForUser() throws Exception {

		IRODSAccount adminAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);

		// Skip if pre iRODS 4.3.0.
		Assume.assumeFalse("iRODS 4.3.0 does not support user quotas", irodsFileSystem.getIRODSAccessObjectFactory()
				.getIRODSServerProperties(adminAccount).isAtLeastIrods430());

		long quotaVal = 600000L;
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);

		QuotaAO adminQuotaAO = irodsFileSystem.getIRODSAccessObjectFactory().getQuotaAO(adminAccount);
		adminQuotaAO.setUserQuotaTotal(irodsAccount.getUserName(), quotaVal);

		Quota actual = adminQuotaAO.getGlobalQuotaForAUser(irodsAccount.getUserName());
		Assert.assertNotNull("null quota list result returned", actual);
		Assert.assertEquals("invalid user name", irodsAccount.getUserName(), actual.getUserName());
		Assert.assertEquals("invalid zone", irodsAccount.getZone(), actual.getZoneName());
		Assert.assertEquals("invalid resource, should be 'total'", "total", actual.getResourceName());
		Assert.assertEquals("invalid quota value", quotaVal, actual.getQuotaLimit());
	}

	@Test
	public void testSetGlobalForUserThenListAllGlobalQuota() throws Exception {

		long quotaVal = 765543L;
		IRODSAccount adminAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);

		// Skip if pre iRODS 4.3.0.
		Assume.assumeFalse("iRODS 4.3.0 does not support user quotas", irodsFileSystem.getIRODSAccessObjectFactory()
				.getIRODSServerProperties(adminAccount).isAtLeastIrods430());

		QuotaAO adminQuotaAO = irodsFileSystem.getIRODSAccessObjectFactory().getQuotaAO(adminAccount);
		adminQuotaAO.setUserQuotaTotal(irodsAccount.getUserName(), quotaVal);

		List<Quota> actual = adminQuotaAO.listAllGlobalQuota();
		Assert.assertTrue("empty quota list result returned", actual.size() > 0);

		boolean foundQuota = false;

		for (Quota quota : actual) {
			if (quota.getUserName().equals(irodsAccount.getUserName()) && quota.getResourceName().equals("total")) {
				foundQuota = true;
				Assert.assertEquals("did not properly set resource quota value", quotaVal, quota.getQuotaLimit());
			}
		}

		Assert.assertTrue("did not find expected user global quota setting", foundQuota);
	}

}
