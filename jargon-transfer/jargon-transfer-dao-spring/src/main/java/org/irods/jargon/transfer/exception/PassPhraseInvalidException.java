/**
 * 
 */
package org.irods.jargon.transfer.exception;

import org.irods.jargon.core.exception.JargonException;

/**
 * Exception when the pass phrase cannot be validated
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class PassPhraseInvalidException extends JargonException {

	private static final long serialVersionUID = -6039687071909450418L;

	/**
	 * @param message
	 */
	public PassPhraseInvalidException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public PassPhraseInvalidException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public PassPhraseInvalidException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public PassPhraseInvalidException(String message, Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public PassPhraseInvalidException(Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public PassPhraseInvalidException(String message,
			int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
