/**
 * 
 */
package org.irods.jargon.conveyor.core.callables;

import org.irods.jargon.conveyor.core.AbstractConveyorCallable;
import org.irods.jargon.conveyor.core.ConveyorExecutionFuture;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.transfer.dao.domain.Transfer;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 *         NOTES: do we want to move the ts callback methods up to the abstract
 *         level?
 * 
 */
public class PutConveyorCallable extends AbstractConveyorCallable {

	/**
	 * @param transferAttempt
	 * @param conveyorService
	 */
	public PutConveyorCallable(Transfer transfer,
			ConveyorService conveyorService) {
		super(transfer, conveyorService);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.AbstractConveyorCallable#call()
	 */
	@Override
	public ConveyorExecutionFuture call() throws Exception {

		TransferControlBlock tcb = this.buildDefaultTransferControlBlock();

		// set the transfer attempt up...how? For now use queue manager service
		// and add methods there...save transfer attempt as instance data?

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void statusCallback(TransferStatus transferStatus)
			throws JargonException {
		// TODO Auto-generated method stub

	}

	@Override
	public void overallStatusCallback(TransferStatus transferStatus)
			throws JargonException {
		// TODO Auto-generated method stub

	}

	@Override
	public CallbackResponse transferAsksWhetherToForceOperation(
			String irodsAbsolutePath, boolean isCollection) {
		// TODO Auto-generated method stub
		return null;
	}

}
