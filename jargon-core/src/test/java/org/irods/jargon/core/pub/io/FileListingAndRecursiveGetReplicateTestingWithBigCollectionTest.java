package org.irods.jargon.core.pub.io;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileListingAndRecursiveGetReplicateTestingWithBigCollectionTest {
	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "FileListingAndRecursiveGetReplicateTestingWithBigCollectionTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	public static final String COLL_DIR = "coll";
	private static final int NUMBER_OF_TEST_FILES = 10000;
	public static final String SUBDIR_NAME = "subDir";
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();

		// put in the thousand files
		String testFilePrefix = "FileListingAndRecursiveGetReplicateTestingWithBigCollectionTest";
		String testFileSuffix = ".txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		FileGenerator.generateManyFilesInGivenDirectory(IRODS_TEST_SUBDIR_PATH
				+ '/' + COLL_DIR, testFilePrefix, testFileSuffix,
				NUMBER_OF_TEST_FILES, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		// make the put subdir
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);

		IRODSFile dirFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		dirFile.mkdirs();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				absPath + COLL_DIR,
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		IRODSFile collectionRoot = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + "/" + COLL_DIR);

		// make a large number of collections too
		for (int i = 0; i < NUMBER_OF_TEST_FILES; i++) {
			IRODSFile newDir = irodsFileFactory
					.instanceIRODSFile(collectionRoot.getAbsolutePath() + "/"
							+ SUBDIR_NAME + i);
			newDir.mkdir();
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFileList() throws Exception {

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ COLL_DIR);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile collectionRoot = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		String[] names = collectionRoot.list();
		Assert.assertNotNull("no names returned", names);
		Assert.assertEquals("did not get all files", NUMBER_OF_TEST_FILES * 2,
				names.length);

	}

	@Test
	public void testFileListFiles() throws Exception {

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ COLL_DIR);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile collectionRoot = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		File[] files = collectionRoot.listFiles();
		Assert.assertNotNull("no files returned", files);
		Assert.assertEquals("did not get all files", NUMBER_OF_TEST_FILES * 2,
				files.length);

	}

	@Test
	public void testFileListWithFilter() throws Exception {

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ COLL_DIR);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile collectionRoot = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		String[] names = collectionRoot
				.list(new IRODSAcceptAllFileNameFilter());
		Assert.assertNotNull("no names returned", names);
		Assert.assertEquals("did not get all files", NUMBER_OF_TEST_FILES * 2,
				names.length);

	}

	@Test
	public void testFileListFilesWithFilter() throws Exception {

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ COLL_DIR);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile collectionRoot = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		File[] files = collectionRoot.listFiles(new IRODSAcceptAllFileFilter());
		Assert.assertNotNull("no files returned", files);
		Assert.assertEquals("did not get all files", NUMBER_OF_TEST_FILES * 2,
				files.length);

	}

	@Test
	public void testFileListFilesWithFileNameFilter() throws Exception {

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ COLL_DIR);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile collectionRoot = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		File[] files = collectionRoot
				.listFiles(new IRODSAcceptAllFileNameFilter());
		Assert.assertNotNull("no files returned", files);
		Assert.assertEquals("did not get all files", NUMBER_OF_TEST_FILES * 2,
				files.length);

	}

}
