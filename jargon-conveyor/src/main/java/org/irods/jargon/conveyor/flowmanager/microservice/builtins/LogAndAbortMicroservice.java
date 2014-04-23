/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.microservice.builtins;

import org.irods.jargon.conveyor.flowmanager.microservice.Microservice;
import org.irods.jargon.conveyor.flowmanager.microservice.MicroserviceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A basic microservice that will send a log message, and depending on log
 * level, dump interesting things. It will return an abort flow.
 * 
 * @author Mike Conway - DICE
 * 
 */
public class LogAndAbortMicroservice extends Microservice {

	private static final Logger log = LoggerFactory
			.getLogger(LogAndAbortMicroservice.class);

	@Override
	public ExecResult execute() throws MicroserviceException {
		log.info("execute()");
		return ExecResult.ABORT_AND_TRIGGER_ANY_ERROR_HANDLER;

	}

}
