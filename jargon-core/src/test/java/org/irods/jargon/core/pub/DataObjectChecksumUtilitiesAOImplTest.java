package org.irods.jargon.core.pub;

import java.util.Properties;

import org.irods.jargon.core.checksum.ChecksumValue;
import org.irods.jargon.core.connection.DiscoveredServerPropertiesCache;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class DataObjectChecksumUtilitiesAOImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "DataObjectChecksumUtilitiesAOImplTest";
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
		SettableJargonProperties jargonProps = new SettableJargonProperties();
		// turn off native checksumming
		jargonProps.setComputeAndVerifyChecksumAfterTransfer(false);
		jargonProps.setComputeChecksumAfterTransfer(false);
		irodsFileSystem.getIrodsSession().setJargonProperties(jargonProps);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testRetrieveExistingChecksumForDataObject() throws Exception {
		// generate a local scratch file
		String testFileName = "testChecksum.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);

		TransferControlBlock tcb = accessObjectFactory.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setComputeAndVerifyChecksumAfterTransfer(true);

		dto.putOperation(fileNameOrig, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, tcb);

		IRODSFile testFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		DataObjectChecksumUtilitiesAO dataObjectChecksumUtilitiesAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectChecksumUtilitiesAO(irodsAccount);
		ChecksumValue actual = dataObjectChecksumUtilitiesAO
				.retrieveExistingChecksumForDataObject(testFile.getAbsolutePath());
		Assert.assertNotNull("checksum missing", actual);

	}

	@Ignore
	public void testRetrieveNonExistingChecksumForDataObject() throws Exception {

		// generate a local scratch file
		String testFileName = "testRetrieveNonExistingChecksumForDataObjectx.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(fileNameOrig, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		IRODSFile testFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		DataObjectChecksumUtilitiesAO dataObjectChecksumUtilitiesAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectChecksumUtilitiesAO(irodsAccount);
		ChecksumValue actual = dataObjectChecksumUtilitiesAO
				.retrieveExistingChecksumForDataObject(testFile.getAbsolutePath());
		Assert.assertNull("null checksum expected", actual);

	}

	@Test
	public void testComputeChecksumOnDataObject() throws Exception {
		// generate a local scratch file
		String testFileName = "testComputeChecksumOnDataObject.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(fileNameOrig, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		IRODSFile testFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		DataObjectChecksumUtilitiesAO dataObjectChecksumUtilitiesAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectChecksumUtilitiesAO(irodsAccount);
		ChecksumValue actual = dataObjectChecksumUtilitiesAO.computeChecksumOnDataObject(testFile);
		Assert.assertNotNull("did not get checksum", actual);

	}

	/**
	 * test for https://github.com/DICE-UNC/jargon/issues/173
	 *
	 * @throws Exception
	 */

	@Test
	public void testComputeChecksumOnDataObjectSpecifyMD5() throws Exception {
		// generate a local scratch file
		String testFileName = "testComputeChecksumOnDataObjectSpecifyMD5.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		SettableJargonProperties jargonProps = new SettableJargonProperties();
		irodsFileSystem.getIrodsSession().setJargonProperties(jargonProps);
		irodsFileSystem.getIrodsSession().getDiscoveredServerPropertiesCache().deleteCachedIRODSServerProperties(
				irodsAccount.getHost(), irodsAccount.getZone(), DiscoveredServerPropertiesCache.CHECKSUM_TYPE);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(fileNameOrig, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		IRODSFile testFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		DataObjectChecksumUtilitiesAO dataObjectChecksumUtilitiesAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectChecksumUtilitiesAO(irodsAccount);
		ChecksumValue actual = dataObjectChecksumUtilitiesAO.computeChecksumOnDataObject(testFile);
		Assert.assertNotNull("did not get checksum", actual);

	}

	@Test
	public void testVerifyLocalFileAgainstIrodsFileChecksum() throws Exception {
		// generate a local scratch file
		String testFileName = "testVerifyLocalFileAgainstIrodsFileChecksum.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(fileNameOrig, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		IRODSFile testFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		DataObjectChecksumUtilitiesAO dataObjectChecksumUtilitiesAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectChecksumUtilitiesAO(irodsAccount);
		ChecksumValue actual = dataObjectChecksumUtilitiesAO.verifyLocalFileAgainstIrodsFileChecksum(fileNameOrig,
				testFile.getAbsolutePath());
		Assert.assertNotNull(actual);

	}
}
