package org.irods.jargon.conveyor.core;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FIXME: DO I NEED SEMAPHORE?
/**
 * Implementation of an executor of conveyor processes. The current
 * implementation runs one conveyor process at a time. Future implementations
 * may run multiple processes, but this may require other refactoring.
 * <p/>
 * The queue uses a <code>Semaphore</code> to manage access to the queue.
 * Callers must call the appropriate <code>lockQueue</code> and
 * <code>unlockQueue</code> methods.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ConveyorExecutorServiceImpl implements ConveyorExecutorService {

	private final Logger log = LoggerFactory
			.getLogger(ConveyorExecutorServiceImpl.class);

	private ErrorStatus errorStatus = ErrorStatus.OK;
	private RunningStatus runningStatus = RunningStatus.IDLE;
	private Object statusSynchronizingObject = new Object();

	/**
	 * Thread pool (just 1 for now) that runs service
	 */
	private final ExecutorService pool = Executors.newSingleThreadExecutor();
	private ConveyorExecutionFuture future;
	// private final Semaphore executorLock = new Semaphore(MAX_AVAILABLE,
	// true);

	/**
	 * Injected properties that control functionality of the conveyor
	 */
	private Properties executorServiceProperties = null;
	private final Object executorServicePropertiesSynchronizingObject = new Object();

	public Properties getExecutorServiceProperties() {
		synchronized (executorServicePropertiesSynchronizingObject) {
			return executorServiceProperties;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.ConveyorExecutorService#
	 * setExecutorServiceProperties(java.util.Properties)
	 */
	@Override
	public void setExecutorServiceProperties(
			final Properties executorServiceProperties) {
		if (executorServiceProperties == null) {
			throw new IllegalArgumentException("null executorServiceProperties");
		}

		synchronized (executorServicePropertiesSynchronizingObject) {
			this.executorServiceProperties = executorServiceProperties;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.ConveyorExecutorService#lockQueue()
	 */
	/*
	 * public void lockQueue() throws ConveyorExecutionException { try {
	 * executorLock.acquire(); } catch (InterruptedException e) {
	 * log.error("interruptedException", e); throw new
	 * ConveyorExecutionException(e); } }
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.ConveyorExecutorService#lockQueueWithTimeout
	 * ()
	 */
	/*
	 * public void lockQueueWithTimeout() throws ConveyorBusyException,
	 * ConveyorExecutionException { int timeout = getTimeoutFromProperties();
	 * try { boolean acquired = executorLock.tryAcquire(timeout,
	 * TimeUnit.SECONDS); if (!acquired) { throw new
	 * ConveyorBusyException("timed out getting lock"); } } catch
	 * (InterruptedException e) { throw new
	 * ConveyorExecutionException("error in locking queue"); } }
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.ConveyorExecutorService#unlockQueue()
	 */
	/*
	 * public void unlockQueue() { executorLock.release(); }
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.ConveyorExecutorService#
	 * executeConveyorCallable
	 * (org.irods.jargon.conveyor.core.AbstractConveyorCallable)
	 */
	@Override
	public ConveyorExecutionFuture executeConveyorCallable(
			final AbstractConveyorCallable conveyorCallable,
			final boolean withTimeout) throws ConveyorBusyException,
			ConveyorExecutionException {

		log.info("executeConveyorCallable");

		if (conveyorCallable == null) {
			throw new IllegalArgumentException("null conveyorCallable");
		}

		log.info("submitting callable:{}", conveyorCallable);

		// here is the lock acquisition, so this isn't called with a lock
		// acquired, it's in the method comment
		/*
		 * if (withTimeout) { log.info("obtain lock with a timeout");
		 * lockQueueWithTimeout(); } else {
		 * log.info("obtain lock without a timeout"); lockQueue(); }
		 */

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
		} finally {
			log.info("unlock the queue");
			// unlockQueue();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.ConveyorExecutorService#shutdown()
	 */
	@Override
	public void shutdown() {
		pool.shutdownNow();
	}

	/**
	 * Get the timeout value for acquiring a lock to the queue from the
	 * properties
	 * 
	 * @return
	 * @throws ConveyorExecutionException
	 */
	/*
	 * private int getTimeoutFromProperties() throws ConveyorExecutionException
	 * { synchronized (executorServicePropertiesSynchronizingObject) { try {
	 * return PropertyUtils.verifyPropExistsAndGetAsInt(
	 * executorServiceProperties, ConveyorExecutorService.TRY_LOCK_TIMEOUT); }
	 * catch (NullPointerException npe) { throw new
	 * ConveyorExecutionException("no timeout property set"); } }
	 * 
	 * }
	 */

	/**
	 * @return the errorStatus
	 */
	@Override
	public ErrorStatus getErrorStatus() {
		synchronized (statusSynchronizingObject) {
			return errorStatus;
		}
	}

	/**
	 * @param errorStatus
	 *            the errorStatus to set
	 */
	@Override
	public void setErrorStatus(final ErrorStatus errorStatus) {

		if (errorStatus == null) {
			throw new IllegalArgumentException("null errorStatus");
		}

		synchronized (statusSynchronizingObject) {
			this.errorStatus = errorStatus;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.ConveyorExecutorService#getRunningStatus()
	 */
	@Override
	public RunningStatus getRunningStatus() {
		synchronized (statusSynchronizingObject) {
			return runningStatus;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.ConveyorExecutorService#setRunningStatus
	 * (org.irods.jargon.conveyor.core.ConveyorExecutorService.RunningStatus)
	 */
	@Override
	public void setRunningStatus(final RunningStatus runningStatus) {

		if (runningStatus == null) {
			throw new IllegalArgumentException("null runningStatus");
		}

		synchronized (statusSynchronizingObject) {
			this.runningStatus = runningStatus;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.ConveyorExecutorService#setOperationCompleted
	 * ()
	 */
	@Override
	public void setOperationCompleted() {
		log.info("setOperationCompleted()");
		synchronized (statusSynchronizingObject) {
			if (runningStatus == RunningStatus.PAUSED_BUSY) {
				log.info("setting paused");
				runningStatus = RunningStatus.PAUSED;
			} else {
				log.info("setting idle");
				runningStatus = RunningStatus.IDLE;
			}
		}
	}

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
	@Override
	public void setBusyForAnOperation() throws ConveyorBusyException {
		log.info("setBusyForOperation()... current status:{}", runningStatus);
		synchronized (statusSynchronizingObject) {
			if (runningStatus == RunningStatus.BUSY
					|| runningStatus == RunningStatus.PROCESSING) {
				log.warn("will return busy exception");
				throw new ConveyorBusyException(
						"cannot perform operation, busy");
			}

			/*
			 * I don't want to overwrite a system set to paused, so set to busy
			 * if I'm idle, otherwise, it will be in a paused state and remain
			 * there
			 */

			if (runningStatus == RunningStatus.PAUSED) {
				log.info("setting to paused busy");
				runningStatus = RunningStatus.PAUSED_BUSY;
			} else {
				log.info("set busy");
				runningStatus = RunningStatus.BUSY;
			}

		}
	}

}