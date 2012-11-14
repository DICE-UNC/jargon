/**
 * 
 */
package org.irods.jargon.core.unittest.functionaltest;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAOImpl;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author mikeconway
 *
 */
public class FileOperationSequences {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "FileOperationSequences";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;

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

	/**
	 * Currently ignored and this test is not in the functional suite
	 * Replication sequence for 
	 * bug [#1044] Jargon allows the creating of folders that exceed the USER_PATH_EXCEEDS_MAX and cannot delete them
	 * @throws Exception
	 */
	@Ignore
	public void testLongFileNameAddAndDeleteBug1044() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		
		String collectionName = FileGenerator.generateRandomString(1068);
		
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSFile topCollection = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection + "/" + collectionName);
		topCollection.mkdirs();
		
		String testFileName = "testLongFileNameAddAndDeleteBug1044.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH  + "/" + collectionName);
		File topScratchFile = new File(absPath);
		topScratchFile.mkdirs();
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		File localFile = new File(localFileName);
	
		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory().getDataTransferOperations(irodsAccount);
		dto.putOperation(localFile, topCollection, null, null);

		IRODSFile filePutToIrods = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(topCollection.getAbsolutePath(), testFileName);
		TestCase.assertTrue("did not get exists", filePutToIrods.exists());
		
		topCollection.deleteWithForceOption();
		topCollection.reset();
		TestCase.assertFalse("collection should not exist", topCollection.exists());
		
		
	}
	
	/**
	 * Currently ignored
	 * Currently ignored and this test is not in the functional suite
	 * Replication sequence for 
	 * bug [#1044] Jargon allows the creating of folders that exceed the USER_PATH_EXCEEDS_MAX and cannot delete them
	 * @throws Exception
	 */
	@Ignore
	public void testLongFileNameAddAndDeleteBugDataObjectNameIsLong1044() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		
		String localTestFileName = "testLongFileNameAddAndDeleteBugDataObjectNameIsLong1044.txt";
		String dataObjecName = FileGenerator.generateRandomString(1068) + ".txt";
		
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH );
		File topScratchFile = new File(absPath);
		topScratchFile.mkdirs();
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, localTestFileName, 2);

		File localFile = new File(localFileName);
	
		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory().getDataTransferOperations(irodsAccount);

		IRODSFile filePutToIrods = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection, dataObjecName);
		dto.putOperation(localFile, filePutToIrods, null, null);
		TestCase.assertTrue("did not get exists", filePutToIrods.exists());
	
	}
	
	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
