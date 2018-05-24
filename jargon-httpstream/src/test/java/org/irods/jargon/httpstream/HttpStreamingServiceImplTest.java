package org.irods.jargon.httpstream;

import java.io.File;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import junit.framework.Assert;

public class HttpStreamingServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "HttpStreamingServiceImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		irodsFileSystem = IRODSFileSystem.instance();
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

	@Ignore
	// see https://github.com/DICE-UNC/jargon/issues/125
	public final void testStreamHttpUrlContentsToIRODSFileCollectionIsTarget() throws Exception {
		// generate a local scratch file
		String testRetrievedFileName = "testStreamHttpUrlContentsToIRODSFileCollectionIsTarget.txt";
		// String testUrl = "http://www.renci.org/~lisa/bigiPlantFile.txt";
		String testUrl = "http://www.unc.edu";

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		destFile.mkdirs();
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);

		HttpStreamingService httpStreamingService = new HttpStreamingServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		String irodsTargetFileAbsPathFromStreaming = httpStreamingService.streamHttpUrlContentsToIRODSFile(testUrl,
				destFile, null, null);
		destFile = irodsFileFactory.instanceIRODSFile(irodsTargetFileAbsPathFromStreaming);

		// now get
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		File retrievedLocalFile = new File(absPath + testRetrievedFileName);

		dataTransferOperationsAO.getOperation(destFile, retrievedLocalFile, null, null);
		Assert.assertTrue("file could not be brought back from irods", retrievedLocalFile.exists());
		Assert.assertTrue("file has no data", retrievedLocalFile.length() > 0);

	}

	@Ignore
	// see https://github.com/DICE-UNC/jargon/issues/125
	public final void testStreamHttpUrlContentsToIRODSFileFileIsTarget() throws Exception {
		// generate a local scratch file
		String testFileName = "testStreamHttpUrlContentsToIRODSFileFileIsTarget.txt";
		String testRetrievedFileName = "testStreamHttpUrlContentsToIRODSFileFileIsTargetResult.txt";
		// String testUrl = "http://www.renci.org/~lisa/bigiPlantFile.txt";
		String testUrl = "http://www.unc.edu";

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testFileName);

		// now put the file
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);

		HttpStreamingService httpStreamingService = new HttpStreamingServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		String irodsTargetFileAbsPathFromStreaming = httpStreamingService.streamHttpUrlContentsToIRODSFile(testUrl,
				destFile, null, null);
		destFile = irodsFileFactory.instanceIRODSFile(irodsTargetFileAbsPathFromStreaming);

		// now get
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		File retrievedLocalFile = new File(absPath + testRetrievedFileName);

		dataTransferOperationsAO.getOperation(destFile, retrievedLocalFile, null, null);
		Assert.assertTrue("file could not be brought back from irods", retrievedLocalFile.exists());
		Assert.assertTrue("file has no data", retrievedLocalFile.length() > 0);

	}

	@Test(expected = HttpStreamingException.class)
	public final void testStreamHttpUrlContentsToIRODSFileURLDoesNotExist() throws Exception {

		String testUrl = "http://127.0.0.1/idontexist";

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		destFile.mkdirs();
		HttpStreamingService httpStreamingService = new HttpStreamingServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		httpStreamingService.streamHttpUrlContentsToIRODSFile(testUrl, destFile, null, null);

	}

}
