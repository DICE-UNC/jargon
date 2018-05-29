package org.irods.jargon.core.exception;

import org.irods.jargon.core.protovalues.ErrorEnum;

/**
 * Security exception when no API privilege for user.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class NoAPIPrivException extends JargonException {

	private static final long serialVersionUID = 5020600855655284826L;
	private static final int ERROR_CODE = ErrorEnum.SYS_NO_API_PRIV.getInt();

	public NoAPIPrivException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	public NoAPIPrivException(final String message, final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public NoAPIPrivException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public NoAPIPrivException(final String message) {
		super(message, ERROR_CODE);
	}

	public NoAPIPrivException(final String message, final Throwable cause) {
		super(message, cause, ERROR_CODE);
	}

	public NoAPIPrivException(final Throwable cause) {
		super(cause, ERROR_CODE);
	}
}
