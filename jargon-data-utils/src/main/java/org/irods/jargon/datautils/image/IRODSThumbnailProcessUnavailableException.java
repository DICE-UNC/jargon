package org.irods.jargon.datautils.image;

import org.irods.jargon.core.exception.JargonException;

/**
 * Indicates that rule-driven thumbnail services are not available on the given iRODS server.
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
	public IRODSThumbnailProcessUnavailableException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public IRODSThumbnailProcessUnavailableException(String message,
			Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public IRODSThumbnailProcessUnavailableException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public IRODSThumbnailProcessUnavailableException(String message,
			Throwable cause, int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public IRODSThumbnailProcessUnavailableException(Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public IRODSThumbnailProcessUnavailableException(String message,
			int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
