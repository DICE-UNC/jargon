/**
 *
 */
package org.irods.jargon.core.checksum;

import org.irods.jargon.core.exception.JargonException;

/**
 * Checksum computation method is not available.
 *
 * @author Mike Conway - DICE
 *
 */
public class ChecksumMethodUnavailableException extends JargonException {

	private static final long serialVersionUID = 6522826462448161134L;

	/**
	 * @param message
	 */
	public ChecksumMethodUnavailableException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ChecksumMethodUnavailableException(final String message,
			final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public ChecksumMethodUnavailableException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ChecksumMethodUnavailableException(final String message,
			final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ChecksumMethodUnavailableException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public ChecksumMethodUnavailableException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
