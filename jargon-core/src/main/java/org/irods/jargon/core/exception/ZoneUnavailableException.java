/**
 *
 */
package org.irods.jargon.core.exception;

/**
 * Maps to a -92111 exception when a federated zone is not available
 * 
 * @author Mike Conway - DICE (www.irods.org) see http://code.renci.org for
 *         trackers, access info, and documentation
 * 
 */
public class ZoneUnavailableException extends JargonException {

	/**
	 *
	 */
	private static final long serialVersionUID = 3405552956160956894L;

	/**
	 * @param message
	 */
	public ZoneUnavailableException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ZoneUnavailableException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public ZoneUnavailableException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ZoneUnavailableException(final String message,
			final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ZoneUnavailableException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public ZoneUnavailableException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
