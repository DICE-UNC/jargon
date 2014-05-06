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
 * This microservice will ask to skip any file containing the specific string
 * SKIPME in the source or target of the transfer status
 * 
 * @author Mike Conway - DICE
 * 
 */
public class SkipFileWithSKIPMEInTheNameMicroservice extends Microservice {

	private static final Logger log = LoggerFactory
			.getLogger(SkipFileWithSKIPMEInTheNameMicroservice.class);

	public static final String SKIPME = "SKIPME";

	@Override
	public ExecResult execute(final TransferStatus transferStatus)
			throws MicroserviceException {
		log.info("execute()");

		boolean foundSkipMe = false;

		if (transferStatus.getSourceFileAbsolutePath().indexOf(SKIPME) > -1) {
			foundSkipMe = true;
		} else if (transferStatus.getTargetFileAbsolutePath().indexOf(SKIPME) > -1) {
			foundSkipMe = true;
		}

		if (foundSkipMe) {
			return ExecResult.SKIP_THIS_FILE;
		} else {
			return ExecResult.CONTINUE;
		}

	}

}
