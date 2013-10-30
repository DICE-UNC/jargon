package org.irods.jargon.core.pub.io;

import java.io.File;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonFileOrCollAlreadyExistsException;
import org.irods.jargon.core.packinstr.DataObjInp;
import org.irods.jargon.core.packinstr.TransferOptions.ForceOption;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryImpl;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.IRODSFileSystemAOImpl;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSFileSystemAOImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IRODSFileSystemAOImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static org.irods.jargon.testutils.AssertionHelper assertionHelper = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsFileSystem = IRODSFileSystem.instance();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(
				testingProperties);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		assertionHelper = new org.irods.jargon.testutils.AssertionHelper();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public final void testIsFileReadable() throws Exception {

		// create a file and place on two resources
		String testFileName = "testIsFileReadable.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				fileNameAndPath.toString(),
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);

		boolean readable = fileSystemAO.isFileReadable(irodsFile);
		Assert.assertTrue(readable);

	}

	@Test
	public final void testIsDirReadable() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);

		boolean readable = fileSystemAO.isFileReadable(irodsFile);
		Assert.assertTrue(readable);

	}

	@Test
	public final void testIsDirWriteable() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);

		boolean writeable = fileSystemAO.isFileWriteable(irodsFile);
		Assert.assertTrue(writeable);

	}

	@Test
	public final void testIsDirWriteableWhenNot() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);

		boolean writeable = fileSystemAO.isFileWriteable(irodsFile);
		Assert.assertFalse(writeable);

	}

	@Test
	public final void testIsDirReadableWhenNot() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);

		boolean readable = fileSystemAO.isFileReadable(irodsFile);
		Assert.assertFalse(readable);

	}

	@Test
	public final void testIsFileWriteable() throws Exception {

		// create a file and place on two resources
		String testFileName = "testIsFileWriteable.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				fileNameAndPath.toString(),
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);

		boolean readable = fileSystemAO.isFileWriteable(irodsFile);
		Assert.assertTrue(readable);

	}

	@Test
	public final void testIsFileNotReadable() throws Exception {

		// create a file and place on two resources
		String testFileName = "testIsFileNotReadable.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				fileNameAndPath.toString(),
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);

		boolean readable = fileSystemAO.isFileReadable(irodsFile);
		Assert.assertFalse(readable);

	}

	@Test
	public final void testIsFileNotWriteable() throws Exception {

		// create a file and place on two resources
		String testFileName = "testIsFileNotWriteable.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				fileNameAndPath.toString(),
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);

		boolean writeable = fileSystemAO.isFileWriteable(irodsFile);
		Assert.assertFalse("file should not be writeable", writeable);

	}

	@Test
	public final void testGetFilePermissions() throws Exception {

		// create a file and place on two resources
		String testFileName = "testGetFilePermissions.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				fileNameAndPath.toString(),
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFileSystemAOImpl fileSystemAO = (IRODSFileSystemAOImpl) accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		int permissions = fileSystemAO.getFilePermissions(irodsFile);
		Assert.assertTrue("permissions not returned", permissions > 0);

	}

	@Test
	public final void testIsFileExists() throws Exception {

		// create a file and place on two resources
		String testFileName = "testIsFileExists.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				fileNameAndPath.toString(),
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);

		boolean exists = fileSystemAO.isFileExists(irodsFile);
		Assert.assertTrue(exists);

	}

	@Test
	public final void testIsFileExistsCollection() throws Exception {

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);

		boolean exists = fileSystemAO.isFileExists(irodsFile);
		Assert.assertTrue(exists);

	}

	@Test
	public final void testIsDirectoryTrue() throws Exception {

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);

		boolean isDir = fileSystemAO.isDirectory(irodsFile);
		Assert.assertTrue("this should be a directory", isDir);

	}

	@Test
	public final void testIsDirectoryFalse() throws Exception {

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		// create a file and place on two resources
		String testFileName = "testIsDirectoryFalse.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				fileNameAndPath.toString(),
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);

		boolean isDir = fileSystemAO.isDirectory(irodsFile);
		Assert.assertFalse("this should be a file, not a directory", isDir);

	}

	@Test
	public final void testIsFileNotExists() throws Exception {

		// create a file and place on two resources
		String testFileName = "testIsFileNotExists.txt";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		// now get an irods file and see if it is readable, it should be

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);

		boolean exists = fileSystemAO.isFileExists(irodsFile);
		Assert.assertFalse("file should not exist", exists);

	}

	@Test
	public final void testGetListInDir() throws Exception {
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, "");

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);
		List<String> subdirs = fileSystemAO.getListInDir(irodsFile);
		Assert.assertNotNull(subdirs);
		Assert.assertTrue("no results", subdirs.size() > 0);
	}

	@Test
	public final void testGetListInDirReplicatedFiles() throws Exception {

		// generate a local scratch file
		String testCollectionName = "testGetListInDirReplicatedFiles";
		String testFileName = "testGetListInDirReplicatedFiles.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ "/" + testCollectionName);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testCollectionName);

		String targetIrodsFile = targetIrodsCollection + '/' + testFileName;
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);

		IRODSFile irodsTargetCollectionFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		irodsTargetCollectionFile.mkdirs();

		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		// now replicate this file

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO
				.replicateIrodsDataObject(
						targetIrodsFile,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY));

		// should get a list of one file
		IRODSFile collectionFile = irodsFileFactory.instanceIRODSFile(destFile
				.getParent());
		File[] children = collectionFile.listFiles();

		Assert.assertEquals("should not display two files, one is a replica",
				1, children.length);

	}

	@Test
	public final void testGetListInDirWithFileFilter() throws Exception {
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, "");

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);
		List<File> subdirs = fileSystemAO.getListInDirWithFileFilter(irodsFile,
				new IRODSAcceptAllFileFilter());
		Assert.assertNotNull(subdirs);
		Assert.assertTrue("no results", subdirs.size() > 0);

	}

	@Test
	public final void testGetListFromPreparedSubdir() throws Exception {
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String topLevelTestDir = "getListFromPreparedSubdirTopLevel";
		String subdir1 = "subdir1";
		String subdir2 = "subdir2";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFile topDir = irodsFileFactory.instanceIRODSFile(
				targetIrodsCollection, topLevelTestDir);
		topDir.mkdirs();

		// + 2 subdirs

		IRODSFile dir = irodsFileFactory.instanceIRODSFile(
				targetIrodsCollection, topLevelTestDir + '/' + subdir1);
		dir.mkdirs();

		dir = irodsFileFactory.instanceIRODSFile(targetIrodsCollection,
				topLevelTestDir + '/' + subdir2);
		dir.mkdirs();

		// +1 file
		String testFileName = "testList.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		File localFile = new File(absPath, testFileName);

		DataTransferOperations dataTransferOperations = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		TransferControlBlock tcb = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.USE_FORCE);
		dataTransferOperations.putOperation(localFile, topDir, null, tcb);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/'
						+ topLevelTestDir);
		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);
		List<String> subdirs = fileSystemAO.getListInDir(irodsFile);
		Assert.assertNotNull(subdirs);
		Assert.assertTrue("no results", subdirs.size() == 3);
	}

	@Test
	public final void testGetListFilteredFromPreparedSubdir() throws Exception {
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String topLevelTestDir = "getListFromPreparedSubdirTopLevel";
		String subdir1 = "subdir1";
		String subdir2 = "subdir2";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);

		IRODSFile dirFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		dirFile.mkdirs();

		// + 2 subdirs

		dirFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection
				+ '/' + topLevelTestDir + '/' + subdir1);
		dirFile.mkdirs();

		dirFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection
				+ '/' + topLevelTestDir + '/' + subdir2);
		dirFile.mkdirs();

		// +1 file
		String testFileName = "testList.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ topLevelTestDir);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		TransferControlBlock tcb = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.USE_FORCE);
		dto.putOperation(
				fileNameAndPath.toString(),
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, tcb);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/'
						+ topLevelTestDir);
		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);
		List<String> subdirs = fileSystemAO.getListInDirWithFilter(irodsFile,
				new IRODSAcceptAllFileNameFilter());
		Assert.assertNotNull(subdirs);
		Assert.assertTrue("no results", subdirs.size() == 3);
	}

	@Test
	public void testFileGetDataType() throws Exception {
		String testFileName = "testGetFileDataType.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				fileNameAndPath.toString(),
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);

		ObjectType dataType = fileSystemAO.getFileDataType(irodsFile);

		Assert.assertEquals("did not get expected data type for a file",
				ObjectType.DATA_OBJECT, dataType);
	}

	@Test
	public void testFileGetDataTypeDirectory() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);

		ObjectType dataType = fileSystemAO.getFileDataType(irodsFile);

		Assert.assertEquals("did not get expected data type for a directory",
				ObjectType.COLLECTION, dataType);
	}

	@Test
	public final void testFileCreate() throws Exception {

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		// create a file and place on two resources
		String testFileName = "testFileCreateNoResc.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);
		int fileNbr = fileSystemAO.createFile(targetIrodsCollection + '/'
				+ testFileName, DataObjInp.OpenFlags.READ_WRITE,
				DataObjInp.DEFAULT_CREATE_MODE);
		Assert.assertTrue("should have received a file number for the create",
				fileNbr > 0);

	}

	@Test
	public final void testMkdir() throws Exception {

		String testDir = "testMkdir";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDir);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);

		fileSystemAO.mkdir(irodsFile, true);
		assertionHelper.assertIrodsFileOrCollectionExists(
				irodsFile.getAbsolutePath(),
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
	}

	@Test
	public final void testMkdirNonRecursiveButOk() throws Exception {

		String testDir = "testMkdirNonRecursiveButOk";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDir);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);

		fileSystemAO.mkdir(irodsFile, false);
		assertionHelper.assertIrodsFileOrCollectionExists(
				targetIrodsCollection,
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
	}

	@Test(expected = JargonException.class)
	public final void testMkdirNonRecursiveNotOk() throws Exception {

		String testDir = "testMkdir/otherstuffinbetween/andmorestuff";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDir);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);

		fileSystemAO.mkdir(irodsFile, false);
	}

	@Test
	public void testCreateFileThenClose() throws Exception {
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		// create a file and place on two resources
		String testFileName = "testCreateFileThenClose.txt";

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);

		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);
		int fileNbr = fileSystemAO.createFile(targetIrodsCollection + '/'
				+ testFileName, DataObjInp.OpenFlags.READ_WRITE,
				DataObjInp.DEFAULT_CREATE_MODE);
		Assert.assertTrue("file id not returned", fileNbr > 0);

		// fileSystemAO.fileClose(fileNbr);
		// no error = success
		irodsSession.closeSession();
	}

	@Test(expected = JargonFileOrCollAlreadyExistsException.class)
	public void testCreateFileThatAlreadyExists() throws Exception {
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		// create a file and place on two resources
		String testFileName = "testFileCreateThenClose.txt";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				fileNameAndPath.toString(),
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);
		fileSystemAO
				.createFile(targetIrodsCollection + '/' + testFileName,
						DataObjInp.OpenFlags.READ_WRITE,
						DataObjInp.DEFAULT_CREATE_MODE);
	}

	@Test
	public void testFileDeleteForce() throws Exception {
		String testFileName = "testDeleteForce.txt";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				2);
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				2);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				fileNameAndPath.toString(),
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		fileSystemAO.fileDeleteForce(irodsFile);

		// check if file exists
		assertionHelper.assertIrodsFileOrCollectionDoesNotExist(
				irodsFile.getAbsolutePath(),
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
	}

	@Test
	public void testFileDeleteNoForce() throws Exception {
		// TODO: add test of presence in trash to assertions
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String testFileName = "testFileDeleteNoForce.doc";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				2);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				fileNameAndPath.toString(),
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		fileSystemAO.fileDeleteNoForce(irodsFile);

		// check if file exists
		assertionHelper.assertIrodsFileOrCollectionDoesNotExist(
				irodsFile.getAbsolutePath(),
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
	}

	@Test
	public void testDirectoryDeleteForce() throws Exception {
		String testDirectoryName = "testDirectoryDeleteForce";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirectoryName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);

		IRODSFile dirFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		dirFile.mkdirs();

		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		fileSystemAO.directoryDeleteForce(irodsFile);

		// check if dir exists
		assertionHelper.assertIrodsFileOrCollectionDoesNotExist(
				irodsFile.getAbsolutePath(),
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
	}

	@Test
	public final void testRenameFile() throws Exception {

		String testFileName = "testRenameFile.txt";
		String testRenamedFileName = "testRenamedFile.txt";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				fileNameAndPath.toString(),
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		IRODSFile irodsRenameFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/'
						+ testRenamedFileName);
		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);

		fileSystemAO.renameFile(irodsFile, irodsRenameFile);
		assertionHelper.assertIrodsFileOrCollectionDoesNotExist(
				irodsFile.getAbsolutePath(),
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		assertionHelper.assertIrodsFileOrCollectionExists(
				irodsRenameFile.getAbsolutePath(),
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
	}

	@Test
	public final void testRenameDirectory() throws Exception {

		// create a file and place on two resources
		String testDirectory = "testRenameDirectoryColl";
		String testRenamedDirectory = "testRenamedDirectoryColl";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testDirectory);
		irodsFile.mkdirs();
		IRODSFile irodsRenameFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/'
						+ testRenamedDirectory);
		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);

		fileSystemAO.renameDirectory(irodsFile, irodsRenameFile);
		assertionHelper.assertIrodsFileOrCollectionDoesNotExist(
				irodsFile.getAbsolutePath(),
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		assertionHelper.assertIrodsFileOrCollectionExists(
				irodsRenameFile.getAbsolutePath(),
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
	}

	@Test
	public final void testPhysicalMove() throws Exception {
		String testFileName = "testPhysicalMove.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);
		
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				fileNameAndPath.toString(),
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);

		fileSystemAO
				.physicalMove(
						irodsFile,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY));

		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		DataObject dataObject = dataObjectAO.findByAbsolutePath(irodsFile
				.getAbsolutePath());
		Assert.assertEquals(
				"not moved to new resource",
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY),
				dataObject.getResourceName());

	}

}
