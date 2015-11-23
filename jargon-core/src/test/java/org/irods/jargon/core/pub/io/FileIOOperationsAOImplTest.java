/**
 *
 */
package org.irods.jargon.core.pub.io;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
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
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class FileIOOperationsAOImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "FileIOOperationsAOImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static org.irods.jargon.testutils.AssertionHelper assertionHelper = null;
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
		assertionHelper = new org.irods.jargon.testutils.AssertionHelper();
		irodsFileSystem = IRODSFileSystem.instance();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.FileIOOperationsAOImpl#write(int, byte[], int, int)}
	 * .
	 */

	@Test
	public final void testWrite() throws Exception {
		String testFileName = "testFileWriteByteArrayx.csv";

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
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		boolean created = irodsFile.createNewFile();
		Assert.assertTrue("file was not created, cannot proceed", created);
		Assert.assertTrue("file I created is not a file", irodsFile.isFile());
		Assert.assertTrue("i cannot write an output stream",
				irodsFile.canWrite());

		FileIOOperations fileIOOperationsAO = new FileIOOperationsAOImpl(
				irodsFileSystem.getIrodsSession(), irodsAccount);
		// get a simple byte array
		String myBytes = "ajjjjjjjjjjjjjjjjjjjjjjjjfeiiiiiiiiiiiiiii54454545";
		byte[] myBytesArray = myBytes.getBytes();

		irodsFile.open();

		fileIOOperationsAO.write(irodsFile.getFileDescriptor(), myBytesArray,
				0, myBytesArray.length);

		irodsFile.close();
		irodsFile.open();
		long length = irodsFile.length();
		irodsFile.close();
		assertionHelper.assertIrodsFileOrCollectionExists(
				irodsFile.getAbsolutePath(), accessObjectFactory, irodsAccount);
		Assert.assertEquals("length of file does not match data written",
				myBytesArray.length, length);

	}

	@Test
	public final void testSeek() throws Exception {
		// generate a local scratch file
		String testFileName = "testfileseek.txt";

		long fileLengthInBytes = 3072;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String inputFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						fileLengthInBytes);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

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
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		irodsFile.open();
		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(
				irodsFileSystem.getIrodsSession(), irodsAccount);

		long seekVal = fileIOOperations.seek(irodsFile.getFileDescriptor(),
				200L, FileIOOperations.SeekWhenceType.SEEK_START);
		Assert.assertEquals("did not move file pointer", 200L, seekVal);
		irodsFile.close();

	}

}
