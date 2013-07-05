/**
 * 
 */
package org.irods.jargon.conveyor.unittest.utils;

import org.irods.jargon.conveyor.core.ConveyorBusyException;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handy utilities for tests that need to run transfers via conveyer and test
 * the results
 * 
 * @author Mike Conway - DICE (www.irods.org) see
 *         https://code.renci.org/gf/project/jargon/
 * 
 */
public class TransferTestRunningUtilities {

	private static final Logger log = LoggerFactory
			.getLogger(TransferTestRunningUtilities.class);

	/**
	 * Given a <code>ConveyorService</code> running a transfer, keep pinging and
	 * sleeping until the queue is available, up until a timeout
	 * 
	 * @param conveyorService
	 * @param timeoutInSeconds
	 * @throws JargonException
	 */
	public static void waitForTransferToRunOrTimeout(
			final ConveyorService conveyorService, final int timeoutInSeconds)
			throws JargonException {

		final long timeout = timeoutInSeconds * 1000;
		final long sleepTime;
		if (timeoutInSeconds == -1) {
			sleepTime = 1000;
		} else {
			sleepTime = timeout / 10;

		}

		final long startMillis = System.currentTimeMillis();

		long elapsed;
		while (true) {
			elapsed = System.currentTimeMillis() - startMillis;
			if (timeoutInSeconds == -1) {
				// ignore timeout
			} else if (elapsed >= timeout) {
				throw new JargonException("timeout!");
			}

			try {

				conveyorService.getConveyorExecutorService()
						.setBusyForAnOperation();
				conveyorService.getConveyorExecutorService()
						.setOperationCompleted();
				break;
			} catch (ConveyorBusyException cbe) {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					break;
				}
				continue;
			}

		}

	}

	/**
	 * Check the running transfer, if it's going, then wait until a certain
	 * number of files have been processed then exit
	 * 
	 * @param conveyorService
	 * @param timeoutInSeconds
	 * @param numberOfFiles
	 * @throws JargonException
	 */
	public static void waitForTransferToTransferNFiles(
			final ConveyorService conveyorService, final int timeoutInSeconds,
			final int numberOfFiles) throws JargonException {

		final long timeout = timeoutInSeconds * 1000;
		final long sleepTime;
		if (timeoutInSeconds == -1) {
			sleepTime = 1000;
		} else {
			sleepTime = timeout / 10;

		}

		final long startMillis = System.currentTimeMillis();

		long elapsed;
		while (true) {
			elapsed = System.currentTimeMillis() - startMillis;
			if (timeoutInSeconds == -1) {
				// ignore timeout
			} else if (elapsed >= timeout) {
				throw new JargonException("timeout!");
			}

			try {

				conveyorService.getConveyorExecutorService()
						.setBusyForAnOperation();
				conveyorService.getConveyorExecutorService()
						.setOperationCompleted();
				break;
			} catch (ConveyorBusyException cbe) {

				int nbrFilesSoFar = conveyorService
						.getConveyorExecutorService()
						.getNumberFilesTransferredSoFarInCurrentTransfer();
				log.info("nbr files so far:{}", nbrFilesSoFar);
				if (nbrFilesSoFar >= numberOfFiles) {
					break;
				}

				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					break;
				}
				continue;
			}

		}

	}

}
