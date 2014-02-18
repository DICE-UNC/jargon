package org.irods.jargon.conveyor.synch;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.conveyor.core.RejectedTransferException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferStateEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Timer task that can periodically schedule synchronization tasks. This is
 * meant to run periodically, and check the synchronizations in the transfer
 * database to schedule appropriate synchronization jobs
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class SynchPeriodicScheduler extends TimerTask {

	private final ConveyorService conveyorService;

	private static final DateFormat dateFormat = SimpleDateFormat
			.getDateTimeInstance();

	public static final Logger log = LoggerFactory
			.getLogger(SynchPeriodicScheduler.class);

	/**
	 * Default constructor with necessary dependencies.
	 * 
	 * @param conveyorService
	 */
	public SynchPeriodicScheduler(final ConveyorService conveyorService) {

		if (conveyorService == null) {
			throw new IllegalArgumentException("Null conveyorService");
		}

		this.conveyorService = conveyorService;
	}

	@Override
	public void run() {
		log.info("running synch periodic scheduler, listing existing synchs...");

		List<Synchronization> synchronizations;

		try {
			synchronizations = conveyorService
					.getSynchronizationManagerService()
					.listAllSynchronizations();
		} catch (ConveyorExecutionException e) {
			log.error("synch exception listing synch data", e);
			throw new JargonRuntimeException(
					"synch exception listing synch data", e);
		}

		log.info("synchs listed, inspecting for pending jobs...");
		Calendar nowDate = Calendar.getInstance();

		for (Synchronization synchronization : synchronizations) {
			log.info("evaluating synch:{}", synchronization);
			if (computeShouldSynchBasedOnCurrentDateAndSynchProperties(
					synchronization, nowDate)) {
				scheduleASynchronization(synchronization);
			}
		}

		log.info("schedule process completed");

	}

	/**
	 * Schedule a synchronization, after checking if a synch is already in the
	 * queue for this specification.
	 * 
	 * @param synchronization
	 */
	private void scheduleASynchronization(final Synchronization synchronization) {
		log.info("scheduling a synchronizaton:{}", synchronization);
		boolean alreadyInQueue = false;
		Set<Transfer> transfers = synchronization.getTransfers();
		for (Transfer transfer : transfers) {
			if (transfer.getTransferState() == TransferStateEnum.ENQUEUED
					|| transfer.getTransferState() == TransferStateEnum.PROCESSING
					|| transfer.getTransferState() == TransferStateEnum.PAUSED) {
				log.info(
						"will not schedule this synch, as this synch transfer is already in the queue:{}",
						transfer);
				alreadyInQueue = true;
				break;
			}
		}

		if (alreadyInQueue) {
			return;
		}

		log.info("no conflicting synch in queue, go ahead and schedule");
		try {
			conveyorService.getSynchronizationManagerService()
					.triggerSynchronizationNow(synchronization);
		} catch (RejectedTransferException e) {
			log.error("error enqueuing a synch process for synch:{}",
					synchronization, e);
			throw new JargonRuntimeException("synch enqueue error", e);
		} catch (ConveyorExecutionException e) {
			log.error("error enqueuing a synch process for synch:{}",
					synchronization, e);
			throw new JargonRuntimeException("synch enqueue error", e);
		}

		log.info("synchronization enqueued");

	}

	/**
	 * Given the specification in the <code>Synchronization</code>,
	 * 
	 * @param synchronization
	 * @param nowDate
	 * @return
	 */
	protected boolean computeShouldSynchBasedOnCurrentDateAndSynchProperties(
			final Synchronization synchronization, final Calendar nowDate) {

		Calendar targetDate = nowDate;

		if (synchronization.getLastSynchronized() == null) {
			log.info("this is the first synchronization, go ahead and schedule");
			return true;
		}

		// there has been a previous synch, so evaluate the date
		boolean shouldSchedule = false;
		log.info("last synch date was:{}",
				dateFormat.format(synchronization.getLastSynchronized()));

		switch (synchronization.getFrequencyType()) {
		case EVERY_HOUR:
			targetDate.add(Calendar.HOUR, -1);
			log.info("target date for hourly:{}",
					dateFormat.format(targetDate.getTime()));
			break;
		case EVERY_DAY:
			targetDate.add(Calendar.DAY_OF_WEEK, -1);
			log.info("target date for daily:{}",
					dateFormat.format(targetDate.getTime()));
			break;
		case EVERY_WEEK:
			targetDate.add(Calendar.DAY_OF_WEEK, -7);
			log.info("target date for weekly:{}",
					dateFormat.format(targetDate.getTime()));
			break;
		case EVERY_TWO_MINUTES:
			targetDate.add(Calendar.MINUTE, -2);
			log.info("target date for every two minutes:{}",
					dateFormat.format(targetDate.getTime()));
			break;
		default:
			log.error("unknown frequency type:{}",
					synchronization.getFrequencyType());
			throw new JargonRuntimeException("unknown frequency type");
		}

		if (synchronization.getLastSynchronized().getTime() < targetDate
				.getTime().getTime()) {
			shouldSchedule = true;
		}
		return shouldSchedule;
	}

}
