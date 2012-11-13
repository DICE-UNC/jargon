package org.irods.jargon.core.connection;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;

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
	public final void testGSIAccountCreate() throws Exception {

		if (!testingPropertiesHelper.isTestGSI(testingProperties)) {
			return;
		}

		File credentialFile = new File(
				(String) testingProperties
						.get(TestingPropertiesHelper.IRODS_GSI_CERT_PATH));

		GSIIRODSAccount irodsAccount = GSIUtilities
				.createGSIIRODSAccountFromCredential(
						credentialFile,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_GSI_HOST_KEY),
						testingPropertiesHelper.getPropertyValueAsInt(
								testingProperties,
								TestingPropertiesHelper.IRODS_GSI_PORT_KEY), "");

		Assert.assertFalse("did not set user DN from cert", irodsAccount
				.getDistinguishedName().isEmpty());

	}

	@Test
	public final void testGSIAccountCreateWithDefaultResource()
			throws Exception {

		if (!testingPropertiesHelper.isTestGSI(testingProperties)) {
			return;
		}

		String testResc = "resc";

		File credentialFile = new File(
				(String) testingProperties
						.get(TestingPropertiesHelper.IRODS_GSI_CERT_PATH));

		GSIIRODSAccount irodsAccount = GSIUtilities
				.createGSIIRODSAccountFromCredential(
						credentialFile,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_GSI_HOST_KEY),
						testingPropertiesHelper.getPropertyValueAsInt(
								testingProperties,
								TestingPropertiesHelper.IRODS_GSI_PORT_KEY),
						testResc);

		Assert.assertFalse("did not set user DN from cert", irodsAccount
				.getDistinguishedName().isEmpty());

		Assert.assertEquals("did not set default storage resource", testResc,
				irodsAccount.getDefaultStorageResource());

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testGSIAccountCreateWithMissingCertFile()
			throws Exception {

		if (!testingPropertiesHelper.isTestGSI(testingProperties)) {
			return;
		}

		String testResc = "resc";

		File credentialFile = new File("/blah/blah/blah/idontexist.pem");

		GSIUtilities
				.createGSIIRODSAccountFromCredential(
						credentialFile,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_GSI_HOST_KEY),
						testingPropertiesHelper.getPropertyValueAsInt(
								testingProperties,
								TestingPropertiesHelper.IRODS_GSI_PORT_KEY),
						testResc);

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testGSIAccountCreateWithNullCertFile() throws Exception {

		if (!testingPropertiesHelper.isTestGSI(testingProperties)) {
			return;
		}

		String testResc = "resc";

		File credentialFile = null;

		GSIUtilities
				.createGSIIRODSAccountFromCredential(
						credentialFile,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_GSI_HOST_KEY),
						testingPropertiesHelper.getPropertyValueAsInt(
								testingProperties,
								TestingPropertiesHelper.IRODS_GSI_PORT_KEY),
						testResc);

	}

}
