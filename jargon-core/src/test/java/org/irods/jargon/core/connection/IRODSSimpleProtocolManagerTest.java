package org.irods.jargon.core.connection;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSSimpleProtocolManagerTest {
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
	public void testGetIRODSConnection() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSManagedConnection connection = irodsConnectionManager
				.getIRODSProtocol(irodsAccount);
		Assert.assertTrue("this connection is not connected",
				connection.isConnected());
		connection.disconnect();
		Assert.assertFalse("the connection is not closed after disconnect",
				connection.isConnected());
	}

	@Test(expected = AuthenticationException.class)
	public void testGetIRODSConnectionForInvalidUser() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, "iam-a-bogus-user", "irockthecode");
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		irodsConnectionManager.getIRODSProtocol(irodsAccount);

	}

	@Test
	public void testReturnIRODSConnection() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSManagedConnection connection = irodsConnectionManager
				.getIRODSProtocol(irodsAccount);
		irodsConnectionManager.returnIRODSConnection(connection);
		Assert.assertFalse("the connection is not closed after disconnect",
				connection.isConnected());
	}

	@Test
	public void testReturnClosedIRODSConnection() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSManagedConnection connection = irodsConnectionManager
				.getIRODSProtocol(irodsAccount);
		connection.disconnect();
		irodsConnectionManager.returnIRODSConnection(connection);
		Assert.assertFalse("the connection is not closed after disconnect",
				connection.isConnected());
	}

	@Test
	public void testReturnIRODSConnectionWithIoException() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSManagedConnection connection = irodsConnectionManager
				.getIRODSProtocol(irodsAccount);
		irodsConnectionManager.returnConnectionWithIoException(connection);
		Assert.assertFalse("the connection is not closed after disconnect",
				connection.isConnected());
	}

	@Test
	public void testReturnClosedIRODSConnectionWithIoException()
			throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSManagedConnection connection = irodsConnectionManager
				.getIRODSProtocol(irodsAccount);
		connection.disconnect();
		irodsConnectionManager.returnConnectionWithIoException(connection);
		Assert.assertFalse("the connection is not closed after disconnect",
				connection.isConnected());
	}

	@Test
	public void testOpenAndClose50Connections() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		for (int i = 0; i < 50; i++) {
			IRODSManagedConnection connection = irodsConnectionManager
					.getIRODSProtocol(irodsAccount);
			Assert.assertTrue("this connection is not connected",
					connection.isConnected());
			connection.disconnect();
			Assert.assertFalse("the connection is not closed after disconnect",
					connection.isConnected());
		}

	}

	@Test
	public void testOpenAndCloseNConnectionsFrom3Threads() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		// IRODSAccount irodsAccount2 =
		// testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();

		ConnectionLoop conn1 = new ConnectionLoop(irodsAccount,
				irodsConnectionManager, 30);
		ConnectionLoop conn2 = new ConnectionLoop(irodsAccount,
				irodsConnectionManager, 30);
		ConnectionLoop conn3 = new ConnectionLoop(irodsAccount,
				irodsConnectionManager, 30);

		Thread t1 = new Thread(conn1);
		t1.start();
		Thread.sleep(100); // FIXME: temp code, investigate conn closed by peer,
							// I think it's virtualbox
		Thread t2 = new Thread(conn2);
		t2.start();
		// Thread.sleep(200);
		Thread t3 = new Thread(conn3);
		t3.start();
		int finishedCtr = 0;
		boolean allFinished = false;

		while (!allFinished) {
			finishedCtr = 0;
			if (conn1.isFinished()) {
				finishedCtr++;
			}

			if (conn2.isFinished()) {
				finishedCtr++;
			}

			if (conn3.isFinished()) {
				finishedCtr++;
			}

			if (finishedCtr == 3) {
				allFinished = true;
			} else {
				Thread.sleep(1000);
			}
		}

		if (conn1.caughtException != null) {
			throw new Exception(conn1.caughtException);
		}

		if (conn2.caughtException != null) {
			throw new Exception(conn2.caughtException);
		}

		if (conn3.caughtException != null) {
			throw new Exception(conn3.caughtException);
		}

	}

	class ConnectionLoop implements Runnable {
		private final IRODSAccount irodsAccount;
		private final IRODSProtocolManager irodsConnectionManager;
		private final int iterations;
		private boolean finished = false;
		private Exception caughtException = null;

		public ConnectionLoop(final IRODSAccount irodsAccount,
				final IRODSProtocolManager irodsConnectionManager,
				final int iterations) {
			this.irodsAccount = irodsAccount;
			this.irodsConnectionManager = irodsConnectionManager;
			this.iterations = iterations;
		}

		public synchronized boolean isFinished() {
			return finished;
		}

		public synchronized void setFinished(final boolean finished) {
			this.finished = finished;
		}

		@Override
		public void run() {
			try {
				for (int i = 0; i < iterations; i++) {
					// Pause for 1 second
					Thread.sleep(1000);
					IRODSManagedConnection connection = irodsConnectionManager
							.getIRODSProtocol(irodsAccount);
					connection.shutdown();
				}
				setFinished(true);

			} catch (InterruptedException e) {
				setFinished(true);
				caughtException = e;
			} catch (JargonException je) {
				setFinished(true);
				caughtException = je;
			}
		}
	}

}
