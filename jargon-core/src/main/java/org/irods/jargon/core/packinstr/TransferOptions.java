package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.protovalues.ChecksumEncodingEnum;

/**
 * Represents options that control the transfer of data to and from iRODS (get
 * and put). This is not an immutable object to make setting the various options
 * easier on the caller. Within Jargon, the {@code TransferOptions} are not
 * shared, rather a copy constructor creates a new instance in the various data
 * transfer methods, as these copies may be overridden in the code when dealing
 * with an individual file transfer.
 * <p>
 * Note that UDP options are included here, but the UDP option is not yet
 * implemented in jargon, and will have no effect.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class TransferOptions {

	/**
	 * Behavior controlling overwrite
	 */
	public enum ForceOption {
		USE_FORCE, NO_FORCE, ASK_CALLBACK_LISTENER
	}

	public enum PutOptions {
		NORMAL, MSSO_FILE
	}

	public static final int DEFAULT_UDP_SEND_RATE = 600000;
	public static final int DEFAULT_UDP_PACKET_SIZE = 8192;
	public static final int DEFAULT_MAX_PARALLEL_THREADS = 4;

	private int maxThreads = DEFAULT_MAX_PARALLEL_THREADS;
	private int udpSendRate = DEFAULT_UDP_SEND_RATE;
	private int udpPacketSize = DEFAULT_UDP_PACKET_SIZE;
	private boolean allowPutGetResourceRedirects = false;
	private boolean intraFileStatusCallbacks = false;
	private ForceOption forceOption = ForceOption.ASK_CALLBACK_LISTENER;
	private boolean useParallelTransfer = true;
	private ChecksumEncodingEnum checksumEncoding = ChecksumEncodingEnum.DEFAULT;
	/**
	 * Number of callbacks before an intra file callback listener will be
	 * notified, no matter how many bytes passed
	 */
	private int intraFileStatusCallbacksNumberCallsInterval = 5;
	/**
	 * Number of bytes in a callback before in intra file callback listener will
	 * be notified, no matter how many calls have been made
	 */
	private long intraFileStatusCallbacksTotalBytesInterval = 4194304;

	/**
	 * DataType option for putting certain types of special files
	 */
	private PutOptions putOption = PutOptions.NORMAL;

	/**
	 * Store a checksum of the file after it has been transferred. This will
	 * only take precedence over
	 * {@code computeAndVerifyChecksumAfterTransfer} if the value there is
	 * {@code false}
	 */
	private boolean computeChecksumAfterTransfer = false;
	/**
	 * Store a checksum of the file and verify after it has been transferred.
	 * This is 'stronger' than {@code computeChecksumAfterTransfer}, and
	 * will do the verify even if both values are {@code true}
	 */
	private boolean computeAndVerifyChecksumAfterTransfer = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public synchronized String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TransferOptions [maxThreads=");
		builder.append(maxThreads);
		builder.append(", udpSendRate=");
		builder.append(udpSendRate);
		builder.append(", udpPacketSize=");
		builder.append(udpPacketSize);
		builder.append(", allowPutGetResourceRedirects=");
		builder.append(allowPutGetResourceRedirects);
		builder.append(", intraFileStatusCallbacks=");
		builder.append(intraFileStatusCallbacks);
		builder.append(", ");
		if (forceOption != null) {
			builder.append("forceOption=");
			builder.append(forceOption);
			builder.append(", ");
		}
		builder.append("useParallelTransfer=");
		builder.append(useParallelTransfer);
		builder.append(", ");
		if (checksumEncoding != null) {
			builder.append("checksumEncoding=");
			builder.append(checksumEncoding);
			builder.append(", ");
		}
		builder.append("intraFileStatusCallbacksNumberCallsInterval=");
		builder.append(intraFileStatusCallbacksNumberCallsInterval);
		builder.append(", intraFileStatusCallbacksTotalBytesInterval=");
		builder.append(intraFileStatusCallbacksTotalBytesInterval);
		builder.append(", ");
		if (putOption != null) {
			builder.append("putOption=");
			builder.append(putOption);
			builder.append(", ");
		}
		builder.append("computeChecksumAfterTransfer=");
		builder.append(computeChecksumAfterTransfer);
		builder.append(", computeAndVerifyChecksumAfterTransfer=");
		builder.append(computeAndVerifyChecksumAfterTransfer);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Copy constructor creates a new {@code TransferOptions} based on the
	 * passed-in version. This is done so that the options may be safely passed
	 * between transfer methods that may update the transfer options.
	 *
	 * @param transferOptions
	 *            {@code TransferOptions}
	 */
	public TransferOptions(final TransferOptions transferOptions) {
		this();
		if (transferOptions != null) {

			synchronized (this) {
				setMaxThreads(transferOptions.getMaxThreads());
				setUdpPacketSize(transferOptions.getUdpPacketSize());
				setUdpSendRate(transferOptions.getUdpSendRate());
				setAllowPutGetResourceRedirects(transferOptions
						.isAllowPutGetResourceRedirects());
				setComputeChecksumAfterTransfer(transferOptions
						.isComputeChecksumAfterTransfer());
				setComputeAndVerifyChecksumAfterTransfer(transferOptions
						.isComputeAndVerifyChecksumAfterTransfer());
				setIntraFileStatusCallbacks(transferOptions.intraFileStatusCallbacks);
				setForceOption(transferOptions.getForceOption());
				setUseParallelTransfer(transferOptions.isUseParallelTransfer());
				setPutOption(transferOptions.getPutOption());
				setChecksumEncoding(transferOptions.getChecksumEncoding());
				setIntraFileStatusCallbacksNumberCallsInterval(transferOptions
						.getIntraFileStatusCallbacksNumberCallsInterval());
				setIntraFileStatusCallbacksTotalBytesInterval(transferOptions
						.getIntraFileStatusCallbacksTotalBytesInterval());
			}
		}
	}

	/**
	 * Default (no values) constructor.
	 */
	public TransferOptions() {

	}

	/**
	 * Get the desired max threads value for paralell transfers
	 *
	 * @return {@code int} with the desired max parallel transfer threads.
	 *         0 means use default in iRODS.
	 */
	public synchronized int getMaxThreads() {
		return maxThreads;
	}

	/**
	 * Set the desired max threads value for parallel transfers.
	 *
	 * @param maxThreads
	 *            {@code int} with the maximum desired parallel transfer
	 *            threads, 0 means use the default in iRODS.
	 */
	public synchronized void setMaxThreads(final int maxThreads) {
		this.maxThreads = maxThreads;
	}

	/**
	 * Get the UDP send rate if UDP transfers in use.
	 *
	 * @return {@code int} with the UDP send rate
	 */
	public synchronized int getUdpSendRate() {
		return udpSendRate;
	}

	/**
	 * Set the UDP send rate if UDP transfers in use.
	 *
	 * @param udpSendRate
	 *            {@code int} with the desired UDP send rate.
	 */
	public synchronized void setUdpSendRate(final int udpSendRate) {
		this.udpSendRate = udpSendRate;
	}

	/**
	 * Get the desired UDP packet size if UDP transfers are in use.
	 *
	 * @return {@code int} with desired UDP packet size.
	 */
	public synchronized int getUdpPacketSize() {
		return udpPacketSize;
	}

	/**
	 * Set the desired UDP packet size if UDP transfers are in use.
	 *
	 * @param udpPacketSize
	 */
	public synchronized void setUdpPacketSize(final int udpPacketSize) {
		this.udpPacketSize = udpPacketSize;
	}

	/**
	 * Should puts/gets redirect to the resource server that holds the data?
	 * (equivalent to the -I in iput/iget>
	 *
	 * @return the allowPutGetResourceRedirects {@code boolean} that will
	 *         be {@code true} if redirecting is desired
	 */
	public synchronized boolean isAllowPutGetResourceRedirects() {
		return allowPutGetResourceRedirects;
	}

	/**
	 * Should puts/gets redirect to the resource server that holds the data?
	 * (equivalent to the -I in iput/iget>
	 *
	 * @param allowPutGetResourceRedirects
	 *            the allowPutGetResourceRedirects to set
	 */
	public synchronized void setAllowPutGetResourceRedirects(
			final boolean allowPutGetResourceRedirects) {
		this.allowPutGetResourceRedirects = allowPutGetResourceRedirects;
	}

	/**
	 * @param computeChecksumAfterTransfer
	 *            the computeChecksumAfterTransfer to set
	 */
	public synchronized void setComputeChecksumAfterTransfer(
			final boolean computeChecksumAfterTransfer) {
		this.computeChecksumAfterTransfer = computeChecksumAfterTransfer;
	}

	/**
	 * @return the computeChecksumAfterTransfer
	 */
	public synchronized boolean isComputeChecksumAfterTransfer() {
		return computeChecksumAfterTransfer;
	}

	/**
	 * @param computeAndVerifyChecksumAfterTransfer
	 *            the computeAndVerifyChecksumAfterTransfer to set
	 */
	public synchronized void setComputeAndVerifyChecksumAfterTransfer(
			final boolean computeAndVerifyChecksumAfterTransfer) {
		this.computeAndVerifyChecksumAfterTransfer = computeAndVerifyChecksumAfterTransfer;
	}

	/**
	 * @return the computeAndVerifyChecksumAfterTransfer
	 */
	public synchronized boolean isComputeAndVerifyChecksumAfterTransfer() {
		return computeAndVerifyChecksumAfterTransfer;
	}

	/**
	 * @return the intraFileStatusCallbacks value. If {@code true}, then
	 *         call-backs will be sent on progress within-file, if a listener is
	 *         present.
	 */
	public synchronized boolean isIntraFileStatusCallbacks() {
		return intraFileStatusCallbacks;
	}

	/**
	 * @param intraFileStatusCallbacks
	 *            {@code boolean} with the intraFileStatusCallbacks
	 *            behavior desired. If {@code true} and a call-back
	 *            listener is provided, then within-file status call-backs will
	 *            be generated during transfers. This has a slight performance
	 *            penalty.
	 */
	public synchronized void setIntraFileStatusCallbacks(
			final boolean intraFileStatusCallbacks) {
		this.intraFileStatusCallbacks = intraFileStatusCallbacks;
	}

	/**
	 * Get the prevailing force option for this transfer
	 *
	 * @return {@link ForceOption} enum value that indicates the force mode to
	 *         use
	 */
	public synchronized ForceOption getForceOption() {
		return forceOption;
	}

	/**
	 * Set the prevailing force option for this transfer.
	 *
	 * @param forceOption
	 *            {@link ForceOption} enum value
	 */
	public synchronized void setForceOption(final ForceOption forceOption) {
		this.forceOption = forceOption;
	}

	/**
	 * Is parallel transfer allowed for this operation?
	 *
	 * @return useParallelTransfer {@code boolean} which is
	 *         {@code true} if parallel transfers can be usd
	 */
	public synchronized boolean isUseParallelTransfer() {
		return useParallelTransfer;
	}

	/**
	 * Set whether parallel transfers can be used
	 *
	 * @param useParallelTransfer
	 *            {@code boolean} with the useParallelTransfer option
	 */
	public synchronized void setUseParallelTransfer(
			final boolean useParallelTransfer) {
		this.useParallelTransfer = useParallelTransfer;
	}

	/**
	 * @return the putOption
	 */
	public synchronized PutOptions getPutOption() {
		return putOption;
	}

	/**
	 * @param putOption
	 *            the putOption to set
	 */
	public synchronized void setPutOption(final PutOptions putOption) {
		this.putOption = putOption;
	}

	public synchronized ChecksumEncodingEnum getChecksumEncoding() {
		return checksumEncoding;
	}

	/**
	 * Set the type of checksum to use for iRODS. The default is MD5, and for
	 * versions of iRODS 3.3.1 and higher SHA256 is available
	 *
	 * @param checksumEncoding
	 */
	public synchronized void setChecksumEncoding(
			final ChecksumEncodingEnum checksumEncoding) {

		if (checksumEncoding == null) {
			throw new IllegalArgumentException("null checksumEncoding");
		}

		this.checksumEncoding = checksumEncoding;

	}

	/**
	 * @return the intraFileStatusCallbacksNumberCallsInterval
	 */
	public synchronized int getIntraFileStatusCallbacksNumberCallsInterval() {
		return intraFileStatusCallbacksNumberCallsInterval;
	}

	/**
	 * @param intraFileStatusCallbacksNumberCallsInterval
	 *            the intraFileStatusCallbacksNumberCallsInterval to set
	 */
	public synchronized void setIntraFileStatusCallbacksNumberCallsInterval(
			final int intraFileStatusCallbacksNumberCallsInterval) {
		this.intraFileStatusCallbacksNumberCallsInterval = intraFileStatusCallbacksNumberCallsInterval;
	}

	/**
	 * @return the intraFileStatusCallbacksTotalBytesInterval
	 */
	public synchronized long getIntraFileStatusCallbacksTotalBytesInterval() {
		return intraFileStatusCallbacksTotalBytesInterval;
	}

	/**
	 * @param intraFileStatusCallbacksTotalBytesInterval
	 *            the intraFileStatusCallbacksTotalBytesInterval to set
	 */
	public synchronized void setIntraFileStatusCallbacksTotalBytesInterval(
			final long intraFileStatusCallbacksTotalBytesInterval) {
		this.intraFileStatusCallbacksTotalBytesInterval = intraFileStatusCallbacksTotalBytesInterval;
	}
}
