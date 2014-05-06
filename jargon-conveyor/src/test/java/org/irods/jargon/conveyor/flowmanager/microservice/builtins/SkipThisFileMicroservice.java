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
 * level, dump interesting things.
 * <p/>
 * This microservice will ask to skip this file
 * 
 * @author Mike Conway - DICE
 * 
 */
public class SkipThisFileMicroservice extends Microservice {

	private static final Logger log = LoggerFactory
			.getLogger(SkipThisFileMicroservice.class);

	@Override
	public ExecResult execute(final TransferStatus transferStatus)
			throws MicroserviceException {
		log.info("execute()");
		return ExecResult.SKIP_THIS_FILE;

	}

}
