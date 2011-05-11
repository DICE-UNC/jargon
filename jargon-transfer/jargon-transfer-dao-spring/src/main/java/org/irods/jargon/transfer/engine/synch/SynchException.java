package org.irods.jargon.transfer.engine.synch;

/**
 * Exception in synchronization processing
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class SynchException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4138364269153394731L;

	/**
	 * 
	 */
	public SynchException() {
	}

	/**
	 * @param arg0
	 */
	public SynchException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public SynchException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public SynchException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
