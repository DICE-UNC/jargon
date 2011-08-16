/**
 * 
 */
package org.irods.jargon.transfer.engine;

import org.irods.jargon.core.packinstr.TransferOptions;

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
	private TransferOptions transferOptions;

	/**
	 * Get the detailed configuration for the transfer
	 * 
	 * @return {@link TransferOptions} that control the transfer or
	 *         <code>null</code> if no options specified. Note that this object
	 *         represents shared state, and should be treated as such
	 */
	public synchronized TransferOptions getTransferOptions() {
		return transferOptions;
	}

	/**
	 * Set the detailed options that control the transfer. This actually
	 * defensively copies the <code>TransferOptions</code> to limit any
	 * multi-thread access issues
	 * 
	 * @param transferOptions
	 *            {@link TransferOptions} that will be copied for the instance
	 *            data, or <code>null</code> if default behaviors are desired
	 */
	public synchronized void setTransferOptions(
			final TransferOptions transferOptions) {
		this.transferOptions = new TransferOptions(transferOptions);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("TransferEngineConfigurationProperties");
		sb.append("\n   transferOptions:");
		sb.append(transferOptions);
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
