package org.irods.jargon.core.connection;

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
	 * Ask for a null mech, get an exception
	 *
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceNullMechanism() throws Exception {
		AuthenticationFactory authenticationFactory = new AuthenticationFactoryImpl();
		authenticationFactory.instanceAuthMechanism(null);

	}
}
