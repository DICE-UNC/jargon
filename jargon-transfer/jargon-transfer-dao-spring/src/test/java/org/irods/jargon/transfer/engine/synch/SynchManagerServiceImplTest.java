package org.irods.jargon.transfer.engine.synch;

import java.util.Date;

import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.irods.jargon.transfer.dao.domain.SynchronizationType;
import org.irods.jargon.transfer.dao.domain.TransferStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:transfer-dao-beans.xml",
        "classpath:transfer-dao-hibernate-spring.cfg.xml", "classpath:test-beans.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class SynchManagerServiceImplTest {

    @Autowired
    private SynchManagerService synchManagerService;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testCreateNewSynchConfiguration() throws Exception {
        Synchronization synchConfiguration = new Synchronization();
        synchConfiguration.setCreatedAt(new Date());
        synchConfiguration.setDefaultResourceName("test");
        synchConfiguration.setIrodsHostName("host");
        synchConfiguration.setIrodsPassword("xxx");
        synchConfiguration.setIrodsPort(1247);
        synchConfiguration.setIrodsSynchDirectory("/synchdir");
        synchConfiguration.setIrodsUserName("userName");
        synchConfiguration.setIrodsZone("zone");
        synchConfiguration.setLastSynchronizationStatus(TransferStatus.OK);
        synchConfiguration.setLocalSynchDirectory("/localdir");
        synchConfiguration.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
        synchManagerService.createNewSynchConfiguration(synchConfiguration);
    }

    @Autowired
    public void setSynchManagerService(final SynchManagerService synchManagerService) {
        this.synchManagerService = synchManagerService;
    }

    public SynchManagerService getSynchManagerService() {
        return synchManagerService;
    }

}
