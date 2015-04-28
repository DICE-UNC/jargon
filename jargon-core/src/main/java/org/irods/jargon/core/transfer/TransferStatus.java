/**
 *
 */
package org.irods.jargon.core.transfer;

import org.irods.jargon.core.exception.JargonException;

/**
 * Immutable object represents the status of a file transfer (get or put)
 * operation. This can be used for progress reporting.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class TransferStatus {

	public enum TransferType {
		PUT, GET, REPLICATE, COPY, SYNCH
	}

	/***
	 * A transfer state represents tthe status of individual files. There is an
	 * OVERALL_ status that indicates the beginning of an entire transfer, which
	 * is either a file, or a collection of files recursively.
	 * 
	 * @author Mike Conway - DICE (www.irods.org)
	 */
	public enum TransferState {
		IN_PROGRESS_START_FILE, IN_PROGRESS_COMPLETE_FILE, SUCCESS, FAILURE, PAUSED, CANCELLED, RESTARTING, SKIPPING, OVERALL_INITIATION, OVERALL_COMPLETION, SYNCH_INITIALIZATION, SYNCH_DIFF_GENERATION, SYNCH_DIFF_RESOLVE_STEP, SYNCH_COMPLETION
	}

	private final TransferState transferState;
	private final TransferType transferType;
	private final TransferType transferEnclosingType;
	private final String sourceFileAbsolutePath;
	private final String targetFileAbsolutePath;
	private final String transferHost;

	private final String transferZone;
	private final String targetResource;
	private final long totalSize;
	private final long bytesTransfered;
	private final int totalFilesTransferredSoFar;

	/**
	 * Skipped files during restart are counted in total files, so this number
	 * reflects the number of files skipped in the total. Subtract this number
	 * for the total transferred so far to get the actual files transferred in
	 * this transfer attempt.
	 * <p/>
	 * This is done so that any progress bar for a restarted transfer hides the
	 * skipping process and just appears to proceed normally, while apps that
	 * have more sophisticated accounting can do the delta to get a breakout
	 * number
	 * 
	 */
	private final int totalFilesSkippedSoFar;
	private final int totalFilesToTransfer;
	private final Exception transferException;
	private final boolean intraFileStatusReport;

	/**
	 * Create an immutable transfer status object for a complete file or overall
	 * transfer.
	 * 
	 * @param transferType
	 *            <code>TransferType</code> that indicates the type of transfer
	 * @param sourceFileAbsolutePath
	 *            <code>String</code> absolute path to the source file
	 * @param targetFileAbsolutePath
	 *            <code>String</code> absolute path to the target file
	 * @param targetResource
	 *            <code>String</code> with an optional resource, set to blank if
	 *            unused.
	 * @param totalSize
	 *            <code>long</code> with the total size of the file
	 * @param bytesTransfered
	 *            <code>long</code> with the total transferred so far, which is
	 *            some fraction of the total size
	 * @param totalFilesTransferredSoFar
	 *            <code>int<code> with the total files transferred, including this status callback
	 * @param totalFilesSkippedSoFar
	 *            <code>int<code> with the total files skipped if this is a
	 *            restart, including this status callback
	 * @param totalFilesToTransfer
	 *            <code>int</code> with the total files involved in this
	 *            operation
	 * @param transferState
	 *            <code>TransferState</code> indicating whether the transfer is
	 *            ongoing or has completed
	 * @param transferHost
	 *            <code>String</code> with the host name for the transfer
	 * @param transferZone
	 *            <code>String</code> with the zone name for the transfer
	 */
	public static TransferStatus instance(final TransferType transferType,
			final String sourceFileAbsolutePath,
			final String targetFileAbsolutePath, final String targetResource,
			final long totalSize, final long bytesTransfered,
			final int totalFilesTransferredSoFar,
			final int totalFilesSkippedSoFar, final int totalFilesToTransfer,
			final TransferState transferState, final String transferHost,
			final String transferZone) throws JargonException {

		return new TransferStatus(transferType, null, sourceFileAbsolutePath,
				targetFileAbsolutePath, targetResource, totalSize,
				bytesTransfered, totalFilesTransferredSoFar,
				totalFilesSkippedSoFar, totalFilesToTransfer, transferState,
				null, false, transferHost, transferZone);

	}

	/**
	 * Create a callback for a step in a synchronization process.
	 * 
	 * @param transferType
	 *            <code>TransferType</code> that indicates the type of transfer
	 * @param sourceFileAbsolutePath
	 *            <code>String</code> absolute path to the source file
	 * @param targetFileAbsolutePath
	 *            <code>String</code> absolute path to the target file
	 * @param targetResource
	 *            <code>String</code> with an optional resource, set to blank if
	 *            unused.
	 * @param totalSize
	 *            <code>long</code> with the total size of the file
	 * @param bytesTransfered
	 *            <code>long</code> with the total transferred so far, which is
	 *            some fraction of the total size
	 * @param totalFilesTransferredSoFar
	 *            <code>int<code> with the total files transferred, including this status callback
	 * @param totalFilesSkipedSoFar
	 *            <code>int<code> with the total files skipped in restarting,
	 *            including this status callback
	 * @param totalFilesToTransfer
	 *            <code>int</code> with the total files involved in this
	 *            operation
	 * @param transferState
	 *            <code>TransferState</code> indicating whether the transfer is
	 *            ongoing or has completed
	 * @param transferHost
	 *            <code>String</code> with the host name for the transfer
	 * @param transferZone
	 *            <code>String</code> with the zone name for the transfer
	 * @return
	 * @throws JargonException
	 */
	public static TransferStatus instanceForSynch(
			final TransferType transferType,
			final String sourceFileAbsolutePath,
			final String targetFileAbsolutePath, final String targetResource,
			final long totalSize, final long bytesTransfered,
			final int totalFilesTransferredSoFar,
			final int totalFilesSkippedSoFar, final int totalFilesToTransfer,
			final TransferState transferState, final String transferHost,
			final String transferZone) throws JargonException {

		return new TransferStatus(transferType, TransferType.SYNCH,
				sourceFileAbsolutePath, targetFileAbsolutePath, targetResource,
				totalSize, bytesTransfered, totalFilesTransferredSoFar,
				totalFilesSkippedSoFar, totalFilesToTransfer, transferState,
				null, false, transferHost, transferZone);

	}

	/**
	 * Create an immutable transfer status object for a partial transfer of a
	 * file. This status object represents partial progress within a file.
	 * 
	 * @param transferType
	 *            <code>TransferType</code> that indicates the type of transfer
	 * @param sourceFileAbsolutePath
	 *            <code>String</code> absolute path to the source file
	 * @param targetFileAbsolutePath
	 *            <code>String</code> absolute path to the target file
	 * @param targetResource
	 *            <code>String</code> with an optional resource, set to blank if
	 *            unused.
	 * @param totalSize
	 *            <code>long</code> with the total size of the file
	 * @param bytesTransfered
	 *            <code>long</code> with the total transferred so far, which is
	 *            some fraction of the total size
	 * @param totalFilesTransferredSoFar
	 *            <code>int<code> with the total files transferred, including this status callback
	 * @param totalFilesToTransfer
	 *            <code>int</code> with the total files involved in this
	 *            operation * @param transferHost <code>String</code> with the
	 *            host name for the transfer
	 * @param transferZone
	 *            <code>String</code> with the zone name for the transfer
	 * 
	 */
	public static TransferStatus instanceForIntraFileStatus(
			final TransferType transferType, final long totalSize,
			final long bytesTransfered) throws JargonException {

		return new TransferStatus(transferType, null, "", "", "", totalSize,
				bytesTransfered, 0, 0, 0, TransferState.IN_PROGRESS_START_FILE,
				null, true, "", "");
	}

	/**
	 * Create an immutable transfer status object including an exception
	 * 
	 * @param transferType
	 *            <code>TransferType</code> that indicates the type of transfer
	 * @param sourceFileAbsolutePath
	 *            <code>String</code> absolute path to the source file
	 * @param targetFileAbsolutePath
	 *            <code>String</code> absolute path to the target file, set to
	 *            blank if unused
	 * @param targetResource
	 *            <code>String</code> with an optional resource, set to blank if
	 *            unused.
	 * @param totalSize
	 *            <code>long</code> with the total size of the file
	 * @param bytesTransfered
	 *            <code>long</code> with the total transferred so far, which is
	 *            some fraction of the total size
	 * @param totalFilesTransferredSoFar
	 *            <code>int<code> with the total files transferred, including this status callback
	 * @param totalFilesSkippedSoFar
	 *            <code>int<code> with the total files skipped in restarting,
	 *            including this status callback
	 * @param totalFilesToTransfer
	 *            <code>int</code> with the total files involved in this
	 *            operation
	 * @param exception
	 *            <code>TransferState</code> indicating whether the transfer is
	 *            ongoing or has completed
	 * @param transferHost
	 *            <code>String</code> with the host name for the transfer
	 * @param transferZone
	 *            <code>String</code> with the zone name for the transfer
	 */

	public static TransferStatus instanceForException(
			final TransferType transferType,
			final String sourceFileAbsolutePath,
			final String targetFileAbsolutePath, final String targetResource,
			final long totalSize, final long bytesTransfered,
			final int totalFilesTransferredSoFar,
			final int totalFilesSkippedSoFar, final int totalFilesToTransfer,
			final Exception exception, final String transferHost,
			final String transferZone) throws JargonException {

		return new TransferStatus(transferType, null, sourceFileAbsolutePath,
				targetFileAbsolutePath, targetResource, totalSize,
				bytesTransfered, totalFilesTransferredSoFar,
				totalFilesSkippedSoFar, totalFilesToTransfer,
				TransferState.FAILURE, exception, false, transferHost,
				transferZone);

	}

	/**
	 * Create an instance of a status call-back for an exception during a
	 * synchronization process
	 * 
	 * @param transferType
	 * @param sourceFileAbsolutePath
	 * @param targetFileAbsolutePath
	 * @param targetResource
	 * @param totalSize
	 * @param bytesTransfered
	 * @param totalFilesTransferredSoFar
	 * @param totalFileSkippedSoFar
	 * @param totalFilesToTransfer
	 * @param exception
	 * @param transferHost
	 *            <code>String</code> with the host name for the transfer
	 * @param transferZone
	 *            <code>String</code> with the zone name for the transfer
	 * @return
	 * @throws JargonException
	 */
	public static TransferStatus instanceForExceptionForSynch(
			final TransferType transferType,
			final String sourceFileAbsolutePath,
			final String targetFileAbsolutePath, final String targetResource,
			final long totalSize, final long bytesTransfered,
			final int totalFilesTransferredSoFar,
			final int totalFilesSkippedSoFar, final int totalFilesToTransfer,
			final Exception exception, final String transferHost,
			final String transferZone) throws JargonException {

		return new TransferStatus(transferType, TransferType.SYNCH,
				sourceFileAbsolutePath, targetFileAbsolutePath, targetResource,
				totalSize, bytesTransfered, totalFilesTransferredSoFar,
				totalFilesSkippedSoFar, totalFilesToTransfer,
				TransferState.FAILURE, exception, false, transferHost,
				transferZone);

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("transfer status:");
		sb.append("\n   transferState:");
		sb.append(transferState);
		sb.append("\n   transferType:");
		sb.append(transferType);

		if (transferEnclosingType != null) {
			sb.append("\n  enclosed by transfer type:");
			sb.append(transferEnclosingType);
		}

		sb.append("\n   sourceFileAbsolutePath:");
		sb.append(sourceFileAbsolutePath);
		sb.append("\n   targetFileAbsolutePath:");
		sb.append(targetFileAbsolutePath);
		sb.append("\n   targetResource:");
		sb.append(targetResource);
		sb.append("\n   totalSize:");
		sb.append(totalSize);
		sb.append("\n   bytesTransferred:");
		sb.append(bytesTransfered);
		sb.append("\n   totalFilesTransferredSoFar:");
		sb.append(totalFilesTransferredSoFar);
		sb.append("\n   totalFilesSkippedSoFar:");
		sb.append(totalFilesSkippedSoFar);
		sb.append("\n   totalFilesToTransfer:");
		sb.append(totalFilesToTransfer);
		sb.append("\n   transferException:");
		sb.append(transferException);
		sb.append("\n   intraFileStatusReport");
		sb.append(intraFileStatusReport);
		sb.append("\n   transferHost:");
		sb.append(transferHost);
		sb.append("\n   transferZone:");
		sb.append(transferZone);
		return sb.toString();
	}

	/**
	 * Private constructor, use the static instance methods.
	 * 
	 * @param transferType
	 * @param transferEnclosingType
	 * @param sourceFileAbsolutePath
	 * @param targetFileAbsolutePath
	 * @param targetResource
	 * @param totalSize
	 * @param bytesTransferred
	 * @param totalFilesTransferredSoFar
	 * @param totalFilesSkippedSoFar
	 * @param totalFilesToTransfer
	 * @param transferState
	 * @param transferException
	 * @param intraFileStatusReport
	 * @throws JargonException
	 */
	private TransferStatus(final TransferType transferType,
			final TransferType transferEnclosingType,
			final String sourceFileAbsolutePath,
			final String targetFileAbsolutePath, final String targetResource,
			final long totalSize, final long bytesTransferred,
			final int totalFilesTransferredSoFar,
			final int totalFilesSkippedSoFar, final int totalFilesToTransfer,
			final TransferState transferState,
			final Exception transferException,
			final boolean intraFileStatusReport, final String transferHost,
			final String transferZone) throws JargonException {

		if (totalSize < 0) {
			throw new JargonException("totalSize less than zero");
		}

		if (bytesTransferred < 0) {
			throw new JargonException("bytesTransferred is less than zero");
		}

		if (!intraFileStatusReport) {

			if (totalFilesTransferredSoFar < 0) {
				throw new JargonException(
						"totalFilesTransferredSoFar is less than zero");
			}

			if (totalFilesToTransfer < 0) {
				throw new JargonException(
						"totalFilesToTransfer is less than zero");
			}

			if (transferType == null) {
				throw new JargonException("null transfer type");
			}

			if (sourceFileAbsolutePath == null
					|| sourceFileAbsolutePath.isEmpty()) {
				throw new JargonException(
						"null or empty sourceFileAbsolutePath");
			}

			if (targetFileAbsolutePath == null) {
				throw new JargonException("null  targetFileAbsolutePath");
			}

			if (targetResource == null) {
				throw new JargonException(
						"null targetResource, set to blank if unused");
			}

			if (transferState == null) {
				throw new JargonException("null transferState");
			}

			if (transferHost == null || transferHost.isEmpty()) {
				throw new JargonException("null transferHost");
			}

			if (transferZone == null || transferZone.isEmpty()) {
				throw new JargonException("null transferZone");
			}
		}

		this.transferType = transferType;
		this.transferEnclosingType = transferEnclosingType;
		this.sourceFileAbsolutePath = sourceFileAbsolutePath;
		this.targetFileAbsolutePath = targetFileAbsolutePath;
		this.targetResource = targetResource;
		this.totalSize = totalSize;
		bytesTransfered = bytesTransferred;
		this.transferState = transferState;
		this.transferException = transferException;
		this.totalFilesToTransfer = totalFilesToTransfer;
		this.totalFilesTransferredSoFar = totalFilesTransferredSoFar;
		this.totalFilesSkippedSoFar = totalFilesSkippedSoFar;
		this.intraFileStatusReport = intraFileStatusReport;
		this.transferHost = transferHost;
		this.transferZone = transferZone;

	}

	public final TransferType getTransferType() {
		return transferType;
	}

	public final String getSourceFileAbsolutePath() {
		return sourceFileAbsolutePath;
	}

	public final String getTargetFileAbsolutePath() {
		return targetFileAbsolutePath;
	}

	public final long getTotalSize() {
		return totalSize;
	}

	public final long getBytesTransfered() {
		return bytesTransfered;
	}

	public final TransferState getTransferState() {
		return transferState;
	}

	public final Exception getTransferException() {
		return transferException;
	}

	public String getTargetResource() {
		return targetResource;
	}

	public int getTotalFilesToTransfer() {
		return totalFilesToTransfer;
	}

	public int getTotalFilesTransferredSoFar() {
		return totalFilesTransferredSoFar;
	}

	public boolean isIntraFileStatusReport() {
		return intraFileStatusReport;
	}

	public TransferType getTransferEnclosingType() {
		return transferEnclosingType;
	}

	/**
	 * @return the transferHost
	 */
	public String getTransferHost() {
		return transferHost;
	}

	/**
	 * @return the transferZone
	 */
	public String getTransferZone() {
		return transferZone;
	}

	/**
	 * @return the totalFilesSkippedSoFar
	 */
	public int getTotalFilesSkippedSoFar() {
		return totalFilesSkippedSoFar;
	}

}
