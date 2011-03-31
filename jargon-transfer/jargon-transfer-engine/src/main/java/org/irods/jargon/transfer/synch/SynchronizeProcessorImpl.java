package org.irods.jargon.transfer.synch;

import java.io.File;
import java.util.Enumeration;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.datautils.synchproperties.SynchPropertiesService;
import org.irods.jargon.datautils.synchproperties.UserSynchTarget;
import org.irods.jargon.datautils.tree.FileTreeDiffEntry;
import org.irods.jargon.datautils.tree.FileTreeDiffEntry.DiffType;
import org.irods.jargon.datautils.tree.FileTreeDiffUtility;
import org.irods.jargon.datautils.tree.FileTreeModel;
import org.irods.jargon.datautils.tree.FileTreeNode;
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
public class SynchronizeProcessorImpl implements SynchronizeProcessor {

	private IRODSAccount irodsAccount;

	private IRODSAccessObjectFactory irodsAccessObjectFactory;

	private SynchPropertiesService synchPropertiesService;

	private TransferManager transferManager;

	private FileTreeDiffUtility fileTreeDiffUtility;

	private static final char SLASH = '/';

	private static final Logger log = LoggerFactory
			.getLogger(SynchronizeProcessorImpl.class);

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
	 * org.irods.jargon.transfer.synch.SynchronizeProcessor#synchronizeLocalToIrods
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public void synchronizeLocalToIRODS(final String synchDeviceName,
			final String irodsRootAbsolutePath) throws JargonException {
		if (synchDeviceName == null || synchDeviceName.isEmpty()) {
			throw new IllegalArgumentException("null synchDeviceName");
		}

		if (irodsRootAbsolutePath == null || irodsRootAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null irodsRootAbsolutePath");
		}

		log.info("synchronizeLocalToIrods for device:{}", synchDeviceName);
		log.info("irodsRootAbsolutePath:{}", irodsRootAbsolutePath);
		// look up the synch information stored in iRODS at the indicated root
		// path
		UserSynchTarget userSynchTarget = synchPropertiesService
				.getUserSynchTargetForUserAndAbsolutePath(
						irodsAccount.getUserName(), synchDeviceName,
						irodsRootAbsolutePath);
		log.info("user synch target resolved as:{}", userSynchTarget);
		synchronizeLocalToIRODS(synchDeviceName,
				userSynchTarget.getLocalSynchRootAbsolutePath(),
				irodsRootAbsolutePath,
				userSynchTarget.getLastLocalSynchTimestamp(),
				userSynchTarget.getLastIRODSSynchTimestamp());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.synch.SynchronizeProcessor#synchronizeLocalToIRODS
	 * (java.lang.String, java.lang.String, java.lang.String, long, long)
	 */
	// FIXME: refactor to lookup info from sych data, doesn't need to be passed
	// here, add alt method for lookup
	@Override
	public void synchronizeLocalToIRODS(final String synchDeviceName,
			final String localRootAbsolutePath,
			final String irodsRootAbsolutePath,
			final long timestampforLastSynchLeftHandSide,
			final long timestampForLastSynchRightHandSide)
			throws JargonException {

		if (synchDeviceName == null || synchDeviceName.isEmpty()) {
			throw new IllegalArgumentException("null synchDeviceName");
		}

		if (localRootAbsolutePath == null || localRootAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null localRootAbsolutePath");
		}

		if (irodsRootAbsolutePath == null || irodsRootAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null irodsRootAbsolutePath");
		}

		if (timestampforLastSynchLeftHandSide < 0) {
			throw new IllegalArgumentException(
					"negative timestampforLastSynchLeftHandSide, set to 0 if not specified");
		}

		if (timestampForLastSynchRightHandSide < 0) {
			throw new IllegalArgumentException(
					"negative timestampForLastSynchRightHandSide, set to 0 if not specified");
		}

		checkInitialization();

		processSynchGivenSynchMetadata(synchDeviceName, localRootAbsolutePath,
				irodsRootAbsolutePath, timestampforLastSynchLeftHandSide,
				timestampForLastSynchRightHandSide);
	}

	/**
	 * @param synchDeviceName
	 * @param localRootAbsolutePath
	 * @param irodsRootAbsolutePath
	 * @param timestampforLastSynchLeftHandSide
	 * @param timestampForLastSynchRightHandSide
	 * @throws JargonException
	 */
	private void processSynchGivenSynchMetadata(final String synchDeviceName,
			final String localRootAbsolutePath,
			final String irodsRootAbsolutePath,
			final long timestampforLastSynchLeftHandSide,
			final long timestampForLastSynchRightHandSide)
			throws JargonException {
		checkForPendingSynchsOnTargetDirectory();

		log.info("synchronizeLocalToIRODS for device:{}", synchDeviceName);
		log.info("   localRootAbsolutePath:{}", localRootAbsolutePath);
		log.info("    irodsRootAbsolutePath:{}", irodsRootAbsolutePath);
		log.info("   timestampForLastSynchLeftHandSide:{}",
				timestampforLastSynchLeftHandSide);
		log.info("   timestampForLastSynchRightHandSide:{}",
				timestampForLastSynchRightHandSide);

		String calculatedLocalRoot = "";
		if (localRootAbsolutePath.length() > 1) {
			if (localRootAbsolutePath.lastIndexOf(SLASH) == localRootAbsolutePath
					.length() - 1) {
				log.debug("removing a trailing slash from local absolute path");
				calculatedLocalRoot = localRootAbsolutePath.substring(0,
						localRootAbsolutePath.length() - 1);
			} else {
				calculatedLocalRoot = localRootAbsolutePath;
			}
		}

		String calculatedIrodsRoot = "";
		if (irodsRootAbsolutePath.length() > 1) {
			if (irodsRootAbsolutePath.lastIndexOf(SLASH) == irodsRootAbsolutePath
					.length() - 1) {
				log.debug("removing a trailing slash from irods absolute path");
				calculatedIrodsRoot = irodsRootAbsolutePath.substring(0,
						irodsRootAbsolutePath.length() - 1);
			} else {
				calculatedIrodsRoot = irodsRootAbsolutePath;
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

	/**
	 * need to implement... Check the transfer queue, if there are any transfers
	 * already in the queue for this directory on iRODS (including enqueued,
	 * error, warning, paused, etc) that could possibly be restarted, then don't
	 * do the transfer. This is a check to avoid double-queue of a synch job.
	 */
	private void checkForPendingSynchsOnTargetDirectory() {
		// TODO: implement me

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
