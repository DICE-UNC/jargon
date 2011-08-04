/**
 * 
 */
package org.irods.jargon.transfer.engine.synch;

/**
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class SynchRuntimeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3608826733455937719L;

	/**
	 * 
	 */
	public SynchRuntimeException() {
	}

	/**
	 * @param arg0
	 */
	public SynchRuntimeException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public SynchRuntimeException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public SynchRuntimeException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
