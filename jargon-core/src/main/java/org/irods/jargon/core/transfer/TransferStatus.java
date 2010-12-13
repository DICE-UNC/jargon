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
public final class TransferStatus {

	public enum TransferType {
		PUT, GET, REPLICATE
	}

	public enum TransferState {
		IN_PROGRESS, SUCCESS, FAILURE, PAUSED, CANCELLED
	}

	private final TransferState transferState;
	private final TransferType transferType;
	private final String sourceFileAbsolutePath;
	private final String targetFileAbsolutePath;
	private final String targetResource;
	private final long totalSize;
	private final long bytesTransfered;
	private final int totalFilesTransferredSoFar;
	private int totalFilesToTransfer;
	private final Exception transferException;

	/**
	 * Create an immutable transfer status object
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
	 *            operation
	 * @param transferState
	 *            <code>TransferState</code> indicating whether the transfer is
	 *            ongoing or has completed
	 */
	public static TransferStatus instance(final TransferType transferType,
			final String sourceFileAbsolutePath,
			final String targetFileAbsolutePath, final String targetResource,
			final long totalSize, final long bytesTransfered,
			final int totalFilesTransferredSoFar,
			final int totalFilesToTransfer, final TransferState transferState)
			throws JargonException {

		return new TransferStatus(transferType, sourceFileAbsolutePath,
				targetFileAbsolutePath, targetResource, totalSize,
				bytesTransfered, totalFilesTransferredSoFar,
				totalFilesToTransfer, transferState, null);

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
	 * @param totalFilesToTransfer
	 *            <code>int</code> with the total files involved in this
	 *            operation
	 * @param exception
	 *            <code>TransferState</code> indicating whether the transfer is
	 *            ongoing or has completed
	 */

	public static TransferStatus instanceForException(
			final TransferType transferType,
			final String sourceFileAbsolutePath,
			final String targetFileAbsolutePath, final String targetResource,
			final long totalSize, final long bytesTransfered,
			final int totalFilesTransferredSoFar,
			final int totalFilesToTransfer, final Exception exception)
			throws JargonException {

		return new TransferStatus(transferType, sourceFileAbsolutePath,
				targetFileAbsolutePath, targetResource, totalSize,
				bytesTransfered, totalFilesTransferredSoFar,
				totalFilesToTransfer, TransferState.FAILURE, exception);

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("transfer status:");
		sb.append("\n   transferState:");
		sb.append(transferState);
		sb.append("\n   transferType:");
		sb.append(transferType);
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
		sb.append("\n   totalFilesToTransfer:");
		sb.append(totalFilesToTransfer);
		sb.append("\n   transferException:");
		sb.append(transferException);
		return sb.toString();
	}

	/**
	 * @param transferType
	 * @param sourceFileAbsolutePath
	 * @param targetFileAbsolutePath
	 * @param targetResource
	 * @param totalSize
	 * @param bytesTransferred
	 * @param totalFilesTransferredSoFar
	 * @param totalFilesToTransfer
	 * @param transferState
	 * @param transferException
	 * @throws JargonException
	 */
	private TransferStatus(final TransferType transferType,
			final String sourceFileAbsolutePath,
			final String targetFileAbsolutePath, final String targetResource,
			final long totalSize, final long bytesTransferred,
			final int totalFilesTransferredSoFar,
			final int totalFilesToTransfer, final TransferState transferState,
			final Exception transferException) throws JargonException {

		if (totalSize < 0) {
			throw new JargonException("totalSize less than zero");
		}

		if (bytesTransferred < 0) {
			throw new JargonException("bytesTransferred is less than zero");
		}

		if (totalFilesTransferredSoFar < 0) {
			throw new JargonException(
					"totalFilesTransferredSoFar is less than zero");
		}

		if (totalFilesToTransfer < 0) {
			throw new JargonException("totalFilesToTransfer is less than zero");
		}

		if (transferType == null) {
			throw new JargonException("null transfer type");
		}

		if (sourceFileAbsolutePath == null || sourceFileAbsolutePath.isEmpty()) {
			throw new JargonException("null or empty sourceFileAbsolutePath");
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

		this.transferType = transferType;
		this.sourceFileAbsolutePath = sourceFileAbsolutePath;
		this.targetFileAbsolutePath = targetFileAbsolutePath;
		this.targetResource = targetResource;
		this.totalSize = totalSize;
		this.bytesTransfered = bytesTransferred;
		this.transferState = transferState;
		this.transferException = transferException;
		this.totalFilesToTransfer = totalFilesToTransfer;
		this.totalFilesTransferredSoFar = totalFilesTransferredSoFar;

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

	public void setTotalFilesToTransfer(final int totalFilesToTransfer) {
		this.totalFilesToTransfer = totalFilesToTransfer;
	}

	public int getTotalFilesTransferredSoFar() {
		return totalFilesTransferredSoFar;
	}

}
