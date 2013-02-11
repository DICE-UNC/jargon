package org.irods.jargon.core.pub;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.domain.ObjStat.SpecColType;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MountedCollectionAOImplForMSSOTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "MountedCollectionAOImplForMSSOTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
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
	public void testMountMSSOWorkflow() throws Exception {
		String targetCollectionName = "testMountMSSOWorkflow";
		String subMountCollection =  "testMountMSSOWorkflowMounted";
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
								+ targetCollectionName + "/" + subMountCollection);
		
				IRODSFile parentCollection = irodsFileSystem
						.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
								targetIrodsCollection);
				parentCollection.mkdirs();
				
		// put the test msso out there
		String workflowFileTransferredToIrods = targetIrodsCollection + "/" + "eCWkflow.mss";
		Stream2StreamAO stream2Stream = irodsFileSystem.getIRODSAccessObjectFactory().getStream2StreamAO(irodsAccount);
		stream2Stream.streamClasspathResourceToIRODSFile(mssoFile, workflowFileTransferredToIrods);
		
		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(mountedCollectionPath,
				irodsAccount.getDefaultStorageResource());

		// create the msso workflow mount

		mountedCollectionAO.createAnMSSOMount(workflowFileTransferredToIrods,mountedCollectionPath);
		
		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		ObjStat mountedCollection = collectionAndDataObjectListAndSearchAO.retrieveObjectStatForPath(mountedCollectionPath);
		Assert.assertNotNull("no objstat",mountedCollection);
		Assert.assertEquals("did not mount a struct file type", SpecColType.STRUCT_FILE_COLL, mountedCollection.getSpecColType());
		
	}
	
	@Test(expected=FileNotFoundException.class)
	public void testMountMSSOWorkflowNotExists() throws Exception {
		String targetCollectionName = "testMountMSSOWorkflowNotExists";
		String subMountCollection =  "testMountMSSOWorkflowNotExistsMounted";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);
		
		String mountedCollectionPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName + "/" + subMountCollection);
		
				IRODSFile parentCollection = irodsFileSystem
						.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
								targetIrodsCollection);
				parentCollection.mkdirs();
				
		// put the test msso out there
		String workflowFileTransferredToIrods = targetIrodsCollection + "/" + "eCWkflow.mss";
		
		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(mountedCollectionPath,
				irodsAccount.getDefaultStorageResource());

		// create the msso workflow mount

		mountedCollectionAO.createAnMSSOMount(workflowFileTransferredToIrods,mountedCollectionPath);
		
	}
	
	
	@Test
	public void testUnmountMSSOWorkflow() throws Exception {
		String targetCollectionName = "testUnmountMSSOWorkflow";
		String subMountCollection =  "testUnmountMSSOWorkflowMounted";
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
								+ targetCollectionName + "/" + subMountCollection);
		
				IRODSFile parentCollection = irodsFileSystem
						.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
								targetIrodsCollection);
				parentCollection.mkdirs();
				
		// put the test msso out there
		String workflowFileTransferredToIrods = targetIrodsCollection + "/" + "eCWkflow.mss";
		Stream2StreamAO stream2Stream = irodsFileSystem.getIRODSAccessObjectFactory().getStream2StreamAO(irodsAccount);
		stream2Stream.streamClasspathResourceToIRODSFile(mssoFile, workflowFileTransferredToIrods);
		
		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(mountedCollectionPath,
				irodsAccount.getDefaultStorageResource());

		// create the msso workflow mount

		mountedCollectionAO.createAnMSSOMount(workflowFileTransferredToIrods,mountedCollectionPath);
		
		mountedCollectionAO.unmountACollection(mountedCollectionPath, irodsAccount.getDefaultStorageResource());
		
		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		ObjStat mountedCollection = collectionAndDataObjectListAndSearchAO.retrieveObjectStatForPath(mountedCollectionPath);
		Assert.assertNotNull("no objstat",mountedCollection);
		Assert.assertEquals("should now be a normal collection", SpecColType.NORMAL, mountedCollection.getSpecColType());
		
	}


}
