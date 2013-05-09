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
	public ConveyorRuntimeException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public ConveyorRuntimeException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public ConveyorRuntimeException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public ConveyorRuntimeException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
