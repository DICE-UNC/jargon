package org.irods.jargon.datautils.sharing;

import java.io.File;
import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataObjectAOImpl;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class AnonymousAccessServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "AnonymousAccessServiceImplTest";
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
	public final void testIsAnonymousAccessSetUpForDataObjectWithAnonymousAccess() throws Exception {
		String testFileName = "testIsAnonymousAccessSetUpForDataObjectWithAnonymousAccess.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);
		File sourceFile = new File(fileNameOrig);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory().getDataTransferOperations(irodsAccount);
		dto.putOperation(sourceFile, irodsFile, null, null);

		dataObjectAO.setAccessPermissionRead("", targetIrodsCollection + "/"
				+ testFileName, IRODSAccount.PUBLIC_USERNAME);

		AnonymousAccessService anonymousAccessService = new AnonymousAccessServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		boolean hasAccess = anonymousAccessService
				.isAnonymousAccessSetUp(irodsFile.getAbsolutePath() + "/"
						+ testFileName);

		TestCase.assertTrue("did not have expected access", hasAccess);
	}

	@Test(expected = FileNotFoundException.class)
	public final void testIsAnonymousAccessSetUpForDataObjectWithAnonymousAccessNotExists()
			throws Exception {
		String testFileName = "testIsAnonymousAccessSetUpForDataObjectWithAnonymousAccessNotExists.txt";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);

		AnonymousAccessService anonymousAccessService = new AnonymousAccessServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		anonymousAccessService
				.isAnonymousAccessSetUp(irodsFile.getAbsolutePath() + "/"
						+ testFileName);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testIsAnonymousAccessSetUpNullFile() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		AnonymousAccessService anonymousAccessService = new AnonymousAccessServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		anonymousAccessService.isAnonymousAccessSetUp(null);

	}

	@Test(expected = JargonException.class)
	public final void testIsAnonymousAccessSetUpNullAnonUserSet()
			throws Exception {

		String testFileName = "testIsAnonymousAccessSetUpNullAnonUserSet.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);
		File sourceFile = new File(fileNameOrig);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dto = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dto.putOperation(sourceFile, irodsFile, null, null);

		AnonymousAccessService anonymousAccessService = new AnonymousAccessServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		anonymousAccessService.setAnonymousUserName(null);
		anonymousAccessService.isAnonymousAccessSetUp(irodsFile
				.getAbsolutePath() + "/"
				+ testFileName);

	}
	
}
