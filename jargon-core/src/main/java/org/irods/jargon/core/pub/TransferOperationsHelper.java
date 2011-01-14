package org.irods.jargon.core.pub;

import java.io.File;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileImpl;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatus.TransferState;
import org.irods.jargon.core.transfer.TransferStatus.TransferType;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 *         Functions to support transfer operations. These are used internally.
 *         See {@link org.irods.jargon.core.pub.DataTransferOperations} for
 *         public methods.
 * 
 */
final class TransferOperationsHelper {

	static Logger log = LoggerFactory.getLogger(TransferOperationsHelper.class);

	private final IRODSSession irodsSession;
	private final IRODSAccount irodsAccount;

	/**
	 * Initializer creates an instance of htis class.
	 * 
	 * @param irodsSession
	 *            <code>IRODSSession</code> that can connect to iRODS
	 * @param irodsAccount
	 *            <code>IRODSAccount</code> for this connection.
	 * @return
	 * @throws JargonException
	 */
	protected final static TransferOperationsHelper instance(
			final IRODSSession irodsSession, final IRODSAccount irodsAccount)
			throws JargonException {
		return new TransferOperationsHelper(irodsSession, irodsAccount);
	}

	private TransferOperationsHelper(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		if (irodsSession == null || irodsAccount == null) {
			throw new JargonException("null irodsSession or irodsAccount");
		}

		this.irodsSession = irodsSession;
		this.irodsAccount = irodsAccount;

	}

	/**
	 * Recursively get a file from iRODS. This utility method is used
	 * internally, and can process call-backs as well as filtering and
	 * cancellation.
	 * 
	 * @param irodsSourceFile
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} that points to
	 *            the file or collection to retrieve.
	 * @param targetLocalFile
	 *            <code>File</code> that will hold the retrieved data.
	 * @param transferStatusCallbackListener
	 *            {@link org.irods.jargon.core.transfer.TransferStatusCallbackListener}
	 *            implementation that will receive callbacks indicating the
	 *            real-time status of the transfer.
	 * @param transferControlBlock
	 *            an optional
	 *            {@link org.irods.jargon.core.transfer.TransferControlBlock}
	 *            that provides a common object to communicate between the
	 *            object requesting the transfer, and the method performing the
	 *            transfer. This control block may contain a filter that can be
	 *            used to control restarts, and provides a way for the
	 *            requesting process to send a cancellation. This may be set to
	 *            null if not required.
	 * @throws JargonException
	 */
	protected void recursivelyGet(
			final IRODSFile irodsSourceFile,
			final File targetLocalFile,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock)
			throws JargonException {

		log.info("recursively getting source file: {}",
				irodsSourceFile.getAbsolutePath());
		log.info("    into iRODS file: {}", targetLocalFile.getAbsolutePath());

		for (File fileInSourceCollection : irodsSourceFile.listFiles()) {
			// for each file in the given source collection, put the data file,
			// or create the new irodsCollection and step into it

			// check for a cancel or pause at the top of the loop
			if (transferControlBlock != null
					&& (transferControlBlock.isCancelled() || transferControlBlock
							.isPaused())) {
				log.info("transfer cancelled or paused");
				if (transferStatusCallbackListener != null) {
					TransferState interruptStatus;
					if (transferControlBlock
							.shouldTransferBeAbandonedDueToNumberOfErrors()) {
						interruptStatus = TransferState.FAILURE;
					} else if (transferControlBlock.isCancelled()) {
						interruptStatus = TransferState.CANCELLED;
					} else {
						interruptStatus = TransferState.PAUSED;
					}

					TransferStatus status = TransferStatus.instance(
							TransferType.GET,
							fileInSourceCollection.getAbsolutePath(),
							targetLocalFile.getAbsolutePath(), "",
							fileInSourceCollection.length(),
							fileInSourceCollection.length(), 0, 0,
							interruptStatus);
					transferStatusCallbackListener.statusCallback(status);
					return;
				}
			}

			if (fileInSourceCollection.isDirectory()) {

				// make a dir in the target collection
				StringBuilder sb = new StringBuilder();
				sb.append(targetLocalFile.getAbsolutePath());
				sb.append('/');
				sb.append(fileInSourceCollection.getName());
				log.info(
						"recursively creating parent directory in local file system at: {}",
						sb.toString());

				File newSubCollection = new File(sb.toString());
				boolean mkDirResult = newSubCollection.mkdirs();

				if (mkDirResult == false) {
					log.info(
							"could not make directory, this is not necessarily an error: {}",
							newSubCollection.getAbsolutePath());
				}

				recursivelyGet((IRODSFileImpl) fileInSourceCollection,
						newSubCollection, transferStatusCallbackListener,
						transferControlBlock);

			} else {
				this.processGetOfSingleFile(
						(IRODSFileImpl) fileInSourceCollection,
						targetLocalFile, transferStatusCallbackListener,
						transferControlBlock);
			}
		}
	}

