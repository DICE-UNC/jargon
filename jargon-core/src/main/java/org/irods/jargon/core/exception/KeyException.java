/**
 *
 */
package org.irods.jargon.core.exception;

/**
 * equvalent to the -1800000 or 1801000 error
 *
 * @author Mike Conway - DICE
 *
 */
public class KeyException extends InternalIrodsOperationException {

	private static final long serialVersionUID = -1323427987927154954L;

	public KeyException(final String message) {
		super(message);
	}

	public KeyException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public KeyException(final Throwable cause) {
		super(cause);
	}

	public KeyException(final String message, final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public KeyException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public KeyException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
