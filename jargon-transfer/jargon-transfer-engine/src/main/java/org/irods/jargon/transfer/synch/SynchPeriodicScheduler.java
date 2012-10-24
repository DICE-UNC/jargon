package org.irods.jargon.transfer.synch;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.irods.jargon.transfer.dao.domain.TransferState;
import org.irods.jargon.transfer.engine.TransferManager;
import org.irods.jargon.transfer.engine.synch.SynchException;
import org.irods.jargon.transfer.engine.synch.SynchManagerService;
import org.irods.jargon.transfer.engine.synch.SynchRuntimeException;
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

	private final TransferManager transferManager;
	@SuppressWarnings("unused")
	private final IRODSAccessObjectFactory irodsAccessObjectFactory;
	private static final DateFormat dateFormat = SimpleDateFormat
			.getDateTimeInstance();

	public static final Logger log = LoggerFactory
			.getLogger(SynchPeriodicScheduler.class);

	/**
	 * Default constructor with necessary dependencies.
	 * 
	 * @param transferManager
	 * @param irodsFileSystem
	 */
	public SynchPeriodicScheduler(final TransferManager transferManager,
			final IRODSAccessObjectFactory irodsAccessObjectFactory) {

		if (transferManager == null) {
			throw new IllegalArgumentException("Null transfer manager");
		}

		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException("Null irodsAccessObjectFactory");
		}

		this.transferManager = transferManager;
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	@Override
	public void run() {
		log.info("running synch periodic scheduler, listing existing synchs...");
		SynchManagerService synchManagerService = transferManager
				.getTransferServiceFactory().instanceSynchManagerService();
		List<Synchronization> synchronizations;

		try {
			synchronizations = synchManagerService.listAllSynchronizations();
		} catch (SynchException e) {
			log.error("synch exception listing synch data", e);
			throw new SynchRuntimeException(
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
		Set<LocalIRODSTransfer> localIRODSTransfers = synchronization
				.getLocalIRODSTransfers();
		for (LocalIRODSTransfer localIRODSTransfer : localIRODSTransfers) {
			if (localIRODSTransfer.getTransferState() == TransferState.ENQUEUED
					|| localIRODSTransfer.getTransferState() == TransferState.PROCESSING
					|| localIRODSTransfer.getTransferState() == TransferState.PAUSED) {
				log.info(
						"will not schedule this synch, as this synch transfer is already in the queue:{}",
						localIRODSTransfer);
				alreadyInQueue = true;
				break;
			}
		}

		if (alreadyInQueue) {
			return;
		}

		log.info("no conflicting synch in queue, go ahead and schedule");
		try {
			transferManager.enqueueASynch(synchronization,
					synchronization.getGridAccount());
		} catch (JargonException e) {
			log.error("error enqueuing a synch process for synch:{}",
					synchronization, e);
			throw new SynchRuntimeException("synch enqueue error", e);
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
			throw new SynchRuntimeException("unknown frequency type");
		}

		if (synchronization.getLastSynchronized().getTime() < targetDate
				.getTime().getTime()) {
			shouldSchedule = true;
		}
		return shouldSchedule;
	}

}
