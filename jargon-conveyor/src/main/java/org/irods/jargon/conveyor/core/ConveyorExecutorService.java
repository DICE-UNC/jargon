/**
 * 
 */
package org.irods.jargon.conveyor.core;

import java.util.Properties;

/**
 * Interface for the mechanism to process transfer operations.  This object provides the common lock for controlling access to the queue, 
 * so that certain operations that may effect a transfer that is currently running will not occur until the queue is idle.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface ConveyorExecutorService {

	public static final int MAX_AVAILABLE = 1;
	public static final String TRY_LOCK_TIMEOUT = "try.lock.timeout.seconds";
	
	
	 /**
	  * Execute the given conveyor process and return a processing result.  This method will block until the process terminates.  
	  * <p/>
	  * Note that the various lock and unlock methods in this class are used to coordinate queue operations, and it is the responsibility
	  * of the caller to obtain and release these locks for any operations that depend on the state of the queue (for example, database operations
	  * that could alter a transfer currently running).  <b>This method will implicitly obtain a lock, and so a call to lock the queue should
	  * not be made before doing this operation</b>.
	  * <p/>
	  * Note that the <code>withTimeout</code> flag indicates that a timeout should be enforced for getting a lock from the queue.  If the 
	  * lock attempt is not successful, a <code>ConveyorExecutionException</code> will occur.  This is useful for interactive interfaces
	  * 
	  * @param conveyorCallable {@link ConveyorCallable} that will be run
	  * @param withTimeout <code>boolean</code> with <code>true</code> indicating that the call to obtain a lock should have a time-out
	  * @return <code>boolean</code> that indicates that a lock was acquired, <code>false</code> means the lock attempt timed out
	 * @throws InterruptedException 
	 * @throws ConvyorExecutionTimeoutException if an execute is called with <code>withTimout</code> as <code>true</code>, and the operation times out
	 * @throws ConveyorExecutionException for any exception in the actual execution
	  */
	ConveyorExecutionFuture executeConveyorCallable(final AbstractConveyorCallable conveyorCallable, final boolean withTimeout)
			throws InterruptedException,ConveyorExecutionTimeoutException, ConveyorExecutionException;

	/**
	 * Shut down the underlying pool (will attempt to do so in an orderly fashion).  Note that this will block on a currently running execution, and is meant for cleanup.
	 */
	void shutdown();

	/**
	 * Release a lock on the queue
	 */
	void unlockQueue();

	/**
	 * Try an immediate lock on the queue
	 * @throws InterruptedException
	 */
	void lockQueue() throws InterruptedException;

	/**
	 * Try and pbtain a lock on the queue, with a timeout defined in the provided properties defined by the key 
	 * "try.lock.timeout.seconds"
	 * @return 
	 * @throws InterruptedException
	 * @throws ConveyorExecutionException 
	 */
	boolean lockQueueWithTimeout() throws InterruptedException, ConveyorExecutionException;

	/**
	 * Inject <code>Properties</code> that can control aspects of this service
	 * @param executorServiceProperties
	 */
	void setExecutorServiceProperties(Properties executorServiceProperties);

}
