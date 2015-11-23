/**
 *
 */
package org.irods.jargon.core.exception;

/**
 * equvalent to the -1800000 or 1801000 error
 *
 * @author Mike Conway - DICE
 *
 */
public class KeyException extends InternalIrodsOperationException {

	/**
	 *
	 */
	private static final long serialVersionUID = -1323427987927154954L;

	/**
	 * @param message
	 */
	public KeyException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public KeyException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public KeyException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public KeyException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public KeyException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public KeyException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
