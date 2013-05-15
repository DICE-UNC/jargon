/**
 * 
 */
package org.irods.jargon.conveyor.core;

import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;

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
	void updateTransferAfterSuccessfulFileTransfer(
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
	void updateTransferAfterFailedFileTransfer(TransferStatus transferStatus,
			TransferAttempt transferAttempt) throws ConveyorExecutionException;

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

	/**
	 * Make necessary updates to the given transfer upon overall success (all
	 * files or operations involved are complete). This sets the overall status
	 * and status of the attempt.
	 * 
	 * @param transferStatus
	 *            {@link TransferStatus} from the callback
	 * @param transferAttempt
	 *            {@link TransferAttempt}
	 * @throws ConveyorExecutionException
	 */
	void updateTransferAfterOverallSuccess(
			org.irods.jargon.core.transfer.TransferStatus transferStatus,
			TransferAttempt transferAttempt) throws ConveyorExecutionException;

	/**
	 * Make necessary updates to the given transfer upon overall failure. This
	 * sets the overall status and status of the attempt.
	 * 
	 * @param transferStatus
	 *            {@link TransferStatus} from the callback
	 * @param transferAttempt
	 *            {@link TransferAttempt}
	 * @throws ConveyorExecutionException
	 */
	void updateTransferAfterOverallFailure(
			org.irods.jargon.core.transfer.TransferStatus transferStatus,
			TransferAttempt transferAttempt) throws ConveyorExecutionException;

	/**
	 * Make the necessary updates to the given transfer and transfer item based
	 * on the notification that a file was skipped during a restart process. The
	 * item may or may not be logged mediated by the 'log successful files' and
	 * 'log restart files' settings, but at any rate the transfer attempt is
	 * updated
	 * 
	 * @param transferStatus
	 *            {@link TransferStatus} from the callback
	 * @param transferAttempt
	 *            {@link TransferAttempt}
	 * @throws ConveyorExecutionException
	 */
	void updateTransferAfterRestartFileSkipped(TransferStatus transferStatus,
			TransferAttempt transferAttempt) throws ConveyorExecutionException;

	/**
	 * Prepare the transfer to be placed into the enqueued state with a transfer
	 * attempt ready to process
	 * 
	 * @param transfer
	 * @return
	 * @throws ConveyorExecutionException
	 */
	TransferAttempt prepareTransferForProcessing(Transfer transfer)
			throws ConveyorExecutionException;

	/**
	 * Prepare the given transfer for a restart. This includes setting up a new
	 * <code>TransferAttempt</code> with the proper restart path.
	 * 
	 * @param transferId
	 *            <code>long</code> that is the valid id of a transfer in the
	 *            database
	 * @return {@link Transfer} with proper setup enqueued for restart
	 * @throws ConveyorExecutionException
	 * @throws RejectedTransferException
	 */
	Transfer prepareTransferForRestart(final long transferId)
			throws ConveyorExecutionException, RejectedTransferException;

}
