/**
 * 
 */
package org.irods.jargon.conveyor.core;

/**
 * Properties that can control the behavior of the conveyor engine, including
 * <code>TransferOptions</code> that control the detailed configuration of the
 * jargon transfers.
 * <p/>
 * These properties reflect the underlying configuration, and translate them
 * into a usable class that is cached and updated whenever the underlying
 * conveyor configuration is changed. The management of this is the
 * responsibility of the <code>ConfigurationService</code>
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class CachedConveyorConfigurationProperties {

	private boolean logSuccessfulTransfers = false;
	private int maxErrorsBeforeCancel = 0;

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
	public boolean isLogSuccessfulTransfers() {
		return logSuccessfulTransfers;
	}

	/**
	 * 
	 * @param logSuccessfulTransfers
	 */
	public void setLogSuccessfulTransfers(final boolean logSuccessfulTransfers) {
		this.logSuccessfulTransfers = logSuccessfulTransfers;
	}

	/**
	 * @return the maxErrorsBeforeCancel
	 */
	public int getMaxErrorsBeforeCancel() {
		return maxErrorsBeforeCancel;
	}

	/**
	 * @param maxErrorsBeforeCancel
	 *            the maxErrorsBeforeCancel to set
	 */
	public void setMaxErrorsBeforeCancel(int maxErrorsBeforeCancel) {
		this.maxErrorsBeforeCancel = maxErrorsBeforeCancel;
	}


}