	/**
	 * In a transfer operation, process the given iRODS file as a data object to
	 * be retrieved.
	 * 
	 * @param irodsSourceFile
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} that is the
	 *            source of the get.
	 * @param targetLocalFile
	 *            <code>File</code> on the local file system to which the files
	 *            will be transferrred.
	 * @param transferStatusCallbackListener
	 *            {@link org.irods.jargon.core.transfer.TransferStatusCallbackListener}
	 *            implementation that will receive callbacks of success/failure
	 *            of each individual file transfer. This may be set to
	 *            <code>null</code>, in which case, exceptions that are thrown
	 *            will be rethrown by this method to the caller.
	 * @param transferControlBlock
	 *            {@link org.irods.jargon.core.transfer.TransferControlBlock}
	 *            implementation that is the communications mechanism between
	 *            the initiator of the transfer and the transfer process. This
	 *            may be set to <code>null</code> if those facilities are not
	 *            needed.
	 * @throws JargonException
	 */
	protected void processGetOfSingleFile(
			final IRODSFile irodsSourceFile,
			final File targetLocalFile,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock)
			throws JargonException {

		log.info("get of single file...filtered?");

		if (!transferControlBlock.filter(irodsSourceFile.getAbsolutePath())) {
			log.info("file is filtered and discarded: {}",
					irodsSourceFile.getAbsolutePath());
			return;
		}

		log.info("filter passed, process...");
		try {
			DataObjectAO dataObjectAO = new DataObjectAOImpl(irodsSession,
					irodsAccount);
			dataObjectAO.getDataObjectFromIrods(irodsSourceFile,
					targetLocalFile);

			int totalFiles = 0;
			int totalFilesSoFar = 0;

			if (transferControlBlock != null) {
				transferControlBlock.incrementFilesTransferredSoFar();
				totalFiles = transferControlBlock.getTotalFilesToTransfer();
				totalFilesSoFar = transferControlBlock
						.getTotalFilesTransferredSoFar();
			}

			if (transferStatusCallbackListener != null) {
				log.warn("success will be passed back to existing callback listener");

				long sourceFileLength = irodsSourceFile.length();
				TransferStatus status = TransferStatus.instance(
						TransferType.GET, irodsSourceFile.getAbsolutePath(),
						targetLocalFile.getAbsolutePath(), "",
						sourceFileLength, sourceFileLength, totalFilesSoFar,
						totalFiles, TransferState.SUCCESS);

				transferStatusCallbackListener.statusCallback(status);
			}

		} catch (JargonException je) {
			// may rethrow or send back to the callback listener
			log.error("exception in transfer", je);

			int totalFiles = 0;
			int totalFilesSoFar = 0;

			if (transferControlBlock != null) {
				transferControlBlock.reportErrorInTransfer();
				totalFiles = transferControlBlock.getTotalFilesToTransfer();
				totalFilesSoFar = transferControlBlock
						.getTotalFilesTransferredSoFar();
			}

			if (transferStatusCallbackListener != null) {
				log.warn("exception will be passed back to existing callback listener");

				TransferStatus status = TransferStatus.instanceForException(
						TransferType.PUT, irodsSourceFile.getAbsolutePath(),
						targetLocalFile.getAbsolutePath(), "",
						targetLocalFile.length(), targetLocalFile.length(),
						totalFilesSoFar, totalFiles, je);

				transferStatusCallbackListener.statusCallback(status);

			} else {
				log.warn("exception will be re-thrown, as there is no status callback listener");
				throw je;

			}
		}
	}

