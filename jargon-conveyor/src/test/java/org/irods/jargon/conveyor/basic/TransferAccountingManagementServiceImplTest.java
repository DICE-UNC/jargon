package org.irods.jargon.conveyor.basic;

import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.conveyor.core.ConfigurationPropertyConstants;
import org.irods.jargon.conveyor.core.ConfigurationService;
import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.GridAccountService;
import org.irods.jargon.conveyor.core.QueueManagerService;
import org.irods.jargon.conveyor.core.RejectedTransferException;
import org.irods.jargon.conveyor.core.TransferAccountingManagementService;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatus.TransferState;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.transfer.dao.TransferDAO;
import org.irods.jargon.transfer.dao.domain.ConfigurationProperty;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferItem;
import org.irods.jargon.transfer.dao.domain.TransferStateEnum;
import org.irods.jargon.transfer.dao.domain.TransferStatusEnum;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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

	@Autowired
	private TransferDAO transferDAO;

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
		transfer.setCreatedAt(new Timestamp(System.currentTimeMillis()));
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
		transfer.setCreatedAt(new Timestamp(System.currentTimeMillis()));
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
		Assert.assertEquals("should have  error message",
				TransferAccountingManagementService.ERROR_ATTEMPTING_TO_RUN,
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
		transfer.setCreatedAt(new Timestamp(System.currentTimeMillis()));
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
						0, 0, 0, TransferState.FAILURE, transfer
								.getGridAccount().getHost(), transfer
								.getGridAccount().getZone());

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
	public void testPrepareTransferForRestart() throws Exception {
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
		transfer.setCreatedAt(new Timestamp(System.currentTimeMillis()));
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
				100L, 100L, 1, 0, 2, TransferState.IN_PROGRESS_COMPLETE_FILE,
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
		JargonException myException;
		try {
			throw new JargonException("blah");
		} catch (JargonException je) {
			myException = je;
		}

		TransferStatus overallStatus = TransferStatus.instanceForException(
				TransferStatus.TransferType.GET, transfer
						.getIrodsAbsolutePath(), transfer
						.getLocalAbsolutePath(), transfer.getGridAccount()
						.getDefaultResource(), 0L, 0L, 0, 0, 0, myException,
				irodsAccount.getHost(), irodsAccount.getZone());

		transferAccountingManagementService.updateTransferAfterOverallFailure(
				overallStatus, attemptWith1Successful);

		// now schedule a restart...

		transferAccountingManagementService
				.prepareTransferForRestart(attemptWith1Successful.getTransfer()
						.getId());

		Assert.assertEquals("with restart, should have status enqueued",
				TransferStateEnum.ENQUEUED, transfer.getTransferState());
		Assert.assertEquals("should have reset to transfer status of OK",
				TransferStatusEnum.OK, transfer.getLastTransferStatus());

		TransferAttempt[] attemptsAfterRestart = new TransferAttempt[transfer
				.getTransferAttempts().size()];
		transfer.getTransferAttempts().toArray(attemptsAfterRestart);

		Assert.assertEquals(2, attemptsAfterRestart.length);
		TransferAttempt restartAttempt = attemptsAfterRestart[attemptsAfterRestart.length - 1];

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

	}

	@Test(expected = RejectedTransferException.class)
	public void testPrepareTransferForRestartBadId() throws Exception {
		transferAccountingManagementService.prepareTransferForRestart(new Long(
				-1000));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPrepareTransferForExecutionNullTransfer() throws Exception {
		transferAccountingManagementService.prepareTransferForExecution(null);
	}

	@Test(expected = ConveyorExecutionException.class)
	public void testPrepareTransferForExecutionNullIdInTransfer()
			throws Exception {
		Transfer transfer = new Transfer();
		transferAccountingManagementService
				.prepareTransferForExecution(transfer);
	}

	@Test(expected = ConveyorExecutionException.class)
	public void testPrepareTransferForExecutionNoAttemptInTransfer()
			throws Exception {
		String testUserName = "test1";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUserName, testUserName);
		String passPhrase = irodsAccount.getUserName();
		gridAccountService.validatePassPhrase(passPhrase);
		GridAccount gridAccount = gridAccountService
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);

		Transfer transfer = new Transfer();
		transfer.setGridAccount(gridAccount);
		transfer.setIrodsAbsolutePath("x");
		transfer.setLocalAbsolutePath("blah");
		transfer.setTransferState(TransferStateEnum.ENQUEUED);
		transfer.setTransferType(TransferType.PUT);
		transferDAO.save(transfer);
		transferAccountingManagementService
				.prepareTransferForExecution(transfer);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUpdateTransferAfterSuccessfulFileTransferNullStatus()
			throws Exception {
		TransferAttempt transferAttempt = new TransferAttempt();
		transferAccountingManagementService
				.updateTransferAfterSuccessfulFileTransfer(null,
						transferAttempt);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUpdateTransferAfterSuccessfulFileTransferNullTransfer()
			throws Exception {
		TransferStatus transferStatus = Mockito.mock(TransferStatus.class);
		transferAccountingManagementService
				.updateTransferAfterSuccessfulFileTransfer(transferStatus, null);
	}

	@Test
	public void testUpdateTransferAfterOverallSuccess() throws Exception {
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
		transfer.setCreatedAt(new Timestamp(System.currentTimeMillis()));
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
				100L, 100L, 1, 0, 2, TransferState.IN_PROGRESS_COMPLETE_FILE,
				irodsAccount.getHost(), irodsAccount.getZone());

		transferAccountingManagementService
				.updateTransferAfterSuccessfulFileTransfer(status,
						transferAttempt);

		TransferStatus overallSuccess = TransferStatus
				.instance(TransferStatus.TransferType.PUT, "/", "/", "", 1L,
						1L, 1, 0, 1,
						TransferStatus.TransferState.OVERALL_COMPLETION,
						"host", "zone");

		transferAccountingManagementService.updateTransferAfterOverallSuccess(
				overallSuccess, transferAttempt);
		Assert.assertEquals(TransferStatusEnum.OK,
				transfer.getLastTransferStatus());

		TransferAttempt[] attemptsAfterSuccess = new TransferAttempt[transfer
				.getTransferAttempts().size()];
		transfer.getTransferAttempts().toArray(attemptsAfterSuccess);

		Assert.assertEquals(1, attemptsAfterSuccess.length);
		TransferAttempt successfulAttempt = attemptsAfterSuccess[attemptsAfterSuccess.length - 1];

		Assert.assertNotNull("should be an end date for attempt",
				successfulAttempt.getAttemptEnd());
		Assert.assertNotNull("no transfer attempt status set",
				successfulAttempt.getAttemptStatus());
		Assert.assertEquals("should have an error attempt status",
				TransferStatusEnum.OK, successfulAttempt.getAttemptStatus());

	}

	@Test
	public void testUpdateTransferAfterOverallWarning() throws Exception {
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
		transfer.setCreatedAt(new Timestamp(System.currentTimeMillis()));
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
				100L, 100L, 1, 0, 2, TransferState.IN_PROGRESS_COMPLETE_FILE,
				irodsAccount.getHost(), irodsAccount.getZone());

		transferAccountingManagementService
				.updateTransferAfterSuccessfulFileTransfer(status,
						transferAttempt);

		TransferStatus overallSuccess = TransferStatus
				.instance(TransferStatus.TransferType.PUT, "/", "/", "", 1L,
						1L, 1, 0, 1,
						TransferStatus.TransferState.OVERALL_COMPLETION,
						"host", "zone");

		transferAccountingManagementService
				.updateTransferAfterOverallWarningByFileErrorThreshold(
						overallSuccess, transferAttempt);
		Assert.assertEquals(TransferStatusEnum.WARNING,
				transfer.getLastTransferStatus());

		TransferAttempt[] attemptsAfterSuccess = new TransferAttempt[transfer
				.getTransferAttempts().size()];
		transfer.getTransferAttempts().toArray(attemptsAfterSuccess);

		Assert.assertEquals(1, attemptsAfterSuccess.length);
		TransferAttempt successfulAttempt = attemptsAfterSuccess[attemptsAfterSuccess.length - 1];

		Assert.assertNotNull("should be an end date for attempt",
				successfulAttempt.getAttemptEnd());
		Assert.assertNotNull("no transfer attempt status set",
				successfulAttempt.getAttemptStatus());
		Assert.assertEquals(
				"shold have error message",
				TransferAccountingManagementService.WARNING_SOME_FAILED_MESSAGE,
				successfulAttempt.getErrorMessage());
		Assert.assertEquals("should have a warning attempt status",
				TransferStatusEnum.WARNING,
				successfulAttempt.getAttemptStatus());

	}

	@Test
	public void testUpdateTransferAfterOverallWarningNoFilesTransferred()
			throws Exception {
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
		transfer.setCreatedAt(new Timestamp(System.currentTimeMillis()));
		transfer.setIrodsAbsolutePath("/path");
		transfer.setLocalAbsolutePath("/local");
		transfer.setTransferType(TransferType.PUT);
		transfer.setGridAccount(gridAccount);

		TransferAttempt transferAttempt = transferAccountingManagementService
				.prepareTransferForProcessing(transfer);
		transferAttempt = transferAccountingManagementService
				.prepareTransferForExecution(transferAttempt.getTransfer());

		TransferStatus overallSuccess = TransferStatus
				.instance(TransferStatus.TransferType.PUT, "/", "/", "", 1L,
						1L, 1, 0, 1,
						TransferStatus.TransferState.OVERALL_COMPLETION,
						"host", "zone");

		transferAccountingManagementService
				.updateTransferAfterOverallWarningNoFilesTransferred(
						overallSuccess, transferAttempt);

		Assert.assertEquals(TransferStatusEnum.WARNING,
				transfer.getLastTransferStatus());

		TransferAttempt[] attemptsAfterSuccess = new TransferAttempt[transfer
				.getTransferAttempts().size()];
		transfer.getTransferAttempts().toArray(attemptsAfterSuccess);

		Assert.assertEquals(1, attemptsAfterSuccess.length);
		TransferAttempt successfulAttempt = attemptsAfterSuccess[attemptsAfterSuccess.length - 1];

		Assert.assertNotNull("should be an end date for attempt",
				successfulAttempt.getAttemptEnd());
		Assert.assertNotNull("no transfer attempt status set",
				successfulAttempt.getAttemptStatus());
		Assert.assertEquals(
				"shold have error message",
				TransferAccountingManagementService.WARNING_NO_FILES_TRANSFERRED_MESSAGE,
				successfulAttempt.getErrorMessage());
		Assert.assertEquals("should have a warning attempt status",
				TransferStatusEnum.WARNING,
				successfulAttempt.getAttemptStatus());

	}

	@Test
	public void testUpdateTransferAfterOverallFailure() throws Exception {
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
		transfer.setCreatedAt(new Timestamp(System.currentTimeMillis()));
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
				100L, 100L, 1, 0, 2, TransferState.IN_PROGRESS_COMPLETE_FILE,
				irodsAccount.getHost(), irodsAccount.getZone());

		transferAccountingManagementService
				.updateTransferAfterSuccessfulFileTransfer(status,
						transferAttempt);

		TransferStatus overallSuccess = TransferStatus
				.instance(TransferStatus.TransferType.PUT, "/", "/", "", 1L,
						1L, 1, 0, 1,
						TransferStatus.TransferState.OVERALL_COMPLETION,
						"host", "zone");

		transferAccountingManagementService
				.updateTransferAfterOverallFailureByFileErrorThreshold(
						overallSuccess, transferAttempt);
		Assert.assertEquals(TransferStatusEnum.ERROR,
				transfer.getLastTransferStatus());

		TransferAttempt[] attemptsAfterSuccess = new TransferAttempt[transfer
				.getTransferAttempts().size()];
		transfer.getTransferAttempts().toArray(attemptsAfterSuccess);

		Assert.assertEquals(1, attemptsAfterSuccess.length);
		TransferAttempt successfulAttempt = attemptsAfterSuccess[attemptsAfterSuccess.length - 1];

		Assert.assertNotNull("should be an end date for attempt",
				successfulAttempt.getAttemptEnd());
		Assert.assertNotNull("no transfer attempt status set",
				successfulAttempt.getAttemptStatus());
		Assert.assertEquals("shold have error message",
				TransferAccountingManagementService.ERROR_SOME_FAILED_MESSAGE,
				successfulAttempt.getErrorMessage());
		Assert.assertEquals("should have a warning attempt status",
				TransferStatusEnum.ERROR, successfulAttempt.getAttemptStatus());

	}

	@Test
	public void testUpdateTransferAfterOverallCancel() throws Exception {
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
		transfer.setCreatedAt(new Timestamp(System.currentTimeMillis()));
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
				100L, 100L, 1, 0, 2, TransferState.IN_PROGRESS_COMPLETE_FILE,
				irodsAccount.getHost(), irodsAccount.getZone());

		transferAccountingManagementService
				.updateTransferAfterSuccessfulFileTransfer(status,
						transferAttempt);

		transferAccountingManagementService
				.updateTransferAfterCancellation(transferAttempt);

		Assert.assertEquals(TransferStatusEnum.OK,
				transfer.getLastTransferStatus());

		TransferAttempt[] attemptsAfterSuccess = new TransferAttempt[transfer
				.getTransferAttempts().size()];
		transfer.getTransferAttempts().toArray(attemptsAfterSuccess);

		Assert.assertEquals(1, attemptsAfterSuccess.length);
		TransferAttempt successfulAttempt = attemptsAfterSuccess[attemptsAfterSuccess.length - 1];

		Assert.assertNotNull("should be an end date for attempt",
				successfulAttempt.getAttemptEnd());
		Assert.assertNotNull("no transfer attempt status set",
				successfulAttempt.getAttemptStatus());
		Assert.assertEquals("shold have error message",
				TransferAccountingManagementService.WARNING_CANCELLED_MESSAGE,
				successfulAttempt.getErrorMessage());
		Assert.assertEquals("should have a warning attempt status",
				TransferStatusEnum.OK, successfulAttempt.getAttemptStatus());

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
		transfer.setCreatedAt(new Timestamp(System.currentTimeMillis()));
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
				100L, 100L, 1, 0, 2, TransferState.IN_PROGRESS_COMPLETE_FILE,
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
		JargonException myException;
		try {
			throw new JargonException("blah");
		} catch (JargonException je) {
			myException = je;
		}

		TransferStatus overallStatus = TransferStatus.instanceForException(
				TransferStatus.TransferType.GET, transfer
						.getIrodsAbsolutePath(), transfer
						.getLocalAbsolutePath(), transfer.getGridAccount()
						.getDefaultResource(), 0L, 0L, 0, 0, 0, myException,
				irodsAccount.getHost(), irodsAccount.getZone());

		transferAccountingManagementService.updateTransferAfterOverallFailure(
				overallStatus, attemptWith1Successful);

		// now schedule a restart...

		transferAccountingManagementService
				.prepareTransferForRestart(attemptWith1Successful.getTransfer()
						.getId());

		Assert.assertEquals("with restart, should have status enqueued",
				TransferStateEnum.ENQUEUED, transfer.getTransferState());
		Assert.assertEquals("should have reset to transfer status of OK",
				TransferStatusEnum.OK, transfer.getLastTransferStatus());

		TransferAttempt[] attemptsAfterRestart = new TransferAttempt[transfer
				.getTransferAttempts().size()];
		transfer.getTransferAttempts().toArray(attemptsAfterRestart);

		Assert.assertEquals(2, attemptsAfterRestart.length);
		TransferAttempt restartAttempt = attemptsAfterRestart[attemptsAfterRestart.length - 1];

		Assert.assertNull("should not be an end date for attempt",
				restartAttempt.getAttemptEnd());
		Assert.assertNull("should not be a start date for attempt",
				restartAttempt.getAttemptStart());
		Assert.assertNotNull("no transfer attempt status set",
				restartAttempt.getAttemptStatus());
		Assert.assertEquals("should have an OK attempt status",
				TransferStatusEnum.OK, restartAttempt.getAttemptStatus());
		Assert.assertEquals("/local/1.txt",
				restartAttempt.getLastSuccessfulPath());

		// now show the transfer as executing
		transferAccountingManagementService
				.prepareTransferForExecution(restartAttempt.getTransfer());

		// now show skipping the first file
		status = TransferStatus.instance(TransferStatus.TransferType.PUT,
				"/local/1.txt", "/path", "", 100L, 100L, 1, 0, 2,
				TransferState.RESTARTING, irodsAccount.getHost(),
				irodsAccount.getZone());

		// HERE!!!!!!
		transferAccountingManagementService
				.updateTransferAfterRestartFileSkipped(status, restartAttempt);

		// this should show up under the transfer attempt as a successful,
		// skipped file

		TransferItem[] itemsAfterRestart = new TransferItem[restartAttempt
				.getTransferItems().size()];
		restartAttempt.getTransferItems().toArray(itemsAfterRestart);

		Assert.assertEquals(1, itemsAfterRestart.length);
		TransferItem restartedItem = itemsAfterRestart[itemsAfterRestart.length - 1];
		Assert.assertNotNull("null transfer item", restartedItem);
		Assert.assertEquals("did not set source path",
				status.getSourceFileAbsolutePath(),
				restartedItem.getSourceFileAbsolutePath());
		Assert.assertEquals(status.getTargetFileAbsolutePath(),
				restartedItem.getTargetFileAbsolutePath());
		Assert.assertEquals("should not be flagged as an error", false,
				restartedItem.isError());
		Assert.assertEquals("should  be flagged as a restart", true,
				restartedItem.isSkipped());

	}

	@Test
	public void testUpdateTransferAfterFailedFileTransfer() throws Exception {
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
		transfer.setCreatedAt(new Timestamp(System.currentTimeMillis()));
		transfer.setIrodsAbsolutePath("/path");
		transfer.setLocalAbsolutePath("/local");
		transfer.setTransferType(TransferType.PUT);
		transfer.setGridAccount(gridAccount);

		TransferAttempt transferAttempt = transferAccountingManagementService
				.prepareTransferForProcessing(transfer);
		transferAttempt = transferAccountingManagementService
				.prepareTransferForExecution(transferAttempt.getTransfer());

		// cause an error now after 1 file
		JargonException myException;
		try {
			throw new JargonException("blah");
		} catch (JargonException je) {
			myException = je;
		}

		TransferStatus status = TransferStatus.instanceForException(
				TransferStatus.TransferType.GET, transfer
						.getIrodsAbsolutePath(), transfer
						.getLocalAbsolutePath(), transfer.getGridAccount()
						.getDefaultResource(), 0L, 0L, 0, 0, 0, myException,
				irodsAccount.getHost(), irodsAccount.getZone());

		transferAccountingManagementService
				.updateTransferAfterFailedFileTransfer(status, transferAttempt,
						1);

		List<TransferItem> transferItems = transferAttempt.getTransferItems();
		TransferItem failureItem = transferItems.get(0);
		Assert.assertFalse("no transfer items", transferItems.isEmpty());
		Assert.assertEquals("should have transfer type of PUT",
				TransferType.PUT, failureItem.getTransferType());
		Assert.assertEquals("wrong source path",
				status.getSourceFileAbsolutePath(),
				failureItem.getSourceFileAbsolutePath());
		Assert.assertEquals("wrong target path",
				status.getTargetFileAbsolutePath(),
				failureItem.getTargetFileAbsolutePath());
		Assert.assertEquals("did not set exception message",
				myException.getMessage(), failureItem.getErrorMessage());
		Assert.assertFalse("did not fill in stack trace", failureItem
				.getErrorStackTrace().isEmpty());
		Assert.assertTrue("should be marked as an error", failureItem.isError());

	}
}
