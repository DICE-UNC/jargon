package org.irods.jargon.core.connection.auth;

import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class KerberosAuthTest {

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
	public final void testAuthenticate() throws Exception {
		AuthenticationFactory authenticationFactory = new AuthenticationFactoryImpl();
		KerberosAuth authMechanism = (KerberosAuth) authenticationFactory
				.instanceAuthMechanism(IRODSAccount.AuthScheme.KERBEROS.name());
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, "test1@IRODSKRB", "");

		authMechanism.authenticate(null, irodsAccount);


	}

	@Test
	public final void testKerberosAuth() throws Exception {
		AuthenticationFactory authenticationFactory = new AuthenticationFactoryImpl();
		AuthMechanism authMechanism = authenticationFactory
				.instanceAuthMechanism(IRODSAccount.AuthScheme.KERBEROS.name());
		TestCase.assertNotNull("null authMechanism from factory", authMechanism);
		boolean iskrb = authMechanism instanceof KerberosAuth;
		TestCase.assertTrue("did not get kerberos auth mechanism", iskrb);

	}

}