	/**
	 * Method to recursively put a collection. This method can monitor for a
	 * cancellation, and can also provide callbacks to a process.
	 * 
	 * @param irodsFileAbsolutePath
	 *            <code>String</code> with the absolute path to an iRODS file
	 *            that should be replicated.
	 * @param targetResource
	 *            <code>String</code> with the resource to which the file should
	 *            be replicated.
	 * @param transferStatusCallbackListener
	 *            an optional
	 *            {@link org.irods.jargon.core.transfer.TransferStatusCallbackListener}
	 *            that can receive status callbacks. This may be set to null if
	 *            this functionality is not required.
	 * @param transferControlBlock
	 *            an optional
	 *            {@link org.irods.jargon.core.transfer.TransferControlBlock}
	 *            that provides a common object to communicate between the
	 *            object requesting the transfer, and the method performing the
	 *            transfer. This control block may contain a filter that can be
	 *            used to control restarts, and provides a way for the
	 *            requesting process to send a cancellation. This may be set to
	 *            null if not required.
	 * @throws JargonException
	 */
	protected void recursivelyPut(
			final File sourceFile,
			final IRODSFile targetIrodsCollection,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock)
			throws JargonException {

		// sourceFile is a directory, contracts were checked in putOperation()
		final CollectionAO collectionAO = new CollectionAOImpl(irodsSession,
				irodsAccount);
		final DataObjectAO dataObjectAO = new DataObjectAOImpl(irodsSession,
				irodsAccount);

		if (!sourceFile.isDirectory()) {
			throw new JargonException(
					"source file is not a directory, cannot recursively put");
		}

		log.info("recursively putting source file: {}",
				sourceFile.getAbsolutePath());
		log.info("    into iRODS file: {}",
				targetIrodsCollection.getAbsolutePath());
		log.info("     to resource:{}", targetIrodsCollection.getResource());

		for (File fileInSourceCollection : sourceFile.listFiles()) {

			// check for a cancel or pause at the top of the loop
			if (transferControlBlock != null
					&& (transferControlBlock.isCancelled() || transferControlBlock
							.isPaused())) {
				noifyPauseOrCancelCallbackForPut(targetIrodsCollection,
						transferStatusCallbackListener, transferControlBlock,
						fileInSourceCollection);
				break;
			}

			if (fileInSourceCollection.isDirectory()) {
				recursivelyPutACollection(targetIrodsCollection,
						transferStatusCallbackListener, transferControlBlock,
						collectionAO, fileInSourceCollection);

				// a pause will need to bubble back up

				if (transferControlBlock != null
						&& (transferControlBlock.isCancelled() || transferControlBlock
								.isPaused())) {
					log.info("returning, is paused or cancelled");
					break;
				}
			} else {
				if (transferControlBlock != null) {

					// bypass if pause encountered
					if (transferControlBlock.isCancelled()
							|| transferControlBlock.isPaused()) {
						log.info("returning, is paused or cancelled");
						break;
					}

					// if I am restarting, see if I need to transfer this file

					if (!transferControlBlock.filter(fileInSourceCollection
							.getAbsolutePath())) {
						log.debug("file filtered and not transferred");
						continue;
					}
				}

				putASingleFile(fileInSourceCollection, targetIrodsCollection,
						transferStatusCallbackListener, transferControlBlock,
						dataObjectAO);
			}
		}
	}

