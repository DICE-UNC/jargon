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

	@Override
	public abstract ConveyorExecutionFuture call()
			throws ConveyorExecutionException;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.TransferStatusCallbackListener#statusCallback
	 * (org.irods.jargon.core.transfer.TransferStatus)
	 */
	@Override
	public void statusCallback(TransferStatus transferStatus)
			throws JargonException {

		if (this.conveyorService.getTransferStatusCallbackListener() == null) {
			return;
		}

		this.conveyorService.getTransferStatusCallbackListener()
				.statusCallback(transferStatus);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.transfer.TransferStatusCallbackListener#
	 * overallStatusCallback(org.irods.jargon.core.transfer.TransferStatus)
	 */
	@Override
	public void overallStatusCallback(TransferStatus transferStatus)
			throws JargonException {

		if (this.conveyorService.getTransferStatusCallbackListener() == null) {
			return;
		}

		this.conveyorService.getTransferStatusCallbackListener()
				.overallStatusCallback(transferStatus);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.transfer.TransferStatusCallbackListener#
	 * transferAsksWhetherToForceOperation(java.lang.String, boolean)
	 */
	@Override
	public abstract CallbackResponse transferAsksWhetherToForceOperation(
			String irodsAbsolutePath, boolean isCollection);

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

}
