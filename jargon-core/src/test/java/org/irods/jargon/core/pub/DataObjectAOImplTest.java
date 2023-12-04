package org.irods.jargon.core.pub;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.exception.CatalogSQLException;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.OverwriteException;
import org.irods.jargon.core.packinstr.TransferOptions;
import org.irods.jargon.core.packinstr.TransferOptions.ForceOption;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.domain.Resource;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.pub.domain.UserGroup;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.remoteexecute.RemoteExecuteServiceImpl;
import org.irods.jargon.core.transfer.DefaultTransferControlBlock;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener.CallbackResponse;
import org.irods.jargon.core.transfer.TransferStatusCallbackListenerTestingImplementation;
import org.irods.jargon.core.utils.CollectionAndPath;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.testutils.AssertionHelper;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import junit.framework.TestCase;

public class DataObjectAOImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "dataObjectAOImplTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static AssertionHelper assertionHelper = null;
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		assertionHelper = new AssertionHelper();
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@After
	public void afterEach() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	/**
	 * Bug https://github.com/DICE-UNC/jargon/issues/12 read length set to zero
	 * dataObjectAOImpl.findDomainByMetadataQuery #12
	 *
	 * @throws Exception
	 */
	@Test
	public void testFindDomainByMetadataQueryBug12() throws Exception {

		String testFileName = "testFindDomainByMetadataQueryBug12.dat";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 10);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		// put scratch file into irods in the right place on the first resource

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String dataObjectAbsPath = targetIrodsCollection + '/' + testFileName;

		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsCollection, irodsAccount.getDefaultStorageResource(), null, null);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		// initialize the AVU data
		String expectedAttribName = "testfbmdattrib1bug12";
		String expectedAttribValue = "testfbmdvalue1bug12";
		String expectedAttribUnits = "test1fbmdunitsbug12S";

		AvuData avuData = AvuData.instance(expectedAttribName, expectedAttribValue, expectedAttribUnits);

		dataObjectAO.deleteAVUMetadata(dataObjectAbsPath, avuData);
		dataObjectAO.addAVUMetadata(dataObjectAbsPath, avuData);

		AVUQueryElement elem1 = AVUQueryElement.instanceForValueQuery(AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, expectedAttribName);

		AVUQueryElement elem2 = AVUQueryElement.instanceForValueQuery(AVUQueryElement.AVUQueryPart.VALUE,
				QueryConditionOperators.EQUAL, expectedAttribValue);

		List<DataObject> result = dataObjectAO.findDomainByMetadataQuery(Arrays.asList(elem1, elem2));
		Assert.assertFalse("no query result returned", result.isEmpty());
		Assert.assertEquals(testFileName, result.get(0).getDataName());
	}

	@Test
	public void testDataObjectAOImpl() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl.instance(irodsSession);
		DataObjectAO dataObjectAO = accessObjectFactory.getDataObjectAO(irodsAccount);
		irodsSession.closeSession();
		Assert.assertNotNull(dataObjectAO);
	}

	@Test
	public void testPutWithTargetSpecifiedAsCollection() throws Exception {
		// generate a local scratch file
		String testFileName = "testPutWithTargetSpecifiedAsCollection.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		File localFile = new File(localFileName);

		// now put the file
		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, true);

		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsFile + "/" + testFileName, accessObjectFactory,
				irodsAccount);

	}

	@Test
	public void testPutOverwriteFileNotInIRODS() throws Exception {
		// generate a local scratch file
		String testFileName = "testPutOverwriteFileNotInIRODS.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, true);
		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsFile, accessObjectFactory, irodsAccount);
	}

	@Test
	public void testPutExecutableFile() throws Exception {
		// generate a local scratch file
		String testFileName = "testPutExecutableFile.sh";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		File localFile = new File(localFileName);
		localFile.setExecutable(true);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, true);
		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsFile, accessObjectFactory, irodsAccount);
	}

	@Test
	public void testChecksumAndPut0KFile() throws Exception {
		// generate a local scratch file
		String testFileName = "testChecksumAndPut0KFile.sh";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = absPath + testFileName;
		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		File localFile = new File(localFileName);
		localFile.createNewFile();

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		TransferOptions transferOptions = new TransferOptions();
		transferOptions.setComputeAndVerifyChecksumAfterTransfer(true);
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock.instance();
		transferControlBlock.setTransferOptions(transferOptions);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, transferControlBlock, null, false);
		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsFile, accessObjectFactory, irodsAccount);
	}

	@Test(expected = JargonException.class)
	public void testPutOverwriteUnknownCollection() throws Exception {
		// generate a local scratch file
		String testFileName = "testPutOverwriteFileNotInIRODS.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/Imnothererightnow/" + testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, true);
	}

	@Test(expected = JargonException.class)
	public void testPutOverwriteNoLocalFile() throws Exception {
		// generate a local scratch file
		String testFileName = "IdontExistMate.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/Imnothererightnow/" + testFileName);
		File localFile = new File(absPath + '/' + testFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, true);
	}

	/**
	 * put a file with no force, there will be no callback listener
	 *
	 * @throws Exception
	 */
	@Test(expected = OverwriteException.class)
	public void testPutNoOverwriteFileAlreadyPresentNoCallbackListener() throws Exception {
		// generate a local scratch file
		String testFileName = "testPutNoOverwriteFileAlreadyPresentNoCallbackListener.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		File localFile = new File(localFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, false);
		// second put to do test
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, null, null, false);
	}

	/**
	 * put a file with no force configured and a callback listener
	 *
	 * @throws Exception
	 */
	@Test(expected = OverwriteException.class)
	public void testPutNoOverwriteFileAlreadyPresentNoForceInOptionsNoCallbackListener() throws Exception {
		// generate a local scratch file
		String testFileName = "testPutNoOverwriteFileAlreadyPresentNoForceInOptionsNoCallbackListener.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 3);

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		File localFile = new File(localFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, false);
		// second put to do test
		TransferControlBlock tcb = accessObjectFactory.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.NO_FORCE);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, null, null, false);
	}

	/**
	 * put a file with ask listener configured and a callback listener that says
	 * don't overwrite
	 *
	 * @throws Exception
	 */
	@Test
	public void testPutNoOverwriteFileAlreadyPresentAskCallbackListenerSaysNoThisFile() throws Exception {
		int firstLength = 3;
		int secondLength = 5;
		// generate a local scratch file
		String testFileName = "testPutNoOverwriteFileAlreadyPresentAskCallbackListenerSaysNoThisFile.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, firstLength);

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		File localFile = new File(localFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, false);
		// second put to do test, update file to diff length to make sure is
		// skipped
		localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, secondLength);
		TransferControlBlock tcb = accessObjectFactory.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.ASK_CALLBACK_LISTENER);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();
		transferStatusCallbackListener.setForceOption(CallbackResponse.NO_THIS_FILE);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, tcb, transferStatusCallbackListener, false);

		Assert.assertEquals("should have skipped file and not overwritten", firstLength, destFile.length());
		Assert.assertEquals("transferControlBlock should not have been overwritten", ForceOption.ASK_CALLBACK_LISTENER,
				tcb.getTransferOptions().getForceOption());

	}

	/**
	 * put a file with ask listener configured and a callback listener that says
	 * don't overwrite for all files
	 *
	 * @throws Exception
	 */
	@Test
	public void testPutNoOverwriteFileAlreadyPresentAskCallbackListenerSaysNoAllFiles() throws Exception {
		int firstLength = 3;
		int secondLength = 5;
		// generate a local scratch file
		String testFileName = "testPutNoOverwriteFileAlreadyPresentAskCallbackListenerSaysNoAllFiles.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, firstLength);

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		File localFile = new File(localFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, false);
		// second put to do test, update file to diff length to make sure is
		// skipped
		localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, secondLength);
		TransferControlBlock tcb = accessObjectFactory.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.ASK_CALLBACK_LISTENER);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();
		transferStatusCallbackListener.setForceOption(CallbackResponse.NO_FOR_ALL);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, tcb, transferStatusCallbackListener, false);

		Assert.assertEquals("should have skipped file and not overwritten", firstLength, destFile.length());
		Assert.assertEquals("transferControlBlock should have been overwritten", ForceOption.NO_FORCE,
				tcb.getTransferOptions().getForceOption());

	}

	/**
	 * put a file with ask listener configured and a callback listener that says
	 * overwrite for this one file
	 *
	 * @throws Exception
	 */
	@Test
	public void testPutNoOverwriteFileAlreadyPresentAskCallbackListenerSaysYesThisFile() throws Exception {
		int firstLength = 3;
		int secondLength = 5;
		// generate a local scratch file
		String testFileName = "testPutNoOverwriteFileAlreadyPresentAskCallbackListenerSaysYesThisFile.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, firstLength);

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		File localFile = new File(localFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, false);

		localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, secondLength);
		TransferControlBlock tcb = accessObjectFactory.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.ASK_CALLBACK_LISTENER);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();
		transferStatusCallbackListener.setForceOption(CallbackResponse.YES_THIS_FILE);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, tcb, transferStatusCallbackListener, false);

		Assert.assertEquals("should have overwritten file", secondLength, destFile.length());
		Assert.assertEquals("transferControlBlock should not have been overwritten", ForceOption.ASK_CALLBACK_LISTENER,
				tcb.getTransferOptions().getForceOption());

	}

	/**
	 * put a file with ask listener configured and a callback listener that says
	 * overwrite for this one file
	 *
	 * @throws Exception
	 */
	@Test
	public void testPutNoOverwriteFileAlreadyPresentAskCallbackListenerSaysYesAllFiles() throws Exception {
		int firstLength = 3;
		int secondLength = 5;
		// generate a local scratch file
		String testFileName = "testPutNoOverwriteFileAlreadyPresentAskCallbackListenerSaysYesAllFiles.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, firstLength);

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		File localFile = new File(localFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, false);

		localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, secondLength);
		TransferControlBlock tcb = accessObjectFactory.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.ASK_CALLBACK_LISTENER);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();
		transferStatusCallbackListener.setForceOption(CallbackResponse.YES_FOR_ALL);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, tcb, transferStatusCallbackListener, false);

		Assert.assertEquals("should have overwritten file", secondLength, destFile.length());
		Assert.assertEquals("transferControlBlock should have been overwritten", ForceOption.USE_FORCE,
				tcb.getTransferOptions().getForceOption());

	}

	@Test
	public void testPutOverwriteFileAlreadyPresent() throws Exception {
		// generate a local scratch file
		String testFileName = "testPutOverwriteFileAlreadyPresent.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		File localFile = new File(localFileName);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, true);
		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsFile, accessObjectFactory, irodsAccount);
	}

	@Test
	public void testFindByCollectionPathAndDataName() throws Exception {
		// generate a local scratch file
		String testFileName = "testFindByCollectionPathAndDataName.dat";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 10);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		File sourceFile = new File(localFileName);
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);

		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		dto.putOperation(sourceFile, destFile, null, null);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		DataObject dataObject = dataObjectAO.findByCollectionNameAndDataName(targetIrodsCollection, testFileName);
		Assert.assertNotNull("null data object, was not found", dataObject);

	}

	/**
	 * Bug [#1139] Spaces at the begin or end of a data object name will cause an
	 * exception
	 *
	 * @throws Exception
	 */
	@Test
	public void testFindByAbsolutePathSpacesInProvidedPathSpacesInActualFileNameBug1139Leading() throws Exception {
		// generate a local scratch file
		String testFileName = " testFindByAbsolutePathSpacesInProvidedPathSpacesInActualFileNameBug1139Leading.dat";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 10);

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		File sourceFile = new File(localFileName);
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);

		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		dto.putOperation(sourceFile, destFile, null, null);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		String targetIrodsFileQueryName = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testFileName);
		DataObject dataObject = dataObjectAO.findByAbsolutePath(targetIrodsFileQueryName);
		Assert.assertNotNull("null data object, was not found", dataObject);

	}

	@Test
	public void testFindById() throws Exception {
		// generate a local scratch file
		String testFileName = "testFindById.dat";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		// now put the file
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);

		dto.putOperation(localFileName, targetIrodsFile, "", null, null);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		DataObject dataObject = dataObjectAO.findByAbsolutePath(targetIrodsFile);
		DataObject actual = dataObjectAO.findById(dataObject.getId());
		Assert.assertNotNull("null data object, was not found", actual);

	}

	@Test(expected = DataNotFoundException.class)
	public void testFindByIdNotFound() throws Exception {
		// generate a local scratch file

		// now put the file
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO.findById(999999999);

	}

	/**
	 * Test for https://github.com/DICE-UNC/jargon/issues/190
	 *
	 * @throws Exception
	 */
	@Test
	public void testFindByAbsolutePathPercentInNameBug190() throws Exception {
		// generate a local scratch file
		String testFileName = "testFindByAbsolutePathPercentInNameBug190.dat";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		String targetIrodsFileParentPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH + "/%");

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFile parentDir = accessObjectFactory.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsFileParentPath);
		parentDir.mkdirs();

		IRODSFile targetIrodsFile = accessObjectFactory.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(parentDir.getAbsolutePath(), testFileName);

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);

		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		dto.putOperation(localFileName, targetIrodsFile.getAbsolutePath(),
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		DataObject dataObject = dataObjectAO.findByAbsolutePath(targetIrodsFile.getAbsolutePath());
		Assert.assertNotNull("null data object, was not found", dataObject);

	}

	@Test
	public void testFindByAbsolutePath() throws Exception {
		// generate a local scratch file
		String testFileName = "testFindByAbsolutePath.dat";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsFile,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		DataObject dataObject = dataObjectAO.findByAbsolutePath(targetIrodsFile);
		Assert.assertNotNull("null data object, was not found", dataObject);

	}

	@Test(expected = JargonException.class)
	public void testFindByAbsolutePathWhenIsACollection() throws Exception {
		// generate a local scratch file

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		DataObject dataObject = dataObjectAO.findByAbsolutePath(targetIrodsFile);
		Assert.assertNotNull("null data object, was not found", dataObject);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testFindByAbsolutePathNullFileName() throws Exception {
		// generate a local scratch file
		String testFileName = null;

		// now put the file
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		dataObjectAO.findByAbsolutePath(testFileName);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testFindByAbsolutePathBlankFileName() throws Exception {
		// generate a local scratch file
		String testFileName = "";

		// now put the file
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		dataObjectAO.findByAbsolutePath(testFileName);

	}

	@Test
	public void instanceIRODSFileForPath() throws Exception {

		// generate a local scratch file
		String testFileName = "testGetIRODSFileForPath.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 10);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);

		dto.putOperation(localFileName, targetIrodsCollection, "", null, null);

		accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAO dataObjectAO = accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO.instanceIRODSFileForPath(targetIrodsCollection + '/' + testFileName);
		Assert.assertNotNull("irodsFile was null", irodsFile);
		Assert.assertTrue("this file did not exist", irodsFile.exists());
		Assert.assertTrue("was not a file", irodsFile.isFile());

	}

	@Test
	public void instanceIRODSFileForPathNotExists() throws Exception {

		// generate a local scratch file
		String testFileName = "idontexistForThisPath.csv";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAO dataObjectAO = accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO.instanceIRODSFileForPath(targetIrodsCollection + '/' + testFileName);
		Assert.assertNotNull("irodsFile was null", irodsFile);
		Assert.assertFalse("this file exists", irodsFile.exists());
	}

	@Test
	public final void testGet() throws Exception {

		String testFileName = "testGet.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		String getFileName = "testGetResult.txt";
		String getResultLocalPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File localFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dataTransferOperations.putOperation(localFileName, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO.instanceIRODSFileForPath(targetIrodsCollection + '/' + testFileName);

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile, null, null);

		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH + '/' + getFileName);
		assertionHelper.assertLocalScratchFileLengthEquals(IRODS_TEST_SUBDIR_PATH + '/' + getFileName, 100);

	}

	/**
	 * test for transfer get of file with parens and spaces in name gives file not
	 * found #1
	 *
	 * @throws Exception
	 */
	@Test
	public final void testGetWithParensBug1() throws Exception {

		String testFileName = "testGetWithParensBug1 (1).txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		String getFileName = "testGetWithParensBug1 (1) result ().txt";
		String getResultLocalPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File localFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dataTransferOperations.putOperation(localFileName, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO.instanceIRODSFileForPath(targetIrodsCollection + '/' + testFileName);

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile, null, null);

		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH + '/' + getFileName);
		assertionHelper.assertLocalScratchFileLengthEquals(IRODS_TEST_SUBDIR_PATH + '/' + getFileName, 100);

	}

	/**
	 * Do a get when the local file aready exists (should throw an error for
	 * overwrite)
	 *
	 * @throws Exception
	 */
	@Test(expected = OverwriteException.class)
	public final void testGetLocalFileAlreadyExists() throws Exception {

		String testFileName = "testGetLocalFileAlreadyExists.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		String getFileName = "testGetLocalFileAlreadyExistsResult.txt";
		String getResultLocalPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File localFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dataTransferOperations.putOperation(localFileName, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO.instanceIRODSFileForPath(targetIrodsCollection + '/' + testFileName);

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile, null, null);
		// now I know the local file exits, get again and see an overwrite
		// errror
		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile, null, null);

	}

	/**
	 * Do a get when the local file aready exists (should throw an error for
	 * overwrite)
	 *
	 * @throws Exception
	 */
	@Test
	public final void testGetLocalFileAskCallbackListenerGetAYes() throws Exception {

		String testFileName = "testGetLocalFileAskCallbackListenerGetAYes.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		String getFileName = "testGetLocalFileAskCallbackListenerGetAYesResult.txt";
		String getResultLocalPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File localFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dataTransferOperations.putOperation(localFileName, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO.instanceIRODSFileForPath(targetIrodsCollection + '/' + testFileName);

		TransferControlBlock transferControlBlock = irodsFileSystem.getIrodsSession()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		transferControlBlock.getTransferOptions().setForceOption(ForceOption.ASK_CALLBACK_LISTENER);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();
		transferStatusCallbackListener.setForceOption(CallbackResponse.YES_THIS_FILE);

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile, null, null);
		// now I know the local file exits, get again and it should overwrite
		// with no error
		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile, transferControlBlock, transferStatusCallbackListener);
		// check tomake sure transfer control block was not altered
		Assert.assertEquals("transfer control block should not have been altered", ForceOption.ASK_CALLBACK_LISTENER,
				transferControlBlock.getTransferOptions().getForceOption());
	}

	/**
	 * Do a get when the local file aready exists, ask the callback listener, and
	 * get a no for this file. This should not be in error
	 *
	 * @throws Exception
	 */
	@Test
	public final void testGetLocalFileAskCallbackListenerGetANo() throws Exception {

		int firstLength = 13;
		int secondLength = 32;
		String sourceFileName = "testGetLocalFileAskCallbackListenerGetANo.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String sourceLocalFileAbsolutePath = FileGenerator.generateFileOfFixedLengthGivenName(absPath, sourceFileName,
				firstLength);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		String getFileName = "testGetLocalFileAskCallbackListenerGetANoResult.txt";
		String getResultLocalPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File getResultLocalFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dataTransferOperations.putOperation(sourceLocalFileAbsolutePath, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO.instanceIRODSFileForPath(targetIrodsCollection + '/' + sourceFileName);

		TransferControlBlock transferControlBlock = irodsFileSystem.getIrodsSession()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		transferControlBlock.getTransferOptions().setForceOption(ForceOption.ASK_CALLBACK_LISTENER);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();
		transferStatusCallbackListener.setForceOption(CallbackResponse.NO_THIS_FILE);

		dataObjectAO.getDataObjectFromIrods(irodsFile, getResultLocalFile, transferControlBlock,
				transferStatusCallbackListener);
		// now I know the local file exits, get again and it should overwrite
		// with no error, check it was truly overwritten by making source file a
		// different length and checking

		irodsFile.delete();
		sourceLocalFileAbsolutePath = FileGenerator.generateFileOfFixedLengthGivenName(absPath, sourceFileName,
				secondLength);
		dataTransferOperations.putOperation(sourceLocalFileAbsolutePath, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		dataObjectAO.getDataObjectFromIrods(irodsFile, getResultLocalFile, transferControlBlock,
				transferStatusCallbackListener);
		// check tomake sure transfer control block was not altered
		Assert.assertEquals("transfer control block should not have been alteredl", ForceOption.ASK_CALLBACK_LISTENER,
				transferControlBlock.getTransferOptions().getForceOption());
		Assert.assertEquals("file should not be the new length", firstLength, getResultLocalFile.length());
	}

	/**
	 * Do a get with an overwrite situation, callback listener set to be asked, but
	 * no callback listener provided (this is an error state)
	 *
	 * @throws Exception
	 */
	@Test(expected = OverwriteException.class)
	public final void testGetLocalFileAskCallbackListenerNoCallbackListenerSet() throws Exception {

		int firstLength = 13;
		int secondLength = 32;
		String sourceFileName = "testGetLocalFileAskCallbackListenerNoCallbackListenerSet.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String sourceLocalFileAbsolutePath = FileGenerator.generateFileOfFixedLengthGivenName(absPath, sourceFileName,
				firstLength);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		String getFileName = "testGetLocalFileAskCallbackListenerNoCallbackListenerSetResult.txt";
		String getResultLocalPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File getResultLocalFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dataTransferOperations.putOperation(sourceLocalFileAbsolutePath, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO.instanceIRODSFileForPath(targetIrodsCollection + '/' + sourceFileName);

		TransferControlBlock transferControlBlock = irodsFileSystem.getIrodsSession()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		transferControlBlock.getTransferOptions().setForceOption(ForceOption.ASK_CALLBACK_LISTENER);

		dataObjectAO.getDataObjectFromIrods(irodsFile, getResultLocalFile, transferControlBlock, null);
		// now I know the local file exits, get again and it should overwrite
		// with no error, check it was truly overwritten by making source file a
		// different length and checking

		irodsFile.delete();
		sourceLocalFileAbsolutePath = FileGenerator.generateFileOfFixedLengthGivenName(absPath, sourceFileName,
				secondLength);
		dataTransferOperations.putOperation(sourceLocalFileAbsolutePath, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		dataObjectAO.getDataObjectFromIrods(irodsFile, getResultLocalFile, transferControlBlock, null);
		// expect an overwrite exception
	}

	/**
	 * Do a get when the local file aready exists, ask the callback listener, and
	 * get a no all for this file. This should not be in error, and should update
	 * the transfer options
	 *
	 * @throws Exception
	 */
	@Test
	public final void testGetLocalFileAskCallbackListenerGetANoAll() throws Exception {

		int firstLength = 3;
		int secondLength = 16;
		String sourceFileName = "testGetLocalFileAskCallbackListenerGetANoAll.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String sourceLocalFileAbsolutePath = FileGenerator.generateFileOfFixedLengthGivenName(absPath, sourceFileName,
				firstLength);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		String getFileName = "testGetLocalFileAskCallbackListenerGetANoAllResult.txt";
		String getResultLocalPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File getResultLocalFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dataTransferOperations.putOperation(sourceLocalFileAbsolutePath, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO.instanceIRODSFileForPath(targetIrodsCollection + '/' + sourceFileName);

		TransferControlBlock transferControlBlock = irodsFileSystem.getIrodsSession()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		transferControlBlock.getTransferOptions().setForceOption(ForceOption.ASK_CALLBACK_LISTENER);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();
		transferStatusCallbackListener.setForceOption(CallbackResponse.NO_FOR_ALL);

		dataObjectAO.getDataObjectFromIrods(irodsFile, getResultLocalFile, transferControlBlock,
				transferStatusCallbackListener);
		// now I know the local file exits, get again and it should overwrite
		// with no error, check it was truly overwritten by making source file a
		// different length and checking

		irodsFile.delete();
		sourceLocalFileAbsolutePath = FileGenerator.generateFileOfFixedLengthGivenName(absPath, sourceFileName,
				secondLength);
		dataTransferOperations.putOperation(sourceLocalFileAbsolutePath, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		dataObjectAO.getDataObjectFromIrods(irodsFile, getResultLocalFile, transferControlBlock,
				transferStatusCallbackListener);
		// check tomake sure transfer control block was not altered
		Assert.assertEquals("transfer control block should have been altered to no for all", ForceOption.NO_FORCE,
				transferControlBlock.getTransferOptions().getForceOption());
		Assert.assertEquals("file should not be the new length", firstLength, getResultLocalFile.length());
	}

	/**
	 * Do a get when the local file aready exists (should throw an error for
	 * overwrite)
	 *
	 * @throws Exception
	 */
	@Test
	public final void testGetLocalFileAskCallbackListenerGetAYesAll() throws Exception {

		int secondLength = 13;
		String sourceFileName = "testGetLocalFileAskCallbackListenerGetAYesAll.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String sourceLocalFileAbsolutePath = FileGenerator.generateFileOfFixedLengthGivenName(absPath, sourceFileName,
				1);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		String getFileName = "testGetLocalFileAskCallbackListenerGetAYesAllResult.txt";
		String getResultLocalPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File getResultLocalFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dataTransferOperations.putOperation(sourceLocalFileAbsolutePath, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO.instanceIRODSFileForPath(targetIrodsCollection + '/' + sourceFileName);

		TransferControlBlock transferControlBlock = irodsFileSystem.getIrodsSession()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		transferControlBlock.getTransferOptions().setForceOption(ForceOption.ASK_CALLBACK_LISTENER);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();
		transferStatusCallbackListener.setForceOption(CallbackResponse.YES_FOR_ALL);

		dataObjectAO.getDataObjectFromIrods(irodsFile, getResultLocalFile, transferControlBlock,
				transferStatusCallbackListener);
		// now I know the local file exits, get again and it should overwrite
		// with no error, check it was truly overwritten by making source file a
		// different length and checking

		irodsFile.delete();
		sourceLocalFileAbsolutePath = FileGenerator.generateFileOfFixedLengthGivenName(absPath, sourceFileName,
				secondLength);
		dataTransferOperations.putOperation(sourceLocalFileAbsolutePath, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		dataObjectAO.getDataObjectFromIrods(irodsFile, getResultLocalFile, transferControlBlock,
				transferStatusCallbackListener);
		// check tomake sure transfer control block was not altered
		Assert.assertEquals("transfer control block should have been altered to yes for all", ForceOption.USE_FORCE,
				transferControlBlock.getTransferOptions().getForceOption());
		Assert.assertEquals("file should now be the new length", secondLength, getResultLocalFile.length());

	}

	/**
	 * Do a get when the local file aready exists (should throw an error for
	 * overwrite)
	 *
	 * @throws Exception
	 */
	@Test(expected = OverwriteException.class)
	public final void testGetLocalFileAlreadyExistsTransferOptionNoForce() throws Exception {

		String testFileName = "testGetLocalFileAlreadyExistsTransferOptionNoForce.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		String getFileName = "testGetLocalFileAlreadyExistsTransferOptionNoForceResult.txt";
		String getResultLocalPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File localFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		TransferControlBlock transferControlBlock = irodsFileSystem.getIrodsSession()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		transferControlBlock.getTransferOptions().setForceOption(ForceOption.NO_FORCE);

		DataTransferOperations dataTransferOperations = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dataTransferOperations.putOperation(localFileName, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO.instanceIRODSFileForPath(targetIrodsCollection + '/' + testFileName);

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile, transferControlBlock, null);
		// now I know the local file exits, get again and see an overwrite
		// errror
		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile, transferControlBlock, null);

	}

	/**
	 * Do a get when the local file aready exists, but the force option is set
	 *
	 * @throws Exception
	 */
	@Test
	public final void testGetLocalFileAlreadyExistsTransferOptionForce() throws Exception {

		String testFileName = "testGetLocalFileAlreadyExistsTransferOptionForce.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		String getFileName = "testGetLocalFileAlreadyExistsTransferOptionForceResult.txt";
		String getResultLocalPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File localFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		TransferControlBlock transferControlBlock = irodsFileSystem.getIrodsSession()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		transferControlBlock.getTransferOptions().setForceOption(ForceOption.USE_FORCE);

		DataTransferOperations dataTransferOperations = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dataTransferOperations.putOperation(localFileName, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO.instanceIRODSFileForPath(targetIrodsCollection + '/' + testFileName);

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile, null, null);
		// now I know the local file exits, get again and see an overwrite
		// errror
		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile, transferControlBlock, null);

	}

	/**
	 * Get a data object where the source name and the target name are different. It
	 * should be retrieved as the target name.
	 *
	 * @throws Exception
	 */
	@Test
	public final void testGetProvidingDifferentSourceAndTargetDataNames() throws Exception {

		String sourceFileName = "testGetProvidingDifferentSourceAndTargetDataNames.txt";
		String targetFileName = "target file for testGetProvidingDifferentSourceAndTargetDataNames.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, sourceFileName, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		String getResultLocalPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ targetFileName;
		File localFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dataTransferOperations.putOperation(localFileName, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO.instanceIRODSFileForPath(targetIrodsCollection + '/' + sourceFileName);

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile, null, null);

		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH + '/' + targetFileName);
		assertionHelper.assertLocalScratchFileLengthEquals(IRODS_TEST_SUBDIR_PATH + '/' + targetFileName, 100);

	}

	@Test
	public final void testGetExecutable() throws Exception {

		String testFileName = "testGetExecutable.sh";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		File localFile = new File(localFileName);
		localFile.setExecutable(true);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		String getFileName = "testGetExecutableResult.sh";
		String getResultLocalPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		localFile = new File(getResultLocalPath);
		localFile.setExecutable(true);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dataTransferOperations.putOperation(localFileName, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO.instanceIRODSFileForPath(targetIrodsCollection + '/' + testFileName);

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile, null, null);

		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH + '/' + getFileName);
		assertionHelper.assertLocalScratchFileLengthEquals(IRODS_TEST_SUBDIR_PATH + '/' + getFileName, 100);
		Assert.assertTrue("local file should be executable", localFile.canExecute());

	}

	/**
	 * Seems to now really mean much especially on windows running as root
	 *
	 * @throws Exception
	 */
	@Ignore
	public final void testGetNotExecutable() throws Exception {

		String testFileName = "testGetNotExecutable.sh";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		File localFile = new File(localFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		String getFileName = "testGetNotExecutableResult.sh";
		String getResultLocalPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		localFile = new File(getResultLocalPath);

		localFile.setExecutable(false);

		if (localFile.canExecute()) {
			return;
		}

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dataTransferOperations.putOperation(localFileName, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO.instanceIRODSFileForPath(targetIrodsCollection + '/' + testFileName);

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile, null, null);

		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH + '/' + getFileName);
		assertionHelper.assertLocalScratchFileLengthEquals(IRODS_TEST_SUBDIR_PATH + '/' + getFileName, 100);
		Assert.assertFalse("local file should not be executable", localFile.canExecute());

	}

	@Ignore
	public final void testGetWithIntraFileCallbacks() throws Exception {

		int testFileLen = 2099999;
		// generate a local scratch file
		String testFileName = "testGetWithIntraFileCallbacks.doc";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, testFileLen);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		String getFileName = "testGetWithIntraFileCallbacksResult.doc";
		String getResultLocalPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File localFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dataTransferOperations.putOperation(localFileName, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO.instanceIRODSFileForPath(targetIrodsCollection + '/' + testFileName);

		TransferOptions transferOptions = new TransferOptions();
		transferOptions.setIntraFileStatusCallbacks(true);
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock.instance();
		transferControlBlock.setTransferOptions(transferOptions);

		TestingStatusCallbackListener transferStatusCallbackListener = new TestingStatusCallbackListener();

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile, transferControlBlock, transferStatusCallbackListener);

		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH + '/' + getFileName);
		assertionHelper.assertLocalScratchFileLengthEquals(IRODS_TEST_SUBDIR_PATH + '/' + getFileName, testFileLen);

		Assert.assertTrue("did not get intra-file callbacks",
				transferStatusCallbackListener.getNumberIntraFileCallbacks() > 0);
		Assert.assertTrue("did not get any byte count from intra-file callbacks",
				transferStatusCallbackListener.getBytesReportedIntraFileCallbacks() > 0);
		Assert.assertFalse("accumulated more bytes than file size",
				transferStatusCallbackListener.getBytesReportedIntraFileCallbacks() > testFileLen);

	}

	@Test
	public final void testGetParallelWithIntraFileCallbacks() throws Exception {

		if (!testingPropertiesHelper.isTestParallelTransfer(testingProperties)) {
			return;
		}

		int testFileLen = 200 * 1024 * 1024;

		// generate a local scratch file
		String testFileName = "testGetParallelWithIntraFileCallbacks.doc";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, testFileLen);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		String getFileName = "testGetParallelWithIntraFileCallbacksResult.doc";
		String getResultLocalPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File localFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dataTransferOperations.putOperation(localFileName, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO.instanceIRODSFileForPath(targetIrodsCollection + '/' + testFileName);

		TransferOptions transferOptions = new TransferOptions();
		transferOptions.setIntraFileStatusCallbacks(true);
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock.instance();
		transferControlBlock.setTransferOptions(transferOptions);

		TestingStatusCallbackListener transferStatusCallbackListener = new TestingStatusCallbackListener();

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile, transferControlBlock, transferStatusCallbackListener);

		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH + '/' + getFileName);
		assertionHelper.assertLocalScratchFileLengthEquals(IRODS_TEST_SUBDIR_PATH + '/' + getFileName, testFileLen);

		Assert.assertTrue("did not get intra-file callbacks",
				transferStatusCallbackListener.getNumberIntraFileCallbacks() > 0);
		Assert.assertTrue("did not get any byte count from intra-file callbacks",
				transferStatusCallbackListener.getBytesReportedIntraFileCallbacks() > 0);
		Assert.assertFalse("accumulated more bytes than file size",
				transferStatusCallbackListener.getBytesReportedIntraFileCallbacks() > testFileLen);

	}

	@Test
	public final void testGetFileGTParallelMaxNoParallelInOptions() throws Exception {

		int testFileLen = 33 * 1024 * 1024;
		// generate a local scratch file
		String testFileName = "testGetFileGTParallelMaxNoParallelInOptions.doc";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, testFileLen);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		String getFileName = "returnedTestGetFileGTParallelMaxNoParallelInOptions.doc";
		String getResultLocalPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + "/return") + getFileName;
		File localFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		TestingStatusCallbackListener transferStatusCallbackListener = new TestingStatusCallbackListener();
		TransferControlBlock transferControlBlock = irodsFileSystem.getIrodsSession()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		transferControlBlock.getTransferOptions().setUseParallelTransfer(false);

		DataTransferOperations dataTransferOperations = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dataTransferOperations.putOperation(localFileName, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				transferStatusCallbackListener, transferControlBlock);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO.instanceIRODSFileForPath(targetIrodsCollection + '/' + testFileName);

		transferControlBlock = irodsFileSystem.getIrodsSession()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		transferControlBlock.getTransferOptions().setUseParallelTransfer(false);
		transferControlBlock.getTransferOptions().setIntraFileStatusCallbacks(true);

		transferStatusCallbackListener = new TestingStatusCallbackListener();

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile, transferControlBlock, transferStatusCallbackListener);

		Assert.assertTrue("local file that was returned does not exist", localFile.exists());
		Assert.assertEquals("local file wrong length", testFileLen, localFile.length());

	}

	@Test
	public final void testGetGiveLocalTargetAsCollection() throws Exception {

		// generate a local scratch file
		String testFileName = "testGetGiveLocalTargetAsCollection.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		String getResultLocalPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		File localFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		DataTransferOperations dataTransferOperations = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dataTransferOperations.putOperation(localFileName, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO.instanceIRODSFileForPath(targetIrodsCollection + '/' + testFileName);

		// clear the file from scratch

		File scratchLocalFile = new File(localFileName);
		boolean success = scratchLocalFile.delete();

		TransferOptions transferOptions = new TransferOptions();
		transferOptions.setIntraFileStatusCallbacks(false);
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock.instance();
		transferControlBlock.setTransferOptions(transferOptions);

		TestingStatusCallbackListener transferStatusCallbackListener = new TestingStatusCallbackListener();

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile, transferControlBlock, transferStatusCallbackListener);

		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		assertionHelper.assertLocalScratchFileLengthEquals(IRODS_TEST_SUBDIR_PATH + '/' + testFileName, 100);
		Assert.assertTrue("delete did not report success in response", success);

		Assert.assertFalse("got intra-file callbacks",
				transferStatusCallbackListener.getNumberIntraFileCallbacks() > 0);
	}

	@Test(expected = DataNotFoundException.class)
	public final void testGetSpecifyingDifferentResource() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		accessObjectFactory.getIRODSFileFactory(irodsAccount);
		if (!accessObjectFactory.getEnvironmentalInfoAO(irodsAccount).getIRODSServerProperties().isAtLeastIrods420()) {
			return;
		}

		// generate a local scratch file
		String testFileName = "testGetSpecifyingDifferentResource.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		String getFileName = "testGetSpecifyingDifferentResourceReturnedFile.txt";
		String getResultLocalPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File localFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dataTransferOperations.putOperation(localFileName, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO.instanceIRODSFileForPath(targetIrodsCollection + '/' + testFileName);
		irodsFile.setResource(testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY));

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile, null, null);
	}

	@Test(expected = FileNotFoundException.class)
	public final void testFindMetadataValuesForDataObjectNotFound() throws Exception {
		String testFileName = "testFindMetadataValuesForDataObjectNotFound.dat";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		dataObjectAO.findMetadataValuesForDataObject(targetIrodsCollection + "/" + testFileName);
	}

	@Test
	public final void testFindMetadataValuesForDataObjectById() throws Exception {
		String testFileName = "testFindMetadataValuesForDataObjectById.dat";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String dataObjectAbsPath = targetIrodsCollection + '/' + testFileName;

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		// initialize the AVU data
		String expectedAttribName = "testmdattrib1";
		String expectedAttribValue = "testmdvalue1";
		String expectedAttribUnits = "test1mdunits";

		AvuData avuData = AvuData.instance(expectedAttribName, expectedAttribValue, expectedAttribUnits);
		DataObjectAO dataObjectAO = accessObjectFactory.getDataObjectAO(irodsAccount);
		dataObjectAO.deleteAVUMetadata(dataObjectAbsPath, avuData);
		dataObjectAO.addAVUMetadata(dataObjectAbsPath, avuData);

		List<MetaDataAndDomainData> result = dataObjectAO
				.findMetadataValuesForDataObject(targetIrodsCollection + "/" + testFileName);
		Assert.assertFalse("no query result returned", result.isEmpty());

		MetaDataAndDomainData expected = result.get(0);

		MetaDataAndDomainData actual = dataObjectAO
				.findMetadataValueForDataObjectById(targetIrodsCollection + "/" + testFileName, expected.getAvuId());
		Assert.assertEquals("did not find avu", expected.getAvuAttribute(), actual.getAvuAttribute());

	}

	@Test
	public final void testFindMetadataValuesForDataObject() throws Exception {
		String testFileName = "testFindMetadataValuesForDataObject.dat";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String dataObjectAbsPath = targetIrodsCollection + '/' + testFileName;

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		// initialize the AVU data
		String expectedAttribName = "testmdattrib1";
		String expectedAttribValue = "testmdvalue1";
		String expectedAttribUnits = "test1mdunits";

		AvuData avuData = AvuData.instance(expectedAttribName, expectedAttribValue, expectedAttribUnits);
		DataObjectAO dataObjectAO = accessObjectFactory.getDataObjectAO(irodsAccount);
		dataObjectAO.deleteAVUMetadata(dataObjectAbsPath, avuData);
		dataObjectAO.addAVUMetadata(dataObjectAbsPath, avuData);

		List<MetaDataAndDomainData> result = dataObjectAO
				.findMetadataValuesForDataObject(targetIrodsCollection + "/" + testFileName);
		Assert.assertFalse("no query result returned", result.isEmpty());
	}

	/**
	 * Bug: metadata query on replicated data object repeats metadata #178
	 * https://github.com/DICE-UNC/jargon/issues/178
	 *
	 * @throws Exception
	 */
	@Test
	public final void testFindMetadataValuesForDataObjectBug178() throws Exception {
		String testFileName = "testFindMetadataValuesForDataObjectBug178.dat";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String dataObjectAbsPath = targetIrodsCollection + '/' + testFileName;

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		// initialize the AVU data
		String expectedAttribName = "testFindMetadataValuesForDataObjectBug178attrib1";
		String expectedAttribValue = "testFindMetadataValuesForDataObjectBug178value1";
		String expectedAttribUnits = "test1mdunits";

		AvuData avuData = AvuData.instance(expectedAttribName, expectedAttribValue, expectedAttribUnits);
		DataObjectAO dataObjectAO = accessObjectFactory.getDataObjectAO(irodsAccount);
		dataObjectAO.deleteAVUMetadata(dataObjectAbsPath, avuData);
		dataObjectAO.addAVUMetadata(dataObjectAbsPath, avuData);

		// now replicate

		dataObjectAO.replicateIrodsDataObject(dataObjectAbsPath,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY));

		List<MetaDataAndDomainData> result = dataObjectAO
				.findMetadataValuesForDataObject(targetIrodsCollection + "/" + testFileName);
		Assert.assertFalse("no query result returned", result.isEmpty());
		Assert.assertEquals(1, result.size());
	}

	/**
	 * Bug: metadata query on replicated data object repeats metadata #178
	 * https://github.com/DICE-UNC/jargon/issues/178
	 *
	 * @throws Exception
	 */
	@Test
	public final void testFindMetadataValuesForDataObjectDemoRescBug178() throws Exception {
		String testFileName = "testFindMetadataValuesForDataObjectDemoRescBug178.dat";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String dataObjectAbsPath = targetIrodsCollection + '/' + testFileName;

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		// initialize the AVU data
		String expectedAttribName = "testFindMetadataValuesForDataObjectDemoRescBug178attrib1";
		String expectedAttribValue = "testFindMetadataValuesForDataObjectDemoRescBug178value1";
		String expectedAttribUnits = "test1mdunits";

		AvuData avuData = AvuData.instance(expectedAttribName, expectedAttribValue, expectedAttribUnits);
		DataObjectAO dataObjectAO = accessObjectFactory.getDataObjectAO(irodsAccount);
		dataObjectAO.deleteAVUMetadata(dataObjectAbsPath, avuData);
		dataObjectAO.addAVUMetadata(dataObjectAbsPath, avuData);

		// now replicate

		dataObjectAO.replicateIrodsDataObject(dataObjectAbsPath, "demoResc");

		List<MetaDataAndDomainData> result = dataObjectAO
				.findMetadataValuesForDataObject(targetIrodsCollection + "/" + testFileName);
		Assert.assertFalse("no query result returned", result.isEmpty());
		Assert.assertEquals(1, result.size());
	}

	@Test
	public final void testListMetadataValuesForDataObject() throws Exception {
		String testFileName = "testListMetadataValuesForDataObject.dat";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String dataObjectAbsPath = targetIrodsCollection + '/' + testFileName;

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		// initialize the AVU data
		String expectedAttribName = "testmdattrib1";
		String expectedAttribValue = "testmdvalue1";
		String expectedAttribUnits = "test1mdunits";

		AvuData avuData = AvuData.instance(expectedAttribName, expectedAttribValue, expectedAttribUnits);
		DataObjectAO dataObjectAO = accessObjectFactory.getDataObjectAO(irodsAccount);
		dataObjectAO.deleteAVUMetadata(dataObjectAbsPath, avuData);
		dataObjectAO.addAVUMetadata(dataObjectAbsPath, avuData);

		List<AVUQueryElement> queryElements = new ArrayList<>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, expectedAttribName));

		List<MetaDataAndDomainData> result = dataObjectAO.findMetadataValuesForDataObjectUsingAVUQuery(queryElements,
				targetIrodsCollection, testFileName);
		Assert.assertFalse("no query result returned", result.isEmpty());
	}

	/**
	 * Test for metadata query on replicated data object repeats metadata #178
	 * https://github.com/DICE-UNC/jargon/issues/178
	 *
	 * @throws Exception
	 */
	@Test
	public final void testListMetadataValuesForReplicatedDataObjectBug178() throws Exception {
		String testFileName = "testListMetadataValuesForReplicatedDataObjectBug178.dat";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String dataObjectAbsPath = targetIrodsCollection + '/' + testFileName;

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		// initialize the AVU data
		String expectedAttribName = "testListMetadataValuesForReplicatedDataObjectBug178";
		String expectedAttribValue = "testListMetadataValuesForReplicatedDataObjectBug178";
		String expectedAttribUnits = "";

		AvuData avuData = AvuData.instance(expectedAttribName, expectedAttribValue, expectedAttribUnits);
		DataObjectAO dataObjectAO = accessObjectFactory.getDataObjectAO(irodsAccount);
		dataObjectAO.deleteAVUMetadata(dataObjectAbsPath, avuData);
		dataObjectAO.addAVUMetadata(dataObjectAbsPath, avuData);

		// replicate
		dataObjectAO.replicateIrodsDataObject(dataObjectAbsPath,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY));

		List<AVUQueryElement> queryElements = new ArrayList<>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, expectedAttribName));

		List<MetaDataAndDomainData> result = dataObjectAO.findMetadataValuesForDataObjectUsingAVUQuery(queryElements,
				targetIrodsCollection, testFileName);
		Assert.assertFalse("no query result returned", result.isEmpty());
		Assert.assertEquals("should only be one avu result", 1, result.size());
	}

	@Test
	public final void testListMetadataValuesForReplicatedDataObjectBug178ReplFirst() throws Exception {
		String testFileName = "testListMetadataValuesForReplicatedDataObjectBug178ReplFirst.dat";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String dataObjectAbsPath = targetIrodsCollection + '/' + testFileName;

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		DataObjectAO dataObjectAO = accessObjectFactory.getDataObjectAO(irodsAccount);

		// initialize the AVU data
		String expectedAttribName = "testListMetadataValuesForReplicatedDataObjectBug178ReplFirst";
		String expectedAttribValue = "testListMetadataValuesForReplicatedDataObjectBug178ReplFirst";
		String expectedAttribUnits = "";

		// replicate
		dataObjectAO.replicateIrodsDataObject(dataObjectAbsPath,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY));

		AvuData avuData = AvuData.instance(expectedAttribName, expectedAttribValue, expectedAttribUnits);

		dataObjectAO.deleteAVUMetadata(dataObjectAbsPath, avuData);
		dataObjectAO.addAVUMetadata(dataObjectAbsPath, avuData);

		List<AVUQueryElement> queryElements = new ArrayList<>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, expectedAttribName));

		List<MetaDataAndDomainData> result = dataObjectAO.findMetadataValuesForDataObjectUsingAVUQuery(queryElements,
				targetIrodsCollection, testFileName);
		Assert.assertFalse("no query result returned", result.isEmpty());
		Assert.assertEquals("should only be one avu result", 1, result.size());
	}

	@Test
	public final void testListMetadataValuesForDataObjectAsFile() throws Exception {
		String testFileName = "testListMetadataValuesForDataObjectAsFile.dat";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		// put scratch file into irods in the right place on the first resource

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);

		DataTransferOperations transfer = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		transfer.putOperation(new File(localFileName), targetIrodsFile, null, null);

		// initialize the AVU data
		String expectedAttribName = "testmdattrib1file";
		String expectedAttribValue = "testmdvalue1file";
		String expectedAttribUnits = "test1mdunitsfile";
		AvuData avuData = AvuData.instance(expectedAttribName, expectedAttribValue, expectedAttribUnits);

		dataObjectAO.deleteAVUMetadata(targetIrodsFile.getAbsolutePath(), avuData);

		dataObjectAO.addAVUMetadata(targetIrodsFile.getAbsolutePath(), avuData);

		List<AVUQueryElement> queryElements = new ArrayList<>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, expectedAttribName));

		List<MetaDataAndDomainData> result = dataObjectAO.findMetadataValuesForDataObject(targetIrodsFile);

		Assert.assertFalse("no query result returned", result.isEmpty());
	}

	@Test
	public final void testFindMetadataValuesByMetadataQuery() throws Exception {
		String testFileName = "testFindMetadataValuesByMetadataQueryFile.csv";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 10);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		// put scratch file into irods in the right place on the first resource

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String dataObjectAbsPath = targetIrodsCollection + '/' + testFileName;

		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsCollection, irodsAccount.getDefaultStorageResource(), null, null);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		// initialize the AVU data
		String expectedAttribName = "testfbmdattrib1";
		String expectedAttribValue = "testfbmdvalue1";
		String expectedAttribUnits = "test1fbmdunits";

		AvuData avuData = AvuData.instance(expectedAttribName, expectedAttribValue, expectedAttribUnits);

		dataObjectAO.deleteAVUMetadata(dataObjectAbsPath, avuData);
		dataObjectAO.addAVUMetadata(dataObjectAbsPath, avuData);

		List<AVUQueryElement> queryElements = new ArrayList<>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, expectedAttribName));

		List<MetaDataAndDomainData> result = dataObjectAO.findMetadataValuesByMetadataQuery(queryElements);
		Assert.assertFalse("no query result returned", result.isEmpty());
	}

	/**
	 * Add 2 avu's that are the same except the attrib value is upper case in the
	 * second. A case insensiitive query should return both
	 *
	 * @throws Exception
	 */
	@Test
	public final void testFindMetadataValuesByMetadataQueryCaseInsensitive() throws Exception {
		String testFileName = "testFindMetadataValuesByMetadataQuery.csv";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 10);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		// put scratch file into irods in the right place on the first resource

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSFile testFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection);
		testFile.delete();
		testFile.mkdirs();

		String dataObjectAbsPath = targetIrodsCollection + '/' + testFileName;

		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);

		if (!dto.getIRODSServerProperties().isSupportsCaseInsensitiveQueries()) {
			return;
		}

		dto.putOperation(localFileName, targetIrodsCollection, irodsAccount.getDefaultStorageResource(), null, null);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		// initialize the AVU data
		String expectedAttribName = "testfbmdattrib1";
		String expectedAttribValue = "testfbmdvalue1";
		String expectedAttribUnits = "test1fbmdunits";

		AvuData avuData = AvuData.instance(expectedAttribName, expectedAttribValue, expectedAttribUnits);

		dataObjectAO.deleteAVUMetadata(dataObjectAbsPath, avuData);
		dataObjectAO.addAVUMetadata(dataObjectAbsPath, avuData);

		expectedAttribValue = expectedAttribValue.toUpperCase();

		avuData = AvuData.instance(expectedAttribName, expectedAttribValue, expectedAttribUnits);

		dataObjectAO.deleteAVUMetadata(dataObjectAbsPath, avuData);
		dataObjectAO.addAVUMetadata(dataObjectAbsPath, avuData);

		List<AVUQueryElement> queryElements = new ArrayList<>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, expectedAttribName));

		List<MetaDataAndDomainData> result = dataObjectAO.findMetadataValuesByMetadataQuery(queryElements, 0, true);
		Assert.assertFalse("no query result returned", result.isEmpty());
		// Assert.assertTrue("should be 2 or more results", result.size() > 2);
	}

	@Test
	public void testFindDomainByMetadataQuery() throws Exception {

		String testFileName = "testFindDomainByMetadataQuery.dat";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 10);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		// put scratch file into irods in the right place on the first resource

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String dataObjectAbsPath = targetIrodsCollection + '/' + testFileName;

		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsCollection, irodsAccount.getDefaultStorageResource(), null, null);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		// initialize the AVU data
		String expectedAttribName = "testfbmdattrib1";
		String expectedAttribValue = "testfbmdvalue1";
		String expectedAttribUnits = "test1fbmdunits";

		AvuData avuData = AvuData.instance(expectedAttribName, expectedAttribValue, expectedAttribUnits);

		dataObjectAO.deleteAVUMetadata(dataObjectAbsPath, avuData);
		dataObjectAO.addAVUMetadata(dataObjectAbsPath, avuData);

		List<AVUQueryElement> queryElements = new ArrayList<>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, expectedAttribName));

		List<DataObject> result = dataObjectAO.findDomainByMetadataQuery(queryElements);
		Assert.assertFalse("no query result returned", result.isEmpty());
		Assert.assertEquals(testFileName, result.get(0).getDataName());
	}

	@Test
	public void testFindDomainByMetadataQueryCaseInsensitive() throws Exception {

		String testFileName = "testFindDomainByMetadataQueryCaseInsensitive.dat";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 10);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		// put scratch file into irods in the right place on the first resource

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSFile targetFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		targetFile.delete();
		targetFile.mkdirs();
		String dataObjectAbsPath = targetIrodsCollection + '/' + testFileName;

		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);

		if (!dto.getIRODSServerProperties().isSupportsCaseInsensitiveQueries()) {
			return;
		}

		dto.putOperation(localFileName, targetIrodsCollection, irodsAccount.getDefaultStorageResource(), null, null);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		// initialize the AVU data
		String expectedAttribName = "testfbmdattrib1";
		String expectedAttribValue = "testfbmdvalue1";
		String expectedAttribUnits = "test1fbmdunits";

		AvuData avuData = AvuData.instance(expectedAttribName, expectedAttribValue, expectedAttribUnits);

		dataObjectAO.deleteAVUMetadata(dataObjectAbsPath, avuData);
		dataObjectAO.addAVUMetadata(dataObjectAbsPath, avuData);

		List<AVUQueryElement> queryElements = new ArrayList<>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, expectedAttribName));

		List<DataObject> result = dataObjectAO.findDomainByMetadataQuery(queryElements, 0, true);
		Assert.assertFalse("no query result returned", result.isEmpty());
		Assert.assertEquals(testFileName, result.get(0).getDataName());
	}

	@Test
	public final void testReplicateAsynch() throws Exception {
		// generate a local scratch file

		String testFileName = "testReplicateAsynch.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);
		irodsFile.delete();

		irodsFile.setResource(testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY));
		File localFile = new File(fileNameOrig);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);

		dataObjectAO.putLocalDataObjectToIRODS(localFile, irodsFile, true);

		dataObjectAO.replicateIrodsDataObjectAsynchronously(targetIrodsCollection, testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY), 1);

		// I won't wait, just make sure it runs without error
	}

	@Test
	public final void testReplicate() throws Exception {
		// generate a local scratch file

		String testFileName = "testReplicate1.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);
		irodsFile.delete();

		irodsFile.setResource(testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY));
		File localFile = new File(fileNameOrig);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);

		dataObjectAO.putLocalDataObjectToIRODS(localFile, irodsFile, true);

		dataObjectAO.replicateIrodsDataObject(targetIrodsCollection + '/' + testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY));

		List<Resource> resources = dataObjectAO.listFileResources(targetIrodsCollection + "/" + testFileName);
		Assert.assertEquals("did not find expected resources", 2, resources.size());

	}

	/**
	 * Normal copy operation with tcb to noforce option, should just copy the file
	 *
	 * @throws Exception
	 */
	@Test
	public final void testCopyIRODSDataObjectToDataObjectNoForce() throws Exception {

		String testFileName = "testCopyIRODSDataObjectNoForce.txt";
		String testCopyToFileName = "testCopyIRODSDataObjectNoForceCopyTo.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig), irodsFile, null, null, false);
		IRODSFile irodsSourceFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);
		IRODSFile irodsTargetFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testCopyToFileName);
		TransferControlBlock tcb = irodsFileSystem.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.NO_FORCE);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();

		dataObjectAO.copyIRODSDataObject(irodsSourceFile, irodsTargetFile, tcb, transferStatusCallbackListener);

		IRODSFile checkCopiedFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testCopyToFileName);
		Assert.assertTrue("new file does not exist", checkCopiedFile.exists());

	}

	/**
	 * Copy where the source file does not exist
	 *
	 * @throws Exception
	 */
	@Test(expected = DataNotFoundException.class)
	public final void testCopyIRODSDataObjectToDataObjectSourceNotExists() throws Exception {

		String testFileName = "testCopyIRODSDataObjectToDataObjectSourceNotExists.txt";
		String testCopyToFileName = "testCopyIRODSDataObjectToDataObjectSourceNotExistsCopyTo.txt";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsSourceFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);
		IRODSFile irodsTargetFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testCopyToFileName);
		TransferControlBlock tcb = irodsFileSystem.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.NO_FORCE);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();

		dataObjectAO.copyIRODSDataObject(irodsSourceFile, irodsTargetFile, tcb, transferStatusCallbackListener);

	}

	/**
	 * Copy where the source file is a collection
	 *
	 * @throws Exception
	 */
	@Test(expected = JargonException.class)
	public final void testCopyIRODSDataObjectToDataObjectSourceIsCollection() throws Exception {

		String testCopyToFileName = "testCopyIRODSDataObjectToDataObjectSourceNotExistsCopyTo.txt";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsSourceFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		IRODSFile irodsTargetFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testCopyToFileName);
		TransferControlBlock tcb = irodsFileSystem.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.NO_FORCE);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();

		dataObjectAO.copyIRODSDataObject(irodsSourceFile, irodsTargetFile, tcb, transferStatusCallbackListener);
	}

	/**
	 * Copy where the source file is null
	 *
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testCopyIRODSDataObjectSourceFileNull() throws Exception {

		String testCopyToFileName = "testCopyIRODSDataObjectToDataObjectSourceNotExistsCopyTo.txt";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsSourceFile = null;
		IRODSFile irodsTargetFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testCopyToFileName);
		TransferControlBlock tcb = irodsFileSystem.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.NO_FORCE);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();

		dataObjectAO.copyIRODSDataObject(irodsSourceFile, irodsTargetFile, tcb, transferStatusCallbackListener);
	}

	/**
	 * Copy where the target file is null
	 *
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testCopyIRODSDataObjectTargetFileNull() throws Exception {

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsSourceFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		IRODSFile irodsTargetFile = null;
		TransferControlBlock tcb = irodsFileSystem.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.NO_FORCE);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();

		dataObjectAO.copyIRODSDataObject(irodsSourceFile, irodsTargetFile, tcb, transferStatusCallbackListener);

	}

	/**
	 * Normal copy operation with tcb to noforce option, should just copy the file
	 *
	 * @throws Exception
	 */
	@Test(expected = OverwriteException.class)
	public final void testCopyIRODSDataObjectToDataObjectNoForceWhenOverwrite() throws Exception {

		String testFileName = "testCopyIRODSDataObjectToDataObjectNoForceWhenOverwrite.txt";
		String testCopyToFileName = "testCopyIRODSDataObjectToDataObjectNoForceWhenOverwriteCopyTo.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig), irodsFile, null, null, false);
		IRODSFile irodsSourceFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);
		IRODSFile irodsTargetFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testCopyToFileName);
		TransferControlBlock tcb = irodsFileSystem.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.NO_FORCE);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();

		dataObjectAO.copyIRODSDataObject(irodsSourceFile, irodsTargetFile, tcb, transferStatusCallbackListener);

		dataObjectAO.copyIRODSDataObject(irodsSourceFile, irodsTargetFile, tcb, transferStatusCallbackListener);

	}

	/**
	 * Normal copy operation with tcb to force option, should just copy the file
	 *
	 * @throws Exception
	 */
	@Test
	public final void testCopyIRODSDataObjectToDataObjectForceWhenOverwrite() throws Exception {

		String testFileName = "testCopyIRODSDataObjectToDataObjectForceWhenOverwrite.txt";
		String testCopyToFileName = "testCopyIRODSDataObjectToDataObjectForceWhenOverwriteCopyTo.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig), irodsFile, null, null, false);
		IRODSFile irodsSourceFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);
		IRODSFile irodsTargetFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testCopyToFileName);
		TransferControlBlock tcb = irodsFileSystem.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.USE_FORCE);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();

		dataObjectAO.copyIRODSDataObject(irodsSourceFile, irodsTargetFile, tcb, transferStatusCallbackListener);

		dataObjectAO.copyIRODSDataObject(irodsSourceFile, irodsTargetFile, tcb, transferStatusCallbackListener);

		IRODSFile checkCopiedFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testCopyToFileName);
		Assert.assertTrue("new file does not exist", checkCopiedFile.exists());
	}

	@Test
	public final void testGetResourcesForDataObject() throws Exception {
		// generate a local scratch file

		String testFileName = "testGetResourcesForDataObject.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFileFactory factory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		IRODSFile rmFile = factory.instanceIRODSFile(targetIrodsCollection, testFileName);
		rmFile.delete();

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(fileNameOrig, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		dataObjectAO.replicateIrodsDataObject(targetIrodsCollection + '/' + testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY));

		// now query to get the two resources the data object was replicated to

		List<Resource> resources = dataObjectAO.getResourcesForDataObject(targetIrodsCollection, testFileName);

		Assert.assertTrue("should be at least2 resources for this data object", resources.size() >= 2);

	}

	@Test
	public final void testComputeSHA1Checksum() throws Exception {

		// generate a local scratch file
		String testFileName = "testComputeSHA1Checksum.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 20);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(fileNameOrig, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		byte[] sha1Checksum = dataObjectAO
				.computeSHA1ChecksumOfIrodsFileByReadingDataFromStream(targetIrodsCollection + "/" + testFileName);
		Assert.assertNotNull(sha1Checksum);
		Assert.assertFalse(sha1Checksum.length == 0);
	}

	@Test
	public final void testChecksum() throws Exception {

		// generate a local scratch file
		String testFileName = "testChecksum.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(fileNameOrig, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile testFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		String computedChecksum = dataObjectAO.computeMD5ChecksumOnDataObject(testFile);
		Assert.assertTrue("did not return a checksum", computedChecksum.length() > 0);
		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		ObjStat objStat = collectionAndDataObjectListAndSearchAO.retrieveObjectStatForPath(testFile.getAbsolutePath());
		Assert.assertFalse("didn't retrieve ObjStat and checksum", objStat.getChecksum().isEmpty());

	}

	@Test
	public final void testVerifyChecksum() throws Exception {

		// generate a local scratch file
		String testFileName = "testVerifyChecksum.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);
		File localFile = new File(fileNameOrig);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(fileNameOrig, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile testFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		Assert.assertTrue("checksums do not match",
				dataObjectAO.verifyChecksumBetweenLocalAndIrods(testFile, localFile));
	}

	@Test
	public final void testReplicateToResourceGroup() throws Exception {
		// generate a local scratch file

		String testFileName = "testReplicateToResourceGroup.txt";
		String testDir = "testReplicateToResourceGroup";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testDir);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		// if (accessObjectFactory.getIRODSServerProperties(irodsAccount)
		// .isConsortiumVersion()) {
		// return;
		// }

		// resource groups not a thing anymore
		if (accessObjectFactory.getIRODSServerProperties(irodsAccount).isAtLeastIrods410()) {
			return;
		}

		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection);
		irodsFile.delete();
		irodsFile.mkdirs();

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(fileNameOrig, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO.replicateIrodsDataObjectToAllResourcesInResourceGroup(targetIrodsCollection + "/" + testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_GROUP_KEY));

		List<Resource> resources = dataObjectAO.listFileResources(targetIrodsCollection + "/" + testFileName);

		Assert.assertEquals(2, resources.size());

	}

	@Test
	public void testAddAVUMetadataToDataObject() throws Exception {
		String testFileName = "testAddAVUMetadataToDataObject.txt";
		String expectedAttribName = "testAddAVUMetadataToDataObject";
		String expectedValueName = "testval1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/" + testFileName;

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		targetIrodsFile.delete();
		targetIrodsFile.mkdirs();
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig), targetIrodsFile, null, null);

		AvuData avuData = AvuData.instance(expectedAttribName, expectedValueName, "");
		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO.addAVUMetadata(targetIrodsDataObject, avuData);

		List<AVUQueryElement> avuQueryElements = new ArrayList<AVUQueryElement>();
		avuQueryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, expectedAttribName));

		List<DataObject> dataObjects = dataObjectAO.findDomainByMetadataQuery(avuQueryElements);
		Assert.assertTrue(dataObjects.size() >= 1);
	}

	@Test
	public void testAddAVUMetadataToDataObjectWithSet() throws Exception {
		String testFileName = "testAddAVUMetadataToDataObjectWithSet.txt";
		String expectedAttribName = "testAddAVUMetadataToDataObjectWithSet";
		String expectedValueName = "testval1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/" + testFileName;

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		targetIrodsFile.delete();
		targetIrodsFile.mkdirs();
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig), targetIrodsFile, null, null);

		AvuData avuData = AvuData.instance(expectedAttribName, expectedValueName, "");
		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO.setAVUMetadata(targetIrodsDataObject, avuData);

		List<AVUQueryElement> avuQueryElements = new ArrayList<>();
		avuQueryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, expectedAttribName));

		List<DataObject> dataObjects = dataObjectAO.findDomainByMetadataQuery(avuQueryElements);
		Assert.assertTrue(dataObjects.size() >= 1);
	}

	@Test
	public void testAddAVUMetadataToDataObjectWithSetParentAndPath() throws Exception {
		String testFileName = "testAddAVUMetadataToDataObjectWithSetParentAndPath.txt";
		String expectedAttribName = "testAddAVUMetadataToDataObjectWithSetParentAndPath";
		String expectedValueName = "testval1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		targetIrodsFile.delete();
		targetIrodsFile.mkdirs();
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig), targetIrodsFile, null, null);

		AvuData avuData = AvuData.instance(expectedAttribName, expectedValueName, "");
		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO.setAVUMetadata(targetIrodsCollection, testFileName, avuData);

		List<AVUQueryElement> avuQueryElements = new ArrayList<>();
		avuQueryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, expectedAttribName));

		List<DataObject> dataObjects = dataObjectAO.findDomainByMetadataQuery(avuQueryElements);
		Assert.assertTrue(dataObjects.size() >= 1);
	}

	/**
	 * Test for https://github.com/DICE-UNC/jargon/issues/227
	 *
	 * imeta add -d testBulkAddAVUMetadataToDataObjectSpecialCharsBug227.txt
	 * icommand áàâ
	 *
	 *
	 * @throws Exception
	 */
	@Test
	public void testAddAVUMetadataToDataObjectSpecialCharsBug227() throws Exception {
		String testFileName = "testBulkAddAVUMetadataToDataObjectSpecialCharsBug227X.txt";
		String expectedAttribName = "testAddAVUMetadataToDataObjectSpecialCharsBug227";
		String expectedValueName = "áàâ";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/" + testFileName;

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		targetIrodsFile.delete();
		targetIrodsFile.mkdirs();
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig), targetIrodsFile, null, null);

		AvuData avuData = AvuData.instance(expectedAttribName, expectedValueName, "");

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO.addAVUMetadata(targetIrodsDataObject, avuData);

		List<AVUQueryElement> avuQueryElements = new ArrayList<AVUQueryElement>();
		avuQueryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, expectedAttribName));

		List<MetaDataAndDomainData> metadata = dataObjectAO.findMetadataValuesByMetadataQuery(avuQueryElements);
		Assert.assertTrue(metadata.size() >= 1);
		MetaDataAndDomainData actual = metadata.get(0);
		Assert.assertEquals("char mismatch", expectedValueName, actual.getAvuValue());

	}

	@Test
	public void testBulkAddAVUMetadataToDataObject() throws Exception {
		String testFileName = "testBulkAddAVUMetadataToDataObject.txt";
		String expectedAttribName = "testBulkAddAVUMetadataToDataObject";
		String expectedValueName = "testval1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/" + testFileName;

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		targetIrodsFile.delete();
		targetIrodsFile.mkdirs();
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig), targetIrodsFile, null, null);

		AvuData avuData = AvuData.instance(expectedAttribName, expectedValueName, "");
		List<AvuData> bulkAvuData = new ArrayList<>();
		bulkAvuData.add(avuData);
		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		List<BulkAVUOperationResponse> response = dataObjectAO.addBulkAVUMetadataToDataObject(targetIrodsDataObject,
				bulkAvuData);

		Assert.assertNotNull(response);
		Assert.assertFalse(response.isEmpty());

		List<AVUQueryElement> avuQueryElements = new ArrayList<AVUQueryElement>();
		avuQueryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, expectedAttribName));

		List<DataObject> dataObjects = dataObjectAO.findDomainByMetadataQuery(avuQueryElements);
		Assert.assertTrue(dataObjects.size() >= 1);
	}

	@Test
	public void testAddAVUMetadataToDataObjectTwice() throws Exception {
		String testFileName = "testAddAVUMetadataToDataObjectTwice.txt";
		String expectedAttribName = "testAddAVUMetadataToDataObjectTwice";
		String expectedValueName = "testval1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/" + testFileName;

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		// Skip if iRODS 4.3.0.
		Assume.assumeFalse("iRODS 4.3.0 does not report an error on duplicate AVUs",
				accessObjectFactory.getIRODSServerProperties(irodsAccount).isVersion("rods4.3.0"));

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		DataTransferOperations dataTransferOperationsAO = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig), targetIrodsFile, null, null);

		AvuData avuData = AvuData.instance(expectedAttribName, expectedValueName, "");
		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO.addAVUMetadata(targetIrodsDataObject, avuData);

		if (accessObjectFactory.getIRODSServerProperties(irodsAccount).isAtLeastIrods431()) {
			Assert.assertThrows(CatalogSQLException.class,
					() -> dataObjectAO.addAVUMetadata(targetIrodsDataObject, avuData));
		} else {
			Assert.assertThrows(DuplicateDataException.class,
					() -> dataObjectAO.addAVUMetadata(targetIrodsDataObject, avuData));
		}

		List<AVUQueryElement> avuQueryElements = new ArrayList<AVUQueryElement>();
		avuQueryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, expectedAttribName));

		List<DataObject> dataObjects = dataObjectAO.findDomainByMetadataQuery(avuQueryElements);
		// overhead for avus in trash need to add a rmtrash and rmavu method in
		// test prep
		Assert.assertTrue(dataObjects.size() >= 1);
	}

	/*
	 * [#161] iRODS inconsistantly handles duplicate AVU data for data objects
	 */
	public void testAddAVUMetadataToDataObjectTwiceIncludeUnitsVal() throws Exception {
		String testFileName = "testAddAVUMetadataToDataObjectTwiceIncludeUnitsVal.txt";
		String expectedAttribName = "testAddAVUMetadataToDataObjectTwice";
		String expectedValueName = "testval1";
		String expectedUnitsVal = "testunits1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/" + testFileName;

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig), targetIrodsFile, null, null);

		AvuData avuData = AvuData.instance(expectedAttribName, expectedValueName, expectedUnitsVal);
		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO.addAVUMetadata(targetIrodsDataObject, avuData);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		if (accessObjectFactory.getIRODSServerProperties(irodsAccount).isAtLeastIrods430()) {
			Assert.assertThrows(CatalogSQLException.class,
					() -> dataObjectAO.addAVUMetadata(targetIrodsDataObject, avuData));
		} else {
			Assert.assertThrows(DuplicateDataException.class,
					() -> dataObjectAO.addAVUMetadata(targetIrodsDataObject, avuData));
		}

		List<AVUQueryElement> avuQueryElements = new ArrayList<AVUQueryElement>();
		avuQueryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, expectedAttribName));

		List<DataObject> dataObjects = dataObjectAO.findDomainByMetadataQuery(avuQueryElements);
		Assert.assertTrue(dataObjects.size() == 1);
	}

	@Test
	public void testAddAVUMetadataToDataObjectTwiceSameNameDiffVal() throws Exception {
		String testFileName = "testAddAVUMetadataToDataObjectTwiceSameNameDiffVal.txt";
		String expectedAttribName = "testAddAVUMetadataToDataObjectTwiceSameNameDiffVal";
		String expectedValueName = "testAddAVUMetadataToDataObjectTwiceSameNameDiffValtestval1";
		String expectedValueName2 = "testAddAVUMetadataToDataObjectTwiceSameNameDiffValtestval2";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/" + testFileName;

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig), targetIrodsFile, null, null);

		AvuData avuData = AvuData.instance(expectedAttribName, expectedValueName, "");
		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO.addAVUMetadata(targetIrodsDataObject, avuData);

		avuData = AvuData.instance(expectedAttribName, expectedValueName2, "");
		dataObjectAO.addAVUMetadata(targetIrodsDataObject, avuData);

		List<AVUQueryElement> avuQueryElements = new ArrayList<AVUQueryElement>();
		avuQueryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, expectedAttribName));

		List<MetaDataAndDomainData> metadata = dataObjectAO.findMetadataValuesByMetadataQuery(avuQueryElements);
		// overhead for avus in trash need to add a rmtrash and rmavu method in
		// test prep
		Assert.assertTrue(metadata.size() >= 2);
	}

	@Test
	public void testDeleteAVUMetadataFromDataObject() throws Exception {
		String testFileName = "testDeleteAVUMetadataFromDataObject.txt";
		String expectedAttribName = "testDeleteAVUMetadataFromDataObject";
		String expectedValueName = "testval1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/" + testFileName;

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig), targetIrodsFile, null, null);

		AvuData avuData = AvuData.instance(expectedAttribName, expectedValueName, "");
		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO.addAVUMetadata(targetIrodsDataObject, avuData);

		// now delete and query again, should be no data

		dataObjectAO.deleteAVUMetadata(targetIrodsDataObject, avuData);

		List<AVUQueryElement> avuQueryElements = new ArrayList<AVUQueryElement>();
		avuQueryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, expectedAttribName));

		List<DataObject> dataObjects = dataObjectAO.findDomainByMetadataQuery(avuQueryElements);
		Assert.assertTrue(dataObjects.isEmpty());
	}

	@Test(expected = DataNotFoundException.class)
	public void testDeleteAVUMetadataFromDataObjectDataObjectNotExists() throws Exception {
		String testFileName = "testDeleteAVUMetadataFromDataObjectDataObjectNotExists.xml";
		String expectedAttribName = "testDeleteAVUMetadataFromDataObjectDataObjectNotExists";
		String expectedValueName = "testDeleteAVUMetadataFromDataObjectDataObjectNotExiststestval1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/" + testFileName;
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		AvuData avuData = AvuData.instance(expectedAttribName, expectedValueName, "");
		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO.addAVUMetadata(targetIrodsDataObject, avuData);
	}

	@Test
	public final void testSetRead() throws Exception {
		// generate a local scratch file

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		// if (props.isConsortiumVersion()) {
		// return;
		// }

		String testFileName = "testSetRead.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig), irodsFile, null, null, false);

		dataObjectAO.setAccessPermissionRead("", targetIrodsCollection + "/" + testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		// log in as the secondary user and test read access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);

		CollectionAO collectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		collectionAO.setAccessPermissionRead(irodsAccount.getZone(), targetIrodsCollection,
				secondaryAccount.getUserName(), true);

		IRODSFile irodsFileForSecondaryUser = irodsFileSystem.getIRODSFileFactory(secondaryAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);
		Assert.assertTrue("should be able to read", irodsFileForSecondaryUser.canRead());

	}

	@Test
	public final void testSetReadAsAdmin() throws Exception {
		// generate a local scratch file

		String testFileName = "testSetReadAsAdmin.doc";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccountRods = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(testingProperties,
						testingProperties.getProperty(TestingPropertiesHelper.IRODS_ADMIN_USER_KEY),
						testingProperties.getProperty(TestingPropertiesHelper.IRODS_ADMIN_PASSWORD_KEY));
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig), irodsFile, null, null, false);

		CollectionAO rodsCollectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccountRods);

		rodsCollectionAO.setAccessPermissionReadAsAdmin("", targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY), true);

		// log in as the secondary user and test read access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSFile irodsFileForSecondaryUser = irodsFileSystem.getIRODSFileFactory(secondaryAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);
		Assert.assertTrue(irodsFileForSecondaryUser.canRead());

	}

	@Test
	public final void testSetWriteAsAdmin() throws Exception {
		// generate a local scratch file

		String testFileName = "testSetWriteAsAdmin.doc";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccountRods = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(testingProperties,
						testingProperties.getProperty(TestingPropertiesHelper.IRODS_ADMIN_USER_KEY),
						testingProperties.getProperty(TestingPropertiesHelper.IRODS_ADMIN_PASSWORD_KEY));
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig), irodsFile, null, null, false);

		CollectionAO rodsCollectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccountRods);

		rodsCollectionAO.setAccessPermissionWriteAsAdmin("", targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY), true);

		// if (props.isConsortiumVersion()) {
		// return;
		// }

		// log in as the secondary user and test read access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSFile irodsFileForSecondaryUser = irodsFileSystem.getIRODSFileFactory(secondaryAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);
		Assert.assertTrue(irodsFileForSecondaryUser.canWrite());

	}

	@Test
	public final void testSetOwnAsAdmin() throws Exception {
		// generate a local scratch file

		String testFileName = "testSetOwnAsAdmin.doc";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccountRods = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(testingProperties,
						testingProperties.getProperty(TestingPropertiesHelper.IRODS_ADMIN_USER_KEY),
						testingProperties.getProperty(TestingPropertiesHelper.IRODS_ADMIN_PASSWORD_KEY));
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig), irodsFile, null, null, false);

		DataObjectAO rodsDataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccountRods);

		CollectionAO rodsCollectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccountRods);
		rodsCollectionAO.setAccessPermissionReadAsAdmin(irodsAccount.getZone(), targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY), true);

		rodsDataObjectAO.setAccessPermissionOwnInAdminMode("", targetIrodsCollection + "/" + testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		// log in as the secondary user and test read access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSFile irodsFileForSecondaryUser = irodsFileSystem.getIRODSFileFactory(secondaryAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);
		DataObjectAO dataObjectAOSecondaryUser = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(secondaryAccount);
		UserFilePermission userFilePermission = dataObjectAOSecondaryUser.getPermissionForDataObjectForUserName(
				irodsFileForSecondaryUser.getAbsolutePath(), secondaryAccount.getUserName());
		Assert.assertTrue("user should have own permission",
				userFilePermission.getFilePermissionEnum() == FilePermissionEnum.OWN);

	}

	/**
	 * Added to test appending path in dataObjectAOImpl causes duplicate file name
	 * #73
	 *
	 * https://github.com/DICE-UNC/jargon/issues/73
	 *
	 * @throws Exception
	 */
	@Test
	public final void testRemoveAccessPermissionsAsAdminForUser() throws Exception {
		// generate a local scratch file

		String testFileName = "testRemoveAccessPermissionsAsAdminForUser.doc";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccountRods = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(testingProperties,
						testingProperties.getProperty(TestingPropertiesHelper.IRODS_ADMIN_USER_KEY),
						testingProperties.getProperty(TestingPropertiesHelper.IRODS_ADMIN_PASSWORD_KEY));

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig), irodsFile, null, null, false);

		DataObjectAO rodsDataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccountRods);

		CollectionAO rodsCollectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccountRods);
		rodsCollectionAO.setAccessPermissionReadAsAdmin(irodsAccount.getZone(), targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY), true);

		rodsDataObjectAO.setAccessPermissionOwnInAdminMode("", targetIrodsCollection + "/" + testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		// log in as the secondary user and test read access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSFile irodsFileForSecondaryUser = irodsFileSystem.getIRODSFileFactory(secondaryAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);
		DataObjectAO dataObjectAOSecondaryUser = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(secondaryAccount);
		UserFilePermission userFilePermission = dataObjectAOSecondaryUser.getPermissionForDataObjectForUserName(
				irodsFileForSecondaryUser.getAbsolutePath(), secondaryAccount.getUserName());
		Assert.assertTrue("user should have own permission",
				userFilePermission.getFilePermissionEnum() == FilePermissionEnum.OWN);

		// now remove in admin mode

		rodsDataObjectAO.removeAccessPermissionsForUserInAdminMode(irodsAccount.getZone(),
				targetIrodsCollection + "/" + testFileName, secondaryAccount.getUserName());

		userFilePermission = dataObjectAO.getPermissionForDataObjectForUserName(
				irodsFileForSecondaryUser.getAbsolutePath(), secondaryAccount.getUserName());
		Assert.assertTrue("user should not have own permission", userFilePermission == null);

	}

	@Test
	public final void testSetWrite() throws Exception {
		// generate a local scratch file

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		/*
		 * if (props.isConsortiumVersion()) { return; }
		 */

		String testFileName = "testSetWrite.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig), irodsFile, null, null, false);

		dataObjectAO.setAccessPermissionWrite("", targetIrodsCollection + "/" + testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		// log in as the secondary user and test read access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSFile irodsFileForSecondaryUser = irodsFileSystem.getIRODSFileFactory(secondaryAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);
		Assert.assertTrue(irodsFileForSecondaryUser.canWrite());

	}

	@Test
	public final void testSetOwn() throws Exception {
		// generate a local scratch file

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		/*
		 * if (props.isConsortiumVersion()) { return; }
		 */

		String testFileName = "testSetOwn.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig), irodsFile, null, null, false);

		dataObjectAO.setAccessPermissionOwn("", targetIrodsCollection + "/" + testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));
		CollectionAO collectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		collectionAO.setAccessPermissionOwn(irodsAccount.getZone(), targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY), true);

		// log in as the secondary user and test read access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSFile irodsFileForSecondaryUser = irodsFileSystem.getIRODSFileFactory(secondaryAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);

		IRODSFileSystemAO irodsFileSystemAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getIRODSFileSystemAO(secondaryAccount);
		int permissions = irodsFileSystemAO.getFilePermissions(irodsFileForSecondaryUser);

		Assert.assertTrue(permissions >= IRODSFile.OWN_PERMISSIONS);

	}

	@Test
	public final void testSetPublicWrite() throws Exception {
		// generate a local scratch file

		String testFileName = "testSetPublicRead.txt";
		String testUserName = "public";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig), irodsFile, true);

		dataObjectAO.setAccessPermissionWrite("", targetIrodsCollection + "/" + testFileName, testUserName);

		IRODSFileSystemAO irodsFileSystemAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getIRODSFileSystemAO(irodsAccount);
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection,
				testFileName);
		int permissions = irodsFileSystemAO.getFilePermissionsForGivenUser(irodsFile, testUserName);

		Assert.assertTrue(permissions >= IRODSFile.WRITE_PERMISSIONS);

	}

	@Test

	public final void testGetPermissionsOwn() throws Exception {
		// generate a local scratch file

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		/*
		 * if (props.isConsortiumVersion()) { return; }
		 */

		String testFileName = "testGetPermissionsOwn.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig), irodsFile, true);
		CollectionAO collectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		collectionAO.setAccessPermissionOwn("", targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY), true);

		// log in as the secondary user and test read access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSFile irodsFileForSecondaryUser = irodsFileSystem.getIRODSFileFactory(secondaryAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);
		DataObjectAO secondaryDataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(secondaryAccount);
		FilePermissionEnum filePermissionEnum = secondaryDataObjectAO.getPermissionForDataObject(
				irodsFileForSecondaryUser.getAbsolutePath(), secondaryAccount.getUserName(), "");

		Assert.assertEquals("should have found own permissions", FilePermissionEnum.OWN, filePermissionEnum);

	}

	/**
	 * bug [#1575] [iROD-Chat:10314] jargon-core permissions issue
	 *
	 * @throws Exception
	 */
	@Ignore
	// https://github.com/DICE-UNC/jargon/issues/130
	public final void testSetPermissionsForUserInGroupToObject() throws Exception {
		// generate a local scratch file

		String testFileName = "testSetPermissionsForUserInGroupToObject.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String testUser = "testUserInGroup123";
		String testGroup = "testGroupForTestingUser123";
		String newPassword = "blargh";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		UserAO userAO = irodsFileSystem.getIRODSAccessObjectFactory().getUserAO(irodsAccount);
		UserGroupAO userGroupAO = irodsFileSystem.getIRODSAccessObjectFactory().getUserGroupAO(irodsAccount);

		try {
			userAO.deleteUser(testUser);
			userGroupAO.removeUserGroup(testGroup);
		} catch (Exception e) {
			// ignore
		}

		User user = new User();
		user.setUserType(UserTypeEnum.RODS_USER);
		user.setName(testUser);
		userAO.addUser(user);
		userAO.changeAUserPasswordByAnAdmin(testUser, newPassword);

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testGroup);
		userGroup.setZone(irodsAccount.getZone());
		userGroupAO.addUserGroup(userGroup);
		userGroupAO.addUserToGroup(testGroup, testUser, irodsAccount.getZone());

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig), irodsFile, true);

		dataObjectAO.setAccessPermissionRead("", targetIrodsCollection + "/" + testFileName, testGroup);

		// log in as the group user and test read access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(testingProperties, testUser, newPassword);
		IRODSFile irodsFileForSecondaryUser = irodsFileSystem.getIRODSFileFactory(secondaryAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);

		FilePermissionEnum filePermissionEnum = dataObjectAO.getPermissionForDataObject(
				irodsFileForSecondaryUser.getAbsolutePath(), secondaryAccount.getUserName(), "");

		Assert.assertEquals("should have found read permissions", FilePermissionEnum.READ, filePermissionEnum);

	}

	@Test
	public final void testGetPermissionsForGivenUserWhoHasRead() throws Exception {
		// generate a local scratch file

		String testFileName = "testGetPermissionsForGivenUserWhoHasRead.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig), irodsFile, true);

		dataObjectAO.setAccessPermissionRead("", targetIrodsCollection + "/" + testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		// log in as the secondary user and test read access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSFile irodsFileForSecondaryUser = irodsFileSystem.getIRODSFileFactory(secondaryAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);

		FilePermissionEnum filePermissionEnum = dataObjectAO.getPermissionForDataObject(
				irodsFileForSecondaryUser.getAbsolutePath(), secondaryAccount.getUserName(), "");

		Assert.assertEquals("should have found read permissions", FilePermissionEnum.READ, filePermissionEnum);

	}

	@Test
	public final void testSetAccessPermissionRead() throws Exception {
		String testFileName = "testSetAccessPermissionRead.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		CollectionAO collectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig), irodsFile, true);
		collectionAO.setAccessPermissionRead(irodsAccount.getZone(), targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY), true);
		dataObjectAO.setAccessPermission("", targetIrodsCollection + "/" + testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
				FilePermissionEnum.READ);

		/*
		 * if (props.isConsortiumVersion()) { return; }
		 */

		// log in as the secondary user and test read access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSFile irodsFileForSecondaryUser = irodsFileSystem.getIRODSFileFactory(secondaryAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);

		Assert.assertTrue("user unable to read directory", irodsFileForSecondaryUser.canRead());

	}

	@Test
	public final void testSetAccessPermissionWrite() throws Exception {
		String testFileName = "testSetAccessPermissionWrite.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig), irodsFile, true);

		CollectionAO rodsCollectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);

		rodsCollectionAO.setAccessPermissionWrite("", targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY), true);

		/*
		 * if (props.isConsortiumVersion()) { return; }
		 */

		// log in as the secondary user and test read access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSFile irodsFileForSecondaryUser = irodsFileSystem.getIRODSFileFactory(secondaryAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);
		Assert.assertTrue("secondary user cannot write file", irodsFileForSecondaryUser.canWrite());

	}

	@Test
	public final void testListPermissionsForDataObject() throws Exception {
		// generate a local scratch file

		String testFileName = "testListPermissionsForDataObject.xls";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig), irodsFile, true);

		dataObjectAO.setAccessPermissionRead("", targetIrodsCollection + "/" + testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		List<UserFilePermission> userFilePermissions = dataObjectAO
				.listPermissionsForDataObject(targetIrodsCollection + "/" + testFileName);
		Assert.assertNotNull("got a null userFilePermissions", userFilePermissions);
		Assert.assertFalse("did not find permissions", userFilePermissions.isEmpty());
		Assert.assertTrue("did not find the two permissions", userFilePermissions.size() >= 2);

		boolean foundIt = false;
		for (UserFilePermission permission : userFilePermissions) {
			if (permission.getUserName()
					.equals(testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY))) {
				foundIt = true;
				Assert.assertEquals("user group not correctly determined", UserTypeEnum.RODS_USER,
						permission.getUserType());
			}
		}
		Assert.assertTrue("did not find user group in permissions", foundIt);

	}

	/**
	 * [#1575] jargon-core permissions issue Add a user to a group, add that group
	 * to file permissions, and list the group
	 *
	 * @throws Exception
	 */
	@Test
	public final void testListPermissionsForDataObjectLookingForGroupPermissions() throws Exception {
		// generate a local scratch file

		String testFileName = "testListPermissionsForDataObjectLookingForGroupPermissions.xls";
		String testUserGroup = "testListPermissionsForDataObjectLookingForGroupPermissions";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccount secondaryIrodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);

		UserGroupAO userGroupAO = irodsFileSystem.getIRODSAccessObjectFactory().getUserGroupAO(irodsAccount);

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroup);
		userGroup.setZone(irodsAccount.getZone());

		userGroupAO.removeUserGroup(userGroup);
		userGroupAO.addUserGroup(userGroup);

		userGroupAO.addUserToGroup(testUserGroup, secondaryIrodsAccount.getUserName(), null);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig), irodsFile, true);

		dataObjectAO.setAccessPermissionRead("", targetIrodsCollection + "/" + testFileName, testUserGroup);

		List<UserFilePermission> userFilePermissions = dataObjectAO
				.listPermissionsForDataObject(targetIrodsCollection + "/" + testFileName);
		userGroupAO.removeUserGroup(userGroup);
		Assert.assertNotNull("got a null userFilePermissions", userFilePermissions);
		Assert.assertFalse("did not find  permissions", userFilePermissions.isEmpty());
		Assert.assertTrue("did not find the two permissions", userFilePermissions.size() >= 2);

		boolean foundIt = false;
		for (UserFilePermission permission : userFilePermissions) {
			if (permission.getUserName().equals(testUserGroup)) {
				foundIt = true;
				Assert.assertEquals("user group not correctly determined", UserTypeEnum.RODS_GROUP,
						permission.getUserType());
			}
		}
		Assert.assertTrue("did not find user group in permissions", foundIt);

	}

	/**
	 * [#1575] jargon-core permissions issue Add a user to a group, add that group
	 * to file permissions, and list the group
	 *
	 * @throws Exception
	 */
	@Test
	public final void testGetPermissionsForDataObjectForUserViaGroupPermissions() throws Exception {
		// generate a local scratch file

		String testFileName = "testGetPermissionsForDataObjectForUserViaGroupPermissions.xls";
		String testUserGroup = "testGetPermissionsForDataObjectForUserViaGroupPermissions";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccount secondaryIrodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);

		UserGroupAO userGroupAO = irodsFileSystem.getIRODSAccessObjectFactory().getUserGroupAO(irodsAccount);

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroup);
		userGroup.setZone(irodsAccount.getZone());

		userGroupAO.removeUserGroup(userGroup);
		userGroupAO.addUserGroup(userGroup);

		userGroupAO.addUserToGroup(testUserGroup, secondaryIrodsAccount.getUserName(), null);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig), irodsFile, true);

		dataObjectAO.setAccessPermissionRead("", targetIrodsCollection + "/" + testFileName, testUserGroup);

		UserFilePermission perm = dataObjectAO.getPermissionForDataObjectForUserName(
				targetIrodsCollection + "/" + testFileName, secondaryIrodsAccount.getUserName());
		Assert.assertNotNull("did not get perm for user", perm);

	}

	@Test
	public final void testListPermissionsForDataObjectAfterGivingPublicWrite() throws Exception {
		// generate a local scratch file

		String testFileName = "testListPermissionsForDataObjectAfterGivingPublicWrite.xls";
		String testPublicUser = "public";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig), irodsFile, true);

		dataObjectAO.setAccessPermissionRead("", targetIrodsCollection + "/" + testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		dataObjectAO.setAccessPermissionWrite("", targetIrodsCollection + "/" + testFileName, testPublicUser);

		List<UserFilePermission> userFilePermissions = dataObjectAO
				.listPermissionsForDataObject(targetIrodsCollection + "/" + testFileName);
		Assert.assertNotNull("got a null userFilePermissions", userFilePermissions);
		Assert.assertFalse("did not find  permissions", userFilePermissions.isEmpty());
		Assert.assertTrue("did not find the 3 permissions", userFilePermissions.size() >= 3);

	}

	@Test
	public void testOverwriteFileAvuMetadata() throws Exception {
		String testFileName = "testOverwriteFileAvuMetadata.txt";
		String expectedAttribName = "testOverwriteFileAvuMetadataAttrib1";
		String expectedAttribValue = "testOverwriteFileAvuMetadataValue1";
		String expectedNewValue = "testOverwriteFileAvuMetadataValue1ThatsOverwriten";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection,
				testFileName);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig), irodsFile, true);

		AvuData dataToAdd = AvuData.instance(expectedAttribName, expectedAttribValue, "");
		dataObjectAO.addAVUMetadata(targetIrodsCollection, testFileName, dataToAdd);
		AvuData overwriteAvuData = AvuData.instance(expectedAttribName, expectedNewValue, "");

		dataObjectAO.modifyAVUMetadata(targetIrodsCollection, testFileName, dataToAdd, overwriteAvuData);

		List<MetaDataAndDomainData> metadata = dataObjectAO.findMetadataValuesForDataObject(targetIrodsCollection,
				testFileName);

		boolean attribFound = false;
		for (MetaDataAndDomainData metadataEntry : metadata) {
			if (expectedAttribName.equals(metadataEntry.getAvuAttribute())
					&& expectedNewValue.equals(metadataEntry.getAvuValue())) {
				attribFound = true;
			}
		}

		Assert.assertTrue(attribFound);

	}

	@Test
	public void testOverwriteFileAvuMetadataSpecifyAbsPath() throws Exception {
		String testFileName = "testOverwriteFileAvuMetadataSpecifyAbsPath.txt";
		String expectedAttribName = "testOverwriteFileAvuMetadataSpecifyAbsPathAvuMetadataAttrib1";
		String expectedAttribValue = "testOverwriteFileAvuMetadataSpecifyAbsPathAvuMetadataValue1";
		String expectedNewValue = "testOverwriteFileAvuMetadataSpecifyAbsPathAvuMetadataValue1ThatsOverwriten";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection,
				testFileName);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig), irodsFile, true);

		AvuData dataToAdd = AvuData.instance(expectedAttribName, expectedAttribValue, "");
		dataObjectAO.addAVUMetadata(targetIrodsCollection, testFileName, dataToAdd);
		AvuData overwriteAvuData = AvuData.instance(expectedAttribName, expectedNewValue, "");

		dataObjectAO.modifyAVUMetadata(targetIrodsCollection + "/" + testFileName, dataToAdd, overwriteAvuData);

		List<MetaDataAndDomainData> metadata = dataObjectAO
				.findMetadataValuesForDataObject(targetIrodsCollection + "/" + testFileName);
		irodsFileSystem.close();

		boolean attribFound = false;
		for (MetaDataAndDomainData metadataEntry : metadata) {
			if (expectedAttribName.equals(metadataEntry.getAvuAttribute())
					&& expectedNewValue.equals(metadataEntry.getAvuValue())) {
				attribFound = true;
			}
		}

		Assert.assertTrue(attribFound);
	}

	@Test
	public void testOverwriteAvuMetadataGivenNameAndUnit() throws Exception {
		String testFileName = "testOverwriteAvuMetadataGivenNameAndUnit.txt";
		String expectedAttribName = "testOverwriteAvuMetadataGivenNameAndUnitAvuMetadataAttrib1";
		String expectedAttribValue = "testOverwriteAvuMetadataGivenNameAndUnitAvuMetadataValue1";
		String expectedNewValue = "testOverwriteAvuMetadataGivenNameAndUnitAvuMetadataValue1ThatsOverwriten";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection,
				testFileName);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig), irodsFile, true);

		AvuData dataToAdd = AvuData.instance(expectedAttribName, expectedAttribValue, "");
		dataObjectAO.addAVUMetadata(targetIrodsCollection, testFileName, dataToAdd);
		AvuData overwriteAvuData = AvuData.instance(expectedAttribName, expectedNewValue, "");

		dataObjectAO.modifyAvuValueBasedOnGivenAttributeAndUnit(targetIrodsCollection + "/" + testFileName,
				overwriteAvuData);

		List<MetaDataAndDomainData> metadata = dataObjectAO.findMetadataValuesForDataObject(targetIrodsCollection,
				testFileName);

		boolean attribFound = false;
		for (MetaDataAndDomainData metadataEntry : metadata) {
			if (expectedAttribName.equals(metadataEntry.getAvuAttribute())
					&& expectedNewValue.equals(metadataEntry.getAvuValue())) {
				attribFound = true;
			}
		}

		Assert.assertTrue(attribFound);
	}

	@Test
	public void testRemoveAvuMetadataAvuDataDoesNotExist() throws Exception {
		String testDirName = "testRemoveAvuMetadataAvuDataDoesNotExistDir";
		String expectedAttribName = "testattrib1";
		String expectedAttribValue = "testvalue1";

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);

		IRODSFile dirFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection);
		dirFile.mkdirs();

		CollectionAO collectionAO = accessObjectFactory.getCollectionAO(irodsAccount);

		AvuData avuData = AvuData.instance(expectedAttribName, expectedAttribValue, "");

		collectionAO.deleteAVUMetadata(targetIrodsCollection, avuData);
	}

	@Test
	public void testGetHostForGetProvidingResourceNameWhenShouldDifferentResource() throws Exception {

		String useDistribResources = testingProperties.getProperty("test.option.distributed.resources");

		if (useDistribResources != null && useDistribResources.equals("true")) {
			// do the test
		} else {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getEnvironmentalInfoAO(irodsAccount);

		IRODSServerProperties irodsServerProperties = environmentalInfoAO.getIRODSServerProperties();

		if (!irodsServerProperties
				.isTheIrodsServerAtLeastAtTheGivenReleaseVersion(RemoteExecuteServiceImpl.STREAMING_API_CUTOFF)) {
			irodsFileSystem.close();
			return;
		}

		// generate a local scratch file
		String testFileName = "testGetHostForGetProvidingResourceNameWhenShouldDifferentResource.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		DataTransferOperations transferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		transferOperationsAO.putOperation(fileNameAndPath.toString(), targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_TERTIARY_RESOURCE_KEY), null, null);
		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		String hostInfo = dataObjectAO.getHostForGetOperation(targetIrodsCollection + "/" + testFileName, "");
		irodsFileSystem.close();
		Assert.assertNotNull(
				"null info from lookup of host for get operation was not expected, re-routing should occur", hostInfo);
	}

	@Test
	public final void testListPermissionsForDataObjectForUser() throws Exception {
		// generate a local scratch file

		String testFileName = "testListPermissionsForDataObjectForUser.xls";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig), irodsFile, true);

		dataObjectAO.setAccessPermissionRead("", targetIrodsCollection + "/" + testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		UserFilePermission userFilePermission = dataObjectAO.getPermissionForDataObjectForUserName(
				targetIrodsCollection + "/" + testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		Assert.assertNotNull("got a null userFilePermission", userFilePermission);
		Assert.assertEquals("did not find the right user name",
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
				userFilePermission.getUserName());

	}

	@Test
	public final void testListPermissionsForDataObjectForUserViaParentNameDataName() throws Exception {
		// generate a local scratch file

		String testFileName = "testListPermissionsForDataObjectForUserViaParentNameDataName.xls";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig), irodsFile, true);

		dataObjectAO.setAccessPermissionRead("", targetIrodsCollection + "/" + testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		UserFilePermission userFilePermission = dataObjectAO.getPermissionForDataObjectForUserName(
				targetIrodsCollection, testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		Assert.assertNotNull("got a null userFilePermission", userFilePermission);
		Assert.assertEquals("did not find the right user name",
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
				userFilePermission.getUserName());

	}

	@Ignore
	// TODO: need to parameterize the interval, right now the file needs to be
	// huge to get callbacks.
	public void testPutFileReceiveIntraFileCallbacks() throws Exception {
		long testSize = 8000000;
		// generate a local scratch file
		String testFileName = "testPutFileReceiveIntraFileCallbacks.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, testSize);

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		TransferOptions transferOptions = new TransferOptions();
		transferOptions.setIntraFileStatusCallbacks(true);
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock.instance();
		transferControlBlock.setTransferOptions(transferOptions);

		TestingStatusCallbackListener transferStatusCallbackListener = new TestingStatusCallbackListener();

		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, transferControlBlock,
				transferStatusCallbackListener, false);
		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsFile, accessObjectFactory, irodsAccount);
		Assert.assertTrue("did not get intra-file callbacks",
				transferStatusCallbackListener.getNumberIntraFileCallbacks() > 0);
		Assert.assertTrue("did not get any byte count from intra-file callbacks",
				transferStatusCallbackListener.getBytesReportedIntraFileCallbacks() > 0);
		Assert.assertFalse("accumulated more bytes than file size",
				transferStatusCallbackListener.getBytesReportedIntraFileCallbacks() > testSize);
	}

	@Test
	public void testPutToIRODSFileWithDifferentFileName() throws Exception {
		// generate a local scratch file
		String testFileName = "testPutToIRODSFileWithDifferentFileName.txt";
		String testTargetFileName = "testPutToIRODSFileWithDifferentFileNameTarget.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 3);

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testTargetFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		TransferOptions transferOptions = new TransferOptions();
		transferOptions.setComputeAndVerifyChecksumAfterTransfer(true);
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock.instance();
		transferControlBlock.setTransferOptions(transferOptions);

		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, transferControlBlock, null, false);

		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsFile, accessObjectFactory, irodsAccount);
	}

	/**
	 * Create a source file in one collection, put to a target iRODS file with a
	 * different collection not specifying the target file name.
	 *
	 * @throws Exception
	 */
	@Test
	public void testPutAFileGivingFileNameInOneCollectionToATargetCollectionGivingCollectionName() throws Exception {

		String testCollectionSubdir = "testPutAFileGivingFileNameInOneCollectionToATargetCollectionGivingCollectionName";
		String testTargetCollectionSubdir = "testPutAFileGivingFileNameInOneCollectionToATargetCollectionGivingCollectionNameTarget";

		String testFileName = "test.txt";
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testTargetCollectionSubdir);
		IRODSFile targetIrodsCollectionFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		targetIrodsCollectionFile.mkdirs();
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + "/" + testCollectionSubdir);

		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 3);

		File localFile = new File(absPath, testFileName);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);

		TransferOptions transferOptions = new TransferOptions();
		transferOptions.setComputeAndVerifyChecksumAfterTransfer(true);
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock.instance();
		transferControlBlock.setTransferOptions(transferOptions);

		dataObjectAO.putLocalDataObjectToIRODS(localFile, targetIrodsCollectionFile, transferControlBlock, null, false);

		targetIrodsCollectionFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection, testFileName);

		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsCollectionFile.getAbsolutePath(),
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
	}

	@Test
	public void testGetListingEntryForAbsolutePath() throws Exception {
		// generate a local scratch file
		String testFileName = "testGetListingEntryForAbsolutePath.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 20);

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		TransferOptions transferOptions = new TransferOptions();
		transferOptions.setComputeAndVerifyChecksumAfterTransfer(true);
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock.instance();
		transferControlBlock.setTransferOptions(transferOptions);

		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, transferControlBlock, null, false);

		ObjStat objStat = dataObjectAO.retrieveObjStat(destFile.getAbsolutePath());
		CollectionAndDataObjectListingEntry actual = dataObjectAO
				.getListingEntryForAbsolutePath(destFile.getAbsolutePath());
		CollectionAndPath collectionAndPath = MiscIRODSUtils
				.separateCollectionAndPathFromGivenAbsolutePath(destFile.getAbsolutePath());
		Assert.assertNotNull(actual);
		Assert.assertEquals(objStat.getAbsolutePath(), actual.getFormattedAbsolutePath());
		Assert.assertEquals(collectionAndPath.getCollectionParent(), actual.getParentPath());
		Assert.assertEquals(objStat.getObjSize(), actual.getDataSize());
		Assert.assertEquals(objStat.getOwnerName(), actual.getOwnerName());
		Assert.assertEquals(objStat.getOwnerZone(), actual.getOwnerZone());
		Assert.assertEquals(objStat.getCreatedAt(), actual.getCreatedAt());
		Assert.assertEquals(objStat.getModifiedAt(), actual.getModifiedAt());
		Assert.assertEquals(objStat.getObjectType(), actual.getObjectType());
		Assert.assertEquals(objStat.getSpecColType(), actual.getSpecColType());

	}

	@Test
	public void testPutFileVerifyChecksum() throws Exception {
		// generate a local scratch file
		String testFileName = "testPutFileVerifyChecksum.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 20);

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		TransferOptions transferOptions = new TransferOptions();
		transferOptions.setComputeAndVerifyChecksumAfterTransfer(true);
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock.instance();
		transferControlBlock.setTransferOptions(transferOptions);

		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, transferControlBlock, null, false);

		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsFile, accessObjectFactory, irodsAccount);
	}

	@Test
	public void testParallelPutFileVerifyChecksum() throws Exception {

		if (!testingPropertiesHelper.isTestParallelTransfer(testingProperties)) {
			return;
		}

		// generate a local scratch file
		String testFileName = "testParallelPutFileVerifyChecksum.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				33 * 1024 * 1024);

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		TransferOptions transferOptions = new TransferOptions();
		transferOptions.setComputeAndVerifyChecksumAfterTransfer(true);
		transferOptions.setMaxThreads(5);
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock.instance();
		transferControlBlock.setTransferOptions(transferOptions);

		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, transferControlBlock, null, false);
		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsFile, accessObjectFactory, irodsAccount);
	}

	@Test
	public final void testGetWithChecksumVerification() throws Exception {

		// generate a local scratch file
		String testFileName = "testGetWithChecksumVerification.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);
		String getFileName = "testGetWithChecksumVerificationResult.txt";
		String getResultLocalPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File localFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);

		dto.putOperation(localFileName, targetIrodsCollection, "", null, null);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO.instanceIRODSFileForPath(targetIrodsCollection + '/' + testFileName);

		TransferControlBlock tcb = irodsFileSystem.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setComputeAndVerifyChecksumAfterTransfer(true);

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile, tcb, null);

		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH + '/' + getFileName);
		assertionHelper.assertLocalScratchFileLengthEquals(IRODS_TEST_SUBDIR_PATH + '/' + getFileName, 100);

	}

	@Test
	public final void testParallelGetWithChecksumVerification() throws Exception {

		if (!testingPropertiesHelper.isTestParallelTransfer(testingProperties)) {
			return;
		}

		// generate a local scratch file
		String testFileName = "testParallelGetWithChecksumVerification.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String sourceFileAbsolutePath = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				80000 * 1024);

		String getResultFileName = "testParallelGetWithChecksumVerificationResult.txt";
		String getResultLocalPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getResultFileName;
		File getResultLocalFile = new File(getResultLocalPath);
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);

		dto.putOperation(sourceFileAbsolutePath, targetIrodsCollection, "", null, null);

		accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO.instanceIRODSFileForPath(targetIrodsCollection + '/' + testFileName);

		TransferControlBlock tcb = irodsFileSystem.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setComputeAndVerifyChecksumAfterTransfer(true);

		dataObjectAO.getDataObjectFromIrods(irodsFile, getResultLocalFile, tcb, null);

		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH + '/' + getResultFileName);

	}

	/**
	 * Bug 629-malloc/resource error in irods when doing findDomainByMetadataQuery
	 */
	@Test
	public void testFindDataObjectDomainDataByAVUQueryForBug629() throws Exception {

		String testCollName = "testFindDataObjectDomainDataByAVUQueryForBug629";
		String testFilePrefix = "testFindDataObjectDomainDataByAVUQueryForBug629-";
		String testFileSuffix = ".txt";
		int count = 200;
		String expectedAttribName = "testattrib1";
		String expectedAttribValue = "testvalue1";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testCollName);

		IRODSAccessObjectFactory aoFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		// generate some test files, first delete the test subdir

		IRODSFile testSubdir = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		testSubdir.delete();
		testSubdir.mkdirs();

		DataObjectAO dAO = aoFactory.getDataObjectAO(irodsAccount);
		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);

		AvuData avuData = null;

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String sourceFileAbsolutePath = FileGenerator.generateFileOfFixedLengthGivenName(absPath, "testFileForAVU.txt",
				1);
		File sourceFile = new File(sourceFileAbsolutePath);

		IRODSFile dataFile = null;
		StringBuilder sb = null;
		for (int i = 0; i < count; i++) {
			sb = new StringBuilder();
			sb.append(testFilePrefix);
			sb.append(i);
			sb.append(testFileSuffix);
			dataFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(testSubdir.getAbsolutePath(),
					sb.toString());
			dto.putOperation(sourceFile, dataFile, null, null);
			avuData = AvuData.instance(expectedAttribName, expectedAttribValue + i, "");
			dAO.addAVUMetadata(dataFile.getAbsolutePath(), avuData);

		}

		ArrayList<AVUQueryElement> avus = new ArrayList<AVUQueryElement>();
		avus.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.ATTRIBUTE, QueryConditionOperators.EQUAL,
				expectedAttribName));
		avus.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.VALUE, QueryConditionOperators.LIKE,

				expectedAttribValue + "%"));

		List<DataObject> files = dAO.findDomainByMetadataQuery(avus);
		Assert.assertNotNull("null files returned", files);
		Assert.assertTrue("did not get all of the files", files.size() >= count);

	}

	/**
	 * Bug 629-malloc/resource error in irods when doing findDomainByMetadataQuery
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testFindDataObjectDomainDataByAVUQueryForBug629NoQueryElements() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		DataObjectAO dAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		ArrayList<AVUQueryElement> avus = new ArrayList<>();

		dAO.findDomainByMetadataQuery(avus);

	}

	@Test
	public final void testIsUserHasAccessWhenUserWhoHasRead() throws Exception {
		// generate a local scratch file

		String testFileName = "testIsUserHasAccessWhenUserWhoHasRead.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig), irodsFile, true);

		dataObjectAO.setAccessPermissionRead("", targetIrodsCollection + "/" + testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		// log in as the secondary user and test read access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSFile irodsFileForSecondaryUser = irodsFileSystem.getIRODSFileFactory(secondaryAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);

		boolean hasAccess = dataObjectAO.isUserHasAccess(irodsFileForSecondaryUser.getAbsolutePath(),
				secondaryAccount.getUserName());
		Assert.assertTrue("did not have expected access", hasAccess);
	}

	@Test
	public final void testIsUserHasAccessWhenUserHasNoAccess() throws Exception {
		// generate a local scratch file

		String testFileName = "testIsUserHasAccessWhenUserHasNoAccess.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig), irodsFile, true);

		// log in as the secondary user and test read access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromTertiaryTestProperties(testingProperties);
		IRODSFile irodsFileForSecondaryUser = irodsFileSystem.getIRODSFileFactory(secondaryAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);

		boolean hasAccess = dataObjectAO.isUserHasAccess(irodsFileForSecondaryUser.getAbsolutePath(),
				secondaryAccount.getUserName());
		Assert.assertFalse("should not have expected access", hasAccess);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testIsUserHasAccessNullFileName() throws Exception {
		// generate a local scratch file

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		FileCatalogObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		dataObjectAO.isUserHasAccess(null, "hello");
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testIsUserHasAccessBlankFileName() throws Exception {
		// generate a local scratch file

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		FileCatalogObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		dataObjectAO.isUserHasAccess("", "hello");
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testIsUserHasAccessNullUserName() throws Exception {
		// generate a local scratch file

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		FileCatalogObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		dataObjectAO.isUserHasAccess("file", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testIsUserHasAccessBlankUserName() throws Exception {
		// generate a local scratch file

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		FileCatalogObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		dataObjectAO.isUserHasAccess("file", "");
	}

	@Test
	public final void testListReplicationsForFileInResGroup() throws Exception {

		if (testingPropertiesHelper.isTestEirods(testingProperties)) {
			return;
		}

		String testFileName = "testListReplicationsForFileInResGroup.txt";
		String testDir = "testListReplicationsForFileInResGroup";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testDir);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		// resource groups not a thing anymore
		if (accessObjectFactory.getIRODSServerProperties(irodsAccount).isAtLeastIrods410()) {
			return;
		}

		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection);
		irodsFile.delete();
		irodsFile.mkdirs();

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(fileNameOrig, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO.replicateIrodsDataObjectToAllResourcesInResourceGroup(targetIrodsCollection + "/" + testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_GROUP_KEY));

		List<DataObject> resources = dataObjectAO.listReplicationsForFileInResGroup(targetIrodsCollection, testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_GROUP_KEY));

		Assert.assertEquals(1, resources.size());

	}

	@Test
	public final void testListReplicationsForFileInResGroupNonExistent() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		if (accessObjectFactory.getIRODSServerProperties(irodsAccount).isAtLeastIrods410()) {
			return;
		}

		String testFileName = "testListReplicationsForFileInResGroup.txt";
		String testDir = "testListReplicationsForFileInResGroup";

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testDir);

		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection);
		irodsFile.delete();
		irodsFile.mkdirs();

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		List<DataObject> resources = dataObjectAO.listReplicationsForFileInResGroup(targetIrodsCollection, testFileName,
				"bogus name");

		Assert.assertEquals(0, resources.size());

	}

	@Test(expected = UnsupportedOperationException.class)
	public final void testListReplicationsForFileInResGroupNonExistentWhenIrods41Plus() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		if (!accessObjectFactory.getIRODSServerProperties(irodsAccount).isAtLeastIrods410()) {
			throw new UnsupportedOperationException("throw for expected exception");
		}

		String testFileName = "testListReplicationsForFileInResGroup.txt";
		String testDir = "testListReplicationsForFileInResGroup";

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testDir);

		dataObjectAO.listReplicationsForFileInResGroup(targetIrodsCollection, testFileName, "bogus name");

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testListReplicationsForFileInResGroupNullCollection() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		if (accessObjectFactory.getIRODSServerProperties(irodsAccount).isAtLeastIrods410()) {
			throw new IllegalArgumentException("throw for expected exception");
		}

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO.listReplicationsForFileInResGroup(null, "blah", "bogus name");

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testListReplicationsForFileInResGroupBlankFile() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		if (accessObjectFactory.getIRODSServerProperties(irodsAccount).isAtLeastIrods410()) {
			throw new IllegalArgumentException("throw for expected exception");
		}

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO.listReplicationsForFileInResGroup("coll", "", "bogus name");

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testListReplicationsForFileInResGroupNullRescGroup() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		if (accessObjectFactory.getIRODSServerProperties(irodsAccount).isAtLeastIrods410()) {
			throw new IllegalArgumentException("throw for expected exception");
		}

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO.listReplicationsForFileInResGroup("coll", "xxx", null);
	}

	@Test
	public final void testGetTotalNumberOfReplsForDataObject() throws Exception {

		String testFileName = "testGetTotalNumberOfReplsForDataObject.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);
		irodsFile.delete();

		irodsFile.setResource(testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY));
		File localFile = new File(fileNameOrig);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);

		dataObjectAO.putLocalDataObjectToIRODS(localFile, irodsFile, true);

		dataObjectAO.replicateIrodsDataObject(targetIrodsCollection + '/' + testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY));

		int replicas = dataObjectAO.getTotalNumberOfReplsForDataObject(targetIrodsCollection, testFileName);
		Assert.assertEquals("did not count two replicas", 2, replicas);

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testGetTotalNumberOfReplsForDataObjectNullCollection() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO.getTotalNumberOfReplsForDataObject(null, "blah");

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testGetTotalNumberOfReplsForDataObjectBlankFile() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO.getTotalNumberOfReplsForDataObject("coll", "");

	}

	@Test
	public final void testCountReplicationsForFileInResGroup() throws Exception {

		if (testingPropertiesHelper.isTestEirods(testingProperties)) {
			return;
		}

		String testFileName = "testCountReplicationsForFileInResGroup.txt";
		String testDir = "testCountReplicationsForFileInResGroup";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testDir);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		// resource groups not a thing anymore
		if (accessObjectFactory.getIRODSServerProperties(irodsAccount).isAtLeastIrods410()) {
			return;
		}

		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection);
		irodsFile.delete();
		irodsFile.mkdirs();

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(fileNameOrig, targetIrodsCollection,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO.replicateIrodsDataObjectToAllResourcesInResourceGroup(targetIrodsCollection + "/" + testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_GROUP_KEY));

		int nbrRepls = dataObjectAO.getTotalNumberOfReplsInResourceGroupForDataObject(targetIrodsCollection,
				testFileName, testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_GROUP_KEY));

		Assert.assertEquals(1, nbrRepls);

	}

	@Test
	public final void testTrimReplicasForDataObjectByResourceName() throws Exception {

		String testFileName = "testTrimReplicasForDataObject.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);
		irodsFile.delete();

		irodsFile.setResource(testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY));
		File localFile = new File(fileNameOrig);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);

		dataObjectAO.putLocalDataObjectToIRODS(localFile, irodsFile, true);

		dataObjectAO.replicateIrodsDataObject(targetIrodsCollection + '/' + testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY));

		int replicas = dataObjectAO.getTotalNumberOfReplsForDataObject(targetIrodsCollection, testFileName);
		Assert.assertEquals("did not count two replicas", 2, replicas);

		dataObjectAO.trimDataObjectReplicas(targetIrodsCollection, testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY), 1, -1, false);

		replicas = dataObjectAO.getTotalNumberOfReplsForDataObject(targetIrodsCollection, testFileName);
		Assert.assertEquals("did not count one replica, should have trimmed 1", 1, replicas);

	}

	@Test
	public final void testTrimReplicasForDataObjectByResourceNameInvalid() throws Exception {

		String testFileName = "testTrimReplicasForDataObjectByResourceNameInvalid.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);
		irodsFile.delete();

		irodsFile.setResource(testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY));
		File localFile = new File(fileNameOrig);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);

		dataObjectAO.putLocalDataObjectToIRODS(localFile, irodsFile, true);

		dataObjectAO.replicateIrodsDataObject(targetIrodsCollection + '/' + testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY));

		int replicas = dataObjectAO.getTotalNumberOfReplsForDataObject(targetIrodsCollection, testFileName);
		Assert.assertEquals("did not count two replicas", 2, replicas);

		try {
			dataObjectAO.trimDataObjectReplicas(targetIrodsCollection, testFileName, "invalid resource name", -1, -1,
					false);
		} catch (DataNotFoundException dnf) {
			if (irodsFileSystem.getIRODSAccessObjectFactory().getIRODSServerProperties(irodsAccount)
					.isAtLeastIrods410()) {
				return;
			} else {
				Assert.fail("should not throw an exception here");
			}
		}

		replicas = dataObjectAO.getTotalNumberOfReplsForDataObject(targetIrodsCollection, testFileName);
		Assert.assertEquals("should not have trimed anything", 2, replicas);

	}

	@Test(expected = DataNotFoundException.class)
	public final void testTrimReplicasForDataObjectInvalidFileName() throws Exception {

		String testFileName = "testTrimReplicasForDataObjectInvalidFileName.txt";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		dataObjectAO.trimDataObjectReplicas(targetIrodsCollection, testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY), -1, -1, false);

	}

	@Test
	public final void testTrimReplicasForDataObjectByReplicaNumber() throws Exception {

		String testFileName = "testTrimReplicasForDataObjectByReplicaNumber.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);
		irodsFile.delete();

		irodsFile.setResource(testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY));
		File localFile = new File(fileNameOrig);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);

		dataObjectAO.putLocalDataObjectToIRODS(localFile, irodsFile, true);

		dataObjectAO.replicateIrodsDataObject(targetIrodsCollection + '/' + testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY));

		// List<Resource>
		// resourcdataObjectAO.listFileResources(targetIrodsCollection + '/' +
		// testFileName);

		List<DataObject> replicas = dataObjectAO.listReplicationsForFile(targetIrodsCollection, testFileName);

		Assert.assertEquals("did not count two replicas", 2, replicas.size());

		DataObject firstDataObject = replicas.get(0);

		dataObjectAO.trimDataObjectReplicas(targetIrodsCollection, testFileName, "", 1,
				firstDataObject.getDataReplicationNumber(), false);

		int ctr = dataObjectAO.getTotalNumberOfReplsForDataObject(targetIrodsCollection, testFileName);
		Assert.assertEquals("did not count one replica, should have trimmed 1", 1, ctr);

	}

	@Test
	public final void testListReplicasForDataObject() throws Exception {

		String testFileName = "testListReplicasForDataObject.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);
		irodsFile.delete();

		irodsFile.setResource(testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY));
		File localFile = new File(fileNameOrig);

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);

		dataObjectAO.putLocalDataObjectToIRODS(localFile, irodsFile, true);

		dataObjectAO.replicateIrodsDataObject(targetIrodsCollection + '/' + testFileName,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY));

		List<DataObject> replicatedObjs = dataObjectAO.listReplicationsForFile(targetIrodsCollection, testFileName);
		Assert.assertEquals("did not count two replicas", 2, replicatedObjs.size());

	}

	@Test
	public final void testListReplicasForDataObjectNotFound() throws Exception {

		String testFileName = "testListReplicasForDataObjectNotFOund.txt";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		dataObjectAO.listReplicationsForFile(targetIrodsCollection, testFileName);

	}

	@Test
	public void testBulkDeleteAVUMetadataFromDataObject() throws Exception {
		String testFileName = "testBulkDeleteAVUMetadataFromDataObject.txt";
		String expectedAttribName = "testBulkDeleteAVUMetadataFromDataObject";
		String expectedValueName = "testval1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/" + testFileName;

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		targetIrodsFile.delete();
		targetIrodsFile.mkdirs();
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig), targetIrodsFile, null, null);

		AvuData avuData = AvuData.instance(expectedAttribName, expectedValueName, "");

		List<AvuData> bulkAvuData = new ArrayList<>();
		bulkAvuData.add(avuData);
		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		List<BulkAVUOperationResponse> response = dataObjectAO.addBulkAVUMetadataToDataObject(targetIrodsDataObject,
				bulkAvuData);

		// now delete it

		response = dataObjectAO.deleteBulkAVUMetadataFromDataObject(targetIrodsDataObject, bulkAvuData);

		Assert.assertNotNull(response);
		Assert.assertFalse(response.isEmpty());

		List<AVUQueryElement> avuQueryElements = new ArrayList<AVUQueryElement>();
		avuQueryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, expectedAttribName));

		List<DataObject> dataObjects = dataObjectAO.findDomainByMetadataQuery(avuQueryElements);
		Assert.assertFalse(dataObjects.size() >= 1);
	}

	@Test
	public void testDeleteAllAVUMetadataFromDataObject() throws Exception {
		String testFileName = "testDeleteAllAVUMetadataFromDataObject.txt";
		String expectedAttribName = "testDeleteAllAVUMetadataFromDataObject";
		String expectedValueName = "testval1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/" + testFileName;

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		targetIrodsFile.delete();
		targetIrodsFile.mkdirs();
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig), targetIrodsFile, null, null);

		AvuData avuData = AvuData.instance(expectedAttribName, expectedValueName, "");

		List<AvuData> bulkAvuData = new ArrayList<>();
		bulkAvuData.add(avuData);
		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		List<BulkAVUOperationResponse> response = dataObjectAO.addBulkAVUMetadataToDataObject(targetIrodsDataObject,
				bulkAvuData);

		// now delete it

		dataObjectAO.deleteAllAVUForDataObject(targetIrodsDataObject);

		Assert.assertNotNull(response);
		Assert.assertFalse(response.isEmpty());

		List<AVUQueryElement> avuQueryElements = new ArrayList<AVUQueryElement>();
		avuQueryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, expectedAttribName));

		List<DataObject> dataObjects = dataObjectAO.findDomainByMetadataQuery(avuQueryElements);
		Assert.assertFalse(dataObjects.size() >= 1);

	}

	@Test
	public void testListDataTypes() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) accessObjectFactory.getDataObjectAO(irodsAccount);
		List<String> dataTypes = dataObjectAO.listDataTypes();
		Assert.assertNotNull("no data types returned", dataTypes);
		Assert.assertFalse("no data types returned", dataTypes.isEmpty());
	}

	@Ignore // FIXME: pending https://github.com/DICE-UNC/jargon/issues/319
	public void testSysmetaModifyExpireDate() throws Exception {
		// generate a local scratch file
		String testFileName = "testSysmetaModifyExpireDate.dat";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		// now put the file
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);

		dto.putOperation(localFileName, targetIrodsFile, "", null, null);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		Date dateNow = new Date();
		dataObjectAO.modifyDataObjectSysTime(dateNow, targetIrodsFile);

		// FIXME: need to look up and do assertion

		DataObject dataObject = dataObjectAO.findByAbsolutePath(targetIrodsFile);
		TestCase.assertNotNull("no data object found", dataObject);

	}

}
