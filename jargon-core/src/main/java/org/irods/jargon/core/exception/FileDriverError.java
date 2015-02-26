/**
 *
 */
package org.irods.jargon.core.exception;

/**
 * File System errors (e.g. -522000) from iRODS. Specific numeric iRODS error
 * codes are avaialable in this object
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class FileDriverError extends JargonException {

	/**
	 *
	 */
	private static final long serialVersionUID = -5370926952990437497L;

	/**
	 * @param message
	 */
	public FileDriverError(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public FileDriverError(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public FileDriverError(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public FileDriverError(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public FileDriverError(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public FileDriverError(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
