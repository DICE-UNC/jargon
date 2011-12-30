package org.irods.jargon.core.pub;

import java.io.File;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonFileOrCollAlreadyExistsException;
import org.irods.jargon.core.pub.domain.Resource;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.transfer.DefaultTransferControlBlock;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatus.TransferState;
import org.irods.jargon.core.transfer.TransferStatusCallbackListenerTestingImplementation;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.icommandinvoke.IcommandInvoker;
import org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext;
import org.irods.jargon.testutils.icommandinvoke.icommands.IlsCommand;
import org.irods.jargon.testutils.icommandinvoke.icommands.IputCommand;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class DataTransferOperationsImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "DataTransferOperationsImplTest";
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
	public void testPhysicalMove() throws Exception {
		String testFileName = "testPhysicalMove.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		// put scratch file into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		iputCommand.setLocalFileName(fileNameOrig);
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperations
				.physicalMove(
						irodsFile.getAbsolutePath(),
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY));

		IlsCommand ilsCommand = new IlsCommand();
		ilsCommand.setLongFormat(true);
		ilsCommand.setIlsBasePath(targetIrodsCollection + '/' + testFileName);
		String ilsResult = invoker
				.invokeCommandAndGetResultAsString(ilsCommand);
		Assert.assertTrue(
				"file is not in new resource",
				ilsResult.indexOf(testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY)) != -1);
	}

	@Test
	public void testMoveSourceFileTargetFile() throws Exception {
		String testFileName = "testMoveSourceFileTargetFile.txt";
		String newTestFileName = "testMoveSourceFileTargetFileIWasMoved.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(fileNameOrig,
				targetIrodsCollection, "", null, null);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		IRODSFile irodsDestFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + '/' + newTestFileName);

		dataTransferOperations.move(irodsFile.getAbsolutePath(),
				irodsDestFile.getAbsolutePath());

		Assert.assertFalse(irodsFile.exists());
		Assert.assertTrue(irodsDestFile.exists());

	}

	@Test(expected = JargonFileOrCollAlreadyExistsException.class)
	public void testMoveFileTriggerOverwrite() throws Exception {
		String testFileName = "testMoveFileTriggerOverwrite.txt";
		String newTestFileName = "testMoveFileTriggerOverwriteWasMoved.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		// put scratch file into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		iputCommand.setLocalFileName(fileNameOrig);
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		IRODSFile irodsDestFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + '/' + newTestFileName);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperations.move(irodsFile.getAbsolutePath(),
				irodsDestFile.getAbsolutePath());

		// move again, causing an overwrite
		invoker.invokeCommandAndGetResultAsString(iputCommand);
		dataTransferOperations.move(irodsFile.getAbsolutePath(),
				irodsDestFile.getAbsolutePath());

	}

	@Test
	public void testMoveSourceFileTargetCollection() throws Exception {
		String testFileName = "testMoveSourceFileTargetCollection.txt";
		String targetCollection = "testMoveSourceFileTargetCollection-TargetCollection";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperations.putOperation(fileNameOrig,
				targetIrodsCollection, "", null, null);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		// make the target irods collection
		IRODSFile targetCollectionFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + '/' + targetCollection);
		targetCollectionFile.mkdir();

		dataTransferOperations.move(irodsFile.getAbsolutePath(),
				targetCollectionFile.getAbsolutePath());

		IRODSFile actualFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetCollectionFile.getAbsolutePath() + '/'
								+ testFileName);

		Assert.assertTrue("did not find the newly moved file",
				actualFile.exists());
		Assert.assertFalse("did not move source file, still exists",
				irodsFile.exists());

	}

	@Test
	public void testMoveFileToSelf() throws Exception {
		String testFileName = "testMoveFileToSelf.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(fileNameOrig,
				targetIrodsCollection, "", null, null);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		dataTransferOperations.move(irodsFile.getAbsolutePath(),
				irodsFile.getAbsolutePath());

		IRODSFile actualFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						irodsFile.getAbsolutePath());

		Assert.assertTrue("source file should still be in place",
				actualFile.exists());

	}

	@Test(expected = IllegalArgumentException.class)
	public void testMoveFileSourceFileNotExists() throws Exception {
		String testFileName = "/testMove.txt";
		String newTestFileName = "testMoveIWasMoved.txt";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/blah");

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		IRODSFile irodsDestFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + '/' + newTestFileName);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperations.move(irodsFile.getAbsolutePath(),
				irodsDestFile.getAbsolutePath());

		Assert.assertFalse(irodsFile.exists());
		Assert.assertTrue(irodsDestFile.exists());

	}

	@Test
	public void testMoveCollection() throws Exception {

		// generate a local scratch file
		String testOrigDirectory = "origdirectory";
		String testNewDirectory = "newdirectory";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(
						targetIrodsCollection + '/' + testOrigDirectory);

		irodsFile.mkdirs();

		IRODSFile irodsDestFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + '/' + testNewDirectory);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperations.move(irodsFile.getAbsolutePath(),
				irodsDestFile.getAbsolutePath());

		Assert.assertFalse(irodsFile.exists());
		Assert.assertTrue(irodsDestFile.exists());

	}

	@Test
	public void testPutOneFile() throws Exception {
		// generate a local scratch file
		String testFileName = "testPutOverwriteFileNotInIRODS.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
		assertionHelper.assertIrodsFileMatchesLocalFileChecksum(
				destFile.getAbsolutePath(), localFile.getAbsolutePath());
	}

	@Test
	public void testPutOneFileWithResourceRerouting() throws Exception {

		if (!testingPropertiesHelper
				.isTestDistributedResources(testingProperties)) {
			return;
		}

		// generate a local scratch file
		String testFileName = "testPutOneFileWithResourceRerouting.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		irodsFileSystem.closeAndEatExceptions();

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		destFile.setResource(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_TERTIARY_RESOURCE_KEY));
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		SettableJargonProperties settableProperties = new SettableJargonProperties();
		settableProperties.setAllowPutGetResourceRedirects(true);
		irodsFileSystem.getIrodsSession().setJargonProperties(
				settableProperties);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		List<Resource> resources = dataObjectAO.listFileResources(destFile
				.getAbsolutePath());
		Assert.assertEquals("did not get expected resource", 1,
				resources.size());
		Resource firstResource = resources.get(0);
		Assert.assertEquals("resource for file not correct",
				destFile.getResource(), firstResource.getName());
		irodsFileSystem.closeAndEatExceptions(irodsAccount);
		// there should only be one connection in the session map (secondary
		// account should have been closed
		Assert.assertNull("session from reroute leaking",
				irodsFileSystem.getConnectionMap());

	}

	/**
	 * Get one file, using rerouting of resources. This will only run if
	 * configured in testing properites, and with a proper test configuration.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetOneFileWithResourceRerouting() throws Exception {

		if (!testingPropertiesHelper
				.isTestDistributedResources(testingProperties)) {
			return;
		}

		String testFileName = "testGetOneFileWithResourceRerouting.txt";
		String testRetrievedFileName = "testGetOneFileWithResourceReroutingRetrieved.txt";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		irodsFileSystem.closeAndEatExceptions();

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		destFile.setResource(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_TERTIARY_RESOURCE_KEY));
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		SettableJargonProperties settableProperties = new SettableJargonProperties();
		settableProperties.setAllowPutGetResourceRedirects(true);
		irodsFileSystem.getIrodsSession().setJargonProperties(
				settableProperties);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		destFile.reset();

		File retrieveFile = new File(absPath + "/" + testRetrievedFileName);
		dataTransferOperationsAO.getOperation(destFile, retrieveFile, null,
				null);
		Assert.assertTrue("retrieved file should exist", retrieveFile.exists());

		irodsFileSystem.closeAndEatExceptions(irodsAccount);
		// there should only be one connection in the session map (secondary
		// account should have been closed
		Assert.assertNull("session from reroute leaking",
				irodsFileSystem.getConnectionMap());

	}

	@Test
	public void testPutOneFileWithBlankInResource() throws Exception {
		// generate a local scratch file
		String testFileName = "testPutOneFileWithBlankInResource.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// now put the file
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestPropertiesWithBlankResource(testingProperties);
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
		irodsFileSystem.close();
		assertionHelper.assertIrodsFileMatchesLocalFileChecksum(
				destFile.getAbsolutePath(), localFile.getAbsolutePath());
	}

	@Test
	public void testGetOneFileWithCallback() throws Exception {
		// generate a local scratch file
		String testFileName = "testGetOneFileWithCallback.txt";
		String testRetrievedFileName = "testGetOneFileWithCallbackRetrieved.txt";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// now put the file
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		IRODSFile getIRODSFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		File getLocalFile = new File(absPath + "/" + testRetrievedFileName);
		TestingStatusCallbackListener testCallbackListener = new TestingStatusCallbackListener();

		// now get the file
		dataTransferOperationsAO.getOperation(getIRODSFile, getLocalFile,
				testCallbackListener, null);

		assertionHelper.assertIrodsFileMatchesLocalFileChecksum(
				getIRODSFile.getAbsolutePath(), getLocalFile.getAbsolutePath());
		Assert.assertEquals("did not expect any errors", 0,
				testCallbackListener.getErrorCallbackCount());
		Assert.assertEquals("file callback, initial and completion", 3,
				testCallbackListener.getSuccessCallbackCount());
		Assert.assertEquals("did not get the full irods file name in callback",
				targetIrodsFile, testCallbackListener.getLastSourcePath());
		Assert.assertEquals("did not get the full local file name in callback",
				getLocalFile.getAbsolutePath(),
				testCallbackListener.getLastTargetPath());

	}

	@Ignore
	public void testGet1point5GBFileWhenNoParallelIsPolicy() throws Exception {
		// generate a local scratch file
		String testFileName = "testGet1point5GBFileWhenNoParallelIsPolicy.txt";
		String testRetrievedFileName = "testGet1point5GBFileWhenNoParallelIsPolicyRetrieved.txt";
		long length = 1610612736;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						length);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// now put the file
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		IRODSFile getIRODSFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		File getLocalFile = new File(absPath + "/" + testRetrievedFileName);
		TestingStatusCallbackListener testCallbackListener = new TestingStatusCallbackListener();

		// now get the file
		dataTransferOperationsAO.getOperation(getIRODSFile, getLocalFile,
				testCallbackListener, null);

		assertionHelper.assertIrodsFileMatchesLocalFileChecksum(
				getIRODSFile.getAbsolutePath(), getLocalFile.getAbsolutePath());
		Assert.assertEquals("did not expect any errors", 0,
				testCallbackListener.getErrorCallbackCount());
		Assert.assertEquals("file callback, initial and completion", 3,
				testCallbackListener.getSuccessCallbackCount());
		Assert.assertEquals("did not get the full irods file name in callback",
				targetIrodsFile, testCallbackListener.getLastSourcePath());
		Assert.assertEquals("did not get the full local file name in callback",
				getLocalFile.getAbsolutePath(),
				testCallbackListener.getLastTargetPath());

	}

	@Test
	public void testGetOneFileWithCallbackAndControlBlockCheckOneFileToBeTransferred()
			throws Exception {
		// generate a local scratch file
		String testFileName = "testGetOneFileWithCallbackAndControlBlockCheckOneFileToBeTransferred.txt";
		String testRetrievedFileName = "testGetOneFileWithCallbackAndControlBlockCheckOneFileToBeTransferredRetrieved.txt";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		IRODSFile getIRODSFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		File getLocalFile = new File(absPath + "/" + testRetrievedFileName);
		TestingStatusCallbackListener testCallbackListener = new TestingStatusCallbackListener();
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();

		// now get the file
		dataTransferOperationsAO.getOperation(getIRODSFile, getLocalFile,
				testCallbackListener, transferControlBlock);

		Assert.assertEquals("Should have counted 1 file to transfer", 1,
				transferControlBlock.getTotalFilesToTransfer());
		Assert.assertEquals(
				"should have 1 file transferred as reflected in transferControlBlock",
				1, transferControlBlock.getTotalFilesTransferredSoFar());
		Assert.assertTrue(
				"No errors should have been accumulated in the transferControlBlock",
				transferControlBlock.getErrorCount() == 0);

	}

	@Test
	public void testGetWithCancelThenRestart() throws Exception {

		String rootCollection = "testGetWithCancelThenRestart";
		String getToCollection = "testGetWithCancelThenRestartGetToCollection";

		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();

		TransferStatusCallbackListenerTestingImplementation listener = new TransferStatusCallbackListenerTestingImplementation(
				transferControlBlock, 0, 4);

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator.generateManyFilesInParentCollectionByAbsolutePath(
				localCollectionAbsolutePath, "testPutWithCancel", ".txt", 10,
				1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);
		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		// now get, cancel after 4
		dataTransferOperationsAO.getOperation(destFile.getAbsolutePath() + "/"
				+ rootCollection, scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ "/" + getToCollection), "", listener,
				transferControlBlock);

		IRODSFile irodsSourceFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				irodsCollectionRootAbsolutePath + "/" + rootCollection);

		// now restart with the 4th file
		TransferControlBlock restartControlBlock = DefaultTransferControlBlock
				.instance(irodsSourceFile.listFiles()[2].getAbsolutePath());
		dataTransferOperationsAO
				.getOperation(
						destFile.getAbsolutePath() + "/" + rootCollection,
						scratchFileUtils
								.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
										+ "/" + getToCollection), "", null,
						restartControlBlock);

		File getToTargetFile = new File(
				scratchFileUtils
						.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
								+ "/" + getToCollection + "/" + rootCollection));

		Assert.assertEquals("should have 10 files in coll after restart", 10,
				getToTargetFile.list().length);
		Assert.assertEquals("should have counted 10 files", 10,
				restartControlBlock.getTotalFilesTransferredSoFar());
	}

	@Test
	public void testGetOneFileWithNoCallbackAndControlBlock() throws Exception {
		// generate a local scratch file
		String testFileName = "testGetOneFileWithNoCallbackAndControlBlock.txt";
		String testRetrievedFileName = "testGetOneFileWithNoCallbackAndControlBlockRetrieved.txt";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		IRODSFile getIRODSFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		File getLocalFile = new File(absPath + "/" + testRetrievedFileName);
		TestingStatusCallbackListener testCallbackListener = new TestingStatusCallbackListener();
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();

		// now get the file
		dataTransferOperationsAO.getOperation(getIRODSFile, getLocalFile,
				testCallbackListener, transferControlBlock);

		Assert.assertEquals(
				"Should not have counted transfers even if no status callback",
				1, transferControlBlock.getTotalFilesToTransfer());
	}

	@Test
	public void testPutOneFileWithSpacesInTheName() throws Exception {
		// generate a local scratch file
		String testFileName = "Addendum - Duke Digital Projects Developer.docx";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
		assertionHelper.assertIrodsFileMatchesLocalFileChecksum(
				destFile.getAbsolutePath(), localFile.getAbsolutePath());
	}

	/*
	 * For [#521] iDrop synch issue (nothing happens during synch) ref
	 * [iROD-Chat:7184] iDrop troubles
	 */
	@Test
	public void testPutCollectionWithZeroLengthFilesInIt() throws Exception {
		// generate a local scratch file
		String testColl = "testPutCollectionWithZeroLengthFilesInIt";
		int nbrFiles = 10;
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ "/" + testColl);

		File localDir = new File(absPath);
		localDir.mkdirs();

		// make n number of empty files in test dir

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		File localFile = null;
		for (int i = 0; i < nbrFiles; i++) {
			localFile = new File(localDir, testColl + i + ".txt");
			localFile.createNewFile();
		}

		// now put the files

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(localDir, destFile, null, null);
		destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile, testColl);
		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(
				localDir, (File) destFile);
	}

	@Test
	public void testGetCollectionWithTwoFilesNoCallbacks() throws Exception {

		String rootCollection = "testGetCollectionWithTwoFilesNoCallbacks";
		String returnedLocalCollection = "testGetCollectionWithTwoFilesNoCallbacksReturnedLocalFiles";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH); // TODO: add
		// test with
		// trailing
		// '/'

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testGetCollectionWithTwoFilesNoCallbacks", 1, 1, 1,
						"testFile", ".txt", 2, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
		destFile.close();

		destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath + "/"
						+ rootCollection);

		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(
				localFile, (File) destFile);

		// now get the files into a local return collection and verify

		IRODSFile getIrodsFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath + "/"
						+ rootCollection);
		String returnLocalCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + returnedLocalCollection);
		File returnLocalFile = new File(returnLocalCollectionAbsolutePath);

		dataTransferOperationsAO.getOperation(getIrodsFile, returnLocalFile,
				null, null);

		String returnLocalCollectionCompareAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + returnedLocalCollection + '/' + rootCollection);
		File returnCompareLocalFile = new File(
				returnLocalCollectionCompareAbsolutePath);

		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(
				localFile, returnCompareLocalFile);
	}

	@Test
	public void testPutMultipleCollectionsMultipleFiles() throws Exception {

		String rootCollection = "testPutMultipleCollectionsMultipleFiles";
		String returnedCollection = "testPutMultipleCollectionsMultipleFilesReturned";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String returnedCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + returnedCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testPutCollectionWithTwoFiles", 3, 5, 3, "testFile",
						".txt", 10, 9, 20, 200);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
		destFile.close();

		// File returnedData = new File(returnedCollectionAbsolutePath + "/" +
		// rootCollection);

		dataTransferOperationsAO.getOperation(destFile.getAbsolutePath() + "/"
				+ rootCollection, returnedCollectionAbsolutePath, "", null,
				null);

		File returnedData = new File(returnedCollectionAbsolutePath + "/"
				+ rootCollection);
		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(
				localFile, returnedData);
	}

	@Test
	public void testPutThenGetOneFile() throws Exception {
		// generate a local scratch file
		String testFileName = "testPutThenGetOneFile.txt";
		String testRetrievedFileName = "testPutThenGetOneFileRetreived.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		// now get
		File retrievedLocalFile = new File(absPath + testRetrievedFileName);
		dataTransferOperationsAO.getOperation(destFile, retrievedLocalFile,
				null, null);

		// compare checkums

		long origChecksum = scratchFileUtils
				.computeFileCheckSumViaAbsolutePath(localFile.getAbsolutePath());
		long retrievedChecksum = scratchFileUtils
				.computeFileCheckSumViaAbsolutePath(localFile.getAbsolutePath());

		// byte[] retrievedChecksum =
		// scratchFileUtils.computeFileCheckSumViaAbsolutePath(retrievedLocalFile.getAbsolutePath());

		Assert.assertEquals(origChecksum, retrievedChecksum);
	}

	@Test
	public void testPutThenGetMultipleCollectionsMultipleFiles()
			throws Exception {

		String rootCollection = "testPutThenGetMultipleCollectionsMultipleFiles";
		String returnCollection = "testPutThenGetMultipleCollectionsMultipleFilesReturn";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String returnCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + returnCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testPutThenGetMultipleCollectionsMultipleFiles", 2, 3,
						2, "testFile", ".txt", 3, 2, 20, 200);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		irodsFileSystem = IRODSFileSystem.instance();
		irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);

		// I've put a mess of stuff, now get it

		File returnLocalFile = new File(returnCollectionAbsolutePath);
		destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath + '/'
						+ rootCollection);

		dataTransferOperationsAO.getOperation(destFile, returnLocalFile, null,
				null);

		File resultOfPutFile = new File(returnCollectionAbsolutePath + '/'
				+ rootCollection);

		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(
				localFile, resultOfPutFile);
	}

	@Test
	public void testPutThenGetMultipleCollectionsMultipleFilesWithCallbacks()
			throws Exception {

		String rootCollection = "testPutThenGetMultipleCollectionsMultipleFilesWithCallbacks";
		String returnCollection = "testPutThenGetMultipleCollectionsMultipleFilesWithCallbacksReturn";

		TransferStatusCallbackListenerTestingImplementation listener = new TransferStatusCallbackListenerTestingImplementation();

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String returnCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + returnCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testPutThenGetMultipleCollectionsMultipleFiles", 1, 2,
						2, "testFile", ".txt", 4, 2, 20, 200);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, listener,
				null);

		irodsFileSystem = IRODSFileSystem.instance();
		irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);

		// I've put a mess of stuff, now get it

		File returnLocalFile = new File(returnCollectionAbsolutePath);
		destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath + '/'
						+ rootCollection);
		dataTransferOperationsAO.getOperation(destFile, returnLocalFile,
				listener, null);

		File resultOfPutFile = new File(returnCollectionAbsolutePath + '/'
				+ rootCollection);

		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(
				localFile, resultOfPutFile);

		Assert.assertTrue(listener.getGetCallbackCtr() > 0);
		Assert.assertEquals(listener.getGetCallbackCtr(),
				listener.getPutCallbackCtr());
	}

	@Test
	public void testPutMultipleCollectionsMultipleFilesWithCallbacksAndControlBlock()
			throws Exception {

		String rootCollection = "testPutMultipleCollectionsMultipleFilesWithCallbacksAndControlBlock";

		TransferStatusCallbackListenerTestingImplementation listener = new TransferStatusCallbackListenerTestingImplementation();

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testPutThenGetMultipleCollectionsMultipleFiles", 2, 3,
						2, "testFile", ".txt", 4, 2, 20, 200);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();

		dataTransferOperationsAO.putOperation(localFile, destFile, listener,
				transferControlBlock);

		Assert.assertTrue("did not do a pre-count of files",
				transferControlBlock.getTotalFilesToTransfer() > 0);
		Assert.assertEquals(
				"did not transfer the amount of files I pre-counted",
				transferControlBlock.getTotalFilesToTransfer(),
				transferControlBlock.getTotalFilesTransferredSoFar());
	}

	@Test
	public void testPutWithCancel() throws Exception {

		String rootCollection = "testPutWithCancel";
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();

		TransferStatusCallbackListenerTestingImplementation listener = new TransferStatusCallbackListenerTestingImplementation(
				transferControlBlock, 0, 3);

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator.generateManyFilesInParentCollectionByAbsolutePath(
				localCollectionAbsolutePath, "testPutWithCancel", ".txt", 10,
				1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, listener,
				transferControlBlock);

		irodsFileSystem = IRODSFileSystem.instance();
		irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath + "/"
						+ rootCollection);

		// I've put a mess of stuff, now get it

		Assert.assertEquals("should have  3 files in coll before cancelled", 3,
				destFile.list().length);
		Assert.assertTrue("should have a status callback of cancelled",
				listener.isCancelEncountered());
	}

	@Test
	public void testPutWithCancelThenRestart() throws Exception {

		String rootCollection = "testPutWithCancelThenRestartCollection";
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();

		TransferStatusCallbackListenerTestingImplementation listener = new TransferStatusCallbackListenerTestingImplementation(
				transferControlBlock, 0, 3);

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator.generateManyFilesInParentCollectionByAbsolutePath(
				localCollectionAbsolutePath, "testPutWithCancel", ".txt", 10,
				1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);
		File[] localFiles = localFile.listFiles();

		dataTransferOperationsAO.putOperation(localFile, destFile, listener,
				transferControlBlock);

		// now restart
		TransferControlBlock restartControlBlock = DefaultTransferControlBlock
				.instance(localFiles[3].getAbsolutePath());
		dataTransferOperationsAO.putOperation(localFile, destFile, listener,
				restartControlBlock);

		IRODSFile destTarget = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						destFile.getAbsolutePath() + "/" + rootCollection);

		Assert.assertEquals("should have 9 files in coll after restart", 9,
				destTarget.list().length);
		Assert.assertEquals("should have counted 10 files", 10,
				restartControlBlock.getTotalFilesTransferredSoFar());
	}

	@Test
	public void testPutWithPause() throws Exception {

		String rootCollection = "testPutWithPause";
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();

		TransferStatusCallbackListenerTestingImplementation listener = new TransferStatusCallbackListenerTestingImplementation(
				transferControlBlock, 2, 0);

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator.generateManyFilesInParentCollectionByAbsolutePath(
				localCollectionAbsolutePath, "testPutWithPause", ".txt", 10, 1,
				2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, listener,
				transferControlBlock);

		irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath + "/"
						+ rootCollection);

		// I've put a mess of stuff, now get it

		Assert.assertEquals("wrong number of files in coll before pause", 2,
				destFile.list().length);
		Assert.assertTrue("should have a status callback of pause",
				listener.isPauseEncountered());
	}

	@Test
	public void testReplicateOneFile() throws Exception {
		// generate a local scratch file
		String testFileName = "testReplicateOneFile.doc";
		String targetResource = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY);
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();

		dataTransferOperationsAO.putOperation(localFile, destFile,
				transferStatusCallbackListener, null);

		// now replicate
		dataTransferOperationsAO.replicate(targetIrodsFile, targetResource,
				transferStatusCallbackListener,
				DefaultTransferControlBlock.instance());

		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);

		IlsCommand ilsCommand = new IlsCommand();
		ilsCommand.setLongFormat(true);
		ilsCommand.setIlsBasePath(targetIrodsFile);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);

		String ilsResult = invoker
				.invokeCommandAndGetResultAsString(ilsCommand);
		Assert.assertTrue(
				"file is not in new resource",
				ilsResult.indexOf(testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY)) != -1);
		Assert.assertTrue(
				"file is not in original resource",
				ilsResult.indexOf(testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY)) != -1);

		Assert.assertEquals("did not get expected success callbacks", 1,
				transferStatusCallbackListener.getReplicateCallbackCtr());
	}

	@Test
	public void testReplicateMultipleCollectionsMultipleFilesWithCallbacks()
			throws Exception {

		String rootCollection = "testReplicateMultipleCollectionsMultipleFilesWithCallbacks";
		String targetResource = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY);

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath, "rootCollection", 1, 2, 2,
						"testFile", ".txt", 4, 2, 20, 200);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File localFile = new File(localCollectionAbsolutePath);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, irodsFile, null, null);

		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();

		// now replicate
		dataTransferOperationsAO.replicate(irodsCollectionRootAbsolutePath
				+ "/" + rootCollection, targetResource,
				transferStatusCallbackListener,
				DefaultTransferControlBlock.instance());

		Assert.assertTrue("did not get expected success callback",
				transferStatusCallbackListener.getReplicateCallbackCtr() > 0);
		Assert.assertTrue("did not get expected exception callback",
				transferStatusCallbackListener.getExceptionCallbackCtr() == 0);
	}

	@Test
	public void testReplicateMultipleCollectionsMultipleFilesWithControlBlock()
			throws Exception {

		String rootCollection = "testReplicateMultipleCollectionsMultipleFilesWithControlBlock";
		String targetResource = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY);

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath, rootCollection, 1, 2, 2,
						"testFile", ".txt", 4, 2, 20, 200);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File localFile = new File(localCollectionAbsolutePath);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, irodsFile, null, null);

		// TransferStatusCallbackListenerTestingImplementation
		// transferStatusCallbackListener = new
		// TransferStatusCallbackListenerTestingImplementation();
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();

		// now replicate
		dataTransferOperationsAO.replicate(irodsCollectionRootAbsolutePath
				+ "/" + rootCollection, targetResource, null,
				transferControlBlock);

		Assert.assertTrue("did not pre-count files to replicate",
				transferControlBlock.getTotalFilesToTransfer() > 0);
		Assert.assertEquals(
				"did not count files as replicated to match pre-count total",
				transferControlBlock.getTotalFilesToTransfer(),
				transferControlBlock.getTotalFilesTransferredSoFar());

	}

	@Test
	public void testReplicateWithCancel() throws Exception {

		String rootCollection = "testReplicateWithCancel";
		String targetResource = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY);

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator.generateManyFilesInParentCollectionByAbsolutePath(
				localCollectionAbsolutePath, "testReplicateWithCancel", ".txt",
				10, 1, 2);

		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();

		TransferStatusCallbackListenerTestingImplementation listener = new TransferStatusCallbackListenerTestingImplementation(
				transferControlBlock, 0, 3);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File localFile = new File(localCollectionAbsolutePath);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, irodsFile, null, null);

		// now replicate
		dataTransferOperationsAO.replicate(irodsCollectionRootAbsolutePath
				+ "/" + rootCollection, targetResource, listener,
				transferControlBlock);

		Assert.assertTrue("did not get expected success callback",
				listener.getReplicateCallbackCtr() == 4);
		Assert.assertTrue("should have a status callback of cancelled",
				listener.isCancelEncountered());

	}

	@Test
	public void testReplicateWithCancelThenRestart() throws Exception {

		String rootCollection = "testReplicateWithCancelThenRestart";
		String targetResource = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY);

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator.generateManyFilesInParentCollectionByAbsolutePath(
				localCollectionAbsolutePath, "testReplicateWithCancel", ".txt",
				10, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File localFile = new File(localCollectionAbsolutePath);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, irodsFile, null, null);

		// now replicate with a cancel that will occur
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();

		TransferStatusCallbackListenerTestingImplementation listener = new TransferStatusCallbackListenerTestingImplementation(
				transferControlBlock, 0, 3);
		dataTransferOperationsAO.replicate(irodsCollectionRootAbsolutePath
				+ "/" + rootCollection, targetResource, listener,
				transferControlBlock);

		Assert.assertTrue("did not get expected success callback",
				listener.getReplicateCallbackCtr() == 4);
		Assert.assertTrue("should have a status callback of cancelled",
				listener.isCancelEncountered());

		// now restart the replication providing the restart point
		IRODSFile parentOfFilesInIrods = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath + "/"
						+ rootCollection);

		transferControlBlock = DefaultTransferControlBlock
				.instance(parentOfFilesInIrods.listFiles()[2].getAbsolutePath());
		listener = new TransferStatusCallbackListenerTestingImplementation(
				transferControlBlock, 0, 0);

		dataTransferOperationsAO.replicate(irodsCollectionRootAbsolutePath
				+ "/" + rootCollection, targetResource, listener,
				transferControlBlock);

		// assess the callbacks
		Assert.assertEquals("should have counted 10 files", 10,
				transferControlBlock.getTotalFilesTransferredSoFar());

	}

	@Test
	public void testReplicateWithPause() throws Exception {

		String rootCollection = "testReplicateWithPause";
		String targetResource = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY);

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator.generateManyFilesInParentCollectionByAbsolutePath(
				localCollectionAbsolutePath, "testReplicateWithPause", ".txt",
				10, 1, 2);

		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();

		TransferStatusCallbackListenerTestingImplementation listener = new TransferStatusCallbackListenerTestingImplementation(
				transferControlBlock, 3, 0);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File localFile = new File(localCollectionAbsolutePath);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, irodsFile, null, null);

		// now replicate
		dataTransferOperationsAO.replicate(irodsCollectionRootAbsolutePath
				+ "/" + rootCollection, targetResource, listener,
				transferControlBlock);

		Assert.assertTrue("did not get expected success callback",
				listener.getReplicateCallbackCtr() == 4);
		Assert.assertTrue("should have a status callback of paused",
				listener.isPauseEncountered());

	}

	@Test
	public void testPutCollectionWithTwoFiles() throws Exception {

		String rootCollection = "testPutCollectionWithTwoFiles";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH); // TODO: add
		// test with
		// trailing
		// '/'

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testPutCollectionWithTwoFiles", 1, 1, 1, "testFile",
						".txt", 2, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
		destFile.close();

		irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath + "/"
						+ rootCollection);

		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(
				localFile, (File) destFile);
	}

	@Test
	public void testPutCollectionWithTwoFilesControlBlockNoCallback()
			throws Exception {

		String rootCollection = "testPutCollectionWithTwoFilesControlBlockNoCallback";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH); // TODO: add

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testPutCollectionWithTwoFilesControlBlockNoCallback",
						1, 1, 1, "testFile", ".txt", 2, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();

		dataTransferOperationsAO.putOperation(localFile, destFile, null,
				transferControlBlock);
		destFile.close();

		Assert.assertEquals("did not compute a total files expected of 2", 2,
				transferControlBlock.getTotalFilesToTransfer());
		Assert.assertEquals("did not count transfers equal to total counted",
				transferControlBlock.getTotalFilesToTransfer(),
				transferControlBlock.getTotalFilesTransferredSoFar());

	}

	@Test
	public void testPutCollectionWithTwoFilesNoControlBlockCallback()
			throws Exception {

		String rootCollection = "testPutCollectionWithTwoFilesNoControlBlockCallback";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH); // TODO: add

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testPutCollectionWithTwoFilesNoControlBlockCallback",
						1, 1, 1, "testFile", ".txt", 2, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		TransferStatusCallbackListenerTestingImplementation listener = new TransferStatusCallbackListenerTestingImplementation(
				null, 3, 0);

		dataTransferOperationsAO.putOperation(localFile, destFile, listener,
				null);
		destFile.close();

		// test passes if gracefully handles no control block specified
		Assert.assertTrue(true);

	}

	@Test
	public void testGetWithCancel() throws Exception {

		String rootCollection = "testGetWithCancel";
		String returnedLocalCollection = "testGetWithCancelReturned";

		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();

		TransferStatusCallbackListenerTestingImplementation listener = new TransferStatusCallbackListenerTestingImplementation(
				transferControlBlock, 0, 3);

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator.generateManyFilesInParentCollectionByAbsolutePath(
				localCollectionAbsolutePath, "testGetWithCancel", ".txt", 10,
				1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		// now get the files into a local return collection and verify
		IRODSFile getIrodsFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath + "/"
						+ rootCollection);
		String returnLocalCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + returnedLocalCollection);
		File returnLocalFile = new File(returnLocalCollectionAbsolutePath);

		dataTransferOperationsAO.getOperation(getIrodsFile, returnLocalFile,
				listener, transferControlBlock);

		String returnLocalCollectionCompareAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + returnedLocalCollection + '/' + rootCollection);

		File returnCompareLocalFile = new File(
				returnLocalCollectionCompareAbsolutePath);

		// I've put a mess of stuff, now get it
		Assert.assertEquals("wrong number of files in coll before cancelled",
				3, returnCompareLocalFile.list().length);
		Assert.assertTrue("should have a status callback of cancelled",
				listener.isCancelEncountered());
	}

	@Test
	public void testGetWithCallbacksMultipleLevelsOfCollections()
			throws Exception {

		String rootCollection = "testGetWithCallbacksMultipleLevelsOfCollections";
		String returnedLocalCollection = "testGetWithCallbacksMultipleLevelsOfCollectionsReturned";

		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();

		TransferStatusCallbackListenerTestingImplementation listener = new TransferStatusCallbackListenerTestingImplementation(
				transferControlBlock, 0, 0);

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath, rootCollection, 3, 4, 2,
						"prefix", ".suffix", 7, 3, 10, 300);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		// now get the files into a local return collection and verify
		IRODSFile getIrodsFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath + "/"
						+ rootCollection);
		String returnLocalCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + returnedLocalCollection);
		File returnLocalFile = new File(returnLocalCollectionAbsolutePath);

		dataTransferOperationsAO.getOperation(getIrodsFile, returnLocalFile,
				listener, transferControlBlock);

		String returnLocalCollectionCompareAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + returnedLocalCollection + '/' + rootCollection);

		File returnCompareLocalFile = new File(
				returnLocalCollectionCompareAbsolutePath);

		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(
				localFile, returnCompareLocalFile);

	}

	@Test
	public void testGetWithCallbacksMultipleLevelsOfCollectionsVerifyCountsInControlBlock()
			throws Exception {

		String rootCollection = "testGetWithCallbacksMultipleLevelsOfCollectionsVerifyCountsInControlBlock";
		String returnedLocalCollection = "testGetWithCallbacksMultipleLevelsOfCollectionsVerifyCountsInControlBlockReturned";

		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();

		TransferStatusCallbackListenerTestingImplementation listener = new TransferStatusCallbackListenerTestingImplementation(
				transferControlBlock, 0, 0);

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath, rootCollection, 2, 3, 2,
						"prefix", ".suffix", 7, 2, 1000, 30000);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		// now get the files into a local return collection and verify
		IRODSFile getIrodsFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath + "/"
						+ rootCollection);
		String returnLocalCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + returnedLocalCollection);
		File returnLocalFile = new File(returnLocalCollectionAbsolutePath);

		dataTransferOperationsAO.getOperation(getIrodsFile, returnLocalFile,
				listener, transferControlBlock);

		Assert.assertEquals(
				"did not get count matching expected files in the transferControlBlock",
				transferControlBlock.getTotalFilesToTransfer(),
				transferControlBlock.getTotalFilesTransferredSoFar());

	}

	@Test
	public void testMoveSourceCollectionTargetCollection() throws Exception {

		String rootCollection = "testMoveSourceCollectionTargetCollection";
		String targetCollection = "targetCollectionForTestMoveSourceCollectionTargetCollection";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String irodsCollectionTargetAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ targetCollection);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testMoveCollectionWithTwoFilesUnderneathAParent", 1,
						1, 1, "testFile", ".txt", 2, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		// make the target

		IRODSFile targetParent = irodsFileFactory
				.instanceIRODSFile(irodsCollectionTargetAbsolutePath);
		targetParent.mkdirs();

		dataTransferOperationsAO.move(irodsCollectionRootAbsolutePath + "/"
				+ rootCollection, targetParent.getAbsolutePath());

		irodsFileSystem = IRODSFileSystem.instance();
		irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionTargetAbsolutePath + "/"
						+ rootCollection);

		Assert.assertTrue("did not find expected targetcollection",
				destFile.isDirectory());
	}

	// see note in release notes...potential bug?
	@Ignore
	public void testMoveCollectionWithTwoFilesUnderneathSameParent()
			throws Exception {

		// this test case passes if no error, tests code to avoid irods error
		// -837000
		String rootCollection = "testMoveCollectionWithTwoFilesUnderneathAParent";
		String targetCollection = "testMoveCollectionWithTwoFilesUnderneathAParentNewTarget";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String irodsCollectionTargetAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ rootCollection + "/" + targetCollection);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testMoveCollectionWithTwoFilesUnderneathAParent", 1,
						1, 1, "testFile", ".txt", 2, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		// make the target

		IRODSFile targetParent = irodsFileFactory
				.instanceIRODSFile(irodsCollectionTargetAbsolutePath);
		targetParent.mkdirs();

		dataTransferOperationsAO.move(irodsCollectionRootAbsolutePath + "/"
				+ rootCollection, targetParent.getAbsolutePath());

		Assert.assertTrue(true);
	}

	@Ignore
	// FIXME: possible irods bug when source and new parent under same tree
	public void testMoveCollectionWithTwoFilesUnderneathWithSameName()
			throws Exception {

		// this test case passes if no error, tests code to avoid irods error
		// -837000
		String rootCollection = "testMoveCollectionWithTwoFilesUnderneathAParent";
		String targetCollection = "testMoveCollectionWithTwoFilesUnderneathAParent";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String irodsCollectionTargetAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ targetCollection);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testMoveCollectionWithTwoFilesUnderneathAParent", 1,
						1, 1, "testFile", ".txt", 2, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		// make the target

		IRODSFile targetParent = irodsFileFactory
				.instanceIRODSFile(irodsCollectionTargetAbsolutePath);
		targetParent.mkdirs();

		dataTransferOperationsAO.move(irodsCollectionRootAbsolutePath + "/"
				+ rootCollection, targetParent.getAbsolutePath());

		Assert.assertTrue(true);
	}

	// see note in release notes...potential bug?
	@Ignore
	public void testMoveCollectionWithTwoFilesUnderneathNewParentDifferentTree()
			throws Exception {

		// this test case passes if no error, tests code to avoid irods error
		// -837000
		String rootCollection = "testMoveCollectionWithTwoFilesUnderneathAParent";
		String targetCollection = "testMoveCollectionWithTwoFilesUnderneathAParentNewTarget";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String irodsCollectionTargetAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ targetCollection);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testMoveCollectionWithTwoFilesUnderneathNewParentDifferentTree",
						1, 1, 1, "testFile", ".txt", 2, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		// make the target

		IRODSFile targetParent = irodsFileFactory
				.instanceIRODSFile(irodsCollectionTargetAbsolutePath);
		targetParent.mkdirs();

		dataTransferOperationsAO.move(irodsCollectionRootAbsolutePath,
				irodsCollectionTargetAbsolutePath);

		IRODSFile actualFile = irodsFileFactory.instanceIRODSFile(targetParent
				.getAbsolutePath() + "/" + rootCollection);

		Assert.assertTrue(actualFile.exists());
	}

	@Test
	public void testMoveFileSpecifyCollectionAsTargetSourceFileHasNoExtension()
			throws Exception {
		String testFileName = "testMoveFileSpecifyCollectionAsTargetSourceFileHasNoExtension";
		String targetCollection = "testMoveFileSpecifyCollectionAsTargetSourceFileHasNoExtension-TargetCollection";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		// put scratch file into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		iputCommand.setLocalFileName(fileNameOrig);
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		// make the target irods collection
		IRODSFile targetCollectionFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + '/' + targetCollection);
		targetCollectionFile.mkdir();

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperations.move(irodsFile.getAbsolutePath(),
				targetCollectionFile.getAbsolutePath());

		IRODSFile actualFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetCollectionFile.getAbsolutePath() + '/'
								+ testFileName);

		Assert.assertTrue("did not find the newly moved file",
				actualFile.exists());
		Assert.assertFalse("did not move source file, still exists",
				irodsFile.exists());

	}

	@Test
	public void testGetJargonProperties() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		JargonProperties jargonProperties = dataTransferOperationsAO
				.getJargonProperties();
		Assert.assertNotNull("null jargonProperties", jargonProperties);
	}

	@Test
	public void testCopyCollectionToTarget() throws Exception {

		// generate a local scratch file
		String testOrigDirectory = "testCopyCollectionToTargetCollection";
		String testTargetDirectory = "testCopyCollectionToTargetCollectionTarget";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + testOrigDirectory);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String irodsOriginalAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testOrigDirectory);

		String irodsTargetAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testTargetDirectory);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath, "prefixForColl", 2, 3, 2,
						"testFile", ".txt", 2, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(localCollectionAbsolutePath,
				irodsCollectionRootAbsolutePath, "", null, null);

		dataTransferOperations.copy(irodsOriginalAbsolutePath, "",
				irodsTargetAbsolutePath, null, false, null);

		File localFile = new File(localCollectionAbsolutePath);
		IRODSFile targetFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						irodsTargetAbsolutePath, testOrigDirectory);

		// compare the local source to the copied-to target
		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(
				localFile, (File) targetFile);

	}

	@Test
	public void testCopyCollectionWithAnEmptyChildCollectionToTarget()
			throws Exception {

		// generate a local scratch file
		String testOrigDirectory = "testCopyCollectionWithAnEmptyChildCollectionToTarget";
		String testOrigSubdir = "testCopyCollectionWithAnEmptyChildCollectionToTargetSubdir";
		String testTargetDirectory = "testCopyCollectionWithAnEmptyChildCollectionToTargetTarget";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String irodsOriginalAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testOrigDirectory);

		IRODSFile testDir = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(irodsOriginalAbsolutePath);
		testDir.mkdirs();

		testDir = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(irodsOriginalAbsolutePath, testOrigSubdir);
		testDir.mkdirs();

		String irodsTargetAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testTargetDirectory);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.copy(irodsOriginalAbsolutePath, "",
				irodsTargetAbsolutePath, null, false, null);

		IRODSFile targetDir = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(irodsTargetAbsolutePath, testOrigDirectory);
		Assert.assertTrue("did not find copied directory under target",
				targetDir.exists() && targetDir.isDirectory());
		targetDir = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetDir.getAbsolutePath(), testOrigSubdir);
		Assert.assertTrue(
				"did not find child of copied directory under target",
				targetDir.exists() && targetDir.isDirectory());

	}

	@Test(expected = DuplicateDataException.class)
	public void testCopyCollectionToSelfParent() throws Exception {

		// generate a local scratch file
		String testOrigDirectory = "testCopyCollectionToSelfParent";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + testOrigDirectory);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String irodsOriginalAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testOrigDirectory);

		String irodsTargetAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath, "prefixForColl", 2, 3, 2,
						"testFile", ".txt", 2, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(localCollectionAbsolutePath,
				irodsCollectionRootAbsolutePath, "", null, null);

		dataTransferOperations.copy(irodsOriginalAbsolutePath, "",
				irodsTargetAbsolutePath, null, false, null);

	}

	@Test(expected = DuplicateDataException.class)
	public void testCopyCollectionToSelfParentWhenFile() throws Exception {

		String testFileName = "testCopyCollectionToSelfParentWhenFile.doc";

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(fileNameOrig,
				irodsCollectionRootAbsolutePath, "", null, null);

		dataTransferOperations.copy(irodsCollectionRootAbsolutePath + "/"
				+ testFileName, "", irodsCollectionRootAbsolutePath, null,
				false, null);
	}

	@Test
	public void testCopyCollectionNoForceNoOverwriteTransferControlBlock()
			throws Exception {

		// generate a local scratch file
		String testOrigDirectory = "testCopyCollectionNoForceNoOverwriteTransferControlBlockOrig";
		String testTargetDirectory = "testCopyCollectionNoForceNoOverwriteTransferControlBlockTarget";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + testOrigDirectory);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String irodsOriginalAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testOrigDirectory);

		String irodsTargetAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testTargetDirectory);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath, "prefixForColl", 2, 3, 2,
						"testFile", ".txt", 2, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(localCollectionAbsolutePath,
				irodsCollectionRootAbsolutePath, "", null, null);

		dataTransferOperations.copy(irodsOriginalAbsolutePath, "",
				irodsTargetAbsolutePath, null, false, null);

		File localFile = new File(localCollectionAbsolutePath);
		IRODSFile targetFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						irodsTargetAbsolutePath, testOrigDirectory);

		// compare the local source to the copied-to target
		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(
				localFile, (File) targetFile);

	}

	@Test
	@Ignore
	public void testCopyCollectionWithCancelThenRestart() throws Exception {

		String rootCollection = "testCopyCollectionWithCancelThenRestart";
		String targetCollection = "testCopyCollectionWithCancelThenRestartTarget";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String irodsCollectionTargetAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ targetCollection);

		FileGenerator.generateManyFilesInParentCollectionByAbsolutePath(
				localCollectionAbsolutePath,
				"testCopyCollectionWithCancelThenRestart", ".txt", 10, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		// now move with a cancel

		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();

		TransferStatusCallbackListenerTestingImplementation listener = new TransferStatusCallbackListenerTestingImplementation(
				transferControlBlock, 0, 2);

		dataTransferOperationsAO.copy(destFile.getAbsolutePath() + "/"
				+ rootCollection, "", irodsCollectionTargetAbsolutePath,
				listener, false, transferControlBlock);

		Assert.assertEquals("did not hit cancel as anticipated in copy", 2,
				transferControlBlock.getTotalFilesTransferredSoFar());

		// now do a restart and complete the copy
		IRODSFile copySourceFiles = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				destFile.getAbsolutePath() + "/" + rootCollection);

		transferControlBlock = DefaultTransferControlBlock
				.instance(copySourceFiles.listFiles()[2].getAbsolutePath());
		listener = new TransferStatusCallbackListenerTestingImplementation(
				transferControlBlock, 0, 0);

		dataTransferOperationsAO.copy(destFile.getAbsolutePath() + "/"
				+ rootCollection, "", irodsCollectionTargetAbsolutePath,
				listener, false, transferControlBlock);

		int countRestarted = 0;
		int countSuccess = 0;

		for (TransferStatus callback : listener.getStatusCache()) {
			if (callback.getTransferState() == TransferState.RESTARTING) {
				countRestarted++;
			} else if (callback.getTransferState() == TransferState.SUCCESS) {
				countSuccess++;
			} else {
				Assert.fail("unknown transfer status in cache:" + callback);
			}
		}

		Assert.assertEquals(
				"did not get expected number of restarting callbacks", 3,
				countRestarted);
		Assert.assertEquals("did not get expected number of success callbacks",
				7, countSuccess);

	}

}