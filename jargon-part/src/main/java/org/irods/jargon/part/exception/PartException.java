/**
 * 
 */
package org.irods.jargon.part.exception;

/**
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class PartException extends Exception {

	private static final long serialVersionUID = -2919077732985478518L;

	public PartException() {
		super();
	}

	public PartException(String message, Throwable cause) {
		super(message, cause);
	}

	public PartException(String message) {
		super(message);
	}

	public PartException(Throwable cause) {
		super(cause);
	}

	


}
