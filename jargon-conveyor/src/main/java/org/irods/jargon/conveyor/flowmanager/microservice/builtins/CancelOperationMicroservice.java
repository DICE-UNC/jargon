/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.microservice.builtins;

import org.irods.jargon.conveyor.flowmanager.microservice.Microservice;
import org.irods.jargon.conveyor.flowmanager.microservice.MicroserviceException;
import org.irods.jargon.core.transfer.TransferStatus;

/**
 * Simply cancels the chain
 * 
 * @author Mike Conway - DICE
 * 
 */
public class CancelOperationMicroservice extends Microservice {

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
		return ExecResult.CANCEL_OPERATION;
	}

}
