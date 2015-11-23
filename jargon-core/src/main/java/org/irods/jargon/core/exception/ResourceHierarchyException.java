/**
 *
 */
package org.irods.jargon.core.exception;

/**
 * Exception related to composable resources and resource hierarchies. This may
 * be extended with more specific errors
 *
 * @author Mike Conway - DICE
 *
 */
public class ResourceHierarchyException extends JargonException {

	/**
	 *
	 */
	private static final long serialVersionUID = -6420684577388932659L;

	/**
	 * @param message
	 */
	public ResourceHierarchyException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ResourceHierarchyException(final String message,
			final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public ResourceHierarchyException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ResourceHierarchyException(final String message,
			final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ResourceHierarchyException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
		// TDO Auto-gnerated constructor stub
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public ResourceHierarchyException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
