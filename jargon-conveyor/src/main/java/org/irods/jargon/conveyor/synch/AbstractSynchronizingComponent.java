package org.irods.jargon.conveyor.synch;

import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.core.transfer.TransferControlBlock;

public class AbstractSynchronizingComponent {
	private ConveyorService conveyorService;
	private TransferControlBlock transferControlBlock;

	public AbstractSynchronizingComponent(
			final ConveyorService conveyorService,
			final TransferControlBlock transferControlBlock) {
		if (conveyorService == null) {
			throw new IllegalArgumentException("null conveyorService");
		}

		if (transferControlBlock == null) {
			throw new IllegalArgumentException("null transferControlBlock");
		}

		this.conveyorService = conveyorService;
		this.transferControlBlock = transferControlBlock;
	}

	/**
	 * @return the conveyorService
	 */
	public ConveyorService getConveyorService() {
		return conveyorService;
	}

	/**
	 * @param conveyorService
	 *            the conveyorService to set
	 */
	public void setConveyorService(ConveyorService conveyorService) {
		this.conveyorService = conveyorService;
	}

	/**
	 * @return the transferControlBlock
	 */
	public TransferControlBlock getTransferControlBlock() {
		return transferControlBlock;
	}

	/**
	 * @param transferControlBlock
	 *            the transferControlBlock to set
	 */
	public void setTransferControlBlock(
			TransferControlBlock transferControlBlock) {
		this.transferControlBlock = transferControlBlock;
	}

	/**
	 * convenience method checks if this operation is cancelled
	 * 
	 * @return <code>boolean</code> indicating that a cancellation was received
	 */
	public boolean isCancelled() {
		return this.getTransferControlBlock().isCancelled();
	}
}