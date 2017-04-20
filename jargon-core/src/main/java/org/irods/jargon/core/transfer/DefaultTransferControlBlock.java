package org.irods.jargon.core.transfer;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.TransferOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default transfer control block that will by default return true for every
 * filter operation. This control block is used to communicate between a process
 * requesting a transfer, and the actual transfer process. When recursively
 * doing operations such as a put or a get, there is a need to handle restarts,
 * and this object contains a method that can be overridden to filter which
 * files need to be transferred.
 * <p/>
 * This class also contains a shared value that can be used to set a cancel in a
 * recursive transfer operation.
 * <p/>
 * This implementation will cancel a transfer if the current errors exceeds the
 * maximum error threshold.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class DefaultTransferControlBlock implements TransferControlBlock {

	private static final Logger log = LoggerFactory
			.getLogger(DefaultTransferControlBlock.class);

	private String restartAbsolutePath = "";
	private boolean cancelled = false;
	private boolean restartHit = false;
	private boolean paused = false;
	private int maximumErrorsBeforeCanceling = MAX_ERROR_DEFAULT;
	private int errorCount = 0;
	private int totalFilesSkippedSoFar = 0;
	private int totalFilesToTransfer = 0;
	private int totalFilesTransferredSoFar = 0;
	/**
	 * Options to control transfer behavior, may be left null, in which case
	 * defaults will be used. Specifying here overrides the default settings for
	 * this transfer
	 */
	private TransferOptions transferOptions = null;
	private long totalBytesTransferredSoFar = 0L;
	private long totalBytesToTransfer = 0L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.TransferControlBlock#resetTransferData()
	 */
	@Override
	public synchronized void resetTransferData() {
		restartHit = false;
		restartAbsolutePath = "";
		errorCount = 0;
		totalFilesToTransfer = 0;
		totalFilesTransferredSoFar = 0;
		totalBytesTransferredSoFar = 0;
		totalBytesToTransfer = 0;
	}

	/**
	 * Initializer that takes a restart path. This will be ignored if blank or
	 * null.
	 *
	 * @param restartAbsolutePath
	 *            <code>String</code> with a restart path. This may be set to
	 *            blank or null if restarts are not desired.
	 * @param maxErrorsBeforeCancelling
	 *            <code>int</code> with the maximum errors to tolerate before
	 *            transfer is canceled. A value of -1 indicates that errors will
	 *            be ignored.
	 * @return instance of <code>DefaultTransferControlBlock</code>
	 * @throws JargonException
	 */
	public final static TransferControlBlock instance(
			final String restartAbsolutePath,
			final int maxErrorsBeforeCancelling) throws JargonException {
		return new DefaultTransferControlBlock(restartAbsolutePath,
				maxErrorsBeforeCancelling);
	}

	/**
	 * Initializer that takes a restart path, and a max errors before cancel.
	 * The restart path will be ignored if blank or null.
	 *
	 * @param restartAbsolutePath
	 *            <code>String</code> with a restart path. This may be set to
	 *            blank or null if restarts are not desired.
	 * @return instance of <code>DefaultTransferControlBlock</code>
	 * @throws JargonException
	 */
	public final static TransferControlBlock instance(
			final String restartAbsolutePath) throws JargonException {
		return new DefaultTransferControlBlock(restartAbsolutePath,
				MAX_ERROR_DEFAULT);
	}

	/**
	 * Initializer that will have no restart path.
	 *
	 * @return {@link TransferControlBlock}
	 * @throws JargonException
	 */
	public final static TransferControlBlock instance() throws JargonException {
		return new DefaultTransferControlBlock(null, MAX_ERROR_DEFAULT);
	}

	private DefaultTransferControlBlock(final String restartAbsolutePath,
			final int maximumErrorsBeforeCancelling) throws JargonException {

		if (maximumErrorsBeforeCancelling < -1) {
			throw new JargonException(
					"maximumErrorsBeforeCancelling must be >= -1");
		}
		maximumErrorsBeforeCanceling = maximumErrorsBeforeCancelling;
		this.restartAbsolutePath = restartAbsolutePath;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.TransferControlBlock#filter(java.lang.
	 * String)
	 */
	@Override
	public synchronized boolean filter(final String absolutePath)
			throws JargonException {

		/*
		 * this simple filter looks for a match on the restart value (last good
		 * file). When it hits this file, then any subsequent files are
		 * transmitted.
		 */

		log.info("filtering: {}", absolutePath);

		if (restartAbsolutePath == null || restartAbsolutePath.isEmpty()) {
			log.info("no filter");
			return true;
		}

		if (restartHit) {
			log.info("filter passes");
			return true;
		}

		if (absolutePath.equals(restartAbsolutePath)) {
			log.info("hit the restart path");
			restartHit = true;
			// will be true for the next file
			return false;
		}

		return false;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.transfer.TransferControlBlock#isCancelled()
	 */
	@Override
	public boolean isCancelled() {
		synchronized (this) {
			return cancelled;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.TransferControlBlock#setCancelled(boolean)
	 */
	@Override
	public void setCancelled(final boolean cancelled) {
		synchronized (this) {
			this.cancelled = cancelled;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.transfer.TransferControlBlock#isPaused()
	 */
	@Override
	public boolean isPaused() {
		synchronized (this) {
			return paused;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.TransferControlBlock#setPaused(boolean)
	 */
	@Override
	public void setPaused(final boolean paused) {
		synchronized (this) {
			this.paused = paused;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.irods.jargon.core.transfer.TransferControlBlock#
	 * getMaximumErrorsBeforeCanceling()
	 */
	@Override
	public int getMaximumErrorsBeforeCanceling() {
		synchronized (this) {
			return maximumErrorsBeforeCanceling;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.irods.jargon.core.transfer.TransferControlBlock#
	 * setMaximumErrorsBeforeCanceling(int)
	 */
	@Override
	public void setMaximumErrorsBeforeCanceling(
			final int maximumErrorsBeforeCanceling) throws JargonException {
		if (maximumErrorsBeforeCanceling < -1) {
			throw new JargonException(
					"maximumErrorsBeforeCancelling must be >= -1");
		}

		synchronized (this) {
			this.maximumErrorsBeforeCanceling = maximumErrorsBeforeCanceling;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.transfer.TransferControlBlock#getErrorCount()
	 */
	@Override
	public int getErrorCount() {
		synchronized (this) {
			return errorCount;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.TransferControlBlock#reportErrorInTransfer
	 * ()
	 */
	@Override
	public void reportErrorInTransfer() {
		synchronized (this) {
			errorCount++;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.irods.jargon.core.transfer.TransferControlBlock#
	 * reportErrorAndSeeIfTransferNeedsToBeCancelled()
	 */
	@Override
	public boolean shouldTransferBeAbandonedDueToNumberOfErrors() {
		boolean cancelForErrors = false;
		synchronized (this) {
			if (maximumErrorsBeforeCanceling > 0) {
				cancelForErrors = (errorCount >= maximumErrorsBeforeCanceling);
			}
		}
		return cancelForErrors;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.TransferControlBlock#getTotalFilesToTransfer
	 * ()
	 */
	@Override
	public int getTotalFilesToTransfer() {
		synchronized (this) {
			if (shouldTransferBeAbandonedDueToNumberOfErrors()) {
				log.warn("cancelling transfer due to error threshold");
				setCancelled(true);
			}
			return totalFilesToTransfer;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.TransferControlBlock#setTotalFilesToTransfer
	 * (int)
	 */
	@Override
	public void setTotalFilesToTransfer(final int totalFilesToTransfer) {
		synchronized (this) {
			this.totalFilesToTransfer = totalFilesToTransfer;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.irods.jargon.core.transfer.TransferControlBlock#
	 * getTotalFilesTransferredSoFar()
	 */
	@Override
	public int getTotalFilesTransferredSoFar() {
		synchronized (this) {
			return totalFilesTransferredSoFar;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.irods.jargon.core.transfer.TransferControlBlock#
	 * incrementFilesTransferredSoFar()
	 */
	@Override
	public int incrementFilesTransferredSoFar() {
		synchronized (this) {
			return ++totalFilesTransferredSoFar;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.transfer.TransferControlBlock#
	 * incrementFilesSkippedSoFar()
	 */
	@Override
	public int incrementFilesSkippedSoFar() {
		synchronized (this) {
			totalFilesTransferredSoFar++;
			return ++totalFilesSkippedSoFar;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.TransferControlBlock#getTransferOptions()
	 */
	@Override
	public synchronized TransferOptions getTransferOptions() {
		return transferOptions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.TransferControlBlock#setTransferOptions
	 * (org.irods.jargon.core.packinstr.TransferOptions)
	 */
	@Override
	public synchronized void setTransferOptions(
			final TransferOptions transferOptions) {
		this.transferOptions = transferOptions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.transfer.TransferControlBlock#
	 * getTotalBytesTransferredSoFar()
	 */
	@Override
	public synchronized long getTotalBytesTransferredSoFar() {
		return totalBytesTransferredSoFar;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.transfer.TransferControlBlock#
	 * incrementTotalBytesTransferredSoFar(long)
	 */
	@Override
	public synchronized void incrementTotalBytesTransferredSoFar(
			final long totalBytesTransferredSoFar) {
		this.totalBytesTransferredSoFar += totalBytesTransferredSoFar;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.TransferControlBlock#getTotalBytesToTransfer
	 * ()
	 */
	@Override
	public synchronized long getTotalBytesToTransfer() {
		return totalBytesToTransfer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.TransferControlBlock#setTotalBytesToTransfer
	 * (long)
	 */
	@Override
	public synchronized void setTotalBytesToTransfer(
			final long totalBytesToTransfer) {
		this.totalBytesToTransfer = totalBytesToTransfer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.TransferControlBlock#getRestartAbsolutePath
	 * ()
	 */
	@Override
	public synchronized String getRestartAbsolutePath() {
		return restartAbsolutePath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.TransferControlBlock#setRestartAbsolutePath
	 * (java.lang.String)
	 */
	@Override
	public void setRestartAbsolutePath(final String restartAbsolutePath) {
		if (restartAbsolutePath == null) {
			throw new IllegalArgumentException(
					"null restartAbsolutePath, set to blank if not required");
		}
		synchronized (this) {
			this.restartAbsolutePath = restartAbsolutePath;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.TransferControlBlock#getTotalFilesSkippedSoFar
	 * ()
	 */
	@Override
	public synchronized int getTotalFilesSkippedSoFar() {
		return totalFilesSkippedSoFar;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.TransferControlBlock#setTotalFilesSkippedSoFar
	 * (int)
	 */
	@Override
	public synchronized void setTotalFilesSkippedSoFar(
			final int totalFilesSkippedSoFar) {
		this.totalFilesSkippedSoFar = totalFilesSkippedSoFar;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.transfer.TransferControlBlock#
	 * getActualFilesTransferredWithoutSkippedSoFar()
	 */
	@Override
	public int getActualFilesTransferredWithoutSkippedSoFar() {
		return totalFilesTransferredSoFar - totalFilesSkippedSoFar;
	}

}
