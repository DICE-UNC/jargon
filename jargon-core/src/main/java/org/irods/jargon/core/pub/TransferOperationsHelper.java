package org.irods.jargon.core.pub;

import java.io.File;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.OverwriteException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatus.TransferState;
import org.irods.jargon.core.transfer.TransferStatus.TransferType;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener.FileStatusCallbackResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mike Conway - DICE (www.irods.org)
 *
 *         Functions to support transfer operations. These are used internally.
 *         See {@link org.irods.jargon.core.pub.DataTransferOperations} for
 *         public methods.
 *
 */
final class TransferOperationsHelper {

	static Logger log = LogManager.getLogger(TransferOperationsHelper.class);
	private final DataObjectAOImpl dataObjectAO;
	private final CollectionAO collectionAO;

	/**
	 * Initializer creates an instance of this class.
	 *
	 * @param irodsSession
	 *            {@code IRODSSession} that can connect to iRODS
	 * @param irodsAccount
	 *            {@code IRODSAccount} for this connection.
	 * @return
	 * @throws JargonException
	 */
	final static TransferOperationsHelper instance(final IRODSSession irodsSession, final IRODSAccount irodsAccount)
			throws JargonException {
		return new TransferOperationsHelper(irodsSession, irodsAccount);
	}

	private TransferOperationsHelper(final IRODSSession irodsSession, final IRODSAccount irodsAccount)
			throws JargonException {
		if (irodsSession == null || irodsAccount == null) {
			throw new JargonException("null irodsSession or irodsAccount");
		}

		dataObjectAO = new DataObjectAOImpl(irodsSession, irodsAccount);
		collectionAO = new CollectionAOImpl(irodsSession, irodsAccount);

	}

	/**
	 * Recursively get a file from iRODS. This utility method is used internally,
	 * and can process call-backs as well as filtering and cancellation.
	 *
	 * @param irodsSourceFile
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} that points to the
	 *            file or collection to retrieve.
	 * @param targetLocalFile
	 *            {@code File} that will hold the retrieved data.
	 * @param transferStatusCallbackListener
	 *            {@link org.irods.jargon.core.transfer.TransferStatusCallbackListener}
	 *            implementation that will receive callbacks indicating the
	 *            real-time status of the transfer.
	 * @param transferControlBlock
	 *            an optional
	 *            {@link org.irods.jargon.core.transfer.TransferControlBlock} that
	 *            provides a common object to communicate between the object
	 *            requesting the transfer, and the method performing the transfer.
	 *            This control block may contain a filter that can be used to
	 *            control restarts, and provides a way for the requesting process to
	 *            send a cancellation. This is required.
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	void recursivelyGet(final IRODSFile irodsSourceFile, final File targetLocalFile,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock)
			throws OverwriteException, FileNotFoundException, JargonException {

		log.info("recursively getting source file: {}", irodsSourceFile.getAbsolutePath());
		log.info("    into iRODS file: {}", targetLocalFile.getAbsolutePath());

		for (File fileInSourceCollection : irodsSourceFile.listFiles()) {

			if (Thread.interrupted()) {
				log.info("cancellation detected, set cancelled in tcb");
				transferControlBlock.setCancelled(true);
			}

			/**
			 * See if I want to close and renew the socket
			 */
			if (collectionAO.getIRODSProtocol().getPipelineConfiguration().getSocketRenewalIntervalInSeconds() > 0) {
				collectionAO.getIRODSSession().currentConnectionCheckRenewalOfSocket(collectionAO.getIRODSAccount());
			}

			// for each file in the given source collection, put the data file,
			// or create the new irodsCollection and step into it

			((IRODSFile) fileInSourceCollection).setResource(irodsSourceFile.getResource());

			// check for a cancel or pause at the top of the loop
			if (transferControlBlock.isCancelled() || transferControlBlock.isPaused()) {
				log.info("transfer cancelled or paused");
				if (transferStatusCallbackListener != null) {
					TransferState interruptStatus;
					if (transferControlBlock.shouldTransferBeAbandonedDueToNumberOfErrors()) {
						interruptStatus = TransferState.FAILURE;
					} else if (transferControlBlock.isCancelled()) {
						interruptStatus = TransferState.CANCELLED;
					} else {
						interruptStatus = TransferState.PAUSED;
					}

					TransferStatus status = TransferStatus.instance(TransferType.GET,
							fileInSourceCollection.getAbsolutePath(), targetLocalFile.getAbsolutePath(), "",
							fileInSourceCollection.length(), fileInSourceCollection.length(),
							transferControlBlock.getTotalFilesTransferredSoFar(),
							transferControlBlock.getTotalFilesSkippedSoFar(),
							transferControlBlock.getTotalFilesToTransfer(), interruptStatus,
							dataObjectAO.getIRODSAccount().getHost(), dataObjectAO.getIRODSAccount().getZone());
					transferStatusCallbackListener.statusCallback(status);
					return;
				}
			}

			// may have returned above if cancelled

