package org.irods.jargon.core.pub.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.connection.SettableJargonPropertiesMBean;
import org.irods.jargon.core.pub.DataObjectChecksumUtilitiesAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PackingIrodsOutputStreamTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "PackingIrodsOutputStreamTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;
	private static JargonProperties originalProperties;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(testingProperties);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	/**
	 * Make sure fresh properties before each test, some tests manipulate them
	 *
	 * @throws Exception
	 */
	@Before
	public void beforeEachTest() throws Exception {
		if (originalProperties == null) {
			originalProperties = irodsFileSystem.getJargonProperties();
		} else {
			irodsFileSystem.getIrodsSession().setJargonProperties(originalProperties);
		}
	}

	@Test
	public void testWriteLargeStream() throws Exception {
		String testFileName = "testWriteLargeStream.txt";
		;
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				50 * 1024 * 1024);

		new File(localFilePath);
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		IRODSFileOutputStream irodsFileOutputStream = irodsFileFactory.instanceIRODSFileOutputStream(irodsFile);
		PackingIrodsOutputStream packingIrodsOutputStream = new PackingIrodsOutputStream(irodsFileOutputStream);
		InputStream fileInputStream = new BufferedInputStream(new FileInputStream(new File(localFilePath)));

		byte[] buffer = new byte[8 * 1024];

		int n = 0;

		while (-1 != (n = fileInputStream.read(buffer))) {
			packingIrodsOutputStream.write(buffer, 0, n);
		}
		packingIrodsOutputStream.flush();
		fileInputStream.close();
		packingIrodsOutputStream.close();
		DataObjectChecksumUtilitiesAO dataObjectChecksumUtilitiesAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectChecksumUtilitiesAO(irodsAccount);
		dataObjectChecksumUtilitiesAO.verifyLocalFileAgainstIrodsFileChecksum(localFilePath,
				irodsFile.getAbsolutePath()); // throws exception
		// if mismatch

	}

	/**
	 * test for https://github.com/DICE-UNC/jargon/issues/200
	 *
	 * IndexOutOfBoundsException in PackingIrodsOutputStream #200
	 *
	 * @throws Exception
	 */
	@Test
	public void testWriteStreamBug200() throws Exception {

		String testFileName = "testWriteStreamBug200.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				20 * 1024 * 1024 + 7);

		new File(localFilePath);
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		SettableJargonPropertiesMBean settableJargonProperties = (SettableJargonPropertiesMBean) irodsFileSystem.getIrodsSession()
				.getJargonProperties();
		settableJargonProperties.setPutBufferSize(32768);
		irodsFileSystem.getIrodsSession().setJargonProperties(settableJargonProperties);
		IRODSFileOutputStream irodsFileOutputStream = irodsFileFactory.instanceIRODSFileOutputStream(irodsFile);
		PackingIrodsOutputStream packingIrodsOutputStream = new PackingIrodsOutputStream(irodsFileOutputStream);
		InputStream fileInputStream = new BufferedInputStream(new FileInputStream(new File(localFilePath)));

		int buffSize = 8 * 1024 + 3;
		byte[] buffer = new byte[buffSize];

		int n = 0;

		while (-1 != (n = fileInputStream.read(buffer))) {
			packingIrodsOutputStream.write(buffer, 0, n);
		}
		packingIrodsOutputStream.flush();
		fileInputStream.close();
		packingIrodsOutputStream.close();
		DataObjectChecksumUtilitiesAO dataObjectChecksumUtilitiesAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectChecksumUtilitiesAO(irodsAccount);
		dataObjectChecksumUtilitiesAO.verifyLocalFileAgainstIrodsFileChecksum(localFilePath,
				irodsFile.getAbsolutePath()); // throws exception
		// if mismatch

	}

	/**
	 * test for https://github.com/DICE-UNC/jargon/issues/200
	 *
	 * IndexOutOfBoundsException in PackingIrodsOutputStream #200
	 *
	 * @throws Exception
	 */
	@Test
	public void testWriteStreamBug200b() throws Exception {

		String testFileName = "testWriteStreamBug200b.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 16526);

		new File(localFilePath);
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		SettableJargonPropertiesMBean settableJargonProperties = (SettableJargonPropertiesMBean) irodsFileSystem.getIrodsSession()
				.getJargonProperties();
		settableJargonProperties.setPutBufferSize(1024);
		irodsFileSystem.getIrodsSession().setJargonProperties(settableJargonProperties);
		IRODSFileOutputStream irodsFileOutputStream = irodsFileFactory.instanceIRODSFileOutputStream(irodsFile);
		PackingIrodsOutputStream packingIrodsOutputStream = new PackingIrodsOutputStream(irodsFileOutputStream);
		InputStream fileInputStream = new BufferedInputStream(new FileInputStream(new File(localFilePath)));

		int buffSize = 2024;
		byte[] buffer = new byte[buffSize];

		int n = 0;

		while (-1 != (n = fileInputStream.read(buffer))) {
			packingIrodsOutputStream.write(buffer, 0, n);
		}
		packingIrodsOutputStream.flush();
		fileInputStream.close();
		packingIrodsOutputStream.close();
		DataObjectChecksumUtilitiesAO dataObjectChecksumUtilitiesAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectChecksumUtilitiesAO(irodsAccount);
		dataObjectChecksumUtilitiesAO.verifyLocalFileAgainstIrodsFileChecksum(localFilePath,
				irodsFile.getAbsolutePath()); // throws exception
		// if mismatch

	}

	@Test
	public void testWriteLargeStream2() throws Exception {
		String testFileName = "testWriteLargeStream2.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				20 * 1024 * 1024 + 7);

		new File(localFilePath);
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		IRODSFileOutputStream irodsFileOutputStream = irodsFileFactory.instanceIRODSFileOutputStream(irodsFile);
		PackingIrodsOutputStream packingIrodsOutputStream = new PackingIrodsOutputStream(irodsFileOutputStream);
		InputStream fileInputStream = new BufferedInputStream(new FileInputStream(new File(localFilePath)));

		int buffSize = 8 * 1024 + 3;
		byte[] buffer = new byte[buffSize];

		int n = 0;

		while (-1 != (n = fileInputStream.read(buffer))) {
			packingIrodsOutputStream.write(buffer, 0, n);
		}
		packingIrodsOutputStream.flush();
		fileInputStream.close();
		packingIrodsOutputStream.close();
		DataObjectChecksumUtilitiesAO dataObjectChecksumUtilitiesAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectChecksumUtilitiesAO(irodsAccount);
		dataObjectChecksumUtilitiesAO.verifyLocalFileAgainstIrodsFileChecksum(localFilePath,
				irodsFile.getAbsolutePath()); // throws exception
		// if mismatch

	}

}
