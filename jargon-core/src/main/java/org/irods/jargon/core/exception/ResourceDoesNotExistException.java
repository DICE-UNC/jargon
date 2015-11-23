/**
 *
 */
package org.irods.jargon.core.exception;

/**
 * Equivalent to -78000 exception for resource does not exist
 *
 * @author Mike Conway - DICE
 *
 */
public class ResourceDoesNotExistException extends ResourceHierarchyException {

	/**
	 *
	 */
	private static final long serialVersionUID = 8122529669867030185L;

	/**
	 * @param message
	 */
	public ResourceDoesNotExistException(final String message) {
		super(message);

	}

	/**
	 * @param message
	 * @param cause
	 */
	public ResourceDoesNotExistException(final String message,
			final Throwable cause) {
		super(message, cause);

	}

	/**
	 * @param cause
	 */
	public ResourceDoesNotExistException(final Throwable cause) {
		super(cause);

	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ResourceDoesNotExistException(final String message,
			final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);

	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ResourceDoesNotExistException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);

	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public ResourceDoesNotExistException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);

	}

}
