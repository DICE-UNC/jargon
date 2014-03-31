/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow;

import org.irods.jargon.transfer.dao.domain.TransferType;

/**
 * Represents a selector for a flow
 * 
 * @author Mike Conway - DICE
 * 
 */
public class Selector {

	private String hostSelector;
	private String zoneSelector;
	private TransferType transferTypeSelector;

	/**
	 * @return the hostSelector
	 */
	public String getHostSelector() {
		return hostSelector;
	}

	/**
	 * @param hostSelector
	 *            the hostSelector to set
	 */
	public void setHostSelector(String hostSelector) {
		this.hostSelector = hostSelector;
	}

	/**
	 * @return the zoneSelector
	 */
	public String getZoneSelector() {
		return zoneSelector;
	}

	/**
	 * @param zoneSelector
	 *            the zoneSelector to set
	 */
	public void setZoneSelector(String zoneSelector) {
		this.zoneSelector = zoneSelector;
	}

	/**
	 * @return the transferTypeSelector
	 */
	public TransferType getTransferTypeSelector() {
		return transferTypeSelector;
	}

	/**
	 * @param transferTypeSelector
	 *            the transferTypeSelector to set
	 */
	public void setTransferTypeSelector(TransferType transferTypeSelector) {
		this.transferTypeSelector = transferTypeSelector;
	}

}
