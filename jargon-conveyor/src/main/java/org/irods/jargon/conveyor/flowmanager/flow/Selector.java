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
public class Selector implements Cloneable {

	public enum FlowActionEnum {
		ANY, PUT, GET
	}

	public static final String ANY = "*";

	private String hostSelector = ANY;
	private String zoneSelector = ANY;
	private FlowActionEnum flowActionEnum = FlowActionEnum.ANY;

	@Override
	public synchronized Selector clone() {
		Selector clone = new Selector();
		clone.setFlowActionEnum(flowActionEnum);
		clone.setHostSelector(new String(hostSelector));
		clone.setZoneSelector(new String(zoneSelector));
		return clone;
	}

	/**
	 * @return the hostSelector
	 */
	public synchronized String getHostSelector() {
		return hostSelector;
	}

	/**
	 * @param hostSelector
	 *            the hostSelector to set
	 */
	public synchronized void setHostSelector(final String hostSelector) {
		this.hostSelector = hostSelector;
	}

	/**
	 * @return the zoneSelector
	 */
	public synchronized String getZoneSelector() {
		return zoneSelector;
	}

	/**
	 * @param zoneSelector
	 *            the zoneSelector to set
	 */
	public synchronized void setZoneSelector(final String zoneSelector) {
		this.zoneSelector = zoneSelector;
	}

	/**
	 * @return the transferTypeSelector
	 */
	public synchronized FlowActionEnum getFlowActionEnum() {
		return flowActionEnum;
	}

	/**
	 * @param transferTypeSelector
	 *            the transferTypeSelector to set
	 */
	public synchronized void setFlowActionEnum(
			final FlowActionEnum flowActionEnum) {
		this.flowActionEnum = flowActionEnum;
	}

}
