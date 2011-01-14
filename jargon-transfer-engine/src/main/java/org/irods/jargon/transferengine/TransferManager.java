/**
 * 
 */
package org.irods.jargon.transferengine;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.transfer.DefaultTransferControlBlock;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.transferengine.domain.LocalIRODSTransfer;
import org.irods.jargon.transferengine.domain.LocalIRODSTransferItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central manager of transfer engine. Manages status of entire engine on behalf
 * of client callers. This class is a singleton, and will keep track of the
 * current status of the transfer, receiving callbacks from the various
 * transfers that are underway. Clients can subscribe to this transfer manager
 * for information about the current operations of the transfer engine and
 * receive information about the status and history of the queue.
 * 
 * This manager also is the interface through which transfers may be enqueued by
 * clients,.
 * 
 * This class might evolve into some sort of scheduler, and these capabilities
 * would be exposed here in cooperation with the
 * <code>TransferQueueService</code>
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */

public final class TransferManager {

	public enum ErrorStatus {
		OK, WARNING, ERROR
	}

	public enum RunningStatus {
		IDLE, PROCESSING, PAUSED
	}

	private ErrorStatus errorStatus;
	private RunningStatus runningStatus;
	private final TransferManagerCallbackListener transferManagerCallbackListener;
	private final TransferQueueService transferQueueService;
	private TransferRunner currentTransferRunner = null;

	/**
	 * Indicates whether detailed logging is desired for successful transfers
	 * (might move into a config block later)
	 */
	private boolean logSuccessfulTransfers = true;

	private static final Logger log = LoggerFactory
			.getLogger(TransferManager.class);

	public static TransferManager instance() throws JargonException {
		return new TransferManager(null, true, null);
	}

	/**
	 * Create an instance of a <code>TransferManager</code>. There should only
	 * be one transfer manager per application.
	 * 
	 * @param transferManagerCallbackListener
	 *            implementation of the
	 *            {@link org.irods.jargon.transferengine.TransferManagerCallbackListener}
	 *            class that can receive callbacks from the running transfer
	 *            process.
	 * @return instance of <code>TransferManager</code>
	 * @throws JargonException
	 */
	public static TransferManager instanceWithCallbackListener(
			final TransferManagerCallbackListener transferManagerCallbackListener)
			throws JargonException {
		return instanceWithCallbackListener(transferManagerCallbackListener,
				true);
	}

	/**
	 * Create an instance of a <code>TransferManager</code>. There should only
	 * be one transfer manager per application. This initializer allows
	 * specification of recording of successful transfers. Note that
	 * <code>logSuccessfulTransfers</code> will likely move to a configuration
	 * block in later versions.
	 * 
	 * @param transferManagerCallbackListener
	 *            implementation of the
	 *            {@link org.irods.jargon.transferengine.TransferManagerCallbackListener}
	 *            class that can receive callbacks from the running transfer
	 *            process.
	 * @param logSuccessfulTransfers
	 *            <code>boolean</code> that indicates whether successful
	 *            transfers should be logged to the internal database.
	 * @return instance of <code>TransferManager</code>
	 * @throws JargonException
	 */
	public static TransferManager instanceWithCallbackListener(
			final TransferManagerCallbackListener transferManagerCallbackListener,
			final boolean logSuccessfulTransfers) throws JargonException {
		return new TransferManager(transferManagerCallbackListener,
				logSuccessfulTransfers, null);

	}

	/**
	 * Create an instance of a <code>TransferManager</code>. There should only
	 * be one transfer manager per application. This initializer allows
	 * specification of recording of successful transfers. Note that
	 * <code>logSuccessfulTransfers</code> will likely move to a configuration
	 * block in later versions. This method also will create the transfer
	 * database in the users home directory with the given name
	 * 
	 * @param transferManagerCallbackListener
	 *            implementation of the
	 *            {@link org.irods.jargon.transferengine.TransferManagerCallbackListener}
	 *            class that can receive callbacks from the running transfer
	 *            process.
	 * @param logSuccessfulTransfers
	 *            <code>boolean</code> that indicates whether successful
	 *            transfers should be logged to the internal database.
	 * @param transferDatabaseName
	 *            <code>String</code> with the name of the transfer database.
	 *            The database will be in the .idrop directory under the user
	 *            directory with the given name
	 * @return instance of <code>TransferManager</code>
	 * @throws JargonException
	 */
	public static TransferManager instanceWithCallbackListenerAndUserLevelDatabase(
			final TransferManagerCallbackListener transferManagerCallbackListener,
			final boolean logSuccessfulTransfers,
			final String transferDatabaseName) throws JargonException {
		return new TransferManager(transferManagerCallbackListener,
				logSuccessfulTransfers, transferDatabaseName);

	}

