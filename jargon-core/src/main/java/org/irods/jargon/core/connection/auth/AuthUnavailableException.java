
package org.irods.jargon.core.connection.auth;

import org.irods.jargon.core.exception.JargonException;

/**
 * Represents an exception where the given auth mechanism is not available
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class AuthUnavailableException extends JargonException {

	private static final long serialVersionUID = -7178590696091208081L;

	/**
	 * @param message
	 */
	public AuthUnavailableException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public AuthUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public AuthUnavailableException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public AuthUnavailableException(String message, Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public AuthUnavailableException(Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public AuthUnavailableException(String message,
			int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
