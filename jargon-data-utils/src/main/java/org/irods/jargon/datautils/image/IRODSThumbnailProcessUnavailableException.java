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

	private static final long serialVersionUID = 6228909626014709002L;

	public IRODSThumbnailProcessUnavailableException(final String message) {
		super(message);
	}

	public IRODSThumbnailProcessUnavailableException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public IRODSThumbnailProcessUnavailableException(final Throwable cause) {
		super(cause);
	}

	public IRODSThumbnailProcessUnavailableException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public IRODSThumbnailProcessUnavailableException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public IRODSThumbnailProcessUnavailableException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
