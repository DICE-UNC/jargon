/**
 * 
 */
package org.irods.jargon.conveyor.basic;

import java.util.Date;

import org.irods.jargon.conveyor.core.AbstractConveyorComponentService;
import org.irods.jargon.conveyor.core.ConfigurationService;
import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.GridAccountService;
import org.irods.jargon.conveyor.core.TransferAccountingManagementService;
import org.irods.jargon.conveyor.utils.ExceptionUtils;
import org.irods.jargon.transfer.dao.TransferAttemptDAO;
import org.irods.jargon.transfer.dao.TransferDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.TransferItemDAO;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferItem;
import org.irods.jargon.transfer.dao.domain.TransferState;
import org.irods.jargon.transfer.dao.domain.TransferStatus;
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
@Transactional
public class TransferAccountingManagementServiceImpl extends
		AbstractConveyorComponentService implements
		TransferAccountingManagementService {

	public static final String ERROR_ATTEMPTING_TO_RUN = "An error occurred while attempting to create and invoke the transfer process";

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

		transfer.setLastTransferStatus(TransferStatus.OK);
		transfer.setTransferState(TransferState.PROCESSING);
		transfer.setUpdatedAt(new Date());
		TransferAttempt transferAttempt = new TransferAttempt();
		transferAttempt.setTransfer(transfer);
		transferAttempt.setAttemptStart(new Date());
		transferAttempt.setTransfer(transfer);
		transferAttempt.setAttemptStatus(TransferStatus.OK);

		try {
			transfer.getTransferAttempts().add(transferAttempt);
			transferDAO.save(transfer);
			log.info("transfer attempt added:{}", transferAttempt);
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
	public TransferItem updateTransferAfterSuccessfulFileTransfer(
			final org.irods.jargon.core.transfer.TransferStatus transferStatus,
			final TransferAttempt transferAttempt)
			throws ConveyorExecutionException {

		if (!this.getConfigurationService()
				.getCachedConveyorConfigurationProperties()
				.isLogSuccessfulTransfers()) {
			log.info("not logging successful transfer");
		}

		log.info("updated last good path to:{}",
				transferStatus.getSourceFileAbsolutePath());
		transferAttempt.setLastSuccessfulPath(transferStatus
				.getSourceFileAbsolutePath());
		transferAttempt.setTotalFilesTransferredSoFar(transferAttempt
				.getTotalFilesTransferredSoFar() + 1);

		// create transfer item
		TransferItem transferItem = new TransferItem();
		transferItem.setFile(true);
		transferItem.setSourceFileAbsolutePath(transferStatus
				.getSourceFileAbsolutePath());
		transferItem.setTargetFileAbsolutePath(transferStatus
				.getTargetFileAbsolutePath());
		transferItem.setTransferredAt(new Date());

		try {
			transferItem.setTransferAttempt(transferAttempt);
			transferAttempt.getTransferItems().add(transferItem);
			transferItemDAO.save(transferItem);
		} catch (TransferDAOException ex) {
			throw new ConveyorExecutionException(
					"error saving transfer attempt", ex);
		}

		return transferItem;
	}
        
        @Override
        public TransferItem updateTransferAfterFailedFileTransfer(
                org.irods.jargon.core.transfer.TransferStatus transferStatus,
                TransferAttempt transferAttempt)
                throws ConveyorExecutionException {
            
            
                    // TODO: What is the global error exception and stack trace in transfer attempt?
            
                    // TODO: how to handle retries??
                    transferAttempt.setAttemptStatus(org.irods.jargon.transfer.dao.domain.TransferStatus.ERROR);
                    
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

                    return transferItem;
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

		transfer.setLastTransferStatus(TransferStatus.ERROR);
		transfer.setTransferState(TransferState.COMPLETE);
		transfer.setUpdatedAt(new Date());

		transferAttempt.setAttemptStatus(TransferStatus.ERROR);
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
}