	private TransferManager(
			final TransferManagerCallbackListener transferManagerCallbackListener,
			final boolean logSuccessfulTransfers,
			final String pathToTransferDatabase) throws JargonException {

		this.errorStatus = ErrorStatus.OK;
		this.runningStatus = RunningStatus.IDLE;
		IRODSFileSystem.instance();

		if (pathToTransferDatabase == null || pathToTransferDatabase.isEmpty()) {
			transferQueueService = TransferQueueService.instance();
		} else {
			transferQueueService = TransferQueueService
					.instanceGivingPathToTransferDatabase(pathToTransferDatabase);
		}

		this.logSuccessfulTransfers = logSuccessfulTransfers;

		// callback listener may be null
		this.transferManagerCallbackListener = transferManagerCallbackListener;
		log.info("processing queue at startup");
		transferQueueService.processQueueAtStartup();
	}

	private void publishRunningStatus(final RunningStatus runningStatus)
			throws JargonException {
		if (runningStatus == null) {
			throw new JargonException("null running status");
		}

		if (transferManagerCallbackListener != null) {
			transferManagerCallbackListener
					.transferManagerRunningStatusUpdate(runningStatus);
		}
	}

	private void publishErrorStatus(final ErrorStatus errorStatus)
			throws JargonException {
		if (errorStatus == null) {
			throw new JargonException("null error status");
		}

		if (transferManagerCallbackListener != null) {
			transferManagerCallbackListener
					.transferManagerErrorStatusUpdate(errorStatus);
		}
	}

	public synchronized RunningStatus getRunningStatus() {
		return runningStatus;
	}

	public synchronized boolean isPaused() {
		return runningStatus == RunningStatus.PAUSED;
	}

	/**
	 * Pause the queue.
	 * 
	 * @throws JargonException
	 */
	public synchronized void pause() throws JargonException {
		log.info("pause...");
		if (runningStatus == RunningStatus.PAUSED) {
			log.info("already paused");
		}

		if (currentTransferRunner != null) {
			log.info("pause currently running transfer: {}",
					currentTransferRunner.getLocalIRODSTransfer());
			currentTransferRunner.getTransferControlBlock().setPaused(true);
		}

		runningStatus = RunningStatus.PAUSED;
		publishRunningStatus(runningStatus);
	}

	/**
	 * Restart the queue after being paused, will pick up the next job in the
	 * queue and run it.
	 * 
	 * @throws JargonException
	 */
	public synchronized void resume() throws JargonException {
		log.info("resume...");
		if (runningStatus != RunningStatus.PAUSED) {
			log.info("not paused");
			return;
		}

		log.info("resuming");
		// need to check for pending transfers via database and restart (idle
		// versus processing)
		runningStatus = RunningStatus.IDLE;
		publishRunningStatus(runningStatus);
		processNextInQueueIfIdle();
	}

	public synchronized void notifyWarningCondition() throws JargonException {
		log.info("notify warning");

		if (runningStatus == RunningStatus.PAUSED) {
			return;
		}

		// warning does not override error status
		if (errorStatus != ErrorStatus.ERROR) {
			errorStatus = ErrorStatus.WARNING;
			publishErrorStatus(errorStatus);
		}
	}

	public synchronized void notifyErrorCondition() throws JargonException {

		if (runningStatus == RunningStatus.PAUSED) {
			return;
		}

		log.info("notify error");
		errorStatus = ErrorStatus.ERROR;
		publishErrorStatus(errorStatus);
	}

	public synchronized void notifyOKCondition() throws JargonException {

		if (runningStatus == RunningStatus.PAUSED) {
			return;
		}

		log.info("notify OK");
		errorStatus = ErrorStatus.OK;
		publishErrorStatus(errorStatus);
	}

	public synchronized void notifyProcessing() throws JargonException {

		if (runningStatus == RunningStatus.PAUSED) {
			return;
		}

		log.info("notify as processing");
		runningStatus = RunningStatus.PROCESSING;
		publishRunningStatus(runningStatus);
	}

	public synchronized void notifyEnqueued(
			final LocalIRODSTransfer enqueuedTransfer) {
		log.info("a transfer has been enqueued: {}", enqueuedTransfer);
	}

