package org.irods.jargon.transfer.engine;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.transfer.dao.SynchronizationDAO;
import org.irods.jargon.transfer.dao.TransferDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.TransferItemDAO;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferItem;
import org.irods.jargon.transfer.dao.domain.TransferState;
import org.irods.jargon.transfer.dao.domain.TransferStatus;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manage the transfer queue and display of status of transfers. This
 * thread-safe object is meant to be a singleton and manages processing of a
 * transfer queue on behalf of the <code>TransferManager</code>
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@Transactional
public class TransferQueueServiceImpl implements TransferQueueService {

	private final Logger log = LoggerFactory
			.getLogger(TransferQueueServiceImpl.class);

	/**
	 * @throws JargonException
	 */
	public TransferQueueServiceImpl() {
		super();
	}

	private TransferDAO localIRODSTransferDAO;

	private TransferItemDAO localIRODSTransferItemDAO;

	private SynchronizationDAO synchronizationDAO;

	public SynchronizationDAO getSynchronizationDAO() {
		return synchronizationDAO;
	}

	public void setSynchronizationDAO(
			final SynchronizationDAO synchronizationDAO) {
		this.synchronizationDAO = synchronizationDAO;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferQueueService#dequeueTransfer()
	 */
	@Override
	public Transfer dequeueTransfer() throws JargonException {
		log.debug("entering dequeueTransfer()");
		Transfer transfer = null;
		try {

			List<Transfer> localIRODSTransferList = localIRODSTransferDAO
					.findByTransferState(TransferState.ENQUEUED,
							TransferState.PROCESSING, TransferState.PAUSED);

			log.info(">>>>> results of find by transfer state:{}",
					localIRODSTransferList);

			if (localIRODSTransferList != null
					& localIRODSTransferList.size() > 0) {
				transfer = localIRODSTransferList.get(0);
				log.debug("dequeue transfer:{}", transfer);
				// trigger lazy loading of the grid account
				transfer.getGridAccount().getHost();
				transfer.setTransferState(TransferState.PROCESSING);
				transfer.setLastTransferStatus(TransferStatus.OK);

				localIRODSTransferDAO.save(transfer);
			}
		} catch (TransferDAOException e) {
			log.error("error in transaction", e);
			throw new JargonException(e);
		}

		log.info("dequeued");
		return transfer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferQueueService#enqueuePutTransfer
	 * (java.lang.String, java.lang.String, java.lang.String,
	 * org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public Transfer enqueuePutTransfer(final String localSourceAbsolutePath,
			final String targetIRODSAbsolutePath, final String targetResource,
			final GridAccount gridAccount) throws JargonException {
		log.debug("entering enqueuePutTransfer()");

		if (localSourceAbsolutePath == null
				|| localSourceAbsolutePath.isEmpty()) {
			throw new JargonException(
					"localSourceAbsolutePath is null or empty");
		}

		if (targetIRODSAbsolutePath == null
				|| targetIRODSAbsolutePath.isEmpty()) {
			throw new JargonException(
					"targetIRODSAbsolutePath is null or empty");
		}

		if (targetResource == null) {
			throw new JargonException(
					"targetResource is null, set as blank if not used");
		}

		if (gridAccount == null) {
			throw new JargonException("null gridAccount");
		}

		log.info("enqueue put transfer from local source: {}",
				localSourceAbsolutePath);
		log.info("   target iRODS path: {}", targetIRODSAbsolutePath);
		log.info("   target resource:{}", targetResource);

		Transfer enqueuedTransfer = new Transfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath(targetIRODSAbsolutePath);
		enqueuedTransfer.setLocalAbsolutePath(localSourceAbsolutePath);

		enqueuedTransfer.setGridAccount(gridAccount);
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferState(TransferState.ENQUEUED);
		enqueuedTransfer.setLastTransferStatus(TransferStatus.OK);

		this.updateLocalIRODSTransfer(enqueuedTransfer);

		log.info("enqueued...");
		return enqueuedTransfer;

	}

	@Override
	public Transfer enqueueSynchTransfer(final Synchronization synchronization,
			final GridAccount gridAccount) throws JargonException {

		log.info("enqueue a synchronization");

		if (synchronization == null) {
			throw new IllegalArgumentException("null synchronization");
		}

		if (gridAccount == null) {
			throw new IllegalArgumentException("null gridAccount");
		}

		log.info("synchronization:{}", synchronization);
		log.info("gridAccount:{}", gridAccount);

		if (synchronization.getId() == null) {
			throw new JargonException(
					"synchronization is not persisted in database?");
		}

		Transfer enqueuedTransfer = new Transfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath(synchronization
				.getIrodsSynchDirectory());
		enqueuedTransfer.setLocalAbsolutePath(synchronization
				.getLocalSynchDirectory());
		enqueuedTransfer.setGridAccount(gridAccount);
		enqueuedTransfer.setTransferType(TransferType.SYNCH);
		enqueuedTransfer.setTransferState(TransferState.ENQUEUED);
		enqueuedTransfer.setLastTransferStatus(TransferStatus.OK);
		enqueuedTransfer.setSynchronization(synchronization);
		synchronization.getTransfers().add(enqueuedTransfer);
		localIRODSTransferDAO.save(enqueuedTransfer);
		log.info("transfer saved and associated with synchronization");
		return enqueuedTransfer;

	}

	@Override
	public Transfer enqueueGetTransfer(final String irodsSourceAbsolutePath,
			final String targetLocalAbsolutePath, final String sourceResource,
			final GridAccount gridAccount) throws JargonException {
		log.debug("entering enqueueGetTransfer()");

		if (irodsSourceAbsolutePath == null
				|| irodsSourceAbsolutePath.isEmpty()) {
			throw new JargonException(
					"irodsSourceAbsolutePath is null or empty");
		}

		if (targetLocalAbsolutePath == null
				|| targetLocalAbsolutePath.isEmpty()) {
			throw new JargonException(
					"targetLocalAbsolutePath is null or empty");
		}

		if (sourceResource == null) {
			throw new JargonException(
					"sourceResource is null, set as blank if not used");
		}

		if (gridAccount == null) {
			throw new JargonException("null gridAccount");
		}

		log.info("enqueue get transfer from irods source: {}",
				irodsSourceAbsolutePath);
		log.info("   target local path: {}", targetLocalAbsolutePath);
		log.info("   target resource:{}", sourceResource);

		Transfer enqueuedTransfer = new Transfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath(irodsSourceAbsolutePath);
		enqueuedTransfer.setLocalAbsolutePath(targetLocalAbsolutePath);
		enqueuedTransfer.setGridAccount(gridAccount);
		enqueuedTransfer.setTransferType(TransferType.GET);
		enqueuedTransfer.setTransferState(TransferState.ENQUEUED);
		enqueuedTransfer.setLastTransferStatus(TransferStatus.OK);

		try {
			log.info("saving...{}", enqueuedTransfer);
			localIRODSTransferDAO.save(enqueuedTransfer);
		} catch (TransferDAOException e) {
			log.error("error in transaction", e);
			throw new JargonException(e);
		}

		log.info("enqueued...");
		return enqueuedTransfer;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.TransferQueueService#
	 * markTransferAsErrorAndTerminate
	 * (org.irods.jargon.transfer.dao.domain.Transfer,
	 * org.irods.jargon.transfer.engine.TransferManager)
	 */
	@Override
	public void markTransferAsErrorAndTerminate(
			final Transfer localIRODSTransfer,
			final TransferManager transferManager) throws JargonException {
		markTransferAsErrorAndTerminate(localIRODSTransfer, null,
				transferManager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.TransferQueueService#
	 * markTransferAsErrorAndTerminate
	 * (org.irods.jargon.transfer.dao.domain.Transfer, java.lang.Exception,
	 * org.irods.jargon.transfer.engine.TransferManager)
	 */
	@Override
	public void markTransferAsErrorAndTerminate(
			final Transfer localIRODSTransfer, final Exception errorException,
			final TransferManager transferManager) throws JargonException {

		try {

			Transfer mergedTransfer = localIRODSTransferDAO
					.findById(localIRODSTransfer.getId());

			mergedTransfer.setTransferState(TransferState.COMPLETE);
			mergedTransfer.setLastTransferStatus(TransferStatus.ERROR);

			if (errorException != null) {
				log.warn("setting global exception to:{}", errorException);
				// mergedTransfer.setGlobalException(errorException.getMessage());
			}

			log.info("saving as error{}", mergedTransfer);
			localIRODSTransferDAO.save(mergedTransfer);
		} catch (TransferDAOException e) {
			log.error("error in transaction", e);
			throw new JargonException(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferQueueService#getLastNInQueue
	 * (int)
	 */
	@Override
	public List<Transfer> getLastNInQueue(final int countOfEntriesToShow)
			throws JargonException {
		log.debug("entering getLastNInQueue(int countOfEntriesToShow)");
		if (countOfEntriesToShow <= 0) {
			throw new JargonException("must show at least 1 entry");
		}

		try {

			List<Transfer> localIRODSTransferList = localIRODSTransferDAO
					.findAllSortedDesc(countOfEntriesToShow);
			return localIRODSTransferList;
		} catch (TransferDAOException e) {
			throw new JargonException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferQueueService#getCurrentQueue()
	 */
	@Override
	public List<Transfer> getCurrentQueue() throws JargonException {
		log.debug("entering getCurrentQueue()");
		try {
			List<Transfer> localIRODSTransferList = localIRODSTransferDAO
					.findByTransferState(80, TransferState.ENQUEUED,
							TransferState.PROCESSING, TransferState.PAUSED);
			return localIRODSTransferList;
		} catch (TransferDAOException e) {
			throw new JargonException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferQueueService#getErrorQueue()
	 */
	@Override
	public List<Transfer> getErrorQueue() throws JargonException {
		log.debug("entering getErrorQueue()");
		try {
			List<Transfer> localIRODSTransferList = localIRODSTransferDAO
					.findByTransferStatus(80, TransferStatus.ERROR);
			return localIRODSTransferList;
		} catch (TransferDAOException e) {
			throw new JargonException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferQueueService#getWarningQueue()
	 */
	@Override
	public List<Transfer> getWarningQueue() throws JargonException {
		log.debug("entering getWarningQueue()");
		try {

			List<Transfer> localIRODSTransferList = localIRODSTransferDAO
					.findByTransferStatus(80, TransferStatus.WARNING);
			return localIRODSTransferList;
		} catch (TransferDAOException e) {
			throw new JargonException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferQueueService#showErrorTransfers
	 * ()
	 */
	@Override
	public List<Transfer> showErrorTransfers() throws JargonException {
		return getErrorQueue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferQueueService#showWarningTransfers
	 * ()
	 */
	@Override
	public List<Transfer> showWarningTransfers() throws JargonException {
		return getWarningQueue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferQueueService#getRecentQueue()
	 */
	@Override
	// @Transactional
	public List<Transfer> getRecentQueue() throws JargonException {
		return getLastNInQueue(80);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.TransferQueueService#purgeQueue()
	 */
	@Override
	public void purgeQueue() throws JargonException {
		log.debug("entering purgeQueue()");
		try {

			log.info("purging the queue of all items (except a processing item");
			localIRODSTransferDAO.purgeQueue();
		} catch (TransferDAOException e) {
			throw new JargonException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferQueueService#purgeEntireQueue()
	 */
	@Override
	public void purgeEntireQueue() throws JargonException {
		log.debug("entering purgeEntireQueue()");
		try {

			log.info("purging the queue of all items");
			localIRODSTransferDAO.purgeEntireQueue();
		} catch (TransferDAOException e) {
			throw new JargonException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferQueueService#purgeSuccessful()
	 */
	@Override
	public void purgeSuccessful() throws JargonException {
		log.info("purging the queue of all complete items");
		try {

			log.info("purging the queue of all items (except a processing item");
			localIRODSTransferDAO.purgeSuccessful();
		} catch (TransferDAOException e) {
			throw new JargonException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.TransferQueueService#
	 * getAllTransferItemsForTransfer(java.lang.Long)
	 */
	@Override
	public List<TransferItem> getAllTransferItemsForTransfer(
			final Long localIRODSTransferId) throws JargonException {
		log.debug("entering getAllTransferItemsForTransfer(Long localIRODSTransferId)");
		List<TransferItem> items = new ArrayList<TransferItem>();
		try {

			items = localIRODSTransferItemDAO
					.findAllItemsForTransferByTransferId(localIRODSTransferId);
		} catch (TransferDAOException e) {
			throw new JargonException(e);
		}
		return items;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.TransferQueueService#
	 * getErrorTransferItemsForTransfer(java.lang.Long)
	 */
	@Override
	public List<TransferItem> getErrorTransferItemsForTransfer(
			final Long localIRODSTransferId) throws JargonException {
		log.debug("entering getAllTransferItemsForTransfer(Long localIRODSTransferId)");
		List<TransferItem> items = new ArrayList<TransferItem>();
		try {

			items = localIRODSTransferItemDAO
					.findErrorItemsByTransferId(localIRODSTransferId);
		} catch (TransferDAOException e) {
			throw new JargonException(e);
		}
		return items;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferQueueService#restartTransfer
	 * (org.irods.jargon.transfer.dao.domain.Transfer)
	 */
	@Override
	public void restartTransfer(final Transfer localIRODSTransfer)
			throws JargonException {
		if (localIRODSTransfer == null) {
			throw new JargonException("localIRODSTransfer");
		}
		log.info("restarting a transfer:{}", localIRODSTransfer);
		/*
		 * try {
		 * 
		 * 
		 * Transfer txfrToUpdate = localIRODSTransferDAO
		 * .findById(localIRODSTransfer.getId());
		 * log.info("beginning tx to store status of this transfer ");
		 * log.info(">>>>restart last successful path:{}",
		 * txfrToUpdate.getLastSuccessfulPath());
		 * txfrToUpdate.setLastTransferStatus(TransferStatus.OK);
		 * txfrToUpdate.setTransferState(TransferState.ENQUEUED);
		 * txfrToUpdate.setGlobalException("");
		 * txfrToUpdate.setGlobalExceptionStackTrace("");
		 * localIRODSTransferDAO.save(txfrToUpdate);
		 * log.info("status reset and enqueued for restart");
		 * 
		 * } catch (TransferDAOException e) { log.error("error in transaction",
		 * e); throw new JargonException(e); }
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferQueueService#resubmitTransfer
	 * (org.irods.jargon.transfer.dao.domain.Transfer)
	 */
	@Override
	public void resubmitTransfer(final Transfer localIRODSTransfer)
			throws JargonException {

		if (localIRODSTransfer == null) {
			throw new JargonException("localIRODSTransfer");
		}

		log.info("restarting a transfer:{}", localIRODSTransfer);

		// try {
		// TODO: Need to add in transfer attempts here
		// Transfer txfrToUpdate = localIRODSTransferDAO
		// .findInitializedById(localIRODSTransfer.getId());
		// Set<TransferItem> items = txfrToUpdate
		// .getLocalIRODSTransferItems();
		//
		// for (TransferItem item : items) {
		// localIRODSTransferItemDAO.delete(item);
		// }
		// txfrToUpdate
		// .setLocalIRODSTransferItems(new HashSet<TransferItem>());
		// txfrToUpdate.setTransferStatus(TransferStatus.OK);
		// txfrToUpdate.setTransferState(TransferState.ENQUEUED);
		// txfrToUpdate.setLastSuccessfulPath("");
		//
		// localIRODSTransferDAO.save(txfrToUpdate);
		// } catch (TransferDAOException e) {
		// log.error("error in transaction", e);
		// throw new JargonException(e);
		// }

	}

	@Override
	public Transfer enqueueReplicateTransfer(final String irodsAbsolutePath,
			final String targetResource, final GridAccount gridAccount)
			throws JargonException {

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new JargonException("irodsAbsolutePath is null or empty");
		}

		if (targetResource == null) {
			throw new JargonException(
					"targetResource is null, set as blank if not used");
		}

		if (gridAccount == null) {
			throw new JargonException("null irodsAccount");
		}

		log.info("enqueue replicate transfer from iRODS: {}", irodsAbsolutePath);
		log.info("   target resource:{}", targetResource);

		Transfer enqueuedTransfer = new Transfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath(irodsAbsolutePath);
		enqueuedTransfer.setLocalAbsolutePath("");
		enqueuedTransfer.setGridAccount(gridAccount);
		enqueuedTransfer.setTransferType(TransferType.REPLICATE);
		enqueuedTransfer.setTransferState(TransferState.ENQUEUED);
		enqueuedTransfer.setLastTransferStatus(TransferStatus.OK);

		try {
			localIRODSTransferDAO.save(enqueuedTransfer);
		} catch (TransferDAOException e) {
			log.error("error in transaction", e);
			throw new JargonException(e);
		}

		log.info("enqueued...");
		return enqueuedTransfer;

	}

	@Override
	public Transfer enqueueCopyTransfer(final String irodsSourceAbsolutePath,
			final String targetResource, final String irodsTargetAbsolutePath,
			final GridAccount gridAccount) throws JargonException {

		if (irodsSourceAbsolutePath == null
				|| irodsSourceAbsolutePath.isEmpty()) {
			throw new JargonException(
					"irodsSourceAbsolutePath is null or empty");
		}

		if (targetResource == null) {
			throw new JargonException(
					"targetResource is null, set as blank if not used");
		}

		if (irodsTargetAbsolutePath == null
				|| irodsTargetAbsolutePath.isEmpty()) {
			throw new JargonException(
					"irodsTargetAbsolutePath is null or empty");
		}

		if (gridAccount == null) {
			throw new JargonException("null gridAccount");
		}

		log.info("enqueue copy transfer from iRODS: {}",
				irodsSourceAbsolutePath);
		log.info("  to target iRODS path: {}", irodsTargetAbsolutePath);
		log.info("   target resource:{}", targetResource);

		Transfer enqueuedTransfer = new Transfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setLocalAbsolutePath(irodsSourceAbsolutePath);
		enqueuedTransfer.setIrodsAbsolutePath(irodsTargetAbsolutePath);
		enqueuedTransfer.setGridAccount(gridAccount);
		enqueuedTransfer.setTransferType(TransferType.COPY);
		enqueuedTransfer.setTransferState(TransferState.ENQUEUED);
		enqueuedTransfer.setLastTransferStatus(TransferStatus.OK);

		try {
			localIRODSTransferDAO.save(enqueuedTransfer);
		} catch (TransferDAOException e) {
			log.error("error in transaction", e);
			throw new JargonException(e);
		}

		log.info("enqueued...");
		return enqueuedTransfer;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferQueueService#setTransferAsCancelled
	 * (org.irods.jargon.transfer.dao.domain.Transfer)
	 */
	@Override
	public void setTransferAsCancelled(final Transfer localIRODSTransfer)
			throws JargonException {

		if (localIRODSTransfer == null) {
			throw new JargonException("localIRODSTransfer is null");
		}

		log.info("cancelling a transfer:{}", localIRODSTransfer);

		try {

			Transfer txfrToCancel = localIRODSTransferDAO
					.findById(localIRODSTransfer.getId());
			if (!txfrToCancel.getTransferState().equals(TransferState.COMPLETE)) {
				txfrToCancel.setLastTransferStatus(TransferStatus.OK);
				txfrToCancel.setTransferState(TransferState.CANCELLED);
				localIRODSTransferDAO.save(txfrToCancel);
				log.info("status set to cancelled");
			}

		} catch (TransferDAOException e) {
			log.error("error in transaction", e);
			throw new JargonException(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferQueueService#processQueueAtStartup
	 * ()
	 */
	@Override
	public void processQueueAtStartup() throws JargonException {
		log.info("in startup...");
		List<Transfer> currentQueue = getCurrentQueue();

		if (currentQueue.isEmpty()) {
			log.info("queue is empty");
			return;
		}

		for (Transfer localIrodsTransfer : currentQueue) {
			if (localIrodsTransfer.getTransferState().equals(
					TransferState.PROCESSING)) {
				log.info("resetting a processing transfer to enqueued:{}",
						localIrodsTransfer);
				resetTransferToEnqueued(localIrodsTransfer);
			}
		}

	}

	/**
	 * Reset a transfer to enqueued. Used on startup so transfers marked as
	 * processed are not treated as such during dequeue.
	 * 
	 * @param transferToReset
	 *            {@link org.irods.jargon.transfer.dao.domain.Transfer} to be
	 *            reset
	 * @throws JargonException
	 */
	private void resetTransferToEnqueued(final Transfer transferToReset)
			throws JargonException {

		try {
			transferToReset.setLastTransferStatus(TransferStatus.OK);
			transferToReset.setTransferState(TransferState.ENQUEUED);
			localIRODSTransferDAO.save(transferToReset);
			log.info("status set to enqueued");
		} catch (TransferDAOException e) {
			log.error("error in transaction", e);
			throw new JargonException(e);
		}
	}

	/**
	 * @param localIRODSTransferDAO
	 *            the localIRODSTransferDAO to set
	 */
	public void setLocalIRODSTransferDAO(final TransferDAO localIRODSTransferDAO) {
		this.localIRODSTransferDAO = localIRODSTransferDAO;
	}

	/**
	 * @param localIRODSTransferItemDAO
	 *            the localIRODSTransferItemDAO to set
	 */
	public void setLocalIRODSTransferItemDAO(
			final TransferItemDAO localIRODSTransferItemDAO) {
		this.localIRODSTransferItemDAO = localIRODSTransferItemDAO;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.TransferQueueService#
	 * updateLocalIRODSTransfer (org.irods.jargon.transfer.dao.domain.Transfer)
	 */
	@Override
	public void updateLocalIRODSTransfer(final Transfer localIrodsTransfer)
			throws JargonException {

		try {
			localIRODSTransferDAO.save(localIrodsTransfer);
		} catch (TransferDAOException e) {
			log.error(
					"transferDAOException when updating the localIrodsTransfer:{}",
					localIrodsTransfer, e);
			throw new JargonException(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.TransferQueueService#
	 * findLocalIRODSTransferById(java.lang.Long)
	 */
	@Override
	public Transfer findLocalIRODSTransferById(final Long id)
			throws JargonException {
		try {
			Transfer transfer = localIRODSTransferDAO.findById(id);
			// go ahead and load the synch
			Synchronization synchronization = transfer.getSynchronization();
			if (synchronization != null) {
				synchronization.getName();
			}
			return transfer;
		} catch (TransferDAOException e) {
			throw new JargonException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.TransferQueueService#
	 * findLocalIRODSTransferByIdInitializeItems(java.lang.Long)
	 */
	@Override
	public Transfer findLocalIRODSTransferByIdInitializeItems(final Long id)
			throws JargonException {
		try {
			return localIRODSTransferDAO.findInitializedById(id);
		} catch (TransferDAOException e) {
			throw new JargonException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferQueueService#addItemToTransfer
	 * (org.irods.jargon.transfer.dao.domain.Transfer,
	 * org.irods.jargon.transfer.dao.domain.TransferItem)
	 */
	@Override
	public void addItemToTransfer(final Transfer localIRODSTransfer,
			final TransferItem localIRODSTransferItem) throws JargonException {
		// TODO: Need to add in transfer attempt here
		// if (localIRODSTransfer == null) {
		// throw new IllegalArgumentException("null localIRODSTransfer");
		// }
		//
		// if (localIRODSTransferItem == null) {
		// throw new IllegalArgumentException("null localIRODSTransferItem");
		// }
		//
		// try {
		// Transfer merged = localIRODSTransferDAO
		// .findInitializedById(localIRODSTransfer.getId());
		// merged.getLocalIRODSTransferItems().add(localIRODSTransferItem);
		// } catch (TransferDAOException e) {
		// throw new JargonException(e);
		// }
	}

	/*
	 * @Override public void updateUserPasswordInTransferManagerData( final
	 * GridAccount gridAccount, final String newPasswordValue) throws
	 * CannotUpdateTransferInProgressException, JargonException {
	 * 
	 * 
	 * log.info("updateUserPasswordInTransferManagerData()");
	 * 
	 * if (gridAccount == null) { throw new
	 * IllegalArgumentException("null gridAccount"); }
	 * 
	 * if (newPasswordValue == null || newPasswordValue.isEmpty()) { throw new
	 * IllegalArgumentException("null or empty newPasswordValue"); }
	 * 
	 * log.info("irods account for change:{}", gridAccount); String
	 * encryptedPassword = HibernateUtil.obfuscate(newPasswordValue);
	 * 
	 * 
	 * List<LocalIRODSTransfer> currentQueue = this.getRecentQueue(); for
	 * (Transfer transfer : currentQueue) { if (transfer.getTransferState() ==
	 * TransferState.ENQUEUED || transfer.getTransferState() ==
	 * TransferState.PROCESSING) { if
	 * (transfer.getTransferHost().equals(irodsAccount.getHost()) &&
	 * transfer.getTransferZone().equals( irodsAccount.getZone()) &&
	 * transfer.getTransferUserName().equals( irodsAccount.getUserName())) {
	 * log.warn(
	 * "cannot update, as an enquened or processing transfer exists for the given user:{}"
	 * , transfer); throw new CannotUpdateTransferInProgressException(
	 * "cannot update password at this time, transfers are enqueued or running"
	 * ); } } }
	 * 
	 * 
	 * for (Transfer transfer : currentQueue) {
	 * 
	 * if (transfer.getTransferHost().equals(irodsAccount.getHost()) &&
	 * transfer.getTransferZone() .equals(irodsAccount.getZone()) &&
	 * transfer.getTransferUserName().equals( irodsAccount.getUserName())) {
	 * 
	 * transfer.setTransferPassword(encryptedPassword);
	 * localIRODSTransferDAO.save(transfer); log.info("updated transfer:{}",
	 * transfer);
	 * 
	 * } }
	 * 
	 * 
	 * 
	 * log.info("processing synchronizations");
	 * 
	 * List<Synchronization> synchronizations = synchronizationDAO.findAll();
	 * for (Synchronization synchronization : synchronizations) { if
	 * (synchronization.getIrodsHostName().equals(irodsAccount.getHost()) &&
	 * synchronization.getIrodsZone().equals(irodsAccount.getZone()) &&
	 * synchronization .getIrodsUserName().equals(irodsAccount.getUserName())) {
	 * log.info("synchronization needs new password:{}", synchronization);
	 * synchronization.setIrodsPassword(encryptedPassword);
	 * synchronizationDAO.save(synchronization); } }
	 * 
	 * log.info("password updates successful");
	 * 
	 * 
	 * }
	 */

}
