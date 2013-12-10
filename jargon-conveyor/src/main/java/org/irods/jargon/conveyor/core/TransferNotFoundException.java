/**
 * 
 */
package org.irods.jargon.conveyor.core;

/**
 * A transfer cannot be found
 * 
 * @author Mike Conway - DICE (www.irods.org) see
 *         https://code.renci.org/gf/project/jargon/
 * 
 */
public class TransferNotFoundException extends ConveyorExecutionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1060500590074590041L;

	public TransferNotFoundException() {
	}

	/**
	 * @param arg0
	 */
	public TransferNotFoundException(final String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public TransferNotFoundException(final Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public TransferNotFoundException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

}