	/**
	 * @param targetIrodsCollection
	 * @param transferStatusCallbackListener
	 * @param transferControlBlock
	 * @param fileInSourceCollection
	 * @throws JargonException
	 */
	private void noifyPauseOrCancelCallbackForPut(
			final IRODSFile targetIrodsCollection,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock,
			final File fileInSourceCollection) throws JargonException {
		log.info("transfer cancelled or paused");
		if (transferStatusCallbackListener != null) {
			TransferState interruptStatus;

			if (transferControlBlock
					.shouldTransferBeAbandonedDueToNumberOfErrors()) {
				interruptStatus = TransferState.FAILURE;
			} else if (transferControlBlock.isCancelled()) {
				interruptStatus = TransferState.CANCELLED;
			} else {
				interruptStatus = TransferState.PAUSED;
			}

			TransferStatus status = TransferStatus.instance(TransferType.PUT,
					fileInSourceCollection.getAbsolutePath(),
					targetIrodsCollection.getAbsolutePath(), "",
					fileInSourceCollection.length(),
					fileInSourceCollection.length(), 0, 0, interruptStatus);
			transferStatusCallbackListener.statusCallback(status);
		}
	}

	/**
	 * Put a single file to iRODS.
	 * 
	 * @param fileInSourceCollection
	 * @param targetIrodsCollection
	 * @param transferStatusCallbackListener
	 * @param transferControlBlock
	 * @param dataObjectAO
	 * @throws JargonException
	 */
	private void putASingleFile(
			final File fileInSourceCollection,
			final IRODSFile targetIrodsCollection,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock,
			final DataObjectAO dataObjectAO) throws JargonException {

		log.info("put of single file");

		StringBuilder sb = new StringBuilder();
		sb.append(targetIrodsCollection.getAbsolutePath());
		sb.append('/');
		sb.append(fileInSourceCollection.getName());
		IRODSFile newIrodsFile = dataObjectAO.instanceIRODSFileForPath(sb
				.toString());
		newIrodsFile.setResource(targetIrodsCollection.getResource());

		try {
			dataObjectAO.putLocalDataObjectToIRODS(fileInSourceCollection,
					newIrodsFile, true);

			int totalFiles = 0;
			int totalFilesSoFar = 0;

			if (transferControlBlock != null) {
				transferControlBlock.incrementFilesTransferredSoFar();
				totalFiles = transferControlBlock.getTotalFilesToTransfer();
				totalFilesSoFar = transferControlBlock
						.getTotalFilesTransferredSoFar();
			}

			if (transferStatusCallbackListener != null) {
				TransferStatus status = TransferStatus.instance(
						TransferType.PUT,
						fileInSourceCollection.getAbsolutePath(),
						newIrodsFile.getAbsolutePath(), "",
						fileInSourceCollection.length(),
						fileInSourceCollection.length(), totalFilesSoFar,
						totalFiles, TransferState.SUCCESS);
				transferStatusCallbackListener.statusCallback(status);
			}
		} catch (JargonException je) {

			processRecursivePutException(fileInSourceCollection,
					transferStatusCallbackListener, newIrodsFile,
					transferControlBlock, je);
		}
	}

	/**
	 * @param fileInSourceCollection
	 * @param transferStatusCallbackListener
	 * @param newIrodsFile
	 * @param transferControlBlock
	 * @param je
	 * @throws JargonException
	 */
	private void processRecursivePutException(
			final File fileInSourceCollection,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final IRODSFile newIrodsFile,
			final TransferControlBlock transferControlBlock,
			final JargonException je) throws JargonException {

		log.error("exception in transfer", je);
		if (transferStatusCallbackListener != null) {
			log.warn("exception will be passed back to existing callback listener");

			int totalFiles = 0;
			int totalFilesSoFar = 0;

			if (transferControlBlock != null) {
				transferControlBlock.reportErrorInTransfer();
				totalFiles = transferControlBlock.getTotalFilesToTransfer();
				totalFilesSoFar = transferControlBlock
						.getTotalFilesTransferredSoFar();
			}

			TransferStatus status = TransferStatus.instanceForException(
					TransferType.PUT, fileInSourceCollection.getAbsolutePath(),
					newIrodsFile.getAbsolutePath(), "",
					fileInSourceCollection.length(),
					fileInSourceCollection.length(), totalFilesSoFar,
					totalFiles, je);

			log.info("status callback to be sent for error:{}", status);
			transferStatusCallbackListener.statusCallback(status);

		} else {
			log.warn("exception will be re-thrown, as there is no status callback listener");
			throw je;

		}
	}

