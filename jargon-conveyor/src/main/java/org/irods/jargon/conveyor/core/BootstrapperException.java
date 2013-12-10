/**
 * 
 */
package org.irods.jargon.conveyor.core;

/**
 * Exception bootstrapping the conveyor framework
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class BootstrapperException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6504361021682319203L;

	/**
	 * 
	 */
	public BootstrapperException() {
	}

	/**
	 * @param message
	 */
	public BootstrapperException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public BootstrapperException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public BootstrapperException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
