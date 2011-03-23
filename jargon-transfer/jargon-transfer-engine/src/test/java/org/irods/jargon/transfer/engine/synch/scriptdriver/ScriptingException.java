package org.irods.jargon.transfer.engine.synch.scriptdriver;

/**
 * Exception processing a testing script
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ScriptingException extends Exception {

	/**
	 * 
	 */
	public ScriptingException() {
	}

	/**
	 * @param arg0
	 */
	public ScriptingException(final String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public ScriptingException(final Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public ScriptingException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

}
