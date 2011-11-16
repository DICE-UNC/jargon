/**
 * 
 */
package org.irods.jargon.core.exception;

/**
 * The file requested does not exist in IRODS. Generally, this is non-fatal, and
 * should be handled as a message to the user
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class FileNotFoundException extends JargonException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4561596226207958550L;

	/**
	 * @param message
	 */
	public FileNotFoundException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public FileNotFoundException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public FileNotFoundException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public FileNotFoundException(String message, int infoValue) {
		super(message, infoValue);
	}

}
