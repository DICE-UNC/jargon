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
 * To help with test assertions, this will also keep a count of times a
 * microservice is invoked in the InvocationContext object
 * 
 * @author Mike Conway - DICE
 * 
 */
public class LogAndContinueMicroservice extends Microservice {

	public static final String COUNT_KEY = "LogAndContinueMicroservice:COUNT_KEY";

	private static final Logger log = LoggerFactory
			.getLogger(LogAndContinueMicroservice.class);

	@Override
	public ExecResult execute(final TransferStatus transferStatus)
			throws MicroserviceException {
		log.info("execute()");

		Object countObjVal = getInvocationContext().getSharedProperties().get(
				COUNT_KEY);
		if (countObjVal == null) {
			getInvocationContext().getSharedProperties().put(COUNT_KEY, 1);
		} else {
			int count = (Integer) countObjVal;
			getInvocationContext().getSharedProperties()
					.put(COUNT_KEY, ++count);
		}

		return ExecResult.CONTINUE;

	}

}
