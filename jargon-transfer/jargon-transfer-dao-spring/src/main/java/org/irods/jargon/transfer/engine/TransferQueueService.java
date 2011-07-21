package org.irods.jargon.transfer.engine;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransferItem;
import org.irods.jargon.transfer.dao.domain.Synchronization;

public interface TransferQueueService {

    /**
     * 
     * @return
     * @throws JargonException
     */
    LocalIRODSTransfer dequeueTransfer() throws JargonException;

    /**
     * 
     * @param localSourceAbsolutePath
     * @param targetIRODSAbsolutePath
     * @param targetResource
     * @param irodsAccount
     * @return
     * @throws JargonException
     */
    LocalIRODSTransfer enqueuePutTransfer(final String localSourceAbsolutePath, final String targetIRODSAbsolutePath,
            final String targetResource, final IRODSAccount irodsAccount) throws JargonException;

    /**
     * 
     * @param irodsSourceAbsolutePath
     * @param targetLocalAbsolutePath
     * @param sourceResource
     * @param irodsAccount
     * @return
     * @throws JargonException
     */
    LocalIRODSTransfer enqueueGetTransfer(final String irodsSourceAbsolutePath, final String targetLocalAbsolutePath,
            final String sourceResource, final IRODSAccount irodsAccount) throws JargonException;

    /**
     * 
     * @param localIRODSTransfer
     * @param transferManager
     * @throws JargonException
     */
    void markTransferAsErrorAndTerminate(final LocalIRODSTransfer localIRODSTransfer,
            final TransferManager transferManager) throws JargonException;

    /**
     * 
     * @param localIRODSTransfer
     * @param errorException
     * @param transferManager
     * @throws JargonException
     */
    void markTransferAsErrorAndTerminate(final LocalIRODSTransfer localIRODSTransfer, final Exception errorException,
            final TransferManager transferManager) throws JargonException;

    /**
     * 
     * @param countOfEntriesToShow
     * @return
     * @throws JargonException
     */
    List<LocalIRODSTransfer> getLastNInQueue(final int countOfEntriesToShow) throws JargonException;

    /**
     * 
     * @return
     * @throws JargonException
     */
    List<LocalIRODSTransfer> getCurrentQueue() throws JargonException;

    /**
     * 
     * @return
     * @throws JargonException
     */
    List<LocalIRODSTransfer> getErrorQueue() throws JargonException;

    /**
     * 
     * @return
     * @throws JargonException
     */
    List<LocalIRODSTransfer> getWarningQueue() throws JargonException;

    /**
     * 
     * @return
     * @throws JargonException
     */
    List<LocalIRODSTransfer> showErrorTransfers() throws JargonException;

    /**
     * 
     * @return
     * @throws JargonException
     */
    List<LocalIRODSTransfer> showWarningTransfers() throws JargonException;

    /**
     * 
     * @return
     * @throws JargonException
     */
    List<LocalIRODSTransfer> getRecentQueue() throws JargonException;

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
    List<LocalIRODSTransferItem> getAllTransferItemsForTransfer(final Long localIRODSTransferId) throws JargonException;

    /**
     * 
     * @param localIRODSTransferId
     * @return
     * @throws JargonException
     */
    List<LocalIRODSTransferItem> getErrorTransferItemsForTransfer(final Long localIRODSTransferId)
            throws JargonException;

    /**
     * 
     * @param localIRODSTransfer
     * @throws JargonException
     */
    void restartTransfer(final LocalIRODSTransfer localIRODSTransfer) throws JargonException;

    /**
     * 
     * @param localIRODSTransfer
     * @throws JargonException
     */
    void resubmitTransfer(final LocalIRODSTransfer localIRODSTransfer) throws JargonException;

    /**
     * 
     * @param irodsAbsolutePath
     * @param targetResource
     * @param irodsAccount
     * @return
     * @throws JargonException
     */
    LocalIRODSTransfer enqueueReplicateTransfer(final String irodsAbsolutePath, final String targetResource,
            final IRODSAccount irodsAccount) throws JargonException;

    /**
     * 
     * @param localIRODSTransfer
     * @throws JargonException
     */
    void setTransferAsCancelled(final LocalIRODSTransfer localIRODSTransfer) throws JargonException;

    /**
     * 
     * @throws JargonException
     */
    void processQueueAtStartup() throws JargonException;

    /**
     * Update the information about a transfer
     * 
     * @param localIrodsTransfer
     *            {@link LocalIRODSTransfer} to be updated
     * @throws JargonException
     */
    void updateLocalIRODSTransfer(final LocalIRODSTransfer localIrodsTransfer) throws JargonException;

    /**
     * Look up the given transfer from the transfer store based on the unique id
     * 
     * @param id
     *            <code>Long</code> with the unique id of the transfer
     * @return {@link LocalIRODSTransfer}
     * @throws JargonException
     */
    LocalIRODSTransfer findLocalIRODSTransferById(Long id) throws JargonException;

    /**
     * Look up the given tranfer from the transfer store based on the unique id, and initialize any child information
     * 
     * @param id
     *            <code>Long</code> with the unique id of the transfer
     * @return {@link LocalIRODSTransfer}
     * @throws JargonException
     */
    LocalIRODSTransfer findLocalIRODSTransferByIdInitializeItems(Long id) throws JargonException;

    void addItemToTransfer(LocalIRODSTransfer localIRODSTransfer, LocalIRODSTransferItem localIRODSTransferItem)
            throws JargonException;

    /**
     * Enqueue a copy operation from iRODS to iRODS
     * 
     * @param irodsSourceAbsolutePath
     *            <code>String</code> with the absolute path to the iRODS source file
     * @param targetResource
     *            <code>String</code> with the resource to which to copy the file
     * @param irodsTargetAbsolutePath
     *            <code>String</code> with the target absolute path for the copy operation
     * @param irodsAccount
     *            {@link IRODSAccount} that describes the host and user information
     * @return {@link LocalIRODSTransfer} that represents the enqueued operation
     * @throws JargonException
     */
    LocalIRODSTransfer enqueueCopyTransfer(String irodsSourceAbsolutePath, String targetResource,
            String irodsTargetAbsolutePath, IRODSAccount irodsAccount) throws JargonException;

    /**
     * Enqueue a synchronization operation between a local and an iRODS folder
     * @param synchronization {@link Synchronization} that specifies the folder synch relationship
     * @param irodsAccount {@link IRODSAccount} that specifies the connection information
     * @return
     * @throws JargonException
     */
	LocalIRODSTransfer enqueueSynchTransfer(Synchronization synchronization,
			IRODSAccount irodsAccount) throws JargonException;

}