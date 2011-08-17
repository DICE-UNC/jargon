/**
 * 
 */
package org.irods.jargon.transfer.engine;


/**
 * Properties that can control the behavior of the transfer engine, including
 * <code>TransferOptions</code> that control the detailed configuration of the
 * jargon transfers.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class TransferEngineConfigurationProperties {

	private boolean logSuccessfulTransfers;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("TransferEngineConfigurationProperties");
		sb.append("\n   isLogSuccessfulTransfers:");
		sb.append(logSuccessfulTransfers);
		return sb.toString();
	}

	/**
	 * Are successful transfer details logged in the database?
	 * 
	 * @return
	 */
	public synchronized boolean isLogSuccessfulTransfers() {
		return logSuccessfulTransfers;
	}

	/**
	 * 
	 * @param logSuccessfulTransfers
	 */
	public synchronized void setLogSuccessfulTransfers(
			final boolean logSuccessfulTransfers) {
		this.logSuccessfulTransfers = logSuccessfulTransfers;
	}

}
