package org.irods.jargon.core.pub;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class CollectionAndDataObjectListAndSearchAOImplForMSSOTest {
	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	public static final String IRODS_TEST_SUBDIR_PATH = "CollectionAndDataObjectListAndSearchAOImplForMSSOTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		new org.irods.jargon.testutils.filemanip.ScratchFileUtils(
				testingProperties);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
		IRODSAccount.instance("srbbrick15.ucsd.edu", 9947, "rods", "RODS", "",
				"raja8", "");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testGetFullObjectForStructuredCollection() throws Exception {

		String targetCollectionName = "testMountMSSOWorkflow";
		String subMountCollection = "testMountMSSOWorkflowMounted";
		String mssoFile = "/msso/eCWkflow.mss";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);

		String mountedCollectionPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName + "/"
								+ subMountCollection);

		IRODSFile parentCollection = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		parentCollection.mkdirs();

		// put the test msso out there
		String workflowFileTransferredToIrods = targetIrodsCollection + "/"
				+ "eCWkflow.mss";

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(mountedCollectionPath,
				irodsAccount.getDefaultStorageResource());

		// create the msso workflow mount
		File mssoAsFile = LocalFileUtils.getClasspathResourceAsFile(mssoFile);

		mountedCollectionAO.createAnMSSOMountForWorkflow(
				mssoAsFile.getAbsolutePath(), workflowFileTransferredToIrods,
				mountedCollectionPath);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		Object actual = collectionAndDataObjectListAndSearchAO
				.getFullObjectForType(mountedCollectionPath);
		Assert.assertNotNull("object was null", actual);
		boolean isCollection = actual instanceof Collection;
		Assert.assertTrue("was not a collection", isCollection);

	}

	@Test
	public void testListCollectionsUnderStructCollPathWhenNoDataInSubdirTestFor522002Error()
			throws Exception {

		String targetCollectionName = "testListCollectionsUnderStructCollPath";
		String subMountCollection = "testListCollectionsUnderStructCollPathMounted";
		String mssoFile = "/msso/eCWkflow.mss";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);

		String mountedCollectionPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName + "/"
								+ subMountCollection);

		IRODSFile parentCollection = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		parentCollection.mkdirs();

		// put the test msso out there
		String workflowFileTransferredToIrods = targetIrodsCollection + "/"
				+ "eCWkflow.mss";

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(mountedCollectionPath,
				irodsAccount.getDefaultStorageResource());

		// create the msso workflow mount

		File mssoAsFile = LocalFileUtils.getClasspathResourceAsFile(mssoFile);

		mountedCollectionAO.createAnMSSOMountForWorkflow(
				mssoAsFile.getAbsolutePath(), workflowFileTransferredToIrods,
				mountedCollectionPath);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		List<CollectionAndDataObjectListingEntry> entries = collectionAndDataObjectListAndSearchAO
				.listDataObjectsAndCollectionsUnderPath(mountedCollectionPath);
		Assert.assertNotNull("null entries returned", entries);

	}

	// FIXME: get rid of
	/*
	 * @Test public void testListCollectionsRaja() throws Exception {
	 * 
	 * String targetIrodsCollection = "/raja8/home/rods/msso/mssot1";
	 * 
	 * 
	 * CollectionAndDataObjectListAndSearchAO
	 * collectionAndDataObjectListAndSearchAO =
	 * irodsFileSystem.getIRODSAccessObjectFactory
	 * ().getCollectionAndDataObjectListAndSearchAO(rajaAccount);
	 * 
	 * List<CollectionAndDataObjectListingEntry> entries =
	 * collectionAndDataObjectListAndSearchAO
	 * .listCollectionsUnderPath(targetIrodsCollection, 0);
	 * Assert.assertNotNull("null entries returned", entries);
	 * 
	 * 
	 * }
	 */

	// FIXME: should go into workflow-specific test
	@Test
	public void testIngestWorkflowParameterFile() throws Exception {
		String targetCollectionName = "testIngestWorkflowParameterFile";
		String targetStageCollectionName = "staging";
		String subMountCollection = "testIngestWorkflowParameterFileMounted";
		String mssoFile = "/msso/eCWkflow.mss";
		String mssoParamFile = "/msso/eCWkflow.mpf";
		String targetParamFile = "eCWkflow.mpf";
		String stage1Name = "stage1.txt";
		String stage2Name = "stage2.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);

		String targetStagingCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName + "/"
								+ targetStageCollectionName);

		String mountedCollectionPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName + "/"
								+ subMountCollection);

		IRODSFile parentCollection = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		parentCollection.mkdirs();

		IRODSFile stagingCollection = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetStagingCollection);
		stagingCollection.mkdirs();

		// two files in staging
		IRODSFile stagingFile1 = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetStagingCollection,
				stage1Name);
		stagingFile1.createNewFile();
		IRODSFile stagingFile2 = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetStagingCollection,
				stage2Name);
		stagingFile2.createNewFile();

		// put the test msso out there
		String workflowFileTransferredToIrods = targetIrodsCollection + "/"
				+ "eCWkflow.mss";
		Stream2StreamAO stream2Stream = irodsFileSystem
				.getIRODSAccessObjectFactory().getStream2StreamAO(irodsAccount);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(mountedCollectionPath,
				irodsAccount.getDefaultStorageResource());

		// create the msso workflow mount

		File mssoAsFile = LocalFileUtils.getClasspathResourceAsFile(mssoFile);

		mountedCollectionAO.createAnMSSOMountForWorkflow(
				mssoAsFile.getAbsolutePath(), workflowFileTransferredToIrods,
				mountedCollectionPath);
		// create a param file and stage it to the now mounted workflow
		String workflowParamAsString = LocalFileUtils
				.getClasspathResourceFileAsString(mssoParamFile);
		workflowParamAsString = workflowParamAsString.replaceAll("stagein1",
				stagingFile1.getAbsolutePath());
		workflowParamAsString = workflowParamAsString.replaceAll("stagein2",
				stagingFile2.getAbsolutePath());

		// put the parameter file, causing the workflow to fire
		String paramFilePath = mountedCollectionPath + "/" + targetParamFile;
		IRODSFile paramFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(paramFilePath);
		stream2Stream.streamBytesToIRODSFile(workflowParamAsString.getBytes(),
				paramFile);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		List<CollectionAndDataObjectListingEntry> entries = collectionAndDataObjectListAndSearchAO
				.listDataObjectsAndCollectionsUnderPath(mountedCollectionPath);
		Assert.assertNotNull("null entries returned", entries);

	}

	@Test
	public void testGetFullObjectForTypeInTestWorkflow() throws Exception {
		String targetCollectionName = "testGetFullObjectForTypeInTestWorkflow";
		String targetStageCollectionName = "staging";
		String subMountCollection = "testGetFullObjectForTypeInTestWorkflowMounted";
		String mssoFile = "/msso/eCWkflow.mss";
		String mssoParamFile = "/msso/eCWkflow.mpf";
		String targetParamFile = "eCWkflow.mpf";
		String stage1Name = "stage1.txt";
		String stage2Name = "stage2.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);

		String targetStagingCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName + "/"
								+ targetStageCollectionName);

		String mountedCollectionPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName + "/"
								+ subMountCollection);

		IRODSFile parentCollection = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		parentCollection.mkdirs();

		IRODSFile stagingCollection = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetStagingCollection);
		stagingCollection.mkdirs();

		// two files in staging
		IRODSFile stagingFile1 = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetStagingCollection,
				stage1Name);
		stagingFile1.createNewFile();
		IRODSFile stagingFile2 = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetStagingCollection,
				stage2Name);
		stagingFile2.createNewFile();

		// put the test msso out there
		String workflowFileTransferredToIrods = targetIrodsCollection + "/"
				+ "eCWkflow.mss";
		Stream2StreamAO stream2Stream = irodsFileSystem
				.getIRODSAccessObjectFactory().getStream2StreamAO(irodsAccount);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(mountedCollectionPath,
				irodsAccount.getDefaultStorageResource());

		// create the msso workflow mount

		File mssoAsFile = LocalFileUtils.getClasspathResourceAsFile(mssoFile);

		mountedCollectionAO.createAnMSSOMountForWorkflow(
				mssoAsFile.getAbsolutePath(), workflowFileTransferredToIrods,
				mountedCollectionPath);
		// create a param file and stage it to the now mounted workflow
		String workflowParamAsString = LocalFileUtils
				.getClasspathResourceFileAsString(mssoParamFile);
		workflowParamAsString = workflowParamAsString.replaceAll("stagein1",
				stagingFile1.getAbsolutePath());
		workflowParamAsString = workflowParamAsString.replaceAll("stagein2",
				stagingFile2.getAbsolutePath());

		// put the parameter file, causing the workflow to fire
		String paramFilePath = mountedCollectionPath + "/" + targetParamFile;
		IRODSFile paramFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(paramFilePath);
		stream2Stream.streamBytesToIRODSFile(workflowParamAsString.getBytes(),
				paramFile);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		Object actual = collectionAndDataObjectListAndSearchAO
				.getFullObjectForType(mountedCollectionPath + "/"
						+ "eCWkflow.run");
		Assert.assertNotNull("no data object", actual);
		// FIXME:add field checks, map various paths here

	}

}
