package org.irods.jargon.core.pub;

import java.io.File;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonFileOrCollAlreadyExistsException;
import org.irods.jargon.core.exception.OverwriteException;
import org.irods.jargon.core.exception.PathTooLongException;
import org.irods.jargon.core.packinstr.TransferOptions;
import org.irods.jargon.core.packinstr.TransferOptions.ForceOption;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.domain.Resource;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.transfer.DefaultTransferControlBlock;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatus.TransferState;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener.CallbackResponse;
import org.irods.jargon.core.transfer.TransferStatusCallbackListenerTestingImplementation;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class DataTransferOperationsImplTest {

	private static Properties testingProperties = new Properties();
	private static JargonProperties jargonOriginalProperties = null;
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "DataTransferOperationsImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static org.irods.jargon.testutils.AssertionHelper assertionHelper = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		irodsFileSystem = IRODSFileSystem.instance();
		SettableJargonProperties settableJargonProperties = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		settableJargonProperties.setInternalCacheBufferSize(-1);
		settableJargonProperties.setInternalOutputStreamBufferSize(65535);
		jargonOriginalProperties = settableJargonProperties;
		irodsFileSystem.getIrodsSession().setJargonProperties(
				settableJargonProperties);
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

	@Before
	public void before() throws Exception {
		// be sure that normal parallel stuff is set up
		irodsFileSystem.getIrodsSession().setJargonProperties(
				jargonOriginalProperties);
	}

	@Test
	public void testPhysicalMove() throws Exception {
		String testFileName = "testPhysicalMove.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(fileNameOrig, targetIrodsCollection, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null,
				null);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		dto.physicalMove(
				irodsFile.getAbsolutePath(),
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY));

		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		DataObject actual = dataObjectAO.findByAbsolutePath(irodsFile
				.getAbsolutePath());

		Assert.assertEquals(
				"file is not in new resource",
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY),
				actual.getResourceName());
	}

	@Test
	public void testPhysicalMoveDelegatedThroughMove() throws Exception {
		String testFileName = "testPhysicalMoveDelegatedThroughMove.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(fileNameOrig, targetIrodsCollection, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null,
				null);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		IRODSFile targetFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection + '/' + testFileName);

		String testResource = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY);
		targetFile.setResource(testResource);
		dto.move(irodsFile, targetFile);

		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		DataObject actual = dataObjectAO.findByAbsolutePath(irodsFile
				.getAbsolutePath());

		Assert.assertEquals(
				"file is not in new resource",
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY),
				actual.getResourceName());
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

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(fileNameOrig, targetIrodsCollection, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null,
				null);

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
		dto.putOperation(fileNameOrig, targetIrodsCollection, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null,
				null);

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

	@Test(expected = JargonException.class)
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
		String testFileName = "testPutOneFile.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						32 * 1024);

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

	/**
	 * Bug [#1837] timeout on put using 3.3.2-SNAPSHOT
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPutOneFileNoParallelBug1837() throws Exception {
		// generate a local scratch file
		String testFileName = "testPutOneFileNoParallelBug1837.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						32 * 1024 * 1024);

		SettableJargonProperties props = new SettableJargonProperties(
				jargonOriginalProperties);
		props.setMaxParallelThreads(0);
		props.setUseParallelTransfer(false);
		irodsFileSystem.getIrodsSession().setJargonProperties(props);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// set jargon properties for no parallel

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

		TransferOptions transferOptions = new TransferOptions();
		transferOptions.setComputeAndVerifyChecksumAfterTransfer(true);
		transferOptions.setMaxThreads(0);
		transferOptions.setUseParallelTransfer(false);

		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();
		transferControlBlock.setTransferOptions(transferOptions);

		dataTransferOperationsAO.putOperation(localFile, destFile, null,
				transferControlBlock);
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

		TransferControlBlock restartControlBlock = irodsFileSystem
				.getIrodsSession()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		restartControlBlock.getTransferOptions().setForceOption(
				ForceOption.USE_FORCE);
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

	/**
	 * Test ref issue: https://github.com/DICE-UNC/jargon/issues/30
	 * 
	 * @throws Exception
	 */
	@Test(expected = FileNotFoundException.class)
	public void testGetCollectionWithTwoFilesWithNoCallbacksNoPermission()
			throws Exception {

		String rootCollection = "testGetCollectionWithTwoFilesWithNoCallbacksNoPermission";
		String returnedLocalCollection = "testGetCollectionWithTwoFilesWithNoCallbacksNoPermissionLocalFiles";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH); // TODO: add

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testGetCollectionWithTwoFilesNoCallbacks", 1, 1, 1,
						"testFile", ".txt", 2, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);

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

		dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						secondaryAccount);

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

	/**
	 * Test ref issue: https://github.com/DICE-UNC/jargon/issues/30
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetCollectionWithTwoFilesWithCallbacksNoPermission()
			throws Exception {

		String rootCollection = "testGetCollectionWithTwoFilesWithCallbacksNoPermission";
		String returnedLocalCollection = "testGetCollectionWithTwoFilesWithCallbacksNoPermissionLocalFiles";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH); // TODO: add

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testGetCollectionWithTwoFilesNoCallbacks", 1, 1, 1,
						"testFile", ".txt", 2, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);

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

		dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						secondaryAccount);

		TestingStatusCallbackListener testCallbackListener = new TestingStatusCallbackListener();

		dataTransferOperationsAO.getOperation(getIrodsFile, returnLocalFile,
				testCallbackListener, null);

		Assert.assertTrue("did not get errors from callbacks",
				testCallbackListener.getErrorCallbacks().size() > 0);

	}

	/**
	 * Get a collection to a target directory, one file will be an overwrite,
	 * and the no force option will cause an exception
	 * 
	 * @throws Exception
	 */
	@Test(expected = OverwriteException.class)
	public void testGetCollectionWithOverwriteNoForce() throws Exception {

		String rootCollection = "testGetCollectionWithOverwriteNoForce";
		String returnedLocalCollection = "testGetCollectionWithOverwriteNoForceReturned";
		String overwriteFileName = "overwriteFileName.txt";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testGetCollectionWithOverwriteNoForce", 1, 1, 1,
						"testFile", ".txt", 5, 5, 1, 2);
		FileGenerator.generateFileOfFixedLengthGivenName(
				localCollectionAbsolutePath, overwriteFileName, 1);

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
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ "/" + returnedLocalCollection + "/" + rootCollection);
		// create the target directory for the get, and seed it with a file
		// causing the overwrite
		File returnedCollectionFileToSeed = new File(absPath);
		returnedCollectionFileToSeed.mkdirs();
		FileGenerator.generateFileOfFixedLengthGivenName(absPath,
				overwriteFileName, 1);

		// now get the files into a local return collection and verify
		IRODSFile getIrodsFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath + "/"
						+ rootCollection);
		String returnLocalCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + returnedLocalCollection);
		File returnLocalFile = new File(returnLocalCollectionAbsolutePath);
		TransferControlBlock tcb = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.NO_FORCE);
		dataTransferOperationsAO.getOperation(getIrodsFile, returnLocalFile,
				null, tcb);

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

	/*
	 * https://github.com/DICE-UNC/jargon/issues/1 transfer get of file with
	 * parens and spaces in name gives file not found #1
	 */
	@Test
	public void testPutThenGetOneFileBug1() throws Exception {
		// generate a local scratch file
		String testFileName = "testPutThenGetOneFileBug1 (1).txt";
		String testRetrievedFileName = "ttestPutThenGetOneFileBug1 (1) Retreived.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

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

	/**
	 * Bug [#1615] special chars (alpha, beta) causing synch to stop
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPutThenGetOneFileWithSpecialChars() throws Exception {
		// generate a local scratch file
		String testFileName = "testPutThenGetOneFileWithSpecialChars���������������������������������������.txt";
		String testRetrievedFileName = "testPutThenGetOneFileRetreived������������������������������������������������������������������������������������������u,1o���������������������������������������.txt";
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

		Assert.assertEquals(origChecksum, retrievedChecksum);
	}

	/**
	 * Create a collection with a few files, then try and put a file that would
	 * be an overwrite. Force is not specified, so it should be an overwrite
	 * exception on the transfer
	 * 
	 * @throws Exception
	 */
	@Test(expected = OverwriteException.class)
	public void testPutCollectionWhereOneFileIsOverwriteNoForceSpecified()
			throws Exception {

		String testCollectionSubdir = "testPutCollectionWhereOneFileIsOverwriteNoForceSpecifiedSubdir";
		int nbrFilesInDir = 5; // make >= 5
		String filePrefix = "fileNameForTesting";
		String fileSuffix = ".txt";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testCollectionSubdir);
		IRODSFile targetIrodsCollectionFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);
		targetIrodsCollectionFile.mkdirs();
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ "/" + testCollectionSubdir + "staging");
		String computedFileName = filePrefix + "2" + fileSuffix;
		FileGenerator.generateFileOfFixedLengthGivenName(absPath,
				computedFileName, 3);

		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		// put initial file into subdir
		File localFile = new File(absPath + computedFileName);
		TransferControlBlock tcb = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.NO_FORCE);

		IRODSFile destFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataTransferOperationsAO.putOperation(localFile, destFile, null, tcb);

		// now generate a number of files in the local collection to transfer
		// again, with the one file already in irods
		String genFileName = "";
		absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ "/" + testCollectionSubdir);
		for (int i = 0; i < nbrFilesInDir; i++) {
			genFileName = filePrefix + i + fileSuffix;
			FileGenerator.generateFileOfFixedLengthGivenName(absPath,
					genFileName, i + 10);
		}

		// no force set, transfer the collection where there will be the 1
		// overwrite error
		tcb.resetTransferData();
		dataTransferOperationsAO.putOperation(localFile, destFile, null, tcb);
		// put the collection with new files + 1 overwrite now
		localFile = new File(absPath + "/");
		destFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataTransferOperationsAO.putOperation(localFile, destFile, null, tcb);

	}

	/**
	 * Create a collection with a few files, then try and put a file that would
	 * be an overwrite. In this case the callback listener will be asked and
	 * should answer 'no', causing a skip, not an exception.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPutCollectionWhereOneFileIsOverwriteAskCallbackListenerRespondsNo()
			throws Exception {

		int firstLength = 11;
		int secondLength = 27;
		String testCollectionSubdir = "testPutCollectionWhereOneFileIsOverwriteAskCallbackListenerRespondsNo";
		int nbrFilesInDir = 5; // make >= 5
		String filePrefix = "fileNameForTesting";
		String fileSuffix = ".txt";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testCollectionSubdir);
		IRODSFile targetIrodsCollectionFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);
		targetIrodsCollectionFile.mkdirs();
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ "/" + testCollectionSubdir + "staging");
		String computedFileName = filePrefix + "2" + fileSuffix;
		FileGenerator.generateFileOfFixedLengthGivenName(absPath,
				computedFileName, firstLength);

		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		// put initial file into subdir
		File localFile = new File(absPath + computedFileName);
		TransferControlBlock tcb = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.NO_FORCE);

		IRODSFile destFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataTransferOperationsAO.putOperation(localFile, destFile, null, tcb);

		// now generate a number of files in the local collection to transfer
		// again, with the one file already in irods
		String genFileName = "";
		absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ "/" + testCollectionSubdir);
		for (int i = 0; i < nbrFilesInDir; i++) {
			genFileName = filePrefix + i + fileSuffix;
			FileGenerator.generateFileOfFixedLengthGivenName(absPath,
					genFileName, secondLength);
		}

		// setup callback listener to say no when file comes up
		tcb.resetTransferData();
		tcb.getTransferOptions().setForceOption(
				ForceOption.ASK_CALLBACK_LISTENER);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();
		transferStatusCallbackListener
				.setForceOption(CallbackResponse.NO_THIS_FILE);
		// put the collection with new files
		localFile = new File(absPath + "/");
		targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		destFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataTransferOperationsAO.putOperation(localFile, destFile,
				transferStatusCallbackListener, tcb);

		// make sure chosen file was not overwritten
		IRODSFile compareIrods = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + testCollectionSubdir,
				computedFileName);
		Assert.assertEquals("chosen overwritten-should have stayed the same",
				firstLength, compareIrods.length());

	}

	/**
	 * Create a collection with a few files, then try and put a file that would
	 * be an overwrite. In this case the callback listener will be asked and
	 * should answer 'yes', causing it to force overwrite
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPutCollectionWhereOneFileIsOverwriteAskCallbackListenerRespondsYes()
			throws Exception {

		int firstLength = 11;
		int secondLength = 27;
		String testCollectionSubdir = "testPutCollectionWhereOneFileIsOverwriteAskCallbackListenerRespondsYes";
		int nbrFilesInDir = 20; // make >= 5
		String filePrefix = "fileNameForTesting";
		String fileSuffix = ".txt";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testCollectionSubdir);
		IRODSFile targetIrodsCollectionFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);
		targetIrodsCollectionFile.mkdirs();
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ "/" + testCollectionSubdir + "staging");
		String computedFileName = filePrefix + "11" + fileSuffix;
		FileGenerator.generateFileOfFixedLengthGivenName(absPath,
				computedFileName, firstLength);

		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		// put initial file into subdir
		File localFile = new File(absPath + computedFileName);
		TransferControlBlock tcb = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.NO_FORCE);

		IRODSFile destFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataTransferOperationsAO.putOperation(localFile, destFile, null, tcb);

		// now generate a number of files in the local collection to transfer
		// again, with the one file already in irods
		String genFileName = "";
		absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ "/" + testCollectionSubdir);
		for (int i = 0; i < nbrFilesInDir; i++) {
			genFileName = filePrefix + i + fileSuffix;
			FileGenerator.generateFileOfFixedLengthGivenName(absPath,
					genFileName, secondLength);
		}

		// setup callback listener to say no when file comes up
		tcb.resetTransferData();
		tcb.getTransferOptions().setForceOption(
				ForceOption.ASK_CALLBACK_LISTENER);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();
		transferStatusCallbackListener
				.setForceOption(CallbackResponse.YES_THIS_FILE);
		// put the collection with new files
		localFile = new File(absPath + "/");
		targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		destFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataTransferOperationsAO.putOperation(localFile, destFile,
				transferStatusCallbackListener, tcb);

		// make sure chosen file was overwritten will have new length
		IRODSFile compareIrods = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + testCollectionSubdir,
				computedFileName);
		Assert.assertEquals("chosen not overwritten", secondLength,
				compareIrods.length());

	}

	/**
	 * Create a collection with a few files, then try and put a file that would
	 * be an overwrite. Force is specified, so it should be an overwrite the
	 * file
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPutCollectionWhereOneFileIsOverwriteForceSpecified()
			throws Exception {

		int firstLength = 11;
		int secondLength = 27;
		String testCollectionSubdir = "testPutCollectionWhereOneFileIsOverwriteForceSpecified";
		int nbrFilesInDir = 5; // make >= 5
		String filePrefix = "fileNameForTesting";
		String fileSuffix = ".txt";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testCollectionSubdir);
		IRODSFile targetIrodsCollectionFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);
		targetIrodsCollectionFile.mkdirs();
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ "/" + testCollectionSubdir + "staging");
		String computedFileName = filePrefix + "2" + fileSuffix;
		FileGenerator.generateFileOfFixedLengthGivenName(absPath,
				computedFileName, firstLength);

		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		// put initial file into subdir
		File localFile = new File(absPath + computedFileName);
		TransferControlBlock tcb = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.USE_FORCE);

		IRODSFile destFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataTransferOperationsAO.putOperation(localFile, destFile, null, tcb);

		// now generate a number of files in the local collection to transfer
		// again, with the one file already in irods
		String genFileName = "";
		absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ "/" + testCollectionSubdir);
		for (int i = 0; i < nbrFilesInDir; i++) {
			genFileName = filePrefix + i + fileSuffix;
			FileGenerator.generateFileOfFixedLengthGivenName(absPath,
					genFileName, secondLength);
		}

		tcb.resetTransferData();
		dataTransferOperationsAO.putOperation(localFile, destFile, null, tcb);
		// put the collection with new files + 1 overwrite now
		localFile = new File(absPath + "/");
		destFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataTransferOperationsAO.putOperation(localFile, destFile, null, tcb);

		// make sure chosen file was overwritten will have new length

		IRODSFile compareIrods = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + testCollectionSubdir,
				computedFileName);
		Assert.assertEquals("chosen file not overwritten", secondLength,
				compareIrods.length());
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
	public void testPutWithSkip() throws Exception {

		String rootCollection = "testPutWithSkip";
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();

		/*
		 * Keep an even number for testing skips
		 */
		int fileCtr = 10;

		// listener will give skip callback for odd files
		TransferStatusCallbackListenerTestingImplementation listener = new TransferStatusCallbackListenerTestingImplementation(
				transferControlBlock, 0, 0, true);

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator.generateManyFilesInParentCollectionByAbsolutePath(
				localCollectionAbsolutePath, "testPutWithCancel", ".txt",
				fileCtr, 1, 2);

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

		int expectedSkipped = fileCtr / 2;
		Assert.assertEquals("didnt get an init callback for each file",
				fileCtr, listener.getInitCallbackCtr());
		Assert.assertEquals("didnt get skips for half", expectedSkipped,
				listener.getSkipCtr());
		Assert.assertEquals("didnt get transfers for half", expectedSkipped,
				listener.getPutCallbackCtr());

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

		// check for two resources

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		List<Resource> resources = dataObjectAO
				.listFileResources(targetIrodsFile);

		Assert.assertEquals("should be two resources for this file", 2,
				resources.size());

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
	public void testGetWithSkipOfEvenFiles() throws Exception {

		String rootCollection = "testGetWithSkipOfEvenFiles";
		String returnedLocalCollection = "testGetWithSkipOfEvenFilesReturned";

		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();

		TransferStatusCallbackListenerTestingImplementation listener = new TransferStatusCallbackListenerTestingImplementation(
				transferControlBlock, 0, 0, true);

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath, rootCollection, 3, 4, 2,
						"prefix", ".suffix", 7, 2, 1, 3);

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

		Assert.assertEquals("transfer controls out of balance",
				listener.getInitCallbackCtr(),
				transferControlBlock.getTotalFilesTransferredSoFar());

		Assert.assertEquals(
				"skip counts disagree between listener and transferControlBlock",
				listener.getSkipCtr(),
				transferControlBlock.getTotalFilesSkippedSoFar());

	}

	/**
	 * FIXME: looks like an iRODS bug? Ignored for now see
	 * https://github.com/DICE-UNC/jargon/issues/63
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMoveSourceCollectionTargetCollectionBug63()
			throws Exception {

		String rootCollection = "testMoveSourceCollectionTargetCollectionBug63";
		String rootCollection2 = "testMoveSourceCollectionTargetCollectionBug63Coll2";
		String testFileName = "testMoveSourceCollectionTargetCollectionBug63.txt";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ rootCollection);
		String irodsCollectionRoot2AbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ rootCollection2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);

		IRODSFile rootFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		IRODSFile root2File = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRoot2AbsolutePath);

		rootFile.mkdirs();

		FileGenerator.generateFileOfFixedLengthGivenName(
				localCollectionAbsolutePath, testFileName, 10);

		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath + "/"
				+ testFileName);

		dataTransferOperationsAO.putOperation(localFile, rootFile, null, null);
		dataTransferOperationsAO.move(rootFile, root2File);

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

		dataTransferOperationsAO.move(irodsCollectionRootAbsolutePath + "/"
				+ rootCollection, targetParent.getAbsolutePath());

		irodsFileSystem = IRODSFileSystem.instance();
		irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionTargetAbsolutePath);

		Assert.assertTrue("did not find expected targetcollection",
				destFile.isDirectory());
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

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(fileNameOrig, targetIrodsCollection, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null,
				null);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		// make the target irods collection
		IRODSFile targetCollectionFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + '/' + targetCollection);
		targetCollectionFile.mkdir();

		dto.move(irodsFile.getAbsolutePath(),
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

	@SuppressWarnings("deprecation")
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

	/**
	 * Normal test of consilidated 'copy()' method, this time with a collection,
	 * using the string path sigs
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCopyCollectionToTargetCollection() throws Exception {

		// generate a local scratch file
		String testOrigDirectory = "testCopyCollectionToTargetCollectionB";
		String testTargetDirectory = "testCopyCollectionToTargetCollectionBTarget";

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
				irodsTargetAbsolutePath, null, null);

		File localFile = new File(localCollectionAbsolutePath);
		IRODSFile targetFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						irodsTargetAbsolutePath, testOrigDirectory);

		// compare the local source to the copied-to target
		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(
				localFile, (File) targetFile);

	}

	@Test
	public void testCopyCollectionToTargetCollectionWithResource()
			throws Exception {

		// generate a local scratch file
		String testOrigDirectory = "testCopyCollectionToTargetCollectionWithResource";
		String testTargetDirectory = "testCopyCollectionToTargetCollectionWithResourceTarget";

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

		String targetResource = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY);

		dataTransferOperations.copy(irodsOriginalAbsolutePath, targetResource,
				irodsTargetAbsolutePath, null, null);

		File localFile = new File(localCollectionAbsolutePath);
		IRODSFile targetFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						irodsTargetAbsolutePath, testOrigDirectory);

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		// compare the local source to the copied-to target
		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(
				localFile, (File) targetFile);

		String foundResc = getResourceFromFiles(targetFile, dataObjectAO);
		Assert.assertEquals("did not set correct resource", targetResource,
				foundResc);

	}

	@Test
	public void testCopyCollectionToTargetCollectionWithResourceInIrodsAccount()
			throws Exception {

		// generate a local scratch file
		String testOrigDirectory = "testCopyCollectionToTargetCollectionWithResourceInIrodsAccount";
		String testTargetDirectory = "testCopyCollectionToTargetCollectionWithResourceInIrodsAccountTarget";

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

		String targetResource = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY);
		irodsAccount.setDefaultStorageResource(targetResource);

		dataTransferOperations.copy(irodsOriginalAbsolutePath, "",
				irodsTargetAbsolutePath, null, null);

		File localFile = new File(localCollectionAbsolutePath);
		IRODSFile targetFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						irodsTargetAbsolutePath, testOrigDirectory);

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		// compare the local source to the copied-to target
		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(
				localFile, (File) targetFile);

		String foundResc = getResourceFromFiles(targetFile, dataObjectAO);
		Assert.assertEquals("did not set correct resource", targetResource,
				foundResc);

	}

	private String getResourceFromFiles(IRODSFile parent,
			DataObjectAO dataObjectAO) throws Exception {
		String resource = "";
		for (File child : parent.listFiles()) {
			if (child.isFile()) {
				List<Resource> resources = dataObjectAO.listFileResources(child
						.getAbsolutePath());
				if (resources.isEmpty()) {
					break;
				} else {
					resource = resources.get(0).getName();
					break;
				}
			} else {
				resource = getResourceFromFiles((IRODSFile) child, dataObjectAO);
				break;
			}
		}
		return resource;

	}

	/**
	 * Normal test of consilidated 'copy()' method, this time with a collection,
	 * using the string path sigs. This will do this twice, simulating an
	 * overwrite, but I have force turned on
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCopyCollectionToTargetCollectionOverwriteForce()
			throws Exception {

		// generate a local scratch file
		String testOrigDirectory = "testCopyCollectionToTargetCollectionOverwriteForce";
		String testTargetDirectory = "testCopyCollectionToTargetCollectionOverwriteForceTarget";

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
		TransferControlBlock tcb = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.USE_FORCE);

		dataTransferOperations.putOperation(localCollectionAbsolutePath,
				irodsCollectionRootAbsolutePath, "", null, null);

		dataTransferOperations.copy(irodsOriginalAbsolutePath, "",
				irodsTargetAbsolutePath, null, null);
		dataTransferOperations.copy(irodsOriginalAbsolutePath, "",
				irodsTargetAbsolutePath, null, tcb);

		File localFile = new File(localCollectionAbsolutePath);
		IRODSFile targetFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						irodsTargetAbsolutePath, testOrigDirectory);

		// compare the local source to the copied-to target
		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(
				localFile, (File) targetFile);

	}

	/**
	 * Normal copy operation with tcb to noforce option, should just copy the
	 * file
	 * 
	 * @throws Exception
	 */
	@Test(expected = OverwriteException.class)
	public final void testCopyIRODSDataObjectToDataObjectNoForceWhenOverwrite()
			throws Exception {

		String testFileName = "testCopyIRODSDataObjectToDataObjectNoForceWhenOverwrite.txt";
		String testCopyToFileName = "testCopyIRODSDataObjectToDataObjectNoForceWhenOverwriteCopyTo.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, null, null);
		IRODSFile irodsSourceFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + testFileName);
		IRODSFile irodsTargetFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + testCopyToFileName);
		TransferControlBlock tcb = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.NO_FORCE);
		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperations
				.copy(irodsSourceFile, irodsTargetFile, null, tcb);
		dataTransferOperations
				.copy(irodsSourceFile, irodsTargetFile, null, tcb);

	}

	/**
	 * Normal copy operation with tcb to force option, should just copy the file
	 * without exception
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testCopyIRODSDataObjectToDataObjectForceWhenOverwrite()
			throws Exception {

		String testFileName = "testCopyIRODSDataObjectToDataObjectForceWhenOverwrite.txt";
		String testCopyToFileName = "testCopyIRODSDataObjectToDataObjectForceWhenOverwriteCopyTo.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, null, null);
		IRODSFile irodsSourceFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + testFileName);
		IRODSFile irodsTargetFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + testCopyToFileName);
		TransferControlBlock tcb = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.USE_FORCE);
		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperations
				.copy(irodsSourceFile, irodsTargetFile, null, tcb);
		dataTransferOperations
				.copy(irodsSourceFile, irodsTargetFile, null, tcb);
		Assert.assertTrue(true); // really just looking for no excep

	}

	/**
	 * Normal copy operation with tcb, this uses the signature that takes
	 * strings instead of files
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testCopyIRODSDataObjectToDataObjectNoForceNoOverwriteStringSignatures()
			throws Exception {

		String testFileName = "testCopyIRODSDataObjectToDataObjectNoForceNoOverwriteStringSignatures.txt";
		String testCopyToFileName = "testCopyIRODSDataObjectToDataObjectNoForceNoOverwriteStringSignaturesCopyTo.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, null, null);
		IRODSFile irodsSourceFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + testFileName);
		IRODSFile irodsTargetFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + testCopyToFileName);
		TransferControlBlock tcb = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.NO_FORCE);
		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperations.copy(irodsSourceFile.getAbsolutePath(), "",
				irodsTargetFile.getAbsolutePath(), null, tcb);

		Assert.assertTrue(true); // really just looking for no excep

	}

	@SuppressWarnings("deprecation")
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

	@SuppressWarnings("deprecation")
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

	@SuppressWarnings("deprecation")
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

	@SuppressWarnings("deprecation")
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

	// @Test
	@SuppressWarnings("deprecation")
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

	/**
	 * https://github.com/DICE-UNC/jargon/issues/63
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMoveThenMoveBackBug63() throws Exception {

		String rootCollection = "testMoveThenMoveBackBug63";
		String targetCollection = "testMoveThenMoveBackBug63TargetCollection";

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

		dataTransferOperationsAO.move(irodsCollectionRootAbsolutePath + "/"
				+ rootCollection, targetParent.getAbsolutePath());

		// now move it back to the source
		dataTransferOperationsAO.move(targetParent.getAbsolutePath(),
				irodsCollectionRootAbsolutePath + "/" + rootCollection);

		// expect this to run without error, with the reported bug being
		// org.irods.jargon.core.exception.JargonFileOrCollAlreadyExistsException:
		// Collection already exists

	}

	/**
	 * Replication sequence for bug [#1044] Jargon allows the creating of
	 * folders that exceed the USER_PATH_EXCEEDS_MAX and cannot delete them
	 * 
	 * @throws Exception
	 */
	@Test(expected = PathTooLongException.class)
	public void testLongFileNameAddAndDeleteBugDataObjectNameIsLong1044()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String localTestFileName = "testLongFileNameAddAndDeleteBugDataObjectNameIsLong1044.txt";
		String dataObjecName = FileGenerator.generateRandomString(1068)
				+ ".txt";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		File topScratchFile = new File(absPath);
		topScratchFile.mkdirs();
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, localTestFileName,
						2);

		File localFile = new File(localFileName);

		DataTransferOperations dto = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		IRODSFile filePutToIrods = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection,
				dataObjecName);
		dto.putOperation(localFile, filePutToIrods, null, null);

	}

}
