package org.irods.jargon.core.utils;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MiscIRODSUtilsTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	public static final String IRODS_TEST_SUBDIR_PATH = "MiscIRODSUtilsTest";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testIsFileInThisZoneNullPath() {
		MiscIRODSUtils.isFileInThisZone(null, testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties));
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testIsFileInThisZoneBlankPath() {
		MiscIRODSUtils.isFileInThisZone("", testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties));
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testIsFileInThisZoneNullAccount() {
		MiscIRODSUtils.isFileInThisZone("path", null);
	}

	@Test
	public final void testIsFileInThisZoneWhenInZone() throws Exception {
		String targetIrodsPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		boolean inZone = MiscIRODSUtils.isFileInThisZone(targetIrodsPath,
				irodsAccount);
		Assert.assertTrue("should be in zone", inZone);
	}
	
	@Test
	public final void testIsFileInThisZoneWhenNotInZone() throws Exception {
		String targetIrodsPath = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		boolean inZone = MiscIRODSUtils.isFileInThisZone(targetIrodsPath,
				irodsAccount);
		Assert.assertFalse("should not be in zone", inZone);
	}
	
	@Test
	public final void testGetDefaultStorageResourceWhenInZone() throws Exception {
		String targetIrodsPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		String defaultStorageResource = MiscIRODSUtils.getDefaultIRODSResourceFromAccountIfFileInZone(targetIrodsPath, irodsAccount);
		Assert.assertEquals("should pull default resource from IRODS account", irodsAccount.getDefaultStorageResource(), defaultStorageResource);
	}
	
	@Test
	public final void testGetDefaultStorageResourceWhenNotInZone() throws Exception {
		String targetIrodsPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		String defaultStorageResource = MiscIRODSUtils.getDefaultIRODSResourceFromAccountIfFileInZone(targetIrodsPath, irodsAccount);
		Assert.assertEquals("should pull default resource from IRODS account", "", defaultStorageResource);
	}
}
