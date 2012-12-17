/**
 * 
 */
package org.irods.jargon.conveyor.core;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of an executor of conveyor processes. The current
 * implementation runs one conveyor process at a time. Future implementations
 * may run multiple processes, but this may require other refactoring.
 * <p/>
 * The queue uses a <code>Semaphore</code> to regulate the timing of changes to
 * the database, and to manage access to the queue. Callers must call teh
 * appropriate <code>lockQueue</code> and <code>unlockQueue</code> methods.
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
	private final Semaphore executorLock = new Semaphore(MAX_AVAILABLE, true);
	/**
	 * Injected properties that control functionality of the conveyor
	 */
	private Properties executorServiceProperties = new Properties();

	public Properties getExecutorServiceProperties() {
		return executorServiceProperties;
	}

	public void setExecutorServiceProperties(Properties executorServiceProperties) {
		this.executorServiceProperties = executorServiceProperties;
	}

	private final Logger log = LoggerFactory
			.getLogger(ConveyorExecutorServiceImpl.class);

	@Override
	public void lockQueue() throws InterruptedException {
		executorLock.acquire();
	}
	
	public void lockQueueWithTimeout() {
		
	}

	@Override
	public void unlockQueue() {
		executorLock.release();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.ConveyorExecutorService#
	 * executeConveyorCallable
	 * (org.irods.jargon.conveyor.core.AbstractConveyorCallable)
	 */
	@Override
	public ConveyorExecutionFuture executeConveyorCallable(
			final AbstractConveyorCallable conveyorCallable)
			throws ConveyorExecutionException {

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.ConveyorExecutorService#shutdown()
	 */
	@Override
	public synchronized void shutdown() {
		pool.shutdownNow();
	}

}