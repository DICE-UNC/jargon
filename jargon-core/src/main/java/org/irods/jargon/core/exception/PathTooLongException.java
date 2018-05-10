/**
 *
 */
package org.irods.jargon.core.exception;

/**
 * Represents an exception caused by an iRODS file path exceeding the maximum
 * length
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class PathTooLongException extends ProtocolException {

	private static final long serialVersionUID = -892954436360716634L;

	public PathTooLongException(final String message) {
		super(message);
	}

	public PathTooLongException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public PathTooLongException(final Throwable cause) {
		super(cause);
	}

	public PathTooLongException(final String message, final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public PathTooLongException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public PathTooLongException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
