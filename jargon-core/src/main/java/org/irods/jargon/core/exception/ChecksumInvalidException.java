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

	public ChecksumInvalidException(final String message) {
		super(message);
	}

	public ChecksumInvalidException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ChecksumInvalidException(final Throwable cause) {
		super(cause);
	}

	public ChecksumInvalidException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public ChecksumInvalidException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public ChecksumInvalidException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
