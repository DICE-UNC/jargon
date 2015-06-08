/**
 * 
 */
package org.irods.jargon.core.exception;

/**
 * Exceptions that may involve misconfiguration of iRODS or a pluggable
 * operation within iRODS.
 * 
 * @author Mike Conway - DICE
 *
 */
public class InternalIrodsOperationException extends JargonException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5478419146067271934L;

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public InternalIrodsOperationException(String message,
			int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public InternalIrodsOperationException(String message, Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InternalIrodsOperationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public InternalIrodsOperationException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public InternalIrodsOperationException(Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 */
	public InternalIrodsOperationException(Throwable cause) {
		super(cause);
	}

}
