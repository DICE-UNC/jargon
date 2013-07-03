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
	public ZoneUnavailableException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ZoneUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public ZoneUnavailableException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ZoneUnavailableException(String message, Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ZoneUnavailableException(Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public ZoneUnavailableException(String message,
			int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
