package org.irods.jargon.core.pub.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.DataObjInp.OpenFlags;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
		String testFileName = "testCloseFileThenStream.csv";

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
				+ '/' + testFileName,
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
	}

	@Test
	public final void testIRODSFileOutputStreamIRODSFileShouldCreate()
			throws Exception {
		String testFileName = "testFileShouldCreate.txt";
		String string1 = "jfaijfjasidjfaisehfuaehfahfhudhfuashfuasfdhaisdfhaisdhfiaf";

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

		irodsFileOutputStream.write(string1.getBytes());
		irodsFileOutputStream.close();
		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsCollection
				+ '/' + testFileName,
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);

		IRODSFileInputStream irodsFileInputStream = irodsFileFactory
				.instanceIRODSFileInputStream(irodsFile);
		String actual = MiscIRODSUtils
				.convertStreamToString(irodsFileInputStream);
		irodsFileInputStream.close();
		irodsFileSystem.closeAndEatExceptions();
		Assert.assertEquals("should be first string string", string1, actual);

	}

	/**
	 * Create an output stream where the parent directory does not really exist,
	 * should create any intervening dirs
	 *
	 *
	 * @throws Exception
	 */
	@Ignore
	public final void testIRODSFileOutputStreamIRODSFileShouldCreateEvenThoughParentDirDoesNotExist()
			throws Exception {
		String testFileName = "testFileShouldCreate.txt";
		String testSubdir = "testsubdir";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testSubdir);

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
				+ "/" + testFileName,
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);

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
		String testFileName = "testIRODSFileOutputStreamIRODSFileShouldOpen.txt";
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
				+ '/' + testFileName,
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		Assert.assertTrue("no file descriptor assigned",
				irodsFileOutputStream.getFileDescriptor() > -1);

	}

	@Test
	public final void testIRODSFileOutputStreamOverwrite() throws Exception {
		String testFileName = "testIRODSFileOutputStreamOverwrite.txt";
		String string1 = "jfaijfjasidjfaisehfuaehfahfhudhfuashfuasfdhaisdfhaisdhfiaf";
		String string2 = "nvmzncvzmvnzx,mcv";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 8);

		new File(localFilePath);
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

		irodsFileOutputStream.write(string1.getBytes());
		irodsFileOutputStream.close();
		irodsFileOutputStream = irodsFileFactory.instanceIRODSFileOutputStream(
				irodsFile, OpenFlags.WRITE_TRUNCATE);

		irodsFileOutputStream.write(string2.getBytes());
		irodsFileOutputStream.close();

		IRODSFileInputStream irodsFileInputStream = irodsFileFactory
				.instanceIRODSFileInputStream(irodsFile);
		String actual = MiscIRODSUtils
				.convertStreamToString(irodsFileInputStream);
		irodsFileInputStream.close();

		Assert.assertEquals("should be second string", string2, actual);

	}

	/**
	 * Test for #52 Overwriting a file with IRODSFileOutputStream deletes file
	 * metadata
	 *
	 * @throws Exception
	 */
	@Test
	public final void testIRODSFileOutputStreamOverwriteBug52()
			throws Exception {
		String testFileName = "testIRODSFileOutputStreamOverwriteBug52.txt";
		String string1 = "jfaijfjasidjfaisehfuaehfahfhudhfuashfuasfdhaisdfhaisdhfiaf";
		String string2 = "nvmzncvzmvnzx,mcv";

		String expectedAttribName = "testIRODSFileOutputStreamOverwriteBug52";
		String expectedValueName = "blahblahblah";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 8);

		new File(localFilePath);
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

		irodsFileOutputStream.write(string1.getBytes());
		irodsFileOutputStream.close();

		// add an AVU

		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);

		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedValueName, "");

		dataObjectAO.addAVUMetadata(irodsFile.getAbsolutePath(), avuData);

		irodsFileOutputStream = irodsFileFactory.instanceIRODSFileOutputStream(
				irodsFile, OpenFlags.WRITE_TRUNCATE);

		irodsFileOutputStream.write(string2.getBytes());
		irodsFileOutputStream.close();

		IRODSFileInputStream irodsFileInputStream = irodsFileFactory
				.instanceIRODSFileInputStream(irodsFile);
		String actual = MiscIRODSUtils
				.convertStreamToString(irodsFileInputStream);
		irodsFileInputStream.close();

		Assert.assertEquals("should be second string", string2, actual);

		List<AVUQueryElement> avuQueryElements = new ArrayList<AVUQueryElement>();
		avuQueryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryPart.ATTRIBUTE, AVUQueryOperatorEnum.EQUAL,
				expectedAttribName));

		List<DataObject> dataObjects = dataObjectAO
				.findDomainByMetadataQuery(avuQueryElements);
		Assert.assertTrue("avu not preserved on stream overwrite",
				dataObjects.size() >= 1);

	}

	@Test
	public final void testIRODSFileOutputStreamReadTruncate() throws Exception {
		String testFileName = "testIRODSFileOutputStreamReadWriteTruncate.txt";
		String string1 = "jfaijfjasidjfaisehfuaehfahfhudhfuashfuasfdhaisdfhaisdhfiaf";
		String string2 = "nvmzncvzmvnzx,mcv";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 8);

		new File(localFilePath);
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
				.instanceIRODSFileOutputStream(irodsFile,
						OpenFlags.READ_TRUNCATE);

		irodsFileOutputStream.write(string1.getBytes());
		irodsFileOutputStream.close();
		irodsFileOutputStream = irodsFileFactory.instanceIRODSFileOutputStream(
				irodsFile, OpenFlags.WRITE_TRUNCATE);

		irodsFileOutputStream.write(string2.getBytes());
		irodsFileOutputStream.close();

		IRODSFileInputStream irodsFileInputStream = irodsFileFactory
				.instanceIRODSFileInputStream(irodsFile);
		String actual = MiscIRODSUtils
				.convertStreamToString(irodsFileInputStream);
		irodsFileInputStream.close();

		Assert.assertEquals("should be second string", string2, actual);
	}

	@Test
	public final void testIRODSFileOutputStreamReadWriteAndAppend()
			throws Exception {
		String testFileName = "testIRODSFileOutputStreamReadWriteAndAppend.txt";
		String string1 = "jfaijfjasidjfaisehfuaehfahfhudhfuashfuasfdhaisdfhaisdhfiaf";
		String string2 = "nvmzncvzmvnzx,mcv";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 8);

		new File(localFilePath);
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
				.instanceIRODSFileOutputStream(irodsFile, OpenFlags.WRITE);

		irodsFileOutputStream.write(string1.getBytes());
		irodsFileOutputStream.close();
		irodsFileOutputStream = irodsFileFactory.instanceIRODSFileOutputStream(
				irodsFile, OpenFlags.READ_WRITE);

		irodsFileOutputStream.write(string2.getBytes());
		irodsFileOutputStream.close();

		IRODSFileInputStream irodsFileInputStream = irodsFileFactory
				.instanceIRODSFileInputStream(irodsFile);
		String actual = MiscIRODSUtils
				.convertStreamToString(irodsFileInputStream);
		irodsFileInputStream.close();

		Assert.assertEquals("should be concatenated string", string1 + string2,
				actual);

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

	@Ignore
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
		String testFileName = "testIRODSFileOutputStreamIRODSFileClose.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				100);

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

		IRODSFileOutputStream irodsFileOutputStream = irodsFileFactory
				.instanceIRODSFileOutputStream(irodsFile);
		irodsFileOutputStream.close();
		// no error is success
	}

	/**
	 * [#700] cdr errors multiple FITS processes writing to irods
	 *
	 * @throws Exception
	 */

	@Test
	public final void testIRODSFileOutputStreamMultipleWritesToParentDir()
			throws Exception {
		int numberWrites = 10;
		String testFileNamePrefix = "testIRODSFileOutputStreamMultipleWritesToParentDir";
		String testFileNameSuffix = ".txt";
		String testSubdir = "testIRODSFileOutputStreamMultipleWritesToParentDir";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		absPath = FileGenerator.generateFileOfFixedLengthGivenName(absPath,
				testFileNamePrefix + testFileNameSuffix, 10 * 1024 * 1024);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testSubdir);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile parentDir = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		parentDir.mkdirs();

		Executors.newFixedThreadPool(numberWrites);

		final List<OutputStreamWriteTestWriter> writerThreads = new ArrayList<OutputStreamWriteTestWriter>();

		OutputStreamWriteTestWriter outputStreamWriter;

		for (int i = 0; i < numberWrites; i++) {
			outputStreamWriter = new OutputStreamWriteTestWriter(absPath,
					targetIrodsCollection + "/" + testFileNamePrefix + i
					+ testFileNameSuffix,
					irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
			writerThreads.add(outputStreamWriter);
		}

		for (OutputStreamWriteTestWriter writer : writerThreads) {
			Assert.assertNull("should not be an exception",
					writer.getException());
		}
	}

	@Test
	public final void testIRODSFileOutputStreamIRODSFileCloseTwice()
			throws Exception {
		String testFileName = "testIRODSFileOutputStreamIRODSFileCloseTwice.txt";
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
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		IRODSFileOutputStream irodsFileOutputStream = irodsFileFactory
				.instanceIRODSFileOutputStream(irodsFile);
		irodsFileOutputStream.close();
		irodsFileOutputStream.close();
	}

	/**
	 * Write to an output stream that is underneath the root
	 *
	 * @throws Exception
	 */
	@Test
	public final void testWriteToOutputStreamInSubdirUnderRoot()
			throws Exception {
		String testCollName = "testWriteToOutputStreamInSubdirUnderRoot";
		String testFileName = "testWriteToOutputStreamInSubdirUnderRoot.csv";

		String targetIrodsCollection = "/" + testCollName;
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);

		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();

		if (props.isConsortiumVersion()) {
			return;
		}

		IRODSFile targetCollection = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		targetCollection.deleteWithForceOption();
		targetCollection.mkdirs();

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

}

