package org.irods.jargon.akubra.impl;

import java.util.Properties;

import javax.transaction.Transaction;

import junit.framework.TestCase;

import org.akubraproject.BlobStoreConnection;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.testutils.AssertionHelper;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;


public class IRODSBlobStoreTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IRODSBlobStoreTest";
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
	public void testInstance() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSBlobStore irodsBlobStore = IRODSBlobStore.instance(irodsAccount,
				irodsSession);
		TestCase.assertNotNull(
				"just a basic check, success without error is a pass",
				irodsBlobStore);
	}

	@Test
	public void testOpenConnection() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSBlobStore irodsBlobStore = IRODSBlobStore.instance(irodsAccount,
				irodsSession);
		BlobStoreConnection irodsBlobStoreConnection = irodsBlobStore.openConnection(null, null);
		TestCase.assertNotNull("no blob store connection returned",irodsBlobStoreConnection);
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testOpenConnectionTryingToPassTxn() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSBlobStore irodsBlobStore = IRODSBlobStore.instance(irodsAccount,
				irodsSession);
		Transaction tx = Mockito.mock(Transaction.class);
		irodsBlobStore.openConnection(tx, null);

	}
}
