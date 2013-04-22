package org.irods.jargon.datautils.connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSCommands;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class TempPasswordCachingProtocolManagerTest {

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
	public void testGetIRODSProtocol() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TempPasswordCachingProtocolManager manager = new TempPasswordCachingProtocolManager(
				irodsAccount, irodsFileSystem.getIrodsSession(), irodsFileSystem.getIrodsProtocolManager());
		IRODSSession irodsSession = IRODSSession.instance(manager);
		Assert.assertNotNull("null manager returned", manager);
		IRODSCommands commands = manager.getIRODSProtocol(irodsAccount,
				irodsSession
						.buildPipelineConfigurationBasedOnJargonProperties(), irodsFileSystem.getIrodsSession());
		Assert.assertTrue("commands not connected", commands.isConnected());
		manager.returnIRODSConnection(commands);
		manager.destroy();
		commands.disconnect();
	}

	@Test
	public void testGetIRODSProtocolViaIRODSFileSystem() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TempPasswordCachingProtocolManager manager = new TempPasswordCachingProtocolManager(
				irodsAccount, irodsFileSystem.getIrodsSession(), irodsFileSystem.getIrodsProtocolManager());
		IRODSFileSystem irodsFileSystem = new IRODSFileSystem(manager);
		EnvironmentalInfoAO eAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getEnvironmentalInfoAO(irodsAccount);
		eAO.getIRODSServerPropertiesFromIRODSServer();
		irodsFileSystem.close();
	}

	@Test
	public void testGetIRODSProtocolViaIRODSFileSystemMultiThreaded()
			throws Exception {
		int numThreads = 3;
		final int numTimes = 10;
		final IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TempPasswordCachingProtocolManager manager = new TempPasswordCachingProtocolManager(
				irodsAccount, irodsFileSystem.getIrodsSession(), irodsFileSystem.getIrodsProtocolManager());
		final IRODSFileSystem irodsFileSystem = new IRODSFileSystem(manager);

		final Random randomGenerator = new Random();

		Runnable connRunnable = new Runnable() {

			@Override
			public void run() {

				for (int i = 0; i < numTimes; i++) {

					int randomInt = randomGenerator.nextInt(2000);
					try {
						Thread.sleep(randomInt);
						EnvironmentalInfoAO eAO = irodsFileSystem
								.getIRODSAccessObjectFactory()
								.getEnvironmentalInfoAO(irodsAccount);
						eAO.getIRODSServerPropertiesFromIRODSServer();
						randomInt = randomGenerator.nextInt(1000);
						Thread.sleep(randomInt);
						irodsFileSystem.close();
					} catch (Exception e) {
						throw new JargonRuntimeException("error in thread", e);
					}

				}
			}
		};

		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < numThreads; i++) {
			Thread t = new Thread(connRunnable);
			t.start();
			threads.add(t);
		}

		for (Thread thread : threads) {
			thread.join();
		}

	}
}
