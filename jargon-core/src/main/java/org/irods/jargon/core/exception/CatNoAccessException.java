package org.irods.jargon.core.exception;

/**
 * The current user has no access to the information in the catalog, or may not
 * have ACL access to a given file or collection
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class CatNoAccessException extends JargonException {

	private static final long serialVersionUID = 8114364975943244537L;

	/**
	 * @param message
	 */
	public CatNoAccessException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public CatNoAccessException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public CatNoAccessException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
