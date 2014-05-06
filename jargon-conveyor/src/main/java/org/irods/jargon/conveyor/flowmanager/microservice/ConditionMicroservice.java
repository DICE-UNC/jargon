/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.microservice;

import org.irods.jargon.core.transfer.TransferStatus;

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
	 * org.irods.jargon.conveyor.flowmanager.microservice.Microservice#execute
	 * (org.irods.jargon.core.transfer.TransferStatus)
	 */
	@Override
	public ExecResult execute(final TransferStatus transferStatus)
			throws MicroserviceException {
		return ExecResult.CONTINUE;
	}

}
