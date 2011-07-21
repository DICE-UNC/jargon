package org.irods.jargon.transfer.dao;

import java.util.Date;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.irods.jargon.transfer.dao.domain.FrequencyType;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.irods.jargon.transfer.dao.domain.SynchronizationType;
import org.irods.jargon.transfer.dao.domain.TransferState;
import org.irods.jargon.transfer.dao.domain.TransferStatus;
import org.irods.jargon.transfer.dao.domain.TransferType;
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
public class SynchronizationDAOTest {

	@Autowired
	private SynchronizationDAO synchronizationDAO;
	
	@Autowired
	private LocalIRODSTransferDAO localIrodsTransferDAO;


	public LocalIRODSTransferDAO getLocalIrodsTransferDAO() {
		return localIrodsTransferDAO;
	}

	public void setLocalIrodsTransferDAO(LocalIRODSTransferDAO localIrodsTransferDAO) {
		this.localIrodsTransferDAO = localIrodsTransferDAO;
	}

	public void setSynchronizationDAO(
			final SynchronizationDAO synchronizationDAO) {
		this.synchronizationDAO = synchronizationDAO;
	}

	@Test
	public void testSave() throws Exception {
		Synchronization synchronization = new Synchronization();
		synchronization.setCreatedAt(new Date());
		synchronization.setDefaultResourceName("test");
		synchronization.setFrequencyType(FrequencyType.EVERY_HOUR);
		synchronization.setIrodsHostName("host");
		synchronization.setIrodsPassword("password");
		synchronization.setIrodsPort(1247);
		synchronization.setIrodsSynchDirectory("irods/dir");
		synchronization.setIrodsUserName("user");
		synchronization.setIrodsZone("zone");
		synchronization.setLocalSynchDirectory("local/synch");
		synchronization.setName("test");
		synchronization
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchronizationDAO.save(synchronization);
		Assert.assertTrue("did not set id", synchronization.getId() > 0);

	}

	@Test
	public void testFindById() throws Exception {
		Synchronization synchronization = new Synchronization();
		synchronization.setCreatedAt(new Date());
		synchronization.setDefaultResourceName("test");
		synchronization.setFrequencyType(FrequencyType.EVERY_HOUR);
		synchronization.setIrodsHostName("host");
		synchronization.setIrodsPassword("password");
		synchronization.setIrodsPort(1247);
		synchronization.setIrodsSynchDirectory("irods/dir");
		synchronization.setIrodsUserName("user");
		synchronization.setIrodsZone("zone");
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
		Synchronization synchronization = new Synchronization();
		synchronization.setCreatedAt(new Date());
		synchronization.setDefaultResourceName("test");
		synchronization.setFrequencyType(FrequencyType.EVERY_HOUR);
		synchronization.setIrodsHostName("host");
		synchronization.setIrodsPassword("password");
		synchronization.setIrodsPort(1247);
		synchronization.setIrodsSynchDirectory("irods/dir");
		synchronization.setIrodsUserName("user");
		synchronization.setIrodsZone("zone");
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
		Synchronization synchronization = new Synchronization();
		synchronization.setCreatedAt(new Date());
		synchronization.setDefaultResourceName("test");
		synchronization.setFrequencyType(FrequencyType.EVERY_HOUR);
		synchronization.setIrodsHostName("host");
		synchronization.setIrodsPassword("password");
		synchronization.setIrodsPort(1247);
		synchronization.setIrodsSynchDirectory("irods/dir");
		synchronization.setIrodsUserName("user");
		synchronization.setIrodsZone("zone");
		synchronization.setLocalSynchDirectory("local/synch");
		synchronization.setName("test");
		synchronization
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchronizationDAO.save(synchronization);

		synchronization = new Synchronization();
		synchronization.setCreatedAt(new Date());
		synchronization.setDefaultResourceName("test");
		synchronization.setFrequencyType(FrequencyType.EVERY_HOUR);
		synchronization.setIrodsHostName("host");
		synchronization.setIrodsPassword("password");
		synchronization.setIrodsPort(1247);
		synchronization.setIrodsSynchDirectory("irods/dirs");
		synchronization.setIrodsUserName("user");
		synchronization.setIrodsZone("zone");
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
		Synchronization synchronization = new Synchronization();
		synchronization.setCreatedAt(new Date());
		synchronization.setDefaultResourceName("test");
		synchronization.setFrequencyType(FrequencyType.EVERY_HOUR);
		synchronization.setIrodsHostName("host");
		synchronization.setIrodsPassword("password");
		synchronization.setIrodsPort(1247);
		synchronization.setIrodsSynchDirectory("irods/dir");
		synchronization.setIrodsUserName("user");
		synchronization.setIrodsZone("zone");
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
		Synchronization synchronization = new Synchronization();
		synchronization.setCreatedAt(new Date());
		synchronization.setDefaultResourceName("test");
		synchronization.setFrequencyType(FrequencyType.EVERY_HOUR);
		synchronization.setIrodsHostName("host");
		synchronization.setIrodsPassword("password");
		synchronization.setIrodsPort(1247);
		synchronization.setIrodsSynchDirectory("irods/dir");
		synchronization.setIrodsUserName("user");
		synchronization.setIrodsZone("zone");
		synchronization.setLocalSynchDirectory("local/synch");
		synchronization.setName("testSaveWithLocalIRODSTransfer");
		synchronization
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchronizationDAO.save(synchronization);
		
		LocalIRODSTransfer localIRODSTransfer = new LocalIRODSTransfer();
		localIRODSTransfer.setCreatedAt(new Date());
		localIRODSTransfer.setIrodsAbsolutePath("/irods/path");
		localIRODSTransfer.setLocalAbsolutePath("/local/path");
		localIRODSTransfer.setSynchronization(synchronization);
		localIRODSTransfer.setTransferHost("host");
		localIRODSTransfer.setTransferPort(1247);
		localIRODSTransfer.setTransferPassword("password");
		localIRODSTransfer.setTransferResource("xxx");
		localIRODSTransfer.setTransferState(TransferState.ENQUEUED);
		localIRODSTransfer.setTransferStatus(TransferStatus.OK);
		localIRODSTransfer.setTransferType(TransferType.SYNCH);
		localIRODSTransfer.setTransferUserName("user");
		localIRODSTransfer.setTransferZone("zone");
		synchronization.getLocalIRODSTransfers().add(localIRODSTransfer);
		
		
		Assert.assertTrue("did not set id", synchronization.getId() > 0);

		Synchronization actual = synchronizationDAO.findById(synchronization.getId());
		TestCase.assertNotNull("did not find actual synch", actual);
		TestCase.assertTrue("did not find localIRODSTransfer in synchronization", synchronization.getLocalIRODSTransfers().size() > 0);
		
	}
	