	/**
	 * @param targetIrodsCollection
	 * @param transferStatusCallbackListener
	 * @param transferControlBlock
	 * @param collectionAO
	 * @param fileInSourceCollection
	 * @throws JargonException
	 */
	private void recursivelyPutACollection(
			final IRODSFile targetIrodsCollection,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock,
			final CollectionAO collectionAO, final File fileInSourceCollection)
			throws JargonException {

		// make a dir in the target collection
		StringBuilder sb = new StringBuilder();
		sb.append(targetIrodsCollection.getAbsolutePath());
		sb.append('/');
		sb.append(fileInSourceCollection.getName());
		log.info("recursively creating parent directory in irods at: {}",
				sb.toString());

		IRODSFile newSubCollection = collectionAO
				.instanceIRODSFileForCollectionPath(sb.toString());
		newSubCollection.setResource(targetIrodsCollection.getResource());

		try {
			newSubCollection.mkdirs();
			recursivelyPut(fileInSourceCollection, newSubCollection,
					transferStatusCallbackListener, transferControlBlock);
		} catch (JargonException je) {

			processRecursivePutException(fileInSourceCollection,
					transferStatusCallbackListener, newSubCollection,
					transferControlBlock, je);

		}
	}

	/**
	 * Method to recursively replicate a collection. This method can monitor for
	 * a cancellation, and can also provide callbacks to a process.
	 * 
	 * @param irodsFileAbsolutePath
	 *            <code>String</code> with the absolute path to an iRODS file
	 *            that should be replicated.
	 * @param targetResource
	 *            <code>String</code> with the resource to which the file should
	 *            be replicated.
	 * @param transferStatusCallbackListener
	 *            an optional
	 *            {@link org.irods.jargon.core.transfer.TransferStatusCallbackListener}
	 *            that can receive status callbacks. This may be set to null if
	 *            this functionality is not required.
	 * @param transferControlBlock
	 *            an optional
	 *            {@link org.irods.jargon.core.transfer.TransferControlBlock}
	 *            that provides a common object to communicate between the
	 *            object requesting the transfer, and the method performing the
	 *            transfer. This control block may contain a filter that can be
	 *            used to control restarts, and provides a way for the
	 *            requesting process to send a cancellation. This may be set to
	 *            null if not required.
	 * @throws JargonException
	 */
	protected void recursivelyReplicate(
			final IRODSFile sourceFile,
			final String targetResource,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock)
			throws JargonException {

		if (!sourceFile.isDirectory()) {
			throw new JargonException(
					"source file is not a directory, cannot recursively replicate");
		}

		log.info("recursively replicating source file: {}",
				sourceFile.getAbsolutePath());
		log.info("    into resource: {}", targetResource);

		for (File fileInSourceCollection : sourceFile.listFiles()) {

			// check for a cancel or pause at the top of the loop
			if (transferControlBlock != null
					&& (transferControlBlock.isCancelled() || transferControlBlock
							.isPaused())) {
				log.info("transfer cancelled or paused");
				if (transferStatusCallbackListener != null) {
					TransferState interruptStatus;
					if (transferControlBlock
							.shouldTransferBeAbandonedDueToNumberOfErrors()) {
						interruptStatus = TransferState.FAILURE;
					} else if (transferControlBlock.isCancelled()) {
						interruptStatus = TransferState.CANCELLED;
					} else {
						interruptStatus = TransferState.PAUSED;
					}

					TransferStatus status = TransferStatus.instance(
							TransferType.PUT,
							fileInSourceCollection.getAbsolutePath(), "",
							targetResource, fileInSourceCollection.length(),
							fileInSourceCollection.length(), 0, 0,
							interruptStatus);
					transferStatusCallbackListener.statusCallback(status);
					return;
				}
			}

			if (fileInSourceCollection.isDirectory()) {

				replicateWhenADirectory(targetResource,
						transferStatusCallbackListener, transferControlBlock,
						fileInSourceCollection);

				// a pause will need to bubble back up
				if (transferControlBlock != null
						&& (transferControlBlock.isCancelled() || transferControlBlock
								.isPaused())) {
					log.info("returning, is paused or cancelled");
					break;
				}

			} else {
				this.processReplicationOfSingleFile(
						fileInSourceCollection.getAbsolutePath(),
						targetResource, transferStatusCallbackListener,
						transferControlBlock);
			}
		}
	}

