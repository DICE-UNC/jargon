package org.irods.jargon.core.pub.io;

import java.io.File;
import java.util.Properties;

import org.junit.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.MountedCollectionAO;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for IRODSFile methods across zones
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class FederatedIRODSFileInputStreamTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "FederatedIRODSFileInputStreamTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(
				testingProperties);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		new org.irods.jargon.testutils.AssertionHelper();
		irodsFileSystem = IRODSFileSystem.instance();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	// FIXME: 9000 error
	public final void testReadStreamICanAccess() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		int length = 3;

		String testFileName = "testReadStreamICanAccess.txt";
		String testSubdir = "testReadStreamICanAccess";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);

		String targetIrodsPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testSubdir);

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						length);
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
		 * setup done, now connect from the first zone and try to list the coll
		 * with the data object
		 */

		IRODSAccount fedAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileInputStream irodsFileInputStream = irodsFileSystem
				.getIRODSFileFactory(fedAccount).instanceIRODSFileInputStream(
						destFile.getAbsolutePath() + "/" + testFileName);
		// read the rest
		int bytesRead = 0;

		while ((irodsFileInputStream.read()) > -1) {
			bytesRead++;
		}

		irodsFileInputStream.close();
		Assert.assertEquals("whole file not read back", length, bytesRead);

	}

	/**
	 * Bug [#1842] [iROD-Chat:11109] imcoll symlinks across zones
	 *
	 * @throws Exception
	 */
	@Test
	public void testStreamDataObjectViaSoftLinkToAnotherZoneBug1842()
			throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String fileName = "testStreamDataObjectViaSoftLinkToAnotherZoneBug1842.txt";
		String testSubdir = "testStreamDataObjectViaSoftLinkToAnotherZoneBug1842";
		String mountSubdir = "testStreamDataObjectViaSoftLinkToAnotherZoneBug1842SoftLink";

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

		mountedCollectionAO.unmountACollection(softLinkCollection, "");

		mountedCollectionAO.createASoftLink(targetIrodsCollection,
				softLinkCollection);

		IRODSFileInputStream irodsFileInputStream = irodsFileSystem
				.getIRODSFileFactory(zone1Account)
				.instanceIRODSFileInputStream(
						softLinkCollection + "/" + fileName);
		// read the rest
		int bytesRead = 0;

		while ((irodsFileInputStream.read()) > -1) {
			bytesRead++;
		}

		irodsFileInputStream.close();
		Assert.assertEquals("whole file not read back", length, bytesRead);

	}

}
