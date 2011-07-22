package org.irods.jargon.transfer.synch;

import java.io.File;
import java.util.Enumeration;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.datautils.synchproperties.SynchPropertiesService;
import org.irods.jargon.datautils.tree.FileTreeDiffEntry;
import org.irods.jargon.datautils.tree.FileTreeDiffEntry.DiffType;
import org.irods.jargon.datautils.tree.FileTreeDiffUtility;
import org.irods.jargon.datautils.tree.FileTreeModel;
import org.irods.jargon.datautils.tree.FileTreeNode;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.irods.jargon.transfer.engine.TransferManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *FIXME:parked for now, eventually could transform into another synchDiffProcessor variant that uses the queue to sched gets/puts
 * Compare a local watched folder to a remote iRODS folder and enqueue necessary
 * transfers to synchronize between the two.
 * 
 * @author Mike Conway - DICE (www.irods.org) FIXME: decide on creation style
 *         TODO: dev notes a plus on one side before the last synch indicates a
 *         deletion? Skip for now, but test out how this would look
 * 
 */
public class CopyOfSynchronizeProcessorImpl implements SynchronizeProcessor {

	private IRODSAccount irodsAccount;

	private IRODSAccessObjectFactory irodsAccessObjectFactory;

	private SynchPropertiesService synchPropertiesService;

	private TransferManager transferManager;

	private FileTreeDiffUtility fileTreeDiffUtility;
	
	private SynchronizingDiffProcessor synchronizingDiffProcessor;

	public SynchronizingDiffProcessor getSynchronizingDiffProcessor() {
		return synchronizingDiffProcessor;
	}

	public void setSynchronizingDiffProcessor(
			SynchronizingDiffProcessor synchronizingDiffProcessor) {
		this.synchronizingDiffProcessor = synchronizingDiffProcessor;
	}

	private static final char SLASH = '/';

	private static final Logger log = LoggerFactory
			.getLogger(CopyOfSynchronizeProcessorImpl.class);

	/**
	 * Private constructor
	 * 
	 * @param irodsAccount
	 * @param irodsAccessObjectFactory
	 * @param transferManager
	 */
	public CopyOfSynchronizeProcessorImpl() {
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

		// gather the synch times and update the AVU metadata to reflect this
		// synch TODO: do I do this here or at end of synch? MCC
		// getTimestampsAndUpdateSynchDataInIRODS(irodsAccount.getUserName(),
		// synchDeviceName, irodsRootAbsolutePath);

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

		// process the diff, scheduling appropriate transfers to synchronize
		processDiff((FileTreeNode) diffModel.getRoot(),
				calculatedLocalRoot.toString(), calculatedIrodsRoot.toString(),
				timestampforLastSynchLeftHandSide,
				timestampForLastSynchRightHandSide);
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


	// given a tree model, do any necessary operations to synchronize
	private void processDiff(final FileTreeNode diffNode,
			final String localRootAbsolutePath,
			final String irodsRootAbsolutePath,
			final long timestampforLastSynchLeftHandSide,
			final long timestampForLastSynchRightHandSide)
			throws JargonException {

		FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) diffNode
				.getUserObject();

		if (fileTreeDiffEntry.getDiffType() == DiffType.DIRECTORY_NO_DIFF) {
			log.debug("evaluating directory: {}", fileTreeDiffEntry
					.getCollectionAndDataObjectListingEntry()
					.getFormattedAbsolutePath());
			FileTreeNode childNode;
			@SuppressWarnings("rawtypes")
			Enumeration children = diffNode.children();
			while (children.hasMoreElements()) {
				childNode = (FileTreeNode) children.nextElement();
				processDiff(childNode, localRootAbsolutePath,
						irodsRootAbsolutePath,
						timestampforLastSynchLeftHandSide,
						timestampForLastSynchRightHandSide);
			}
		} else if (fileTreeDiffEntry.getDiffType() == DiffType.LEFT_HAND_PLUS) {
			log.debug("local file is new directory {}", fileTreeDiffEntry
					.getCollectionAndDataObjectListingEntry()
					.getFormattedAbsolutePath());
			scheduleLocalToIrods(diffNode, localRootAbsolutePath,
					irodsRootAbsolutePath);
		} else if (fileTreeDiffEntry.getDiffType() == DiffType.RIGHT_HAND_PLUS) {
			log.debug("irods file is new directory {}", fileTreeDiffEntry
					.getCollectionAndDataObjectListingEntry()
					.getFormattedAbsolutePath());
			scheduleIrodsToLocal(diffNode, localRootAbsolutePath,
					irodsRootAbsolutePath);
		} else if (fileTreeDiffEntry.getDiffType() == DiffType.LEFT_HAND_NEWER) {
			log.debug("left hand file is newer than irods{}", fileTreeDiffEntry
					.getCollectionAndDataObjectListingEntry()
					.getFormattedAbsolutePath());
			scheduleLocalToIrods(diffNode, localRootAbsolutePath,
					irodsRootAbsolutePath);
		} else if (fileTreeDiffEntry.getDiffType() == DiffType.RIGHT_HAND_NEWER) {
			log.debug("irods files is newer than left hand side {}",
					fileTreeDiffEntry.getCollectionAndDataObjectListingEntry()
							.getFormattedAbsolutePath());
			scheduleIrodsToLocal(diffNode, localRootAbsolutePath,
					irodsRootAbsolutePath);
		} else {
			log.warn("unknown diff type:{}", fileTreeDiffEntry);
		}

	}

