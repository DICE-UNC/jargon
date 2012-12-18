/**
 * 
 */
package org.irods.jargon.conveyor.core;

/**
 * An immutable value object that represents the state of the transfer queue
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class QueueStatus {

	public enum RunningStatus {
		RUNNING, IDLE, PAUSED, CANCELLING, QUIESCE
	}
	
	public enum ErrorStatus {
		OK, WARNING, ERROR
	}
	
	private final RunningStatus runningStatus;
	public RunningStatus getRunningStatus() {
		return runningStatus;
	}


	public ErrorStatus getErrorStatus() {
		return errorStatus;
	}


	private final ErrorStatus errorStatus;
	
	
	/**
	 * 
	 */
	public QueueStatus(final RunningStatus runningStatus, final ErrorStatus errorStatus) {
		
		if (runningStatus == null) {
			throw new IllegalArgumentException("null running status");
		}
		
		if (errorStatus == null) {
			throw new IllegalArgumentException("null error status");
		}
		
		this.runningStatus = runningStatus;
		this.errorStatus = errorStatus;
	}

}
