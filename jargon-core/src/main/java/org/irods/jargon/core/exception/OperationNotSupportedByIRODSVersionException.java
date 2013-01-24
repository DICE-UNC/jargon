/**
 * 
 */
package org.irods.jargon.core.exception;

/**
 * Exception when operation cannot be supported on the given iRODS version
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class OperationNotSupportedByIRODSVersionException extends
		JargonException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3536008740969078628L;

	/**
	 * @param message
	 */
	public OperationNotSupportedByIRODSVersionException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public OperationNotSupportedByIRODSVersionException(String message,
			Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public OperationNotSupportedByIRODSVersionException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public OperationNotSupportedByIRODSVersionException(String message,
			Throwable cause, int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public OperationNotSupportedByIRODSVersionException(Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public OperationNotSupportedByIRODSVersionException(String message,
			int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
