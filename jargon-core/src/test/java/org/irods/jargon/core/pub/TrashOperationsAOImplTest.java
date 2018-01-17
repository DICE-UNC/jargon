package org.irods.jargon.core.pub;

import java.io.File;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.Assert;

public class TrashOperationsAOImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "TrashOperationsAOImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(testingProperties);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
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
	public void testEmptyTrashForLoggedInUserWithAge() throws Exception {
		String testFileName = "testEmptyTrashForLoggedInUserWithAge.txt";
		String testCollectionName = "testEmptyTrashForLoggedInUserWithAge";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromSecondaryTestProperties(testingProperties,
						IRODS_TEST_SUBDIR_PATH + "/" + testCollectionName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		irodsAccount.setDefaultStorageResource("");

		TrashOperationsAO trashOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getTrashOperationsAO(irodsAccount);
		trashOperationsAO.emptyTrashForLoggedInUser("", 0);

		File sourceFile = new File(absPath + testFileName);
		IRODSFile targetIRODSColl = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);

		targetIRODSColl.deleteWithForceOption();
		targetIRODSColl.mkdirs();

		DataTransferOperations dataTransferOperations = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dataTransferOperations.putOperation(sourceFile, targetIRODSColl, null, null);

		targetIRODSColl.delete();
		// I made some trash

		trashOperationsAO.emptyTrashForLoggedInUser("", 15);
		IRODSFile trashHome = trashOperationsAO.getTrashHomeForLoggedInUser();
		Assert.assertFalse("trash should not be empty because of age", trashHome.listFiles().length == 0);

	}

	@Test
	public void testEmptyTrashForLoggedInUser() throws Exception {
		String testFileName = "testEmptyTrashForLoggedInUser.txt";
		String testCollectionName = "testEmptyTrashForLoggedInUser";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromSecondaryTestProperties(testingProperties,
						IRODS_TEST_SUBDIR_PATH + "/" + testCollectionName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		irodsAccount.setDefaultStorageResource("");

		File sourceFile = new File(absPath + testFileName);
		IRODSFile targetIRODSColl = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);

		targetIRODSColl.deleteWithForceOption();
		targetIRODSColl.mkdirs();

		DataTransferOperations dataTransferOperations = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dataTransferOperations.putOperation(sourceFile, targetIRODSColl, null, null);
		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);

		targetIRODSColl.delete();
		// I made some trash

		TrashOperationsAO trashOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getTrashOperationsAO(irodsAccount);
		trashOperationsAO.emptyTrashForLoggedInUser("", 0);
		IRODSFile trashHome = trashOperationsAO.getTrashHomeForLoggedInUser();
		Assert.assertTrue("trash not empty", trashHome.listFiles().length == 0);

	}

	@Test
	public void testEmptyAllTrashForUserAsAdmin() throws Exception {
		String testFileName = "testEmptyAllTrashForUserAsAdmin.txt";
		String testCollectionName = "testEmptyAllTrashForUserAsAdmin";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromSecondaryTestProperties(testingProperties,
						IRODS_TEST_SUBDIR_PATH + "/" + testCollectionName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		irodsAccount.setDefaultStorageResource("");

		File sourceFile = new File(absPath + testFileName);
		IRODSFile targetIRODSColl = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);

		targetIRODSColl.deleteWithForceOption();
		targetIRODSColl.mkdirs();

		DataTransferOperations dataTransferOperations = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dataTransferOperations.putOperation(sourceFile, targetIRODSColl, null, null);
		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);

		targetIRODSColl.delete();
		// I made some trash

		IRODSAccount rods = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);

		TrashOperationsAO trashOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory().getTrashOperationsAO(rods);
		trashOperationsAO.emptyAllTrashAsAdmin("", -1);

		TrashOperationsAO userTrashOps = irodsFileSystem.getIRODSAccessObjectFactory()
				.getTrashOperationsAO(irodsAccount);
		IRODSFile trashHome = userTrashOps.getTrashHomeForLoggedInUser();
		Assert.assertTrue("trash not empty", trashHome.listFiles().length == 0);

	}

	@Test
	public void testEmptyTrashForUserAsAdmin() throws Exception {
		String testFileName = "testEmptyTrashForUserAsAdmin.txt";
		String testCollectionName = "testEmptyTrashForUserAsAdmin";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromSecondaryTestProperties(testingProperties,
						IRODS_TEST_SUBDIR_PATH + "/" + testCollectionName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		irodsAccount.setDefaultStorageResource("");

		File sourceFile = new File(absPath + testFileName);
		IRODSFile targetIRODSColl = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);

		targetIRODSColl.deleteWithForceOption();
		targetIRODSColl.mkdirs();

		DataTransferOperations dataTransferOperations = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dataTransferOperations.putOperation(sourceFile, targetIRODSColl, null, null);
		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);

		targetIRODSColl.delete();
		// I made some trash

		IRODSAccount rods = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);

		TrashOperationsAO trashOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory().getTrashOperationsAO(rods);
		trashOperationsAO.emptyTrashAdminMode(irodsAccount.getUserName(), "", 0);

		TrashOperationsAO userTrashOps = irodsFileSystem.getIRODSAccessObjectFactory()
				.getTrashOperationsAO(irodsAccount);
		IRODSFile trashHome = userTrashOps.getTrashHomeForLoggedInUser();
		Assert.assertTrue("trash not empty", trashHome.listFiles().length == 0);

	}

}
