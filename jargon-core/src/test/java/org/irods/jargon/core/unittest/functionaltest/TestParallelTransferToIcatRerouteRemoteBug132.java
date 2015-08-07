package org.irods.jargon.core.unittest.functionaltest;

import java.io.File;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for https://github.com/DICE-UNC/jargon/issues/132
 * 
 * sys len read error parallel transfer 4.0.3 iRODS / PAM, remote storage
 * resource #132
 * 
 * @author Mike Conway - DICE
 * 
 */
public class TestParallelTransferToIcatRerouteRemoteBug132 {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "TestParallelTransferToIcatRerouteRemoteBug132x";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		irodsFileSystem = IRODSFileSystem.instance();

		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testBugCase() throws Exception {

		if (!testingPropertiesHelper
				.isTestDistributedResources(testingProperties)) {
			return;
		}
		String testFileName = "testBugCase.txt";
		long testFileLength = 49853441;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						testFileLength);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		irodsAccount
				.setDefaultStorageResource(testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_TERTIARY_RESOURCE_KEY));

		SettableJargonProperties jargonProperties = new SettableJargonProperties();
		jargonProperties.setUseTransferThreadsPool(false);
		jargonProperties.setLongTransferRestart(false);
		jargonProperties.setComputeAndVerifyChecksumAfterTransfer(true);
		irodsFileSystem.getIrodsSession().setJargonProperties(jargonProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File localSourceFile = new File(localFileName);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		destFile.setResource(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_TERTIARY_RESOURCE_KEY));

		dataTransferOperationsAO.putOperation(localSourceFile, destFile, null,
				null);

		Assert.assertTrue(destFile.exists());
		Assert.assertEquals(testFileLength, destFile.length());

	}

	@Test
	public void testBugCasePam() throws Exception {

		String testName = "testBugCasePam";

		if (!testingPropertiesHelper
				.isTestDistributedResources(testingProperties)) {
			return;
		}

		if (!testingPropertiesHelper.isTestPAM(testingProperties)) {
			return;
		}

		String testFileName = "testBugCasePam.txt";
		long testFileLength = 49853441;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						testFileLength);

		String targetIrodsColl = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromPamTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testName);

		String targetIrodsFile = targetIrodsColl;
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildPamIrodsAccountFromTestProperties(testingProperties);
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		destFile.deleteWithForceOption();
		irodsAccount
				.setDefaultStorageResource(testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_TERTIARY_RESOURCE_KEY));

		SettableJargonProperties jargonProperties = new SettableJargonProperties();
		jargonProperties.setUseTransferThreadsPool(false);
		jargonProperties.setLongTransferRestart(false);
		jargonProperties.setComputeAndVerifyChecksumAfterTransfer(true);
		irodsFileSystem.getIrodsSession().setJargonProperties(jargonProperties);

		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File localSourceFile = new File(localFileName);

		destFile.setResource(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_TERTIARY_RESOURCE_KEY));

		dataTransferOperationsAO.putOperation(localSourceFile, destFile, null,
				null);

		Assert.assertTrue(destFile.exists());
		Assert.assertEquals(testFileLength, destFile.length());

	}

}
