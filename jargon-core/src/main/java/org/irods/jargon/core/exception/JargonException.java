/**
 *
 */
package org.irods.jargon.core.exception;

/**
 * A general exception that has occurrred in IRODS.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 * 
 */
public class JargonException extends Exception {

	private static final long serialVersionUID = -4060585048895549767L;
	private final int underlyingIRODSExceptionCode;

	/**
	 * @param message
	 */
	public JargonException(final String message) {
		super(message);
		underlyingIRODSExceptionCode = 0;
	}

	/**
	 * @param message
	 * @param cause
	 */
	public JargonException(final String message, final Throwable cause) {
		super(message, cause);
		underlyingIRODSExceptionCode = 0;
	}

	/**
	 * @param cause
	 */
	public JargonException(final Throwable cause) {
		super(cause);
		underlyingIRODSExceptionCode = 0;
	}

	/**
	 * @param message
	 * @param cause
	 */
	public JargonException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause);
		this.underlyingIRODSExceptionCode = underlyingIRODSExceptionCode;
	}

	/**
	 * @param cause
	 */
	public JargonException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause);
		this.underlyingIRODSExceptionCode = underlyingIRODSExceptionCode;
	}

	public JargonException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message);
		this.underlyingIRODSExceptionCode = underlyingIRODSExceptionCode;
	}

	/**
	 * @return the underlyingIRODSExceptionCode
	 */
	public int getUnderlyingIRODSExceptionCode() {
		return underlyingIRODSExceptionCode;
	}

}
