package org.irods.jargon.core.pub;

import org.irods.jargon.core.connection.ConnectionProgressStatus;
import org.irods.jargon.core.connection.ConnectionProgressStatus.CallbackType;
import org.irods.jargon.core.connection.ConnectionProgressStatusListener;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.packinstr.TransferOptions;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatus.TransferType;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;

/**
 * Implementation of a listener for intra-file status call-backs. This object
 * receives call-backs from the lower-level connection, and can then generate
 * higher-level status call-backs as needed. The lifetime of this listener
 * should be for a transfer of one file.
 * <p>
 * This object uses simple optimizations, such as aggregating multiple low-level
 * call-backs to fewer higher-level call-backs, to keep the chatter to a
 * minimum. Note that this is an initial implementation, and further
 * configuration and optimization strategies may be employed later. In other
 * words, this object may change.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class DefaultIntraFileProgressCallbackListener implements ConnectionProgressStatusListener {

	private final TransferStatusCallbackListener transferStatusCallbackListener;
	private final TransferType transferType;
	private final long totalBytesToTransfer;
	private long totalBytesTransferred;
	private final int interval;
	private final TransferOptions transferOptions;

	private int countOfMessagesSinceLastSend = 0;
	private long countOfBytesSinceLastSend = 0L;
	public static final int BYTE_COUNT_MESSAGE_THRESHOLD = 5;
	public static final long BYTE_COUNT_BYTE_THRESHOLD = 4194304;

	/**
	 * Static initializer method to create an immutable call-back listener for
	 * intra-file status information.
	 *
	 * @param transferType
	 *            {@link TransferType} enum value
	 * @param totalBytesToTransfer
	 *            {@code long} with the total size of the file under transfer
	 * @param transferControlBlock
	 *            {@link TransferControlBlock} with information about the current
	 *            transfer
	 * @param transferStatusCallbackListener
	 *            {@link TransferStatusCallbackListener} that will recieve the
	 *            aggregated callbacks from this listener.
	 * @param transferOptions
	 *            {@link TransferOptions} that can contain configuration of
	 *            inta-file callback behavior
	 * @return {@link ConnectionProgressStatusListener}
	 */
	public static ConnectionProgressStatusListener instanceSettingTransferOptions(final TransferType transferType,
			final long totalBytesToTransfer, final TransferControlBlock transferControlBlock,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferOptions transferOptions) {
		return new DefaultIntraFileProgressCallbackListener(transferType, totalBytesToTransfer, transferControlBlock,
				transferStatusCallbackListener, BYTE_COUNT_MESSAGE_THRESHOLD, transferOptions);
	}

	/**
	 * Static initializer method to create an immutable call-back listener for
	 * intra-file status information.
	 *
	 * @param transferType
	 *            {@link TransferType} enum value
	 * @param totalBytesToTransfer
	 *            {@code long} with the total size of the file under transfer
	 * @param transferControlBlock
	 *            {@link TransferControlBlock} with information about the current
	 *            transfer
	 * @param transferStatusCallbackListener
	 *            {@link TransferStatusCallbackListener} that will recieve the
	 *            aggregated callbacks from this listener.
	 * @return {@link ConnectionProgressStatusListener}
	 */
	public static ConnectionProgressStatusListener instance(final TransferType transferType,
			final long totalBytesToTransfer, final TransferControlBlock transferControlBlock,
			final TransferStatusCallbackListener transferStatusCallbackListener) {
		return new DefaultIntraFileProgressCallbackListener(transferType, totalBytesToTransfer, transferControlBlock,
				transferStatusCallbackListener, BYTE_COUNT_MESSAGE_THRESHOLD, null);
	}

	private DefaultIntraFileProgressCallbackListener(final TransferType transferType, final long totalBytesToTransfer,
			final TransferControlBlock transferControlBlock,
			final TransferStatusCallbackListener transferStatusCallbackListener, final int interval,
			final TransferOptions transferOptions) {

		if (transferControlBlock == null) {
			throw new IllegalArgumentException("transferControlBlock is null");
		}

		if (transferStatusCallbackListener == null) {
			throw new IllegalArgumentException("transferStatusCallbackListener is null");
		}

		if (transferType == null) {
			throw new IllegalArgumentException("null transferType");
		}

		this.transferStatusCallbackListener = transferStatusCallbackListener;
		this.transferType = transferType;
		this.totalBytesToTransfer = totalBytesToTransfer;
		this.interval = interval;

		if (transferOptions == null) {
			this.transferOptions = new TransferOptions();
			this.transferOptions.setIntraFileStatusCallbacksNumberCallsInterval(BYTE_COUNT_MESSAGE_THRESHOLD);
			this.transferOptions.setIntraFileStatusCallbacksTotalBytesInterval(BYTE_COUNT_BYTE_THRESHOLD);
		} else {
			this.transferOptions = transferOptions;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.connection.ConnectionProgressStatusListener#
	 * connectionProgressStatusCallback
	 * (org.irods.jargon.core.connection.ConnectionProgressStatus)
	 */
	@Override
	public synchronized void connectionProgressStatusCallback(final ConnectionProgressStatus connectionProgressStatus) {

		// are there bytes to count?
		if (connectionProgressStatus.getCallbackType() == CallbackType.SEND_PROGRESS
				|| connectionProgressStatus.getCallbackType() == CallbackType.RECEIVE_PROGRESS) {
			accumulateAndSend(connectionProgressStatus);

		} else if (connectionProgressStatus.getCallbackType() == CallbackType.OPERATIONAL_MESSAGE) {
			// not yet implemented, would be a message like 'starting parallel
			// transfer', or 'computing checksum'
		}
	}

	private void accumulateAndSend(final ConnectionProgressStatus connectionProgressStatus) {
		countOfMessagesSinceLastSend++;
		countOfBytesSinceLastSend += connectionProgressStatus.getByteCount();
		totalBytesTransferred += connectionProgressStatus.getByteCount();

		/*
		 * at this point transfer options guaranteed to not be null, it should be set in
		 * the constructor
		 */

		if (countOfMessagesSinceLastSend > transferOptions.getIntraFileStatusCallbacksNumberCallsInterval()
				|| countOfBytesSinceLastSend > transferOptions.getIntraFileStatusCallbacksTotalBytesInterval()) {
			try {
				TransferStatus transferStatus = TransferStatus.instanceForIntraFileStatus(transferType,
						totalBytesToTransfer, totalBytesTransferred);
				transferStatusCallbackListener.statusCallback(transferStatus);
				countOfMessagesSinceLastSend = 0;
				countOfBytesSinceLastSend = 0;
			} catch (JargonException e) {
				throw new JargonRuntimeException("error sending status callback", e);
			}
		}
	}

	/**
	 * @return the transferOptions
	 */
	public synchronized TransferOptions getTransferOptions() {
		return transferOptions;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DefaultIntraFileProgressCallbackListener [");
		if (transferStatusCallbackListener != null) {
			builder.append("transferStatusCallbackListener=");
			builder.append(transferStatusCallbackListener);
			builder.append(", ");
		}
		if (transferType != null) {
			builder.append("transferType=");
			builder.append(transferType);
			builder.append(", ");
		}
		builder.append("totalBytesToTransfer=");
		builder.append(totalBytesToTransfer);
		builder.append(", totalBytesTransferred=");
		builder.append(totalBytesTransferred);
		builder.append(", interval=");
		builder.append(interval);
		builder.append(", ");
		if (transferOptions != null) {
			builder.append("transferOptions=");
			builder.append(transferOptions);
			builder.append(", ");
		}
		builder.append("countOfMessagesSinceLastSend=");
		builder.append(countOfMessagesSinceLastSend);
		builder.append("]");
		return builder.toString();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.connection.ConnectionProgressStatusListener#
	 * finalConnectionProgressStatusCallback
	 * (org.irods.jargon.core.connection.ConnectionProgressStatus)
	 */
	@Override
	public void finalConnectionProgressStatusCallback(final ConnectionProgressStatus connectionProgressStatus) {
		try {
			TransferStatus transferStatus = TransferStatus.instanceForIntraFileStatus(transferType,
					totalBytesToTransfer, totalBytesToTransfer);
			transferStatusCallbackListener.statusCallback(transferStatus);
		} catch (JargonException e) {
			throw new JargonRuntimeException("error sending status callback", e);
		}

	}
}
