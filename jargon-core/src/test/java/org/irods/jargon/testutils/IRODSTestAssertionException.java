/**
 * 
 */
package org.irods.jargon.testutils;

/**
 * Exception raised by an invalid assertion when unit testing
 * 
 * @author Mike Conway, DICE (www.irods.org)
 * @since
 * 
 */
public class IRODSTestAssertionException extends Exception {

	public IRODSTestAssertionException() {
	}

	public IRODSTestAssertionException(String arg0) {
		super(arg0);
	}

	public IRODSTestAssertionException(Throwable arg0) {
		super(arg0);
	}

	public IRODSTestAssertionException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
