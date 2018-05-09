package org.irods.jargon.core.exception;

/**
 * An exception in a file integrity check, such as a checksum validation error
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class FileIntegrityException extends JargonException {

	private static final long serialVersionUID = 722452082397664532L;

	public FileIntegrityException(final String message) {
		super(message);
	}

	public FileIntegrityException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public FileIntegrityException(final Throwable cause) {
		super(cause);
	}

	public FileIntegrityException(final String message, final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public FileIntegrityException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public FileIntegrityException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
