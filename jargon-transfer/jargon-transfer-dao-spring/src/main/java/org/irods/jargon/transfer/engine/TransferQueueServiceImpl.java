package org.irods.jargon.transfer.engine;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.transfer.dao.LocalIRODSTransferDAO;
import org.irods.jargon.transfer.dao.LocalIRODSTransferItemDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransferItem;
import org.irods.jargon.transfer.dao.domain.TransferState;
import org.irods.jargon.transfer.dao.domain.TransferStatus;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.irods.jargon.transfer.util.HibernateUtil;
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
public class TransferQueueServiceImpl implements TransferQueueService {

	private final Logger log = LoggerFactory
			.getLogger(TransferQueueServiceImpl.class);

	/**
	 * @throws JargonException
	 */
	public TransferQueueServiceImpl() {
		super();
	}

	private LocalIRODSTransferDAO localIRODSTransferDAO;
	private LocalIRODSTransferItemDAO localIRODSTransferItemDAO;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferQueueService#dequeueTransfer()
	 */
	@Override
	@Transactional
	public LocalIRODSTransfer dequeueTransfer() throws JargonException {
		log.debug("entering dequeueTransfer()");
		LocalIRODSTransfer transfer = null;
		try {

			List<LocalIRODSTransfer> localIRODSTransferList = localIRODSTransferDAO
					.findByTransferState(TransferState.ENQUEUED,
							TransferState.PROCESSING, TransferState.PAUSED);
			if (localIRODSTransferList != null
					& localIRODSTransferList.size() > 0) {
				transfer = localIRODSTransferList.get(0);
				log.debug("dequeue transfer:{}", transfer);

				transfer.setTransferStart(new Date());
				transfer.setTransferState(TransferState.PROCESSING);
				transfer.setTransferStatus(TransferStatus.OK);

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
	@Transactional
	public LocalIRODSTransfer enqueuePutTransfer(
			final String localSourceAbsolutePath,
			final String targetIRODSAbsolutePath, final String targetResource,
			final IRODSAccount irodsAccount) throws JargonException {
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

		if (irodsAccount == null) {
			throw new JargonException("null irodsAccount");
		}

		log.info("enqueue put transfer from local source: {}",
				localSourceAbsolutePath);
		log.info("   target iRODS path: {}", targetIRODSAbsolutePath);
		log.info("   target resource:{}", targetResource);

		LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath(targetIRODSAbsolutePath);
		enqueuedTransfer.setLocalAbsolutePath(localSourceAbsolutePath);
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(targetResource);
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(HibernateUtil
				.obfuscate(irodsAccount.getPassword()));
		enqueuedTransfer.setTransferState(TransferState.ENQUEUED);
		enqueuedTransfer.setTransferStatus(TransferStatus.OK);
		
		this.updateLocalIRODSTransfer(enqueuedTransfer);

		log.info("enqueued...");
		return enqueuedTransfer;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferQueueService#enqueueGetTransfer
	 * (java.lang.String, java.lang.String, java.lang.String,
	 * org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	@Transactional
	public LocalIRODSTransfer enqueueGetTransfer(
			final String irodsSourceAbsolutePath,
			final String targetLocalAbsolutePath, final String sourceResource,
			final IRODSAccount irodsAccount) throws JargonException {
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

		if (irodsAccount == null) {
			throw new JargonException("null irodsAccount");
		}

		log.info("enqueue get transfer from irods source: {}",
				irodsSourceAbsolutePath);
		log.info("   target local path: {}", targetLocalAbsolutePath);
		log.info("   target resource:{}", sourceResource);

		LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath(irodsSourceAbsolutePath);
		enqueuedTransfer.setLocalAbsolutePath(targetLocalAbsolutePath);
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(sourceResource);
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.GET);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(HibernateUtil
				.obfuscate(irodsAccount.getPassword()));
		enqueuedTransfer.setTransferState(TransferState.ENQUEUED);
		enqueuedTransfer.setTransferStatus(TransferStatus.OK);

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
	 * (org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer,
	 * org.irods.jargon.transfer.engine.TransferManager)
	 */
	@Override
	@Transactional
	public void markTransferAsErrorAndTerminate(
			final LocalIRODSTransfer localIRODSTransfer,
			final TransferManager transferManager) throws JargonException {
		markTransferAsErrorAndTerminate(localIRODSTransfer, null,
				transferManager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.TransferQueueService#
	 * markTransferAsErrorAndTerminate
	 * (org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer,
	 * java.lang.Exception, org.irods.jargon.transfer.engine.TransferManager)
	 */
	@Override
	@Transactional
	public void markTransferAsErrorAndTerminate(
			final LocalIRODSTransfer localIRODSTransfer,
			final Exception errorException,
			final TransferManager transferManager) throws JargonException {

		try {

			LocalIRODSTransfer mergedTransfer = localIRODSTransferDAO
					.findById(localIRODSTransfer.getId());

			mergedTransfer.setTransferEnd(new Date());
			mergedTransfer.setTransferState(TransferState.COMPLETE);
			mergedTransfer.setTransferStatus(TransferStatus.ERROR);

			if (errorException != null) {
				log.warn("setting global exception to:{}", errorException);
				mergedTransfer.setGlobalException(errorException.getMessage());
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
	@Transactional
	public List<LocalIRODSTransfer> getLastNInQueue(
			final int countOfEntriesToShow) throws JargonException {
		log.debug("entering getLastNInQueue(int countOfEntriesToShow)");
		if (countOfEntriesToShow <= 0) {
			throw new JargonException("must show at least 1 entry");
		}

		try {

			List<LocalIRODSTransfer> localIRODSTransferList = localIRODSTransferDAO
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
	@Transactional
	public List<LocalIRODSTransfer> getCurrentQueue() throws JargonException {
		log.debug("entering getCurrentQueue()");
		try {
			List<LocalIRODSTransfer> localIRODSTransferList = localIRODSTransferDAO
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
	@Transactional
	public List<LocalIRODSTransfer> getErrorQueue() throws JargonException {
		log.debug("entering getErrorQueue()");
		try {
			List<LocalIRODSTransfer> localIRODSTransferList = localIRODSTransferDAO
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
	@Transactional
	public List<LocalIRODSTransfer> getWarningQueue() throws JargonException {
		log.debug("entering getWarningQueue()");
		try {

			List<LocalIRODSTransfer> localIRODSTransferList = localIRODSTransferDAO
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
	@Transactional
	public List<LocalIRODSTransfer> showErrorTransfers() throws JargonException {
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
	@Transactional
	public List<LocalIRODSTransfer> showWarningTransfers()
			throws JargonException {
		return getWarningQueue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferQueueService#getRecentQueue()
	 */
	@Override
	@Transactional
	public List<LocalIRODSTransfer> getRecentQueue() throws JargonException {
		return getLastNInQueue(80);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.TransferQueueService#purgeQueue()
	 */
	@Override
	@Transactional
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
	 * org.irods.jargon.transfer.engine.TransferQueueService#purgeSuccessful()
	 */
	@Override
	@Transactional
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
	@Transactional
	public List<LocalIRODSTransferItem> getAllTransferItemsForTransfer(
			final Long localIRODSTransferId) throws JargonException {
		log.debug("entering getAllTransferItemsForTransfer(Long localIRODSTransferId)");
		List<LocalIRODSTransferItem> items = new ArrayList<LocalIRODSTransferItem>();
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
	@Transactional
	public List<LocalIRODSTransferItem> getErrorTransferItemsForTransfer(
			final Long localIRODSTransferId) throws JargonException {
		log.debug("entering getAllTransferItemsForTransfer(Long localIRODSTransferId)");
		List<LocalIRODSTransferItem> items = new ArrayList<LocalIRODSTransferItem>();
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
	 * (org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer)
	 */
	@Override
	 @Transactional
	public void restartTransfer(final LocalIRODSTransfer localIRODSTransfer)
			throws JargonException {
		if (localIRODSTransfer == null) {
			throw new JargonException("localIRODSTransfer");
		}
		log.info("restarting a transfer:{}", localIRODSTransfer);
		try {

			LocalIRODSTransfer txfrToUpdate = localIRODSTransferDAO
					.findById(localIRODSTransfer.getId());
			log.info("beginning tx to store status of this transfer ");
			log.info(">>>>restart last successful path:{}",
					txfrToUpdate.getLastSuccessfulPath());
			txfrToUpdate.setTransferStatus(TransferStatus.OK);
			txfrToUpdate.setTransferState(TransferState.ENQUEUED);
			txfrToUpdate.setGlobalException("");
			txfrToUpdate.setGlobalExceptionStackTrace("");
			localIRODSTransferDAO.save(txfrToUpdate);
			log.info("status reset and enqueued for restart");
		} catch (TransferDAOException e) {
			log.error("error in transaction", e);
			throw new JargonException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferQueueService#resubmitTransfer
	 * (org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer)
	 */
	@Override
	@Transactional
	public void resubmitTransfer(final LocalIRODSTransfer localIRODSTransfer)
			throws JargonException {

		if (localIRODSTransfer == null) {
			throw new JargonException("localIRODSTransfer");
		}

		log.info("restarting a transfer:{}", localIRODSTransfer);

		try {

			LocalIRODSTransfer txfrToUpdate = localIRODSTransferDAO
					.findInitializedById(localIRODSTransfer.getId());
			Set<LocalIRODSTransferItem> items = txfrToUpdate
					.getLocalIRODSTransferItems();

			for (LocalIRODSTransferItem item : items) {
				localIRODSTransferItemDAO.delete(item);
			}
			txfrToUpdate
					.setLocalIRODSTransferItems(new HashSet<LocalIRODSTransferItem>());
			txfrToUpdate.setTransferStatus(TransferStatus.OK);
			txfrToUpdate.setTransferState(TransferState.ENQUEUED);
			txfrToUpdate.setLastSuccessfulPath("");

			localIRODSTransferDAO.save(txfrToUpdate);
		} catch (TransferDAOException e) {
			log.error("error in transaction", e);
			throw new JargonException(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.TransferQueueService#
	 * enqueueReplicateTransfer(java.lang.String, java.lang.String,
	 * org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	@Transactional
	public LocalIRODSTransfer enqueueReplicateTransfer(
			final String irodsAbsolutePath, final String targetResource,
			final IRODSAccount irodsAccount) throws JargonException {

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new JargonException("irodsAbsolutePath is null or empty");
		}

		if (targetResource == null) {
			throw new JargonException(
					"targetResource is null, set as blank if not used");
		}

		if (irodsAccount == null) {
			throw new JargonException("null irodsAccount");
		}

		log.info("enqueue replicate transfer from iRODS: {}", irodsAbsolutePath);
		log.info("   target resource:{}", targetResource);

		LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath(irodsAbsolutePath);
		enqueuedTransfer.setLocalAbsolutePath("");
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(targetResource);
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.REPLICATE);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(HibernateUtil
				.obfuscate(irodsAccount.getPassword()));
		enqueuedTransfer.setTransferState(TransferState.ENQUEUED);
		enqueuedTransfer.setTransferStatus(TransferStatus.OK);

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
	 * (org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer)
	 */
	@Override
	@Transactional
	public void setTransferAsCancelled(
			final LocalIRODSTransfer localIRODSTransfer) throws JargonException {

		if (localIRODSTransfer == null) {
			throw new JargonException("localIRODSTransfer is null");
		}

		log.info("cancelling a transfer:{}", localIRODSTransfer);

		try {

			LocalIRODSTransfer txfrToCancel = localIRODSTransferDAO
					.findById(localIRODSTransfer.getId());
			if (!txfrToCancel.getTransferState().equals(TransferState.COMPLETE)) {
				txfrToCancel.setTransferStatus(TransferStatus.OK);
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
	@Transactional
	public void processQueueAtStartup() throws JargonException {
		log.info("in startup...");
		List<LocalIRODSTransfer> currentQueue = getCurrentQueue();

		if (currentQueue.isEmpty()) {
			log.info("queue is empty");
			return;
		}

		for (LocalIRODSTransfer localIrodsTransfer : currentQueue) {
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
	 *            {@link org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer}
	 *            to be reset
	 * @throws JargonException
	 */
	private void resetTransferToEnqueued(
			final LocalIRODSTransfer transferToReset) throws JargonException {

		try {
			transferToReset.setTransferStatus(TransferStatus.OK);
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
	public void setLocalIRODSTransferDAO(
			final LocalIRODSTransferDAO localIRODSTransferDAO) {
		this.localIRODSTransferDAO = localIRODSTransferDAO;
	}

	/**
	 * @param localIRODSTransferItemDAO
	 *            the localIRODSTransferItemDAO to set
	 */
	public void setLocalIRODSTransferItemDAO(
			final LocalIRODSTransferItemDAO localIRODSTransferItemDAO) {
		this.localIRODSTransferItemDAO = localIRODSTransferItemDAO;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.transfer.engine.TransferQueueService#updateLocalIRODSTransfer(org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer)
	 */
	@Override
	@Transactional
	public void updateLocalIRODSTransfer(LocalIRODSTransfer localIrodsTransfer)
			throws JargonException {
		
		try {
			localIRODSTransferDAO.save(localIrodsTransfer);
		} catch (TransferDAOException e) {
			throw new JargonException(e);
		}
		
	}
	
	@Override
	@Transactional
	public LocalIRODSTransfer findLocalIRODSTransferById(final Long id) throws JargonException {
		try {
			return localIRODSTransferDAO.findById(id);
		} catch (TransferDAOException e) {
			throw new JargonException(e);
		}
	}
	
	@Override
	@Transactional
	public LocalIRODSTransfer findLocalIRODSTransferByIdInitializeItems(final Long id) throws JargonException {
		try {
			return localIRODSTransferDAO.findInitializedById(id);
		} catch (TransferDAOException e) {
			throw new JargonException(e);
		}
	}
	
	@Override
	@Transactional
	public void addItemToTransfer(final LocalIRODSTransfer localIRODSTransfer, final LocalIRODSTransferItem localIRODSTransferItem) throws JargonException {
		
		if (localIRODSTransfer == null) {
			throw new IllegalArgumentException("null localIRODSTransfer");
		}
		
		if (localIRODSTransferItem == null) {
			throw new IllegalArgumentException("null localIRODSTransferItem");
		}
	
		try {
			LocalIRODSTransfer merged = localIRODSTransferDAO.findInitializedById(localIRODSTransfer.getId());
			merged.getLocalIRODSTransferItems().add(localIRODSTransferItem);
		} catch (TransferDAOException e) {
			throw new JargonException(e);
		}
	}


}
