package org.irods.jargon.datautils.image;

import org.irods.jargon.core.exception.JargonException;

/**
 * Indicates that rule-driven thumbnail services are not available on the given
 * iRODS server.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSThumbnailProcessUnavailableException extends JargonException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6228909626014709002L;

	/**
	 * @param message
	 */
	public IRODSThumbnailProcessUnavailableException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public IRODSThumbnailProcessUnavailableException(final String message,
			final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public IRODSThumbnailProcessUnavailableException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public IRODSThumbnailProcessUnavailableException(final String message,
			final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public IRODSThumbnailProcessUnavailableException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public IRODSThumbnailProcessUnavailableException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
