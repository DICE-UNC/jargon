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
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.icommandinvoke.IcommandInvoker;
import org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext;
import org.irods.jargon.testutils.icommandinvoke.icommands.IputCommand;
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
	private static org.irods.jargon.testutils.AssertionHelper assertionHelper = null;

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
	}

	@Test
	public final void testRead() throws Exception {
		// generate a local scratch file
		String testFileName = "testfileseek.txt";
		int fileLengthInKb = 2;
		long fileLengthInBytes = fileLengthInKb * 1024;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String inputFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						fileLengthInBytes);

		// put scratch file into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		iputCommand.setLocalFileName(fileNameAndPath.toString());
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		// read back the test file so I can compare

		// here I'm saving the source file as a byte array as my 'expected'
		// value for my test assertion
		BufferedInputStream fis = new BufferedInputStream(new FileInputStream(
				inputFileName));
		byte[] inputBytes = new byte[1024];
		fis.read(inputBytes);
		fis.close();

		// now try to do the seek

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);

		IRODSFileFactory irodsFileFactory = new IRODSFileFactoryImpl(
				irodsSession, irodsAccount);

		IRODSRandomAccessFile randomAccessFile = irodsFileFactory
				.instanceIRODSRandomAccessFile(targetIrodsCollection + '/'
						+ testFileName);

		char readData = (char) randomAccessFile.read();
		char expectedReadData = (char) inputBytes[0];

		irodsSession.closeSession();
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

		// put scratch file into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		iputCommand.setLocalFileName(fileNameAndPath.toString());
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		// here I'm saving the source file as a byte array as my 'expected'
		// value for my test assertion
		BufferedInputStream fis = new BufferedInputStream(new FileInputStream(
				inputFileName));
		byte[] inputBytes = new byte[1024];
		fis.read(inputBytes);
		fis.close();

		// now try to do the seek

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);

		IRODSFileFactory irodsFileFactory = new IRODSFileFactoryImpl(
				irodsSession, irodsAccount);

		IRODSRandomAccessFile randomAccessFile = irodsFileFactory
				.instanceIRODSRandomAccessFile(targetIrodsCollection + '/'
						+ testFileName);

		randomAccessFile.seek(200L, FileIOOperations.SeekWhenceType.SEEK_START);
		byte[] bytesToRead = new byte[20];
		randomAccessFile.read(bytesToRead);
		byte[] expectedBytes = new byte[20];
		System.arraycopy(inputBytes, 200, expectedBytes, 0, 20);
		irodsSession.closeSession();
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
		// put scratch file into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		iputCommand.setLocalFileName(fileNameAndPath.toString());
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);

		IRODSFileFactory irodsFileFactory = new IRODSFileFactoryImpl(
				irodsSession, irodsAccount);

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

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#readBoolean()}.
	 */
	@Test
	public void testReadBoolean() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#readByte()}.
	 */
	@Test
	public void testReadByte() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#readChar()}.
	 */
	@Test
	public void testReadChar() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#readDouble()}.
	 */
	@Test
	public void testReadDouble() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#readFloat()}.
	 */
	@Test
	public void testReadFloat() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#readFully(byte[])}
	 * .
	 */
	@Test
	public void testReadFullyByteArray() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#readFully(byte[], int, int)}
	 * .
	 */
	@Test
	public void testReadFullyByteArrayIntInt() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#readInt()}.
	 */
	@Test
	public void testReadInt() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#readLine()}.
	 */
	@Test
	public void testReadLine() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#readLong()}.
	 */
	@Test
	public void testReadLong() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#readShort()}.
	 */
	@Test
	public void testReadShort() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#readUTF()}.
	 */
	@Test
	public void testReadUTF() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#readUnsignedByte()}
	 * .
	 */
	@Test
	public void testReadUnsignedByte() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#readUnsignedShort()}
	 * .
	 */
	@Test
	public void testReadUnsignedShort() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#skipBytes(int)}
	 * .
	 */
	@Test
	public void testSkipBytes() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#write(int)}.
	 */
	@Test
	public void testWriteInt() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#write(byte[])}.
	 */
	@Test
	public void testWriteByteArray() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#write(byte[], int, int)}
	 * .
	 */
	@Test
	public void testWriteByteArrayIntInt() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#writeBoolean(boolean)}
	 * .
	 */
	@Test
	public void testWriteBoolean() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#writeByte(int)}
	 * .
	 */
	@Test
	public void testWriteByte() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#writeBytes(java.lang.String)}
	 * .
	 */
	@Test
	public void testWriteBytes() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#writeChar(int)}
	 * .
	 */
	@Test
	public void testWriteChar() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#writeChars(java.lang.String)}
	 * .
	 */
	@Test
	public void testWriteChars() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#writeDouble(double)}
	 * .
	 */
	@Test
	public void testWriteDouble() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#writeFloat(float)}
	 * .
	 */
	@Test
	public void testWriteFloat() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#writeInt(int)}.
	 */
	@Test
	public void testWriteInt1() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#writeLong(long)}
	 * .
	 */
	@Test
	public void testWriteLong() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#writeShort(int)}
	 * .
	 */
	@Test
	public void testWriteShort() {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSRandomAccessFile#writeUTF(java.lang.String)}
	 * .
	 */
	@Test
	public void testWriteUTF() {
	}

}