	public synchronized void notifyComplete() throws JargonException {
		log.info("notifyComplete");

		// if I am paused, then remain paused until resumed
		if (runningStatus != RunningStatus.PAUSED) {
			runningStatus = RunningStatus.IDLE;
			publishRunningStatus(runningStatus);
			processNextInQueueIfIdle();
		}
	}

	public synchronized ErrorStatus getErrorStatus() {
		return errorStatus;
	}

	/**
	 * Purge all transfers in the queue that are not marked as 'PROCESSING' and
	 * resets the error status
	 * 
	 * @throws JargonException
	 */
	public synchronized void purgeAllTransfers() throws JargonException {
		transferQueueService.purgeQueue();
		resetStatus();
	}

	/**
	 * Purge all transfers in the queue that are marked as 'COMPLETE' and have
	 * no errors
	 * 
	 * @throws JargonException
	 */
	public synchronized void purgeSuccessfulTransfers() throws JargonException {
		transferQueueService.purgeSuccessful();
		resetStatus();
	}

	public void enqueueAPut(final String sourceAbsolutePath,
			final String targetAbsolutePath, final String resource,
			final IRODSAccount irodsAccount) throws JargonException {

		if (sourceAbsolutePath == null || sourceAbsolutePath.isEmpty()) {
			throw new JargonException("sourceAbsolutePath is null or empty");
		}

		if (targetAbsolutePath == null || targetAbsolutePath.isEmpty()) {
			throw new JargonException("targetAbsolutePath is null or empty");
		}

		if (resource == null) {
			throw new JargonException(
					"resource is null, set to blank if default is desired");
		}

		if (irodsAccount == null) {
			throw new JargonException("irodsAccount is null");
		}

		log.info("enquing a put transfer");
		log.info("   sourceAbsolutePath: {}", sourceAbsolutePath);
		log.info("   targetAbsolutePath: {}", targetAbsolutePath);
		log.info("   resource: {}", resource);
		log.info("   irodsAccount: {}", irodsAccount);

		synchronized (this) {

			transferQueueService.enqueuePutTransfer(sourceAbsolutePath,
					targetAbsolutePath, resource, irodsAccount);

			log.info("enqueue of put, current running status: {}",
					runningStatus);
			log.info("current error status:{}", errorStatus);

			processNextInQueueIfIdle();
		}

	}

	public void enqueueAGet(final String irodsSourceAbsolutePath,
			final String targetLocalAbsolutePath, final String resource,
			final IRODSAccount irodsAccount) throws JargonException {

		if (irodsSourceAbsolutePath == null
				|| irodsSourceAbsolutePath.isEmpty()) {
			throw new JargonException(
					"irodsSourceAbsolutePath is null or empty");
		}

		if (targetLocalAbsolutePath == null
				|| targetLocalAbsolutePath.isEmpty()) {
			throw new JargonException(
					"targetLocalAbsolutePath is null or empty");
		}

		if (resource == null) {
			throw new JargonException(
					"resource is null, set to blank if default is desired");
		}

		if (irodsAccount == null) {
			throw new JargonException("irodsAccount is null");
		}

		log.info("enquing a get transfer");
		log.info("   irodsSourceAbsolutePath: {}", irodsSourceAbsolutePath);
		log.info("   targetLocalAbsolutePath: {}", targetLocalAbsolutePath);
		log.info("   resource: {}", resource);
		log.info("   irodsAccount: {}", irodsAccount);

		synchronized (this) {

			transferQueueService.enqueueGetTransfer(irodsSourceAbsolutePath,
					targetLocalAbsolutePath, resource, irodsAccount);

			log.info("enqueue of get, current running status: {}",
					runningStatus);
			log.info("current error status:{}", errorStatus);

			processNextInQueueIfIdle();
		}

	}

	/**
	 * Put a replication operation into the processing queue. This will be
	 * processed according to the schedule of the transfer engine, and may not
	 * be immediate. If the queue is idle, the tranfer will be launched
	 * immediately.
	 * 
	 * @param irodsAbsolutePath
	 *            <code>String</code> with the absolute path to the iRODS file
	 *            or collection to replicate
	 * @param targetResource
	 *            <code>String</code> with the name of the resource to which to
	 *            replicate. Blank will cause iRODS to use configured defaults.
	 *            If such a default is not configured, an iRODS error may occur.
	 * @param irodsAccount
	 *            <code>IRODSAccount</code> with information on the zone to
	 *            which the command will be sent.
	 * @throws JargonException
	 */
	public void enqueueAReplicate(final String irodsAbsolutePath,
			final String targetResource, final IRODSAccount irodsAccount)
			throws JargonException {

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new JargonException("irodsAbsolutePath is null or empty");
		}

