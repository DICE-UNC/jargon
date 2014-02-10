/**
 * 
 */
package org.irods.jargon.conveyor.core.callables;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.conveyor.synch.AbstractSynchronizingDiffCreator;
import org.irods.jargon.conveyor.synch.AbstractSynchronizingDiffProcessor;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatus.TransferState;
import org.irods.jargon.core.transfer.TransferStatus.TransferType;
import org.irods.jargon.datautils.tree.FileTreeModel;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.irods.jargon.transfer.dao.domain.Transfer;
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

		Synchronization synchronization = this.getTransfer()
				.getSynchronization();

		assert synchronization != null;

		log.info("getting diff creation service...");

		try {
			AbstractSynchronizingDiffCreator diffCreator = this
					.getConveyorService().getSynchComponentFactory()
					.instanceDiffCreator(synchronization, tcb);
			FileTreeModel diffModel = diffCreator.createDiff(getTransfer());

			log.info("have file tree model, now process the diff to resolve it...get diff processor from factory");

			AbstractSynchronizingDiffProcessor diffProcessor = this
					.getConveyorService().getSynchComponentFactory()
					.instanceDiffProcessor(synchronization, tcb);

			log.info("..have diff processor, now invoke...");

			/*
			 * Note I register this callable as the callback listener, so that
			 * status updates flow back to this processor
			 */
			diffProcessor.execute(this.getTransferAttempt(), diffModel,
					this);

			log.info("processing complete, send the final callback");

			sendSynchCompleteSuccessful(getTransfer(), synchronization);
		} catch (Exception e) {
			log.error("error encountered during synch processing", e);
			this.reportConveyerExceptionDuringProcessing(e);
		}

	}

	private void sendSynchCompleteSuccessful(final Transfer transfer,
			final Synchronization synchronization)
			throws ConveyorExecutionException {
		// make an overall status callback that a synch is initiated
		TransferStatus overallSynchStartStatus;
		try {
			overallSynchStartStatus = TransferStatus.instance(
					TransferType.SYNCH, synchronization
							.getLocalSynchDirectory(), synchronization
							.getIrodsSynchDirectory(), synchronization
							.getDefaultStorageResource(), 0L, 0L, 0, 0, 0,
					TransferState.OVERALL_COMPLETION, transfer.getGridAccount()
							.getHost(), transfer.getGridAccount().getZone());

			this.overallStatusCallback(overallSynchStartStatus);

		} catch (JargonException e) {
			log.error("error creating synch", e);
			throw new ConveyorExecutionException("error in synch processing", e);
		}
	}
}
