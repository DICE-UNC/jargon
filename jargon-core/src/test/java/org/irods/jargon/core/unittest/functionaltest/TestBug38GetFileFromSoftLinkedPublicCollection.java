package org.irods.jargon.core.unittest.functionaltest;

import java.io.File;
import java.util.Properties;

import org.junit.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.CatNoAccessException;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.MountedCollectionAO;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test Case for Bug #38 https://github.com/DICE-UNC/jargon/issues/38 Data Not
 * Found doing idrop desktop download from file in public via softlink
 *
 * @author Mike Conway - DICE
 *
 */
public class TestBug38GetFileFromSoftLinkedPublicCollection {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "TestBug38GetFileFromSoftLinkedPublicCollection";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		irodsFileSystem = IRODSFileSystem.instance();

		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testBugCase() throws Exception {

		// make a dir as test1

		String testDir = "softlinkme";
		String softLinkSubdir = "softlinkhere";
		String localRetrievedDir = "localRetrievedDir";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);
		IRODSFile irodsFile = collectionAO
				.instanceIRODSFileForCollectionPath(targetIrodsCollection + "/"
						+ testDir);
		irodsFile.mkdirs();

		// make some subfiles

		String subfile1 = "subfile1.txt";
		String subfile2 = "subfile2.txt";

