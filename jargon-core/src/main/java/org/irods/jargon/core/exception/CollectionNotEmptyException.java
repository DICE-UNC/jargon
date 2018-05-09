/**
 *
 */
package org.irods.jargon.core.exception;

/**
 * Represents a condition where a delete operation is attempted without force
 * when the collection is not empty. This can also occur when trying to create
 * soft links.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class CollectionNotEmptyException extends JargonException {

	private static final long serialVersionUID = -5336692567586516928L;

	public CollectionNotEmptyException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public CollectionNotEmptyException(final String message) {
		super(message);
	}

	public CollectionNotEmptyException(final Throwable cause) {
		super(cause);
	}

	public CollectionNotEmptyException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public CollectionNotEmptyException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public CollectionNotEmptyException(final String message, final int info) {
		super(message, info);
	}

}
