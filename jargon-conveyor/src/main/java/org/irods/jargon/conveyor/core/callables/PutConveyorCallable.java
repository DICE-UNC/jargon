/**
 * 
 */
package org.irods.jargon.conveyor.core.callables;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.ConveyorExecutionFuture;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.Transfer;

/**
 * Callable that will run a put operation and handle callbacks
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class PutConveyorCallable extends AbstractConveyorCallable {

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
            
                GridAccount gridAccount = this.transfer.getGridAccount();
                TransferControlBlock tcb = this.buildDefaultTransferControlBlock();
                
                IRODSFileSystem irodsFileSystem = null;
                IRODSAccount irodsAccount = null;
                try {
                    irodsFileSystem = IRODSFileSystem.instance();
                    
                    irodsAccount = IRODSAccount.instance(
                        gridAccount.getHost(),
                        gridAccount.getPort(),
                        gridAccount.getUserName(),
                        gridAccount.getPassword(),
                        gridAccount.getDefaultPath(),
                        gridAccount.getZone(),
                        gridAccount.getDefaultResource());
                    
                    DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
                    dataTransferOperationsAO.putOperation(
                                transfer.getLocalAbsolutePath(),
                                transfer.getIrodsAbsolutePath(),
                                transfer.getGridAccount().getDefaultResource(),
                                this,
                                tcb);
                } catch (JargonException ex) {
                    Logger.getLogger(PutConveyorCallable.class.getName()).log(Level.SEVERE, null, ex);
                    throw new ConveyorExecutionException(ex);
                }
                

		// set the transfer attempt up...how? For now use queue manager service
		// and add methods there...save transfer attempt as instance data?

		// final DataTransferOperations dataTransferOperations = transferManager
		// .getIrodsFileSystem().getIRODSAccessObjectFactory()
		// .getDataTransferOperations(irodsAccount);
		return null;
	}

	@Override
	public void statusCallback(TransferStatus transferStatus)
			throws JargonException {
		// TODO Auto-generated method stub

	}

	@Override
	public void overallStatusCallback(TransferStatus transferStatus)
			throws JargonException {
		// TODO Auto-generated method stub

	}

	@Override
	public CallbackResponse transferAsksWhetherToForceOperation(
			String irodsAbsolutePath, boolean isCollection) {
		// TODO Auto-generated method stub
		return null;
	}

}
