/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.microservice;

import org.irods.jargon.core.transfer.TransferStatus;

/**
 * Default error handler will propogate the abort
 * 
 * @author Mike Conway - DICE
 * 
 */
public class ErrorHandlerMicroservice extends Microservice {

	/**
	 * 
	 */
	public ErrorHandlerMicroservice() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.flowmanager.microservice.Microservice#execute()
	 */
	@Override
	public ExecResult execute(final TransferStatus transferStatus)
			throws MicroserviceException {
		return ExecResult.ABORT_AND_TRIGGER_ANY_ERROR_HANDLER;
	}

}
