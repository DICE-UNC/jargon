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
	public ConveyorExecutionFuture call() throws ConveyorExecutionException {

		TransferControlBlock tcb = this.buildDefaultTransferControlBlock();

		IRODSAccount irodsAccount = null;
		try {
                        irodsAccount = getConveyorService().getGridAccountService().irodsAccountForGridAccount(getTransfer().getGridAccount());
                        irodsAccount.setDefaultStorageResource("renci-vault1");
 
			DataTransferOperations dataTransferOperationsAO = getIrodsAccessObjectFactory()
					.getDataTransferOperations(irodsAccount);
			dataTransferOperationsAO.putOperation(getTransfer()
					.getLocalAbsolutePath(), getTransfer()
					.getIrodsAbsolutePath(), getTransfer().getGridAccount()
					.getDefaultResource(), this, tcb);
		} catch (JargonException ex) {
			log.error("error doing transfer", ex);
			throw new ConveyorExecutionException(ex);
		}

		// set the transfer attempt up...how? For now use queue manager service
		// and add methods there...save transfer attempt as instance data?

		// final DataTransferOperations dataTransferOperations = transferManager
		// .getIrodsFileSystem().getIRODSAccessObjectFactory()
		// .getDataTransferOperations(irodsAccount);
		return new ConveyorExecutionFuture();
	}

	@Override
	public void statusCallback(TransferStatus transferStatus)
			throws JargonException {
		log.info("status callback:{}", transferStatus);

	}

	@Override
	public void overallStatusCallback(TransferStatus transferStatus)
			throws JargonException {
		log.info("overall status callback:{}", transferStatus);

	}

	@Override
	public CallbackResponse transferAsksWhetherToForceOperation(
			String irodsAbsolutePath, boolean isCollection) {
		log.info("transferAsksWhetherToForceOperation");
		return CallbackResponse.YES_FOR_ALL;
	}

}
