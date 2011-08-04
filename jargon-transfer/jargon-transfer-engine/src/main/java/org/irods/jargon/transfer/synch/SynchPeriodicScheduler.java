package org.irods.jargon.transfer.synch;

import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import org.irods.jargon.core.pub.CollectionAOImpl;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.transfer.dao.domain.Synchronization;
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
	private final IRODSAccessObjectFactory irodsAccessObjectFactory;

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
		SynchManagerService synchManagerService = transferManager.getTransferServiceFactory().instanceSynchManagerService();
		List<Synchronization> synchronizations;
		
		try {
			synchronizations = synchManagerService.listAllSynchronizations();
		} catch (SynchException e) {
			log.error("synch exception listing synch data", e);
			throw new SynchRuntimeException("synch exception listing synch data", e);
		}
		
		log.info("synchs listed, inspecting for pending jobs...");
		Date nowDate = new Date();
		
		for(Synchronization synchronization : synchronizations) {
			log.info("evaluating synch:{}", synchronization);
			
		
		}
		
		
	}
	
	protected boolean computeShoulDSynchBasedOnCurrentDateAndSynchProperties(Synchronization synchronization, final Date nowDate) throws SynchException {
		return false;
	}
	
	
}
