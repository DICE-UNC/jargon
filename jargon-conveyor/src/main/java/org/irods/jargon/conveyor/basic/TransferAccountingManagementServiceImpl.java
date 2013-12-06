package org.irods.jargon.conveyor.basic;

import java.sql.Timestamp;
import java.util.Date;

import org.irods.jargon.conveyor.core.AbstractConveyorComponentService;
import org.irods.jargon.conveyor.core.ConfigurationService;
import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.GridAccountService;
import org.irods.jargon.conveyor.core.RejectedTransferException;
import org.irods.jargon.conveyor.core.TransferAccountingManagementService;
import org.irods.jargon.conveyor.utils.ExceptionUtils;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.transfer.TransferStatus;
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
@Transactional(noRollbackFor = { JargonException.class }, rollbackFor = { ConveyorExecutionException.class })
public class TransferAccountingManagementServiceImpl extends
		AbstractConveyorComponentService implements
		TransferAccountingManagementService {

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
	@SuppressWarnings("unused")
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
	 * @param transferItemDAO
	 *            the transferItemDAO to set
	 */
	public void setTransferItemDAO(final TransferItemDAO transferItemDAO) {
		this.transferItemDAO = transferItemDAO;
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
		transfer.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

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

		transferAttempt.setAttemptStart(new Timestamp(System
				.currentTimeMillis()));
		transferAttempt.setAttemptStatus(TransferStatusEnum.OK);
		transferAttempt.setUpdatedAt(transferAttempt.getAttemptStart());

		try {
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
	 * prepareTransferForProcessing
	 * (org.irods.jargon.transfer.dao.domain.Transfer)
	 */
	@Override
	public TransferAttempt prepareTransferForProcessing(final Transfer transfer)
			throws ConveyorExecutionException {

		log.info("prepareTransferForProcessing()");

		if (transfer == null) {
			throw new IllegalArgumentException("null transfer");
		}

		log.info("transfer:{}", transfer);

		/*
		 * Note that the transfer object being passed in is not expected to be
		 * already persisted, so this will add a new transfer
		 */

		log.info("building transfer attempt...");

		long currentTime = System.currentTimeMillis();
		transfer.setLastTransferStatus(TransferStatusEnum.OK);
		transfer.setSequenceNumber(currentTime);
		transfer.setTransferState(TransferStateEnum.ENQUEUED);
		transfer.setUpdatedAt(new Date(currentTime));

		TransferAttempt transferAttempt = new TransferAttempt();
		transferAttempt.setSequenceNumber(currentTime);
		transferAttempt.setTransfer(transfer);
		transferAttempt.setTransfer(transfer);
		transferAttempt.setAttemptStatus(TransferStatusEnum.OK);
		transferAttempt.setCreatedAt(new Date(currentTime));
		transferAttempt.setUpdatedAt(transferAttempt.getCreatedAt());

		try {
			transfer.getTransferAttempts().add(transferAttempt);
			// transferAttemptDAO.save(transferAttempt);
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
			final TransferStatus transferStatus,
			final TransferAttempt transferAttempt)
			throws ConveyorExecutionException {

		log.info("updateTransferAfterSuccessfulFileTransfer()");

		if (transferStatus == null) {
			throw new IllegalArgumentException("null transferStatus");
		}

		if (transferAttempt == null) {
			throw new IllegalArgumentException("null transferAttempt");
		}

		log.info("updated last good path to:{}",
				transferStatus.getSourceFileAbsolutePath());

		long currentTime = System.currentTimeMillis();
		Date currentDate = new Date(currentTime);

		transferAttempt.setLastSuccessfulPath(transferStatus
				.getSourceFileAbsolutePath());
		transferAttempt.setTotalFilesTransferredSoFar(transferStatus
				.getTotalFilesTransferredSoFar());
		transferAttempt.setTotalFilesSkippedSoFar(transferStatus
				.getTotalFilesSkippedSoFar());
		transferAttempt.setTotalFilesCount(transferStatus
				.getTotalFilesToTransfer());
		transferAttempt.setUpdatedAt(currentDate);

		if (getConfigurationService()
				.getCachedConveyorConfigurationProperties()
				.isLogSuccessfulTransfers()) {

			// create transfer item
			TransferItem transferItem = new TransferItem();
			transferItem.setSequenceNumber(currentTime);
			transferItem.setFile(true);
			transferItem.setTransferType(transferAttempt.getTransfer()
					.getTransferType());
			transferItem.setSourceFileAbsolutePath(transferStatus
					.getSourceFileAbsolutePath());
			transferItem.setTargetFileAbsolutePath(transferStatus
					.getTargetFileAbsolutePath());
			transferItem.setTransferredAt(currentDate);
			transferItem.setLengthInBytes(transferStatus.getBytesTransfered());

			// try {
			transferItem.setTransferAttempt(transferAttempt);
			transferAttempt.getTransferItems().add(transferItem);

			/*
			 * try { transferItemDAO.save(transferItem); } catch
			 * (TransferDAOException e) { throw new ConveyorExecutionException(
			 * "error saving transfer attempt", e); }
			 */

		}

		try {
			transferAttemptDAO.save(transferAttempt);
		} catch (TransferDAOException e) {
			throw new ConveyorExecutionException(
					"error saving transfer attempt", e);
		}

		// transferAttemptDAO.save(localTransferAttempt);
		/*
		 * } catch (TransferDAOException ex) { throw new
		 * ConveyorExecutionException( "error saving transfer attempt", ex); }
		 */

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.TransferAccountingManagementService#
	 * updateTransferAfterFailedFileTransfer
	 * (org.irods.jargon.core.transfer.TransferStatus,
	 * org.irods.jargon.transfer.dao.domain.TransferAttempt, int)
	 */
	@Override
	public void updateTransferAfterFailedFileTransfer(
			final org.irods.jargon.core.transfer.TransferStatus transferStatus,
			final TransferAttempt transferAttempt,
			final int totalFileErrorsSoFar) throws ConveyorExecutionException {

		if (transferStatus == null) {
			throw new IllegalArgumentException("null transfer status");
		}

		if (transferAttempt == null) {
			throw new IllegalArgumentException("null transfer attempt");
		}

		long currentTime = System.currentTimeMillis();
		Date currentDate = new Date(currentTime);
		transferAttempt.setAttemptStatus(TransferStatusEnum.ERROR);
		transferAttempt.setUpdatedAt(currentDate);
		transferAttempt.setTotalFilesErrorSoFar(totalFileErrorsSoFar);

		// create transfer item
		TransferItem transferItem = new TransferItem();
		transferItem.setSequenceNumber(currentTime);
		transferItem.setTransferType(transferAttempt.getTransfer()
				.getTransferType());
		transferItem.setFile(true);
		transferItem.setSourceFileAbsolutePath(transferStatus
				.getSourceFileAbsolutePath());
		transferItem.setTargetFileAbsolutePath(transferStatus
				.getTargetFileAbsolutePath());
		transferItem.setError(true);
		transferItem.setTransferredAt(currentDate);
		transferItem.setTransferAttempt(transferAttempt);

		if (transferStatus.getTransferException() != null) {
			transferItem.setErrorMessage(transferStatus.getTransferException()
					.getMessage());
			transferItem.setErrorStackTrace(ExceptionUtils
					.stackTraceToString(transferStatus.getTransferException()));
		}

		transferAttempt.getTransferItems().add(transferItem);

		try {
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

		// log.info("exception:{}", exception);

		Transfer transfer = localTransferAttempt.getTransfer();

		transfer.setLastTransferStatus(TransferStatusEnum.ERROR);
		transfer.setTransferState(TransferStateEnum.COMPLETE);
		transfer.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

		localTransferAttempt.setAttemptStatus(TransferStatusEnum.ERROR);
		localTransferAttempt.setAttemptEnd(new Timestamp(System
				.currentTimeMillis()));
		localTransferAttempt.setErrorMessage(ERROR_ATTEMPTING_TO_RUN);
		localTransferAttempt.setGlobalException(exception.getMessage());
		localTransferAttempt.setGlobalExceptionStackTrace(ExceptionUtils
				.stackTraceToString(exception));
		localTransferAttempt.setUpdatedAt(localTransferAttempt.getAttemptEnd());

		try {
			log.info("saving transfer data via DAO");
			// transferAttemptDAO.save(transferAttempt);
			transferDAO.save(transfer);
		} catch (TransferDAOException ex) {
			log.error("transferDAO exception saving data", ex);
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
			final ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.TransferAccountingManagementService#
	 * updateTransferAfterOverallWarningByFileErrorThreshold
	 * (org.irods.jargon.core.transfer.TransferStatus,
	 * org.irods.jargon.transfer.dao.domain.TransferAttempt)
	 */
	@Override
	public void updateTransferAfterOverallWarningByFileErrorThreshold(
			final TransferStatus transferStatus,
			final TransferAttempt transferAttempt)
			throws ConveyorExecutionException {
		log.info("updateTransferStatusAfterOverallWarning()");
		transferUpdateOverall(transferStatus, transferAttempt,
				TransferStatusEnum.WARNING, TransferStateEnum.COMPLETE,
				WARNING_SOME_FAILED_MESSAGE);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.TransferAccountingManagementService#
	 * updateTransferAfterOverallWarningNoFilesTransferred
	 * (org.irods.jargon.core.transfer.TransferStatus,
	 * org.irods.jargon.transfer.dao.domain.TransferAttempt)
	 */
	@Override
	public void updateTransferAfterOverallWarningNoFilesTransferred(
			final TransferStatus transferStatus,
			final TransferAttempt transferAttempt)
			throws ConveyorExecutionException {
		log.info("updateTransferAfterOverallWarningNoFilesTransferred()");
		transferUpdateOverall(transferStatus, transferAttempt,
				TransferStatusEnum.WARNING, TransferStateEnum.COMPLETE,
				WARNING_NO_FILES_TRANSFERRED_MESSAGE);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.TransferAccountingManagementService#
	 * updateTransferAfterOverallFailureByFileErrorThreshold
	 * (org.irods.jargon.core.transfer.TransferStatus,
	 * org.irods.jargon.transfer.dao.domain.TransferAttempt)
	 */
	@Override
	public void updateTransferAfterOverallFailureByFileErrorThreshold(
			final TransferStatus transferStatus,
			final TransferAttempt transferAttempt)
			throws ConveyorExecutionException {
		log.info("updateTransferStatusAfterOverallWarning()");
		transferUpdateOverall(transferStatus, transferAttempt,
				TransferStatusEnum.ERROR, TransferStateEnum.COMPLETE,
				ERROR_SOME_FAILED_MESSAGE);

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
			final org.irods.jargon.core.transfer.TransferStatus transferStatus,
			final TransferAttempt transferAttempt)
			throws ConveyorExecutionException {

		log.info("updateTransferAfterOverallSuccess()");

		transferUpdateOverall(transferStatus, transferAttempt,
				TransferStatusEnum.OK, TransferStateEnum.COMPLETE, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.TransferAccountingManagementService#
	 * updateTransferAfterCancellation (
	 * org.irods.jargon.transfer.dao.domain.TransferAttempt)
	 */
	@Override
	public void updateTransferAfterCancellation(
			final TransferAttempt transferAttempt)
			throws ConveyorExecutionException {
		log.info("updateTransferAfterCancellation()");

		if (transferAttempt == null) {
			throw new IllegalArgumentException("null transferAttempt");
		}

		log.info("transferAttempt:{}", transferAttempt);

		TransferAttempt localTransferAttempt;
		try {
			localTransferAttempt = transferAttemptDAO.findById(transferAttempt
					.getId());
			if (localTransferAttempt == null) {
				log.error("null transfer attempt found, cannot update the database");
				throw new ConveyorExecutionException(
						"error finding transfer attempt");

			}
		} catch (TransferDAOException e) {
			throw new ConveyorExecutionException(
					"error finding transfer attempt", e);
		}

		Transfer transfer = localTransferAttempt.getTransfer();

		transfer.setLastTransferStatus(TransferStatusEnum.OK);
		transfer.setTransferState(TransferStateEnum.CANCELLED);
		transfer.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
		localTransferAttempt.setAttemptEnd(new Timestamp(System
				.currentTimeMillis()));
		localTransferAttempt.setAttemptStatus(TransferStatusEnum.OK);
		localTransferAttempt.setUpdatedAt(localTransferAttempt.getAttemptEnd());
		localTransferAttempt.setErrorMessage(WARNING_CANCELLED_MESSAGE);

		try {
			transferAttemptDAO.save(localTransferAttempt);
			transferDAO.save(transfer);

		} catch (TransferDAOException ex) {
			throw new ConveyorExecutionException(
					"error saving transfer attempt", ex);
		}
	}

	/**
	 * Handle details of updating the transfer status with an OK, Success, or
	 * Failure status as described by the parameters
	 * 
	 * @param transferStatus
	 *            {@link TransferStatus} for overall completion
	 * @param transferAttempt
	 *            {@link TransferAttempt}
	 * @param transferStatusEnum
	 * @param transferStateEnum
	 * @param errorMessage
	 * @throws ConveyorExecutionException
	 */
	private void transferUpdateOverall(final TransferStatus transferStatus,
			final TransferAttempt transferAttempt,
			final TransferStatusEnum transferStatusEnum,
			final TransferStateEnum transferState, final String errorMessage)
			throws ConveyorExecutionException {

		log.info("transferUpdateOverall()");

		if (transferStatus == null) {
			throw new IllegalArgumentException("null transferStatus");
		}

		if (transferAttempt == null) {
			throw new IllegalArgumentException("null transferAttempt");
		}

		log.info("transferAttempt:{}", transferAttempt);
		log.info("transferStatus:{}", transferStatus);

		/*
		 * TransferAttempt localTransferAttempt; try { localTransferAttempt =
		 * transferAttemptDAO.findById(transferAttempt .getId()); if
		 * (localTransferAttempt == null) {
		 * log.error("null transfer attempt found, cannot update the database");
		 * throw new ConveyorExecutionException(
		 * "error finding transfer attempt");
		 * 
		 * } } catch (TransferDAOException e) { throw new
		 * ConveyorExecutionException( "error finding transfer attempt", e); }
		 */
		Transfer transfer = transferAttempt.getTransfer();

		log.info("transfer for update:{}", transfer);

		transfer.setLastTransferStatus(transferStatusEnum);
		transfer.setTransferState(transferState);
		transfer.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
		transferAttempt
				.setAttemptEnd(new Timestamp(System.currentTimeMillis()));
		transferAttempt.setAttemptStatus(transferStatusEnum);
		transferAttempt.setUpdatedAt(transferAttempt.getAttemptEnd());
		transferAttempt.setErrorMessage(errorMessage);

		log.info("updated transfer attempt:{}", transferAttempt);

		try {
			// transferAttemptDAO.save(localTransferAttempt);
			transferDAO.save(transfer);
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
			final TransferStatus transferStatus,
			final TransferAttempt transferAttempt)
			throws ConveyorExecutionException {
		log.info("updateTransferAfterOverallFailure()");

		if (transferStatus == null) {
			throw new IllegalArgumentException("null transferStatus");
		}

		if (transferAttempt == null) {
			throw new IllegalArgumentException("null transferAttempt");
		}

		log.info("transferAttempt:{}", transferAttempt);
		log.info("transferStatus:{}", transferStatus);

		TransferAttempt localTransferAttempt;
		try {
			localTransferAttempt = transferAttemptDAO.findById(transferAttempt
					.getId());
			if (localTransferAttempt == null) {
				log.error("null transfer attempt found, cannot update the database");
				throw new ConveyorExecutionException(
						"error finding transfer attempt");

			}
		} catch (TransferDAOException e) {
			throw new ConveyorExecutionException(
					"error finding transfer attempt", e);
		}

		Transfer transfer = localTransferAttempt.getTransfer();
		transfer.setLastTransferStatus(TransferStatusEnum.ERROR);
		transfer.setTransferState(TransferStateEnum.COMPLETE);
		transfer.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
		localTransferAttempt.setAttemptEnd(new Timestamp(System
				.currentTimeMillis()));
		localTransferAttempt.setAttemptStatus(TransferStatusEnum.ERROR);
		localTransferAttempt.setErrorMessage(ERROR_IN_TRANSFER_AT_IRODS_LEVEL);
		localTransferAttempt.setGlobalException(ExceptionUtils
				.messageOrNullFromException(transferStatus
						.getTransferException()));
		localTransferAttempt.setGlobalExceptionStackTrace(ExceptionUtils
				.stackTraceToString(transferStatus.getTransferException()));

		try {
			transferAttemptDAO.save(localTransferAttempt);
			transferDAO.save(transfer);
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
			final org.irods.jargon.core.transfer.TransferStatus transferStatus,
			final TransferAttempt transferAttempt)
			throws ConveyorExecutionException {

		log.info("updateTransferAfterRestartFileSkipped()");

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

		long currentTime = System.currentTimeMillis();
		Date currentDate = new Date(currentTime);
		localTransferAttempt.setTotalFilesCount(transferStatus
				.getTotalFilesToTransfer());
		localTransferAttempt.setUpdatedAt(currentDate);
		localTransferAttempt.setTotalFilesSkippedSoFar(transferStatus
				.getTotalFilesSkippedSoFar());
		localTransferAttempt.setTotalFilesTransferredSoFar(transferStatus
				.getTotalFilesTransferredSoFar());
		log.info("total skipped so far:{}",
				localTransferAttempt.getTotalFilesSkippedSoFar());

		try {
			transferAttemptDAO.save(localTransferAttempt);
			log.info("transfer attempt was saved");
		} catch (TransferDAOException e) {
			throw new ConveyorExecutionException(
					"error saving transfer attempt", e);
		}

		if (!getConfigurationService()
				.getCachedConveyorConfigurationProperties()
				.isLogSuccessfulTransfers()) {
			log.info("not logging restart...update transfer attempt with counts");
			return;
		}

		log.info("logging restart, log transfer item");

		// create transfer item
		TransferItem transferItem = new TransferItem();
		transferItem.setSequenceNumber(currentTime);
		transferItem.setFile(true);
		transferItem.setSkipped(true);
		transferItem.setTransferType(localTransferAttempt.getTransfer()
				.getTransferType());
		transferItem.setSourceFileAbsolutePath(transferStatus
				.getSourceFileAbsolutePath());
		transferItem.setTargetFileAbsolutePath(transferStatus
				.getTargetFileAbsolutePath());
		transferItem.setTransferredAt(currentDate);

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

		long currentTime = System.currentTimeMillis();
		Date currentDate = new Date(currentTime);

		log.info("building transfer attempt based on previous attempt...");
		transfer.setTransferState(TransferStateEnum.ENQUEUED);
		transfer.setUpdatedAt(currentDate);
		transfer.setLastTransferStatus(TransferStatusEnum.OK);

		TransferAttempt newTransferAttempt = new TransferAttempt();
		newTransferAttempt.setSequenceNumber(currentTime);
		newTransferAttempt.setAttemptStatus(TransferStatusEnum.OK);
		newTransferAttempt.setLastSuccessfulPath(lastTransferAttempt
				.getLastSuccessfulPath());
		newTransferAttempt.setCreatedAt(currentDate);
		newTransferAttempt.setUpdatedAt(currentDate);
		newTransferAttempt.setTransfer(transfer);
		transfer.getTransferAttempts().add(newTransferAttempt);
		log.info("added new transfer attempt:{}", newTransferAttempt);

		try {
			transferAttemptDAO.save(newTransferAttempt);
		} catch (TransferDAOException e) {
			log.error("error updating transfer for restart", e);
			throw new ConveyorExecutionException(
					"cannot update transfer for restart", e);
		}

		return transfer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.TransferAccountingManagementService#
	 * prepareTransferForRestart(long)
	 */
	@Override
	public Transfer prepareTransferForResubmit(final long transferId)
			throws ConveyorExecutionException, RejectedTransferException {
		log.info("transferId:{}", transferId);
		log.info("looking up transfer to resubmit...");

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
					"no previous attempt found to base resubmit on");
		}

		long currentTime = System.currentTimeMillis();
		Date currentDate = new Date(currentTime);

		log.info("building transfer attempt based on previous attempt...");
		transfer.setTransferState(TransferStateEnum.ENQUEUED);
		transfer.setUpdatedAt(currentDate);
		transfer.setLastTransferStatus(TransferStatusEnum.OK);

		TransferAttempt newTransferAttempt = new TransferAttempt();
		newTransferAttempt.setSequenceNumber(currentTime);
		newTransferAttempt.setAttemptStatus(TransferStatusEnum.OK);
		newTransferAttempt.setCreatedAt(currentDate);
		newTransferAttempt.setUpdatedAt(currentDate);
		newTransferAttempt.setTransfer(transfer);
		transfer.getTransferAttempts().add(newTransferAttempt);
		log.info("added new transfer attempt:{}", newTransferAttempt);

		try {
			transferDAO.save(transfer);
		} catch (TransferDAOException e) {
			log.error("error updating transfer for restart", e);
			throw new ConveyorExecutionException(
					"cannot update transfer for restart", e);
		}

		return transfer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.TransferAccountingManagementService#
	 * isLogSuccessfulTransfers()
	 */
	@Override
	public boolean isLogSuccessfulTransfers() throws ConveyorExecutionException {
		return getConfigurationService()
				.getCachedConveyorConfigurationProperties()
				.isLogSuccessfulTransfers();
	}

}
