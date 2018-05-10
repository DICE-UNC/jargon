/**
 *
 */
package org.irods.jargon.core.exception;

/**
 * A resource is not valid for this iRODS Zone, This is equivalent to:
 *
 * CAT_INVALID_RESOURCE -831000
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class InvalidResourceException extends ResourceHierarchyException {

	private static final long serialVersionUID = -7796830852660980659L;

	public InvalidResourceException(final String message) {
		super(message);
	}

	public InvalidResourceException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public InvalidResourceException(final Throwable cause) {
		super(cause);
	}

	public InvalidResourceException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public InvalidResourceException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public InvalidResourceException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
