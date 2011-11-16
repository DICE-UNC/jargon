package org.irods.jargon.core.pub;

import java.io.File;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class FederatedCollectionAndDataObjectListAndSearchAOImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "FederatedCollectionAndDataObjectListAndSearchAOImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;
	public static final String FED_READ_DIR = "fedread";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
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
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testSomething() {
		
	}
	
	@Ignore //FIXME: currently ignored
	public void testListCollectionWithOneDataObject() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		/*
		 * put a collection on the federated zone, this will be accessed from a
		 * user on the primary zone this will use the fedread directory as
		 * specified in the6 test-scripts/fedtestsetup_fedzonex.sh scripts.
		 */

		String fileName = "testListCollectionWithOneDataObject.txt";
		String testSubdir = "testListCollectionWithOneDataObject";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);

		String targetIrodsPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(
						testingProperties, "/" + IRODS_TEST_SUBDIR_PATH + "/"
								+ testSubdir);

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, fileName, 3);
		File localFile = new File(localFilePath);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsPath);
		
		// delete to clean up
		destFile.deleteWithForceOption();
		destFile.mkdirs();
		
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
		
		/*
		 * setup done, now connect from the first zone and try to list the coll with the data object
		 */
		
		IRODSAccount fedAccount = testingPropertiesHelper
		.buildIRODSAccountFromTestProperties(testingProperties);
		
		CollectionAndDataObjectListAndSearchAO collectionListAndSearchAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAndDataObjectListAndSearchAO(fedAccount);
		List<CollectionAndDataObjectListingEntry> entries = collectionListAndSearchAO.listDataObjectsUnderPath(targetIrodsPath, 0);		
		TestCase.assertNotNull("null entries returned", entries);
		//TestCase.assertFalse("no entries found for federated collection", entries.isEmpty());
				
	}

}
