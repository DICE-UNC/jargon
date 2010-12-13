package org.irods.jargon.akubra.impl;

import static org.junit.Assert.fail;

import java.net.URI;
import java.util.Properties;

import junit.framework.TestCase;

import org.akubraproject.Blob;
import org.akubraproject.BlobStoreConnection;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileFactoryImpl;
import org.irods.jargon.testutils.AssertionHelper;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.irods.jargon.testutils.icommandinvoke.IcommandInvoker;
import org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext;
import org.irods.jargon.testutils.icommandinvoke.icommands.IputCommand;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestiRODSBlobStoreConnectionTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IRODSBlobStoreConnectionTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static AssertionHelper assertionHelper = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		assertionHelper = new AssertionHelper();
	}

	@Test
	public void testIRODSBlobStoreConnection() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSBlobStore irodsBlobStore = IRODSBlobStore.instance(irodsAccount,
				irodsSession);
		IRODSFileFactory irodsFileFactory = new IRODSFileFactoryImpl(
				irodsSession, irodsAccount);
		BlobStoreConnection irodsBlobStoreConnection = new iRODSBlobStoreConnection(
				irodsBlobStore, irodsFileFactory);
		TestCase.assertNotNull("no connection returned",
				irodsBlobStoreConnection);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIRODSBlobStoreConnectionNullBlobStore() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSFileFactory irodsFileFactory = new IRODSFileFactoryImpl(
				irodsSession, irodsAccount);
		new iRODSBlobStoreConnection(null, irodsFileFactory);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIRODSBlobStoreConnectionNullFileFactory() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSBlobStore irodsBlobStore = IRODSBlobStore.instance(irodsAccount,
				irodsSession);
		new iRODSBlobStoreConnection(irodsBlobStore, null);
	}

	@Test
	public void testGetBlobURIMapOfStringString() throws Exception {
		String testFileName = "testGetBlobURIMapOfStringString.csv";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFile = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSBlobStore irodsBlobStore = IRODSBlobStore.instance(irodsAccount,
				irodsSession);
		IRODSFileFactory irodsFileFactory = new IRODSFileFactoryImpl(
				irodsSession, irodsAccount);
		BlobStoreConnection irodsBlobStoreConnection = new iRODSBlobStoreConnection(
				irodsBlobStore, irodsFileFactory);
		// create an irodsFile
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		
		// put scratch file into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();
		iputCommand.setLocalFileName(localFile);
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection, testFileName);
		URI irodsFileUri = irodsFile.toURI();
		
		Blob irodsBlob = irodsBlobStoreConnection.getBlob(irodsFileUri, null);
		TestCase.assertNotNull("no blob returned", irodsBlob);
		
	}

	@Test
	public void testListBlobIds() {
		fail("Not yet implemented");
	}

	@Test
	public void testSync() {
		fail("Not yet implemented");
	}

}
