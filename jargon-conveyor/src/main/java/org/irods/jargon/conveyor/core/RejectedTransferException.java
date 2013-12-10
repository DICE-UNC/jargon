/**
 * 
 */
package org.irods.jargon.conveyor.core;

/**
 * Exception caused by a requested transfer operation being rejected (e.g. for
 * being a duplicate)
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class RejectedTransferException extends ConveyorExecutionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8444222046737658883L;

	/**
	 * 
	 */
	public RejectedTransferException() {
	}

	/**
	 * @param arg0
	 */
	public RejectedTransferException(final String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public RejectedTransferException(final Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public RejectedTransferException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

}
