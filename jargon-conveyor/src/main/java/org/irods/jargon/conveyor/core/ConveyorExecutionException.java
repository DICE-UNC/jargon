/**
 * 
 */
package org.irods.jargon.conveyor.core;

/**
 * Exception caused in the processing of a conveyor task. Note that this is an
 * error in the processing, not in the actual underlying task, which is normally
 * passed to the <code>TransferStatusCallbackListener</code>
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ConveyorExecutionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 394410484893939617L;

	/**
	 * 
	 */
	public ConveyorExecutionException() {
	}

	/**
	 * @param arg0
	 */
	public ConveyorExecutionException(final String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public ConveyorExecutionException(final Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public ConveyorExecutionException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

}
