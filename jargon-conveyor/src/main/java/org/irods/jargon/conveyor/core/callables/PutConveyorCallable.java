/**
 * 
 */
package org.irods.jargon.conveyor.core.callables;

import org.irods.jargon.conveyor.core.AbstractConveyorCallable;
import org.irods.jargon.conveyor.core.ConveyorExecutionFuture;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;

/**
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class PutConveyorCallable extends AbstractConveyorCallable {

	/**
	 * @param transferStatusCallbackListener
	 * @param transferControlBlock
	 * @param transferAttempt
	 * @param conveyorService
	 */
	public PutConveyorCallable(
			TransferStatusCallbackListener transferStatusCallbackListener,
			TransferControlBlock transferControlBlock,
			TransferAttempt transferAttempt, ConveyorService conveyorService) {
		super(transferStatusCallbackListener, transferControlBlock,
				transferAttempt, conveyorService);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.conveyor.core.AbstractConveyorCallable#call()
	 */
	@Override
	public ConveyorExecutionFuture call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
