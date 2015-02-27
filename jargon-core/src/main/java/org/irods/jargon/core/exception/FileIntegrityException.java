package org.irods.jargon.core.exception;

/**
 * An exception in a file integrity check, such as a checksum validation error
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class FileIntegrityException extends JargonException {

	/**
	 *
	 */
	private static final long serialVersionUID = 722452082397664532L;

	/**
	 * @param message
	 */
	public FileIntegrityException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public FileIntegrityException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public FileIntegrityException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public FileIntegrityException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public FileIntegrityException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public FileIntegrityException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
