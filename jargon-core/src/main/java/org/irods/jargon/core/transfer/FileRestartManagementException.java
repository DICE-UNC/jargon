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

	private static final long serialVersionUID = 4787837160966228541L;

	public FileRestartManagementException(final String message) {
		super(message);
	}

	public FileRestartManagementException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public FileRestartManagementException(final Throwable cause) {
		super(cause);
	}

	public FileRestartManagementException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public FileRestartManagementException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public FileRestartManagementException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
