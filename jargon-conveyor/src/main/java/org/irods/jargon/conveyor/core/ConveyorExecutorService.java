/**
 * 
 */
package org.irods.jargon.conveyor.core;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.irods.jargon.transfer.dao.domain.TransferAttempt;

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

	ExecutorService executor = Executors.newFixedThreadPool(1);

	/**
	 * @return the currentTransferAttempt
	 */
	TransferAttempt getCurrentTransferAttempt();

	void requestCancel(final TransferAttempt transferAttempt)
			throws ConveyorExecutionException;

	public enum ErrorStatus {
		OK, WARNING, ERROR
	}

	public enum RunningStatus {
		IDLE, PAUSED, BUSY, PAUSED_BUSY
	}

	/***
	 * Given a properly configured transfer attempt, execute the transfer and
	 * cause all of the various updates to occur
	 * 
	 * @param conveyorCallable
	 *            {@link ConveyorCallable} that will be run
	 * 
	 * @throws ConvyorExecutionTimeoutException
	 *             if an execute is called with <code>withTimout</code> as
	 *             <code>true</code>, and the operation times out
	 * @throws ConveyorExecutionException
	 *             for any exception in the actual execution
	 */
	void processTransfer(final TransferAttempt transferAttempt,
			final ConveyorService conveyorService)
			throws ConveyorBusyException, ConveyorExecutionException;

	/**
	 * Shut down the underlying pool (will attempt to do so in an orderly
	 * fashion). Note that this will block on a currently running execution, and
	 * is meant for cleanup.
	 */
	void shutdown();

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

	/**
	 * Return an object that reflects the status of the queue (running and error
	 * status in a value object)
	 * 
	 * @return {@link QueueStatus}
	 */
	QueueStatus getQueueStatus();

	ConveyorService getConveyorService();

	void setConveyorService(final ConveyorService conveyorService);

	/**
	 * Convenience method to peek at the number of files tranferred so far in
	 * the current transfer
	 * 
	 * @return <code>int</code> with number of files transferred so far. Note
	 *         that this will return 0 if a current transfer is not available
	 */
	int getNumberFilesTransferredSoFarInCurrentTransfer();

	/**
	 * Request that the conveyor service pauses. This will cause a cancellation
	 * of any currently running process too. The queue will halt until unpause
	 * is called.
	 * 
	 * @throws ConveyorExecutionException
	 */
	void requestPause() throws ConveyorExecutionException;

	/**
	 * Unpause the queue and release the next item.
	 * 
	 * @throws ConveyorExecutionException
	 */
	void requestResumeFromPause() throws ConveyorExecutionException;

}
