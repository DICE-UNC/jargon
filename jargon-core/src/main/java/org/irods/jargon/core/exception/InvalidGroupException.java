/**
 *
 */
package org.irods.jargon.core.exception;

/**
 * There was an invalid iRODS user group
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class InvalidGroupException extends JargonException {

	private static final long serialVersionUID = 284100966221964252L;

	public InvalidGroupException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	public InvalidGroupException(final String message) {
		super(message);
	}

	public InvalidGroupException(final Throwable cause) {
		super(cause);
	}

	public InvalidGroupException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
