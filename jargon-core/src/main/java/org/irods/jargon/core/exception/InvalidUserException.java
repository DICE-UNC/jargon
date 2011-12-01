package org.irods.jargon.core.exception;

import org.irods.jargon.core.protovalues.ErrorEnum;

/**
 * The given user is invalid in iRODS
 * 
 * @author Mike Conway
 * 
 */
public class InvalidUserException extends JargonException {
	private static final long serialVersionUID = -8718442214402431485L;
	private static final int ERROR_CODE = ErrorEnum.CAT_INVALID_USER.getInt();

	public InvalidUserException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	public InvalidUserException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public InvalidUserException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public InvalidUserException(final String message) {
		super(message, ERROR_CODE);
	}

	public InvalidUserException(final String message, final Throwable cause) {
		super(message, cause, ERROR_CODE);
	}

	public InvalidUserException(final Throwable cause) {
		super(cause, ERROR_CODE);
	}
}
