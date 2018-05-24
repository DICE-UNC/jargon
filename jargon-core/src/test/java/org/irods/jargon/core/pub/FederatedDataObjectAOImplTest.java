package org.irods.jargon.core.pub;

import java.io.File;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.Assert;

public class FederatedDataObjectAOImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "FederatedDataObjectAOImplTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testFindByCollectionPathAndDataNameCrossZone() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}
		// generate a local scratch file
		String testFileName = "testFindByCollectionPathAndDataNameCrossZone.dat";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 10);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(testingProperties,
						IRODS_TEST_SUBDIR_PATH);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(testingProperties,
						IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		File sourceFile = new File(localFileName);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);
		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);

		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		destFile.deleteWithForceOption();
		dto.putOperation(sourceFile, destFile, null, null);

		IRODSAccount localZoneAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(localZoneAccount);

		DataObject dataObject = dataObjectAO.findByCollectionNameAndDataName(targetIrodsCollection, testFileName);
		Assert.assertNotNull("null data object, was not found", dataObject);

	}

	/**
	 * Replicates bug -832000 in cloud browser clicking on file in fed zone #152
	 *
	 * @throws Exception
	 */
	@Test
	public void testFindByCollectionPathAndDataNameCrossZoneInForeignZoneHomeDirBug152() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}
		// generate a local scratch file
		String testFileName = "testFindByCollectionPathAndDataNameCrossZoneInForeignZoneHomeDirBug152.dat";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 10);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneHomeDirTestProperties(testingProperties,
						IRODS_TEST_SUBDIR_PATH);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneHomeDirTestProperties(testingProperties,
						IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		File sourceFile = new File(localFileName);
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);

		IRODSFile targetIrodsCollectionAsIrodsFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection);
		targetIrodsCollectionAsIrodsFile.mkdirs();

		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		destFile.deleteWithForceOption();
		dto.putOperation(sourceFile, destFile, null, null);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		DataObject dataObject = dataObjectAO.findByCollectionNameAndDataName(targetIrodsCollection, testFileName);
		Assert.assertNotNull("null data object, was not found", dataObject);

	}

}