	/**
	 * @param targetResource
	 * @param transferStatusCallbackListener
	 * @param transferControlBlock
	 * @param fileInSourceCollection
	 * @throws JargonException
	 */
	private void replicateWhenADirectory(
			final String targetResource,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock,
			final File fileInSourceCollection) throws JargonException {
		try {
			recursivelyReplicate((IRODSFile) fileInSourceCollection,
					targetResource, transferStatusCallbackListener,
					transferControlBlock);
		} catch (Exception je) {
			// may rethrow or send back to the callback listener
			notifyReplicationTransferException(targetResource,
					transferStatusCallbackListener, transferControlBlock,
					fileInSourceCollection, je);
		}
	}

	/**
	 * @param targetResource
	 * @param transferStatusCallbackListener
	 * @param fileInSourceCollection
	 * @param je
	 * @throws JargonException
	 */
	private void notifyReplicationTransferException(
			final String targetResource,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock,
			final File fileInSourceCollection, final Exception je)
			throws JargonException {

		int totalFiles = 0;
		int totalFilesSoFar = 0;

		if (transferControlBlock != null) {
			transferControlBlock.reportErrorInTransfer();
			totalFiles = transferControlBlock.getTotalFilesToTransfer();
			totalFilesSoFar = transferControlBlock
					.getTotalFilesTransferredSoFar();
		}

		TransferStatus status = TransferStatus.instanceForException(
				TransferType.REPLICATE,
				fileInSourceCollection.getAbsolutePath(), "", targetResource,
				fileInSourceCollection.length(),
				fileInSourceCollection.length(), totalFilesSoFar, totalFiles,
				je);

		transferStatusCallbackListener.statusCallback(status);
	}

	/**
	 * Put a single file to iRODS.
	 * 
	 * @param sourceFile
	 *            <code>File</code> on the local file system that will be the
	 *            source of the put.
	 * @param targetIrodsFile
	 *            {@link org.irods.jargon.core.pub.io.File} that is the remote
	 *            file on iRODS which is the target of the put.
	 * @param transferStatusCallbackListener
	 *            {@link org.irods.jargon.core.transfer.TransferStatusCallbackListener}
	 *            implementation that will receive callbacks of success/failure
	 *            of each individual file transfer. This may be set to
	 *            <code>null</code>, in which case, exceptions that are thrown
	 *            will be rethrown by this method to the caller.
	 * @param transferControlBlock
	 *            {@link org.irods.jargon.core.transfer.TransferControlBlock}
	 *            implementation that is the communications mechanism between
	 *            the initiator of the transfer and the transfer process. This
	 *            may be set to <code>null</code> if those facilities are not
	 *            needed.
	 * @throws JargonException
	 */
	protected void processPutOfSingleFile(
			final File sourceFile,
			final IRODSFile targetIrodsFile,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock)
			throws JargonException {

		log.info("put of single file");
		DataObjectAO dataObjectAO = new DataObjectAOImpl(irodsSession,
				irodsAccount);

		try {
			dataObjectAO.putLocalDataObjectToIRODS(sourceFile, targetIrodsFile,
					true);

			int totalFiles = 0;
			int totalFilesSoFar = 0;

			if (transferControlBlock != null) {
				transferControlBlock.incrementFilesTransferredSoFar();
				totalFiles = transferControlBlock.getTotalFilesToTransfer();
				totalFilesSoFar = transferControlBlock
						.getTotalFilesTransferredSoFar();
			}

			if (transferStatusCallbackListener != null) {
				log.warn("success will be passed back to existing callback listener");

				TransferStatus status = TransferStatus.instance(
						TransferType.PUT, sourceFile.getAbsolutePath(),
						targetIrodsFile.getAbsolutePath(),
						targetIrodsFile.getResource(), sourceFile.length(),
						sourceFile.length(), totalFilesSoFar, totalFiles,
						TransferState.SUCCESS);

				transferStatusCallbackListener.statusCallback(status);
			}
		} catch (JargonException je) {
			// may rethrow or send back to the callback listener
			log.error("exception in transfer", je);

			int totalFiles = 0;
			int totalFilesSoFar = 0;

			if (transferControlBlock != null) {
				transferControlBlock.reportErrorInTransfer();
				totalFiles = transferControlBlock.getTotalFilesToTransfer();
				totalFilesSoFar = transferControlBlock
						.getTotalFilesTransferredSoFar();
			}

			if (transferStatusCallbackListener != null) {
				log.warn("exception will be passed back to existing callback listener");

				TransferStatus status = TransferStatus.instanceForException(
						TransferType.PUT, sourceFile.getAbsolutePath(),
						targetIrodsFile.getAbsolutePath(),
						targetIrodsFile.getResource(), sourceFile.length(),
						targetIrodsFile.length(), totalFilesSoFar, totalFiles,
						je);

				transferStatusCallbackListener.statusCallback(status);

			} else {
				log.warn("exception will be re-thrown, as there is no status callback listener");
				throw je;

			}
		}
	}

