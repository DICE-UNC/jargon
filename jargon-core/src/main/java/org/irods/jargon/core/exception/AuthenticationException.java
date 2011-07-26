package org.irods.jargon.core.exception;

import org.irods.jargon.core.protovalues.ErrorEnum;

public class AuthenticationException extends JargonException {
	private static final long serialVersionUID = -8718442214402431485L;
	private static final int ERROR_CODE = ErrorEnum.CAT_INVALID_USER.getInt();

	public AuthenticationException(final String message) {
		super(message, ERROR_CODE);
	}

	public AuthenticationException(final String message, final Throwable cause) {
		super(message, cause, ERROR_CODE);
	}

	public AuthenticationException(final Throwable cause) {
		super(cause, ERROR_CODE);
	}
}
