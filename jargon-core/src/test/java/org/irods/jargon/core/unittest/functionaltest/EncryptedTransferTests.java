/**
 *
 */
package org.irods.jargon.core.unittest.functionaltest;

import java.io.File;
import java.util.Properties;

import org.irods.jargon.core.connection.AuthScheme;
import org.irods.jargon.core.connection.ClientServerNegotiationPolicy.SslNegotiationPolicy;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.testutils.IRODSTestAssertionException;
import org.irods.jargon.testutils.TestConfigurationException;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Functional tests of various permutations of ssl negotiation and auth methods.
 * These tests are contingent on the iRODS configuration and various testing
 * properties settings
 *
 * @author Mike Conway - DICE
 *
 */
public class EncryptedTransferTests {

	private static Properties testingProperties = new Properties();
	private static JargonProperties jargonOriginalProperties = null;
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "EncryptedParallelTransferTests";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		irodsFileSystem = IRODSFileSystem.instance();
		SettableJargonProperties settableJargonProperties = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		settableJargonProperties.setInternalCacheBufferSize(-1);
		settableJargonProperties.setInternalOutputStreamBufferSize(65535);
		jargonOriginalProperties = settableJargonProperties;
		irodsFileSystem.getIrodsSession().setJargonProperties(settableJargonProperties);
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(testingProperties);
		scratchFileUtils.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.clearIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
	}

	@AfterClass
	public static void afterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Before
	public void before() throws Exception {
		// be sure that normal parallel stuff is set up
		irodsFileSystem.getIrodsSession().setJargonProperties(jargonOriginalProperties);
	}

	@Test
	public void testParallelTransferWithAesEncryptionSet()
			throws JargonException, TestConfigurationException, IRODSTestAssertionException {

		/*
		 * Only run if ssl enabled
		 */
		if (!testingPropertiesHelper.isTestSsl(testingProperties)) {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		irodsAccount.setAuthenticationScheme(AuthScheme.STANDARD);

		SettableJargonProperties settableJargonProperties = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		settableJargonProperties.setNegotiationPolicy(SslNegotiationPolicy.CS_NEG_REQUIRE);
		irodsFileSystem.getIrodsSession().setJargonProperties(settableJargonProperties);

		String testFileName = "testPutOneFile.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				320 * 1024 * 1024);

		String targetIrodsPath = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		TransferControlBlock tcb = irodsFileSystem.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setComputeAndVerifyChecksumAfterTransfer(true);

		dataTransferOperationsAO.putOperation(localFileName, targetIrodsPath,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, tcb);

	}

	@Test
	public void testParallelTransferRoundTripWithAesEncryptionSet()
			throws JargonException, TestConfigurationException, IRODSTestAssertionException {

		/*
		 * Only run if ssl enabled
		 */
		if (!testingPropertiesHelper.isTestSsl(testingProperties)) {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		irodsAccount.setAuthenticationScheme(AuthScheme.STANDARD);

		SettableJargonProperties settableJargonProperties = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		settableJargonProperties.setNegotiationPolicy(SslNegotiationPolicy.CS_NEG_REQUIRE);
		irodsFileSystem.getIrodsSession().setJargonProperties(settableJargonProperties);

		String testFileName = "testParallelTransferRoundTripWithAesEncryptionSet.txt";
		String testGetFileTargetName = "testParallelTransferRoundTripWithAesEncryptionSetGetTarget.txt";

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				37 * 1024 * 1024);

		String targetIrodsPath = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		TransferControlBlock tcb = irodsFileSystem.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setComputeAndVerifyChecksumAfterTransfer(true);

		dataTransferOperationsAO.putOperation(localFileName, targetIrodsPath,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, tcb);

		String localGetFileAbsolutePath = absPath + testGetFileTargetName;

		dataTransferOperationsAO.getOperation(targetIrodsPath, localGetFileAbsolutePath, "", null, tcb);
		File localFile = new File(localGetFileAbsolutePath);
		Assert.assertTrue("file does not exist after get", localFile.exists());

		long checksum1 = scratchFileUtils.computeFileCheckSumViaAbsolutePath(localGetFileAbsolutePath);
		long checksum2 = scratchFileUtils.computeFileCheckSumViaAbsolutePath(localFileName);
		Assert.assertEquals("checksums don't match on two local files after roundtrip", checksum1, checksum2);

	}

	@Ignore
	// localized test will save for now
	public void testParallelTransferWithAesEncryptionSetAllAs()
			throws JargonException, TestConfigurationException, IRODSTestAssertionException {

		/*
		 * Only run if ssl enabled
		 */
		if (!testingPropertiesHelper.isTestSsl(testingProperties)) {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		irodsAccount.setAuthenticationScheme(AuthScheme.STANDARD);

		SettableJargonProperties settableJargonProperties = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		settableJargonProperties.setNegotiationPolicy(SslNegotiationPolicy.CS_NEG_REQUIRE);
		irodsFileSystem.getIrodsSession().setJargonProperties(settableJargonProperties);

		String localFileName = "/home/mconway/temp/ssltest/the_file.txt";
		String targetIrodsPath = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + "afile.txt");
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);

		dataTransferOperationsAO.putOperation(localFileName, targetIrodsPath,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

	}

	/**
	 * Test for https://github.com/DICE-UNC/jargon/issues/228
	 *
	 * @throws JargonException
	 * @throws TestConfigurationException
	 * @throws IRODSTestAssertionException
	 */
	@Test
	public void testParallelTransferSetNoNegotiationBug228()
			throws JargonException, TestConfigurationException, IRODSTestAssertionException {

		/*
		 * Only run if ssl enabled
		 */
		if (!testingPropertiesHelper.isTestSsl(testingProperties)) {
			return;
		}

		/*
		 * Only run if pam enabled
		 */
		if (!testingPropertiesHelper.isTestPAM(testingProperties)) {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper.buildPamIrodsAccountFromTestProperties(testingProperties);
		irodsAccount.setAuthenticationScheme(AuthScheme.PAM);

		SettableJargonProperties settableJargonProperties = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		settableJargonProperties.setNegotiationPolicy(SslNegotiationPolicy.CS_NEG_DONT_CARE);
		settableJargonProperties.setComputeAndVerifyChecksumAfterTransfer(true);
		irodsFileSystem.getIrodsSession().setJargonProperties(settableJargonProperties);

		long length = 2l * 1024l * 1024l * 1024l;
		String testFileName = "testParallelTransferSetNoNegotiationBug228.txt";
		String returnedTestFileName = "testParallelTransferSetNoNegotiationBug228Get.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, length);

		String targetIrodsPath = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromPamTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);

		IRODSFile collFile = irodsFileSystem.getIRODSAccessObjectFactory().getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsPath);
		IRODSFile parentFile = (IRODSFile) collFile.getParentFile();
		parentFile.delete();
		parentFile.mkdirs();
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);

		dataTransferOperationsAO.putOperation(localFileName, targetIrodsPath, "", null, null);

		File getFile = new File(absPath, returnedTestFileName);
		File getFileParent = getFile.getParentFile();
		getFileParent.delete();
		getFileParent.mkdirs();
		dataTransferOperationsAO.getOperation(targetIrodsPath, getFile.getAbsolutePath(), "", null, null);

		// checksum verification is in place, so no error = success

	}

	@Test
	public void testNormalTransferWithAesEncryptionSet()
			throws JargonException, TestConfigurationException, IRODSTestAssertionException {

		/*
		 * Only run if ssl enabled
		 */
		if (!testingPropertiesHelper.isTestSsl(testingProperties)) {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		irodsAccount.setAuthenticationScheme(AuthScheme.STANDARD);

		SettableJargonProperties settableJargonProperties = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		settableJargonProperties.setNegotiationPolicy(SslNegotiationPolicy.CS_NEG_REQUIRE);
		irodsFileSystem.getIrodsSession().setJargonProperties(settableJargonProperties);

		String testFileName = "testNormalTransferWithAesEncryptionSet.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				25 * 1024 * 1024);

		String targetIrodsPath = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);

		dataTransferOperationsAO.putOperation(localFileName, targetIrodsPath, "", null, null);

	}

}
