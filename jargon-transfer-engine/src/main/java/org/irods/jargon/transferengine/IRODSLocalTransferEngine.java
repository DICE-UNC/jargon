/**
 * 
 */
package org.irods.jargon.transferengine;

import java.io.File;
import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;
import org.irods.jargon.transferengine.domain.LocalIRODSTransfer;
import org.irods.jargon.transferengine.domain.LocalIRODSTransferItem;
import org.irods.jargon.transferengine.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to manage a transfer from a client to an iRODS server via put ,
 * replicate, or get. This engine will monitor transfers and maintain status
 * information for reporting, restarting, and other management aspects,
 * reporting this information to the <code>TransferManager</code>.
 * 
 * Note that this class is meant to receive and process callbacks from a single
 * transfer process. If, in the future, multiple simultaneous transfers are
 * implemented, there will need to be one transfer engine per transfer
 * operation. This class needs to maintain a reference to the current transfer
 * so that it may properly associate callbacks from the actual transfer with the
 * specfic enqueued transfer operation, which is held in the
 * <code>currentTransfer</code> instance variable.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
final class IRODSLocalTransferEngine implements TransferStatusCallbackListener {

	private final TransferManager transferManager;
	private LocalIRODSTransfer currentTransfer;
	private final TransferControlBlock transferControlBlock;
	private final boolean logSuccessfulTransfers;
	private boolean aborted = false;

	private static final Logger log = LoggerFactory
			.getLogger(IRODSLocalTransferEngine.class);

	/**
	 * Static initializer to create transfer engine. By default, successful
	 * transfers are to be logged.
	 * 
	 * @param irodsAccessObjectFactory
	 *            <code>IRODSAccessObjectFactoryImpl</code> that provides access
	 *            to objects that interact with iRODS.
	 * @param transferControlBlock
	 *            <code>TransferControlBlock</code> implementation that provides
	 *            a common communications object between the manager and the
	 *            actual transfer process. For example, this block has a method
	 *            to communicate a cancellation from the manager to the running
	 *            transfer. This block also can be implemented to provide a
	 *            filter to select files to transfer, such as a restart filter.
	 * @return <code>IRODSLocalTransferEngine</code> instance.
	 * @throws JargonException
	 */

	protected static IRODSLocalTransferEngine instance(
			final TransferManager transferManager,
			final TransferControlBlock transferControlBlock)
			throws JargonException {
		return new IRODSLocalTransferEngine(transferManager,
				transferControlBlock, true);
	}

	/**
	 * Static initializer to create transfer engine.
	 * 
	 * @param irodsAccessObjectFactory
	 *            <code>IRODSAccessObjectFactoryImpl</code> that provides access
	 *            to objects that interact with iRODS.
	 * @param transferControlBlock
	 *            <code>TransferControlBlock</code> implementation that provides
	 *            a common communications object between the manager and the
	 *            actual transfer process. For example, this block has a method
	 *            to communicate a cancellation from the manager to the running
	 *            transfer. This block also can be implemented to provide a
	 *            filter to select files to transfer, such as a restart filter.
	 * @param logSuccessfulTransfers
	 *            <code>boolean</code> that indicates whether successful
	 *            transfers are logged. This can be turned off by setting to
	 *            <code>false</code>, useful for very large transfers where
	 *            local database overhead is an issue.
	 * @return <code>IRODSLocalTransferEngine</code> instance.
	 * @throws JargonException
	 */

	protected static IRODSLocalTransferEngine instance(
			final TransferManager transferManager,
			final TransferControlBlock transferControlBlock,
			final boolean logSuccessfulTransfers) throws JargonException {
		return new IRODSLocalTransferEngine(transferManager,
				transferControlBlock, logSuccessfulTransfers);
	}

	private IRODSLocalTransferEngine(final TransferManager transferManager,
			final TransferControlBlock transferControlBlock,
			final boolean logSuccessfulTransfers) throws JargonException {

		if (transferManager == null) {
			throw new JargonException("transferManager is null");
		}

		if (transferControlBlock == null) {
			throw new JargonException("transferControlBlock is null");
		}

		this.transferManager = transferManager;
		this.transferControlBlock = transferControlBlock;
		this.logSuccessfulTransfers = logSuccessfulTransfers;
		IRODSFileSystem.instance();

	}