		// use a local file and put it

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, subfile1, 10);
		String localFileName2 = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, subfile2, 100);

		File localFile1 = new File(localFileName);
		File localFile2 = new File(localFileName2);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		IRODSFileFactory fileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile subFile = fileFactory.instanceIRODSFile(
				irodsFile.getAbsolutePath(), subfile1);

		IRODSFile subFile2 = fileFactory.instanceIRODSFile(
				irodsFile.getAbsolutePath(), subfile2);

		// TransferControlBlock tcb =

		dataTransferOperations.putOperation(localFile1, subFile, null, null);

		subFile = fileFactory.instanceIRODSFile(irodsFile.getAbsolutePath(),
				subfile2);

		dataTransferOperations.putOperation(localFile2, subFile2, null, null);
		// now make a soft link under public

		String publicDir = "/" + irodsAccount.getZone() + "/home/public";
		IRODSFile publicFile = fileFactory.instanceIRODSFile(publicDir);

		if (!publicFile.exists()) {
			publicFile.mkdirs();
			collectionAO.setAccessPermission(irodsAccount.getZone(), publicDir,
					"public", true, FilePermissionEnum.READ);
		}

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		String mountedDir = publicDir + "/" + softLinkSubdir;

		mountedCollectionAO.unmountACollection(mountedDir,
				irodsAccount.getDefaultStorageResource());

		// create a soft link between the 'irodsFile' that is the top subdir in
		// the user directory to the public directory

		mountedCollectionAO.createASoftLink(irodsFile.getAbsolutePath(),
				mountedDir);

		// now get one of the files

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + localRetrievedDir);

		dataTransferOperations.getOperation(mountedDir,
				localCollectionAbsolutePath, "", null, null);

		File returnDirFile = new File(localCollectionAbsolutePath);
		Assert.assertTrue("local dir not created", returnDirFile.exists());

		Assert.assertFalse("no files returned",
				returnDirFile.listFiles().length == 0);

	}

	@Test
	public void testBugCaseDoGetAsAnon() throws Exception {

		// make a dir as test1

		String testDir = "testBugCaseDoGetAsAnonsoftlinkme";
		String softLinkSubdir = "testBugCaseDoGetAsAnonsoftlinkhere";
		String localRetrievedDir = "testBugCaseDoGetAsAnonlocalRetrievedDir";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccount annonAccount = testingPropertiesHelper
				.buildAnonymousIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);
		IRODSFile irodsFile = collectionAO
				.instanceIRODSFileForCollectionPath(targetIrodsCollection + "/"
						+ testDir);
		irodsFile.mkdirs();

		// make some subfiles

		String subfile1 = "subfile1.txt";
		String subfile2 = "subfile2.txt";

		// use a local file and put it

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, subfile1, 10);
		String localFileName2 = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, subfile2, 100);

		File localFile1 = new File(localFileName);
		File localFile2 = new File(localFileName2);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		IRODSFileFactory fileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile subFile = fileFactory.instanceIRODSFile(
				irodsFile.getAbsolutePath(), subfile1);

		IRODSFile subFile2 = fileFactory.instanceIRODSFile(
				irodsFile.getAbsolutePath(), subfile2);

		// TransferControlBlock tcb =

		dataTransferOperations.putOperation(localFile1, subFile, null, null);

		subFile = fileFactory.instanceIRODSFile(irodsFile.getAbsolutePath(),
				subfile2);

		dataTransferOperations.putOperation(localFile2, subFile2, null, null);
		// now make a soft link under public

		String publicDir = "/" + irodsAccount.getZone() + "/home/public";
		IRODSFile publicFile = fileFactory.instanceIRODSFile(publicDir);

		if (!publicFile.exists()) {
			publicFile.mkdirs();
			collectionAO.setAccessPermission(irodsAccount.getZone(), publicDir,
					"public", true, FilePermissionEnum.READ);
		}

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		String mountedDir = publicDir + "/" + softLinkSubdir;

		mountedCollectionAO.unmountACollection(mountedDir,
				irodsAccount.getDefaultStorageResource());

		// create a soft link between the 'irodsFile' that is the top subdir in
		// the user directory to the public directory

		mountedCollectionAO.createASoftLink(irodsFile.getAbsolutePath(),
				mountedDir);

		// now get one of the files
		dataTransferOperations = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(annonAccount);

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + localRetrievedDir);

		dataTransferOperations.getOperation(mountedDir,
				localCollectionAbsolutePath, "", null, null);

		File returnDirFile = new File(localCollectionAbsolutePath);
		Assert.assertTrue("local dir not created", returnDirFile.exists());

		Assert.assertFalse("no files returned",
				returnDirFile.listFiles().length == 0);

	}

	@Test(expected = CatNoAccessException.class)
	public void testBugCaseAsSecondaryUser() throws Exception {

		// make a dir as test1

		String testDir = "softlinkme2";
		String softLinkSubdir = "softlinkhere2";
		String localRetrievedDir = "localRetrievedDir2";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccount irodsAccount2 = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);
		IRODSFile irodsFile = collectionAO
				.instanceIRODSFileForCollectionPath(targetIrodsCollection + "/"
						+ testDir);
		irodsFile.mkdirs();

		// make some subfiles

		String subfile1 = "subfile1a.txt";
		String subfile2 = "subfile2a.txt";

		// use a local file and put it

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, subfile1, 10);
		String localFileName2 = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, subfile2, 100);

		File localFile1 = new File(localFileName);
		File localFile2 = new File(localFileName2);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		IRODSFileFactory fileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile subFile = fileFactory.instanceIRODSFile(
				irodsFile.getAbsolutePath(), subfile1);

		IRODSFile subFile2 = fileFactory.instanceIRODSFile(
				irodsFile.getAbsolutePath(), subfile2);

		// TransferControlBlock tcb =

		dataTransferOperations.putOperation(localFile1, subFile, null, null);

		subFile = fileFactory.instanceIRODSFile(irodsFile.getAbsolutePath(),
				subfile2);

		dataTransferOperations.putOperation(localFile2, subFile2, null, null);
		// now make a soft link under public

		String publicDir = "/" + irodsAccount.getZone() + "/home/public";
		IRODSFile publicFile = fileFactory.instanceIRODSFile(publicDir);

		if (!publicFile.exists()) {
			publicFile.mkdirs();
			collectionAO.setAccessPermission(irodsAccount.getZone(), publicDir,
					"public", true, FilePermissionEnum.READ);
		}

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		String mountedDir = publicDir + "/" + softLinkSubdir;

		mountedCollectionAO.unmountACollection(mountedDir,
				irodsAccount.getDefaultStorageResource());

		// create a soft link between the 'irodsFile' that is the top subdir in
		// the user directory to the public directory

		mountedCollectionAO.createASoftLink(irodsFile.getAbsolutePath(),
				mountedDir);

		// now get one of the files

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + localRetrievedDir);

		DataTransferOperations dataTransferOperations2 = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount2);

		dataTransferOperations2.getOperation(mountedDir,
				localCollectionAbsolutePath, "", null, null);

	}

	@Test
	public void testBugCaseFullObjectTypeForSoftLinkedCollectionAsAnon()
			throws Exception {

		// make a dir as test1

		String testDir = "softlinkme3";
		String softLinkSubdir = "softlinkhere3";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccount annonAccount = testingPropertiesHelper
				.buildAnonymousIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);
		IRODSFile irodsFile = collectionAO
				.instanceIRODSFileForCollectionPath(targetIrodsCollection + "/"
						+ testDir);
		irodsFile.mkdirs();

		// make some subfiles

		String subfile1 = "subfile1a.txt";
		String subfile2 = "subfile2a.txt";

		// use a local file and put it

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, subfile1, 10);
		String localFileName2 = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, subfile2, 100);

		File localFile1 = new File(localFileName);
		File localFile2 = new File(localFileName2);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		IRODSFileFactory fileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile subFile = fileFactory.instanceIRODSFile(
				irodsFile.getAbsolutePath(), subfile1);

		IRODSFile subFile2 = fileFactory.instanceIRODSFile(
				irodsFile.getAbsolutePath(), subfile2);

		dataTransferOperations.putOperation(localFile1, subFile, null, null);

		subFile = fileFactory.instanceIRODSFile(irodsFile.getAbsolutePath(),
				subfile2);

		dataTransferOperations.putOperation(localFile2, subFile2, null, null);
		// now make a soft link under public

		String publicDir = "/" + irodsAccount.getZone() + "/home/public";
		IRODSFile publicFile = fileFactory.instanceIRODSFile(publicDir);

		if (!publicFile.exists()) {
			publicFile.mkdirs();
			collectionAO.setAccessPermission(irodsAccount.getZone(), publicDir,
					"public", true, FilePermissionEnum.READ);
		}

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		String mountedDir = publicDir + "/" + softLinkSubdir;

		mountedCollectionAO.unmountACollection(mountedDir,
				irodsAccount.getDefaultStorageResource());

		// create a soft link between the 'irodsFile' that is the top subdir in
		// the user directory to the public directory

		mountedCollectionAO.createASoftLink(irodsFile.getAbsolutePath(),
				mountedDir);

		// get the full object type, should be a coll

		CollectionAndDataObjectListAndSearchAO collListAndSearch = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(annonAccount);
		Object retObj = collListAndSearch.getFullObjectForType(mountedDir);
		Assert.assertNotNull("null full object type", retObj);

	}

	@Test
	public void testBugCaseFullObjectTypeForSoftLinkedCollectionAsLoggedInUser()
			throws Exception {

		// make a dir as test1

		String testDir = "softlinkme4";
		String softLinkSubdir = "softlinkhere4";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);
		IRODSFile irodsFile = collectionAO
				.instanceIRODSFileForCollectionPath(targetIrodsCollection + "/"
						+ testDir);
		irodsFile.mkdirs();

		// make some subfiles

		String subfile1 = "subfile1a.txt";
		String subfile2 = "subfile2a.txt";

		// use a local file and put it

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, subfile1, 10);
		String localFileName2 = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, subfile2, 100);

		File localFile1 = new File(localFileName);
		File localFile2 = new File(localFileName2);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		IRODSFileFactory fileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile subFile = fileFactory.instanceIRODSFile(
				irodsFile.getAbsolutePath(), subfile1);

		IRODSFile subFile2 = fileFactory.instanceIRODSFile(
				irodsFile.getAbsolutePath(), subfile2);

		dataTransferOperations.putOperation(localFile1, subFile, null, null);

		subFile = fileFactory.instanceIRODSFile(irodsFile.getAbsolutePath(),
				subfile2);

		dataTransferOperations.putOperation(localFile2, subFile2, null, null);
		// now make a soft link under public

		String publicDir = "/" + irodsAccount.getZone() + "/home/public";
		IRODSFile publicFile = fileFactory.instanceIRODSFile(publicDir);

		if (!publicFile.exists()) {
			publicFile.mkdirs();
			collectionAO.setAccessPermission(irodsAccount.getZone(), publicDir,
					"public", true, FilePermissionEnum.READ);
		}

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		String mountedDir = publicDir + "/" + softLinkSubdir;

		mountedCollectionAO.unmountACollection(mountedDir,
				irodsAccount.getDefaultStorageResource());

		// create a soft link between the 'irodsFile' that is the top subdir in
		// the user directory to the public directory

		mountedCollectionAO.createASoftLink(irodsFile.getAbsolutePath(),
				mountedDir);

		// get the full object type, should be a coll

		CollectionAndDataObjectListAndSearchAO collListAndSearch = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(secondaryAccount);
		Object retObj = collListAndSearch.getFullObjectForType(mountedDir);
		Assert.assertNotNull("null full object type", retObj);

	}
}
