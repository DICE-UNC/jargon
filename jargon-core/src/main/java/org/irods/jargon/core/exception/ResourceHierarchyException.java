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

	private static final long serialVersionUID = -6420684577388932659L;

	public ResourceHierarchyException(final String message) {
		super(message);
	}

	public ResourceHierarchyException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ResourceHierarchyException(final Throwable cause) {
		super(cause);
	}

	public ResourceHierarchyException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public ResourceHierarchyException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public ResourceHierarchyException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
