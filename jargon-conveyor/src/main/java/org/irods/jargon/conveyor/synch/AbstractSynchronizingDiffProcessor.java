package org.irods.jargon.conveyor.synch;

import java.util.Enumeration;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatus.TransferState;
import org.irods.jargon.core.transfer.TransferStatus.TransferType;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.datautils.tree.FileTreeDiffEntry;
import org.irods.jargon.datautils.tree.FileTreeDiffEntry.DiffType;
import org.irods.jargon.datautils.tree.FileTreeModel;
import org.irods.jargon.datautils.tree.FileTreeNode;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclass for a mechanism to take a difference tree model and
 * synchronize between iRODS and the local file system. This can be implemented
 * for different synch strategies.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public abstract class AbstractSynchronizingDiffProcessor {

	private static final Logger log = LoggerFactory
			.getLogger(AbstractSynchronizingDiffProcessor.class);

	private final ConveyorService conveyorService;
	private final TransferControlBlock transferControlBlock;

	private IRODSAccount irodsAccount = null;
	private DataTransferOperations dataTransferOperations = null;

	/**
	 * Create an instance with an initialized reference to the conveyor service
	 * 
	 * @param conveyorService
	 *            {@link ConveyorService} reference
	 */
	public AbstractSynchronizingDiffProcessor(ConveyorService conveyorService,
			final TransferControlBlock transferControlBlock) {
		super();

		if (conveyorService == null) {
			throw new IllegalArgumentException("null conveyorService");
		}

		if (transferControlBlock == null) {
			throw new IllegalArgumentException("null transferControlBlock");
		}

		this.conveyorService = conveyorService;
		this.transferControlBlock = transferControlBlock;

	}

	/**
	 * Given a diff embodied in a <code>FileTreeModel</code>, do necessary
	 * operations to synchronize between local and iRODS.
	 * 
	 * @param TransferAttempt
	 *            {@link TransferAttempt} of type <code>SYNCH</code>, with a
	 *            parent {@link Synchronization} that describes the synch
	 *            relationship
	 * @param diffModel
	 *            {@link FileTreeModel} that embodies the diff between local and
	 *            iRODS
	 * @param transferStatusCallbackListener
	 *            {@link TransferStatusCallbackListener} that will recieve
	 *            callbacks on the status of the diff processing
	 * @throws ConveyorExecutionException
	 */
	public void execute(TransferAttempt transferAttempt,
			FileTreeModel diffModel,
			TransferStatusCallbackListener transferStatusCallbackListener)
			throws ConveyorExecutionException {

		log.info("processDiff()");

		assert transferAttempt != null;
		assert diffModel != null;
		assert transferStatusCallbackListener != null;

		try {
			this.signalStartupCallback(transferAttempt.getTransfer()
					.getSynchronization(), transferStatusCallbackListener);

			synchronized (this) {
				if (irodsAccount == null) {
					irodsAccount = this
							.getConveyorService()
							.getGridAccountService()
							.irodsAccountForGridAccount(
									transferAttempt.getTransfer()
											.getSynchronization()
											.getGridAccount());
				}

				if (dataTransferOperations == null) {
					this.dataTransferOperations = this.getConveyorService()
							.getIrodsAccessObjectFactory()
							.getDataTransferOperations(irodsAccount);
				}
			}

			processDiff((FileTreeNode) diffModel.getRoot(), transferAttempt
					.getTransfer().getSynchronization()
					.getLocalSynchDirectory(), transferAttempt.getTransfer()
					.getSynchronization().getIrodsSynchDirectory());

		} catch (JargonException e) {
			throw new ConveyorExecutionException(e);
		}

	}

/**
	 * Send a message that we are starting the diff resolve step
	 * 
	 * @param synchronization {@link Synchronization
	 * @param transferStatusCallbackListener
	 * @throws JargonException
	 */
	protected void signalStartupCallback(final Synchronization synchronization,
			final TransferStatusCallbackListener transferStatusCallbackListener)
			throws JargonException {
		// make an overall status callback that a synch is initiated

		TransferStatus overallSynchStartStatus = TransferStatus.instance(
				TransferType.SYNCH, synchronization.getLocalSynchDirectory(),
				synchronization.getIrodsSynchDirectory(), synchronization
						.getDefaultStorageResource(), 0L, 0L, 0, 0, 0,
				TransferState.SYNCH_DIFF_RESOLVE_STEP, synchronization
						.getGridAccount().getHost(), synchronization
						.getGridAccount().getZone());
		transferStatusCallbackListener
				.overallStatusCallback(overallSynchStartStatus);

	}

	/**
	 * @return the conveyorService
	 */
	public ConveyorService getConveyorService() {
		return conveyorService;
	}

	/**
	 * Recursive method to process a difference node and its children.
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
			final String irodsRootAbsolutePath)
			throws ConveyorExecutionException {

		if (transferControlBlock.isCancelled()
				|| transferControlBlock.isPaused()) {
			log.info("cancelling...");
			return;
		}

		final FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) diffNode
				.getUserObject();

		log.debug("processing diff node:{}", fileTreeDiffEntry);

		processDiffNode(diffNode, localRootAbsolutePath, irodsRootAbsolutePath,
				fileTreeDiffEntry);

	}

	/**
	 * @param diffNode
	 * @param localRootAbsolutePath
	 * @param irodsRootAbsolutePath
	 * @param fileTreeDiffEntry
	 * @throws TransferEngineException
	 */
	private void processDiffNode(final FileTreeNode diffNode,
			final String localRootAbsolutePath,
			final String irodsRootAbsolutePath,
			final FileTreeDiffEntry fileTreeDiffEntry)
			throws ConveyorExecutionException {
		if (fileTreeDiffEntry.getDiffType() == DiffType.DIRECTORY_NO_DIFF) {
			evaluateDirectoryNode(diffNode, localRootAbsolutePath,
					irodsRootAbsolutePath, fileTreeDiffEntry);
		} else if (fileTreeDiffEntry.getDiffType() == DiffType.LEFT_HAND_PLUS) {
			log.debug("local file is new directory {}", fileTreeDiffEntry
					.getCollectionAndDataObjectListingEntry()
					.getFormattedAbsolutePath());
			scheduleLocalToIrods(diffNode, localRootAbsolutePath,
					irodsRootAbsolutePath);
		} else if (fileTreeDiffEntry.getDiffType() == DiffType.FILE_OUT_OF_SYNCH) {
			log.debug("local file out of synch with irods {}",
					fileTreeDiffEntry.getCollectionAndDataObjectListingEntry()
							.getFormattedAbsolutePath());
			scheduleLocalToIrodsWithIrodsBackup(diffNode,
					localRootAbsolutePath, irodsRootAbsolutePath);
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
	 * Move the local file to iRODS with iRODS backed up
	 * 
	 * @param diffNode
	 * @param localRootAbsolutePath
	 * @param irodsRootAbsolutePath
	 * @throws TransferEngineException
	 */
	private void scheduleLocalToIrodsWithIrodsBackup(
			final FileTreeNode diffNode, final String localRootAbsolutePath,
			final String irodsRootAbsolutePath)
			throws ConveyorExecutionException {
		log.info("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>\n\n");
		log.info("scheduleLocalToIrodsWithIrodsBackup for diffNode:{}",
				diffNode);

		FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) diffNode
				.getUserObject();
		CollectionAndDataObjectListingEntry entry = fileTreeDiffEntry
				.getCollectionAndDataObjectListingEntry();

		try {

			String targetRelativePath = entry.getFormattedAbsolutePath()
					.substring(localRootAbsolutePath.length());

			// became
			// /testFileTreeDiffLocalLocalFileLengthSameLocalChecksumUpdated.txt

			IRODSFile targetFile = this
					.getConveyorService()
					.getIrodsAccessObjectFactory()
					.getIRODSFileFactory(getIrodsAccount())
					.instanceIRODSFile(irodsRootAbsolutePath,
							targetRelativePath);

			if (targetFile.getName().charAt(0) == '.') {
				log.debug("no backups of hidden files");
				return;
			}

			// became
			// irods://test1@localhost:1247/test1/home/test1/jargon-scratch/InPlaceSynchronizingDiffProcessorImplTest/testFileTreeDiffLocalLocalFileLengthSameLocalChecksumUpdated/testFileTreeDiffLocalLocalFileLengthSameLocalChecksumUpdated.txt

			log.debug("target file name in iRODS:{}",
					targetFile.getAbsolutePath());

			/*
			 * For backup, take the path under the users home directory, remove
			 * the zone/home/username part, and stick it under
			 * zone/home/username/backup dir name/...
			 */

			String pathBelowUserHome = targetFile.getParent().substring(
					usersHomeRootLength);

			StringBuilder irodsBackupAbsPath = new StringBuilder();
			irodsBackupAbsPath.append(usersHomeRoot);
			irodsBackupAbsPath.append(SLASH);
			irodsBackupAbsPath.append(BACKUP_PREFIX);
			irodsBackupAbsPath.append(pathBelowUserHome);

			// this became
			// /test1/home/test1/synch-backup/testFileTreeDiffLocalLocalFileLengthSameLocalChecksumUpdated.txt

			String backupFileName = LocalFileUtils
					.getFileNameWithTimeStampInterposed(targetFile.getName());
			IRODSFile backupFile = getIRODSFileFactory().instanceIRODSFile(
					irodsBackupAbsPath.toString(), backupFileName);
			backupFile.getParentFile().mkdirs();
			log.debug("backup file name:{}", backupFile.getAbsolutePath());

			targetFile.renameTo(backupFile);
			log.debug("rename done");

			transferControlBlock.resetTransferData();
			dataTransferOperations.putOperation(
					entry.getFormattedAbsolutePath(),
					targetFile.getAbsolutePath(),
					irodsAccount.getDefaultStorageResource(), this,
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
									irodsRootAbsolutePath,
									irodsAccount.getDefaultStorageResource(),
									0L, 0L, 0, 0, e, irodsAccount.getHost(),
									irodsAccount.getZone());
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
			final FileTreeDiffEntry fileTreeDiffEntry)
			throws ConveyorExecutionException {
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
			processDiff(childNode, localRootAbsolutePath, irodsRootAbsolutePath);
		}
	}

	private void scheduleIrodsToLocal(final FileTreeNode diffNode,
			final String localRootAbsolutePath,
			final String irodsRootAbsolutePath)
			throws ConveyorExecutionException {

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
	 * iRODS
	 * 
	 * @param diffNode
	 * @param localRootAbsolutePath
	 * @param irodsRootAbsolutePath
	 */
	private void scheduleLocalToIrods(final FileTreeNode diffNode,
			final String localRootAbsolutePath,
			final String irodsRootAbsolutePath)
			throws ConveyorExecutionException {
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
		StringBuilder sb = new StringBuilder(irodsRootAbsolutePath);
		if (entry.getObjectType() == ObjectType.COLLECTION) {
			targetRelativePath = entry.getParentPath().substring(
					localRootAbsolutePath.length());
			log.info("entry is a collection, setting targetRelativePath to:{}",
					targetRelativePath);
			sb.append("/");
		} else {

			if (entry.getPathOrName().charAt(0) == '.') {
				log.debug("no backups of hidden files");
				return;
			}

			targetRelativePath = entry.getFormattedAbsolutePath().substring(
					localRootAbsolutePath.length());
			log.info("entry is a file, setting targetRelativePath to:{}",
					targetRelativePath);
		}

		sb.append(targetRelativePath);

		String putPath = sb.toString();

		log.info("processing a put to irods under target at computed path:{}",
				putPath);

		try {
			transferControlBlock.resetTransferData();
			dataTransferOperations.putOperation(
					entry.getFormattedAbsolutePath(), putPath,
					irodsAccount.getDefaultStorageResource(), this,
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
									0L, 0L, 0, 0, e, irodsAccount.getHost(),
									irodsAccount.getZone());
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
	 * @return the transferControlBlock
	 */
	protected TransferControlBlock getTransferControlBlock() {
		return transferControlBlock;
	}

	/**
	 * @return the irodsAccount
	 */
	protected synchronized IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	/**
	 * @param irodsAccount
	 *            the irodsAccount to set
	 */
	protected synchronized void setIrodsAccount(IRODSAccount irodsAccount) {
		this.irodsAccount = irodsAccount;
	}

	/**
	 * @return the dataTransferOperations
	 */
	protected synchronized DataTransferOperations getDataTransferOperations() {
		return dataTransferOperations;
	}

	/**
	 * @param dataTransferOperations
	 *            the dataTransferOperations to set
	 */
	protected synchronized void setDataTransferOperations(
			DataTransferOperations dataTransferOperations) {
		this.dataTransferOperations = dataTransferOperations;
	}

}