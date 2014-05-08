package org.irods.jargon.conveyor.gridaccount;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GridAccountConfigurationProcessorTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "GridAccountServiceImplTest";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(
				testingProperties);
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSerializeIRODSAccountListToFile() throws Exception {
		String testFileName = "testSerializeIRODSAccountListToFile.txt";
		ArrayList<IRODSAccount> irodsAccounts = new ArrayList<IRODSAccount>();
		irodsAccounts.add(testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, "test", "test"));
		String testFileAbsPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ "/" + testFileName);
		File testFile = new File(testFileAbsPath);
		GridAccountConfigurationProcessor.serializeIRODSAccountListToFile(
				testFile, irodsAccounts);
		Assert.assertTrue("file does not exist", testFile.exists());
		List<IRODSAccount> actual = GridAccountConfigurationProcessor
				.deserializeIRODSAccountListFromFile(testFile);
		IRODSAccount expected = irodsAccounts.get(0);
		Assert.assertEquals("did not get an account", 1, actual.size());
		Assert.assertEquals("bad host", expected.getHost(), actual.get(0)
				.getHost());
		Assert.assertEquals("bad port", expected.getPort(), actual.get(0)
				.getPort());
		Assert.assertEquals("bad zone", expected.getZone(), actual.get(0)
				.getZone());
		Assert.assertEquals("bad user", expected.getUserName(), actual.get(0)
				.getUserName());
		Assert.assertEquals("bad resource", expected
				.getDefaultStorageResource(), actual.get(0)
				.getDefaultStorageResource());
		Assert.assertEquals("bad home dir", expected.getHomeDirectory(), actual
				.get(0).getHomeDirectory());
		Assert.assertEquals("bad auth type",
				expected.getAuthenticationScheme(), actual.get(0)
						.getAuthenticationScheme());

	}

	@Test(expected = IllegalArgumentException.class)
	public void testSerializeIRODSAccountListNullFile() throws Exception {
		ArrayList<IRODSAccount> irodsAccounts = new ArrayList<IRODSAccount>();
		GridAccountConfigurationProcessor.serializeIRODSAccountListToFile(null,
				irodsAccounts);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSerializeIRODSAccountListNullAccounts() throws Exception {
		String testFileName = "testSerializeIRODSAccountListToFile.txt";
		String testFileAbsPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ "/" + testFileName);
		File testFile = new File(testFileAbsPath);

		GridAccountConfigurationProcessor.serializeIRODSAccountListToFile(
				testFile, null);
	}

	@Test
	public void testSerializeIRODSAccountListEmptyAccounts() throws Exception {
		String testFileName = "testSerializeIRODSAccountListToFile.txt";
		ArrayList<IRODSAccount> irodsAccounts = new ArrayList<IRODSAccount>();
		String testFileAbsPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ "/" + testFileName);
		File testFile = new File(testFileAbsPath);

		GridAccountConfigurationProcessor.serializeIRODSAccountListToFile(
				testFile, irodsAccounts);
		List<IRODSAccount> actual = GridAccountConfigurationProcessor
				.deserializeIRODSAccountListFromFile(testFile);
		Assert.assertEquals("should get empty account list", 0, actual.size());

	}

}
