package org.irods.jargon.core.pub.io;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryImpl;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.icommandinvoke.IcommandInvoker;
import org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext;
import org.irods.jargon.testutils.icommandinvoke.icommands.IputCommand;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSFileOutputStreamTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IRODSFileOutputStreamTest";
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

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public final void testWriteInt() throws Exception {
		String testFileName = "testWriteInt.csv";

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
		IRODSFileOutputStream irodsFileOutputStream = irodsFileFactory
				.instanceIRODSFileOutputStream(irodsFile);

		int writtenInt = 3;
		irodsFileOutputStream.write(writtenInt);
		irodsFileOutputStream.close();
		irodsFile.close();
		// now reopen and read back
		irodsFile.open();
		IRODSFileInputStream irodsFileInputStream = irodsFileFactory
				.instanceIRODSFileInputStream(irodsFile);
		int readBackInt = irodsFileInputStream.read();

		irodsFileInputStream.close();
		irodsFile.close();

		Assert.assertEquals("did not get back the int I wrote", writtenInt,
				readBackInt);
		irodsFileSystem.closeAndEatExceptions();

	}

	@Test
	public final void testWriteByteArray() throws Exception {
		String testFileName = "testWriteByteArray.csv";

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
		IRODSFileOutputStream irodsFileOutputStream = irodsFileFactory
				.instanceIRODSFileOutputStream(irodsFile);

		// get a simple byte array
		String myBytes = "ajjjjjjjjjjjjjjjjjjjjjjjjfeiiiiiiiiiiiiiii54454545";
		byte[] myBytesArray = myBytes.getBytes();

		irodsFileOutputStream.write(myBytesArray);
		irodsFileOutputStream.write(myBytesArray);

		irodsFile.close();

		irodsFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection
				+ '/' + testFileName);

		long length = irodsFile.length();

		Assert.assertEquals("file length does not match bytes written",
				myBytesArray.length * 2, length);

		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public final void testWriteByteArrayIntInt() throws Exception {
		String testFileName = "testWriteByteArrayIntInt.csv";

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
		irodsFile.createNewFile();
		IRODSFileOutputStream irodsFileOutputStream = irodsFileFactory
				.instanceIRODSFileOutputStream(irodsFile);

		// get a simple byte array
		String myBytes = "ajjjjjjjjjfjjifi98jdkjfaklsdfjaidnadfjaisdfaskdjfaijfjfjad;fasjgjgkjjasfgkasgkjas;dfjas;df9920jdsaklfaslkdfja;sdjfasffjjjjjjjjjjjjjfeiiiiiiiiiiiiiii54454545";
		String expectedBytes = "fjjifi98jd";
		byte[] myBytesArray = myBytes.getBytes();
		byte[] myExpectedBytesArray = expectedBytes.getBytes();

		// should write fjjifi98jd
		irodsFileOutputStream.write(myBytesArray, 10, 10);
		irodsFileOutputStream.close();
		irodsFile.close();

		// now reopen and read back
		irodsFile.open();
		IRODSFileInputStream irodsFileInputStream = irodsFileFactory
				.instanceIRODSFileInputStream(irodsFile);
		byte[] readBytesBuffer = new byte[myExpectedBytesArray.length];
		irodsFileInputStream.read(readBytesBuffer);

		irodsFileInputStream.close();
		irodsFile.close();
		irodsFileSystem.closeAndEatExceptions();

		boolean equalArrays = Arrays.equals(myExpectedBytesArray,
				readBytesBuffer);

		Assert.assertTrue(
				"did not read back what I wrote to the output stream",
				equalArrays);
	}

	@Test
	public final void testClose() throws Exception {
		String testFileName = "testClose.csv";

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
		irodsFile.createNewFile();
		IRODSFileOutputStream irodsFileOutputStream = irodsFileFactory
				.instanceIRODSFileOutputStream(irodsFile);

		// get a simple byte array
		String myBytes = "ajjjjjjjjjfjjifi98jdkjfaklsdfjaidnadfjaisdfaskdjfaijfjfjad;fasjgjgkjjasfgkasgkjas;dfjas;df9920jdsaklfaslkdfja;sdjfasffjjjjjjjjjjjjjfeiiiiiiiiiiiiiii54454545";
		byte[] myBytesArray = myBytes.getBytes();
		// should write fjjifi98jd
		irodsFileOutputStream.write(myBytesArray, 10, 10);
		irodsFileOutputStream.close();
		irodsFile.close();
		irodsFileSystem.closeAndEatExceptions();
		Assert.assertTrue(irodsFile.getFileDescriptor() < 0);
	}

	@Test
	public final void testCloseStreamTwice() throws Exception {
		String testFileName = "testCloseStreamTwice.csv";

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
		irodsFile.createNewFile();
		IRODSFileOutputStream irodsFileOutputStream = irodsFileFactory
				.instanceIRODSFileOutputStream(irodsFile);

		// get a simple byte array
		String myBytes = "ajjjjjjjjjfjjifi98jdkjfaklsdfjaidnadfjaisdfaskdjfaijfjfjad;fasjgjgkjjasfgkasgkjas;dfjas;df9920jdsaklfaslkdfja;sdjfasffjjjjjjjjjjjjjfeiiiiiiiiiiiiiii54454545";
		byte[] myBytesArray = myBytes.getBytes();
		// should write fjjifi98jd
		irodsFileOutputStream.write(myBytesArray, 10, 10);
		irodsFileOutputStream.close();
		irodsFileOutputStream.close();
		irodsFile.close();
		irodsFileSystem.closeAndEatExceptions();
		Assert.assertTrue(irodsFile.getFileDescriptor() < 0);
	}

	@Test
	public final void testCloseFileThenStream() throws Exception {
		String testFileName = "testCloseStreamTwice.csv";

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
		irodsFile.createNewFile();
		IRODSFileOutputStream irodsFileOutputStream = irodsFileFactory
				.instanceIRODSFileOutputStream(irodsFile);

		irodsFile.close();
		irodsFileOutputStream.close();
		irodsFileSystem.closeAndEatExceptions();
		Assert.assertTrue(irodsFile.getFileDescriptor() < 0);
	}

	@Test
	public final void testIRODSFileOutputStreamIRODSFileDoesNotExist()
			throws Exception {
		String testFileName = "testFileShouldCreate.txt";

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

		irodsFileFactory.instanceIRODSFileOutputStream(irodsFile);
		irodsFileSystem.closeAndEatExceptions();
		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsCollection
				+ '/' + testFileName);
	}

	@Test
	public final void testIRODSFileOutputStreamIRODSFileShouldCreate()
			throws Exception {
		String testFileName = "testFileShouldCreate.txt";

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

		irodsFile.createNewFile();
		irodsFileSystem.closeAndEatExceptions();
		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsCollection
				+ '/' + testFileName);

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testIRODSFileOutputStreamEmptyStringFileName()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		irodsFileFactory.instanceIRODSFile("");

	}

	@Test
	public final void testIRODSFileOutputStreamIRODSFileShouldOpen()
			throws Exception {
		String testFileName = "testFileShouldOpen.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 8);

		File localFile = new File(localFilePath);
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

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFile, irodsFile, null, null);

		IRODSFileOutputStream irodsFileOutputStream = irodsFileFactory
				.instanceIRODSFileOutputStream(irodsFile);
		irodsFileSystem.closeAndEatExceptions();
		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsCollection
				+ '/' + testFileName);
		Assert.assertTrue("no file descriptor assigned",
				irodsFileOutputStream.getFileDescriptor() > -1);

	}

	@Test
	public final void testIRODSFileOutputStreamWithReroute() throws Exception {

		if (!testingPropertiesHelper
				.isTestDistributedResources(testingProperties)) {
			return;
		}

		String testFileName = "testIRODSFileOutputStreamWithReroute.txt";

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
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		irodsFile
				.setResource(testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_TERTIARY_RESOURCE_KEY));

		IRODSFileOutputStream irodsFileOutputStream = irodsFileFactory
				.instanceIRODSFileOutputStreamWithRerouting(irodsFile);
		int writtenInt = 3;
		irodsFileOutputStream.write(writtenInt);
		irodsFileOutputStream.close();
		irodsFileSystem.closeAndEatExceptions(irodsAccount);

		Assert.assertTrue(
				"did not get session closing stream for re-route",
				irodsFileOutputStream instanceof SessionClosingIRODSFileOutputStream);
		Assert.assertNull("session from reroute leaking",
				irodsFileSystem.getConnectionMap());

	}

	@Test
	public final void testIRODSFileOutputStreamWithRerouteNoReroute()
			throws Exception {

		String testFileName = "testIRODSFileOutputStreamWithRerouteNoReroute.txt";

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
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		IRODSFileOutputStream irodsFileOutputStream = irodsFileFactory
				.instanceIRODSFileOutputStreamWithRerouting(irodsFile);
		int writtenInt = 3;
		irodsFileOutputStream.write(writtenInt);
		irodsFileOutputStream.close();
		irodsFileSystem.closeAndEatExceptions(irodsAccount);

		Assert.assertTrue("did not get normal stream for re-route",
				irodsFileOutputStream instanceof IRODSFileOutputStream);
		Assert.assertNull("session from reroute leaking",
				irodsFileSystem.getConnectionMap());

	}

	@Test
	public final void testIRODSFileOutputStreamIRODSFileClose()
			throws Exception {
		String testFileName = "testFileClose.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				1);

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
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		IRODSFileOutputStream irodsFileOutputStream = irodsFileFactory
				.instanceIRODSFileOutputStream(irodsFile);
		irodsFileOutputStream.close();
		irodsSession.closeSession();
		// no error is success
	}

	@Test
	public final void testIRODSFileOutputStreamIRODSFileCloseTwice()
			throws Exception {
		String testFileName = "testFileClose.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				1);

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
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		IRODSFileOutputStream irodsFileOutputStream = irodsFileFactory
				.instanceIRODSFileOutputStream(irodsFile);
		irodsFileOutputStream.close();
		irodsFileOutputStream.close();
		irodsSession.closeSession();
	}

}
