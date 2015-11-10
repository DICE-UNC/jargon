/**
 *
 */
package org.irods.jargon.core.exception;

/**
 * The data requested does not exist in IRODS. Generally, this is non-fatal, and
 * should be handled as a message to the user
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class DataNotFoundException extends JargonException {

	/**
	 *
	 */
	private static final long serialVersionUID = -7242363525516259108L;

	/**
	 * @param message
	 */
	public DataNotFoundException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public DataNotFoundException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DataNotFoundException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