		if (targetResource == null) {
			throw new JargonException(
					"targetResource is null, set to blank if default is desired");
		}

		if (irodsAccount == null) {
			throw new JargonException("irodsAccount is null");
		}

		log.info("enquing a replicate transfer");
		log.info("   irodsAbsolutePath: {}", irodsAbsolutePath);
		log.info("   targetResource: {}", targetResource);
		log.info("   irodsAccount: {}", irodsAccount);

		synchronized (this) {

			transferQueueService.enqueueReplicateTransfer(irodsAbsolutePath,
					targetResource, irodsAccount);

			processNextInQueueIfIdle();
		}

	}

	/**
	 * This method can be called to trigger processing of the next queued item
	 * using the criteria inside of the transfer manager. This method will check
	 * the idle and pause status of the queue.
	 * 
	 * @throws JargonException
	 */
	public synchronized void processNextInQueueIfIdle() throws JargonException {
		log.info("process next in queue");

		if (runningStatus == RunningStatus.IDLE) {
			log.info("queue is idle, go ahead and process next");
			dequeueNextAndLaunchTransferRunner();
		} else {
			log.info("not idle, do not process");
		}
	}

	private void dequeueNextAndLaunchTransferRunner() throws JargonException {
		log.info("process next in queue");

		if (this.isPaused()) {
			log.info("I am paused, don't process");
			return;
		}

		LocalIRODSTransfer dequeued = transferQueueService.dequeueTransfer();

		if (dequeued == null) {
			log.info("queue is empty");
			runningStatus = RunningStatus.IDLE;
			publishRunningStatus(runningStatus);
			return;
		}

		errorStatus = ErrorStatus.OK;
		publishErrorStatus(errorStatus);
		runningStatus = RunningStatus.PROCESSING;
		publishRunningStatus(runningStatus);

		// last successful path will have a restart value or be blank
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance(dequeued.getLastSuccessfulPath());
		
		log.info(">>>> dequeue {}", dequeued);

		log.info("getting a transferRunner to process");
		currentTransferRunner = new TransferRunner(this, dequeued,
				transferControlBlock, transferQueueService);
		final Thread transferThread = new Thread(currentTransferRunner);
		log.info("launching transfer thread");
		transferThread.start();

		log.info(
				">>>>transfer runner is launched to handle dequeued transfer:{}",
				currentTransferRunner);
	}

	/**
	 * Get the list of transfers that are currently in the queue available for
	 * processing
	 * 
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.transferengine.domain.LocalIRODSTransfer}
	 * @throws JargonException
	 */
	public List<LocalIRODSTransfer> getCurrentQueue() throws JargonException {
		return transferQueueService.getCurrentQueue();
	}

	/**
	 * Get the list of transfers that were recently added, including enqueued,
	 * processing, error, etc
	 * 
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.transferengine.domain.LocalIRODSTransfer}
	 * @throws JargonException
	 */
	public List<LocalIRODSTransfer> getRecentQueue() throws JargonException {
		return transferQueueService.getRecentQueue();
	}

	/**
	 * Get the list of transfers that were recently added, including enqueued,
	 * processing, error, etc
	 * 
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.transferengine.domain.LocalIRODSTransfer}
	 * @throws JargonException
	 */
	public List<LocalIRODSTransfer> getErrorQueue() throws JargonException {
		return transferQueueService.getErrorQueue();
	}

	/**
	 * Get the list of transfers that have a status of wraning
	 * 
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.transferengine.domain.LocalIRODSTransfer}
	 * @throws JargonException
	 */
	public List<LocalIRODSTransfer> getWarningQueue() throws JargonException {
		return transferQueueService.getWarningQueue();
	}

	public TransferManagerCallbackListener getTransferManagerCallbackListener() {
		return transferManagerCallbackListener;
	}

	public synchronized void setErrorStatus(final ErrorStatus errorStatus) {
		this.errorStatus = errorStatus;
	}

	public synchronized void setRunningStatus(final RunningStatus runningStatus) {
		this.runningStatus = runningStatus;
	}

	/**
	 * This method will reset the error status of the transfer engine if not
	 * currently processing and notify any listeners. This would be done when
	 * the queue is purged so that purged errors are no longer reflected.
	 */
	public synchronized void resetStatus() throws JargonException {
		if (this.getRunningStatus() == RunningStatus.IDLE
				|| this.getRunningStatus() == RunningStatus.PAUSED) {
			this.notifyOKCondition();

		}
	}

	/**
	 * Get a list of all of the transfer items for the given transfer. This
	 * particular method will return all transfer items, and other methods exist
	 * to filter for errors and other attributes.
	 * 
	 * @param localIRODSTransferId
	 *            <code>Long</code> with the key of the given transfer
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.transferengine.LocalIRODSTransferItem}
	 *         with query results.
	 * @throws JargonException
	 */
	public List<LocalIRODSTransferItem> getAllTransferItemsForTransfer(
			final Long localIRODSTransferId) throws JargonException {
		return transferQueueService
				.getAllTransferItemsForTransfer(localIRODSTransferId);
	}

	/**
	 * Get a list of all of the transfer items for the given transfer that are
	 * an error.
	 * 
	 * @param localIRODSTransferId
	 *            <code>Long</code> with the key of the given transfer
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.transferengine.LocalIRODSTransferItem}
	 *         with query results.
	 * @throws JargonException
	 */
	public List<LocalIRODSTransferItem> getErrorTransferItemsForTransfer(
			final Long localIRODSTransferId) throws JargonException {
		return transferQueueService
				.getErrorTransferItemsForTransfer(localIRODSTransferId);
	}

	/**
	 * Restart a transfer, this will consult the last good file and begin the
	 * transfer after that last noted location
	 * 
	 * @param localIRODSTransfer
	 * @throws JargonException
	 */
	public void restartTransfer(final LocalIRODSTransfer localIRODSTransfer)
			throws JargonException {
		log.info("restarting:{}", localIRODSTransfer);
		synchronized (this) {
			transferQueueService.restartTransfer(localIRODSTransfer);
			this.processNextInQueueIfIdle();
		}
	}

	/**
	 * Resubmit a transfer, this will cause the transfer to be restarted from
	 * the beginning and will clear the status of individually transmitted items
	 * 
	 * @param localIRODSTransfer
	 * @throws JargonException
	 */
	public void resubmitTransfer(final LocalIRODSTransfer localIRODSTransfer)
			throws JargonException {
		log.info("restarting:{}", localIRODSTransfer);
		synchronized (this) {
			transferQueueService.resubmitTransfer(localIRODSTransfer);
			this.processNextInQueueIfIdle();
		}
	}

	/**
	 * Mark a transfer as cancelled. Note that a completed transfer will be
	 * untouched.
	 * 
	 * @param localIRODSTransfer
	 * @throws JargonException
	 */
	public void cancelTransfer(final LocalIRODSTransfer localIRODSTransfer)
			throws JargonException {
		// see if this is the current transfer, and cancel the running transfer
		// if necessary
		synchronized (this) {

			if (currentTransferRunner == null) {
				log.info("no running transfer to cancel");
				return;
			}

			// if the current transfer equals the given transfer, then send a
			// cancel
			LocalIRODSTransfer runningTransfer = currentTransferRunner
					.getLocalIRODSTransfer();
			log.info("cancel sent for:{}", localIRODSTransfer);
			log.info("currently running transfer: {}", runningTransfer);

			log.info("current txfr id:{}", runningTransfer.getId());
			log.info("txr to cancel id: {}", localIRODSTransfer.getId());
			log.info("are these equal: {}",
					runningTransfer.getId().equals(localIRODSTransfer.getId()));

			if (runningTransfer.getId().equals(localIRODSTransfer.getId())) {
				log.info("cancelling running transfer:{}", runningTransfer);
				currentTransferRunner.getTransferControlBlock().setCancelled(
						true);
			} else {
				log.info("the given transfer not running, will just set the status to cancelled");
				transferQueueService.setTransferAsCancelled(localIRODSTransfer);
			}
		}

	}

	public boolean isLogSuccessfulTransfers() {
		return logSuccessfulTransfers;
	}

	public void setLogSuccessfulTransfers(final boolean logSuccessfulTransfers) {
		this.logSuccessfulTransfers = logSuccessfulTransfers;
	}

	protected void notifyStatusUpdate(final TransferStatus transferStatus)
			throws JargonException {
		if (transferStatus == null) {
			throw new JargonException("null transfer status");
		}

		if (transferManagerCallbackListener != null) {
			transferManagerCallbackListener
					.transferStatusCallback(transferStatus);
		}
	}

	public TransferQueueService getTransferQueueService() {
		return transferQueueService;
	}

}
