/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow;

/**
 * @author Mike Conway - DICE
 * 
 */
public class FlowSpecConfigurationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 178281169504283852L;

	/**
	 * 
	 */
	public FlowSpecConfigurationException() {
	}

	/**
	 * @param arg0
	 */
	public FlowSpecConfigurationException(final String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public FlowSpecConfigurationException(final Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public FlowSpecConfigurationException(final String arg0,
			final Throwable arg1) {
		super(arg0, arg1);
	}

}
