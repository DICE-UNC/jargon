package org.irods.jargon.conveyor.core;

import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.irods.jargon.conveyor.core.callables.ConveyorCallableFactory;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private final ConveyorCallableFactory conveyorCallableFactory = new ConveyorCallableFactory();
	private Future<ConveyorExecutionFuture> currentTransfer = null;

	/**
	 * Thread pool (just 1 for now) that runs service
	 */
	private final ExecutorService pool = Executors.newSingleThreadExecutor();

	/**
	 * Injected properties that control functionality of the conveyor
	 */
	private Properties executorServiceProperties = null;

	private ConveyorService conveyorService;

	public Properties getExecutorServiceProperties() {
		synchronized (this) {
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

		synchronized (this) {
			this.executorServiceProperties = executorServiceProperties;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.ConveyorExecutorService#unlockQueue()
	 */
	/*
	 * public void unlockQueue() { executorLock.release(); }
	 */

	@Override
	public void processTransfer(final TransferAttempt transferAttempt,
			ConveyorService conveyorService) throws ConveyorBusyException,
			ConveyorExecutionException {

		log.info("processTransferAndHandleReturn");

		if (transferAttempt == null) {
			throw new IllegalArgumentException("null transferAttempt");
		}

		if (conveyorService == null) {
			throw new IllegalArgumentException("null conveyorService");
		}

		/*
		 * Note that the dequeue operation in the queue manager has already set
		 * the queue status to busy. The callable below will handle any further
		 * updates to the queue status based on callbacks from the running
		 * transfer process.
		 */

		log.info("submitting transferAttempt:{}", transferAttempt);
		synchronized (this) {

			Callable<ConveyorExecutionFuture> callable = conveyorCallableFactory
					.instanceCallableForOperation(transferAttempt,
							conveyorService);

			this.currentTransfer = pool.submit(callable);

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.ConveyorExecutorService#shutdown()
	 */
	@Override
	public void shutdown() {
		synchronized (this) {
			pool.shutdownNow();
		}
	}

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
			notifyCallbackListenerOfChangeInStatus();
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
			notifyCallbackListenerOfChangeInStatus();
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
				setRunningStatus(RunningStatus.PAUSED);
			} else {
				log.info("setting idle");
				setRunningStatus(RunningStatus.IDLE);
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
		// log.debug("setBusyForOperation()... current status:{}",
		// runningStatus);
		synchronized (statusSynchronizingObject) {
			if (runningStatus == RunningStatus.BUSY) {
				// log.debug("will return busy exception");
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
				this.setRunningStatus(RunningStatus.PAUSED_BUSY);
			} else {
				log.info("set busy");
				this.setRunningStatus(RunningStatus.BUSY);
			}
		}
	}

	/**
	 * @return the currentTransfer
	 */
	public Future<ConveyorExecutionFuture> getCurrentTransfer() {
		return currentTransfer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.ConveyorExecutorService#getQueueStatus()
	 */
	@Override
	public QueueStatus getQueueStatus() {
		return new QueueStatus(runningStatus, errorStatus);
	}

	@Override
	public ConveyorService getConveyorService() {
		return this.conveyorService;
	}

	@Override
	public void setConveyorService(ConveyorService conveyorService) {
		this.conveyorService = conveyorService;
	}

	/**
	 * Be sure to always call the set and get error and running status methods,
	 * so that those setters will properly notify the callback listener of a
	 * change in that status! This method will send a notification of a new
	 * QueueStatus up to the listener (e.g. the iDrop gui).
	 */
	private void notifyCallbackListenerOfChangeInStatus() {
		ConveyorCallbackListener listener = conveyorService
				.getConveyorCallbackListener();
		if (listener != null) {
			synchronized (statusSynchronizingObject) {
				listener.setQueueStatus(getQueueStatus());
			}
		}
	}

}