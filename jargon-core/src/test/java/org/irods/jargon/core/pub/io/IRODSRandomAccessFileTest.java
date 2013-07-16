/**
 * 
 */
package org.irods.jargon.core.pub.io;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Arrays;
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
 * FIXME: implement tests, mirror in 2.3.1, and add to suite
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSRandomAccessFileTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IRODSRandomAccessFileTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;

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
	 * Bug [#1557] Griffin log shows error -1220000 raised from Jargon for ftp
	 * write
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testCreateAndCloseNoDefResc() throws Exception {
		// generate a local scratch file
		String testFileName = "testCreateAndCloseNoDefResc.txt";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		irodsAccount.setDefaultStorageResource("");

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSRandomAccessFile irodsRandomAccessFile = irodsFileFactory
				.instanceIRODSRandomAccessFile(targetIrodsCollection + "/"
						+ testFileName);
		irodsRandomAccessFile.close();
		irodsFileSystem.closeAndEatExceptions();

	}

	@Test
	public final void testRead() throws Exception {
		// generate a local scratch file
		String testFileName = "testRead.txt";
		int fileLengthInKb = 2;
		long fileLengthInBytes = fileLengthInKb * 1024;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String inputFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						fileLengthInBytes);

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

		// read back the test file so I can compare

		// here I'm saving the source file as a byte array as my 'expected'
		// value for my test assertion
		BufferedInputStream fis = new BufferedInputStream(new FileInputStream(
				inputFileName));
		byte[] inputBytes = new byte[1024];
		fis.read(inputBytes);
		fis.close();

		// now try to do the seek
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSRandomAccessFile randomAccessFile = irodsFileFactory
				.instanceIRODSRandomAccessFile(targetIrodsCollection + '/'
						+ testFileName);

		char readData = (char) randomAccessFile.read();
		char expectedReadData = (char) inputBytes[0];

		Assert.assertEquals(
				"byte I read does not match the first byte I wrote",
				expectedReadData, readData);

	}

	@Test
	public final void testSeekLongInt() throws Exception {
		// generate a local scratch file
		String testFileName = "testfileseek.txt";
		int fileLengthInKb = 2;
		long fileLengthInBytes = fileLengthInKb * 1024;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String inputFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						fileLengthInBytes);

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

		// here I'm saving the source file as a byte array as my 'expected'
		// value for my test assertion
		BufferedInputStream fis = new BufferedInputStream(new FileInputStream(
				inputFileName));
		byte[] inputBytes = new byte[1024];
		fis.read(inputBytes);
		fis.close();

		// now try to do the seek

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSRandomAccessFile randomAccessFile = irodsFileFactory
				.instanceIRODSRandomAccessFile(targetIrodsCollection + '/'
						+ testFileName);

		randomAccessFile.seek(200L, FileIOOperations.SeekWhenceType.SEEK_START);
		byte[] bytesToRead = new byte[20];
		randomAccessFile.read(bytesToRead);
		byte[] expectedBytes = new byte[20];
		System.arraycopy(inputBytes, 200, expectedBytes, 0, 20);
		Assert.assertTrue(
				"did not seek and read the same data that I originally wrote",
				Arrays.equals(expectedBytes, bytesToRead));

	}

	/**
	 * Bug 45 - SYS_UNMATCHED_API_NUM (-12000) when attempting to get a file
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUnmatchedAPIWhenReadingRAFile() throws Exception {

		String testFileName = "testfileForApi.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				1);

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

		IRODSRandomAccessFile randomAccessFile = irodsFileFactory
				.instanceIRODSRandomAccessFile(targetIrodsCollection + '/'
						+ testFileName);

		int nbytes = 0;
		int offset = 0;
		byte data[] = new byte[4096];
		boolean dataRead = false;

		while ((nbytes = randomAccessFile.read(data, offset, 4096)) > 0) {
			offset += nbytes;
			dataRead = true;
		}
		randomAccessFile.close();
		Assert.assertTrue("did not read back any data", dataRead);
	}

}
