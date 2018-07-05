/**
 *
 */
package org.irods.jargon.zipservice.api;

import org.irods.jargon.zipservice.api.exception.ZipServiceException;

/**
 * The requested bundle would be larger than the size configured
 *
 * @author Mike Conway - DICE
 *
 */
public class ZipRequestTooLargeException extends ZipServiceException {

	/**
	 *
	 */
	private static final long serialVersionUID = 5159072487608024771L;

	/**
	 * @param message
	 */
	public ZipRequestTooLargeException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ZipRequestTooLargeException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public ZipRequestTooLargeException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ZipRequestTooLargeException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ZipRequestTooLargeException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public ZipRequestTooLargeException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
