/**
 * 
 */
package org.irods.jargon.conveyor.synch;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.datautils.tree.FileTreeDiffEntry;
import org.irods.jargon.datautils.tree.FileTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process a one-way local to iRODS diff
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class LocalToIRODSDiffProcessor extends
		AbstractSynchronizingDiffProcessor {

	private static final Logger log = LoggerFactory
			.getLogger(LocalToIRODSDiffProcessor.class);

	public LocalToIRODSDiffProcessor(ConveyorService conveyorService,
			TransferControlBlock transferControlBlock) {
		super(conveyorService, transferControlBlock);
	}

	/**
	 * the node is a local file/collection that needs to be scheduled to move to
	 * iRODS
	 * 
	 * @param diffNode
	 * @param localRootAbsolutePath
	 * @param irodsRootAbsolutePath
	 */
	protected void scheduleLocalToIrods(final FileTreeNode diffNode,
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
		sb.append("/");

		sb.append(targetRelativePath);

		String putPath = sb.toString();

		log.info("processing a put to irods under target at computed path:{}",
				putPath);

		try {
			this.getTransferControlBlock().resetTransferData();
			this.getDataTransferOperations().putOperation(
					entry.getFormattedAbsolutePath(), putPath,
					this.getIrodsAccount().getDefaultStorageResource(), this,
					this.getTransferControlBlock());
		} catch (Exception e) {

			log.error("error in put operation as part of synch", e);
			this.getTransferControlBlock().reportErrorInTransfer();

			if (this.getTransferStatusCallbackListener() == null) {
				throw new ConveyorExecutionException(
						"error occurred in synch, no status callback listener was specified",
						e);

			} else {
				try {
					TransferStatus transferStatus = TransferStatus
							.instanceForExceptionForSynch(
									TransferStatus.TransferType.SYNCH, entry
											.getFormattedAbsolutePath(), sb
											.toString(), this.getIrodsAccount()
											.getDefaultStorageResource(), 0L,
									0L, 0, 0, 0, e, this.getIrodsAccount()
											.getHost(), this.getIrodsAccount()
											.getZone());
					this.getTransferStatusCallbackListener().statusCallback(
							transferStatus);
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
	 * Move the local file to iRODS with iRODS backed up
	 * 
	 * @param diffNode
	 * @param localRootAbsolutePath
	 * @param irodsRootAbsolutePath
	 * @throws TransferEngineException
	 */
	protected void scheduleMatchedFileOutOfSynch(final FileTreeNode diffNode,
			final String localRootAbsolutePath,
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

			IRODSFile targetFile = this.getIrodsFileFactory()
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

			IRODSFile userHome = this.getIrodsFileFactory()
					.instanceIRODSFileUserHomeDir(
							getIrodsAccount().getUserName());

			/*
			 * For backup, take the path under the users home directory, remove
			 * the zone/home/username part, and stick it under
			 * zone/home/username/backup dir name/...
			 */

			String pathBelowUserHome = targetFile.getParent().substring(
					userHome.getAbsolutePath().length());

			StringBuilder irodsBackupAbsPath = new StringBuilder();
			irodsBackupAbsPath.append(userHome.getAbsolutePath());
			irodsBackupAbsPath.append('/');
			irodsBackupAbsPath.append(BACKUP_PREFIX);
			irodsBackupAbsPath.append(pathBelowUserHome);

			// this became
			// /test1/home/test1/synch-backup/testFileTreeDiffLocalLocalFileLengthSameLocalChecksumUpdated.txt

			String backupFileName = LocalFileUtils
					.getFileNameWithTimeStampInterposed(targetFile.getName());
			IRODSFile backupFile = this
					.getConveyorService()
					.getIrodsAccessObjectFactory()
					.getIRODSFileFactory(getIrodsAccount())
					.instanceIRODSFile(irodsBackupAbsPath.toString(),
							backupFileName);
			backupFile.getParentFile().mkdirs();
			log.debug("backup file name:{}", backupFile.getAbsolutePath());

			targetFile.renameTo(backupFile);
			log.debug("rename done");

			this.getTransferControlBlock().resetTransferData();
			this.getDataTransferOperations().putOperation(
					entry.getFormattedAbsolutePath(),
					targetFile.getAbsolutePath(),
					this.getIrodsAccount().getDefaultStorageResource(), this,
					this.getTransferControlBlock());

		} catch (Exception e) {

			log.error("error in put operation as part of synch", e);
			this.getTransferControlBlock().reportErrorInTransfer();

			if (this.getTransferStatusCallbackListener() == null) {
				throw new ConveyorExecutionException(
						"error occurred in synch, no status callback listener was specified",
						e);

			} else {
				try {
					TransferStatus transferStatus = TransferStatus
							.instanceForExceptionForSynch(
									TransferStatus.TransferType.SYNCH, entry
											.getFormattedAbsolutePath(),
									irodsRootAbsolutePath, this
											.getIrodsAccount()
											.getDefaultStorageResource(), 0L,
									0L, 0, 0, 0, e, this.getIrodsAccount()
											.getHost(), this.getIrodsAccount()
											.getZone());
					this.getTransferStatusCallbackListener().statusCallback(
							transferStatus);
				} catch (JargonException e1) {
					log.error("error building transfer status", e1);
					throw new JargonRuntimeException(
							"exception building transfer status", e1);
				}
			}

		}

		log.info("put done");

	}

}