class OutputStreamWriteTestWriter implements Callable<String> {

	final String localFileAbsolutePath;
	final String targetIrodsFileName;
	final IRODSAccessObjectFactory irodsAccessObjectFactory;
	final IRODSAccount irodsAccount;
	Exception exception = null;

	OutputStreamWriteTestWriter(final String localFileAbsolutePath,
			final String targetIrodsFileName,
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		this.localFileAbsolutePath = localFileAbsolutePath;
		this.targetIrodsFileName = targetIrodsFileName;
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
		this.irodsAccount = irodsAccount;
	}

	@Override
	public String call() throws Exception {
		File localFile = new File(localFileAbsolutePath);
		InputStream inputStream = new FileInputStream(localFile);
		IRODSFileOutputStream outputStream = irodsAccessObjectFactory
				.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFileOutputStream(targetIrodsFileName);
		try {

			int myBuffSize = irodsAccessObjectFactory.getJargonProperties()
					.getInputToOutputCopyBufferByteSize();

			int doneCnt = -1;

			byte buf[] = new byte[myBuffSize];

			while ((doneCnt = inputStream.read(buf, 0, myBuffSize)) >= 0) {

				if (doneCnt == 0) {
					Thread.yield();
				} else {
					outputStream.write(buf, 0, doneCnt);
				}
			}

			outputStream.flush();

		} catch (FileNotFoundException e) {
			exception = e;
			throw new JargonException(
					"file not found exception copying buffers", e);
		} catch (Exception e) {
			exception = e;
			throw new JargonException("Exception copying buffers", e);
		} finally {

			try {
				inputStream.close();
			} catch (Exception e) {
			}

			try {
				outputStream.close();
			} catch (Exception e) {
			}

		}
		return targetIrodsFileName;
	}

	/**
	 * @return the exception
	 */
	public Exception getException() {
		return exception;
	}

}
