/**
 * 
 */
package org.irods.jargon.core.pub;

/**
 * 
 * @author Mike Conway - DICE (www.irods.org) see http://code.renci.org for
 *         trackers, access info, and documentation
 * 
 */
public class TransferStatistics {

	private long totalBytes = 0;
	private int seconds = 0;
	private int kbPerSecond = 0;

	/**
	 * 
	 */
	public TransferStatistics() {
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("TransferStatistics");
		sb.append("\n\t totalByte:");
		sb.append(totalBytes);
		sb.append("\n\t seconds:");
		sb.append(seconds);
		sb.append("\n\t kbPerSecond:");
		sb.append(kbPerSecond);
		return sb.toString();
	}

	public long getTotalBytes() {
		return totalBytes;
	}

	public void setTotalBytes(long totalBytes) {
		this.totalBytes = totalBytes;
	}

	public int getSeconds() {
		return seconds;
	}

	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}

	public int getKbPerSecond() {
		return kbPerSecond;
	}

	public void setKbPerSecond(int kbPerSecond) {
		this.kbPerSecond = kbPerSecond;
	}

}
