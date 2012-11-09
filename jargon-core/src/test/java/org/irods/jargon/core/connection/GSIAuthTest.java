package org.irods.jargon.core.connection;

import java.io.File;
import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.connection.auth.GSIUtilities;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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

		File credentialFile =new File((String) testingProperties.get(TestingPropertiesHelper.IRODS_GSI_CERT_PATH));
		
		GSIIRODSAccount irodsAccount = GSIUtilities
				.createGSIIRODSAccountFromCredential(
						credentialFile,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_GSI_HOST_KEY),
						testingPropertiesHelper.getPropertyValueAsInt(
								testingProperties,
								TestingPropertiesHelper.IRODS_GSI_PORT_KEY),
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_GSI_DN_KEY));
		
		AuthResponse authResponse = irodsFileSystem
				.getIRODSAccessObjectFactory().authenticateIRODSAccount(
						irodsAccount);
		TestCase.assertNotNull("no authenticating account",
				authResponse.getAuthenticatingIRODSAccount());

	}

}
