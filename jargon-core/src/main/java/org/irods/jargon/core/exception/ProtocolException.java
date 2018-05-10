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

	private static final long serialVersionUID = -6238216076420776338L;

	public ProtocolException(final String message) {
		super(message);
	}

	public ProtocolException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ProtocolException(final Throwable cause) {
		super(cause);
	}

	public ProtocolException(final String message, final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public ProtocolException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public ProtocolException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
