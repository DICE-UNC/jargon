package org.irods.jargon.core.packinstr;

/**
 * Represents options that control the transfer of data to and from iRODS (get
 * and put). This is not an immutable object to make setting the various options
 * easier on the caller. Within Jargon, the <code>TransferOptions</code> are not
 * shared, rather a copy constructor creates a new instance in the various data
 * transfer methods, as these copies may be overridden in the code when dealing
 * with an individual file transfer.
 * <p/>
 * Note that UDP options are included here, but the UDP option is not yet
 * implemented in jargon, and will have no effect.
 * <p/>
 * <b>Note:</b> this part of the API is new and subject to re-factoring. The
 * transfer options are currently not fully supported within the API.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class TransferOptions {

	/**
	 * Desired transfer method (some impl still needed here)
	 */
	public enum TransferType {
		STANDARD, NO_PARALLEL, UDP
	}

	/**
	 * Behavior controlling overwrite (some impl still needed here)
	 */
	public enum ForceOption {
		USE_FORCE, NO_FORCE, ASK_CALLBACK_LISTENER
	}

	public static final int DEFAULT_UDP_SEND_RATE = 600000;
	public static final int DEFAULT_UDP_PACKET_SIZE = 8192;
	public static final int DEFAULT_MAX_PARALLEL_THREADS = 4;

	private int maxThreads = DEFAULT_MAX_PARALLEL_THREADS;
	private int udpSendRate = DEFAULT_UDP_SEND_RATE;
	private int udpPacketSize = DEFAULT_UDP_PACKET_SIZE;
	private TransferType transferType = TransferType.STANDARD;
	private boolean allowPutGetResourceRedirects = false;
	private boolean intraFileStatusCallbacks = false;
	private ForceOption forceOption = ForceOption.ASK_CALLBACK_LISTENER;

	/**
	 * Store a checksum of the file after it has been transferred. This will
	 * only take precedence over
	 * <code>computeAndVerifyChecksumAfterTransfer</code> if the value there is
	 * <code>false</code>
	 */
	private boolean computeChecksumAfterTransfer = false;
	/**
	 * Store a checksum of the file and verify after it has been transferred.
	 * This is 'stronger' than <code>computeChecksumAfterTransfer</code>, and
	 * will do the verify even if both values are <code>true</code>
	 */
	private boolean computeAndVerifyChecksumAfterTransfer = false;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("transferOptions:");
		sb.append("\n   maxThreads:");
		sb.append(maxThreads);
		sb.append("\n  transferType:");
		sb.append(transferType);
		sb.append("\n   udpSendRate:");
		sb.append(udpSendRate);
		sb.append("\n udpPacketSize:");
		sb.append(udpPacketSize);
		sb.append("\n allowPutGetResourceRedirects:");
		sb.append(allowPutGetResourceRedirects);
		sb.append("\n   computeChecksumAfterTransfer:");
		sb.append(computeChecksumAfterTransfer);
		sb.append("\n   computeAndVerifyChecksumAfterTransfer:");
		sb.append(computeAndVerifyChecksumAfterTransfer);
		sb.append("\n   intraFileStatusCallbacks:");
		sb.append(intraFileStatusCallbacks);
		sb.append("\n   forceOption:");
		sb.append(forceOption);
		return sb.toString();
	}

	/**
	 * Copy constructor creates a new <code>TransferOptions</code> based on the
	 * passed-in version. This is done so that the options may be safely passed
	 * between transfer methods that may update the transfer options.
	 * 
	 * @param transferOptions
	 *            <code>TransferOptions</code>
	 */
	public TransferOptions(final TransferOptions transferOptions) {
		this();
		if (transferOptions != null) {

			synchronized (this) {
				setMaxThreads(transferOptions.getMaxThreads());
				setTransferType(transferOptions.getTransferType());
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
			}
		}
	}

	/**
	 * Default (no values) constructor.
	 */
	public TransferOptions() {

	}

	/**
	 * Get the desired mode of transport (parallel i/o, no parallel, etc) for
	 * this transfer NOTE: work in progress, not currently effective
	 * 
	 * @return {@link TransferType} indicating current transfer mode
	 */
	public synchronized TransferType getTransferType() {
		return transferType;
	}

	/**
	 * Sets the desired mode of transport (prallel i/o, etc) for this transfer.
	 * 
	 * @param transferType
	 *            {@link TransferType} indicating current transfer mode
	 */
	public synchronized void setTransferType(final TransferType transferType) {
		this.transferType = transferType;
	}

	/**
	 * Get the desired max threads value for paralell transfers
	 * 
	 * @return <code>int</code> with the desired max parallel transfer threads.
	 *         0 means use default in iRODS.
	 */
	public synchronized int getMaxThreads() {
		return maxThreads;
	}

	/**
	 * Set the desired max threads value for parallel transfers.
	 * 
	 * @param maxThreads
	 *            <code>int</code> with the maximum desired parallel transfer
	 *            threads, 0 means use the default in iRODS.
	 */
	public synchronized void setMaxThreads(final int maxThreads) {
		this.maxThreads = maxThreads;
	}

	/**
	 * Get the UDP send rate if UDP transfers in use.
	 * 
	 * @return <code>int</code> with the UDP send rate
	 */
	public synchronized int getUdpSendRate() {
		return udpSendRate;
	}

	/**
	 * Set the UDP send rate if UDP transfers in use.
	 * 
	 * @param udpSendRate
	 *            <code>int</code> with the desired UDP send rate.
	 */
	public synchronized void setUdpSendRate(final int udpSendRate) {
		this.udpSendRate = udpSendRate;
	}

	/**
	 * Get the desired UDP packet size if UDP transfers are in use.
	 * 
	 * @return <code>int</code> with desired UDP packet size.
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
	 * @return the allowPutGetResourceRedirects <code>boolean</code> that will
	 *         be <code>true</code> if redirecting is desired
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
	 * @return the intraFileStatusCallbacks value. If <code>true</code>, then
	 *         call-backs will be sent on progress within-file, if a listener is
	 *         present.
	 */
	public synchronized boolean isIntraFileStatusCallbacks() {
		return intraFileStatusCallbacks;
	}

	/**
	 * @param intraFileStatusCallbacks
	 *            <code>boolean</code> with the intraFileStatusCallbacks
	 *            behavior desired. If <code>true</code> and a call-back
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
	public synchronized void setForceOption(ForceOption forceOption) {
		this.forceOption = forceOption;
	}

}
