/**
 *
 */
package org.irods.jargon.core.unittest.functionaltest;

import java.util.Properties;

import org.junit.Assert;

import org.irods.jargon.core.connection.AuthScheme;
import org.irods.jargon.core.connection.ClientServerNegotiationPolicy.SslNegotiationPolicy;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.connection.TrustAllX509TrustManager;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Functional tests of various permutations of ssl negotiation and auth methods.
 * These tests are contingent on the iRODS configuration and various testing
 * properties settings
 *
 * @author Mike Conway - DICE
 *
 */
public class SslNegotiationFunctionalTests {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem = null;
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
	public void testStandardLoginNoNegotiationFromClient()
			throws JargonException {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		irodsAccount.setAuthenticationScheme(AuthScheme.STANDARD);

		SettableJargonProperties settableJargonProperties = (SettableJargonProperties) irodsFileSystem
				.getJargonProperties();
		settableJargonProperties
				.setNegotiationPolicy(SslNegotiationPolicy.NO_NEGOTIATION);
		irodsFileSystem.getIrodsSession().setJargonProperties(
				settableJargonProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		AuthResponse actual = accessObjectFactory
				.authenticateIRODSAccount(irodsAccount);
		Assert.assertNotNull(actual);
	}

	@Test
	public void testStandardLoginNegDontCareFromClient() throws JargonException {

		/*
		 * Only run if ssl enabled
		 */
		if (!testingPropertiesHelper.isTestSsl(testingProperties)) {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		irodsAccount.setAuthenticationScheme(AuthScheme.STANDARD);

		SettableJargonProperties settableJargonProperties = (SettableJargonProperties) irodsFileSystem
				.getJargonProperties();
		settableJargonProperties
				.setNegotiationPolicy(SslNegotiationPolicy.CS_NEG_DONT_CARE);
		irodsFileSystem.getIrodsSession().setJargonProperties(
				settableJargonProperties);
		TrustAllX509TrustManager manager = new TrustAllX509TrustManager();
		irodsFileSystem.getIrodsSession().setX509TrustManager(manager);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		AuthResponse actual = accessObjectFactory
				.authenticateIRODSAccount(irodsAccount);
		Assert.assertNotNull(actual);
		// Do some thing
		EnvironmentalInfoAO environmentalInfoAO = accessObjectFactory
				.getEnvironmentalInfoAO(irodsAccount);
		long timeVal = environmentalInfoAO.getIRODSServerCurrentTime();
		Assert.assertTrue("time val was missing", timeVal > 0);
	}

	@Test
	public void testStandardLoginRequiredFromClient() throws JargonException {

		/*
		 * Only run if ssl enabled
		 */
		if (!testingPropertiesHelper.isTestSsl(testingProperties)) {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		irodsAccount.setAuthenticationScheme(AuthScheme.STANDARD);

		SettableJargonProperties settableJargonProperties = (SettableJargonProperties) irodsFileSystem
				.getJargonProperties();
		settableJargonProperties
				.setNegotiationPolicy(SslNegotiationPolicy.CS_NEG_REQUIRE);
		irodsFileSystem.getIrodsSession().setJargonProperties(
				settableJargonProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		AuthResponse actual = accessObjectFactory
				.authenticateIRODSAccount(irodsAccount);
		Assert.assertNotNull(actual);
		// Do some thing
		EnvironmentalInfoAO environmentalInfoAO = accessObjectFactory
				.getEnvironmentalInfoAO(irodsAccount);
		long timeVal = environmentalInfoAO.getIRODSServerCurrentTime();
		Assert.assertTrue("time val was missing", timeVal > 0);
	}

	@Test
	public void testMultiplePamLoginNegDontCareFromClientShouldTestPamWhenSslAlreadyNegotiated()
			throws JargonException {

		/*
		 * Only run if ssl enabled
		 */
		if (!testingPropertiesHelper.isTestSsl(testingProperties)) {
			return;
		}

		/*
		 * Only run if pam enabled
		 */
		if (!testingPropertiesHelper.isTestPAM(testingProperties)) {
			return;
		}

		int times = 150;
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildPamIrodsAccountFromTestProperties(testingProperties);
		irodsAccount.setAuthenticationScheme(AuthScheme.PAM);

		SettableJargonProperties settableJargonProperties = (SettableJargonProperties) irodsFileSystem
				.getJargonProperties();
		settableJargonProperties
				.setNegotiationPolicy(SslNegotiationPolicy.CS_NEG_DONT_CARE);
		irodsFileSystem.getIrodsSession().setJargonProperties(
				settableJargonProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		for (int i = 0; i < times; i++) {
			AuthResponse actual = accessObjectFactory
					.authenticateIRODSAccount(irodsAccount);
			Assert.assertNotNull(actual);
			// Do some thing
			EnvironmentalInfoAO environmentalInfoAO = accessObjectFactory
					.getEnvironmentalInfoAO(irodsAccount);
			environmentalInfoAO.getIRODSServerPropertiesFromIRODSServer();
			long timeVal = environmentalInfoAO.getIRODSServerCurrentTime();
			Assert.assertTrue("time val was missing", timeVal > 0);
			accessObjectFactory.closeSessionAndEatExceptions();
		}
	}
}
