package org.irods.jargon.core.pub;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.ChecksumEncodingEnum;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSAccessObjectFactoryImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	public static final String IRODS_TEST_SUBDIR_PATH = "IRODSAccessObjectFactoryImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	@SuppressWarnings("unused")
	private static org.irods.jargon.testutils.AssertionHelper assertionHelper = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		assertionHelper = new org.irods.jargon.testutils.AssertionHelper();
	}

	@Test
	public final void testIRODSAccessObjectFactoryImpl() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory irodsAccessObjectFactory = new IRODSAccessObjectFactoryImpl();
		irodsAccessObjectFactory.setIrodsSession(irodsSession);
		UserAO userAO = irodsAccessObjectFactory.getUserAO(irodsAccount);
		Assert.assertNotNull("userAO was null", userAO);
		irodsSession.closeSession();
	}

	@Test
	public final void testBuildDefaultTransferControlBlockFromJargonPropertiesWithSHA256()
			throws Exception {

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);

		SettableJargonProperties settableProperties = new SettableJargonProperties(
				irodsSession.getJargonProperties());
		settableProperties.setChecksumEncoding(ChecksumEncodingEnum.SHA256);
		irodsSession.setJargonProperties(settableProperties);

		IRODSAccessObjectFactory irodsAccessObjectFactory = new IRODSAccessObjectFactoryImpl();
		irodsAccessObjectFactory.setIrodsSession(irodsSession);
		TransferControlBlock tcb = irodsAccessObjectFactory
				.buildDefaultTransferControlBlockBasedOnJargonProperties();

		Assert.assertEquals("did not set sha256", ChecksumEncodingEnum.SHA256,
				tcb.getTransferOptions().getChecksumEncoding());

	}

	@Test(expected = JargonException.class)
	public final void testIRODSAccessObjectFactoryImplNoSessionSet()
			throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = new IRODSAccessObjectFactoryImpl();
		irodsAccessObjectFactory.getUserAO(irodsAccount);
	}

	@Test(expected = AuthenticationException.class)
	public final void authenticateWithValidThenInvalid() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		IRODSAccessObjectFactory irodsAccessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		AuthResponse authResponse = irodsAccessObjectFactory
				.authenticateIRODSAccount(irodsAccount);
		Assert.assertNotNull("no auth response", authResponse);
		irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		irodsAccount.setPassword("bogus");
		authResponse = irodsAccessObjectFactory
				.authenticateIRODSAccount(irodsAccount);
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public final void authenticateWithValid() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		IRODSAccessObjectFactory irodsAccessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		AuthResponse authResponse = irodsAccessObjectFactory
				.authenticateIRODSAccount(irodsAccount);
		Assert.assertNotNull("no auth response", authResponse);
		// get again from cache
		authResponse = irodsAccessObjectFactory
				.authenticateIRODSAccount(irodsAccount);
		Assert.assertNotNull("no auth response", authResponse);
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test(expected = AuthenticationException.class)
	public final void authenticateWithInalid() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		irodsAccount.setPassword("bogus");
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		IRODSAccessObjectFactory irodsAccessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		irodsAccessObjectFactory.authenticateIRODSAccount(irodsAccount);
	}

	@Test
	public final void authenticateWithInvalidMultipleTimes() throws Exception {

		int ctr = 200;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		irodsAccount.setPassword("bogus");
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		IRODSAccessObjectFactory irodsAccessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		for (int i = 0; i < ctr; i++) {

			try {
				irodsAccessObjectFactory.authenticateIRODSAccount(irodsAccount);
			} catch (Exception e) {
				// ignore
			}
		}
	}

}
