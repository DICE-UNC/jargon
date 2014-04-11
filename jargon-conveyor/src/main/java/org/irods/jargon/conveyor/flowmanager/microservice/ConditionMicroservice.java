/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.microservice;

/**
 * Microservice super class for a condition. By default this will return a
 * continue
 * 
 * @author Mike Conway - DICE
 *
 */
public class ConditionMicroservice extends Microservice {

	/**
	 * 
	 */
	public ConditionMicroservice() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.flowmanager.microservice.Microservice#execute()
	 */
	@Override
	public ExecResult execute() throws MicroserviceException {
		return ExecResult.CONTINUE;
	}

}
