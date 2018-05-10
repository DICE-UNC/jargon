/**
 *
 */
package org.irods.jargon.core.exception;

/**
 * Exception caused by an invalid argument to a bound SQL query in iRODS. This
 * is the equivalent of the iRODS CAT_INVALID_ARGUMENT -816000
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class InvalidArgumentException extends ProtocolException {

	private static final long serialVersionUID = -8059435486261380492L;

	public InvalidArgumentException(final String message) {
		super(message);
	}

	public InvalidArgumentException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public InvalidArgumentException(final Throwable cause) {
		super(cause);
	}

	public InvalidArgumentException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public InvalidArgumentException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public InvalidArgumentException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
