/**
 *
 */
package org.irods.jargon.testutils;

/**
 * General exception caused by using Test utilities
 * @author Mike Conway, DICE (10/16/2009)
 * @since 10/16/2009
 *
 */
public class TestingUtilsException extends Exception {

	/**
	 * 
	 */
	public TestingUtilsException() {
		super();
	}

	/**
	 * @param arg0
	 */
	public TestingUtilsException(String arg0) {
		super(arg0);
		
	}

	/**
	 * @param arg0
	 */
	public TestingUtilsException(Throwable arg0) {
		super(arg0);
		
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public TestingUtilsException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		
	}

}
