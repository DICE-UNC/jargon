package org.irods.jargon.core.connection;

import java.util.Properties;

import junit.framework.TestCase;

import org.ietf.jgss.GSSCredential;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class GSIAuthTest {
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
	public final void testGSIAuthValid() throws Exception {

		if (!testingPropertiesHelper.isTestGSI(testingProperties)) {
			return;
		}


		String gsiHost = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_GSI_HOST_KEY);
		int gsiPort = testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, TestingPropertiesHelper.IRODS_GSI_PORT_KEY);
		String gsiZone =  testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_GSI_ZONE_KEY);
		String userDN = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_GSI_DN_KEY);
		GSSCredential gssCredential = Mockito.mock(GSSCredential.class);
		Mockito.when(gssCredential.getRemainingLifetime()).thenReturn(100);

		GSIIRODSAccount irodsAccount = GSIIRODSAccount.instance(gsiHost,
				gsiPort, gsiZone, userDN, gssCredential, "", "");

		AuthResponse authResponse = irodsFileSystem
				.getIRODSAccessObjectFactory().authenticateIRODSAccount(
						irodsAccount);
		TestCase.assertNotNull("no authenticating account",
				authResponse.getAuthenticatingIRODSAccount());

	}

}
