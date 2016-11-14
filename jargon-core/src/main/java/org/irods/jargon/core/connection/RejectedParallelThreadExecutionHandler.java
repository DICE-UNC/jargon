package org.irods.jargon.core.connection;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for rejected executions of parallel threads
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class RejectedParallelThreadExecutionHandler implements
		RejectedExecutionHandler {

	public static final Logger log = LoggerFactory
			.getLogger(RejectedParallelThreadExecutionHandler.class);

	/**
	 *
	 */
	public RejectedParallelThreadExecutionHandler() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.util.concurrent.RejectedExecutionHandler#rejectedExecution(java.
	 * lang.Runnable, java.util.concurrent.ThreadPoolExecutor)
	 */
	@Override
	public void rejectedExecution(final Runnable arg0,
			final ThreadPoolExecutor arg1) {
		log.error("parallel transfer thread execution failed due to lack of threads in the transfer queue, please adjust the parameters in jargon.properties to increase the cap on the pool");
		throw new RejectedExecutionException(
				"Execution of parallel thread failed due to unavailable threads in the pool");
	}

}