	/**
	 * Process the operation described by the <code>LocalIRODSTransfer</code>
	 * 
	 * @param localIrodsTransfer
	 *            <code>LocalIRODSTransfer</code> that has information to
	 *            accomplish a transfer of data to or from iRODS.
	 * @throws JargonException
	 */
	protected synchronized void processOperation(
			final LocalIRODSTransfer localIrodsTransfer) throws JargonException {

		if (localIrodsTransfer == null) {
			throw new JargonException("localIrodsTransfer is null");
		}

		log.info("processing transfer: {}", localIrodsTransfer);

		JargonException transferException = null;

		setCurrentTransfer(localIrodsTransfer);
		transferManager.notifyProcessing();

		final IRODSAccount irodsAccount = IRODSAccount.instance(
				localIrodsTransfer.getTransferHost(), localIrodsTransfer
						.getTransferPort(), localIrodsTransfer
						.getTransferUserName(), HibernateUtil
						.retrieve(localIrodsTransfer.getTransferPassword()),
				"/", localIrodsTransfer.getTransferZone(), localIrodsTransfer
						.getTransferResource());

		// initiate the operation and process callbacks
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		final DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		final IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);

		if (localIrodsTransfer.getTransferType().equals(
				LocalIRODSTransfer.TRANSFER_TYPE_PUT)) {
			transferException = transferTypePut(localIrodsTransfer,
					dataTransferOperations, irodsFileFactory);
		} else if (localIrodsTransfer.getTransferType().equals(
				LocalIRODSTransfer.TRANSFER_TYPE_REPLICATE)) {
			transferException = transferTypeReplicate(localIrodsTransfer,
					dataTransferOperations, irodsFileFactory);
		} else if (localIrodsTransfer.getTransferType().equals(
				LocalIRODSTransfer.TRANSFER_TYPE_GET)) {
			transferException = transferTypeGet(localIrodsTransfer,
					dataTransferOperations, irodsFileFactory);
		} else {
			log.error("invalid transfer type: {}", localIrodsTransfer);
			throw new JargonException("invalid transfer type:"
					+ localIrodsTransfer.getTransferType());
		}

		// wrap up
		log.info("processing finished for this operation");
		irodsFileSystem.close();
		// now update the transfer 'header'

		Transaction tx = null;

		log.info("getting hibernate session factory and opening session");
		final Session session = transferManager.getTransferQueueService()
				.getHibernateUtil().getSession();

