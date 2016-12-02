package org.irods.jargon.core.unittest.functionaltest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.pub.DataObjectChecksumUtilitiesAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileOutputStream;
import org.irods.jargon.core.pub.io.PackingIrodsOutputStream;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class StreamOpsLazyWalkTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "StreamOpsLazyWalkTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;
	private static JargonProperties originalProperties;

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
	 * Make sure fresh properties before each test, some tests manipulate them
	 * 
	 * @throws Exception
	 */
	@Before
	public void beforeEachTest() throws Exception {
		if (originalProperties == null) {
			originalProperties = irodsFileSystem.getJargonProperties();
		} else {
			irodsFileSystem.getIrodsSession().setJargonProperties(
					originalProperties);
		}
	}

	/**
	 * test for https://github.com/DICE-UNC/jargon/issues/200
	 * 
	 * IndexOutOfBoundsException in PackingIrodsOutputStream #200
	 * 
	 * @throws Exception
	 */
	@Test
	public void lazyWalk() throws Exception {

		String testFileName = "lazyWalk.txt";
		int fileSizeSeed = 1 * 1024;
		int putGetBufferSizeSeed = 1 * 1024;
		int clientSpecifiedBufferSizeSeed = 1 * 2024;
		int fileSizeWalkBound = 10 * 1024;
		int putGetBufferSizeWalkBound = 5 * 1024;
		int clientBufferSizeWalkBound = 3 * 1024;

		int fileSizeIterations = 50;
		int bufferTwiddleIterations = 10;

		int fileSizeBase = fileSizeSeed;
		int putGetBufferSize;
		int clientSpecifiedBufferSize;

		for (int i = 0; i <= fileSizeIterations; i++) {

			fileSizeBase = fileSizeBase
					+ ThreadLocalRandom.current().nextInt(fileSizeBase,
							fileSizeBase + fileSizeWalkBound) * 2;
			putGetBufferSize = putGetBufferSizeSeed;
			clientSpecifiedBufferSize = clientSpecifiedBufferSizeSeed;

			for (int j = 0; j <= bufferTwiddleIterations; j++) {

				doATest(putGetBufferSize, clientSpecifiedBufferSize,
						fileSizeBase, testFileName);

				putGetBufferSize = putGetBufferSize
						+ ThreadLocalRandom.current().nextInt(putGetBufferSize,
								putGetBufferSize + putGetBufferSizeWalkBound);

				clientSpecifiedBufferSize = clientSpecifiedBufferSize
						+ ThreadLocalRandom.current().nextInt(
								clientSpecifiedBufferSize,
								clientSpecifiedBufferSize
										+ clientBufferSizeWalkBound);

			}

		}

	}

	private void doATest(int putGetBufferSizeInProps,
			int clientSpecifiedBufferSize, int fileSize, String fileName)
			throws Exception {

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, fileName, fileSize);

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
				.instanceIRODSFile(targetIrodsCollection + '/' + fileName);
		irodsFile.deleteWithForceOption();

		SettableJargonProperties settableJargonProperties = (SettableJargonProperties) irodsFileSystem
				.getIrodsSession().getJargonProperties();
		settableJargonProperties.setPutBufferSize(putGetBufferSizeInProps);
		irodsFileSystem.getIrodsSession().setJargonProperties(
				settableJargonProperties);
		IRODSFileOutputStream irodsFileOutputStream = irodsFileFactory
				.instanceIRODSFileOutputStream(irodsFile);
		PackingIrodsOutputStream packingIrodsOutputStream = new PackingIrodsOutputStream(
				irodsFileOutputStream);
		InputStream fileInputStream = new BufferedInputStream(
				new FileInputStream(new File(localFilePath)));

		byte[] buffer = new byte[clientSpecifiedBufferSize];

		int n = 0;

		while (-1 != (n = fileInputStream.read(buffer))) {
			packingIrodsOutputStream.write(buffer, 0, n);
		}
		packingIrodsOutputStream.flush();
		fileInputStream.close();
		packingIrodsOutputStream.close();
		DataObjectChecksumUtilitiesAO dataObjectChecksumUtilitiesAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getDataObjectChecksumUtilitiesAO(irodsAccount);
		dataObjectChecksumUtilitiesAO.verifyLocalFileAgainstIrodsFileChecksum(
				localFilePath, irodsFile.getAbsolutePath()); // throws exception
		File localFile = new File(localFilePath);
		localFile.delete();
		// if mismatch

	}

}
