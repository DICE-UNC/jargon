package org.irods.jargon.core.exception;

/**
 * The operation is an attempted overwrite, but no force option was specified.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class OverwriteException extends JargonException {

	private static final long serialVersionUID = -4561596226207958550L;

	public OverwriteException(final String message) {
		super(message);
	}

	public OverwriteException(final Throwable cause) {
		super(cause);
	}

	public OverwriteException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
