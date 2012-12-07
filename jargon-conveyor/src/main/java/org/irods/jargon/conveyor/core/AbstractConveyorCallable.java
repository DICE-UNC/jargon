/**
 * 
 */
package org.irods.jargon.conveyor.core;

import java.util.concurrent.Callable;

import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;


/**
 * Abstract super class for a transfer running process
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public abstract class AbstractConveyorCallable implements Callable<ConveyorExecutionFuture> {
	
	private final TransferStatusCallbackListener transferStatusCallbackListener;
	private final TransferControlBlock transferControlBlock;
	
	/**
	 * Default constructor takes required hooks for bi-directional communication with caller of the transfer processor
	 * @param transferStatusCallbackListener {@link TransferStatusCallbackListener} that will receive status callbacks as the process runs
	 * @param transferControlBlock {@link TransferControlBlock} that contains configuration information, and allows caller of this process to send data to
	 * the running transfer, including cancellation
	 */
	public AbstractConveyorCallable(final TransferStatusCallbackListener transferStatusCallbackListener, final TransferControlBlock transferControlBlock) {
		if (transferStatusCallbackListener == null) {
			throw new IllegalArgumentException("null transferStatusCallbackListener");
		}
		
		if (transferControlBlock == null) {
			throw new IllegalArgumentException("null transferControlBlock");
		}
		
		this.transferStatusCallbackListener = transferStatusCallbackListener;
		this.transferControlBlock = transferControlBlock;
	}
	
	@Override
	public abstract ConveyorExecutionFuture call() throws Exception;
	
	public TransferStatusCallbackListener getTransferStatusCallbackListener() {
		return transferStatusCallbackListener;
	}

	public TransferControlBlock getTransferControlBlock() {
		return transferControlBlock;
	}


}
