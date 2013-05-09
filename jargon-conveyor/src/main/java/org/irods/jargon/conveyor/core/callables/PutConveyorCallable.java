/**
 * 
 */
package org.irods.jargon.conveyor.core.callables;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.ConveyorExecutionFuture;
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
	 * @see org.irods.jargon.conveyor.core.AbstractConveyorCallable#call()
	 */
	@Override
	public ConveyorExecutionFuture call() throws ConveyorExecutionException {

		TransferControlBlock tcb = buildDefaultTransferControlBlock();

		IRODSAccount irodsAccount = null;
		try {
			irodsAccount = getConveyorService().getGridAccountService()
					.irodsAccountForGridAccount(getTransfer().getGridAccount());

			DataTransferOperations dataTransferOperationsAO = getIrodsAccessObjectFactory()
					.getDataTransferOperations(irodsAccount);
			dataTransferOperationsAO.putOperation(getTransfer()
					.getLocalAbsolutePath(), getTransfer()
					.getIrodsAbsolutePath(), getTransfer().getGridAccount()
					.getDefaultResource(), this, tcb);
		} catch (JargonException ex) {
			log.error("error doing transfer", ex);
			this.reportConveyerExceptionDuringProcessing(ex);
		} catch (Exception ex) {
			log.error("unanticipated exception occurred", ex);
			this.reportConveyerExceptionDuringProcessing(ex);
		}

		return new ConveyorExecutionFuture();
	}

	@Override
	public void statusCallback(final TransferStatus transferStatus)
			throws JargonException {
		log.info("status callback:{}", transferStatus);

		if (transferStatus.getTransferState() == TransferStatus.TransferState.SUCCESS
				|| transferStatus.getTransferState() == TransferStatus.TransferState.IN_PROGRESS_COMPLETE_FILE) {
			try {
				getConveyorService().getTransferAccountingManagementService()
						.updateTransferAfterSuccessfulFileTransfer(
								transferStatus, getTransferAttempt());
			} catch (ConveyorExecutionException ex) {
				throw new JargonException(ex.getMessage(), ex.getCause());
			}
		}

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
	public void overallStatusCallback(final TransferStatus transferStatus)
			throws JargonException {
		log.info("overall status callback:{}", transferStatus);
		if (transferStatus.getTransferState() == TransferStatus.TransferState.OVERALL_COMPLETION) {
			log.info("overall completion...releasing queue");
			getConveyorService().getConveyorExecutorService()
					.setOperationCompleted();
		} else if (transferStatus.getTransferState() == TransferStatus.TransferState.FAILURE) {
			log.error("failure to transfer in status...releasing queue");
			getConveyorService().getConveyorExecutorService()
					.setOperationCompleted();
		}
		super.overallStatusCallback(transferStatus);

	}

	@Override
	public CallbackResponse transferAsksWhetherToForceOperation(
			final String irodsAbsolutePath, final boolean isCollection) {
		log.info("transferAsksWhetherToForceOperation");
		return CallbackResponse.YES_FOR_ALL;
	}

}
