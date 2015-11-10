/**
 *
 */
package org.irods.jargon.core.exception;

/**
 * Exceptions related to network protocols or packing instruction format errors
 *
 * @author Mike Conway - DICE
 *
 */
public class ProtocolException extends JargonException {

	/**
	 *
	 */
	private static final long serialVersionUID = -6238216076420776338L;

	/**
	 * @param message
	 */
	public ProtocolException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ProtocolException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public ProtocolException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ProtocolException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ProtocolException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public ProtocolException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
