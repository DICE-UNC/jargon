/**
 * 
 */
package org.irods.jargon.conveyor.core;

/**
 * Represents a time-out or the conveyor is busy and cannot currently perform an
 * operation
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ConveyorBusyException extends ConveyorExecutionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1738765007550443864L;

	/**
	 * 
	 */
	public ConveyorBusyException() {
	}

	/**
	 * @param arg0
	 */
	public ConveyorBusyException(final String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public ConveyorBusyException(final Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public ConveyorBusyException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

}
