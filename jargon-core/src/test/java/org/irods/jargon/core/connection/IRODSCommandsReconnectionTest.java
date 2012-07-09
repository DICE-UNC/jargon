/**
 * 
 */
package org.irods.jargon.core.connection;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Functional style test of reconnect behavior of IRODSCommands.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSCommandsReconnectionTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testConnectReconnectThenDisconnect() throws Exception {
		SettableJargonProperties jargonProperties = new SettableJargonProperties();
		jargonProperties.setReconnect(true);
		IRODSFileSystem testFS = IRODSFileSystem.instance();
		testFS.getIrodsSession().setJargonProperties(jargonProperties);
		int interationCount = 10;
		int count = 4;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		for (int i = 0; i < interationCount; i++) {

			System.out.println("============================= iteration:" + i
					+ "==========================");

			// connect
			IRODSCommands irodsCommands = testFS.getIRODSAccessObjectFactory()
					.getIrodsSession().currentConnection(irodsAccount);

			for (int j = 0; j < count; j++) {

				System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + j);
				// reconnect
				irodsCommands.reconnect();

				EnvironmentalInfoAO environmentalInfoAO = testFS
						.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
								irodsAccount);

				IRODSServerProperties props = environmentalInfoAO
						.getIRODSServerProperties();
				Assert.assertNotNull("null props", props);
			}

			System.out
					.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  now disconnect >>>>>>>>>");
			// disconnect
			testFS.closeAndEatExceptions();
		}

	}

}
