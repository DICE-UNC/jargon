package org.irods.jargon.core.exception;

import org.irods.jargon.core.protovalues.ErrorEnum;

/**
 * Security exception when no access for file or collection
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class NoAccessException extends JargonException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 912976071792452411L;
	private static final int ERROR_CODE = ErrorEnum.CAT_NO_ACCESS_PERMISSION
			.getInt();

	public NoAccessException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	public NoAccessException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public NoAccessException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public NoAccessException(final String message) {
		super(message, ERROR_CODE);
	}

	public NoAccessException(final String message, final Throwable cause) {
		super(message, cause, ERROR_CODE);
	}

	public NoAccessException(final Throwable cause) {
		super(cause, ERROR_CODE);
	}
}
