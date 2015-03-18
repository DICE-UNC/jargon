package org.irods.jargon.datautils.connectiontester;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.datautils.connectiontester.ConnectionTester.TestType;
import org.irods.jargon.datautils.connectiontester.TestResultEntry.OperationType;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConnectionTesterImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "ConnectionTesterImplTest";
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
	public void testRunTestsSmall() throws Exception {
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		// now put the file
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsFile);
		irodsFile.mkdirs();

		File localFile = new File(absPath);
		localFile.mkdirs();

		ConnectionTesterConfiguration connectionTesterConfiguration = new ConnectionTesterConfiguration();
		connectionTesterConfiguration.setCleanupOnCompletion(true);
		connectionTesterConfiguration.setIrodsParentDirectory(targetIrodsFile);
		connectionTesterConfiguration.setLocalSourceParentDirectory(absPath);

		ConnectionTester connectionTester = new ConnectionTesterImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount,
				connectionTesterConfiguration);

		List<TestType> testTypes = new ArrayList<TestType>();
		testTypes.add(TestType.SMALL);

		ConnectionTestResult actual = connectionTester.runTests(testTypes);
		Assert.assertNotNull("null result", actual);
		Assert.assertEquals("did not set two entries", 2, actual
				.getTestResults().size());

		TestResultEntry putResult = actual.getTestResults().get(0);

		Assert.assertEquals("did not get put result", OperationType.PUT,
				putResult.getOperationType());
		Assert.assertTrue(putResult.isSuccess());
		Assert.assertTrue(putResult.getTransferRateBytesPerSecond() > 0);

		TestResultEntry getResult = actual.getTestResults().get(1);

		Assert.assertEquals("did not get get result", OperationType.GET,
				getResult.getOperationType());
		Assert.assertTrue(getResult.isSuccess());
		Assert.assertTrue(getResult.getTransferRateBytesPerSecond() > 0);

	}

	@Test
	public void testRunTestsSmallAndLarge() throws Exception {
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		// now put the file
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsFile);
		irodsFile.mkdirs();

		File localFile = new File(absPath);
		localFile.mkdirs();

		ConnectionTesterConfiguration connectionTesterConfiguration = new ConnectionTesterConfiguration();
		connectionTesterConfiguration.setCleanupOnCompletion(true);
		connectionTesterConfiguration.setIrodsParentDirectory(targetIrodsFile);
		connectionTesterConfiguration.setLocalSourceParentDirectory(absPath);

		ConnectionTester connectionTester = new ConnectionTesterImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount,
				connectionTesterConfiguration);

		List<TestType> testTypes = new ArrayList<TestType>();
		testTypes.add(TestType.SMALL);
		testTypes.add(TestType.LARGE);

		ConnectionTestResult actual = connectionTester.runTests(testTypes);
		Assert.assertNotNull("null result", actual);
		Assert.assertEquals("did not set four entries", 4, actual
				.getTestResults().size());

	}

}
