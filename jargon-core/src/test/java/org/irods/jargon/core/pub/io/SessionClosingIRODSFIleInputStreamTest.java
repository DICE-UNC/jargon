/**
 *
 */
package org.irods.jargon.core.pub.io;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
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

/**
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class SessionClosingIRODSFIleInputStreamTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "SessionClosingIRODSFIleInputStreamTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileInputStream#read()}.
	 */
	@Test
	public final void testRead() throws Exception {
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		String testFileName = "testread.txt";
		int fileLength = 40;

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		org.irods.jargon.testutils.filemanip.FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				fileLength);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(fileNameAndPath.toString(), targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection, testFileName);
		SessionClosingIRODSFileInputStream fis = irodsFileFactory.instanceSessionClosingIRODSFileInputStream(irodsFile);

		ByteArrayOutputStream actualFileContents = new ByteArrayOutputStream();

		// read the rest
		int bytesRead = 0;

		int readBytes;
		while ((readBytes = fis.read()) > -1) {
			actualFileContents.write(readBytes);
			bytesRead++;
		}

		fis.close();
		irodsFileSystem.closeAndEatExceptions();

		Assert.assertEquals("whole file not read back", fileLength, bytesRead);
	}

}
