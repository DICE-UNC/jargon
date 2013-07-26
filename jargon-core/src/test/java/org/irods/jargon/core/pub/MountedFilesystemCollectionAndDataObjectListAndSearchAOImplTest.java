package org.irods.jargon.core.pub;

import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MountedFilesystemCollectionAndDataObjectListAndSearchAOImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "MountedFilesystemCollectionAndDataObjectListAndSearchAOImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
	
		
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		
		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}
		
		
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(
				testingProperties);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}
		
		irodsFileSystem.closeAndEatExceptions();

	}

	@Test
	public void testListFilesInMountedDir() throws Exception {
		
		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}
		
		// this test requires the prop test.option.reg.basedir to be set, and to contain the contents of test-data/reg.  This is a manual setup step
		
		String targetCollectionName = "testListFilesInMountedDirMountedx";
		String localMountDir = "testListFilesInMountedDirLocal";
		String scratchDir = "testListFilesInMountedDir";

		//String localCollectionAbsolutePath = testingProperties.getProperty(TestingPropertiesHelper.IRODS_REG_BASEDIR);
		
		String localCollectionAbsolutePath = "/home/test1/reg";
		String localScratchAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + scratchDir);
		
		
		FileGenerator.generateManyFilesInParentCollectionByAbsolutePath(
				localScratchAbsolutePath,
				"testCreateAndRemoveMountedFileSystem", ".txt", 10, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);
		
		IRODSFile unmountFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection);
		unmountFile.delete();

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		mountedCollectionAO.createMountedFileSystemCollection(
				localCollectionAbsolutePath, targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());
		
		
		// put the scratch files to the mount
		
		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory().getDataTransferOperations(irodsAccount);
		dto.putOperation(localScratchAbsolutePath, targetIrodsCollection, irodsAccount.getDefaultStorageResource(), null, null);
		

		CollectionAndDataObjectListAndSearchAO ao = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		List<CollectionAndDataObjectListingEntry> actual = ao
				.listDataObjectsUnderPath(targetIrodsCollection, 0);
		Assert.assertFalse("no results", actual.isEmpty());

	}

}
