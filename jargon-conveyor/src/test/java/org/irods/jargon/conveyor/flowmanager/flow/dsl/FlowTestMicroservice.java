/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow.dsl;

import org.irods.jargon.conveyor.flowmanager.microservice.Microservice;
import org.irods.jargon.conveyor.flowmanager.microservice.MicroserviceException;
import org.irods.jargon.core.transfer.TransferStatus;

/**
 * Dummy microservice for flow testing
 * 
 * @author Mike Conway - DICE
 * 
 */
public class FlowTestMicroservice extends Microservice {

	/**
	 * 
	 */
	public FlowTestMicroservice() {
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
		return null;
	}

}
