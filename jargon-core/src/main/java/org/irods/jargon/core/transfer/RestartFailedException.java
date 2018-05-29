/**
 *
 */
package org.irods.jargon.core.transfer;

/**
 * The restart process failed in a way that further attempts at restarting
 * should not be tried
 *
 * @author Mike Conway -DICE
 *
 */
public class RestartFailedException extends FileRestartManagementException {

	private static final long serialVersionUID = 7185214710917119172L;

	public RestartFailedException(final String message) {
		super(message);
	}

	public RestartFailedException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public RestartFailedException(final Throwable cause) {
		super(cause);
	}

	public RestartFailedException(final String message, final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public RestartFailedException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public RestartFailedException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
