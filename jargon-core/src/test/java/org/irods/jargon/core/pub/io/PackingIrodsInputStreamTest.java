package org.irods.jargon.core.pub.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.DataObjectChecksumUtilitiesAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class PackingIrodsInputStreamTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "PackingIrodsInputStreamTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public final void testInputStream1() throws Exception {

		String testFileName = "testInputStream1.txt";
		String newLocalFileName = "testInputStream1-new.txt";
		int fileLength = 100 * 1024 + 7;

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = org.irods.jargon.testutils.filemanip.FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, fileLength);
		File localFile = new File(localFilePath);

		// put scratch file into irods in the right place

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection, testFileName);

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFile, irodsFile, null, null);

		IRODSFileInputStream fis = irodsFileFactory.instanceIRODSFileInputStream(irodsFile.getAbsolutePath());
		PackingIrodsInputStream pis = new PackingIrodsInputStream(fis);

		File newLocal = new File(absPath, newLocalFileName);

		OutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(newLocal));

		final byte[] buffer = new byte[512];

		int n = 0;
		while (-1 != (n = pis.read(buffer))) {
			fileOutputStream.write(buffer, 0, n);
		}
		fileOutputStream.flush();

		pis.close();
		fileOutputStream.close();

		DataObjectChecksumUtilitiesAO dataObjectChecksumUtilitiesAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectChecksumUtilitiesAO(irodsAccount);
		dataObjectChecksumUtilitiesAO.verifyLocalFileAgainstIrodsFileChecksum(newLocal.getAbsolutePath(),
				irodsFile.getAbsolutePath());

	}

	@Test
	public final void testInputStream2() throws Exception {

		String testFileName = "testInputStream2.txt";
		String newLocalFileName = "testInputStream2-new.txt";
		int fileLength = 120 * 1024 * 1024 + 7;

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = org.irods.jargon.testutils.filemanip.FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, fileLength);
		File localFile = new File(localFilePath);

		// put scratch file into irods in the right place

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection, testFileName);

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFile, irodsFile, null, null);

		IRODSFileInputStream fis = irodsFileFactory.instanceIRODSFileInputStream(irodsFile.getAbsolutePath());
		PackingIrodsInputStream pis = new PackingIrodsInputStream(fis);

		File newLocal = new File(absPath, newLocalFileName);

		OutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(newLocal));

		final byte[] buffer = new byte[5177];

		int n = 0;
		while (-1 != (n = pis.read(buffer))) {
			fileOutputStream.write(buffer, 0, n);
		}
		fileOutputStream.flush();

		pis.close();
		fileOutputStream.close();

		DataObjectChecksumUtilitiesAO dataObjectChecksumUtilitiesAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectChecksumUtilitiesAO(irodsAccount);
		dataObjectChecksumUtilitiesAO.verifyLocalFileAgainstIrodsFileChecksum(newLocal.getAbsolutePath(),
				irodsFile.getAbsolutePath());

	}

	@Test
	public final void testInputStream3() throws Exception {

		String testFileName = "testInputStream3.txt";
		String newLocalFileName = "testInputStream3-new.txt";
		int fileLength = 120;

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = org.irods.jargon.testutils.filemanip.FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, fileLength);
		File localFile = new File(localFilePath);

		// put scratch file into irods in the right place

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection, testFileName);

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFile, irodsFile, null, null);

		IRODSFileInputStream fis = irodsFileFactory.instanceIRODSFileInputStream(irodsFile.getAbsolutePath());
		PackingIrodsInputStream pis = new PackingIrodsInputStream(fis);

		File newLocal = new File(absPath, newLocalFileName);

		OutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(newLocal));

		final byte[] buffer = new byte[5177];

		int n = 0;
		while (-1 != (n = pis.read(buffer))) {
			fileOutputStream.write(buffer, 0, n);
		}
		fileOutputStream.flush();

		pis.close();
		fileOutputStream.close();

		DataObjectChecksumUtilitiesAO dataObjectChecksumUtilitiesAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectChecksumUtilitiesAO(irodsAccount);
		dataObjectChecksumUtilitiesAO.verifyLocalFileAgainstIrodsFileChecksum(newLocal.getAbsolutePath(),
				irodsFile.getAbsolutePath());

	}

	@Test
	public final void testInputStream4() throws Exception {

		String testFileName = "testInputStream4.txt";
		String newLocalFileName = "testInputStream4-new.txt";
		int fileLength = 120;

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = org.irods.jargon.testutils.filemanip.FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, fileLength);
		File localFile = new File(localFilePath);

		// put scratch file into irods in the right place

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection, testFileName);

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFile, irodsFile, null, null);

		IRODSFileInputStream fis = irodsFileFactory.instanceIRODSFileInputStream(irodsFile.getAbsolutePath());
		PackingIrodsInputStream pis = new PackingIrodsInputStream(fis);

		File newLocal = new File(absPath, newLocalFileName);

		OutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(newLocal));

		final byte[] buffer = new byte[51 * 1024 * 1024];

		int n = 0;
		while (-1 != (n = pis.read(buffer))) {
			fileOutputStream.write(buffer, 0, n);
		}
		fileOutputStream.flush();

		pis.close();
		fileOutputStream.close();

		DataObjectChecksumUtilitiesAO dataObjectChecksumUtilitiesAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectChecksumUtilitiesAO(irodsAccount);
		dataObjectChecksumUtilitiesAO.verifyLocalFileAgainstIrodsFileChecksum(newLocal.getAbsolutePath(),
				irodsFile.getAbsolutePath());

	}

	@Test
	public final void testInputStream5() throws Exception {

		String testFileName = "testInputStream5.txt";
		String newLocalFileName = "testInputStream5-new.txt";
		int fileLength = 90 * 1024 * 1025 + 1;

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = org.irods.jargon.testutils.filemanip.FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, fileLength);
		File localFile = new File(localFilePath);

		// put scratch file into irods in the right place

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection, testFileName);

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFile, irodsFile, null, null);

		IRODSFileInputStream fis = irodsFileFactory.instanceIRODSFileInputStream(irodsFile.getAbsolutePath());
		PackingIrodsInputStream pis = new PackingIrodsInputStream(fis);

		File newLocal = new File(absPath, newLocalFileName);

		OutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(newLocal));

		final byte[] buffer = new byte[51 * 1024 * 1024];

		int n = 0;
		while (-1 != (n = pis.read(buffer))) {
			fileOutputStream.write(buffer, 0, n);
		}
		fileOutputStream.flush();

		pis.close();
		fileOutputStream.close();

		DataObjectChecksumUtilitiesAO dataObjectChecksumUtilitiesAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectChecksumUtilitiesAO(irodsAccount);
		dataObjectChecksumUtilitiesAO.verifyLocalFileAgainstIrodsFileChecksum(newLocal.getAbsolutePath(),
				irodsFile.getAbsolutePath());

	}

	@Test
	public final void testSkip1() throws Exception {

		String testFileName = "testSkip1.txt";
		int fileLength = 93 * 1024 + 7;

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = org.irods.jargon.testutils.filemanip.FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, fileLength);
		File localFile = new File(localFilePath);

		// put scratch file into irods in the right place

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection, testFileName);

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFile, irodsFile, null, null);

		IRODSFileInputStream fis = irodsFileFactory.instanceIRODSFileInputStream(irodsFile.getAbsolutePath());
		PackingIrodsInputStream pis = new PackingIrodsInputStream(fis);
		long toSkip = 55 * 1024;
		long skipped = pis.skip(toSkip);
		pis.close();
		Assert.assertEquals("didn't get expected skip", toSkip, skipped);

	}

	@Test
	public final void testSkip2() throws Exception {

		String testFileName = "testSkip2.txt";
		int fileLength = 93 * 1024 + 7;

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = org.irods.jargon.testutils.filemanip.FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, fileLength);
		File localFile = new File(localFilePath);

		// put scratch file into irods in the right place

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection, testFileName);

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFile, irodsFile, null, null);

		IRODSFileInputStream fis = irodsFileFactory.instanceIRODSFileInputStream(irodsFile.getAbsolutePath());
		PackingIrodsInputStream pis = new PackingIrodsInputStream(fis);
		long toSkip = 80 * 1024 * 1024;
		long skipped = pis.skip(toSkip);
		pis.close();
		Assert.assertTrue("didn't get expected skip", skipped > 0);

	}
}
