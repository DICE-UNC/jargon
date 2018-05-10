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

	private static final long serialVersionUID = 3569078752323506006L;

	public RemoteScriptExecutionException(final String message) {
		super(message);
	}

	public RemoteScriptExecutionException(final Throwable cause) {
		super(cause);
	}

	public RemoteScriptExecutionException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
