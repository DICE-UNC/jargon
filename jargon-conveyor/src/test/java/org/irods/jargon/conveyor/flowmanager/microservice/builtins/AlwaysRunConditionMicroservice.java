/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.microservice.builtins;

import org.irods.jargon.conveyor.flowmanager.microservice.ConditionMicroservice;
import org.irods.jargon.conveyor.flowmanager.microservice.MicroserviceException;
import org.irods.jargon.core.transfer.TransferStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A stub microservice that will always say go ahead.
 * 
 * @author Mike Conway - DICE
 * 
 */
public class AlwaysRunConditionMicroservice extends ConditionMicroservice {

	private static final Logger log = LoggerFactory
			.getLogger(AlwaysRunConditionMicroservice.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.flowmanager.microservice.ConditionMicroservice
	 * #execute()
	 */
	@Override
	public ExecResult execute(final TransferStatus transferStatus)
			throws MicroserviceException {
		log.info("execute()");
		evaluateContext();
		return ExecResult.CONTINUE;

	}

}
