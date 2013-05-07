/**
 * 
 */
package org.irods.jargon.conveyor.basic;

import java.util.Date;
import java.util.logging.Level;

import org.irods.jargon.conveyor.core.AbstractConveyorComponentService;
import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.GridAccountService;
import org.irods.jargon.conveyor.core.TransferAccountingManagementService;
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

	/**
	 * @param Transfer
	 *            the Transfer to use to prepare the TransferAttempt
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

    @Override
    public TransferItem updateTransferAfterSuccessfulFileTransfer(
                    org.irods.jargon.core.transfer.TransferStatus transferStatus,
                    TransferAttempt transferAttempt) 
                    throws ConveyorExecutionException {
        
                log.info("updated last good path to:{}", transferStatus.getSourceFileAbsolutePath());
                transferAttempt.setLastSuccessfulPath(transferStatus.getSourceFileAbsolutePath());

                // create transfer item
                TransferItem transferItem = new TransferItem();
                transferItem.setFile(true);
                transferItem.setSourceFileAbsolutePath(transferStatus
                                .getSourceFileAbsolutePath());
                transferItem.setTargetFileAbsolutePath(transferStatus
                                .getTargetFileAbsolutePath());
                transferItem.setTransferredAt(new Date());
                
                try {
                    transferAttempt.getTransferItems().add(transferItem);
                    transferAttemptDAO.save(transferAttempt);
                } catch (TransferDAOException ex) {
                    throw new ConveyorExecutionException(
					"error saving transfer attempt", ex);
                }
                
                return transferItem;
    }
        

}
