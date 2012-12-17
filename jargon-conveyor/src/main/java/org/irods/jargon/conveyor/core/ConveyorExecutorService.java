/**
 * 
 */
package org.irods.jargon.conveyor.core;

/**
 * Interface for the mechanism to process transfer operations.  This object provides the common lock for 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface ConveyorExecutorService {

	public static final int MAX_AVAILABLE = 1;
	
	
	 /**
	  * 
	  * Execute the given conveyor process and return a processing result.  This method will block until the process terminates.
	  * @param conveyorCallable
	  * @return
	  * @throws ConveyorExecutionException
	  */
	ConveyorExecutionFuture executeConveyorCallable(final AbstractConveyorCallable conveyorCallable)
			throws ConveyorExecutionException;

	/**
	 * Shut down the underlying pool (will attempt to do so in an orderly fashion).  Note that this will block on a currently running execution, and is meant for cleanup.
	 */
	void shutdown();

	public abstract void unlockQueue();

	public abstract void lockQueue() throws InterruptedException;

}
