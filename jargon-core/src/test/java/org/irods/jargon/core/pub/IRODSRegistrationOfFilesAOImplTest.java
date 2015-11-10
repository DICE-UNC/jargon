package org.irods.jargon.core.pub;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.exception.CollectionNotEmptyException;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Note that these tests assume localhost right now and will just be ignored if
 * running against a remote host
 *
 * @author mconway
 *
 */
public class IRODSRegistrationOfFilesAOImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IRODSRegistrationOfFilesAOImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static org.irods.jargon.testutils.AssertionHelper assertionHelper = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		irodsFileSystem = IRODSFileSystem.instance();
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(
				testingProperties);
		scratchFileUtils
		.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.clearIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
		.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		assertionHelper = new org.irods.jargon.testutils.AssertionHelper();
	}

	@AfterClass
	public static void afterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public final void testIRODSRegistrationOfFilesAOImpl() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		irodsFileSystem.getIRODSAccessObjectFactory()
		.getIRODSRegistrationOfFilesAO(irodsAccount);
	}

	@Test
	public final void testRegisterPhysicalCollectionRecursivelyToIRODS()
			throws Exception {

		if (testingPropertiesHelper.isTestRegistration(testingProperties)
				&& testingPropertiesHelper
				.isTestFileSystemMountLocal(testingProperties)) {
		} else {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String rootCollection = "testRegisterPhysicalCollectionRecursivelyToIRODS";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
		.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
				localCollectionAbsolutePath,
				"testPutCollectionWithTwoFiles", 1, 1, 1, "testFile",
				".txt", 2, 2, 1, 2);

		IRODSRegistrationOfFilesAO ao = irodsFileSystem
				.getIRODSAccessObjectFactory().getIRODSRegistrationOfFilesAO(
						irodsAccount);

		ao.registerPhysicalCollectionRecursivelyToIRODS(
				localCollectionAbsolutePath,
				targetIrodsCollection,
				false,
				testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				"");

		IRODSFile parentFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection
						+ "/testPutCollectionWithTwoFileslvl1nbr0");
		Assert.assertTrue("irodsCollection does not exist", parentFile.exists());

	}

	@Test
	public final void testRegisterPhysicalDataFileToIRODS() throws Exception {

		if (testingPropertiesHelper.isTestRegistration(testingProperties)
				&& testingPropertiesHelper
				.isTestFileSystemMountLocal(testingProperties)) {
		} else {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSRegistrationOfFilesAO ao = irodsFileSystem
				.getIRODSAccessObjectFactory().getIRODSRegistrationOfFilesAO(
						irodsAccount);

		String testFileName = "testRegisterPhysicalDataFileToIRODS.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		ao.registerPhysicalDataFileToIRODS(fileNameOrig, targetIrodsCollection
				+ "/" + testFileName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), "",
				false);

		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsCollection
				+ "/" + testFileName,
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
	}

	/**
	 * Register a file as an overwrite situation
	 *
	 * @throws Exception
	 */
	@Test(expected = DuplicateDataException.class)
	public final void testRegisterPhysicalDataFileToIRODSTwiceNoForce()
			throws Exception {

		if (testingPropertiesHelper.isTestRegistration(testingProperties)
				&& testingPropertiesHelper
				.isTestFileSystemMountLocal(testingProperties)) {
		} else {
			throw new DuplicateDataException(
					"throw to get expected while skipping");
		}

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSRegistrationOfFilesAO ao = irodsFileSystem
				.getIRODSAccessObjectFactory().getIRODSRegistrationOfFilesAO(
						irodsAccount);

		String testFileName = "testRegisterPhysicalDataFileToIRODSTwiceNoForce.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		ao.registerPhysicalDataFileToIRODS(fileNameOrig, targetIrodsCollection
				+ "/" + testFileName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), "",
				false);

		ao.registerPhysicalDataFileToIRODS(fileNameOrig, targetIrodsCollection
				+ "/" + testFileName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), "",
				false);
	}

	/**
	 * Call a method to register a file when the local file is a collection
	 *
	 * @throws Exception
	 */
	@Test(expected = JargonException.class)
	public final void testRegisterPhysicalDataFileToIRODSWhenCollection()
			throws Exception {
		if (testingPropertiesHelper.isTestRegistration(testingProperties)
				&& testingPropertiesHelper
				.isTestFileSystemMountLocal(testingProperties)) {
		} else {
			throw new JargonException("throw to honor expected error");

		}

		String testFileName = "testRegisterPhysicalDataFileToIRODSWhenCollection.txt";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSRegistrationOfFilesAO ao = irodsFileSystem
				.getIRODSAccessObjectFactory().getIRODSRegistrationOfFilesAO(
						irodsAccount);

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		ao.registerPhysicalDataFileToIRODS(absPath, targetIrodsCollection + "/"
				+ testFileName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), "",
				false);

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testRegisterPhysicalDataFileToIRODSWhenResourceIsBlank()
			throws Exception {
		if (testingPropertiesHelper.isTestRegistration(testingProperties)
				&& testingPropertiesHelper
				.isTestFileSystemMountLocal(testingProperties)) {
		} else {

			throw new IllegalArgumentException(
					"throw to get expected exception..skipping");
		}

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSRegistrationOfFilesAO ao = irodsFileSystem
				.getIRODSAccessObjectFactory().getIRODSRegistrationOfFilesAO(
						irodsAccount);

		String testFileName = "testRegisterPhysicalDataFileToIRODSWhenResourceIsBlank.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		ao.registerPhysicalDataFileToIRODS(fileNameOrig, targetIrodsCollection
				+ "/" + testFileName, "", "", false);
	}

	/**
	 * Register a non-existent file
	 *
	 * @throws Exception
	 */
	@Test(expected = DataNotFoundException.class)
	public final void testRegisterPhysicalDataFileToIRODSLocalFileMissing()
			throws Exception {

		if (testingPropertiesHelper.isTestRegistration(testingProperties)
				&& testingPropertiesHelper
				.isTestFileSystemMountLocal(testingProperties)) {
		} else {

			throw new DataNotFoundException(
					"throw to get expected exception..skipping");
		}
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSRegistrationOfFilesAO ao = irodsFileSystem
				.getIRODSAccessObjectFactory().getIRODSRegistrationOfFilesAO(
						irodsAccount);

		String testFileName = "testRegisterPhysicalDataFileToIRODSLocalFileMissing.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		ao.registerPhysicalDataFileToIRODS(
				absPath + "/" + testFileName,
				targetIrodsCollection + "/" + testFileName,
				testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				"", false);
	}

	/**
	 * The irods parent collection is a subdir that does not exist
	 *
	 * @throws Exception
	 */
	@Test(expected = DataNotFoundException.class)
	public final void testRegisterPhysicalDataFileToIRODSIRODSParentMissing()
			throws Exception {

		if (testingPropertiesHelper.isTestRegistration(testingProperties)
				&& testingPropertiesHelper
				.isTestFileSystemMountLocal(testingProperties)) {
		} else {
			throw new DataNotFoundException("honor expected");
		}

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSRegistrationOfFilesAO ao = irodsFileSystem
				.getIRODSAccessObjectFactory().getIRODSRegistrationOfFilesAO(
						irodsAccount);

		String testFileName = "testRegisterPhysicalDataFileToIRODSIRODSParentMissing.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties,
						IRODS_TEST_SUBDIR_PATH
						+ "/testRegisterPhysicalDataFileToIRODSIRODSParentMissing");

		ao.registerPhysicalDataFileToIRODS(fileNameOrig, targetIrodsCollection
				+ "/" + testFileName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), "",
				false);
	}

	/**
	 * register a file and ask a checksum to be registered as well
	 *
	 * @throws Exception
	 */
	@Test
	public final void testRegisterPhysicalDataFileToIRODSRegisterChecksum()
			throws Exception {

		if (testingPropertiesHelper.isTestRegistration(testingProperties)
				&& testingPropertiesHelper
				.isTestFileSystemMountLocal(testingProperties)) {
		} else {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSRegistrationOfFilesAO ao = irodsFileSystem
				.getIRODSAccessObjectFactory().getIRODSRegistrationOfFilesAO(
						irodsAccount);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();

		// test is only valid for 3.1
		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.1")) {
			irodsFileSystem.closeAndEatExceptions();
			return;
		}

		String testFileName = "testRegisterPhysicalDataFileToIRODSRegisterChecksum.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		ao.registerPhysicalDataFileToIRODS(fileNameOrig, targetIrodsCollection
				+ "/" + testFileName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), "",
				true);

		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsCollection
				+ "/" + testFileName,
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		DataObject dataObject = dataObjectAO
				.findByAbsolutePath(targetIrodsCollection + "/" + testFileName);
		Assert.assertNotNull("did not find the data object", dataObject);
		Assert.assertFalse("did not register a checksum", dataObject
				.getChecksum().isEmpty());
	}

	@Test
	public final void testRegisterPhysicalDataFileToIRODSWithVerifyLocalChecksum()
			throws Exception {

		if (testingPropertiesHelper.isTestRegistration(testingProperties)
				&& testingPropertiesHelper
				.isTestFileSystemMountLocal(testingProperties)) {
		} else {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSRegistrationOfFilesAO ao = irodsFileSystem
				.getIRODSAccessObjectFactory().getIRODSRegistrationOfFilesAO(
						irodsAccount);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();

		// test is only valid for 3.1
		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.1")) {
			irodsFileSystem.closeAndEatExceptions();
			return;
		}

		String testFileName = "testRegisterPhysicalDataFileToIRODSWithVerifyLocalChecksum.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String verifiedChecksum = ao
				.registerPhysicalDataFileToIRODSWithVerifyLocalChecksum(
						fileNameOrig,
						targetIrodsCollection + "/" + testFileName,
						testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						"");

		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsCollection
				+ "/" + testFileName,
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		DataObject dataObject = dataObjectAO
				.findByAbsolutePath(targetIrodsCollection + "/" + testFileName);
		Assert.assertNotNull("did not find the data object", dataObject);
		Assert.assertFalse("did not register a checksum", dataObject
				.getChecksum().isEmpty());
		Assert.assertEquals("checksum mismatch", verifiedChecksum,
				dataObject.getChecksum());
	}

	@Test
	public final void testUnregisterButDoNotDeletePhysicalFile()
			throws Exception {

		if (testingPropertiesHelper.isTestRegistration(testingProperties)
				&& testingPropertiesHelper
				.isTestFileSystemMountLocal(testingProperties)) {
		} else {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSRegistrationOfFilesAO ao = irodsFileSystem
				.getIRODSAccessObjectFactory().getIRODSRegistrationOfFilesAO(
						irodsAccount);

		String testFileName = "testUnregisterButDoNotDeletePhysicalFile.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		ao.registerPhysicalDataFileToIRODS(fileNameOrig, targetIrodsCollection
				+ "/" + testFileName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), "",
				false);

		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsCollection
				+ "/" + testFileName,
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);

		ao.unregisterDataObject(targetIrodsCollection + "/" + testFileName);
		File localFile = new File(fileNameOrig);
		Assert.assertTrue("local file is missing", localFile.exists());
		IRODSFile targetFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection, testFileName);
		Assert.assertFalse("irods file should not exist", targetFile.exists());
	}

	/**
	 * Unregister a non-existent file, expect a 'false' return from the
	 * unregister
	 *
	 * @throws Exception
	 */
	@Test
	public final void testUnregisterDoesNotExist() throws Exception {
		if (!testingPropertiesHelper.isTestRegistration(testingProperties)) {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSRegistrationOfFilesAO ao = irodsFileSystem
				.getIRODSAccessObjectFactory().getIRODSRegistrationOfFilesAO(
						irodsAccount);

		String testFileName = "testUnregisterButDoNotDeletePhysicalFile.txt";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		boolean status = ao.unregisterDataObject(targetIrodsCollection + "/"
				+ testFileName);

		Assert.assertFalse("should have gotten a false on this delete", status);

	}

	@Ignore
	public final void testRegisterPhysicalCollectionRecursivelyToIRODSAsAReplica() {
		fail("Not yet implemented");
	}

	/**
	 * Put the file, then register again as a replica to a second resource
	 *
	 * @throws Exception
	 */
	@Test
	public final void testRegisterPhysicalDataFileToIRODSAsAReplica()
			throws Exception {

		if (testingPropertiesHelper.isTestRegistration(testingProperties)
				&& testingPropertiesHelper
				.isTestFileSystemMountLocal(testingProperties)) {
		} else {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSRegistrationOfFilesAO ao = irodsFileSystem
				.getIRODSAccessObjectFactory().getIRODSRegistrationOfFilesAO(
						irodsAccount);

		String testFileName = "testRegisterPhysicalCollectionRecursivelyToIRODSAsAReplica.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(fileNameOrig,
				targetIrodsCollection,
				irodsAccount.getDefaultStorageResource(), null, null);

		ao.registerPhysicalDataFileToIRODSAsAReplica(
				fileNameOrig,
				targetIrodsCollection + "/" + testFileName,
				testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY),
				"", false);

		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsCollection
				+ "/" + testFileName,
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
	}

	/**
	 * register a file as a replica when that file doesn't yet exist
	 *
	 * @throws Exception
	 */
	@Test(expected = DataNotFoundException.class)
	public final void testRegisterPhysicalDataFileToIRODSAsAReplicaWhenFileDoesNotYetExist()
			throws Exception {

		if (!testingPropertiesHelper.isTestRegistration(testingProperties)) {

			throw new DataNotFoundException(
					"throw to match expected error when skipping");
		}

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSRegistrationOfFilesAO ao = irodsFileSystem
				.getIRODSAccessObjectFactory().getIRODSRegistrationOfFilesAO(
						irodsAccount);

		String testFileName = "testRegisterPhysicalDataFileToIRODSAsAReplicaWhenFileDoesNotYetExist.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		ao.registerPhysicalDataFileToIRODSAsAReplica(
				fileNameOrig,
				targetIrodsCollection + "/" + testFileName,
				testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY),
				"", false);

		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsCollection
				+ "/" + testFileName,
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
	}

	/**
	 * Create a nested collection and then unregister it recursively
	 *
	 * @throws Exception
	 */
	@Test
	public final void testUnregisterPhysicalCollectionRecursively()
			throws Exception {

		if (testingPropertiesHelper.isTestRegistration(testingProperties)
				&& testingPropertiesHelper
				.isTestFileSystemMountLocal(testingProperties)) {
		} else {
			return;
		}

		String rootCollection = "testUnregisterPhysicalCollectionRecursively";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		targetIrodsFile.deleteWithForceOption();

		FileGenerator
		.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
				localCollectionAbsolutePath,
				"testPutCollectionWithTwoFiles", 4, 7, 1, "testFile",
				".txt", 2, 2, 1, 2);

		IRODSRegistrationOfFilesAO ao = irodsFileSystem
				.getIRODSAccessObjectFactory().getIRODSRegistrationOfFilesAO(
						irodsAccount);

		ao.registerPhysicalCollectionRecursivelyToIRODS(
				localCollectionAbsolutePath,
				targetIrodsCollection,
				false,
				testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				"");

		ao.unregisterCollection(targetIrodsCollection, true);

		IRODSFile parentFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection
						+ "/testPutCollectionWithTwoFileslvl1nbr0");
		Assert.assertFalse("irodsCollection should not exist",
				parentFile.exists());

	}

	/**
	 * Create a nested collection and then unregister it without recursion
	 *
	 * @throws Exception
	 */
	@Test(expected = CollectionNotEmptyException.class)
	public final void testUnregisterPhysicalCollectionNoRecursive()
			throws Exception {
		if (testingPropertiesHelper.isTestRegistration(testingProperties)
				&& testingPropertiesHelper
				.isTestFileSystemMountLocal(testingProperties)) {
		} else {
			throw new CollectionNotEmptyException("throwing to match expected");
		}

		String rootCollection = "testUnregisterPhysicalCollectionNoRecursive";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		targetIrodsFile.deleteWithForceOption();

		FileGenerator
		.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
				localCollectionAbsolutePath,
				"testPutCollectionWithTwoFiles", 1, 1, 1, "testFile",
				".txt", 2, 2, 1, 2);

		IRODSRegistrationOfFilesAO ao = irodsFileSystem
				.getIRODSAccessObjectFactory().getIRODSRegistrationOfFilesAO(
						irodsAccount);

		ao.registerPhysicalCollectionRecursivelyToIRODS(
				localCollectionAbsolutePath,
				targetIrodsCollection,
				false,
				testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				"");

		ao.unregisterCollection(targetIrodsCollection, false);

		IRODSFile parentFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection
						+ "/testPutCollectionWithTwoFileslvl1nbr0");
		Assert.assertFalse("irodsCollection should not exist",
				parentFile.exists());

	}
}
