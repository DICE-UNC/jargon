/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.microservice;

/**
 * General exception in Microservice processing
 * 
 * @author Mike
 * 
 */
public class MicroserviceException extends Exception {

	private static final long serialVersionUID = 7521674518232217903L;

	/**
	 * 
	 */
	public MicroserviceException() {
	}

	/**
	 * @param arg0
	 */
	public MicroserviceException(final String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public MicroserviceException(final Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public MicroserviceException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

}
