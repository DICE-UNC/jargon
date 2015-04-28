/**
 *
 */
package org.irods.jargon.core.query;

/**
 * Represents an exception in a gen query builder operation.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class GenQueryBuilderException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = -1775439743556087882L;

	/**
	 *
	 */
	public GenQueryBuilderException() {
	}

	/**
	 * @param arg0
	 */
	public GenQueryBuilderException(final String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public GenQueryBuilderException(final Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public GenQueryBuilderException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

}
