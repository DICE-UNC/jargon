/**
 * 
 */
package org.irods.jargon.conveyor.core.callables;

import java.util.concurrent.Callable;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.ConveyorExecutionFuture;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.Transfer;
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
	private final ConveyorService conveyorService;

	private static final Logger log = LoggerFactory
			.getLogger(AbstractConveyorCallable.class);

	/**
	 * Conveniece method to get a <code>IRODSAccount</code> with a decrypted
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

	final Transfer transfer, final ConveyorService conveyorService) {

		if (transfer == null) {
			throw new IllegalArgumentException("null transfer");
		}

		if (conveyorService == null) {
			throw new IllegalArgumentException("null conveyorService");
		}

		this.transfer = transfer;
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
	public abstract void statusCallback(TransferStatus transferStatus)
			throws JargonException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.transfer.TransferStatusCallbackListener#
	 * overallStatusCallback(org.irods.jargon.core.transfer.TransferStatus)
	 */
	@Override
	public abstract void overallStatusCallback(TransferStatus transferStatus)
			throws JargonException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.transfer.TransferStatusCallbackListener#
	 * transferAsksWhetherToForceOperation(java.lang.String, boolean)
	 */
	@Override
	public abstract CallbackResponse transferAsksWhetherToForceOperation(
			String irodsAbsolutePath, boolean isCollection);

}
