/**
 * 
 */
package org.irods.jargon.core.exception;

/**
 * @author conwaymc
 *
 */
public class ReplicaTokenLockException extends JargonException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9034193901095098533L;

	/**
	 * @param message
	 */
	public ReplicaTokenLockException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ReplicaTokenLockException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public ReplicaTokenLockException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ReplicaTokenLockException(String message, Throwable cause, int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ReplicaTokenLockException(Throwable cause, int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public ReplicaTokenLockException(String message, int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