	/**
	 * Replicate a single file and process any exceptions or success callbacks.
	 * 
	 * @param irodsFileAbsolutePath
	 * @param targetResource
	 * @param transferStatusCallbackListener
	 *            {@link org.irods.jargon.core.transfer.TransferStatusCallbackListener}
	 *            implementation that will receive callbacks of success/failure
	 *            of each individual file transfer. This may be set to
	 *            <code>null</code>, in which case, exceptions that are thrown
	 *            will be rethrown by this method to the caller.
	 * @param transferControlBlock
	 *            {@link org.irods.jargon.core.transfer.TransferControlBlock}
	 *            implementation that is the communications mechanism between
	 *            the initiator of the transfer and the transfer process. This
	 *            may be set to <code>null</code> if those facilities are not
	 *            needed.
	 * @throws JargonException
	 */
	protected void processReplicationOfSingleFile(
			final String irodsFileAbsolutePath,
			final String targetResource,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock)
			throws JargonException {
		log.info("replicate single file");

		try {
			final DataObjectAO dataObjectAO = new DataObjectAOImpl(
					irodsSession, irodsAccount);

			dataObjectAO.replicateIrodsDataObject(irodsFileAbsolutePath,
					targetResource);

			log.info("replicate successful for file: {}", irodsFileAbsolutePath);

			int totalFiles = 0;
			int totalFilesSoFar = 0;

			if (transferControlBlock != null) {
				transferControlBlock.incrementFilesTransferredSoFar();
				totalFiles = transferControlBlock.getTotalFilesToTransfer();
				totalFilesSoFar = transferControlBlock
						.getTotalFilesTransferredSoFar();
			}

			// I do not track length during a replication
			if (transferStatusCallbackListener != null) {
				TransferStatus transferStatus = TransferStatus.instance(
						TransferType.REPLICATE, irodsFileAbsolutePath, "",
						targetResource, 0, 0, totalFilesSoFar, totalFiles,
						TransferState.SUCCESS);
				transferStatusCallbackListener.statusCallback(transferStatus);
			}

		} catch (JargonException e) {
			// may rethrow or send back to the callback listener
			log.error("exception in transfer", e);

			int totalFiles = 0;
			int totalFilesSoFar = 0;

			if (transferControlBlock != null) {
				transferControlBlock.reportErrorInTransfer();
				totalFiles = transferControlBlock.getTotalFilesToTransfer();
				totalFilesSoFar = transferControlBlock
						.getTotalFilesTransferredSoFar();
			}

			if (transferStatusCallbackListener != null) {
				log.warn("exception will be passed back to existing callback listener");

				TransferStatus status = TransferStatus.instanceForException(
						TransferType.REPLICATE, irodsFileAbsolutePath, "",
						targetResource, 0L, 0L, totalFilesSoFar, totalFiles, e);

				transferStatusCallbackListener.statusCallback(status);

			} else {
				log.warn("exception will be re-thrown, as there is no status callback listener");
				throw e;

			}
		}
	}

}
