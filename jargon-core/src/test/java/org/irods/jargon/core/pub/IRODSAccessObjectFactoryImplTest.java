package org.irods.jargon.core.pub;

import static org.junit.Assert.*;

import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSAccessObjectFactoryImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IRODSAccessObjectFactoryImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	@SuppressWarnings("unused")
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

	@Test
	public final void testIRODSAccessObjectFactoryImpl() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
		.instance();
		IRODSSession irodsSession = IRODSSession
		.instance(irodsConnectionManager);
		IRODSAccessObjectFactoryImpl irodsAccessObjectFactory = new IRODSAccessObjectFactoryImpl();
		irodsAccessObjectFactory.setIrodsSession(irodsSession);
		UserAO userAO = irodsAccessObjectFactory.getUserAO(irodsAccount);
		TestCase.assertNotNull("userAO was null", userAO);
		irodsSession.closeSession();
	}
	
	@Test(expected=JargonException.class)
	public final void testIRODSAccessObjectFactoryImplNoSessionSet() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactoryImpl irodsAccessObjectFactory = new IRODSAccessObjectFactoryImpl();
		UserAO userAO = irodsAccessObjectFactory.getUserAO(irodsAccount);
	}

}
