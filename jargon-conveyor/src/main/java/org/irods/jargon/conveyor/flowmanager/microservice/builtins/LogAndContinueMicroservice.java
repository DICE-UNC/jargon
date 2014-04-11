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
 * level, dump interesting things
 * 
 * @author Mike Conway - DICE
 *
 */
public class LogAndContinueMicroservice extends Microservice {

	private static final Logger log = LoggerFactory
			.getLogger(LogAndContinueMicroservice.class);

	@Override
	public ExecResult execute() throws MicroserviceException {
		log.info("execute()");
		return ExecResult.CONTINUE;

	}

}
