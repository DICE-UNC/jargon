/**
 *
 */
package org.irods.jargon.core.pub.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Assert;

import org.irods.jargon.core.connection.ConnectionProgressStatusListener;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.DefaultIntraFileProgressCallbackListener;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.transfer.DefaultTransferControlBlock;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus.TransferType;
import org.irods.jargon.core.transfer.TransferStatusCallbackListenerTestingImplementation;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class RODSFIleInputStreamForSoftLinksTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IrodsFileInputStreamTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
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
	 * {@link org.irods.jargon.core.pub.io.IRODSFileInputStream#read()}.
	 */
	@Test
	public final void testRead() throws Exception {
		String testFileName = "testread.txt";
		int fileLength = 40;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		org.irods.jargon.testutils.filemanip.FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						fileLength);

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
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(
				targetIrodsCollection, testFileName);
		IRODSFileInputStream fis = irodsFileFactory
				.instanceIRODSFileInputStream(irodsFile);

		ByteArrayOutputStream actualFileContents = new ByteArrayOutputStream();

		// read the rest
		int bytesRead = 0;

		int readBytes;
		while ((readBytes = fis.read()) > -1) {
			actualFileContents.write(readBytes);
			bytesRead++;
		}

		Assert.assertEquals("whole file not read back", fileLength, bytesRead);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileInputStream#read(byte[], int, int)}
	 * .
	 */
	@Test
	public final void testInputStreamWithRerouting() throws Exception {

		if (!testingPropertiesHelper
				.isTestDistributedResources(testingProperties)) {
			return;
		}

		String testFileName = "testInputStreamWithRerouting.txt";
		int fileLength = 100;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = org.irods.jargon.testutils.filemanip.FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						fileLength);
		File localFile = new File(localFilePath);

		// put scratch file into irods in the right place

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(
				targetIrodsCollection, testFileName);
		irodsFile
				.setResource(testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_TERTIARY_RESOURCE_KEY));

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFile, irodsFile, null, null);

		ByteArrayOutputStream actualFileContents = new ByteArrayOutputStream();

		IRODSFileInputStream fis = irodsFileFactory
				.instanceIRODSFileInputStreamWithRerouting(irodsFile
						.getAbsolutePath());

		byte[] readBytesBuffer = new byte[512];
		while (((fis.read(readBytesBuffer, 0, readBytesBuffer.length))) > -1) {
			actualFileContents.write(readBytesBuffer);
		}

		fis.close();
		Assert.assertTrue("did not get instance of session closing stream",
				fis instanceof SessionClosingIRODSFileInputStream);
		Assert.assertNull("session from reroute leaking",
				irodsFileSystem.getConnectionMap());

	}

	@Test
	public final void testInputStreamWithReroutingNoRerouteNeeded()
			throws Exception {

		String testFileName = "testInputStreamWithReroutingNoRerouteNeeded.txt";
		int fileLength = 100;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = org.irods.jargon.testutils.filemanip.FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						fileLength);
		File localFile = new File(localFilePath);

		// put scratch file into irods in the right place

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(
				targetIrodsCollection, testFileName);

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFile, irodsFile, null, null);

		ByteArrayOutputStream actualFileContents = new ByteArrayOutputStream();

		IRODSFileInputStream fis = irodsFileFactory
				.instanceIRODSFileInputStreamWithRerouting(irodsFile
						.getAbsolutePath());

		byte[] readBytesBuffer = new byte[512];
		while (((fis.read(readBytesBuffer, 0, readBytesBuffer.length))) > -1) {
			actualFileContents.write(readBytesBuffer);
		}

		fis.close();
		Assert.assertFalse("did not get instance of session closing stream",
				fis instanceof SessionClosingIRODSFileInputStream);
		Assert.assertNull("session from reroute leaking",
				irodsFileSystem.getConnectionMap());

	}

	/**
	 * Test method for {@link java.io.FileInputStream#read(byte[])}.
	 */
	@Test
	public final void testReadByteArray() throws Exception {
		String testFileName = "testReadByteArray.txt";
		int fileLength = 230;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		org.irods.jargon.testutils.filemanip.FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						fileLength);

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
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(
				targetIrodsCollection, testFileName);
		IRODSFileInputStream fis = irodsFileFactory
				.instanceIRODSFileInputStream(irodsFile);

		ByteArrayOutputStream actualFileContents = new ByteArrayOutputStream();

		int bytesRead = 0;
		int readLength = 0;
		byte[] readBytesBuffer = new byte[1024];
		while ((readLength = (fis.read(readBytesBuffer))) > -1) {
			actualFileContents.write(readBytesBuffer);
			bytesRead += readLength;
		}

		Assert.assertEquals("whole file not read back", fileLength, bytesRead);
	}

	@Test
	public final void testReadWithByteCountingWrapper() throws Exception {
		String testFileName = "testReadWithByteCountingWrapper.txt";
		int fileLength = 23000000;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String absPathToFile = org.irods.jargon.testutils.filemanip.FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						fileLength);

		File sourceFile = new File(absPathToFile);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(
				targetIrodsCollection, testFileName);

		DataTransferOperations transferOperations = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		transferOperations.putOperation(sourceFile, irodsFile, null, null);

		IRODSFileInputStream fis = irodsFileFactory
				.instanceIRODSFileInputStream(irodsFile);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();
		ConnectionProgressStatusListener connectionProgressStatusListener = DefaultIntraFileProgressCallbackListener
				.instance(TransferType.GET, fileLength, transferControlBlock,
						transferStatusCallbackListener);
		InputStream wrapper = new ByteCountingCallbackInputStreamWrapper(
				connectionProgressStatusListener, fis);

		int bytesRead = 0;
		int readLength = 0;
		byte[] readBytesBuffer = new byte[2048];
		while ((readLength = (wrapper.read(readBytesBuffer))) > -1) {
			bytesRead += readLength;
		}

		wrapper.close();
		Assert.assertEquals("whole file not read back", fileLength, bytesRead);
		Assert.assertTrue("did not get callbacks",
				transferStatusCallbackListener.getIntraFileCallbackCtr() > 0);

	}

	/**
	 * Test method for {@link java.io.FileInputStream#skip(long)}.
	 */
	@Test
	public final void testSkip() throws Exception {
		// generate a local scratch file
		String testFileName = "testfileskip.txt";
		int fileLengthInKb = 2;
		long fileLengthInBytes = fileLengthInKb * 1024;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
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

		ByteArrayOutputStream actualFileContents = new ByteArrayOutputStream();

		// now try to do the seek

		// now try to do the read

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(
				targetIrodsCollection, testFileName);
		IRODSFileInputStream fis = irodsFileFactory
				.instanceIRODSFileInputStream(irodsFile);

		long skipped = fis.skip(1024L);

		// may have skipped a different value?

		long numberBytesReadAfterSkip = 0L;

		// read the rest

		int readBytes;
		byte[] readBytesBuffer = new byte[512];
		while ((readBytes = (fis.read(readBytesBuffer, 0,
				readBytesBuffer.length))) > -1) {
			actualFileContents.write(readBytes);
			numberBytesReadAfterSkip += readBytes;
		}

		Assert.assertEquals(
				"I did not skip and then read the remainder of the specified file",
				fileLengthInBytes, skipped + numberBytesReadAfterSkip);
	}

	/**
	 * Test method for {@link java.io.FileInputStream#close()}.
	 */
	@Test
	public final void testClose() throws Exception {
		String testFileName = "testClose.txt";
		int fileLength = 1;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		org.irods.jargon.testutils.filemanip.FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						fileLength);

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
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(
				targetIrodsCollection, testFileName);
		IRODSFileInputStream fis = irodsFileFactory
				.instanceIRODSFileInputStream(irodsFile);

		fis.close();

		// no error equals success
		Assert.assertTrue(true);
	}
}
