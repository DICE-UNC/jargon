/**
 * 
 */
package org.irods.jargon.core.exception;

/**
 * An exception occurs where an operation does not specify a default storage
 * resource, and no rule exists on iRODS to assign a default resource
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class NoResourceDefinedException extends ResourceHierarchyException {

	private static final long serialVersionUID = 7273836835708379860L;

	public NoResourceDefinedException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	public NoResourceDefinedException(final String message,
			final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public NoResourceDefinedException(final String message,
			final Throwable cause) {
		super(message, cause);
	}

	public NoResourceDefinedException(final String message) {
		super(message);
	}

	public NoResourceDefinedException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public NoResourceDefinedException(final Throwable cause) {
		super(cause);
	}

}
