/**
 *
 */
package org.irods.jargon.core.exception;

/**
 * @author Mike Conway - DICE
 *
 */
public class EncryptionException extends JargonException {

	private static final long serialVersionUID = -6836562937507775737L;

	public EncryptionException(final String message) {
		super(message);
	}

	public EncryptionException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public EncryptionException(final Throwable cause) {
		super(cause);
	}

	public EncryptionException(final String message, final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public EncryptionException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public EncryptionException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
