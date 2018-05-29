package org.irods.jargon.core.exception;

import org.irods.jargon.core.protovalues.ErrorEnum;

/**
 * Rule engine exception that can be a side effect of version-inappropriate
 * operations/and or non specified error (sorry)
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class NoMoreRulesException extends JargonException {

	private static final long serialVersionUID = -4497115388532060530L;
	private static final int ERROR_CODE = ErrorEnum.NO_MORE_RULES_ERR.getInt();

	public NoMoreRulesException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	public NoMoreRulesException(final String message, final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public NoMoreRulesException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public NoMoreRulesException(final String message) {
		super(message, ERROR_CODE);
	}

	public NoMoreRulesException(final String message, final Throwable cause) {
		super(message, cause, ERROR_CODE);
	}

	public NoMoreRulesException(final Throwable cause) {
		super(cause, ERROR_CODE);
	}
}
