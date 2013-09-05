/**
 * 
 */
package org.irods.jargon.conveyor.core.callables;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ConveyorCallableFactory {

	private final Logger log = LoggerFactory
			.getLogger(ConveyorCallableFactory.class);

	public ConveyorCallableFactory() {
	}

	/**
	 * Given a <code>Transfer</code> create the <code>Callable</code> that will
	 * process that transfer
	 * 
	 * @param conveyorService
	 *            {@link ConveyorService} that handles all operations
	 * @return {@link AbstractConveyorCallable} that will process the transfer
	 * @throws ConveyorExecutionException
	 *             if a callable cannot be created or some other error occurs
	 */
	public AbstractConveyorCallable instanceCallableForOperation(
			TransferAttempt transferAttempt,
			final ConveyorService conveyorService)
			throws ConveyorExecutionException {

		log.info("instanceCallableForOperation()");

		if (transferAttempt == null) {
			throw new IllegalArgumentException("transferAttempt is null");
		}

		if (conveyorService == null) {
			throw new IllegalArgumentException("conveyorService is null");
		}

		log.info("transferAttempt for callable:{}", transferAttempt);

		if (transferAttempt.getTransfer() == null) {
			log.error("no transfer in transfer attempt");
			throw new ConveyorExecutionException(
					"no transfer found for given transfer attempt");
		}

		switch (transferAttempt.getTransfer().getTransferType()) {
		case PUT:
			return new PutConveyorCallable(transferAttempt, conveyorService);
		case GET:
			return new GetConveyorCallable(transferAttempt, conveyorService);
		case REPLICATE:
			return new ReplicateConveyorCallable(transferAttempt,
					conveyorService);
		case COPY:
			return new CopyConveyorCallable(transferAttempt, conveyorService);
		case SYNCH:
			throw new ConveyorExecutionException(
					"synch transfer not implemented");
		default:
			throw new ConveyorExecutionException(
					"Unable to create a processor for the given transfer");

		}
	}

}
