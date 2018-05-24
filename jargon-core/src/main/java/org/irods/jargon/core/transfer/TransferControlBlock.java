package org.irods.jargon.core.transfer;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.TransferOptions;

/**
 * An interface for an object that can control a recursive transfer process,
 * providing a common reference object between the transferring process and the
 * recursive transfer method (get, put, replicate, etc).
 * <p>
 * Implementations of this class can act as a filter to select items for
 * transfer, and can also be used to signal a cancel.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface TransferControlBlock {

	public final int MAX_ERROR_DEFAULT = 5;
	public final int MAX_ERROR_IGNORE = -1;

	/**
	 * Indicates whether the given operation should be cancelled. This method must
	 * be synchronized.
	 *
	 * @return {@code boolean} if the operation should be cancelled.
	 */
	boolean isCancelled();

	/**
	 * Indicates that the given operation should be paused. This method must be
	 * synchronized
	 *
	 * @return {@code boolean} if the operation should be paused.
	 */
	boolean isPaused();

	/**
	 * Send a signal to cancel the operation. A transfer operation will check this
	 * and will cancel a recursive transfer if indicated. This method must be
	 * synchronized.
	 *
	 * @param cancelled
	 *            {@code boolean} that will be true if the operation must be
	 *            cancelled.
	 */
	void setCancelled(final boolean cancelled);

	/**
	 * Send a signal to pause the operation. A transfer operation will check this
	 * and will pause a recursive transfer if indicated. This method must be
	 * synchronized.
	 *
	 * @param paused
	 *            {@code boolean} that will be true if the operation must be paused.
	 */
	void setPaused(final boolean paused);

	/**
	 * Method to filter the transfer. An absolute path appropriate to the transfer
	 * is given, and a booelan will be returned that indicates whether the file
	 * should be transferred or not. A {@code false} indicates to ignore this file.
	 *
	 * The absolute path depends on the operation. For a put, it would be the local
	 * source file. For a get, it would be the irods file. For a replication, it
	 * would be the irods file.
	 *
	 * @param absolutePath
	 *            {@code String} with the appropriate file path to filter.
	 * @return {@code boolean} with a value of {@code true} if the file should be
	 *         acted upon.
	 * @throws JargonException
	 *             for iRODS error
	 */
	boolean filter(final String absolutePath) throws JargonException;

	/**
	 * Get the maximum number of errors to allow before canceling the transfer
	 *
	 * @return {@code int} with the maximum number of errors before canceling.
	 */
	int getMaximumErrorsBeforeCanceling();

	/**
	 * Set the maximum number of errors to allow before canceling the transfer
	 *
	 * @param maximumErrorsBeforeCancelling
	 *            {@code int} with the maximum number of errors before canceling. -1
	 *            indicates that the number of errors will be ignored
	 * @throws JargonException
	 *             for iRODS error
	 */
	void setMaximumErrorsBeforeCanceling(final int maximumErrorsBeforeCancelling) throws JargonException;

	/**
	 * Get the total number of transfer errors that have occurred in this transfer
	 * so far
	 *
	 * @return {@code int} with the number of errors encountered so far.
	 */
	int getErrorCount();

	/**
	 * Check if enough errors have been accumulated to cause the transfer to be
	 * abandoned.
	 *
	 * @return {@code boolean} that indicates whether reporting this error requires
	 *         that the transfer be cancelled.
	 */
	boolean shouldTransferBeAbandonedDueToNumberOfErrors();

	/**
	 * Indicate an error in the transfer. This method will increment the error
	 * counter in the {@code TransferControlBlock}.
	 */
	void reportErrorInTransfer();

	/**
	 * Gets the total number of files to be transferred. This is initialized
	 * automatically if a callback listener has been added.
	 *
	 * @return {@code int}
	 *
	 */
	int getTotalFilesToTransfer();

	/**
	 * Set the total number of files to be transferred. This is initialized
	 * automatically if a callback listener has been added.
	 *
	 * @param totalFilesToTransfer
	 *            {@code int}
	 */
	void setTotalFilesToTransfer(int totalFilesToTransfer);

	/**
	 * Get a running total of the files transferred so far. This is initialized
	 * automatically if a callback listener has been added.
	 *
	 * @return {@code int}
	 */
	int getTotalFilesTransferredSoFar();

	/**
	 * Get the total of files transferred, minus any files transferred by skipping
	 *
	 * @return {@code int}
	 */
	int getActualFilesTransferredWithoutSkippedSoFar();

	/**
	 * Increment the count of files that have been transferred so far and return
	 * that amount (to avoid act-then-check)
	 *
	 * @return incremented value from control block
	 */
	int incrementFilesTransferredSoFar();

	/**
	 * Increment the count of files that have been skipped so far in restarting and
	 * return that amount (to avoid act-then-check). This simultaneously increments
	 * the total files too.
	 *
	 * @return incremented value from control block
	 */
	int incrementFilesSkippedSoFar();

	/**
	 * Get the total number of bytes (for all files) transferred so far
	 *
	 * @return {@code long} with the total number of byte that have been transferred
	 *         for all files
	 */
	long getTotalBytesTransferredSoFar();

	/**
	 * Add the given number of bytes to the running total of all bytes transferred
	 *
	 * @param totalBytesTransferredSoFar
	 *            {@code long} to add to the running total of bytes
	 */
	void incrementTotalBytesTransferredSoFar(long totalBytesTransferredSoFar);

	/**
	 * Get the total number of bytes to be transferred for all files
	 *
	 * @return {@code long} with the total number of bytes
	 */
	long getTotalBytesToTransfer();

	/**
	 * Set the total bytes that will be for the whole transfer
	 *
	 * @param totalBytesToTransfer
	 *            {@code long} with total bytes to transfer for all files
	 */
	void setTotalBytesToTransfer(long totalBytesToTransfer);

	/**
	 * Set the options that will control the details of the transfer. Note that this
	 * may be set to {@code null}, in which case, defaults will be computed during
	 * the transfer.
	 *
	 * @param transferOptions
	 *            {@link TransferOptions}
	 */
	void setTransferOptions(TransferOptions transferOptions);

	/**
	 * Get the options currently controlling the details of the transfer. These may
	 * be {@code null} if they have not yet been specified.
	 *
	 * @return {@link TransferOptions}
	 */
	TransferOptions getTransferOptions();

	/**
	 * Reset the 'per transfer' fields so that the control block can be used again
	 * with fresh statistics.
	 */
	void resetTransferData();

	/**
	 * Get the (optional) restart location
	 *
	 * @return {@code String} with an optional (blank if not provided) absolute path
	 *         that was the last good path in a prior transfer. This is used to seek
	 *         the restart point
	 */
	String getRestartAbsolutePath();

	/**
	 * Set the (optional) restart location. If not desired, set to blank.
	 *
	 * @param restartAbsolutePath
	 *            {@code String} with an optional restart path, this should be the
	 *            last 'good' path in a prior transfer
	 */
	void setRestartAbsolutePath(String restartAbsolutePath);

	/**
	 * Get the number of files skipped during the restart process
	 *
	 * @return {@code int} with the number of files skipped so far
	 */
	int getTotalFilesSkippedSoFar();

	/**
	 * Set the number of files skipped during processing
	 *
	 * @param totalFilesSkippedSoFar
	 *            {@code int}
	 */
	void setTotalFilesSkippedSoFar(int totalFilesSkippedSoFar);

}
