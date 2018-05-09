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

	private static final long serialVersionUID = 5478419146067271934L;

	public InternalIrodsOperationException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	public InternalIrodsOperationException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public InternalIrodsOperationException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public InternalIrodsOperationException(final String message) {
		super(message);
	}

	public InternalIrodsOperationException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public InternalIrodsOperationException(final Throwable cause) {
		super(cause);
	}

}
