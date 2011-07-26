package org.irods.jargon.core.pub;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.utils.ChannelTools;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class Stream2StreamAOImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "Stream2StreamAOImplTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
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
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testStreamByteArrayToIRODSFile() throws Exception {
		String testFileName = "testStreamByteArrayToIRODSFile.xls";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		FileInputStream fis = new FileInputStream(new File(fileNameOrig));

		final ReadableByteChannel inputChannel = Channels.newChannel(fis);
		final WritableByteChannel outputChannel = Channels.newChannel(bos);

		ChannelTools.fastChannelCopy(inputChannel, outputChannel);

		byte[] bytesToStream = bos.toByteArray();
		Stream2StreamAO stream2StreamAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getStream2StreamAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);
		irodsFile.delete();

		stream2StreamAO.streamBytesToIRODSFile(bytesToStream, irodsFile);

		byte[] localChecksum = LocalFileUtils
				.computeMD5FileCheckSumViaAbsolutePath(fileNameOrig);

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		String irodsChecksum = dataObjectAO
				.computeMD5ChecksumOnDataObject(irodsFile);
		byte[] irodsChecksumAsByte = LocalFileUtils
				.hexStringToByteArray(irodsChecksum);

		Assert.assertTrue(
				"checksum from orig bytes and irods file do not match",
				Arrays.equals(localChecksum, irodsChecksumAsByte));

	}

	@Test
	public void testStreamIRODSFileToByteArray() throws Exception {
		String testFileName = "testStreamIRODSFileToByteArray.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 40);
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperationsAO.putOperation(fileNameOrig,
				targetIrodsCollection, "", null, null);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);

		Stream2StreamAO stream2StreamAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getStream2StreamAO(irodsAccount);
		byte[] actual = stream2StreamAO.streamFileToByte(irodsFile);

		Assert.assertEquals("byte length and file length do not match",
				irodsFile.length(), actual.length);

	}

}
