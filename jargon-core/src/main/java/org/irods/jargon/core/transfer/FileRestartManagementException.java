/**
 *
 */
package org.irods.jargon.core.transfer;

import org.irods.jargon.core.exception.JargonException;

/**
 * General exception in the management of restart file information
 *
 * @author Mike Conway - DICE
 *
 */
public class FileRestartManagementException extends JargonException {

	/**
	 *
	 */
	private static final long serialVersionUID = 4787837160966228541L;

	/**
	 * @param message
	 */
	public FileRestartManagementException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public FileRestartManagementException(final String message,
			final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public FileRestartManagementException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public FileRestartManagementException(final String message,
			final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public FileRestartManagementException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public FileRestartManagementException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
