/**
 * 
 */
package org.irods.jargon.conveyor.core;

import org.irods.jargon.conveyor.core.ConveyorExecutorService.ErrorStatus;
import org.irods.jargon.conveyor.core.ConveyorExecutorService.RunningStatus;

/**
 * An immutable value object that represents the state of the transfer queue
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class QueueStatus {

	private final RunningStatus runningStatus;

	private final ErrorStatus errorStatus;

	/**
	 * 
	 */
	public QueueStatus(final RunningStatus runningStatus,
			final ErrorStatus errorStatus) {

		if (runningStatus == null) {
			throw new IllegalArgumentException("null running status");
		}

		if (errorStatus == null) {
			throw new IllegalArgumentException("null error status");
		}

		this.runningStatus = runningStatus;
		this.errorStatus = errorStatus;
	}

	public RunningStatus getRunningStatus() {
		return runningStatus;
	}

	public ErrorStatus getErrorStatus() {
		return errorStatus;
	}

}
