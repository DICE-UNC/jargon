package org.irods.jargon.datautils.connection;

import java.util.Properties;

import org.junit.Assert;

import org.irods.jargon.core.connection.AbstractIRODSMidLevelProtocol;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSMidLevelProtocol;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConnectionCreatingPoolableObjectFactoryTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@After
	public void tearDown() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testConnectionCreatingPoolableObjectFactory() {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		ConnectionCreatingPoolableObjectFactory factory = new ConnectionCreatingPoolableObjectFactory(
				irodsAccount, irodsFileSystem.getIrodsSession(),
				irodsFileSystem.getIrodsProtocolManager());
		Assert.assertNotNull("null factory", factory);
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void testConnectionCreatingPoolableObjectFactoryNullAccount() {
		IRODSAccount irodsAccount = null;
		new ConnectionCreatingPoolableObjectFactory(irodsAccount,
				irodsFileSystem.getIrodsSession(),
				irodsFileSystem.getIrodsProtocolManager());
	}

	@Test
	public void testMakeObject() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		ConnectionCreatingPoolableObjectFactory factory = new ConnectionCreatingPoolableObjectFactory(
				irodsAccount, irodsFileSystem.getIrodsSession(),
				irodsFileSystem.getIrodsProtocolManager());
		Object conn = factory.makeObject();
		Assert.assertNotNull("null connection returned", conn);
		boolean isCommand = conn instanceof IRODSMidLevelProtocol;
		Assert.assertTrue("did not get a commands", isCommand);
		factory.destroyObject(conn);
		AbstractIRODSMidLevelProtocol actual = (AbstractIRODSMidLevelProtocol) conn;
		Assert.assertFalse("command not disconnected after destroy",
				actual.isConnected());
	}

	/*
	 * @Test public void testMultiThreadedAccess() throws Exception { final
	 * IRODSAccount irodsAccount = testingPropertiesHelper
	 * .buildIRODSAccountFromTestProperties(testingProperties); final
	 * ConnectionCreatingPoolableObjectFactory factory = new
	 * ConnectionCreatingPoolableObjectFactory( irodsAccount);
	 * 
	 * final Random randomGenerator = new Random();
	 * 
	 * Runnable connRunnable = new Runnable() { public void run() { int
	 * randomInt = randomGenerator.nextInt(3000); try { Thread.sleep(randomInt);
	 * Object conn = factory.makeObject();
	 * Assert.assertNotNull("null connection returned", conn); randomInt =
	 * randomGenerator.nextInt(3000); Thread.sleep(randomInt);
	 * 
	 * 
	 * } catch (InterruptedException e) { throw new
	 * JargonRuntimeException("error in thread",e); }
	 * 
	 * } };
	 * 
	 * 
	 * 
	 * Object conn = factory.makeObject();
	 * Assert.assertNotNull("null connection returned", conn); boolean isCommand
	 * = conn instanceof IRODSCommands;
	 * Assert.assertTrue("did not get a commands", isCommand);
	 * factory.destroyObject(conn); IRODSCommands actual = (IRODSCommands) conn;
	 * Assert.assertFalse("command not disconnected after destroy",
	 * actual.isConnected()); }
	 */
}
