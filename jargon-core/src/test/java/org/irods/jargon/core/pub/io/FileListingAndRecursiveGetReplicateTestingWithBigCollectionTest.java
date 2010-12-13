package org.irods.jargon.core.pub.io;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.irods.jargon.testutils.icommandinvoke.IcommandInvoker;
import org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext;
import org.irods.jargon.testutils.icommandinvoke.icommands.ImkdirCommand;
import org.irods.jargon.testutils.icommandinvoke.icommands.IputCommand;
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

		// put in the thousand files
		String testFilePrefix = "FileListingAndRecursiveGetReplicateTestingWithBigCollectionTest";
		String testFileSuffix = ".txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		FileGenerator.generateManyFilesInGivenDirectory(IRODS_TEST_SUBDIR_PATH
				+ '/' + COLL_DIR, testFilePrefix, testFileSuffix,
				NUMBER_OF_TEST_FILES, 1, 2);

		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);

		// make the put subdir
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		ImkdirCommand iMkdirCommand = new ImkdirCommand();
		iMkdirCommand.setCollectionName(targetIrodsCollection);
		invoker.invokeCommandAndGetResultAsString(iMkdirCommand);

		// put the files by putting the collection
		IputCommand iputCommand = new IputCommand();
		iputCommand.setForceOverride(true);
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setLocalFileName(absPath + COLL_DIR);
		iputCommand.setRecursive(true);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
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
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile collectionRoot = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		String[] names = collectionRoot.list();
		irodsFileSystem.close();
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
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile collectionRoot = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		File[] files = collectionRoot.listFiles();
		irodsFileSystem.close();
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
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile collectionRoot = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		String[] names = collectionRoot.list(new IRODSAcceptAllFileNameFilter());
		irodsFileSystem.close();
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
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile collectionRoot = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		File[] files = collectionRoot.listFiles(new IRODSAcceptAllFileFilter());
		irodsFileSystem.close();
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
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile collectionRoot = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		File[] files = collectionRoot.listFiles(new IRODSAcceptAllFileNameFilter());
		irodsFileSystem.close();
		Assert.assertNotNull("no files returned", files);
		Assert.assertEquals("did not get all files", NUMBER_OF_TEST_FILES * 2,
				files.length);

	}
	
	

}
