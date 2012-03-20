package org.irods.jargon.core.pub;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.OverwriteException;
import org.irods.jargon.core.packinstr.TransferOptions;
import org.irods.jargon.core.packinstr.TransferOptions.ForceOption;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.domain.Resource;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.pub.domain.UserGroup;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.remoteexecute.RemoteExecuteServiceImpl;
import org.irods.jargon.core.transfer.DefaultTransferControlBlock;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener.CallbackResponse;
import org.irods.jargon.core.transfer.TransferStatusCallbackListenerTestingImplementation;
import org.irods.jargon.testutils.AssertionHelper;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.irods.jargon.testutils.icommandinvoke.IcommandException;
import org.irods.jargon.testutils.icommandinvoke.IcommandInvoker;
import org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext;
import org.irods.jargon.testutils.icommandinvoke.icommands.IlsCommand;
import org.irods.jargon.testutils.icommandinvoke.icommands.ImetaAddCommand;
import org.irods.jargon.testutils.icommandinvoke.icommands.ImetaCommand.MetaObjectType;
import org.irods.jargon.testutils.icommandinvoke.icommands.ImetaRemoveCommand;
import org.irods.jargon.testutils.icommandinvoke.icommands.ImkdirCommand;
import org.irods.jargon.testutils.icommandinvoke.icommands.IputCommand;
import org.irods.jargon.testutils.icommandinvoke.icommands.IrmCommand;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

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
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		assertionHelper = new AssertionHelper();
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testDataObjectAOImpl() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		irodsSession.closeSession();
		Assert.assertNotNull(dataObjectAO);
	}

	@Test
	public void testPutWithTargetSpecifiedAsCollection() throws Exception {
		// generate a local scratch file
		String testFileName = "testPutWithTargetSpecifiedAsCollection.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		File localFile = new File(localFileName);

		// now put the file
		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, true);

		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsFile + "/"
				+ testFileName);
	}

	@Test
	public void testPutOverwriteFileNotInIRODS() throws Exception {
		// generate a local scratch file
		String testFileName = "testPutOverwriteFileNotInIRODS.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, true);
		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsFile);
	}

	@Test
	public void testPutExecutableFile() throws Exception {
		// generate a local scratch file
		String testFileName = "testPutExecutableFile.sh";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);
		localFile.setExecutable(true);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, true);
		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsFile);
	}

	@Test
	public void testChecksumAndPut0KFile() throws Exception {
		// generate a local scratch file
		String testFileName = "testChecksumAndPut0KFile.sh";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = absPath + testFileName;
		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);
		localFile.createNewFile();

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		TransferOptions transferOptions = new TransferOptions();
		transferOptions.setComputeAndVerifyChecksumAfterTransfer(true);
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();
		transferControlBlock.setTransferOptions(transferOptions);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile,
				transferControlBlock, null);
		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsFile);
	}

	@Test(expected = JargonException.class)
	public void testPutOverwriteUnknownCollection() throws Exception {
		// generate a local scratch file
		String testFileName = "testPutOverwriteFileNotInIRODS.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH
								+ "/Imnothererightnow/" + testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, true);
	}

	@Test(expected = JargonException.class)
	public void testPutOverwriteNoLocalFile() throws Exception {
		// generate a local scratch file
		String testFileName = "IdontExistMate.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH
								+ "/Imnothererightnow/" + testFileName);
		File localFile = new File(absPath + '/' + testFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, true);
	}

	/**
	 * put a file with no force, there will be no callback listener
	 * 
	 * @throws Exception
	 */
	@Test(expected = OverwriteException.class)
	public void testPutNoOverwriteFileAlreadyPresentNoCallbackListener()
			throws Exception {
		// generate a local scratch file
		String testFileName = "testPutNoOverwriteFileAlreadyPresentNoCallbackListener.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, false);
		// second put to do test
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, null, null);
	}

	/**
	 * put a file with no force configured and a callback listener
	 * 
	 * @throws Exception
	 */
	@Test(expected = OverwriteException.class)
	public void testPutNoOverwriteFileAlreadyPresentNoForceInOptionsNoCallbackListener()
			throws Exception {
		// generate a local scratch file
		String testFileName = "testPutNoOverwriteFileAlreadyPresentNoForceInOptionsNoCallbackListener.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 3);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, false);
		// second put to do test
		TransferControlBlock tcb = accessObjectFactory
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.NO_FORCE);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, null, null);
	}

	/**
	 * put a file with ask listener configured and a callback listener that says
	 * don't overwrite
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPutNoOverwriteFileAlreadyPresentAskCallbackListenerSaysNoThisFile()
			throws Exception {
		int firstLength = 3;
		int secondLength = 5;
		// generate a local scratch file
		String testFileName = "testPutNoOverwriteFileAlreadyPresentAskCallbackListenerSaysNoThisFile.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						firstLength);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, false);
		// second put to do test, update file to diff length to make sure is
		// skipped
		localFileName = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, secondLength);
		TransferControlBlock tcb = accessObjectFactory
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(
				ForceOption.ASK_CALLBACK_LISTENER);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();
		transferStatusCallbackListener
				.setForceOption(CallbackResponse.NO_THIS_FILE);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, tcb,
				transferStatusCallbackListener);

		destFile.reset();
		Assert.assertEquals("should have skipped file and not overwritten",
				firstLength, destFile.length());
		Assert.assertEquals(
				"transferControlBlock should not have been overwritten",
				ForceOption.ASK_CALLBACK_LISTENER, tcb.getTransferOptions()
						.getForceOption());

	}

	/**
	 * put a file with ask listener configured and a callback listener that says
	 * don't overwrite for all files
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPutNoOverwriteFileAlreadyPresentAskCallbackListenerSaysNoAllFiles()
			throws Exception {
		int firstLength = 3;
		int secondLength = 5;
		// generate a local scratch file
		String testFileName = "testPutNoOverwriteFileAlreadyPresentAskCallbackListenerSaysNoAllFiles.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						firstLength);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, false);
		// second put to do test, update file to diff length to make sure is
		// skipped
		localFileName = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, secondLength);
		TransferControlBlock tcb = accessObjectFactory
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(
				ForceOption.ASK_CALLBACK_LISTENER);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();
		transferStatusCallbackListener
				.setForceOption(CallbackResponse.NO_FOR_ALL);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, tcb,
				transferStatusCallbackListener);

		destFile.reset();
		Assert.assertEquals("should have skipped file and not overwritten",
				firstLength, destFile.length());
		Assert.assertEquals(
				"transferControlBlock should have been overwritten",
				ForceOption.NO_FORCE, tcb.getTransferOptions().getForceOption());

	}

	/**
	 * put a file with ask listener configured and a callback listener that says
	 * overwrite for this one file
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPutNoOverwriteFileAlreadyPresentAskCallbackListenerSaysYesThisFile()
			throws Exception {
		int firstLength = 3;
		int secondLength = 5;
		// generate a local scratch file
		String testFileName = "testPutNoOverwriteFileAlreadyPresentAskCallbackListenerSaysYesThisFile.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						firstLength);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, false);

		localFileName = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, secondLength);
		TransferControlBlock tcb = accessObjectFactory
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(
				ForceOption.ASK_CALLBACK_LISTENER);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();
		transferStatusCallbackListener
				.setForceOption(CallbackResponse.YES_THIS_FILE);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, tcb,
				transferStatusCallbackListener);

		destFile.reset();
		Assert.assertEquals("should have overwritten file", secondLength,
				destFile.length());
		Assert.assertEquals(
				"transferControlBlock should not have been overwritten",
				ForceOption.ASK_CALLBACK_LISTENER, tcb.getTransferOptions()
						.getForceOption());

	}

	/**
	 * put a file with ask listener configured and a callback listener that says
	 * overwrite for this one file
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPutNoOverwriteFileAlreadyPresentAskCallbackListenerSaysYesAllFiles()
			throws Exception {
		int firstLength = 3;
		int secondLength = 5;
		// generate a local scratch file
		String testFileName = "testPutNoOverwriteFileAlreadyPresentAskCallbackListenerSaysYesAllFiles.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						firstLength);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, false);

		localFileName = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, secondLength);
		TransferControlBlock tcb = accessObjectFactory
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(
				ForceOption.ASK_CALLBACK_LISTENER);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();
		transferStatusCallbackListener
				.setForceOption(CallbackResponse.YES_FOR_ALL);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, tcb,
				transferStatusCallbackListener);

		destFile.reset();
		Assert.assertEquals("should have overwritten file", secondLength,
				destFile.length());
		Assert.assertEquals(
				"transferControlBlock should have been overwritten",
				ForceOption.USE_FORCE, tcb.getTransferOptions()
						.getForceOption());

	}

	@Test
	public void testPutOverwriteFileAlreadyPresent() throws Exception {
		// generate a local scratch file
		String testFileName = "testPutOverwriteFileAlreadyPresent.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// put scratch file into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		iputCommand.setLocalFileName(localFileName);
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, true);
		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsFile);
	}

	@Test
	public void testFindByCollectionPathAndDataName() throws Exception {
		// generate a local scratch file
		String testFileName = "testFindByCollectionPathAndDataName.dat";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 10);

		testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		new File(localFileName);

		// put scratch file into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		iputCommand.setLocalFileName(localFileName);
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);

		DataObject dataObject = dataObjectAO.findByCollectionNameAndDataName(
				targetIrodsCollection, testFileName);
		Assert.assertNotNull("null data object, was not found", dataObject);

	}

	@Test
	public void testFindByAbsolutePath() throws Exception {
		// generate a local scratch file
		String testFileName = "testFindByAbsolutePath.dat";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		new File(localFileName);

		// put scratch file into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		iputCommand.setLocalFileName(localFileName);
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		// now put the file
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		DataObject dataObject = dataObjectAO
				.findByAbsolutePath(targetIrodsFile);
		Assert.assertNotNull("null data object, was not found", dataObject);

	}

	@Test(expected = DataNotFoundException.class)
	public void testFindByAbsolutePathWhenIsACollection() throws Exception {
		// generate a local scratch file

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		DataObject dataObject = dataObjectAO
				.findByAbsolutePath(targetIrodsFile);
		Assert.assertNotNull("null data object, was not found", dataObject);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testFindByAbsolutePathNullFileName() throws Exception {
		// generate a local scratch file
		String testFileName = null;

		// now put the file
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		dataObjectAO.findByAbsolutePath(testFileName);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testFindByAbsolutePathBlankFileName() throws Exception {
		// generate a local scratch file
		String testFileName = "";

		// now put the file
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		dataObjectAO.findByAbsolutePath(testFileName);

	}

	@Test
	public void testFindWhere() throws Exception {
		// generate a local scratch file
		String testFileName = "testFindWhere.dat";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 10);

		testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		new File(localFileName);

		// put scratch file into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		iputCommand.setLocalFileName(localFileName);
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);

		String query = RodsGenQueryEnum.COL_DATA_SIZE.getName() + " >  '0'";

		List<DataObject> dataObjects = dataObjectAO.findWhere(query);
		Assert.assertNotNull(
				"null list returned, should be empty list if no data",
				dataObjects);
		Assert.assertTrue("no results returned, expected at least one",
				dataObjects.size() > 0);
	}

	@Test
	public void instanceIRODSFileForPath() throws Exception {

		// generate a local scratch file
		String testFileName = "testGetIRODSFileForPath.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 10);

		testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		new File(localFileName);

		// put scratch file into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		iputCommand.setLocalFileName(localFileName);
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO
				.instanceIRODSFileForPath(targetIrodsCollection + '/'
						+ testFileName);
		Assert.assertNotNull("irodsFile was null", irodsFile);
		Assert.assertTrue("this file did not exist", irodsFile.exists());
		Assert.assertTrue("was not a file", irodsFile.isFile());

	}

	@Test
	public void instanceIRODSFileForPathNotExists() throws Exception {

		// generate a local scratch file
		String testFileName = "idontexistForThisPath.csv";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO
				.instanceIRODSFileForPath(targetIrodsCollection + '/'
						+ testFileName);
		Assert.assertNotNull("irodsFile was null", irodsFile);
		Assert.assertFalse("this file exists", irodsFile.exists());
	}

	@Test
	public final void testGet() throws Exception {

		String testFileName = "testGet.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String getFileName = "testGetResult.txt";
		String getResultLocalPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File localFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dataTransferOperations
				.putOperation(
						localFileName,
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						null, null);

		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO
				.instanceIRODSFileForPath(targetIrodsCollection + '/'
						+ testFileName);

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile);

		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH
				+ '/' + getFileName);
		assertionHelper.assertLocalScratchFileLengthEquals(
				IRODS_TEST_SUBDIR_PATH + '/' + getFileName, 100);

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
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String getFileName = "testGetLocalFileAlreadyExistsResult.txt";
		String getResultLocalPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File localFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dataTransferOperations
				.putOperation(
						localFileName,
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						null, null);

		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO
				.instanceIRODSFileForPath(targetIrodsCollection + '/'
						+ testFileName);

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile);
		// now I know the local file exits, get again and see an overwrite
		// errror
		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile);

	}

	/**
	 * Do a get when the local file aready exists (should throw an error for
	 * overwrite)
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testGetLocalFileAskCallbackListenerGetAYes()
			throws Exception {

		String testFileName = "testGetLocalFileAskCallbackListenerGetAYes.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String getFileName = "testGetLocalFileAskCallbackListenerGetAYesResult.txt";
		String getResultLocalPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File localFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dataTransferOperations
				.putOperation(
						localFileName,
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						null, null);

		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO
				.instanceIRODSFileForPath(targetIrodsCollection + '/'
						+ testFileName);

		TransferControlBlock transferControlBlock = irodsFileSystem
				.getIrodsSession()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		transferControlBlock.getTransferOptions().setForceOption(
				ForceOption.ASK_CALLBACK_LISTENER);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();
		transferStatusCallbackListener
				.setForceOption(CallbackResponse.YES_THIS_FILE);

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile);
		// now I know the local file exits, get again and it should overwrite
		// with no error
		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile,
				transferControlBlock, transferStatusCallbackListener);
		// check tomake sure transfer control block was not altered
		Assert.assertEquals(
				"transfer control block should not have been altered",
				ForceOption.ASK_CALLBACK_LISTENER, transferControlBlock
						.getTransferOptions().getForceOption());
	}

	/**
	 * Do a get when the local file aready exists, ask the callback listener,
	 * and get a no for this file. This should not be in error
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testGetLocalFileAskCallbackListenerGetANo()
			throws Exception {

		int firstLength = 13;
		int secondLength = 32;
		String sourceFileName = "testGetLocalFileAskCallbackListenerGetANo.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String sourceLocalFileAbsolutePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, sourceFileName,
						firstLength);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String getFileName = "testGetLocalFileAskCallbackListenerGetANoResult.txt";
		String getResultLocalPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File getResultLocalFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dataTransferOperations
				.putOperation(
						sourceLocalFileAbsolutePath,
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						null, null);

		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO
				.instanceIRODSFileForPath(targetIrodsCollection + '/'
						+ sourceFileName);

		TransferControlBlock transferControlBlock = irodsFileSystem
				.getIrodsSession()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		transferControlBlock.getTransferOptions().setForceOption(
				ForceOption.ASK_CALLBACK_LISTENER);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();
		transferStatusCallbackListener
				.setForceOption(CallbackResponse.NO_THIS_FILE);

		dataObjectAO.getDataObjectFromIrods(irodsFile, getResultLocalFile,
				transferControlBlock, transferStatusCallbackListener);
		// now I know the local file exits, get again and it should overwrite
		// with no error, check it was truly overwritten by making source file a
		// different length and checking

		irodsFile.deleteWithForceOption();
		sourceLocalFileAbsolutePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, sourceFileName,
						secondLength);
		dataTransferOperations
				.putOperation(
						sourceLocalFileAbsolutePath,
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						null, null);

		dataObjectAO.getDataObjectFromIrods(irodsFile, getResultLocalFile,
				transferControlBlock, transferStatusCallbackListener);
		// check tomake sure transfer control block was not altered
		Assert.assertEquals(
				"transfer control block should not have been alteredl",
				ForceOption.ASK_CALLBACK_LISTENER, transferControlBlock
						.getTransferOptions().getForceOption());
		Assert.assertEquals("file should not be the new length", firstLength,
				getResultLocalFile.length());
	}

	/**
	 * Do a get with an overwrite situation, callback listener set to be asked,
	 * but no callback listener provided (this is an error state)
	 * 
	 * @throws Exception
	 */
	@Test(expected = OverwriteException.class)
	public final void testGetLocalFileAskCallbackListenerNoCallbackListenerSet()
			throws Exception {

		int firstLength = 13;
		int secondLength = 32;
		String sourceFileName = "testGetLocalFileAskCallbackListenerNoCallbackListenerSet.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String sourceLocalFileAbsolutePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, sourceFileName,
						firstLength);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String getFileName = "testGetLocalFileAskCallbackListenerNoCallbackListenerSetResult.txt";
		String getResultLocalPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File getResultLocalFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dataTransferOperations
				.putOperation(
						sourceLocalFileAbsolutePath,
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						null, null);

		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO
				.instanceIRODSFileForPath(targetIrodsCollection + '/'
						+ sourceFileName);

		TransferControlBlock transferControlBlock = irodsFileSystem
				.getIrodsSession()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		transferControlBlock.getTransferOptions().setForceOption(
				ForceOption.ASK_CALLBACK_LISTENER);

		dataObjectAO.getDataObjectFromIrods(irodsFile, getResultLocalFile,
				transferControlBlock, null);
		// now I know the local file exits, get again and it should overwrite
		// with no error, check it was truly overwritten by making source file a
		// different length and checking

		irodsFile.deleteWithForceOption();
		sourceLocalFileAbsolutePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, sourceFileName,
						secondLength);
		dataTransferOperations
				.putOperation(
						sourceLocalFileAbsolutePath,
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						null, null);

		dataObjectAO.getDataObjectFromIrods(irodsFile, getResultLocalFile,
				transferControlBlock, null);
		// expect an overwrite exception
	}

	/**
	 * Do a get when the local file aready exists, ask the callback listener,
	 * and get a no all for this file. This should not be in error, and should
	 * update the transfer options
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testGetLocalFileAskCallbackListenerGetANoAll()
			throws Exception {

		int firstLength = 3;
		int secondLength = 16;
		String sourceFileName = "testGetLocalFileAskCallbackListenerGetANoAll.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String sourceLocalFileAbsolutePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, sourceFileName,
						firstLength);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String getFileName = "testGetLocalFileAskCallbackListenerGetANoAllResult.txt";
		String getResultLocalPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File getResultLocalFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dataTransferOperations
				.putOperation(
						sourceLocalFileAbsolutePath,
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						null, null);

		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO
				.instanceIRODSFileForPath(targetIrodsCollection + '/'
						+ sourceFileName);

		TransferControlBlock transferControlBlock = irodsFileSystem
				.getIrodsSession()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		transferControlBlock.getTransferOptions().setForceOption(
				ForceOption.ASK_CALLBACK_LISTENER);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();
		transferStatusCallbackListener
				.setForceOption(CallbackResponse.NO_FOR_ALL);

		dataObjectAO.getDataObjectFromIrods(irodsFile, getResultLocalFile,
				transferControlBlock, transferStatusCallbackListener);
		// now I know the local file exits, get again and it should overwrite
		// with no error, check it was truly overwritten by making source file a
		// different length and checking

		irodsFile.deleteWithForceOption();
		sourceLocalFileAbsolutePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, sourceFileName,
						secondLength);
		dataTransferOperations
				.putOperation(
						sourceLocalFileAbsolutePath,
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						null, null);

		dataObjectAO.getDataObjectFromIrods(irodsFile, getResultLocalFile,
				transferControlBlock, transferStatusCallbackListener);
		// check tomake sure transfer control block was not altered
		Assert.assertEquals(
				"transfer control block should have been altered to no for all",
				ForceOption.NO_FORCE, transferControlBlock.getTransferOptions()
						.getForceOption());
		Assert.assertEquals("file should not be the new length", firstLength,
				getResultLocalFile.length());
	}

	/**
	 * Do a get when the local file aready exists (should throw an error for
	 * overwrite)
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testGetLocalFileAskCallbackListenerGetAYesAll()
			throws Exception {

		int secondLength = 13;
		String sourceFileName = "testGetLocalFileAskCallbackListenerGetAYesAll.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String sourceLocalFileAbsolutePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, sourceFileName, 1);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String getFileName = "testGetLocalFileAskCallbackListenerGetAYesAllResult.txt";
		String getResultLocalPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File getResultLocalFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dataTransferOperations
				.putOperation(
						sourceLocalFileAbsolutePath,
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						null, null);

		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO
				.instanceIRODSFileForPath(targetIrodsCollection + '/'
						+ sourceFileName);

		TransferControlBlock transferControlBlock = irodsFileSystem
				.getIrodsSession()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		transferControlBlock.getTransferOptions().setForceOption(
				ForceOption.ASK_CALLBACK_LISTENER);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();
		transferStatusCallbackListener
				.setForceOption(CallbackResponse.YES_FOR_ALL);

		dataObjectAO.getDataObjectFromIrods(irodsFile, getResultLocalFile,
				transferControlBlock, transferStatusCallbackListener);
		// now I know the local file exits, get again and it should overwrite
		// with no error, check it was truly overwritten by making source file a
		// different length and checking

		irodsFile.deleteWithForceOption();
		sourceLocalFileAbsolutePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, sourceFileName,
						secondLength);
		dataTransferOperations
				.putOperation(
						sourceLocalFileAbsolutePath,
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						null, null);

		dataObjectAO.getDataObjectFromIrods(irodsFile, getResultLocalFile,
				transferControlBlock, transferStatusCallbackListener);
		// check tomake sure transfer control block was not altered
		Assert.assertEquals(
				"transfer control block should have been altered to yes for all",
				ForceOption.USE_FORCE, transferControlBlock
						.getTransferOptions().getForceOption());
		Assert.assertEquals("file should now be the new length", secondLength,
				getResultLocalFile.length());

	}

	/**
	 * Do a get when the local file aready exists (should throw an error for
	 * overwrite)
	 * 
	 * @throws Exception
	 */
	@Test(expected = OverwriteException.class)
	public final void testGetLocalFileAlreadyExistsTransferOptionNoForce()
			throws Exception {

		String testFileName = "testGetLocalFileAlreadyExistsTransferOptionNoForce.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String getFileName = "testGetLocalFileAlreadyExistsTransferOptionNoForceResult.txt";
		String getResultLocalPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File localFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		TransferControlBlock transferControlBlock = irodsFileSystem
				.getIrodsSession()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		transferControlBlock.getTransferOptions().setForceOption(
				ForceOption.NO_FORCE);

		DataTransferOperations dataTransferOperations = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dataTransferOperations
				.putOperation(
						localFileName,
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						null, null);

		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO
				.instanceIRODSFileForPath(targetIrodsCollection + '/'
						+ testFileName);

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile,
				transferControlBlock, null);
		// now I know the local file exits, get again and see an overwrite
		// errror
		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile,
				transferControlBlock, null);

	}

	/**
	 * Do a get when the local file aready exists, but the force option is set
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testGetLocalFileAlreadyExistsTransferOptionForce()
			throws Exception {

		String testFileName = "testGetLocalFileAlreadyExistsTransferOptionForce.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String getFileName = "testGetLocalFileAlreadyExistsTransferOptionForceResult.txt";
		String getResultLocalPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File localFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		TransferControlBlock transferControlBlock = irodsFileSystem
				.getIrodsSession()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		transferControlBlock.getTransferOptions().setForceOption(
				ForceOption.USE_FORCE);

		DataTransferOperations dataTransferOperations = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dataTransferOperations
				.putOperation(
						localFileName,
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						null, null);

		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO
				.instanceIRODSFileForPath(targetIrodsCollection + '/'
						+ testFileName);

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile);
		// now I know the local file exits, get again and see an overwrite
		// errror
		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile,
				transferControlBlock, null);

	}

	/**
	 * Get a data object where the source name and the target name are
	 * different. It should be retrieved as the target name.
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testGetProvidingDifferentSourceAndTargetDataNames()
			throws Exception {

		String sourceFileName = "testGetProvidingDifferentSourceAndTargetDataNames.txt";
		String targetFileName = "target file for testGetProvidingDifferentSourceAndTargetDataNames.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, sourceFileName,
						100);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String getResultLocalPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ targetFileName;
		File localFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dataTransferOperations
				.putOperation(
						localFileName,
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						null, null);

		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO
				.instanceIRODSFileForPath(targetIrodsCollection + '/'
						+ sourceFileName);

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile);

		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH
				+ '/' + targetFileName);
		assertionHelper.assertLocalScratchFileLengthEquals(
				IRODS_TEST_SUBDIR_PATH + '/' + targetFileName, 100);

	}

	@Test
	public final void testGetExecutable() throws Exception {

		String testFileName = "testGetExecutable.sh";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		File localFile = new File(localFileName);
		localFile.setExecutable(true);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String getFileName = "testGetExecutableResult.sh";
		String getResultLocalPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		localFile = new File(getResultLocalPath);
		localFile.setExecutable(true);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dataTransferOperations
				.putOperation(
						localFileName,
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						null, null);

		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO
				.instanceIRODSFileForPath(targetIrodsCollection + '/'
						+ testFileName);

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile);

		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH
				+ '/' + getFileName);
		assertionHelper.assertLocalScratchFileLengthEquals(
				IRODS_TEST_SUBDIR_PATH + '/' + getFileName, 100);
		Assert.assertTrue("local file should be executable",
				localFile.canExecute());

	}

	@Test
	public final void testGetNotExecutable() throws Exception {

		String testFileName = "testGetNotExecutable.sh";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		File localFile = new File(localFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String getFileName = "testGetNotExecutableResult.sh";
		String getResultLocalPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		localFile = new File(getResultLocalPath);
		localFile.setExecutable(true);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dataTransferOperations
				.putOperation(
						localFileName,
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						null, null);

		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO
				.instanceIRODSFileForPath(targetIrodsCollection + '/'
						+ testFileName);

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile);

		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH
				+ '/' + getFileName);
		assertionHelper.assertLocalScratchFileLengthEquals(
				IRODS_TEST_SUBDIR_PATH + '/' + getFileName, 100);
		Assert.assertFalse("local file should not be executable",
				localFile.canExecute());

	}

	@Test
	public final void testGetWithIntraFileCallbacks() throws Exception {

		int testFileLen = 3000000;
		// generate a local scratch file
		String testFileName = "testGetWithIntraFileCallbacks.doc";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						testFileLen);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String getFileName = "testGetWithIntraFileCallbacksResult.doc";
		String getResultLocalPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File localFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dataTransferOperations
				.putOperation(
						localFileName,
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						null, null);

		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO
				.instanceIRODSFileForPath(targetIrodsCollection + '/'
						+ testFileName);

		TransferOptions transferOptions = new TransferOptions();
		transferOptions.setIntraFileStatusCallbacks(true);
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();
		transferControlBlock.setTransferOptions(transferOptions);

		TestingStatusCallbackListener transferStatusCallbackListener = new TestingStatusCallbackListener();

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile,
				transferControlBlock, transferStatusCallbackListener);

		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH
				+ '/' + getFileName);
		assertionHelper.assertLocalScratchFileLengthEquals(
				IRODS_TEST_SUBDIR_PATH + '/' + getFileName, testFileLen);

		Assert.assertTrue(
				"did not get intra-file callbacks",
				transferStatusCallbackListener.getNumberIntraFileCallbacks() > 0);
		Assert.assertTrue(
				"did not get any byte count from intra-file callbacks",
				transferStatusCallbackListener
						.getBytesReportedIntraFileCallbacks() > 0);
		Assert.assertFalse("accumulated more bytes than file size",
				transferStatusCallbackListener
						.getBytesReportedIntraFileCallbacks() > testFileLen);

	}

	@Test
	public final void testGetParallelWithIntraFileCallbacks() throws Exception {

		int testFileLen = 33 * 1024 * 1024;
		// generate a local scratch file
		String testFileName = "testGetParallelWithIntraFileCallbacks.doc";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						testFileLen);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String getFileName = "testGetParallelWithIntraFileCallbacksResult.doc";
		String getResultLocalPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File localFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dataTransferOperations
				.putOperation(
						localFileName,
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						null, null);

		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO
				.instanceIRODSFileForPath(targetIrodsCollection + '/'
						+ testFileName);

		TransferOptions transferOptions = new TransferOptions();
		transferOptions.setIntraFileStatusCallbacks(true);
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();
		transferControlBlock.setTransferOptions(transferOptions);

		TestingStatusCallbackListener transferStatusCallbackListener = new TestingStatusCallbackListener();

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile,
				transferControlBlock, transferStatusCallbackListener);

		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH
				+ '/' + getFileName);
		assertionHelper.assertLocalScratchFileLengthEquals(
				IRODS_TEST_SUBDIR_PATH + '/' + getFileName, testFileLen);

		Assert.assertTrue(
				"did not get intra-file callbacks",
				transferStatusCallbackListener.getNumberIntraFileCallbacks() > 0);
		Assert.assertTrue(
				"did not get any byte count from intra-file callbacks",
				transferStatusCallbackListener
						.getBytesReportedIntraFileCallbacks() > 0);
		Assert.assertFalse("accumulated more bytes than file size",
				transferStatusCallbackListener
						.getBytesReportedIntraFileCallbacks() > testFileLen);

	}

	@Test
	public final void testGetFileGTParallelMaxNoParallelInOptions()
			throws Exception {

		int testFileLen = 33 * 1024 * 1024;
		// generate a local scratch file
		String testFileName = "testGetFileGTParallelMaxNoParallelInOptions.doc";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						testFileLen);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String getFileName = "testGetParallelWithIntraFileCallbacksResult.doc";
		String getResultLocalPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File localFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		TestingStatusCallbackListener transferStatusCallbackListener = new TestingStatusCallbackListener();
		TransferControlBlock transferControlBlock = irodsFileSystem
				.getIrodsSession()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		transferControlBlock.getTransferOptions().setUseParallelTransfer(false);

		DataTransferOperations dataTransferOperations = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dataTransferOperations
				.putOperation(
						localFileName,
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						transferStatusCallbackListener, transferControlBlock);

		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO
				.instanceIRODSFileForPath(targetIrodsCollection + '/'
						+ testFileName);

		transferControlBlock = irodsFileSystem.getIrodsSession()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		transferControlBlock.getTransferOptions().setUseParallelTransfer(false);
		transferControlBlock.getTransferOptions().setIntraFileStatusCallbacks(
				true);

		transferStatusCallbackListener = new TestingStatusCallbackListener();

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile,
				transferControlBlock, transferStatusCallbackListener);

		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH
				+ '/' + getFileName);
		assertionHelper.assertLocalScratchFileLengthEquals(
				IRODS_TEST_SUBDIR_PATH + '/' + getFileName, testFileLen);

	}

	@Test
	public final void testGetGiveLocalTargetAsCollection() throws Exception {

		// generate a local scratch file
		String testFileName = "testGetGiveLocalTargetAsCollection.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		String getResultLocalPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		File localFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dataTransferOperations = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dataTransferOperations
				.putOperation(
						localFileName,
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						null, null);

		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO
				.instanceIRODSFileForPath(targetIrodsCollection + '/'
						+ testFileName);

		// clear the file from scratch

		File scratchLocalFile = new File(localFileName);
		boolean success = scratchLocalFile.delete();

		TransferOptions transferOptions = new TransferOptions();
		transferOptions.setIntraFileStatusCallbacks(false);
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();
		transferControlBlock.setTransferOptions(transferOptions);

		TestingStatusCallbackListener transferStatusCallbackListener = new TestingStatusCallbackListener();

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile,
				transferControlBlock, transferStatusCallbackListener);

		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH
				+ '/' + testFileName);
		assertionHelper.assertLocalScratchFileLengthEquals(
				IRODS_TEST_SUBDIR_PATH + '/' + testFileName, 100);
		Assert.assertTrue("delete did not report success in response", success);

		Assert.assertFalse(
				"got intra-file callbacks",
				transferStatusCallbackListener.getNumberIntraFileCallbacks() > 0);
	}

	@Test(expected = DataNotFoundException.class)
	public final void testGetSpecifyingDifferentResource() throws Exception {
		// generate a local scratch file
		String testFileName = "testGetSpecifyingDifferentResource.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		String getFileName = "testGetSpecifyingDifferentResourceReturnedFile.txt";
		String getResultLocalPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File localFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		accessObjectFactory.getIRODSFileFactory(irodsAccount);

		DataTransferOperations dataTransferOperations = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dataTransferOperations
				.putOperation(
						localFileName,
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						null, null);

		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO
				.instanceIRODSFileForPath(targetIrodsCollection + '/'
						+ testFileName);
		irodsFile
				.setResource(testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY));

		dataObjectAO.getDataObjectFromIrods(irodsFile, localFile);

		assertionHelper
				.assertLocalFileNotExistsInScratch(IRODS_TEST_SUBDIR_PATH + "/"
						+ "GetResult" + testFileName);

	}

	@Test
	public final void testListMetadataValuesForDataObject() throws Exception {
		String testFileName = "testListMetadataValuesForDataObject.dat";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		String getFileName = "testGetSpecifyingDifferentResourceReturnedFile.txt";
		String getResultLocalPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		new File(getResultLocalPath);

		// put scratch file into irods in the right place on the first resource
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String dataObjectAbsPath = targetIrodsCollection + '/' + testFileName;

		iputCommand.setLocalFileName(localFileName);
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setIrodsResource(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY));
		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		// initialize the AVU data
		String expectedAttribName = "testmdattrib1";
		String expectedAttribValue = "testmdvalue1";
		String expectedAttribUnits = "test1mdunits";

		ImetaRemoveCommand imetaRemoveCommand = new ImetaRemoveCommand();
		imetaRemoveCommand.setAttribName(expectedAttribName);
		imetaRemoveCommand.setAttribValue(expectedAttribValue);
		imetaRemoveCommand.setAttribUnits(expectedAttribUnits);
		imetaRemoveCommand.setMetaObjectType(MetaObjectType.DATA_OBJECT_META);
		imetaRemoveCommand.setObjectPath(dataObjectAbsPath);
		invoker.invokeCommandAndGetResultAsString(imetaRemoveCommand);

		ImetaAddCommand imetaAddCommand = new ImetaAddCommand();
		imetaAddCommand.setMetaObjectType(MetaObjectType.DATA_OBJECT_META);
		imetaAddCommand.setAttribName(expectedAttribName);
		imetaAddCommand.setAttribValue(expectedAttribValue);
		imetaAddCommand.setAttribUnits(expectedAttribUnits);
		imetaAddCommand.setObjectPath(dataObjectAbsPath);
		invoker.invokeCommandAndGetResultAsString(imetaAddCommand);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				AVUQueryOperatorEnum.EQUAL, expectedAttribName));

		List<MetaDataAndDomainData> result = dataObjectAO
				.findMetadataValuesForDataObjectUsingAVUQuery(queryElements,
						targetIrodsCollection, testFileName);
		Assert.assertFalse("no query result returned", result.isEmpty());
	}

	@Test
	public final void testListMetadataValuesForDataObjectAsFile()
			throws Exception {
		String testFileName = "testListMetadataValuesForDataObjectAsFile.dat";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		// put scratch file into irods in the right place on the first resource

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + testFileName);

		DataTransferOperations transfer = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		transfer.putOperation(new File(localFileName), targetIrodsFile, null,
				null);

		// initialize the AVU data
		String expectedAttribName = "testmdattrib1file";
		String expectedAttribValue = "testmdvalue1file";
		String expectedAttribUnits = "test1mdunitsfile";
		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedAttribValue, expectedAttribUnits);

		dataObjectAO.deleteAVUMetadata(targetIrodsFile.getAbsolutePath(),
				avuData);

		dataObjectAO.addAVUMetadata(targetIrodsFile.getAbsolutePath(), avuData);

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				AVUQueryOperatorEnum.EQUAL, expectedAttribName));

		List<MetaDataAndDomainData> result = dataObjectAO
				.findMetadataValuesForDataObject(targetIrodsFile);

		Assert.assertFalse("no query result returned", result.isEmpty());
	}

	@Test
	public final void testFindMetadataValuesByMetadataQuery() throws Exception {
		String testFileName = "testFindMetadataValuesByMetadataQuery.csv";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 10);

		// put scratch file into irods in the right place on the first resource
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String dataObjectAbsPath = targetIrodsCollection + '/' + testFileName;

		iputCommand.setLocalFileName(localFileName);
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setIrodsResource(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY));
		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		// initialize the AVU data
		String expectedAttribName = "testfbmdattrib1";
		String expectedAttribValue = "testfbmdvalue1";
		String expectedAttribUnits = "test1fbmdunits";

		ImetaRemoveCommand imetaRemoveCommand = new ImetaRemoveCommand();
		imetaRemoveCommand.setAttribName(expectedAttribName);
		imetaRemoveCommand.setAttribValue(expectedAttribValue);
		imetaRemoveCommand.setAttribUnits(expectedAttribUnits);
		imetaRemoveCommand.setMetaObjectType(MetaObjectType.DATA_OBJECT_META);
		imetaRemoveCommand.setObjectPath(dataObjectAbsPath);
		invoker.invokeCommandAndGetResultAsString(imetaRemoveCommand);

		ImetaAddCommand imetaAddCommand = new ImetaAddCommand();
		imetaAddCommand.setMetaObjectType(MetaObjectType.DATA_OBJECT_META);
		imetaAddCommand.setAttribName(expectedAttribName);
		imetaAddCommand.setAttribValue(expectedAttribValue);
		imetaAddCommand.setAttribUnits(expectedAttribUnits);
		imetaAddCommand.setObjectPath(dataObjectAbsPath);
		invoker.invokeCommandAndGetResultAsString(imetaAddCommand);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				AVUQueryOperatorEnum.EQUAL, expectedAttribName));

		List<MetaDataAndDomainData> result = dataObjectAO
				.findMetadataValuesByMetadataQuery(queryElements);
		Assert.assertFalse("no query result returned", result.isEmpty());
	}

	@Test
	public void testFindDomainByMetadataQuery() throws Exception {

		String testFileName = "testFindDomainByMetadataQuery.dat";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 10);

		// put scratch file into irods in the right place on the first resource
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String dataObjectAbsPath = targetIrodsCollection + '/' + testFileName;

		iputCommand.setLocalFileName(localFileName);
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setIrodsResource(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY));
		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		// initialize the AVU data
		String expectedAttribName = "testfbmdattrib1";
		String expectedAttribValue = "testfbmdvalue1";
		String expectedAttribUnits = "test1fbmdunits";

		ImetaRemoveCommand imetaRemoveCommand = new ImetaRemoveCommand();
		imetaRemoveCommand.setAttribName(expectedAttribName);
		imetaRemoveCommand.setAttribValue(expectedAttribValue);
		imetaRemoveCommand.setAttribUnits(expectedAttribUnits);
		imetaRemoveCommand.setMetaObjectType(MetaObjectType.DATA_OBJECT_META);
		imetaRemoveCommand.setObjectPath(dataObjectAbsPath);
		invoker.invokeCommandAndGetResultAsString(imetaRemoveCommand);

		ImetaAddCommand imetaAddCommand = new ImetaAddCommand();
		imetaAddCommand.setMetaObjectType(MetaObjectType.DATA_OBJECT_META);
		imetaAddCommand.setAttribName(expectedAttribName);
		imetaAddCommand.setAttribValue(expectedAttribValue);
		imetaAddCommand.setAttribUnits(expectedAttribUnits);
		imetaAddCommand.setObjectPath(dataObjectAbsPath);
		invoker.invokeCommandAndGetResultAsString(imetaAddCommand);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				AVUQueryOperatorEnum.EQUAL, expectedAttribName));

		List<DataObject> result = dataObjectAO
				.findDomainByMetadataQuery(queryElements);
		Assert.assertFalse("no query result returned", result.isEmpty());
		Assert.assertEquals(testFileName, result.get(0).getDataName());
	}

	@Test
	public final void testReplicate() throws Exception {
		// generate a local scratch file

		String testFileName = "testReplicate1.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + "/" + testFileName);
		irodsFile.deleteWithForceOption();

		irodsFile.reset();
		irodsFile.setResource(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY));
		File localFile = new File(fileNameOrig);

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		dataObjectAO.putLocalDataObjectToIRODS(localFile, irodsFile, true);

		dataObjectAO
				.replicateIrodsDataObject(
						targetIrodsCollection + '/' + testFileName,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY));

		List<Resource> resources = dataObjectAO
				.listFileResources(targetIrodsCollection + "/" + testFileName);
		Assert.assertEquals("did not find expected resources", 2,
				resources.size());

	}

	/**
	 * Normal copy operation with tcb to noforce option, should just copy the
	 * file
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testCopyIRODSDataObjectToDataObjectNoForce()
			throws Exception {

		String testFileName = "testCopyIRODSDataObjectNoForce.txt";
		String testCopyToFileName = "testCopyIRODSDataObjectNoForceCopyTo.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, null, null);
		IRODSFile irodsSourceFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + testFileName);
		IRODSFile irodsTargetFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + testCopyToFileName);
		TransferControlBlock tcb = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.NO_FORCE);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();

		dataObjectAO.copyIRODSDataObject(irodsSourceFile, irodsTargetFile, tcb,
				transferStatusCallbackListener);

		IRODSFile checkCopiedFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + testCopyToFileName);
		Assert.assertTrue("new file does not exist", checkCopiedFile.exists());

	}

	/**
	 * Copy where the source file does not exist
	 * 
	 * @throws Exception
	 */
	@Test(expected = DataNotFoundException.class)
	public final void testCopyIRODSDataObjectToDataObjectSourceNotExists()
			throws Exception {

		String testFileName = "testCopyIRODSDataObjectToDataObjectSourceNotExists.txt";
		String testCopyToFileName = "testCopyIRODSDataObjectToDataObjectSourceNotExistsCopyTo.txt";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsSourceFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + testFileName);
		IRODSFile irodsTargetFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + testCopyToFileName);
		TransferControlBlock tcb = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.NO_FORCE);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();

		dataObjectAO.copyIRODSDataObject(irodsSourceFile, irodsTargetFile, tcb,
				transferStatusCallbackListener);

	}

	/**
	 * Copy where the source file is a collection
	 * 
	 * @throws Exception
	 */
	@Test(expected = JargonException.class)
	public final void testCopyIRODSDataObjectToDataObjectSourceIsCollection()
			throws Exception {

		String testCopyToFileName = "testCopyIRODSDataObjectToDataObjectSourceNotExistsCopyTo.txt";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsSourceFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		IRODSFile irodsTargetFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + testCopyToFileName);
		TransferControlBlock tcb = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.NO_FORCE);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();

		dataObjectAO.copyIRODSDataObject(irodsSourceFile, irodsTargetFile, tcb,
				transferStatusCallbackListener);
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
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsSourceFile = null;
		IRODSFile irodsTargetFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + testCopyToFileName);
		TransferControlBlock tcb = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.NO_FORCE);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();

		dataObjectAO.copyIRODSDataObject(irodsSourceFile, irodsTargetFile, tcb,
				transferStatusCallbackListener);
	}

	/**
	 * Copy where the target file is null
	 * 
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testCopyIRODSDataObjectTargetFileNull() throws Exception {

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsSourceFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		IRODSFile irodsTargetFile = null;
		TransferControlBlock tcb = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.NO_FORCE);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();

		dataObjectAO.copyIRODSDataObject(irodsSourceFile, irodsTargetFile, tcb,
				transferStatusCallbackListener);

	}

	/**
	 * Normal copy operation with tcb to noforce option, should just copy the
	 * file
	 * 
	 * @throws Exception
	 */
	@Test(expected = OverwriteException.class)
	public final void testCopyIRODSDataObjectToDataObjectNoForceWhenOverwrite()
			throws Exception {

		String testFileName = "testCopyIRODSDataObjectToDataObjectNoForceWhenOverwrite.txt";
		String testCopyToFileName = "testCopyIRODSDataObjectToDataObjectNoForceWhenOverwriteCopyTo.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, null, null);
		IRODSFile irodsSourceFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + testFileName);
		IRODSFile irodsTargetFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + testCopyToFileName);
		TransferControlBlock tcb = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.NO_FORCE);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();

		dataObjectAO.copyIRODSDataObject(irodsSourceFile, irodsTargetFile, tcb,
				transferStatusCallbackListener);

		dataObjectAO.copyIRODSDataObject(irodsSourceFile, irodsTargetFile, tcb,
				transferStatusCallbackListener);

	}

	/**
	 * Normal copy operation with tcb to force option, should just copy the file
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testCopyIRODSDataObjectToDataObjectForceWhenOverwrite()
			throws Exception {

		String testFileName = "testCopyIRODSDataObjectToDataObjectForceWhenOverwrite.txt";
		String testCopyToFileName = "testCopyIRODSDataObjectToDataObjectForceWhenOverwriteCopyTo.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, null, null);
		IRODSFile irodsSourceFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + testFileName);
		IRODSFile irodsTargetFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + testCopyToFileName);
		TransferControlBlock tcb = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.USE_FORCE);
		TransferStatusCallbackListenerTestingImplementation transferStatusCallbackListener = new TransferStatusCallbackListenerTestingImplementation();

		dataObjectAO.copyIRODSDataObject(irodsSourceFile, irodsTargetFile, tcb,
				transferStatusCallbackListener);

		dataObjectAO.copyIRODSDataObject(irodsSourceFile, irodsTargetFile, tcb,
				transferStatusCallbackListener);

		IRODSFile checkCopiedFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + testCopyToFileName);
		Assert.assertTrue("new file does not exist", checkCopiedFile.exists());
	}

	@SuppressWarnings("deprecation")
	@Test
	public final void testCopyNoForce() throws Exception {

		String testFileName = "testCopyNoForce.txt";
		String testCopyToFileName = "testCopyToFileName.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, null, null);
		dataObjectAO.copyIrodsDataObject(targetIrodsCollection + "/"
				+ testFileName, targetIrodsCollection + "/"
				+ testCopyToFileName, "");

		IRODSFile checkCopiedFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + testCopyToFileName);
		Assert.assertTrue("new file does not exist", checkCopiedFile.exists());

	}

	@SuppressWarnings("deprecation")
	@Test(expected = OverwriteException.class)
	public final void testCopyNoForceOverwriteSituation() throws Exception {

		String testFileName = "testCopyNoForceOverwriteSituation.txt";
		String testCopyToFileName = "testCopyNoForceOverwriteSituationCopyTo.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, null, null);
		dataObjectAO.copyIrodsDataObject(targetIrodsCollection + "/"
				+ testFileName, targetIrodsCollection + "/"
				+ testCopyToFileName, "");

		dataObjectAO.copyIrodsDataObject(targetIrodsCollection + "/"
				+ testFileName, targetIrodsCollection + "/"
				+ testCopyToFileName, "");

	}

	@SuppressWarnings("deprecation")
	@Test
	public final void testCopyWithForceOverwriteSituation() throws Exception {

		String testFileName = "testCopyWithForceOverwriteSituation.txt";
		String testCopyToFileName = "testCopyWithForceOverwriteSituationCopyTo.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, null, null);
		dataObjectAO.copyIrodsDataObject(targetIrodsCollection + "/"
				+ testFileName, targetIrodsCollection + "/"
				+ testCopyToFileName, "");

		dataObjectAO.copyIrodsDataObjectWithForce(targetIrodsCollection + "/"
				+ testFileName, targetIrodsCollection + "/"
				+ testCopyToFileName, "");

		IRODSFile checkCopiedFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + testCopyToFileName);
		Assert.assertTrue("new file does not exist", checkCopiedFile.exists());

	}

	@Test
	public final void testGetResourcesForDataObject() throws Exception {
		// generate a local scratch file

		String testFileName = "testGetResourcesForDataObject.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		// make sure all replicas are removed
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		IrmCommand rmvCommand = new IrmCommand();
		rmvCommand.setForce(true);
		rmvCommand.setObjectName(targetIrodsCollection + '/' + testFileName);
		try {
			invoker.invokeCommandAndGetResultAsString(rmvCommand);
		} catch (IcommandException ice) {
			if (ice.getMessage().indexOf("exist") != -1) {
				// ignore, nothing to remove
			} else {
				throw ice;
			}
		}

		// put scratch file into irods in the right place
		IputCommand iputCommand = new IputCommand();

		iputCommand.setLocalFileName(fileNameOrig);
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);

		invoker.invokeCommandAndGetResultAsString(iputCommand);
		iputCommand = new IputCommand();

		iputCommand.setLocalFileName(fileNameOrig);
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setIrodsResource(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY));
		iputCommand.setForceOverride(true);

		invoker.invokeCommandAndGetResultAsString(iputCommand);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		dataObjectAO
				.replicateIrodsDataObject(
						targetIrodsCollection + '/' + testFileName,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY));

		// now query to get the two resources the data object was replicated to

		List<Resource> resources = dataObjectAO.getResourcesForDataObject(
				targetIrodsCollection, testFileName);

		Assert.assertEquals("should be 2 resources for this data object", 2,
				resources.size());

	}

	@Test
	public final void testChecksum() throws Exception {

		// generate a local scratch file
		String testFileName = "testChecksum.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);

		// put scratch file into irods in the right place
		IputCommand iputCommand = new IputCommand();

		iputCommand.setLocalFileName(fileNameOrig);
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);

		invoker.invokeCommandAndGetResultAsString(iputCommand);

		iputCommand = new IputCommand();

		iputCommand.setLocalFileName(fileNameOrig);
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setIrodsResource(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY));
		iputCommand.setForceOverride(true);

		invoker.invokeCommandAndGetResultAsString(iputCommand);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile testFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		String computedChecksum = dataObjectAO
				.computeMD5ChecksumOnDataObject(testFile);
		Assert.assertTrue("did not return a checksum",
				computedChecksum.length() > 0);
	}

	@Ignore
	public final void testReplicateToResourceGroup() throws Exception {
		// generate a local scratch file

		String testFileName = "testReplicateToResourceGroup.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		// make sure all replicas are removed
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		IrmCommand rmvCommand = new IrmCommand();
		rmvCommand.setForce(true);
		rmvCommand.setObjectName(targetIrodsCollection + '/' + testFileName);
		try {
			invoker.invokeCommandAndGetResultAsString(rmvCommand);
		} catch (IcommandException ice) {
			if (ice.getMessage().indexOf("exist") != -1) {
				// ignore, nothing to remove
			} else {
				throw ice;
			}
		}

		// put scratch file into irods in the right place
		IputCommand iputCommand = new IputCommand();

		iputCommand.setLocalFileName(fileNameOrig);
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);

		invoker.invokeCommandAndGetResultAsString(iputCommand);

		iputCommand = new IputCommand();

		iputCommand.setLocalFileName(fileNameOrig);
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setIrodsResource(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY));
		iputCommand.setForceOverride(true);

		invoker.invokeCommandAndGetResultAsString(iputCommand);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO
				.replicateIrodsDataObjectToAllResourcesInResourceGroup(
						targetIrodsCollection + "/" + testFileName,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_GROUP_KEY));

		IlsCommand ilsCommand = new IlsCommand();
		ilsCommand.setLongFormat(true);
		ilsCommand.setIlsBasePath(targetIrodsCollection + '/' + testFileName);
		String ilsResult = invoker
				.invokeCommandAndGetResultAsString(ilsCommand);
		Assert.assertTrue(
				"file is not in new resource",
				ilsResult.indexOf(testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY)) != -1);
		Assert.assertTrue(
				"file is not in original resource",
				ilsResult.indexOf(testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY)) != -1);

	}

	@Test
	public void testAddAVUMetadataToDataObject() throws Exception {
		String testFileName = "testAddAVUMetadataToDataObject.txt";
		String expectedAttribName = "testAddAVUMetadataToDataObject";
		String expectedValueName = "testval1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/"
				+ testFileName;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig),
				targetIrodsFile, null, null);

		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedValueName, "");
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO.addAVUMetadata(targetIrodsDataObject, avuData);

		List<AVUQueryElement> avuQueryElements = new ArrayList<AVUQueryElement>();
		avuQueryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryPart.ATTRIBUTE, AVUQueryOperatorEnum.EQUAL,
				expectedAttribName));

		List<DataObject> dataObjects = dataObjectAO
				.findDomainByMetadataQuery(avuQueryElements);
		Assert.assertTrue(dataObjects.size() == 1);
	}

	@Test
	public void testAddAVUMetadataToDataObjectTwice() throws Exception {
		String testFileName = "testAddAVUMetadataToDataObjectTwice.txt";
		String expectedAttribName = "testAddAVUMetadataToDataObjectTwice";
		String expectedValueName = "testval1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/"
				+ testFileName;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig),
				targetIrodsFile, null, null);

		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedValueName, "");
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO.addAVUMetadata(targetIrodsDataObject, avuData);
		dataObjectAO.addAVUMetadata(targetIrodsDataObject, avuData);

		List<AVUQueryElement> avuQueryElements = new ArrayList<AVUQueryElement>();
		avuQueryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryPart.ATTRIBUTE, AVUQueryOperatorEnum.EQUAL,
				expectedAttribName));

		List<DataObject> dataObjects = dataObjectAO
				.findDomainByMetadataQuery(avuQueryElements);
		Assert.assertTrue(dataObjects.size() == 1);
	}

	/*
	 * [#161] iRODS inconsistantly handles duplicate AVU data for data objects
	 */
	@Test(expected = DuplicateDataException.class)
	public void testAddAVUMetadataToDataObjectTwiceIncludeUnitsVal()
			throws Exception {
		String testFileName = "testAddAVUMetadataToDataObjectTwiceIncludeUnitsVal.txt";
		String expectedAttribName = "testAddAVUMetadataToDataObjectTwice";
		String expectedValueName = "testval1";
		String expectedUnitsVal = "testunits1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/"
				+ testFileName;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig),
				targetIrodsFile, null, null);

		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedValueName, expectedUnitsVal);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO.addAVUMetadata(targetIrodsDataObject, avuData);
		dataObjectAO.addAVUMetadata(targetIrodsDataObject, avuData);

		List<AVUQueryElement> avuQueryElements = new ArrayList<AVUQueryElement>();
		avuQueryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryPart.ATTRIBUTE, AVUQueryOperatorEnum.EQUAL,
				expectedAttribName));

		List<DataObject> dataObjects = dataObjectAO
				.findDomainByMetadataQuery(avuQueryElements);
		Assert.assertTrue(dataObjects.size() == 1);
	}

	@Test
	public void testAddAVUMetadataToDataObjectTwiceSameNameDiffVal()
			throws Exception {
		String testFileName = "testAddAVUMetadataToDataObjectTwiceSameNameDiffVal.txt";
		String expectedAttribName = "testAddAVUMetadataToDataObjectTwiceSameNameDiffVal";
		String expectedValueName = "testAddAVUMetadataToDataObjectTwiceSameNameDiffValtestval1";
		String expectedValueName2 = "testAddAVUMetadataToDataObjectTwiceSameNameDiffValtestval2";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/"
				+ testFileName;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig),
				targetIrodsFile, null, null);

		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedValueName, "");
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO.addAVUMetadata(targetIrodsDataObject, avuData);

		avuData = AvuData.instance(expectedAttribName, expectedValueName2, "");
		dataObjectAO.addAVUMetadata(targetIrodsDataObject, avuData);

		List<AVUQueryElement> avuQueryElements = new ArrayList<AVUQueryElement>();
		avuQueryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryPart.ATTRIBUTE, AVUQueryOperatorEnum.EQUAL,
				expectedAttribName));

		List<MetaDataAndDomainData> metadata = dataObjectAO
				.findMetadataValuesByMetadataQuery(avuQueryElements);
		Assert.assertEquals("did not find 2 metadata values", 2,
				metadata.size());
	}

	@Test
	public void testDeleteAVUMetadataFromDataObject() throws Exception {
		String testFileName = "testDeleteAVUMetadataFromDataObject.txt";
		String expectedAttribName = "testDeleteAVUMetadataFromDataObject";
		String expectedValueName = "testval1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/"
				+ testFileName;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig),
				targetIrodsFile, null, null);

		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedValueName, "");
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO.addAVUMetadata(targetIrodsDataObject, avuData);

		// now delete and query again, should be no data

		dataObjectAO.deleteAVUMetadata(targetIrodsDataObject, avuData);

		List<AVUQueryElement> avuQueryElements = new ArrayList<AVUQueryElement>();
		avuQueryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryPart.ATTRIBUTE, AVUQueryOperatorEnum.EQUAL,
				expectedAttribName));

		List<DataObject> dataObjects = dataObjectAO
				.findDomainByMetadataQuery(avuQueryElements);
		Assert.assertTrue(dataObjects.isEmpty());
	}

	@Test(expected = DataNotFoundException.class)
	public void testDeleteAVUMetadataFromDataObjectDataObjectNotExists()
			throws Exception {
		String testFileName = "testDeleteAVUMetadataFromDataObjectDataObjectNotExists.xml";
		String expectedAttribName = "testDeleteAVUMetadataFromDataObjectDataObjectNotExists";
		String expectedValueName = "testDeleteAVUMetadataFromDataObjectDataObjectNotExiststestval1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/"
				+ testFileName;
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedValueName, "");
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO.addAVUMetadata(targetIrodsDataObject, avuData);
	}

	@Test
	public final void testSetRead() throws Exception {
		// generate a local scratch file

		String testFileName = "testSetRead.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, null, null);

		dataObjectAO.setAccessPermissionRead("", targetIrodsCollection + "/"
				+ testFileName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		// log in as the secondary user and test read access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSFile irodsFileForSecondaryUser = irodsFileSystem
				.getIRODSFileFactory(secondaryAccount).instanceIRODSFile(
						targetIrodsCollection + "/" + testFileName);
		Assert.assertTrue(irodsFileForSecondaryUser.canRead());

	}

	@Test
	public final void testSetReadAsAdmin() throws Exception {
		// generate a local scratch file

		String testFileName = "testSetReadAsAdmin.doc";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccountRods = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_ADMIN_USER_KEY),
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_ADMIN_PASSWORD_KEY));
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, null, null);

		DataObjectAO rodsDataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccountRods);

		rodsDataObjectAO
				.setAccessPermissionReadInAdminMode(
						"",
						targetIrodsCollection + "/" + testFileName,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		// log in as the secondary user and test read access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSFile irodsFileForSecondaryUser = irodsFileSystem
				.getIRODSFileFactory(secondaryAccount).instanceIRODSFile(
						targetIrodsCollection + "/" + testFileName);
		Assert.assertTrue(irodsFileForSecondaryUser.canRead());

	}

	@Test
	public final void testSetWriteAsAdmin() throws Exception {
		// generate a local scratch file

		String testFileName = "testSetWriteAsAdmin.doc";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccountRods = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_ADMIN_USER_KEY),
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_ADMIN_PASSWORD_KEY));
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, null, null);

		DataObjectAO rodsDataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccountRods);

		rodsDataObjectAO
				.setAccessPermissionWriteInAdminMode(
						"",
						targetIrodsCollection + "/" + testFileName,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		// log in as the secondary user and test read access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSFile irodsFileForSecondaryUser = irodsFileSystem
				.getIRODSFileFactory(secondaryAccount).instanceIRODSFile(
						targetIrodsCollection + "/" + testFileName);
		Assert.assertTrue(irodsFileForSecondaryUser.canWrite());

	}

	@Test
	public final void testSetOwnAsAdmin() throws Exception {
		// generate a local scratch file

		String testFileName = "testSetOwnAsAdmin.doc";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccountRods = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_ADMIN_USER_KEY),
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_ADMIN_PASSWORD_KEY));
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, null, null);

		DataObjectAO rodsDataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccountRods);

		rodsDataObjectAO
				.setAccessPermissionOwnInAdminMode(
						"",
						targetIrodsCollection + "/" + testFileName,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		// log in as the secondary user and test read access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSFile irodsFileForSecondaryUser = irodsFileSystem
				.getIRODSFileFactory(secondaryAccount).instanceIRODSFile(
						targetIrodsCollection + "/" + testFileName);
		DataObjectAO dataObjectAOSecondaryUser = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getDataObjectAO(secondaryAccount);
		UserFilePermission userFilePermission = dataObjectAOSecondaryUser
				.getPermissionForDataObjectForUserName(
						irodsFileForSecondaryUser.getAbsolutePath(),
						secondaryAccount.getUserName());
		Assert.assertTrue(
				"user should have own permission",
				userFilePermission.getFilePermissionEnum() == FilePermissionEnum.OWN);

	}

	@Test
	public final void testSetWrite() throws Exception {
		// generate a local scratch file

		String testFileName = "testSetWrite.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, null, null);

		dataObjectAO.setAccessPermissionWrite("", targetIrodsCollection + "/"
				+ testFileName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		// log in as the secondary user and test read access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSFile irodsFileForSecondaryUser = irodsFileSystem
				.getIRODSFileFactory(secondaryAccount).instanceIRODSFile(
						targetIrodsCollection + "/" + testFileName);
		Assert.assertTrue(irodsFileForSecondaryUser.canWrite());

	}

	@Test
	public final void testSetOwn() throws Exception {
		// generate a local scratch file

		String testFileName = "testSetOwn.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, null, null);

		dataObjectAO.setAccessPermissionOwn("", targetIrodsCollection + "/"
				+ testFileName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		// log in as the secondary user and test read access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSFile irodsFileForSecondaryUser = irodsFileSystem
				.getIRODSFileFactory(secondaryAccount).instanceIRODSFile(
						targetIrodsCollection + "/" + testFileName);

		IRODSFileSystemAO irodsFileSystemAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getIRODSFileSystemAO(
						secondaryAccount);
		int permissions = irodsFileSystemAO
				.getFilePermissions(irodsFileForSecondaryUser);

		Assert.assertTrue(permissions >= IRODSFile.OWN_PERMISSIONS);

	}

	@Test
	public final void testSetPublicWrite() throws Exception {
		// generate a local scratch file

		String testFileName = "testSetPublicRead.txt";
		String testUserName = "public";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, true);

		dataObjectAO.setAccessPermissionWrite("", targetIrodsCollection + "/"
				+ testFileName, testUserName);

		IRODSFileSystemAO irodsFileSystemAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getIRODSFileSystemAO(
						irodsAccount);
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection, testFileName);
		int permissions = irodsFileSystemAO.getFilePermissionsForGivenUser(
				irodsFile, testUserName);

		Assert.assertTrue(permissions >= IRODSFile.WRITE_PERMISSIONS);

	}

	@Test
	public final void testGetPermissionsOwn() throws Exception {
		// generate a local scratch file

		String testFileName = "testGetPermissionsOwn.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, true);

		dataObjectAO.setAccessPermissionOwn("", targetIrodsCollection + "/"
				+ testFileName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		// log in as the secondary user and test read access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSFile irodsFileForSecondaryUser = irodsFileSystem
				.getIRODSFileFactory(secondaryAccount).instanceIRODSFile(
						targetIrodsCollection + "/" + testFileName);
		DataObjectAO secondaryDataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getDataObjectAO(secondaryAccount);
		FilePermissionEnum filePermissionEnum = secondaryDataObjectAO
				.getPermissionForDataObject(
						irodsFileForSecondaryUser.getAbsolutePath(),
						secondaryAccount.getUserName(), "");

		Assert.assertEquals("should have found own permissions",
				FilePermissionEnum.OWN, filePermissionEnum);

	}

	@Test
	public final void testGetPermissionsForGivenUserWhoHasRead()
			throws Exception {
		// generate a local scratch file

		String testFileName = "testGetPermissionsForGivenUserWhoHasRead.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, true);

		dataObjectAO.setAccessPermissionRead("", targetIrodsCollection + "/"
				+ testFileName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		// log in as the secondary user and test read access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSFile irodsFileForSecondaryUser = irodsFileSystem
				.getIRODSFileFactory(secondaryAccount).instanceIRODSFile(
						targetIrodsCollection + "/" + testFileName);

		FilePermissionEnum filePermissionEnum = dataObjectAO
				.getPermissionForDataObject(
						irodsFileForSecondaryUser.getAbsolutePath(),
						secondaryAccount.getUserName(), "");

		Assert.assertEquals("should have found read permissions",
				FilePermissionEnum.READ, filePermissionEnum);

	}

	@Test
	public final void testSetReadThenRemove() throws Exception {
		// generate a local scratch file

		String testFileName = "testSetReadThenRemove.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, true);

		dataObjectAO.setAccessPermissionWrite("", targetIrodsCollection + "/"
				+ testFileName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		// log in as the secondary user and test read access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSFile irodsFileForSecondaryUser = irodsFileSystem
				.getIRODSFileFactory(secondaryAccount).instanceIRODSFile(
						targetIrodsCollection + "/" + testFileName);
		Assert.assertTrue(irodsFileForSecondaryUser.canRead());

		dataObjectAO.removeAccessPermissionsForUser("", targetIrodsCollection
				+ "/" + testFileName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		IRODSFileSystemAO irodsFileSystemAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getIRODSFileSystemAO(
						secondaryAccount);
		int permissions = irodsFileSystemAO
				.getFilePermissions(irodsFileForSecondaryUser);
		Assert.assertTrue("should not have permissions anymore",
				permissions < IRODSFile.WRITE_PERMISSIONS);

	}

	@Test
	public final void testListPermissionsForDataObject() throws Exception {
		// generate a local scratch file

		String testFileName = "testListPermissionsForDataObject.xls";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, true);

		dataObjectAO.setAccessPermissionRead("", targetIrodsCollection + "/"
				+ testFileName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		List<UserFilePermission> userFilePermissions = dataObjectAO
				.listPermissionsForDataObject(targetIrodsCollection + "/"
						+ testFileName);
		Assert.assertNotNull("got a null userFilePermissions",
				userFilePermissions);
		Assert.assertEquals("did not find the two permissions", 2,
				userFilePermissions.size());

		boolean foundIt = false;
		for (UserFilePermission permission : userFilePermissions) {
			if (permission
					.getUserName()
					.equals(testingProperties
							.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY))) {
				foundIt = true;
				Assert.assertEquals("user group not correctly determined",
						UserTypeEnum.RODS_USER, permission.getUserType());
			}
		}
		Assert.assertTrue("did not find user group in permissions", foundIt);

	}

	/**
	 * Add a user to a group, add that group to file permissions, and list the
	 * group
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testListPermissionsForDataObjectLookingForGroupPermissions()
			throws Exception {
		// generate a local scratch file

		String testFileName = "testListPermissionsForDataObjectLookingForGroupPermissions.xls";
		String testUserGroup = "testListPermissionsForDataObjectLookingForGroupPermissions";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		UserGroupAO userGroupAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getUserGroupAO(irodsAccount);

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroup);
		userGroup.setZone(irodsAccount.getZone());

		userGroupAO.removeUserGroup(userGroup);
		userGroupAO.addUserGroup(userGroup);

		userGroupAO.addUserToGroup(testUserGroup, irodsAccount.getUserName(),
				null);

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, true);

		dataObjectAO.setAccessPermissionRead("", targetIrodsCollection + "/"
				+ testFileName, testUserGroup);

		List<UserFilePermission> userFilePermissions = dataObjectAO
				.listPermissionsForDataObject(targetIrodsCollection + "/"
						+ testFileName);
		userGroupAO.removeUserGroup(userGroup);
		Assert.assertNotNull("got a null userFilePermissions",
				userFilePermissions);
		Assert.assertEquals("did not find the two permissions", 2,
				userFilePermissions.size());

		boolean foundIt = false;
		for (UserFilePermission permission : userFilePermissions) {
			if (permission.getUserName().equals(testUserGroup)) {
				foundIt = true;
				Assert.assertEquals("user group not correctly determined",
						UserTypeEnum.RODS_GROUP, permission.getUserType());
			}
		}
		Assert.assertTrue("did not find user group in permissions", foundIt);

	}

	@Test
	public final void testListPermissionsForDataObjectAfterGivingPublicWrite()
			throws Exception {
		// generate a local scratch file

		String testFileName = "testListPermissionsForDataObjectAfterGivingPublicWrite.xls";
		String testPublicUser = "public";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, true);

		dataObjectAO.setAccessPermissionRead("", targetIrodsCollection + "/"
				+ testFileName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		dataObjectAO.setAccessPermissionWrite("", targetIrodsCollection + "/"
				+ testFileName, testPublicUser);

		List<UserFilePermission> userFilePermissions = dataObjectAO
				.listPermissionsForDataObject(targetIrodsCollection + "/"
						+ testFileName);
		Assert.assertNotNull("got a null userFilePermissions",
				userFilePermissions);
		Assert.assertEquals("did not find the 3 permissions", 3,
				userFilePermissions.size());

	}

	@Test
	public void testOverwriteFileAvuMetadata() throws Exception {
		String testFileName = "testOverwriteFileAvuMetadata.txt";
		String expectedAttribName = "testOverwriteFileAvuMetadataAttrib1";
		String expectedAttribValue = "testOverwriteFileAvuMetadataValue1";
		String expectedNewValue = "testOverwriteFileAvuMetadataValue1ThatsOverwriten";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection, testFileName);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, true);

		AvuData dataToAdd = AvuData.instance(expectedAttribName,
				expectedAttribValue, "");
		dataObjectAO.addAVUMetadata(targetIrodsCollection, testFileName,
				dataToAdd);
		AvuData overwriteAvuData = AvuData.instance(expectedAttribName,
				expectedNewValue, "");

		dataObjectAO.modifyAVUMetadata(targetIrodsCollection, testFileName,
				dataToAdd, overwriteAvuData);

		List<MetaDataAndDomainData> metadata = dataObjectAO
				.findMetadataValuesForDataObject(targetIrodsCollection,
						testFileName);

		Assert.assertEquals("should only be one avu entry", 1, metadata.size());

		for (MetaDataAndDomainData metadataEntry : metadata) {
			Assert.assertEquals("did not find attrib name", expectedAttribName,
					metadataEntry.getAvuAttribute());
			Assert.assertEquals("did not find attrib val", expectedNewValue,
					metadataEntry.getAvuValue());
		}

	}

	@Test
	public void testOverwriteFileAvuMetadataSpecifyAbsPath() throws Exception {
		String testFileName = "testOverwriteFileAvuMetadataSpecifyAbsPath.txt";
		String expectedAttribName = "testOverwriteFileAvuMetadataSpecifyAbsPathAvuMetadataAttrib1";
		String expectedAttribValue = "testOverwriteFileAvuMetadataSpecifyAbsPathAvuMetadataValue1";
		String expectedNewValue = "testOverwriteFileAvuMetadataSpecifyAbsPathAvuMetadataValue1ThatsOverwriten";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection, testFileName);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, true);

		AvuData dataToAdd = AvuData.instance(expectedAttribName,
				expectedAttribValue, "");
		dataObjectAO.addAVUMetadata(targetIrodsCollection, testFileName,
				dataToAdd);
		AvuData overwriteAvuData = AvuData.instance(expectedAttribName,
				expectedNewValue, "");

		dataObjectAO.modifyAVUMetadata(targetIrodsCollection + "/"
				+ testFileName, dataToAdd, overwriteAvuData);

		List<MetaDataAndDomainData> metadata = dataObjectAO
				.findMetadataValuesForDataObject(targetIrodsCollection + "/"
						+ testFileName);
		irodsFileSystem.close();

		Assert.assertEquals("should only be one avu entry", 1, metadata.size());

		for (MetaDataAndDomainData metadataEntry : metadata) {
			Assert.assertEquals("did not find attrib name", expectedAttribName,
					metadataEntry.getAvuAttribute());
			Assert.assertEquals("did not find attrib val", expectedNewValue,
					metadataEntry.getAvuValue());
		}

	}

	@Test
	public void testOverwriteAvuMetadataGivenNameAndUnit() throws Exception {
		String testFileName = "testOverwriteAvuMetadataGivenNameAndUnit.txt";
		String expectedAttribName = "testOverwriteAvuMetadataGivenNameAndUnitAvuMetadataAttrib1";
		String expectedAttribValue = "testOverwriteAvuMetadataGivenNameAndUnitAvuMetadataValue1";
		String expectedNewValue = "testOverwriteAvuMetadataGivenNameAndUnitAvuMetadataValue1ThatsOverwriten";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection, testFileName);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, true);

		AvuData dataToAdd = AvuData.instance(expectedAttribName,
				expectedAttribValue, "");
		dataObjectAO.addAVUMetadata(targetIrodsCollection, testFileName,
				dataToAdd);
		AvuData overwriteAvuData = AvuData.instance(expectedAttribName,
				expectedNewValue, "");

		dataObjectAO.modifyAvuValueBasedOnGivenAttributeAndUnit(
				targetIrodsCollection + "/" + testFileName, overwriteAvuData);

		List<MetaDataAndDomainData> metadata = dataObjectAO
				.findMetadataValuesForDataObject(targetIrodsCollection,
						testFileName);

		Assert.assertEquals("should only be one avu entry", 1, metadata.size());

		for (MetaDataAndDomainData metadataEntry : metadata) {
			Assert.assertEquals("did not find attrib name", expectedAttribName,
					metadataEntry.getAvuAttribute());
			Assert.assertEquals("did not find attrib val", expectedNewValue,
					metadataEntry.getAvuValue());
		}
	}

	@Test
	public void testRemoveAvuMetadataAvuDataDoesNotExist() throws Exception {
		String testDirName = "testRemoveAvuMetadataAvuDataDoesNotExistDir";
		String expectedAttribName = "testattrib1";
		String expectedAttribValue = "testvalue1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		// put scratch collection into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		ImkdirCommand imkdirCommand = new ImkdirCommand();
		imkdirCommand.setCollectionName(targetIrodsCollection);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(imkdirCommand);

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);

		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedAttribValue, "");

		collectionAO.deleteAVUMetadata(targetIrodsCollection, avuData);
	}

	@Test
	public void testGetHostForGetProvidingResourceNameWhenShouldDifferentResource()
			throws Exception {

		String useDistribResources = testingProperties
				.getProperty("test.option.distributed.resources");

		if (useDistribResources != null && useDistribResources.equals("true")) {
			// do the test
		} else {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);

		IRODSServerProperties irodsServerProperties = environmentalInfoAO
				.getIRODSServerProperties();

		if (!irodsServerProperties
				.isTheIrodsServerAtLeastAtTheGivenReleaseVersion(RemoteExecuteServiceImpl.STREAMING_API_CUTOFF)) {
			irodsFileSystem.close();
			return;
		}

		// generate a local scratch file
		String testFileName = "testGetHostForGetProvidingResourceNameWhenShouldDifferentResource.txt";
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

		DataTransferOperations transferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		transferOperationsAO
				.putOperation(
						fileNameAndPath.toString(),
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_TERTIARY_RESOURCE_KEY),
						null, null);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		String hostInfo = dataObjectAO.getHostForGetOperation(
				targetIrodsCollection + "/" + testFileName, "");
		irodsFileSystem.close();
		Assert.assertNotNull(
				"null info from lookup of host for get operation was not expected, re-routing should occur",
				hostInfo);
	}

	@Test
	public void testGetHostForGetProvidingResourceNameWhenShouldBeSameHost()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);

		IRODSServerProperties irodsServerProperties = environmentalInfoAO
				.getIRODSServerProperties();

		if (!irodsServerProperties
				.isTheIrodsServerAtLeastAtTheGivenReleaseVersion(RemoteExecuteServiceImpl.STREAMING_API_CUTOFF)) {
			irodsFileSystem.close();
			return;
		}

		// generate a local scratch file
		String testFileName = "testGetHostForGetProvidingResourceName.txt";
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

		DataTransferOperations transferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		transferOperationsAO.putOperation(fileNameAndPath.toString(),
				targetIrodsCollection, "", null, null);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		String hostInfo = dataObjectAO
				.getHostForGetOperation(
						targetIrodsCollection + "/" + testFileName,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY));
		irodsFileSystem.close();
		Assert.assertNull(
				"null info from lookup of host for get operation was expected, as no re-routing should occur",
				hostInfo);

	}

	@Test
	public final void testListPermissionsForDataObjectForUser()
			throws Exception {
		// generate a local scratch file

		String testFileName = "testListPermissionsForDataObjectForUser.xls";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, true);

		dataObjectAO.setAccessPermissionRead("", targetIrodsCollection + "/"
				+ testFileName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		UserFilePermission userFilePermission = dataObjectAO
				.getPermissionForDataObjectForUserName(
						targetIrodsCollection + "/" + testFileName,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		Assert.assertNotNull("got a null userFilePermission",
				userFilePermission);
		Assert.assertEquals(
				"did not find the right user name",
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
				userFilePermission.getUserName());

	}

	@Test
	public final void testListPermissionsForDataObjectForUserViaParentNameDataName()
			throws Exception {
		// generate a local scratch file

		String testFileName = "testListPermissionsForDataObjectForUserViaParentNameDataName.xls";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		dataObjectAO.putLocalDataObjectToIRODS(new File(fileNameOrig),
				irodsFile, true);

		dataObjectAO.setAccessPermissionRead("", targetIrodsCollection + "/"
				+ testFileName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		UserFilePermission userFilePermission = dataObjectAO
				.getPermissionForDataObjectForUserName(
						targetIrodsCollection,
						testFileName,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		Assert.assertNotNull("got a null userFilePermission",
				userFilePermission);
		Assert.assertEquals(
				"did not find the right user name",
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
				userFilePermission.getUserName());

	}

	@Test
	public void testPutFileReceiveIntraFileCallbacks() throws Exception {
		long testSize = 8000000;
		// generate a local scratch file
		String testFileName = "testPutFileReceiveIntraFileCallbacks.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						testSize);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		TransferOptions transferOptions = new TransferOptions();
		transferOptions.setIntraFileStatusCallbacks(true);
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();
		transferControlBlock.setTransferOptions(transferOptions);

		TestingStatusCallbackListener transferStatusCallbackListener = new TestingStatusCallbackListener();

		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile,
				transferControlBlock, transferStatusCallbackListener);
		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsFile);
		Assert.assertTrue(
				"did not get intra-file callbacks",
				transferStatusCallbackListener.getNumberIntraFileCallbacks() > 0);
		Assert.assertTrue(
				"did not get any byte count from intra-file callbacks",
				transferStatusCallbackListener
						.getBytesReportedIntraFileCallbacks() > 0);
		Assert.assertFalse("accumulated more bytes than file size",
				transferStatusCallbackListener
						.getBytesReportedIntraFileCallbacks() > testSize);
	}

	@Test
	public void testPutToIRODSFileWithDifferentFileName() throws Exception {
		// generate a local scratch file
		String testFileName = "testPutToIRODSFileWithDifferentFileName.txt";
		String testTargetFileName = "testPutToIRODSFileWithDifferentFileNameTarget.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 3);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testTargetFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		TransferOptions transferOptions = new TransferOptions();
		transferOptions.setComputeAndVerifyChecksumAfterTransfer(true);
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();
		transferControlBlock.setTransferOptions(transferOptions);

		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile,
				transferControlBlock, null);

		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsFile);
	}

	/**
	 * Create a source file in one collection, put to a target iRODS file with a
	 * different collection not specifying the target file name.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPutAFileGivingFileNameInOneCollectionToATargetCollectionGivingCollectionName()
			throws Exception {

		String testCollectionSubdir = "testPutAFileGivingFileNameInOneCollectionToATargetCollectionGivingCollectionName";
		String testTargetCollectionSubdir = "testPutAFileGivingFileNameInOneCollectionToATargetCollectionGivingCollectionNameTarget";

		String testFileName = "test.txt";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testTargetCollectionSubdir);
		IRODSFile targetIrodsCollectionFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);
		targetIrodsCollectionFile.mkdirs();
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ "/" + testCollectionSubdir);

		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				3);

		File localFile = new File(absPath, testFileName);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);

		TransferOptions transferOptions = new TransferOptions();
		transferOptions.setComputeAndVerifyChecksumAfterTransfer(true);
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();
		transferControlBlock.setTransferOptions(transferOptions);

		dataObjectAO.putLocalDataObjectToIRODS(localFile,
				targetIrodsCollectionFile, transferControlBlock, null);

		targetIrodsCollectionFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection,
				testFileName);

		assertionHelper
				.assertIrodsFileOrCollectionExists(targetIrodsCollectionFile
						.getAbsolutePath());
	}

	@Test
	public void testPutFileVerifyChecksum() throws Exception {
		// generate a local scratch file
		String testFileName = "testPutFileVerifyChecksum.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 20);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		TransferOptions transferOptions = new TransferOptions();
		transferOptions.setComputeAndVerifyChecksumAfterTransfer(true);
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();
		transferControlBlock.setTransferOptions(transferOptions);

		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile,
				transferControlBlock, null);

		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsFile);
	}

	@Test
	public void testParallelPutFileVerifyChecksum() throws Exception {
		// generate a local scratch file
		String testFileName = "testParallelPutFileVerifyChecksum.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						40000 * 1024);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		TransferOptions transferOptions = new TransferOptions();
		transferOptions.setComputeAndVerifyChecksumAfterTransfer(true);
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();
		transferControlBlock.setTransferOptions(transferOptions);

		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile,
				transferControlBlock, null);
		assertionHelper.assertIrodsFileOrCollectionExists(targetIrodsFile);
	}

	@SuppressWarnings("deprecation")
	@Test
	public final void testGetWithChecksumVerification() throws Exception {

		// generate a local scratch file
		String testFileName = "testGetWithChecksumVerification.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		String getFileName = "testGetWithChecksumVerificationResult.txt";
		String getResultLocalPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getFileName;
		File localFile = new File(getResultLocalPath);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dto.putOperation(localFileName, targetIrodsCollection, "", null, null);

		accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO
				.instanceIRODSFileForPath(targetIrodsCollection + '/'
						+ testFileName);

		TransferOptions transferOptions = new TransferOptions();
		transferOptions.setComputeAndVerifyChecksumAfterTransfer(true);

		dataObjectAO.getDataObjectFromIrodsGivingTransferOptions(irodsFile,
				localFile, transferOptions);

		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH
				+ '/' + getFileName);
		assertionHelper.assertLocalScratchFileLengthEquals(
				IRODS_TEST_SUBDIR_PATH + '/' + getFileName, 100);

	}

	@SuppressWarnings("deprecation")
	@Test
	public final void testParallelGetWithChecksumVerification()
			throws Exception {

		// generate a local scratch file
		String testFileName = "testParallelGetWithChecksumVerification.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String sourceFileAbsolutePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						80000 * 1024);

		String getResultFileName = "testParallelGetWithChecksumVerificationResult.txt";
		String getResultLocalPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
				+ getResultFileName;
		File getResultLocalFile = new File(getResultLocalPath);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dto = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dto.putOperation(sourceFileAbsolutePath, targetIrodsCollection, "",
				null, null);

		accessObjectFactory.getIRODSFileFactory(irodsAccount);
		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = dataObjectAO
				.instanceIRODSFileForPath(targetIrodsCollection + '/'
						+ testFileName);

		TransferOptions transferOptions = new TransferOptions();
		transferOptions.setComputeAndVerifyChecksumAfterTransfer(true);

		dataObjectAO.getDataObjectFromIrodsGivingTransferOptions(irodsFile,
				getResultLocalFile, transferOptions);

		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH
				+ '/' + getResultFileName);

	}

	/**
	 * Bug 629-malloc/resource error in irods when doing
	 * findDomainByMetadataQuery
	 */
	@Test
	public void testFindDataObjectDomainDataByAVUQueryForBug629()
			throws Exception {

		String testCollName = "testFindDataObjectDomainDataByAVUQueryForBug629";
		String testFilePrefix = "testFindDataObjectDomainDataByAVUQueryForBug629-";
		String testFileSuffix = ".txt";
		int count = 200;
		String expectedAttribName = "testattrib1";
		String expectedAttribValue = "testvalue1";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollName);

		IRODSAccessObjectFactory aoFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		// generate some test files, first delete the test subdir

		IRODSFile testSubdir = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);
		testSubdir.deleteWithForceOption();
		testSubdir.mkdirs();

		DataObjectAO dAO = aoFactory.getDataObjectAO(irodsAccount);
		DataTransferOperations dto = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		AvuData avuData = null;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String sourceFileAbsolutePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath,
						"testFileForAVU.txt", 1);
		File sourceFile = new File(sourceFileAbsolutePath);

		IRODSFile dataFile = null;
		StringBuilder sb = null;
		for (int i = 0; i < count; i++) {
			sb = new StringBuilder();
			sb.append(testFilePrefix);
			sb.append(i);
			sb.append(testFileSuffix);
			dataFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(testSubdir.getAbsolutePath(),
							sb.toString());
			dto.putOperation(sourceFile, dataFile, null, null);
			avuData = AvuData.instance(expectedAttribName, expectedAttribValue
					+ i, "");
			dAO.addAVUMetadata(dataFile.getAbsolutePath(), avuData);

		}

		ArrayList<AVUQueryElement> avus = new ArrayList<AVUQueryElement>();
		avus.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.ATTRIBUTE,
				AVUQueryOperatorEnum.EQUAL, expectedAttribName));
		avus.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.VALUE,
				AVUQueryOperatorEnum.LIKE, expectedAttribValue + "%"));

		List<DataObject> files = dAO.findDomainByMetadataQuery(avus);
		TestCase.assertNotNull("null files returned", files);
		TestCase.assertEquals("did not get all of the files", count,
				files.size());

	}

	/**
	 * Bug 629-malloc/resource error in irods when doing
	 * findDomainByMetadataQuery
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testFindDataObjectDomainDataByAVUQueryForBug629NoQueryElements()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		DataObjectAO dAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);

		ArrayList<AVUQueryElement> avus = new ArrayList<AVUQueryElement>();

		dAO.findDomainByMetadataQuery(avus);

	}

}
