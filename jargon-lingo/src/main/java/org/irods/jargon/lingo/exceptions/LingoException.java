/**
 * 
 */
package org.irods.jargon.lingo.exceptions;

/**
 * General exception in the web application
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class LingoException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4702890345903625538L;

	/**
	 * 
	 */
	public LingoException() {
	}

	/**
	 * @param message
	 */
	public LingoException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public LingoException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public LingoException(String message, Throwable cause) {
		super(message, cause);
	}

}
