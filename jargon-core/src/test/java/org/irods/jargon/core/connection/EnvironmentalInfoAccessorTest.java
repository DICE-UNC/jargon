package org.irods.jargon.core.connection;

import static org.junit.Assert.*;

import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class EnvironmentalInfoAccessorTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.AssertionHelper assertionHelper = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		assertionHelper = new org.irods.jargon.testutils.AssertionHelper();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testEnvironmentalInfoAccessor() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		EnvironmentalInfoAccessor target = new EnvironmentalInfoAccessor(
				irodsSession.currentConnection(irodsAccount));
		TestCase.assertNotNull(target);

	}

	@Test
	public final void testGetIRODSServerProperties() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		EnvironmentalInfoAccessor target = new EnvironmentalInfoAccessor(
				irodsSession.currentConnection(irodsAccount));
		IRODSServerProperties irodsServerProperties = target
				.getIRODSServerProperties();
		TestCase.assertEquals(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_ZONE_KEY),
				irodsServerProperties.getRodsZone());
	}

}
