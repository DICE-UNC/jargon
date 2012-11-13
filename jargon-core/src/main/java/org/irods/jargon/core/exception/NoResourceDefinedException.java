/**
 * 
 */
package org.irods.jargon.core.exception;

/**
 * An exception occurs where an operation does not specify a default storage resource, and no rule exists on iRODS to assign a default resource
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class NoResourceDefinedException extends JargonException {
	
	private static final long serialVersionUID = 7273836835708379860L;

	public NoResourceDefinedException(String message,
			int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	public NoResourceDefinedException(String message, Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public NoResourceDefinedException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoResourceDefinedException(String message) {
		super(message);
	}

	public NoResourceDefinedException(Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public NoResourceDefinedException(Throwable cause) {
		super(cause);
	}

	



}
