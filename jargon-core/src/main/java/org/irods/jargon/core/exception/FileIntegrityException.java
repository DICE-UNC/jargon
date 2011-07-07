
package org.irods.jargon.core.exception;

/**
 * An exception in a file integrity check, such as a checksum validation error
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
	public FileIntegrityException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public FileIntegrityException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public FileIntegrityException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public FileIntegrityException(String message, Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public FileIntegrityException(Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public FileIntegrityException(String message,
			int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
