/**
 *
 */
package org.irods.jargon.core.exception;

/**
 * Runtime version of JargonException for cases where JargonException cannot be
 * thrown
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class JargonRuntimeException extends RuntimeException {

	private static final long serialVersionUID = -187881112002016775L;

	public JargonRuntimeException() {
	}

	public JargonRuntimeException(final String message) {
		super(message);
	}

	public JargonRuntimeException(final Throwable cause) {
		super(cause);
	}

	public JargonRuntimeException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
