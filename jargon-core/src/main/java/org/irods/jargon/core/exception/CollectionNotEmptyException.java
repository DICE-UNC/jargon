/**
 * 
 */
package org.irods.jargon.core.exception;

/**
 * Represents a condition where a delete operation is attempted without force when the collection is not empty
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class CollectionNotEmptyException extends JargonException {


	private static final long serialVersionUID = -5336692567586516928L;

	public CollectionNotEmptyException(final String message,
			final Throwable cause) {
		super(message, cause);
	}

	public CollectionNotEmptyException(final String message) {
		super(message);
	}

	public CollectionNotEmptyException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public CollectionNotEmptyException(final String message,
			final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 */
	public CollectionNotEmptyException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public CollectionNotEmptyException(final String message,
			final int info) {
		super(message, info);
	}

}
