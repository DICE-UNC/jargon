/**
 * 
 */
package org.irods.jargon.conveyor.basic;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.irods.jargon.conveyor.core.AbstractConveyorComponentService;
import org.irods.jargon.conveyor.core.ConveyorBusyException;
import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.ConveyorExecutorService.ErrorStatus;
import org.irods.jargon.conveyor.core.ConveyorExecutorService.RunningStatus;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.conveyor.core.GridAccountService;
import org.irods.jargon.conveyor.core.QueueManagerService;
import org.irods.jargon.conveyor.core.RejectedTransferException;
import org.irods.jargon.conveyor.core.TransferNotFoundException;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.transfer.dao.TransferAttemptDAO;
import org.irods.jargon.transfer.dao.TransferDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferItem;
import org.irods.jargon.transfer.dao.domain.TransferStateEnum;
import org.irods.jargon.transfer.dao.domain.TransferStatusEnum;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic implementation of a queue manager service
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@Transactional(rollbackFor = { ConveyorExecutionException.class }, noRollbackFor = { JargonException.class }, propagation = Propagation.REQUIRED)
public class BasicQueueManagerServiceImpl extends
		AbstractConveyorComponentService implements QueueManagerService {

	/**
	 * Injected dependency
	 */
	private TransferDAO transferDAO;

	/**
	 * Injected dependency
	 */
	private TransferAttemptDAO transferAttemptDAO;

	/**
	 * Injected dependency
	 */
	private ConveyorService conveyorService;

	/**
	 * Injected dependency
	 */
	private GridAccountService gridAccountService;

	private static final Logger log = LoggerFactory
			.getLogger(BasicQueueManagerServiceImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.QueueManagerService#preprocessQueueAtStartup
	 * ()
	 */
	@Override
	public void preprocessQueueAtStartup() throws ConveyorExecutionException {

		log.info("preprocessQueueAtStartup()");
		List<Transfer> transfers = listAllTransfersInQueue();

		for (Transfer transfer : transfers) {
			if (transfer.getTransferState() == TransferStateEnum.PROCESSING) {
				log.info("found a processing transfer, set it to enqueued:{}",
						transfer);
				reenqueueTransferAtBootstrapTime(transfer.getId());
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.QueueManagerService#
	 * enqueueRestartOfTransferOperation(long)
	 */
	@Override
	public void enqueueRestartOfTransferOperation(final long transferId)
			throws TransferNotFoundException, RejectedTransferException,
			ConveyorExecutionException {

		log.info("enqueueTransferOperation()");

		reenqueueTransfer(transferId);

		log.info("restart enqueued, will trigger the queue");
		dequeueNextOperation();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.QueueManagerService#
	 * reenqueueTransferAtBootstrapTime(long)
	 */
	@Override
	public void reenqueueTransferAtBootstrapTime(final long transferId)
			throws TransferNotFoundException, RejectedTransferException,
			ConveyorExecutionException {

		log.info("reenqueueTransferAtBootstrapTime()");

		reenqueueTransfer(transferId);

		log.info("restart enqueued, queue is not yet triggered...");

	}

	/**
	 * @param transferId
	 * @throws TransferNotFoundException
	 * @throws ConveyorExecutionException
	 * @throws RejectedTransferException
	 */
	private void reenqueueTransfer(final long transferId)
			throws TransferNotFoundException, ConveyorExecutionException,
			RejectedTransferException {
		if (transferId <= 0) {
			throw new IllegalArgumentException("illegal transferId");
		}

		Transfer existingTransfer;
		try {
			existingTransfer = transferDAO.findById(transferId);
			if (existingTransfer == null) {
				log.error("cannot find tranfser to restart");
				throw new TransferNotFoundException("unable to find transfer");
			}
		} catch (TransferDAOException e) {
			throw new ConveyorExecutionException();
		}
		conveyorService.getTransferAccountingManagementService()
				.restartProcessingTransferAtStartup(transferId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.QueueManagerService#
	 * enqueueRestartOfTransferOperation(long)
	 */
	@Override
	public void enqueueResubmitOfTransferOperation(final long transferId)
			throws TransferNotFoundException, RejectedTransferException,
			ConveyorExecutionException {

		log.info("enqueueTransferOperation()");

		if (transferId <= 0) {
			throw new IllegalArgumentException("illegal transferId");
		}

		Transfer existingTransfer;
		try {
			existingTransfer = transferDAO.findById(transferId);
			if (existingTransfer == null) {
				log.error("cannot find tranfser to restart");
				throw new TransferNotFoundException("unable to find transfer");
			}
		} catch (TransferDAOException e) {
			throw new ConveyorExecutionException();
		}
		// evaluateTransferForExecution(existingTransfer);

		conveyorService.getTransferAccountingManagementService()
				.prepareTransferForResubmit(transferId);

		log.info("restart enqueued, will trigger the queue");
		dequeueNextOperation();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.QueueManagerService#enqueueTransferOperation
	 * (org.irods.jargon.transfer.dao.domain.Transfer,
	 * org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public void enqueueTransferOperation(final Transfer transfer,
			final IRODSAccount irodsAccount) throws RejectedTransferException,
			ConveyorExecutionException {

		log.info("enqueueTransferOperation()");

		if (transfer == null) {
			throw new IllegalArgumentException("null transfer");
		}

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		log.info("transfer:{}", transfer);
		log.info("irodsAccount:{}", irodsAccount);

		log.info("looking up corresponding GridAccount...");
		GridAccount gridAccount = gridAccountService
				.findGridAccountByIRODSAccount(irodsAccount);
		if (gridAccount == null) {
			log.error("error finding grid account for irodsAccount:{}",
					irodsAccount);
			throw new ConveyorExecutionException(
					"unable to resolve gridAccount from given irodsAccount");
		}

		log.info("building transfer...");

		/*
		 * Check to see if there is any reason to reject this transfer
		 * (malformed, duplicate). The evaluate method will throw a rejected
		 * exception if appropriate
		 */
		long currentTime = System.currentTimeMillis();
		Date currentDate = new Date(currentTime);

		transfer.setGridAccount(gridAccount);
		transfer.setTransferState(TransferStateEnum.ENQUEUED);
		transfer.setUpdatedAt(currentDate);
		transfer.setCreatedAt(currentDate);

		/*
		 * Enqueue triggers a dequeue
		 */

		try {
			transferDAO.save(transfer);
			conveyorService.getTransferAccountingManagementService()
					.prepareTransferForProcessing(transfer);
			dequeueNextOperation();
		} catch (TransferDAOException e) {
			log.error("error saving transfer", e);
			throw new ConveyorExecutionException("error saving transfer", e);
		}

		log.info("transfer added:{}", transfer);

	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	public List<Transfer> listAllTransfersInQueue()
			throws ConveyorExecutionException {
		log.info("listAllTransfersInQueue");

		try {
			return transferDAO.findAll();
		} catch (TransferDAOException e) {
			log.error("error listing all transfers", e);
			throw new ConveyorExecutionException("error listing transfers", e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.QueueManagerService#dequeueNextOperation()
	 */
	@Override
	public void dequeueNextOperation() throws ConveyorExecutionException {
		log.info("dequeueNextOperation()");

		try {
			if (getConveyorExecutorService().getRunningStatus() == RunningStatus.PAUSED
					|| getConveyorExecutorService().getRunningStatus() == RunningStatus.PAUSED_BUSY) {
				log.info("paused, do not dequeue");
				return;
			}

			getConveyorExecutorService().setBusyForAnOperation();
		} catch (ConveyorBusyException e) {
			log.info("busy, ignore..");
			return;
		}

		TransferAttempt transferAttempt = null;

		try {

			List<Transfer> transfers = transferDAO
					.findByTransferState(TransferStateEnum.ENQUEUED);

			if (transfers.isEmpty()) {
				log.info("nothing to process...");
				getConveyorExecutorService().setOperationCompleted();
				return;
			}

			Transfer transfer = transfers.get(0);
			log.info("have transfer to run... setting up the new attempt:{}",
					transfer);

			// upon dequeue clear the error status
			getConveyorExecutorService().setErrorStatus(ErrorStatus.OK);
			transferAttempt = transferAttemptDAO
					.findLastTransferAttemptForTransferByTransferId(transfer
							.getId());

			if (transferAttempt == null) {
				log.warn(
						"transfer attempt is not available in the transfer:{}",
						transfer);
			}

			transferAttempt.setAttemptStart(new Timestamp(System
					.currentTimeMillis()));
			transfer.setTransferState(TransferStateEnum.PROCESSING);
			transfer.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
			transferDAO.save(transfer);
			transferAttemptDAO.save(transferAttempt);

			getConveyorExecutorService().processTransfer(transferAttempt,
					conveyorService);
		} catch (JargonException je) {
			log.error("jargon exception dequeue operation, will unlock queue");

			if (transferAttempt != null) {
				log.info("updating transfer with exception", je);
				getConveyorService().getTransferAccountingManagementService()
						.updateTransferAttemptWithConveyorException(
								transferAttempt, je);

			}

			getConveyorExecutorService().setOperationCompleted();
			getConveyorService().getConveyorCallbackListener()
					.signalUnhandledConveyorException(je);
			dequeueNextOperation();
		} catch (Exception e) {
			log.error("jargon exception dequeue operation, will unlock queue");

			if (transferAttempt != null) {
				log.info("updating transfer with exception", e);
				getConveyorService().getTransferAccountingManagementService()
						.updateTransferAttemptWithConveyorException(
								transferAttempt, e);

			}

			getConveyorExecutorService().setOperationCompleted();
			getConveyorService().getConveyorCallbackListener()
					.signalUnhandledConveyorException(e);
			dequeueNextOperation();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.QueueManagerService#processTransfer(java
	 * .lang.String, java.lang.String,
	 * org.irods.jargon.core.connection.IRODSAccount,
	 * org.irods.jargon.transfer.dao.domain.TransferType)
	 */
	@Override
	public void enqueueTransferOperation(final String irodsFile,
			final String localFile, final IRODSAccount irodsAccount,
			final TransferType type) throws ConveyorExecutionException {
		log.info("processTransfer()");
		Transfer transfer = new Transfer();
		transfer.setCreatedAt(new Timestamp(System.currentTimeMillis()));
		transfer.setIrodsAbsolutePath(irodsFile);
		transfer.setLocalAbsolutePath(localFile);
		transfer.setTransferType(type);
		log.info("ready to enqueue transfer:{}", transfer);
		enqueueTransferOperation(transfer, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.QueueManagerService#purgeAllFromQueue()
	 */
	@Override
	public void purgeAllFromQueue() throws ConveyorBusyException,
			ConveyorExecutionException {
		log.info("purgeAllFromQueue()");

		log.info("see if conveyor is busy");

		try {
			getConveyorExecutorService().setBusyForAnOperation();
		} catch (ConveyorBusyException e) {
			log.info("conveyor is busy, cannot purge");
			throw e;
		}

		log.info("purge...");

		try {
			transferDAO.purgeEntireQueue();
		} catch (TransferDAOException e) {
			log.error("jargon exception dequeue operation, will unlock queue");
			throw new ConveyorExecutionException(e);
		} finally {
			getConveyorExecutorService().setOperationCompleted();

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.QueueManagerService#purgeSuccessfulFromQueue
	 * ()
	 */
	@Override
	public void purgeSuccessfulFromQueue() throws ConveyorBusyException,
			ConveyorExecutionException {
		log.info("purgeSuccessfulFromQueue()");

		log.info("see if conveyor is busy");

		try {
			getConveyorExecutorService().setBusyForAnOperation();
		} catch (ConveyorBusyException e) {
			log.info("conveyor is busy, cannot purge");
			throw e;
		}

		log.debug("entering purgeSuccessful()");
		try {
			List<Transfer> transfers = transferDAO.findAll();

			for (Transfer transfer : transfers) {
				if ((transfer.getTransferState() == TransferStateEnum.COMPLETE || transfer
						.getTransferState() == TransferStateEnum.CANCELLED)
						&& (transfer.getLastTransferStatus() == TransferStatusEnum.OK)) {
					log.info("deleting...{}", transfer);
					transferDAO.delete(transfer);
				}
			}

		} catch (TransferDAOException e) {
			log.error("jargon exception dequeue operation, will unlock queue");
			throw new ConveyorExecutionException(e);
		} finally {
			getConveyorExecutorService().setOperationCompleted();

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.QueueManagerService#deleteTransferFromQueue
	 * (org.irods.jargon.transfer.dao.domain.Transfer)
	 */
	@Override
	public void deleteTransferFromQueue(final Transfer transfer)
			throws ConveyorBusyException, ConveyorExecutionException {
		log.info("deleteTransferFromQueue()");

		log.info("see if conveyor is busy");

		try {
			getConveyorExecutorService().setBusyForAnOperation();
		} catch (ConveyorBusyException e) {
			log.info("conveyor is busy, cannot purge");
			throw e;
		}

		log.info("delete transfer id:{} ...", transfer.getId());

		try {
			transferDAO.delete(transfer);
		} catch (TransferDAOException e) {
			log.error("jargon exception deleting transfer");
			throw new ConveyorExecutionException(e);
		} finally {
			getConveyorExecutorService().setOperationCompleted();

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.QueueManagerService#cancelTransfer(long)
	 */
	@Override
	public void cancelTransfer(final long transferId)
			throws TransferNotFoundException, ConveyorExecutionException {

		TransferAttempt transferAttemptToCancel;
		try {
			transferAttemptToCancel = transferAttemptDAO
					.findLastTransferAttemptForTransferByTransferId(transferId);
			if (transferAttemptToCancel == null) {
				log.error("cannot find transfer to cancel");
				throw new TransferNotFoundException("unable to find transfer");
			}

		} catch (TransferDAOException e) {
			log.error("error in dao finding transfer attempt by id");
			throw new ConveyorExecutionException(e);
		}

		// check state of transfer attempt
		if (transferAttemptToCancel.getTransfer().getTransferState() == TransferStateEnum.PROCESSING) {

			TransferAttempt current = getConveyorService()
					.getConveyorExecutorService().getCurrentTransferAttempt();
			// check to see if this is the currently processing transfer attempt
			if (current != null
					&& transferAttemptToCancel.getId().longValue() == getConveyorService()
							.getConveyorExecutorService()
							.getCurrentTransferAttempt().getId().longValue()) {
				log.info("matched currently running transfer attempt - cancelling transfer");
				getConveyorService().getConveyorExecutorService()
						.requestCancel(transferAttemptToCancel);
			} else {
				log.info("no current seen, but go ahead and update the database indicating that the transfer is cancelled");
				conveyorService.getTransferAccountingManagementService()
						.updateTransferAfterCancellation(current);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.QueueManagerService#
	 * initializeGivenTransferByLoadingChildren
	 * (org.irods.jargon.transfer.dao.domain.Transfer)
	 */
	@Override
	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	public Transfer initializeGivenTransferByLoadingChildren(
			final Transfer transfer) throws ConveyorExecutionException {
		log.info("initializeGivenTransferByLoadingChildren");
		try {
			return transferDAO.initializeChildrenForTransfer(transfer);
		} catch (TransferDAOException e) {
			log.error("jargon exception dequeue operation, will unlock queue");
			throw new ConveyorExecutionException(e);
		}
	}

	/**
	 * @param transferDAO
	 *            the transferDAO to set
	 */
	public void setTransferDAO(final TransferDAO transferDAO) {
		this.transferDAO = transferDAO;
	}

	/**
	 * @param transferAttemptDAO
	 *            the transferAttemptDAO to set
	 */
	public void setTransferAttemptDAO(
			final TransferAttemptDAO transferAttemptDAO) {
		this.transferAttemptDAO = transferAttemptDAO;
	}

	/**
	 * @param gridAccountService
	 *            the gridAccountService to set
	 */
	public void setGridAccountService(
			final GridAccountService gridAccountService) {
		this.gridAccountService = gridAccountService;
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
	 * org.irods.jargon.conveyor.core.QueueManagerService#findTransferByTransferId
	 * (long)
	 */
	@Override
	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	public Transfer findTransferByTransferId(final long transferId)
			throws ConveyorExecutionException {
		log.info("initializeGivenTransferByLoadingChildren");
		try {
			return transferDAO.findInitializedById(transferId);
		} catch (TransferDAOException e) {
			log.error("error in dao finding transfer by id");
			throw new ConveyorExecutionException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.QueueManagerService#saveOrUpdateTransfer
	 * (org.irods.jargon.transfer.dao.domain.Transfer)
	 */
	@Override
	public void saveOrUpdateTransfer(final Transfer transfer)
			throws TransferNotFoundException, ConveyorExecutionException {
		log.info("saveOrUpdateTransfer()");

		if (transfer == null) {
			throw new IllegalArgumentException("null transfer");
		}

		log.info("saving transfer:{}", transfer);

		try {
			transferDAO.save(transfer);
		} catch (TransferDAOException e) {
			log.error("error saving transfer", e);
			throw new ConveyorExecutionException("error saving transfer", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.QueueManagerService#
	 * addTransferAttemptToTransfer(long,
	 * org.irods.jargon.transfer.dao.domain.TransferAttempt)
	 */
	@Override
	public void addTransferAttemptToTransfer(final long transferId,
			final TransferAttempt transferAttempt)
			throws TransferNotFoundException, ConveyorExecutionException {
		log.info("addTransferAttemptToTransfer");
		if (transferAttempt == null) {
			throw new IllegalArgumentException("null transferAttempt");
		}

		log.info("looking up transfer by id...");

		Transfer transfer;

		try {
			transfer = transferDAO.findById(new Long(transferId));
		} catch (TransferDAOException e) {
			log.error("exception finding transfer", e);
			throw new ConveyorExecutionException("error finding transfe", e);
		}

		if (transfer == null) {
			log.error("transfer could not be found");
			throw new TransferNotFoundException("unable to find transfer");
		}

		transferAttempt.setCreatedAt(new Timestamp(System.currentTimeMillis()));
		transferAttempt.setSequenceNumber(System.currentTimeMillis());
		transferAttempt.setUpdatedAt(transferAttempt.getCreatedAt());
		transferAttempt.setTransfer(transfer);
		transfer.getTransferAttempts().add(transferAttempt);
		log.info("attempt added");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.QueueManagerService#getNextTransferItems
	 * (long, int, int)
	 */
	@Override
	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	public List<TransferItem> getNextTransferItems(
			final long transferAttemptId, final int start, final int length)
			throws ConveyorExecutionException {
		List<TransferItem> items = null;

		log.info("getNextTransferItems");
		if (transferAttemptId <= 0) {
			throw new IllegalArgumentException("invalid transferId");
		}
		try {
			items = transferAttemptDAO.listTransferItemsInTransferAttempt(
					transferAttemptId, start, length);
		} catch (TransferDAOException e) {
			log.error("exception retrieving transfer items", e);
			throw new ConveyorExecutionException(
					"error finding transfer items", e);
		}

		return items;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.QueueManagerService#getNextTransferItems
	 * (java.lang.Long, int, int, boolean, boolean)
	 */
	@Override
	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	public List<TransferItem> getNextTransferItems(
			final Long transferAttemptId, final int start, final int length,
			final boolean showSuccess, final boolean showSkipped)
			throws ConveyorExecutionException {
		List<TransferItem> items = null;

		log.info("getNextTransferItems");
		if (transferAttemptId <= 0) {
			throw new IllegalArgumentException("invalid transferId");
		}
		try {
			items = transferAttemptDAO.listTransferItemsInTransferAttempt(
					transferAttemptId, start, length, showSuccess, showSkipped);
		} catch (TransferDAOException e) {
			log.error("exception retrieving transfer items", e);
			throw new ConveyorExecutionException(
					"error finding transfer items", e);
		}

		return items;
	}
}
