/**
 * 
 */
package org.irods.jargon.conveyor.core;

import java.util.concurrent.Callable;

import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;
import org.irods.jargon.transfer.dao.domain.Transfer;

/**
 * Abstract super class for a transfer running process
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public abstract class AbstractConveyorCallable implements
		Callable<ConveyorExecutionFuture>, TransferStatusCallbackListener {

	private final Transfer transfer;
	private final ConveyorService conveyorService;

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
	public abstract ConveyorExecutionFuture call() throws Exception;

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

}
