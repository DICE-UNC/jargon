/**
 * 
 */
package org.irods.jargon.conveyor.core;

import java.util.concurrent.Callable;

import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;

/**
 * Abstract super class for a transfer running process
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public abstract class AbstractConveyorCallable implements
		Callable<ConveyorExecutionFuture> {

	private final TransferStatusCallbackListener transferStatusCallbackListener;
	private final TransferControlBlock transferControlBlock;
	private final TransferAttempt transferAttempt;
	private final ConveyorService conveyorService;

	/**
	 * Default constructor takes required hooks for bi-directional communication
	 * with caller of the transfer processor
	 * 
	 * @param transferStatusCallbackListener
	 *            {@link TransferStatusCallbackListener} that will receive
	 *            status callbacks as the process runs
	 * @param transferControlBlock
	 *            {@link TransferControlBlock} that contains configuration
	 *            information, and allows caller of this process to send data to
	 *            the running transfer, including cancellation FIXME: fix
	 *            comment
	 */
	public AbstractConveyorCallable(
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock,
			final TransferAttempt transferAttempt,
			final ConveyorService conveyorService) {
		if (transferStatusCallbackListener == null) {
			throw new IllegalArgumentException(
					"null transferStatusCallbackListener");
		}

		if (transferControlBlock == null) {
			throw new IllegalArgumentException("null transferControlBlock");
		}

		if (transferAttempt == null) {
			throw new IllegalArgumentException("null transferAttempt");
		}

		if (conveyorService == null) {
			throw new IllegalArgumentException("null conveyorService");
		}

		this.transferStatusCallbackListener = transferStatusCallbackListener;
		this.transferControlBlock = transferControlBlock;
		this.transferAttempt = transferAttempt;
		this.conveyorService = conveyorService;
	}

	@Override
	public abstract ConveyorExecutionFuture call() throws Exception;

	public TransferStatusCallbackListener getTransferStatusCallbackListener() {
		return transferStatusCallbackListener;
	}

	public TransferControlBlock getTransferControlBlock() {
		return transferControlBlock;
	}

	/**
	 * @return the transferAttempt
	 */
	public synchronized TransferAttempt getTransferAttempt() {
		return transferAttempt;
	}

	/**
	 * @return the conveyorService
	 */
	public synchronized ConveyorService getConveyorService() {
		return conveyorService;
	}

}
