/**
 * 
 */
package org.irods.jargon.conveyor.core;

import java.util.Properties;

/**
 * Interface for the mechanism to process transfer operations. This object
 * provides the common lock for controlling access to the queue, so that certain
 * operations that may effect a transfer that is currently running will not
 * occur until the queue is idle.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface ConveyorExecutorService {

	public static final int MAX_AVAILABLE = 1;
	public static final String TRY_LOCK_TIMEOUT = "try.lock.timeout.seconds";

	public enum ErrorStatus {
		OK, WARNING, ERROR
	}

	public enum RunningStatus {
		IDLE, PROCESSING, PAUSED, BUSY, PAUSED_BUSY
	}

	/**
	 * Execute the given conveyor process and return a processing result. This
	 * method will block until the process terminates.
	 * <p/>
	 * Note that the various lock and unlock methods in this class are used to
	 * coordinate queue operations, and it is the responsibility of the caller
	 * to obtain and release these locks for any operations that depend on the
	 * state of the queue (for example, database operations that could alter a
	 * transfer currently running). <b>This method will implicitly obtain a
	 * lock, and so a call to lock the queue should not be made before doing
	 * this operation</b>.
	 * <p/>
	 * Note that the <code>withTimeout</code> flag indicates that a timeout
	 * should be enforced for getting a lock from the queue. If the lock attempt
	 * is not successful, a <code>ConveyorExecutionException</code> will occur.
	 * This is useful for interactive interfaces
	 * 
	 * @param conveyorCallable
	 *            {@link ConveyorCallable} that will be run
	 * @param withTimeout
	 *            <code>boolean</code> with <code>true</code> indicating that
	 *            the call to obtain a lock should have a time-out
	 * @return <code>boolean</code> that indicates that a lock was acquired,
	 *         <code>false</code> means the lock attempt timed out
	 * @throws ConvyorExecutionTimeoutException
	 *             if an execute is called with <code>withTimout</code> as
	 *             <code>true</code>, and the operation times out
	 * @throws ConveyorExecutionException
	 *             for any exception in the actual execution
	 */
	ConveyorExecutionFuture executeConveyorCallable(
			final AbstractConveyorCallable conveyorCallable,
			final boolean withTimeout) throws ConveyorBusyException,
			ConveyorExecutionException;

	/**
	 * Shut down the underlying pool (will attempt to do so in an orderly
	 * fashion). Note that this will block on a currently running execution, and
	 * is meant for cleanup.
	 */
	void shutdown();

	/**
	 * Release a lock on the queue
	 */
	// void unlockQueue();

	/**
	 * Try an immediate lock on the queue
	 * 
	 * @throws ConveyorExecutionException
	 */
	// void lockQueue() throws ConveyorExecutionException;

	/**
	 * Try and obtain a lock on the queue, with a timeout defined in the
	 * provided properties defined by the key "try.lock.timeout.seconds"
	 * 
	 * @throws ConveyorBusyException
	 *             if a time-out occurs obtaining the lock
	 * @throws ConveyorExecutionException
	 */
	/*
	 * void lockQueueWithTimeout() throws ConveyorBusyException,
	 * ConveyorExecutionException;
	 */

	/**
	 * Inject <code>Properties</code> that can control aspects of this service
	 * 
	 * @param executorServiceProperties
	 */
	void setExecutorServiceProperties(Properties executorServiceProperties);

	/**
	 * See if the system is in a state where I can perform an operation that may
	 * affect a running operation. If I am in a state to do such an operation,
	 * the transfer queue status will be set to BUSY or will remain PAUSED.
	 * <p/>
	 * Note that it is incumbent on an operation that grabs the queue and sets
	 * it to busy to return it to a paused or idle status by calling
	 * <code>setOperationCompleted()</code>. This sequence is meant for
	 * operations that should occur when the queue is not running (e.g. purging
	 * the queue, changing the pass phrase).
	 * <p/>
	 * Note that this operation will return a <code>ConveyorBusyException</code>
	 * if the queue is busy when the operation is requested. This can be
	 * presented to the caller as an instruction to complete such operations
	 * when the queue is paused or idle.
	 * 
	 * @throws ConveyorBusyException
	 *             if the queue is busy
	 */
	public abstract void setBusyForAnOperation() throws ConveyorBusyException;

	/**
	 * This method releases the queue from a busy or 'busy and paused' status
	 * back to idle or paused, and is meant to finish an operation that should
	 * block the queue running, as set by the <code>setBusyForOperation()</code>
	 * method.
	 * <p/>
	 * Clients that call the <code>setBusyForOperation()</code> are required to
	 * finish by calling this method.
	 */
	void setOperationCompleted();

	void setRunningStatus(final RunningStatus runningStatus);

	RunningStatus getRunningStatus();

	void setErrorStatus(final ErrorStatus errorStatus);

	ErrorStatus getErrorStatus();

}
