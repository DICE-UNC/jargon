/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.microservice.builtins;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.RejectedTransferException;
import org.irods.jargon.conveyor.flowmanager.microservice.Microservice;
import org.irods.jargon.conveyor.flowmanager.microservice.MicroserviceException;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Microservice to enqueue a transfer. This will use the current transfer status
 * and then check the shared whiteboard for any override parameters.
 * 
 * @author Mike Conway - DICE
 * 
 */
public class EnqueueTransferMicroservice extends Microservice {

	private static final Logger log = LoggerFactory
			.getLogger(EnqueueTransferMicroservice.class);

	/**
	 * Parameters that will override those in the transfer status for the new
	 * transfer
	 */
	public static final String LOCAL_FILE_NAME = EnqueueTransferMicroservice.class
			.getName() + ":LOCAL_PATH";

	public static final String IRODS_FILE_NAME = EnqueueTransferMicroservice.class
			.getName() + ":IRODS_PATH";

	public static final String RESOURCE = EnqueueTransferMicroservice.class
			.getName() + ":RESOURCE";

	public static final String ENQUEUED_TRANSFER = EnqueueTransferMicroservice.class
			.getName() + ":ENQUEUED_TRANSFER";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.flowmanager.microservice.Microservice#execute
	 * (org.irods.jargon.core.transfer.TransferStatus)
	 */
	@Override
	public ExecResult execute(TransferStatus transferStatus)
			throws MicroserviceException {

		log.info("execute");

		Transfer oldTransfer = this.getInvocationContext().getTransferAttempt()
				.getTransfer();

		log.info("currentTransfer:{}", oldTransfer);

		Transfer transfer = new Transfer();
		transfer.setTransferType(oldTransfer.getTransferType());
		transfer.setGridAccount(oldTransfer.getGridAccount());

		if (this.getInvocationContext().getSharedProperties()
				.get(LOCAL_FILE_NAME) != null) {
			log.info("overriding source file name");
			transfer.setLocalAbsolutePath((String) this.getInvocationContext()
					.getSharedProperties().get(LOCAL_FILE_NAME));
		} else {
			transfer.setLocalAbsolutePath(oldTransfer.getLocalAbsolutePath());
		}

		if (this.getInvocationContext().getSharedProperties()
				.get(IRODS_FILE_NAME) != null) {
			log.info("overriding irods file name");
			transfer.setIrodsAbsolutePath((String) this.getInvocationContext()
					.getSharedProperties().get(IRODS_FILE_NAME));
		} else {
			transfer.setIrodsAbsolutePath(oldTransfer.getIrodsAbsolutePath());
		}

		if (this.getInvocationContext().getSharedProperties().get(RESOURCE) != null) {
			log.info("overriding resource name");
			transfer.setResourceName((String) this.getInvocationContext()
					.getSharedProperties().get(RESOURCE));
		} else {
			transfer.setResourceName(oldTransfer.getResourceName());
		}

		log.info("updated transfer is:{}", transfer);
		try {
			this.getContainerEnvironment()
					.getConveyorService()
					.getQueueManagerService()
					.enqueueTransferOperation(transfer,
							this.getInvocationContext().getIrodsAccount());

			// add the enqueued transfer to the whiteboard

			this.getInvocationContext().getSharedProperties()
					.put(ENQUEUED_TRANSFER, transfer);

		} catch (RejectedTransferException e) {
			log.error("rejected transfer:{}", transfer, e);
			throw new MicroserviceException("updated transfer was rejected", e);
		} catch (ConveyorExecutionException e) {
			log.error("conveyor exception enqueueing new transfer", e);
			throw new MicroserviceException(
					"conveyor exception enqueueing new transfer", e);
		}

		return ExecResult.CONTINUE;

	}
}
