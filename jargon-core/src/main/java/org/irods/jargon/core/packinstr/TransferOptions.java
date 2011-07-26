/**
 * 
 */
package org.irods.jargon.core.packinstr;

/**
 * Represents options that control the transfer of data to and from iRODS (get
 * and put). This is not an immutable object to make seting the various options
 * easier on the caller. Within Jargon, the <code>TransferOptions</code> are not
 * shared, rather a copy constructor creates a new instance in the various data
 * transfer methods, as these copies may be overridden in the code when dealing
 * with an individual file transfer.
 * <p/>
 * Note that udp options are included here, but the UDP option is not yet
 * implemented in jargon, and will have no effect.
 * <p/>
 * <b>Note:</b> this part of the API is new and subject to refactoring. The
 * transfer options are currently not fully supported within the API.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class TransferOptions {

	public enum TransferType {
		STANDARD, NO_PARALLEL, UDP
	}

	public static final int DEFAULT_UDP_SEND_RATE = 600000;
	public static final int DEFAULT_UDP_PACKET_SIZE = 8192;
	public static final int DEFAULT_MAX_PARALLEL_THREADS = 4;

	private int maxThreads = DEFAULT_MAX_PARALLEL_THREADS;
	private int udpSendRate = DEFAULT_UDP_SEND_RATE;
	private int udpPacketSize = DEFAULT_UDP_PACKET_SIZE;
	private TransferType transferType = TransferType.STANDARD;
	private boolean allowPutGetResourceRedirects = false;
	/**
	 * Store a checksum of the file after it has been transferred. This will
	 * only take precidence over
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
		if (transferOptions == null) {
			throw new IllegalArgumentException("null transferOptions");
		}

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
		}
	}

	/**
	 * Default (no values) constructor.
	 */
	public TransferOptions() {

	}

	public synchronized TransferType getTransferType() {
		return transferType;
	}

	public synchronized void setTransferType(final TransferType transferType) {
		this.transferType = transferType;
	}

	public synchronized int getMaxThreads() {
		return maxThreads;
	}

	public synchronized void setMaxThreads(final int maxThreads) {
		this.maxThreads = maxThreads;
	}

	public synchronized int getUdpSendRate() {
		return udpSendRate;
	}

	public synchronized void setUdpSendRate(final int udpSendRate) {
		this.udpSendRate = udpSendRate;
	}

	public synchronized int getUdpPacketSize() {
		return udpPacketSize;
	}

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
	public void setComputeAndVerifyChecksumAfterTransfer(
			final boolean computeAndVerifyChecksumAfterTransfer) {
		this.computeAndVerifyChecksumAfterTransfer = computeAndVerifyChecksumAfterTransfer;
	}

	/**
	 * @return the computeAndVerifyChecksumAfterTransfer
	 */
	public boolean isComputeAndVerifyChecksumAfterTransfer() {
		return computeAndVerifyChecksumAfterTransfer;
	}

}
