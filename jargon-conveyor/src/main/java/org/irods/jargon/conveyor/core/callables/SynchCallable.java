/**
 * 
 */
package org.irods.jargon.conveyor.core.callables;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process a synchronization transfer
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class SynchCallable extends AbstractConveyorCallable {

	private static final Logger log = LoggerFactory
			.getLogger(SynchCallable.class);

	/**
	 * @param transferAttempt
	 * @param conveyorService
	 */
	public SynchCallable(TransferAttempt transferAttempt,
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

		log.info("processCallForThisTransfer for synch");

		assert tcb != null;
		assert irodsAccount != null;

	}

}
