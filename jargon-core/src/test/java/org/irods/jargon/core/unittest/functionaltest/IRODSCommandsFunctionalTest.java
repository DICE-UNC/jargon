package org.irods.jargon.core.unittest.functionaltest;

import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSCommandsFunctionalTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem = null;

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

	/**
	 * [#1039] invalid auth potentially leaving open connection/agent
	 *
	 * @throws Exception
	 */
	@Test
	public void testLotsOfInvalidAuths() throws Exception {

		int ctr = 1000;
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		irodsAccount.setPassword("bogus");

		for (int i = 0; i < ctr; i++) {
			try {
				irodsFileSystem.getIRODSAccessObjectFactory().authenticateIRODSAccount(irodsAccount);
			} catch (AuthenticationException ae) {
				// ok
			}
		}

	}

}
