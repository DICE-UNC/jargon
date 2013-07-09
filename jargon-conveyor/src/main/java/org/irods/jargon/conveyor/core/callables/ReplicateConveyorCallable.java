/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.irods.jargon.conveyor.core.callables;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callable to process a replication
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public class ReplicateConveyorCallable extends AbstractConveyorCallable {

	private static final Logger log = LoggerFactory
			.getLogger(PutConveyorCallable.class);

	public ReplicateConveyorCallable(TransferAttempt transferAttempt,
			ConveyorService conveyorService) {
		super(transferAttempt, conveyorService);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.callables.AbstractConveyorCallable#
	 * processCallForThisTransfer
	 * (org.irods.jargon.core.transfer.TransferControlBlock,
	 * org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	void processCallForThisTransfer(TransferControlBlock tcb,
			IRODSAccount irodsAccount) throws ConveyorExecutionException,
			JargonException {
		log.info("processCallForThisTransfer()");
		DataTransferOperations dataTransferOperationsAO = getIrodsAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dataTransferOperationsAO.replicate(
				getTransfer().getIrodsAbsolutePath(), getTransfer()
						.getResourceName(), this, tcb);
	}
}
