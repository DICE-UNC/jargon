/**
 * 
 */
package org.irods.jargon.core.exception;

/**
 * Represents a condition where a file or collection is being added, and it
 * already exists. Note that some operations allow an override flag that would
 * automatically overwrite such data.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class JargonFileOrCollAlreadyExistsException extends JargonException {
 
	private static final long serialVersionUID = 8322543242606980799L;

	public JargonFileOrCollAlreadyExistsException(String message,
			Throwable cause) {
		super(message, cause);
	}

	public JargonFileOrCollAlreadyExistsException(String message) {
		super(message);
	}

	public JargonFileOrCollAlreadyExistsException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * @param message
	 * @param cause
	 */
	public JargonFileOrCollAlreadyExistsException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 */
	public JargonFileOrCollAlreadyExistsException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public JargonFileOrCollAlreadyExistsException(String message, int info) {
		super(message, info);
	}

}
