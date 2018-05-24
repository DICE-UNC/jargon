package org.irods.jargon.core.exception;

import org.irods.jargon.core.protovalues.ErrorEnum;

/**
 * Invalid input parameter
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class InvalidInputParameterException extends ProtocolException {

	/**
	 *
	 */
	private static final long serialVersionUID = 8360268535568188141L;
	private static final int ERROR_CODE = ErrorEnum.INVALID_INPUT_PARAM.getInt();

	public InvalidInputParameterException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	public InvalidInputParameterException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public InvalidInputParameterException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public InvalidInputParameterException(final String message) {
		super(message, ERROR_CODE);
	}

	public InvalidInputParameterException(final String message, final Throwable cause) {
		super(message, cause, ERROR_CODE);
	}

	public InvalidInputParameterException(final Throwable cause) {
		super(cause, ERROR_CODE);
	}
}
