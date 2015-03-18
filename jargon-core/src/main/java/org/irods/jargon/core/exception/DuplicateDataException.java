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
public class DuplicateDataException extends JargonException {

	/**
	 *
	 */
	private static final long serialVersionUID = 3569078752323506006L;

	/**
	 * @param message
	 */
	public DuplicateDataException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public DuplicateDataException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DuplicateDataException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
