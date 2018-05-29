package org.irods.jargon.core.pub;

import java.io.File;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.domain.ObjStat.SpecColType;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
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
		scratchFileUtils.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testMountMSSOWorkflow() throws Exception {

		if (!testingPropertiesHelper.isTestWorkflow(testingProperties)) {
			return;
		}

		String targetCollectionName = "testMountMSSOWorkflow";
		String subMountCollection = "testMountMSSOWorkflowMounted";
		String mssoFile = "/msso/eCWkflow.mss";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + targetCollectionName);

		String mountedCollectionPath = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + targetCollectionName + "/" + subMountCollection);

		IRODSFile parentCollection = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		parentCollection.mkdirs();

		String workflowFileTransferredToIrods = targetIrodsCollection + "/" + "eCWkflow.mss";
		File mssoAsFile = LocalFileUtils.getClasspathResourceAsFile(mssoFile);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getMountedCollectionAO(irodsAccount);

		mountedCollectionAO.unmountACollection(mountedCollectionPath, irodsAccount.getDefaultStorageResource());

		// create the msso workflow mount

		mountedCollectionAO.createAnMSSOMountForWorkflow(mssoAsFile.getAbsolutePath(), workflowFileTransferredToIrods,
				mountedCollectionPath);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		ObjStat mountedCollection = collectionAndDataObjectListAndSearchAO
				.retrieveObjectStatForPath(mountedCollectionPath);
		Assert.assertNotNull("no objstat", mountedCollection);
		Assert.assertEquals("did not mount a struct file type", SpecColType.STRUCT_FILE_COLL,
				mountedCollection.getSpecColType());
		ObjStat wssObjStat = collectionAndDataObjectListAndSearchAO
				.retrieveObjectStatForPath(workflowFileTransferredToIrods);
		Assert.assertNotNull("null objstat for wss file", wssObjStat);

	}
}
