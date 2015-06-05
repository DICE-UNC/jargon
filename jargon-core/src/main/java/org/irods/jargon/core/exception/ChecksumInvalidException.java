/**
 * 
 */
package org.irods.jargon.core.exception;

/**
 * @author Mike Conway - DICE General exception for checksum mismatches
 *
 */
public class ChecksumInvalidException extends JargonException {

	private static final long serialVersionUID = 6069493884085439471L;

	/**
	 * @param message
	 */
	public ChecksumInvalidException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ChecksumInvalidException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public ChecksumInvalidException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ChecksumInvalidException(String message, Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ChecksumInvalidException(Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public ChecksumInvalidException(String message,
			int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
