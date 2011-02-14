package org.irods.jargon.transferengine;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.transferengine.domain.LocalIRODSTransfer;
import org.irods.jargon.transferengine.domain.LocalIRODSTransferItem;

/**
 * Central manager of transfer engine. Manages status of entire engine on behalf
 * of client callers. This class is a singleton, and will keep track of the
 * current status of the transfer, receiving callbacks from the various
 * transfers that are underway. Clients can subscribe to this transfer manager
 * for information about the current operations of the transfer engine and
 * receive information about the status and history of the queue.
 * 
 * This manager also is the interface through which transfers may be enqueued by
 * clients,.
 * 
 * This class might evolve into some sort of scheduler, and these capabilities
 * would be exposed here in cooperation with the
 * <code>TransferQueueService</code>
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface TransferManager {

	public enum ErrorStatus {
		OK, WARNING, ERROR
	}

	public enum RunningStatus {
		IDLE, PROCESSING, PAUSED
	}

	RunningStatus getRunningStatus();

	boolean isPaused();

	/**
	 * Pause the queue.
	 * 
	 * @throws JargonException
	 */
	void pause() throws JargonException;

	/**
	 * Restart the queue after being paused, will pick up the next job in the
	 * queue and run it.
	 * 
	 * @throws JargonException
	 */
	void resume() throws JargonException;

	void notifyWarningCondition() throws JargonException;

	void notifyErrorCondition() throws JargonException;

	void notifyOKCondition() throws JargonException;

	void notifyProcessing() throws JargonException;

	void notifyEnqueued(final LocalIRODSTransfer enqueuedTransfer);

	void notifyComplete() throws JargonException;

	ErrorStatus getErrorStatus();

	/**
	 * Purge all transfers in the queue that are not marked as 'PROCESSING' and
	 * resets the error status
	 * 
	 * @throws JargonException
	 */
	void purgeAllTransfers() throws JargonException;

	/**
	 * Purge all transfers in the queue that are marked as 'COMPLETE' and have
	 * no errors
	 * 
	 * @throws JargonException
	 */
	void purgeSuccessfulTransfers() throws JargonException;

	void enqueueAPut(final String sourceAbsolutePath,
			final String targetAbsolutePath, final String resource,
			final IRODSAccount irodsAccount) throws JargonException;

	void enqueueAGet(final String irodsSourceAbsolutePath,
			final String targetLocalAbsolutePath, final String resource,
			final IRODSAccount irodsAccount) throws JargonException;

	/**
	 * Put a replication operation into the processing queue. This will be
	 * processed according to the schedule of the transfer engine, and may not
	 * be immediate. If the queue is idle, the tranfer will be launched
	 * immediately.
	 * 
	 * @param irodsAbsolutePath
	 *            <code>String</code> with the absolute path to the iRODS file
	 *            or collection to replicate
	 * @param targetResource
	 *            <code>String</code> with the name of the resource to which to
	 *            replicate. Blank will cause iRODS to use configured defaults.
	 *            If such a default is not configured, an iRODS error may occur.
	 * @param irodsAccount
	 *            <code>IRODSAccount</code> with information on the zone to
	 *            which the command will be sent.
	 * @throws JargonException
	 */
	void enqueueAReplicate(final String irodsAbsolutePath,
			final String targetResource, final IRODSAccount irodsAccount)
			throws JargonException;

	/**
	 * This method can be called to trigger processing of the next queued item
	 * using the criteria inside of the transfer manager. This method will check
	 * the idle and pause status of the queue.
	 * 
	 * @throws JargonException
	 */
	void processNextInQueueIfIdle() throws JargonException;

	/**
	 * Get the list of transfers that are currently in the queue available for
	 * processing
	 * 
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.transferengine.domain.LocalIRODSTransfer}
	 * @throws JargonException
	 */
	List<LocalIRODSTransfer> getCurrentQueue() throws JargonException;

	/**
	 * Get the list of transfers that were recently added, including enqueued,
	 * processing, error, etc
	 * 
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.transferengine.domain.LocalIRODSTransfer}
	 * @throws JargonException
	 */
	List<LocalIRODSTransfer> getRecentQueue() throws JargonException;

	/**
	 * Get the list of transfers that were recently added, including enqueued,
	 * processing, error, etc
	 * 
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.transferengine.domain.LocalIRODSTransfer}
	 * @throws JargonException
	 */
	List<LocalIRODSTransfer> getErrorQueue() throws JargonException;

	/**
	 * Get the list of transfers that have a status of wraning
	 * 
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.transferengine.domain.LocalIRODSTransfer}
	 * @throws JargonException
	 */
	List<LocalIRODSTransfer> getWarningQueue() throws JargonException;

	TransferManagerCallbackListener getTransferManagerCallbackListener();

	void setErrorStatus(final ErrorStatus errorStatus);

	void setRunningStatus(final RunningStatus runningStatus);

	/**
	 * This method will reset the error status of the transfer engine if not
	 * currently processing and notify any listeners. This would be done when
	 * the queue is purged so that purged errors are no longer reflected.
	 */
	void resetStatus() throws JargonException;

	/**
	 * Get a list of all of the transfer items for the given transfer. This
	 * particular method will return all transfer items, and other methods exist
	 * to filter for errors and other attributes.
	 * 
	 * @param localIRODSTransferId
	 *            <code>Long</code> with the key of the given transfer
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.transferengine.LocalIRODSTransferItem}
	 *         with query results.
	 * @throws JargonException
	 */
	List<LocalIRODSTransferItem> getAllTransferItemsForTransfer(
			final Long localIRODSTransferId) throws JargonException;

	/**
	 * Get a list of all of the transfer items for the given transfer that are
	 * an error.
	 * 
	 * @param localIRODSTransferId
	 *            <code>Long</code> with the key of the given transfer
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.transferengine.LocalIRODSTransferItem}
	 *         with query results.
	 * @throws JargonException
	 */
	List<LocalIRODSTransferItem> getErrorTransferItemsForTransfer(
			final Long localIRODSTransferId) throws JargonException;

	/**
	 * Restart a transfer, this will consult the last good file and begin the
	 * transfer after that last noted location
	 * 
	 * @param localIRODSTransfer
	 * @throws JargonException
	 */
	void restartTransfer(final LocalIRODSTransfer localIRODSTransfer)
			throws JargonException;

	/**
	 * Resubmit a transfer, this will cause the transfer to be restarted from
	 * the beginning and will clear the status of individually transmitted items
	 * 
	 * @param localIRODSTransfer
	 * @throws JargonException
	 */
	void resubmitTransfer(final LocalIRODSTransfer localIRODSTransfer)
			throws JargonException;

	/**
	 * Mark a transfer as cancelled. Note that a completed transfer will be
	 * untouched.
	 * 
	 * @param localIRODSTransfer
	 * @throws JargonException
	 */
	void cancelTransfer(final LocalIRODSTransfer localIRODSTransfer)
			throws JargonException;

	boolean isLogSuccessfulTransfers();

	void setLogSuccessfulTransfers(final boolean logSuccessfulTransfers);

	TransferQueueService getTransferQueueService();

}