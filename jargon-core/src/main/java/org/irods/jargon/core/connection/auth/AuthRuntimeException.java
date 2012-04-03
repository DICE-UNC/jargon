package org.irods.jargon.core.connection.auth;

import org.irods.jargon.core.exception.JargonRuntimeException;

/**
 * Represents a runtime exception involving the setup and execution of
 * authentication mechanisms. This exception represents configuration and
 * similar issues, versus communication or credential exceptions.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class AuthRuntimeException extends JargonRuntimeException {

	private static final long serialVersionUID = -5720969727236267279L;

	public AuthRuntimeException() {
	}

	/**
	 * @param message
	 */
	public AuthRuntimeException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public AuthRuntimeException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public AuthRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

}
