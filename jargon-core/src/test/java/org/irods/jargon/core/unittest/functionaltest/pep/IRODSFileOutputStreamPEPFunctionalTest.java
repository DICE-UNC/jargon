package org.irods.jargon.core.unittest.functionaltest.pep;

import java.io.File;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.packinstr.DataObjInp.OpenFlags;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileOutputStream;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * NOTE
 *
 * These tests require the jargon-functional.re rules to be configured in the
 * target iRODS server
 *
 * @author Mike Conway - DICE
 *
 */
public class IRODSFileOutputStreamPEPFunctionalTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IRODSFileOutputStreamPEPFunctionalTest";
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
	public final void testPepFiresOnClose() throws Exception {
		String testFileName = "testPepFiresOnClose.txt";
		String string1 = "jfaijfjasidjfaisehfuaehfahfhudhfuashfuasfdhaisdfhaisdhfiaf";
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
		assertionHelper.assertDataObjectFlaggedWithAVU(
				irodsFile.getAbsolutePath(), "postProcForPutFired",
				accessObjectFactory, irodsAccount);

	}

	/**
	 * Test of bug 146, requires jargon-functional.re rule for postProcForPut to
	 * be enabled!
	 *
	 * @throws Exception
	 */
	@Test
	public final void testIRODSFileOutputStreamOverwriteNonexistentBug146()
			throws Exception {
		String testFileName = "testIRODSFileOutputStreamOverwriteNonexistentBug146.txt";
		String string = "jfaijfjasidjfaisehfuaehfahfhudhfuashfuasfdhaisdfhaisdhfiaf";
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
						OpenFlags.WRITE_TRUNCATE);

		irodsFileOutputStream.write(string.getBytes());
		irodsFileOutputStream.close();
		assertionHelper.assertDataObjectFlaggedWithAVU(
				irodsFile.getAbsolutePath(), "postProcForPutFired",
				accessObjectFactory, irodsAccount);

	}

}