			try {
				if (fileInSourceCollection.isDirectory()) {

					// make a dir in the target collection
					StringBuilder sb = new StringBuilder();
					sb.append(targetLocalFile.getAbsolutePath());
					sb.append('/');
					sb.append(fileInSourceCollection.getName());
					log.info("recursively creating parent directory in local file system at: {}", sb.toString());

					File newSubCollection = new File(sb.toString());
					boolean success = newSubCollection.mkdirs();

					if (!success) {
						log.warn("unable to make directories in local file system, log and proceed");
					}

					recursivelyGet((IRODSFile) fileInSourceCollection, newSubCollection, transferStatusCallbackListener,
							transferControlBlock);

				} else {
					processGetOfSingleFile((IRODSFile) fileInSourceCollection, targetLocalFile,
							transferStatusCallbackListener, transferControlBlock);
				}
			} catch (JargonException e) {
				if (!transferControlBlock.isCancelled()) {
					throw e;
				}
			} catch (Exception e) {
				if (!transferControlBlock.isCancelled()) {

					log.info("unanticipated exception will be transformed into a Jargon exception", e);
					throw new JargonException(e);
				}
			}
		}
	}

	/**
	 * In a transfer operation, process the given iRODS file as a data object to be
	 * retrieved.
	 *
	 * @param irodsSourceFile
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} that is the source
	 *            of the get.
	 * @param targetLocalFile
	 *            {@code File} on the local file system to which the files will be
	 *            transferrred.
	 * @param transferStatusCallbackListener
	 *            {@link org.irods.jargon.core.transfer.TransferStatusCallbackListener}
	 *            implementation that will receive callbacks of success/failure of
	 *            each individual file transfer. This may be set to {@code null}, in
	 *            which case, exceptions that are thrown will be rethrown by this
	 *            method to the caller.
	 * @param transferControlBlock
	 *            {@link org.irods.jargon.core.transfer.TransferControlBlock}
	 *            implementation that is the communications mechanism between the
	 *            initiator of the transfer and the transfer process. This is
	 *            required.
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	void processGetOfSingleFile(final IRODSFile irodsSourceFile, final File targetLocalFile,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock)
			throws OverwriteException, FileNotFoundException, JargonException {

		log.info("processGetOfSingleFile()");

		if (irodsSourceFile == null) {
			throw new IllegalArgumentException("null irodsSourceFile");
		}

		if (targetLocalFile == null) {
			throw new IllegalArgumentException("null targetLocalFile");
		}

		/*
		 * This is a transfer of a single file, normalize the target path to be a file,
		 * rather then the parent collection, so restarts can find the previous attempt.
		 */

		File targetLocalFileAsFile = null;
		if (targetLocalFile.isDirectory()) {
			StringBuilder sb = new StringBuilder();
			sb.append(targetLocalFile.getAbsolutePath());
			sb.append("/");
			sb.append(irodsSourceFile.getName());
			targetLocalFileAsFile = new File(sb.toString());
		} else {
			targetLocalFileAsFile = targetLocalFile;
		}

		log.info("get of single file...filtered?");

		int totalFiles = 0;

		totalFiles = transferControlBlock.getTotalFilesToTransfer();

		if (!transferControlBlock.filter(irodsSourceFile.getAbsolutePath())) {
			log.info("file is filtered and discarded: {}", irodsSourceFile.getAbsolutePath());

			transferControlBlock.incrementFilesSkippedSoFar();

			if (transferStatusCallbackListener != null) {
				TransferStatus status = TransferStatus.instance(TransferType.GET, irodsSourceFile.getAbsolutePath(),
						targetLocalFileAsFile.getAbsolutePath(), "", 0, 0,
						transferControlBlock.getTotalFilesTransferredSoFar(),
						transferControlBlock.getTotalFilesSkippedSoFar(), totalFiles, TransferState.RESTARTING,
						dataObjectAO.getIRODSAccount().getHost(), dataObjectAO.getIRODSAccount().getZone());

				transferStatusCallbackListener.statusCallback(status);
			}
			return;
		}

		// may have returned above if filtered

		log.info("filter passed, process...");

		try {

			if (transferStatusCallbackListener != null) {

				long sourceFileLength = irodsSourceFile.length();
				TransferStatus status = TransferStatus.instance(TransferType.GET, irodsSourceFile.getAbsolutePath(),
						targetLocalFileAsFile.getAbsolutePath(), "", sourceFileLength, 0,
						transferControlBlock.getTotalFilesTransferredSoFar(),
						transferControlBlock.getTotalFilesSkippedSoFar(), totalFiles,
						TransferState.IN_PROGRESS_START_FILE, dataObjectAO.getIRODSAccount().getHost(),
						dataObjectAO.getIRODSAccount().getZone());

				/*
				 * The callback listener may respond with a request to skip this particular file
				 */

				FileStatusCallbackResponse response = transferStatusCallbackListener.statusCallback(status);
				if (response == FileStatusCallbackResponse.SKIP) {
					log.info("file signalled as skipped in callback response:{}", irodsSourceFile.getAbsolutePath());
					transferControlBlock.incrementFilesSkippedSoFar();
					status = TransferStatus.instance(TransferType.GET, irodsSourceFile.getAbsolutePath(),
							targetLocalFileAsFile.getAbsolutePath(), "", 0, 0,
							transferControlBlock.getTotalFilesTransferredSoFar(),
							transferControlBlock.getTotalFilesSkippedSoFar(), totalFiles, TransferState.SKIPPING,
							dataObjectAO.getIRODSAccount().getHost(), dataObjectAO.getIRODSAccount().getZone());

					transferStatusCallbackListener.statusCallback(status);
					return;
				}

			}

			try {
				dataObjectAO.getDataObjectFromIrods(irodsSourceFile, targetLocalFileAsFile, transferControlBlock,
						transferStatusCallbackListener);
			} catch (JargonException e) {
				log.error("exception in transfer, will abandon the connection and rethrow", e);
				throw e;
			} catch (Exception e) {
				log.error("exception in transfer, will abandon the connection and rethrow", e);
				dataObjectAO.getIRODSAccessObjectFactory().getIrodsSession()
						.discardSessionForErrors(dataObjectAO.getIRODSAccount());
				throw new JargonException(e);
			}

			transferControlBlock.incrementFilesTransferredSoFar();

			if (transferStatusCallbackListener != null) {

				long sourceFileLength = irodsSourceFile.length();
				TransferStatus status = TransferStatus.instance(TransferType.GET, irodsSourceFile.getAbsolutePath(),
						targetLocalFileAsFile.getAbsolutePath(), "", sourceFileLength, sourceFileLength,
						transferControlBlock.getTotalFilesTransferredSoFar(),
						transferControlBlock.getTotalFilesSkippedSoFar(), totalFiles,
						TransferState.IN_PROGRESS_COMPLETE_FILE, dataObjectAO.getIRODSAccount().getHost(),
						dataObjectAO.getIRODSAccount().getZone());

				transferStatusCallbackListener.statusCallback(status);
			}

		} catch (JargonException je) {

			if (!transferControlBlock.isCancelled()) {

				// may re-throw or send back to the call-back listener
				log.error("exception in transfer", je);

				transferControlBlock.reportErrorInTransfer();

				if (transferStatusCallbackListener != null) {
					log.warn("exception will be passed back to existing callback listener");

					TransferStatus status = TransferStatus.instanceForException(TransferType.GET,
							irodsSourceFile.getAbsolutePath(), targetLocalFileAsFile.getAbsolutePath(), "",
							targetLocalFileAsFile.length(), transferControlBlock.getTotalBytesTransferredSoFar(),
							transferControlBlock.getTotalFilesTransferredSoFar(),
							transferControlBlock.getTotalFilesSkippedSoFar(), totalFiles, je,
							dataObjectAO.getIRODSAccount().getHost(), dataObjectAO.getIRODSAccount().getZone());

					transferStatusCallbackListener.statusCallback(status);

				} else {
					log.warn("exception will be re-thrown, as there is no status callback listener");
					throw je;

				}
			}
		}
	}

	/**
	 * Method to recursively put a collection. This method can monitor for a
	 * cancellation, and can also provide callbacks to a process.
	 *
	 * @param irodsFileAbsolutePath
	 *            {@code String} with the absolute path to an iRODS file that should
	 *            be replicated.
	 * @param targetResource
	 *            {@code String} with the resource to which the file should be
	 *            replicated.
	 * @param transferStatusCallbackListener
	 *            an optional
	 *            {@link org.irods.jargon.core.transfer.TransferStatusCallbackListener}
	 *            that can receive status callbacks. This may be set to null if this
	 *            functionality is not required.
	 * @param transferControlBlock
	 *            an optional
	 *            {@link org.irods.jargon.core.transfer.TransferControlBlock} that
	 *            provides a common object to communicate between the object
	 *            requesting the transfer, and the method performing the transfer.
	 *            This control block may contain a filter that can be used to
	 *            control restarts, and provides a way for the requesting process to
	 *            send a cancellation. This may be set to null if not required.
	 * @throws JargonException
	 */
	void recursivelyPut(final File sourceFile, final IRODSFile targetIrodsCollection,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock)
			throws OverwriteException, DataNotFoundException, JargonException {

		if (sourceFile == null) {
			throw new IllegalArgumentException("null source file");
		}

		if (targetIrodsCollection == null) {
			throw new IllegalArgumentException("null targetIrodsCollection");
		}

		if (!sourceFile.isDirectory()) {
			throw new JargonException("source file is not a directory, cannot recursively put");
		}

		log.info("recursively putting source file: {}", sourceFile.getAbsolutePath());
		log.info("    into iRODS file: {}", targetIrodsCollection.getAbsolutePath());
		log.info("     to resource:{}", targetIrodsCollection.getResource());

		try {
			File[] files = sourceFile.listFiles();
			if (files != null) {
				for (File fileInSourceCollection : files) {

					if (Thread.interrupted()) {
						log.info("cancellation detected, set cancelled in tcb");
						transferControlBlock.setCancelled(true);
					}

					// check for a cancel or pause at the top of the loop
					if (transferControlBlock.isCancelled() || transferControlBlock.isPaused()) {
						log.info("will notify pause or cancel for this put");
						notifyPauseOrCancelCallbackForPut(targetIrodsCollection, transferStatusCallbackListener,
								transferControlBlock, fileInSourceCollection);
						break;
					}

					/**
					 * See if I want to close and renew the socket
					 */
					if (collectionAO.getIRODSProtocol().getPipelineConfiguration()
							.getSocketRenewalIntervalInSeconds() > 0) {
						collectionAO.getIRODSSession()
								.currentConnectionCheckRenewalOfSocket(collectionAO.getIRODSAccount());
					}

					if (fileInSourceCollection.isDirectory()) {
						recursivelyPutACollection(targetIrodsCollection, transferStatusCallbackListener,
								transferControlBlock, fileInSourceCollection);

					} else {

						processPutOfSingleFile(fileInSourceCollection, targetIrodsCollection,
								transferStatusCallbackListener, transferControlBlock);
					}
				}
			}
		} catch (Exception e) {
			if (!transferControlBlock.isCancelled()) {
				log.info("unanticipated exception will be transformed into a Jargon exception", e);
				throw new JargonException(e);
			}
		}
	}

	/**
	 * A put operation has been cancelled or paused, give the appropraite callback
	 *
	 * @param targetIrodsCollection
	 *            {@link IRODSFile} that was the source
	 * @param transferStatusCallbackListener
	 *            {@link TransferStatusCallbackListener} that receives the call-back
	 * @param transferControlBlock
	 *            {@link TransferControlBlock} that contains information about the
	 *            transfer
	 * @param fileInSourceCollection
	 *            {@link File} that was the current source of the put
	 * @throws JargonException
	 */
	private void notifyPauseOrCancelCallbackForPut(final IRODSFile targetIrodsCollection,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock, final File fileInSourceCollection) throws JargonException {

		log.info("transfer cancelled or paused, signal with a callback");
		if (transferStatusCallbackListener != null) {

			TransferState interruptStatus;

			if (transferControlBlock.shouldTransferBeAbandonedDueToNumberOfErrors()) {
				interruptStatus = TransferState.FAILURE;
			} else if (transferControlBlock.isCancelled()) {
				interruptStatus = TransferState.CANCELLED;
			} else {
				interruptStatus = TransferState.PAUSED;
			}

			TransferStatus status = TransferStatus.instance(TransferType.PUT, fileInSourceCollection.getAbsolutePath(),
					targetIrodsCollection.getAbsolutePath(), "", fileInSourceCollection.length(),
					fileInSourceCollection.length(), transferControlBlock.getTotalFilesTransferredSoFar(),
					transferControlBlock.getTotalFilesSkippedSoFar(), transferControlBlock.getTotalFilesToTransfer(),
					interruptStatus, dataObjectAO.getIRODSAccount().getHost(),
					dataObjectAO.getIRODSAccount().getZone());
			log.info("status callback for cancel:{}", status);
			transferStatusCallbackListener.statusCallback(status);
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
	private void processRecursivePutException(final File fileInSourceCollection,
			final TransferStatusCallbackListener transferStatusCallbackListener, final IRODSFile newIrodsFile,
			final TransferControlBlock transferControlBlock, final JargonException je) throws JargonException {

		log.error("exception in transfer", je);
		if (transferStatusCallbackListener != null) {
			log.warn("exception will be passed back to existing callback listener");

			int totalFiles = 0;

			if (transferControlBlock != null) {
				transferControlBlock.reportErrorInTransfer();
				totalFiles = transferControlBlock.getTotalFilesToTransfer();
			}

			int filesTransferredSoFar = 0;
			int filesSkippedSoFar = 0;

			if (transferControlBlock != null) {
				filesTransferredSoFar = transferControlBlock.getTotalFilesTransferredSoFar();
				filesSkippedSoFar = transferControlBlock.getTotalFilesSkippedSoFar();
			}

			TransferStatus status = TransferStatus.instanceForException(TransferType.PUT,
					fileInSourceCollection.getAbsolutePath(), newIrodsFile.getAbsolutePath(), "",
					fileInSourceCollection.length(), fileInSourceCollection.length(), filesTransferredSoFar,
					filesSkippedSoFar, totalFiles, je, dataObjectAO.getIRODSAccount().getHost(),
					dataObjectAO.getIRODSAccount().getZone());

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
	 * @param fileInSourceCollection
	 * @throws JargonException
	 */
	private void recursivelyPutACollection(final IRODSFile targetIrodsCollection,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock, final File fileInSourceCollection)
			throws OverwriteException, DataNotFoundException, JargonException {

		// make a dir in the target collection
		StringBuilder sb = new StringBuilder();
		sb.append(targetIrodsCollection.getAbsolutePath());
		sb.append('/');
		sb.append(fileInSourceCollection.getName());
		log.info("recursively creating parent directory in irods at: {}", sb.toString());

		IRODSFile newSubCollection = collectionAO.instanceIRODSFileForCollectionPath(sb.toString());
		newSubCollection.setResource(targetIrodsCollection.getResource());

		try {
			newSubCollection.mkdirs();
			recursivelyPut(fileInSourceCollection, newSubCollection, transferStatusCallbackListener,
					transferControlBlock);
		} catch (JargonException je) {

			if (!transferControlBlock.isCancelled()) {
				processRecursivePutException(fileInSourceCollection, transferStatusCallbackListener, newSubCollection,
						transferControlBlock, je);
			}

		} catch (Exception e) {

			if (!transferControlBlock.isCancelled()) {
				log.error("unanticipated exception will be transformed into a JargonException and processed", e);
				JargonException je = new JargonException(e);
				processRecursivePutException(fileInSourceCollection, transferStatusCallbackListener, newSubCollection,
						transferControlBlock, je);
			}
		}
	}

	/**
	 * Method to recursively replicate a collection. This method can monitor for a
	 * cancellation, and can also provide callbacks to a process.
	 *
	 * @param irodsFileAbsolutePath
	 *            {@code String} with the absolute path to an iRODS file that should
	 *            be replicated.
	 * @param targetResource
	 *            {@code String} with the resource to which the file should be
	 *            replicated.
	 * @param transferStatusCallbackListener
	 *            an optional
	 *            {@link org.irods.jargon.core.transfer.TransferStatusCallbackListener}
	 *            that can receive status callbacks. This may be set to null if this
	 *            functionality is not required.
	 * @param transferControlBlock
	 *            an optional
	 *            {@link org.irods.jargon.core.transfer.TransferControlBlock} that
	 *            provides a common object to communicate between the object
	 *            requesting the transfer, and the method performing the transfer.
	 *            This control block may contain a filter that can be used to
	 *            control restarts, and provides a way for the requesting process to
	 *            send a cancellation. This may be set to null if not required.
	 * @throws JargonException
	 */
	void recursivelyReplicate(final IRODSFile sourceFile, final String targetResource,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock) throws JargonException {

		if (!sourceFile.isDirectory()) {
			throw new JargonException("source file is not a directory, cannot recursively replicate");
		}

		log.info("recursively replicating source file: {}", sourceFile.getAbsolutePath());
		log.info("    into resource: {}", targetResource);

		for (File fileInSourceCollection : sourceFile.listFiles()) {

			if (Thread.interrupted()) {
				log.info("cancellation detected, set cancelled in tcb");
				transferControlBlock.setCancelled(true);
			}

			// check for a cancel or pause at the top of the loop
			if (transferControlBlock.isCancelled() || transferControlBlock.isPaused()) {
				log.info("transfer cancelled or paused");
				if (transferStatusCallbackListener != null) {
					TransferState interruptStatus;
					if (transferControlBlock.shouldTransferBeAbandonedDueToNumberOfErrors()) {
						interruptStatus = TransferState.FAILURE;
					} else if (transferControlBlock.isCancelled()) {
						interruptStatus = TransferState.CANCELLED;
					} else {
						interruptStatus = TransferState.PAUSED;
					}

					TransferStatus status = TransferStatus.instance(TransferType.REPLICATE,
							fileInSourceCollection.getAbsolutePath(), "", targetResource,
							fileInSourceCollection.length(), fileInSourceCollection.length(),
							transferControlBlock.getTotalFilesTransferredSoFar(),
							transferControlBlock.getTotalFilesSkippedSoFar(),
							transferControlBlock.getTotalFilesToTransfer(), interruptStatus,
							dataObjectAO.getIRODSAccount().getHost(), dataObjectAO.getIRODSAccount().getZone());
					transferStatusCallbackListener.statusCallback(status);
					return;
				}
			}

			if (fileInSourceCollection.isDirectory()) {

				replicateWhenADirectory(targetResource, transferStatusCallbackListener, transferControlBlock,
						fileInSourceCollection);

				// a pause will need to bubble back up
				if (transferControlBlock.isCancelled() || transferControlBlock.isPaused()) {
					log.info("returning, is paused or cancelled");
					break;
				}

			} else {
				processReplicationOfSingleFile(fileInSourceCollection.getAbsolutePath(), targetResource,
						transferStatusCallbackListener, transferControlBlock);
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
	private void replicateWhenADirectory(final String targetResource,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock, final File fileInSourceCollection) throws JargonException {
		try {
			recursivelyReplicate((IRODSFile) fileInSourceCollection, targetResource, transferStatusCallbackListener,
					transferControlBlock);
		} catch (Exception je) {
			// may rethrow or send back to the callback listener

			if (!transferControlBlock.isCancelled()) {
				notifyReplicationTransferException(targetResource, transferStatusCallbackListener, transferControlBlock,
						fileInSourceCollection, je);
			}
		}
	}

	/**
	 * @param targetResource
	 * @param transferStatusCallbackListener
	 * @param fileInSourceCollection
	 * @param je
	 * @throws JargonException
	 */
	private void notifyReplicationTransferException(final String targetResource,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock, final File fileInSourceCollection, final Exception je)
			throws JargonException {

		int totalFiles = 0;
		int totalFilesSoFar = 0;
		int totalFilesSkipped = 0;

		transferControlBlock.reportErrorInTransfer();
		totalFiles = transferControlBlock.getTotalFilesToTransfer();
		totalFilesSoFar = transferControlBlock.getTotalFilesTransferredSoFar();
		totalFilesSkipped = transferControlBlock.getTotalFilesSkippedSoFar();

		TransferStatus status = TransferStatus.instanceForException(TransferType.REPLICATE,
				fileInSourceCollection.getAbsolutePath(), "", targetResource, fileInSourceCollection.length(),
				fileInSourceCollection.length(), totalFilesSoFar, totalFilesSkipped, totalFiles, je,
				dataObjectAO.getIRODSAccount().getHost(), dataObjectAO.getIRODSAccount().getZone());

		transferStatusCallbackListener.statusCallback(status);
	}

	/**
	 * Put a single file to iRODS.
	 *
	 * @param sourceFile
	 *            {@code File} on the local file system that will be the source of
	 *            the put.
	 * @param targetIrodsFile
	 *            {@link org.irods.jargon.core.pub.io.File} that is the remote file
	 *            on iRODS which is the target of the put.
	 * @param transferStatusCallbackListener
	 *            {@link org.irods.jargon.core.transfer.TransferStatusCallbackListener}
	 *            implementation that will receive callbacks of success/failure of
	 *            each individual file transfer. This may be set to {@code null}, in
	 *            which case, exceptions that are thrown will be rethrown by this
	 *            method to the caller.
	 * @param transferControlBlock
	 *            {@link org.irods.jargon.core.transfer.TransferControlBlock}
	 *            implementation that is the communications mechanism between the
	 *            initiator of the transfer and the transfer process.
	 * @throws JargonException
	 */
	void processPutOfSingleFile(final File sourceFile, final IRODSFile targetIrodsFile,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock)
			throws OverwriteException, DataNotFoundException, JargonException {

		log.info("put of single file");

		if (sourceFile == null) {
			throw new IllegalArgumentException("null sourceFile");
		}

		if (targetIrodsFile == null) {
			throw new IllegalArgumentException("null targetIrodsFile");
		}

		if (transferControlBlock == null) {
			throw new IllegalArgumentException("null transferControlBlock");
		}

		/*
		 * This is a transfer of a single file, normalize the target path to be a file,
		 * rather then the parent collection, so restarts can find the previous attempt.
		 */

		IRODSFile targetFileAsFile = null;
		if (targetIrodsFile.isDirectory()) {
			StringBuilder sb = new StringBuilder();
			sb.append(targetIrodsFile.getAbsolutePath());
			sb.append("/");
			sb.append(sourceFile.getName());
			targetFileAsFile = collectionAO.getIRODSFileFactory().instanceIRODSFile(sb.toString());
			targetFileAsFile.setResource(targetIrodsFile.getResource());
		} else {
			targetFileAsFile = targetIrodsFile;
		}

		try {

			// if I am restarting a recursive transfer. Consult the last good
			// path to see if I need to transfer this file

			if (!transferControlBlock.filter(sourceFile.getAbsolutePath())) {
				log.debug("file filtered and not transferred");
				transferControlBlock.incrementFilesSkippedSoFar();
				TransferStatus status = TransferStatus.instance(TransferType.PUT, sourceFile.getAbsolutePath(),
						targetFileAsFile.getAbsolutePath(), "", 0, 0,
						transferControlBlock.getTotalFilesTransferredSoFar(),
						transferControlBlock.getTotalFilesSkippedSoFar(),
						transferControlBlock.getTotalFilesToTransfer(), TransferState.RESTARTING,
						dataObjectAO.getIRODSAccount().getHost(), dataObjectAO.getIRODSAccount().getZone());

				transferStatusCallbackListener.statusCallback(status);
				return;
			}

			if (transferStatusCallbackListener != null) {
				TransferStatus status = TransferStatus.instance(TransferType.PUT, sourceFile.getAbsolutePath(),
						targetFileAsFile.getAbsolutePath(), targetFileAsFile.getResource(), sourceFile.length(), 0,
						transferControlBlock.getTotalFilesTransferredSoFar(),
						transferControlBlock.getTotalFilesSkippedSoFar(),
						transferControlBlock.getTotalFilesToTransfer(), TransferState.IN_PROGRESS_START_FILE,
						dataObjectAO.getIRODSAccount().getHost(), dataObjectAO.getIRODSAccount().getZone());

				/*
				 * I make the status callback, and the listener, if configured, may respond to
				 * skip this file or continue. If they say skip, then send a callback that says
				 * this was done, and increment the skipped count in the tcb
				 */

				FileStatusCallbackResponse response = transferStatusCallbackListener.statusCallback(status);
				if (response == FileStatusCallbackResponse.SKIP) {
					log.info("file signalled as skipped in callback response:{}", sourceFile.getAbsolutePath());
					transferControlBlock.incrementFilesSkippedSoFar();

					status = TransferStatus.instance(TransferType.PUT, sourceFile.getAbsolutePath(),
							targetFileAsFile.getAbsolutePath(), "", 0, 0,
							transferControlBlock.getTotalFilesTransferredSoFar(),
							transferControlBlock.getTotalFilesSkippedSoFar(),
							transferControlBlock.getTotalFilesToTransfer(), TransferState.SKIPPING,
							dataObjectAO.getIRODSAccount().getHost(), dataObjectAO.getIRODSAccount().getZone());

					transferStatusCallbackListener.statusCallback(status);
					return;
				}
			}

			/*
			 * The put operation handles any restart processing
			 */
			dataObjectAO.putLocalDataObjectToIRODS(sourceFile, targetFileAsFile, transferControlBlock,
					transferStatusCallbackListener, false);

			transferControlBlock.incrementFilesTransferredSoFar();

			if (transferStatusCallbackListener != null) {

				TransferStatus status = TransferStatus.instance(TransferType.PUT, sourceFile.getAbsolutePath(),
						targetFileAsFile.getAbsolutePath(), targetFileAsFile.getResource(), sourceFile.length(),
						sourceFile.length(), transferControlBlock.getTotalFilesTransferredSoFar(),
						transferControlBlock.getTotalFilesSkippedSoFar(),
						transferControlBlock.getTotalFilesToTransfer(), TransferState.IN_PROGRESS_COMPLETE_FILE,
						dataObjectAO.getIRODSAccount().getHost(), dataObjectAO.getIRODSAccount().getZone());

				transferStatusCallbackListener.statusCallback(status);
			}
		} catch (JargonException je) {

			if (!transferControlBlock.isCancelled()) {

				// may re throw or send back to the callback listener
				log.error("exception in transfer", je);

				transferControlBlock.reportErrorInTransfer();

				if (transferStatusCallbackListener != null) {
					log.warn("exception will be passed back to existing callback listener");

					TransferStatus status = TransferStatus.instanceForException(TransferType.PUT,
							sourceFile.getAbsolutePath(), targetFileAsFile.getAbsolutePath(),
							targetFileAsFile.getResource(), sourceFile.length(), targetFileAsFile.length(),
							transferControlBlock.getTotalFilesTransferredSoFar(),
							transferControlBlock.getTotalFilesSkippedSoFar(),
							transferControlBlock.getTotalFilesToTransfer(), je,
							dataObjectAO.getIRODSAccount().getHost(), dataObjectAO.getIRODSAccount().getZone());

					transferStatusCallbackListener.statusCallback(status);

				} else {
					log.warn("exception will be re-thrown, as there is no status callback listener");
					throw je;

				}
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
	 *            implementation that will receive callbacks of success/failure of
	 *            each individual file transfer. This may be set to {@code null}, in
	 *            which case, exceptions that are thrown will be rethrown by this
	 *            method to the caller.
	 * @param transferControlBlock
	 *            {@link org.irods.jargon.core.transfer.TransferControlBlock}
	 *            implementation that is the communications mechanism between the
	 *            initiator of the transfer and the transfer process. This may be
	 *            set to {@code null} if those facilities are not needed.
	 * @throws JargonException
	 */
	void processReplicationOfSingleFile(final String irodsFileAbsolutePath, final String targetResource,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock) throws JargonException {
		log.info("replicate single file");

		if (!transferControlBlock.filter(irodsFileAbsolutePath)) {
			log.info("file is filtered and discarded: {}", irodsFileAbsolutePath);
			transferControlBlock.incrementFilesSkippedSoFar();
			TransferStatus status = TransferStatus.instance(TransferType.REPLICATE, irodsFileAbsolutePath, "",
					targetResource, 0, 0, transferControlBlock.getTotalFilesTransferredSoFar(),
					transferControlBlock.getTotalFilesSkippedSoFar(), transferControlBlock.getTotalFilesToTransfer(),
					TransferState.RESTARTING, dataObjectAO.getIRODSAccount().getHost(),
					dataObjectAO.getIRODSAccount().getZone());
			transferStatusCallbackListener.statusCallback(status);
			return;
		}

		log.info("filter passed, process...");

		try {

			dataObjectAO.replicateIrodsDataObject(irodsFileAbsolutePath, targetResource);

			log.info("replicate successful for file: {}", irodsFileAbsolutePath);
			transferControlBlock.incrementFilesTransferredSoFar();

			// I do not track length during a replication
			if (transferStatusCallbackListener != null) {
				TransferStatus transferStatus = TransferStatus.instance(TransferType.REPLICATE, irodsFileAbsolutePath,
						"", targetResource, 0, 0, transferControlBlock.getTotalFilesTransferredSoFar(),
						transferControlBlock.getTotalFilesSkippedSoFar(),
						transferControlBlock.getTotalFilesToTransfer(), TransferState.SUCCESS,
						dataObjectAO.getIRODSAccount().getHost(), dataObjectAO.getIRODSAccount().getZone());
				transferStatusCallbackListener.statusCallback(transferStatus);
			}

		} catch (JargonException e) {

			if (!transferControlBlock.isCancelled()) {
				handleExceptionInReplicate(irodsFileAbsolutePath, targetResource, transferStatusCallbackListener,
						transferControlBlock, transferControlBlock.getTotalFilesToTransfer(),
						transferControlBlock.getTotalFilesTransferredSoFar(),
						transferControlBlock.getTotalFilesSkippedSoFar(), e);
			}
		} catch (Exception e) {
			if (!transferControlBlock.isCancelled()) {
				log.error(
						"unanticipated exception in replicate, will wrap as a JargonException so that callback handlers may have a crack at it...",
						e);
				handleExceptionInReplicate(irodsFileAbsolutePath, targetResource, transferStatusCallbackListener,
						transferControlBlock, transferControlBlock.getTotalFilesTransferredSoFar(),
						transferControlBlock.getTotalFilesSkippedSoFar(),
						transferControlBlock.getTotalFilesToTransfer(), new JargonException(e));
			}
		}
	}

	private void handleExceptionInReplicate(final String irodsFileAbsolutePath, final String targetResource,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock, final int totalFiles, final int totalFilesSoFar,
			final int totalFilesSkippedSoFar, final JargonException e) throws JargonException {

		log.error("exception in transfer", e);

		if (transferStatusCallbackListener != null) {
			log.warn("exception will be passed back to existing callback listener");

			TransferStatus status = TransferStatus.instanceForException(TransferType.REPLICATE, irodsFileAbsolutePath,
					"", targetResource, 0L, 0L, totalFilesSoFar, totalFilesSkippedSoFar, totalFiles, e,
					dataObjectAO.getIRODSAccount().getHost(), dataObjectAO.getIRODSAccount().getZone());

			transferStatusCallbackListener.statusCallback(status);

		} else {
			log.warn("exception will be re-thrown, as there is no status callback listener");
			throw e;

		}
	}

	void recursivelyCopy(final IRODSFile irodsSourceFile, final String targetResource,
			final String targetIrodsFileAbsolutePath,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock)
			throws OverwriteException, DataNotFoundException, JargonException {

		if (!irodsSourceFile.isDirectory()) {
			throw new JargonException("source file is not a directory, cannot recursively copy");
		}

		log.info("recursively copying source file: {}", irodsSourceFile.getAbsolutePath());
		log.info("to target file: {}", targetIrodsFileAbsolutePath);
		log.info("resource: {}", targetResource);
		IRODSFile childTargetFile = null;

		for (File fileInSourceCollection : irodsSourceFile.listFiles()) {

			if (Thread.interrupted()) {
				log.info("cancellation detected, set cancelled in tcb");
				transferControlBlock.setCancelled(true);
			}

			// check for a cancel or pause at the top of the loop
			if (transferControlBlock.isCancelled() || transferControlBlock.isPaused()) {
				log.info("transfer cancelled or paused");
				if (transferStatusCallbackListener != null) {
					TransferState interruptStatus;
					if (transferControlBlock.shouldTransferBeAbandonedDueToNumberOfErrors()) {
						interruptStatus = TransferState.FAILURE;
					} else if (transferControlBlock.isCancelled()) {
						interruptStatus = TransferState.CANCELLED;
					} else {
						interruptStatus = TransferState.PAUSED;
					}

					TransferStatus status = TransferStatus.instance(TransferType.COPY,
							fileInSourceCollection.getAbsolutePath(), targetIrodsFileAbsolutePath, targetResource, 0L,
							0L, transferControlBlock.getTotalFilesTransferredSoFar(),
							transferControlBlock.getTotalFilesSkippedSoFar(),
							transferControlBlock.getTotalFilesToTransfer(), interruptStatus,
							dataObjectAO.getIRODSAccount().getHost(), dataObjectAO.getIRODSAccount().getZone());
					transferStatusCallbackListener.statusCallback(status);
					return;
				}
			}

			if (fileInSourceCollection.isDirectory()) {

				log.debug("source is a collection, create the target");
				StringBuilder targetCollectionName = new StringBuilder(targetIrodsFileAbsolutePath);
				targetCollectionName.append("/");
				targetCollectionName.append(fileInSourceCollection.getName());
				String targetCollection = targetCollectionName.toString();
				childTargetFile = collectionAO.instanceIRODSFileForCollectionPath(targetCollection);
				childTargetFile.mkdirs();

				recursivelyCopy((IRODSFile) fileInSourceCollection, targetResource, targetCollection,
						transferStatusCallbackListener, transferControlBlock);

			} else {
				StringBuilder sb = new StringBuilder();
				sb.append(targetIrodsFileAbsolutePath);
				sb.append("/");
				sb.append(fileInSourceCollection.getName());
				processCopyOfSingleFile(fileInSourceCollection.getAbsolutePath(), targetResource, sb.toString(),
						transferStatusCallbackListener, transferControlBlock);
			}
		}
	}

	/**
	 * Process the copy of a source file to a given target file. This is an iRODS to
	 * iRODS copy.
	 *
	 * @param irodsSourceFileAbsolutePath
	 *            {@code String} with the absolute path to the source file, which is
	 *            an iRODS data object, not a collection
	 * @param targetResource
	 *            {@code String} with the optional (blank if not specified) resource
	 *            to which the file wil be copied
	 * @param irodsTargetFileAbsolutePath
	 *            {@code String} with the absolute path to the iRODS target file or
	 *            collection to which the single file will be copied.
	 * @param transferStatusCallbackListener
	 *            {@link org.irods.jargon.core.transfer.TransferStatusCallbackListener}
	 *            implementation that will receive callbacks of success/failure of
	 *            each individual file transfer. This may be set to {@code null}, in
	 *            which case, exceptions that are thrown will be rethrown by this
	 *            method to the caller.
	 * @param transferControlBlock
	 *            {@link org.irods.jargon.core.transfer.TransferControlBlock}
	 *            implementation that is the communications mechanism between the
	 *            initiator of the transfer and the transfer process. This may be
	 *            set to {@code null} if those facilities are not needed.
	 * @throws JargonException
	 */
	void processCopyOfSingleFile(final String irodsSourceFileAbsolutePath, final String targetResource,
			final String irodsTargetFileAbsolutePath,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock)
			throws OverwriteException, DataNotFoundException, JargonException {

		log.info("processCopyOfSingleFile()");
		log.info("irodsSourceFileAbsolutePath:{}", irodsSourceFileAbsolutePath);
		log.info("targetResource:{}", targetResource);
		log.info("irodsTargetFileAbsolutePath:{}", irodsTargetFileAbsolutePath);

		try {

			if (!transferControlBlock.filter(irodsSourceFileAbsolutePath)) {
				log.info("file is filtered and discarded: {}", irodsTargetFileAbsolutePath);
				transferControlBlock.incrementFilesSkippedSoFar();
				TransferStatus status = TransferStatus.instance(TransferType.COPY, irodsSourceFileAbsolutePath,
						irodsTargetFileAbsolutePath, targetResource, 0, 0,
						transferControlBlock.getTotalFilesTransferredSoFar(),
						transferControlBlock.getTotalFilesSkippedSoFar(),
						transferControlBlock.getTotalFilesToTransfer(), TransferState.RESTARTING,
						dataObjectAO.getIRODSAccount().getHost(), dataObjectAO.getIRODSAccount().getZone());
				transferStatusCallbackListener.statusCallback(status);
				return;
			}

			log.info("filter passed, process...");

			IRODSFile irodsSourceFile = dataObjectAO.getIRODSFileFactory()
					.instanceIRODSFile(irodsSourceFileAbsolutePath);
			IRODSFile irodsTargetFile = dataObjectAO.getIRODSFileFactory()
					.instanceIRODSFile(irodsTargetFileAbsolutePath);
			irodsTargetFile.setResource(targetResource);
			dataObjectAO.copyIRODSDataObject(irodsSourceFile, irodsTargetFile, transferControlBlock,
					transferStatusCallbackListener);
			log.info("copy successful for file: {}", irodsSourceFileAbsolutePath);
			transferControlBlock.incrementFilesTransferredSoFar();

			// I do not track length during a copy
			if (transferStatusCallbackListener != null) {
				TransferStatus transferStatus = TransferStatus.instance(TransferType.COPY, irodsSourceFileAbsolutePath,
						irodsTargetFileAbsolutePath, targetResource, 0, 0,
						transferControlBlock.getTotalFilesTransferredSoFar(),
						transferControlBlock.getTotalFilesSkippedSoFar(),
						transferControlBlock.getTotalFilesToTransfer(), TransferState.SUCCESS,
						dataObjectAO.getIRODSAccount().getHost(), dataObjectAO.getIRODSAccount().getZone());
				transferStatusCallbackListener.statusCallback(transferStatus);
			}

		} catch (JargonException e) {

			if (!transferControlBlock.isCancelled()) {
				// may rethrow or send back to the callback listener
				log.error("exception in transfer", e);

				transferControlBlock.reportErrorInTransfer();

				if (transferStatusCallbackListener != null) {
					log.warn("exception will be passed back to existing callback listener");

					TransferStatus status = TransferStatus.instanceForException(TransferType.COPY,
							irodsSourceFileAbsolutePath, irodsTargetFileAbsolutePath, targetResource, 0L, 0L,
							transferControlBlock.getTotalFilesTransferredSoFar(),
							transferControlBlock.getTotalFilesSkippedSoFar(),
							transferControlBlock.getTotalFilesToTransfer(), e, dataObjectAO.getIRODSAccount().getHost(),
							dataObjectAO.getIRODSAccount().getZone());

					transferStatusCallbackListener.statusCallback(status);

				} else {
					log.warn("exception will be re-thrown, as there is no status callback listener");
					throw e;
				}
			}
		}
	}

}