/**
 *
 */
package org.irods.jargon.core.pub.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Properties;

import org.junit.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.MountedCollectionAO;
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
public class IRODSFIleInputStreamForSoftLinksTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IRODSFIleInputStreamForSoftLinksTest";
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
	 * read from an input stream on a soft-linked data object
	 *
	 * @throws Exception
	 */
	@Test
	public final void testReadWhenASoftLinkedDataObject() throws Exception {
		// generate a local scratch file

		String sourceCollectionName = "testReadWhenASoftLinkedDataObjectSource";
		String targetCollectionName = "testReadWhenASoftLinkedDataObjectTarget";
		String testFileName = "testReadWhenASoftLinkedDataObject.txt";
		int fileLength = 100;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						fileLength);

		String sourceIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ sourceCollectionName);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		// set up source collection
		IRODSFile sourceFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						sourceIrodsCollection);
		sourceFile.mkdirs();

		// create the soft link
		mountedCollectionAO.createASoftLink(sourceIrodsCollection,
				targetIrodsCollection);

		File localFile = new File(localFileName);

		// now put the file

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(
				targetIrodsCollection, testFileName);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		IRODSFileInputStream fis = irodsFileFactory
				.instanceIRODSFileInputStream(destFile);

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

}
