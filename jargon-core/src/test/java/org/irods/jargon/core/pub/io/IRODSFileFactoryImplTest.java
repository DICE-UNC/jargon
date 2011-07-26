/**
 * 
 */
package org.irods.jargon.core.pub.io;

import java.net.URI;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryImpl;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.icommandinvoke.IcommandInvoker;
import org.irods.jargon.testutils.icommandinvoke.icommands.IputCommand;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSFileFactoryImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IRODSFileFactoryAOImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static org.irods.jargon.testutils.AssertionHelper assertionHelper = null;

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
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testIRODSFileFactoryAOImpl() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		irodsSession.closeSession();
		Assert.assertNotNull(irodsFileFactory);
	}

	@Test
	public final void testCreateFileByPathString() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile("/apath");
		irodsSession.closeSession();
		Assert.assertNotNull(irodsFile);
	}

	@Test
	public final void testCreateFileByUri() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		URI testURI = testingPropertiesHelper
				.buildUriFromTestPropertiesForFileInUserDir(testingProperties,
						"home/afile.txt");
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(testURI);
		irodsSession.closeSession();
		Assert.assertNotNull(irodsFile);
	}

	@Test
	public final void testCreateFileByParentChildString() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile("/apath",
				"child");
		irodsSession.closeSession();
		Assert.assertNotNull(irodsFile);
	}

	@Test(expected = JargonException.class)
	public final void testCreateOutStreamFromFileNotExists() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile("/apath",
				"child");
		irodsFileFactory.instanceIRODSFileOutputStream(irodsFile);

	}

	@Test
	public final void testCreateOutStreamFromFile() throws Exception {
		String testFileName = "testCreateOutStreamFromFile.csv";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				1);

		// put scratch file into irods in the right place
		org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		iputCommand.setLocalFileName(fileNameAndPath.toString());
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(
				targetIrodsCollection, testFileName);
		IRODSFileOutputStream fos = irodsFileFactory
				.instanceIRODSFileOutputStream(irodsFile);
		irodsSession.closeSession();
		Assert.assertNotNull("null output stream returned from initializer",
				fos);
	}

	@Ignore
	// TODO: currently doesnt generate an exception, see about file permissions
	// modification to make tese fail w/JargonException
	public final void testCreateOutStreamFromFileThatICantWrite()
			throws Exception {
		String testFileName = "testCreateOutStreamFromFileThatDoesNotExist.csv";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String sourceFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 20);

		// put scratch file into irods in the right place
		org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromSecondaryTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		iputCommand.setLocalFileName(sourceFileName);
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(
				targetIrodsCollection, testFileName);
		IRODSFileOutputStream fos = irodsFileFactory
				.instanceIRODSFileOutputStream(irodsFile);
		irodsSession.closeSession();
		Assert.assertNotNull("null output stream returned from initializer",
				fos);
	}

	@Test
	public final void testCreateOutStreamFromFileThatDoesNotExist()
			throws Exception {
		String testFileName = "testCreateOutStreamFromFileThatDoesNotExist.csv";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(
				targetIrodsCollection, testFileName);
		IRODSFileOutputStream fos = irodsFileFactory
				.instanceIRODSFileOutputStream(irodsFile);
		irodsSession.closeSession();
		Assert.assertNotNull("null output stream returned from initializer",
				fos);
	}

	@Test(expected = JargonException.class)
	public final void testCreateOutStreamFromFileNameNotExists()
			throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		irodsFileFactory.instanceIRODSFileOutputStream("/stringfilename");

	}

	@Test
	public final void testCreateOutStreamFromFileName() throws Exception {
		String testFileName = "testCreateOutStreamFromFileName.csv";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				1);

		org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		iputCommand.setLocalFileName(fileNameAndPath.toString());
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFileOutputStream fos = irodsFileFactory
				.instanceIRODSFileOutputStream(targetIrodsCollection + '/'
						+ testFileName);
		irodsSession.closeSession();
		Assert.assertNotNull("null output stream returned from initializer",
				fos);

	}

	@Test
	public final void testCreateInputStreamFromFile() throws Exception {
		String testFileName = "testCreateInputStreamFromFile.csv";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				1);

		// put scratch file into irods in the right place
		org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		iputCommand.setLocalFileName(fileNameAndPath.toString());
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(
				targetIrodsCollection, testFileName);
		IRODSFileInputStream fos = irodsFileFactory
				.instanceIRODSFileInputStream(irodsFile);
		irodsSession.closeSession();
		Assert.assertNotNull("null input stream returned from initializer", fos);
	}

	@Test
	public final void testCreateSessionClosingInputStreamFromFile()
			throws Exception {
		String testFileName = "testCreateSessionClosingInputStreamFromFile.csv";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				1);

		// put scratch file into irods in the right place
		org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		iputCommand.setLocalFileName(fileNameAndPath.toString());
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(
				targetIrodsCollection, testFileName);
		SessionClosingIRODSFileInputStream fos = irodsFileFactory
				.instanceSessionClosingIRODSFileInputStream(irodsFile);
		Assert.assertNotNull("null input stream returned from initializer", fos);
	}

	@Test(expected = JargonException.class)
	public final void testCreateInputStreamFromNonExistentFile()
			throws Exception {
		String testFileName = "testCreateInputStreamFromNonExistentFile.csv";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(
				targetIrodsCollection, testFileName);
		IRODSFileInputStream fos = irodsFileFactory
				.instanceIRODSFileInputStream(irodsFile);
		irodsSession.closeSession();
		Assert.assertNotNull("null input stream returned from initializer", fos);
	}

	@Test
	public final void testCreateInputStreamFromFileName() throws Exception {
		String testFileName = "testCreateInputStreamFromFileName.csv";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				1);

		org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		iputCommand.setLocalFileName(fileNameAndPath.toString());
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFileInputStream fos = irodsFileFactory
				.instanceIRODSFileInputStream(targetIrodsCollection + '/'
						+ testFileName);
		irodsSession.closeSession();
		Assert.assertNotNull("null input stream returned from initializer", fos);
	}

	@Test
	public final void testCreateSessionClosingInputStreamFromFileName()
			throws Exception {
		String testFileName = "testCreateSessionClosingInputStreamFromFileName.csv";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				1);

		org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		iputCommand.setLocalFileName(fileNameAndPath.toString());
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		SessionClosingIRODSFileInputStream fos = irodsFileFactory
				.instanceSessionClosingIRODSFileInputStream(targetIrodsCollection
						+ '/' + testFileName);
		irodsSession.closeSession();
		Assert.assertNotNull("null input stream returned from initializer", fos);
	}

	@Test(expected = JargonException.class)
	public final void testCreateInputStreamFromFileNameMissing()
			throws Exception {
		String testFileName = "testCreateInputStreamFromFileNameMissing.csv";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFileInputStream fos = irodsFileFactory
				.instanceIRODSFileInputStream(targetIrodsCollection + '/'
						+ testFileName);
		irodsSession.closeSession();
		Assert.assertNotNull("null input stream returned from initializer", fos);

	}

	@Test
	public final void testCreateRandomAccessFileFromFileThatExists()
			throws Exception {
		String testFileName = "testCreateRandomAccessFileFromFromFileThatExists.csv";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				1);

		// put scratch file into irods in the right place
		org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		iputCommand.setLocalFileName(fileNameAndPath.toString());
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(
				targetIrodsCollection, testFileName);
		IRODSRandomAccessFile irodsRandomAccessFile = irodsFileFactory
				.instanceIRODSRandomAccessFile(irodsFile);
		irodsSession.closeSession();
		Assert.assertNotNull("should have a random access file",
				irodsRandomAccessFile);
	}

	@Test
	public final void testCreateRandomAccessFileFromFileThatDoesNotExist()
			throws Exception {
		String testFileName = "testCreateRandomAccessFileFromFromFileThatDoesNotExist.csv";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(
				targetIrodsCollection, testFileName);
		IRODSRandomAccessFile irodsRandomAccessFile = irodsFileFactory
				.instanceIRODSRandomAccessFile(irodsFile);
		irodsSession.closeSession();
		Assert.assertNotNull("should have a random access file",
				irodsRandomAccessFile);
		assertionHelper.assertIrodsFileOrCollectionExists(irodsFile
				.getAbsolutePath());
	}

	@Test
	public final void testCreateRandomAccessFileFromStringExists()
			throws Exception {
		String testFileName = "testCreateRandomAccessFileFromFromStringExists.csv";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				1);

		// put scratch file into irods in the right place
		org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		iputCommand.setLocalFileName(fileNameAndPath.toString());
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSRandomAccessFile irodsRandomAccessFile = irodsFileFactory
				.instanceIRODSRandomAccessFile(targetIrodsCollection + '/'
						+ testFileName);
		irodsSession.closeSession();
		Assert.assertNotNull("should have a random access file",
				irodsRandomAccessFile);
	}

}