		try {
			log.info("beginning tx");
			tx = session.beginTransaction();

			LocalIRODSTransfer wrapUpTransfer = (LocalIRODSTransfer) session
					.load(LocalIRODSTransfer.class, localIrodsTransfer.getId());
			log.info("wrap up transfer before update:{}", wrapUpTransfer);

			if (aborted == true) {
				log.info("transfer was aborted, mark as cancelled, warning status, and add a message");
				markTransferWasAborted(wrapUpTransfer);
			} else if (transferException != null) {
				markTransferException(wrapUpTransfer, transferException);
			} else if (wrapUpTransfer.getTransferState() != null
					&& (wrapUpTransfer.getTransferState().equals(
							LocalIRODSTransfer.TRANSFER_STATE_PAUSED) || wrapUpTransfer
							.getTransferState()
							.equals(LocalIRODSTransfer.TRANSFER_STATE_CANCELLED))) {
				log.info("paused or cancelled, do not compute error/warning global status");
			} else if (transferControlBlock.getTotalFilesTransferredSoFar() == 0
					&& transferControlBlock.getErrorCount() == 0) {
				markWarningZeroFilesTransferred(wrapUpTransfer);
			} else {
				evaluateTransferErrorsPerFile(wrapUpTransfer);
			}

			wrapUpTransfer.setTransferEnd(new Date());

			log.info("wrap up of finished transfer at update time {}",
					wrapUpTransfer);
			session.update(wrapUpTransfer);
			log.info("updated");
			setCurrentTransfer(wrapUpTransfer);
			log.info("commit");
			tx.commit();

		} catch (RuntimeException e) {

			if (tx != null) {
				tx.rollback();
			}

			log.error("error in transaction", e);
			throw new JargonException(e);
		} finally {
			// session.close();
			// LOG.info("session closed");
		}

	}

	private void markTransferWasAborted(final LocalIRODSTransfer wrapUpTransfer)
			throws JargonException {
		wrapUpTransfer
				.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_WARNING);
		wrapUpTransfer
				.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_CANCELLED);
		wrapUpTransfer.setGlobalException("transfer was aborted by user");
		transferManager.notifyWarningCondition();

	}

	/**
	 * @param localIrodsTransfer
	 * @param dataTransferOperations
	 * @param irodsFileFactory
	 * @return
	 * @throws JargonException
	 */
	private JargonException transferTypePut(
			final LocalIRODSTransfer localIrodsTransfer,
			final DataTransferOperations dataTransferOperations,
			final IRODSFileFactory irodsFileFactory) throws JargonException {

		final IRODSFile targetFile = irodsFileFactory
				.instanceIRODSFile(localIrodsTransfer.getIrodsAbsolutePath());
		targetFile.setResource(localIrodsTransfer.getTransferResource());
		final File localFile = new File(
				localIrodsTransfer.getLocalAbsolutePath());

		JargonException transferException = null;

		try {
			dataTransferOperations.putOperation(localFile, targetFile, this,
					transferControlBlock);
		} catch (JargonException je) {
			log.error("exception in transfer will be marked as a global exception, ending the transfer operation");
			transferException = je;
		}
		return transferException;
	}

	/**
	 * 
	 * @param localIrodsTransfer
	 * @param dataTransferOperations
	 * @param irodsFileFactory
	 * @return
	 * @throws JargonException
	 */
	private JargonException transferTypeGet(
			final LocalIRODSTransfer localIrodsTransfer,
			final DataTransferOperations dataTransferOperations,
			final IRODSFileFactory irodsFileFactory) throws JargonException {

		final IRODSFile sourceFile = irodsFileFactory
				.instanceIRODSFile(localIrodsTransfer.getIrodsAbsolutePath());
		sourceFile.setResource(localIrodsTransfer.getTransferResource());
		final File localFile = new File(
				localIrodsTransfer.getLocalAbsolutePath());

		JargonException transferException = null;

		try {
			dataTransferOperations.getOperation(sourceFile, localFile, this,
					transferControlBlock);
		} catch (JargonException je) {
			log.error("exception in transfer will be marked as a global exception, ending the transfer operation");
			transferException = je;
		}
		return transferException;
	}

	/**
	 * @param localIrodsTransfer
	 * @param dataTransferOperations
	 * @param irodsFileFactory
	 * @return
	 * @throws JargonException
	 */
	private JargonException transferTypeReplicate(
			final LocalIRODSTransfer localIrodsTransfer,
			final DataTransferOperations dataTransferOperations,
			final IRODSFileFactory irodsFileFactory) throws JargonException {
		final IRODSFile targetFile = irodsFileFactory
				.instanceIRODSFile(localIrodsTransfer.getIrodsAbsolutePath());
		targetFile.setResource(localIrodsTransfer.getTransferResource());
		JargonException transferException = null;

		try {
			dataTransferOperations.replicate(
					localIrodsTransfer.getIrodsAbsolutePath(),
					localIrodsTransfer.getTransferResource(), this,
					transferControlBlock);
		} catch (JargonException je) {
			log.error("exception in transfer will be marked as a global exception, ending the transfer operation");
			transferException = je;
		}
		return transferException;
	}

	/**
	 * @throws JargonException
	 */
	private void evaluateTransferErrorsPerFile(
			final LocalIRODSTransfer localIrodsTransfer) throws JargonException {

		synchronized (this) {
			if (transferControlBlock.getErrorCount() > 0
					&& transferControlBlock.getErrorCount() < transferControlBlock
							.getMaximumErrorsBeforeCanceling()) {
				localIrodsTransfer
						.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_WARNING);
				localIrodsTransfer
						.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_COMPLETE);
				transferManager.notifyWarningCondition();
			} else if (transferControlBlock.getErrorCount() > 0) {
				localIrodsTransfer
						.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_ERROR);
				localIrodsTransfer
						.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_COMPLETE);
				transferManager.notifyErrorCondition();
			} else {
				localIrodsTransfer
						.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_OK);
				localIrodsTransfer
						.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_COMPLETE);
			}
		}
	}

	/**
	 * @throws JargonException
	 */
	private void markWarningZeroFilesTransferred(
			final LocalIRODSTransfer localIrodsTransfer) throws JargonException {
		log.warn("no files transferred, ignore and close out as a warning");
		localIrodsTransfer
				.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_WARNING);
		localIrodsTransfer
				.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_COMPLETE);
		localIrodsTransfer
				.setGlobalException("There were no files found to transfer, transfer completed successfully with no action");
		transferManager.notifyWarningCondition();
	}

	/**
	 * @param transferException
	 * @throws JargonException
	 */
	private void markTransferException(
			final LocalIRODSTransfer localIrodsTransfer,
			final JargonException transferException) throws JargonException {
		localIrodsTransfer
				.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_ERROR);
		localIrodsTransfer
				.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_COMPLETE);
		localIrodsTransfer.setGlobalException(transferException.getMessage());
		transferManager.notifyErrorCondition();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.TransferStatusCallbackListener#statusCallback
	 * (org.irods.jargon.core.transfer.TransferStatus)
	 */
	@Override
	public void statusCallback(final TransferStatus transferStatus)
			throws JargonException {

		log.info("statusCallback: {}", transferStatus);

		// status callback from the recursive transfer operation in Jargon
		synchronized (this) {
			LocalIRODSTransferItem localIRODSTransferItem = new LocalIRODSTransferItem();
			localIRODSTransferItem.setFile(true);
			localIRODSTransferItem.setSourceFileAbsolutePath(transferStatus
					.getSourceFileAbsolutePath());
			localIRODSTransferItem.setTargetFileAbsolutePath(transferStatus
					.getTargetFileAbsolutePath());
			localIRODSTransferItem.setTransferredAt(new Date());
			processStatusCallback(transferStatus, localIRODSTransferItem);
			transferManager.notifyStatusUpdate(transferStatus);
		}

	}

	/**
	 * @param transferStatus
	 * @param localIRODSTransferItem
	 * @throws HibernateException
	 * @throws JargonException
	 */
	private void processStatusCallback(final TransferStatus transferStatus,
			final LocalIRODSTransferItem localIRODSTransferItem)
			throws HibernateException, JargonException {

		if (transferStatus.getTransferState() == TransferStatus.TransferState.FAILURE) {
			log.error("error in this transfer, mark");
			localIRODSTransferItem.setError(true);
			localIRODSTransferItem.setErrorMessage(transferStatus
					.getTransferException().getMessage());

		} else {
			localIRODSTransferItem.setError(false);
		}

		final Session session = transferManager.getTransferQueueService()
				.getHibernateUtil().getSession();

		Transaction tx = null;
		try {
			log.info("beginning tx to store status of this transfer item");
			tx = session.beginTransaction();

			LocalIRODSTransfer mergedTransfer = (LocalIRODSTransfer) session
					.load(LocalIRODSTransfer.class, currentTransfer.getId());

			log.info("loaded merged transfer for status update:{}",
					mergedTransfer);

			boolean updateItemRequired = true;
			// if pause or cancel, update overall status, no item to store
			if (transferStatus.getTransferState() == TransferStatus.TransferState.CANCELLED) {
				log.info("this transfer has been cancelled");
				mergedTransfer
						.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_CANCELLED);
				updateItemRequired = false;
			} else if (transferStatus.getTransferState() == TransferStatus.TransferState.PAUSED) {
				log.info("this transfer has been paused");
				mergedTransfer
						.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_PAUSED);
				updateItemRequired = false;
			}

			if (transferStatus.getTransferState() == TransferStatus.TransferState.SUCCESS) {

				log.info("updated last good path to:{}",
						transferStatus.getSourceFileAbsolutePath());
				mergedTransfer.setLastSuccessfulPath(transferStatus
						.getSourceFileAbsolutePath());
				mergedTransfer.setTotalFilesCount(transferStatus
						.getTotalFilesToTransfer());
				mergedTransfer.setTotalFilesTransferredSoFar(transferStatus
						.getTotalFilesTransferredSoFar());

				if (this.logSuccessfulTransfers) {
					updateItemRequired = true;
				} else {
					log.debug("transfer not logged in database");
					updateItemRequired = false;
				}
			}

			// add the item to the local transfers. Note that if this is a
			// success, it is not added as a line item if logSuccessfulTransfers
			// is false
			if (updateItemRequired) {
				localIRODSTransferItem.setLocalIRODSTransfer(mergedTransfer);
				log.info("updating transfer item:", localIRODSTransferItem);
				mergedTransfer.getLocalIRODSTransferItems().add(
						localIRODSTransferItem);
			}

			log.info("final merged transfer:{}", mergedTransfer);
			session.update(mergedTransfer);
			log.info("update done");
			session.flush();
			tx.commit();
			log.info("transfer item status saved in database");

		} catch (RuntimeException e) {

			if (tx != null) {
				tx.rollback();
			}

			log.error("error in transaction", e);
			throw new JargonException(e);
		} finally {
			// session.close();
			// LOG.info("session closed");
		}
	}

	protected synchronized LocalIRODSTransfer getCurrentTransfer() {
		return currentTransfer;
	}

	protected synchronized void setCurrentTransfer(
			final LocalIRODSTransfer currentTransfer) {
		this.currentTransfer = currentTransfer;
	}

	public synchronized TransferManager getTransferManager() {
		return transferManager;
	}

	public TransferControlBlock getTransferControlBlock() {
		return transferControlBlock;
	}

	public boolean isLogSuccessfulTransfers() {
		return logSuccessfulTransfers;
	}

}
