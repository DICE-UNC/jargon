package org.irods.jargon.transfer.synch;

import java.io.File;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.TransferOptions;
import org.irods.jargon.core.packinstr.TransferOptions.ForceOption;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatus.TransferState;
import org.irods.jargon.core.transfer.TransferStatus.TransferType;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;
import org.irods.jargon.datautils.synchproperties.SynchPropertiesService;
import org.irods.jargon.datautils.tree.FileTreeDiffUtility;
import org.irods.jargon.datautils.tree.FileTreeModel;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.irods.jargon.transfer.engine.TransferManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compare a local watched folder to a remote iRODS folder and enqueue necessary
 * transfers to synchronize between the two. This implementation is meant to
 * plug into the <code>TransferManager</code>, and SYNCH transfer processes will
 * delegate to this class.
 * <p/>
 * The synchronizing processor is not thread-safe, and is meant to be
 * initialized and run by one thread.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public class SynchronizeProcessorImpl implements SynchronizeProcessor,
		TransferStatusCallbackListener {

	private IRODSAccount irodsAccount;
	private IRODSAccessObjectFactory irodsAccessObjectFactory;
	private SynchPropertiesService synchPropertiesService;
	private TransferManager transferManager;
	private FileTreeDiffUtility fileTreeDiffUtility;
	private SynchronizingDiffProcessor synchronizingDiffProcessor;
	private TransferControlBlock transferControlBlock;
	private TransferStatusCallbackListener callbackListener;

	/**
	 * @return the callbackListener
	 */
	public TransferStatusCallbackListener getCallbackListener() {
		return callbackListener;
	}

	/**
	 * @param callbackListener
	 *            the callbackListener to set
	 */
	public void setCallbackListener(
			final TransferStatusCallbackListener callbackListener) {
		this.callbackListener = callbackListener;
	}

	private static final char SLASH = '/';

	private static final Logger log = LoggerFactory
			.getLogger(SynchronizeProcessorImpl.class);

	public SynchronizingDiffProcessor getSynchronizingDiffProcessor() {
		return synchronizingDiffProcessor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.synch.SynchronizeProcessor#
	 * setSynchronizingDiffProcessor
	 * (org.irods.jargon.transfer.synch.SynchronizingDiffProcessor)
	 */
	@Override
	public void setSynchronizingDiffProcessor(
			final SynchronizingDiffProcessor synchronizingDiffProcessor) {
		this.synchronizingDiffProcessor = synchronizingDiffProcessor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.synch.SynchronizeProcessor#synchronizeLocalToIRODS
	 * (org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer)
	 */
	@Override
	public void synchronizeLocalToIRODS(
			final LocalIRODSTransfer localIRODSTransfer) throws JargonException {

		if (localIRODSTransfer == null) {
			throw new IllegalArgumentException("null localIRODSTransfer");
		}

		checkInitialization();

		processSynchGivenSynchMetadata(localIRODSTransfer, 0, 0);
		log.info("synch done");

		if (callbackListener != null) {
			TransferStatus overallSynchStartStatus = TransferStatus.instance(
					TransferType.SYNCH, localIRODSTransfer.getSynchronization()
							.getLocalSynchDirectory(), localIRODSTransfer
							.getSynchronization().getIrodsSynchDirectory(),
					localIRODSTransfer.getSynchronization()
							.getDefaultResourceName(), 0L, 0L, 0, 0,
					TransferState.SYNCH_COMPLETION, irodsAccount.getHost(),
					irodsAccount.getZone());
			callbackListener.overallStatusCallback(overallSynchStartStatus);
		}
		/*
		 * // look up the synch information stored in iRODS at the indicated
		 * root // path UserSynchTarget userSynchTarget = synchPropertiesService
		 * .getUserSynchTargetForUserAndAbsolutePath(
		 * irodsAccount.getUserName(), synchDeviceName, irodsRootAbsolutePath);
		 * log.info("user synch target resolved as:{}", userSynchTarget);
		 * synchronizeLocalToIRODS(synchDeviceName,
		 * userSynchTarget.getLocalSynchRootAbsolutePath(),
		 * irodsRootAbsolutePath, userSynchTarget.getLastLocalSynchTimestamp(),
		 * userSynchTarget.getLastIRODSSynchTimestamp());
		 */
	}

	private void processSynchGivenSynchMetadata(
			final LocalIRODSTransfer localIRODSTransfer,
			final long timestampforLastSynchLeftHandSide,
			final long timestampForLastSynchRightHandSide)
			throws JargonException {

		log.info("processSynchGivenSynchMetadata");

		if (localIRODSTransfer == null) {
			throw new IllegalArgumentException("null localIRODSTransfer");
		}

		Synchronization synchronization = localIRODSTransfer
				.getSynchronization();

		if (synchronization == null) {
			throw new JargonException("null synchronization in transfer");
		}

		log.info("localIRODSTransfer:{}", localIRODSTransfer);
		log.info("synchronization:{}", synchronization);

		String calculatedLocalRoot = "";
		if (synchronization.getLocalSynchDirectory().length() > 1) {
			if (synchronization.getLocalSynchDirectory().lastIndexOf(SLASH) == synchronization
					.getLocalSynchDirectory().length() - 1) {
				log.debug("removing a trailing slash from local absolute path");
				calculatedLocalRoot = synchronization.getLocalSynchDirectory()
						.substring(
								0,
								synchronization.getLocalSynchDirectory()
										.length() - 1);
			} else {
				calculatedLocalRoot = synchronization.getLocalSynchDirectory();
			}
		}

		String calculatedIrodsRoot = "";
		if (synchronization.getIrodsSynchDirectory().length() > 1) {
			if (synchronization.getIrodsSynchDirectory().lastIndexOf(SLASH) == synchronization
					.getIrodsSynchDirectory().length() - 1) {
				log.debug("removing a trailing slash from irods absolute path");
				calculatedIrodsRoot = synchronization.getIrodsSynchDirectory()
						.substring(
								0,
								synchronization.getIrodsSynchDirectory()
										.length() - 1);
			} else {
				calculatedIrodsRoot = synchronization.getIrodsSynchDirectory();
			}
		}

		if (callbackListener != null) {
			// make an overall status callback that a synch is initiated
			TransferStatus overallSynchStartStatus = TransferStatus.instance(
					TransferType.SYNCH,
					synchronization.getLocalSynchDirectory(),
					synchronization.getIrodsSynchDirectory(),
					synchronization.getDefaultResourceName(), 0L, 0L, 0, 0,
					TransferState.SYNCH_INITIALIZATION, irodsAccount.getHost(),
					irodsAccount.getZone());
			callbackListener.overallStatusCallback(overallSynchStartStatus);
		}

		// make an overall status callback that a synch is initiated
		if (callbackListener != null) {

			TransferStatus overallSynchStartStatus = TransferStatus.instance(
					TransferType.SYNCH,
					synchronization.getLocalSynchDirectory(),
					synchronization.getIrodsSynchDirectory(),
					synchronization.getDefaultResourceName(), 0L, 0L, 0, 0,
					TransferState.SYNCH_DIFF_GENERATION,
					irodsAccount.getHost(), irodsAccount.getZone());
			callbackListener.overallStatusCallback(overallSynchStartStatus);
		}

		// generate a diff between the lhs local directory and the rhs iRODS
		// directory
		FileTreeModel diffModel = fileTreeDiffUtility.generateDiffLocalToIRODS(
				new File(calculatedLocalRoot.toString()),
				calculatedIrodsRoot.toString(),
				timestampforLastSynchLeftHandSide,
				timestampForLastSynchRightHandSide);
		log.debug("diff model obtained:{}", diffModel);
		if (diffModel == null) {
			throw new JargonException(
					"null diff model returned, cannot process");
		}

		// make an overall status callback that a synch is initiated
		if (callbackListener != null) {

			TransferStatus overallSynchStartStatus = TransferStatus.instance(
					TransferType.SYNCH,
					synchronization.getLocalSynchDirectory(),
					synchronization.getIrodsSynchDirectory(),
					synchronization.getDefaultResourceName(), 0L, 0L, 0, 0,
					TransferState.SYNCH_DIFF_STEP, irodsAccount.getHost(),
					irodsAccount.getZone());
			callbackListener.overallStatusCallback(overallSynchStartStatus);
		}

		synchronizingDiffProcessor.processDiff(localIRODSTransfer, diffModel);

		log.debug("processing complete");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.synch.SynchronizeProcessor#
	 * getTimestampsAndUpdateSynchDataInIRODS(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void getTimestampsAndUpdateSynchDataInIRODS(final String userName,
			final String synchDeviceName, final String irodsRootAbsolutePath)
			throws JargonException {
		synchPropertiesService.updateTimestampsToCurrent(userName,
				synchDeviceName, irodsRootAbsolutePath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.synch.SynchronizeProcessor#getIrodsAccount()
	 */
	@Override
	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.synch.SynchronizeProcessor#setIrodsAccount(
	 * org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public void setIrodsAccount(final IRODSAccount irodsAccount) {
		this.irodsAccount = irodsAccount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.synch.SynchronizeProcessor#
	 * getIrodsAccessObjectFactory()
	 */
	@Override
	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.synch.SynchronizeProcessor#
	 * setIrodsAccessObjectFactory
	 * (org.irods.jargon.core.pub.IRODSAccessObjectFactory)
	 */
	@Override
	public void setIrodsAccessObjectFactory(
			final IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.synch.SynchronizeProcessor#getTransferManager()
	 */
	@Override
	public TransferManager getTransferManager() {
		return transferManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.synch.SynchronizeProcessor#setTransferManager
	 * (org.irods.jargon.transfer.engine.TransferManager)
	 */
	@Override
	public void setTransferManager(final TransferManager transferManager) {
		this.transferManager = transferManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.synch.SynchronizeProcessor#getFileTreeDiffUtility
	 * ()
	 */
	@Override
	public FileTreeDiffUtility getFileTreeDiffUtility() {
		return fileTreeDiffUtility;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.synch.SynchronizeProcessor#setFileTreeDiffUtility
	 * (org.irods.jargon.datautils.tree.FileTreeDiffUtility)
	 */
	@Override
	public void setFileTreeDiffUtility(
			final FileTreeDiffUtility fileTreeDiffUtility) {
		this.fileTreeDiffUtility = fileTreeDiffUtility;
	}

	/**
	 * checks that all appropriate values have been set.
	 */
	private void checkInitialization() {
		if (irodsAccount == null) {
			throw new IllegalStateException("no irodsAccount was set");
		}

		if (irodsAccessObjectFactory == null) {
			throw new IllegalStateException(
					"no irodsAccessObjectFactory was set");
		}

		if (transferManager == null) {
			throw new IllegalStateException("no transferManager was set");
		}

		if (synchPropertiesService == null) {
			throw new IllegalStateException("no synchPropertiesService was set");
		}

		if (fileTreeDiffUtility == null) {
			throw new IllegalStateException("no fileTreeDiffUtility was set");
		}

		if (synchronizingDiffProcessor == null) {
			throw new IllegalStateException(
					"no synchronizingDiffProcessor was set");
		}

		if (transferControlBlock == null) {
			throw new IllegalStateException("no transferControlBlock was set");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.synch.SynchronizeProcessor#
	 * getSynchPropertiesService()
	 */
	@Override
	public SynchPropertiesService getSynchPropertiesService() {
		return synchPropertiesService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.synch.SynchronizeProcessor#
	 * setSynchPropertiesService
	 * (org.irods.jargon.datautils.synchproperties.SynchPropertiesService)
	 */
	@Override
	public void setSynchPropertiesService(
			final SynchPropertiesService synchPropertiesService) {
		this.synchPropertiesService = synchPropertiesService;
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
		if (callbackListener != null) {
			log.debug("overall status callback:{}", transferStatus);
			callbackListener.statusCallback(transferStatus);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.transfer.TransferStatusCallbackListener#
	 * overallStatusCallback(org.irods.jargon.core.transfer.TransferStatus)
	 */
	@Override
	public void overallStatusCallback(final TransferStatus transferStatus)
			throws JargonException {
		if (callbackListener != null) {
			log.debug("overall status callback:{}", transferStatus);
			callbackListener.overallStatusCallback(transferStatus);
		}

	}

	/**
	 * Get the {@link TransferControlBlock} that is managing, and provides a
	 * communication method to the transferring process.
	 * 
	 * @return {@link TransferControlBlock} that provides 2-way communication
	 *         with the transferring process.
	 */
	public TransferControlBlock getTransferControlBlock() {
		return transferControlBlock;
	}

	/**
	 * Set the <code>TransferControlBlock</code> that manages the transferring
	 * process.
	 * 
	 * @param transferControlBlock
	 *            {@link TransferControlBlock} controlling the current transfer.
	 */
	public void setTransferControlBlock(
			final TransferControlBlock transferControlBlock) {
		this.transferControlBlock = transferControlBlock;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.transfer.TransferStatusCallbackListener#
	 * transferAsksWhetherToOverwriteDuringOperation(java.lang.String, boolean)
	 */
	@Override
	public ForceOption transferAsksWhetherToForceOperation(
			String irodsAbsolutePath, boolean isCollection) {
		// currently will overwrite, this needs to be set in transfer options
		return TransferOptions.ForceOption.USE_FORCE;

	}

}
