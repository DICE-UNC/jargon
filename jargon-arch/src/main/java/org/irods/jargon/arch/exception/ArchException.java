package org.irods.jargon.arch.exception;

/**
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class ArchException extends Exception {

	private static final long serialVersionUID = 7089422009239410189L;

	/**
	 * 
	 */
	public ArchException() {
	}

	/**
	 * @param message
	 */
	public ArchException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ArchException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ArchException(String message, Throwable cause) {
		super(message, cause);
	}

}
