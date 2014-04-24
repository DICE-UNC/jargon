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
 * A stub microservice that will never say go ahead but will end normally with a
 * <code>TERMINATE_FLOW_FAIL_PRECONDITION</code>
 * 
 * @author Mike Conway - DICE
 * 
 */
public class AlwaysDontRunConditionMicroservice extends ConditionMicroservice {

	private static final Logger log = LoggerFactory
			.getLogger(AlwaysDontRunConditionMicroservice.class);

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
		this.evaluateContext();
		return ExecResult.TERMINATE_FLOW_FAIL_PRECONDITION;

	}

}
