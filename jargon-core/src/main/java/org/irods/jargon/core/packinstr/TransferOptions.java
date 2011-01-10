/**
 * 
 */
package org.irods.jargon.core.packinstr;

/**
 * Represents options that control the transfer of data to and from iRODS (get
 * and put). This is not an immutable object to make seting the various options
 * easier on the caller.
 * <p/>
 * Note that udp options are included here, but the UDP option is not yet implemented in jargon, and will have no effect.
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

	public TransferType getTransferType() {
		return transferType;
	}

	public void setTransferType(final TransferType transferType) {
		this.transferType = transferType;
	}

	public int getMaxThreads() {
		return maxThreads;
	}

	public void setMaxThreads(final int maxThreads) {
		this.maxThreads = maxThreads;
	}

	public int getUdpSendRate() {
		return udpSendRate;
	}

	public void setUdpSendRate(final int udpSendRate) {
		this.udpSendRate = udpSendRate;
	}

	public int getUdpPacketSize() {
		return udpPacketSize;
	}

	public void setUdpPacketSize(final int udpPacketSize) {
		this.udpPacketSize = udpPacketSize;
	}

}
