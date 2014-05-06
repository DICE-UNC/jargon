/**
 * 
 */
package org.irods.jargon.conveyor.basic;

import java.io.File;
import java.sql.Timestamp;
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
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.transfer.dao.SynchronizationDAO;
import org.irods.jargon.transfer.dao.TransferAttemptDAO;
import org.irods.jargon.transfer.dao.TransferDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferStateEnum;
import org.irods.jargon.transfer.dao.domain.TransferStatusEnum;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Mike Conway - DICE (www.irods.org) see
 *         https://code.renci.org/gf/project/jargon/
 * 
 */
@Transactional(rollbackFor = { ConveyorExecutionException.class }, noRollbackFor = { JargonException.class }, propagation = Propagation.REQUIRED)
public class BasicSychronizationManagerServiceImpl extends
		AbstractConveyorComponentService implements
		SynchronizationManagerService {

	/**
	 * Injected dependency
	 */
	private SynchronizationDAO synchronizationDAO;

	/**
	 * injected dependency
	 */
	private TransferDAO transferDAO;

	public TransferDAO getTransferDAO() {
		return transferDAO;
	}

	public void setTransferDAO(final TransferDAO transferDAO) {
		this.transferDAO = transferDAO;
	}

	public TransferAttemptDAO getTransferAttemptDAO() {
		return transferAttemptDAO;
	}

	public void setTransferAttemptDAO(
			final TransferAttemptDAO transferAttemptDAO) {
		this.transferAttemptDAO = transferAttemptDAO;
	}

	/**
	 * injected dependency
	 */
	private TransferAttemptDAO transferAttemptDAO;

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
	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
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
	public void addOrUpdateSynchronization(final Synchronization synchronization)
			throws ConveyorExecutionException {

		log.info("addOrUpdateSynchronization()");

		if (synchronization == null) {
			throw new IllegalArgumentException("null synchronization");
		}

		validateSynchronization(synchronization);
		Date now = new Date();

		synchronization.setUpdatedAt(now);
		if (synchronization.getId() == null) {
			synchronization.setCreatedAt(now);
		}

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
			irodsFile = conveyorService
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
	public void deleteSynchronization(final Synchronization synchronization)
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
	public void purgeSynchronizationHistory(
			final Synchronization synchronization)
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
	public void triggerSynchronizationNow(final Synchronization synchronization)
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
		synchronization.getTransfers().add(transfer);
		transfer.setTransferState(TransferStateEnum.ENQUEUED);
		transfer.setTransferType(TransferType.SYNCH);
		transfer.setUpdatedAt(now);
		try {

			// transferDAO.save(transfer);
			synchronizationDAO.save(synchronization);
		} catch (TransferDAOException ex) {
			log.error("error saving synch", ex);
			throw new ConveyorExecutionException("error savign synch", ex);
		}

		log.info("built transfer for synch:{}", transfer);

		IRODSAccount irodsAccount = conveyorService.getGridAccountService()
				.irodsAccountForGridAccount(synchronization.getGridAccount());
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
	public void setSynchronizationDAO(
			final SynchronizationDAO synchronizationDAO) {
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
	public void setConveyorService(final ConveyorService conveyorService) {
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
	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	public Synchronization findById(final long id)
			throws ConveyorExecutionException {
		try {
			return synchronizationDAO.findById(id);
		} catch (TransferDAOException e) {
			log.error("error finding by id", e);
			throw new ConveyorExecutionException(
					"could not find the synchronization by id", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.SynchronizationManagerService#
	 * updateSynchronizationWithWarningCompletion
	 * (org.irods.jargon.core.transfer.TransferStatus,
	 * org.irods.jargon.transfer.dao.domain.TransferAttempt)
	 */
	@Override
	public void updateSynchronizationWithWarningCompletion(
			final TransferStatus transferStatus,
			final TransferAttempt transferAttempt)
			throws ConveyorExecutionException {
		log.info("updateSynchronizationWithWarningCompletion()");

		if (transferStatus == null) {
			throw new IllegalArgumentException("null transferStatus");
		}

		if (transferAttempt == null) {
			throw new IllegalArgumentException("null transferAtempt");
		}

		log.info("delegate update of transfer to accounting management service...");
		getConveyorService().getTransferAccountingManagementService()
				.updateTransferAfterOverallWarningByFileErrorThreshold(
						transferStatus, transferAttempt);
		Synchronization synchronization = transferAttempt.getTransfer()
				.getSynchronization();
		if (synchronization == null) {
			throw new ConveyorExecutionException(
					"no synchronization configured for the transfer");
		}

		log.info("updating synchronization for this success...");
		synchronization
				.setLastSynchronizationStatus(TransferStatusEnum.WARNING);
		synchronization.setLastSynchronized(new Date());
		try {
			synchronizationDAO.save(synchronization);
		} catch (TransferDAOException e) {
			log.info("error saving synchronization");
			throw new ConveyorExecutionException(e);
		}

	}

	@Override
	public void updateSynchronizationWithSuccessfulCompletion(
			final TransferStatus transferStatus,
			final TransferAttempt transferAttempt)
			throws ConveyorExecutionException {
		log.info("updateSynchronizationWithSuccessfulCompletion()");

		if (transferStatus == null) {
			throw new IllegalArgumentException("null transferStatus");
		}

		if (transferAttempt == null) {
			throw new IllegalArgumentException("null transferAtempt");
		}

		log.info("update of transfer...");

		Transfer transfer = transferAttempt.getTransfer();

		transfer.setLastTransferStatus(TransferStatusEnum.OK);
		transfer.setTransferState(TransferStateEnum.COMPLETE);
		transfer.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
		transferAttempt
				.setAttemptEnd(new Timestamp(System.currentTimeMillis()));
		transferAttempt.setAttemptStatus(TransferStatusEnum.OK);
		transferAttempt.setUpdatedAt(transferAttempt.getAttemptEnd());
		transferAttempt.setErrorMessage("");
		Synchronization synchronization = transfer.getSynchronization();
		if (synchronization == null) {
			throw new ConveyorExecutionException(
					"no synchronization configured for the transfer");
		}

		log.info("updating synchronization for this success...");
		synchronization.setLastSynchronizationStatus(TransferStatusEnum.OK);
		synchronization.setLastSynchronized(new Date());

		log.info("updated transfer attempt:{}", transferAttempt);

		try {
			synchronizationDAO.save(synchronization);
			// transferAttemptDAO.save(transferAttempt);
			// transferDAO.save(transfer);
		} catch (TransferDAOException ex) {
			log.error("transferDAOException on save of transfer data", ex);
			throw new ConveyorExecutionException(
					"error saving transfer attempt", ex);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.SynchronizationManagerService#
	 * updateSynchronizationWithFailure
	 * (org.irods.jargon.core.transfer.TransferStatus,
	 * org.irods.jargon.transfer.dao.domain.TransferAttempt)
	 */
	@Override
	public void updateSynchronizationWithFailure(
			final TransferStatus transferStatus,
			final TransferAttempt transferAttempt)
			throws ConveyorExecutionException {
		log.info("updateSynchronizationWithFailure()");

		if (transferStatus == null) {
			throw new IllegalArgumentException("null transferStatus");
		}

		if (transferAttempt == null) {
			throw new IllegalArgumentException("null transferAtempt");
		}

		log.info("delegate update of transfer to accounting management service...");
		getConveyorService().getTransferAccountingManagementService()
				.updateTransferAfterOverallFailure(transferStatus,
						transferAttempt);
		Synchronization synchronization = transferAttempt.getTransfer()
				.getSynchronization();
		if (synchronization == null) {
			throw new ConveyorExecutionException(
					"no synchronization configured for the transfer");
		}

		log.info("updating synchronization for this success...");
		synchronization.setLastSynchronizationStatus(TransferStatusEnum.ERROR);
		synchronization.setLastSynchronized(new Date());
		synchronization.setLastSynchronizationMessage(transferAttempt
				.getErrorMessage());
		try {
			synchronizationDAO.save(synchronization);
		} catch (TransferDAOException e) {
			log.info("error saving synchronization");
			throw new ConveyorExecutionException(e);
		}

	}

}
