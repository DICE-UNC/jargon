package org.irods.jargon.core.pub.io;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
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
public class FederatedIRODSFileImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "FedratedIRODSFileTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static org.irods.jargon.testutils.AssertionHelper assertionHelper = null;
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
		assertionHelper = new org.irods.jargon.testutils.AssertionHelper();
		irodsFileSystem = IRODSFileSystem.instance();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public final void testExistsAcrossZone() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String testFileName = "testExistsAcrossZone.txt";
		String testSubdir = "testExistsAcrossZone";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);

		String targetIrodsPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testSubdir);

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 3);
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
		IRODSFile target = irodsFileSystem.getIRODSFileFactory(fedAccount)
				.instanceIRODSFile(destFile.getAbsolutePath());
		Assert.assertTrue(target.exists());
	}

	/**
	 * Exists on a cross zone file that does not exist
	 *
	 * @throws Exception
	 */
	@Test
	public final void testExistsAcrossZoneNotExists() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String testFileName = "testExistsAcrossZoneNotExists.txt";
		String testSubdir = "testExistsAcrossZoneNotExists";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);

		String targetIrodsPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testSubdir);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsPath);

		// delete to clean up
		destFile.deleteWithForceOption();
		destFile.mkdirs();

		/*
		 * setup done, now connect from the first zone and try to list the coll
		 * with the data object
		 */

		IRODSAccount fedAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile target = irodsFileSystem.getIRODSFileFactory(fedAccount)
				.instanceIRODSFile(destFile.getAbsolutePath(), testFileName);
		Assert.assertFalse("file should not exist", target.exists());
	}

	@Test
	public final void testIsDirAcrossZone() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String testSubdir = "testIsDirAcrossZone";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);

		String targetIrodsPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testSubdir);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsPath);

		// delete to clean up
		destFile.deleteWithForceOption();
		destFile.mkdirs();

		/*
		 * setup done, now connect from the first zone and try to list the coll
		 * with the data object
		 */

		IRODSAccount fedAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile target = irodsFileSystem.getIRODSFileFactory(fedAccount)
				.instanceIRODSFile(destFile.getAbsolutePath());
		Assert.assertTrue("should be a dir", target.isDirectory());
	}

	@Test
	public final void testIsFileAcrossZone() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String testFileName = "testIsFileAcrossZone.txt";
		String testSubdir = "testIsFileAcrossZone";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);

		String targetIrodsPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testSubdir);

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 3);
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
		IRODSFile target = irodsFileSystem.getIRODSFileFactory(fedAccount)
				.instanceIRODSFile(destFile.getAbsolutePath(), testFileName);
		Assert.assertTrue("should be a file", target.isFile());
	}

	@Test
	public final void testIsReadFileAcrossZone() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String testFileName = "testIsReadFileAcrossZone.txt";
		String testSubdir = "testIsReadFileAcrossZone";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);

		String targetIrodsPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testSubdir);

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 3);
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
		IRODSFile target = irodsFileSystem.getIRODSFileFactory(fedAccount)
				.instanceIRODSFile(destFile.getAbsolutePath(), testFileName);
		Assert.assertTrue("should be a readable file", target.canRead());
	}

	@Test
	public final void testGetLastModAcrossZone() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String testFileName = "testGetLastModAcrossZone.txt";
		String testSubdir = "testGetLastModAcrossZone";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);

		String targetIrodsPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testSubdir);

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 3);
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
		IRODSFile target = irodsFileSystem.getIRODSFileFactory(fedAccount)
				.instanceIRODSFile(destFile.getAbsolutePath(), testFileName);
		Assert.assertTrue("should have last mod", target.lastModified() > 0);
	}

	@Test
	public final void testGetLastModNotExistsAcrossZone() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String testFileName = "testGetLastModNotExistsAcrossZone.txt";
		String testSubdir = "testGetLastModNotExistsAcrossZone";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);

		String targetIrodsPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testSubdir);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsPath);

		// delete to clean up
		destFile.deleteWithForceOption();
		destFile.mkdirs();

		/*
		 * setup done, now connect from the first zone and try to list the coll
		 * with the data object
		 */

		IRODSAccount fedAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile target = irodsFileSystem.getIRODSFileFactory(fedAccount)
				.instanceIRODSFile(destFile.getAbsolutePath(), testFileName);
		Assert.assertTrue("should not have last mod",
				target.lastModified() == 0);
	}

	@Test
	public final void testIsFileWhenNotExistsAcrossZone() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String testFileName = "testIsFileWhenNotExistsAcrossZone.txt";
		String testSubdir = "testIsFileWhenNotExistsAcrossZone";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);

		String targetIrodsPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testSubdir);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsPath);

		// delete to clean up
		destFile.deleteWithForceOption();
		destFile.mkdirs();

		/*
		 * setup done, now connect from the first zone and try to list the coll
		 * with the data object
		 */

		IRODSAccount fedAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile target = irodsFileSystem.getIRODSFileFactory(fedAccount)
				.instanceIRODSFile(destFile.getAbsolutePath(), testFileName);
		Assert.assertFalse("should not be a file, does not exist",
				target.isFile());
	}

	/**
	 * Make subdirectories in another zone for which I have write access
	 *
	 * @throws Exception
	 */
	@Test
	public final void testMkdirsInAnotherZone() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String testDir = "testMkdirsInAnotherZone/andanother";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneWriteTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDir);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		boolean success = irodsFile.mkdirs();

		Assert.assertTrue("did not get success in the mkdirs command", success);
		assertionHelper.assertIrodsFileOrCollectionExists(
				irodsFile.getAbsolutePath(), accessObjectFactory, irodsAccount);
	}

}
