/**
 * 
 */
package org.irods.jargon.transfer.synch;

import java.util.Enumeration;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;
import org.irods.jargon.datautils.tree.FileTreeDiffEntry;
import org.irods.jargon.datautils.tree.FileTreeDiffEntry.DiffType;
import org.irods.jargon.datautils.tree.FileTreeModel;
import org.irods.jargon.datautils.tree.FileTreeNode;
import org.irods.jargon.transfer.TransferEngineException;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.irods.jargon.transfer.engine.TransferManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Given a diff model between a local and iRODS root directory, process the diff
 * to bring the directories into a synchronized state. This implementation will
 * do direct puts/gets as the diff is processed.
 * <p/>
 * This instance is meant to be created per-synchronization operation, and is
 * not meant to be shared across multiple synchronization processes.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class InPlaceSynchronizingDiffProcessorImpl implements
		SynchronizingDiffProcessor {

	private IRODSAccessObjectFactory irodsAccessObjectFactory;
	private TransferManager transferManager;
	private DataTransferOperations dataTransferOperations;
	private TransferStatusCallbackListener callbackListener;
	private IRODSAccount irodsAccount;

	private static final Logger log = LoggerFactory
			.getLogger(InPlaceSynchronizingDiffProcessorImpl.class);

	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	public void setIrodsAccount(final IRODSAccount irodsAccount) {
		this.irodsAccount = irodsAccount;
	}

	public TransferStatusCallbackListener getCallbackListener() {
		return callbackListener;
	}

	public void setCallbackListener(
			final TransferStatusCallbackListener callbackListener) {
		this.callbackListener = callbackListener;
	}

	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	public void setIrodsAccessObjectFactory(
			final IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	public TransferManager getTransferManager() {
		return transferManager;
	}

	public void setTransferManager(final TransferManager transferManager) {
		this.transferManager = transferManager;
	}

	/**
	 * Default constructor, expects setting of dependencies by injection
	 */
	public InPlaceSynchronizingDiffProcessorImpl() {

	}

	@Override
	public void processDiff(final LocalIRODSTransfer localIRODSTransfer,
			final FileTreeModel diffModel) throws TransferEngineException {

		log.info("processDiff()");

		if (localIRODSTransfer == null) {
			throw new IllegalArgumentException("null localIRODSTransfer");
		}

		if (diffModel == null) {
			throw new IllegalArgumentException("null diffModel");
		}

		log.info("localIRODSTransfer:{}", localIRODSTransfer);
		log.info("diffModel:{}", diffModel);

		if (localIRODSTransfer.getTransferType() != TransferType.SYNCH) {
			throw new TransferEngineException("not a synch transfer");
		}

		checkContracts();

		if (dataTransferOperations == null) {
			try {
				dataTransferOperations = irodsAccessObjectFactory
						.getDataTransferOperations(irodsAccount);
			} catch (JargonException e) {
				log.error("exception creating dataTransferOperations", e);
				throw new TransferEngineException(e);
			}
		}

		processDiffWithValidData(localIRODSTransfer, diffModel);

	}

	/**
	 * Do the actual diff processing, since contracts and dependencies have all
	 * been validated
	 * 
	 * @param localIRODSTransfer
	 * @param irodsAccount
	 * @param diffModel
	 * @throws TransferEngineException
	 */
	private void processDiffWithValidData(
			final LocalIRODSTransfer localIRODSTransfer,
			final FileTreeModel diffModel) throws TransferEngineException {

		log.info("processDiffWithValidData");
		log.info("difModel:{}", diffModel);
		Synchronization synchronization = localIRODSTransfer
				.getSynchronization();

		processDiff((FileTreeNode) diffModel.getRoot(),
				synchronization.getLocalSynchDirectory(),
				synchronization.getIrodsSynchDirectory(), 0, 0);

	}

	// given a tree model, do any necessary operations to synchronize
	private void processDiff(final FileTreeNode diffNode,
			final String localRootAbsolutePath,
			final String irodsRootAbsolutePath,
			final long timestampforLastSynchLeftHandSide,
			final long timestampForLastSynchRightHandSide)
			throws TransferEngineException {

		FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) diffNode
				.getUserObject();

		log.debug("processing diff node:{}", fileTreeDiffEntry);

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
			final String irodsRootAbsolutePath) throws TransferEngineException {

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

		log.info("doing a get from irods under target at:{}",
				targetRelativePath);

		log.warn("get operations not yet implemented!");

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
			final String irodsRootAbsolutePath) throws TransferEngineException {
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
		sb.append("/");
		sb.append(targetRelativePath);

		log.info("processing a put to irods under target at:{}",
				targetRelativePath);

		try {
			dataTransferOperations.putOperation(
					entry.getFormattedAbsolutePath(), sb.toString(),
					irodsAccount.getDefaultStorageResource(), callbackListener,
					null);
		} catch (JargonException e) {
			log.error("error in put operation as part of synch", e);
			throw new TransferEngineException(e);
		}

		log.info("put done");

	}

	private void checkContracts() throws TransferEngineException {

		if (irodsAccessObjectFactory == null) {
			throw new TransferEngineException(
					"irodsAccessObjectFactory dependency has not been set");
		}

		if (transferManager == null) {
			throw new TransferEngineException(
					"transferManager dependency has not been set");
		}

		if (irodsAccount == null) {
			throw new TransferEngineException("irodsAccount has not been set");
		}
	}

}
