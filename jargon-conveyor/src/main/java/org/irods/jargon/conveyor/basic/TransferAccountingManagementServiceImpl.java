package org.irods.jargon.conveyor.basic;

import java.util.Date;

import org.irods.jargon.conveyor.core.AbstractConveyorComponentService;
import org.irods.jargon.conveyor.core.ConfigurationService;
import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.GridAccountService;
import org.irods.jargon.conveyor.core.RejectedTransferException;
import org.irods.jargon.conveyor.core.TransferAccountingManagementService;
import org.irods.jargon.conveyor.utils.ExceptionUtils;
import org.irods.jargon.transfer.dao.TransferAttemptDAO;
import org.irods.jargon.transfer.dao.TransferDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.TransferItemDAO;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferItem;
import org.irods.jargon.transfer.dao.domain.TransferStateEnum;
import org.irods.jargon.transfer.dao.domain.TransferStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of a service to manage transfers and update them as they
 * execute, based on callbacks and conveyor actions.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@Transactional(rollbackFor = { ConveyorExecutionException.class })
public class TransferAccountingManagementServiceImpl extends
		AbstractConveyorComponentService implements
		TransferAccountingManagementService {

	public static final String ERROR_ATTEMPTING_TO_RUN = "An error occurred while attempting to create and invoke the transfer process";
	public static final String ERROR_IN_TRANSFER_AT_IRODS_LEVEL = "An error during the transfer process at the client or in iRODS";
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
	private TransferItemDAO transferItemDAO;

	/**
	 * Injected dependency
	 */
	private GridAccountService gridAccountService;

	/**
	 * Injected dependency
	 */

	private ConfigurationService configurationService;

	private static final Logger log = LoggerFactory
			.getLogger(TransferAccountingManagementServiceImpl.class);

	/**
	 * 
	 */
	public TransferAccountingManagementServiceImpl() {
	}

	/**
	 * @return the transferDAO
	 */
	public TransferDAO getTransferDAO() {
		return transferDAO;
	}

	/**
	 * @param transferDAO
	 *            the transferDAO to set
	 */
	public void setTransferDAO(final TransferDAO transferDAO) {
		this.transferDAO = transferDAO;
	}

	/**
	 * @return the transferAttemptDAO
	 */
	public TransferAttemptDAO getTransferAttemptDAO() {
		return transferAttemptDAO;
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
	 * @return the transferItemDAO
	 */
	public TransferItemDAO getTransferItemDAO() {
		return transferItemDAO;
	}

	/**
	 * @param transferItemDAO
	 *            the transferItemDAO to set
	 */
	public void setTransferItemDAO(final TransferItemDAO transferItemDAO) {
		this.transferItemDAO = transferItemDAO;
	}

	/**
	 * @return the gridAccountService
	 */
	public GridAccountService getGridAccountService() {
		return gridAccountService;
	}

	/**
	 * @param gridAccountService
	 *            the gridAccountService to set
	 */
	public void setGridAccountService(
			final GridAccountService gridAccountService) {
		this.gridAccountService = gridAccountService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.TransferAccountingManagementService#
	 * prepareTransferForExecution
	 * (org.irods.jargon.transfer.dao.domain.Transfer)
	 */
	@Override
	public TransferAttempt prepareTransferForExecution(final Transfer transfer)
			throws ConveyorExecutionException {

		log.info("building transfer attempt...");
		if (transfer == null) {
			throw new IllegalArgumentException("transfer is null");
		}

		if (transfer.getId() == null) {
			throw new ConveyorExecutionException(
					"transfer does not have an id, it may not be stored in the transfer database");
		}

		transfer.setLastTransferStatus(TransferStatusEnum.OK);
		transfer.setTransferState(TransferStateEnum.PROCESSING);
		transfer.setUpdatedAt(new Date());

		TransferAttempt transferAttempt = null;
		try {
			transferAttempt = transferAttemptDAO
					.findLastTransferAttemptForTransferByTransferId(transfer
							.getId());
			if (transferAttempt == null) {
				log.error("couldn't find the transfer attempt in transfer:{}",
						transfer);
				throw new ConveyorExecutionException(
						"Unable to find transfer attempt for execution");
			}
		} catch (TransferDAOException e) {
			log.error("error saving transfer", e);
			throw new ConveyorExecutionException(
					"error saving transfer attempt", e);
		}

		transferAttempt.setAttemptStart(new Date());
		transferAttempt.setAttemptStatus(TransferStatusEnum.OK);

		try {
			transferDAO.save(transfer);
			log.info("transfer saved:{}", transfer);
			transferAttemptDAO.save(transferAttempt);
			log.info("transfer attempt added:{}", transferAttempt);
			return transferAttempt;
		} catch (TransferDAOException e) {
			log.error("error saving transfer", e);
			throw new ConveyorExecutionException(
					"error saving transfer attempt", e);
		}
	}

	@Override
	public TransferAttempt prepareTransferForProcessing(final Transfer transfer)
			throws ConveyorExecutionException {

		log.info("building transfer attempt...");

		transfer.setLastTransferStatus(TransferStatusEnum.OK);
		transfer.setTransferState(TransferStateEnum.ENQUEUED);
		transfer.setUpdatedAt(new Date());
		TransferAttempt transferAttempt = new TransferAttempt();
		transferAttempt.setTransfer(transfer);
		transferAttempt.setTransfer(transfer);
		transferAttempt.setAttemptStatus(TransferStatusEnum.OK);

		try {
			transferDAO.save(transfer);
			transfer.getTransferAttempts().add(transferAttempt);
			transferAttemptDAO.save(transferAttempt);
			log.info("transfer attempt added:{}", transferAttempt);
			transferDAO.save(transfer);
			log.info("transfer saved:{}", transfer);
			return transferAttempt;
		} catch (TransferDAOException e) {
			log.error("error saving transfer", e);
			throw new ConveyorExecutionException(
					"error saving transfer attempt", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.TransferAccountingManagementService#
	 * updateTransferAfterSuccessfulFileTransfer
	 * (org.irods.jargon.core.transfer.TransferStatus,
	 * org.irods.jargon.transfer.dao.domain.TransferAttempt)
	 */
	@Override
	public void updateTransferAfterSuccessfulFileTransfer(
			final org.irods.jargon.core.transfer.TransferStatus transferStatus,
			final TransferAttempt transferAttempt)
			throws ConveyorExecutionException {

		TransferAttempt localTransferAttempt;
		try {
			localTransferAttempt = transferAttemptDAO.findById(transferAttempt
					.getId());
			if (localTransferAttempt == null) {
				log.error("null tranfer attempt found, cannot update the database");
				throw new ConveyorExecutionException(
						"error finding transfer attempt");

			}
		} catch (TransferDAOException e) {
			throw new ConveyorExecutionException(
					"error finding transfer attempt", e);
		}
		log.info("updated last good path to:{}",
				transferStatus.getSourceFileAbsolutePath());
		localTransferAttempt.setLastSuccessfulPath(transferStatus
				.getSourceFileAbsolutePath());
		localTransferAttempt.setTotalFilesTransferredSoFar(transferStatus
				.getTotalFilesTransferredSoFar());
		localTransferAttempt.setTotalFilesCount(transferStatus
				.getTotalFilesToTransfer());

		if (!this.getConfigurationService()
				.getCachedConveyorConfigurationProperties()
				.isLogSuccessfulTransfers()) {
			log.info("not logging successful transfer...update transfer attempt with counts");
			try {
				transferAttemptDAO.save(localTransferAttempt);
				return;
			} catch (TransferDAOException e) {
				throw new ConveyorExecutionException(
						"error saving transfer attempt", e);
			}
		}

		// create transfer item
		TransferItem transferItem = new TransferItem();
		transferItem.setFile(true);
		transferItem.setTransferType(transferAttempt.getTransfer()
				.getTransferType()); // FIXME: why have transfer type here?
		transferItem.setSourceFileAbsolutePath(transferStatus
				.getSourceFileAbsolutePath());
		transferItem.setTargetFileAbsolutePath(transferStatus
				.getTargetFileAbsolutePath());
		transferItem.setTransferredAt(new Date());

		try {
			transferItem.setTransferAttempt(localTransferAttempt);
			localTransferAttempt.getTransferItems().add(transferItem);
			transferItemDAO.save(transferItem);
		} catch (TransferDAOException ex) {
			throw new ConveyorExecutionException(
					"error saving transfer attempt", ex);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.TransferAccountingManagementService#
	 * updateTransferAfterFailedFileTransfer
	 * (org.irods.jargon.core.transfer.TransferStatus,
	 * org.irods.jargon.transfer.dao.domain.TransferAttempt)
	 */
	@Override
	public void updateTransferAfterFailedFileTransfer(
			org.irods.jargon.core.transfer.TransferStatus transferStatus,
			TransferAttempt transferAttempt) throws ConveyorExecutionException {

		// TODO: What is the global error exception and stack trace in transfer
		// attempt?

		// TODO: how to handle retries??
		transferAttempt
				.setAttemptStatus(org.irods.jargon.transfer.dao.domain.TransferStatusEnum.ERROR);

		// create transfer item
		TransferItem transferItem = new TransferItem();
		transferItem.setFile(true);
		transferItem.setSourceFileAbsolutePath(transferStatus
				.getSourceFileAbsolutePath());
		transferItem.setTargetFileAbsolutePath(transferStatus
				.getTargetFileAbsolutePath());
		transferItem.setTransferredAt(new Date());
		transferItem.setErrorMessage(transferItem.getErrorMessage());
		transferItem.setErrorStackTrace(transferItem.getErrorStackTrace());

		try {
			transferAttempt.getTransferItems().add(transferItem);
			transferAttemptDAO.save(transferAttempt);
		} catch (TransferDAOException ex) {
			throw new ConveyorExecutionException(
					"error saving transfer attempt", ex);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.TransferAccountingManagementService#
	 * updateTransferAttemptWithConveyorException
	 * (org.irods.jargon.transfer.dao.domain.TransferAttempt,
	 * java.lang.Exception)
	 */
	@Override
	public void updateTransferAttemptWithConveyorException(
			final TransferAttempt transferAttempt, final Exception exception)
			throws ConveyorExecutionException {

		log.info("updateTransferAttemptWithConveyorException()");

		if (transferAttempt == null) {
			throw new IllegalArgumentException("null transferAttempt");
		}

		if (exception == null) {
			throw new IllegalArgumentException("null exception");
		}

		log.info("transferAttempt:{}", transferAttempt);
		log.info("exception:{}", exception);

		Transfer transfer = transferAttempt.getTransfer();

		transfer.setLastTransferStatus(TransferStatusEnum.ERROR);
		transfer.setTransferState(TransferStateEnum.COMPLETE);
		transfer.setUpdatedAt(new Date());

		transferAttempt.setAttemptStatus(TransferStatusEnum.ERROR);
		transferAttempt.setAttemptEnd(new Date());
		transferAttempt.setErrorMessage(ERROR_ATTEMPTING_TO_RUN);
		transferAttempt.setGlobalException(exception.getMessage());
		transferAttempt.setGlobalExceptionStackTrace(ExceptionUtils
				.stackTraceToString(exception));

		try {
			transferAttemptDAO.save(transferAttempt);
		} catch (TransferDAOException ex) {
			throw new ConveyorExecutionException(
					"error saving transfer attempt", ex);
		}

	}

	/**
	 * @return the configurationService
	 */
	public ConfigurationService getConfigurationService() {
		return configurationService;
	}

	/**
	 * @param configurationService
	 *            the configurationService to set
	 */
	public void setConfigurationService(
			ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.TransferAccountingManagementService#
	 * updateTransferAfterOverallSuccess
	 * (org.irods.jargon.core.transfer.TransferStatus,
	 * org.irods.jargon.transfer.dao.domain.TransferAttempt)
	 */
	@Override
	public void updateTransferAfterOverallSuccess(
			org.irods.jargon.core.transfer.TransferStatus transferStatus,
			TransferAttempt transferAttempt) throws ConveyorExecutionException {

		log.info("updateTransferAfterOverallSuccess()");

		if (transferStatus == null) {
			throw new IllegalArgumentException("null transferStatus");
		}

		if (transferAttempt == null) {
			throw new IllegalArgumentException("null transferAttempt");
		}

		log.info("transferAttempt:{}", transferAttempt);
		log.info("transferStatus:{}", transferStatus);

		Transfer transfer = transferAttempt.getTransfer();
		transfer.setLastTransferStatus(TransferStatusEnum.OK); // FIXME...evaluate
																// transfer for
																// errors
		transfer.setTransferState(TransferStateEnum.COMPLETE);
		transfer.setUpdatedAt(new Date());
		transferAttempt.setAttemptEnd(new Date());
		transferAttempt.setAttemptStatus(TransferStatusEnum.OK);

		try {
			transferDAO.save(transfer);
			transferAttemptDAO.save(transferAttempt);
		} catch (TransferDAOException ex) {
			throw new ConveyorExecutionException(
					"error saving transfer attempt", ex);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.TransferAccountingManagementService#
	 * updateTransferAfterOverallFailure
	 * (org.irods.jargon.core.transfer.TransferStatus,
	 * org.irods.jargon.transfer.dao.domain.TransferAttempt)
	 */
	@Override
	public void updateTransferAfterOverallFailure(
			org.irods.jargon.core.transfer.TransferStatus transferStatus,
			TransferAttempt transferAttempt) throws ConveyorExecutionException {
		log.info("updateTransferAfterOverallFailure()");

		if (transferStatus == null) {
			throw new IllegalArgumentException("null transferStatus");
		}

		if (transferAttempt == null) {
			throw new IllegalArgumentException("null transferAttempt");
		}

		log.info("transferAttempt:{}", transferAttempt);
		log.info("transferStatus:{}", transferStatus);

		Transfer transfer = transferAttempt.getTransfer();
		transfer.setLastTransferStatus(TransferStatusEnum.ERROR);
		transfer.setTransferState(TransferStateEnum.COMPLETE);
		transfer.setUpdatedAt(new Date());
		transferAttempt.setAttemptEnd(new Date());
		transferAttempt.setAttemptStatus(TransferStatusEnum.ERROR);
		transferAttempt.setErrorMessage(ERROR_IN_TRANSFER_AT_IRODS_LEVEL);
		transferAttempt.setGlobalException(ExceptionUtils
				.messageOrNullFromException(transferStatus
						.getTransferException()));
		transferAttempt.setGlobalExceptionStackTrace(ExceptionUtils
				.stackTraceToString(transferStatus.getTransferException()));

		try {
			transferDAO.save(transfer);
			transferAttemptDAO.save(transferAttempt);
		} catch (TransferDAOException ex) {
			throw new ConveyorExecutionException(
					"error saving transfer attempt", ex);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.TransferAccountingManagementService#
	 * updateTransferAfterRestartFileSkipped
	 * (org.irods.jargon.core.transfer.TransferStatus,
	 * org.irods.jargon.transfer.dao.domain.TransferAttempt)
	 */
	@Override
	public void updateTransferAfterRestartFileSkipped(
			org.irods.jargon.core.transfer.TransferStatus transferStatus,
			TransferAttempt transferAttempt) throws ConveyorExecutionException {
		TransferAttempt localTransferAttempt;
		try {
			localTransferAttempt = transferAttemptDAO.findById(transferAttempt
					.getId());
			if (localTransferAttempt == null) {
				log.error("null tranfer attempt found, cannot update the database");
				throw new ConveyorExecutionException(
						"error finding transfer attempt");

			}
		} catch (TransferDAOException e) {
			throw new ConveyorExecutionException(
					"error finding transfer attempt", e);
		}

		localTransferAttempt.setTotalFilesTransferredSoFar(transferStatus
				.getTotalFilesTransferredSoFar());
		localTransferAttempt.setTotalFilesCount(transferStatus
				.getTotalFilesToTransfer());

		if (!this.getConfigurationService()
				.getCachedConveyorConfigurationProperties()
				.isLogSuccessfulTransfers()
				&& this.getConfigurationService()
						.getCachedConveyorConfigurationProperties()
						.isRecordRestartFiles()) {
			log.info("not logging restart...update transfer attempt with counts");
			try {
				transferAttemptDAO.save(localTransferAttempt);
				return;
			} catch (TransferDAOException e) {
				throw new ConveyorExecutionException(
						"error saving transfer attempt", e);
			}
		}

		// create transfer item
		TransferItem transferItem = new TransferItem();
		transferItem.setFile(true);
		transferItem.setSkipped(true);
		transferItem.setTransferType(transferAttempt.getTransfer()
				.getTransferType());
		transferItem.setSourceFileAbsolutePath(transferStatus
				.getSourceFileAbsolutePath());
		transferItem.setTargetFileAbsolutePath(transferStatus
				.getTargetFileAbsolutePath());
		transferItem.setTransferredAt(new Date());

		try {
			transferItem.setTransferAttempt(localTransferAttempt);
			localTransferAttempt.getTransferItems().add(transferItem);
			transferItemDAO.save(transferItem);
		} catch (TransferDAOException ex) {
			throw new ConveyorExecutionException(
					"error saving transfer attempt", ex);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.TransferAccountingManagementService#
	 * prepareTransferForRestart(long)
	 */
	@Override
	public Transfer prepareTransferForRestart(final long transferId)
			throws ConveyorExecutionException, RejectedTransferException {
		log.info("transferId:{}", transferId);
		log.info("looking up transfer to restart...");

		Transfer transfer;
		try {
			transfer = transferDAO.findById(new Long(transferId));
		} catch (TransferDAOException e) {
			log.error("error looking up transfer by id", e);
			throw new ConveyorExecutionException(
					"unable to lookup transfer by id", e);
		}

		TransferAttempt lastTransferAttempt;
		try {
			lastTransferAttempt = transferAttemptDAO
					.findLastTransferAttemptForTransferByTransferId(transferId);
		} catch (TransferDAOException e) {
			log.error("error looking up last transfer attempt", e);
			throw new ConveyorExecutionException(
					"unable to lookup last transfer attempt", e);
		}

		if (lastTransferAttempt == null) {
			throw new RejectedTransferException(
					"no previous attempt found to base restart on");
		}

		log.info("building transfer attempt based on previous attempt...");
		transfer.setTransferState(TransferStateEnum.ENQUEUED);
		transfer.setUpdatedAt(new Date());
		transfer.setLastTransferStatus(TransferStatusEnum.OK);

		try {
			transferDAO.save(transfer);
		} catch (TransferDAOException e) {
			log.error("error updating transfer for restart", e);
			throw new ConveyorExecutionException(
					"cannot update transfer for restart", e);
		}

		return transfer;
	}

}
