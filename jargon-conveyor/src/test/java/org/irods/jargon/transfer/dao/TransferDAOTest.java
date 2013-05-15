package org.irods.jargon.transfer.dao;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferState;
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
public class TransferDAOTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

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

	@Test
	public void testSave() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		GridAccount gridAccount = DomainUtils
				.gridAccountFromIRODSAccount(irodsAccount);
		gridAccountDAO.save(gridAccount);
		Transfer enqueuedTransfer = new Transfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath("/tmp");
		enqueuedTransfer.setLocalAbsolutePath("/tmp");
		enqueuedTransfer.setGridAccount(gridAccount);
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferState(TransferState.PROCESSING);
		enqueuedTransfer.setLastTransferStatus(TransferStatusEnum.ERROR);
		assertTrue(enqueuedTransfer.getId() == null);
		transferDAO.save(enqueuedTransfer);
		assertTrue(enqueuedTransfer.getId() != null);

	}

	/**
	 * @param localIRODSTransferDAO
	 *            the localIRODSTransferDAO to set
	 */
	public void setLocalIRODSTransferDAO(final TransferDAO transferDAO) {
		this.transferDAO = transferDAO;
	}

}
