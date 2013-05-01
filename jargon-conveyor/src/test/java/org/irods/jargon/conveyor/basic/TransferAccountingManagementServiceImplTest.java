package org.irods.jargon.conveyor.basic;

import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.GridAccountService;
import org.irods.jargon.conveyor.core.TransferAccountingManagementService;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.Transfer;
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
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPrepareTransferForExecution() throws Exception {
            String testUserName = "user1";
            IRODSAccount irodsAccount = testingPropertiesHelper
                            .buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
                                            testingProperties, testUserName, testUserName);
            String passPhrase = "ooogabooga";
            gridAccountService.validatePassPhrase(passPhrase);
            GridAccount gridAccount = gridAccountService
                            .addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);
            
            Transfer transfer = new Transfer();
            transfer.setCreatedAt(new Date());
            transfer.setIrodsAbsolutePath("/path");
            transfer.setLocalAbsolutePath("local");
            transfer.setTransferType(TransferType.PUT);
            transfer.setGridAccount(gridAccount);

            transferAccountingManagementService.prepareTransferForExecution(transfer);

	}

}
