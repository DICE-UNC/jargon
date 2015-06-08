/**
 * 
 */
package org.irods.jargon.core.exception;

/**
 * equvalent to the -1800000 or 1801000 error
 * 
 * @author Mike Conway - DICE
 *
 */
public class KeyException extends InternalIrodsOperationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1323427987927154954L;

	/**
	 * @param message
	 */
	public KeyException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public KeyException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public KeyException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public KeyException(String message, Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public KeyException(Throwable cause, int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public KeyException(String message, int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
