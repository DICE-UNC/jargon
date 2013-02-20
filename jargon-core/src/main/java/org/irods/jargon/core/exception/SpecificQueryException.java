/**
 * 
 */
package org.irods.jargon.core.exception;

/**
 * Exception in specific query processing. (-853000)
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class SpecificQueryException extends JargonException {

	private static final long serialVersionUID = 7553446240365967481L;

	/**
	 * @param message
	 */
	public SpecificQueryException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public SpecificQueryException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public SpecificQueryException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public SpecificQueryException(String message, Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public SpecificQueryException(Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public SpecificQueryException(String message,
			int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
