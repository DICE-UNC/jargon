/**
 *
 */
package org.irods.jargon.core.exception;

/**
 * Internal exception in the client-server negotiation process
 *
 * @author Mike Conway - DICE
 *
 */
public class ClientServerNegotiationException extends
		InternalIrodsOperationException {

	/**
	 *
	 */
	private static final long serialVersionUID = 4915631767830728158L;

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public ClientServerNegotiationException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ClientServerNegotiationException(final String message,
			final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ClientServerNegotiationException(final String message,
			final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public ClientServerNegotiationException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ClientServerNegotiationException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 */
	public ClientServerNegotiationException(final Throwable cause) {
		super(cause);
	}

}
