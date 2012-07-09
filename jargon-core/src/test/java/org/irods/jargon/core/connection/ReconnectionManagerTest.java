package org.irods.jargon.core.connection;

import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class ReconnectionManagerTest {

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

	@Test
	public final void testCreate() {
		IRODSCommands irodsCommands = Mockito.mock(IRODSCommands.class);
		ReconnectionManager reconnectionManager = ReconnectionManager
				.instance(irodsCommands);
		TestCase.assertNotNull(
				"did not set irodsCommand in reconnection manager",
				reconnectionManager.getIrodsCommands());
	}

}
