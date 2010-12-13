/**
 *
 */
package org.irods.jargon.testutils.icommandinvoke;

/**
 * General exception invoking an irods command
 * @author Mike Conway, DICE (www.irods.org)
 *
 */
public class IcommandException extends Exception {

	/**
	 * 
	 */
	public IcommandException() {
		super();
	}

	/**
	 * @param arg0
	 */
	public IcommandException(String arg0) {
		super(arg0);
		
	}

	/**
	 * @param arg0
	 */
	public IcommandException(Throwable arg0) {
		super(arg0);
		
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public IcommandException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		
	}

}
