/**
 * 
 */
package org.irods.jargon.conveyor.core;

/**
 * Represents a time-out in an execution of an operation
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ConveyorExecutionTimeoutException extends
		ConveyorExecutionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1738765007550443864L;

	/**
	 * 
	 */
	public ConveyorExecutionTimeoutException() {
	}

	/**
	 * @param arg0
	 */
	public ConveyorExecutionTimeoutException(final String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public ConveyorExecutionTimeoutException(final Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public ConveyorExecutionTimeoutException(final String arg0,
			final Throwable arg1) {
		super(arg0, arg1);
	}

}
