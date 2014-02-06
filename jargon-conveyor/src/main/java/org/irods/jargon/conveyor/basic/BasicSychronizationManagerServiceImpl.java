/**
 * 
 */
package org.irods.jargon.conveyor.basic;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.irods.jargon.conveyor.core.AbstractConveyorComponentService;
import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.conveyor.core.RejectedTransferException;
import org.irods.jargon.conveyor.core.SynchronizationManagerService;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.transfer.dao.SynchronizationDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferStateEnum;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Mike Conway - DICE (www.irods.org) see
 *         https://code.renci.org/gf/project/jargon/
 * 
 */
public class BasicSychronizationManagerServiceImpl extends
		AbstractConveyorComponentService implements
		SynchronizationManagerService {

	/**
	 * Injected dependency
	 */
	private SynchronizationDAO synchronizationDAO;

	/**
	 * Injected dependency
	 */
	private ConveyorService conveyorService;

	private static final Logger log = LoggerFactory
			.getLogger(BasicSychronizationManagerServiceImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.SynchronizationManagerService#
	 * listAllSynchronizations()
	 */
	@Override
	public List<Synchronization> listAllSynchronizations()
			throws ConveyorExecutionException {
		log.info("listAllSynchronizations()");
		try {
			return synchronizationDAO.findAll();
		} catch (TransferDAOException e) {
			log.error("error finding all", e);
			throw new ConveyorExecutionException("error finding all", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.SynchronizationManagerService#
	 * triggerExecutionOfAnyNextPendingSynchronization()
	 */
	@Override
	public void triggerExecutionOfAnyNextPendingSynchronization()
			throws ConveyorExecutionException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.SynchronizationManagerService#
	 * addOrUpdateSynchronization
	 * (org.irods.jargon.transfer.dao.domain.Synchronization)
	 */
	@Override
	public void addOrUpdateSynchronization(Synchronization synchronization)
			throws ConveyorExecutionException {

		log.info("addOrUpdateSynchronization()");

		if (synchronization == null) {
			throw new IllegalArgumentException("null synchronization");
		}

		validateSynchronization(synchronization);
		try {
			synchronizationDAO.save(synchronization);
		} catch (TransferDAOException e) {
			log.error("error saving synchronization", e);
			throw new ConveyorExecutionException(
					"error saving synchronization", e);
		}

	}

	/**
	 * Check files in synch to make sure they exist, will also verify the iRODS
	 * account
	 * 
	 * @param synchronization
	 * @throws ConveyorExecutionException
	 */
	private void validateSynchronization(final Synchronization synchronization)
			throws ConveyorExecutionException {
		log.info("validateSynchronization()");
		assert synchronization != null;

		IRODSAccount irodsAccount = conveyorService.getGridAccountService()
				.irodsAccountForGridAccount(synchronization.getGridAccount());

		if (synchronization.getIrodsSynchDirectory() == null
				|| synchronization.getIrodsSynchDirectory().isEmpty()) {
			throw new ConveyorExecutionException(
					"no irods synch directory found");
		}

		IRODSFile irodsFile;

		try {
			irodsFile = this.conveyorService
					.getIrodsAccessObjectFactory()
					.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(synchronization.getIrodsSynchDirectory());

			if (!irodsFile.exists()) {
				throw new ConveyorExecutionException("irodsFile does not exist");
			}

			if (!irodsFile.isDirectory()) {
				throw new ConveyorExecutionException("irodsFile not collection");
			}

		} catch (JargonException e) {
			throw new ConveyorExecutionException(
					"jargonException checking irods synch directory");
		}

		if (synchronization.getLocalSynchDirectory() == null
				|| synchronization.getLocalSynchDirectory().isEmpty()) {
			throw new ConveyorExecutionException(
					"no local synch directory found");
		}

		File localFile = new File(synchronization.getLocalSynchDirectory());

		if (!localFile.exists()) {
			throw new ConveyorExecutionException("localFile does not exist");
		}

		if (!localFile.isDirectory()) {
			throw new ConveyorExecutionException("localFile not collection");
		}

		log.info("we're valid");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.SynchronizationManagerService#
	 * deleteSynchronization
	 * (org.irods.jargon.transfer.dao.domain.Synchronization)
	 */
	@Override
	public void deleteSynchronization(Synchronization synchronization)
			throws ConveyorExecutionException {
		log.info("deleteSynchronization()");

		if (synchronization == null) {
			throw new IllegalArgumentException("null synchronization");
		}

		try {
			synchronizationDAO.delete(synchronization);
		} catch (TransferDAOException e) {
			log.error("error deleting", e);
			throw new ConveyorExecutionException("error deleting", e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.SynchronizationManagerService#
	 * purgeSynchronizationHistory
	 * (org.irods.jargon.transfer.dao.domain.Synchronization)
	 */
	@Override
	public void purgeSynchronizationHistory(Synchronization synchronization)
			throws DataNotFoundException, ConveyorExecutionException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.SynchronizationManagerService#
	 * triggerSynchronizationNow
	 * (org.irods.jargon.transfer.dao.domain.Synchronization)
	 */
	@Override
	public void triggerSynchronizationNow(Synchronization synchronization)
			throws RejectedTransferException, ConveyorExecutionException {

		log.info(" triggerSynchronizationNow()");

		if (synchronization == null) {
			throw new IllegalArgumentException("null synchronization");
		}

		log.info("scheduling...");
		scheduleASynchronization(synchronization);
	}

	private void scheduleASynchronization(Synchronization synchronization)
			throws RejectedTransferException, ConveyorExecutionException {
		log.info("scheduling a synchronization:{}", synchronization);
		boolean alreadyInQueue = false;
		try {
			synchronization = synchronizationDAO.findById(synchronization
					.getId());
			synchronization.getTransfers();
		} catch (TransferDAOException e) {
			log.error("error looking up synchronization data", e);
			throw new ConveyorExecutionException("error looking up a synch", e);
		}
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

		Transfer transfer = new Transfer();
		Date now = new Date();
		transfer.setCreatedAt(now);
		transfer.setGridAccount(synchronization.getGridAccount());
		transfer.setIrodsAbsolutePath(synchronization.getIrodsSynchDirectory());
		transfer.setLocalAbsolutePath(synchronization.getLocalSynchDirectory());
		transfer.setResourceName(synchronization.getGridAccount()
				.getDefaultResource());
		transfer.setSynchronization(synchronization);
		transfer.setTransferState(TransferStateEnum.ENQUEUED);
		transfer.setTransferType(TransferType.SYNCH);
		transfer.setUpdatedAt(now);

		log.info("built transfer for synch:{}", transfer);

		IRODSAccount irodsAccount = this.conveyorService
				.getGridAccountService().irodsAccountForGridAccount(
						synchronization.getGridAccount());
		conveyorService.getQueueManagerService().enqueueTransferOperation(
				transfer, irodsAccount);

		log.info("synchronization enqueued");

	}

	/**
	 * @return the synchronizationDAO
	 */
	public SynchronizationDAO getSynchronizationDAO() {
		return synchronizationDAO;
	}

	/**
	 * @param synchronizationDAO
	 *            the synchronizationDAO to set
	 */
	public void setSynchronizationDAO(SynchronizationDAO synchronizationDAO) {
		this.synchronizationDAO = synchronizationDAO;
	}

	/**
	 * @return the conveyorService
	 */
	public ConveyorService getConveyorService() {
		return conveyorService;
	}

	/**
	 * @param conveyorService
	 *            the conveyorService to set
	 */
	public void setConveyorService(ConveyorService conveyorService) {
		this.conveyorService = conveyorService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.SynchronizationManagerService#findById
	 * (long)
	 */
	@Override
	public Synchronization findById(long id) throws ConveyorExecutionException {
		try {
			return synchronizationDAO.findById(id);
		} catch (TransferDAOException e) {
			log.error("error finding by id", e);
			throw new ConveyorExecutionException(
					"could not find the synchronization by id", e);
		}
	}

}
