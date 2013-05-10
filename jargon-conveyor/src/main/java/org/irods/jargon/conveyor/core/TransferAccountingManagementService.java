/**
 * 
 */
package org.irods.jargon.conveyor.core;

import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferItem;

/**
 * Service to manage updates to a <code>Transfer</code> as a result of running
 * that transfer. This would include updates to the current attempt, and
 * file-by-file accounting procedures as signaled in the transfer callback
 * process
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */

public interface TransferAccountingManagementService {

	/**
	 * Set up Transfer Attempt for Transfer about the be processed
	 * 
	 * @param transfer
	 *            {@link Transfer} containing populated data
	 * @return {@link TransferAttempt} based on the <code>Transfer</code>
	 * 
	 * @throws ConveyorExecutionException
	 */
	TransferAttempt prepareTransferForExecution(Transfer transfer)
			throws ConveyorExecutionException;
        
        
        /**
	 * Update a transfer attempt (with last successful path) and transfer item
         * after a successful transfer.
	 * 
	 * @param transferStatus
	 *            {@link TransferStatus} returned from status callback
	 * @param transferAttempt
	 *            {@link TransferAttempt} that resulted in successful transfer
	 * @throws ConveyorExecutionException
	 */
	TransferItem updateTransferAfterSuccessfulFileTransfer(
			TransferStatus transferStatus, TransferAttempt transferAttempt)
			throws ConveyorExecutionException;
        
        
        /**
	 * Update a transfer due to an error returned in callback from Jargon.
	 * 
         * @param transferStatus
         *            {@link TransferStatus} returned from status callback
	 * @param transferAttempt
	 *            {@link TransferAttempt} that resulted in the transfer error
	 * @throws ConveyorExecutionException
	 */
        TransferItem updateTransferAfterFailedFileTransfer(
            TransferStatus transferStatus,
            TransferAttempt transferAttempt)
            throws ConveyorExecutionException;

        
	/**
	 * Update a transfer due to an error trying to set up and run the transfer
	 * in the conveyor framework. This is distinct from errors that come back to
	 * conveyor based on callbacks from Jargon, and covers errors in managing
	 * the queue or internal databases, or other programming logic or
	 * initialization issues.
	 * 
	 * @param transferAttempt
	 *            {@link TransferAttempt} that resulted in the error
	 * @param exception
	 *            <code>Exception</code> that occurred
	 * @throws ConveyorExecutionException
	 */
	void updateTransferAttemptWithConveyorException(
			final TransferAttempt transferAttempt, final Exception exception)
			throws ConveyorExecutionException;

}
