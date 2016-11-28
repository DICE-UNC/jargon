/**
 *
 */
package org.irods.jargon.core.exception;

/**
 * Exception when operation cannot be supported on the given iRODS version
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class OperationNotSupportedByThisServerException extends
InternalIrodsOperationException {

	/**
	 *
	 */
	private static final long serialVersionUID = 3536008740969078628L;

	/**
	 * @param message
	 */
	public OperationNotSupportedByThisServerException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public OperationNotSupportedByThisServerException(final String message,
			final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public OperationNotSupportedByThisServerException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public OperationNotSupportedByThisServerException(final String message,
			final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public OperationNotSupportedByThisServerException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public OperationNotSupportedByThisServerException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
