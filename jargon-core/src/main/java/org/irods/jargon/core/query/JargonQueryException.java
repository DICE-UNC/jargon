/**
 *
 */
package org.irods.jargon.core.query;

/**
 * Exception with the format or content of a query.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class JargonQueryException extends Exception {

	private static final long serialVersionUID = -3526152048790551955L;
	private String query = "";

	/**
	 *
	 */
	public JargonQueryException() {
		super();
	}

	/**
	 * @param message
	 */
	public JargonQueryException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public JargonQueryException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public JargonQueryException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public void setQuery(final String query) {
		this.query = query;
	}

	public String getQuery() {
		return query;
	}

}
