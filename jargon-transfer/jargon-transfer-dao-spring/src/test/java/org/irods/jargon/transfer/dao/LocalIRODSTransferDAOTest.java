package org.irods.jargon.transfer.dao;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.irods.jargon.transfer.dao.domain.TransferState;
import org.irods.jargon.transfer.dao.domain.TransferStatus;
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
		"classpath:transfer-dao-hibernate-spring.cfg.xml",
		"classpath:test-beans.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class LocalIRODSTransferDAOTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@Autowired
	private LocalIRODSTransferDAO localIRODSTransferDAO;

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

	@Test
	public void testSave() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		GridAccount gridAccount = DomainUtils
				.gridAccountFromIRODSAccount(irodsAccount);
		gridAccountDAO.save(gridAccount);
		LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath("/tmp");
		enqueuedTransfer.setLocalAbsolutePath("/tmp");
		enqueuedTransfer.setGridAccount(gridAccount);
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferState(TransferState.PROCESSING);
		enqueuedTransfer.setTransferStatus(TransferStatus.ERROR);
		assertTrue(enqueuedTransfer.getId() == null);
		localIRODSTransferDAO.save(enqueuedTransfer);
		assertTrue(enqueuedTransfer.getId() != null);

	}

	@Test
	public void testPurgeQueue() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		GridAccount gridAccount = DomainUtils
				.gridAccountFromIRODSAccount(irodsAccount);
		gridAccountDAO.save(gridAccount);
		LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath("/tmp");
		enqueuedTransfer.setLocalAbsolutePath("/tmp");
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setGridAccount(gridAccount);
		enqueuedTransfer.setTransferState(TransferState.COMPLETE);
		enqueuedTransfer.setTransferStatus(TransferStatus.OK);

		assertTrue(enqueuedTransfer.getId() == null);

		localIRODSTransferDAO.save(enqueuedTransfer);

		assertTrue(enqueuedTransfer.getId() != null);

		localIRODSTransferDAO.purgeQueue();
		assertTrue(localIRODSTransferDAO.findByTransferState(
				TransferState.COMPLETE).size() == 0);

	}

	/**
	 * @param localIRODSTransferDAO
	 *            the localIRODSTransferDAO to set
	 */
	public void setLocalIRODSTransferDAO(
			final LocalIRODSTransferDAO localIRODSTransferDAO) {
		this.localIRODSTransferDAO = localIRODSTransferDAO;
	}

}
