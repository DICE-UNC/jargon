/**
 * 
 */
package org.irods.jargon.conveyor.core;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferType;

/**
 * Manages the persistent queue of transfer information
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface QueueManagerService {

	/**
	 * Cause a put operation (transfer to iRODS) to occur. This transfer will be
	 * based on the given iRODS account information.
	 * 
	 * @param transfer
	 *            {@link Transfer} to be executed
	 * @param irodsAccount
	 *            {@link IRODSAccount} describing the
	 * @throws ConveyorExecutionException
	 */
	void enqueueTransferOperation(final Transfer transfer,
			final IRODSAccount irodsAccount) throws ConveyorExecutionException;

	/**
	 * Signal that,if the queue is not busy, that the next pending operation
	 * should be launched
	 * 
	 * @throws ConveyerExecutionException
	 */
	void dequeueNextOperation() throws ConveyorExecutionException;

	/**
	 * Convenience function for iDrop to start a transfer based on the given
	 * iRODS account information.
	 * 
	 * @param irodsFile
	 *            String full path of iRODS file/folder for get or put
	 * @param localFile
	 *            String full path of local file/folder for get or put
	 * @param irodsAccount
	 *            {@link IRODSAccount} describing the IRODSAccount
	 * @param TransferType
	 *            {@link TransferType} type of transfer - GET, PUT, etc
	 * @throws ConveyorExecutionException
	 */
	void processTransfer(final String irodsFile, final String localFile,
			final IRODSAccount irodsAccount, final TransferType type)
			throws ConveyorExecutionException;

	/**
	 * Purge all of contents of queue, no matter what the status
	 * 
	 * @throws ConveyorBusyException
	 *             if the conveyor framework is busy, this indicates that the
	 *             queue should be idle before purging
	 * @throws ConveyorExecutionException
	 *             for other errors
	 */
	void purgeAllFromQueue() throws ConveyorBusyException,
			ConveyorExecutionException;

	/**
	 * Get a list of the entire contents of the transfer queue
	 * 
	 * @return <code>List</code> of {@link Transfer} containing the entire
	 *         contents of the queue (including completed, etc)
	 * @throws ConveyorExecutionException
	 */
	List<Transfer> listAllTransfersInQueue() throws ConveyorExecutionException;

	/**
	 * Get a filled out (children initialized) depiction of a transfer.
	 * 
	 * @param transfer
	 *            {@link Transfer} that will be updated in place with
	 *            initialized children
	 * @throws ConveyorExecutionException
	 */
	Transfer initializeGivenTransferByLoadingChildren(Transfer transfer)
			throws ConveyorExecutionException;

}
