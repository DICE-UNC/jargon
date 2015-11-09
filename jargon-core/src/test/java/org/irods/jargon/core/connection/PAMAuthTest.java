package org.irods.jargon.core.connection;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class PAMAuthTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
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

	}

}
