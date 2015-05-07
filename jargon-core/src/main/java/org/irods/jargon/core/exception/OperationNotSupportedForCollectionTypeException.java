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
public class OperationNotSupportedForCollectionTypeException extends
		InternalIrodsOperationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4691758091501465065L;

	/**
	 * @param message
	 */
	public OperationNotSupportedForCollectionTypeException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public OperationNotSupportedForCollectionTypeException(
			final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public OperationNotSupportedForCollectionTypeException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public OperationNotSupportedForCollectionTypeException(
			final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public OperationNotSupportedForCollectionTypeException(
			final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public OperationNotSupportedForCollectionTypeException(
			final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
