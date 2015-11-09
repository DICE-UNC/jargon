/**
 *
 */
package org.irods.jargon.core.exception;

/**
 * Exception in client/server negotiation phases
 *
 * @author Mike Conway - DICE
 *
 */
public class NegotiationException extends ProtocolException {

	/**
	 *
	 */
	private static final long serialVersionUID = 7745320223422850977L;

	/**
	 * @param message
	 */
	public NegotiationException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NegotiationException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public NegotiationException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public NegotiationException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public NegotiationException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public NegotiationException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
