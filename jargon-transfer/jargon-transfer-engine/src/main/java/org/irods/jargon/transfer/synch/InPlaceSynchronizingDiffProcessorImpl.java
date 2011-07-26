package org.irods.jargon.transfer.synch;

import java.util.Enumeration;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
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
	private transient DataTransferOperations dataTransferOperations;
	private TransferStatusCallbackListener callbackListener;
	private IRODSAccount irodsAccount;
	private TransferControlBlock transferControlBlock;

	public static final String SLASH = "/";

	private static final Logger log = LoggerFactory
			.getLogger(InPlaceSynchronizingDiffProcessorImpl.class);

	
	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	/**
	 * Required dependency
	 * @param irodsAccount {@link IRODSAccount} for the given synch
	 */
	public void setIrodsAccount(final IRODSAccount irodsAccount) {
		this.irodsAccount = irodsAccount;
	}

	
	public TransferStatusCallbackListener getCallbackListener() {
		return callbackListener;
	}

	/**
	 * Optional dependency
	 * @param callbackListener {@link TransferStatusCallbackListener} implementation that can receive progress callbacks for transfers
	 */
	public void setCallbackListener(
			final TransferStatusCallbackListener callbackListener) {
		this.callbackListener = callbackListener;
	}

	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	/**
	 * Required dependency
	 * @param irodsAccessObjectFactory {@link IRODSAccessObjectFactory} that can create various iRODS accessing service objects
	 */
	public void setIrodsAccessObjectFactory(
			final IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	public TransferManager getTransferManager() {
		return transferManager;
	}

	/**
	 * Required dependency
	 * @param transferManager {@link TransferManager} implementation that manages the transfer queue and operations
	 */
	public void setTransferManager(final TransferManager transferManager) {
		this.transferManager = transferManager;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.synch.SynchronizingDiffProcessor#processDiff
	 * (org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer,
	 * org.irods.jargon.datautils.tree.FileTreeModel)
	 */
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

		final Synchronization synchronization = localIRODSTransfer
				.getSynchronization();

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

		if (dataTransferOperations == null) {
			try {
				dataTransferOperations = irodsAccessObjectFactory
						.getDataTransferOperations(irodsAccount);
				processDiffWithValidData(diffModel,
						calculatedLocalRoot, calculatedIrodsRoot);
			} catch (Exception e) {
				log.error("exception creating dataTransferOperations", e);

				if (callbackListener == null) {
					throw new TransferEngineException(
							"error occurred in synch, no status callback listener was specified",
							e);
			
				} else {
				
					
					try {
						final TransferStatus transferStatus = TransferStatus
								.instanceForExceptionForSynch(
										TransferStatus.TransferType.SYNCH,
										localIRODSTransfer
												.getLocalAbsolutePath(),
										localIRODSTransfer
												.getIrodsAbsolutePath(),
										localIRODSTransfer
												.getTransferResource(), 0L, 0L,
										0, 0, e);
						callbackListener.statusCallback(transferStatus);
					} catch (JargonException e1) {
						log.error("error building transfer status", e1);
						throw new JargonRuntimeException(
								"exception building transfer status", e1);
					}
				}
			}
		}

	}

	/**
	 * 
	 * Do the actual diff processing, since contracts and dependencies have all
	 * been validated. Note that the calculated paths below should have trailing
	 * slash characters trimmed so that relative paths can be computed
	 * correctly.
	 * 
	 * @param localIRODSTransfer
	 *            {@link LocalIRODSTransfer} with data for synch
	 * @param diffModel
	 *            {@link FileTreeModel} that contains a diff tree to process
	 * @param calculatedLocalRoot
	 *            <code>String</code> with the calculated local root path, this
	 *            is used to calculate relative paths for the descending diff
	 * @param calculatedIrodsRoot
	 *            <code>String</code> with the calculated iRODS root path, this
	 *            is used to calculate relative paths for the descending diff
	 * 
	 */
	private void processDiffWithValidData(
			final FileTreeModel diffModel, final String calculatedLocalRoot,
			final String calculatedIrodsRoot) throws TransferEngineException {

		log.info("processDiffWithValidData");
		log.info("difModel:{}", diffModel);

		processDiff((FileTreeNode) diffModel.getRoot(), calculatedLocalRoot,
				calculatedIrodsRoot, 0, 0);

	}

	/**
	 * Recursive method to process a difference node and its children
	 * 
	 * @param diffNode
	 * @param localRootAbsolutePath
	 * @param irodsRootAbsolutePath
	 * @param timestampforLastSynchLeftHandSide
	 * @param timestampForLastSynchRightHandSide
	 * @throws TransferEngineException
	 */
	private void processDiff(final FileTreeNode diffNode,
			final String localRootAbsolutePath,
			final String irodsRootAbsolutePath,
			final long timestampforLastSynchLeftHandSide,
			final long timestampForLastSynchRightHandSide)
			throws TransferEngineException {

		if (transferControlBlock.isCancelled()
				|| transferControlBlock.isPaused()) {
			log.info("cancelling...");
			return;
		}

		final FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) diffNode
				.getUserObject();

		log.debug("processing diff node:{}", fileTreeDiffEntry);

		processDiffNode(diffNode, localRootAbsolutePath, irodsRootAbsolutePath,
				timestampforLastSynchLeftHandSide,
				timestampForLastSynchRightHandSide, fileTreeDiffEntry);

	}

	/**
	 * @param diffNode
	 * @param localRootAbsolutePath
	 * @param irodsRootAbsolutePath
	 * @param timestampforLastSynchLeftHandSide
	 * @param timestampForLastSynchRightHandSide
	 * @param fileTreeDiffEntry
	 * @throws TransferEngineException
	 */
	private void processDiffNode(final FileTreeNode diffNode,
			final String localRootAbsolutePath,
			final String irodsRootAbsolutePath,
			final long timestampforLastSynchLeftHandSide,
			final long timestampForLastSynchRightHandSide,
			final FileTreeDiffEntry fileTreeDiffEntry) throws TransferEngineException {
		if (fileTreeDiffEntry.getDiffType() == DiffType.DIRECTORY_NO_DIFF) {
			evaluateDirectoryNode(diffNode, localRootAbsolutePath,
					irodsRootAbsolutePath, timestampforLastSynchLeftHandSide,
					timestampForLastSynchRightHandSide, fileTreeDiffEntry);
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

	/**
	 * @param diffNode
	 * @param localRootAbsolutePath
	 * @param irodsRootAbsolutePath
	 * @param timestampforLastSynchLeftHandSide
	 * @param timestampForLastSynchRightHandSide
	 * @param fileTreeDiffEntry
	 * @throws TransferEngineException
	 */
	private void evaluateDirectoryNode(final FileTreeNode diffNode,
			final String localRootAbsolutePath,
			final String irodsRootAbsolutePath,
			final long timestampforLastSynchLeftHandSide,
			final long timestampForLastSynchRightHandSide,
			final FileTreeDiffEntry fileTreeDiffEntry) throws TransferEngineException {
		log.debug("evaluating directory: {}", fileTreeDiffEntry
				.getCollectionAndDataObjectListingEntry()
				.getFormattedAbsolutePath());
		FileTreeNode childNode;
		@SuppressWarnings("rawtypes")
		final Enumeration children = diffNode.children();
		while (children.hasMoreElements()) {

			if (transferControlBlock.isCancelled()
					|| transferControlBlock.isPaused()) {
				log.info("cancelling...");
				break;
			}

			childNode = (FileTreeNode) children.nextElement();
			processDiff(childNode, localRootAbsolutePath,
					irodsRootAbsolutePath,
					timestampforLastSynchLeftHandSide,
					timestampForLastSynchRightHandSide);
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
			transferControlBlock.resetTransferData();
			dataTransferOperations.putOperation(
					entry.getFormattedAbsolutePath(), sb.toString(),
					irodsAccount.getDefaultStorageResource(), callbackListener,
					transferControlBlock);
		} catch (Exception e) {

			log.error("error in put operation as part of synch", e);
			transferControlBlock.reportErrorInTransfer();

			if (callbackListener == null) {
				throw new TransferEngineException(
						"error occurred in synch, no status callback listener was specified",
						e);
				
			} else {
				try {
					TransferStatus transferStatus = TransferStatus
							.instanceForExceptionForSynch(
									TransferStatus.TransferType.SYNCH,
									entry.getFormattedAbsolutePath(),
									sb.toString(),
									irodsAccount.getDefaultStorageResource(),
									0L, 0L, 0, 0, e);
					callbackListener.statusCallback(transferStatus);
				} catch (JargonException e1) {
					log.error("error building transfer status", e1);
					throw new JargonRuntimeException(
							"exception building transfer status", e1);
				}
			}

		}

		log.info("put done");

	}

	/**
	 * Ensure that required dependencies are present before doing operations.
	 * 
	 * @throws TransferEngineException
	 */
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

		if (transferControlBlock == null) {
			throw new TransferEngineException("null transferControlBlock");
		}
	}

	public TransferControlBlock getTransferControlBlock() {
		return transferControlBlock;
	}

	public void setTransferControlBlock(
			final TransferControlBlock transferControlBlock) {
		this.transferControlBlock = transferControlBlock;
	}

}
