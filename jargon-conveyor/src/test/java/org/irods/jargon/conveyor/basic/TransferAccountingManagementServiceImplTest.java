package org.irods.jargon.conveyor.basic;

import java.util.Date;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.conveyor.core.GridAccountService;
import org.irods.jargon.conveyor.core.TransferAccountingManagementService;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.transfer.TransferStatus.TransferState;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferStatus;
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
	private GridAccountService gridAccountService;

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
		Assert.assertEquals(TransferStatus.OK, transfer.getLastTransferStatus());
		Assert.assertFalse("no id set", transferAttempt.getId() == 0);
		Assert.assertNotNull("no transfer parent in attempt",
				transferAttempt.getTransfer());

		Assert.assertNotNull("no start set for attempt",
				transferAttempt.getAttemptStart());
		Assert.assertNull("should not be an end date for attempt",
				transferAttempt.getAttemptEnd());
		Assert.assertNotNull("no transfer attempt status set",
				transferAttempt.getAttemptStatus());
		Assert.assertEquals("should have ok for status in attempt",
				TransferStatus.OK, transferAttempt.getAttemptStatus());
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
				.prepareTransferForExecution(transfer);

		Exception myException;

		try {
			throw new JargonException("blah");
		} catch (JargonException je) {
			myException = je;
		}

		transferAccountingManagementService
				.updateTransferAttemptWithConveyorException(transferAttempt,
						myException);

		Assert.assertNotNull("null transfer attempt", transferAttempt);
		Assert.assertEquals(TransferStatus.ERROR,
				transfer.getLastTransferStatus());

		Assert.assertNotNull("should be an end date for attempt",
				transferAttempt.getAttemptEnd());
		Assert.assertNotNull("no transfer attempt status set",
				transferAttempt.getAttemptStatus());
		Assert.assertEquals("should have an error attempt status",
				TransferStatus.ERROR, transferAttempt.getAttemptStatus());
		Assert.assertEquals(
				"should have  error message",
				TransferAccountingManagementServiceImpl.ERROR_ATTEMPTING_TO_RUN,
				transferAttempt.getErrorMessage());
		Assert.assertEquals("should have global exception message",
				myException.getMessage(), transferAttempt.getGlobalException());
		Assert.assertNotNull("should have stack trace info",
				transferAttempt.getGlobalExceptionStackTrace());
		Assert.assertFalse("empty stack trace", transferAttempt
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
				.prepareTransferForExecution(transfer);

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
				overallStatus, transferAttempt);

		Assert.assertNotNull("null transfer attempt", transferAttempt);
		Assert.assertEquals(TransferStatus.ERROR,
				transfer.getLastTransferStatus());

		Assert.assertNotNull("should be an end date for attempt",
				transferAttempt.getAttemptEnd());
		Assert.assertNotNull("no transfer attempt status set",
				transferAttempt.getAttemptStatus());
		Assert.assertEquals("should have an error attempt status",
				TransferStatus.ERROR, transferAttempt.getAttemptStatus());

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

		Transfer transfer = new Transfer();
		transfer.setCreatedAt(new Date());
		transfer.setIrodsAbsolutePath("/path");
		transfer.setLocalAbsolutePath("local");
		transfer.setTransferType(TransferType.PUT);
		transfer.setGridAccount(gridAccount);

		TransferAttempt transferAttempt = transferAccountingManagementService
				.prepareTransferForExecution(transfer);

	}

}
