/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.microservice.builtins;

import org.irods.jargon.conveyor.flowmanager.microservice.Microservice;
import org.irods.jargon.conveyor.flowmanager.microservice.MicroserviceException;
import org.irods.jargon.core.transfer.TransferStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A basic microservice that will send a log message, and depending on log
 * level, dump interesting things. It will respond with a call to skip the rest
 * of the chain
 * 
 * @author Mike Conway - DICE
 * 
 */
public class LogAndSkipChainMicroservice extends Microservice {

	private static final Logger log = LoggerFactory
			.getLogger(LogAndSkipChainMicroservice.class);

	@Override
	public ExecResult execute(final TransferStatus transferStatus)
			throws MicroserviceException {
		log.info("execute()");
		return ExecResult.SKIP_THIS_CHAIN;

	}

}
