package org.irods.jargon.core.pub;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.packinstr.TransferOptions.ForceOption;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests various data transfer operations between federated zones.
 * <p/>
 * Note that the test properties and server config must be set up per the
 * test-scripts/fedTestSetup.txt file. By default, the tests will be skipped.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class FederatedDataTransferOperationsImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "FederatedDataTransferOperationsImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static org.irods.jargon.testutils.AssertionHelper assertionHelper = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		irodsFileSystem = IRODSFileSystem.instance();
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

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

	/**
	 * Write to a collection on a federated zone2 from zone1 with approprate
	 * write permissions
	 *
	 * @throws Exception
	 */
	@Test
	public void testPutCollectionWithTwoFilesInAnotherZone() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String rootCollection = "testPutCollectionWithTwoFilesInAnotherZone";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneWriteTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount crossZoneAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);
		IRODSFile crossZoneColl = irodsFileSystem.getIRODSFileFactory(
				crossZoneAccount).instanceIRODSFile(
				irodsCollectionRootAbsolutePath);
		crossZoneColl.deleteWithForceOption();
		crossZoneColl.mkdirs();

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testPutCollectionWithTwoFilesInAnotherZone", 1, 1, 1,
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

		irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(crossZoneAccount);
		destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath + "/"
						+ rootCollection);

		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(
				localFile, (File) destFile);
	}

	@Test
	public void testGetCollectionWithTwoFilesInAnotherZone() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String rootCollection = "testGetCollectionWithTwoFilesInAnotherZone";
		String returnedLocalCollection = "testGetCollectionWithTwoFilesInAnotherZoneReturnedLocalFiles";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneWriteTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testGetCollectionWithTwoFilesInAnotherZone", 1, 1, 1,
						"testGetCollectionWithTwoFilesInAnotherZone", ".txt",
						2, 2, 1, 2);

		IRODSAccount crossZoneAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(crossZoneAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						crossZoneAccount);
		File localFile = new File(localCollectionAbsolutePath);
		TransferControlBlock tcb = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.USE_FORCE);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, tcb);
		destFile.close();

		destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath + "/"
						+ rootCollection);

		// now get the files into a local return collection from another zone
		// and verify
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
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
	 * Bug [#1842] [iROD-Chat:11109] imcoll symlinks across zones
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetDataObjectViaSoftLinkToAnotherZoneBug1842()
			throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String fileName = "testGetDataObjectViaSoftLinkToAnotherZoneBug1842.txt";
		String testSubdir = "testGetDataObjectViaSoftLinkToAnotherZoneBug1842";
		String mountSubdir = "testGetDataObjectViaSoftLinkToAnotherZoneBug1842SoftLink";
		String returnedLocalCollection = "testGetDataObjectViaSoftLinkToAnotherZoneBug1842Returned";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testSubdir);
		int length = 300;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, fileName, length);
		File localFile = new File(localFilePath);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);

		// delete to clean up
		destFile.deleteWithForceOption();
		destFile.mkdirs();

		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		IRODSAccount zone1Account = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		// make a symlink in zone1 to the coll in zone2
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						zone1Account);

		String softLinkCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ mountSubdir);

		IRODSFile getIrodsFile = irodsFileSystem.getIRODSFileFactory(
				zone1Account).instanceIRODSFile(softLinkCollection, fileName);

		mountedCollectionAO.unmountACollection(softLinkCollection, "");

		mountedCollectionAO.createASoftLink(targetIrodsCollection,
				softLinkCollection);

		String returnLocalCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + returnedLocalCollection);
		File returnLocalFile = new File(returnLocalCollectionAbsolutePath);

		dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						zone1Account);

		dataTransferOperationsAO.getOperation(getIrodsFile, returnLocalFile,
				null, null);

		File returnCompareLocalFile = new File(
				returnLocalCollectionAbsolutePath, fileName);

		Assert.assertTrue("got file", returnCompareLocalFile.exists());
	}

}