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
	public ClientServerNegotiationException(String message,
			int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ClientServerNegotiationException(String message, Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ClientServerNegotiationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public ClientServerNegotiationException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ClientServerNegotiationException(Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 */
	public ClientServerNegotiationException(Throwable cause) {
		super(cause);
	}

}
