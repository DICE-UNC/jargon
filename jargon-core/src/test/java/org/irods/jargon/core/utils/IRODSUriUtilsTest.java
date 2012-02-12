package org.irods.jargon.core.utils;

import java.net.URI;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSUriUtilsTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IRODSUriUtilsTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(
				testingProperties);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
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
	public void testGetUserNameFromURI() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		URI testURI = testingPropertiesHelper
				.buildUriFromTestPropertiesForFileInUserDir(testingProperties,
						"home/afile.txt");
		String actual = IRODSUriUtils.getUserNameFromURI(testURI);
		Assert.assertNotNull("null user name", actual);
		Assert.assertEquals("did not derive user name from URI",
				irodsAccount.getUserName(), actual);
	}

	@Test
	public void testGetPasswordFromURI() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		URI testURI = testingPropertiesHelper
				.buildUriFromTestPropertiesForFileInUserDir(testingProperties,
						"home/afile.txt");
		String actual = IRODSUriUtils.getPasswordFromURI(testURI);
		Assert.assertNotNull("null password", actual);
		Assert.assertEquals("did not derive password from URI",
				irodsAccount.getPassword(), actual);
	}

	@Test
	public void testGetPasswordFromURINoPassword() throws Exception {

		URI testURI = testingPropertiesHelper
				.buildUriFromTestPropertiesForFileNoUserInfo(testingProperties,
						"home/afile.txt");

		String actual = IRODSUriUtils.getPasswordFromURI(testURI);
		Assert.assertNull("password should be null", actual);
	}

	@Test
	public void testGetZoneFromURI() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		URI testURI = testingPropertiesHelper
				.buildUriFromTestPropertiesForFileNoUserInfo(testingProperties,
						"home/afile.txt");
		String actual = IRODSUriUtils.getZoneFromURI(testURI);
		Assert.assertNotNull("null zone", actual);
		Assert.assertEquals("did not derive zone from URI",
				irodsAccount.getZone(), actual);
	}

	@Test
	public void testGetHostFromURI() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		URI testURI = testingPropertiesHelper
				.buildUriFromTestPropertiesForFileInUserDir(testingProperties,
						"home/afile.txt");
		String actual = IRODSUriUtils.getHostFromURI(testURI);
		Assert.assertNotNull("null host", actual);
		Assert.assertEquals("did not derive host from URI",
				irodsAccount.getHost(), actual);
	}

	@Test
	public void testGetPortFromURI() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		URI testURI = testingPropertiesHelper
				.buildUriFromTestPropertiesForFileInUserDir(testingProperties,
						"home/afile.txt");
		int actual = IRODSUriUtils.getPortFromURI(testURI);
		Assert.assertEquals("did not derive port from URI",
				irodsAccount.getPort(), actual);
	}

	@Test
	public void testGetAbsolutePathFromURI() throws Exception {

		URI testURI = testingPropertiesHelper
				.buildUriFromTestPropertiesForFileInUserDir(testingProperties,
						"home/afile.txt");
		String actual = IRODSUriUtils.getAbsolutePathFromURI(testURI);
		Assert.assertNotNull("no path returned", actual);
	}

	@Test
	public void testGetURIFromAccountAndPath() throws Exception {
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		URI testURI = testingPropertiesHelper
				.buildUriFromTestPropertiesForFileInUserDirNoPasswordOrZone(
						testingProperties,
						IRODS_TEST_SUBDIR_PATH);
		TestCase.assertEquals(
				"uri not computed correctly",
				testURI.toString(),
				IRODSUriUtils.buildURIForAnAccountAndPath(irodsAccount,
						targetIrodsCollection).toString());

	}

}
