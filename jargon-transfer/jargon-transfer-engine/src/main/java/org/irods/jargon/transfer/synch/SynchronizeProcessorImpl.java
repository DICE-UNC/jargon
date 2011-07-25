package org.irods.jargon.transfer.synch;

import java.io.File;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.transfer.TransferStatus;
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
 * transfers to synchronize between the two.
 * 
 * @author Mike Conway - DICE (www.irods.org) FIXME: decide on creation style
 *         TODO: dev notes a plus on one side before the last synch indicates a
 *         deletion? Skip for now, but test out how this would look
 * 
 */
public class SynchronizeProcessorImpl implements SynchronizeProcessor, TransferStatusCallbackListener {

	private IRODSAccount irodsAccount;
	private IRODSAccessObjectFactory irodsAccessObjectFactory;
	private SynchPropertiesService synchPropertiesService;
	private TransferManager transferManager;
	private FileTreeDiffUtility fileTreeDiffUtility;
	private SynchronizingDiffProcessor synchronizingDiffProcessor;
	private static final char SLASH = '/';

	private static final Logger log = LoggerFactory
			.getLogger(SynchronizeProcessorImpl.class);

	public SynchronizingDiffProcessor getSynchronizingDiffProcessor() {
		return synchronizingDiffProcessor;
	}

	@Override
	public void setSynchronizingDiffProcessor(
			final SynchronizingDiffProcessor synchronizingDiffProcessor) {
		this.synchronizingDiffProcessor = synchronizingDiffProcessor;
	}

	

	/**
	 * Private constructor
	 * 
	 * @param irodsAccount
	 * @param irodsAccessObjectFactory
	 * @param transferManager
	 */
	public SynchronizeProcessorImpl() {
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

		synchronizingDiffProcessor.processDiff(localIRODSTransfer,
				 diffModel);

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

	@Override
	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	@Override
	public void setIrodsAccount(final IRODSAccount irodsAccount) {
		this.irodsAccount = irodsAccount;
	}

	@Override
	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	@Override
	public void setIrodsAccessObjectFactory(
			final IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	@Override
	public TransferManager getTransferManager() {
		return transferManager;
	}

	@Override
	public void setTransferManager(final TransferManager transferManager) {
		this.transferManager = transferManager;
	}

	@Override
	public FileTreeDiffUtility getFileTreeDiffUtility() {
		return fileTreeDiffUtility;
	}

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
	}

	@Override
	public SynchPropertiesService getSynchPropertiesService() {
		return synchPropertiesService;
	}

	@Override
	public void setSynchPropertiesService(
			final SynchPropertiesService synchPropertiesService) {
		this.synchPropertiesService = synchPropertiesService;
	}

	@Override
	public void statusCallback(TransferStatus transferStatus)
			throws JargonException {
		log.debug("status callback:{}", transferStatus);
		
	}

	@Override
	public void overallStatusCallback(TransferStatus transferStatus)
			throws JargonException {
		log.debug("overall status callback:{}", transferStatus);
		
	}

}
