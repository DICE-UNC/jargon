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
public class ClientServerNegotiationException extends InternalIrodsOperationException {

	private static final long serialVersionUID = 4915631767830728158L;

	public ClientServerNegotiationException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	public ClientServerNegotiationException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public ClientServerNegotiationException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ClientServerNegotiationException(final String message) {
		super(message);
	}

	public ClientServerNegotiationException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public ClientServerNegotiationException(final Throwable cause) {
		super(cause);
	}

}
