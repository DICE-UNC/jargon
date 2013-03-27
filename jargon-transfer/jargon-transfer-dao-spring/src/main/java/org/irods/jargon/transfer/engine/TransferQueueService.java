package org.irods.jargon.transfer.engine;

import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferItem;
import org.irods.jargon.transfer.dao.domain.Synchronization;

public interface TransferQueueService {

	/**
	 * 
	 * @return
	 * @throws JargonException
	 */
	Transfer dequeueTransfer() throws JargonException;

	/**
	 * 
	 * @param localSourceAbsolutePath
	 * @param targetIRODSAbsolutePath
	 * @param targetResource
	 * @param gridAccount
	 * @return
	 * @throws JargonException
	 */
	Transfer enqueuePutTransfer(final String localSourceAbsolutePath,
			final String targetIRODSAbsolutePath, final String targetResource,
			final GridAccount gridAccount) throws JargonException;

	/**
	 * 
	 * @param irodsSourceAbsolutePath
	 * @param targetLocalAbsolutePath
	 * @param sourceResource
	 * @param gridAccount
	 * @return
	 * @throws JargonException
	 */
	Transfer enqueueGetTransfer(final String irodsSourceAbsolutePath,
			final String targetLocalAbsolutePath, final String sourceResource,
			final GridAccount gridAccount) throws JargonException;

	/**
	 * 
	 * @param localIRODSTransfer
	 * @param transferManager
	 * @throws JargonException
	 */
	void markTransferAsErrorAndTerminate(
			final Transfer localIRODSTransfer,
			final TransferManager transferManager) throws JargonException;

	/**
	 * 
	 * @param localIRODSTransfer
	 * @param errorException
	 * @param transferManager
	 * @throws JargonException
	 */
	void markTransferAsErrorAndTerminate(
			final Transfer localIRODSTransfer,
			final Exception errorException,
			final TransferManager transferManager) throws JargonException;

	/**
	 * 
	 * @param countOfEntriesToShow
	 * @return
	 * @throws JargonException
	 */
	List<Transfer> getLastNInQueue(final int countOfEntriesToShow)
			throws JargonException;

	/**
	 * 
	 * @return
	 * @throws JargonException
	 */
	List<Transfer> getCurrentQueue() throws JargonException;

	/**
	 * 
	 * @return
	 * @throws JargonException
	 */
	List<Transfer> getErrorQueue() throws JargonException;

	/**
	 * 
	 * @return
	 * @throws JargonException
	 */
	List<Transfer> getWarningQueue() throws JargonException;

	/**
	 * 
	 * @return
	 * @throws JargonException
	 */
	List<Transfer> showErrorTransfers() throws JargonException;

	/**
	 * 
	 * @return
	 * @throws JargonException
	 */
	List<Transfer> showWarningTransfers() throws JargonException;

	/**
	 * 
	 * @return
	 * @throws JargonException
	 */
	List<Transfer> getRecentQueue() throws JargonException;

	/**
	 * 
	 * @throws JargonException
	 */
	void purgeQueue() throws JargonException;

	/**
	 * 
	 * @throws JargonException
	 */
	void purgeSuccessful() throws JargonException;

	/**
	 * 
	 * @param localIRODSTransferId
	 * @return
	 * @throws JargonException
	 */
	List<TransferItem> getAllTransferItemsForTransfer(
			final Long localIRODSTransferId) throws JargonException;

	/**
	 * 
	 * @param localIRODSTransferId
	 * @return
	 * @throws JargonException
	 */
	List<TransferItem> getErrorTransferItemsForTransfer(
			final Long localIRODSTransferId) throws JargonException;

	/**
	 * 
	 * @param localIRODSTransfer
	 * @throws JargonException
	 */
	void restartTransfer(final Transfer localIRODSTransfer)
			throws JargonException;

	/**
	 * 
	 * @param localIRODSTransfer
	 * @throws JargonException
	 */
	void resubmitTransfer(final Transfer localIRODSTransfer)
			throws JargonException;

	/**
	 * 
	 * @param irodsAbsolutePath
	 * @param targetResource
	 * @param gridAccount
	 * @return
	 * @throws JargonException
	 */
	Transfer enqueueReplicateTransfer(final String irodsAbsolutePath,
			final String targetResource, final GridAccount gridAccount)
			throws JargonException;

	/**
	 * 
	 * @param localIRODSTransfer
	 * @throws JargonException
	 */
	void setTransferAsCancelled(final Transfer localIRODSTransfer)
			throws JargonException;

	/**
	 * 
	 * @throws JargonException
	 */
	void processQueueAtStartup() throws JargonException;

	/**
	 * Update the information about a transfer
	 * 
	 * @param localIrodsTransfer
	 *            {@link Transfer} to be updated
	 * @throws JargonException
	 */
	void updateLocalIRODSTransfer(final Transfer localIrodsTransfer)
			throws JargonException;

	/**
	 * Look up the given transfer from the transfer store based on the unique id
	 * 
	 * @param id
	 *            <code>Long</code> with the unique id of the transfer
	 * @return {@link Transfer}
	 * @throws JargonException
	 */
	Transfer findLocalIRODSTransferById(Long id)
			throws JargonException;

	/**
	 * Look up the given tranfer from the transfer store based on the unique id,
	 * and initialize any child information
	 * 
	 * @param id
	 *            <code>Long</code> with the unique id of the transfer
	 * @return {@link Transfer}
	 * @throws JargonException
	 */
	Transfer findLocalIRODSTransferByIdInitializeItems(Long id)
			throws JargonException;

	void addItemToTransfer(Transfer localIRODSTransfer,
			TransferItem localIRODSTransferItem)
			throws JargonException;

	/**
	 * Enqueue a copy operation from iRODS to iRODS
	 * 
	 * @param irodsSourceAbsolutePath
	 *            <code>String</code> with the absolute path to the iRODS source
	 *            file
	 * @param targetResource
	 *            <code>String</code> with the resource to which to copy the
	 *            file
	 * @param irodsTargetAbsolutePath
	 *            <code>String</code> with the target absolute path for the copy
	 *            operation
	 * @param gridAccount
	 *            {@link GridAccount} that describes the host and user
	 *            information
	 * @return {@link Transfer} that represents the enqueued operation
	 * @throws JargonException
	 */
	Transfer enqueueCopyTransfer(String irodsSourceAbsolutePath,
			String targetResource, String irodsTargetAbsolutePath,
			GridAccount gridAccount) throws JargonException;

	/**
	 * Enqueue a synchronization operation between a local and an iRODS folder
	 * 
	 * @param synchronization
	 *            {@link Synchronization} that specifies the folder synch
	 *            relationship
	 * @param gridAccount
	 *            {@link GridAccount} that describes the host and user
	 *            information
	 * @return
	 * @throws JargonException
	 */
	Transfer enqueueSynchTransfer(Synchronization synchronization,
			GridAccount gridAccount) throws JargonException;

	/**
	 * Delete the entire contents of the transfser queue, regardless of status
	 * @throws JargonException
	 */
	void purgeEntireQueue() throws JargonException;

}