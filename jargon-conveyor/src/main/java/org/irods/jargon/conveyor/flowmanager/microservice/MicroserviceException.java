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
	public MicroserviceException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public MicroserviceException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public MicroserviceException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public MicroserviceException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
