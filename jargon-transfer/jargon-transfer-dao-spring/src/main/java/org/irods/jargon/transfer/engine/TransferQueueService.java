package org.irods.jargon.transfer.engine;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransferItem;

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
	LocalIRODSTransfer enqueuePutTransfer(final String localSourceAbsolutePath,
			final String targetIRODSAbsolutePath, final String targetResource,
			final IRODSAccount irodsAccount) throws JargonException;

	/**
	 * 
	 * @param irodsSourceAbsolutePath
	 * @param targetLocalAbsolutePath
	 * @param sourceResource
	 * @param irodsAccount
	 * @return
	 * @throws JargonException
	 */
	LocalIRODSTransfer enqueueGetTransfer(final String irodsSourceAbsolutePath,
			final String targetLocalAbsolutePath, final String sourceResource,
			final IRODSAccount irodsAccount) throws JargonException;

	/**
	 * 
	 * @param localIRODSTransfer
	 * @param transferManager
	 * @throws JargonException
	 */
	void markTransferAsErrorAndTerminate(
			final LocalIRODSTransfer localIRODSTransfer,
			final TransferManager transferManager) throws JargonException;

	/**
	 * 
	 * @param localIRODSTransfer
	 * @param errorException
	 * @param transferManager
	 * @throws JargonException
	 */
	void markTransferAsErrorAndTerminate(
			final LocalIRODSTransfer localIRODSTransfer,
			final Exception errorException,
			final TransferManager transferManager) throws JargonException;

	/**
	 * 
	 * @param countOfEntriesToShow
	 * @return
	 * @throws JargonException
	 */
	List<LocalIRODSTransfer> getLastNInQueue(final int countOfEntriesToShow)
			throws JargonException;

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
	List<LocalIRODSTransferItem> getAllTransferItemsForTransfer(
			final Long localIRODSTransferId) throws JargonException;

	/**
	 * 
	 * @param localIRODSTransferId
	 * @return
	 * @throws JargonException
	 */
	List<LocalIRODSTransferItem> getErrorTransferItemsForTransfer(
			final Long localIRODSTransferId) throws JargonException;

	/**
	 * 
	 * @param localIRODSTransfer
	 * @throws JargonException
	 */
	void restartTransfer(final LocalIRODSTransfer localIRODSTransfer)
			throws JargonException;

	/**
	 * 
	 * @param localIRODSTransfer
	 * @throws JargonException
	 */
	void resubmitTransfer(final LocalIRODSTransfer localIRODSTransfer)
			throws JargonException;

	/**
	 * 
	 * @param irodsAbsolutePath
	 * @param targetResource
	 * @param irodsAccount
	 * @return
	 * @throws JargonException
	 */
	LocalIRODSTransfer enqueueReplicateTransfer(final String irodsAbsolutePath,
			final String targetResource, final IRODSAccount irodsAccount)
			throws JargonException;

	/**
	 * 
	 * @param localIRODSTransfer
	 * @throws JargonException
	 */
	void setTransferAsCancelled(final LocalIRODSTransfer localIRODSTransfer)
			throws JargonException;

	/**
	 * 
	 * @throws JargonException
	 */
	void processQueueAtStartup() throws JargonException;
	
	void updateLocalIRODSTransfer(final LocalIRODSTransfer localIrodsTransfer) throws JargonException;

	LocalIRODSTransfer findLocalIRODSTransferById(Long id)
			throws JargonException;

	LocalIRODSTransfer findLocalIRODSTransferByIdInitializeItems(Long id)
			throws JargonException;

	void addItemToTransfer(LocalIRODSTransfer localIRODSTransfer,
			LocalIRODSTransferItem localIRODSTransferItem)
			throws JargonException;

}