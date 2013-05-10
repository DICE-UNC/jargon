/**
 * 
 */
package org.irods.jargon.conveyor.core.callables;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callable that will run a put operation and handle callbacks
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class PutConveyorCallable extends AbstractConveyorCallable {

	private static final Logger log = LoggerFactory
			.getLogger(PutConveyorCallable.class);

	/**
	 * @param transfer
	 * @param conveyorService
	 */
	public PutConveyorCallable(final Transfer transfer,
			final TransferAttempt transferAttempt,
			final ConveyorService conveyorService) {
		super(transfer, transferAttempt, conveyorService);
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
		dataTransferOperationsAO.putOperation(getTransfer()
				.getLocalAbsolutePath(), getTransfer().getIrodsAbsolutePath(),
				getTransfer().getGridAccount().getDefaultResource(), this, tcb);
	}

}