	private void scheduleIrodsToLocal(final FileTreeNode diffNode,
			final String localRootAbsolutePath,
			final String irodsRootAbsolutePath) throws JargonException {

		log.info("\n\n<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n\n");

		log.info("scheduleIrodsToLocal for diffNode:{}", diffNode);

		/*
		 * the diff node will have the absolute local path of the file, this is
		 * the source of the get. the iRODS path will be the local path minus
		 * the local root, appended to the iRODS root
		 */

		FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) diffNode
				.getUserObject();
		CollectionAndDataObjectListingEntry entry = fileTreeDiffEntry
				.getCollectionAndDataObjectListingEntry();

		String targetRelativePath;
		if (entry.getObjectType() == ObjectType.COLLECTION) {
			targetRelativePath = entry.getParentPath().substring(
					irodsRootAbsolutePath.length());
		} else {
			targetRelativePath = entry.getFormattedAbsolutePath().substring(
					irodsRootAbsolutePath.length());
		}

		StringBuilder sb = new StringBuilder(localRootAbsolutePath);
		sb.append(targetRelativePath);

		log.info("enqueueing a put to irods under target at:{}",
				targetRelativePath);
		transferManager.enqueueAGet(entry.getFormattedAbsolutePath(),
				sb.toString(), irodsAccount.getDefaultStorageResource(),
				irodsAccount);
	}

	/**
	 * the node is a local file/collection that needs to be scheduled to move to
	 * irods
	 * 
	 * @param diffNode
	 * @param localRootAbsolutePath
	 * @param irodsRootAbsolutePath
	 */
	private void scheduleLocalToIrods(final FileTreeNode diffNode,
			final String localRootAbsolutePath,
			final String irodsRootAbsolutePath) throws JargonException {
		/*
		 * the diff node will have the absolute path of the local file, this is
		 * the source of the put. the irods path will be the local parent
		 * collection relative path, appended to the local root.
		 */

		log.info("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>\n\n");
		log.info("scheduleLocalToIrods for diffNode:{}", diffNode);

		FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) diffNode
				.getUserObject();
		CollectionAndDataObjectListingEntry entry = fileTreeDiffEntry
				.getCollectionAndDataObjectListingEntry();

		String targetRelativePath;
		if (entry.getObjectType() == ObjectType.COLLECTION) {
			targetRelativePath = entry.getParentPath().substring(
					localRootAbsolutePath.length());
		} else {
			targetRelativePath = entry.getFormattedAbsolutePath().substring(
					localRootAbsolutePath.length());
		}

		StringBuilder sb = new StringBuilder(irodsRootAbsolutePath);
		sb.append(targetRelativePath);

		log.info("enqueueing a put to irods under target at:{}",
				targetRelativePath);
		transferManager.enqueueAPut(entry.getFormattedAbsolutePath(),
				sb.toString(), irodsAccount.getDefaultStorageResource(),
				irodsAccount);

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
			throw new IllegalStateException("no synchronizingDiffProcessor was set");
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

}
