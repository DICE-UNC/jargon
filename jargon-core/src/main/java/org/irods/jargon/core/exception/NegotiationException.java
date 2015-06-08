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
	public NegotiationException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NegotiationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public NegotiationException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public NegotiationException(String message, Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public NegotiationException(Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public NegotiationException(String message, int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
