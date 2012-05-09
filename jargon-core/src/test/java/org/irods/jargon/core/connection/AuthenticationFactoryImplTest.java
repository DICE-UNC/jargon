package org.irods.jargon.core.connection;

import junit.framework.TestCase;

import org.irods.jargon.core.connection.AuthMechanism;
import org.irods.jargon.core.connection.AuthenticationFactory;
import org.irods.jargon.core.connection.AuthenticationFactoryImpl;
import org.irods.jargon.core.connection.GSIAuth;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.KerberosAuth;
import org.irods.jargon.core.connection.auth.AuthUnavailableException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class AuthenticationFactoryImplTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * Ask for and get the kerberos auth mech
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testInstanceAuthMechanismKerberos() throws Exception {
		AuthenticationFactory authenticationFactory = new AuthenticationFactoryImpl();
		AuthMechanism authMechanism = authenticationFactory
				.instanceAuthMechanism(IRODSAccount.AuthScheme.KERBEROS.name());
		TestCase.assertNotNull(
				"null auth mechanism returned, should be Kerberos",
				authMechanism);
		boolean isRightType = authMechanism instanceof KerberosAuth;
		TestCase.assertTrue("Not kerberos auth", isRightType);
	}

	/**
	 * Ask for and get the globus GSI auth mechanism
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testInstanceAuthMechanismGSI() throws Exception {
		AuthenticationFactory authenticationFactory = new AuthenticationFactoryImpl();
		AuthMechanism authMechanism = authenticationFactory
				.instanceAuthMechanism(IRODSAccount.AuthScheme.GSI.name());
		TestCase.assertNotNull("null auth mechanism returned, should be GSI",
				authMechanism);
		boolean isRightType = authMechanism instanceof GSIAuth;
		TestCase.assertTrue("Not GSI auth", isRightType);
	}

	/**
	 * Ask for a null mech, get an exception
	 * 
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceNullMechanism() throws Exception {
		AuthenticationFactory authenticationFactory = new AuthenticationFactoryImpl();
		authenticationFactory.instanceAuthMechanism(null);

	}

	/**
	 * Ask for a blank mech, get an exception
	 * 
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceBlankMechanism() throws Exception {
		AuthenticationFactory authenticationFactory = new AuthenticationFactoryImpl();
		authenticationFactory.instanceAuthMechanism("");

	}

	/**
	 * Ask for a mech that does not exist
	 * 
	 * @throws Exception
	 */
	@Test(expected = AuthUnavailableException.class)
	public final void testInstanceUnknownMechanism() throws Exception {
		AuthenticationFactory authenticationFactory = new AuthenticationFactoryImpl();
		authenticationFactory.instanceAuthMechanism("bogus");

	}

}
