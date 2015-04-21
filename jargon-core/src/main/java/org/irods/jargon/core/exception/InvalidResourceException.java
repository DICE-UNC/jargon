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
public class InvalidResourceException extends JargonException {

	/**
	 *
	 */
	private static final long serialVersionUID = -7796830852660980659L;

	/**
	 * @param message
	 */
	public InvalidResourceException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidResourceException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public InvalidResourceException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public InvalidResourceException(final String message,
			final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public InvalidResourceException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public InvalidResourceException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
