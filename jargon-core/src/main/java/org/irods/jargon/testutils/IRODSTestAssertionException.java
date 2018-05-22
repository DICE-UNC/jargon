/**
 *
 */
package org.irods.jargon.testutils;

/**
 * Exception raised by an invalid assertion when unit testing
 *
 * @author Mike Conway, DICE (www.irods.org)
 *
 */
public class IRODSTestAssertionException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 5512182235772432580L;

	public IRODSTestAssertionException() {
	}

	public IRODSTestAssertionException(final String arg0) {
		super(arg0);
	}

	public IRODSTestAssertionException(final Throwable arg0) {
		super(arg0);
	}

	public IRODSTestAssertionException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

}