	@Test
	public void testSaveWithLocalIRODSTransferThenFindAllTransfers() throws Exception {
		
		String testName = "testSaveWithLocalIRODSTransferThenFindAllTransfers";
		Synchronization synchronization = new Synchronization();
		synchronization.setCreatedAt(new Date());
		synchronization.setDefaultResourceName("test");
		synchronization.setFrequencyType(FrequencyType.EVERY_HOUR);
		synchronization.setIrodsHostName("host");
		synchronization.setIrodsPassword("password");
		synchronization.setIrodsPort(1247);
		synchronization.setIrodsSynchDirectory(testName);
		synchronization.setIrodsUserName("user");
		synchronization.setIrodsZone("zone");
		synchronization.setLocalSynchDirectory("local/synch");
		synchronization.setName(testName);
		synchronization
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchronizationDAO.save(synchronization);
		
		LocalIRODSTransfer localIRODSTransfer = new LocalIRODSTransfer();
		localIRODSTransfer.setCreatedAt(new Date());
		localIRODSTransfer.setIrodsAbsolutePath(testName);
		localIRODSTransfer.setLocalAbsolutePath("/local/path");
		localIRODSTransfer.setSynchronization(synchronization);
		localIRODSTransfer.setTransferHost("host");
		localIRODSTransfer.setTransferPort(1247);
		localIRODSTransfer.setTransferPassword("password");
		localIRODSTransfer.setTransferResource("xxx");
		localIRODSTransfer.setTransferState(TransferState.ENQUEUED);
		localIRODSTransfer.setTransferStatus(TransferStatus.OK);
		localIRODSTransfer.setTransferType(TransferType.SYNCH);
		localIRODSTransfer.setTransferUserName("user");
		localIRODSTransfer.setTransferZone("zone");
		synchronization.getLocalIRODSTransfers().add(localIRODSTransfer);
		
		List<LocalIRODSTransfer> allTransfers = localIrodsTransferDAO.findAll();
		
		boolean foundTransfer = false;
		for (LocalIRODSTransfer actualTransfer : allTransfers) {
			if (actualTransfer.getIrodsAbsolutePath().equals(testName)) {
				foundTransfer = true;
				TestCase.assertNotNull("transfer did not have synch", actualTransfer.getSynchronization());
				TestCase.assertEquals("synch did not have proper data", testName, actualTransfer.getSynchronization().getName());
			} 
		}
		
		TestCase.assertTrue("did not find synch", foundTransfer);
		
		
	}


}
