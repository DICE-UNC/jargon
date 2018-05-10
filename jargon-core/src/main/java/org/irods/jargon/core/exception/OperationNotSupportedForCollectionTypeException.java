/**
 *
 */
package org.irods.jargon.core.exception;

/**
 * Exception when operation cannot be supported by the given special collection
 * type
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class OperationNotSupportedForCollectionTypeException extends InternalIrodsOperationException {

	private static final long serialVersionUID = -4691758091501465065L;

	public OperationNotSupportedForCollectionTypeException(final String message) {
		super(message);
	}

	public OperationNotSupportedForCollectionTypeException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public OperationNotSupportedForCollectionTypeException(final Throwable cause) {
		super(cause);
	}

	public OperationNotSupportedForCollectionTypeException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public OperationNotSupportedForCollectionTypeException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public OperationNotSupportedForCollectionTypeException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
