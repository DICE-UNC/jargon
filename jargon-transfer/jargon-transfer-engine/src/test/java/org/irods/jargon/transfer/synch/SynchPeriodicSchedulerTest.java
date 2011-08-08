package org.irods.jargon.transfer.synch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.transfer.TransferServiceFactoryImpl;
import org.irods.jargon.transfer.dao.domain.FrequencyType;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.irods.jargon.transfer.dao.domain.TransferState;
import org.irods.jargon.transfer.engine.TransferManager;
import org.irods.jargon.transfer.engine.synch.SynchManagerService;
import org.irods.jargon.transfer.util.HibernateUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class SynchPeriodicSchedulerTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testConstructor() throws Exception {
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		new SynchPeriodicScheduler(transferManager, irodsAccessObjectFactory);
		Assert.assertTrue(true);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testConstructorNullTransferManager() throws Exception {
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		TransferManager transferManager = null;
		new SynchPeriodicScheduler(transferManager, irodsAccessObjectFactory);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testConstructorNullAccessObjectFactory() throws Exception {
		IRODSAccessObjectFactory irodsAccessObjectFactory = null;
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		new SynchPeriodicScheduler(transferManager, irodsAccessObjectFactory);
	}

	@Test
	public final void testShouldScheduleBasedOnHourly() throws Exception {
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		SynchPeriodicScheduler scheduler = new SynchPeriodicScheduler(
				transferManager, irodsAccessObjectFactory);
		Synchronization synchronization = new Synchronization();
		synchronization.setFrequencyType(FrequencyType.EVERY_HOUR);
		Calendar targetDate = Calendar.getInstance();
		targetDate.add(Calendar.HOUR, -2);
		synchronization.setLastSynchronized(targetDate.getTime());
		boolean shouldSchedule = scheduler
				.computeShouldSynchBasedOnCurrentDateAndSynchProperties(
						synchronization, Calendar.getInstance());
		Assert.assertTrue("should have scheduled", shouldSchedule);

	}

	@Test
	public final void testShouldNotScheduleBasedOnHourly() throws Exception {
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		SynchPeriodicScheduler scheduler = new SynchPeriodicScheduler(
				transferManager, irodsAccessObjectFactory);
		Synchronization synchronization = new Synchronization();
		synchronization.setFrequencyType(FrequencyType.EVERY_HOUR);
		Calendar targetDate = Calendar.getInstance();
		targetDate.add(Calendar.MINUTE, -45);
		synchronization.setLastSynchronized(targetDate.getTime());
		boolean shouldSchedule = scheduler
				.computeShouldSynchBasedOnCurrentDateAndSynchProperties(
						synchronization, Calendar.getInstance());
		Assert.assertFalse("should not have scheduled", shouldSchedule);

	}

	@Test
	public final void testShouldScheduleBasedOnDaily() throws Exception {
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		SynchPeriodicScheduler scheduler = new SynchPeriodicScheduler(
				transferManager, irodsAccessObjectFactory);
		Synchronization synchronization = new Synchronization();
		synchronization.setFrequencyType(FrequencyType.EVERY_DAY);
		Calendar targetDate = Calendar.getInstance();
		targetDate.add(Calendar.DAY_OF_WEEK, -8);
		synchronization.setLastSynchronized(targetDate.getTime());
		boolean shouldSchedule = scheduler
				.computeShouldSynchBasedOnCurrentDateAndSynchProperties(
						synchronization, Calendar.getInstance());
		Assert.assertTrue("should have scheduled", shouldSchedule);

	}

	@Test
	public final void testShouldNotScheduleBasedOnDaily() throws Exception {
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		SynchPeriodicScheduler scheduler = new SynchPeriodicScheduler(
				transferManager, irodsAccessObjectFactory);
		Synchronization synchronization = new Synchronization();
		synchronization.setFrequencyType(FrequencyType.EVERY_DAY);
		Calendar targetDate = Calendar.getInstance();
		targetDate.add(Calendar.HOUR, -2);
		synchronization.setLastSynchronized(targetDate.getTime());
		boolean shouldSchedule = scheduler
				.computeShouldSynchBasedOnCurrentDateAndSynchProperties(
						synchronization, Calendar.getInstance());
		Assert.assertFalse("should not have scheduled", shouldSchedule);

	}

	@Test
	public final void testShouldScheduleBasedOnWeekly() throws Exception {
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		SynchPeriodicScheduler scheduler = new SynchPeriodicScheduler(
				transferManager, irodsAccessObjectFactory);
		Synchronization synchronization = new Synchronization();
		synchronization.setFrequencyType(FrequencyType.EVERY_WEEK);
		Calendar targetDate = Calendar.getInstance();
		targetDate.add(Calendar.DAY_OF_WEEK, -8);
		synchronization.setLastSynchronized(targetDate.getTime());
		boolean shouldSchedule = scheduler
				.computeShouldSynchBasedOnCurrentDateAndSynchProperties(
						synchronization, Calendar.getInstance());
		Assert.assertTrue("should have scheduled", shouldSchedule);

	}

	@Test
	public final void testShouldNotScheduleBasedOnWeekly() throws Exception {
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		SynchPeriodicScheduler scheduler = new SynchPeriodicScheduler(
				transferManager, irodsAccessObjectFactory);
		Synchronization synchronization = new Synchronization();
		synchronization.setFrequencyType(FrequencyType.EVERY_WEEK);
		Calendar targetDate = Calendar.getInstance();
		targetDate.add(Calendar.DAY_OF_WEEK, -2);
		synchronization.setLastSynchronized(targetDate.getTime());
		boolean shouldSchedule = scheduler
				.computeShouldSynchBasedOnCurrentDateAndSynchProperties(
						synchronization, Calendar.getInstance());
		Assert.assertFalse("should not have scheduled", shouldSchedule);

	}

	@Test
	public final void testScheduleADailySynch() throws Exception {
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		SynchPeriodicScheduler scheduler = new SynchPeriodicScheduler(
				transferManager, irodsAccessObjectFactory);
		Synchronization synchronization = new Synchronization();
		synchronization.setFrequencyType(FrequencyType.EVERY_DAY);
		List<Synchronization> synchronizations = new ArrayList<Synchronization>();
		synchronizations.add(synchronization);
		SynchManagerService synchManagerService = Mockito
				.mock(SynchManagerService.class);
		Mockito.when(synchManagerService.listAllSynchronizations()).thenReturn(
				synchronizations);

		TransferServiceFactoryImpl transferServiceFactory = Mockito
				.mock(TransferServiceFactoryImpl.class);
		Mockito.when(transferServiceFactory.instanceSynchManagerService())
				.thenReturn(synchManagerService);

		Mockito.when(transferManager.getTransferServiceFactory()).thenReturn(
				transferServiceFactory);

		Set<LocalIRODSTransfer> localIRODSTransfers = new HashSet<LocalIRODSTransfer>();
		synchronization.setLocalIRODSTransfers(localIRODSTransfers);
		Calendar targetDate = Calendar.getInstance();
		targetDate.add(Calendar.DAY_OF_WEEK, -8);
		synchronization.setLastSynchronized(targetDate.getTime());
		synchronization.setIrodsPassword(HibernateUtil.obfuscate("password"));
		synchronization.setIrodsHostName("host");
		synchronization.setIrodsPort(1247);
		synchronization.setIrodsUserName("user");
		scheduler.run();
		Mockito.verify(transferManager).enqueueASynch(synchronization,
				synchronization.buildIRODSAccountFromSynchronizationData());

	}

	@Test
	public final void testScheduleADailySynchButOneInQueue() throws Exception {
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		SynchPeriodicScheduler scheduler = new SynchPeriodicScheduler(
				transferManager, irodsAccessObjectFactory);
		Synchronization synchronization = new Synchronization();
		synchronization.setFrequencyType(FrequencyType.EVERY_DAY);
		List<Synchronization> synchronizations = new ArrayList<Synchronization>();
		synchronizations.add(synchronization);
		SynchManagerService synchManagerService = Mockito
				.mock(SynchManagerService.class);
		Mockito.when(synchManagerService.listAllSynchronizations()).thenReturn(
				synchronizations);

		TransferServiceFactoryImpl transferServiceFactory = Mockito
				.mock(TransferServiceFactoryImpl.class);
		Mockito.when(transferServiceFactory.instanceSynchManagerService())
				.thenReturn(synchManagerService);

		Mockito.when(transferManager.getTransferServiceFactory()).thenReturn(
				transferServiceFactory);

		Set<LocalIRODSTransfer> localIRODSTransfers = new HashSet<LocalIRODSTransfer>();
		LocalIRODSTransfer localIRODSTransfer = new LocalIRODSTransfer();
		localIRODSTransfer.setTransferState(TransferState.ENQUEUED);
		localIRODSTransfers.add(localIRODSTransfer);

		synchronization.setLocalIRODSTransfers(localIRODSTransfers);
		Calendar targetDate = Calendar.getInstance();
		targetDate.add(Calendar.DAY_OF_WEEK, -8);
		synchronization.setLastSynchronized(targetDate.getTime());
		synchronization.setIrodsPassword(HibernateUtil.obfuscate("password"));
		synchronization.setIrodsHostName("host");
		synchronization.setIrodsPort(1247);
		synchronization.setIrodsUserName("user");
		scheduler.run();
		Mockito.verify(transferManager, Mockito.never()).enqueueASynch(
				synchronization,
				synchronization.buildIRODSAccountFromSynchronizationData());

	}

}
