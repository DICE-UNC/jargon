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
 * level, dump interesting things. It will respond with a call to cancel the op
 * 
 * @author Mike Conway - DICE
 * 
 */
public class LogAndCancelMicroservice extends Microservice {

	private static final Logger log = LoggerFactory
			.getLogger(LogAndCancelMicroservice.class);

	@Override
	public ExecResult execute(final TransferStatus transferStatus)
			throws MicroserviceException {
		log.info("execute()");
		return ExecResult.CANCEL_OPERATION;

	}

}
