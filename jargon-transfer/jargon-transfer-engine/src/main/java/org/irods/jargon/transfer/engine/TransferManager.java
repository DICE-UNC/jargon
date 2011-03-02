package org.irods.jargon.transfer.engine;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransferItem;

/**
 * Interface for a simple queue manager that can manage transfers to iRODS
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

	/**
	 * Get the running status of the tranfer queue (e.g. is it idle or busy).
	 * @return {@link RunningStatus} with the current queue status
	 */
	RunningStatus getRunningStatus();

	/**
	 * Is the queue currently paused?
	 * @return <code>boolean</code> that is true if paused
	 */
	boolean isPaused();

	/**
	 * Set the queue to a paused state.  
	 * @throws JargonException
	 */
	void pause() throws JargonException;

	/**
	 * Resume a paused queue
	 * @throws JargonException
	 */
	void resume() throws JargonException;

	/**
	 * Get the error status of the queue.  The error status reflects the current or last transfer, and will be overridden when the
	 * queue is restarted.
	 * @return {@link ErrorStatus}
	 */
	ErrorStatus getErrorStatus();

	/**
	 * Purge all transfers in the queue
	 * @throws JargonException
	 */
	void purgeAllTransfers() throws JargonException;

	/**
	 * Purge all successful transfers that are in the queue
	 * @throws JargonException
	 */
	void purgeSuccessfulTransfers() throws JargonException;

	/**
	 * Put a file from local to iRODS
	 * @param sourceAbsolutePath <code>String</code> with the absolute path to the local file to be transferred to iRODS
	 * @param targetAbsolutePath <code>String</code> with the absolute path to the target file in iRODS
	 * @param resource <code>String</code> with the optional resource to specifiy as the transfer target
	 * @param irodsAccount <code>IRODSAccount</code> that describes the target iRODS and user identity
	 * @throws JargonException
	 */
	void enqueueAPut(final String sourceAbsolutePath,
			final String targetAbsolutePath, final String resource,
			final IRODSAccount irodsAccount) throws JargonException;

	/**
	 * Get a file from iRODS
	 * @param sourceAbsolutePath <code>String</code> with the absolute path to the iRODS file to be transferred to the local file system
	 * @param targetAbsolutePath <code>String</code> with the absolute path to the target file in the local file system
	 * @param resource <code>String</code> with the optional resource to specifiy as the transfer target
	 * @param irodsAccount <code>IRODSAccount</code> that describes the target iRODS and user identity
	 * @throws JargonException
	 */
	void enqueueAGet(final String irodsSourceAbsolutePath,
			final String targetLocalAbsolutePath, final String resource,
			final IRODSAccount irodsAccount) throws JargonException;

	/**
	 * Replicate iRODS files
	 * @param irodsAbsolutePath <code>String</code> with the iRODS file to replicate by absolute path
	 * @param targetResource <code>String</code> with the resource to which the file will replicat
	 * @param irodsAccount <code>IRODSAccount</code> describing the user and zone
	 * @throws JargonException
	 */
	 void enqueueAReplicate(final String irodsAbsolutePath,
			final String targetResource, final IRODSAccount irodsAccount)
			throws JargonException;

	 /**
	  * Signal to check the queue and process if there's anything there
	  * @throws JargonException
	  */
	void processNextInQueueIfIdle() throws JargonException;

	/**
	 * Get a view of the current queue, which includes enqueued, processing
	 * @return <code>List</code> of {@link LocalIRODSTransfer} that reflects the current queue
	 * @throws JargonException
	 */
	 List<LocalIRODSTransfer> getCurrentQueue()
			throws JargonException;

	/**
	 * Get a view of the current queue, which includes enqueued, processing, and recently completed
	 * @return <code>List</code> of {@link LocalIRODSTransfer} that reflects the recent queue
	 * @throws JargonException
	 */
	 List<LocalIRODSTransfer> getRecentQueue()
			throws JargonException;

	 /**
		 * Get a view of the errors in the queue
		 * @return <code>List</code> of {@link LocalIRODSTransfer} that reflects the errors in the queue
		 * @throws JargonException
		 */
	List<LocalIRODSTransfer> getErrorQueue()
			throws JargonException;

	/**
	 * Get a view of the warning transfers in the queue
	 * @return <code>List</code> of {@link LocalIRODSTransfer} that reflects the warnings in the queue
	 * @throws JargonException
	 */
	List<LocalIRODSTransfer> getWarningQueue()
			throws JargonException;

	/**
	 * Get a reference to the registered callback listener
	 * @return {@link TransferManagerCallbackListener}
	 */
	 TransferManagerCallbackListener getTransferManagerCallbackListener();

	 /**
	  * Display a view of all items in a given transfer
	  * @param localIRODSTransferId
	  * @return
	  * @throws JargonException
	  */
	List<LocalIRODSTransferItem> getAllTransferItemsForTransfer(
			final Long localIRODSTransferId) throws JargonException;

	/**
	 * Get all errors associated with a given transfer
	 * @param localIRODSTransferId
	 * @return
	 * @throws JargonException
	 */
	 List<LocalIRODSTransferItem> getErrorTransferItemsForTransfer(
			final Long localIRODSTransferId) throws JargonException;

	 /**
	  * Restart the indicated transfer.  This will restart at the last good file.
	  * @param localIRODSTransfer
	  * @throws JargonException
	  */
	void restartTransfer(
			final LocalIRODSTransfer localIRODSTransfer) throws JargonException;

	/**
	 * Restart a transfer, beginning at the first file.
	 * @param localIRODSTransfer
	 * @throws JargonException
	 */
	void resubmitTransfer(
			final LocalIRODSTransfer localIRODSTransfer) throws JargonException;

	/**
	 * Cancel a running transfer at the first available time.
	 * @param localIRODSTransfer
	 * @throws JargonException
	 */
	void cancelTransfer(
			final LocalIRODSTransfer localIRODSTransfer) throws JargonException;

	/**
	 * Indicate whether detail items are turned on
	 * @return
	 */
	boolean isLogSuccessfulTransfers();

	/**
	 * Indicate whether successful transfer item details are recorded.
	 * @param logSuccessfulTransfers
	 */
	void setLogSuccessfulTransfers(
			final boolean logSuccessfulTransfers);

	/**
	 * Get a reference to a service that can manipulate the underlying queue.
	 * @return
	 */
	TransferQueueService getTransferQueueService();

    public void resetStatus() throws JargonException;

}