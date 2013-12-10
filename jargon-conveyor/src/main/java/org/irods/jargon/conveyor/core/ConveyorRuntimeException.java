/**
 * 
 */
package org.irods.jargon.conveyor.core;

/**
 * Unprocessable runtime exception in conveyor framework
 * 
 * @author Mike Conway - DICE (www.irods.org) see
 *         https://code.renci.org/gf/project/jargon/
 * 
 */
public class ConveyorRuntimeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6566360568054196399L;

	/**
	 * 
	 */
	public ConveyorRuntimeException() {
	}

	/**
	 * @param arg0
	 */
	public ConveyorRuntimeException(final String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public ConveyorRuntimeException(final Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public ConveyorRuntimeException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

}
