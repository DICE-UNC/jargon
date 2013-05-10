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
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author lisa
 */
public class GetConveyorCallable extends AbstractConveyorCallable {

	private static final Logger log = LoggerFactory
			.getLogger(PutConveyorCallable.class);

	/**
	 * @param transfer
	 * @param conveyorService
	 */
	public GetConveyorCallable(Transfer transfer,
			TransferAttempt transferAttempt, ConveyorService conveyorService) {
		super(transfer, transferAttempt, conveyorService);
	}

	@Override
	public void statusCallback(TransferStatus transferStatus)
			throws JargonException {
		log.info("status callback:{}", transferStatus);

		// TransferStatus.TransferState.RESTARTING = skipped seeking restart
		// point

		/*
		 * add a property to tell this to log that restart in the attempt,
		 * otherwise it can be skipped. consider transfer 'levels' and where
		 * this falls
		 */

		// TransferStatus.TransferState.FAILURE

		/*
		 * create failure item get exception from callback and add to item
		 */

		// TransferStatus.TransferState.CANCELLED or
		// TransferStatus.TransferState.PAUSED

		/*
		 *  
		 */

		super.statusCallback(transferStatus);

	}

	@Override
	public void overallStatusCallback(TransferStatus transferStatus)
			throws JargonException {
		log.info("overall status callback:{}", transferStatus);
		if (transferStatus.getTransferState() == TransferStatus.TransferState.OVERALL_COMPLETION) {
			log.info("overall completion...releasing queue");
			this.getConveyorService().getConveyorExecutorService()
					.setOperationCompleted();
		} else if (transferStatus.getTransferState() == TransferStatus.TransferState.FAILURE) {
			log.error("failure to transfer in status...releasing queue");
			this.getConveyorService().getConveyorExecutorService()
					.setOperationCompleted();
		}
		super.overallStatusCallback(transferStatus);

	}

	@Override
	public CallbackResponse transferAsksWhetherToForceOperation(
			String irodsAbsolutePath, boolean isCollection) {
		log.info("transferAsksWhetherToForceOperation");
		return CallbackResponse.YES_FOR_ALL;
	}

	@Override
	void processCallForThisTransfer(TransferControlBlock tcb,
			IRODSAccount irodsAccount) throws ConveyorExecutionException,
			JargonException {
		DataTransferOperations dataTransferOperationsAO = getIrodsAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dataTransferOperationsAO.getOperation(getTransfer()
				.getIrodsAbsolutePath(), getTransfer().getLocalAbsolutePath(),
				getTransfer().getGridAccount().getDefaultResource(), this, tcb);

	}

}
