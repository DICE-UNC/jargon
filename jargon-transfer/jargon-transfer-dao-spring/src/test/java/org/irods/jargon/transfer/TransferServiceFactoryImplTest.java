package org.irods.jargon.transfer;

import junit.framework.Assert;

import org.irods.jargon.transfer.engine.ConfigurationService;
import org.irods.jargon.transfer.engine.TransferQueueService;
import org.irods.jargon.transfer.engine.synch.SynchManagerService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:transfer-dao-beans.xml",
		"classpath:transfer-dao-hibernate-spring.cfg.xml",
		"classpath:test-beans.xml" })
public class TransferServiceFactoryImplTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testInstanceTransferQueueService() throws Exception {
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();
		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();
		Assert.assertNotNull("null transfer queue service returned",
				transferQueueService);

	}

	@Test
	public void testInstanceSynchManagerService() {
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();
		SynchManagerService synchManagerService = transferServiceFactory
				.instanceSynchManagerService();
		Assert.assertNotNull("null synchManagerServcie returned",
				synchManagerService);
	}

	@Test
	public void testInstanceConfigurationService() {
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();
		ConfigurationService configurationService = transferServiceFactory
				.instanceConfigurationService();
		Assert.assertNotNull("null configurationService returned",
				configurationService);
	}

}
