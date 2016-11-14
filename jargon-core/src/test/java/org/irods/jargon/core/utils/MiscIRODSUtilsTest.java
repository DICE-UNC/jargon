package org.irods.jargon.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.PathTooLongException;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
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
		String targetIrodsPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		boolean inZone = MiscIRODSUtils.isFileInThisZone(targetIrodsPath,
				irodsAccount);
		Assert.assertFalse("should not be in zone", inZone);
	}

	@Test
	public final void testGetDefaultStorageResourceWhenInZone()
			throws Exception {
		String targetIrodsPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		String defaultStorageResource = MiscIRODSUtils
				.getDefaultIRODSResourceFromAccountIfFileInZone(
						targetIrodsPath, irodsAccount);
		Assert.assertEquals("should pull default resource from IRODS account",
				irodsAccount.getDefaultStorageResource(),
				defaultStorageResource);
	}

	@Test
	public final void testGetDefaultStorageResourceWhenNotInZone()
			throws Exception {
		String targetIrodsPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		String defaultStorageResource = MiscIRODSUtils
				.getDefaultIRODSResourceFromAccountIfFileInZone(
						targetIrodsPath, irodsAccount);
		Assert.assertEquals("should pull default resource from IRODS account",
				"", defaultStorageResource);
	}

	/**
	 * Break an iRODS abs path into components, then rebuild the whole path
	 *
	 * @throws Exception
	 */
	@Test
	public final void testBreakAndRebuildPathFromComponents() throws Exception {
		String targetPath = "/a/path/in/irods/here";
		List<String> pathComponents = MiscIRODSUtils
				.breakIRODSPathIntoComponents(targetPath);
		Assert.assertEquals("did not break into right number of paths", 6,
				pathComponents.size());
		String actual = MiscIRODSUtils.buildPathFromComponentsUpToIndex(
				pathComponents, -1);
		Assert.assertEquals("did not reconstitute path correctly", targetPath,
				actual);

	}

	/**
	 * Break an iRODS abs path into components. Null path should be error
	 *
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testBuildPathFromComponentsWhenNull() throws Exception {
		MiscIRODSUtils.buildPathFromComponentsUpToIndex(null, -1);

	}

	/**
	 * Break an iRODS abs path into components. If the path is empty, should
	 * give '/' as the path back
	 *
	 * @throws Exception
	 */
	@Test
	public final void testBreakAndRebuildPathFromComponentsWhenEmpty()
			throws Exception {
		String targetPath = "/";
		List<String> pathComponents = new ArrayList<String>();

		String actual = MiscIRODSUtils.buildPathFromComponentsUpToIndex(
				pathComponents, -1);
		Assert.assertEquals("did not reconstitute path correctly", targetPath,
				actual);

	}

	/**
	 * Break an iRODS abs path into components, then rebuild the whole path when
	 * the path is just root
	 *
	 * @throws Exception
	 */
	@Test
	public final void testBreakAndRebuildPathFromComponentsWhenRoot()
			throws Exception {
		String targetPath = "/";
		List<String> pathComponents = MiscIRODSUtils
				.breakIRODSPathIntoComponents(targetPath);
		Assert.assertEquals("did not break into right number of paths", 0,
				pathComponents.size());
		String actual = MiscIRODSUtils.buildPathFromComponentsUpToIndex(
				pathComponents, -1);
		Assert.assertEquals("did not reconstitute path correctly", targetPath,
				actual);

	}

	/**
	 * Break an iRODS abs path into components, then rebuild the path using the
	 * first 3 components
	 *
	 * @throws Exception
	 */
	@Test
	public final void testBreakAndRebuildPathFromComponentsGetFirstThreeParts()
			throws Exception {
		String targetPath = "/a/path/in/irods/here";
		List<String> pathComponents = MiscIRODSUtils
				.breakIRODSPathIntoComponents(targetPath);
		String actual = MiscIRODSUtils.buildPathFromComponentsUpToIndex(
				pathComponents, 3);
		Assert.assertEquals("did not reconstitute path correctly",
				"/a/path/in", actual);

	}

	/**
	 * Break user out of user name when just a user
	 */
	@Test
	public void testGetUserInUserNameJustUserName() {
		String testUser = "justauser";
		String actual = MiscIRODSUtils.getUserInUserName(testUser);
		Assert.assertEquals(testUser, actual);
	}

	/**
	 * Break user out of user name when in user#zone
	 */
	@Test
	public void testGetUserInUserNameWhenHasZone() {
		String testUser = "justauser#zone";
		String expected = "justauser";
		String actual = MiscIRODSUtils.getUserInUserName(testUser);
		Assert.assertEquals(expected, actual);
	}

	/**
	 * what about if it has a user# with no zone?
	 */
	@Test
	public void testGetUserInUserNameWhenHasPoundNoZone() {
		String testUser = "justauser#";
		String expected = "justauser";
		String actual = MiscIRODSUtils.getUserInUserName(testUser);
		Assert.assertEquals(expected, actual);
	}

	/**
	 * how about #zone
	 */
	@Test
	public void testGetUserInUserNameWhenHasPoundNoUser() {
		String testUser = "#zone";
		String expected = "";
		String actual = MiscIRODSUtils.getUserInUserName(testUser);
		Assert.assertEquals(expected, actual);
	}

	/**
	 * how about null
	 */
	@Test
	public void testGetUserInUserNameWhenNull() {
		String testUser = null;
		String expected = "";
		String actual = MiscIRODSUtils.getUserInUserName(testUser);
		Assert.assertEquals(expected, actual);
	}

	/**
	 * Break user out of user name when just a user
	 */
	@Test
	public void testGetZoneInUserNameJustUserName() {
		String testUser = "justauser";
		String actual = MiscIRODSUtils.getZoneInUserName(testUser);
		Assert.assertEquals("", actual);
	}

	/**
	 * Break user out of zone name when in user#zone
	 */
	@Test
	public void testGetZoneInUserNameWhenHasZone() {
		String testUser = "justauser#zone";
		String expected = "zone";
		String actual = MiscIRODSUtils.getZoneInUserName(testUser);
		Assert.assertEquals(expected, actual);
	}

	/**
	 * what about if it has a user# with no zone?
	 */
	@Test
	public void testGetZoneInUserNameWhenHasPoundNoZone() {
		String testUser = "justauser#";
		String expected = "";
		String actual = MiscIRODSUtils.getZoneInUserName(testUser);
		Assert.assertEquals(expected, actual);
	}

	/**
	 * how about #zone
	 */
	@Test
	public void testGetZoneInUserNameWhenHasPoundNoUser() {
		String testUser = "#zone";
		String expected = "zone";
		String actual = MiscIRODSUtils.getZoneInUserName(testUser);
		Assert.assertEquals(expected, actual);
	}

	/**
	 * how about null
	 */
	@Test
	public void testGetZoneInUserNameWhenNull() {
		String testUser = null;
		String expected = "";
		String actual = MiscIRODSUtils.getZoneInUserName(testUser);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testComputeHomeDirectoryForIRODSAccount() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		String path = MiscIRODSUtils
				.computeHomeDirectoryForIRODSAccount(irodsAccount);
		String expected = "/" + irodsAccount.getZone() + "/home/"
				+ irodsAccount.getUserName();
		Assert.assertEquals("did not correctly compute path", expected, path);
	}

	@Test
	public void testComputeHomeDirectoryForGivenUserInSameZoneAsIRODSAccount()
			throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		String userName = "blah";
		String path = MiscIRODSUtils
				.computeHomeDirectoryForGivenUserInSameZoneAsIRODSAccount(
						irodsAccount, userName);

		String expected = "/" + irodsAccount.getZone() + "/home/" + userName;
		Assert.assertEquals("did not correctly compute path", expected, path);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testComputeHomeDirectoryForGivenUserInSameZoneAsIRODSAccountNullAccount()
			throws Exception {
		IRODSAccount irodsAccount = null;
		String userName = "blah";
		MiscIRODSUtils
		.computeHomeDirectoryForGivenUserInSameZoneAsIRODSAccount(
				irodsAccount, userName);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testComputeHomeDirectoryForGivenUserInSameZoneAsIRODSAccountNullUserName()
			throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		String userName = null;
		MiscIRODSUtils
		.computeHomeDirectoryForGivenUserInSameZoneAsIRODSAccount(
				irodsAccount, userName);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testComputeHomeDirectoryForGivenUserInSameZoneAsIRODSAccountBlankUserName()
			throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		String userName = "";
		MiscIRODSUtils
		.computeHomeDirectoryForGivenUserInSameZoneAsIRODSAccount(
				irodsAccount, userName);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testComputeHomeDirectoryForIRODSAccountNull() throws Exception {
		MiscIRODSUtils.computeHomeDirectoryForIRODSAccount(null);

	}

	@Test
	public void testPathLengthOK() throws Exception {
		MiscIRODSUtils.checkPathSizeForMax("hello");
	}

	@Test(expected = PathTooLongException.class)
	public void testPathLengthTooLong() throws Exception {
		MiscIRODSUtils.checkPathSizeForMax(FileGenerator
				.generateRandomString(1050));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPathLengthNull() throws Exception {
		MiscIRODSUtils.checkPathSizeForMax(null);
	}

	@Test
	public void testPathLengthParentAndChildOK() throws Exception {
		MiscIRODSUtils.checkPathSizeForMax("hello", "there");
	}

	@Test(expected = PathTooLongException.class)
	public void testPathLengthParentAndChildTooLong() throws Exception {
		MiscIRODSUtils.checkPathSizeForMax(
				FileGenerator.generateRandomString(1000),
				FileGenerator.generateRandomString(1000));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPathLengthParentNull() throws Exception {
		MiscIRODSUtils.checkPathSizeForMax(null, "blah");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPathLengthChildNull() throws Exception {
		MiscIRODSUtils.checkPathSizeForMax("blah", null);
	}

	@Test
	public void testBuildIRODSUserHomeForAccountUsingDefaultScheme()
			throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		String computedPath = MiscIRODSUtils
				.buildIRODSUserHomeForAccountUsingDefaultScheme(irodsAccount);
		String expected = "/" + irodsAccount.getZone() + "/home/"
				+ irodsAccount.getUserName();
		Assert.assertEquals("did not build expected path", expected,
				computedPath);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuildIRODSUserHomeForAccountUsingDefaultSchemeNullAccount()
			throws Exception {
		MiscIRODSUtils.buildIRODSUserHomeForAccountUsingDefaultScheme(null);

	}

	/**
	 * Bug [#1575] jargon-core permissions issue
	 *
	 * @throws Exception
	 */
	@Test
	public void getZoneInPathWhenNoZone() throws Exception {
		String path = "/";
		String zone = MiscIRODSUtils.getZoneInPath(path);
		Assert.assertNotNull("zone was null", zone);
	}

	@Test(expected = JargonException.class)
	public void testSubtractPrefixFromGivenPathShorterThanPrefix()
			throws Exception {
		String prefix = "/a/prefix/here";
		String path = "/a/pre";
		MiscIRODSUtils.subtractPrefixFromGivenPath(prefix, path);
	}

	@Test
	public void testSubtractPrefixFromGivenPath() throws Exception {
		String prefix = "/a/prefix/here";
		String remainder = "/and/some/more";
		String path = prefix + remainder;
		String actual = MiscIRODSUtils
				.subtractPrefixFromGivenPath(prefix, path);
		Assert.assertEquals(remainder, actual);
	}

}
