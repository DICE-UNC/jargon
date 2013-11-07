package org.irods.jargon.transfer.dao;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.transfer.dao.domain.FrequencyType;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.irods.jargon.transfer.dao.domain.SynchronizationType;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferStateEnum;
import org.irods.jargon.transfer.dao.domain.TransferStatusEnum;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.irods.jargon.transfer.util.DomainUtils;
import org.junit.AfterClass;
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
public class SynchronizationDAOTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@Autowired
	private SynchronizationDAO synchronizationDAO;

	@Autowired
	private TransferDAO transferDAO;

	@Autowired
	private GridAccountDAO gridAccountDAO;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	public TransferDAO getLocalIrodsTransferDAO() {
		return transferDAO;
	}

	public void setLocalIrodsTransferDAO(final TransferDAO transferDAO) {
		this.transferDAO = transferDAO;
	}

	public void setSynchronizationDAO(
			final SynchronizationDAO synchronizationDAO) {
		this.synchronizationDAO = synchronizationDAO;
	}

	public void setGridAccountDAO(final GridAccountDAO gridAccountDAO) {
		this.gridAccountDAO = gridAccountDAO;
	}

	@Test
	public void testSave() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		GridAccount gridAccount = DomainUtils
				.gridAccountFromIRODSAccount(irodsAccount);

		gridAccountDAO.save(gridAccount);
		Synchronization synchronization = new Synchronization();
		synchronization.setCreatedAt(new Date());
		synchronization.setFrequencyType(FrequencyType.EVERY_HOUR);
		synchronization.setGridAccount(gridAccount);
		synchronization.setIrodsSynchDirectory("irods/dir");
		synchronization.setLocalSynchDirectory("local/synch");
		synchronization.setName("test");
		synchronization
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchronizationDAO.save(synchronization);
		Assert.assertTrue("did not set id", synchronization.getId() > 0);

	}

	@Test
	public void testFindById() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		GridAccount gridAccount = DomainUtils
				.gridAccountFromIRODSAccount(irodsAccount);
		gridAccountDAO.save(gridAccount);
		Synchronization synchronization = new Synchronization();
		synchronization.setCreatedAt(new Date());
		synchronization.setGridAccount(gridAccount);
		synchronization.setFrequencyType(FrequencyType.EVERY_HOUR);
		synchronization.setIrodsSynchDirectory("irods/dir");
		synchronization.setLocalSynchDirectory("local/synch");
		synchronization.setName("test");
		synchronization
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchronizationDAO.save(synchronization);

		Synchronization actual = synchronizationDAO.findById(synchronization
				.getId());
		Assert.assertNotNull("did not find synch by id", actual);

	}

	@Test
	public void testFindByName() throws Exception {
		String testName = "testFindByName";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		GridAccount gridAccount = DomainUtils
				.gridAccountFromIRODSAccount(irodsAccount);
		gridAccountDAO.save(gridAccount);
		Synchronization synchronization = new Synchronization();
		synchronization.setCreatedAt(new Date());
		synchronization.setGridAccount(gridAccount);
		synchronization.setFrequencyType(FrequencyType.EVERY_HOUR);
		synchronization.setIrodsSynchDirectory("irods/dir");
		synchronization.setLocalSynchDirectory("local/synch");
		synchronization.setName(testName);
		synchronization
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchronizationDAO.save(synchronization);

		Synchronization actual = synchronizationDAO.findByName(testName);
		Assert.assertNotNull("did not find synch by name", actual);
	}

	@Test
	public void testFindAll() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		GridAccount gridAccount = DomainUtils
				.gridAccountFromIRODSAccount(irodsAccount);
		gridAccountDAO.save(gridAccount);
		Synchronization synchronization = new Synchronization();
		synchronization.setCreatedAt(new Date());
		synchronization.setGridAccount(gridAccount);
		synchronization.setFrequencyType(FrequencyType.EVERY_HOUR);
		synchronization.setIrodsSynchDirectory("irods/dir");
		synchronization.setLocalSynchDirectory("local/synch");
		synchronization.setName("test");
		synchronization
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchronizationDAO.save(synchronization);

		synchronization = new Synchronization();
		synchronization.setCreatedAt(new Date());
		synchronization.setGridAccount(gridAccount);
		synchronization.setFrequencyType(FrequencyType.EVERY_HOUR);
		synchronization.setIrodsSynchDirectory("irods/dirs");
		synchronization.setLocalSynchDirectory("local/sync2");
		synchronization.setName("test2");
		synchronization
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchronizationDAO.save(synchronization);

		List<Synchronization> synchronizations = synchronizationDAO.findAll();
		Assert.assertTrue("did not find expected synchs",
				synchronizations.size() > 0);

	}

	@Test
	public void testDelete() throws Exception {
		String testName = "testDelete";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		GridAccount gridAccount = DomainUtils
				.gridAccountFromIRODSAccount(irodsAccount);
		gridAccountDAO.save(gridAccount);
		Synchronization synchronization = new Synchronization();
		synchronization.setCreatedAt(new Date());
		synchronization.setGridAccount(gridAccount);
		synchronization.setFrequencyType(FrequencyType.EVERY_HOUR);
		synchronization.setIrodsSynchDirectory("irods/dir");
		synchronization.setLocalSynchDirectory("local/synch");
		synchronization.setName(testName);
		synchronization
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchronizationDAO.save(synchronization);

		Synchronization actual = synchronizationDAO.findByName(testName);
		synchronizationDAO.delete(actual);

		Synchronization lookUpAgain = synchronizationDAO.findByName(testName);
		Assert.assertNull("did not delete synch", lookUpAgain);

	}

	@Test
	public void testSaveWithLocalIRODSTransfer() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		GridAccount gridAccount = DomainUtils
				.gridAccountFromIRODSAccount(irodsAccount);
		gridAccountDAO.save(gridAccount);
		Synchronization synchronization = new Synchronization();
		synchronization.setCreatedAt(new Date());
		synchronization.setGridAccount(gridAccount);
		synchronization.setFrequencyType(FrequencyType.EVERY_HOUR);
		synchronization.setIrodsSynchDirectory("irods/dir");
		synchronization.setLocalSynchDirectory("local/synch");
		synchronization.setName("testSaveWithLocalIRODSTransfer");
		synchronization
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchronizationDAO.save(synchronization);

		Transfer transfer = new Transfer();
		transfer.setCreatedAt(new Timestamp(System.currentTimeMillis()));
		transfer.setIrodsAbsolutePath("/irods/path");
		transfer.setLocalAbsolutePath("/local/path");
		transfer.setSynchronization(synchronization);
		transfer.setGridAccount(gridAccount);
		transfer.setTransferState(TransferStateEnum.ENQUEUED);
		transfer.setLastTransferStatus(TransferStatusEnum.OK);
		transfer.setTransferType(TransferType.SYNCH);
		synchronization.getTransfers().add(transfer);

		Assert.assertTrue("did not set id", synchronization.getId() > 0);

		Synchronization actual = synchronizationDAO.findById(synchronization
				.getId());
		Assert.assertNotNull("did not find actual synch", actual);
		Assert.assertTrue("did not find localIRODSTransfer in synchronization",
				synchronization.getTransfers().size() > 0);

	}

	@Test
	public void testSaveWithLocalIRODSTransferThenFindAllTransfers()
			throws Exception {

		String testName = "testSaveWithLocalIRODSTransferThenFindAllTransfers";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		GridAccount gridAccount = DomainUtils
				.gridAccountFromIRODSAccount(irodsAccount);
		gridAccountDAO.save(gridAccount);
		Synchronization synchronization = new Synchronization();
		synchronization.setCreatedAt(new Date());
		synchronization.setGridAccount(gridAccount);
		synchronization.setFrequencyType(FrequencyType.EVERY_HOUR);
		synchronization.setIrodsSynchDirectory(testName);
		synchronization.setLocalSynchDirectory("local/synch");
		synchronization.setName(testName);
		synchronization
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchronizationDAO.save(synchronization);

		Transfer transfer = new Transfer();
		transfer.setCreatedAt(new Timestamp(System.currentTimeMillis()));
		transfer.setIrodsAbsolutePath(testName);
		transfer.setLocalAbsolutePath("/local/path");
		transfer.setSynchronization(synchronization);
		transfer.setGridAccount(gridAccount);
		transfer.setTransferState(TransferStateEnum.ENQUEUED);
		transfer.setLastTransferStatus(TransferStatusEnum.OK);
		transfer.setTransferType(TransferType.SYNCH);
		synchronization.getTransfers().add(transfer);

		List<Transfer> allTransfers = transferDAO.findAll();

		boolean foundTransfer = false;
		for (Transfer actualTransfer : allTransfers) {
			if (actualTransfer.getIrodsAbsolutePath().equals(testName)) {
				foundTransfer = true;
				Assert.assertNotNull("transfer did not have synch",
						actualTransfer.getSynchronization());
				Assert.assertEquals("synch did not have proper data", testName,
						actualTransfer.getSynchronization().getName());
			}
		}

		Assert.assertTrue("did not find synch", foundTransfer);

	}

}
