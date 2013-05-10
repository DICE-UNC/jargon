/**
 * 
 */
package org.irods.jargon.conveyor.core.callables;

import java.util.concurrent.Callable;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.ConveyorExecutionFuture;
import org.irods.jargon.conveyor.core.ConveyorRuntimeException;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatus.TransferState;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract super class for a transfer running process
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public abstract class AbstractConveyorCallable implements
		Callable<ConveyorExecutionFuture>, TransferStatusCallbackListener {

	// private final Transfer transfer;
	private final Transfer transfer;
	private final TransferAttempt transferAttempt;
	private final ConveyorService conveyorService;

	private static final Logger log = LoggerFactory
			.getLogger(AbstractConveyorCallable.class);

	/**
	 * Convenience method to get a <code>IRODSAccount</code> with a decrypted
	 * password
	 * 
	 * @param gridAccount
	 * @return
	 * @throws ConveyorExecutionException
	 */
	IRODSAccount getIRODSAccountForGridAccount(final GridAccount gridAccount)
			throws ConveyorExecutionException {

		log.info("getIRODSAccountForGridAccount()");
		if (gridAccount == null) {
			throw new IllegalArgumentException("null gridAccount");
		}

		return this.getConveyorService().getGridAccountService()
				.irodsAccountForGridAccount(gridAccount);
	}

	/**
	 * Convenience method to get the <code>IRODSAccessObjectFactory</code>
	 * 
	 * @return
	 */
	IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return conveyorService.getIrodsAccessObjectFactory();
	}

	/**
	 * 
	 * Default constructor takes required hooks for bi-directional communication
	 * with caller of the transfer processor
	 * 
	 * @param transfer
	 * @param conveyorService
	 */
	public AbstractConveyorCallable(

	final Transfer transfer, final TransferAttempt transferAttempt,
			final ConveyorService conveyorService) {

		if (transfer == null) {
			throw new IllegalArgumentException("null transfer");
		}

		if (transferAttempt == null) {
			throw new IllegalArgumentException("null transferAttempt");
		}

		if (conveyorService == null) {
			throw new IllegalArgumentException("null conveyorService");
		}

		this.transfer = transfer;
		this.transferAttempt = transferAttempt;
		this.conveyorService = conveyorService;
	}

	/**
	 * This method is to be implemented by each specific transfer subclass. This
	 * is wrapped in the <code>call()</code> method of this abstract superclass,
	 * so that this superclass can wrap the call with appropriate
	 * setup/teardown, as well as error handling.
	 * 
	 * @param tcb
	 *            {@link TransferControlBlock} that contains transfer options
	 *            and other information shared with the underlying transfer
	 * @param irodsAccount
	 *            {@link IRODSAccount} that has been resolved for this operation
	 * 
	 * @throws ConveyorExecutionException
	 *             for errors in conveyor processing
	 * @throws JargonException
	 *             for errors in actual jargon-irods processing
	 */
	abstract void processCallForThisTransfer(final TransferControlBlock tcb,
			final IRODSAccount irodsAccount) throws ConveyorExecutionException,
			JargonException;

	/**
	 * Call method will invoke the <code>processCall</code> method to be
	 * implemented in each subclass. This will contain the specific code for
	 * each type of operation, and wrap it in error checking and other common
	 * processing.
	 * <p/>
	 * Note that any type of error is trapped in the catch, and conveyor will
	 * attempt to log these errors as part of the transfer attempt, so that the
	 * database reflects any issues at all with how the transfers are managed.
	 * It is possible that conveyor itself will have errors and be unable to
	 * flag these in the database, in which case it tries to log these and throw
	 * an exception, but in this case something is really wrong.
	 * <p/>
	 * The actual transfers are done in the callable, and if any sort of
	 * 'normal' iRODS or Jargon errors occur, these are not thrown from Jargon,
	 * because there is a registered callback listener. If the callback listener
	 * is present, Jargon will not throw an error from iRODS work, instead it
	 * puts the error in the callback, and normally, conveyor sees these
	 * callbacks and logs the errors in the conveyor database.
	 * 
	 * @throws <code>ConveyorExecutionException</code> if something really bad
	 *         happens. Mostly, any errors should be trapped and stuck in the
	 *         database for the transfer. Errors should only be thrown if
	 *         something goes wrong with that processing, and in that case
	 *         something is pretty much messed up anyhow. In other words if you
	 *         get an exception to throw here, something is broken with the
	 *         framework itself, and the framework is giving up.
	 * 
	 */
	@Override
	public final ConveyorExecutionFuture call()
			throws ConveyorExecutionException {
		TransferControlBlock tcb = buildDefaultTransferControlBlock();

		IRODSAccount irodsAccount = null;
		try {
			irodsAccount = getConveyorService().getGridAccountService()
					.irodsAccountForGridAccount(getTransfer().getGridAccount());

			processCallForThisTransfer(tcb, irodsAccount);
			return new ConveyorExecutionFuture();
		} catch (Exception ex) {
			log.error(
					"*********** unanticipated exception occurred  *************",
					ex);
			this.reportConveyerExceptionDuringProcessing(ex);
			throw new ConveyorExecutionException(
					"unhandled exception during transfer process", ex);
		}
	}

	/**
	 * @return the transfer
	 */
	public Transfer getTransfer() {
		return transfer;
	}

	/**
	 * @return the transferAttempt
	 */
	public TransferAttempt getTransferAttempt() {
		return transferAttempt;
	}

	/**
	 * @return the conveyorService
	 */
	public ConveyorService getConveyorService() {
		return conveyorService;
	}

	/**
	 * Get the <code>TransferControlBlock</code> that will control this
	 * transfer, based on configuration
	 * 
	 * @return {@link TransferControlBlock} TODO: this is null right now, need
	 *         to implement in configuration service
	 */
	public TransferControlBlock buildDefaultTransferControlBlock() {
		return conveyorService.getConfigurationService()
				.buildDefaultTransferControlBlockBasedOnConfiguration();
	}

	@Override
	public void statusCallback(final TransferStatus transferStatus)
			throws JargonException {
		log.info("put status callback:{}", transferStatus);
		try {
			if (transferStatus.getTransferState() == TransferState.SUCCESS
					|| transferStatus.getTransferState() == TransferStatus.TransferState.IN_PROGRESS_COMPLETE_FILE) {

				updateTransferStateOnFileCompletion(transferStatus);
			} else if (transferStatus.getTransferState() == TransferState.OVERALL_INITIATION) {

			} else if (transferStatus.getTransferState() == TransferState.RESTARTING) {
				// TransferStatus.TransferState.RESTARTING = skipped seeking
				// restart
				// point
				/*
				 * add a property to tell this to log that restart in the
				 * attempt, otherwise it can be skipped. consider transfer
				 * 'levels' and where this falls
				 */
			} else if (transferStatus.getTransferState() == TransferState.FAILURE) {
				// TransferStatus.TransferState.FAILURE
				/*
				 * create failure item get exception from callback and add to
				 * item
				 */
				try {
					this.getConveyorService()
							.getTransferAccountingManagementService()
							.updateTransferAfterFailedFileTransfer(
									transferStatus, getTransferAttempt());
				} catch (ConveyorExecutionException ex) {
					throw new JargonException(ex.getMessage(), ex.getCause());
				}
			}

			else if (transferStatus.getTransferState() == TransferState.CANCELLED
					|| transferStatus.getTransferState() == TransferState.PAUSED) {
				// TransferStatus.TransferState.CANCELLED or
				// TransferStatus.TransferState.PAUSED
				/*
                         *  
                         */
			}
		} catch (ConveyorExecutionException ex) {
			throw new JargonException(ex.getMessage(), ex.getCause());
		}

		conveyorService.getTransferStatusCallbackListener().statusCallback(
				transferStatus);

	}

	@Override
	public void overallStatusCallback(final TransferStatus transferStatus)
			throws JargonException {
		log.info("overall status callback:{}", transferStatus);
		if (transferStatus.getTransferState() == TransferStatus.TransferState.OVERALL_COMPLETION) {
			log.info("overall completion...releasing queue");
			getConveyorService().getConveyorExecutorService()
					.setOperationCompleted();
		} else if (transferStatus.getTransferState() == TransferStatus.TransferState.FAILURE) {
			log.error("failure to transfer in status...releasing queue");
			getConveyorService().getConveyorExecutorService()
					.setOperationCompleted();
		}
		conveyorService.getTransferStatusCallbackListener()
				.overallStatusCallback(transferStatus);

	}

	@Override
	public CallbackResponse transferAsksWhetherToForceOperation(
			final String irodsAbsolutePath, final boolean isCollection) {
		log.info("transferAsksWhetherToForceOperation");
		return CallbackResponse.YES_FOR_ALL;
	}

	/**
	 * Called by callable methods when an unexpected exception occurs in
	 * conveyor processing of the transfer, rather than an error occurring while
	 * the transfer operation is in progress. In other words, when the callable
	 * is setting up the transfer, updating the queue manager, trying to launch
	 * the transfer process, any error that is not signaled by a callback from
	 * Jargon is treated as a conveyor processing error, and the transfer is
	 * marked as an error with a global message.
	 * <p/>
	 * Note that calling this method will unlock the conveyor execution queue
	 * 
	 * @param ex
	 *            <code>Exception</code> that was caught while trying to process
	 *            the transfer
	 */
	void reportConveyerExceptionDuringProcessing(final Exception ex) {
		log.warn("reportConveyerExceptionDuringProcessing() is called");
		Exception myException = null;
		if (ex == null) {
			myException = new ConveyorExecutionException(
					"warning!  An exception was reported but null was provided to the reportConveyerExceptionDuringProcessing() method");
		} else {
			log.info("reported exception:", ex);
		}

		log.info("updating transfer attempt with an exception");
		try {
			this.getConveyorService()
					.getTransferAccountingManagementService()
					.updateTransferAttemptWithConveyorException(
							transferAttempt, myException);
		} catch (ConveyorExecutionException e) {
			log.error("*************  exception occurred in conveyor framework,unable to update conveyor database");
			throw new ConveyorRuntimeException(
					"unprocessable exception in conveyor, not updated in database",
					e);
		} finally {
			// FIXME: do I need to set a callback to overall status listener?
			// this is not in conveyor service yet
			log.info("setting operation completed...");
			this.getConveyorService().getConveyorExecutorService()
					.setOperationCompleted();
		}

	}

	/**
	 * Given a transfer status, update the transfer state on completion of
	 * processing a file
	 * 
	 * @param transferStatus
	 * @throws ConveyorExecutionException
	 */
	protected void updateTransferStateOnFileCompletion(
			final TransferStatus transferStatus)
			throws ConveyorExecutionException {
		getConveyorService().getTransferAccountingManagementService()
				.updateTransferAfterSuccessfulFileTransfer(transferStatus,
						getTransferAttempt());
	}

}
