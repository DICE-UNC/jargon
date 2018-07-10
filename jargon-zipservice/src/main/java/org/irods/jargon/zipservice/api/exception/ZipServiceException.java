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
	public ZipServiceException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ZipServiceException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public ZipServiceException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ZipServiceException(final String message, final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ZipServiceException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public ZipServiceException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
