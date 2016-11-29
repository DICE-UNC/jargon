/**
 *
 */
package org.irods.jargon.core.exception;

/**
 * @author Mike Conway - DICE
 *
 */
public class EncryptionException extends JargonException {

	/**
	 *
	 */
	private static final long serialVersionUID = -6836562937507775737L;

	/**
	 * @param message
	 */
	public EncryptionException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public EncryptionException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public EncryptionException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public EncryptionException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public EncryptionException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public EncryptionException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
