package org.irods.jargon.core.packinstr;

import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.StartupResponseData;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.BeforeClass;
import org.junit.Test;

public class ReconnMsgTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
	}

	@Test
	public final void testReconnMsg() {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		StartupResponseData startupResponseData = new StartupResponseData(0,
				"test", "test", 0, "test", "0");
		new ReconnMsg(irodsAccount, startupResponseData);
		// just looking for no exceptions
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testReconnMsgNullAccount() {
		IRODSAccount irodsAccount = null;
		StartupResponseData startupResponseData = new StartupResponseData(0,
				"test", "test", 0, "test", "test");
		new ReconnMsg(irodsAccount, startupResponseData);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testReconnMsgNullResponseData() {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		StartupResponseData startupResponseData = null;
		new ReconnMsg(irodsAccount, startupResponseData);
	}

}
