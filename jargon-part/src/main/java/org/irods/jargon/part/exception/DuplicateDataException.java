/**
 * 
 */
package org.irods.jargon.part.exception;

/**
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class DuplicateDataException extends Exception {

	public DuplicateDataException(String message, Throwable cause) {
		super(message, cause);
	}

	public DuplicateDataException(String message) {
		super(message);
	}

	public DuplicateDataException(Throwable cause) {
		super(cause);
	}

}
