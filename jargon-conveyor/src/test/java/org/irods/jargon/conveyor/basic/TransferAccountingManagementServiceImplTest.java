package org.irods.jargon.conveyor.basic;

import java.util.Date;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.conveyor.core.ConfigurationPropertyConstants;
import org.irods.jargon.conveyor.core.ConfigurationService;
import org.irods.jargon.conveyor.core.GridAccountService;
import org.irods.jargon.conveyor.core.QueueManagerService;
import org.irods.jargon.conveyor.core.TransferAccountingManagementService;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatus.TransferState;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.transfer.dao.domain.ConfigurationProperty;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferStateEnum;
import org.irods.jargon.transfer.dao.domain.TransferStatusEnum;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:transfer-dao-beans.xml",
		"classpath:transfer-dao-hibernate-spring.cfg.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class TransferAccountingManagementServiceImplTest {
	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@Autowired
	private TransferAccountingManagementService transferAccountingManagementService;

	@Autowired
	private QueueManagerService queueManagerService;

	@Autowired
	private GridAccountService gridAccountService;

	@Autowired
	private ConfigurationService configurationService;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		gridAccountService.resetPassPhraseAndAccounts();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPrepareNewTransferForProcessing() throws Exception {
		String testUserName = "user1";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUserName, testUserName);
		String passPhrase = irodsAccount.getUserName();
		gridAccountService.validatePassPhrase(passPhrase);
		GridAccount gridAccount = gridAccountService
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);

		Transfer transfer = new Transfer();
		transfer.setCreatedAt(new Date());
		transfer.setIrodsAbsolutePath("/path");
		transfer.setLocalAbsolutePath("local");
		transfer.setTransferType(TransferType.PUT);
		transfer.setGridAccount(gridAccount);

		TransferAttempt transferAttempt = transferAccountingManagementService
				.prepareTransferForProcessing(transfer);

		Assert.assertNotNull("null transfer attempt", transferAttempt);
		Assert.assertEquals(TransferStatusEnum.OK,
				transfer.getLastTransferStatus());
		Assert.assertEquals("should have an enqueued state",
				TransferStateEnum.ENQUEUED, transfer.getTransferState());
		Assert.assertFalse("no id set", transferAttempt.getId() == 0);
		Assert.assertNotNull("no transfer parent in attempt",
				transferAttempt.getTransfer());

		Assert.assertNull("should be no start set for attempt",
				transferAttempt.getAttemptStart());
		Assert.assertNull("should not be an end date for attempt",
				transferAttempt.getAttemptEnd());
		Assert.assertNotNull("no transfer attempt status set",
				transferAttempt.getAttemptStatus());
		Assert.assertEquals("should have ok for status in attempt",
				TransferStatusEnum.OK, transferAttempt.getAttemptStatus());
		Assert.assertEquals("should have blank error message", "",
				transferAttempt.getGlobalException());

	}

	@Test
	public void testUpdateTransferAttemptWithConveyorException()
			throws Exception {
		String testUserName = "user1";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUserName, testUserName);
		String passPhrase = irodsAccount.getUserName();
		gridAccountService.validatePassPhrase(passPhrase);
		GridAccount gridAccount = gridAccountService
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);

		Transfer transfer = new Transfer();
		transfer.setCreatedAt(new Date());
		transfer.setIrodsAbsolutePath("/path");
		transfer.setLocalAbsolutePath("local");
		transfer.setTransferType(TransferType.PUT);
		transfer.setGridAccount(gridAccount);

		TransferAttempt transferAttempt = transferAccountingManagementService
				.prepareTransferForProcessing(transfer);

		TransferAttempt transferAttemptExecution = transferAccountingManagementService
				.prepareTransferForExecution(transferAttempt.getTransfer());
		transfer = transferAttempt.getTransfer();

		Exception myException;

		try {
			throw new JargonException("blah");
		} catch (JargonException je) {
			myException = je;
		}

		transferAccountingManagementService
				.updateTransferAttemptWithConveyorException(
						transferAttemptExecution, myException);

		Assert.assertNotNull("null transfer attempt", transferAttemptExecution);
		Assert.assertEquals(TransferStatusEnum.ERROR,
				transfer.getLastTransferStatus());

		Assert.assertNotNull("should be an end date for attempt",
				transferAttemptExecution.getAttemptEnd());
		Assert.assertNotNull("no transfer attempt status set",
				transferAttemptExecution.getAttemptStatus());
		Assert.assertEquals("should have an error attempt status",
				TransferStatusEnum.ERROR,
				transferAttemptExecution.getAttemptStatus());
		Assert.assertEquals(
				"should have  error message",
				TransferAccountingManagementServiceImpl.ERROR_ATTEMPTING_TO_RUN,
				transferAttemptExecution.getErrorMessage());
		Assert.assertEquals("should have global exception message",
				myException.getMessage(),
				transferAttemptExecution.getGlobalException());
		Assert.assertNotNull("should have stack trace info",
				transferAttemptExecution.getGlobalExceptionStackTrace());
		Assert.assertFalse("empty stack trace", transferAttemptExecution
				.getGlobalExceptionStackTrace().isEmpty());

	}

	@Test
	public void testUpdateTransferAttemptWithFailure() throws Exception {
		String testUserName = "user1";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUserName, testUserName);
		String passPhrase = irodsAccount.getUserName();
		gridAccountService.validatePassPhrase(passPhrase);
		GridAccount gridAccount = gridAccountService
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);

		Transfer transfer = new Transfer();
		transfer.setCreatedAt(new Date());
		transfer.setIrodsAbsolutePath("/path");
		transfer.setLocalAbsolutePath("local");
		transfer.setTransferType(TransferType.PUT);
		transfer.setGridAccount(gridAccount);

		TransferAttempt transferAttempt = transferAccountingManagementService
				.prepareTransferForProcessing(transfer);

		TransferAttempt transferAttemptExecution = transferAccountingManagementService
				.prepareTransferForExecution(transferAttempt.getTransfer());
		transfer = transferAttempt.getTransfer();

		try {
			throw new JargonException("blah");
		} catch (JargonException je) {
		}

		org.irods.jargon.core.transfer.TransferStatus overallStatus = org.irods.jargon.core.transfer.TransferStatus
				.instance(
						org.irods.jargon.core.transfer.TransferStatus.TransferType.GET,
						transfer.getIrodsAbsolutePath(), transfer
								.getLocalAbsolutePath(), transfer
								.getGridAccount().getDefaultResource(), 0L, 0L,
						0, 0, TransferState.FAILURE, transfer.getGridAccount()
								.getHost(), transfer.getGridAccount().getZone());

		transferAccountingManagementService.updateTransferAfterOverallFailure(
				overallStatus, transferAttemptExecution);

		Assert.assertNotNull("null transfer attempt", transferAttemptExecution);
		Assert.assertEquals(TransferStatusEnum.ERROR,
				transfer.getLastTransferStatus());

		Assert.assertNotNull("should be an end date for attempt",
				transferAttemptExecution.getAttemptEnd());
		Assert.assertNotNull("no transfer attempt status set",
				transferAttemptExecution.getAttemptStatus());
		Assert.assertEquals("should have an error attempt status",
				TransferStatusEnum.ERROR,
				transferAttemptExecution.getAttemptStatus());

	}

	@Test
	public void testUpdateTransferAfterRestartFileSkipped() throws Exception {
		String testUserName = "user1";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUserName, testUserName);
		String passPhrase = irodsAccount.getUserName();
		gridAccountService.validatePassPhrase(passPhrase);
		GridAccount gridAccount = gridAccountService
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);

		ConfigurationProperty logSuccessful = new ConfigurationProperty();
		logSuccessful
				.setPropertyKey(ConfigurationPropertyConstants.LOG_SUCCESSFUL_FILES_KEY);
		logSuccessful.setPropertyValue("true");
		configurationService.addConfigurationProperty(logSuccessful);

		ConfigurationProperty logRestart = new ConfigurationProperty();
		logRestart
				.setPropertyKey(ConfigurationPropertyConstants.LOG_RESTART_FILES);
		logRestart.setPropertyValue("true");
		configurationService.addConfigurationProperty(logRestart);

		Transfer transfer = new Transfer();
		transfer.setCreatedAt(new Date());
		transfer.setIrodsAbsolutePath("/path");
		transfer.setLocalAbsolutePath("/local");
		transfer.setTransferType(TransferType.PUT);
		transfer.setGridAccount(gridAccount);

		TransferAttempt transferAttempt = transferAccountingManagementService
				.prepareTransferForProcessing(transfer);
		transferAttempt = transferAccountingManagementService
				.prepareTransferForExecution(transferAttempt.getTransfer());
		TransferStatus status = TransferStatus.instance(
				TransferStatus.TransferType.PUT, "/local/1.txt", "/path", "",
				100L, 100L, 1, 2, TransferState.IN_PROGRESS_COMPLETE_FILE,
				irodsAccount.getHost(), irodsAccount.getZone());

		transferAccountingManagementService
				.updateTransferAfterSuccessfulFileTransfer(status,
						transferAttempt);

		TransferAttempt attempts[] = new TransferAttempt[transfer
				.getTransferAttempts().size()];
		attempts = transfer.getTransferAttempts().toArray(attempts);

		Assert.assertEquals(1, attempts.length);
		TransferAttempt attemptWith1Successful = attempts[attempts.length - 1];

		Assert.assertNull("should not be an end date for attempt",
				attemptWith1Successful.getAttemptEnd());
		Assert.assertNotNull("no transfer attempt status set",
				attemptWith1Successful.getAttemptStatus());
		Assert.assertEquals("should have an error attempt status",
				TransferStatusEnum.OK,
				attemptWith1Successful.getAttemptStatus());
		Assert.assertEquals("/local/1.txt",
				attemptWith1Successful.getLastSuccessfulPath());
		Assert.assertEquals(1,
				attemptWith1Successful.getTotalFilesTransferredSoFar());
		Assert.assertEquals(2, attemptWith1Successful.getTotalFilesCount());

		// cause an error now after 1 file

		try {
			throw new JargonException("blah");
		} catch (JargonException je) {
		}

		org.irods.jargon.core.transfer.TransferStatus overallStatus = org.irods.jargon.core.transfer.TransferStatus
				.instance(
						org.irods.jargon.core.transfer.TransferStatus.TransferType.GET,
						transfer.getIrodsAbsolutePath(), transfer
								.getLocalAbsolutePath(), transfer
								.getGridAccount().getDefaultResource(), 0L, 0L,
						0, 0, TransferState.FAILURE, transfer.getGridAccount()
								.getHost(), transfer.getGridAccount().getZone());

		transferAccountingManagementService.updateTransferAfterOverallFailure(
				overallStatus, attemptWith1Successful);

		// now schedule a restart...

		transferAccountingManagementService
				.prepareTransferForRestart(attemptWith1Successful.getTransfer()
						.getId());

		transfer = queueManagerService.findTransferByTransferId(transfer
				.getId());

		Assert.assertEquals("with restart, should have status enqueued",
				TransferStateEnum.ENQUEUED, transfer.getTransferState());
		Assert.assertEquals("should have reset to transfer status of OK",
				TransferStatusEnum.OK, transfer.getLastTransferStatus());

		TransferAttempt[] attemptsAfterRestart = new TransferAttempt[transfer
				.getTransferAttempts().size()];
		transfer.getTransferAttempts().toArray(attemptsAfterRestart);

		Assert.assertEquals(2, attempts.length);
		TransferAttempt restartAttempt = attemptsAfterRestart[attempts.length - 1];

		Assert.assertNull("should not be an end date for attempt",
				restartAttempt.getAttemptEnd());
		Assert.assertNull("should not be a start date for attempt",
				restartAttempt.getAttemptStart());
		Assert.assertNotNull("no transfer attempt status set",
				restartAttempt.getAttemptStatus());
		Assert.assertEquals("should have an error attempt status",
				TransferStatusEnum.OK, restartAttempt.getAttemptStatus());
		Assert.assertEquals("/local/1.txt",
				restartAttempt.getLastSuccessfulPath());
		Assert.assertEquals(1, restartAttempt.getTotalFilesTransferredSoFar());
		Assert.assertEquals(2, restartAttempt.getTotalFilesCount());

	}
}
