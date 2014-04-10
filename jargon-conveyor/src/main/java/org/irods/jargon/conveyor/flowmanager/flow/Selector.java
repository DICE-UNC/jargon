/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow;


/**
 * Represents a selector for a flow
 * 
 * @author Mike Conway - DICE
 * 
 */
public class Selector {

	public enum FlowActionEnum {
		ANY, PUT, GET
	}

	private String hostSelector;
	private String zoneSelector;
	private FlowActionEnum flowActionEnum;

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
	public FlowActionEnum getFlowActionEnum() {
		return flowActionEnum;
	}

	/**
	 * @param transferTypeSelector
	 *            the transferTypeSelector to set
	 */
	public void setFlowActionEnum(FlowActionEnum flowActionEnum) {
		this.flowActionEnum = flowActionEnum;
	}

}
