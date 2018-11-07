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

	private static final long serialVersionUID = 5159072487608024771L;

	public ZipRequestTooLargeException(final String message) {
		super(message);
	}

	public ZipRequestTooLargeException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ZipRequestTooLargeException(final Throwable cause) {
		super(cause);
	}

	public ZipRequestTooLargeException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public ZipRequestTooLargeException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public ZipRequestTooLargeException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
