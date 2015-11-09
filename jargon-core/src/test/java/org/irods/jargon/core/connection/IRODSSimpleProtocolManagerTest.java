package org.irods.jargon.core.connection;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSSimpleProtocolManagerTest {
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
	public void testGetIRODSConnection() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		AbstractIRODSMidLevelProtocol irodsProtocol = irodsFileSystem
				.getIrodsSession().currentConnection(irodsAccount);
		Assert.assertTrue("this connection is not connected",
				irodsProtocol.isConnected());
		irodsProtocol.disconnectWithForce();
		Assert.assertFalse("the connection is not closed after disconnect",
				irodsProtocol.isConnected());
	}

	@Test(expected = InvalidUserException.class)
	public void testGetIRODSConnectionForInvalidUser() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, "iam-a-bogus-user", "irockthecode");
		irodsFileSystem.getIrodsSession().currentConnection(irodsAccount);

	}

	@Test
	public void testOpenAndClose50Connections() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		for (int i = 0; i < 50; i++) {
			AbstractIRODSMidLevelProtocol irodsProtocol = irodsFileSystem
					.getIrodsSession().currentConnection(irodsAccount);

			Assert.assertTrue("this connection is not connected",
					irodsProtocol.isConnected());
			irodsProtocol.disconnectWithForce();
			Assert.assertFalse("the connection is not closed after disconnect",
					irodsProtocol.isConnected());
		}

	}

	@Test
	public void testOpenAndCloseNConnectionsFrom3Threads() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		ConnectionLoop conn1 = new ConnectionLoop(irodsAccount,
				irodsFileSystem, 30);
		ConnectionLoop conn2 = new ConnectionLoop(irodsAccount,
				irodsFileSystem, 30);
		ConnectionLoop conn3 = new ConnectionLoop(irodsAccount,
				irodsFileSystem, 30);

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
		private final IRODSFileSystem irodsFileSystem;
		private final int iterations;
		private boolean finished = false;
		private Exception caughtException = null;

		public ConnectionLoop(final IRODSAccount irodsAccount,
				final IRODSFileSystem irodsFileSystem, final int iterations) {
			this.irodsAccount = irodsAccount;
			this.irodsFileSystem = irodsFileSystem;
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
					AbstractIRODSMidLevelProtocol connection = irodsFileSystem
							.getIrodsSession().currentConnection(irodsAccount);
					connection.disconnect();
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

	/**
	 * Make sure the default authentication factory is created
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreatesDefaultAuthenticationFactoryImpl() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		Assert.assertNotNull(irodsConnectionManager.getAuthenticationFactory());
	}

}
