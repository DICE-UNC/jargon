/**
 *
 */
package org.irods.jargon.core.exception;

/**
 * Error in locating a remote execution script
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class RemoteScriptExecutionException extends JargonException {

	/**
	 *
	 */
	private static final long serialVersionUID = 3569078752323506006L;

	/**
	 * @param message
	 */
	public RemoteScriptExecutionException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public RemoteScriptExecutionException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public RemoteScriptExecutionException(final String message,
			final Throwable cause) {
		super(message, cause);
	}

}
