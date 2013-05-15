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
	 * Add a transfer operation to the queue. This transfer will be based on the
	 * given iRODS account information. The transfer will be scheduled for later
	 * execution.
	 * 
	 * @param transfer
	 *            {@link Transfer} to be executed
	 * @param irodsAccount
	 *            {@link IRODSAccount}
	 * @throws RejectedTransferException
	 *             if the transfer was rejected (e.g. for being a duplicate)
	 * @throws ConveyorExecutionException
	 */
	void enqueueTransferOperation(final Transfer transfer,
			final IRODSAccount irodsAccount) throws RejectedTransferException,
			ConveyorExecutionException;

	/**
	 * Signal that,if the queue is not busy, that the next pending operation
	 * should be launched. This may be safely called even if the queue is busy
	 * (the call will be ignored) or if there are no transfers to process. This
	 * method is suitable for calling by a timer process, for example, that
	 * checks for any pending work.
	 * 
	 * @throws ConveyerExecutionException
	 */
	void dequeueNextOperation() throws ConveyorExecutionException;

	/**
	 * Convenience function for iDrop to start a transfer based on the given
	 * iRODS account information. This transfer will be based on the given iRODS
	 * account information. The transfer will be scheduled for later execution.
	 * <p/>
	 * This method creates a <code>Transfer</code> based on the provided path
	 * and operation details, and places it in the queue.
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
	void enqueueTransferOperation(final String irodsFile,
			final String localFile, final IRODSAccount irodsAccount,
			final TransferType type) throws ConveyorExecutionException;

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
	 * Get a filled out (children initialized) representation of a transfer.
	 * 
	 * @param transfer
	 *            {@link Transfer} that will be updated in place with
	 *            initialized children
	 * @throws ConveyorExecutionException
	 */
	Transfer initializeGivenTransferByLoadingChildren(Transfer transfer)
			throws ConveyorExecutionException;

	/**
	 * Given an id, look up the transfer information in the database.
	 * 
	 * @param transferId
	 *            <code>long</code> with the transfer id
	 * @return {@link Transfer}, with all children except transfer items,
	 *         initialized. This is done for efficiency, as the transfer items
	 *         can be quite large. Note that <code>null</code> will be returned
	 *         if the transfer cannot be found.
	 * @throws ConveyorExecutionException
	 */
	Transfer findTransferByTransferId(final long transferId)
			throws ConveyorExecutionException;

	/**
	 * Cause a transfer to be resubmitted as a restart. A restart will look at
	 * the last successful transfer, and skip files before that restart point.
	 * The actual transfers will resume once the restart point is encountered.
	 * <p/>
	 * Note that various configuration settings control how restarts are logged.
	 * Optionally, the framework can log each skipped file in the restart
	 * process to provide complete accounting for that transfer attempt.
	 * 
	 * @param transferId
	 *            <code>long</code> with the unique id for the transfer
	 * @throws RejectedTransferException
	 *             if the transfer is not suitable for restart
	 * @throws ConveyorExecutionException
	 */
	void enqueueRestartOfTransferOperation(final long transferId)
			throws RejectedTransferException, ConveyorExecutionException;

}
