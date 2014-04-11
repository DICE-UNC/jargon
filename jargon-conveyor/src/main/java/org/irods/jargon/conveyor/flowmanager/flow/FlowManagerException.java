/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow;

/**
 * General checked Exception in the processing of flows
 * 
 * @author Mike Conway - DICE
 *
 */
public class FlowManagerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 409067804821147339L;

	/**
	 * 
	 */
	public FlowManagerException() {
	}

	/**
	 * @param arg0
	 */
	public FlowManagerException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public FlowManagerException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public FlowManagerException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
