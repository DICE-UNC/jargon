package org.irods.jargon.transfer.engine;

import java.util.Date;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.transfer.TransferServiceFactoryImpl;
import org.irods.jargon.transfer.dao.domain.FrequencyType;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.irods.jargon.transfer.dao.domain.SynchronizationType;
import org.irods.jargon.transfer.engine.synch.SynchManagerService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TransferQueueServiceForSynchTest {

	private static Properties testingProperties = new Properties();

	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@SuppressWarnings("unused")
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;

	public static final String IRODS_TEST_SUBDIR_PATH = "TransferQueueServiceForSynchTest";

	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(
				testingProperties);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		String testDatabase = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(".idrop/derby/target/database/transfer");
		String databaseUrl = "jdbc:derby:" + testDatabase;
		DatabasePreparationUtils.clearAllDatabaseForTesting(databaseUrl,
				"transfer", "transfer");

	}

	@Before
	public void setUpEach() throws Exception {

	}

	@Test(expected = IllegalArgumentException.class)
	public void testEnqueueSynchNullSynchronization() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();

		transferQueueService.enqueueSynchTransfer(null, irodsAccount);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testEnqueueSynchNullIrodsAccount() throws Exception {
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();

		transferQueueService.enqueueSynchTransfer(null, null);

	}

	@Test
	public void testEnqueueSynch() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();
		transferQueueService.purgeQueue();
		SynchManagerService synchManagerService = transferServiceFactory
				.instanceSynchManagerService();

		String rootCollection = "testEnqueueSynch";

		Synchronization synchronization = new Synchronization();
		synchronization
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchronization.setName(rootCollection);
		synchronization.setCreatedAt(new Date());
		synchronization.setDefaultResourceName(irodsAccount
				.getDefaultStorageResource());
		synchronization.setFrequencyType(FrequencyType.EVERY_FIFTEEN_MINUTES);
		synchronization.setIrodsHostName(irodsAccount.getHost());
		synchronization.setIrodsPassword(irodsAccount.getPassword());
		synchronization.setIrodsPort(irodsAccount.getPort());
		synchronization.setIrodsSynchDirectory(rootCollection);
		synchronization.setIrodsUserName(irodsAccount.getUserName());
		synchronization.setIrodsZone(irodsAccount.getZone());
		synchronization.setLocalSynchDirectory(rootCollection);
		synchManagerService.createNewSynchConfiguration(synchronization);

		LocalIRODSTransfer synchTransfer = transferQueueService
				.enqueueSynchTransfer(synchronization, irodsAccount);
		Assert.assertNotNull("null synch transfer", synchTransfer);
		Assert.assertEquals("did not get the synch from the transfer",
				rootCollection, synchTransfer.getSynchronization().getName());

	}

	@Test(expected = JargonException.class)
	public void testEnqueueSynchSynchNotPersisted() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();

		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();
		transferQueueService.purgeQueue();
		transferServiceFactory.instanceSynchManagerService();

		String rootCollection = "testEnqueueSynchSynchNotPersisted";

		Synchronization synchronization = new Synchronization();
		synchronization
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchronization.setName(rootCollection);
		synchronization.setCreatedAt(new Date());
		synchronization.setDefaultResourceName(irodsAccount
				.getDefaultStorageResource());
		synchronization.setFrequencyType(FrequencyType.EVERY_FIFTEEN_MINUTES);
		synchronization.setIrodsHostName(irodsAccount.getHost());
		synchronization.setIrodsPassword(irodsAccount.getPassword());
		synchronization.setIrodsPort(irodsAccount.getPort());
		synchronization.setIrodsSynchDirectory(rootCollection);
		synchronization.setIrodsUserName(irodsAccount.getUserName());
		synchronization.setIrodsZone(irodsAccount.getZone());
		synchronization.setLocalSynchDirectory(rootCollection);

		transferQueueService
				.enqueueSynchTransfer(synchronization, irodsAccount);

	}

}
