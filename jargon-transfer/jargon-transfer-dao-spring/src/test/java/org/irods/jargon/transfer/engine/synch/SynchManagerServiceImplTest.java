package org.irods.jargon.transfer.engine.synch;

import java.util.Date;
import java.util.List;

import junit.framework.Assert;

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
		"classpath:transfer-dao-hibernate-spring.cfg.xml",
		"classpath:test-beans.xml" })
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
		synchConfiguration
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchConfiguration.setName("testCreateNewSynchConfiguration");
		synchManagerService.createNewSynchConfiguration(synchConfiguration);
	}

	@Test(expected = ConflictingSynchException.class)
	public void testCreateNewSynchConfigurationDuplicateName() throws Exception {
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
		synchConfiguration
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchConfiguration.setName("testCreateNewSynchConfiguration");
		synchManagerService.createNewSynchConfiguration(synchConfiguration);
		synchConfiguration = new Synchronization();
		synchConfiguration.setCreatedAt(new Date());
		synchConfiguration.setDefaultResourceName("test");
		synchConfiguration.setIrodsHostName("host");
		synchConfiguration.setIrodsPassword("xxx");
		synchConfiguration.setIrodsPort(1247);
		synchConfiguration.setIrodsSynchDirectory("/synchdir2");
		synchConfiguration.setIrodsUserName("userName");
		synchConfiguration.setIrodsZone("zone");
		synchConfiguration.setLastSynchronizationStatus(TransferStatus.OK);
		synchConfiguration.setLocalSynchDirectory("/localdir2");
		synchConfiguration
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchConfiguration.setName("testCreateNewSynchConfiguration");
		synchManagerService.createNewSynchConfiguration(synchConfiguration);
	}

	@Test(expected = ConflictingSynchException.class)
	public void testCreateNewSynchConfigurationDuplicateLocal()
			throws Exception {
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
		synchConfiguration
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchConfiguration
				.setName("testCreateNewSynchConfigurationDuplicateLocal");
		synchManagerService.createNewSynchConfiguration(synchConfiguration);
		synchConfiguration = new Synchronization();
		synchConfiguration.setCreatedAt(new Date());
		synchConfiguration.setDefaultResourceName("test");
		synchConfiguration.setIrodsHostName("host");
		synchConfiguration.setIrodsPassword("xxx");
		synchConfiguration.setIrodsPort(1247);
		synchConfiguration.setIrodsSynchDirectory("/synchdir2");
		synchConfiguration.setIrodsUserName("userName");
		synchConfiguration.setIrodsZone("zone");
		synchConfiguration.setLastSynchronizationStatus(TransferStatus.OK);
		synchConfiguration.setLocalSynchDirectory("/localdir");
		synchConfiguration
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchConfiguration
				.setName("testCreateNewSynchConfigurationDuplicateLocal2");
		synchManagerService.createNewSynchConfiguration(synchConfiguration);
	}

	@Test(expected = ConflictingSynchException.class)
	public void testCreateNewSynchConfigurationDuplicateIrods()
			throws Exception {
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
		synchConfiguration
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchConfiguration
				.setName("testCreateNewSynchConfigurationDuplicateIrods");
		synchManagerService.createNewSynchConfiguration(synchConfiguration);
		synchConfiguration = new Synchronization();
		synchConfiguration.setCreatedAt(new Date());
		synchConfiguration.setDefaultResourceName("test");
		synchConfiguration.setIrodsHostName("host");
		synchConfiguration.setIrodsPassword("xxx");
		synchConfiguration.setIrodsPort(1247);
		synchConfiguration.setIrodsSynchDirectory("/synchdir");
		synchConfiguration.setIrodsUserName("userName");
		synchConfiguration.setIrodsZone("zone");
		synchConfiguration.setLastSynchronizationStatus(TransferStatus.OK);
		synchConfiguration.setLocalSynchDirectory("/localdir2");
		synchConfiguration
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchConfiguration
				.setName("testCreateNewSynchConfigurationDuplicateIrods2");
		synchManagerService.createNewSynchConfiguration(synchConfiguration);
	}

	@Test
	public void testCreateNewSynchConfigurationDuplicateIrodsDiffZone()
			throws Exception {
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
		synchConfiguration
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchConfiguration
				.setName("testCreateNewSynchConfigurationDuplicateIrodsDiffZone");
		synchManagerService.createNewSynchConfiguration(synchConfiguration);
		synchConfiguration = new Synchronization();
		synchConfiguration.setCreatedAt(new Date());
		synchConfiguration.setDefaultResourceName("test");
		synchConfiguration.setIrodsHostName("host");
		synchConfiguration.setIrodsPassword("xxx");
		synchConfiguration.setIrodsPort(1247);
		synchConfiguration.setIrodsSynchDirectory("/synchdir");
		synchConfiguration.setIrodsUserName("userName");
		synchConfiguration.setIrodsZone("zone2");
		synchConfiguration.setLastSynchronizationStatus(TransferStatus.OK);
		synchConfiguration.setLocalSynchDirectory("/localdir2");
		synchConfiguration
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchConfiguration
				.setName("testCreateNewSynchConfigurationDuplicateIrodsDiffZone2");
		synchManagerService.createNewSynchConfiguration(synchConfiguration);
		Assert.assertTrue(true);
	}

	@Test
	public void testListAllSynchConfiguration() throws Exception {
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
		synchConfiguration
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchConfiguration.setName("testCreateNewSynchConfiguration");
		synchManagerService.createNewSynchConfiguration(synchConfiguration);
		List<Synchronization> allSynchs = synchManagerService
				.listAllSynchronizations();
		Assert.assertTrue("did not list synchs", allSynchs.size() > 0);

	}

	@Test
	public void testFindById() throws Exception {
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
		synchConfiguration
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchConfiguration.setName("testFindById");
		synchManagerService.createNewSynchConfiguration(synchConfiguration);

		// now find
		Synchronization actual = synchManagerService
				.findById(synchConfiguration.getId());
		Assert.assertNotNull("did not find synch I just added", actual);

	}

	@Autowired
	public void setSynchManagerService(
			final SynchManagerService synchManagerService) {
		this.synchManagerService = synchManagerService;
	}

	public SynchManagerService getSynchManagerService() {
		return synchManagerService;
	}

}
