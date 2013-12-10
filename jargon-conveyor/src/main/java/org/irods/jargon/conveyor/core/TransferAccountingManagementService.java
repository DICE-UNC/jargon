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

	public static final String WARNING_SOME_FAILED_MESSAGE = "Success, but some file transfers may have failed, please check the transfer details";
	public static final String WARNING_NO_FILES_TRANSFERRED_MESSAGE = "Success, but no files were found to transfer";
	public static final String WARNING_CANCELLED_MESSAGE = "Transfer was cancelled";
	public static final String ERROR_SOME_FAILED_MESSAGE = "Failure, too many file transfers have failed, please check the transfer details";
	public static final String ERROR_ATTEMPTING_TO_RUN = "An error occurred while attempting to create and invoke the transfer process";
	public static final String ERROR_IN_TRANSFER_AT_IRODS_LEVEL = "An error during the transfer process at the client or in iRODS";

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
	 * @param totalFileErrorsSoFar
	 *            <code>int</code> with the total number of errors that have
	 *            occurred so far
	 * @throws ConveyorExecutionException
	 */
	void updateTransferAfterFailedFileTransfer(TransferStatus transferStatus,
			TransferAttempt transferAttempt, final int totalFileErrorsSoFar)
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
	 * Make necessary updates to the given transfer upon cancellation. This sets
	 * the overall status and status of the attempt.
	 * 
	 * @param transferAttempt
	 *            {@link TransferAttempt}
	 * @throws ConveyorExecutionException
	 */
	void updateTransferAfterCancellation(TransferAttempt transferAttempt)
			throws ConveyorExecutionException;

	/**
	 * Make necessary updates to the given transfer upon overall completion with
	 * a warning status (all files or operations involved are complete, but some
	 * were in error at a level below the warning threshold). This sets the
	 * overall status and status of the attempt.
	 * 
	 * 
	 * @param transferStatus
	 *            {@link TransferStatus} from the callback
	 * @param transferAttempt
	 *            {@link TransferAttempt}
	 * @throws ConveyorExecutionException
	 */
	void updateTransferAfterOverallWarningByFileErrorThreshold(
			TransferStatus transferStatus, TransferAttempt transferAttempt)
			throws ConveyorExecutionException;

	/**
	 * Make necessary updates to the given transfer upon overall completion with
	 * a warning status due to the fact that no files were found to transfer
	 * 
	 * @param transferStatus
	 *            {@link TransferStatus} from the callback
	 * @param transferAttempt
	 *            {@link TransferAttempt}
	 * @throws ConveyorExecutionException
	 */
	void updateTransferAfterOverallWarningNoFilesTransferred(
			TransferStatus transferStatus, TransferAttempt transferAttempt)
			throws ConveyorExecutionException;

	/**
	 * Make necessary updates to the given transfer upon overall completion with
	 * a failure status (all files or operations involved are complete, but some
	 * were in error at a level above the warning threshold). This sets the
	 * overall status and status of the attempt.
	 * 
	 * 
	 * 
	 * @param transferStatus
	 *            {@link TransferStatus} from the callback
	 * @param transferAttempt
	 *            {@link TransferAttempt}
	 * @throws ConveyorExecutionException
	 */
	void updateTransferAfterOverallFailureByFileErrorThreshold(
			TransferStatus transferStatus, TransferAttempt transferAttempt)
			throws ConveyorExecutionException;

	/**
	 * Make necessary updates to the given transfer upon overall failure. This
	 * sets the overall status and status of the attempt.
	 * 
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

	/**
	 * Prepare the given transfer for a resubmit. This includes setting up a new
	 * <code>TransferAttempt</code>.
	 * 
	 * @param transferId
	 *            <code>long</code> that is the valid id of a transfer in the
	 *            database
	 * @return {@link Transfer} with proper setup enqueued for resubmit
	 * @throws ConveyorExecutionException
	 * @throws RejectedTransferException
	 */
	Transfer prepareTransferForResubmit(final long transferId)
			throws ConveyorExecutionException, RejectedTransferException;

	/**
	 * Indicates that successful transfers are logged. This delegates to the
	 * configuration service settings.
	 * 
	 * @return <code>boolean</code> that indicates that successful transfer are
	 *         logged
	 * @throws ConveyorExecutionException
	 */
	boolean isLogSuccessfulTransfers() throws ConveyorExecutionException;

}
