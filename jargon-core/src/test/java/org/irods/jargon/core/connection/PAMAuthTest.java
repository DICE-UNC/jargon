package org.irods.jargon.core.connection;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.ClientServerNegotiationPolicy.SslNegotiationPolicy;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PAMAuthTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;

	private static SettableJargonProperties settableJargonProperties;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsFileSystem = IRODSFileSystem.instance();
		settableJargonProperties = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		irodsFileSystem.getIrodsSession().setJargonProperties(
				settableJargonProperties);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Before
	public void before() throws Exception {

		irodsFileSystem.getIrodsSession().setJargonProperties(
				settableJargonProperties);
	}

	@Test
	public final void testPAMAuthWithAnonUsesStandardAuth() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildAnonymousIRODSAccountFromTestProperties(testingProperties);
		irodsAccount.setAuthenticationScheme(AuthScheme.PAM);
		AuthenticationFactory authFactory = new AuthenticationFactoryImpl();
		AuthMechanism authMechanism = authFactory
				.instanceAuthMechanism(irodsAccount);
		boolean isStd = authMechanism instanceof StandardIRODSAuth;
		Assert.assertTrue("did not revert to standard auth for anonymous",
				isStd);
	}

	@Test
	public final void testPAMAuthValid() throws Exception {
		if (!testingPropertiesHelper.isTestPAM(testingProperties)) {
			return;
		}

		String pamUser = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PAM_USER_KEY);
		String pamPassword = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PAM_PASSWORD_KEY);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, pamUser, pamPassword);

		irodsAccount.setAuthenticationScheme(AuthScheme.PAM);
		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		environmentalInfoAO.getIRODSServerCurrentTime();

		AuthResponse authResponse = environmentalInfoAO.getIRODSProtocol()
				.getAuthResponse();
		Assert.assertNotNull("no authenticating account",
				authResponse.getAuthenticatingIRODSAccount());
		Assert.assertEquals("did not set authenticating account to PAM type",
				AuthScheme.PAM, authResponse

				.getAuthenticatingIRODSAccount().getAuthenticationScheme());
		Assert.assertNotNull("no authenticated account",
				authResponse.getAuthenticatedIRODSAccount());
		Assert.assertEquals("did not set authenticated account to std type",
				AuthScheme.STANDARD, authResponse
						.getAuthenticatedIRODSAccount()
						.getAuthenticationScheme());
		Assert.assertNotNull("did not set auth startup response",
				authResponse.getStartupResponse());
		Assert.assertTrue("did not show success", authResponse.isSuccessful());

		// now try something that uses tha

		irodsFileSystem.closeAndEatExceptions();

	}

	@Test
	public final void testPAMAuthValidNegRefuse() throws Exception {
		if (!testingPropertiesHelper.isTestPAM(testingProperties)) {
			return;
		}

		/*
		 * Only run if ssl enabled
		 */
		if (!testingPropertiesHelper.isTestSsl(testingProperties)) {
			return;
		}

		SettableJargonProperties testProps = new SettableJargonProperties(
				settableJargonProperties);
		testProps.setNegotiationPolicy(SslNegotiationPolicy.CS_NEG_REFUSE);
		irodsFileSystem.getIrodsSession().setJargonProperties(testProps);

		String pamUser = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PAM_USER_KEY);
		String pamPassword = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PAM_PASSWORD_KEY);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, pamUser, pamPassword);

		irodsAccount.setAuthenticationScheme(AuthScheme.PAM);
		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		environmentalInfoAO.getIRODSServerCurrentTime();

		AuthResponse authResponse = environmentalInfoAO.getIRODSProtocol()
				.getAuthResponse();
		Assert.assertNotNull("no authenticating account",
				authResponse.getAuthenticatingIRODSAccount());
		Assert.assertEquals("did not set authenticating account to PAM type",
				AuthScheme.PAM, authResponse

				.getAuthenticatingIRODSAccount().getAuthenticationScheme());
		Assert.assertNotNull("no authenticated account",
				authResponse.getAuthenticatedIRODSAccount());
		Assert.assertEquals("did not set authenticated account to std type",
				AuthScheme.STANDARD, authResponse
						.getAuthenticatedIRODSAccount()
						.getAuthenticationScheme());
		Assert.assertNotNull("did not set auth startup response",
				authResponse.getStartupResponse());
		Assert.assertTrue("did not show success", authResponse.isSuccessful());

		// now try something that uses tha

		irodsFileSystem.closeAndEatExceptions();

	}

	@Test
	public final void testPAMAuthValidNegDontCare() throws Exception {
		if (!testingPropertiesHelper.isTestPAM(testingProperties)) {
			return;
		}

		/*
		 * Only run if ssl enabled
		 */
		if (!testingPropertiesHelper.isTestSsl(testingProperties)) {
			return;
		}

		SettableJargonProperties testProps = new SettableJargonProperties(
				settableJargonProperties);
		testProps.setNegotiationPolicy(SslNegotiationPolicy.CS_NEG_DONT_CARE);
		irodsFileSystem.getIrodsSession().setJargonProperties(testProps);

		String pamUser = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PAM_USER_KEY);
		String pamPassword = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PAM_PASSWORD_KEY);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, pamUser, pamPassword);

		irodsAccount.setAuthenticationScheme(AuthScheme.PAM);
		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		environmentalInfoAO.getIRODSServerCurrentTime();

		AuthResponse authResponse = environmentalInfoAO.getIRODSProtocol()
				.getAuthResponse();
		Assert.assertNotNull("no authenticating account",
				authResponse.getAuthenticatingIRODSAccount());
		Assert.assertEquals("did not set authenticating account to PAM type",
				AuthScheme.PAM, authResponse

				.getAuthenticatingIRODSAccount().getAuthenticationScheme());
		Assert.assertNotNull("no authenticated account",
				authResponse.getAuthenticatedIRODSAccount());
		Assert.assertEquals("did not set authenticated account to std type",
				AuthScheme.STANDARD, authResponse
						.getAuthenticatedIRODSAccount()
						.getAuthenticationScheme());
		Assert.assertNotNull("did not set auth startup response",
				authResponse.getStartupResponse());
		Assert.assertTrue("did not show success", authResponse.isSuccessful());

		// now try something that uses tha

		irodsFileSystem.closeAndEatExceptions();

	}

	@Test
	public final void testPAMAuthValidNegRequire() throws Exception {
		if (!testingPropertiesHelper.isTestPAM(testingProperties)) {
			return;
		}

		/*
		 * Only run if ssl enabled
		 */
		if (!testingPropertiesHelper.isTestSsl(testingProperties)) {
			return;
		}

		SettableJargonProperties testProps = new SettableJargonProperties(
				settableJargonProperties);
		testProps.setNegotiationPolicy(SslNegotiationPolicy.CS_NEG_REQUIRE);
		irodsFileSystem.getIrodsSession().setJargonProperties(testProps);

		String pamUser = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PAM_USER_KEY);
		String pamPassword = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PAM_PASSWORD_KEY);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, pamUser, pamPassword);

		irodsAccount.setAuthenticationScheme(AuthScheme.PAM);
		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		environmentalInfoAO.getIRODSServerCurrentTime();

		AuthResponse authResponse = environmentalInfoAO.getIRODSProtocol()
				.getAuthResponse();
		Assert.assertNotNull("no authenticating account",
				authResponse.getAuthenticatingIRODSAccount());
		Assert.assertEquals("did not set authenticating account to PAM type",
				AuthScheme.PAM, authResponse

				.getAuthenticatingIRODSAccount().getAuthenticationScheme());
		Assert.assertNotNull("no authenticated account",
				authResponse.getAuthenticatedIRODSAccount());
		Assert.assertEquals("did not set authenticated account to std type",
				AuthScheme.STANDARD, authResponse
						.getAuthenticatedIRODSAccount()
						.getAuthenticationScheme());
		Assert.assertNotNull("did not set auth startup response",
				authResponse.getStartupResponse());
		Assert.assertTrue("did not show success", authResponse.isSuccessful());

		// now try something that uses tha

		irodsFileSystem.closeAndEatExceptions();

	}

	/**
	 * Unit test for PAM auth failure when password includes semicolon #195
	 * original problem was a -158000 exception in the pack struct, so an auth
	 * exception is expected and shows 'success' in processing the ; char.
	 * 
	 * @throws Exception
	 */
	@Test(expected = AuthenticationException.class)
	public final void testPAMAuthSemicolonBug195() throws Exception {
		if (!testingPropertiesHelper.isTestPAM(testingProperties)) {
			return;
		}

		String pamUser = "someuserBug195";
		String pamPassword = "howAbout;Here";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, pamUser, pamPassword);

		irodsAccount.setAuthenticationScheme(AuthScheme.PAM);
		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		environmentalInfoAO.getIRODSServerCurrentTime();

		AuthResponse authResponse = environmentalInfoAO.getIRODSProtocol()
				.getAuthResponse();
		Assert.assertNotNull("no authenticating account",
				authResponse.getAuthenticatingIRODSAccount());
		Assert.assertEquals("did not set authenticating account to PAM type",
				AuthScheme.PAM, authResponse

				.getAuthenticatingIRODSAccount().getAuthenticationScheme());

		irodsFileSystem.closeAndEatExceptions();

	}

}
