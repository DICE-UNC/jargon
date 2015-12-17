/**
 *
 */
package org.irods.jargon.datautils.filesampler;

import org.irods.jargon.core.exception.JargonException;

/**
 * File is too large for processing
 *
 * @author Mike Conway - DICE
 *
 */
public class FileTooLargeException extends JargonException {

	/**
	 *
	 */
	private static final long serialVersionUID = 9085475135705527553L;

	/**
	 * @param message
	 */
	public FileTooLargeException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public FileTooLargeException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public FileTooLargeException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public FileTooLargeException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public FileTooLargeException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public FileTooLargeException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
