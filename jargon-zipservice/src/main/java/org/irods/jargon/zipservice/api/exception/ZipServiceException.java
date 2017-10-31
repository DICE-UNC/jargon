/**
 * 
 */
package org.irods.jargon.zipservice.api.exception;

import org.irods.jargon.core.exception.JargonException;

/**
 * @author Mike Conway - DICE
 *
 */
public class ZipServiceException extends JargonException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3527136057177654744L;

	/**
	 * @param message
	 */
	public ZipServiceException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ZipServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public ZipServiceException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ZipServiceException(String message, Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ZipServiceException(Throwable cause, int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public ZipServiceException(String message, int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
