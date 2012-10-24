package org.irods.jargon.transfer.engine;

import java.io.File;
import java.util.Date;

import org.hibernate.HibernateException;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;
import org.irods.jargon.datautils.synchproperties.SynchPropertiesService;
import org.irods.jargon.datautils.synchproperties.SynchPropertiesServiceImpl;
import org.irods.jargon.datautils.tree.FileTreeDiffUtility;
import org.irods.jargon.datautils.tree.FileTreeDiffUtilityImpl;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransferItem;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.irods.jargon.transfer.dao.domain.TransferState;
import org.irods.jargon.transfer.engine.synch.ConflictingSynchException;
import org.irods.jargon.transfer.engine.synch.SynchException;
import org.irods.jargon.transfer.synch.InPlaceSynchronizingDiffProcessorImpl;
import org.irods.jargon.transfer.synch.SynchronizeProcessorImpl;
import org.irods.jargon.transfer.util.DomainUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to manage a transfer from a client to an iRODS server via put ,
 * replicate, copy, synch, or get. This engine will monitor transfers and
 * maintain status information for reporting, restarting, and other management
 * aspects, reporting this information to the <code>TransferManager</code>.
 * <p/>
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

	private final Logger log = LoggerFactory
			.getLogger(IRODSLocalTransferEngine.class);

	private final TransferManagerImpl transferManager;

	private LocalIRODSTransfer currentTransfer;

	private final TransferControlBlock transferControlBlock;

	private TransferEngineConfigurationProperties transferEngineConfigurationProperties;

	private boolean aborted = false;

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
	 * @param transferEngineConfigurationProperties
	 *            {@link TransferEngineConfigurationProperties} that will
	 *            control the behavior of the transfers. Note that the caller
	 *            has already resolved the <code>TransferOptions</code> in the
	 *            <code>TransferControlBlock</code>, and this class does no
	 *            further option resolution, it uses the information in the
	 *            <code>TransferControlBlock</code> to control transfer
	 *            behavior, and only uses the
	 *            <code>TransferEngineConfigurationProperties</code> for
	 *            transfer engine specific configuration outside of options used
	 *            within the Jargon core libraries
	 * 
	 * @return <code>IRODSLocalTransferEngine</code> instance.
	 * @throws JargonException
	 */

	protected static IRODSLocalTransferEngine instance(
			final TransferManagerImpl transferManager,
			final TransferControlBlock transferControlBlock,
			final TransferEngineConfigurationProperties transferEngineConfigurationProperties)
			throws JargonException {

		return new IRODSLocalTransferEngine(transferManager,
				transferControlBlock, transferEngineConfigurationProperties);

	}

	private IRODSLocalTransferEngine(
			final TransferManagerImpl transferManager,
			final TransferControlBlock transferControlBlock,
			final TransferEngineConfigurationProperties transferEngineConfigurationProperties)
			throws JargonException {

		if (transferManager == null) {
			throw new JargonException("transferManager is null");
		}

		if (transferControlBlock == null) {
			throw new JargonException("transferControlBlock is null");
		}

		this.transferManager = transferManager;
		this.transferControlBlock = transferControlBlock;
		this.transferEngineConfigurationProperties = transferEngineConfigurationProperties;

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

		IRODSAccount irodsAccount = DomainUtils
				.irodsAccountFromGridAccount(localIrodsTransfer
						.getGridAccount());

		// initiate the operation and process call-backs

		final DataTransferOperations dataTransferOperations = transferManager
				.getIrodsFileSystem().getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		final IRODSFileFactory irodsFileFactory = transferManager
				.getIrodsFileSystem().getIRODSFileFactory(irodsAccount);

		switch (localIrodsTransfer.getTransferType()) {
		case PUT:
			transferException = transferTypePut(localIrodsTransfer,
					dataTransferOperations, irodsFileFactory);
			break;
		case REPLICATE:
			transferException = transferTypeReplicate(localIrodsTransfer,
					dataTransferOperations, irodsFileFactory);
			break;
		case GET:
			transferException = transferTypeGet(localIrodsTransfer,
					dataTransferOperations, irodsFileFactory);
			break;
		case COPY:
			transferException = transferTypeCopy(localIrodsTransfer,
					dataTransferOperations, irodsFileFactory);
			break;
		case SYNCH:
			transferException = transferTypeSynch(localIrodsTransfer,
					dataTransferOperations, irodsFileFactory, irodsAccount);
			break;
		default:
			throw new JargonException("unknown operation type in transfer");
		}

		log.info("file system closed...getting transfer to wrap up");

		LocalIRODSTransfer wrapUpTransfer = getTransferManager()
				.getTransferQueueService().findLocalIRODSTransferById(
						localIrodsTransfer.getId());

		log.info("wrap up transfer before update:{}", wrapUpTransfer);

		if (aborted == true) {
			log.info("transfer was aborted, mark as cancelled, warning status, and add a message");
			markTransferWasAborted(wrapUpTransfer);
		} else if (transferException != null) {
			log.warn("error in transfer will be processed as exception",
					transferException);
			markTransferException(wrapUpTransfer, transferException);
		} else if (wrapUpTransfer.getTransferState() != null
				&& (wrapUpTransfer.getTransferState().equals(
						TransferState.PAUSED) || wrapUpTransfer
						.getTransferState().equals(TransferState.CANCELLED))) {
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
		transferManager.getTransferQueueService().updateLocalIRODSTransfer(
				wrapUpTransfer);

		if (wrapUpTransfer.getSynchronization() != null) {
			updateSynchronizationWithTransferResults(wrapUpTransfer);
		}

		log.info("updated");
		setCurrentTransfer(wrapUpTransfer);

		log.info("transfer processing wrapped up");

	}

	/*
	 * Update the enclosing synchronization with the transfer data
	 */
	private void updateSynchronizationWithTransferResults(
			final LocalIRODSTransfer localIrodsTransfer) throws JargonException {

		Synchronization synchronization = localIrodsTransfer
				.getSynchronization();
		synchronization.setLastSynchronizationMessage(localIrodsTransfer
				.getGlobalException());
		synchronization.setLastSynchronizationStatus(localIrodsTransfer
				.getTransferStatus());
		synchronization
				.setLastSynchronized(localIrodsTransfer.getTransferEnd());

		log.info(
				"updating synchronization after transfer process completed to:{}",
				synchronization);
		try {
			transferManager.getTransferServiceFactory()
					.instanceSynchManagerService()
					.updateSynchConfiguration(synchronization);
		} catch (ConflictingSynchException e) {
			log.error(
					"conflicting synch exception updating synch after transfer",
					e);
			throw new JargonException(
					"conflicting synch exception updating after transfer completed",
					e);
		} catch (SynchException e) {
			log.error("synch exception updating synch after transfer", e);
			throw new JargonException(
					"synch exception updating after transfer completed", e);
		}

	}

	private JargonException transferTypeSynch(
			final LocalIRODSTransfer localIrodsTransfer,
			final DataTransferOperations dataTransferOperations,
			final IRODSFileFactory irodsFileFactory,
			final IRODSAccount irodsAccount) {

		JargonException transferException = null;

		try {
			log.info("transferTypeSynch");

			/*
			 * right now, the synchronizing processor is assumed to be the
			 * InPlaceSynchronizingDiffProcessor, later this and other
			 * dependencies for synch can be pulled out to configuration
			 */

			InPlaceSynchronizingDiffProcessorImpl synchronizingDiffProcessor = new InPlaceSynchronizingDiffProcessorImpl();
			synchronizingDiffProcessor.setCallbackListener(this);
			synchronizingDiffProcessor
					.setIrodsAccessObjectFactory(transferManager
							.getIrodsFileSystem().getIRODSAccessObjectFactory());
			synchronizingDiffProcessor.setIrodsAccount(irodsAccount);
			synchronizingDiffProcessor
					.setTransferControlBlock(transferControlBlock);
			synchronizingDiffProcessor.setTransferManager(transferManager);

			log.info("built synchronizingDiffProcessor:{}",
					synchronizingDiffProcessor);

			FileTreeDiffUtility fileTreeDiffUtility = new FileTreeDiffUtilityImpl(
					irodsAccount, transferManager.getIrodsFileSystem()
							.getIRODSAccessObjectFactory());
			SynchPropertiesService synchPropertiesService = new SynchPropertiesServiceImpl(
					transferManager.getIrodsFileSystem()
							.getIRODSAccessObjectFactory(), irodsAccount);

			SynchronizeProcessorImpl synchronizeProcessorImpl = new SynchronizeProcessorImpl();
			synchronizeProcessorImpl
					.setFileTreeDiffUtility(fileTreeDiffUtility);
			synchronizeProcessorImpl
					.setIrodsAccessObjectFactory(transferManager
							.getIrodsFileSystem().getIRODSAccessObjectFactory());
			synchronizeProcessorImpl.setIrodsAccount(irodsAccount);
			synchronizeProcessorImpl
					.setSynchPropertiesService(synchPropertiesService);
			synchronizeProcessorImpl
					.setFileTreeDiffUtility(fileTreeDiffUtility);
			synchronizeProcessorImpl
					.setSynchronizingDiffProcessor(synchronizingDiffProcessor);
			synchronizeProcessorImpl
					.setTransferControlBlock(transferControlBlock);
			synchronizeProcessorImpl.setTransferManager(transferManager);
			synchronizeProcessorImpl.setCallbackListener(this);
			synchronizingDiffProcessor
					.setCallbackListener(synchronizeProcessorImpl);

			log.info("synchronizeProcessor was built:{}",
					synchronizeProcessorImpl);

			synchronizeProcessorImpl
					.synchronizeLocalToIRODS(localIrodsTransfer);
			log.info("synchronize processing done...");

		} catch (JargonException je) {
			log.error("exception in transfer will be marked as a global exception, ending the transfer operation");
			transferException = je;
		}
		return transferException;

	}

	private JargonException transferTypeCopy(
			final LocalIRODSTransfer localIrodsTransfer,
			final DataTransferOperations dataTransferOperations,
			final IRODSFileFactory irodsFileFactory) {

		JargonException transferException = null;

		try {
			log.info("transferTypeCopy");
			dataTransferOperations.copy(
					localIrodsTransfer.getLocalAbsolutePath(),
					localIrodsTransfer.getGridAccount().getDefaultResource(),
					localIrodsTransfer.getIrodsAbsolutePath(), this, true,
					transferControlBlock);
		} catch (JargonException je) {
			log.error("exception in transfer will be marked as a global exception, ending the transfer operation");
			transferException = je;
		}
		return transferException;

	}

	private void markTransferWasAborted(final LocalIRODSTransfer wrapUpTransfer)
			throws JargonException {
		wrapUpTransfer
				.setTransferStatus(org.irods.jargon.transfer.dao.domain.TransferStatus.WARNING);
		wrapUpTransfer.setTransferState(TransferState.CANCELLED);
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
		targetFile.setResource(localIrodsTransfer.getGridAccount()
				.getDefaultResource());
		final File localFile = new File(
				localIrodsTransfer.getLocalAbsolutePath());

		JargonException transferException = null;

		try {
			log.info("put: {}" + localFile);
			dataTransferOperations.putOperation(localFile, targetFile, this,
					transferControlBlock);
		} catch (JargonException je) {
			log.error("exception in transfer will be marked as a global exception, ending the transfer operation");
			transferException = je;
		}
		return transferException;
	}

	/**
	 * Process a get transfer
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
		sourceFile.setResource(localIrodsTransfer.getGridAccount()
				.getDefaultResource());
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
		targetFile.setResource(localIrodsTransfer.getGridAccount()
				.getDefaultResource());
		JargonException transferException = null;

		try {
			dataTransferOperations.replicate(
					localIrodsTransfer.getIrodsAbsolutePath(),
					localIrodsTransfer.getGridAccount().getDefaultResource(),
					this,
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
						.setTransferStatus(org.irods.jargon.transfer.dao.domain.TransferStatus.WARNING);
				localIrodsTransfer.setTransferState(TransferState.COMPLETE);
				transferManager.notifyWarningCondition();
			} else if (transferControlBlock.getErrorCount() > 0) {
				localIrodsTransfer
						.setTransferStatus(org.irods.jargon.transfer.dao.domain.TransferStatus.ERROR);
				localIrodsTransfer.setTransferState(TransferState.COMPLETE);
				transferManager.notifyErrorCondition();
			} else {
				localIrodsTransfer
						.setTransferStatus(org.irods.jargon.transfer.dao.domain.TransferStatus.OK);
				localIrodsTransfer.setTransferState(TransferState.COMPLETE);
			}
		}
	}

	/**
	 * @throws JargonException
	 */
	private void markWarningZeroFilesTransferred(
			final LocalIRODSTransfer localIrodsTransfer) throws JargonException {
		if (localIrodsTransfer.getSynchronization() != null) {
			log.info("transfer with no files is not a warning during synch");
			localIrodsTransfer
					.setTransferStatus(org.irods.jargon.transfer.dao.domain.TransferStatus.OK);
			localIrodsTransfer.setTransferState(TransferState.COMPLETE);
			return;
		}
		log.warn("no files transferred, ignore and close out as a warning");
		localIrodsTransfer
				.setTransferStatus(org.irods.jargon.transfer.dao.domain.TransferStatus.WARNING);
		localIrodsTransfer.setTransferState(TransferState.COMPLETE);
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
				.setTransferStatus(org.irods.jargon.transfer.dao.domain.TransferStatus.ERROR);
		localIrodsTransfer.setTransferState(TransferState.COMPLETE);
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

		if (log.isInfoEnabled()) {
			if (transferStatus.getTransferState() == TransferStatus.TransferState.PAUSED
					|| transferStatus.getTransferState() == TransferStatus.TransferState.CANCELLED) {
				log.info("pause or cancel was encountered in the callbacks:{}",
						transferStatus);
			}
		}

		if (transferStatus.getTransferState() == TransferStatus.TransferState.OVERALL_INITIATION) {
			log.debug("got startup 0th transfer callback, ignore in database, but do callback to transfer status listener");
			transferManager.notifyStatusUpdate(transferStatus);

		} else if (transferStatus.getTransferState() == TransferStatus.TransferState.IN_PROGRESS_START_FILE) {
			log.debug("got start of file transfer callback, ignore in database, but do callback to transfer status listener");
			transferManager.notifyStatusUpdate(transferStatus);

		} else {

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

		log.info("processing status callback of :{}", transferStatus);

		if (transferStatus.getTransferState() == TransferStatus.TransferState.RESTARTING) {
			log.debug("restarting:{}", transferStatus);
			return;
		}

		if (transferStatus.getTransferState() == TransferStatus.TransferState.FAILURE) {
			log.error("error in this transfer, mark");
			localIRODSTransferItem.setError(true);
			Exception exception = transferStatus.getTransferException();
			
			if (exception != null) {
				localIRODSTransferItem.setErrorMessage(exception.getMessage());
			}

		} else {
			localIRODSTransferItem.setError(false);
		}

		LocalIRODSTransfer mergedTransfer = getTransferManager()
				.getTransferQueueService().findLocalIRODSTransferById(
						currentTransfer.getId());
		log.info("loaded merged transfer for status update:{}", mergedTransfer);

		boolean updateItemRequired = true;
		// if pause or cancel, update overall status, no item to store
		if (transferStatus.getTransferState() == TransferStatus.TransferState.CANCELLED) {
			log.info(">>>>>>>>>>>>>>>>>>this transfer has been cancelled");
			mergedTransfer.setTransferState(TransferState.CANCELLED);
			updateItemRequired = false;
		} else if (transferStatus.getTransferState() == TransferStatus.TransferState.PAUSED) {
			log.info("this transfer has been paused");
			mergedTransfer.setTransferState(TransferState.PAUSED);
			updateItemRequired = false;
		}

		if (transferStatus.getTransferState() == TransferStatus.TransferState.SUCCESS
				|| transferStatus.getTransferState() == TransferStatus.TransferState.IN_PROGRESS_COMPLETE_FILE) {

			// if this is a stand alone transfer (not part of a synch) update
			// the wrapping transfer with last status info
			if (transferStatus.getTransferEnclosingType() != TransferStatus.TransferType.SYNCH) {

				log.info("updated last good path to:{}",
						transferStatus.getSourceFileAbsolutePath());
				mergedTransfer.setLastSuccessfulPath(transferStatus
						.getSourceFileAbsolutePath());
				mergedTransfer.setTotalFilesCount(transferStatus
						.getTotalFilesToTransfer());
				mergedTransfer.setTotalFilesTransferredSoFar(transferStatus
						.getTotalFilesTransferredSoFar());
			}

			if (isLogSuccessfulTransfers()) {
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
			transferManager.getTransferQueueService().addItemToTransfer(
					mergedTransfer, localIRODSTransferItem);
		}

		if (transferStatus.getTransferEnclosingType() != TransferStatus.TransferType.SYNCH) {
			log.info("final merged transfer:{}", mergedTransfer);
			getTransferManager().getTransferQueueService()
					.updateLocalIRODSTransfer(mergedTransfer);
			log.info("update done");
		}
		this.currentTransfer = mergedTransfer;
		log.info("transfer item status saved in database");

	}

	/**
	 * Close the iRODS connection in this thread. This is called by the creator
	 * of this <code>IRODSLocalTransferEngine</code> to wrap up any connections
	 * that are in the <code>ThreadLocal</code> cache.
	 */
	protected synchronized void cleanUp() {
		transferManager.getIrodsFileSystem().closeAndEatExceptions();
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
		boolean logSuccessful = false;
		if (transferEngineConfigurationProperties != null) {
			logSuccessful = transferEngineConfigurationProperties
					.isLogSuccessfulTransfers();
		}
		return logSuccessful;
	}

	@Override
	public void overallStatusCallback(final TransferStatus transferStatus)
			throws JargonException {
		log.info("overall status callback:{}", transferStatus);
		transferManager.notifyOverallStatusUpdate(transferStatus);
	}

	/**
	 * @return the transferEngineConfigurationProperties
	 */
	public synchronized TransferEngineConfigurationProperties getTransferEngineConfigurationProperties() {
		return transferEngineConfigurationProperties;
	}

	/**
	 * @param transferEngineConfigurationProperties
	 *            the transferEngineConfigurationProperties to set
	 */
	public synchronized void setTransferEngineConfigurationProperties(
			final TransferEngineConfigurationProperties transferEngineConfigurationProperties) {
		this.transferEngineConfigurationProperties = transferEngineConfigurationProperties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.transfer.TransferStatusCallbackListener#
	 * transferAsksWhetherToOverwriteDuringOperation(java.lang.String, boolean)
	 */
	@Override
	public CallbackResponse transferAsksWhetherToForceOperation(
			String irodsAbsolutePath, boolean isCollection) {
		// currently will overwrite, this needs to be set in transfer options
		return CallbackResponse.YES_FOR_ALL;

	}

}
