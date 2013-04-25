/**
 * 
 */
package org.irods.jargon.conveyor.core.callables;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.transfer.dao.domain.Transfer;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ConveyorCallableFactory {

	public ConveyorCallableFactory() {
	}

	/**
	 * Given a <code>Transfer</code> create the <code>Callable</code> that will
	 * process that transfer
	 * 
	 * @param transfer
	 *            {@link Transfer} that contains information on the desired
	 *            action
	 * @param conveyorService
	 *            {@link ConveyorService} that handles all operations
	 * @return {@link AbstractConveyorCallable} that will process the transfer
	 * @throws ConveyorExecutionException
	 *             if a callable cannot be created or some other error occurs
	 */
	public AbstractConveyorCallable instanceCallableForOperation(
			final Transfer transfer, final ConveyorService conveyorService)
			throws ConveyorExecutionException {

		if (transfer == null) {
			throw new IllegalArgumentException("transfer is null");
		}

		switch (transfer.getTransferType()) {
		case PUT:
			return new PutConveyorCallable(transfer, conveyorService);
		case GET:
			throw new ConveyorExecutionException("get transfer not implemented");
		case REPLICATE:
			throw new ConveyorExecutionException(
					"replicate transfer not implemented");
		case COPY:
			throw new ConveyorExecutionException(
					"copy transfer not implemented");
		case SYNCH:
			throw new ConveyorExecutionException(
					"synch transfer not implemented");
		default:
			throw new ConveyorExecutionException(
					"Unable to create a processor for the given transfer");

		}
	}

}
