/**
 * 
 */
package org.irods.jargon.conveyor.core;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of an executor of conveyor processes.  The current implementation runs one conveyor process at a time.  Future implementations 
 * may run multiple processes, but this may require other refactoring.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class ConveyorExecutorServiceImpl implements ConveyorExecutorService {
	
	/**
	 * Thread pool (just 1 for now) that runs service
	 */
	 private final ExecutorService pool = Executors.newSingleThreadExecutor();
	 private ConveyorExecutionFuture future;
	 
	 private final Logger log = LoggerFactory
				.getLogger(ConveyorExecutorServiceImpl.class);
	 
	
	 /* (non-Javadoc)
	 * @see org.irods.jargon.conveyor.core.ConveyorExecutorService#executeConveyorCallable(org.irods.jargon.conveyor.core.AbstractConveyorCallable)
	 */
	@Override
	public synchronized ConveyorExecutionFuture executeConveyorCallable(final AbstractConveyorCallable conveyorCallable) throws ConveyorExecutionException {
		 
		 log.info("executeConveyorCallable");
		 
		 if (conveyorCallable == null) {
			 throw new IllegalArgumentException("null conveyorCallable");
		 }
		 
		 log.info("submitting callable:{}", conveyorCallable);
		 
		 try {
			future = pool.submit(conveyorCallable).get();
			return future;
		} catch (InterruptedException e) {
			log.error("interruptedException running conveyorCallable", e);
			throw new ConveyorExecutionException(e);
		} catch (ExecutionException e) {
			log.error("ExecutionException running conveyorCallable", e);
			throw new ConveyorExecutionException(e);
		} catch (Exception e) {
			log.error("Unexpected Exception running conveyorCallable", e);
			throw new ConveyorExecutionException(e);
		}
		 
	 }
	
	
	/* (non-Javadoc)
	 * @see org.irods.jargon.conveyor.core.ConveyorExecutorService#shutdown()
	 */
	@Override
	public synchronized void shutdown() {
		pool.shutdownNow();
	}
	 
}