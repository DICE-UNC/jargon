package org.irods.jargon.core.connection;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount.AuthScheme;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class KerberosAuthTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "KerberosAuthTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		if (!testingPropertiesHelper.isTestKerberos(testingProperties)) {
			return;
		}
		irodsFileSystem = IRODSFileSystem.instance();

		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(
				testingProperties);
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.clearIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
	}

	@AfterClass
	public static void afterClass() throws Exception {
		if (!testingPropertiesHelper.isTestKerberos(testingProperties)) {
			return;
		}
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public final void testAuthenticateNotAuthenticated() throws Exception {
		if (!testingPropertiesHelper.isTestKerberos(testingProperties)) {
			return;
		}
		AuthenticationFactory authenticationFactory = new AuthenticationFactoryImpl();
		KerberosAuth authMechanism = (KerberosAuth) authenticationFactory
				.instanceAuthMechanism(IRODSAccount.AuthScheme.KERBEROS.name());
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, "test2@IRODSKRB", "");
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSCommands irodsProtocol = IRODSCommands.instanceWithoutStartup(
				irodsAccount, irodsConnectionManager, irodsFileSystem
						.getIrodsSession()
						.buildPipelineConfigurationBasedOnJargonProperties(),
				authMechanism);
		irodsAccount.setAuthenticationScheme(IRODSAccount.AuthScheme.KERBEROS);
		irodsAccount.setServiceName("host/irodskrb@IRODSKRB");

		AuthResponse authResponse = authMechanism.authenticate(irodsProtocol,
				irodsAccount);
		Assert.assertNotNull("no authResponse", authResponse);
		Assert.assertFalse("auth should not be successful",
				authResponse.isSuccessful());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testAuthenticateNullIrodsCommands() throws Exception {
		if (!testingPropertiesHelper.isTestKerberos(testingProperties)) {
			return;
		}
		AuthenticationFactory authenticationFactory = new AuthenticationFactoryImpl();
		KerberosAuth authMechanism = (KerberosAuth) authenticationFactory
				.instanceAuthMechanism(IRODSAccount.AuthScheme.KERBEROS.name());
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_KERBEROS_USER_KEY),
						"");
		irodsAccount.setAuthenticationScheme(IRODSAccount.AuthScheme.KERBEROS);
		irodsAccount.setServiceName("host/irodskrb@IRODSKRB");

		authMechanism.authenticate(null, irodsAccount);
	}

	@Test
	public final void testAuthenticate() throws Exception {
		if (!testingPropertiesHelper.isTestKerberos(testingProperties)) {
			return;
		}
		AuthenticationFactory authenticationFactory = new AuthenticationFactoryImpl();
		KerberosAuth authMechanism = (KerberosAuth) authenticationFactory
				.instanceAuthMechanism(IRODSAccount.AuthScheme.KERBEROS.name());
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_KERBEROS_USER_KEY),
						"");
		irodsAccount.setAuthenticationScheme(IRODSAccount.AuthScheme.KERBEROS);
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSCommands irodsProtocol = IRODSCommands.instanceWithoutStartup(
				irodsAccount, irodsConnectionManager, irodsFileSystem
						.getIrodsSession()
						.buildPipelineConfigurationBasedOnJargonProperties(),
				authMechanism);

		AuthResponse authResponse = authMechanism.authenticate(irodsProtocol,
				irodsAccount);
		Assert.assertNotNull("no authResponse", authResponse);
		Assert.assertTrue("auth not successful", authResponse.isSuccessful());
		Assert.assertNotNull("irodsAccount null",
				authResponse.getAuthenticatedIRODSAccount());
		Assert.assertEquals("wrong auth type", AuthScheme.KERBEROS,
				authResponse.getAuthType());
	}

	@Test
	public final void testGetServerName() throws Exception {
		if (!testingPropertiesHelper.isTestKerberos(testingProperties)) {
			return;
		}
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		AuthenticationFactory authenticationFactory = new AuthenticationFactoryImpl();
		KerberosAuth authMechanism = (KerberosAuth) authenticationFactory
				.instanceAuthMechanism(IRODSAccount.AuthScheme.KERBEROS.name());
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_KERBEROS_USER_KEY),
						"");
		irodsAccount.setAuthenticationScheme(IRODSAccount.AuthScheme.KERBEROS);

		IRODSCommands irodsProtocol = IRODSCommands.instanceWithoutStartup(
				irodsAccount, irodsConnectionManager, irodsFileSystem
						.getIrodsSession()
						.buildPipelineConfigurationBasedOnJargonProperties(),
				authMechanism);

		String serverName = authMechanism.sendStartupPackAndGetServerName(
				irodsProtocol, irodsAccount);
		Assert.assertNotNull("null servername", serverName);
		Assert.assertFalse("empty server name", serverName.isEmpty());
	}

	@Test
	public final void testKerberosAuth() throws Exception {
		if (!testingPropertiesHelper.isTestKerberos(testingProperties)) {
			return;
		}
		AuthenticationFactory authenticationFactory = new AuthenticationFactoryImpl();
		AuthMechanism authMechanism = authenticationFactory
				.instanceAuthMechanism(IRODSAccount.AuthScheme.KERBEROS.name());
		Assert.assertNotNull("null authMechanism from factory", authMechanism);
		boolean iskrb = authMechanism instanceof KerberosAuth;
		Assert.assertTrue("did not get kerberos auth mechanism", iskrb);
	}

	/**
	 * A functional test using a kerberos login
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetIRODSPropsAsKerberosUser() throws Exception {
		if (!testingPropertiesHelper.isTestKerberos(testingProperties)) {
			return;
		}
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_KERBEROS_USER_KEY),
						"");
		irodsAccount.setAuthenticationScheme(IRODSAccount.AuthScheme.KERBEROS);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		EnvironmentalInfoAO environmentalInfoAO = accessObjectFactory
				.getEnvironmentalInfoAO(irodsAccount);
		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();
		Assert.assertNotNull("props were null", props);
		// no error = success
	}

	/**
	 * A functional test using a kerberos login
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPutFileAsKerberosUser() throws Exception {

		if (!testingPropertiesHelper.isTestKerberos(testingProperties)) {
			return;
		}

		// generate a local scratch file
		String testFileName = "testPutFileAsKerberosUser.txt";
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
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_KERBEROS_USER_KEY),
						"");

		/*
		 * IRODSAccount irodsAccount = testingPropertiesHelper
		 * .buildIRODSAccountFromTestProperties(testingProperties);
		 */
		irodsFileSystem.closeAndEatExceptions(irodsAccount);
		irodsAccount.setAuthenticationScheme(IRODSAccount.AuthScheme.KERBEROS);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
	}

}
