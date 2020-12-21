package org.irods.jargon.core.pub;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.connection.SettableJargonPropertiesMBean;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.domain.Resource;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.transfer.DefaultTransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatusCallbackListenerTestingImplementation;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test manipulates the jargon properties so that the internal cache mechanism
 * used within jargon is defeated and standard i/o buffering is used
 *
 * @author Mike Conway - DICE (www.irods.org) see http://code.renci.org for
 *         trackers, access info, and documentation
 *
 */
public class DataTransferOperationsImplTestNoInternalCache {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "DataTransferOperationsImplTestNoInternalCache";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static org.irods.jargon.testutils.AssertionHelper assertionHelper = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		irodsFileSystem = IRODSFileSystem.instance();

		SettableJargonPropertiesMBean settableJargonProperties = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		settableJargonProperties.setInternalCacheBufferSize(0);
		settableJargonProperties.setInternalOutputStreamBufferSize(0);
		irodsFileSystem.getIrodsSession().setJargonProperties(settableJargonProperties);

		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(testingProperties);
		scratchFileUtils.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.clearIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		assertionHelper = new org.irods.jargon.testutils.AssertionHelper();
	}

	@AfterClass
	public static void afterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Ignore // FIXME: https://github.com/DICE-UNC/jargon/issues/374
	public void testPhysicalMove() throws Exception {
		String testFileName = "testPhysicalMove.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(fileNameOrig, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		dto.physicalMove(irodsFile.getAbsolutePath(),
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY));

		DataObjectAO dataObjectAO = accessObjectFactory.getDataObjectAO(irodsAccount);
		DataObject actual = dataObjectAO.findByAbsolutePath(irodsFile.getAbsolutePath());

		Assert.assertEquals("file is not in new resource",
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY),
				actual.getResourceName());
	}

	@Test
	public void testMoveSourceFileTargetFile() throws Exception {
		String testFileName = "testMoveSourceFileTargetFile1.txt";
		String newTestFileName = "testMoveSourceFileTargetFileIWasMoved1.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		DataTransferOperations dataTransferOperations = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);

		dataTransferOperations.putOperation(fileNameOrig, targetIrodsCollection, "", null, null);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		IRODSFile irodsDestFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + '/' + newTestFileName);

		dataTransferOperations.move(irodsFile.getAbsolutePath(), irodsDestFile.getAbsolutePath());

		Assert.assertFalse(irodsFile.exists());
		Assert.assertTrue(irodsDestFile.exists());

	}

	@Test
	public void testMoveSourceFileTargetCollection() throws Exception {
		String testFileName = "testMoveSourceFileTargetCollection.txt";
		String targetCollection = "testMoveSourceFileTargetCollection-TargetCollection";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dataTransferOperations.putOperation(fileNameOrig, targetIrodsCollection, "", null, null);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		// make the target irods collection
		IRODSFile targetCollectionFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + '/' + targetCollection);
		targetCollectionFile.mkdir();

		dataTransferOperations.move(irodsFile.getAbsolutePath(), targetCollectionFile.getAbsolutePath());

		IRODSFile actualFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetCollectionFile.getAbsolutePath() + '/' + testFileName);

		Assert.assertTrue("did not find the newly moved file", actualFile.exists());
		Assert.assertFalse("did not move source file, still exists", irodsFile.exists());

	}

	@Test
	public void testMoveCollection() throws Exception {

		// generate a local scratch file
		String testOrigDirectory = "origdirectory";
		String testNewDirectory = "newdirectory";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + '/' + testOrigDirectory);

		irodsFile.mkdirs();

		IRODSFile irodsDestFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + '/' + testNewDirectory);

		DataTransferOperations dataTransferOperations = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dataTransferOperations.move(irodsFile.getAbsolutePath(), irodsDestFile.getAbsolutePath());

		Assert.assertFalse(irodsFile.exists());
		Assert.assertTrue(irodsDestFile.exists());

	}

	@Test
	public void testPutOneFile() throws Exception {
		// generate a local scratch file
		String testFileName = "testPutOneFile.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 32 * 1024);

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
		assertionHelper.assertIrodsFileMatchesLocalFileChecksum(destFile.getAbsolutePath(), localFile.getAbsolutePath(),
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
	}

	@Ignore
	public void testGetBigFileWhenNoParallelIsPolicy() throws Exception {
		// generate a local scratch file
		String testFileName = "testGet1point5GBFileWhenNoParallelIsPolicy.txt";
		String testRetrievedFileName = "testGet1point5GBFileWhenNoParallelIsPolicyRetrieved.txt";
		long length = 32 * 1024 * 1024;

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, length);

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		File localFile = new File(localFileName);

		// now put the file
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		IRODSFile getIRODSFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		File getLocalFile = new File(absPath + "/" + testRetrievedFileName);
		TestingStatusCallbackListener testCallbackListener = new TestingStatusCallbackListener();

		// now get the file
		dataTransferOperationsAO.getOperation(getIRODSFile, getLocalFile, testCallbackListener, null);

		assertionHelper.assertIrodsFileMatchesLocalFileChecksum(getIRODSFile.getAbsolutePath(),
				getLocalFile.getAbsolutePath(), irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		Assert.assertEquals("did not expect any errors", 0, testCallbackListener.getErrorCallbackCount());
		Assert.assertEquals("file callback, initial and completion", 3, testCallbackListener.getSuccessCallbackCount());
		Assert.assertEquals("did not get the full irods file name in callback", targetIrodsFile,
				testCallbackListener.getLastSourcePath());
		Assert.assertEquals("did not get the full local file name in callback", getLocalFile.getAbsolutePath(),
				testCallbackListener.getLastTargetPath());

	}

	@Test
	public void testGetCollectionWithTwoFilesNoCallbacks() throws Exception {

		String rootCollection = "testGetCollectionWithTwoFilesNoCallbacks";
		String returnedLocalCollection = "testGetCollectionWithTwoFilesNoCallbacksReturnedLocalFiles";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH); // TODO:
																												// add
		// test with
		// trailing
		// '/'

		FileGenerator.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(localCollectionAbsolutePath,
				"testGetCollectionWithTwoFilesNoCallbacks", 1, 1, 1, "testFile", ".txt", 2, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
		destFile.close();

		destFile = irodsFileFactory.instanceIRODSFile(irodsCollectionRootAbsolutePath + "/" + rootCollection);

		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(localFile, (File) destFile);

		// now get the files into a local return collection and verify

		IRODSFile getIrodsFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath + "/" + rootCollection);
		String returnLocalCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/' + returnedLocalCollection);
		File returnLocalFile = new File(returnLocalCollectionAbsolutePath);

		dataTransferOperationsAO.getOperation(getIrodsFile, returnLocalFile, null, null);

		String returnLocalCollectionCompareAbsolutePath = scratchFileUtils.createAndReturnAbsoluteScratchPath(
				IRODS_TEST_SUBDIR_PATH + '/' + returnedLocalCollection + '/' + rootCollection);
		File returnCompareLocalFile = new File(returnLocalCollectionCompareAbsolutePath);

		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(localFile, returnCompareLocalFile);
	}

	@Test
	public void testPutMultipleCollectionsMultipleFiles() throws Exception {

		String rootCollection = "testPutMultipleCollectionsMultipleFiles";
		String returnedCollection = "testPutMultipleCollectionsMultipleFilesReturned";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/' + rootCollection);

		String returnedCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/' + returnedCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(localCollectionAbsolutePath,
				"testPutCollectionWithTwoFiles", 3, 5, 3, "testFile", ".txt", 10, 9, 20, 200);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
		destFile.close();

		// File returnedData = new File(returnedCollectionAbsolutePath + "/" +
		// rootCollection);

		dataTransferOperationsAO.getOperation(destFile.getAbsolutePath() + "/" + rootCollection,
				returnedCollectionAbsolutePath, "", null, null);

		File returnedData = new File(returnedCollectionAbsolutePath + "/" + rootCollection);
		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(localFile, returnedData);
	}

	@Test
	public void testParallelPutThenGetOneFile() throws Exception {

		if (!testingPropertiesHelper.isTestParallelTransfer(testingProperties)) {
			return;
		}

		// generate a local scratch file
		String testFileName = "testPutThenGetOneFile.txt";
		String testRetrievedFileName = "testPutThenGetOneFileRetreived.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				33 * 1024 * 1024);

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		// now get
		File retrievedLocalFile = new File(absPath + testRetrievedFileName);
		dataTransferOperationsAO.getOperation(destFile, retrievedLocalFile, null, null);

		// compare checkums

		long origChecksum = scratchFileUtils.computeFileCheckSumViaAbsolutePath(localFile.getAbsolutePath());
		long retrievedChecksum = scratchFileUtils.computeFileCheckSumViaAbsolutePath(localFile.getAbsolutePath());

		// byte[] retrievedChecksum =
		// scratchFileUtils.computeFileCheckSumViaAbsolutePath(retrievedLocalFile.getAbsolutePath());

		Assert.assertEquals(origChecksum, retrievedChecksum);
	}

	@Test
	public void testPutThenGetMultipleCollectionsMultipleFiles() throws Exception {

		String rootCollection = "testPutThenGetMultipleCollectionsMultipleFiles";
		String returnCollection = "testPutThenGetMultipleCollectionsMultipleFilesReturn";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/' + rootCollection);

		String returnCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/' + returnCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(localCollectionAbsolutePath,
				"testPutThenGetMultipleCollectionsMultipleFiles", 2, 3, 2, "testFile", ".txt", 3, 2, 20, 200);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		irodsFileSystem = IRODSFileSystem.instance();
		irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		destFile = irodsFileFactory.instanceIRODSFile(irodsCollectionRootAbsolutePath);

		// I've put a mess of stuff, now get it

		File returnLocalFile = new File(returnCollectionAbsolutePath);
		destFile = irodsFileFactory.instanceIRODSFile(irodsCollectionRootAbsolutePath + '/' + rootCollection);

		dataTransferOperationsAO.getOperation(destFile, returnLocalFile, null, null);

		File resultOfPutFile = new File(returnCollectionAbsolutePath + '/' + rootCollection);

		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(localFile, resultOfPutFile);
	}

	@Test
	public void testReplicateOneFile() throws Exception {
		// generate a local scratch file
		String testFileName = "testReplicateOneFile.doc";
		String targetResource = testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY);
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);

		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();

		dataTransferOperationsAO.putOperation(localFile, destFile, transferStatusCallbackListener, null);

		// now replicate
		dataTransferOperationsAO.replicate(targetIrodsFile, targetResource, transferStatusCallbackListener,
				DefaultTransferControlBlock.instance());

		// check for two resources

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		List<Resource> resources = dataObjectAO.listFileResources(targetIrodsFile);

		Assert.assertEquals("should be two resources for this file", 2, resources.size());

		Assert.assertEquals("did not get expected success callbacks", 1,
				transferStatusCallbackListener.getReplicateCallbackCtr());
	}

	@Test
	public void testReplicateMultipleCollectionsMultipleFilesWithCallbacks() throws Exception {

		String rootCollection = "testReplicateMultipleCollectionsMultipleFilesWithCallbacks";
		String targetResource = testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY);

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(localCollectionAbsolutePath,
				"rootCollection", 1, 2, 2, "testFile", ".txt", 4, 2, 20, 200);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);

		File localFile = new File(localCollectionAbsolutePath);

		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(irodsCollectionRootAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, irodsFile, null, null);

		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();

		// now replicate
		dataTransferOperationsAO.replicate(irodsCollectionRootAbsolutePath + "/" + rootCollection, targetResource,
				transferStatusCallbackListener, DefaultTransferControlBlock.instance());

		Assert.assertTrue("did not get expected success callback",
				transferStatusCallbackListener.getReplicateCallbackCtr() > 0);
		Assert.assertTrue("did not get expected exception callback",
				transferStatusCallbackListener.getExceptionCallbackCtr() == 0);
	}

	@Test
	public void testPutCollectionWithTwoFiles() throws Exception {

		String rootCollection = "testPutCollectionWithTwoFiles";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH); // TODO:
																												// add
		// test with
		// trailing
		// '/'

		FileGenerator.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(localCollectionAbsolutePath,
				"testPutCollectionWithTwoFiles", 1, 1, 1, "testFile", ".txt", 2, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
		destFile.close();

		irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		destFile = irodsFileFactory.instanceIRODSFile(irodsCollectionRootAbsolutePath + "/" + rootCollection);

		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(localFile, (File) destFile);
	}

}
